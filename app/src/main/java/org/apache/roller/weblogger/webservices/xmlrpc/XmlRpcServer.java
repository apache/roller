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

package org.apache.roller.weblogger.webservices.xmlrpc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Runs XML-RPC calls against handler classes.
 *
 * <p>Each handler is registered under a prefix. Its public, non-static
 * methods that return a value, including inherited ones, are callable as
 * <code>prefix.methodName</code>, as with the Apache XML-RPC library's
 * property handler mapping. A new handler instance serves each call. When a
 * name is overloaded, the method whose parameter types fit the call's values
 * is used: int and Integer take an XML-RPC int, boolean a boolean, Hashtable
 * or Map a struct, and Object[], List or Vector an array.
 *
 * <p>A handler's {@link XmlRpcException} becomes a fault with its code and
 * message. Any other failure is logged and answered with a generic fault, so
 * internal details stay on the server.
 */
final class XmlRpcServer {

    private static final Log log = LogFactory.getLog(XmlRpcServer.class);

    /** Marks a value that cannot be passed as a given parameter type. */
    private static final Object NO_MATCH = new Object();

    private record Target(Class<?> handlerClass, List<Method> methods) {
    }

    private final Map<String, Target> targets = new HashMap<>();

    /**
     * @param handlers handler classes by prefix; each needs a public no-argument constructor
     */
    XmlRpcServer(Map<String, Class<?>> handlers) {
        for (Map.Entry<String, Class<?>> handler : handlers.entrySet()) {
            Map<String, List<Method>> byName = new HashMap<>();
            for (Method method : handler.getValue().getMethods()) {
                if (method.getDeclaringClass() == Object.class
                        || Modifier.isStatic(method.getModifiers())
                        || method.getReturnType() == void.class) {
                    continue;
                }
                byName.computeIfAbsent(method.getName(), k -> new ArrayList<>()).add(method);
            }
            for (Map.Entry<String, List<Method>> named : byName.entrySet()) {
                List<Method> methods = named.getValue();
                // getMethods() has no defined order; keep overload resolution stable
                methods.sort(Comparator.comparing(Method::toGenericString));
                targets.put(handler.getKey() + "." + named.getKey(),
                        new Target(handler.getValue(), List.copyOf(methods)));
            }
        }
    }

    /** Runs the call in an XML-RPC request body and returns the response document. */
    byte[] execute(byte[] requestBody) {
        Object result;
        try {
            XmlRpcRequest request = XmlRpcRequestParser.parse(requestBody);
            if (log.isDebugEnabled()) {
                // never the parameters: they carry the caller's password
                log.debug("XML-RPC call to " + request.methodName());
            }
            result = invoke(request.methodName(), request.params());
        } catch (XmlRpcException e) {
            return XmlRpcResponseWriter.fault(e.code, e.getFaultString());
        } catch (RuntimeException e) {
            // Nothing a request does should reach the container as an
            // unhandled exception: its error page would show the message.
            log.error("XML-RPC request failed unexpectedly", e);
            return unknownFault();
        }
        try {
            return XmlRpcResponseWriter.response(result);
        } catch (IllegalArgumentException e) {
            log.error("Could not write the XML-RPC response", e);
            return unknownFault();
        }
    }

    Object invoke(String methodName, List<Object> params) throws XmlRpcException {
        Target target = targets.get(methodName);
        if (target == null) {
            throw new XmlRpcException("No such handler: " + methodName);
        }
        for (Method method : target.methods()) {
            Object[] args = arguments(method.getParameterTypes(), params);
            if (args == null) {
                continue;
            }
            Object result;
            try {
                Object handler = target.handlerClass().getDeclaredConstructor().newInstance();
                result = method.invoke(handler, args);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof XmlRpcException) {
                    throw (XmlRpcException) cause;
                }
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                log.error("XML-RPC method " + methodName + " failed", cause);
                throw unknownException();
            } catch (ReflectiveOperationException e) {
                log.error("Could not call XML-RPC method " + methodName, e);
                throw unknownException();
            }
            if (result == null) {
                log.error("XML-RPC method " + methodName + " returned null");
                throw unknownException();
            }
            return result;
        }
        throw new XmlRpcException("No method matching arguments: " + methodName);
    }

    private static Object[] arguments(Class<?>[] types, List<Object> params) {
        if (types.length != params.size()) {
            return null;
        }
        Object[] args = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            Object arg = convert(types[i], params.get(i));
            if (arg == NO_MATCH) {
                return null;
            }
            args[i] = arg;
        }
        return args;
    }

    private static Object convert(Class<?> type, Object value) {
        if (type == String.class) {
            return value instanceof String ? value : NO_MATCH;
        }
        if (type == int.class || type == Integer.class) {
            return value instanceof Integer ? value : NO_MATCH;
        }
        if (type == boolean.class || type == Boolean.class) {
            return value instanceof Boolean ? value : NO_MATCH;
        }
        if (type == double.class || type == Double.class) {
            return value instanceof Double ? value : NO_MATCH;
        }
        if (type == Date.class) {
            return value instanceof Date ? value : NO_MATCH;
        }
        if (type == byte[].class) {
            return value instanceof byte[] ? value : NO_MATCH;
        }
        if (type == Object[].class) {
            return value instanceof Object[] ? value : NO_MATCH;
        }
        if (type == Hashtable.class) {
            return value instanceof Map ? new Hashtable<>((Map<?, ?>) value) : NO_MATCH;
        }
        if (type == Map.class || type == LinkedHashMap.class) {
            return value instanceof Map ? value : NO_MATCH;
        }
        if (type == List.class || type == ArrayList.class) {
            return value instanceof Object[] ? new ArrayList<>(Arrays.asList((Object[]) value)) : NO_MATCH;
        }
        if (type == Vector.class) {
            return value instanceof Object[] ? new Vector<>(Arrays.asList((Object[]) value)) : NO_MATCH;
        }
        if (type == Object.class) {
            return value;
        }
        return NO_MATCH;
    }

    private static XmlRpcException unknownException() {
        return new XmlRpcException(BaseAPIHandler.UNKNOWN_EXCEPTION, BaseAPIHandler.UNKNOWN_EXCEPTION_MSG);
    }

    private static byte[] unknownFault() {
        return XmlRpcResponseWriter.fault(BaseAPIHandler.UNKNOWN_EXCEPTION, BaseAPIHandler.UNKNOWN_EXCEPTION_MSG);
    }
}
