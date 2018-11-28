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

package org.apache.roller.weblogger.ui.rendering.velocity.deprecated;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryCommentWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogBookmarkFolderWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogReferrerWrapper;
import org.apache.roller.weblogger.pojos.wrapper.ThemeTemplateWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogCategoryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogEntryWrapper;
import org.apache.roller.weblogger.pojos.wrapper.WeblogWrapper;
import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogHitCount;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.WeblogPermission;


/**
 * Provides Roller page templates with access to Roller domain model objects.
 */
public class OldWeblogPageModel {
    public final static String VELOCITY_NULL = "nil";
    
    protected static Log mLogger =
            LogFactory.getFactory().getInstance(OldWeblogPageModel.class);
    
    private BookmarkManager      mBookmarkMgr = null;
    private WeblogEntryManager   mWeblogEntryMgr = null;
    private WeblogManager        mWeblogMgr = null;
    private UserManager          mUserMgr = null;
    private RefererManager       mRefererMgr = null;
    
    private Map                  mCategories = new HashMap();
    private HashMap              mPageMap = new HashMap();
    private HttpServletRequest   mRequest = null;
    private Weblog          mWebsite = null;
    private WeblogEntry      mEntry = null;
    private WeblogCategory   mCategory = null;
    private Date                 mDate = null;
    private boolean              mIsDaySpecified = false;
    private boolean              mIsMonthSpecified = false;
    private String               mLocale = null;
    private WeblogEntryWrapper      mNextEntry = null;
    private WeblogEntryWrapper      mPreviousEntry = null;
    private WeblogEntryWrapper      mLastEntry = null;
    private WeblogEntryWrapper      mFirstEntry = null;
    
    private URLStrategy urlStrategy = null;
    
    //------------------------------------------------------------------------
    
    /** init() must be called to complete construction */
    public OldWeblogPageModel() {}
    
    public String getModelName() {
        return "pageModel";
    }
    
