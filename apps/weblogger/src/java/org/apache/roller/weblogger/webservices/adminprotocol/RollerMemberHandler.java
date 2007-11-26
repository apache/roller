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
package org.apache.roller.weblogger.webservices.adminprotocol;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.jdom.Document;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.Entry;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.EntrySet;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.MemberEntry;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.MemberEntrySet;
import org.apache.roller.weblogger.webservices.adminprotocol.sdk.UnexpectedRootElementException;

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
            List<WeblogPermission> perms = new ArrayList<WeblogPermission>();
            for (Iterator i = users.iterator(); i.hasNext(); ) {
                User user = (User)i.next();
                List<WeblogPermission> permissions = getRoller().getUserManager().getWeblogPermissions(user);
                for (WeblogPermission perm : permissions) {
                    perms.add(perm);
                }
            }
            EntrySet es = toMemberEntrySet(perms);
            return es;
        } catch (WebloggerException re) {
            throw new InternalException("ERROR: Could not get member collection", re);
        }
    }
    
    private EntrySet getEntry() throws HandlerException {
        MemberURI muri = (MemberURI)getUri();
        String handle = muri.getHandle();
        String username = muri.getUsername();
        
        try {
            List<WeblogPermission> perms;
            if (username == null) {
                //get all entries for the given website handle
                Weblog wd = getWebsiteData(handle);
                if (wd == null) {
                    throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
                }
                perms = getRoller().getUserManager().getWeblogPermissions(wd);
            } else {
                //get all entries for the given website handle & username
                Weblog wd = getWebsiteData(handle);
                if (wd == null) {
                    throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
                }
                User ud = getUserData(username);
                if (ud == null) {
                    throw new NotFoundException("ERROR: Unknown user name: " + username);
                }
                WeblogPermission pd = getRoller().getUserManager().getWeblogPermission(wd, ud);
                if (pd == null) {
                    throw new NotFoundException("ERROR: Could not get permissions for user name: " + username + ", handle: " + handle);
                }
                perms = Collections.singletonList(pd);
            }
            
            EntrySet es = toMemberEntrySet(perms);
            return es;
        } catch (WebloggerException re) {
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
            
            List<WeblogPermission> perms= new ArrayList<WeblogPermission>();
            for (int i = 0; i < c.getEntries().length; i++) {
                MemberEntry entry = (MemberEntry)c.getEntries()[i];
                User ud = getUserData(entry.getName());
                Weblog wd = getWebsiteData(entry.getHandle());
                mgr.grantWeblogPermission(wd, ud, toActionList(entry.getPermission()));
                getRoller().flush();
                CacheManager.invalidate(ud);
                CacheManager.invalidate(wd);
                
                WeblogPermission pd = mgr.getWeblogPermission(wd, ud);
                perms.add(pd);
            }
            return toMemberEntrySet(perms);
            
        } catch (WebloggerException re) {
            throw new InternalException("ERROR: Could not create members", re);
        }
    }
    
    private List<String> toActionList(String perm) throws HandlerException {
        List<String> actions = new ArrayList<String>();
        if ("ADMIN".equals(perm))       actions.add(WeblogPermission.ADMIN);
        else if ("AUTHOR".equals(perm)) actions.add(WeblogPermission.POST);
        else                            actions.add(WeblogPermission.EDIT_DRAFT);
        return actions;       
    }
    
    private WeblogPermission getPermissionsData(MemberEntry entry) throws HandlerException {
        return getPermissionsData(entry.getHandle(), entry.getName());
    }
    
    private WeblogPermission getPermissionsData(String handle, String username) throws HandlerException {
        try {
            User ud = getUserData(username);
            Weblog wd = getWebsiteData(handle);
            WeblogPermission pd = getRoller().getUserManager().getWeblogPermission(wd, ud);            
            return pd;
            
        } catch (WebloggerException re) {
            throw new InternalException("ERROR: Could not get permissions data for weblog handle: " + handle + ", user name: " + username, re);
        }
    }
    
    private MemberEntrySet updateMembers(MemberEntrySet c) throws HandlerException {
        List<WeblogPermission> permissionsDatas= new ArrayList<WeblogPermission>();
        for (int i = 0; i < c.getEntries().length; i++) {
            MemberEntry entry = (MemberEntry)c.getEntries()[i];
            WeblogPermission pd = getPermissionsData(entry);
            if (pd == null) {
                throw new NotFoundException("ERROR: Permissions do not exist for weblog handle: " + entry.getHandle() + ", user name: " + entry.getName());
            }
            updatePermissionsData(pd, entry);
            permissionsDatas.add(pd);
        }
        return toMemberEntrySet(permissionsDatas);
    }
    
    
    private void updatePermissionsData(WeblogPermission pd, MemberEntry entry) throws HandlerException {
        // only permission can be updated
        
        try {
            User ud = getUserData(entry.getName());
            Weblog wd = getWebsiteData(entry.getHandle());
            
            UserManager mgr = getRoller().getUserManager();
            mgr.revokeWeblogPermission(wd, ud, WeblogPermission.ALL_ACTIONS);
            mgr.grantWeblogPermission(wd, ud, stringToActionList(entry.getPermission()));
            
            getRoller().flush();
            
            CacheManager.invalidate(ud);
            CacheManager.invalidate(wd);
        } catch (WebloggerException re) {
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
            Weblog wd = getWebsiteData(handle);
            User ud = getUserData(username);

            UserManager mgr = getRoller().getUserManager();
            mgr.revokeWeblogPermission(wd, ud, WeblogPermission.ALL_ACTIONS);
            
            getRoller().flush();            

            CacheManager.invalidate(ud);
            CacheManager.invalidate(wd);
            
            // return empty set, entry was deleted
            List<WeblogPermission> pds = new ArrayList<WeblogPermission>();
            EntrySet es = toMemberEntrySet(pds);
            return es;
            
        } catch (WebloggerException re) {
            throw new InternalException("ERROR: Could not delete entry", re);
        }
    }
    
    private MemberEntry toMemberEntry(WeblogPermission pd) throws HandlerException  {
        try {
            if (pd == null) {
                throw new NullPointerException("ERROR: Null permission data not allowed");
            }
            MemberEntry me = new MemberEntry(pd.getWeblog().getHandle(), pd.getUser().getUserName(), getUrlPrefix());
            me.setPermission(actionsToString(pd));
            return me;
            
        } catch (WebloggerException ex) {
            throw new InternalException("ERROR: getting user or weblog", ex);
        }
    }
    
    private MemberEntrySet toMemberEntrySet(List<WeblogPermission> pds) throws HandlerException {
        if (pds == null) {
            throw new NullPointerException("ERROR: Null permission data not allowed");
        }
        
        List entries = new ArrayList();
        for (WeblogPermission perm : pds) {
            Entry entry = toMemberEntry(perm);
            entries.add(entry);
        }
        MemberEntrySet mes = new MemberEntrySet(getUrlPrefix());
        mes.setEntries((Entry[])entries.toArray(new Entry[0]));
        
        return mes;
    }
    
    private static String actionsToString(WeblogPermission perm) {
        if (perm.hasAction(WeblogPermission.ADMIN)) {
            return MemberEntry.Permissions.ADMIN;
        }
        if (perm.hasAction(WeblogPermission.POST)) {
            return MemberEntry.Permissions.AUTHOR;
        }
        if (perm.hasAction(WeblogPermission.EDIT_DRAFT)) {
            return MemberEntry.Permissions.LIMITED;
        }
        return null;
    }
    
    
    private static List<String> stringToActionList(String s) {
        List<String> actionList = new ArrayList<String>();
        if (s == null) {
            throw new NullPointerException("ERROR: Null string not allowed");
        }
        if (s.equalsIgnoreCase(MemberEntry.Permissions.ADMIN)) {
            actionList.add(WeblogPermission.ADMIN);
        }
        if (s.equalsIgnoreCase(MemberEntry.Permissions.AUTHOR)) {
            actionList.add(WeblogPermission.POST);
        }
        if (s.equalsIgnoreCase(MemberEntry.Permissions.LIMITED)) {
            actionList.add(WeblogPermission.EDIT_DRAFT);
        }
        return actionList;
    }
}

