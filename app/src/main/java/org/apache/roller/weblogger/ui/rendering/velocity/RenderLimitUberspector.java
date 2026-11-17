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
import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.util.introspection.AbstractChainableUberspector;
import org.apache.velocity.util.introspection.Info;
import org.apache.velocity.util.introspection.VelMethod;
import org.apache.velocity.util.introspection.VelPropertyGet;

/**
 * Chained after SecureUberspector, this keeps method calls from templates
 * inside the render limits: each call and property read checks the deadline,
 * a string a call returns may be no longer than the output limit, and
 * String.repeat, which can build a huge string in one call, is not offered.
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
        if (obj instanceof String && "repeat".equals(methodName)) {
            LOG.debug("Refusing String.repeat at " + i);
            return null;
        }
        VelMethod method = super.getMethod(obj, methodName, args, i);
        return method == null ? null : new LimitedMethod(method);
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
            return checked(method.invoke(o, params));
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
