/*
 * Created on Nov 2, 2003
 *
 */
package org.roller.presentation.velocity.plugins.email;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.pojos.WeblogEntryData;
import org.roller.model.PagePlugin;
import org.roller.pojos.WebsiteData;
import org.roller.util.RegexUtil;

/**
 * @author lance
 *
 */
public class ObfuscateEmailPlugin implements PagePlugin
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

	/* (non-Javadoc)
	 * @see org.roller.presentation.velocity.PagePlugin#init(org.roller.presentation.RollerRequest, org.apache.velocity.context.Context)
	 */
	public void init(
            WebsiteData website,
            Object config,
            String baseURL,
            Context ctx)
	{
	}

	/* 
     * Find any likely email addresses and HEX escape them 
     * (non-Javadoc)
	 * @see org.roller.presentation.velocity.PagePlugin#render(java.lang.String)
	 */
	public String render(String str)
	{
        return RegexUtil.encodeEmail(str);
	}
    
    public String render(WeblogEntryData entry, String str)
    {
        return render(str);
    }

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }

    public boolean getSkipOnSingleEntry() {return false;}
}
