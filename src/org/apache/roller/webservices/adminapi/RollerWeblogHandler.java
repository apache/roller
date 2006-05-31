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
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.cache.CacheManager;
import org.apache.roller.util.Utilities;
import org.apache.roller.webservices.adminapi.sdk.Entry;
import org.apache.roller.webservices.adminapi.sdk.EntrySet;
import org.apache.roller.webservices.adminapi.sdk.MissingElementException;
import org.apache.roller.webservices.adminapi.sdk.UnexpectedRootElementException;
import org.apache.roller.webservices.adminapi.sdk.WeblogEntry;
import org.apache.roller.webservices.adminapi.sdk.WeblogEntrySet;

/**
 * This class handles requests concerning Roller weblog resources.
 */
class RollerWeblogHandler extends Handler {
    private static Log log =
            LogFactory.getFactory().getInstance(RollerWeblogHandler.class);
    
    /** Theme name used when creating weblogs */
    private static final String DEFAULT_THEME = "basic";
    
    public RollerWeblogHandler(HttpServletRequest request) throws HandlerException {
        super(request);
    }
    
    protected EntrySet getEntrySet(Document d) throws MissingElementException, UnexpectedRootElementException {
        return new WeblogEntrySet(d, getUrlPrefix());
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
                users = Collections.EMPTY_LIST;
            }
            EntrySet c = toWeblogEntrySet((UserData[])users.toArray(new UserData[0]));
            
