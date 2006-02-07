/*
 * RollerUserHandler.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.roller.presentation.atomadminapi;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.atomadminapi.EntrySet.Types;

/**
 * This class handles request concerning Roller users.
 *
 * @author jtb
 */
class RollerUserHandler extends Handler {
    public RollerUserHandler(HttpServletRequest request) {
        super(request);
    }
    
    public EntrySet processGet() throws Exception {
        if (getUri().isCollection()) {
            return getCollection();
        } else if (getUri().isEntry()) {
            return getEntry();
        } else {
            throw new Exception("ERROR: Unknown GET URI type");
        }
    }
    
    public EntrySet processPost(Reader r) throws Exception {
        if (getUri().isCollection()) {
            return postCollection(r);
        } else {
            throw new Exception("ERROR: Unknown POST URI type");
        }
    }
    
    public EntrySet processPut(Reader r) throws Exception {
        if (getUri().isCollection()) {
            return putCollection(r);
        } else if (getUri().isEntry()) {
            return putEntry(r);
        } else {
            throw new Exception("ERROR: Unknown PUT URI type");
        }
    }
    
    public EntrySet processDelete() throws Exception {
        if (getUri().isEntry()) {
            return deleteEntry();
        } else {
            throw new Exception("ERROR: Unknown DELETE URI type");
        }
    }
    
    private EntrySet getCollection() throws Exception {
        List users = getRoller().getUserManager().getUsers();
        EntrySet es = new UserEntrySet((UserData[])users.toArray(new UserData[0]), getUrlPrefix());
        
        return es;
    }
    
    private EntrySet getEntry() throws Exception {
        UserData ud = getRoller().getUserManager().getUser(getUri().getEntryId());
        if (ud == null) {
            throw new Exception("ERROR: Unknown user name: " + getUri().getEntryId());
        }
        UserData[] uds = new UserData[] { ud };
        EntrySet c = new UserEntrySet(uds, getUrlPrefix());
        
        return c;
    }
    
    private EntrySet postCollection(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new UserEntrySet(collectionDoc, getUrlPrefix());
        createUsers((UserEntrySet)c);
        
        return c;
    }
    
    private EntrySet putCollection(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new UserEntrySet(collectionDoc, getUrlPrefix());
        updateUsers((UserEntrySet)c);
        
        return c;
    }
    
    private EntrySet putEntry(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new UserEntrySet(collectionDoc, getUrlPrefix());
        
        if (c.getEntries().length > 1) {
            throw new Exception("ERROR: Cannot put >1 entries per request");
        }
        if (c.getEntries().length > 0) {
            UserEntry entry = (UserEntry)c.getEntries()[0];
            if (entry.getName() != null && !entry.getName().equals(getUri().getEntryId())) {
                throw new Exception("ERROR: Content name does not match URI name");
            }
            entry.setName(getUri().getEntryId());
            updateUsers((UserEntrySet)c);
        }
        
        return c;
    }
    
    private void createUsers(UserEntrySet c) throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            // Need system user to create website
            getRoller().setUser(UserData.SYSTEM_USER);
            
            for (int i = 0; i < c.getEntries().length; i++) {
                UserEntry entry = (UserEntry)c.getEntries()[i];
                UserData user = entry.toUserData();
                mgr.addUser(user);
            }
            getRoller().commit();
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
    
    private void updateUsers(UserEntrySet c) throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            // Need system user to create user
            getRoller().setUser(UserData.SYSTEM_USER);
            
            for (int i = 0; i < c.getEntries().length; i++) {
                UserEntry entry = (UserEntry)c.getEntries()[i];
                UserData ud = mgr.getUser(entry.getName());
                updateUserData(ud, entry);
                ud.save();
            }
            getRoller().commit();
        } catch (RollerException re) {
            throw new Exception(re);
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
            ud.setLocale(entry.getLocale());
        }
        if (entry.getTimezone() != null) {
            ud.setTimeZone(entry.getTimezone());
        }
        if (entry.getEmailAddress() != null) {
            ud.setEmailAddress(entry.getEmailAddress());
        }
    }
    
    private EntrySet deleteEntry() throws Exception {
        try {
            getRoller().setUser(UserData.SYSTEM_USER);
            UserManager mgr = getRoller().getUserManager();
            UserData ud = mgr.getUser(getUri().getEntryId());
            
            UserData[] uds = new UserData[] { ud };
            EntrySet es = new UserEntrySet(uds, getUrlPrefix());
            
            ud.remove();
            getRoller().commit();
            
            return es;
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
}

