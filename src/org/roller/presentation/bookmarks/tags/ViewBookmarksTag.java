package org.roller.presentation.bookmarks.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.model.BookmarkManager;
import org.roller.pojos.BookmarkComparator;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderData;
import org.roller.pojos.UserData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.util.Utilities;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;


/**
 * @deprecated Is this class even in use anymore? -Lance
 * 
 * <p>Displays a folder of bookmarks or a table that shows all folders.
 * If the folderName property is set then this tag will display the bookmarks
 * in that folder, separated by BR tag line breaks. If the folderName
 * property is not set, this tag will display a table of bookmarks.
 * </p>
 * <p>The bookmarks table display uses the Folder Column and Row values
 * to position the Folders in a table. The bookmarks within each folder
 * are ordered using the Bookmark Priority value. A bookmark's HREF tag
 * is given a CSS class of rBookmark_N where N is the Bookmark's Weight value.
 * </p>
 * @jsp.tag name="ViewBookmarks"
 */
public class ViewBookmarksTag extends org.roller.presentation.tags.HybridTag
{
    static final long serialVersionUID = -4357415994168654686L;
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(ViewBookmarksTag.class);

    /** @jsp.attribute */
    public String getFolderName() { return mFolderName; }
    public void setFolderName(String v) { mFolderName = v; }
    private String mFolderName = null;

    /** @jsp.attribute */
    public String getTitle() { return mTitle; }
    public void setTitle(String v) { mTitle = v; }
    private String mTitle = null;

    /** @jsp.attribute */
    public boolean getShowFolderName() { return mShowFolderName; }
    public void setShowFolderName(boolean v) { mShowFolderName = v; }
    private boolean mShowFolderName = true;

    /** @jsp.attribute */
    public boolean getExpandingFolder() { return mExpandingFolder; }
    public void setExpandingFolder(boolean v) { mExpandingFolder = v; }
    private boolean mExpandingFolder = false;
    
    //------------------------------------------------------------------------

    public String view( String folderName, String title )
    {
        mFolderName = folderName;
        mTitle = title;
        return emit();
    }

    public String view( String folderName, boolean showFolderName )
    {
        mFolderName = folderName;
        mShowFolderName = showFolderName;
        return emit();
    }

    public String view( String folderName, boolean showFolderName, boolean expandingFolder )
    {
        mFolderName = folderName;
        mShowFolderName = showFolderName;
        mExpandingFolder = expandingFolder;
        return emit();
    }
    
    //-------------------------------------------------------------
    /**
     * Process start tag
     * @return EVAL_SKIP_BODY
     */
    public int doStartTag( PrintWriter pw ) throws JspException
    {
        try
        {
            HttpServletRequest req =
                (HttpServletRequest)pageContext.getRequest();
            RollerRequest rreq = RollerRequest.getRollerRequest(req);
            BookmarkManager bookmarkMgr =
                rreq.getRoller().getBookmarkManager();
            UserData user = rreq.getUser();

            FolderData fd = bookmarkMgr.getFolder(
                rreq.getWebsite(), mFolderName);

            if ( fd == null )
            {
                pw.print("<span class=\"error\">");
                pw.print("Error fetching folder named "+mFolderName);
                pw.print("</span>");
                return  Tag.SKIP_BODY;
            }
            emitFolderHTML( pw, pageContext, fd, user );
            return Tag.SKIP_BODY;
        }
        catch (Exception e)
        {
            mLogger.error("Exception",e);
            throw new JspException(
                e.getClass().toString()+": "+e.getMessage(),e);
        }
    }

    //------------------------------------------------------------------------
    public void emitBookmarkHTML( PrintWriter pw, PageContext ctx,
        BookmarkData bookmark, UserData user )
        throws IOException, MalformedURLException
    {
        HttpServletRequest request = (HttpServletRequest)ctx.getRequest();
        String cpath = request.getContextPath();

        String resourcePath = request.getContextPath()
                + RollerContext.getUploadPath(pageContext.getServletContext())
                    + "/" + user.getUserName();

        if ( bookmark.getImage()!=null
         && !bookmark.getImage().trim().equals("") )
        {
            pw.print("<a href=\""+bookmark.getUrl()+"\">");
            pw.println("<img src=\""
                + resourcePath + "/" + bookmark.getImage() + "\" "
                + "alt=\"" + bookmark.getName() + "\" /> " );
            pw.println("</a><br />");
        }
        else if ( bookmark.getFeedUrl()!=null
              && !bookmark.getFeedUrl().trim().equals("") )
        {
            pw.print("<a class=\"rBookmark\" href=\""+bookmark.getUrl()+"\"");
            if ( !bookmark.getDescription().trim().equals("") )
            {
                pw.print(" title=\""+bookmark.getDescription()+"\"");
            }
            pw.print(" >");
            pw.println( bookmark.getName()+"</a>");

            pw.println( "<a href=\""+bookmark.getFeedUrl()+"\">" );
            pw.print  ( "<img src=\""+cpath+"/images/smrssbadge.gif\" " );
            pw.println(     "alt=\"URL of site's RSS feed\"" );
            pw.println(     "class=\"smrssbadge\" /></a>" );

            pw.println( "<br />" );
        }
        else
        {
            pw.print( "<a href=\"" );
            pw.print( bookmark.getUrl() );
            pw.print( "\" " );
            pw.print( "class=\"rBookmark" );
            pw.print( bookmark.getWeight() );
            pw.print( "\" " );
            pw.print( "title=\""  );
            pw.print( bookmark.getDescription() );
            pw.print( "\" >" );
            pw.print( bookmark.getName() );
            pw.println( "</a><br />" );
        }
    }

    //------------------------------------------------------------------------
    public void emitFolderHTML( PrintWriter pw, PageContext ctx,
        FolderData folder, UserData user )
        throws IOException, MalformedURLException
    {
        HttpServletRequest request = (HttpServletRequest)ctx.getRequest();
        String cpath = request.getContextPath();
        // replace spaces with underscores
        String divId = Utilities.stringReplace( folder.getName(), " ", "_" );
        // remove single quotes to prevent javascript errors
        divId = Utilities.stringReplace( divId, "'", "" );
        
        if ( mShowFolderName && !mExpandingFolder )
        {
            pw.println( "<div class=\"rFolder\">"+folder.getName()+"</div>" );
        }

        if ( mShowFolderName && mExpandingFolder )
        {            
            pw.print( "<div class=\"rFolder\"><a href=\"javascript:" );
            pw.print( "toggleFolder('"+divId+"')\"><span id=\"i"+divId+"\">" );
            pw.print( "+</span> "+folder.getName()+"</a></div>" );
            pw.println( "<div id=\""+divId+"\" style=\"display: none\">" );       
        }
        
        Collection bookmarks = folder.getBookmarks();
        //java.util.Collections.sort( bookmarks, new BookmarkComparator() );
        Iterator iter = bookmarks.iterator();
        while ( iter.hasNext() )
        {
            BookmarkData bookmark = (BookmarkData)iter.next();
            emitBookmarkHTML( pw, ctx, bookmark, user );
        }
        
        if (mShowFolderName && mExpandingFolder)
        {
            pw.println( "</div>" );       
            pw.println( "<script type=\"text/javascript\">" );
            pw.println( "<!--" );
            pw.println( "  folderPreference('"+divId+"');" );
            pw.println( "// -->");
            pw.println( "</script>" );
        }
    }
}

