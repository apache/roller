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
/*
 * Created on Nov 2, 2003
 *
 */
package org.apache.roller.presentation.velocity.plugins.email;

import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.model.WeblogEntryPlugin;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.RegexUtil;

/**
 * @author lance
 *
 */
public class ObfuscateEmailPlugin implements WeblogEntryPlugin
{
    protected String name = "Email Scrambler";
    protected String description = "Automatically converts email addresses " +
      "to me-AT-mail-DOT-com format.  Also &quot;scrambles&quot; mailto: links.";
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(ObfuscateEmailPlugin.class);
           
    public ObfuscateEmailPlugin()
    {
        mLogger.debug("ObfuscateEmailPlugin instantiated.");
    }
    
    public String toString() { return name; }

	public void init(WebsiteData website, Map model) throws RollerException {
	}

	/* 
     * Find any likely email addresses and HEX escape them 
     * (non-Javadoc)
	 * @see org.apache.roller.presentation.velocity.WeblogEntryPlugin#render(java.lang.String)
	 */
	public String render(WeblogEntryData entry, String str)
	{
        return RegexUtil.encodeEmail(str);
	}

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }
}
