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

	private String stripMarkup;
    private String lower;
    private String upper;
	private String appendToEnd;

    public TruncateNicelyTag() {
        super();
    }

    /**
     * Get the lower property
	 * @return String lower property
     */
    public String getLower() {
        return this.lower;
    }

    /**
     * Set the upper property
     * @param lower String property
     */
    public void setLower(String l) {
        this.lower = l;
    }

    /**
     * Get the upper property
	 * @return String upper property
     */
    public String getUpper() {
        return this.upper;
    }

    /**
     * Set the upper property
     * @param upper String property
     */
    public void setUpper(String u) {
        this.upper = u;
    }

	public String getAppendToEnd() {
		return this.appendToEnd;
	}

	public void setAppendToEnd(String s) {
		this.appendToEnd = s;
	}

    public String changeString(String text) throws JspException {
					
		int l = NumberUtils.stringToInt(lower);
		int u = NumberUtils.stringToInt(upper);
	
		return StringW.truncateNicely(text, l, u, this.appendToEnd);
    }

    public void initAttributes() {

		this.lower = "10";
		this.upper = "-1";
		this.appendToEnd = "...";

    }

}
