package org.roller.presentation.weblog.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.roller.model.WeblogManager;
import org.roller.pojos.PageData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;


//////////////////////////////////////////////////////////////////////////////
/**
 * @jsp.tag name="ViewWeblogEntries"
 */
public class ViewWeblogEntriesTag 
	extends org.roller.presentation.tags.HybridTag
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerRequest.class);

    /** @jsp.attribute */
    public String getDayTemplate() { return mDayTemplate; }
    public void setDayTemplate( String n ) { mDayTemplate = n; }
    private String mDayTemplate = null;

    /** @jsp.attribute */
    public String getCatName() { return mCatName; }
    public void setCatName( String n ) { mCatName = n; }
    private String mCatName = null;

	/** @jsp.attribute */
	public int getMaxEntries() { return mMaxEntries; }
    public void setMaxEntries( int v ) { mMaxEntries = v; }
	private int mMaxEntries = -1;

   	//------------------------------------------------------------------------ 

	public String view( String catName )
	{
		mCatName = catName;
		return emit();
	}

	public String view( String catName, int maxEntries )
	{
		mCatName = catName;
		mMaxEntries = maxEntries;
		return emit();
	}

   	//------------------------------------------------------------------------ 
    /**
     * This doStartTag is for the weblog template implementation
     * @return EVAL_SKIP_BODY
     */
    public int doStartTag( PrintWriter pw ) throws JspException 
	{
		try
		{
			HttpServletRequest req = 
				(HttpServletRequest)pageContext.getRequest();

			RollerRequest rreq = RollerRequest.getRollerRequest(req);

			// need website so we can get weblog day template
			WebsiteData website = rreq.getWebsite( );
            
            String catName = mCatName;
            if (catName == null)
            {
                catName= req.getParameter(RollerRequest.WEBLOGCATEGORYNAME_KEY);
            }

            String name = null;
            if ( rreq.getUser() != null )
            {
                name = rreq.getUser().getUserName();
            }

			// get recent weblog entries
			int max = (mMaxEntries == -1) ? 15 : mMaxEntries;
            Date dayParam = rreq.getDate(true);
            WeblogManager mgr = rreq.getRoller().getWeblogManager();
            
            //Map map = mgr.getRecentWeblogEntries( 
                //name, dayParam, catName, max, true );

            Map map = mgr.getWeblogEntryObjectMap(
                            rreq.getWebsite(),      // userName
                            null,                  // startDate
                            dayParam,               // endDate
                            catName,                // catName
                            WeblogManager.PUB_ONLY, // status
                            new Integer(max));     // maxEntries      
            
            
            // Get page id if daytemplate is specified
            String pid = null; 
            if ( mDayTemplate != null )
            {
                PageData page =
                    rreq.getRoller().getUserManager().getPageByLink(
                        website, mDayTemplate );
                if (page != null)
                {
                    pid = page.getId();
                }            
            }
            if ( pid == null )
            {
                pid = website.getWeblogDayPageId();
            }
            
            // get day template and run it through Velocity
            Template vtemplate = RuntimeSingleton.getTemplate( pid );

			// through entries, one day per iteration 
			int count = 0;
			Iterator iter = map.keySet().iterator();
			while ( iter.hasNext() )
			{
				// get date and entries for that date
				Date d = (Date)iter.next();

                VelocityContext vcontext = new VelocityContext();
                WeblogEntryMacros macros = 
                    new WeblogEntryMacros( pageContext, d );
                vcontext.put( "macros", macros );

				ArrayList entries = (ArrayList)map.get( d );                
                vcontext.put( "entries", entries ); 

				vtemplate.merge( vcontext, pw );

				if ( mMaxEntries != -1 && count > mMaxEntries ) break;
				count++;
			}
		}
		catch (Exception e)
		{
            mLogger.error("Unexpected exception",e);
			throw new JspException(e);
		}
		return Tag.SKIP_BODY;
    }
}

