package org.roller.presentation.velocity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.BookmarkManager;
import org.roller.model.RefererManager;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.BookmarkComparator;
import org.roller.pojos.FolderData;
import org.roller.pojos.WeblogTemplate;
import org.roller.pojos.RefererData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;
import org.roller.util.StringUtils;

/**
 * Provides Roller page templates with access to Roller domain model objects.
 *  
 * @author llavandowska
 * @author David M Johnson
 */
public class PageModel
{
    public final static String VELOCITY_NULL = "nil";
    
    protected static Log mLogger = 
       LogFactory.getFactory().getInstance(PageModel.class);
    
    private BookmarkManager      mBookmarkMgr = null;
    private WeblogManager        mWeblogMgr = null;
    private UserManager          mUserMgr = null;
    private RefererManager       mRefererMgr = null;

    private Map                mCategories = new HashMap();
    private HashMap              mPageMap = new HashMap();
    private RollerRequest        mRollerReq = null;
    private String               mUsername = null;
    
    private WeblogEntryData      mNextEntry = null;
    private WeblogEntryData      mPreviousEntry = null;

    private WeblogEntryData      mLastEntry = null;

    private WeblogEntryData mFirstEntry;
        
    //------------------------------------------------------------------------
    
    /** init() must be called to complete construction */
    public PageModel() {}
    
