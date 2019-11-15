/*
 * Copyright 1999,2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.tags;

import javax.servlet.jsp.JspException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Code grabbed from retired Jakarta Tablibs project: http://jakarta.apache.org/taglibs/string/
 *
 * Word-wrap a String. This involves formatting a long
 * String to fit within a certain character width of page.
 * A delimiter may be passed in to put at the end of each
 * line and a splitting character can be specified for when
 * a word has to be cut in half.
 *
 * <dl>
 * <dt>delimiter</dt><dd>
 *             Character to put between each line.
 *             Default is a newline character.
 * </dd>
 * <dt>width</dt><dd>
 *             Width to word wrap to.
 *             Default is 80.
 * </dd>
 * <dt>split</dt><dd>
 *             Character to use when a word has to be split.
 *             Default is a - character.
 * </dd>
 * <dt>delimiterInside</dt><dd>
 *             Flag indicating if the delimiter should be included in chunk before length reaches width.
 *             Default is true.
 * </dd>
 * </dl>
 *
 */
public class WordWrapTag extends StringTagSupport {

    private String delimiter;
    private String width;
    private String split;
    private boolean delimiterInside;

    public WordWrapTag() {
        super();
    }

    /**
     * Get the width property
     *
     * @return String property
     */
    public String getWidth() {
        return this.width;
    }

    /**
     * Set the width property
     *
     * @param width String property
     */
    public void setWidth(String width) {
        this.width = width;
    }


    /**
     * Get the delimiter property
     *
     * @return String property
     */
    public String getDelimiter() {
        return this.delimiter;
    }

    /**
     * Set the delimiter property
     *
     * @param delimiter String property
     */
    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }


    /**
     * Get the split property
     *
     * @return String property
     */
    public String getSplit() {
        return this.split;
    }

    /**
     * Set the split property
     *
     * @param split String property
     */
    public void setSplit(String split) {
        this.split = split;
    }

    /**
     * Get the delimiterInside property
     *
     * @return delimiterInside property
     */
    public boolean getDelimiterInside() {
        return this.delimiterInside;
    }

    /**
     * Set the delimiterInside property
     *
     * @param delimiterInside property
     */
    public void setDelimiterInside(boolean delimiterInside) {
        this.delimiterInside = delimiterInside;
    }

    public String changeString(String text) throws JspException {
        return wordWrap(text, NumberUtils.toInt(width), delimiter, split, delimiterInside);
    }

    @Override
    public void initAttributes() {
        this.width = "80";
        this.delimiter = "\n";
        this.split = "-";
        this.delimiterInside = true;
    }

    /**
     * Word-wrap a string.
     *
     * @param str         String to word-wrap
     * @param width       int to wrap at
     * @param delim       String to use to separate lines
     * @param split       String to use to split a word greater than width long
     * @param delimInside wheter or not delim should be included in chunk before length reaches width.
     * @return String that has been word wrapped
     */
    public static String wordWrap(String str, int width, String delim,
                                  String split, boolean delimInside) {
        int sz = str.length();

        /// shift width up one. mainly as it makes the logic easier
        width++;

        // our best guess as to an initial size
        StringBuilder buffer = new StringBuilder(sz / width * delim.length() + sz);

        // every line might include a delim on the end
        if (delimInside) {
            width = width - delim.length();
        } else {
            width--;
        }

        int idx;
        String substr;

        // beware: i is rolled-back inside the loop
        for (int i = 0; i < sz; i += width) {

            // on the last line
            if (i > sz - width) {
                buffer.append(str.substring(i));
                break;
            }

            // the current line
            substr = str.substring(i, i + width);

            // is the delim already on the line
            idx = substr.indexOf(delim);
            if (idx != -1) {
                buffer.append(substr.substring(0, idx));
                buffer.append(delim);
                i -= width - idx - delim.length();

                // Erase a space after a delim. Is this too obscure?
                if (substr.length() > idx + 1 && (substr.charAt(idx + 1) != '\n') && Character.isWhitespace(substr.charAt(idx + 1))) {
                    i++;
                }
                continue;
            }

            idx = -1;

            // figure out where the last space is
            char[] chrs = substr.toCharArray();
            for (int j = width; j > 0; j--) {
                if (Character.isWhitespace(chrs[j - 1])) {
                    idx = j;
                    break;
                }
            }

            // idx is the last whitespace on the line.
            if (idx == -1) {
                for (int j = width; j > 0; j--) {
                    if (chrs[j - 1] == '-') {
                        idx = j;
                        break;
                    }
                }
                if (idx == -1) {
                    buffer.append(substr);
                    buffer.append(delim);
                } else {
                    if (idx != width) {
                        idx++;
                    }
                    buffer.append(substr.substring(0, idx));
                    buffer.append(delim);
                    i -= width - idx;
                }
            } else {
                // insert spaces
                buffer.append(substr.substring(0, idx));
                buffer.append(StringUtils.repeat(" ", width - idx));
                buffer.append(delim);
                i -= width - idx;
            }
        }

        return buffer.toString();
    }
}
