/*
 * Created on Jun 18, 2004
 */
package org.roller.business.hibernate;
import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;
import org.roller.RollerException;
import org.roller.business.BookmarkManagerImpl;
import org.roller.business.PersistenceStrategy;
import org.roller.model.Roller;
import org.roller.pojos.Assoc;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderAssoc;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
/**
 * Hibernate queries.
 * 
 * @author David M Johnson
 */
public class HibernateBookmarkManagerImpl extends BookmarkManagerImpl
{
    /**
     * @param pstrategy
     * @param roller
     */
    public HibernateBookmarkManagerImpl(PersistenceStrategy pstrategy, Roller roller)
    {
        super(pstrategy, roller);
    }

    /**
     * @see org.roller.model.WeblogManager#retrieveWeblogEntries(
     *      org.roller.pojos.FolderData, boolean)
     */
    public List retrieveBookmarks(FolderData folder, boolean subfolders)
                    throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) mStrategy).getSession();
            List bookmarks = new LinkedList();
            if (subfolders)
            {
                // get bookmarks in subfolders
                Criteria assocsQuery = session
                                .createCriteria(FolderAssoc.class);
                assocsQuery.add(Expression.eq("ancestorFolder", folder));
                Iterator assocs = assocsQuery.list().iterator();
                while (assocs.hasNext())
                {
                    FolderAssoc assoc = (FolderAssoc) assocs.next();
                    Criteria bookmarksQuery = session
                                    .createCriteria(BookmarkData.class);
                    bookmarksQuery.add(Expression.eq("folder", assoc
                                    .getFolder()));
                    Iterator bookmarkIter = bookmarksQuery.list().iterator();
                    while (bookmarkIter.hasNext())
                    {
                        BookmarkData entry = (BookmarkData) bookmarkIter.next();
                        bookmarks.add(entry);
                    }
                }
            }
            // get bookmarks in folder
            Criteria bookmarksQuery = session
                            .createCriteria(BookmarkData.class);
            bookmarksQuery.add(Expression.eq("folder", folder));
            Iterator bookmarkIter = bookmarksQuery.list().iterator();
            while (bookmarkIter.hasNext())
            {
                BookmarkData bookmark = (BookmarkData) bookmarkIter.next();
                bookmarks.add(bookmark);
            }
            return bookmarks;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public FolderData getRootFolder(WebsiteData website) throws RollerException
    {
        if (website == null)
            throw new RollerException("website is null");
        try
        {
            Session session = ((HibernateStrategy) mStrategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.createAlias("folder", "f");
            criteria.add(Expression.eq("f.website", website));
            criteria.add(Expression.isNull("ancestorFolder"));
            criteria.add(Expression.eq("relation", FolderAssoc.PARENT));
            List results = criteria.list();
            if (results.size() > 1)
            {
                // Should not have more than one root
                throw new RollerException(
                                "More than one root folder found for website "
                                                + website.getId());
            }
            else if (results.size() == 1)
            {
                // Return root
                return ((FolderAssoc) results.get(0)).getFolder();
            }
            return null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getAllFolders(WebsiteData website) throws RollerException
    {
        if (website == null)
            throw new RollerException("Website is null");
        
        try
        {
            Session session = ((HibernateStrategy) mStrategy).getSession();
            Criteria criteria = session.createCriteria(FolderData.class);
            criteria.add(Expression.eq("website", website));            
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }

    }

    /** 
     * @see org.roller.model.BookmarkManager#isFolderInUser(org.roller.pojos.FolderData)
     */
    public boolean isDuplicateFolderName(FolderData folder) throws RollerException
    {
        // ensure that no sibling folders share the same name
        FolderData parent = 
            null == folder.getId() ? (FolderData)folder.getNewParent() : folder.getParent();
        
        if (null != parent)
        {
            List sameNames;
            try
            {
                Session session = ((HibernateStrategy) mStrategy).getSession();
                Criteria criteria = session.createCriteria(FolderAssoc.class);
                criteria.createAlias("folder", "f");
                criteria.add(Expression.eq("f.name", folder.getName()));
                criteria.add(Expression.eq("ancestorFolder", parent));
                criteria.add(Expression.eq("relation", Assoc.PARENT));
                sameNames = criteria.list();
            }
            catch (HibernateException e)
            {
                throw new RollerException(e);        
            }
            if (sameNames.size() > 0) 
            {
                return true;        
            }
        }
        return false;
    }

    /** 
     * @see org.roller.model.BookmarkManager#getFolderParentAssoc(
     * org.roller.pojos.FolderData)
     */
    public Assoc getFolderParentAssoc(FolderData folder) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("folder", folder));
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

    /** 
     * @see org.roller.model.BookmarkManager#getFolderChildAssocs(
     * org.roller.pojos.FolderData)
     */
    public List getFolderChildAssocs(FolderData folder) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("ancestorFolder", folder));
            criteria.add(Expression.eq("relation", Assoc.PARENT));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    /** 
     * @see org.roller.model.BookmarkManager#getAllFolderDecscendentAssocs(
     * org.roller.pojos.FolderData)
     */
    public List getAllFolderDecscendentAssocs(FolderData folder) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("ancestorFolder", folder));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    /** 
     * @see org.roller.model.BookmarkManager#getFolderAncestorAssocs(
     * org.roller.pojos.FolderData)
     */
    public List getFolderAncestorAssocs(FolderData folder) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("folder", folder));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
}
