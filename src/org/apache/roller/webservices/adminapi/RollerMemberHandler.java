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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.apache.roller.RollerException;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.webservices.adminapi.sdk.Entry;
import org.apache.roller.webservices.adminapi.sdk.EntrySet;
import org.apache.roller.webservices.adminapi.sdk.MemberEntry;
import org.apache.roller.webservices.adminapi.sdk.MemberEntrySet;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;

/**
 * This class handles requests concerning Roller weblog membership (groups).
 */
class RollerMemberHandler extends Handler {
    static class MemberURI extends URI {
        private String username;
        private String handle;
        
        public MemberURI(HttpServletRequest req) throws BadRequestException {
            super(req);
            String entryId = getEntryId();
            if (entryId == null) {
                username = null;
                handle = null;
            } else {
                String[] entryIds = entryId.split("/");
                if (entryIds == null || entryIds.length == 0) {
                    throw new BadRequestException("ERROR: Invalid path info: " + req.getPathInfo());
                }
                handle = entryIds[0];
                if (entryIds.length > 1) {
                    username = entryIds[1];
                }
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
    
    public RollerMemberHandler(HttpServletRequest request) throws HandlerException {
        super(request);
        memberUri = new MemberURI(request);
    }
    
    protected EntrySet getEntrySet(Document d) throws UnexpectedRootElementException {
        return new MemberEntrySet(d, getUrlPrefix());
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
            List users = getRoller().getUserManager().getUsers(null, null, null, 0, -1);
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
                WebsiteData wd = getWebsiteData(handle);
                if (wd == null) {
                    throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
                }
                perms = getRoller().getUserManager().getAllPermissions(wd);
            } else {
                //get all entries for the given website handle & username
                WebsiteData wd = getWebsiteData(handle);
                if (wd == null) {
                    throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
                }
                UserData ud = getUserData(username);
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
        EntrySet c = getEntrySet(r);
        if (c.isEmpty()) {
            throw new BadRequestException("ERROR: No entries");
        }
        c = createMembers((MemberEntrySet)c);
        
        return c;
    }
    
    private EntrySet putCollection(Reader r) throws HandlerException {
        EntrySet c = getEntrySet(r);
        if (c.isEmpty()) {
            throw new BadRequestException("ERROR: No entries");
        }
        c = updateMembers((MemberEntrySet)c);
        
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
        
        c = updateMembers((MemberEntrySet)c);
        
        return c;
    }
    
    private MemberEntrySet createMembers(MemberEntrySet c) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            List permissionsDatas= new ArrayList();
            for (int i = 0; i < c.getEntries().length; i++) {
                MemberEntry entry = (MemberEntry)c.getEntries()[i];
                PermissionsData pd = toPermissionsData(entry);
                mgr.savePermissions(pd);
                getRoller().flush();
                CacheManager.invalidate(pd.getUser());
                CacheManager.invalidate(pd.getWebsite());
                permissionsDatas.add(pd);
            }
            return toMemberEntrySet((PermissionsData[])permissionsDatas.toArray(new PermissionsData[0]));
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not create members", re);
        }
    }
    
    private PermissionsData toPermissionsData(MemberEntry entry) throws HandlerException {
        UserData ud = getUserData(entry.getName());
        WebsiteData wd = getWebsiteData(entry.getHandle());
        PermissionsData pd = new PermissionsData();
        pd.setUser(ud);
        pd.setWebsite(wd);
        pd.setPermissionMask(stringToMask(entry.getPermission()));
        pd.setPending(false);
        
        return pd;
    }
    
    private PermissionsData getPermissionsData(MemberEntry entry) throws HandlerException {
        return getPermissionsData(entry.getHandle(), entry.getName());
    }
    
    private PermissionsData getPermissionsData(String handle, String username) throws HandlerException {
        try {
            UserData ud = getUserData(username);
            WebsiteData wd = getWebsiteData(handle);
            PermissionsData pd = getRoller().getUserManager().getPermissions(wd, ud);
            
            return pd;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get permissions data for weblog handle: " + handle + ", user name: " + username, re);
        }
    }
    
    private MemberEntrySet updateMembers(MemberEntrySet c) throws HandlerException {
        List permissionsDatas= new ArrayList();
        for (int i = 0; i < c.getEntries().length; i++) {
            MemberEntry entry = (MemberEntry)c.getEntries()[i];
            PermissionsData pd = getPermissionsData(entry);
            if (pd == null) {
                throw new NotFoundException("ERROR: Permissions do not exist for weblog handle: " + entry.getHandle() + ", user name: " + entry.getName());
            }
            updatePermissionsData(pd, entry);
            permissionsDatas.add(pd);
        }
        return toMemberEntrySet((PermissionsData[])permissionsDatas.toArray(new PermissionsData[0]));
    }
    
    
    private void updatePermissionsData(PermissionsData pd, MemberEntry entry) throws HandlerException {
        // only permission can be updated
        
        if (entry.getPermission() != null) {
            pd.setPermissionMask(stringToMask(entry.getPermission()));
        }
        
        try {
            UserData ud = getUserData(entry.getName());
            WebsiteData wd = getWebsiteData(entry.getHandle());
            
            UserManager mgr = getRoller().getUserManager();
            mgr.savePermissions(pd);
            getRoller().flush();
            CacheManager.invalidate(ud);
            CacheManager.invalidate(wd);
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
            PermissionsData pd = getPermissionsData(handle, username);
            PermissionsData[] pds;
            if (pd == null) {
                throw new NotFoundException("ERROR: Permissions do not exist for weblog handle: " + handle + ", user name: " + username);
            }
            pds = new PermissionsData[] { pd };
            
            UserManager mgr = getRoller().getUserManager();
            mgr.removePermissions(pd);
            getRoller().flush();
            
            UserData ud = getUserData(username);
            CacheManager.invalidate(ud);
            WebsiteData wd = getWebsiteData(handle);
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

