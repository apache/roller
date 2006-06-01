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
package org.apache.roller.webservices.adminapi;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.apache.roller.RollerException;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.webservices.adminapi.sdk.Entry;
import org.apache.roller.webservices.adminapi.sdk.EntrySet;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.apache.roller.webservices.adminapi.sdk.UserEntry;
import org.apache.roller.webservices.adminapi.sdk.UserEntrySet;

/**
 * This class handles request concerning Roller users.
 *
 * @author jtb
 */
class RollerUserHandler extends Handler {
    public RollerUserHandler(HttpServletRequest request) throws HandlerException {
        super(request);
    }
    
    protected EntrySet getEntrySet(Document d) throws MissingElementException, UnexpectedRootElementException {
        return new UserEntrySet(d, getUrlPrefix());
    }
    
    public EntrySet processGet() throws HandlerException {
        if (getUri().isCollection()) {
            return getCollection();
        } else if (getUri().isEntry()) {
            return getEntry();
        } else {
            throw new BadRequestException("ERROR: Unknown GET URI type");
        }
    }
    
    public EntrySet processPost(Reader r) throws HandlerException {
        if (getUri().isCollection()) {
            return postCollection(r);
        } else {
            throw new BadRequestException("ERROR: Unknown POST URI type");
        }
    }
    
    public EntrySet processPut(Reader r) throws HandlerException {
        if (getUri().isCollection()) {
            return putCollection(r);
        } else if (getUri().isEntry()) {
            return putEntry(r);
        } else {
            throw new BadRequestException("ERROR: Unknown PUT URI type");
        }
    }
    
    public EntrySet processDelete() throws HandlerException {
        if (getUri().isEntry()) {
            return deleteEntry();
        } else {
            throw new BadRequestException("ERROR: Unknown DELETE URI type");
        }
    }
    
    private EntrySet getCollection() throws HandlerException {
        try {
            List users = getRoller().getUserManager().getUsers(0, Integer.MAX_VALUE);
            if (users == null) {
                users = java.util.Collections.EMPTY_LIST;
            }
            EntrySet es = toUserEntrySet((UserData[])users.toArray(new UserData[0]));
            
            return es;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get user collection", re);
        }
    }
    
    private EntrySet getEntry() throws HandlerException {
        try {
            UserData ud = getRoller().getUserManager().getUserByUserName(getUri().getEntryId());
            if (ud == null) {
                throw new NotFoundException("ERROR: Unknown user: " + getUri().getEntryId());
            }
            UserData[] uds = new UserData[] { ud };
            
            EntrySet c = toUserEntrySet(uds);
            return c;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get user collection", re);
        }
    }
    
    private EntrySet postCollection(Reader r) throws HandlerException {
        EntrySet c = getEntrySet(r);
        if (c.isEmpty()) {
            throw new BadRequestException("ERROR: No entries");
        }
        c = createUsers((UserEntrySet)c);
        
        return c;
    }
    
    private EntrySet putCollection(Reader r) throws HandlerException {
        EntrySet c = getEntrySet(r);
        if (c.isEmpty()) {
            throw new BadRequestException("ERROR: No entries");
        }
        c = updateUsers((UserEntrySet)c);
        
        return c;
    }
    
    private EntrySet putEntry(Reader r) throws HandlerException {
        EntrySet c = getEntrySet(r);
        if (c.isEmpty()) {
            throw new BadRequestException("ERROR: No entries");
        }
        if (c.getEntries().length > 1) {
            throw new BadRequestException("ERROR: Cannot put >1 entries per request");
        }
        
        UserEntry entry = (UserEntry)c.getEntries()[0];
        if (entry.getName() != null && !entry.getName().equals(getUri().getEntryId())) {
            throw new BadRequestException("ERROR: Content name does not match URI name");
        }
        entry.setName(getUri().getEntryId());
        c = updateUsers((UserEntrySet)c);
        
        return c;
    }
    
