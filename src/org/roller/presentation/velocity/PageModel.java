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
import org.roller.model.RollerFactory;
import org.roller.pojos.Template;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.FolderData;
import org.roller.pojos.RefererData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.wrapper.CommentDataWrapper;
import org.roller.pojos.wrapper.FolderDataWrapper;
import org.roller.pojos.wrapper.RefererDataWrapper;
import org.roller.pojos.wrapper.TemplateWrapper;
import org.roller.pojos.wrapper.WeblogCategoryDataWrapper;
import org.roller.pojos.wrapper.WeblogEntryDataWrapper;
import org.roller.pojos.wrapper.WebsiteDataWrapper;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.util.DateUtil;
import org.roller.util.StringUtils;

/**
 * Provides Roller page templates with access to Roller domain model objects.
 *
 * @author llavandowska
 * @author David M Johnson
 */
public class PageModel {
    public final static String VELOCITY_NULL = "nil";
    
    protected static Log mLogger =
            LogFactory.getFactory().getInstance(PageModel.class);
    
    private BookmarkManager      mBookmarkMgr = null;
    private WeblogManager        mWeblogMgr = null;
    private UserManager          mUserMgr = null;
    private RefererManager       mRefererMgr = null;
    
    private Map                  mCategories = new HashMap();
    private HashMap              mPageMap = new HashMap();
    private RollerRequest        mRollerReq = null;
    private String               mHandle = null;
    private WebsiteData          mWebsite = null;
    private WeblogEntryDataWrapper      mNextEntry = null;
    private WeblogEntryDataWrapper      mPreviousEntry = null;
    private WeblogEntryDataWrapper      mLastEntry = null;
    private WeblogEntryDataWrapper      mFirstEntry = null;
    
    //------------------------------------------------------------------------
    
    /** init() must be called to complete construction */
    public PageModel() {}
    
