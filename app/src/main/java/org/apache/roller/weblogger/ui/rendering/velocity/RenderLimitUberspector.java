/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.rendering.model.UtilitiesModel;
import org.apache.velocity.util.introspection.AbstractChainableUberspector;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;

/**
 * Chained after SecureUberspector, this keeps method calls from templates
 * inside the render limits: each call and property read checks the deadline,
 * a string a call returns may be no longer than the output limit, a call that
 * could build a longer string is stopped before it runs, and string methods
 * whose result can't be bounded from the call (String.repeat, String.format
 * and others; see StringGrowth) are not offered. Regular expressions given to
 * String methods check the deadline as they run; see DeadlineRegex.
 */
public class RenderLimitUberspector extends AbstractChainableUberspector {

    private static final Log LOG = LogFactory.getLog(RenderLimitUberspector.class);

    @Override
    public Iterator getIterator(Object obj, Info i) {
        RenderSession.checkDeadline();
        return super.getIterator(obj, i);
    }

    @Override
    public VelMethod getMethod(Object obj, String methodName, Object[] args, Info i) {
        VelMethod method = super.getMethod(obj, methodName, args, i);
        if (method == null) {
            return null;
        }
        if (StringGrowth.isRefused(obj, methodName, method.getReturnType())) {
            LOG.debug("Refusing " + obj.getClass().getSimpleName() + "." + methodName
                    + " at " + i);
            return null;
        }
        return new LimitedMethod(method);
    }

    @Override
    public VelPropertyGet getPropertyGet(Object obj, String identifier, Info i) {
        VelPropertyGet getter = super.getPropertyGet(obj, identifier, i);
        return getter == null ? null : new LimitedPropertyGet(getter);
    }

    private static Object checked(Object result) {
        if (result instanceof CharSequence) {
            RenderSession.checkLength(((CharSequence) result).length());
        }
        return result;
    }

    private static final class LimitedMethod implements VelMethod {

        private final VelMethod method;

        LimitedMethod(VelMethod method) {
            this.method = method;
        }

        @Override
        public Object invoke(Object o, Object[] params)
                throws IllegalAccessException, InvocationTargetException {
            RenderSession.checkDeadline();
            RenderSession session = RenderSession.current();
            if (session == null) {
                return method.invoke(o, params);
            }
            String name = method.getMethodName();
            Object[] args = params == null ? new Object[0] : params;

            // before the call: what it can build or take has to fit
            RenderSession.checkLength(StringGrowth.maxResultLength(o, name, args));
            StringGrowth.checkPieces(o, name, args);
            ValueLimits.checkNumberCall(o, name, args);
            boolean container = o instanceof Collection || o instanceof Map;
            if (container) {
                ValueLimits.checkContainerCall(o, name, args, session.getMaxOutputChars());
            }
            long sizeBefore = ValueLimits.size(o);
            boolean builder = o instanceof StringBuilder || o instanceof StringBuffer;
            long lengthBefore = builder ? ((CharSequence) o).length() : 0;

            Object result = DeadlineRegex.NOT_HANDLED;
            if (o instanceof String) {
                try {
                    result = DeadlineRegex.invoke((String) o, name, args);
                } catch (RenderLimitException ex) {
                    throw ex;
                } catch (RuntimeException ex) {
                    // reported as a template error, as the String method's own is
                    throw new InvocationTargetException(ex);
                }
            }
            if (result == DeadlineRegex.NOT_HANDLED) {
                result = method.invoke(o, params);
            }

            // after the call: count what it built toward the render's budget
            checked(result);
            ValueLimits.checkResult(result);
            if (container) {
                RenderSession.chargeItems(ValueLimits.size(o) - sizeBefore);
            } else if (builder) {
                RenderSession.chargeBuilt(((CharSequence) o).length() - lengthBefore);
            } else if (o instanceof String || o instanceof UtilitiesModel) {
                if (result instanceof CharSequence && result != o) {
                    RenderSession.chargeBuilt(((CharSequence) result).length());
                } else if (result instanceof Object[]) {
                    RenderSession.chargeItems(((Object[]) result).length);
                }
            }
            return result;
        }

        @Override
        public boolean isCacheable() {
            return method.isCacheable();
        }

        @Override
        public String getMethodName() {
            return method.getMethodName();
        }

        @Override
        public Method getMethod() {
            return method.getMethod();
        }

        @Override
        public Class<?> getReturnType() {
            return method.getReturnType();
        }
    }

    private static final class LimitedPropertyGet implements VelPropertyGet {

        private final VelPropertyGet getter;

        LimitedPropertyGet(VelPropertyGet getter) {
            this.getter = getter;
        }

        @Override
        public Object invoke(Object o) throws Exception {
            RenderSession.checkDeadline();
            return checked(getter.invoke(o));
        }

        @Override
        public boolean isCacheable() {
            return getter.isCacheable();
        }

        @Override
        public String getMethodName() {
            return getter.getMethodName();
        }
    }
}
