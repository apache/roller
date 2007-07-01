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
package org.apache.roller.weblogger.webservices.atomprotocol;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Collections;
import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.FileNotFoundException;
import org.apache.roller.weblogger.business.FilePathException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.WSSEUtilities;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.activation.FileTypeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.util.URLUtilities;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Weblogger's ROME-based Atom Protocol implementation.
 * 
 * Each Weblogger workspace has two collections, one that accepts entries and 
 * that accepts everything. The entries collection represents the weblog 
 * entries in a single weblog and the everything collection represents that 
 * weblog's uploaded-files. 
 * 
 * Here are the APP URIs suppored by Weblogger:
 * 
 * <pre>
 *    /roller-services/app
 *    Introspection doc
 * 
 *    /roller-services/app/[weblog-handle>/entries
 *    Entry collection for a blog
 * 
 *    /roller-services/app/[weblog-handle]/entries/[offset]
 *    Entry collection for a blog, with offset
 * 
 *    /roller-services/app/[weblog-handle]/entry/[id]
 *    Individual entry (i.e. edit URI)
 * 
 *    /roller-services/app/[weblog-handle]/resources
 *    Resource (i.e. file-uploads) collection for a blog
 * 
 *    /roller-services/app/[weblog-handle]/resources/[offset]
 *    Resource collection for a blog, with offset
 * 
 *    /roller-services/app/[weblog-handle]/resource/*.media-link[name]
 *    Individual resource metadata (i.e. edit URI)
 * 
 *    /roller-services/app/[weblog-handle]/resource/[name]
 *    Individual resource data (i.e. media-edit URI)
 * 
 * </pre>
 * 
 * @author David M Johnson
 */
public class RollerAtomHandler implements AtomHandler {
    private Weblogger roller;
    private User      user;
    private int       maxEntries = 20;
    
    private static boolean throttle = true;
    
    private static Log log =
            LogFactory.getFactory().getInstance(RollerAtomHandler.class);
    
    static {
        throttle = WebloggerConfig.getBooleanProperty("webservices.atomprotocol.oneSecondThrottle");
    }
    
    //---------------------------------------------------------------- construction
    
    /**
     * Create Atom handler for a request and attempt to authenticate user.
     * If user is authenticated, then getAuthenticatedUsername() will return
     * then user's name, otherwise it will return null.
     */
    public RollerAtomHandler(HttpServletRequest request) {
        roller = WebloggerFactory.getWeblogger();
        
        // TODO: decide what to do about authentication, is WSSE going to fly?
        //String userName = authenticateWSSE(request);
        String userName = authenticateBASIC(request);
        if (userName != null) {
            try {
                this.user = roller.getUserManager().getUserByUserName(userName);
            } catch (Exception neverHappen) {
                log.debug("Getting user", neverHappen);
            } 
        }
    }
    
    /**
     * Return weblogHandle of authenticated user or null if there is none.
     */
    public String getAuthenticatedUsername() {
        String ret = null;
        if (this.user != null) {
            ret = user.getUserName();
        }
        return ret;
    }
    
    //---------------------------------------------------------------- introspection
    
    /**
     * Return Atom service document for site, getting blog-name from pathInfo.
     * The workspace will contain collections for entries, categories and resources.
     */
    public AtomService getIntrospection() throws AtomException {
        log.debug("Entering");
        AtomService service = new AtomService();
        List perms = null;
        try {
            perms = roller.getUserManager().getAllPermissions(user);
        } catch (WebloggerException re) {
            throw new AtomException("Getting user's weblogs", re);
        }
        String accept = null;
        try {
            accept = getAcceptedContentTypeRange();
        } catch (WebloggerException re) {
            throw new AtomException("Getting site's accept range", re);
        }
        if (perms != null) {
            for (Iterator iter=perms.iterator(); iter.hasNext();) {
                WeblogPermission perm = (WeblogPermission)iter.next();
                String handle = perm.getWebsite().getHandle();
                
                // Create workspace to represent weblog
                Workspace workspace = new Workspace(
                    Utilities.removeHTML(perm.getWebsite().getName()), "text");
                service.addWorkspace(workspace);
                
                // Create collection for entries within that workspace
                Collection entryCol = new Collection("Weblog Entries", "text", 
                    URLUtilities.getAtomProtocolURL(true)+"/"+handle+"/entries");
                entryCol.setAccept("application/atom+xml;type=entry");
                try {  
                    // Add fixed categories using scheme that points to 
                    // weblog because categories are weblog specific
                    Categories cats = new Categories();
                    cats.setFixed(true);
                    cats.setScheme(getWeblogCategoryScheme(perm.getWebsite()));
                    List rollerCats = roller.getWeblogManager().getWeblogCategories(perm.getWebsite(), false);
                    for (Iterator it = rollerCats.iterator(); it.hasNext();) {
                        WeblogCategory rollerCat = (WeblogCategory)it.next();
                        Category cat = new Category();
                        cat.setTerm(rollerCat.getPath().substring(1));
                        cat.setLabel(rollerCat.getName());
                        cats.addCategory(cat);
                    } 
                    entryCol.addCategories(cats);
                    
                    // Add tags as free-form categories using scheme that points
                    // to site because tags can be considered site-wide
                    Categories tags = new Categories();
                    tags.setFixed(false);
                    entryCol.addCategories(tags);
                    
                } catch (Exception e) {
                    throw new AtomException("Fetching weblog categories");
                }                               
                workspace.addCollection(entryCol);

                // Add media collection for upload dir
                Collection uploadCol = new Collection("Media Files", "text", 
                    URLUtilities.getAtomProtocolURL(true)+"/"+handle+"/resources/");
                uploadCol.setAccept(accept);
                workspace.addCollection(uploadCol);

                // And add one media collection for each of weblog's upload sub-directories
                ThemeResource[] dirs;
                try {
                    dirs = roller.getFileManager().getDirectories(perm.getWebsite());
                    for (int i=0; i<dirs.length; i++) {
                        Collection uploadSubCol = new Collection(
                            "Media Files: " + dirs[i].getPath(), "text",
                            URLUtilities.getAtomProtocolURL(true)+"/"+handle+"/resources/" + dirs[i].getPath());
                        uploadSubCol.setAccept(accept);
                        workspace.addCollection(uploadSubCol);
                    }
                } catch (FilePathException fpe) {
                    throw new AtomException("Getting uploads directories information", fpe);
                } catch (FileNotFoundException fnfe) {
                    throw new AtomException("Getting uploads directories information", fnfe);
                }

            }
        }
        log.debug("Exiting");
        return service;
    }
    
    /**
     * Build accept range by taking things that appear to be content-type rules 
     * from site's file-upload allowed extensions.
     */
    private String getAcceptedContentTypeRange() throws WebloggerException {
        StringBuffer sb = new StringBuffer();
        Weblogger roller = WebloggerFactory.getWeblogger();
        Map config = roller.getPropertiesManager().getProperties();        
        String allows = ((RuntimeConfigProperty)config.get("uploads.types.allowed")).getValue();
        String[] rules = StringUtils.split(StringUtils.deleteWhitespace(allows), ",");
        for (int i=0; i<rules.length; i++) {
            if (rules[i].indexOf("/") == -1) continue;
            if (sb.length() != 0) {
                sb.append(",");
            }
            sb.append(rules[i]);
        }
        return sb.toString();              
    }   
    
    //----------------------------------------------------------------- collections
    
    /**
     * Return collection specified by pathinfo.
     * <pre>
     * Supports these URI forms:
     *    /<blog-name>/entries
     *    /<blog-name>/entries/offset
     *    /<blog-name>/resources
     *    /<blog-name>/resources/offset
     * </pre>
     */
    public Feed getCollection(String[] pathInfo) throws AtomException {
        if (pathInfo.length > 0 && pathInfo[1].equals("entries")) {
            return getCollectionOfEntries(pathInfo);
        } else if (pathInfo.length > 0 && pathInfo[1].equals("resources")) {
            return getCollectionOfResources(pathInfo);
        }
        throw new AtomNotFoundException("Cannot find collection specified");
    }
    
    /**
     * Helper method that returns collection of entries, called by getCollection().
     */
    public Feed getCollectionOfEntries(String[] pathInfo) throws AtomException {
        log.debug("Entering");
        try {
            int start = 0;
            int max = maxEntries;
            if (pathInfo.length > 2) {
                try {
                    String s = pathInfo[2].trim();
                    start = Integer.parseInt(s);
                } catch (Throwable t) {
                    log.warn("Unparsable range: " + pathInfo[2]);
                }
            }        
            String handle = pathInfo[0];
            String absUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
            Weblog website = roller.getUserManager().getWebsiteByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("Cannot find specified weblog");
            }
            if (!canView(website)) {
                throw new AtomNotAuthorizedException("Not authorized to access website: " + handle);
            }
            List entries = entries = roller.getWeblogManager().getWeblogEntries( 
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
            feed.setId(URLUtilities.getAtomProtocolURL(true)
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
                String url = URLUtilities.getAtomProtocolURL(true)+"/"
                        + website.getHandle() + "/entries/" + nextOffset;
                Link nextLink = new Link();
                nextLink.setRel("next");
                nextLink.setHref(url);
                links.add(nextLink);
            }
            if (start > 0) { // add previous link
                int prevOffset = start > max ? start - max : 0;
                String url = URLUtilities.getAtomProtocolURL(true)+"/"
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
    
    /**
     * Helper method that returns collection of resources, called by getCollection().
     *   /handle/resources/offset
     *   /handle/resources/path/offset
     */
    public Feed getCollectionOfResources(String[] rawPathInfo) throws AtomException {
        log.debug("Entering");
        try {
            int start = 0;
            int max = maxEntries;
            String[] pathInfo = rawPathInfo;
            if (rawPathInfo.length > 2) {
                try {
                    start = Integer.parseInt(rawPathInfo[rawPathInfo.length - 1]);
                    pathInfo = new String[rawPathInfo.length - 1];
                    for (int i=0; i<rawPathInfo.length - 1; i++) {
                        pathInfo[i] = rawPathInfo[i];
                    }
                } catch (Exception ingored) {}
            }
            String path = filePathFromPathInfo(pathInfo);
            if (!path.equals("")) path = path + File.separator;
            
            String handle = pathInfo[0];
            String absUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
            Weblog website = roller.getUserManager().getWebsiteByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("Cannot find weblog: " + handle);
            }
            if (!canView(website)) {
                throw new AtomNotAuthorizedException("Not authorized to access website");
            }
                        
            Feed feed = new Feed();
            feed.setId(URLUtilities.getAtomProtocolURL(true)
                +"/"+website.getHandle() + "/resources/" + path + start);                
            feed.setTitle(website.getName());

            Link link = new Link();
            link.setHref(absUrl + "/" + website.getHandle());
            link.setRel("alternate");
            link.setType("text/html");
            feed.setAlternateLinks(Collections.singletonList(link));

            FileManager fmgr = roller.getFileManager();
            ThemeResource[] files = fmgr.getFiles(website, path);

            SortedSet sortedSet = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    ThemeResource f1 = (ThemeResource)o1;
                    ThemeResource f2 = (ThemeResource)o2;
                    if (f1.getLastModified() < f2.getLastModified()) return 1;
                    else if (f1.getLastModified() == f2.getLastModified()) return 0;
                    else return -1;
                }
                public boolean equals(Object obj) {
                    return false;
                }               
            });
                                    
            if (files != null && start < files.length) {  
                for (int j=0; j<files.length; j++) {
                    sortedSet.add(files[j]);
                }
                int count = 0;
                List atomEntries = new ArrayList();
                for (int i=start; i<(start + max) && i<(sortedSet.size()); i++) {
                    Entry entry = createAtomResourceEntry(website, files[i]);
                    atomEntries.add(entry);
                    if (count == 0) {
                        // first entry is most recent
                        feed.setUpdated(entry.getUpdated());
                    }
                    count++;
                }

                List otherLinks = new ArrayList();
                if (start + count < files.length) { // add next link
                    int nextOffset = start + max;
                    String url = URLUtilities.getAtomProtocolURL(true)
                        +"/"+ website.getHandle() + "/resources/" + path + nextOffset;
                    Link nextLink = new Link();
                    nextLink.setRel("next");
                    nextLink.setHref(url);
                    otherLinks.add(nextLink);
                }
                if (start > 0) { // add previous link
                    int prevOffset = start > max ? start - max : 0;
                    String url = URLUtilities.getAtomProtocolURL(true)
                        +"/"+website.getHandle() + "/resources/" + path + prevOffset;
                    Link prevLink = new Link();
                    prevLink.setRel("previous");
                    prevLink.setHref(url);
                    otherLinks.add(prevLink);
                }
                feed.setOtherLinks(otherLinks);
                feed.setEntries(atomEntries);
            }
            
            log.debug("Existing");
            return feed;
       
        } catch (WebloggerException re) {
            throw new AtomException("Getting resource collection");
        }
    }
    
    //--------------------------------------------------------------------- entries
    private void oneSecondThrottle() {
        // Throttle one entry per second per weblog because time-
        // stamp in MySQL and other DBs has only 1 sec resolution
        try { 
            synchronized (getClass()) { 
                Thread.sleep(1000); 
            }  
        } catch (Exception ignored) {} 
    }
    
    /**
     * Create entry in the entry collection (a Weblogger blog has only one).
     */
    public Entry postEntry(String[] pathInfo, Entry entry) throws AtomException {
        log.debug("Entering");
        try {
            // authenticated client posted a weblog entry
            String handle = pathInfo[0];
            Weblog website = 
                roller.getUserManager().getWebsiteByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("Cannot find weblog: " + handle);
            }
            if (!canEdit(website)) {
                throw new AtomNotAuthorizedException("Not authorized to access website: " + handle);
            }
            
            if (throttle) oneSecondThrottle();
            
            // Save it and commit it
            WeblogManager mgr = roller.getWeblogManager();
            WeblogEntry rollerEntry = new WeblogEntry();
            rollerEntry.setWebsite(website);
            rollerEntry.setCreator(this.user);
            copyToRollerEntry(entry, rollerEntry);
            mgr.saveWeblogEntry(rollerEntry);
            roller.flush();

            CacheManager.invalidate(website);
            if (rollerEntry.isPublished()) {
                roller.getIndexManager().addEntryReIndexOperation(rollerEntry);
            }
            
            log.debug("Exiting");
            return createAtomEntry(rollerEntry);

        } catch (WebloggerException re) {
            throw new AtomException("Posting entry", re);
        }
    }
    
    /**
     * Retrieve entry, URI like this /blog-name/entry/id
     */
    public Entry getEntry(String[] pathInfo) throws AtomException {
        log.debug("Entering");
        try {
            if (pathInfo.length > 2) // URI is /blogname/entries/entryid
            {
                if (pathInfo[1].equals("entry")) {
                    WeblogEntry entry = 
                        roller.getWeblogManager().getWeblogEntry(pathInfo[2]);
                    if (entry == null) {
                        throw new AtomNotFoundException("Cannot find specified entry/resource");
                    }
                    if (!canView(entry)) {
                        throw new AtomNotAuthorizedException("Not authorized to view entry");
                    } else {
                        return createAtomEntry(entry);
                    }
                } else if (pathInfo[1].equals("resource") && pathInfo[pathInfo.length - 1].endsWith(".media-link")) {
                    String path = filePathFromPathInfo(pathInfo);                    
                    String fileName = path.substring(0, path.length() - ".media-link".length());
                    String handle = pathInfo[0];
                    Weblog website = 
                        roller.getUserManager().getWebsiteByHandle(handle);                    
                    ThemeResource resource = 
                        roller.getFileManager().getFile(website, fileName);
                    
                    log.debug("Exiting");
                    if (resource != null) return createAtomResourceEntry(website, resource);
                }
            }
            throw new AtomNotFoundException("Cannot find specified entry/resource");
        } catch (WebloggerException re) {
            throw new AtomException("Getting entry");
        }
    }
    
    /**
     * Update entry, URI like this /blog-name/entry/id
     */
    public Entry putEntry(String[] pathInfo, Entry entry) throws AtomException {
        log.debug("Entering");
        try {
            if (pathInfo.length == 3) // URI is /blogname/entries/entryid
            {
                WeblogEntry rollerEntry =
                    roller.getWeblogManager().getWeblogEntry(pathInfo[2]);
                if (rollerEntry == null) {
                    throw new AtomNotFoundException(
                        "Cannot find specified entry/resource");  
                }
                if (canEdit(rollerEntry)) {
            
                    if (throttle) oneSecondThrottle();
                    
                    WeblogManager mgr = roller.getWeblogManager();
                    copyToRollerEntry(entry, rollerEntry);
                    mgr.saveWeblogEntry(rollerEntry);
                    roller.flush();
                    
                    CacheManager.invalidate(rollerEntry.getWebsite());
                    if (rollerEntry.isPublished()) {
                        roller.getIndexManager().addEntryReIndexOperation(rollerEntry);
                    }
                    log.debug("Exiting");
                    return createAtomEntry(rollerEntry);
                }
                throw new AtomNotAuthorizedException("ERROR not authorized to update entry");
            }
            throw new AtomNotFoundException("Cannot find specified entry/resource");
            
        } catch (WebloggerException re) {
            throw new AtomException("Updating entry");
        }
    }
    
    /**
     * Delete entry, URI like this /blog-name/entry/id
     */
    public void deleteEntry(String[] pathInfo) throws AtomException {
        log.debug("Entering");
        try {
            if (pathInfo.length > 2) {
                if (pathInfo[1].equals("entry")) // URI is /blogname/entry/entryid
                {                    
                    WeblogEntry rollerEntry = roller.getWeblogManager().getWeblogEntry(pathInfo[2]);
                    if (rollerEntry == null) {
                        throw new AtomNotFoundException("cannot find specified entry/resource");
                    }
                    if (canEdit(rollerEntry)) {
                        WeblogManager mgr = roller.getWeblogManager();                                                
                        CacheManager.invalidate(rollerEntry.getWebsite());                        
                        reindexEntry(rollerEntry);
                        mgr.removeWeblogEntry(rollerEntry);
                        log.debug("Deleted entry:" + rollerEntry.getAnchor());
                        roller.flush();
                        return;
                    } 
                } else if (pathInfo[1].equals("resource")) {
                    String handle = pathInfo[0];
                    Weblog website = roller.getUserManager().getWebsiteByHandle(handle);
                    if (website == null) {
                        throw new AtomNotFoundException("cannot find specified weblog");
                    }
                    if (canEdit(website) && pathInfo.length > 1) {
                        try {                            
                            String path = filePathFromPathInfo(pathInfo);
                            String fileName = path.substring(0, path.length() - ".media-link".length());
                            FileManager fmgr = roller.getFileManager();
                            fmgr.deleteFile(website, fileName);
                            log.debug("Deleted resource: " + fileName);
                        } catch (Exception e) {
                            String msg = "ERROR in atom.deleteResource";
                            log.error(msg,e);
                            throw new AtomException(msg);
                        }
                        return;
                    }               
                }
                throw new AtomNotAuthorizedException("ERROR not authorized to delete entry");
            }
            throw new AtomNotFoundException("cannot find specified entry/resource");
            
        } catch (WebloggerException re) {
            throw new AtomException("deleting entry");
        }
    }
    
    //-------------------------------------------------------------------- resources
    
    /**
     * Create new resource in generic collection (a Weblogger blog has only one).
     * TODO: can we avoid saving temporary file?
     * TODO: do we need to handle mutli-part MIME uploads?
     * TODO: use Jakarta Commons File-upload?
     */
    public Entry postMedia(String[] pathInfo,
            String title, String slug, String contentType, InputStream is)
            throws AtomException {
        log.debug("Entering");
        try {
            // authenticated client posted a weblog entry
            File tempFile = null;
            String handle = pathInfo[0];
            FileManager fmgr = roller.getFileManager();
            UserManager umgr = roller.getUserManager();
            Weblog website = umgr.getWebsiteByHandle(handle);
            if (!canEdit(website)) {
                throw new AtomNotAuthorizedException("Not authorized to edit weblog: " + handle);
            }
            if (pathInfo.length > 1) {
                // Save to temp file
                String fileName = createFileName(website, 
                    (slug != null) ? slug : Utilities.replaceNonAlphanumeric(title,' '), contentType);
                try {
                    tempFile = File.createTempFile(fileName, "tmp");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    Utilities.copyInputToOutput(is, fos);
                    fos.close();
                                        
                    // Parse pathinfo to determine file path
                    String path = filePathFromPathInfo(pathInfo);
                    
                    if (path.length() > 0) path = path + File.separator;
                    FileInputStream fis = new FileInputStream(tempFile);  
                    fmgr.saveFile(website, path + fileName, contentType, tempFile.length(), fis);
                    fis.close();
                    
                    ThemeResource resource = fmgr.getFile(website, path + fileName);
                    
                    log.debug("Exiting");
                    return createAtomResourceEntry(website, resource);

                } catch (FileIOException fie) {
                    throw new AtomException(
                        "File upload disabled, over-quota or other error", fie);
                } catch (Exception e) {
                    throw new AtomException(
                        "Unexpected error during file upload", e);
                } finally {
                    if (tempFile != null) tempFile.delete();
                }
            }
            throw new AtomException("Incorrect path information");
        
        } catch (WebloggerException re) {
            throw new AtomException("Posting media");
        }
    }
    
    /**
     * Creates a file name for a file based on a weblog, title string and a 
     * content-type. 
     * 
     * @param weblog      Weblog for which file name is being created
     * @param title       Title to be used as basis for file name (or null)
     * @param contentType Content type of file (must not be null)
     * 
     * If a title is specified, the method will apply the same create-anchor 
     * logic we use for weblog entries to create a file name based on the title.
     *
     * If title is null, the base file name will be the weblog handle plus a 
     * YYYYMMDDHHSS timestamp. 
     *
     * The extension will be formed by using the part of content type that
     * comes after he slash. 
     *
     * For example:
     *    weblog.handle = "daveblog"
     *    title         = "Port Antonio"
     *    content-type  = "image/jpg"
     * Would result in port_antonio.jpg
     *
     * Another example:
     *    weblog.handle = "daveblog"
     *    title         = null
     *    content-type  = "image/jpg"
     * Might result in daveblog-200608201034.jpg
     */
    private String createFileName(Weblog weblog, String slug, String contentType) {
        
        if (weblog == null) throw new IllegalArgumentException("weblog cannot be null");
        if (contentType == null) throw new IllegalArgumentException("contentType cannot be null");
        
        String fileName = null;
        
        // Determine the extension based on the contentType. This is a hack.
        // The info we need to map from contentType to file extension is in 
        // JRE/lib/content-type.properties, but Java Activation doesn't provide 
        // a way to do a reverse mapping or to get at the data.
        String[] typeTokens = contentType.split("/");
        String ext = typeTokens[1];
        
        if (slug != null && !slug.trim().equals("")) {              
            // We've got a title, so use it to build file name
            StringTokenizer toker = new StringTokenizer(slug);
            String tmp = null;
            int count = 0;
            while (toker.hasMoreTokens() && count < 5) {
                String s = toker.nextToken();
                s = s.toLowerCase();
                tmp = (tmp == null) ? s : tmp + "_" + s;
                count++;
            }
            fileName = tmp + "." + ext;
            
        } else {            
            // No title or text, so instead we'll use the item's date
            // in YYYYMMDD format to form the file name
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyyMMddHHSS");
            fileName = weblog.getHandle()+"-"+sdf.format(new Date())+"."+ext;
        }
        
        return fileName;
    }
    
    
    /**
     * Update resource specified by pathInfo using data from input stream.
     * Expects pathInfo of form /blog-name/resource/path/name
     */
    public Entry putMedia(String[] pathInfo,
            String contentType, InputStream is) throws AtomException {
        log.debug("Entering");
        if (pathInfo.length > 2) {
            String name = pathInfo[pathInfo.length - 1];
            log.debug("Exiting");
            return postMedia(pathInfo, name, name, contentType, is);
        }
        throw new AtomException("Bad pathInfo");
    }
            
    //------------------------------------------------------------------ URI testers
    
    /**
     * True if URL is the introspection URI.
     */
    public boolean isIntrospectionURI(String[] pathInfo) {
        if (pathInfo.length==0) return true;
        return false;
    }
    
    /**
     * True if URL is a entry URI.
     */
    public boolean isEntryURI(String[] pathInfo) {
        if (pathInfo.length > 2 && pathInfo[1].equals("entry")) return true;
        if (pathInfo.length > 2 && pathInfo[1].equals("resource")) return true;
        return false;
    }
        
    /**
     * True if URL is media edit URI. Media can be udpated, but not metadata.
     */
    public boolean isMediaEditURI(String[] pathInfo) {
        if (pathInfo.length > 1 && pathInfo[1].equals("resource")) return true;
        return false;
    }
        
    /**
     * True if URL is a collection URI of any sort.
     */
    public boolean isCollectionURI(String[] pathInfo) {
        if (pathInfo.length > 1 && pathInfo[1].equals("entries")) return true;
        if (pathInfo.length > 1 && pathInfo[1].equals("resources")) return true;
        if (pathInfo.length > 1 && pathInfo[1].equals("categories")) return true;
        return false;
    }
    
    //------------------------------------------------------------------ permissions
    
    /**
     * Return true if user is allowed to edit an entry.
     */
    private boolean canEdit(WeblogEntry entry) {
        try {
            return entry.hasWritePermissions(this.user);
        } catch (Exception e) {
            log.error("Checking website.canSave()");
        }
        return false;
    }
    
    /**
     * Return true if user is allowed to create/edit weblog entries and file uploads in a website.
     */
    private boolean canEdit(Weblog website) {
        try {
            return website.hasUserPermissions(this.user,WeblogPermission.AUTHOR);
        } catch (Exception e) {
            log.error("Checking website.hasUserPermissions()");
        }
        return false;
    }
    
    /**
     * Return true if user is allowed to view an entry.
     */
    private boolean canView(WeblogEntry entry) {
        return canEdit(entry);
    }
    
    /**
     * Return true if user is allowed to view a website.
     */
    private boolean canView(Weblog website) {
        return canEdit(website);
    }
    
    //-------------------------------------------------------------- authentication
    
    /**
     * Perform WSSE authentication based on information in request.
     * Will not work if Weblogger password encryption is turned on.
     */
    protected String authenticateWSSE(HttpServletRequest request) {
        String wsseHeader = request.getHeader("X-WSSE");
        if (wsseHeader == null) return null;
        
        String ret = null;
        String userName = null;
        String created = null;
        String nonce = null;
        String passwordDigest = null;
        String[] tokens = wsseHeader.split(",");
        for (int i = 0; i < tokens.length; i++) {
            int index = tokens[i].indexOf('=');
            if (index != -1) {
                String key = tokens[i].substring(0, index).trim();
                String value = tokens[i].substring(index + 1).trim();
                value = value.replaceAll("\"", "");
                if (key.startsWith("UsernameToken")) {
                    userName = value;
                } else if (key.equalsIgnoreCase("nonce")) {
                    nonce = value;
                } else if (key.equalsIgnoreCase("passworddigest")) {
                    passwordDigest = value;
                } else if (key.equalsIgnoreCase("created")) {
                    created = value;
                }
            }
        }
        String digest = null;
        try {
            User user = roller.getUserManager().getUserByUserName(userName);
            digest = WSSEUtilities.generateDigest(
                    WSSEUtilities.base64Decode(nonce),
                    created.getBytes("UTF-8"),
                    user.getPassword().getBytes("UTF-8"));
            if (digest.equals(passwordDigest)) {
                ret = userName;
            }
        } catch (Exception e) {
            log.error("During wsseAuthenticataion: " + e.getMessage(), e);
        }
        return ret;
    }
    
    /**
     * BASIC authentication.
     */
    public String authenticateBASIC(HttpServletRequest request) {
        boolean valid = false;
        String userID = null;
        String password = null;
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null) {
                StringTokenizer st = new StringTokenizer(authHeader);
                if (st.hasMoreTokens()) {
                    String basic = st.nextToken();
                    if (basic.equalsIgnoreCase("Basic")) {
                        String credentials = st.nextToken();
                        String userPass = new String(Base64.decodeBase64(credentials.getBytes()));
                        int p = userPass.indexOf(":");
                        if (p != -1) {
                            userID = userPass.substring(0, p);
                            User user = roller.getUserManager().getUserByUserName(userID);
                            boolean enabled = user.getEnabled().booleanValue();
                            if (enabled) {
                                // are passwords encrypted?
                                String encrypted =
                                        WebloggerConfig.getProperty("passwds.encryption.enabled");
                                password = userPass.substring(p+1);
                                if ("true".equalsIgnoreCase(encrypted)) {
                                    password = Utilities.encodePassword(password,
                                            WebloggerConfig.getProperty("passwds.encryption.algorithm"));
                                }
                                valid = user.getPassword().equals(password);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug(e);
        }
        if (valid) return userID;
        return null;
    }
    
    //----------------------------------------------------------- internal utilities
    
    /**
     * Create a Rome Atom entry based on a Weblogger entry.
     * Content is escaped.
     * Link is stored as rel=alternate link.
     */
    private Entry createAtomEntry(WeblogEntry entry) {
        Entry atomEntry = new Entry();
        
        String absUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
        atomEntry.setId(        absUrl + entry.getPermaLink());
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
        atomCat.setScheme(getWeblogCategoryScheme(entry.getWebsite()));
        atomCat.setTerm(entry.getCategory().getPath().substring(1));
        categories.add(atomCat);
        
        // Add Atom categories for each Weblogger tag with null scheme
        for (Iterator tagit = entry.getTags().iterator(); tagit.hasNext();) {
            WeblogEntryTag tag = (WeblogEntryTag) tagit.next();
            Category newcat = new Category();
            newcat.setTerm(tag.getName());
            categories.add(newcat);
        }        
        atomEntry.setCategories(categories);
        
        Link altlink = new Link();
        altlink.setRel("alternate");
        altlink.setHref(absUrl + entry.getPermaLink());
        List altlinks = new ArrayList();
        altlinks.add(altlink);
        atomEntry.setAlternateLinks(altlinks);
        
        Link editlink = new Link();
        editlink.setRel("edit");
        editlink.setHref(
                URLUtilities.getAtomProtocolURL(true)
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
    
    private Entry createAtomResourceEntry(Weblog website, ThemeResource file) {
        String absUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
        String editURI = 
                URLUtilities.getAtomProtocolURL(true)+"/"+website.getHandle()
                + "/resource/" + file.getPath() + ".media-link";
        String editMediaURI = 
                URLUtilities.getAtomProtocolURL(true)+"/"+ website.getHandle()
                + "/resource/" + file.getPath();
        String viewURI = absUrl
                + "/resources/" + website.getHandle()
                + "/" + file.getPath();
        
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
        // TODO: figure out why PNG is missing from Java MIME types
        if (map instanceof MimetypesFileTypeMap) {
            try {
                ((MimetypesFileTypeMap)map).addMimeTypes("image/png png PNG");
            } catch (Exception ignored) {}
        }
        String contentType = map.getContentType(file.getName());
        
        Entry entry = new Entry();
        entry.setId(editMediaURI);
        entry.setTitle(file.getName());
        entry.setUpdated(new Date(file.getLastModified()));
        
        List otherlinks = new ArrayList();        
        entry.setOtherLinks(otherlinks);
        Link editlink = new Link();
            editlink.setRel("edit");
            editlink.setHref(editURI);        
            otherlinks.add(editlink);            
        Link editMedialink = new Link();
            editMedialink.setRel("edit-media");
            editMedialink.setHref(editMediaURI);        
            otherlinks.add(editMedialink);
        
        Content content = new Content();
        content.setSrc(viewURI);
        content.setType(contentType);
        List contents = new ArrayList();
        contents.add(content);
        entry.setContents(contents);
        
        List modules = new ArrayList();
        AppModule app = new AppModuleImpl();
        app.setDraft(false);
        app.setEdited(entry.getUpdated());
        modules.add(app);
        entry.setModules(modules);
        
        return entry;
    }
    
    /**
     * Copy fields from ROME entry to Weblogger entry.
     */
    private void copyToRollerEntry(Entry entry,WeblogEntry rollerEntry) throws WebloggerException {
        
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
                
                if (cat.getScheme() != null && cat.getScheme().equals(getWeblogCategoryScheme(rollerEntry.getWebsite()))) {                
                    String catString = cat.getTerm();
                    if (catString != null) {
                        WeblogCategory rollerCat =
                                roller.getWeblogManager().getWeblogCategoryByPath(
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
        if (categories != null && categories.size() > 0) {
            for (int i=0; i<categories.size(); i++) {
                Category cat = (Category)categories.get(i);            
                if (cat.getScheme() == null) {
                    tags = tags + " " + cat.getTerm();
                }                
            }
        }
        rollerEntry.setTagsAsString(tags);        
    }
        
    private String getWeblogCategoryScheme(Weblog website) {
        return URLUtilities.getWeblogURL(website, null, true);
    }
    
    private String filePathFromPathInfo(String[] pathInfo) {
        String path = "";
        if (pathInfo.length > 2) {
            for (int i = 2; i < pathInfo.length; i++) {
                if (path.length() > 0)
                    path = path + File.separator + pathInfo[i];
                else
                    path = pathInfo[i];
            }
        }
        return path;
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