    /**
     * Initialize PageModel and allow PageModel to initialized VelocityContext.
     */
    public void init(URLStrategy strat,
            HttpServletRequest request,
            Weblog website,
            WeblogEntry entry,
            WeblogCategory category,
            Date date,
            boolean isDay,
            boolean isMonth,
            String locale) {
        
        urlStrategy = strat;
        mRequest = request;
        
        // data we'll need in the methods
        mWebsite = website;
        mEntry = entry;
        mCategory = category;
        mDate = date;
        mIsDaySpecified = isDay;
        mIsMonthSpecified = isMonth;
        mLocale = locale;
        
        mBookmarkMgr = WebloggerFactory.getWeblogger().getBookmarkManager();
        mRefererMgr  = WebloggerFactory.getWeblogger().getRefererManager();
        mUserMgr     = WebloggerFactory.getWeblogger().getUserManager();
        mWeblogMgr   = WebloggerFactory.getWeblogger().getWeblogManager();
        mWeblogEntryMgr   = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        
        // Preload what we can for encapsulation.  What we cannot preload we
        // will use the Managers later to fetch.
        
        // Get the pages, put into context & load map
        if (mWebsite != null) {
            
            List pages = Collections.EMPTY_LIST;
            try {
                pages = mWebsite.getTheme().getTemplates();
            } catch (WebloggerException ex) {
                mLogger.error("error getting weblog pages", ex);
            }
            
            Iterator pageIter = pages.iterator();
            while (pageIter.hasNext()) {
                ThemeTemplate page = (ThemeTemplate) pageIter.next();
                mPageMap.put(page.getName(),ThemeTemplateWrapper.wrap(page));
            }
        }
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates folder.getBookmarks() & sorting */
    public Collection getBookmarks(WeblogBookmarkFolderWrapper folder) {
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
                    mWeblogMgr.getWeblogByHandle(mWebsite.getHandle())).getFolders();
            
            // wrap pojos
            tops = new ArrayList(mTops.size());
            Iterator it = mTops.iterator();
            int i=0;
            while(it.hasNext()) {
                tops.add(i,WeblogBookmarkFolderWrapper.wrap((WeblogBookmarkFolder) it.next()));
                i++;
            }
        } catch (WebloggerException e) {
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
            WeblogEntry entry = mWeblogEntryMgr.getWeblogEntry(entryId);
            return entry.getComments(noSpam, approvedOnly).size();
        } catch (WebloggerException alreadyLogged) {}
        return 0;
    }
    
    //------------------------------------------------------------------------
    
    /** Get comments for weblog entry specified by request */
    public List getComments(WeblogEntryWrapper entry) {
        return getComments(entry, true, true);
    }
    
    /** Get comments for weblog entry specified by request */
    public List getComments(WeblogEntryWrapper wrapper, boolean noSpam, boolean approvedOnly) {
        WeblogEntry entry = wrapper.getPojo();
        List comments = new ArrayList();
        List unwrappped = entry.getComments(noSpam, approvedOnly);
        comments = new ArrayList(unwrappped.size());
        Iterator it = unwrappped.iterator();
        while(it.hasNext()) {
            comments.add(WeblogEntryCommentWrapper.wrap((WeblogEntryComment)it.next(), urlStrategy));
        }
        return comments;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public int getDayHits() {
        try {
            WeblogHitCount hitCount = mWeblogEntryMgr.getHitCountByWeblog(mWebsite);
            
            return (hitCount != null) ? hitCount.getDailyHits() : 0;
            
        } catch (WebloggerException e) {
            mLogger.error("PageModel getDayHits()", e);
        }
        return 0;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates BookmarkManager.getFolder() */
    public WeblogBookmarkFolderWrapper getFolder(String folderPath) {
        try {
            return WeblogBookmarkFolderWrapper.wrap(
                    mBookmarkMgr.getFolder(
                    mWeblogMgr.getWeblogByHandle(mWebsite.getHandle()), folderPath));
        } catch (WebloggerException e) {
            mLogger.error("PageModel getFolder()", e);
        }
        return null;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public ThemeTemplateWrapper getUsersPageByName(WeblogWrapper wrapper, String pageName) {
        Weblog website = wrapper.getPojo();
        ThemeTemplateWrapper page = null;
        try {
            if (website == null)
                throw new NullPointerException("website is null");
            
            if (pageName == null)
                throw new NullPointerException("pageName is null");
            
            page = ThemeTemplateWrapper.wrap(website.getTheme().getTemplateByName(pageName));
        } catch (NullPointerException npe) {
            mLogger.warn(npe.getMessage());
        } catch (WebloggerException e) {
            mLogger.error("ERROR getting user's page by name: " + e.getMessage(),e);
        }
        return page;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public ThemeTemplateWrapper getPageByName(String pageName) {
        return (ThemeTemplateWrapper) mPageMap.get(pageName);
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates UserManager.getPageByName() */
    public String getPageIdByName(String pageName) {
        mLogger.debug("looking up page ["+pageName+"]");
        
        String template_id = null;
        
        try {
            ThemeTemplate pd = mWebsite.getTheme().getTemplateByName(pageName);
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
     * @param maxEntries Maximum number of entries to be returned (only applies 
     *                   if specific day not specified).
     * @param catName    Only return entries from this category and it's
     *                   subcategories. If null, returns all categories of entry
     * @return           Map of Lists of WeblogEntryData, keyed by 8-char date 
     *                   strings.
     */
    public Map getRecentWeblogEntries(int maxEntries, String catName) {
        if (VELOCITY_NULL.equals(catName)) catName = null;
        Map ret = new HashMap();
        try {            
            // If request specifies a category, then use that
            String catParam = null;
            if (mCategory != null) {
                catParam = mCategory.getPath();
            } else if (catName != null) {
                // use category argument instead
                catParam = catName;
            } else if (mWebsite != null) // MAIN
            {
                catParam = mWebsite.getDefaultCategory().getPath();
                if (catParam.equals("/")) {
                    catParam = null;
                }
            }
            
            Calendar cal = null;
            if (mWebsite != null) {
                TimeZone tz = mWebsite.getTimeZoneInstance();
                cal = Calendar.getInstance(tz);
            } else {
                cal = Calendar.getInstance();
            }
            int limit = mWebsite.getEntryDisplayCount();
            Date startDate = null;
            Date endDate = mDate;
            if (endDate == null) endDate = new Date();
            if (mIsDaySpecified) { 
                // URL specified a specific day
                // so get entries for that day
                endDate = DateUtil.getEndOfDay(endDate, cal);
                startDate = DateUtil.getStartOfDay(endDate, cal); 
                // and get them ALL, no limit
                limit = -1;                  
            } else if (mIsMonthSpecified) {
                endDate = DateUtil.getEndOfMonth(endDate, cal);
            }
            Map mRet = WebloggerFactory.getWeblogger().getWeblogEntryManager().getWeblogEntryObjectMap(
                    
                    mWebsite,
                    startDate,                    // startDate
                    endDate,                      // endDate
                    catParam,                     // catName
                    null,WeblogEntry.PUBLISHED,    // status
                    mLocale, 0, limit);  
            
            // need to wrap pojos
            java.util.Date key = null;
            Iterator days = mRet.keySet().iterator();
            while(days.hasNext()) {
                key = (java.util.Date)days.next();
                
                // now we need to go through each entry in a day and wrap
                List wrappedEntries = new ArrayList();
                List entries = (List) mRet.get(key);
                for(int i=0; i < entries.size(); i++) {
                    wrappedEntries.add(i,WeblogEntryWrapper.wrap((WeblogEntry)entries.get(i), urlStrategy));
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
                mFirstEntry = (WeblogEntryWrapper)vals.get(0);
            }
            
            // get last entry in map
            vals = (List)days.get( keys[--numDays] );
            valSize = vals.size();
            if (valSize > 0) {
                mLastEntry = (WeblogEntryWrapper)vals.get(--valSize);
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
            Date day = mDate;
            if (day == null) day = new Date();
            
            // If request specifies a category, then use that
            String catParam = null;
            if (mCategory != null) {
                catParam = mCategory.getPath();
            } else if (categoryName != null) {
                // use category argument instead
                catParam = categoryName;
            } else if (mWebsite != null) // MAIN
            {
                catParam = mWebsite.getDefaultCategory().getPath();
                if (catParam.equals("/")) {
                    catParam = null;
                }
            }
            WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            
            //ret = mgr.getRecentWeblogEntriesArray(
            //name, day, catParam, maxEntries, true );
            
            List mEntries = mgr.getWeblogEntries(
                    
                    mWebsite,
                    null,
                    null,                        // startDate
                    day,                         // endDate
                    catParam,                    // catName
                    null,WeblogEntry.PUBLISHED,   // status
                    null,                        // text
                    null,                        // sortby (null for pubTime)
                    null,
                    mLocale, 0, mWebsite.getEntryDisplayCount());    
            
            // wrap pojos
            ret = new ArrayList(mEntries.size());
            Iterator it = mEntries.iterator();
            int i=0;
            while(it.hasNext()) {
                ret.add(i,WeblogEntryWrapper.wrap((WeblogEntry) it.next(), urlStrategy));
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
                    mRefererMgr.getReferersToDate(mWebsite, date);
            RollerSession rses =
                    RollerSession.getRollerSession(mRequest);
            
            for (Iterator rdItr = refs.iterator(); rdItr.hasNext();) {
                WeblogReferrer referer = (WeblogReferrer) rdItr.next();
                String title =referer.getTitle();
                String excerpt = referer.getExcerpt();
                if (   StringUtils.isNotEmpty(title)
                && StringUtils.isNotEmpty(excerpt) ) {
                    if (referer.getVisible().booleanValue() 
                     || referer.getWebsite().hasUserPermission(rses.getAuthenticatedUser(), WeblogPermission.ADMIN) ) { 
                        referers.add(WeblogReferrerWrapper.wrap(referer, urlStrategy));
                    }
                }
            }
            
        } catch (Exception e) {
            mLogger.error("PageModel getReferersToDate() fails with URL"
                    + mRequest.getRequestURL(), e);
        }
        return referers;
    }
    
    /** Encapsulates RefererManager **/
    public List getEntryReferers(WeblogEntryWrapper entry) {
        ArrayList referers = new ArrayList();
        try {
            List refs = mRefererMgr.getReferersToEntry(entry.getId());
            RollerSession rses =
               RollerSession.getRollerSession(mRequest);
            
            for (Iterator rdItr = refs.iterator(); rdItr.hasNext();) {
                WeblogReferrer referer = (WeblogReferrer) rdItr.next();
                String title =referer.getTitle();
                String excerpt = referer.getExcerpt();
                if (   StringUtils.isNotEmpty(title)
                && StringUtils.isNotEmpty(excerpt) ) {
                    if (referer.getVisible().booleanValue()
                    ||  referer.getWebsite().hasUserPermission(rses.getAuthenticatedUser(), WeblogPermission.ADMIN) ) {
                        referers.add(WeblogReferrerWrapper.wrap(referer, urlStrategy));
                    }
                }
            }
            
        } catch (Exception e) {
            mLogger.error("PageModel getReferersToDate() fails with URL"
                    + mRequest.getRequestURL(), e);
        }
        return referers;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public List getTodaysReferers() {
         return mWebsite.getTodaysReferrers();
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RefererManager */
    public int getTotalHits() {
        return mWebsite.getTodaysHits();
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
            WeblogEntry wd = (WeblogEntry)iter.next();
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
    public Set getWeblogCategories(String categoryName) {
        Set ret = null;
        if (VELOCITY_NULL.equals(categoryName)) categoryName = null;
        
        // Make sure we have not already fetched this category.
        if (categoryName != null) {
            ret = (Set)mCategories.get(categoryName);
        } else {
            ret = (Set)mCategories.get("zzz_null_zzz");
        }
        
        if (null == ret) {
            try {
                WeblogCategory category = null;
                if (categoryName != null) {
                    category = mWeblogEntryMgr.getWeblogCategoryByPath(
                            mWebsite, categoryName);
                } else {
                    category = mWebsite.getDefaultCategory();
                }
                
                Set mRet = category.getWeblogCategories();
                
                // wrap pojos
                ret = new HashSet(mRet.size());
                Iterator it = mRet.iterator();
                int i=0;
                while(it.hasNext()) {
                    ret.add(WeblogCategoryWrapper.wrap((WeblogCategory)it.next(), urlStrategy));
                    i++;
                }
                if (categoryName != null) {
                    mCategories.put(categoryName, ret);
                } else {
                    mCategories.put("zzz_null_zzz", ret);
                }
            } catch (WebloggerException e) {
                mLogger.error(e);
            }
        }
        return ret;
    }
    
    //------------------------------------------------------------------------
    
    /** Encapsulates RollerRequest.getWeblogEntry() */
    public WeblogEntryWrapper getWeblogEntry() {
        
        if(mEntry != null && mEntry.getStatus().equals(WeblogEntry.PUBLISHED))
            return WeblogEntryWrapper.wrap(mEntry, urlStrategy);
        else
            return null;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Get the next occurring Entry.
     */
    public WeblogEntryWrapper getNextEntry() {
        WeblogEntryWrapper currentEntry = getWeblogEntry();
        if (mFirstEntry != null) currentEntry = mFirstEntry;
        if (mNextEntry == null && currentEntry != null) {
            String catName = null;
            if (mCategory != null) {
                catName = mCategory.getName();
            }
            try {
                WeblogEntry nextEntry =
                        mWeblogEntryMgr.getNextEntry(currentEntry.getPojo(), catName, mLocale);
                
                if(nextEntry != null)
                    mNextEntry = WeblogEntryWrapper.wrap(nextEntry, urlStrategy);
                
                // make sure that mNextEntry is not published to future
                if (mNextEntry != null &&
                        mNextEntry.getPubTime().after( new Date() )) {
                    mNextEntry = null;
                }
            } catch (WebloggerException e) {
                mLogger.error("PageModel.getNextEntry)", e);
            }
        }
        return mNextEntry;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Get the previous occurring Entry.
     */
    public WeblogEntryWrapper getPreviousEntry() {
        WeblogEntryWrapper currentEntry = getWeblogEntry();
        if (mLastEntry != null) currentEntry = mLastEntry;
        if (mPreviousEntry == null && currentEntry != null ) {
            String catName = null;
            if (mCategory != null) {
                catName = mCategory.getName();
            }
            try {
                WeblogEntry prevEntry =
                        mWeblogEntryMgr.getPreviousEntry(currentEntry.getPojo(), catName, mLocale);
                
                if(prevEntry != null)
                    mPreviousEntry = WeblogEntryWrapper.wrap(prevEntry, urlStrategy);
            } catch (WebloggerException e) {
                mLogger.error("PageModel.getPreviousEntry)", e);
            }
        }
        return mPreviousEntry;
    }
    
    //------------------------------------------------------------------------
    
    public boolean isUserAuthorizedToEdit() {
        try {
            RollerSession rses =
                    RollerSession.getRollerSession(mRequest);
            if (rses != null && rses.getAuthenticatedUser() != null && mWebsite != null) {
                return mWebsite.hasUserPermission(rses.getAuthenticatedUser(), WeblogPermission.POST);
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
                    RollerSession.getRollerSession(mRequest);
            if (rses != null && rses.getAuthenticatedUser() != null && mWebsite != null) {
                return mWebsite.hasUserPermission(rses.getAuthenticatedUser(), WeblogPermission.POST);
            }
        } catch (Exception e) {
            mLogger.warn("PageModel.isUserAuthorizedToAdmin()", e);
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    
    public boolean isUserAuthenticated() {
        return (mRequest.getUserPrincipal() != null);
    }
    
    //------------------------------------------------------------------------
    
    public String getRequestParameter(String key) {
        return mRequest.getParameter(key);
    }
    
    public int getIntRequestParameter(String key) {
        return Integer.parseInt(mRequest.getParameter(key));
    }
    
    //------------------------------------------------------------------------
    
    public WeblogBookmarkFolderWrapper getFolderByPath(String path) {
        try {
            WeblogBookmarkFolder folder = mBookmarkMgr.getFolder(mWebsite, path);
            
            if(folder != null)
                return WeblogBookmarkFolderWrapper.wrap(folder);
        } catch (WebloggerException e) {
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
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            List recent = wmgr.getComments(
                    
                    mWebsite,
                    null,  // weblog entry
                    null,  // search String
                    null,  // startDate
                    null,WeblogEntryComment.APPROVED, // approved comments only
                    true,          // we want reverse chrono order
                    0,             // offset
                    maxCount);     // no limit
            
            // wrap pojos
            recentComments = new ArrayList(recent.size());
            Iterator it = recent.iterator();
            while(it.hasNext()) {
                recentComments.add(WeblogEntryCommentWrapper.wrap((WeblogEntryComment) it.next(), urlStrategy));
            }
        } catch (WebloggerException e) {
            mLogger.error(e);
        }
        return recentComments;
    }
    
    public boolean getEmailComments() {
        if (mWebsite != null) {
            boolean emailComments = WebloggerRuntimeConfig.getBooleanProperty("users.comments.emailnotify");
            return (mWebsite.getEmailComments().booleanValue() && emailComments);
        }
        return false;
    }
}
