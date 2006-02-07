/*
 * RollerWeblogHandler.java
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
 * This class handles requests concernning Roller weblog resources.
 *
 * @author jtb
 */
class RollerWeblogHandler extends Handler {
    /** Theme name used when creating weblogs */
    private static final String DEFAULT_THEME = "basic";
    
    public RollerWeblogHandler(HttpServletRequest request) {
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
        EntrySet c = new WeblogEntrySet((UserData[])users.toArray(new UserData[0]), getUrlPrefix());
        
        return c;
    }
    
    private EntrySet getEntry() throws Exception {
        WebsiteData wd = getRoller().getUserManager().getWebsiteByHandle(getUri().getEntryId());
        if (wd == null) {
            throw new Exception("ERROR: Unknown weblog handle: " + getUri().getEntryId());
        }
        WebsiteData[] wds = new WebsiteData[] { wd };
        EntrySet c = new WeblogEntrySet(wds, getUrlPrefix());
        
        return c;
    }
    
    private EntrySet postCollection(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new WeblogEntrySet(collectionDoc, getUrlPrefix());
        createWeblogs((WeblogEntrySet)c);
        
        return c;
    }
    
    private EntrySet putCollection(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new WeblogEntrySet(collectionDoc, getUrlPrefix());
        updateWeblogs((WeblogEntrySet)c);
        
        return c;
    }
    
    private EntrySet putEntry(Reader r) throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document collectionDoc = builder.build(r);
        EntrySet c = new WeblogEntrySet(collectionDoc, getUrlPrefix());
        
        if (c.getEntries().length > 1) {
            throw new Exception("ERROR: Cannot put >1 entries per request");
        }
        if (c.getEntries().length > 0) {
            WeblogEntry entry = (WeblogEntry)c.getEntries()[0];
            if (entry.getHandle() != null && !entry.getHandle().equals(getUri().getEntryId())) {
                throw new Exception("ERROR: Content handle does not match URI handle");
            }
            entry.setHandle(getUri().getEntryId());
            updateWeblogs((WeblogEntrySet)c);
        }
        
        return c;
    }
    
    private void createWeblogs(WeblogEntrySet c) throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            //TODO: group blogging check?
            
            // Need system user to create website
            getRoller().setUser(UserData.SYSTEM_USER);
            HashMap pages = null; //getRollerContext().readThemeMacros(form.getTheme());
            
            for (int i = 0; i < c.getEntries().length; i++) {
                WeblogEntry entry = (WeblogEntry)c.getEntries()[i];
                UserData user = mgr.getUser(entry.getCreatingUser());
                WebsiteData wd = mgr.createWebsite(user, pages, entry.getHandle(), entry.getName(), entry.getDescription(), entry.getEmailAddress(), DEFAULT_THEME, entry.getLocale(), entry.getTimezone());
                wd.save();                
            }
            getRoller().commit();
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
    
    private void updateWeblogs(WeblogEntrySet c) throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            //TODO: group blogging check?
            
            // Need system user to create website
            getRoller().setUser(UserData.SYSTEM_USER);
            HashMap pages = null;
            
            for (int i = 0; i < c.getEntries().length; i++) {
                WeblogEntry entry = (WeblogEntry)c.getEntries()[i];
                WebsiteData wd = mgr.getWebsiteByHandle(entry.getHandle());
                updateWebsiteData(wd, entry);
                wd.save();
            }
            getRoller().commit();
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
    
    private void updateWebsiteData(WebsiteData wd, WeblogEntry entry) {
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
    }
    
    private EntrySet deleteEntry() throws Exception {
        try {
            UserManager mgr = getRoller().getUserManager();
            
            // Need system user to create website
            getRoller().setUser(UserData.SYSTEM_USER);
            
            WebsiteData wd = mgr.getWebsiteByHandle(getUri().getEntryId());
            if (wd == null) {
                throw new Exception("ERROR: Uknown weblog handle: " + getUri().getEntryId());
            }
            
            WebsiteData[] wds = new WebsiteData[] { wd };            
            EntrySet es = new WeblogEntrySet(wds, getUrlPrefix());            
            
            wd.remove();
            getRoller().commit();
            
            return es;
        } catch (RollerException re) {
            throw new Exception(re);
        }
    }
}

