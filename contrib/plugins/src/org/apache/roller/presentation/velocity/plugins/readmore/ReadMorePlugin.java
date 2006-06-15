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
package org.apache.roller.presentation.velocity.plugins.readmore;

import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.model.WeblogEntryPlugin;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.util.Utilities;

/**
 * @author lance
 */
public class ReadMorePlugin implements WeblogEntryPlugin
{
    protected String name = "Read More Summary";
    protected String description = "Stops entry after 250 characters and creates " +
        "a link to the full entry.";
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(ReadMorePlugin.class);
       
    String baseURL = "";
    
    public ReadMorePlugin()
    {
        mLogger.debug("ReadMorePlugin instantiated.");
    }
    
    public String toString() { return name; }

	/* (non-Javadoc)
	 * @see org.apache.roller.presentation.velocity.WeblogEntryPlugin#init(
     *   org.apache.roller.presentation.RollerRequest, 
     *   org.apache.velocity.context.Context)
	 */
	public void init(WebsiteData website, Map model) throws RollerException
	{  
        this.baseURL = RollerContext.getRollerContext().getAbsoluteContextUrl();        
	}

	/**
     * @param mgr
     * @param website
     * @return
     */
    private String getPageLink(UserManager mgr, WebsiteData website) throws RollerException
    {
        return website.getDefaultPage().getLink();
    }
    
    
    public String render(WeblogEntryData entry, String str)
    {        
        // in case it didn't initialize
        String pageLink = "Weblog";
        try
        {
            pageLink = getPageLink(
                RollerFactory.getRoller().getUserManager(), entry.getWebsite());
        }
        catch (RollerException e) 
        {
            mLogger.warn("Unable to get pageLink", e);
        }
        
        String result = Utilities.removeHTML(str, true);
        result = Utilities.truncateText(result, 240, 260, "...");
        //String result = Utilities.truncateNicely(entry.getText(), 240, 260, "... ");
        
        // if the result is shorter, we need to add "Read More" link
        if (result.length() < str.length())
        {            
            String link = "<div class=\"readMore\"><a href=\"" + 
                baseURL + entry.getPermaLink() + "\">Read More</a></div>";
            
            result += link;
        }
        return result;
    }


    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }
    
}
