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

package org.apache.roller.weblogger.business.hibernate;

import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Collection;

import org.apache.roller.weblogger.pojos.StatCount;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.criterion.SimpleExpression;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogUserPermission;
import org.apache.roller.weblogger.pojos.PingQueueEntry;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.UserRole;
import org.hibernate.Query;


/**
 * Hibernate implementation of the UserManager.
 */    
@com.google.inject.Singleton
public class HibernateUserManagerImpl implements UserManager {
    
    static final long serialVersionUID = -5128460637997081121L;
    
    private static Log log = LogFactory.getLog(HibernateUserManagerImpl.class);
    
    private final Weblogger roller;
    private final HibernatePersistenceStrategy strategy;
    
    // cached mapping of weblogHandles -> weblogIds
    private Map weblogHandleToIdMap = new Hashtable();
    
    // cached mapping of userNames -> userIds
    private Map userNameToIdMap = new Hashtable();
   
    
    @com.google.inject.Inject
    protected HibernateUserManagerImpl(Weblogger roller, HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate User Manager");
        this.roller = roller;       
        this.strategy = strat;
    }
    
    
    public void saveUser(User data) throws WebloggerException {
        this.strategy.store(data);
    }
        
    public void removeUser(User user) throws WebloggerException {
        this.strategy.remove(user);
        
        // remove entry from cache mapping
        this.userNameToIdMap.remove(user.getUserName());
    }
        
    public void savePermissions(WeblogUserPermission perms) throws WebloggerException {
        this.strategy.store(perms);
    }
        
    public void removePermissions(WeblogUserPermission perms) throws WebloggerException {
        
        // make sure associations are broken
        perms.getWebsite().getPermissions().remove(perms);
        perms.getUser().getPermissions().remove(perms);
        
        this.strategy.remove(perms);
    }
        
    public void addUser(User newUser) throws WebloggerException {
        
        if(newUser == null)
            throw new WebloggerException("cannot add null user");
        
        // TODO BACKEND: we must do this in a better fashion, like getUserCnt()?
        boolean adminUser = false;
        List existingUsers = this.getUsers(null, Boolean.TRUE, null, null, 0, 1);
        if(existingUsers.size() == 0) {
            // Make first user an admin
            adminUser = true;
        }
        
        if(getUserByUserName(newUser.getUserName()) != null ||
                getUserByUserName(newUser.getUserName().toLowerCase()) != null) {
            throw new WebloggerException("error.add.user.userNameInUse");
        }
        
        newUser.grantRole("editor");
        if(adminUser) {
            newUser.grantRole("admin");
            
            //if user was disabled (because of activation user account with e-mail property), 
            //enable it for admin user
            newUser.setEnabled(Boolean.TRUE);
            newUser.setActivationCode(null);
        }
        
        this.strategy.store(newUser);
    }
        
    public User getUser(String id) throws WebloggerException {
        return (User)this.strategy.load(id,User.class);
    }
        
    public User getUserByUserName(String userName) throws WebloggerException {
        return getUserByUserName(userName, Boolean.TRUE);
    }
    
    public User getUserByUserName(String userName, Boolean enabled)
    throws WebloggerException {
        
        if (userName==null )
            throw new WebloggerException("userName cannot be null");
        
        // check cache first
        // NOTE: if we ever allow changing usernames then this needs updating
        if(this.userNameToIdMap.containsKey(userName)) {
            
            User user = this.getUser((String) this.userNameToIdMap.get(userName));
            if(user != null) {
                // only return the user if the enabled status matches
                if(enabled == null || enabled.equals(user.getEnabled())) {
                    log.debug("userNameToIdMap CACHE HIT - "+userName);
                    return user;
                }
            } else {
                // mapping hit with lookup miss?  mapping must be old, remove it
                this.userNameToIdMap.remove(userName);
            }
        }
        
        // cache failed, do lookup
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            
            if (enabled != null) {
                criteria.add(
                        Expression.conjunction()
                        .add(Expression.eq("userName", userName))
                        .add(Expression.eq("enabled", enabled)));
            } else {
                criteria.add(
                        Expression.conjunction()
                        .add(Expression.eq("userName", userName)));
            }
            
            User user = (User) criteria.uniqueResult();
            
            // add mapping to cache
            if(user != null) {
                log.debug("userNameToIdMap CACHE MISS - "+userName);
                this.userNameToIdMap.put(user.getUserName(), user.getId());
            }
            
            return user;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getUsers(Weblog weblog, Boolean enabled, Date startDate, 
                         Date endDate, int offset, int length) 
            throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            
            if (weblog != null) {
                criteria.createAlias("permissions", "permissions");
                criteria.add(Expression.eq("permissions.website", weblog));
            }
            
            if (enabled != null) {
                criteria.add(Expression.eq("enabled", enabled));
            }
            
            if (startDate != null) {
                // if we are doing date range then we must have an end date
                if(endDate == null) {
                    endDate = new Date();
                }
                
                criteria.add(Expression.lt("dateCreated", endDate));
                criteria.add(Expression.gt("dateCreated", startDate));
            }
            
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            
            criteria.addOrder(Order.desc("dateCreated"));
            
            return criteria.list();
            
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
        
    public List getUsersStartingWith(String startsWith, Boolean enabled,
            int offset, int length) throws WebloggerException {
        
        List results = new ArrayList();
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            
            if (enabled != null) {
                criteria.add(Expression.eq("enabled", enabled));
            }
            if (startsWith != null) {
                criteria.add(Expression.disjunction()
                .add(Expression.like("userName", startsWith, MatchMode.START))
                .add(Expression.like("emailAddress", startsWith, MatchMode.START)));
            }
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            results = criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
        return results;
    }
    
    public WeblogUserPermission getPermissions(String inviteId) throws WebloggerException {
        return (WeblogUserPermission)this.strategy.load(inviteId,WeblogUserPermission.class);
    }
    
    /**
     * Return permissions for specified user in website
     */
    public WeblogUserPermission getPermissions(
            Weblog website, User user) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogUserPermission.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("user", user));
            
