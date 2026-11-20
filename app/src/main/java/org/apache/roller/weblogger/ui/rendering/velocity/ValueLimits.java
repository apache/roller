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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Bounds for the values other than strings that a template works with: the
 * text of a list or map, the lists and maps it grows, and the big numbers
 * Velocity makes when arithmetic overflows a long. One call on these can take
 * more memory or time than a whole page, and a call can't be stopped once it
 * has started, so each is checked before it runs.
 */
final class ValueLimits {

    /** Most bits of a number a template may make. */
    static final long MAX_NUMBER_BITS = 100_000;

    /** Most element comparisons one containsAll, removeAll or retainAll may make. */
    static final long MAX_COMPARISONS = 10_000_000;

    private ValueLimits() {
    }

    /** A list or map being walked by textLength. */
    private static final class Frame {
        final Object container;
        final Iterator<?> items;
        boolean first = true;

        Frame(Object container) {
            this.container = container;
            this.items = container instanceof Map
                    ? ((Map<?, ?>) container).entrySet().iterator()
                    : ((Collection<?>) container).iterator();
        }
    }

    /**
     * @return the length of String.valueOf(value), or a number above limit
     *         once it is known to be longer. A list or map is walked element
     *         by element instead of being turned into text, and the walk
     *         stops at the limit, so a huge or deeply nested one is cheap to
     *         check.
     */
    static long textLength(Object value, long limit) {
        if (value instanceof Map.Entry) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) value;
            long key = textLength(entry.getKey(), limit);
            return key > limit ? key : 1 + key + textLength(entry.getValue(), limit - key);
        }
        if (!isContainer(value)) {
            return leafLength(value);
        }
        Deque<Frame> walks = new ArrayDeque<>();
        walks.push(new Frame(value));
        // "[]" or "{}"
        long length = 2;
        while (!walks.isEmpty() && length <= limit) {
            Frame walk = walks.peek();
            if (!walk.items.hasNext()) {
                walks.pop();
                continue;
            }
            if (!walk.first) {
                // ", "
                length += 2;
            }
            walk.first = false;
            Object item = walk.items.next();
            if (walk.container instanceof Map) {
                Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
                // key=value
                length += 1 + visit(entry.getKey(), walk, walks)
                        + visit(entry.getValue(), walk, walks);
            } else {
                length += visit(item, walk, walks);
            }
        }
        return length;
    }

    /** @return the length of item, or 2 for the brackets of a container it starts walking */
    private static long visit(Object item, Frame walk, Deque<Frame> walks) {
        if (item == walk.container) {
            // how AbstractCollection and AbstractMap print themselves
            return item instanceof Map ? "(this Map)".length() : "(this Collection)".length();
        }
        if (isContainer(item)) {
            walks.push(new Frame(item));
            return 2;
        }
        if (item instanceof Map.Entry) {
            // key=value, as a map's entries print
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) item;
            return 1 + visit(entry.getKey(), walk, walks) + visit(entry.getValue(), walk, walks);
        }
        return leafLength(item);
    }

    private static boolean isContainer(Object value) {
        return value instanceof Collection || value instanceof Map;
    }

    /** @return true if the value's text is built from a list or map it holds */
    static boolean holdsValues(Object value) {
        return isContainer(value) || value instanceof Map.Entry;
    }

    private static long leafLength(Object value) {
        return value instanceof CharSequence ? ((CharSequence) value).length()
                : String.valueOf(value).length();
    }

    /** @return the number of elements of a list or map, or 0 for anything else */
    static long size(Object value) {
        if (value instanceof Collection) {
            return ((Collection<?>) value).size();
        }
        return value instanceof Map ? ((Map<?, ?>) value).size() : 0;
    }

    /**
     * Checks a call on a list or map before it runs: the items addAll or
     * putAll may add, or toArray or clone may copy, have to fit the render's
     * budget, a comparison of every element with every other has to stay
     * small, and the text of toString has to fit the output limit.
     */
    static void checkContainerCall(Object receiver, String methodName, Object[] args,
            long maxChars) {
        long receiverSize = size(receiver);
        boolean adds = "addAll".equals(methodName) || "putAll".equals(methodName);
        long added = 0;
        for (Object arg : args) {
            long argSize = size(arg);
            if (adds) {
                added += argSize;
            }
            boolean compares = "containsAll".equals(methodName) && !(receiver instanceof Set)
                    || ("removeAll".equals(methodName) || "retainAll".equals(methodName))
                    && !(arg instanceof Set);
            if (compares && receiverSize > 0 && argSize > MAX_COMPARISONS / receiverSize) {
                throw new RenderLimitException(methodName + " would compare more than "
                        + MAX_COMPARISONS + " pairs of elements");
            }
        }
        if (isCopy(methodName)) {
            added += receiverSize;
        }
        RenderSession.checkItems(added);
        if ("toString".equals(methodName) && args.length == 0) {
            RenderSession.checkLength(textLength(receiver, maxChars));
        }
    }

    /** @return true for the calls that return a copy of a list or map's items */
    static boolean isCopy(String methodName) {
        return "toArray".equals(methodName) || "clone".equals(methodName);
    }

    /**
     * Checks a call on a big number before it runs, for the calls whose
     * result can be far larger than the number: pow, the shifts and the bit
     * setters. Primality tests are refused, as they can take minutes on a
     * number that fits the limit.
     */
    static void checkNumberCall(Object receiver, String methodName, Object[] args) {
        if (receiver instanceof BigInteger) {
            BigInteger n = (BigInteger) receiver;
            long arg = args.length == 1 && args[0] instanceof Number
                    ? ((Number) args[0]).longValue() : 0;
            long bits;
            switch (methodName) {
                case "pow":
                    bits = multiply(n.bitLength(), Math.max(arg, 0));
                    break;
                case "shiftLeft":
                    bits = n.bitLength() + Math.max(arg, 0);
                    break;
                case "shiftRight":
                    bits = n.bitLength() - Math.min(arg, 0);
                    break;
                case "setBit":
                case "flipBit":
                    bits = Math.max(n.bitLength(), arg + 1);
                    break;
                case "isProbablePrime":
                case "nextProbablePrime":
                    throw new RenderLimitException(methodName + " is not available to templates");
                default:
                    bits = 0;
            }
            checkBits(bits);
        } else if (receiver instanceof BigDecimal) {
            BigDecimal d = (BigDecimal) receiver;
            long arg = args.length > 0 && args[0] instanceof Number
                    ? Math.abs(((Number) args[0]).longValue()) : 0;
            // a scale far from zero is cheap to hold, but these write it out
            // in full, at a little over three bits a digit
            long scale = Math.abs((long) d.scale());
            switch (methodName) {
                case "pow":
                    checkBits(multiply(bits(d), arg));
                    break;
                case "toPlainString":
                case "toBigInteger":
                case "toBigIntegerExact":
                    checkBits(bits(d) + 4 * scale);
                    break;
                case "setScale":
                case "movePointLeft":
                case "movePointRight":
                case "scaleByPowerOfTen":
                    checkBits(bits(d) + 4 * (scale + arg));
                    break;
                default:
                    break;
            }
        }
    }

    /** Throws if a number a template made is larger than MAX_NUMBER_BITS. */
    static void checkResult(Object result) {
        if (result instanceof BigInteger || result instanceof BigDecimal) {
            checkBits(bits((Number) result));
        }
    }

    /** @return about how many bits a number holds */
    static long bits(Number n) {
        if (n instanceof BigInteger) {
            return ((BigInteger) n).bitLength();
        }
        if (n instanceof BigDecimal) {
            // a little over three bits a digit
            return ((BigDecimal) n).precision() * 4L;
        }
        return Long.SIZE;
    }

    static void checkBits(long bits) {
        if (bits > MAX_NUMBER_BITS) {
            throw new RenderLimitException("a number would have more than "
                    + MAX_NUMBER_BITS + " bits");
        }
    }

    private static long multiply(long a, long b) {
        if (a == 0 || b == 0) {
            return 0;
        }
        return a > Long.MAX_VALUE / b ? Long.MAX_VALUE : a * b;
    }
}