    /**
     * Initialize PageModel and allow PageModel to initialized VelocityContext.
     * @param rreq
     * @param ctx
     */
    public void init(RollerRequest rreq) {
        mRollerReq = rreq;
        if ( rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE) != null) {
            mWebsite = (WebsiteData)
            rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE);
            mHandle = mWebsite.getHandle();
        } else if ( rreq.getWebsite() != null ) {
            mWebsite = rreq.getWebsite();
            mHandle = mWebsite.getHandle();
        }
        
        try {
            mBookmarkMgr = RollerFactory.getRoller().getBookmarkManager();
            mRefererMgr  = RollerFactory.getRoller().getRefererManager();
            mUserMgr     = RollerFactory.getRoller().getUserManager();
            mWeblogMgr   = RollerFactory.getRoller().getWeblogManager();
            
            // Preload what we can for encapsulation.  What we cannot preload we
            // will use the Managers later to fetch.
            
            // Get the pages, put into context & load map
            if (mWebsite != null) {
                // if we have website from RollerRequest, use it
                mWebsite = rreq.getWebsite();
                
                // Get the pages, put into context & load map
                List pages = mWebsite.getPages();
                Iterator pageIter = pages.iterator();
                while (pageIter.hasNext()) {
                    Template page = (Template) pageIter.next();
                    mPageMap.put(page.getName(), TemplateWrapper.wrap(page));
                }
            }
        } catch (RollerException e) {
            mLogger.error("PageModel Roller get*Manager Exception", e);
        }
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates folder.getBookmarks() & sorting */
    public Collection getBookmarks(FolderDataWrapper folder) {
        Collection bookmarks = null;
        if (folder != null) {
            mLogger.debug("Getting bookmarks for folder : "+folder.getName());

            // since we already have a wrapped pojo we know the output
            // will be wrapped as well :)
            bookmarks = folder.getBookmarks();

            // TODO: need to setup new BookmarkWrapperComparator
            //List mBookmarks = new ArrayList(bookmarks);
            //Collections.sort( mBookmarks, new BookmarkComparator() );
        }
        return bookmarks;
    }
    
    //------------------------------------------------------------------------
    
    /** Get top level bookmark folders. */
    public Collection getTopLevelFolders() {
        List tops = null;
        try {
            Collection mTops = mBookmarkMgr.getRootFolder(
                    mUserMgr.getWebsiteByHandle(mHandle)).getFolders();
            
            // wrap pojos
            tops = new ArrayList(mTops.size());
            Iterator it = mTops.iterator();
            int i=0;
            while(it.hasNext()) {
                tops.add(i, FolderDataWrapper.wrap((FolderData) it.next()));
                i++;
            }
        } catch (RollerException e) {
            tops = new ArrayList();
        }
        return tops;
    }
    
    //------------------------------------------------------------------------
    
    /** Get number of approved non-spam comments for entry */
    public int getCommentCount(String entryId) {
        return getCommentCount(entryId, true, true);
    }
    
    /** Get number of approved non-spam comments for entry */
    public int getCommentCount(String entryId, boolean noSpam, boolean approvedOnly) {
        try {
            WeblogEntryData entry = mWeblogMgr.retrieveWeblogEntry(entryId);
            return entry.getComments(noSpam, approvedOnly).size();
        } catch (RollerException alreadyLogged) {}
        return 0;
    }
    
    //------------------------------------------------------------------------
    
    /** Get comments for weblog entry specified by request */
    public List getComments(WeblogEntryDataWrapper entry) {
        return getComments(entry, true, true);
    }
    
    /** Get comments for weblog entry specified by request */
    public List getComments(WeblogEntryDataWrapper wrapper, boolean noSpam, boolean approvedOnly) {
        WeblogEntryData entry = wrapper.getPojo();
        List comments = new ArrayList();
        List unwrappped = entry.getComments(noSpam, approvedOnly);
        comments = new ArrayList(unwrappped.size());
        Iterator it = unwrappped.iterator();
        while(it.hasNext()) {
            comments.add(CommentDataWrapper.wrap((CommentData)it.next()));
        }
        return comments;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public int getDayHits() {
        try {
            return mRefererMgr.getDayHits(mRollerReq.getWebsite());
        } catch (RollerException e) {
            mLogger.error("PageModel getDayHits()", e);
        }
        return 0;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates BookmarkManager.getFolder() */
    public FolderDataWrapper getFolder(String folderPath) {
        try {
            return FolderDataWrapper.wrap(
                    mBookmarkMgr.getFolder(
                    mUserMgr.getWebsiteByHandle(mHandle), folderPath));
        } catch (RollerException e) {
            mLogger.error("PageModel getFolder()", e);
        }
        return null;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public TemplateWrapper getUsersPageByName(WebsiteDataWrapper wrapper, String pageName) {
        WebsiteData website = wrapper.getPojo();
        TemplateWrapper page = null;
        try {
            if (website == null)
                throw new NullPointerException("website is null");
            
            if (pageName == null)
                throw new NullPointerException("pageName is null");
            
            page = TemplateWrapper.wrap(website.getPageByName(pageName));
        } catch (NullPointerException npe) {
            mLogger.warn(npe.getMessage());
        } catch (RollerException e) {
            mLogger.error("ERROR getting user's page by name: " + e.getMessage(),e);
        }
        return page;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public TemplateWrapper getPageByName(String pageName) {
        return (TemplateWrapper) mPageMap.get(pageName);
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public String getPageIdByName(String pageName) {
        mLogger.debug("looking up page ["+pageName+"]");
        
        String template_id = null;
        
        try {
            Template pd = mWebsite.getPageByName(pageName);
            if(pd != null) {
                template_id = pd.getId();
            }
        } catch(Exception e) {
            mLogger.error(e);
        }
        
        mLogger.debug("returning template id ["+template_id+"]");
        
        return template_id;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Get collection of user pages.
     * @return
     */
    public Object getPages() {
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
    public Map getRecentWeblogEntries(int maxEntries, String categoryName) {
        if (VELOCITY_NULL.equals(categoryName)) categoryName = null;
        Map ret = new HashMap();
        try {

            
            // If request specifies a category, then use that
            String catParam = null;
            if (mRollerReq.getWeblogCategory() != null) {
                catParam = mRollerReq.getWeblogCategory().getPath();
            } else if (categoryName != null) {
                // use category argument instead
                catParam = categoryName;
            } else if (mRollerReq.getWebsite() != null) // MAIN
            {
                catParam = mRollerReq.getWebsite().getDefaultCategory().getPath();
                if (catParam.equals("/")) {
                    catParam = null;
                }
            }
            
            Integer limit = new Integer(maxEntries);
            Date startDate = null;
            Date endDate = mRollerReq.getDate();
            if (endDate == null) endDate = new Date();
            if (mRollerReq.isDateSpecified()) { 
                // URL specified a specific day
                // so get entries for that day
                endDate = DateUtil.getEndOfDay(endDate);
                startDate = DateUtil.getStartOfDay(endDate); 
                // and get them ALL, no limit
                limit = null;                  
            }
            Map mRet = RollerFactory.getRoller().getWeblogManager().getWeblogEntryObjectMap(
                    mRollerReq.getWebsite(),
                    startDate,                    // startDate
                    endDate,                      // endDate
                    catParam,                     // catName
                    WeblogEntryData.PUBLISHED,    // status
                    limit);     // maxEntries
            
            // need to wrap pojos
            java.util.Date key = null;
            Iterator days = mRet.keySet().iterator();
            while(days.hasNext()) {
                key = (java.util.Date)days.next();
                
                // now we need to go through each entry in a day and wrap
                List wrappedEntries = new ArrayList();
                List entries = (List) mRet.get(key);
                for(int i=0; i < entries.size(); i++) {
                    wrappedEntries.add(i,
                            WeblogEntryDataWrapper.wrap((WeblogEntryData)entries.get(i)));
                }
                mRet.put(key, wrappedEntries);
            }
            
            ret = mRet;
            
            setFirstAndLastEntries( ret );
        } catch (Exception e) {
            mLogger.error("PageModel getRecentWeblogEntries()", e);
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Pull the last WeblogEntryData out of the Map.
     * @param ret
     */
    private void setFirstAndLastEntries(Map days) {
        int numDays = days.keySet().size();
        if (numDays > 0) // there is at least one day
        {
            // get first entry in map
            Object[] keys = days.keySet().toArray(new Object[numDays]);
            List vals = (List)days.get( keys[0] );
            int valSize = vals.size();
            if (valSize > 0) {
                mFirstEntry = (WeblogEntryDataWrapper)vals.get(0);
            }
            
            // get last entry in map
            vals = (List)days.get( keys[--numDays] );
            valSize = vals.size();
            if (valSize > 0) {
                mLastEntry = (WeblogEntryDataWrapper)vals.get(--valSize);
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
    public List getRecentWeblogEntriesArray(int maxEntries, String categoryName) {
        if (VELOCITY_NULL.equals(categoryName)) categoryName = null;
        List ret = new ArrayList();
        try {
            Date day = mRollerReq.getDate();
            if (day == null) day = new Date();
            
            // If request specifies a category, then use that
            String catParam = null;
            if (mRollerReq.getWeblogCategory() != null) {
                catParam = mRollerReq.getWeblogCategory().getPath();
            } else if (categoryName != null) {
                // use category argument instead
                catParam = categoryName;
            } else if (mRollerReq.getWebsite() != null) // MAIN
            {
                catParam = mRollerReq.getWebsite().getDefaultCategory().getPath();
                if (catParam.equals("/")) {
                    catParam = null;
                }
            }
            WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
            
            //ret = mgr.getRecentWeblogEntriesArray(
            //name, day, catParam, maxEntries, true );
            
            List mEntries = mgr.getWeblogEntries(
                    mRollerReq.getWebsite(),
                    null,                        // startDate
                    day,                         // endDate
                    catParam,                    // catName
                    WeblogEntryData.PUBLISHED,   // status
                    null,                        // sortby (null for pubTime)
                    new Integer(maxEntries));    // maxEntries
            
            // wrap pojos
            ret = new ArrayList(mEntries.size());
            Iterator it = mEntries.iterator();
            int i=0;
            while(it.hasNext()) {
                ret.add(i, WeblogEntryDataWrapper.wrap((WeblogEntryData) it.next()));
                i++;
            }
        } catch (Exception e) {
            mLogger.error("PageModel getRecentWeblogEntries()", e);
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager **/
    public List getReferers(String date) {
        date = date.trim();
        ArrayList referers = new ArrayList();
        try {
            List refs =
                    mRefererMgr.getReferersToDate(mRollerReq.getWebsite(), date);
            RollerSession rses =
                    RollerSession.getRollerSession(mRollerReq.getRequest());
            
            for (Iterator rdItr = refs.iterator(); rdItr.hasNext();) {
                RefererData referer = (RefererData) rdItr.next();
                String title =referer.getTitle();
                String excerpt = referer.getExcerpt();
                if (   StringUtils.isNotEmpty(title)
                && StringUtils.isNotEmpty(excerpt) ) {
                    if (   referer.getVisible().booleanValue()
                    || rses.isUserAuthorizedToAdmin(referer.getWebsite()) ) {
                        referers.add(RefererDataWrapper.wrap(referer));
                    }
                }
            }
            
        } catch (Exception e) {
            mLogger.error("PageModel getReferersToDate() fails with URL"
                    + mRollerReq.getRequestURL(), e);
        }
        return referers;
    }
    
    /** Encapsulates RefererManager **/
    public List getEntryReferers(WeblogEntryDataWrapper entry) {
        ArrayList referers = new ArrayList();
        try {
            List refs = mRefererMgr.getReferersToEntry(entry.getId());
            RollerSession rses =
               RollerSession.getRollerSession(mRollerReq.getRequest());
            
            for (Iterator rdItr = refs.iterator(); rdItr.hasNext();) {
                RefererData referer = (RefererData) rdItr.next();
                String title =referer.getTitle();
                String excerpt = referer.getExcerpt();
                if (   StringUtils.isNotEmpty(title)
                && StringUtils.isNotEmpty(excerpt) ) {
                    if (referer.getVisible().booleanValue()
                    || rses.isUserAuthorizedToAdmin(referer.getWebsite()) ) {
                        referers.add(RefererDataWrapper.wrap(referer));
                    }
                }
            }
            
        } catch (Exception e) {
            mLogger.error("PageModel getReferersToDate() fails with URL"
                    + mRollerReq.getRequestURL(), e);
        }
        return referers;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public List getTodaysReferers() {
        List referers = null;
        try {
            List mReferers = mRefererMgr.getTodaysReferers(mRollerReq.getWebsite());
            
            // wrap pojos
            referers = new ArrayList(mReferers.size());
            Iterator it = mReferers.iterator();
            int i=0;
            while(it.hasNext()) {
                referers.add(i, RefererDataWrapper.wrap((RefererData) it.next()));
                i++;
            }
            
        } catch (RollerException e) {
            mLogger.error("PageModel getTodaysReferers()", e);
        }
        return (referers == null ? Collections.EMPTY_LIST : referers);
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public int getTotalHits() {
        try {
            return mRefererMgr.getTotalHits(mRollerReq.getWebsite());
        } catch (RollerException e) {
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
    public static Date getUpdateTime( ArrayList weblogEntries ) {
        Date updateTime = null;
        Iterator iter = weblogEntries.iterator();
        while (iter.hasNext()) {
            // NOTE: this will need to be WeblogEntryDataWrapper
            WeblogEntryData wd = (WeblogEntryData)iter.next();
            if ( updateTime == null ) {
                updateTime = wd.getUpdateTime();
            }
            //else if ( updateTime.compareTo(wd.getUpdateTime()) < 0 )
            else if (updateTime.before( wd.getUpdateTime() )) {
                updateTime = wd.getUpdateTime();
            }
        }
        return updateTime;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates WeblogManager.getWeblogCategories() */
    public List getWeblogCategories(String categoryName) {
        List ret = null;
        if (VELOCITY_NULL.equals(categoryName)) categoryName = null;
        
        // Make sure we have not already fetched this category.
        if (categoryName != null) {
            ret = (List)mCategories.get(categoryName);
        } else {
            ret = (List)mCategories.get("zzz_null_zzz");
        }
        
        if (null == ret) {
            try {
                WeblogCategoryData category = null;
                if (categoryName != null) {
                    category = mWeblogMgr.getWeblogCategoryByPath(
                            mRollerReq.getWebsite(), null, categoryName);
                } else {
                    category = mRollerReq.getWebsite().getDefaultCategory();
                }
                
                List mRet = category.getWeblogCategories();
                
                // wrap pojos
                ret = new ArrayList(mRet.size());
                Iterator it = mRet.iterator();
                int i=0;
                while(it.hasNext()) {
                    ret.add(i, WeblogCategoryDataWrapper.wrap((WeblogCategoryData)it.next()));
                    i++;
                }
                if (categoryName != null) {
                    mCategories.put(categoryName, ret);
                } else {
                    mCategories.put("zzz_null_zzz", ret);
                }
            } catch (RollerException e) {
                mLogger.error(e);
            }
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RollerRequest.getWeblogEntry() */
    public WeblogEntryDataWrapper getWeblogEntry() {
        WeblogEntryData entry = mRollerReq.getWeblogEntry();
        
        if(entry != null && entry.getStatus().equals(WeblogEntryData.PUBLISHED))
            return WeblogEntryDataWrapper.wrap(entry);
        else
            return null;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Get the next occurring Entry.
     */
    public WeblogEntryDataWrapper getNextEntry() {
        WeblogEntryDataWrapper currentEntry = getWeblogEntry();
        if (mFirstEntry != null) currentEntry = mFirstEntry;
        if (mNextEntry == null && currentEntry != null) {
            String catName = null;
            if (mRollerReq.getWeblogCategory() != null) {
                catName = mRollerReq.getWeblogCategory().getName();
            }
            try {
                WeblogEntryData nextEntry =
                        mWeblogMgr.getNextEntry(currentEntry.getPojo(), catName);
                
                if(nextEntry != null)
                    mNextEntry = WeblogEntryDataWrapper.wrap(nextEntry);
                
                // make sure that mNextEntry is not published to future
                if (mNextEntry != null &&
                        mNextEntry.getPubTime().after( new Date() )) {
                    mNextEntry = null;
                }
            } catch (RollerException e) {
                mLogger.error("PageModel.getNextEntry)", e);
            }
        }
        return mNextEntry;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Get the previous occurring Entry.
     */
    public WeblogEntryDataWrapper getPreviousEntry() {
        WeblogEntryDataWrapper currentEntry = getWeblogEntry();
        if (mLastEntry != null) currentEntry = mLastEntry;
        if (mPreviousEntry == null && currentEntry != null ) {
            String catName = null;
            if (mRollerReq.getWeblogCategory() != null) {
                catName = mRollerReq.getWeblogCategory().getName();
            }
            try {
                WeblogEntryData prevEntry =
                        mWeblogMgr.getPreviousEntry(currentEntry.getPojo(), catName);
                
                if(prevEntry != null)
                    mPreviousEntry = WeblogEntryDataWrapper.wrap(prevEntry);
            } catch (RollerException e) {
                mLogger.error("PageModel.getPreviousEntry)", e);
            }
        }
        return mPreviousEntry;
    }
    
    //------------------------------------------------------------------------
    
    public boolean isUserAuthorizedToEdit() {
        try {
            RollerSession rses =
                    RollerSession.getRollerSession(mRollerReq.getRequest());
            if (rses.getAuthenticatedUser() != null
                    && mRollerReq.getWebsite() != null) {
                return rses.isUserAuthorizedToAuthor(mRollerReq.getWebsite());
            }
        } catch (Exception e) {
            mLogger.warn("PageModel.isUserAuthorizedToEdit()", e);
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    
    public boolean isUserAuthorizedToAdmin() {
        try {
            RollerSession rses =
                    RollerSession.getRollerSession(mRollerReq.getRequest());
            if (rses.getAuthenticatedUser() != null
                    && mRollerReq.getWebsite() != null) {
                return rses.isUserAuthorizedToAdmin(mRollerReq.getWebsite());
            }
        } catch (Exception e) {
            mLogger.warn("PageModel.isUserAuthorizedToAdmin()", e);
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    
    public boolean isUserAuthenticated() {
        return (mRollerReq.getRequest().getUserPrincipal() != null);
    }
    
    //------------------------------------------------------------------------
    
    public String getRequestParameter(String key) {
        return mRollerReq.getRequest().getParameter(key);
    }
    
    //------------------------------------------------------------------------
    
    public FolderDataWrapper getFolderByPath(String path) {
        try {
            FolderData folder = mBookmarkMgr.getFolderByPath(
                    mWebsite, null, path);
            
            if(folder != null)
                return FolderDataWrapper.wrap(folder);
        } catch (RollerException e) {
            mLogger.error(e);
        }
        
        return null;
    }
    
    /**
     * Facade for WeblogManager.getRecentComments().
     * Get the most recent (chronologically) posted Comments
     * for this website, limited to maxCount.
     * @return List of Comments.
     */
    public List getRecentComments(int maxCount) {
        List recentComments = new ArrayList();
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List recent = wmgr.getComments(
                    mWebsite,
                    null,  // weblog entry
                    null,  // search String
                    null,  // startDate
                    null,  // endDate
                    null,  // pending
                    Boolean.TRUE,  // approved only
                    Boolean.FALSE, // no spam
                    true,          // we want reverse chrono order
                    0,             // offset
                    maxCount);     // no limit
            
            // wrap pojos
            recentComments = new ArrayList(recent.size());
            Iterator it = recent.iterator();
            while(it.hasNext()) {
                recentComments.add(CommentDataWrapper.wrap((CommentData) it.next()));
            }
        } catch (RollerException e) {
            mLogger.error(e);
        }
        return recentComments;
    }
    
    public boolean getEmailComments() {
        if (mRollerReq != null) {
            WebsiteData website = mRollerReq.getWebsite();
            if (website != null) {
                boolean emailComments = RollerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");        
                return (website.getEmailComments().booleanValue() && emailComments);
            }
        }
        return false;
    }
}
