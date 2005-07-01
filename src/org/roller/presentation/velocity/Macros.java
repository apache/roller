
package org.roller.presentation.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.roller.model.RefererManager;
import org.roller.model.UserManager;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.RefererData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.bookmarks.tags.ViewBookmarksTag;
import org.roller.presentation.tags.menu.EditorNavigationBarTag;
import org.roller.presentation.tags.menu.NavigationBarTag;
import org.roller.presentation.weblog.tags.RssBadgeTag;
import org.roller.presentation.weblog.tags.ViewWeblogEntriesTag;
import org.roller.presentation.weblog.tags.WeblogCategoryChooserTag;
import org.roller.util.Utilities;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.RollerFactory;

/**
 * Provides the macros object that is available to Roller templates and 
 * weblog entries.
 */
public class Macros
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(Macros.class);

    protected PageContext mPageContext = null;
    
    protected PageHelper mPageHelper = null;

    /** Maximum depth of recursion for includePage and showWeblogEntry calls */
    public static final int MAX_RECURSION_DEPTH = 6;
    
    /** Keep track of each thread's recursive depth in includePage(). */
    protected static final ThreadLocal mIncludePageTLS = new ThreadLocal();
    
    /** Keep track of each thread's recursive depth in showWeblogEntries(s */
    protected static final ThreadLocal mEntriesTLS = new ThreadLocal();
    
    /** Keep track of each thread's recursive depth in showWeblogEntries(s */
    protected static final ThreadLocal mEntriesDayTLS = new ThreadLocal();

    //------------------------------------------------------------------------
    /** Construts a macros object for a JSP page context. */
    public Macros( PageContext ctx, PageHelper helper )
    {
        mPageContext = ctx;
        mPageHelper = helper;
    }

    //------------------------------------------------------------------------
    /** Get the Roller request object associated with this instance */
    protected RollerRequest getRollerRequest()
    {
        return RollerRequest.getRollerRequest(mPageContext);
    }

    //------------------------------------------------------------------------
    /**
     * Returns the current date formatted according to the specified pattern.
     * @param pattern Date pattern, @see java.text.SimpleDateFormat
     * @return Current date formatted according to specified pattern.
     */
    public String formatCurrentDate( String pattern )
    {
        String date = null;
        try
        {
            SimpleDateFormat format = new SimpleDateFormat( pattern );
            date = format.format( new Date() );
        }
        catch (RuntimeException e)
        {
            date = "ERROR: formatting date";
        }
        return date;
    }

    //------------------------------------------------------------------------
    /**
     * Returns most recent update time of collection of weblog entries using
     * the specified pattern.
     * @param weblogEntries Collection of weblog entries.
     * @param Date format pattern, @see java.text.SimpleDateFormat.
     * @return Most recent update time formatted by pattern.
     */
    public String formatUpdateTime( ArrayList weblogEntries, String pattern )
    {
        String date = null;
        Date updateTime = getUpdateTime(weblogEntries);
        try
        {
            SimpleDateFormat format = new SimpleDateFormat( pattern );
            date = format.format( updateTime );
        }
        catch (RuntimeException e)
        {
            date = "ERROR: formatting date";
        }
        return date;
    }
   
    //------------------------------------------------------------------------
    /**
     * Returns most recent update time of collection of weblog entries.
     * @param weblogEntries Collection of weblog entries.
     * @return Most recent update time.
     */
    public Date getUpdateTime( ArrayList weblogEntries )
    {
        return PageModel.getUpdateTime( weblogEntries );
    }
   
    //------------------------------------------------------------------------
    /**
     * Version number of Roller.
     * @return Version number of Roller.
     */
    public String showVersion()
    {
        ServletContext ctx = mPageContext.getServletContext();
        return ctx.getInitParameter( RollerContext.VERSION_KEY );
    }
    //------------------------------------------------------------------------
    /**
     * Title of your website.
     * @return Title of your website.
     */
    public String showWebsiteTitle()
    {
        WebsiteData wd = null;
        RollerRequest rreq = getRollerRequest();
        try {
            wd = rreq.getWebsite();
        }
        catch (Exception e) {
            return "ERROR finding website in request: " + e.toString();
        }
        return wd.getName();
    }
    //------------------------------------------------------------------------
    /**
     * Website description.
     * @return Website description.
     */
    public String showWebsiteDescription()
    {
        WebsiteData wd = null;
        RollerRequest rreq = getRollerRequest();
        try {
            wd = rreq.getWebsite();
        }
        catch (Exception e) {
            return "ERROR finding website in request: " + e.toString();
        }
        return wd.getDescription();
    }
    //------------------------------------------------------------------------
    /**
     * Show navigation links for pages that are not hidden.
     * @param vertical Display links in a vertical column, separated by BR tags.
     * @return HTML for navigation bar.
     */
    public String showNavBar( boolean vertical )
    {
        NavigationBarTag navTag = new NavigationBarTag();
        navTag.setPageContext(mPageContext);
        navTag.setVertical(vertical);
        return navTag.emit();
    }
    //------------------------------------------------------------------------
    /**
     * Show navigation links for pages that are not hidden, separated by a
     * specified delimeter.
     * @param vertical Display links in a vertical column.
     * @param delimeter Delimeter to separate links.
     * @return HTML for navigation bar.
     */
    public String showNavBar( boolean vertical, String delimiter )
    {
        NavigationBarTag navTag = new NavigationBarTag();
        navTag.setPageContext(mPageContext);
        navTag.setVertical(vertical);
        navTag.setDelimiter(delimiter);
        return navTag.emit();
    }
    //------------------------------------------------------------------------
    /**
     * Show the Roller Editor menu as a navigation bar.
     * specified delimeter.
     * @param vertical Displaylinks in a vertical column.
     * @return HTML for navigation bar.
     * */
    public String showEditorNavBar( boolean vertical )
    {
        EditorNavigationBarTag editorTag = new EditorNavigationBarTag();
        editorTag.setPageContext(mPageContext);
        if ( vertical )
        {
            editorTag.setView("/navbar-vertical.vm");
        }
        else
        {
            editorTag.setView("/navbar-horizontal.vm");
        }
        editorTag.setModel("editor-menu.xml");
        return editorTag.emit();
    }

    //------------------------------------------------------------------------

    /**
     * Display bookmarks in specified folder using specified title.
     * @param folderName Folder to be dislayed.
     * @param title Title to be displayed.
     * @return HTML for bookmarks display.
     */
    public String showBookmarks( String folderName, String title )
    {
        ViewBookmarksTag booksTag = new ViewBookmarksTag();
        booksTag.setPageContext(mPageContext);
        return booksTag.view( folderName, title );
    }

    /**
     * Display bookmarks in specified folder using specified title.
     * @param folderName Folder to be dislayed.
     * @param showFolderName Display folder name as title.
     * @return HTML for bookmarks display.
     */
    public String showBookmarks( String folderName, boolean showFolderName )
    {
        ViewBookmarksTag booksTag = new ViewBookmarksTag();
        booksTag.setPageContext(mPageContext);
        return booksTag.view( folderName, showFolderName );
    }

    /**
     * Display bookmarks in specified folder as an expandable link.
     * @param folderName Folder to be dislayed.
     * @param showFolderName Display folder name as title.
     * @param expandingFolder Show bookmarks in expanding folder.
     * @return HTML and JavaScript for bookmarks display.
     */
    public String showBookmarks(
        String folderName, boolean showFolderName, boolean expandingFolder )
    {
        ViewBookmarksTag booksTag = new ViewBookmarksTag();
        booksTag.setPageContext(mPageContext);
        return booksTag.view( folderName, showFolderName, expandingFolder );
    }

    //------------------------------------------------------------------------
    /**
     * Get {@link org.roller.pojos.UserData UserData} object.
     * @see org.roller.pojos.UserData
     * @return User object.
     */
    public UserData getUser()
    {
        try
        {
            return getRollerRequest().getUser();
        }
        catch (Exception e)
        {
            mLogger.error("Getting user",e); 
        }
        return null;
    }

    //------------------------------------------------------------------------
    /**
     * Get  
     * {@link org.roller.presentation.users.WebsiteDataEx WebsiteDataEx} 
     * object.
     * @see org.roller.pojos.WebsiteData
     * @return Website object.
     */
    public WebsiteData getWebsite()
    {
        try
        {
            return getRollerRequest().getWebsite();
        }
        catch (Exception e)
        {
            mLogger.error("Getting website",e); 
        }
        return null;
    }

    //------------------------------------------------------------------------
    /**
     * Show weblog enties using the day template specified in your site 
     * settings.
     * @return HTML for weblog entries.
     */    
    public String showWeblogEntries()
    {
        return showWeblogEntries(15);
    }

    //------------------------------------------------------------------------
    /**
     * Show up to 100 weblog enties using the day template specified in your site 
     * settings.
     * @param maxEntries Maximum number of entries to display.
     * @return HTML for weblog entries.
     */    
    public String showWeblogEntries(int maxEntries)
    {
        String ret = null;
        try
        {
            // protection from recursion
            if ( mEntriesTLS.get() == null )
            {
                mEntriesTLS.set( new Integer(0) );
            }
            else
            {
                Integer countObj = (Integer)mEntriesTLS.get();
                int count = countObj.intValue();
                if ( count++ > MAX_RECURSION_DEPTH )
                {
                    return "ERROR: recursion level too deep";
                }
                mEntriesTLS.set( new Integer(count) );
            }

            ViewWeblogEntriesTag entriesTag = new ViewWeblogEntriesTag();
            entriesTag.setPageContext(mPageContext);
            entriesTag.setMaxEntries(maxEntries);
            ret = entriesTag.emit();
        }
        finally
        {
            mEntriesTLS.set( null );
        }

        return ret;
    }

    //------------------------------------------------------------------------
    /**
     * Show most recent 15 weblog enties using specified day template.
     * @param dayTemplate Name of day template.
     * @return HTML for weblog entries.
     */    
    public String showWeblogEntries( String dayTemplate )
    {
        return showWeblogEntries(dayTemplate,15);
    }

    //------------------------------------------------------------------------
    /**
     * Show weblog enties using specified day template.
     * @param dayTemplate Name of day template.
     * @param maxEntries Maximum number of entries to display.
     * @return HTML for weblog entries.
     */    
    public String showWeblogEntries( String dayTemplate, int maxEntries )
    {
        String ret = null;
        try
        {
            // protection from recursion
            if ( mEntriesDayTLS.get() == null )
            {
                mEntriesDayTLS.set( new Integer(0) );
            }
            else
            {
                Integer countObj = (Integer)mEntriesDayTLS.get();
                int count = countObj.intValue();
                if ( count++ > MAX_RECURSION_DEPTH )
                {
                    return "ERROR: recursion level too deep";
                }
                mEntriesDayTLS.set( new Integer(count) );
            }

            ViewWeblogEntriesTag entriesTag = new ViewWeblogEntriesTag();
            entriesTag.setPageContext(mPageContext);
            entriesTag.setMaxEntries(maxEntries);
            entriesTag.setDayTemplate( dayTemplate );
            ret = entriesTag.emit();
        }
        finally
        {
            mEntriesDayTLS.set( null );
        }
        return ret;
    }

    //------------------------------------------------------------------------
    /** 
     * Display weblog calendar.
     * @return HTML for calendar.
     */
    public String showWeblogCalendar()
    {
        return showWeblogCalendar(false);
    }

    //------------------------------------------------------------------------
    /** 
     * Display big weblog calendar, well suited for an archive page.
     * @return HTML for calendar.
     */
    public String showBigWeblogCalendar()
    {
        return showWeblogCalendar(true);
    }

    //------------------------------------------------------------------------
    /** 
     * Weblog calendar display implementation.
     * @param big Show big archive style calendar.
     * @return HTML for calendar.
     */
    public String showWeblogCalendar( boolean big )
    {
    	return mPageHelper.showWeblogCalendar(big, null);
    }

    //------------------------------------------------------------------------
    /**
     * Show a list of links to each of your weblog categores.
     * @return HTML for category chooser.
     */
    public String showWeblogCategoryChooser()
    {
        WeblogCategoryChooserTag catTag = new WeblogCategoryChooserTag();
        catTag.setPageContext(mPageContext);
        return catTag.emit();
    }

    //------------------------------------------------------------------------
    /**
     * Show a list of links to each of your RSS feeds.
     * @return HTML for RSS feed links.
     */
    public String showRSSLinks()
    {
        String links = "ERROR: creating RSS links";
        try
        {                
            RollerRequest rreq = getRollerRequest();
            RollerContext rctx = RollerContext.getRollerContext(
                rreq.getServletContext());
                
            UserData ud = rreq.getUser();

            String baseUrl = rctx.getContextUrl(
                (HttpServletRequest)mPageContext.getRequest());
            StringBuffer sb = new StringBuffer();


            sb.append("<a href=\"");
            sb.append( baseUrl );
            sb.append("/rss/");
            sb.append( ud.getUserName() );
            sb.append("\">All</a> ");
            sb.append("[<a href=\"");
            sb.append( baseUrl );
            sb.append("/rss/");
            sb.append( ud.getUserName() );
            sb.append("?excerpts=true\">");
            sb.append("excerpts</a>]");
            sb.append("<br />");

			List cats = rreq.getRoller().getWeblogManager()
                .getWeblogCategories(rreq.getWebsite(), false);
			for (Iterator wbcItr = cats.iterator(); wbcItr.hasNext();) {
				WeblogCategoryData category = (WeblogCategoryData) wbcItr.next();
				String catName = category.getName();
				sb.append("<a href=\"");
				sb.append( baseUrl );
				sb.append("/rss/");
				sb.append( ud.getUserName() );
				sb.append("?catname=");
				sb.append( catName );
				sb.append("\">");
				sb.append( catName );
				sb.append("</a> ");
				sb.append("[<a href=\"");
				sb.append( baseUrl );
				sb.append("/rss/");
				sb.append( ud.getUserName() );
				sb.append("?catname=");
				sb.append( catName );
				sb.append("&amp;excerpts=true\">");
				sb.append("excerpts</a>]");
				sb.append("<br />");
			}
            
            links = sb.toString();
        }
        catch (Exception e)
        {
            mLogger.error("Unexpected exception",e);
        }
        return links;
    }

    //------------------------------------------------------------------------
    /**
     * Show RSS auto-discovery element.
     * @return HTML for RSS auto-discovery element. 
     */
    public String showRSSAutodiscoveryLink()
    {
        String links = "ERROR: error generating RSS autodiscovery link";
        try
        {
            RollerRequest rreq = RollerRequest.getRollerRequest(
                (HttpServletRequest)mPageContext.getRequest());
            RollerContext rctx = RollerContext.getRollerContext(
                rreq.getServletContext());
             
            StringBuffer sb = new StringBuffer();
            sb.append("<link rel=\"alternate\" type=\"application/rss+xml\" ");
            sb.append("title=\"RSS\" href=\"");
            sb.append( rctx.getContextUrl(
                (HttpServletRequest)mPageContext.getRequest()) );
            sb.append( "/rss/" );
            sb.append( getUser().getUserName() );
            sb.append( "\">" );
            links = sb.toString();
        }
        catch (Throwable e)
        {
            mLogger.error("Showing RSS link",e);
        }
        return links;
    }

    //------------------------------------------------------------------------
    /**
     * Show RSS badge with link to your main RSS feed.
     * @return HTML for RSS badge with link to your main RSS feed.
     */
    public String showRSSBadge()
    {
        RssBadgeTag rssTag = new RssBadgeTag();
        rssTag.setPageContext(mPageContext);
        return rssTag.emit();
    }

    //------------------------------------------------------------------------
    /**
     * Expand macros in specified page and include the results in this page.
     * @param Name of page to include.
     * @return HTML for included page.
     */
    public String includePage( String pageName )
    {
        String ret = null;
        try
        {
            // protection from recursion
            if ( mIncludePageTLS.get() == null )
            {
                mIncludePageTLS.set( new Integer(0) );
            }
            else
            {
                Integer countObj = (Integer)mIncludePageTLS.get();
                int count = countObj.intValue();
                if ( count++ > MAX_RECURSION_DEPTH )
                {
                    return "ERROR: recursion level too deep";
                }
                mIncludePageTLS.set( new Integer(count) );
            }            
                       
            // Get included page template 
            RollerRequest rreq = RollerRequest.getRollerRequest(
                (HttpServletRequest)mPageContext.getRequest());                
            UserManager userMgr = rreq.getRoller().getUserManager();
            
            WeblogTemplate pd = userMgr.getPageByName( 
                rreq.getWebsite(), pageName );
            Template vtemplate = null;
            if (pd != null)
            {
                vtemplate = RuntimeSingleton.getTemplate( pd.getId() ); 
            }
            else
            {
                // maybe its in preview mode and doesn't exist yet?
                vtemplate = RuntimeSingleton.getTemplate( pageName );                        
            }
            
            if (vtemplate == null)
            {
                ret = pageName + " : No Page or Template found.";
            }
            else
            {
                // Run it through Velocity
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                VelocityContext vcontext = new VelocityContext();
                ContextLoader.setupContext(vcontext,rreq,
                    (HttpServletResponse)mPageContext.getResponse());
                
                vtemplate.merge( vcontext, pw );           
                ret = sw.toString();
            }
        } 
        catch (Exception e)
        {
            ret = "ERROR: including page " + pageName;
        } 
        finally
        {
            mIncludePageTLS.set( null );
        }
        return ret;
    }

    //------------------------------------------------------------------------
    /**
     * Shows HTML for an image in user's resource directory.
     * @param fileName File name in resource directory
     * @param linkUrl  URL that image links to, null or empty if none
     * @param alt      Description of image and of link
     * @param border   For IMG tag's border attribute
     * @param halign   For IMG tag's halign attribute
     * @param valign   For IMG tag's valign attribute
     * @return String  HTML for displaying image
     */
    public String showResourceImage( String fileName, String linkUrl,
        String alt, int border, String halign, String valign )
    {
        HttpServletRequest request =
            (HttpServletRequest) mPageContext.getRequest();
        String username = getRollerRequest().getUser().getUserName();
        ServletContext app = mPageContext.getServletContext();

        StringBuffer sb = new StringBuffer();
        String uploadPath = null;
        try {
            uploadPath = RollerFactory.getRoller().getFileManager().getUploadUrl();
        } catch(Exception e) {}
        
        if ( uploadPath != null && uploadPath.trim().length() > 0 )
        {
            sb.append( uploadPath );
            sb.append( "/");
            sb.append( username );
            sb.append( "/");
            sb.append( fileName);
        }
        else
        {
            sb.append( request.getContextPath() );
            sb.append( RollerContext.USER_RESOURCES );
            sb.append( "/");
            sb.append( username );
            sb.append( "/");
            sb.append( fileName);
        }
        return showImage( sb.toString(),linkUrl,alt,border,halign,valign );
    }

    //------------------------------------------------------------------------
    /**
     * Shows HTML for an image in user's resource directory.
     * @param imageUrl URL to image
     * @param linkUrl  URL that image links to, null or empty if none
     * @param alt      Description of image and of link
     * @param border   For IMG tag's border attribute
     * @param halign   For IMG tag's halign attribute
     * @param valign   For IMG tag's valign attribute
     * @return String  HTML for displaying image
     */
    public String showImage( String imageUrl, String linkUrl,
        String alt, int border, String halign, String valign )
    {
        StringBuffer sb = new StringBuffer();
        if ( linkUrl!=null && linkUrl.trim().length()>0 )
        {
            sb.append( "<a href=\"");
            sb.append( linkUrl );
            sb.append( "\" >");
        }

        sb.append("<img src=\"");
        sb.append( imageUrl );
        sb.append( "\" alt=\"");
        sb.append( alt);
        sb.append( "\" border=\"");
        sb.append( border);
        sb.append( "\" halign=\"");
        sb.append( halign);
        sb.append( "\" valign=\"");
        sb.append( valign);
        sb.append( "\" />");

        if ( linkUrl!=null && linkUrl.trim().length()>0 )
        {
            sb.append( "</a>");
        }

        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Gets path to images directory on your Roller server.
     * @return Path to images directory on your Roller server.
     */
    public String showImagePath()
    {
        HttpServletRequest request =
            (HttpServletRequest) mPageContext.getRequest();
            
        StringBuffer sb = new StringBuffer();
        sb.append( request.getContextPath() );
        sb.append( "/images" );
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Gets path to your /resources directory, where you may have uploaded 
     * files.
     * @return Resource path.
     */
    public String showResourcePath()
    {
        HttpServletRequest request =
            (HttpServletRequest) mPageContext.getRequest();
            
        String username = getRollerRequest().getUser().getUserName();
        
        ServletContext app = mPageContext.getServletContext();
                
        StringBuffer sb = new StringBuffer();
        String uploadPath = null;
        try {
            uploadPath = RollerFactory.getRoller().getFileManager().getUploadUrl();
        } catch(Exception e) {}
        if ( uploadPath != null && uploadPath.trim().length() > 0 )
        {
            sb.append( uploadPath );
        }
        else
        {
            sb.append( request.getContextPath() );
            sb.append( RollerContext.USER_RESOURCES );
        }
        sb.append( "/" );
        sb.append( username );
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * This is a convenience method to calculate the path to a theme. It
     * basically adds a contextPath and the themes directory.
     * @param Name of theme.
     * @return Theme path.
     */
    public String showThemePath( String theme )
    {
        String themesdir = RollerRuntimeConfig.getProperty("users.themes.path");
        
        HttpServletRequest request =
            (HttpServletRequest)mPageContext.getRequest();
        StringBuffer sb = new StringBuffer();
        sb.append( request.getContextPath());
        sb.append(themesdir);
        sb.append( "/" );
        sb.append( theme );
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * This is a convenience method to calculate the path to a theme image.
     * @param theme Name of theme.
     * @param imageName Name of image.
     * @return Path to image in theme.
     */    
    public String showThemeImagePath( String theme, String imageName )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( showThemePath(theme));
        sb.append( "/images/");
        sb.append( imageName);
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Display a theme image.
     * @param theme Name of theme.
     * @param imageName Name of image. 
     * @return HTML for image.
     */    
    public String showThemeImage( String theme, String imageName )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "<img alt=\""+imageName+"\" src=\"");
        sb.append( showThemeImagePath(theme, imageName));
        sb.append( "\"/>");
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Return the path to a theme's styles directory.
     * @param theme Name of theme.
     * @param stylesheet Name of stylesheet.
     * @return Theme style path.
     */
    public String showThemeStylePath( String theme, String stylesheet )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( showThemePath(theme));
        sb.append( "/styles/");
        sb.append( stylesheet);
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Return HTML for referencing a theme.
     * @param theme Name of theme
     * @param stylesheet Name of stylesheet in theme.
     * @param useImport Use import statement rather than link element.
     * @return HTML for importing theme.
     */
    public String showThemeStyle(
        String theme, String stylesheet, boolean useImport )
    {
        StringBuffer sb = new StringBuffer();
        if (useImport) {
            sb.append( "<style type=\"text/css\">");
            sb.append( "@import url(");
            sb.append( showThemeStylePath(theme, stylesheet));
            sb.append( ");");
            sb.append( "</style>");
        } else {
            sb.append( "<link rel=\"stylesheet\" type=\"text/css\" href=\"");
            sb.append( showThemeStylePath(theme, stylesheet));
            sb.append( "\" />");
        }
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Convenience macro to import a stylesheet using the import statement.
     * @param theme Theme name.
     * @param stylesheet Name of stylesheet within theme.
     * @return HTML for importing theme.
     */
    public String showThemeStyleImport( String theme, String stylesheet )
    {
        return showThemeStyle(theme, stylesheet, true);
    }

    //------------------------------------------------------------------------
    /**
     * Return the path to a theme's scripts directory.
     * @param theme Name of theme.
     * @param scriptFile Name of script in theme.
     * @return Path to theme.
     */
    public String showThemeScriptPath( String theme, String scriptFile )
    {
        StringBuffer sb = new StringBuffer();
        sb.append( showThemePath(theme));
        sb.append( "/scripts/");
        sb.append( scriptFile);
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Return the full HTML to use a scriptFile, for example:
     * <pre>
     * <script type="text/javascript" src="/themes/default/scripts/sample.js">
     * </script>.
     * </pre>
     * @param theme Name of theme.
     * @param scriptFile Name of script in theme.
     * @return Path to theme.
     */
    public String showThemeScript( String theme, String scriptFile)
    {
        StringBuffer sb = new StringBuffer();
        sb.append( "<script type=\"text/javascript\" src=\"");
        sb.append( showThemeScriptPath(theme, scriptFile));
        sb.append( "\"></script>"); // a /> doesn't work in IE
        return sb.toString();
    }

    //------------------------------------------------------------------------
    /**
     * Return the title of the current Roller page being processed.
     * @return Title of the current Roller page being processed.
     */
    public String showPageName()
    {
        WeblogTemplate pd = null;
        RollerRequest rreq = getRollerRequest();
        try
        {
            pd = rreq.getPage();
        }
        catch (Exception e) {
            return "ERROR finding page in request: " + e.toString();
        }
        return pd.getName();
    }

    //------------------------------------------------------------------------
    /**
     * Return the description of the current Roller page being processed.
     * @return Description of the current Roller page being processed.
     */
    public String showPageDescription()
    {
        WeblogTemplate pd = null;
        RollerRequest rreq = getRollerRequest();
        try
        {
            pd = rreq.getPage();
        }
        catch (Exception e)
        {
            return "ERROR finding page in request: " + e.toString();
        }
        return pd.getDescription();
    }

    //------------------------------------------------------------------------
    /**
     * Return the updateTime of the current Roller page being processed.
     * @return UpdateTime of the current Roller page being processed.
     */
    public String showPageUpdateTime()
    {
        WeblogTemplate pd = null;
        RollerRequest rreq = getRollerRequest();
        try
        {
            pd = rreq.getPage();
        }
        catch (Exception e)
        {
            return "ERROR finding page in request: " + e.toString();
        }
        if (pd.getUpdateTime() == null) return "";
        return pd.getUpdateTime().toString();
    }

    //------------------------------------------------------------------------
    /** 
     * Show list of links to today's biggest referers. Shows up to up 
     * referers and limits each to 20 characters width.
     * @return HTML to display referers.
     */
    public String showReferers()
    {
        return showReferers(15,20);
    }

    //------------------------------------------------------------------------
    /** 
     * Show list of links to today's biggest referers. Shows up to up 
     * specified number referers and limits each to 20 characters width.
     * @param max Maximum number of referers to display.
     * @return HTML to display referers.
     */
    public String showReferers( int max )
    {
        return showReferers(max,20);
    }

    //------------------------------------------------------------------------
    /** 
     * Show list of links to today's biggest referers. Shows up to up 
     * specified number referers and limits each to specified characters width.
     * @param max Maximum number of referers to display.
     * @param maxWidth Maximum width in characters of each referer.
     * @return HTML to display referers.
     */
    public String showReferers( int max, int maxWidth )
    {
        try
        {
            RollerRequest rreq = getRollerRequest();
            RefererManager refmgr = rreq.getRoller().getRefererManager();

            StringBuffer sb = new StringBuffer();
            
            sb.append("Today's Page Hits:");
            sb.append( refmgr.getDayHits(rreq.getWebsite()) );
            sb.append("<br/>");
            sb.append("Total Page Hits:");
            sb.append( refmgr.getTotalHits(rreq.getWebsite()) );
            sb.append("<br/>");

            List refs = refmgr.getTodaysReferers(rreq.getWebsite());
            sb.append("");
            sb.append("<ul class=\"rReferersList\">");
            int count = refs.size()>max ? max : refs.size();
            for (int i = 0; i < count; i++)
            {
            	RefererData data = (RefererData)refs.get(i);
                sb.append("<li class=\"rReferersListItem\">");
                sb.append( data.getDisplayUrl( maxWidth, true ) );
                sb.append("</li>");
            }
            sb.append("</ul>");

            return sb.toString();
        }
        catch (Exception e)
        {
            mLogger.error("Displaying referers list",e);
            return "ERROR: displaying referers list";
        }
    }
    
    //------------------------------------------------------------------------
    /**
     * Remove occurences of HTML from a string. Does this by stripping out
     * any text that exists between the characters "&lt;" and "&gt;".
     * @param s String that may contain some HTML.
     * @return String with no HTML.
     */
    public static String removeHTML(String s)
    {
        if ( s==null ) return "";
        else return Utilities.removeHTML(s);
    }

    /** 
     * Escape all characters that need to be escaped for HTML.
     * @param s String that may contain some special HTML characters.
     * @return String with special HTML characters escaped.
     */
    public static String escapeHTML( String s )
    {
        if ( s==null ) return "";
        else return Utilities.escapeHTML(s);
    }

    /** Run both removeHTML and escapeHTML on a string.
     * @param s String to be run through removeHTML and escapeHTML.
     * @return String with HTML removed and HTML special characters escaped.
     */
    public static String removeAndEscapeHTML( String s )
    {
        if ( s==null ) return "";
        else return Utilities.escapeHTML( removeHTML(s) );
    }
}


