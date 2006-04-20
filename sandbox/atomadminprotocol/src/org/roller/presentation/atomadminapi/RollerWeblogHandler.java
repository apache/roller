/*
 * RollerWeblogHandler.java
 *
 * Created on January 17, 2006, 12:44 PM
 */
package org.roller.presentation.atomadminapi;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.UserManager;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.atomadminapi.sdk.Entry;
import org.roller.presentation.atomadminapi.sdk.EntrySet;
import org.roller.presentation.atomadminapi.sdk.MissingElementException;
import org.roller.presentation.atomadminapi.sdk.UnexpectedRootElementException;
import org.roller.presentation.atomadminapi.sdk.WeblogEntry;
import org.roller.presentation.atomadminapi.sdk.WeblogEntrySet;
import org.roller.presentation.cache.CacheManager;
import org.roller.util.Utilities;

/**
 * This class handles requests concerning Roller weblog resources.
 *
 * @author jtb
 */
class RollerWeblogHandler extends Handler {
    private static Log log =
        LogFactory.getFactory().getInstance(RollerWeblogHandler.class);

    /** Theme name used when creating weblogs */
    private static final String DEFAULT_THEME = "basic";
    
    public RollerWeblogHandler(HttpServletRequest request) {
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
                users = Collections.EMPTY_LIST;
            }
            EntrySet c = toWeblogEntrySet(users);
            
            return c;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get weblog collection", re);
        }
    }
    
    private EntrySet getEntry() throws HandlerException {
        try {
            WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(getUri().getEntryId());
            if (wd == null) {
                throw new NotFoundException("ERROR: Unknown weblog handle: " + getUri().getEntryId());
            }
            WebsiteData[] wds = new WebsiteData[] { wd };
            EntrySet c = toWeblogEntrySet(wds);
            
            return c;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not get weblog collection", re);
        }
    }
    
    private EntrySet postCollection(Reader r) throws HandlerException {
        try {
            SAXBuilder builder = new SAXBuilder();
            Document collectionDoc = builder.build(r);
            EntrySet c = new WeblogEntrySet(collectionDoc, getUrlPrefix());
            createWeblogs((WeblogEntrySet)c);
            
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
            EntrySet c = new WeblogEntrySet(collectionDoc, getUrlPrefix());
            updateWeblogs((WeblogEntrySet)c);
            
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
            EntrySet c = new WeblogEntrySet(collectionDoc, getUrlPrefix());
            
            if (c.getEntries().length > 1) {
                throw new BadRequestException("ERROR: Cannot put >1 entries per request");
            }
            if (c.getEntries().length > 0) {
                WeblogEntry entry = (WeblogEntry)c.getEntries()[0];
                if (entry.getHandle() != null && !entry.getHandle().equals(getUri().getEntryId())) {
                    throw new BadRequestException("ERROR: Content handle does not match URI handle");
                }
                entry.setHandle(getUri().getEntryId());
                updateWeblogs((WeblogEntrySet)c);
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
    
    private void createWeblogs(WeblogEntrySet c) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            //TODO: group blogging check?
            
            HashMap pages = null; //getRollerContext().readThemeMacros(form.getTheme());
            
            for (int i = 0; i < c.getEntries().length; i++) {
                WeblogEntry entry = (WeblogEntry)c.getEntries()[i];
                UserData user = mgr.getUser(entry.getCreatingUser());
                WebsiteData wd = new WebsiteData(
                    entry.getHandle(),
                    user,
                    entry.getName(),
                    entry.getDescription(),
                    entry.getEmailAddress(),
                    entry.getEmailAddress(),
                    DEFAULT_THEME,
                    entry.getLocale(),
                    entry.getTimezone());
                
                try {
                    String def = RollerRuntimeConfig.getProperty("users.editor.pages");
                    String[] defs = Utilities.stringToStringArray(def,",");
                    wd.setEditorPage(defs[0]);
                } catch (Exception ex) {
                    log.error("ERROR setting default editor page for weblog", ex);
                }
                
                mgr.addWebsite(wd);
            }
            getRoller().flush();
            
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not create weblogs: " + c, re);
        }
    }
    
    private void updateWeblogs(WeblogEntrySet c) throws HandlerException {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            //TODO: group blogging check?
            
            HashMap pages = null;
            
            for (int i = 0; i < c.getEntries().length; i++) {
                WeblogEntry entry = (WeblogEntry)c.getEntries()[i];
                WebsiteData wd = mgr.getWebsiteByHandle(entry.getHandle());
                if (wd == null) {
                    throw new NotFoundException("ERROR: Uknown weblog: " + entry.getHandle());
                }
                updateWebsiteData(wd, entry);
            }
            getRoller().flush();
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
            wd.setLocale(entry.getLocale());
        }
        if (entry.getTimezone() != null) {
            wd.setTimeZone(entry.getTimezone());
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
        try {
            UserManager mgr = getRoller().getUserManager();
            
            WebsiteData wd = mgr.getWebsiteByHandle(getUri().getEntryId());
            if (wd == null) {
                throw new NotFoundException("ERROR: Uknown weblog handle: " + getUri().getEntryId());
            }
            
            WebsiteData[] wds = new WebsiteData[] { wd };
            EntrySet es = toWeblogEntrySet(wds);
            
            mgr.removeWebsite(wd);
            
            CacheManager.invalidate(wd);
            
            return es;
        } catch (RollerException re) {
            throw new InternalException("ERROR: Could not delete entry: " + getUri().getEntryId(),re);
        }
    }
    
    private WeblogEntry toWeblogEntry(WebsiteData wd) {
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
        
        return we;
    }
    
    private WeblogEntrySet toWeblogEntrySet(List uds) {
        if (uds == null) {
            throw new NullPointerException("ERROR: Null user data not allowed");
        }
        
        WeblogEntrySet wes = new WeblogEntrySet(getUrlPrefix());
        List entries = new ArrayList();
        for (Iterator i = uds.iterator(); i.hasNext(); ) {
            UserData ud = (UserData)i.next();
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
    
    private WeblogEntrySet toWeblogEntrySet(WebsiteData[] wds) {
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

