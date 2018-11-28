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

import org.apache.commons.lang.StringUtils;
import javax.servlet.jsp.JspException;

/**
 * Remove whitespace from start and end of a String.
 * 
 * @author bayard@generationjava.com
 */
public class TrimTag extends StringTagSupport {

    public TrimTag() {
        super();
    }


    public String changeString(String text) throws JspException {
        return StringUtils.trim(text);
    }

    public void initAttributes() {

    }


}
