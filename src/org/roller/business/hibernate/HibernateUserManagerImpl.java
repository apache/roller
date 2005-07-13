/*
 * Created on Jun 16, 2004
 */
package org.roller.business.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Query;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.EqExpression;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Order;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.UserManagerImpl;
import org.roller.model.AutoPingManager;
import org.roller.model.BookmarkManager;
import org.roller.model.PingQueueManager;
import org.roller.model.PingTargetManager;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.FolderData;
import org.roller.pojos.PageData;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.RefererData;
import org.roller.pojos.RoleData;
import org.roller.pojos.UserCookieData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

/**
 * Hibernate queries.
 * @author David M Johnson
 */
public class HibernateUserManagerImpl extends UserManagerImpl
{
    static final long serialVersionUID = -5128460637997081121L;
    
    private static Log mLogger =
        LogFactory.getFactory().getInstance(HibernateUserManagerImpl.class);
    
    /**
     * @param strategy
     */
    public HibernateUserManagerImpl(PersistenceStrategy strategy)
    {
        super(strategy);
        mLogger.debug("Instantiating User Manager");
    }
    
    /**
     * Get websites of a user
     */
    public List getWebsites(UserData user, Boolean enabled)  throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WebsiteData.class);
            if (user != null) 
            {
                criteria.createAlias("permissions","permissions");
                criteria.add(Expression.eq("permissions.user", user));
            }
            if (enabled != null)
            {
                criteria.add(Expression.eq("isEnabled", enabled));
            }
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /**
     * Get users of a website
     */
    public List getUsers(WebsiteData website, Boolean enabled) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(UserData.class);
            if (website != null) 
            {
                criteria.createAlias("permissions","permissions");
                criteria.add(Expression.eq("permissions.website", website));
            }
            if (enabled != null)
            {
                criteria.add(Expression.eq("isEnabled", enabled));
            }
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /** 
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    public PageData getPageByLink(WebsiteData website, String pagelink)
        throws RollerException
    {
        if (website == null)
            throw new RollerException("userName is null");
                                   
        if (pagelink == null)
            throw new RollerException("Pagelink is null");
                                                                      
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(PageData.class);
        criteria.add(Expression.eq("website",website));
        criteria.add(Expression.eq("link",pagelink));        
        criteria.setMaxResults(1);
        try
        {
            List list = criteria.list();
            return list.size()!=0 ? (PageData)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /** 
     * Return website specified by handle.
     */
    public WebsiteData getWebsiteByHandle(String handle, Boolean enabled)
                    throws RollerException
    {
        if (handle==null )
            throw new RollerException("Handle cannot be null");

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(WebsiteData.class);
            if (enabled != null) 
            {
                criteria.add(
                   Expression.conjunction()
                       .add(new EqExpression("handle", handle, true))
                       .add(Expression.eq("isEnabled", enabled)));
            }
            else
            {
                criteria.add(
                    Expression.conjunction()
                        .add(new EqExpression("handle", handle, true)));
            }        
            return (WebsiteData)criteria.uniqueResult();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -     
    public UserData getUser(String userName, Boolean enabled) 
        throws RollerException
    {
        if (userName==null )
            throw new RollerException("userName cannot be null");

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(UserData.class);
            if (enabled != null) 
            {
                criteria.add(
                   Expression.conjunction()
                       .add(new EqExpression("userName", userName, true))
                       .add(Expression.eq("isEnabled", enabled)));
            }
            else
            {
                criteria.add(
                    Expression.conjunction()
                        .add(new EqExpression("userName", userName, true)));
            }        
            return (UserData)criteria.uniqueResult();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    /**
     * @see org.roller.model.UserManager#removeLoginCookies(java.lang.String)
     */
    public void removeLoginCookies(String username) throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(UserCookieData.class); 
        criteria.add(Expression.eq("username", username));        
        List list;
        try
        {
            list = criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
        for (Iterator it = list.iterator(); it.hasNext();)
        {
            String id = ((UserCookieData) it.next()).getId();
            mStrategy.remove(id, UserCookieData.class);
        }        
    }
    
    /**
     * @see org.roller.model.UserManager#checkLoginCookie(java.lang.String)
     */
    public String checkLoginCookie(String value) throws RollerException 
    {
        try 
        {
            value = Utilities.decodeString(value);
        } 
        catch (IOException io) 
        {
            mLogger.warn("Failed to decode rememberMe cookieString");
            return null;
        }
        
        String[] values = StringUtils.split(value, "|");

        if (mLogger.isDebugEnabled()) 
        {
            mLogger.debug("looking up cookieId: " + values[1]);
        }

        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(UserCookieData.class); 
        criteria.add(Expression.eq("username", values[0]));
        criteria.add(Expression.eq("cookieId", values[1])); 
               
        List list;
        try
        {
            list = criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
        UserCookieData cookie = (list.size() > 0) ? (UserCookieData)list.get(0) : null;

        if (cookie != null) 
        {
            if (mLogger.isDebugEnabled()) 
            {
                mLogger.debug("cookieId lookup succeeded, generating new cookieId");
            }
            return saveLoginCookie(cookie);
        } 
        else 
        {
            if (mLogger.isDebugEnabled()) 
            {
                mLogger.debug("cookieId lookup failed, returning null");
            }

            return null;
        }
    }
    
    //------------------------------------------------------------------------    
    /** 
     * @see org.roller.model.UserManager#getPages(WebsiteData)
     */
    public List getPages(WebsiteData website) throws RollerException
    {
        if (website == null)
            throw new RollerException("website is null");
                                                                      
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(PageData.class);
        criteria.add(Expression.eq("website",website)); 
        criteria.addOrder(Order.asc("name"));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /** 
     * @see org.roller.model.UserManager#getPageByName(WebsiteData, java.lang.String)
     */
    public PageData getPageByName(WebsiteData website, String pagename) 
        throws RollerException
    {
        if (website == null)
            throw new RollerException("website is null");
                                   
        if (pagename == null)
            throw new RollerException("Page name is null");
                                   
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(PageData.class);
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.eq("name", pagename));
        criteria.setMaxResults(1);
        try
        {
            List list = criteria.list();
            return list.size()!=0 ? (PageData)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /* 
     * @see org.roller.business.UserManagerBase#getRoles(org.roller.pojos.UserData)
     */
    public List getUserRoles(UserData user) throws RollerException
    {
        
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RoleData.class);
        criteria.add(Expression.eq("user", user));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getUsers(Boolean enabled) throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(UserData.class);            
        if (enabled != null)
        {
            criteria.add(Expression.eq("isEnabled", enabled));
        }
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

//    public void removeUserWebsites(UserData user) throws RollerException
//    {
//        Session session = ((HibernateStrategy)mStrategy).getSession();
//        Criteria criteria = session.createCriteria(WebsiteData.class);
//        criteria.add(Expression.eq("user", user));
//        try
//        {
//            List websites = criteria.list();
//            for (Iterator iter = websites.iterator(); iter.hasNext();) 
//            {
//                WebsiteData website = (WebsiteData)iter.next();
//                website.remove();
//            }            
//        }
//        catch (HibernateException e)
//        {
//            throw new RollerException(e);
//        }
//    }

    /** 
     * @see org.roller.model.UserManager#removeWebsiteContents(org.roller.pojos.WebsiteData)
     */
    public void removeWebsiteContents(WebsiteData website) 
        throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();

            //UserManager umgr = RollerFactory.getRoller().getUserManager();
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            //PersistenceStrategy pstrat = RollerFactory.getRoller().getPersistenceStrategy();
            //QueryFactory factory = pstrat.getQueryFactory();
            
            // remove folders (takes bookmarks with it)
            FolderData rootFolder = bmgr.getRootFolder(website);
            if (null != rootFolder)
            { 
                rootFolder.remove();
                // Still cannot get all Bookmarks cleared!                
//                Iterator allFolders = bmgr.getAllFolders(website).iterator();
//                while (allFolders.hasNext()) 
//                {
//                    FolderData aFolder = (FolderData)allFolders.next();
//                    bmgr.deleteFolderContents(aFolder);
//                    aFolder.remove();
//                }
            }
                        
            // remove entries
            Criteria entryQuery = session.createCriteria(WeblogEntryData.class);
            entryQuery.add(Expression.eq("website", website));
            List entries = entryQuery.list();
            for (Iterator iter = entries.iterator(); iter.hasNext();) 
            {
                WeblogEntryData entry = (WeblogEntryData) iter.next();
                entry.remove();
            }
            
            // remove associated pages
            Criteria pageQuery = session.createCriteria(PageData.class);
            pageQuery.add(Expression.eq("website", website));
            List pages = pageQuery.list();
            for (Iterator iter = pages.iterator(); iter.hasNext();) 
            {
                PageData page = (PageData) iter.next();
                page.remove();
            }
            
            // remove associated referers
            Criteria refererQuery = session.createCriteria(RefererData.class);
            refererQuery.add(Expression.eq("website", website));
            List referers = refererQuery.list();
            for (Iterator iter = referers.iterator(); iter.hasNext();) 
            {
                RefererData referer = (RefererData) iter.next();
                referer.remove();
            }
            
            // remove categories
            WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(website);
            if (null != rootCat)
            {
                rootCat.remove();
                Iterator it = wmgr.getWeblogCategories(website).iterator();
                while (it.hasNext()) 
                {
                     ((WeblogCategoryData)it.next()).remove();
                }
            }

            // Remove the website's ping queue entries
            PingQueueManager pingQueueMgr = RollerFactory.getRoller().getPingQueueManager();
            pingQueueMgr.removeQueueEntriesByWebsite(website);

            // Remove the website's custom ping targets
            PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
            pingTargetMgr.removeCustomPingTargets(website);

            // Remove the website's auto ping configurations
            AutoPingManager autoPingMgr = RollerFactory.getRoller().getAutopingManager();
            List autopings = autoPingMgr.getAutoPingsByWebsite(website);
            autoPingMgr.removeAutoPings(autopings);

        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    /**
     * Return permissions for specified user in website
     */
    public PermissionsData getPermissions(
            WebsiteData website, UserData user) throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(PermissionsData.class);            
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.eq("user", user));
        try
        {
            List list = criteria.list();
            return list.size()!=0 ? (PermissionsData)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /** 
     * Get pending permissions for user
     */
    public List getPendingPermissions(UserData user) throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(PermissionsData.class);            
        criteria.add(Expression.eq("user", user));
        criteria.add(Expression.eq("pending", Boolean.TRUE));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /** 
     * Get pending permissions for website
     */
    public List getPendingPermissions(WebsiteData website) throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(PermissionsData.class);            
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.eq("pending", Boolean.TRUE));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
}

