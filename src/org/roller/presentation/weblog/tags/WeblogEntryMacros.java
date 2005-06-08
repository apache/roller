package org.roller.presentation.weblog.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.WeblogManager;
import org.roller.pojos.RefererData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.Macros;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.PageContext;

//////////////////////////////////////////////////////////////////////////////

/**
 * Provides the macros object that is available to Roller day templates.
 */
public class WeblogEntryMacros extends Macros 
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerRequest.class);

    private Date mDate = null;

    //-----------------------------------------------------------------------
    /**
     * Construct macros object for page context and date.
     * @param ctx JSP page context being processed.
     * @param date Data formatter for entry's date.
     */
    public WeblogEntryMacros( PageContext ctx, Date date )
    {
        super( ctx, null );
        mDate = date;
    }
    
    //-----------------------------------------------------------------------
    public List getDayLinkbacks()
    {
        List list = new ArrayList();
        try
        {
            List refs = null;
            
            Roller roller = getRollerRequest().getRoller();
            String userName = getRollerRequest().getUser().getUserName();
            RefererManager refmgr = roller.getRefererManager();
             
            refs = refmgr.getReferersToDate( getWebsite(), view("yyyyMMdd") ); 
                
            for (Iterator rdItr = refs.iterator(); rdItr.hasNext();) {
					RefererData referer = (RefererData) rdItr.next();
					
					if ( referer.getTitle()==null ) continue;
					if ( referer.getTitle().trim().equals("") ) continue;
					if ( referer.getExcerpt()==null ) continue;
					if ( referer.getExcerpt().trim().equals("") ) continue;
    
					if (   referer.getVisible().booleanValue() 
						|| getRollerRequest().isUserAuthorizedToEdit() )
					{ 
						list.add( referer );
					}
				}
              
        }
        catch (Exception e)
        {
            mLogger.error("Getting referers",e);
        }
        return list;
    }
    
    //-----------------------------------------------------------------------
    /** Show entry date using standard format.
     * @deprecated Use methods on
     * {@link org.roller.presentation.weblog.WeblogEntryDataEx WeblogEntryDataEx}
     * instead.
     */
    public String showEntryDate()
    {
        return this.toString();
    }
    
    //-----------------------------------------------------------------------
    /** Show entry date using specified
     * {@link java.text.SimpleDateFormat SimpleDateFormat}
     * format.
     * @deprecated Use methods on
     * {@link org.roller.presentation.weblog.WeblogEntryDataEx WeblogEntryDataEx}
     * instead.
     */
    public String showFormattedEntryDate( String format )
    {
        return this.view( format );
    }

    //-----------------------------------------------------------------------
    /**
     * Show entry permalink and an edit-entry link if logged in as editor.
     * @param entry Weblog entry for which permalink is to be shown.
     * @deprecated Use methods on
     * {@link org.roller.presentation.weblog.WeblogEntryDataEx WeblogEntryDataEx}
     * instead.
     * @return HTML for displaying entry permalink icon with permalink.
     */
    public String showEntryPermalink( WeblogEntryData entry )
    {
        HttpServletRequest request = getRollerRequest().getRequest();
        String userName = getRollerRequest().getUser().getUserName();

        String edit = null;
        try
        {
            if ( getRollerRequest().isUserAuthorizedToEdit() )
            {
                Hashtable params = new Hashtable();
                params.put( RollerRequest.WEBLOGENTRYID_KEY, entry.getId());
                params.put( RollerRequest.USERNAME_KEY, userName);
                params.put( RollerRequest.ANCHOR_KEY, entry.getAnchor());
                edit = RequestUtils.computeURL( mPageContext,
                    "editWeblog", null, null, params,null,false);
            }
        }
        catch (Exception e)
        {
           // should never happen, but if it does:
           mLogger.error("ERROR creating Edit-Entry URL",e);
        }

        StringBuffer sb = new StringBuffer();
        sb.append( "<a href=\"");
        sb.append( request.getContextPath());
        sb.append( "/page/");
        sb.append( userName );
        sb.append( "/");
        sb.append( showFormattedEntryDate("yyyyMMdd") );
        sb.append( "#" );
        sb.append( entry.getAnchor() );
        sb.append( "\" title=\"Permanent link to this weblog entry\"" );
        sb.append( " class=\"entrypermalink\">#</a>" );
        if ( edit != null )
        {
            sb.append( " [<a href=\""+edit+"\">Edit</a>]" );
        }

        return sb.toString();
    }

    //-----------------------------------------------------------------------
    /**
     * Show day permalink and an edit-entry link if logged in as editor.
     * @return HTML for displaying day permalink icon with permalink.
     */
    public String showDayPermalink()
    {
        HttpServletRequest request =
            (HttpServletRequest)mPageContext.getRequest();

        String userName = 
            RollerRequest.getRollerRequest(request).getUser().getUserName();

        StringBuffer sb = new StringBuffer();
        sb.append( "<a href=\"");
        sb.append( request.getContextPath());
        sb.append( "/page/");
        sb.append( userName );
        sb.append( "/");
        sb.append( showFormattedEntryDate("yyyyMMdd") );
        sb.append( "\"><img class=\"daypermalink\" src=\"" );
        sb.append( request.getContextPath() );
        sb.append( "/images/permalink.gif\" ");
        sb.append( "title=\"Permanent link to this day\" " );
        sb.append( "alt=\"" + showFormattedEntryDate("yyyyMMdd") + "\"/></a>" );

        return sb.toString();
    }

    //-----------------------------------------------------------------------

    public String showCommentsLink( WeblogEntryData entry )
    {
        if (entry.getWebsite().getAllowComments().booleanValue())
        {
            HttpServletRequest request =
                (HttpServletRequest)mPageContext.getRequest();

            int commentCount = 0;
            RollerRequest rreq = null;
            try
            {
                rreq = RollerRequest.getRollerRequest(request);
                WeblogManager mgr = rreq.getRoller().getWeblogManager();
                List comments = mgr.getComments( entry.getId() );
                commentCount = comments.size();
            }
            catch (Exception e)
            {
                // should never happen, but if it does:
                if (rreq != null)
                   mLogger.error("ERROR in showCommentsLink",e);
                else
                    System.err.println("ERROR in showCommentsLink:"+e.toString());
            }
            StringBuffer link = new StringBuffer( request.getContextPath());
            link.append( "/comment.do?method=edit&amp;entryid=");
            link.append( entry.getId() );

            StringBuffer sb = new StringBuffer("<a href=\"");            
            sb.append( link );
            sb.append("\" onclick=\"window.open('");
            sb.append( link ).append("','comments','");
            sb.append("width=480,height=480,scrollbars=yes,status=yes');");
            sb.append("return false\" class=\"entrycommentslink\">Comments [");
            sb.append(commentCount).append("]</a>");
            return sb.toString();
        }
        return "";
    }
    
    /** Format date using pattern */
    public String view( String pattern )
    {
        SimpleDateFormat format = new SimpleDateFormat( pattern );
        return format.format( mDate );
    }
    /** Format date using standard format. */
    public String toString()
    {
        return view("EEEE MMMM dd, yyyy");
    }
}