    /**
     * Initialize PageModel and allow PageModel to initialized VelocityContext.
     * @param rreq
     * @param ctx
     */
    public void init(RollerRequest rreq)
    {
        mRollerReq = rreq;
        UserData user = null;
        if ( rreq.getRequest().getAttribute(RollerRequest.OWNING_USER) != null)
        {
            user = (UserData)
                rreq.getRequest().getAttribute(RollerRequest.OWNING_USER);
        }
        else if ( rreq.getUser() != null )
        {
            user = rreq.getUser();
        }
        if ( user != null )
        {
            mUsername = user.getUserName();
        }
        
        try
        {
            mBookmarkMgr = rreq.getRoller().getBookmarkManager();
            mRefererMgr  = rreq.getRoller().getRefererManager();
            mUserMgr     = rreq.getRoller().getUserManager();
            mWeblogMgr   = rreq.getRoller().getWeblogManager();
            
            /** 
             * Preload what we can for encapsulation.  What we cannot preload we
             * will use the Managers later to fetch.
             */
            if ( mUsername != null )
            {
                // Get the pages, put into context & load map
                WebsiteData website = mUserMgr.getWebsite(user.getUserName());
                List pages = mUserMgr.getPages(website);
                Iterator pageIter = pages.iterator();
                while (pageIter.hasNext())
                {
                    WeblogTemplate page = (WeblogTemplate) pageIter.next();
                    mPageMap.put(page.getName(), page);
                }
            }
            
        }
        catch (RollerException e)
        {
            mLogger.error("PageModel Roller get*Manager Exception", e);
        }
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates folder.getBookmarks() & sorting */
    public Collection getBookmarks(FolderData folder)
    {
        Collection bookmarks = folder.getBookmarks();
        List list = new ArrayList(bookmarks);
        Collections.sort( list, new BookmarkComparator() );
        return list;
    }
    
    //------------------------------------------------------------------------
    
    /** Get top level bookmark folders. */
    public Collection getTopLevelFolders()
    {
        Collection tops = null;
        try
        {
         tops= mBookmarkMgr.getRootFolder(
                    mUserMgr.getWebsite(mUsername)).getFolders();
        }
        catch (RollerException e)
        {
            tops = new ArrayList();
        }
        return tops;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates WeblogManager.getComments().size() */
    public int getCommentCount(String entryId)
    {
        try
        {
            return mWeblogMgr.getComments( entryId ).size();
        }
        catch (RollerException e)
        {
            mLogger.error("PageModel getCommentCount()", e);
        }
        return 0;
    }
    
    //------------------------------------------------------------------------
    
    /** Get comments for weblog entry specified by request */
    public List getComments( WeblogEntryData entry )
    {
        try
        {
            return mWeblogMgr.getComments( entry.getId() );
        }
        catch (RollerException e)
        {
            mLogger.error("PageModel getComments()", e);
        }
        return new ArrayList();
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public int getDayHits()
    {
        try
        {
            return mRefererMgr.getDayHits(mRollerReq.getWebsite());
        }
        catch (RollerException e)
        {
            mLogger.error("PageModel getDayHits()", e);
        }
        return 0;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates BookmarkManager.getFolder() */
    public FolderData getFolder(String folderPath)
    {
        try
        {
            return mBookmarkMgr.getFolder(
                mUserMgr.getWebsite(mUsername), folderPath);
        }
        catch (RollerException e)
        {
            mLogger.error("PageModel getFolder()", e);
        }
        return null;
    }
        
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public WeblogTemplate getUsersPageByName(WebsiteData website, String pageName)
    {
        WeblogTemplate page = null;
        try
        {
            if (website == null) 
                throw new NullPointerException("website is null");
                
            if (pageName == null) 
                throw new NullPointerException("pageName is null");
                
            page = mUserMgr.getPageByName(website, pageName);
        }
        catch (NullPointerException npe)
        {
            mLogger.warn(npe.getMessage());
        }
        catch (RollerException e)
        {
            mLogger.error("ERROR getting user's page by name: " + e.getMessage(),e);
        }
        return page;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public WeblogTemplate getPageByName(String pageName)
    {
        return (WeblogTemplate)mPageMap.get(pageName);
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public String getPageIdByName(String pageName)
    {
        WeblogTemplate pd = (WeblogTemplate)mPageMap.get(pageName);
        if ( pd != null ) 
        {
            return pd.getId();
        }
        else
        {
            return null;
        }
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Get collection of user pages.
     * @return
     */
    public Object getPages()
    {
        return mPageMap.values();
    }
    
    //------------------------------------------------------------------------
    
    /** 
     * Returns a map of up to 100 recent weblog entries for the user and day 
     * specified in the request, filtered by the category specified by the 
     * request, limited by the 'maxEntries' argument, and sorted by reverse 
     * chronological order.
     * 
     * <p>This method will look for a category name in the following places
     * and in the following order:</p>
     * <ul>
     * <li>The request via RollerRequest.getWeblogCategory().</li>
     * <li>The categoryName argument to this method.</li>
     * <li>The default category for the website specified by the request via 
     *     RollerRequest.getWebsite().getDefaultCategory().</li>
     * <li></li>
     * </ul>
     * 
     * @param maxEntries Maximum number of entries to be returned.
     * @param categoryName Only return entries from this category and it's 
     *         subcategories. If null, returns all categories of entry.
     * @return Map of Lists of WeblogEntryData, keyed by 8-char date strings.
     */
    public Map getRecentWeblogEntries(int maxEntries, String categoryName)
    {
        if (VELOCITY_NULL.equals(categoryName)) categoryName = null;
        Map ret = new HashMap();
        try
        {
            Date day = mRollerReq.getDate();
            if (day == null) day = new Date();
            
            // If request specifies a category, then use that
            String catParam = null;
            if (mRollerReq.getWeblogCategory() != null)
            {
                catParam = mRollerReq.getWeblogCategory().getPath();
            }
            else if (categoryName != null)
            {
                // use category argument instead
                catParam = categoryName;
            }
            else if (mRollerReq.getWebsite() != null) // MAIN
            {
                catParam = mRollerReq.getWebsite().getDefaultCategory().getPath();
                if (catParam.equals("/"))
                {
                    catParam = null;
                }
            }
            
            ret = mRollerReq.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                            mRollerReq.getWebsite(),  
                            null,                     // startDate
                            day,                 // endDate
                            catParam,                 // catName
                            WeblogManager.PUB_ONLY,   // status
                            new Integer(maxEntries)); // maxEntries
            
            setFirstAndLastEntries( ret );
        }
        catch (Exception e)
        {
            mLogger.error("PageModel getRecentWeblogEntries()", e);
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Pull the last WeblogEntryData out of the Map.
     * @param ret
     */
    private void setFirstAndLastEntries(Map days)
    {
        int numDays = days.keySet().size();
        if (numDays > 0) // there is at least one day
        {
            // get first entry in map
            Object[] keys = days.keySet().toArray(new Object[numDays]);
            List vals = (List)days.get( keys[0] );
            int valSize = vals.size();
            if (valSize > 0) 
            {
                mFirstEntry = (WeblogEntryData)vals.get(0);
            }
            
            // get last entry in map
            vals = (List)days.get( keys[--numDays] );
            valSize = vals.size();
            if (valSize > 0)
            {
                mLastEntry = (WeblogEntryData)vals.get(--valSize);
            }
        }
    }

    //------------------------------------------------------------------------
    
    /** 
     * Returns list of recent weblog entries for the user and day specified in 
     * the request, filtered by the category specified by the request, limited 
     * by the 'maxEntries' argument, and sorted by reverse chronological order.
     * 
     * <p>This method will look for a category name in the same places and 
     * same order as does the getRecentWeblogEntries() method.</p>
     * 
     * @param maxEntries   Maximum number of entries to be returned.
     * @param categoryName Only return entries from this category and it's 
     *         subcategories. If null, returns all categories of entry.
     * @return List of WeblogEntryData objects in revese chronological order.
     */
    public List getRecentWeblogEntriesArray(int maxEntries, String categoryName)
    {
        if (VELOCITY_NULL.equals(categoryName)) categoryName = null;
        List ret = new ArrayList();
        try
        {
            Date day = mRollerReq.getDate();
            if (day == null) day = new Date();
            
            // If request specifies a category, then use that
            String catParam = null;
            if (mRollerReq.getWeblogCategory() != null)
            {
                catParam = mRollerReq.getWeblogCategory().getPath();
            }
            else if (categoryName != null)
            {
                // use category argument instead
                catParam = categoryName;
            }
            else if (mRollerReq.getWebsite() != null) // MAIN
            {
                catParam = mRollerReq.getWebsite().getDefaultCategory().getPath();
                if (catParam.equals("/"))
                {
                    catParam = null;
                }
            }
            WeblogManager mgr = mRollerReq.getRoller().getWeblogManager();
            
            //ret = mgr.getRecentWeblogEntriesArray( 
                //name, day, catParam, maxEntries, true );
            
            ret = mgr.getWeblogEntries(
                            mRollerReq.getWebsite(), 
                            null,                    // startDate
                            day,                      // endDate
                            catParam,                 // catName
                            WeblogManager.PUB_ONLY,   // status
                            new Integer(maxEntries)); // maxEntries
        }
        catch (Exception e)
        {
            mLogger.error("PageModel getRecentWeblogEntries()", e);
        }
        return ret;
    }   
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager **/
    public List getReferers(String date)
    {
        date = date.trim();
        ArrayList referers = new ArrayList();
        try
        {
            List refs = 
                mRefererMgr.getReferersToDate(mRollerReq.getWebsite(), date);
            
            for (Iterator rdItr = refs.iterator(); rdItr.hasNext();) {
                RefererData referer = (RefererData) rdItr.next();
                String title =referer.getTitle();
                String excerpt = referer.getExcerpt();
                if (   StringUtils.isNotEmpty(title) 
                    && StringUtils.isNotEmpty(excerpt) )
                {
                    if (   referer.getVisible().booleanValue() 
                        || this.mRollerReq.isUserAuthorizedToEdit() )
                    { 
                        referers.add(referer);
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            mLogger.error("PageModel getReferersToDate() fails with URL" 
                            + mRollerReq.getRequestURL(), e);
        }
        return referers;
    }   
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public List getTodaysReferers()
    {
        List referers = null;
        try
        {
            referers = mRefererMgr.getTodaysReferers(mRollerReq.getWebsite());
         
        }
        catch (RollerException e)
        {
            mLogger.error("PageModel getTodaysReferers()", e);
        }
        return (referers == null ? Collections.EMPTY_LIST : referers);
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public int getTotalHits()
    {
        try
        {
            return mRefererMgr.getTotalHits(mRollerReq.getWebsite());
        }
        catch (RollerException e)
        {
            mLogger.error("PageModel getTotalHits()", e);
        }
        return 0;
    }
    
    //------------------------------------------------------------------------
    /**
     * Returns most recent update time of collection of weblog entries.
     * @param weblogEntries Collection of weblog entries.
     * @return Most recent update time.
     */
    public static Date getUpdateTime( ArrayList weblogEntries )
    {
        Date updateTime = null;
        Iterator iter = weblogEntries.iterator();
        while (iter.hasNext())
        {
            WeblogEntryData wd = (WeblogEntryData)iter.next();
            if ( updateTime == null )
            {
                updateTime = wd.getUpdateTime();
            }
            //else if ( updateTime.compareTo(wd.getUpdateTime()) < 0 )
            else if (updateTime.before( wd.getUpdateTime() ))
            {
                updateTime = wd.getUpdateTime();
            }
        }
        return updateTime;
    }

    //------------------------------------------------------------------------
    
    /** Encapsulates WeblogManager.getWeblogCategories() */
    public List getWeblogCategories(String categoryName)
    {
        List ret = null;
        if (VELOCITY_NULL.equals(categoryName)) categoryName = null;
        
        // Make sure we have not already fetched this category.
        if (categoryName != null)
        {
            ret = (List)mCategories.get(categoryName);
        }
        
        if (null == ret) 
        {
            try 
            {
                WeblogCategoryData category = null;                
                if (categoryName != null)
                {
                    category = mWeblogMgr.getWeblogCategoryByPath(
                                  mRollerReq.getWebsite(), null, categoryName);                    
                }
                else 
                {
                    category = mRollerReq.getWebsite().getDefaultCategory();
                }
                ret = category.getWeblogCategories();
                mCategories.put(categoryName, ret);
            }
            catch (RollerException e) 
            {
                mLogger.error(e);
            }
        }       
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RollerRequest.getWeblogEntry() */
    public WeblogEntryData getWeblogEntry()
    {
        return mRollerReq.getWeblogEntry();
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Get the next occurring Entry.
     */
    public WeblogEntryData getNextEntry()
    {
        WeblogEntryData currentEntry = getWeblogEntry();
        if (mFirstEntry != null) currentEntry = mFirstEntry;
        if (mNextEntry == null && currentEntry != null) 
        {
            String catName = null;
            if (mRollerReq.getWeblogCategory() != null)
            {
                catName = mRollerReq.getWeblogCategory().getName();
            }
            try
            {
                mNextEntry = mWeblogMgr.getNextEntry(currentEntry, catName);
                
                // make sure that mNextEntry is not published to future
                if (mNextEntry != null && 
                    mNextEntry.getPubTime().after( new Date() ))
                {
                    mNextEntry = null;
                }
            }
            catch (RollerException e)
            {
                mLogger.error("PageModel.getNextEntry)", e);
            }
        }
        return mNextEntry;
    }

    //------------------------------------------------------------------------
    
    /**
     * Get the previous occurring Entry.
     */
    public WeblogEntryData getPreviousEntry()
    {
        WeblogEntryData currentEntry = getWeblogEntry();
        if (mLastEntry != null) currentEntry = mLastEntry;
        if (mPreviousEntry == null && currentEntry != null )
        {
            String catName = null;
            if (mRollerReq.getWeblogCategory() != null)
            {
                catName = mRollerReq.getWeblogCategory().getName();
            }
            try
            {
                mPreviousEntry = mWeblogMgr.getPreviousEntry(currentEntry, catName);
            }
            catch (RollerException e)
            {
                mLogger.error("PageModel.getPreviousEntry)", e);
            }            
        }
        return mPreviousEntry;
    }

    //------------------------------------------------------------------------
    
    public boolean isUserAuthorizedToEdit()
    {
        try
        {
            return mRollerReq.isUserAuthorizedToEdit();
        }
        catch (Exception e)
        {
            mLogger.warn("PageModel.isUserAuthorizedToEdit)", e);
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    
    public String getRequestParameter(String key)
    {
        return mRollerReq.getRequest().getParameter(key);
    }
    
    //------------------------------------------------------------------------
    
    public FolderData getFolderByPath(String path)
    {
        try
        {
            return mBookmarkMgr.getFolderByPath(
                mUserMgr.getWebsite(mUsername), null, path);
        }
        catch (RollerException e)
        {
            mLogger.error(e);
            return null;
        }
    }

    /**
     * Facade for WeblogManager.getRecentComments().
     * Get the most recent (chronologically) posted Comments
     * for this website, limited to maxCount.  
     * @return List of Comments.
     */
    public List getRecentComments(int maxCount)
    {
        try
        {
            return mWeblogMgr.getRecentComments(mRollerReq.getWebsite(), maxCount);
        }
        catch (RollerException e)
        {
            mLogger.error(e);
            return new ArrayList();
        }
    }
 
    public boolean getEmailComments() 
    {
        WebsiteData website = mRollerReq.getWebsite();
        boolean emailComments = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
        
        return (website.getEmailComments().booleanValue() && emailComments);
    }
}
