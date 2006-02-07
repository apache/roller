/*
 * RollerMemberHandler.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.roller.presentation.atomadminapi;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;

/**
 * This class handles requests concerning Roller weblog membership (groups).
 *
 * @author jtb
 */
class RollerMemberHandler extends Handler {
    static class MemberURI extends URI {
        private String username;
        private String handle;
        
        public MemberURI(HttpServletRequest req) {
            super(req);           
            setHandle(getEntryId());
            if (getEntryIds() != null && getEntryIds().length > 1) {
                setUsername(getEntryIds()[1]);
            }
        }
               
        public boolean hasUsername() {
            return getUsername() != null;
        }

        public String getUsername() {
            return username;
        }

        private void setUsername(String username) {
            this.username = username;
        }

        public String getHandle() {
            return handle;
        }

        private void setHandle(String handle) {
            this.handle = handle;
        }
    }    
    
    private URI memberUri;
    
    public RollerMemberHandler(HttpServletRequest request) {
        super(request);
        memberUri = new MemberURI(request);
    }
    
    protected URI getUri() {
        return memberUri;
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
        // get all permissions: for all users, for all websites
        List users = getRoller().getUserManager().getUsers();
        List perms = new ArrayList();
        for (Iterator i = users.iterator(); i.hasNext(); ) {
            UserData user = (UserData)i.next();
            List permissions = getRoller().getUserManager().getAllPermissions(user);
            for (Iterator j = permissions.iterator(); j.hasNext(); ) {
                PermissionsData pd = (PermissionsData)j.next();
                perms.add(pd);
            }
        }
        EntrySet es = new MemberEntrySet((PermissionsData[])perms.toArray(new PermissionsData[0]), getUrlPrefix());
        return es;
    }
    
    private EntrySet getEntry() throws Exception {
        MemberURI muri = (MemberURI)getUri();
        String handle = muri.getHandle();
        String username = muri.getUsername();
               
        List perms;
        if (username == null) {
            //get all entries for the given website handle
            WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(handle);
            if (wd == null) {
                throw new Exception("ERROR: Unknown weblog handle: " + handle);
            }
            perms = getRoller().getUserManager().getAllPermissions(wd);
        } else {
            //get all entries for the given website handle & username
            WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(handle);
            if (wd == null) {
                throw new Exception("ERROR: Unknown weblog handle: " + handle);
            }
            UserData ud = getRoller().getUserManager().getUser(username);
            if (ud == null) {
                throw new Exception("ERROR: Unknown user name: " + username);
            }
            perms = Collections.singletonList(getRoller().getUserManager().getPermissions(wd, ud));
        }
        
        EntrySet es = new MemberEntrySet((PermissionsData[])perms.toArray(new PermissionsData[0]), getUrlPrefix());
        return es;
    }
    
    private EntrySet postCollection(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new MemberEntrySet(collectionDoc, getUrlPrefix());
        createMembers((MemberEntrySet)c);
        
        return c;
    }
    
    private EntrySet putCollection(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new MemberEntrySet(collectionDoc, getUrlPrefix());
        updateMembers((MemberEntrySet)c);
        
        return c;
    }
    
    private EntrySet putEntry(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new MemberEntrySet(collectionDoc, getUrlPrefix());
        
        if (c.getEntries().length > 1) {
            throw new Exception("ERROR: Cannot put >1 entries per request");
        }
        if (c.getEntries().length > 0) {
            // only one entry
            // if there's zero entries, this is a nop
            MemberEntry entry = (MemberEntry)c.getEntries()[0];
            
            MemberURI muri = (MemberURI)getUri();            
            
            // get handle
            // if there's no handle in the entry, set it
            // if the entry and URI handles do not match, exception
            String handle = muri.getHandle();
            if (entry.getHandle() == null) {
                entry.setHandle(handle);
            } else if (!entry.getHandle().equals(handle)) {
                throw new Exception("ERROR: URI and entry handle do not match");
            }
            
            // get username
            // if there's no name in the entry or the URI, exception
            // if there's no name in the entry, set it
            // if the names in the entry and URI do not match, exception
            String username = muri.getUsername();
            if (entry.getName() == null) {
                if (username == null) {
                    throw new Exception("ERROR: No user name in URI or entry");
                }
                entry.setName(username);
            } else if (username != null && !entry.getName().equals(username)) {
                throw new Exception("ERROR: URI and entry user name do not match");
            }
                                    
            updateMembers((MemberEntrySet)c);
        }
        
        return c;
    }
    
    private void createMembers(MemberEntrySet c) throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            // Need system user to create website
            getRoller().setUser(UserData.SYSTEM_USER);
            
            for (int i = 0; i < c.getEntries().length; i++) {
                MemberEntry entry = (MemberEntry)c.getEntries()[i];
                PermissionsData pd = toPermissionsData(entry);
                pd.save();                
            }
            getRoller().commit();
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
    
    private PermissionsData toPermissionsData(MemberEntry entry) throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            UserData ud = mgr.getUser(entry.getName());
            WebsiteData wd = mgr.getWebsiteByHandle(entry.getHandle());
            PermissionsData pd = new PermissionsData();
            pd.setUser(ud);
            pd.setWebsite(wd);
            pd.setPermissionMask(entry.getPermissionMask());
            pd.setPending(false);
            
            return pd;
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
    
    private PermissionsData getPermissionsData(MemberEntry entry) throws Exception {
        return getPermissionsData(entry.getHandle(), entry.getName());
    }
    
    private PermissionsData getPermissionsData(String handle, String username) throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            UserData ud = mgr.getUser(username);
            WebsiteData wd = mgr.getWebsiteByHandle(handle);
            PermissionsData pd = mgr.getPermissions(wd, ud);
            
            return pd;
        } catch (RollerException re) {
            throw new Exception(re);
        }        
    }    
    private void updateMembers(MemberEntrySet c) throws Exception {
        try {
            getRoller().setUser(UserData.SYSTEM_USER);
            for (int i = 0; i < c.getEntries().length; i++) {
                MemberEntry entry = (MemberEntry)c.getEntries()[i];
                PermissionsData pd = getPermissionsData(entry);
                if (pd == null) {
                    throw new Exception("ERROR: Permissions do not exist for weblog handle: " + entry.getHandle() + ", user name: " + entry.getName());
                }
                updatePermissionsData(pd, entry);
                pd.save();
            }
            getRoller().commit();
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
    
    private void updatePermissionsData(PermissionsData pd, MemberEntry entry) {
        // only permission can be updated
        
        if (entry.getPermission() != null) {
            pd.setPermissionMask(entry.getPermissionMask());
        }
    }
    
    private EntrySet deleteEntry() throws Exception {
        MemberURI muri = (MemberURI)getUri();
        
        String handle = muri.getHandle();
        String username = muri.getUsername();
       
        if (username == null) {
            throw new Exception("ERROR: No user name supplied in URI");
        }
        
        try {
            getRoller().setUser(UserData.SYSTEM_USER);
            PermissionsData pd = getPermissionsData(handle, username);
            PermissionsData[] pds = new PermissionsData[] { pd };               
            EntrySet es = new MemberEntrySet(pds, getUrlPrefix());
        
            pd.remove();
            getRoller().commit();

            return es;            
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
}

