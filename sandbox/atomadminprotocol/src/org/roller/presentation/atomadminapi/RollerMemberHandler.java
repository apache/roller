/*
 * RollerMemberHandler.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.roller.presentation.atomadminapi;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.atomadminapi.sdk.Entry;
import org.roller.presentation.atomadminapi.sdk.EntrySet;
import org.roller.presentation.atomadminapi.sdk.MemberEntry;
import org.roller.presentation.atomadminapi.sdk.MemberEntrySet;
import org.roller.presentation.atomadminapi.sdk.MissingElementException;
import org.roller.presentation.atomadminapi.sdk.UnexpectedRootElementException;
import org.roller.presentation.cache.CacheManager;

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
        // get all permissions: for all users, for all websites
        try {
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
            EntrySet es = toMemberEntrySet((PermissionsData[])perms.toArray(new PermissionsData[0]));
            return es;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get member collection", re);
        }
    }
    
    private EntrySet getEntry() throws HandlerException {
        MemberURI muri = (MemberURI)getUri();
        String handle = muri.getHandle();
        String username = muri.getUsername();
        
        try {
            List perms;
            if (username == null) {
                //get all entries for the given website handle
                WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(handle);
                if (wd == null) {
                    throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
                }
                perms = getRoller().getUserManager().getAllPermissions(wd);
            } else {
                //get all entries for the given website handle & username
                WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(handle);
                if (wd == null) {
                    throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
                }
                UserData ud = getRoller().getUserManager().getUser(username);
                if (ud == null) {
                    throw new NotFoundException("ERROR: Unknown user name: " + username);
                }
                PermissionsData pd = getRoller().getUserManager().getPermissions(wd, ud);
                if (pd == null) {
                    throw new NotFoundException("ERROR: Could not get permissions for user name: " + username + ", handle: " + handle);
                }
                perms = Collections.singletonList(pd);
            }
            
            EntrySet es = toMemberEntrySet((PermissionsData[])perms.toArray(new PermissionsData[0]));
            return es;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get entry for handle: " + handle + ", username: " + username, re);
        }
    }
    
    private EntrySet postCollection(Reader r) throws HandlerException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document collectionDoc = builder.build(r);
            EntrySet c = new MemberEntrySet(collectionDoc, getUrlPrefix());
            createMembers((MemberEntrySet)c);
            
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
            EntrySet c = new MemberEntrySet(collectionDoc, getUrlPrefix());
            updateMembers((MemberEntrySet)c);
            
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
    
    private EntrySet putEntry(Reader r) throws HandlerException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document collectionDoc = builder.build(r);
            EntrySet c = new MemberEntrySet(collectionDoc, getUrlPrefix());
            
            if (c.getEntries().length > 1) {
                throw new BadRequestException("ERROR: Cannot put >1 entries per request");
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
                    throw new BadRequestException("ERROR: URI and entry handle do not match");
                }
                
                // get username
                // if there's no name in the entry or the URI, exception
                // if there's no name in the entry, set it
                // if the names in the entry and URI do not match, exception
                String username = muri.getUsername();
                if (entry.getName() == null) {
                    if (username == null) {
                        throw new BadRequestException("ERROR: No user name in URI or entry");
                    }
                    entry.setName(username);
                } else if (username != null && !entry.getName().equals(username)) {
                    throw new BadRequestException("ERROR: URI and entry user name do not match");
                }
                
                updateMembers((MemberEntrySet)c);
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
    
    private void createMembers(MemberEntrySet c) throws HandlerException {
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
            throw new InternalException("ERROR: Could not create members", re);
        }
    }
    
    private PermissionsData toPermissionsData(MemberEntry entry) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            UserData ud = mgr.getUser(entry.getName());
            WebsiteData wd = mgr.getWebsiteByHandle(entry.getHandle());
            PermissionsData pd = new PermissionsData();
            pd.setUser(ud);
            pd.setWebsite(wd);
            pd.setPermissionMask(stringToMask(entry.getPermission()));
            pd.setPending(false);
            
            return pd;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not convert to permissions data", re);
        }
    }
    
    private PermissionsData getPermissionsData(MemberEntry entry) throws HandlerException {
        return getPermissionsData(entry.getHandle(), entry.getName());
    }
    
    private PermissionsData getPermissionsData(String handle, String username) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            UserData ud = mgr.getUser(username);
            WebsiteData wd = mgr.getWebsiteByHandle(handle);
            PermissionsData pd = mgr.getPermissions(wd, ud);
            
            return pd;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get permissions data", re);
        }
    }
    
    private void updateMembers(MemberEntrySet c) throws HandlerException {
        try {
            getRoller().setUser(UserData.SYSTEM_USER);
            for (int i = 0; i < c.getEntries().length; i++) {
                MemberEntry entry = (MemberEntry)c.getEntries()[i];
                PermissionsData pd = getPermissionsData(entry);
                if (pd == null) {
                    throw new NotFoundException("ERROR: Permissions do not exist for weblog handle: " + entry.getHandle() + ", user name: " + entry.getName());
                }
                updatePermissionsData(pd, entry);
            }
            getRoller().commit();
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not update members", re);
        }
    }
    
    private void updatePermissionsData(PermissionsData pd, MemberEntry entry) throws HandlerException {
        // only permission can be updated
        
        if (entry.getPermission() != null) {
            pd.setPermissionMask(stringToMask(entry.getPermission()));
        }
        
        try {
            UserData ud = getRoller().getUserManager().getUser(entry.getName());
            CacheManager.invalidate(ud);
            WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(entry.getHandle());
            CacheManager.invalidate(wd);
            
            pd.save();
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not update permissions data", re);
        }
        
    }
    
    private EntrySet deleteEntry() throws HandlerException {
        MemberURI muri = (MemberURI)getUri();
        
        String handle = muri.getHandle();
        String username = muri.getUsername();
        
        if (username == null) {
            throw new BadRequestException("ERROR: No user name supplied in URI");
        }
        
        try {
            getRoller().setUser(UserData.SYSTEM_USER);
            PermissionsData pd = getPermissionsData(handle, username);
            PermissionsData[] pds;
            if (pd == null) {
                throw new NotFoundException("ERROR: Permissions do not exist for weblog handle: " + handle + ", user name: " + username);
            }
            pds = new PermissionsData[] { pd };
            
            pd.remove();
            getRoller().commit();
            
            UserData ud = getRoller().getUserManager().getUser(username);
            CacheManager.invalidate(ud);
            WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(handle);
            CacheManager.invalidate(wd);
            
            EntrySet es = toMemberEntrySet(pds);
            return es;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not delete entry", re);
        }
    }
    
    private MemberEntry toMemberEntry(PermissionsData pd) {
        if (pd == null) {
            throw new NullPointerException("ERROR: Null permission data not allowed");
        }
        MemberEntry me = new MemberEntry(pd.getWebsite().getHandle(), pd.getUser().getUserName(), getUrlPrefix());
        me.setPermission(maskToString(pd.getPermissionMask()));
        
        return me;
    }
    private MemberEntrySet toMemberEntrySet(PermissionsData[] pds) {
        if (pds == null) {
            throw new NullPointerException("ERROR: Null permission data not allowed");
        }
        
        List entries = new ArrayList();
        for (int i = 0; i < pds.length; i++) {
            PermissionsData pd = pds[i];
            Entry entry = toMemberEntry(pd);
            entries.add(entry);
        }
        MemberEntrySet mes = new MemberEntrySet(getUrlPrefix());
        mes.setEntries((Entry[])entries.toArray(new Entry[0]));
        
        return mes;
    }
    
    private static String maskToString(short mask) {
        if (mask == PermissionsData.ADMIN) {
            return MemberEntry.Permissions.ADMIN;
        }
        if (mask == PermissionsData.AUTHOR) {
            return MemberEntry.Permissions.AUTHOR;
        }
        if (mask == PermissionsData.LIMITED) {
            return MemberEntry.Permissions.LIMITED;
        }
        return null;
    }
    
    
    private static short stringToMask(String s) {
        if (s == null) {
            throw new NullPointerException("ERROR: Null string not allowed");
        }
        if (s.equalsIgnoreCase(MemberEntry.Permissions.ADMIN)) {
            return PermissionsData.ADMIN;
        }
        if (s.equalsIgnoreCase(MemberEntry.Permissions.AUTHOR)) {
            return PermissionsData.AUTHOR;
        }
        if (s.equalsIgnoreCase(MemberEntry.Permissions.LIMITED)) {
            return PermissionsData.LIMITED;
        }
        return 0;
    }
}

