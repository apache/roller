
package org.roller.model;
import org.roller.RollerException;
import org.roller.pojos.Assoc;
import org.roller.pojos.CommentData;
import org.roller.pojos.WeblogCategoryAssoc;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.UserData;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * Interface to Weblog Management.
 */
public interface WeblogManager extends Serializable
{
    public static final String CATEGORY_ATT = "category.att";
    
    public void release();

    //------------------------------------------------ WeblogCategoryData CRUD

    /** Create new weblog category, NOT a persistent instance. */
    public WeblogCategoryData createWeblogCategory();

    /** Create new weblog category, NOT a persistent instance. */
    public WeblogCategoryData createWeblogCategory(
        WebsiteData website,
        WeblogCategoryData parent,
        String name,
        String description,
        String image) throws RollerException;

    public WeblogCategoryData retrieveWeblogCategory(String id)
        throws RollerException;

    public void moveWeblogCategoryContents(String srcId, String destId)
        throws RollerException;

    //--------------------------------------------- WeblogCategoryData Queries

    /** Get WebLogCategory objects for a website. */
    public List getWeblogCategories(WebsiteData website)
        throws RollerException;

    /** Get WebLogCategory objects for a website. */
    public List getWeblogCategories(WebsiteData website, boolean includeRoot)
        throws RollerException;

    /** 
     * Get top level categories for a website.
     * @param website Website.
     */
    public WeblogCategoryData getRootWeblogCategory(WebsiteData website)
        throws RollerException;

    /**
     * Get absolute path to category, appropriate for use by getWeblogCategoryByPath().
     * @param category WeblogCategoryData.
     * @return         Forward slash separated path string.
     */
    public String getPath(WeblogCategoryData category) throws RollerException;

    /** 
     * Get category specified by website and categoryPath.
     * @param website      Website of WeblogCategory.
     * @param categoryPath Path of WeblogCategory, relative to category root.
     */
   public WeblogCategoryData getWeblogCategoryByPath(
       WebsiteData website, String categoryPath)
       throws RollerException;

    /**
     * Get sub-category by path relative to specified category.
     * @param category  Root of path or null to start at top of category tree.
     * @param path      Path of category to be located.
     * @param website   Website of categories.
     * @return          Category specified by path or null if not found.
     */
    public WeblogCategoryData getWeblogCategoryByPath(
        WebsiteData wd, WeblogCategoryData category, String string)
        throws RollerException;

    //----------------------------------------------- WeblogCategoryAssoc CRUD

    /** Create new weblog category assoc, NOT a persistent instance. */
    public WeblogCategoryAssoc createWeblogCategoryAssoc();

    /** Create new weblog category assoc, NOT a persistent instance. */
    public WeblogCategoryAssoc createWeblogCategoryAssoc(
        WeblogCategoryData category,
        WeblogCategoryData ancestor,
        String relation) throws RollerException;

    public WeblogCategoryAssoc retrieveWeblogCategoryAssoc(String id)
        throws RollerException;

    //------------------------------------------------------- CommentData CRUD

    public CommentData retrieveComment(String id)
        throws RollerException;

    public abstract List getComments( String entryId )
        throws RollerException;

    public abstract List getComments( String entryId, boolean nospam )
        throws RollerException;

    public void removeComment( String id )
        throws RollerException;

    public void removeComments( String[] ids )
        throws RollerException;

    public void removeCommentsForEntry(String entryId)
    	throws RollerException;
    
    public List getRecentComments(WebsiteData website, int maxCount) 
        throws RollerException;

    //------------------------------------------------------- WeblogEntry CRUD

    public WeblogEntryData retrieveWeblogEntry(String id)
        throws RollerException;

    public void removeWeblogEntry( String id )
        throws RollerException;

    //------------------------------------------------ WeblogEntryData Queries

    /**
     * Get WeblogEntries as a list in reverse chronological order.
     * 
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of ALL, DRAFT_ONLY, or PUB_ONLY.
     * @param maxEntries Max entries or null for no limit.
     * @return List of WeblogEntryData objects in reverse chrono order.
     * @throws RollerException
     */
    public List getWeblogEntries(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status,
                    Integer maxEntries)
                    throws RollerException;
    
    /**
     * Get WeblogEntries in range as list in reverse chronological order.
     * The range offset and list arguments enable paging through query results.
     * 
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param offset     Index of first entry to include.
     * @param length     Max number of entries to include.
     * @return List of WeblogEntryData objects in reverse chrono order.
     * @throws RollerException
     */
    public List getWeblogEntries(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status,
                    int     offset,
                    int     length)
                    throws RollerException;
    
