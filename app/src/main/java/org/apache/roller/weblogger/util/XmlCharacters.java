/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.util;

/**
 * Text that Roller writes into XML documents it generates itself.
 */
public final class XmlCharacters {

    private XmlCharacters() {
    }

    /**
     * Drops the characters that XML 1.0 does not allow in a document (most
     * control characters, U+FFFE, U+FFFF and unpaired surrogates). Stored
     * text can contain them, and a single one makes the whole response
     * unreadable for a client.
     *
     * @return the text without those characters, or null for null
     */
    public static String withoutIllegalCharacters(String text) {
        if (text == null) {
            return null;
        }
        StringBuilder out = null;
        int i = 0;
        while (i < text.length()) {
            int cp = text.codePointAt(i);
            int len = Character.charCount(cp);
            boolean legal = cp == 0x9 || cp == 0xA || cp == 0xD
                    || (cp >= 0x20 && cp <= 0xD7FF)
                    || (cp >= 0xE000 && cp <= 0xFFFD)
                    || (cp >= 0x10000 && cp <= 0x10FFFF);
            if (!legal && out == null) {
                out = new StringBuilder(text.length()).append(text, 0, i);
            } else if (legal && out != null) {
                out.appendCodePoint(cp);
            }
            i += len;
        }
        return out == null ? text : out.toString();
    }
}