            return c;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get weblog collection", re);
        }
    }
    
    private EntrySet getEntry() throws HandlerException {
        String handle = getUri().getEntryId();
        try {
            WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(handle);
            if (wd == null) {
                throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
            }
            WebsiteData[] wds = new WebsiteData[] { wd };
            EntrySet c = toWeblogEntrySet(wds);
            
            return c;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get weblog collection", re);
        }
    }
    
    private EntrySet postCollection(Reader r) throws HandlerException {
        EntrySet c = getEntrySet(r);
        if (c.isEmpty()) {
            throw new BadRequestException("ERROR: No entries");
        }
        c = createWeblogs((WeblogEntrySet)c);
        
        return c;
    }
    
    private EntrySet putCollection(Reader r) throws HandlerException {
        EntrySet c = getEntrySet(r);
        if (c.isEmpty()) {
            throw new BadRequestException("ERROR: No entries");
        }
        c = updateWeblogs((WeblogEntrySet)c);
        
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
        
        WeblogEntry entry = (WeblogEntry)c.getEntries()[0];
        if (entry.getHandle() != null && !entry.getHandle().equals(getUri().getEntryId())) {
            throw new BadRequestException("ERROR: Content handle does not match URI handle");
        }
        entry.setHandle(getUri().getEntryId());
        c = updateWeblogs((WeblogEntrySet)c);
        
        return c;
    }
    
    private WeblogEntrySet createWeblogs(WeblogEntrySet c) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            HashMap pages = null; //getRollerContext().readThemeMacros(form.getTheme());
            
            List websiteDatas = new ArrayList();
            for (int i = 0; i < c.getEntries().length; i++) {
                WeblogEntry entry = (WeblogEntry)c.getEntries()[i];
                UserData user = mgr.getUserByUsername(entry.getCreatingUser());
                WebsiteData wd = new WebsiteData(
                        entry.getHandle(),
                        user,
                        entry.getName(),
                        entry.getDescription(),
                        entry.getEmailAddress(),
                        entry.getEmailAddress(),
                        DEFAULT_THEME,
                        entry.getLocale().toString(),
                        entry.getTimezone().getID());
                
                Date dateCreated  = entry.getDateCreated();
                if (dateCreated == null) {
                    dateCreated = new Date();
                }
                wd.setDateCreated(dateCreated);
                
                try {
                    String def = RollerRuntimeConfig.getProperty("users.editor.pages");
                    String[] defs = Utilities.stringToStringArray(def,",");
                    wd.setEditorPage(defs[0]);
                } catch (Exception ex) {
                    log.error("ERROR setting default editor page for weblog", ex);
                }
                
                mgr.addWebsite(wd);
                websiteDatas.add(wd);
            }
            
            getRoller().flush();
            return toWeblogEntrySet((WebsiteData[])websiteDatas.toArray(new WebsiteData[0]));
            
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not create weblogs: " + c, re);
        }
    }
    
    private WeblogEntrySet updateWeblogs(WeblogEntrySet c) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            //TODO: group blogging check?
            
            HashMap pages = null;
            
            List websiteDatas = new ArrayList();
            for (int i = 0; i < c.getEntries().length; i++) {
                WeblogEntry entry = (WeblogEntry)c.getEntries()[i];
                WebsiteData wd = mgr.getWebsiteByHandle(entry.getHandle());
                if (wd == null) {
                    throw new NotFoundException("ERROR: Unknown weblog: " + entry.getHandle());
                }
                updateWebsiteData(wd, entry);
                websiteDatas.add(wd);
            }
            getRoller().flush();
            return toWeblogEntrySet((WebsiteData[])websiteDatas.toArray(new WebsiteData[0]));
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not update weblogs: " + c, re);
        }
    }
    
    private void updateWebsiteData(WebsiteData wd, WeblogEntry entry) throws HandlerException {
        if (entry.getName() != null) {
            wd.setName(entry.getName());
        }
        if (entry.getDescription() != null) {
            wd.setDescription(entry.getDescription());
        }
        if (entry.getLocale() != null) {
            wd.setLocale(entry.getLocale().toString());
        }
        if (entry.getTimezone() != null) {
            wd.setTimeZone(entry.getTimezone().getID());
        }
        if (entry.getEmailAddress() != null) {
            wd.setEmailAddress(entry.getEmailAddress());
        }
        
        try {
            UserManager mgr = getRoller().getUserManager();
            mgr.saveWebsite(wd);
            
            CacheManager.invalidate(wd);
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not update website data", re);
        }
    }
    
    private EntrySet deleteEntry() throws HandlerException {
        String handle = getUri().getEntryId();
        
        try {
            UserManager mgr = getRoller().getUserManager();
            
            WebsiteData wd = mgr.getWebsiteByHandle(handle);
            if (wd == null) {
                throw new NotFoundException("ERROR: Unknown weblog handle: " + handle);
            }
            
            WebsiteData[] wds = new WebsiteData[] { wd };
            EntrySet es = toWeblogEntrySet(wds);
            
            mgr.removeWebsite(wd);
            
            CacheManager.invalidate(wd);
            getRoller().flush();
            
            return es;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not delete entry: " + handle, re);
        }
    }
    
    private WeblogEntry toWeblogEntry(WebsiteData wd) throws HandlerException {
        if (wd == null) {
            throw new NullPointerException("ERROR: Null website data not allowed");
        }
        WeblogEntry we = new WeblogEntry(wd.getHandle(), getUrlPrefix());
        we.setName(wd.getName());
        we.setDescription(wd.getDescription());
        we.setLocale(wd.getLocale());
        we.setTimezone(wd.getTimeZone());
        we.setCreatingUser(wd.getCreator().getUserName());
        we.setEmailAddress(wd.getEmailAddress());
        we.setDateCreated(wd.getDateCreated());
        try {
            AppUrl appUrl = new AppUrl(getRollerContext().getAbsoluteContextUrl(getRequest()), wd.getHandle());
            we.setAppEntriesUrl(appUrl.getEntryUrl().toString());
            we.setAppResourcesUrl(appUrl.getResourceUrl().toString());
        } catch (MalformedURLException mfue) {
            throw new InternalException("ERROR: Could not get APP URLs", mfue);
        }
        
        return we;
    }
    
    private WeblogEntrySet toWeblogEntrySet(UserData[] uds) throws HandlerException {
        if (uds == null) {
            throw new NullPointerException("ERROR: Null user data not allowed");
        }
        
        WeblogEntrySet wes = new WeblogEntrySet(getUrlPrefix());
        List entries = new ArrayList();
        for (int i = 0; i < uds.length; i++) {
            UserData ud = uds[i];
            List permissions = ud.getPermissions();
            for (Iterator j = permissions.iterator(); j.hasNext(); ) {
                PermissionsData pd = (PermissionsData)j.next();
                WebsiteData wd = pd.getWebsite();
                WeblogEntry we = toWeblogEntry(wd);
                entries.add(we);
            }
        }
        wes.setEntries((Entry[])entries.toArray(new Entry[0]));
        
        return wes;
    }
    
    private WeblogEntrySet toWeblogEntrySet(WebsiteData[] wds) throws HandlerException {
        if (wds == null) {
            throw new NullPointerException("ERROR: Null website datas not allowed");
        }
        
        WeblogEntrySet wes = new WeblogEntrySet(getUrlPrefix());
        List entries = new ArrayList();
        for (int i = 0; i < wds.length; i++) {
            WeblogEntry we = toWeblogEntry(wds[i]);
            entries.add(we);
        }
        wes.setEntries((Entry[])entries.toArray(new Entry[0]));
        
        return wes;
    }
}

