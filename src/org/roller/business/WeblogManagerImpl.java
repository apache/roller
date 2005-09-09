/*
 * Created on Feb 24, 2003
 */
package org.roller.business;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryAssoc;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.DateUtil;
import org.roller.util.Utilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.roller.model.RollerFactory;

/**
 * Abstract base implementation using PersistenceStrategy.
 * @author Dave Johnson
 * @author Lance Lavandowska
 */
public abstract class WeblogManagerImpl implements WeblogManager
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(WeblogManagerImpl.class);

    protected PersistenceStrategy mStrategy;
    
    /* inline creation of reverse comparator, anonymous inner class */
    private Comparator reverseComparator = new ReverseComparator();
    /*
    new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            return -1 * ((Date) o1).compareTo((Date)o2);
        }
    };
    */
    
    private SimpleDateFormat formatter = DateUtil.get8charDateFormat();

    public abstract List getWeblogEntries(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status, 
                    Integer maxEntries,
                    Boolean pinned) throws RollerException;

    public abstract List getNextPrevEntries(
                    WeblogEntryData current, 
                    String catName, 
                    int maxEntries, 
                    boolean next) throws RollerException;

    public WeblogManagerImpl(PersistenceStrategy strategy)
    {
        mStrategy = strategy;
    }

    public void release()
    {
    }

    //------------------------------------------------ WeblogCategoryData CRUD

    /**
     * @see org.roller.model.WeblogManager#createWeblogCategory()
     */
    public WeblogCategoryData createWeblogCategory()
    {
        return new WeblogCategoryData();
    }

    /**
     * @see org.roller.model.WeblogManager#createWeblogCategory(
     * org.roller.pojos.WebsiteData, org.roller.pojos.WeblogCategoryData,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    public WeblogCategoryData createWeblogCategory(
        WebsiteData website,
        WeblogCategoryData parent,
        String name,
        String description,
        String image) throws RollerException
    {
        return new WeblogCategoryData(
            null, website, parent, name, description, image);
    }

    public WeblogCategoryData retrieveWeblogCategory(String id)
        throws RollerException
    {
        return (WeblogCategoryData) mStrategy.load(
            id,
            WeblogCategoryData.class);
    }

    //--------------------------------------------- WeblogCategoryData Queries

    public WeblogCategoryData getWeblogCategoryByPath(
        WebsiteData website, String categoryPath) throws RollerException
    {
        return getWeblogCategoryByPath(website, null, categoryPath);
    }

    public String getPath(WeblogCategoryData category) throws RollerException
    {
        if (null == category.getParent())
        {
            return "/";
        }
        else
        {
            String parentPath = getPath(category.getParent());
            parentPath = "/".equals(parentPath) ? "" : parentPath;
            return parentPath + "/" + category.getName();
        }
    }

    public WeblogCategoryData getWeblogCategoryByPath(
        WebsiteData website, WeblogCategoryData category, String path)
        throws RollerException
    {
        final Iterator cats;
        final String[] pathArray = Utilities.stringToStringArray(path, "/");

        if (category == null && (null == path || "".equals(path.trim())))
        {
            throw new RollerException("Bad arguments.");
        }

        if (path.trim().equals("/"))
        {
            return getRootWeblogCategory(website);
        }
        else if (category == null || path.trim().startsWith("/"))
        {
            cats = getRootWeblogCategory(website).getWeblogCategories().iterator();
        }
        else
        {
            cats = category.getWeblogCategories().iterator();
        }

        while (cats.hasNext())
        {
            WeblogCategoryData possibleMatch = (WeblogCategoryData)cats.next();
            if (possibleMatch.getName().equals(pathArray[0]))
            {
                if (pathArray.length == 1)
                {
                    return possibleMatch;
                }
                else
                {
                    String[] subpath = new String[pathArray.length - 1];
                    System.arraycopy(pathArray, 1, subpath, 0, subpath.length);

                    String pathString= Utilities.stringArrayToString(subpath,"/");
                    return getWeblogCategoryByPath(website, possibleMatch, pathString);
                }
            }
        }

        // The category did not match and neither did any sub-categories
        return null;
    }

    //----------------------------------------------- WeblogCategoryAssoc CRUD

    public WeblogCategoryAssoc createWeblogCategoryAssoc()
    {
        return new WeblogCategoryAssoc();
    }

    public WeblogCategoryAssoc createWeblogCategoryAssoc(
        WeblogCategoryData category,
        WeblogCategoryData ancestor,
        String relation) throws RollerException
    {
        return new WeblogCategoryAssoc(null, category, ancestor, relation);
    }

    public WeblogCategoryAssoc retrieveWeblogCategoryAssoc(String id) throws RollerException
    {
        return (WeblogCategoryAssoc)mStrategy.load(id, WeblogCategoryAssoc.class);
    }

    //------------------------------------------------------- CommentData CRUD

    public void removeComment(String id) throws RollerException
    {
        mStrategy.remove(id, CommentData.class);
    }

    public void removeComments(String[] ids) throws RollerException
    {
        for (int i = 0; i < ids.length; i++)
        {
            removeComment(ids[i]);
        }
    }

    public void removeCommentsForEntry(String entryId) throws RollerException
    {
		List comments = getComments(entryId, false); // get all Comments
        for (int i=comments.size()-1; i>=0; i--)
        {
            ((CommentData)comments.get(i)).remove();
        }
	}

    //---------------------------------------------------- CommentData Queries
    
    public CommentData retrieveComment(String id) throws RollerException
    {
        return (CommentData) mStrategy.load(id, CommentData.class);        
    }

    public List getComments(String entryId) throws RollerException
    {
        return getComments(entryId, true);
    }

    //--------------------------------------------------- WeblogEntryData CRUD

    public WeblogEntryData retrieveWeblogEntry(String id)
        throws RollerException
    {
        return (WeblogEntryData) mStrategy.load(
            id, WeblogEntryData.class);
    }

    public void removeWeblogEntry(String id) throws RollerException
    {
        Roller mRoller = RollerFactory.getRoller();
		mRoller.getRefererManager().removeReferersForEntry(id);
		removeCommentsForEntry( id );
        mStrategy.remove(id, WeblogEntryData.class);
    }

    //------------------------------------------------ WeblogEntryData Queries

    /**
     * Gets the Date of the latest Entry publish time, before the end of today,
     * for all WeblogEntries
     *
     * @param userName
     * @return Date
     * @throws RollerException
     */
    public Date getWeblogLastPublishTime(WebsiteData website)
        throws RollerException
    {
        return getWeblogLastPublishTime(website, null);
    }

    //--------------------------------------------------------- Implementation

    /** 
     * Get weblog entries.
     * @see org.roller.model.WeblogManager#getWeblogEntries(
     *   java.lang.String, 
     *   java.util.Date, 
     *   java.util.Date, 
     *   java.lang.String, 
     *   java.lang.String, 
     *   java.lang.Integer)
     */
    public List getWeblogEntries(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status, 
                    Integer maxEntries) throws RollerException
    {
        return getWeblogEntries(
                    website, 
                    startDate, 
                    endDate, 
                    catName, 
                    status, 
                    maxEntries, 
                    null);
    }

    /** 
     * Get webloog entries in range specified by offset and length.
     * @see org.roller.model.WeblogManager#getWeblogEntries(
     *   java.lang.String, 
     *   java.util.Date, 
     *   java.util.Date, 
     *   java.lang.String, 
     *   java.lang.String, 
     *   int offset,
     *   int length)
     */
    public List getWeblogEntries(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status, 
                    int offset,
                    int range) throws RollerException
    {
        List filtered = new ArrayList();
        List entries = getWeblogEntries(
                    website, 
                    startDate, 
                    endDate, 
                    catName, 
                    status, 
                    new Integer(offset + range), 
                    null);
        if (entries.size() < offset) 
        {
            return filtered;
        }
        for (int i=offset; i<entries.size(); i++)
        {
            filtered.add(entries.get(i));
        }
        return filtered;
    }

    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntryDayMap(
     * java.lang.String, 
     * java.util.Date, 
     * java.util.Date, 
     * java.lang.String, 
     * java.lang.String, 
     * java.lang.Integer)
     */
    public Map getWeblogEntryObjectMap(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status, 
                    Integer maxEntries) throws RollerException
    {
        return getWeblogEntryMap(
                        website,
                        startDate,
                        endDate,
                        catName,
                        status,
                        maxEntries,
                        false);
    }
    
    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntryDayMap(
     * java.lang.String, 
     * java.util.Date, 
     * java.util.Date, 
     * java.lang.String, 
     * java.lang.String, 
     * java.lang.Integer)
     */
    public Map getWeblogEntryStringMap(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status, 
                    Integer maxEntries) throws RollerException
    {
        return getWeblogEntryMap(
                        website,
                        startDate,
                        endDate,
                        catName,
                        status,
                        maxEntries,
                        true);
    }
    
    private Map getWeblogEntryMap(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status, 
                    Integer maxEntries,
                    boolean stringsOnly) throws RollerException
    {
        TreeMap map = new TreeMap(reverseComparator);
       
        List entries = getWeblogEntries(
                        website,
                        startDate,
                        endDate,
                        catName,
                        status,
                        maxEntries);
        
        Calendar cal = Calendar.getInstance();
        if (website != null)
        {
            cal.setTimeZone(website.getTimeZoneInstance());
        }
        
        for (Iterator wbItr = entries.iterator(); wbItr.hasNext();)
        {
            WeblogEntryData entry = (WeblogEntryData) wbItr.next();
            Date sDate = DateUtil.getNoonOfDay(entry.getPubTime(), cal);
            if (stringsOnly)
            {
                if (map.get(sDate) == null)
                    map.put(sDate, formatter.format(sDate));
            }
            else
            {
                List dayEntries = (List) map.get(sDate);
                if (dayEntries == null)
                {
                    dayEntries = new ArrayList();
                    map.put(sDate, dayEntries);
                }
                dayEntries.add(entry);
            }
        }
        return map;
    }
    
    /* 
     * @see org.roller.model.WeblogManager#getNextEntry(org.roller.pojos.WeblogEntryData)
     */
    public List getNextEntries(
            WeblogEntryData current, String catName, int maxEntries)
        throws RollerException
    {
        return getNextPrevEntries(current, catName, maxEntries, true);
    }

    /* 
     * @see org.roller.model.WeblogManager#getPreviousEntry(org.roller.pojos.WeblogEntryData)
     */
    public List getPreviousEntries(
            WeblogEntryData current, String catName, int maxEntries)
        throws RollerException
    {
        return getNextPrevEntries(current, catName, maxEntries, false);
    }

    public WeblogEntryData getNextEntry(WeblogEntryData current, String catName) 
        throws RollerException
    {
        WeblogEntryData entry = null;
        List entryList = getNextEntries(current, catName, 1);
        if (entryList != null && entryList.size() > 0)
        {
            entry = (WeblogEntryData)entryList.get(entryList.size()-1);
        }
        return entry;
    }
    
    public WeblogEntryData getPreviousEntry(WeblogEntryData current, String catName) 
        throws RollerException
    {
        WeblogEntryData entry = null;
        List entryList = getPreviousEntries(current, catName, 1);
        if (entryList != null && entryList.size() > 0)
        {
            entry = (WeblogEntryData)entryList.get(0);
        }
        return entry;
    }
    
    /** 
     * @see org.roller.model.WeblogManager#getWeblogEntriesPinnedToMain(int)
     */
    public List getWeblogEntriesPinnedToMain(Integer max) throws RollerException
    {
        return getWeblogEntries(
            null, null, new Date(), null, null, max, Boolean.TRUE);
    }

    /**
     * Get absolute URL to this website.
     * @return Absolute URL to this website.
     */
    public String getUrl(WebsiteData site, String contextUrl)
    {
        String url =
            Utilities.escapeHTML(contextUrl + "/page/" + site.getHandle());
        return url;
    }

}
