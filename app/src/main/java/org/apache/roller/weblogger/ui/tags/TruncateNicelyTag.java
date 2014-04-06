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
import org.apache.commons.lang3.math.NumberUtils;

/**
 * Code grabbed from retired Jakarta Tablibs project: http://jakarta.apache.org/taglibs/string/
 *
 * A more intelligent substring.  It attempts to cut off a string after
 * a space, following predefined or user-supplied lower and upper limits,
 * useful for making short descriptions from long text.  Can also strip
 * HTML, or if not, intelligently close any tags that were left open.
 * It adds on a user-defined ending.
 *
 * <dl>
 * <dt>lower</dt><dd>
 *             Minimum length to truncate at.
 *             Required.
 * </dd>
 * <dt>upper</dt><dd>
 *             Maximum length to truncate at.
 *             Required.
 * </dd>
 * <dt>upper</dt><dd>
 *             String to append to end of truncated string.
 * </dd>
 * </dl>
 *
 * @author timster@mac.com
 */
public class TruncateNicelyTag extends StringTagSupport {

    private String lower;
    private String upper;
    private String appendToEnd;

    public TruncateNicelyTag() {
        super();
    }

    /**
     * Get the lower property
     *
     * @return String lower property
     */
    public String getLower() {
        return this.lower;
    }

    /**
     * Set the upper property
     *
     * @param lower String property
     */
    public void setLower(String lower) {
        this.lower = lower;
    }

    /**
     * Get the upper property
     *
     * @return String upper property
     */
    public String getUpper() {
        return this.upper;
    }

    /**
     * Set the upper property
     *
     * @param upper String property
     */
    public void setUpper(String upper) {
        this.upper = upper;
    }

    public String getAppendToEnd() {
        return this.appendToEnd;
    }

    public void setAppendToEnd(String s) {
        this.appendToEnd = s;
    }

    public String changeString(String text) throws JspException {

        int l = NumberUtils.toInt(lower);
        int u = NumberUtils.toInt(upper);

        return truncateNicely(text, l, u, this.appendToEnd);
    }

    public void initAttributes() {

        this.lower = "10";
        this.upper = "-1";
        this.appendToEnd = "...";

    }

    /**
     * Truncates a string nicely. It will search for the first space
     * after the lower limit and truncate the string there.  It will
     * also append any string passed as a parameter to the end of the
     * string.  The hard limit can be specified to forcibily truncate a
     * string (in the case of an extremely long word or such).  All
     * HTML/XML markup will be stripped from the string prior to
     * processing for truncation.
     *
     * @param str         String the string to be truncated.
     * @param lower       int value of the lower limit.
     * @param upper       int value of the upper limit, -1 if no limit is
     *                    desired. If the uppper limit is lower than the
     *                    lower limit, it will be adjusted to be same as
     *                    the lower limit.
     * @param appendToEnd String to be appended to the end of the
     *                    truncated string.
     *                    This is appended ONLY if the string was indeed
     *                    truncated. The append is does not count towards
     *                    any lower/upper limits.
     */
    private static String truncateNicely(String str, int lower, int upper, String appendToEnd)
    {
        // strip markup from the string
        str = removeXml(str);

        // quickly adjust the upper if it is set lower than 'lower'
        if(upper < lower) {
            upper = lower;
        }

        // now determine if the string fits within the upper limit
        // if it does, go straight to return, do not pass 'go' and collect $200
        if(str.length() > upper) {
            // the magic location int
            int loc;

            // first we determine where the next space appears after lower
            loc = str.lastIndexOf(' ', upper);

            // now we'll see if the location is greater than the lower limit
            if(loc >= lower) {
                // yes it was, so we'll cut it off here
                str = str.substring(0, loc);
            } else {
                // no it wasn't, so we'll cut it off at the upper limit
                str = str.substring(0, upper);
            }

            // the string was truncated, so we append the appendToEnd String
            str = str + appendToEnd;
        }

        return str;
    }

    /**
     * Remove any xml tags from a String.
     */
    private static String removeXml(String str) {
        int sz = str.length();
        StringBuilder buffer = new StringBuilder(sz);
        boolean inTag = false;
        for (int i=0; i<sz; i++) {
            char ch = str.charAt(i);
            if (ch == '<') {
                inTag = true;
            } else
            if (ch == '>') {
                inTag = false;
                continue;
            }
            if (!inTag) {
                buffer.append(ch);
            }
        }
        return buffer.toString();
    }

}
