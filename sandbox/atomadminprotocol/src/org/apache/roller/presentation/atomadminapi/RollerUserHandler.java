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
/*
 * RollerUserHandler.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.apache.roller.presentation.atomadminapi;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.apache.roller.RollerException;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.presentation.atomadminapi.sdk.Entry;
import org.apache.roller.presentation.atomadminapi.sdk.EntrySet;
import org.apache.roller.presentation.atomadminapi.sdk.MissingElementException;
import org.apache.roller.presentation.atomadminapi.sdk.UnexpectedRootElementException;
import org.apache.roller.presentation.atomadminapi.sdk.UserEntry;
import org.apache.roller.presentation.atomadminapi.sdk.UserEntrySet;
import org.apache.roller.presentation.cache.CacheManager;

/**
 * This class handles request concerning Roller users.
 *
 * @author jtb
 */
class RollerUserHandler extends Handler {
    public RollerUserHandler(HttpServletRequest request) throws HandlerException {
        super(request);
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
            List users = getRoller().getUserManager().getUsers();
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
            UserData ud = getRoller().getUserManager().getUserByUsername(getUri().getEntryId());
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
        try {
            SAXBuilder builder = new SAXBuilder();
            Document collectionDoc = builder.build(r);
            EntrySet c = new UserEntrySet(collectionDoc, getUrlPrefix());
            c = createUsers((UserEntrySet)c);
            
            return c;
        } catch (JDOMException je) {
            throw new InternalException("ERROR: Could not post collection", je);
        } catch (IOException ioe) {
            throw new InternalException("ERROR: Could not post collection", ioe);
        } catch (MissingElementException mee) {
            throw new InternalException("ERROR: Could not post collection", mee);
        } catch (UnexpectedRootElementException uree) {
            throw new InternalException("ERROR: Could not post collection", uree);
        }
    }
    
    private EntrySet putCollection(Reader r) throws HandlerException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document collectionDoc = builder.build(r);
            EntrySet c = new UserEntrySet(collectionDoc, getUrlPrefix());
            c = updateUsers((UserEntrySet)c);
            
            return c;
        } catch (JDOMException je) {
            throw new InternalException("ERROR: Could not put collection", je);
        } catch (IOException ioe) {
            throw new InternalException("ERROR: Could not put collection", ioe);
        } catch (MissingElementException mee) {
            throw new InternalException("ERROR: Could not put collection", mee);
        } catch (UnexpectedRootElementException uree) {
            throw new InternalException("ERROR: Could not put collection", uree);
        }
    }
    
    private EntrySet putEntry(Reader r) throws HandlerException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document collectionDoc = builder.build(r);
            EntrySet c = new UserEntrySet(collectionDoc, getUrlPrefix());
            
            if (c.getEntries().length > 1) {
                throw new BadRequestException("ERROR: Cannot put >1 entries per request");
            }
            if (c.getEntries().length > 0) {
                UserEntry entry = (UserEntry)c.getEntries()[0];
                if (entry.getName() != null && !entry.getName().equals(getUri().getEntryId())) {
                    throw new BadRequestException("ERROR: Content name does not match URI name");
                }
                entry.setName(getUri().getEntryId());
                updateUsers((UserEntrySet)c);
            }
            
            return c;
        } catch (JDOMException je) {
            throw new InternalException("ERROR: Could not post collection", je);
        } catch (IOException ioe) {
            throw new InternalException("ERROR: Could not post collection", ioe);
        } catch (MissingElementException mee) {
            throw new InternalException("ERROR: Could not post collection", mee);
        } catch (UnexpectedRootElementException uree) {
            throw new InternalException("ERROR: Could not post collection", uree);
        }
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
                UserData ud = mgr.getUserByUsername(entry.getName());
                if (ud == null) {
                    throw new NotFoundException("ERROR: Uknown user: " + entry.getName());
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
            UserData ud = mgr.getUserByUsername(getUri().getEntryId());
            
            if (ud == null) {
                throw new NotFoundException("ERROR: Uknown user: " + getUri().getEntryId());
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