    private UserEntrySet createUsers(UserEntrySet c) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            List userDatas = new ArrayList();
            for (int i = 0; i < c.getEntries().length; i++) {
                UserEntry entry = (UserEntry)c.getEntries()[i];
                if (entry.getDateCreated() == null) {
                    // if no creation date supplied, add it
                    entry.setDateCreated(new Date());
                }
                UserData ud = toUserData(entry);
                mgr.addUser(ud);
                userDatas.add(ud);
            }
            getRoller().flush();
            return toUserEntrySet((UserData[])userDatas.toArray(new UserData[0]));
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not create users: " + c, re);
        }
    }
    
    private UserEntrySet updateUsers(UserEntrySet c) throws NotFoundException, InternalException {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            List userDatas = new ArrayList();
            for (int i = 0; i < c.getEntries().length; i++) {
                UserEntry entry = (UserEntry)c.getEntries()[i];
                UserData ud = mgr.getUserByUserName(entry.getName());
                if (ud == null) {
                    throw new NotFoundException("ERROR: Unknown user: " + entry.getName());
                }
                updateUserData(ud, entry);
                
                mgr.saveUser(ud);
                CacheManager.invalidate(ud);
                userDatas.add(ud);
            }
            getRoller().flush();
            return toUserEntrySet((UserData[])userDatas.toArray(new UserData[0]));
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not update users: " + c, re);
        }
    }
    
    private void updateUserData(UserData ud, UserEntry entry) {
        // user name cannot be updated
        
        if (entry.getFullName() != null) {
            ud.setFullName(entry.getFullName());
        }
        if (entry.getPassword() != null) {
            ud.setPassword(entry.getPassword());
        }
        if (entry.getLocale() != null) {
            ud.setLocale(entry.getLocale().toString());
        }
        if (entry.getTimezone() != null) {
            ud.setTimeZone(entry.getTimezone().getID());
        }
        if (entry.getEmailAddress() != null) {
            ud.setEmailAddress(entry.getEmailAddress());
        }
    }
    
    private EntrySet deleteEntry() throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            UserData ud = mgr.getUserByUserName(getUri().getEntryId());
            
            if (ud == null) {
                throw new NotFoundException("ERROR: Unknown user: " + getUri().getEntryId());
            }
            // don't allow deletion of the currently authenticated user
            if (ud.getUserName().equals(getUserName())) {
                throw new NotAllowedException("ERROR: Can't delete authenticated user: " + getUserName());
            }
            
            UserData[] uds = new UserData[] { ud };
            mgr.removeUser(ud);
            
            CacheManager.invalidate(ud);
            getRoller().flush();
            EntrySet es = toUserEntrySet(uds);
            return es;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not delete entry: " + getUri().getEntryId(), re);
        }
    }
    
    private UserEntry toUserEntry(UserData ud) {
        if (ud == null) {
            throw new NullPointerException("ERROR: Null user data not allowed");
        }
        
        // password field is not set
        // we never return password field
        
        UserEntry ue = new UserEntry(ud.getUserName(), getUrlPrefix());
        ue.setFullName(ud.getFullName());
        ue.setLocale(ud.getLocale());
        ue.setTimezone(ud.getTimeZone());
        ue.setEmailAddress(ud.getEmailAddress());
        ue.setDateCreated(ud.getDateCreated());
        
        return ue;
    }
    
    private UserEntrySet toUserEntrySet(UserData[] uds) {
        if (uds == null) {
            throw new NullPointerException("ERROR: Null user data not allowed");
        }
        UserEntrySet ues = new UserEntrySet(getUrlPrefix());
        
        List entries = new ArrayList();
        for (int i = 0; i < uds.length; i++) {
            UserData ud = uds[i];
            Entry entry = toUserEntry(ud);
            entries.add(entry);
        }
        ues.setEntries((Entry[])entries.toArray(new Entry[0]));
        
        return ues;
    }
    
    /** This object, as a Roller UserData object. */
    public UserData toUserData(UserEntry ue) {
        if (ue == null) {
            throw new NullPointerException("ERROR: Null user entry not allowed");
        }
        
        //
        // if any of the entry fields are null, the set below amounts
        // to a no-op.
        //
        UserData ud = new UserData();
        ud.setUserName(ue.getName());
        ud.setFullName(ue.getFullName());
        ud.setPassword(ue.getPassword());
        ud.setEmailAddress(ue.getEmailAddress());
        ud.setLocale(ue.getLocale().toString());
        ud.setTimeZone(ue.getTimezone().getID());
        ud.setDateCreated(ue.getDateCreated());
        
        return ud;
    }
}

