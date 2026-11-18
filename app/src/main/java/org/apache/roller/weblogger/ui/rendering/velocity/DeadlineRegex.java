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

import java.util.regex.Pattern;

/**
 * Runs the String methods that take a regular expression (matches,
 * replaceAll, replaceFirst and split) over text that checks the render
 * deadline as the expression reads it, so a slow expression stops with the
 * render instead of holding the thread. Each gives the same result as the
 * String method; see String's documentation for the Pattern call each one is
 * equivalent to.
 */
final class DeadlineRegex {

    /** Returned when the call isn't one of these methods. */
    static final Object NOT_HANDLED = new Object();

    private DeadlineRegex() {
    }

    static Object invoke(String s, String methodName, Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof String)) {
            return NOT_HANDLED;
        }
        String regex = (String) args[0];
        CharSequence text = new DeadlineText(s);
        switch (methodName) {
            case "matches":
                return args.length == 1 ? Pattern.compile(regex).matcher(text).matches()
                        : NOT_HANDLED;
            case "replaceAll":
                return args.length == 2 && args[1] instanceof String
                        ? Pattern.compile(regex).matcher(text).replaceAll((String) args[1])
                        : NOT_HANDLED;
            case "replaceFirst":
                return args.length == 2 && args[1] instanceof String
                        ? Pattern.compile(regex).matcher(text).replaceFirst((String) args[1])
                        : NOT_HANDLED;
            case "split":
                if (args.length == 1) {
                    return Pattern.compile(regex).split(text, 0);
                }
                return args.length == 2 && args[1] instanceof Integer
                        ? Pattern.compile(regex).split(text, (Integer) args[1])
                        : NOT_HANDLED;
            default:
                return NOT_HANDLED;
        }
    }

    /** A string that checks the deadline every few thousand character reads. */
    private static final class DeadlineText implements CharSequence {

        private final String s;
        private int reads = 0;

        DeadlineText(String s) {
            this.s = s;
        }

        @Override
        public char charAt(int index) {
            if ((++reads & 0xfff) == 0) {
                RenderSession.checkDeadline();
            }
            return s.charAt(index);
        }

        @Override
        public int length() {
            return s.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            return s.subSequence(start, end);
        }

        @Override
        public String toString() {
            return s;
        }
    }
}
