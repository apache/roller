/*
 * Created on Mar 4, 2004
 */
package org.roller.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.pojos.Assoc;
import org.roller.pojos.CommentData;
import org.roller.pojos.WeblogCategoryAssoc;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author lance.lavandowska
 */
public class MockWeblogManager implements WeblogManager
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MockUserManager.class);
    
    private PersistenceStrategy mStrategy = null;

    /**
     * @param strategy
     * @param roller
     */
    public MockWeblogManager(PersistenceStrategy strategy, MockRoller roller)
    {
        mStrategy = strategy;
    }

    /* 
     * @see org.roller.model.WeblogManager#release()
     */
    public void release()
    {
    }

    /* 
     * @see org.roller.model.WeblogManager#createWeblogCategory()
     */
    public WeblogCategoryData createWeblogCategory()
    {
        WeblogCategoryData cat = new WeblogCategoryData();
        try
        {
            mStrategy.store(cat);
        }
        catch (RollerException e)
        {
        }
        return cat;
    }

    /* 
     * @see org.roller.model.WeblogManager#createWeblogCategory(org.roller.pojos.WebsiteData, org.roller.pojos.WeblogCategoryData, java.lang.String, java.lang.String, java.lang.String)
     */
    public WeblogCategoryData createWeblogCategory(WebsiteData website,
                                                   WeblogCategoryData parent,
                                                   String name,
                                                   String description,
                                                   String image)
            throws RollerException
    {
        WeblogCategoryData cat = new WeblogCategoryData(null, website, 
                                                        parent, name, 
                                                        description, image);
        try
        {
            mStrategy.store(cat);
        }
        catch (RollerException e)
        {
        }
        return cat;
    }

    /* 
     * @see org.roller.model.WeblogManager#retrieveWeblogCategory(java.lang.String)
     */
    public WeblogCategoryData retrieveWeblogCategory(String id)
            throws RollerException
    { 
        return (WeblogCategoryData)mStrategy.load(id, WeblogCategoryData.class);
    }

    /* 
     * @see org.roller.model.WeblogManager#moveWeblogCategoryContents(java.lang.String, java.lang.String)
     */
    public void moveWeblogCategoryContents(String srcId, String destId)
            throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogCategories(java.lang.String)
     */
    public List getWeblogCategories(String userName) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogCategory(java.lang.String, java.lang.String)
     */
    public WeblogCategoryData getWeblogCategory(String catName, String userName)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getRootWeblogCategory(org.roller.pojos.WebsiteData)
     */
    public WeblogCategoryData getRootWeblogCategory(WebsiteData website)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogCategory(org.roller.pojos.WebsiteData, java.lang.String)
     */
    public WeblogCategoryData getWeblogCategory(WebsiteData website,
                                                String categoryPath)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getPath(org.roller.pojos.WeblogCategoryData)
     */
    public String getPath(WeblogCategoryData category) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogCategoryByPath(org.roller.pojos.WebsiteData, org.roller.pojos.WeblogCategoryData, java.lang.String)
     */
    public WeblogCategoryData getWeblogCategoryByPath(
                                                      WebsiteData wd,
                                                      WeblogCategoryData category,
                                                      String string)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#createWeblogCategoryAssoc()
     */
    public WeblogCategoryAssoc createWeblogCategoryAssoc()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#createWeblogCategoryAssoc(org.roller.pojos.WeblogCategoryData, org.roller.pojos.WeblogCategoryData, java.lang.String)
     */
    public WeblogCategoryAssoc createWeblogCategoryAssoc(
                                                         WeblogCategoryData category,
                                                         WeblogCategoryData ancestor,
                                                         String relation)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#retrieveWeblogCategoryAssoc(java.lang.String)
     */
    public WeblogCategoryAssoc retrieveWeblogCategoryAssoc(String id)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getComments(java.lang.String)
     */
    public List getComments(String entryId) throws RollerException
    {
        return getComments(entryId, true);
    }

    /* 
     * @see org.roller.model.WeblogManager#getComments(java.lang.String, boolean)
     */
    public List getComments(String entryId, boolean nospam) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#removeComment(java.lang.String)
     */
    public void removeComment(String id) throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.WeblogManager#removeComments(java.lang.String[])
     */
    public void removeComments(String[] ids) throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.WeblogManager#retrieveWeblogEntry(java.lang.String)
     */
    public WeblogEntryData retrieveWeblogEntry(String id)
            throws RollerException
    {
        return (WeblogEntryData)mStrategy.load(id, WeblogEntryData.class);
    }

    /* 
     * @see org.roller.model.WeblogManager#removeWeblogEntry(java.lang.String)
     */
    public void removeWeblogEntry(String id) throws RollerException
    {
        mStrategy.remove(id, WeblogEntryData.class);
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogEntryByAnchor(java.lang.String, java.lang.String)
     */
    public WeblogEntryData getWeblogEntryByAnchor(String userName, String anchor)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogLastPublishTime(java.lang.String)
     */
    public Date getWeblogLastPublishTime(String userName)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogLastPublishTime(java.lang.String, java.lang.String)
     */
    public Date getWeblogLastPublishTime(String userName, String catName)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getRecentWeblogEntries(java.lang.String, java.util.Date, java.lang.String, int)
     */
    public Map getRecentWeblogEntries(String userName, Date date,
                                      String catName, int count)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getRecentWeblogEntries(java.lang.String, java.util.Date, java.lang.String, int, boolean)
     */
    public Map getRecentWeblogEntries(String userName, Date date,
                                      String catName, int count,
                                      boolean publishedOnly)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getRecentWeblogEntriesArray(java.lang.String, java.util.Date, java.lang.String, int, boolean)
     */
    public List getRecentWeblogEntriesArray(String userName, Date date,
                                            String catName, int count,
                                            boolean pubOnly)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogEntriesInDateRange(java.lang.String, java.lang.String, java.util.Date, java.util.Date)
     */
    public List getWeblogEntriesInDateRange(String userName, String catName,
                                            Date start, Date end)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogEntriesInDateRange(java.lang.String, java.lang.String, java.util.Date, java.util.Date, boolean)
     */
    public List getWeblogEntriesInDateRange(String userName, String catName,
                                            Date start, Date end,
                                            boolean pubOnly)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogEntriesGroupedByDay(java.lang.String, java.lang.String, java.util.Date, java.util.Date, boolean, boolean)
     */
    public Map getWeblogEntriesGroupedByDay(String userName, String catName,
                                            Date start, Date end,
                                            boolean daysOnly, boolean pubOnly)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getWeblogEntryMonthMap(java.lang.String, java.util.Date, java.lang.String, boolean, boolean)
     */
    public Map getWeblogEntryMonthMap(String userName, Date month, String cat,
                                      boolean daysOnly, boolean publishedOnly)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getAllRecentWeblogEntries(java.util.Date, int)
     */
    public List getAllRecentWeblogEntries(Date endDate, int max)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getAllRecentWeblogEntries(java.util.Date, int, java.lang.String)
     */
    public List getAllRecentWeblogEntries(Date endDate, int max, String catName)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#retrieveWeblogEntries(org.roller.pojos.WeblogCategoryData, boolean)
     */
    public List retrieveWeblogEntries(WeblogCategoryData cat, boolean subcats)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.roller.model.WeblogManager#removeCommentsForEntry(java.lang.String)
     */
    public void removeCommentsForEntry(String entryId) throws RollerException
    {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.roller.model.WeblogManager#retrieveComment(java.lang.String)
     */
    public CommentData retrieveComment(String id) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getRecentWeblogEntriesArray(java.lang.String, java.util.Date, java.lang.String, int, boolean, boolean)
     */
    public List getRecentWeblogEntriesArray(String userName, Date date, String catName, int count, boolean pubOnly, boolean draftOnly) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getNextEntry(org.roller.pojos.WeblogEntryData, java.lang.String)
     */
    public WeblogEntryData getNextEntry(WeblogEntryData current, String catName) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getPreviousEntry(org.roller.pojos.WeblogEntryData, java.lang.String)
     */
    public WeblogEntryData getPreviousEntry(WeblogEntryData current, String catName) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntries(java.lang.String, java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.Integer)
     */
    public List getWeblogEntries(String userName, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntryObjectMap(java.lang.String, java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.Integer)
     */
    public Map getWeblogEntryObjectMap(String userName, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntryStringMap(java.lang.String, java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.Integer)
     */
    public Map getWeblogEntryStringMap(String userName, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntriesPinnedToMain(int)
     */
    public List getWeblogEntriesPinnedToMain(Integer max)
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogCategories(org.roller.pojos.WebsiteData)
     */
    public List getWeblogCategories(WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogCategory(java.lang.String, org.roller.pojos.WebsiteData)
     */
    public WeblogCategoryData getWeblogCategory(String catName, WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntries(org.roller.pojos.WebsiteData, java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.Integer)
     */
    public List getWeblogEntries(WebsiteData website, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntryObjectMap(org.roller.pojos.WebsiteData, java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.Integer)
     */
    public Map getWeblogEntryObjectMap(WebsiteData website, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntryStringMap(org.roller.pojos.WebsiteData, java.util.Date, java.util.Date, java.lang.String, java.lang.String, java.lang.Integer)
     */
    public Map getWeblogEntryStringMap(WebsiteData website, Date startDate, Date endDate, String catName, String status, Integer maxEntries) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntryByAnchor(org.roller.pojos.WebsiteData, java.lang.String)
     */
    public WeblogEntryData getWeblogEntryByAnchor(WebsiteData website, String anchor) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#removeWeblogEntryContents(org.roller.pojos.WeblogEntryData)
     */
    public void removeWeblogEntryContents(WeblogEntryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        
    }

    /** 
     * @see org.roller.model.WeblogManager#createAnchor(org.roller.pojos.WeblogEntryData)
     */
    public String createAnchor(WeblogEntryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#isDuplicateWeblogCategoryName(org.roller.pojos.WeblogCategoryData)
     */
    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /** 
     * @see org.roller.model.WeblogManager#isWeblogCategoryInUse(org.roller.pojos.WeblogCategoryData)
     */
    public boolean isWeblogCategoryInUse(WeblogCategoryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return false;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogCategoryParentAssoc(org.roller.pojos.WeblogCategoryData)
     */
    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogCategoryChildAssocs(org.roller.pojos.WeblogCategoryData)
     */
    public List getWeblogCategoryChildAssocs(WeblogCategoryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getAllWeblogCategoryDecscendentAssocs(org.roller.pojos.WeblogCategoryData)
     */
    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogCategoryAncestorAssocs(org.roller.pojos.WeblogCategoryData)
     */
    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData data) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogCategories(org.roller.pojos.WebsiteData, boolean)
     */
    public List getWeblogCategories(WebsiteData website, boolean includeRoot) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.WeblogManager#getRecentComments(org.roller.pojos.WebsiteData)
     */
    public List getRecentComments(WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

	/* (non-Javadoc)
	 * @see org.roller.model.WeblogManager#getWeblogCategoryByPath(org.roller.pojos.WebsiteData, java.lang.String)
	 */
	public WeblogCategoryData getWeblogCategoryByPath(WebsiteData website, String categoryPath) throws RollerException {
		// TODO Auto-generated method stub
		return null;
	}

    /* 
     * @see org.roller.model.WeblogManager#getRecentComments(org.roller.pojos.WebsiteData, int)
     */
    public List getRecentComments(WebsiteData website, int maxCount) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
