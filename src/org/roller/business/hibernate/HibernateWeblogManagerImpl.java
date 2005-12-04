/*
 * Created on Jun 16, 2004
 */
package org.roller.business.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.WeblogManagerImpl;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.Assoc;
import org.roller.pojos.CommentData;
import org.roller.pojos.RefererData;
import org.roller.pojos.WeblogCategoryAssoc;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.criterion.MatchMode;

/**
 * Hibernate queries, see comments in parent interface.
 * @author David M Johnson
 */
public class HibernateWeblogManagerImpl extends WeblogManagerImpl
{
    static final long serialVersionUID = -3730860865389981439L;
    
    private static Log mLogger =
        LogFactory.getFactory().getInstance(HibernateWeblogManagerImpl.class);

    public HibernateWeblogManagerImpl(PersistenceStrategy strategy)
    {
        super(strategy);
        mLogger.debug("Instantiating Weblog Manager");
    }
    
    public List getNextPrevEntries(
            WeblogEntryData current, String catName, int maxEntries, boolean next)
        throws RollerException
    {
        if (catName != null && catName.trim().equals("/"))
        {
            catName = null;
        }
        Junction conjunction = Expression.conjunction();        
        conjunction.add(Expression.eq("website", current.getWebsite()));
        conjunction.add(Expression.eq("status", WeblogEntryData.PUBLISHED));
        
        if (next)
        {
            conjunction.add(Expression.gt("pubTime", current.getPubTime()));
        }
        else
        {
            conjunction.add(Expression.lt("pubTime", current.getPubTime()));
        }
        
        if (catName != null)
        {
            WeblogCategoryData category = 
                getWeblogCategoryByPath(current.getWebsite(), null, catName);
            if (category != null)
            {
                conjunction.add(Expression.eq("category", category));
            }
            else
            {
                throw new RollerException("Cannot find category: "+catName);
            }
        }

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            criteria.addOrder(Order.desc("pubTime"));
            criteria.add(conjunction);
            criteria.setMaxResults(maxEntries);
            List results = criteria.list();
            return results;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    public WeblogCategoryData getRootWeblogCategory(WebsiteData website)
        throws RollerException
    {
        if (website == null)
            throw new RollerException("website is null");

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.createAlias("category","c");
            
            criteria.add(Expression.eq("c.website", website));
            criteria.add(Expression.isNull("ancestorCategory"));
            criteria.add(Expression.eq("relation", WeblogCategoryAssoc.PARENT));
            
            criteria.setMaxResults(1);    
            
            List list = criteria.list();
            return ((WeblogCategoryAssoc)list.get(0)).getCategory();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogCategories(WebsiteData website, boolean includeRoot) 
        throws RollerException
    {
        if (website == null)
            throw new RollerException("website is null");
        
        if (includeRoot) return getWeblogCategories(website);

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class); 
            criteria.createAlias("category", "c");
            criteria.add(Expression.eq("c.website", website));
            criteria.add(Expression.isNotNull("ancestorCategory"));
            criteria.add(Expression.eq("relation", "PARENT"));
            Iterator assocs = criteria.list().iterator();
            List cats = new ArrayList();
            while (assocs.hasNext())
            {
                WeblogCategoryAssoc assoc = (WeblogCategoryAssoc) assocs.next();
                cats.add(assoc.getCategory());
            }
            return cats;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogCategories(WebsiteData website) throws RollerException
    {
        if (website == null)
            throw new RollerException("website is null");

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryData.class);            
            criteria.add(Expression.eq("website", website));            
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    public List getWeblogEntries(
                    WebsiteData website, 
                    Date    startDate, 
                    Date    endDate, 
                    String  catName, 
                    String  status, 
                    Integer maxEntries,
                    Boolean pinned) throws RollerException
    {
        WeblogCategoryData cat = null;        
        if (StringUtils.isNotEmpty(catName) && website != null)
        {    
           cat = getWeblogCategoryByPath(website, catName);
           if (cat == null) catName = null;
        }
        if (catName != null && catName.trim().equals("/"))
        {
            catName = null;
        }
                    
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            
            if (website != null)
            {
                criteria.add(Expression.eq("website", website));
            }
            else 
            {
                criteria.createAlias("website","w");
                criteria.add(Expression.eq("w.enabled", Boolean.TRUE));
            }
    
            if (startDate != null)
            {
                criteria.add(
                    Expression.ge("pubTime", startDate));
            }
            
            if (endDate != null)
            {
                criteria.add(
                    Expression.le("pubTime", endDate));
            }
                            
            if (cat != null && website != null)
            {
                criteria.add(Expression.eq("category", cat));
            }
            
            if (status != null)
            {
                criteria.add(Expression.eq("status", status));
            }        

            if (pinned != null)
            {
                criteria.add(Expression.eq("pinnedToMain", pinned));
            }
                            
            criteria.addOrder(Order.desc("pubTime"));
            
            if (maxEntries != null) 
            {
                criteria.setMaxResults(maxEntries.intValue());
            }            
            return criteria.list();
        }
        catch (HibernateException e)
        {
            mLogger.error(e);
            throw new RollerException(e);
        }
    }
    
    public WeblogEntryData getWeblogEntryByAnchor(
                    WebsiteData website, String anchor) throws RollerException
    {
        if (website == null)
            throw new RollerException("Website is null");

        if (anchor == null)
            throw new RollerException("Anchor is null");

        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(WeblogEntryData.class);
        criteria.add(Expression.conjunction()
                        .add(Expression.eq("website",website))
                        .add(Expression.eq("anchor",anchor)));
        criteria.addOrder(Order.desc("pubTime"));
        criteria.setMaxResults(1);
        try
        {
            List list = criteria.list();
            return list.size()!=0 ? (WeblogEntryData)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public Date getWeblogLastPublishTime(WebsiteData website, String catName)
        throws RollerException
    {
        WeblogCategoryData cat = null;
        Roller mRoller = RollerFactory.getRoller();
        if (catName != null && website != null)
        {    
           cat = getWeblogCategoryByPath(website, null, catName);
           if (cat == null) catName = null;
        }
        if (catName != null && catName.trim().equals("/"))
        {
            catName = null;
        }
        
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(WeblogEntryData.class);
        criteria.add(Expression.eq("status", WeblogEntryData.PUBLISHED));
        criteria.add(Expression.le("pubTime", new Date()));

        try
        {
            if (website != null)
            {
                criteria.add(Expression.eq("website", website));
            }

            if ( cat != null )
            {
                criteria.add(Expression.eq("category", cat));
            }
            
            criteria.addOrder(Order.desc("pubTime"));
            criteria.setMaxResults(1);        
            List list = criteria.list();
            if (list.size() > 0)
            {
                return ((WeblogEntryData)list.get(0)).getPubTime();
            }
            else
            {
                return null;
            }
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public void moveWeblogCategoryContents(String srcId, String destId)
        throws RollerException
    {
        WeblogCategoryData srcCd =
            (WeblogCategoryData) mStrategy.load(
                srcId, WeblogCategoryData.class);
        
        WeblogCategoryData destCd =
            (WeblogCategoryData) mStrategy.load(
                destId, WeblogCategoryData.class);
        
        if (destCd.descendentOf(srcCd))
        {
            throw new RollerException(
                    "ERROR cannot move parent category into it's own child");
        }
        
        // get all entries in category and subcats
        List results = retrieveWeblogEntries(srcCd, true);    

        // Loop through entries in src cat, assign them to dest cat
        Iterator iter = results.iterator();
        WebsiteData website = destCd.getWebsite();
        while (iter.hasNext())
        {
            WeblogEntryData entry = (WeblogEntryData) iter.next();
            entry.setCategory(destCd);
            entry.setWebsite(website);
            entry.save();
        }
        
        // Make sure website's default and bloggerapi categories 
        // are valid after the move
        
        if (srcCd.getWebsite().getDefaultCategory().getId().equals(srcId)
            || srcCd.getWebsite().getDefaultCategory().descendentOf(srcCd)) 
        {
            srcCd.getWebsite().setDefaultCategory(destCd);
        }
        
        if (srcCd.getWebsite().getBloggerCategory().getId().equals(srcId)
            || srcCd.getWebsite().getBloggerCategory().descendentOf(srcCd)) 
        {
            srcCd.getWebsite().setBloggerCategory(destCd);
        }
    }
    
    public List retrieveWeblogEntries(WeblogCategoryData cat, boolean subcats) 
        throws RollerException
    {                
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            List entries = new LinkedList();
            
            if (subcats)
            {   
                // Get entries in subcategories
                Criteria assocsQuery = 
                    session.createCriteria(WeblogCategoryAssoc.class);
                assocsQuery.add(Expression.eq("ancestorCategory", cat));                
                Iterator assocs = assocsQuery.list().iterator();                
                while (assocs.hasNext())
                {
                    WeblogCategoryAssoc assoc = (WeblogCategoryAssoc)assocs.next();
                    Criteria entriesQuery = 
                        session.createCriteria(WeblogEntryData.class);
                    entriesQuery.add(
                        Expression.eq("category", assoc.getCategory()));
                    Iterator entryIter = entriesQuery.list().iterator();
                    while (entryIter.hasNext())
                    {
                        WeblogEntryData entry = (WeblogEntryData)entryIter.next();
                        entries.add(entry);
                    }
                }                
            }
            
            // Get entries in category
            Criteria entriesQuery = 
                session.createCriteria(WeblogEntryData.class);
            entriesQuery.add(Expression.eq("category", cat));                
            Iterator entryIter = entriesQuery.list().iterator();
            while (entryIter.hasNext())
            {
                WeblogEntryData entry = (WeblogEntryData)entryIter.next();
                entries.add(entry);
            }
            return entries;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public void removeWeblogEntryContents(WeblogEntryData entry) 
        throws RollerException
    {        
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            
            // remove referers
            Criteria refererQuery = session.createCriteria(RefererData.class);
            refererQuery.add(Expression.eq("weblogEntry", entry));
            List entries = refererQuery.list();
            for (Iterator iter = entries.iterator(); iter.hasNext();) 
            {
                RefererData referer = (RefererData) iter.next();
                referer.remove();
            }
            removeCommentsForEntry(entry.getId());
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public String createAnchor(WeblogEntryData entry) throws RollerException
    {        
        try
        {
            // Check for uniqueness of anchor
            String base = entry.createAnchorBase();
            String name = base;
            int count = 0;

            while (true)
            {
                if (count > 0)
                {
                    name = base + count;
                }
                
                Session session = ((HibernateStrategy)mStrategy).getSession();
                Criteria criteria = session.createCriteria(WeblogEntryData.class);
                criteria.add(Expression.eq("website", entry.getWebsite()));
                criteria.add(Expression.eq("anchor", name));
                             
                List results = criteria.list();
                
                if (results.size() < 1)
                {
                    break;
                }
                else
                {
                    count++;
                }
            }
            return name;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public boolean isDuplicateWeblogCategoryName(WeblogCategoryData cat) 
        throws RollerException
    {        
        // ensure that no sibling categories share the same name
        WeblogCategoryData parent = 
            null == cat.getId() ? (WeblogCategoryData)cat.getNewParent() : cat.getParent();
           
        if (null != parent) // don't worry about root
        {
            List sameNames;
            try
            {
                Session session = ((HibernateStrategy)mStrategy).getSession();
                Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
                criteria.createAlias("category", "c");
                criteria.add(Expression.eq("c.name", cat.getName()));
                criteria.add(Expression.eq("ancestorCategory", parent));
                criteria.add(Expression.eq("relation", Assoc.PARENT));
                sameNames = criteria.list();
            }
            catch (HibernateException e)
            {
                throw new RollerException(e);        
            }
            if (sameNames.size() > 1) 
            {
                return true;        
            }           
        }
        return false;
    }

    public boolean isWeblogCategoryInUse(WeblogCategoryData cat) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogEntryData.class);
            criteria.add(Expression.eq("category", cat));   
            criteria.setMaxResults(1); 
            int entryCount = criteria.list().size();
            
            if (entryCount > 0)
            {
                return true;
            }
            
            Iterator cats = cat.getWeblogCategories().iterator();
            while (cats.hasNext())
            {
                WeblogCategoryData childCat = (WeblogCategoryData)cats.next();
                if (childCat.isInUse())
                {
                    return true;
                }
            }
            
            if (cat.getWebsite().getBloggerCategory().equals(cat))
            {
                return true;
            }
            
            if (cat.getWebsite().getDefaultCategory().equals(cat))
            {
                return true;
            }
            
            return false;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public boolean isDescendentOf(
            WeblogCategoryData child, WeblogCategoryData ancestor)
            throws RollerException
    {
        boolean ret = false;
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("category", child));
            criteria.add(Expression.eq("ancestorCategory", ancestor));
            ret = criteria.list().size() > 0;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);        
        }
        return ret;
    }
    
    public Assoc getWeblogCategoryParentAssoc(WeblogCategoryData cat) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("category", cat));
            criteria.add(Expression.eq("relation", Assoc.PARENT));
            List parents = criteria.list();
            if (parents.size() > 1)
            {
                throw new RollerException("ERROR: more than one parent");
            }
            else if (parents.size() == 1)
            {
                return (Assoc) parents.get(0);
            }
            else
            {
                return null;
            }
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getWeblogCategoryChildAssocs(WeblogCategoryData cat) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("ancestorCategory", cat));
            criteria.add(Expression.eq("relation", Assoc.PARENT));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getAllWeblogCategoryDecscendentAssocs(WeblogCategoryData cat) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("ancestorCategory", cat));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getWeblogCategoryAncestorAssocs(WeblogCategoryData cat) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WeblogCategoryAssoc.class);
            criteria.add(Expression.eq("category", cat));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    public List getComments(
        WebsiteData     website, 
        WeblogEntryData entry, 
        String          searchString,
        Date            startDate, 
        Date            endDate, 
        Boolean         pending,
        Boolean         approved,
        Boolean         spam,
        int             offset,
        int             length
        ) throws RollerException {
    
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(CommentData.class);
            
            if (entry != null) {
                criteria.add(Expression.eq("weblogEntry", entry));
            }
            else if (website != null) {
                criteria.createAlias("weblogEntry","e");
                criteria.add(Expression.eq("e.website", website));
            }
            
            if (searchString != null) {
                criteria.add(Expression.disjunction()
                    .add(Expression.like("url", searchString, MatchMode.ANYWHERE))
                    .add(Expression.like("content", searchString, MatchMode.ANYWHERE)));            
            }
            
            if (startDate != null) {
                criteria.add(Expression.ge("postTime", startDate));
            }
            
            if (endDate != null) {
                criteria.add(Expression.le("postTime", endDate));
            }
            
            if (pending != null) {
                criteria.add(Expression.eq("pending", pending));
            }
            
            if (approved != null) {
                criteria.add(Expression.eq("approved", approved));
            }
            
            if (spam != null) {
                criteria.add(Expression.eq("spam", spam));
            }
            
            if (length != -1) {
                criteria.setMaxResults(offset + length);               
            }
            
            criteria.addOrder(Order.desc("postTime"));

            List comments = criteria.list();
            if (offset==0 || comments.size() < offset) {
                return comments;
            }
            List range = new ArrayList();
            for (int i=offset; i<comments.size(); i++) {
                range.add(comments.get(i));
            }
            return range;
            
        } catch (HibernateException e) {
            mLogger.error(e);
            throw new RollerException(e);
        }
    }
}








