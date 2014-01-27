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
 * Gets the leftmost 'n' characters from a string.
 *
 * <dl>
 * <dt>count</dt><dd>
 *             Size of characters to get.
 *             Required.
 * </dd>
 * </dl>
 * 
 * @author bayard@generationjava.com
 */
public class LeftTag extends StringTagSupport {

    private String count;

    public LeftTag() {
        super();
    }

    /**
     * Get the count property
     *
     * @return String property
     */
    public String getCount() {
        return this.count;
    }

    /**
     * Set the count property
     *
     * @param count String property
     */
    public void setCount(String count) {
        this.count = count;
    }



    public String changeString(String text) throws JspException {
        return StringUtils.left(text, NumberUtils.toInt(count));
    }

    public void initAttributes() {

        this.count = "0";

    }

}
