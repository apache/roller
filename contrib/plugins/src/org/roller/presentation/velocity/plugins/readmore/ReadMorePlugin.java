/*
 * Created on Nov 2, 2003
 *
 */
package org.roller.presentation.velocity.plugins.readmore;

import javax.servlet.ServletConfig;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.model.PagePlugin;
import org.roller.util.Utilities;

/**
 * @author lance
 */
public class ReadMorePlugin implements PagePlugin
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
	 * @see org.roller.presentation.velocity.PagePlugin#init(
     *   org.roller.presentation.RollerRequest, 
     *   org.apache.velocity.context.Context)
	 */
	public void init(
            WebsiteData website,
            Object config,
            String baseURL,
            Context ctx) throws RollerException
	{  
        this.baseURL = baseURL;        
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

    /* 
     * This method cannot do it's intended job (since it cannot
     * read the current Entry) so it is to do no work!
     * 
     * (non-Javadoc)
	 * @see org.roller.presentation.velocity.PagePlugin#render(java.lang.String)
	 */
	public String render(String str)
	{
		return str;
	}
    
    public String render(WeblogEntryData entry, boolean skipFlag)
    {
        if (skipFlag) 
            return entry.getText();
        
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
        
        String result = Utilities.removeHTML(entry.getText(), true);
        result = Utilities.truncateText(result, 240, 260, "...");
        //String result = Utilities.truncateNicely(entry.getText(), 240, 260, "... ");
        
        // if the result is shorter, we need to add "Read More" link
        if (result.length() < entry.getText().length())
        {            
            String link = "<div class=\"readMore\"><a href=\"" + 
                baseURL + "comments/" + 
                entry.getWebsite().getHandle() + 
                "/" + pageLink + "/" + Utilities.encode(entry.getAnchor()) +
                "\">Read More</a></div>";
            
            result += link;
        }
        return result;
    }


    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }
}