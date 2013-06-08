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
import org.apache.commons.lang.math.NumberUtils;

/**
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
 * @author bayard@generationjava.com
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
        return StringW.wordWrap(text, NumberUtils.stringToInt(width), delimiter, split, delimiterInside );
    }

    public void initAttributes() {

        this.width = "80";

        this.delimiter = "\n";

        this.split = "-";

        this.delimiterInside = true;
    }

}