    /**
     * Get Weblog Entries grouped by day. This method returns a Map that 
     * contains Lists, each List contains WeblogEntryData objects, and the 
     * Lists are keyed by Date objects.
     * 
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param maxEntries Max entries or null for no limit.
     * @return Map of Lists, keyed by Date, and containing WeblogEntryData.
     * @throws RollerException
     */
    public Map getWeblogEntryObjectMap(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status,
                    Integer maxEntries)
                    throws RollerException;

    /**
     * Get Weblog Entry date strings grouped by day. This method returns a Map 
     * that contains Lists, each List contains YYYYMMDD date strings objects, 
     * and the Lists are keyed by Date objects.
     * 
     * @param userName   User name or null to get for all users.
     * @param startDate  Start date or null for no start date.
     * @param endDate    End date or null for no end date.
     * @param catName    Category path or null for all categories.
     * @param status     Status of DRAFT, PENDING, PUBLISHED or null for all
     * @param maxEntries Max entries or null for no limit.
     * @return Map of Lists, keyed by Date, and containing date strings.
     * @throws RollerException
     */
    public Map getWeblogEntryStringMap(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status,
                    Integer maxEntries)
                    throws RollerException;

    /**
     * Get weblog entries with given category or, optionally, any sub-category
     * of that category.
     * @param cat     Category.
     * @param subcats True if sub-categories are to be fetched.
     * @return        List of weblog entries in category.
     */
    public List retrieveWeblogEntries(WeblogCategoryData cat, boolean subcats)
        throws RollerException;

    /**
     * Get the WeblogEntry following, chronologically, the current entry.  
     * Restrict by the Category, if named.
     * 
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     * @return
     */
    public WeblogEntryData getNextEntry(WeblogEntryData current, String catName) 
        throws RollerException;

    /**
     * Get the WeblogEntry prior to, chronologically, the current entry.
     * Restrict by the Category, if named.
     * 
     * @param current The "current" WeblogEntryData.
     * @param catName The value of the requested Category Name.
     * @return
     */
    public WeblogEntryData getPreviousEntry(WeblogEntryData current, String catName) 
        throws RollerException;

    /**
     * Get entries next after current entry.
     * @param entry Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getNextEntries(
       WeblogEntryData entry, String catName, int maxEntries) throws RollerException;

    /**
     * Get entries previous to current entry.
     * @param entry Current entry.
     * @param catName Only return entries in this category (if not null).
     * @param maxEntries Maximum number of entries to return.
     */
    public List getPreviousEntries(
       WeblogEntryData entry, String catName, int maxEntries) throws RollerException;

    /**
     * Get specified number of most recent pinned and published Weblog Entries.
     * @param max Maximum number to return.
     * @return Collection of WeblogEntryData objects.
     */
    public List getWeblogEntriesPinnedToMain(Integer max) 
        throws RollerException;

    /** Get weblog entry by anchor. */
    public WeblogEntryData getWeblogEntryByAnchor(
        WebsiteData website, String anchor ) throws RollerException;

    /** Get time of last update for a weblog specified by username */
    public Date getWeblogLastPublishTime(WebsiteData website)
        throws RollerException;

    /**
     * Gets the Date of the latest Entry publish time.
     * @param userName User name of weblog or null for all users
     * @param catName  Top-level category name of posts or null for all categories.
     * @return         Date Of last publish time
     * @throws         RollerException
     */
    public Date getWeblogLastPublishTime(WebsiteData website, String catName )
        throws RollerException;

    /**
     * Remove WeblogEntry contents.
     */
    public void removeWeblogEntryContents(WeblogEntryData data) 
        throws RollerException;

    /**
     * Create unique anchor for weblog entry.
     */
    public String createAnchor(WeblogEntryData data)
        throws RollerException;

    /**
     * Check for duplicate category name.
     */
    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData data)
        throws RollerException;

    /**
     * Check if weblog category is in use.
     */
    public boolean isWeblogCategoryInUse(WeblogCategoryData data)
        throws RollerException;

    /**
     * Returns true if ancestor is truly an ancestor of child.
     */
    public boolean isDescendentOf(
        WeblogCategoryData child, WeblogCategoryData ancestor) throws RollerException;

    /**
     */
    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData data) throws RollerException;

    /**
     */
    public List getWeblogCategoryChildAssocs(WeblogCategoryData data) throws RollerException;

    /**
     */
    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData data) throws RollerException;

    /**
     */
    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData data) throws RollerException;

    /**
     * Get the url of a user's weblog.
     * @param user               the user
     * @param contextUrl         the context url, this is prepended and can be absolute or relative depending on what is desired.
     * @return the url of the user's weblog
     * @throws RollerException
     */
    public String getUrl(WebsiteData website, String contextUrl) throws RollerException;
}
