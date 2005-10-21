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
import org.roller.pojos.Assoc;
import org.roller.pojos.BookmarkData;
import org.roller.pojos.FolderAssoc;
import org.roller.pojos.FolderData;
import org.roller.pojos.WebsiteData;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Hibernate queries.
 * 
 * @author David M Johnson
 */
public class HibernateBookmarkManagerImpl extends BookmarkManagerImpl
{
    static final long serialVersionUID = 5286654557062382772L;

    private static Log mLogger =
        LogFactory.getFactory().getInstance(HibernateBookmarkManagerImpl.class);
    
    /**
     * @param pstrategy
     * @param roller
     */
    public HibernateBookmarkManagerImpl(PersistenceStrategy pstrategy)
    {
        super(pstrategy);
        mLogger.debug("Instantiating Bookmark Manager");
    }

    /**
     * @see org.roller.model.BookmarkManager#retrieveBookmarks(
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
     * @see org.roller.model.BookmarkManager#isDuplicateFolderName(org.roller.pojos.FolderData)
     */
    public boolean isDuplicateFolderName(FolderData folder) throws RollerException
    {
        // ensure that no sibling folders share the same name
        boolean isNewFolder = (folder.getId() == null);
        FolderData parent =
            isNewFolder ? (FolderData)folder.getNewParent() : folder.getParent();
        
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
            // If we got some matches
            if (sameNames.size() > 0)
            {
                // if we're saving a new folder, any matches are dups
                if (isNewFolder) return true;
                // otherwise it's a dup it isn't the same one (one match with the same id).
                if (!(sameNames.size() == 1 && folder.getId().equals(((FolderAssoc)sameNames.get(0)).getFolder().getId())))
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

    /**
     * @see org.roller.model.BookmarkManager#isFolderInUse(org.roller.pojos.FolderData)
     */
    public boolean isFolderInUse(FolderData folder) throws RollerException
    {
        try
        {
            // We consider a folder to be "in use" if it contains any bookmarks or has
            // any children.

            // We first determine the number of bookmark entries.
            // NOTE: This seems to be an attempt to optimize, rather than just use getBookmarks(),
            // but I'm not sure that this optimization is really worthwhile, and it ignores
            // caching in the case that the (lazy) getBookmarks has been done already. --agangolli
            // TODO: condider changing to just use getBookmarks().size()
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(BookmarkData.class);
            criteria.add(Expression.eq("folder", folder));
            criteria.setMaxResults(1);
            int entryCount = criteria.list().size();

            // Return true if we have bookmarks or (, failing that, then checking) if we have children
            return (entryCount > 0 || folder.getFolders().size() > 0);
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public boolean isDescendentOf(FolderData child, FolderData ancestor)
        throws RollerException
    {
        boolean ret = false;
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(FolderAssoc.class);
            criteria.add(Expression.eq("folder", child));
            criteria.add(Expression.eq("ancestorFolder", ancestor));
            ret = criteria.list().size() > 0;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);        
        }
        return ret;
    }    
}