            List list = criteria.list();
            return list.size()!=0 ? (WeblogUserPermission)list.get(0) : null;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get pending permissions for user
     */
    public List getPendingPermissions(User user) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogUserPermission.class);
            criteria.add(Expression.eq("user", user));
            criteria.add(Expression.eq("pending", Boolean.TRUE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get pending permissions for website
     */
    public List getPendingPermissions(Weblog website) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogUserPermission.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("pending", Boolean.TRUE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get all permissions of a website (pendings not including)
     */
    public List getAllPermissions(Weblog website) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogUserPermission.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("pending", Boolean.FALSE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    /**
     * Get all permissions of a user.
     */
    public List getAllPermissions(User user) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogUserPermission.class);
            criteria.add(Expression.eq("user", user));
            criteria.add(Expression.eq("pending", Boolean.FALSE));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    public void release() {}
    
    public Map getUserNameLetterMap() throws WebloggerException {
        // TODO: ATLAS getUserNameLetterMap DONE TESTED
        String msg = "Getting username letter map";
        try {      
            String lc = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
            Map results = new TreeMap();
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();
            for (int i=0; i<26; i++) {
                Query query = session.createQuery(
                    "select count(user) from User user where upper(user.userName) like '"+lc.charAt(i)+"%'");
                List row = query.list();
                Number count = (Number) row.get(0);
                results.put(new String(new char[]{lc.charAt(i)}), count);
            }
            return results;
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new WebloggerException(msg, pe);
        }
    }
    
    public List getUsersByLetter(char letter, int offset, int length) 
        throws WebloggerException { 
        // TODO: ATLAS getUsersByLetter DONE
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(User.class);
            criteria.add(Expression.ilike("userName", new String(new char[]{letter}) + "%", MatchMode.START));
            criteria.addOrder(Order.asc("userName"));
            if (offset != 0) {
                criteria.setFirstResult(offset);
            }
            if (length != -1) {
                criteria.setMaxResults(length);
            }
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    public void revokeRole(String roleName, User user) throws WebloggerException {
        UserRole removeme = null;
        Collection roles = user.getRoles();
        Iterator iter = roles.iterator();
        while (iter.hasNext()) {
            UserRole role = (UserRole) iter.next();
            if (role.getRole().equals(roleName)) {
                iter.remove();
                this.strategy.remove(role);
            }
        }
    }


    
    /**
     * Get count of users, enabled only
     */    
    public long getUserCount() throws WebloggerException {
        long ret = 0;
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            String query = "select count(distinct u) from User u where u.enabled=true";
            List result = session.createQuery(query).list();
            ret = ((Number)result.get(0)).intValue();
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
        return ret;
    }
    
     
    /** Doesn't seem to be any other way to get ignore case w/o QBE */
    class IgnoreCaseEqExpression extends SimpleExpression {
        public IgnoreCaseEqExpression(String property, Object value) {
            super(property, value, "=", true);
        }
    }


	public User getUserByActivationCode(String activationCode) throws WebloggerException {

		if (activationCode == null)
			throw new WebloggerException("activationcode is null");

		try {
			Session session = ((HibernatePersistenceStrategy) this.strategy)
					.getSession();
			Criteria criteria = session.createCriteria(User.class);

			criteria.add(Expression.eq("activationCode", activationCode));

			User user = (User) criteria.uniqueResult();

			return user;
		} catch (HibernateException e) {
			throw new WebloggerException(e);
		}		
	}

   /**
     * Creates and stores a pending PermissionsData for user and website specified.
     * TODO BACKEND: do we really need this?  can't we just use storePermissions()?
     */
    public WeblogUserPermission inviteUser(Weblog website,
            User user, short mask) throws WebloggerException {
        
        if (website == null) throw new WebloggerException("Website cannot be null");
        if (user == null) throw new WebloggerException("User cannot be null");
        
        WeblogUserPermission perms = new WeblogUserPermission();
        perms.setWebsite(website);
        perms.setUser(user);
        perms.setPermissionMask(mask);
        this.strategy.store(perms);
        
        // manage associations
        website.getPermissions().add(perms);
        user.getPermissions().add(perms);
        
        return perms;
    }
        
    /**
     * Remove user permissions from a website.
     *
     * TODO: replace this with a domain model method like weblog.retireUser(user)
     */
    public void retireUser(Weblog website, User user) throws WebloggerException {
        
        if (website == null) throw new WebloggerException("Website cannot be null");
        if (user == null) throw new WebloggerException("User cannot be null");
        
        Iterator perms = website.getPermissions().iterator();
        WeblogUserPermission target = null;
        while (perms.hasNext()) {
            WeblogUserPermission pd = (WeblogUserPermission)perms.next();
            if (pd.getUser().getId().equals(user.getId())) {
                target = pd;
                break;
            }
        }
        if (target == null) throw new WebloggerException("User not member of website");
        
        website.removePermission(target);
        this.strategy.remove(target);
    }
        
}






