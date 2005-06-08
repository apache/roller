/*
 * Created on Mar 4, 2004
 */
package org.roller.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.persistence.MockPersistenceStrategy;
import org.roller.pojos.PageData;
import org.roller.pojos.RoleData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author lance.lavandowska
 */
public class MockUserManager implements UserManager
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MockUserManager.class);
    
    private PersistenceStrategy mStrategy = null;

    /**
     * @param strategy
     * @param roller
     */
    public MockUserManager(PersistenceStrategy strategy, MockRoller roller)
    {
        mStrategy = strategy;
    }

    /* 
     * @see org.roller.model.UserManager#release()
     */
    public void release()
    {
    }

    /* 
     * @see org.roller.model.UserManager#getUsers()
     */
    public List getUsers() throws RollerException
    {
        Map userMap = ((MockPersistenceStrategy)mStrategy).getObjectStore(UserData.class);
        return new ArrayList(userMap.values());
    }

    /* 
     * @see org.roller.model.UserManager#getUsers(boolean)
     */
    public List getUsers(boolean enabledOnly) throws RollerException
    {
        Map userMap = ((MockPersistenceStrategy)mStrategy).getObjectStore(UserData.class);
        ArrayList eUsers = new ArrayList();
        Iterator it = userMap.values().iterator();
        while(it.hasNext())
        {
            UserData user = (UserData)it.next();
            if (!enabledOnly || getWebsite(user.getUserName()).getIsEnabled().booleanValue())
            {
                eUsers.add(user);
            }
        }
        return eUsers;
    }

    /* 
     * @see org.roller.model.UserManager#getUser(java.lang.String)
     */
    public UserData getUser(String userName) throws RollerException
    {
        Map userMap = ((MockPersistenceStrategy)mStrategy).getObjectStore(UserData.class);
        Iterator it = userMap.values().iterator();
        while(it.hasNext())
        {
            UserData user = (UserData)it.next();
            if (user.getUserName().equals(userName)) return user;
        }
        return null;
    }

    /* 
     * @see org.roller.model.UserManager#getUser(java.lang.String, boolean)
     */
    public UserData getUser(String userName, boolean enabledOnly)
            throws RollerException
    {
        Map userMap = ((MockPersistenceStrategy)mStrategy).getObjectStore(UserData.class);
        Iterator it = userMap.values().iterator();
        while(it.hasNext())
        {
            UserData user = (UserData)it.next();
            if (user.getUserName().equals(userName))
            {
                if (!enabledOnly || getWebsite(userName).getIsEnabled().booleanValue()) 
                {
                    return user;
                }
                return null;
            }
        }
        return null;
    }

    /* 
     * @see org.roller.model.UserManager#getUserById(java.lang.String)
     */
    public UserData getUserById(String userId) throws RollerException
    {
        return (UserData)mStrategy.load(userId, UserData.class);
    }

    /* 
     * @see org.roller.model.UserManager#getUserById(java.lang.String, boolean)
     */
    public UserData getUserById(String userId, boolean enabledOnly)
            throws RollerException
    {
        UserData user = (UserData)mStrategy.load(userId, UserData.class);
        if (enabledOnly)
        {
            if (getWebsite(user.getUserName()).getIsEnabled().booleanValue())
            {
                return user;
            }
            else
            {
                return null;
            }
        }
        
        return user;
    }

    /* 
     * @see org.roller.model.UserManager#addUser(org.roller.pojos.UserData, java.util.Map, java.lang.String, java.lang.String, java.lang.String)
     */
    public void addUser(UserData user, Map page, String theme, String locale,
                        String timezone) throws RollerException
    {
        // TODO Auto-generated method stub
        // ugh, lots of code duplication needs to go here
    }

    /* 
     * @see org.roller.model.UserManager#retrieveUser(java.lang.String)
     */
    public UserData retrieveUser(String id) throws RollerException
    {
        return (UserData)mStrategy.load(id, UserData.class);
    }

    /* 
     * @see org.roller.model.UserManager#storeUser(org.roller.pojos.UserData)
     */
    public void storeUser(UserData data) throws RollerException
    {
        mStrategy.store(data);
    }

    /* 
     * @see org.roller.model.UserManager#retrieveRole(java.lang.String)
     */
    public RoleData retrieveRole(String id) throws RollerException
    {
        return (RoleData)mStrategy.load(id, RoleData.class);
    }

    /* 
     * @see org.roller.model.UserManager#storeRole(org.roller.pojos.RoleData)
     */
    public void storeRole(RoleData data) throws RollerException
    {
        mStrategy.store(data);
    }

    /* 
     * @see org.roller.model.UserManager#removeRole(java.lang.String)
     */
    public void removeRole(String id) throws RollerException
    {
        mStrategy.remove(id, RoleData.class);
    }

    /* 
     * @see org.roller.model.UserManager#getWebsite(java.lang.String)
     */
    public WebsiteData getWebsite(String userName) throws RollerException
    {
        Map websiteMap = ((MockPersistenceStrategy)mStrategy).getObjectStore(WebsiteData.class);
        Iterator it = websiteMap.values().iterator();
        while(it.hasNext())
        {
            WebsiteData website = (WebsiteData)it.next();
            if (website.getUser().getUserName().equals(userName))
            {
                return website;
            }
        }
        return null;
    }

    /* 
     * @see org.roller.model.UserManager#getWebsite(java.lang.String, boolean)
     */
    public WebsiteData getWebsite(String userName, boolean enabledOnly)
            throws RollerException
    {
        Map websiteMap = ((MockPersistenceStrategy)mStrategy).getObjectStore(WebsiteData.class);
        Iterator it = websiteMap.values().iterator();
        while(it.hasNext())
        {
            WebsiteData website = (WebsiteData)it.next();
            if (website.getUser().getUserName().equals(userName))
            {
                if (enabledOnly)
                {
                    if (website.getIsEnabled().booleanValue()) 
                        return website;
                    else if (enabledOnly) 
                        return null;
                }
                else
                {
                    return website;
                }
            }
        }
        return null;
    }

    /* 
     * @see org.roller.model.UserManager#retrieveWebsite(java.lang.String)
     */
    public WebsiteData retrieveWebsite(String id) throws RollerException
    {
        return (WebsiteData)mStrategy.load(id, WebsiteData.class);
    }

    /* 
     * @see org.roller.model.UserManager#storeWebsite(org.roller.pojos.WebsiteData)
     */
    public void storeWebsite(WebsiteData data) throws RollerException
    {
        mStrategy.store(data);
    }

    /* 
     * @see org.roller.model.UserManager#removeWebsite(java.lang.String)
     */
    public void removeWebsite(String id) throws RollerException
    {
        mStrategy.remove(id, WebsiteData.class);
    }

    /* 
     * @see org.roller.model.UserManager#getPageByName(org.roller.pojos.WebsiteData, java.lang.String)
     */
    public PageData getPageByName(WebsiteData w, String p)
            throws RollerException
    {
        List pages = getPages(w);
        Iterator it = pages.iterator();
        while (it.hasNext())
        {
            PageData page = (PageData)it.next();
            if (page.getName().equals(p)) return page;
        }
        return null;
    }

    /* 
     * @see org.roller.model.UserManager#getPageByLink(org.roller.pojos.WebsiteData, java.lang.String)
     */
    public PageData getPageByLink(WebsiteData w, String p)
            throws RollerException
    {
        List pages = getPages(w);
        Iterator it = pages.iterator();
        while (it.hasNext())
        {
            PageData page = (PageData)it.next();
            if (page.getLink().equals(p)) return page;
        }
        return null;
    }

    /* 
     * @see org.roller.model.UserManager#fixPageLink(org.roller.pojos.PageData)
     */
    public String fixPageLink(PageData data) throws RollerException
    {
        return data.getLink();
    }

    /* 
     * @see org.roller.model.UserManager#getPages(org.roller.pojos.WebsiteData)
     */
    public List getPages(WebsiteData w) throws RollerException
    {
        // TODO Auto-generated method stub
        return new ArrayList();
    }

    /* 
     * @see org.roller.model.UserManager#retrievePage(java.lang.String)
     */
    public PageData retrievePage(String id) throws RollerException
    {
        return (PageData)mStrategy.load(id, PageData.class);
    }

    /* 
     * @see org.roller.model.UserManager#storePage(org.roller.pojos.PageData)
     */
    public void storePage(PageData data) throws RollerException
    {
        mStrategy.store(data);
    }

    /* 
     * @see org.roller.model.UserManager#removePage(java.lang.String)
     */
    public void removePage(String id) throws RollerException
    {
        mStrategy.remove(id, PageData.class);
    }

    /* 
     * @see org.roller.model.UserManager#retrievePageReadOnly(java.lang.String)
     */
    public PageData retrievePageReadOnly(String id) throws RollerException
    {
        return (PageData)mStrategy.load(id, PageData.class);
    }

    /** 
     * @see org.roller.model.UserManager#checkLoginCookie(java.lang.String)
     */
    public String checkLoginCookie(String value) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.UserManager#createLoginCookie(java.lang.String)
     */
    public String createLoginCookie(String username) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** 
     * @see org.roller.model.UserManager#removeLoginCookies(java.lang.String)
     */
    public void removeLoginCookies(String username) throws RollerException
    {
        // TODO Auto-generated method stub
        
    }

    /** 
     * @see org.roller.model.UserManager#removeUserWebsites(org.roller.pojos.UserData)
     */
    public void removeUserWebsites(UserData data) throws RollerException
    {
        // TODO Auto-generated method stub
        
    }

    /** 
     * @see org.roller.model.UserManager#removeWebsiteContents(org.roller.pojos.WebsiteData)
     */
    public void removeWebsiteContents(WebsiteData data) throws RollerException
    {
        // TODO Auto-generated method stub
        
    }

    /* 
     * @see org.roller.model.UserManager#getUserRoles(org.roller.pojos.UserData)
     */
    public List getUserRoles(UserData user) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

}
