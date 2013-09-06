/*
 *  Copyright 2007 Sun Microsystems, Inc.  All rights reserved.
 *  Use is subject to license terms.
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you
 *  may not use this file except in compliance with the License. You may
 *  obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.roller.weblogger.webservices.atomprotocol;

import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import com.sun.syndication.propono.atom.common.rome.AppModule;
import com.sun.syndication.propono.atom.common.rome.AppModuleImpl;
import com.sun.syndication.propono.atom.server.AtomException;
import com.sun.syndication.propono.atom.server.AtomNotAuthorizedException;
import com.sun.syndication.propono.atom.server.AtomNotFoundException;
import com.sun.syndication.propono.atom.server.AtomRequest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogEntryTagComparator;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;


/**
 * Collection of weblog entries.
 * @author davidm.johnson@sun.com
 */
public class EntryCollection {
    private Weblogger      roller;
    private User           user;
    private int            maxEntries = 20;    
    private final String   atomURL;    
    
    private static Log log =
            LogFactory.getFactory().getInstance(EntryCollection.class);
    
    
    public EntryCollection(User user, String atomURL) {
        this.user = user;
        this.atomURL = atomURL;
        this.roller = WebloggerFactory.getWeblogger();
    }
    
    
    public Entry postEntry(AtomRequest areq, Entry entry) throws AtomException {
        log.debug("Entering");
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        try {
            // authenticated client posted a weblog entry
            String handle = pathInfo[0];
            Weblog website = 
                roller.getWeblogManager().getWeblogByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("Cannot find weblog: " + handle);
            }
            if (!RollerAtomHandler.canEdit(user, website)) {
                throw new AtomNotAuthorizedException("Not authorized to access website: " + handle);
            }
            
            RollerAtomHandler.oneSecondThrottle();
            
            // Save it and commit it
            WeblogEntryManager mgr = roller.getWeblogEntryManager();
            WeblogEntry rollerEntry = new WeblogEntry();
            rollerEntry.setWebsite(website);
            rollerEntry.setCreatorUserName(this.user.getUserName());
            rollerEntry.setLocale(website.getLocale());
            copyToRollerEntry(entry, rollerEntry);
            mgr.saveWeblogEntry(rollerEntry);
            roller.flush();

            CacheManager.invalidate(website);
            if (rollerEntry.isPublished()) {
                roller.getIndexManager().addEntryReIndexOperation(rollerEntry);
            }

            rollerEntry = mgr.getWeblogEntry(rollerEntry.getId());
            Entry newEntry = createAtomEntry(rollerEntry);
            for (Iterator it = newEntry.getOtherLinks().iterator(); it.hasNext();) {
                Link link = (Link)it.next();
                if ("edit".equals(link.getRel())) {
                    log.debug("Exiting");
                    return createAtomEntry(rollerEntry);
                }
            }
            log.error("ERROR: no edit link found in saved media entry");
            log.debug("Exiting via exception");

        } catch (WebloggerException re) {
            throw new AtomException("Posting entry", re);
        }
        throw new AtomException("Posting entry");
    }
    
    
    public Entry getEntry(AtomRequest areq) throws AtomException {
        try {
            String entryid = Utilities.stringToStringArray(areq.getPathInfo(),"/")[2];
            WeblogEntry entry = roller.getWeblogEntryManager().getWeblogEntry(entryid);
            if (entry == null) {
                throw new AtomNotFoundException("Cannot find specified entry/resource");
            }
            if (!RollerAtomHandler.canView(user, entry)) {
                throw new AtomNotAuthorizedException("Not authorized to view entry");
            } else {
                return createAtomEntry(entry);
            }
        } catch (WebloggerException ex) {
            throw new AtomException("ERROR fetching entry", ex);
        }
    }
    
    
    public Feed getCollection(AtomRequest areq) throws AtomException {
        log.debug("Entering");
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        try {
            int start = 0;
            int max = maxEntries;
            if (pathInfo.length > 2) {
                try {
                    String s = pathInfo[2].trim();
                    start = Integer.parseInt(s);
                } catch (Exception e) {
                    log.warn("Unparsable range: " + pathInfo[2]);
                }
            }        
            String handle = pathInfo[0];
            String absUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
            Weblog website = roller.getWeblogManager().getWeblogByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("Cannot find specified weblog");
            }
            if (!RollerAtomHandler.canView(user, website)) {
                throw new AtomNotAuthorizedException("Not authorized to access website: " + handle);
            }
            List entries = roller.getWeblogEntryManager().getWeblogEntries( 
                    website,           // website
                    null,              // user
                    null,              // startDate
                    null,              // endDate
                    null,              // catName
                    null,              // tags
                    null,              // status
                    null,              // text
                    "updateTime",      // sortby
                    null,
                    null,              // locale
                    start,             // offset (for range paging)
                    max + 1);          // maxEntries
            Feed feed = new Feed();
            feed.setId(atomURL
                +"/"+website.getHandle() + "/entries/" + start);
            feed.setTitle(website.getName());

            Link link = new Link();
            link.setHref(absUrl + "/" + website.getHandle());
            link.setRel("alternate");
            link.setType("text/html");
            feed.setAlternateLinks(Collections.singletonList(link));

            List atomEntries = new ArrayList();
            int count = 0;
            for (Iterator iter = entries.iterator(); iter.hasNext() && count < maxEntries; count++) {
                WeblogEntry rollerEntry = (WeblogEntry)iter.next();
                Entry entry = createAtomEntry(rollerEntry);
                atomEntries.add(entry);
                if (count == 0) {
                    // first entry is most recent
                    feed.setUpdated(entry.getUpdated());
                }
            }
            List links = new ArrayList();
            if (entries.size() > max) { // add next link
                int nextOffset = start + max;
                String url = atomURL+"/"
                        + website.getHandle() + "/entries/" + nextOffset;
                Link nextLink = new Link();
                nextLink.setRel("next");
                nextLink.setHref(url);
                links.add(nextLink);
            }
            if (start > 0) { // add previous link
                int prevOffset = start > max ? start - max : 0;
                String url = atomURL+"/"
                        +website.getHandle() + "/entries/" + prevOffset;
                Link prevLink = new Link();
                prevLink.setRel("previous");
                prevLink.setHref(url);
                links.add(prevLink);
            }
            if (links.size() > 0) feed.setOtherLinks(links);
            // Use collection URI as id
            feed.setEntries(atomEntries);
            
            log.debug("Exiting");
            return feed;
        
        } catch (WebloggerException re) {
            throw new AtomException("Getting entry collection");
        }
    }
    
    
    public void putEntry(AtomRequest areq, Entry entry) throws AtomException {
        log.debug("Entering");
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        try {
            if (pathInfo.length == 3) // URI is /blogname/entries/entryid
            {
                WeblogEntry rollerEntry =
                    roller.getWeblogEntryManager().getWeblogEntry(pathInfo[2]);
                if (rollerEntry == null) {
                    throw new AtomNotFoundException(
                        "Cannot find specified entry/resource");  
                }
                if (RollerAtomHandler.canEdit(user, rollerEntry)) {
            
                    RollerAtomHandler.oneSecondThrottle();
                    
                    WeblogEntryManager mgr = roller.getWeblogEntryManager();
                    copyToRollerEntry(entry, rollerEntry);
                    rollerEntry.setUpdateTime(new Timestamp(new Date().getTime()));
                    mgr.saveWeblogEntry(rollerEntry);
                    roller.flush();
                    
                    CacheManager.invalidate(rollerEntry.getWebsite());
                    if (rollerEntry.isPublished()) {
                        roller.getIndexManager().addEntryReIndexOperation(rollerEntry);
                    }
                    log.debug("Exiting");
                    return;
                }
                throw new AtomNotAuthorizedException("ERROR not authorized to update entry");
            }
            throw new AtomNotFoundException("Cannot find specified entry/resource");
            
        } catch (WebloggerException re) {
            throw new AtomException("Updating entry");
        }
    }
    
    
    public void deleteEntry(AtomRequest areq) throws AtomException {
        try {
            String[] pathInfo = StringUtils.split(areq.getPathInfo(), "/");
            WeblogEntry rollerEntry = roller.getWeblogEntryManager().getWeblogEntry(pathInfo[2]);
            if (rollerEntry == null) {
                throw new AtomNotFoundException("cannot find specified entry/resource");
            }
            if (RollerAtomHandler.canEdit(user, rollerEntry)) {
                WeblogEntryManager mgr = roller.getWeblogEntryManager();
                CacheManager.invalidate(rollerEntry.getWebsite());
                reindexEntry(rollerEntry);
                mgr.removeWeblogEntry(rollerEntry);
                log.debug("Deleted entry:" + rollerEntry.getAnchor());
                roller.flush();
                return;
            }
            log.debug("Not authorized to delete entry"); 
            log.debug("Exiting via exception"); 
            
        } catch (WebloggerException ex) {
            throw new AtomException("ERROR deleting entry",ex);
        }
        throw new AtomNotAuthorizedException("Not authorized to delete entry");
    }

    
        /**
     * Create a Rome Atom entry based on a Weblogger entry.
     * Content is escaped.
     * Link is stored as rel=alternate link.
     */
    private Entry createAtomEntry(WeblogEntry entry) {
        Entry atomEntry = new Entry();
        
        atomEntry.setId(        entry.getPermalink());
        atomEntry.setTitle(     entry.getTitle());
        atomEntry.setPublished( entry.getPubTime());
        atomEntry.setUpdated(   entry.getUpdateTime());
        
        Content content = new Content();
        content.setType(Content.HTML);
        content.setValue(entry.getText());
        List contents = new ArrayList();
        contents.add(content);
        
        atomEntry.setContents(contents);
        
        if (StringUtils.isNotEmpty(entry.getSummary())) {
            Content summary = new Content();
            summary.setType(Content.HTML);
            summary.setValue(entry.getSummary());
            atomEntry.setSummary(summary);
        }
        
        User creator = entry.getCreator();
        Person author = new Person();
        author.setName(         creator.getUserName());
        author.setEmail(        creator.getEmailAddress());
        atomEntry.setAuthors(   Collections.singletonList(author));
        
        // Add Atom category for Weblogger category, using category scheme
        List categories = new ArrayList();
        Category atomCat = new Category();
        atomCat.setScheme(RollerAtomService.getWeblogCategoryScheme(entry.getWebsite()));
        atomCat.setTerm(entry.getCategory().getPath().substring(1));
        categories.add(atomCat);
        
        // Add Atom categories for each Weblogger tag with null scheme
        Set<WeblogEntryTag> tmp = new TreeSet<WeblogEntryTag>(new WeblogEntryTagComparator());
        tmp.addAll(entry.getTags());
        for (Iterator tagit = tmp.iterator(); tagit.hasNext();) {
            WeblogEntryTag tag = (WeblogEntryTag) tagit.next();
            Category newcat = new Category();
            newcat.setTerm(tag.getName());
            categories.add(newcat);
        }        
        atomEntry.setCategories(categories);
        
        Link altlink = new Link();
        altlink.setRel("alternate");
        altlink.setHref(entry.getPermalink());
        List altlinks = new ArrayList();
        altlinks.add(altlink);
        atomEntry.setAlternateLinks(altlinks);
        
        Link editlink = new Link();
        editlink.setRel("edit");
        editlink.setHref(
                atomURL
                +"/"+entry.getWebsite().getHandle() + "/entry/" + entry.getId());
        List otherlinks = new ArrayList();
        otherlinks.add(editlink);
        atomEntry.setOtherLinks(otherlinks);
        
        List modules = new ArrayList();
        AppModule app = new AppModuleImpl();
        app.setDraft(!WeblogEntry.PUBLISHED.equals(entry.getStatus()));
        app.setEdited(entry.getUpdateTime());
        modules.add(app);
        atomEntry.setModules(modules);
        
        return atomEntry;
    }
    
    /**
     * Copy fields from ROME entry to Weblogger entry.
     */
    private void copyToRollerEntry(Entry entry, WeblogEntry rollerEntry) throws WebloggerException {
        
        Timestamp current = new Timestamp(System.currentTimeMillis());
        Timestamp pubTime = current;
        Timestamp updateTime = current;
        if (entry.getPublished() != null) {
            pubTime = new Timestamp( entry.getPublished().getTime() );
        }
        if (entry.getUpdated() != null) {
            updateTime = new Timestamp( entry.getUpdated().getTime() );
        }
        rollerEntry.setTitle(entry.getTitle());
        if (entry.getContents() != null && entry.getContents().size() > 0) {
            Content content = (Content)entry.getContents().get(0);
            rollerEntry.setText(content.getValue());
        }
        if (entry.getSummary() != null) {
            rollerEntry.setSummary(entry.getSummary().getValue());
        }
        rollerEntry.setPubTime(pubTime);
        rollerEntry.setUpdateTime(updateTime);
        
        AppModule control =
                (AppModule)entry.getModule(AppModule.URI);
        if (control!=null && control.getDraft()) {
            rollerEntry.setStatus(WeblogEntry.DRAFT);
        } else {
            rollerEntry.setStatus(WeblogEntry.PUBLISHED);
        }
                
        // Process incoming categories:
        // Atom categories with weblog-level scheme are Weblogger categories.
        // Atom supports multiple cats, but Weblogger supports one/entry
        // so here we take accept the first category that exists.
        List categories = entry.getCategories();
        if (categories != null && categories.size() > 0) {
            for (int i=0; i<categories.size(); i++) {
                Category cat = (Category)categories.get(i);
                
                if (cat.getScheme() != null && cat.getScheme().equals(
                        RollerAtomService.getWeblogCategoryScheme(rollerEntry.getWebsite()))) {
                    String catString = cat.getTerm();
                    if (catString != null) {
                        WeblogCategory rollerCat =
                                roller.getWeblogEntryManager().getWeblogCategoryByPath(
                                rollerEntry.getWebsite(), catString);
                        if (rollerCat != null) {
                            // Found a valid category, so break out
                            rollerEntry.setCategory(rollerCat);
                            break;
                        }
                    }
                }
            }
        }
        if (rollerEntry.getCategory() == null) {
            // Didn't find a category? Fall back to the default Blogger API category.
            rollerEntry.setCategory(rollerEntry.getWebsite().getBloggerCategory());
        }
        
        // Now process incoming categories that are tags:
        // Atom categories with no scheme are considered tags.
        String tags = "";
        StringBuilder buff = new StringBuilder();
        if (categories != null && categories.size() > 0) {
            for (int i=0; i<categories.size(); i++) {
                Category cat = (Category)categories.get(i);            
                if (cat.getScheme() == null) {
                    buff.append(" ").append(cat.getTerm());
                }                
            }
            tags = buff.toString();
        }
        rollerEntry.setTagsAsString(tags);        
    }

    private void reindexEntry(WeblogEntry entry) throws WebloggerException {
        IndexManager manager = roller.getIndexManager();
        
        // TODO: figure out what's up here and at WeblogEntryFormAction line 696
        //manager.removeEntryIndexOperation(entry);
        
        // if published, index the entry
        if (entry.isPublished()) {
            manager.addEntryReIndexOperation(entry);
        }
    }
}