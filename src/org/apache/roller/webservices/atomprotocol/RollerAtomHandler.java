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
package org.apache.roller.webservices.atomprotocol;
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
import org.apache.roller.model.FileManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.util.RollerMessages;
import org.apache.roller.util.Utilities;
import org.apache.roller.util.WSSEUtilities;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.feed.atom.Person;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.activation.FileTypeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.RollerPropertyData;
import org.apache.roller.util.URLUtilities;
import org.apache.roller.util.cache.CacheManager;

/**
 * Roller's Atom Protocol implementation.
 * <pre>
 * Each Roller workspace has two collections, one that accepts entries and 
 * that accepts everything. The entries collection represents the weblog 
 * entries in a single weblog and the everything collection represents that 
 * weblog's uploaded-files. 
 *
 * Here are the APP URIs suppored by Roller:
 *
 *    /roller-services/app
 *    Introspection doc
 *
 *    /roller-services/app/<weblog-handle>/entries
 *    Entry collection for a blog
 *
 *    /roller-services/app/<weblog-handle>/entries/<offset>
 *    Entry collection for a blog, with offset
 *
 *    /roller-services/app/<weblog-handle>/entry/<id>
 *    Individual entry (i.e. edit URI)
 *
 *    /roller-services/app/<weblog-handle>/resources
 *    Resource (i.e. file-uploads) collection for a blog
 *
 *    /roller-services/app/<weblog-handle>/resources/<offset>
 *    Resource collection for a blog, with offset
 *
 *    /roller-services/app/<weblog-handle>/resource/*.media-link<name>
 *    Individual resource metadata (i.e. edit URI)
 *
 *    /roller-services/app/<weblog-handle>/resource/<name>
 *    Individual resource data (i.e. media-edit URI)
 *
 * </pre>
 *
 * @author David M Johnson
 */
public class RollerAtomHandler implements AtomHandler {
    private HttpServletRequest mRequest;
    private Roller             mRoller;
    private RollerContext      mRollerContext;
    private UserData           user;
    private int                mMaxEntries = 20;
    //private MessageDigest    md5Helper = null;
    //private MD5Encoder       md5Encoder = new MD5Encoder();
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(RollerAtomHandler.class);
    
    //---------------------------------------------------------------- construction
    
    /**
     * Create Atom handler for a request and attempt to authenticate user.
     * If user is authenticated, then getAuthenticatedUsername() will return
     * then user's name, otherwise it will return null.
     */
    public RollerAtomHandler(HttpServletRequest request) {
        mRequest = request;
        mRoller = RollerFactory.getRoller();
        mRollerContext = RollerContext.getRollerContext();
        
        // TODO: decide what to do about authentication, is WSSE going to fly?
        //String userName = authenticateWSSE(request);
        String userName = authenticateBASIC(request);
        if (userName != null) {
            try {
                this.user = mRoller.getUserManager().getUserByUserName(userName);
            } catch (Exception neverHappen) {
                mLogger.debug("ERROR: getting user", neverHappen);
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
        AtomService service = new AtomService();
        List perms = null;
        try {
            perms = mRoller.getUserManager().getAllPermissions(user);

        } catch (RollerException re) {
            throw new AtomException("ERROR: getting user's weblogs", re);
        }
        String accept = null;
        try {
            accept = getAcceptedContentTypeRange();
        } catch (RollerException re) {
            throw new AtomException("ERROR: getting site's accept range", re);
        }
        if (perms != null) {
            for (Iterator iter=perms.iterator(); iter.hasNext();) {
                PermissionsData perm = (PermissionsData)iter.next();
                String handle = perm.getWebsite().getHandle();
                AtomService.Workspace workspace = new AtomService.Workspace();
                workspace.setTitle(Utilities.removeHTML(perm.getWebsite().getName()));
                service.addWorkspace(workspace);
                
                AtomService.Collection entryCol = new AtomService.Collection();
                entryCol.setTitle("Weblog Entries");
                entryCol.setAccept("entry");
                entryCol.setHref(URLUtilities.getAtomProtocolURL(true)+"/"+handle+"/entries");
                try {                    
                    AtomService.Categories cats = new AtomService.Categories();
                    cats.setFixed(true);
                    cats.setScheme(URLUtilities.getWeblogURL(perm.getWebsite(), null, true));
                    List rollerCats = mRoller.getWeblogManager().getWeblogCategories(perm.getWebsite(), false);
                    for (Iterator it = rollerCats.iterator(); it.hasNext();) {
                        WeblogCategoryData rollerCat = (WeblogCategoryData)it.next();
                        AtomService.Category cat = new AtomService.Category();
                        cat.setTerm(rollerCat.getPath());
                        cat.setLabel(rollerCat.getName());
                        cats.addCategory(cat);
                    } 
                    entryCol.addCategories(cats);
                } catch (Exception e) {
                    throw new AtomException("ERROR fetching weblog categories");
                }                               
                workspace.addCollection(entryCol);
                                
                AtomService.Collection uploadCol = new AtomService.Collection();
                uploadCol.setTitle("Media Files");
                uploadCol.setAccept(accept);
                uploadCol.setHref(URLUtilities.getAtomProtocolURL(true)+"/"+handle+"/resources");
                workspace.addCollection(uploadCol);
            }
        }
        return service;
    }
    
    /**
     * Build accept range by taking things that appear to be content-type rules 
     * from site's file-upload allowed extensions.
     */
    private String getAcceptedContentTypeRange() throws RollerException {
        StringBuffer sb = new StringBuffer();
        Roller roller = RollerFactory.getRoller();
        Map config = roller.getPropertiesManager().getProperties();        
        String allows = ((RollerPropertyData)config.get("uploads.types.allowed")).getValue();
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
        int start = 0;
        if (pathInfo.length > 2) {
            try {
                String s = pathInfo[2].trim();
                start = Integer.parseInt(s);
            } catch (Throwable t) {
                mLogger.warn("Unparsable range: " + pathInfo[2]);
            }
        }
        if (pathInfo.length > 0 && pathInfo[1].equals("entries")) {
            return getCollectionOfEntries(pathInfo, start, mMaxEntries);
        } else if (pathInfo.length > 0 && pathInfo[1].equals("resources")) {
            return getCollectionOfResources(pathInfo, start, mMaxEntries);
        }
        throw new AtomNotFoundException("ERROR: cannot find collection specified");
    }
    
    /**
     * Helper method that returns collection of entries, called by getCollection().
     */
    public Feed getCollectionOfEntries(
            String[] pathInfo, int start, int max) throws AtomException {
        try {
            String handle = pathInfo[0];
            String absUrl = RollerRuntimeConfig.getAbsoluteContextURL();
            WebsiteData website = 
                mRoller.getUserManager().getWebsiteByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("ERROR: cannot find specified weblog");
            }
            List entries = null;
            if (canView(website)) {
                entries = mRoller.getWeblogManager().getWeblogEntries( 
                        website,           // website
                        null,              // user
                        null,              // startDate
                        null,              // endDate
                        null,              // catName
                        null,              // status
                        "updateTime",      // sortby
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
                for (Iterator iter = entries.iterator(); iter.hasNext() && count < mMaxEntries; count++) {
                    WeblogEntryData rollerEntry = (WeblogEntryData)iter.next();
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
                return feed;
            }
            throw new AtomNotAuthorizedException("ERROR: not authorized to access website");
        
        } catch (RollerException re) {
            throw new AtomException("ERROR: getting entry collection");
        }
    }
    
    /**
     * Helper method that returns collection of resources, called by getCollection().
     */
    public Feed getCollectionOfResources(
            String[] pathInfo, int start, int max) throws AtomException {
        try {
            String handle = pathInfo[0];
            String absUrl = RollerRuntimeConfig.getAbsoluteContextURL();
            WebsiteData website = 
                mRoller.getUserManager().getWebsiteByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException(
                    "ERROR: cannot find specified weblog");
            }
            FileManager fmgr = mRoller.getFileManager();
            File[] files = fmgr.getFiles(website.getHandle());
                        
            if (canView(website)) {
                Feed feed = new Feed();
                feed.setId(URLUtilities.getAtomProtocolURL(true)
                    +"/"+website.getHandle() + "/entries/" + start);                
                feed.setTitle(website.getName());
                
                Link link = new Link();
                link.setHref(absUrl + "/" + website.getHandle());
                link.setRel("alternate");
                link.setType("text/html");
                feed.setAlternateLinks(Collections.singletonList(link));
                
                SortedSet sortedSet = new TreeSet(new Comparator() {
                    public int compare(Object o1, Object o2) {
                        File f1 = (File)o1;
                        File f2 = (File)o2;
                        if (f1.lastModified() < f2.lastModified()) return 1;
                        else if (f1.lastModified() == f2.lastModified()) return 0;
                        else return -1;
                    }
                    public boolean equals(Object obj) {
                        return false;
                    }               
                });
                List atomEntries = new ArrayList();
                if (files != null && start < files.length) {
                    for (int i=0; i<files.length; i++) {
                        sortedSet.add(files[i]);
                    }
                }
                int count = 0;
                File[] sortedArray = (File[])sortedSet.toArray(new File[sortedSet.size()]);
                for (int i=start; i<(start + max) && i<(sortedArray.length); i++) {
                    Entry entry = createAtomResourceEntry(website, sortedArray[i]);
                    atomEntries.add(entry);
                    if (count == 0) {
                        // first entry is most recent
                        feed.setUpdated(entry.getUpdated());
                    }
                    count++;
                }
                if (start + count < files.length) { // add next link
                    int nextOffset = start + max;
                    String url = URLUtilities.getAtomProtocolURL(true)
                        +"/"+ website.getHandle() + "/resources/" + nextOffset;
                    Link nextLink = new Link();
                    nextLink.setRel("next");
                    nextLink.setHref(url);
                    List next = new ArrayList();
                    next.add(nextLink);
                    feed.setOtherLinks(next);
                }
                if (start > 0) { // add previous link
                    int prevOffset = start > max ? start - max : 0;
                    String url = URLUtilities.getAtomProtocolURL(true)
                        +"/"+website.getHandle() + "/resources/" + prevOffset;
                    Link prevLink = new Link();
                    prevLink.setRel("previous");
                    prevLink.setHref(url);
                    List prev = new ArrayList();
                    prev.add(prevLink);
                    feed.setOtherLinks(prev);
                }
                feed.setEntries(atomEntries);
                return feed;
            }
            throw new AtomNotAuthorizedException(
                "ERROR: not authorized to access website");
       
        } catch (RollerException re) {
            throw new AtomException("ERROR: getting resource collection");
        }
    }
    
    //--------------------------------------------------------------------- entries
    
    /**
     * Create entry in the entry collection (a Roller blog has only one).
     */
    public Entry postEntry(String[] pathInfo, Entry entry) throws AtomException {
        try {
            // authenticated client posted a weblog entry
            String handle = pathInfo[0];
            WebsiteData website = 
                mRoller.getUserManager().getWebsiteByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("ERROR: cannot find specified weblog");
            }
            if (canEdit(website)) {
                // Save it and commit it
                WeblogManager mgr = mRoller.getWeblogManager();
                WeblogEntryData rollerEntry = createRollerEntry(website, entry);
                rollerEntry.setCreator(this.user);
                mgr.saveWeblogEntry(rollerEntry);
                mRoller.flush();

                // Throttle one entry per second
                // (MySQL timestamp has 1 sec resolution, damnit)
                try { Thread.sleep(1000); } catch (Exception ignored) {}

                CacheManager.invalidate(website);
                if (rollerEntry.isPublished()) {
                    mRoller.getIndexManager().addEntryReIndexOperation(rollerEntry);
                }
                return createAtomEntry(rollerEntry);
            }
            throw new AtomNotAuthorizedException(
                "ERROR: not authorized to access website");
            
        } catch (RollerException re) {
            throw new AtomException("ERROR: posting entry");
        }
    }
    
    /**
     * Retrieve entry, URI like this /blog-name/entry/id
     */
    public Entry getEntry(String[] pathInfo) throws AtomException {
        try {
            if (pathInfo.length == 3) // URI is /blogname/entries/entryid
            {
                if (pathInfo[1].equals("entry")) {
                    WeblogEntryData entry = 
                        mRoller.getWeblogManager().getWeblogEntry(pathInfo[2]);
                    if (entry == null) {
                        throw new AtomNotFoundException(
                            "ERROR: cannot find specified entry/resource");
                    }
                    if (!canView(entry)) {
                        throw new AtomNotAuthorizedException(
                            "ERROR: not authorized to view entry");
                    } else {
                        return createAtomEntry(entry);
                    }
                } else if (pathInfo[1].equals("resource") && pathInfo[2].endsWith(".media-link")) {
                    String fileName = 
                        pathInfo[2].substring(0, pathInfo[2].length() - ".media-link".length());
                    String handle = pathInfo[0];
                    WebsiteData website = 
                        mRoller.getUserManager().getWebsiteByHandle(handle);
                    String uploadPath = 
                        RollerFactory.getRoller().getFileManager().getUploadUrl();
                    File resource = 
                        new File(uploadPath + File.separator + fileName);
                    return createAtomResourceEntry(website, resource);
                }
            }
            throw new AtomNotFoundException(
                "ERROR: cannot find specified entry/resource");
        } catch (RollerException re) {
            throw new AtomException("ERROR: getting entry");
        }
    }
    
    /**
     * Update entry, URI like this /blog-name/entry/id
     */
    public Entry putEntry(String[] pathInfo, Entry entry) throws AtomException {
        try {
            if (pathInfo.length == 3) // URI is /blogname/entries/entryid
            {
                WeblogEntryData rollerEntry =
                    mRoller.getWeblogManager().getWeblogEntry(pathInfo[2]);
                if (rollerEntry == null) {
                    throw new AtomNotFoundException(
                        "ERROR: cannot find specified entry/resource");  
                }
                if (canEdit(rollerEntry)) {
                    WeblogManager mgr = mRoller.getWeblogManager();

                    WeblogEntryData rawUpdate = createRollerEntry(rollerEntry.getWebsite(), entry);
                    rollerEntry.setPubTime(rawUpdate.getPubTime());
                    rollerEntry.setUpdateTime(rawUpdate.getUpdateTime());
                    rollerEntry.setText(rawUpdate.getText());
                    rollerEntry.setStatus(rawUpdate.getStatus());
                    rollerEntry.setCategory(rawUpdate.getCategory());
                    rollerEntry.setTitle(rawUpdate.getTitle());

                    mgr.saveWeblogEntry(rollerEntry);
                    mRoller.flush();

                    CacheManager.invalidate(rollerEntry.getWebsite());
                    if (rollerEntry.isPublished()) {
                        mRoller.getIndexManager().addEntryReIndexOperation(rollerEntry);
                    }
                    return createAtomEntry(rollerEntry);
                }
                throw new AtomNotAuthorizedException("ERROR not authorized to update entry");
            }
            throw new AtomNotFoundException("ERROR: cannot find specified entry/resource");
            
        } catch (RollerException re) {
            throw new AtomException("ERROR: updating entry");
        }
    }
    
    /**
     * Delete entry, URI like this /blog-name/entry/id
     */
    public void deleteEntry(String[] pathInfo) throws AtomException {
        try {
            if (pathInfo.length == 3) // URI is /blogname/entry/entryid
            {
                if (pathInfo[1].equals("entry")) {
                    WeblogEntryData rollerEntry = mRoller.getWeblogManager().getWeblogEntry(pathInfo[2]);
                    if (rollerEntry == null) {
                        throw new AtomNotFoundException("ERROR: cannot find specified entry/resource");
                    }
                    if (canEdit(rollerEntry)) {
                        WeblogManager mgr = mRoller.getWeblogManager();
                        mgr.removeWeblogEntry(rollerEntry);
                        mRoller.flush();
                        CacheManager.invalidate(rollerEntry.getWebsite());
                        mRoller.getIndexManager().removeEntryIndexOperation(rollerEntry);
                        return;
                    } 
                } else if (pathInfo[1].equals("resource")) {
                    String handle = pathInfo[0];
                    WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
                    if (website == null) {
                        throw new AtomNotFoundException("ERROR: cannot find specified weblog");
                    }
                    if (canEdit(website) && pathInfo.length > 1) {
                        try {                            
                            String fileName = pathInfo[2];
                            if (pathInfo[2].endsWith(".media-link")) {
                                fileName = fileName.substring(0, pathInfo[2].length() - ".media-link".length());
                            }
                            FileManager fmgr = mRoller.getFileManager();
                            fmgr.deleteFile(website.getHandle(), fileName);
                        } catch (Exception e) {
                            String msg = "ERROR in atom.deleteResource";
                            mLogger.error(msg,e);
                            throw new AtomException(msg);
                        }
                        return;
                    }               
                }
                throw new AtomNotAuthorizedException("ERROR not authorized to delete entry");
            }
            throw new AtomNotFoundException("ERROR: cannot find specified entry/resource");
            
        } catch (RollerException re) {
            throw new AtomException("ERROR: deleting entry");
        }
    }
    
    //-------------------------------------------------------------------- resources
    
    /**
     * Create new resource in generic collection (a Roller blog has only one).
     * TODO: can we avoid saving temporary file?
     * TODO: do we need to handle mutli-part MIME uploads?
     * TODO: use Jakarta Commons File-upload?
     */
    public Entry postMedia(String[] pathInfo,
            String title, String slug, String contentType, InputStream is)
            throws AtomException {
        try {
            // authenticated client posted a weblog entry
            File tempFile = null;
            RollerMessages msgs = new RollerMessages();
            String handle = pathInfo[0];
            WebsiteData website =
                mRoller.getUserManager().getWebsiteByHandle(handle);
            if (canEdit(website) && pathInfo.length > 1) {
                // save to temp file
                String fileName = createFileName(website, (slug != null) ? slug : title, contentType);
                try {
                    FileManager fmgr = mRoller.getFileManager();
                    tempFile = File.createTempFile(fileName, "tmp");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    Utilities.copyInputToOutput(is, fos);
                    fos.close();

                    // If save is allowed by Roller system-wide policies
                    if (fmgr.canSave(website.getHandle(), fileName, contentType, tempFile.length(), msgs)) {
                        // Then save the file
                        FileInputStream fis = new FileInputStream(tempFile);
                        fmgr.saveFile(website.getHandle(), fileName, contentType, tempFile.length(), fis);
                        fis.close();

                        File resource = new File(fmgr.getUploadDir() + File.separator + fileName);
                        return createAtomResourceEntry(website, resource);
                    }

                } catch (IOException e) {
                    String msg = "ERROR reading posted file";
                    mLogger.error(msg,e);
                    throw new AtomException(msg, e);
                } finally {
                    if (tempFile != null) tempFile.delete();
                }
            }
            // TODO: AtomUnsupportedMediaType and AtomRequestEntityTooLarge needed?
            throw new AtomException("File upload denied because:" + msgs.toString());
        
        } catch (RollerException re) {
            throw new AtomException("ERROR: posting media");
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
    private String createFileName(WebsiteData weblog, String title, String contentType) {
        
        if (weblog == null) throw new IllegalArgumentException("weblog cannot be null");
        if (contentType == null) throw new IllegalArgumentException("contentType cannot be null");
        
        String fileName = null;
        
        // Determine the extension based on the contentType. This is a hack.
        // The info we need to map from contentType to file extension is in 
        // JRE/lib/content-type.properties, but Java Activation doesn't provide 
        // a way to do a reverse mapping or to get at the data.
        String[] typeTokens = contentType.split("/");
        String ext = typeTokens[1];
        
        if (title != null && !title.trim().equals("")) {              
            // We've got a title, so use it to build file name
            String base = Utilities.replaceNonAlphanumeric(title, ' ');
            StringTokenizer toker = new StringTokenizer(base);
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
     * Expects pathInfo of form /blog-name/resource/name
     */
    public Entry putMedia(String[] pathInfo,
            String contentType, InputStream is) throws AtomException {
        if (pathInfo.length > 2) {
            String name = pathInfo[2];
            return postMedia(pathInfo, name, name, contentType, is);
        }
        throw new AtomException("ERROR: bad pathInfo");
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
     * True if URL is a category URI.
     */
    public boolean isCategoryURI(String[] pathInfo) {
        if (pathInfo.length > 1 && pathInfo[1].equals("category")) return true;
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
    private boolean canEdit(WeblogEntryData entry) {
        try {
            return entry.hasWritePermissions(this.user);
        } catch (Exception e) {
            mLogger.error("ERROR: checking website.canSave()");
        }
        return false;
    }
    
    /**
     * Return true if user is allowed to create/edit weblog entries and file uploads in a website.
     */
    private boolean canEdit(WebsiteData website) {
        try {
            return website.hasUserPermissions(this.user, PermissionsData.AUTHOR);
        } catch (Exception e) {
            mLogger.error("ERROR: checking website.hasUserPermissions()");
        }
        return false;
    }
    
    /**
     * Return true if user is allowed to view an entry.
     */
    private boolean canView(WeblogEntryData entry) {
        return canEdit(entry);
    }
    
    /**
     * Return true if user is allowed to view a website.
     */
    private boolean canView(WebsiteData website) {
        return canEdit(website);
    }
    
    //-------------------------------------------------------------- authentication
    
    /**
     * Perform WSSE authentication based on information in request.
     * Will not work if Roller password encryption is turned on.
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
            UserData user = mRoller.getUserManager().getUserByUserName(userName);
            digest = WSSEUtilities.generateDigest(
                    WSSEUtilities.base64Decode(nonce),
                    created.getBytes("UTF-8"),
                    user.getPassword().getBytes("UTF-8"));
            if (digest.equals(passwordDigest)) {
                ret = userName;
            }
        } catch (Exception e) {
            mLogger.error("ERROR in wsseAuthenticataion: " + e.getMessage(), e);
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
                            UserData user = mRoller.getUserManager().getUserByUserName(userID);
                            boolean enabled = user.getEnabled().booleanValue();
                            if (enabled) {
                                // are passwords encrypted?
                                RollerContext rollerContext =
                                        RollerContext.getRollerContext();
                                String encrypted =
                                        RollerConfig.getProperty("passwds.encryption.enabled");
                                password = userPass.substring(p+1);
                                if ("true".equalsIgnoreCase(encrypted)) {
                                    password = Utilities.encodePassword(password,
                                            RollerConfig.getProperty("passwds.encryption.algorithm"));
                                }
                                valid = user.getPassword().equals(password);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            mLogger.debug(e);
        }
        if (valid) return userID;
        return null;
    }
    
    //----------------------------------------------------------- internal utilities
    
    /**
     * Create a Rome Atom entry based on a Roller entry.
     * Content is escaped.
     * Link is stored as rel=alternate link.
     */
    private Entry createAtomEntry(WeblogEntryData entry) {
        Entry atomEntry = new Entry();
        Content content = new Content();
        content.setType(Content.HTML);
        content.setValue(entry.getText());
        List contents = new ArrayList();
        contents.add(content);
        
        String absUrl = RollerRuntimeConfig.getAbsoluteContextURL();
        atomEntry.setId(        absUrl + entry.getPermaLink());
        atomEntry.setTitle(     entry.getTitle());
        atomEntry.setContents(  contents);
        atomEntry.setPublished( entry.getPubTime());
        atomEntry.setUpdated(   entry.getUpdateTime());
        
        UserData creator = entry.getCreator();
        Person author = new Person();
        author.setName(         creator.getUserName());
        author.setEmail(        creator.getEmailAddress());
        atomEntry.setAuthors(   Collections.singletonList(author));
        
        List categories = new ArrayList();
        Category atomCat = new Category();
        atomCat.setTerm(entry.getCategory().getPath());
        categories.add(atomCat);
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
        PubControlModule pubControl = new PubControlModuleImpl();
        pubControl.setDraft(
                !WeblogEntryData.PUBLISHED.equals(entry.getStatus()));
        modules.add(pubControl);
        atomEntry.setModules(modules);
        
        return atomEntry;
    }
    
    private Entry createAtomResourceEntry(WebsiteData website, File file) {
        String absUrl = RollerRuntimeConfig.getAbsoluteContextURL();
        String editURI = 
                URLUtilities.getAtomProtocolURL(true)+"/"+website.getHandle()
                + "/resource/" + file.getName() + ".media-link";
        String editMediaURI = 
                URLUtilities.getAtomProtocolURL(true)+"/"+ website.getHandle()
                + "/resource/" + file.getName();
        String viewURI = absUrl
                + "/resources/" + website.getHandle()
                + "/" + file.getName();
        
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
        // TODO: figure out why PNG is missing from Java MIME types
        if (map instanceof MimetypesFileTypeMap) {
            try {
                ((MimetypesFileTypeMap)map).addMimeTypes("image/png png PNG");
            } catch (Exception ignored) {}
        }
        String contentType = map.getContentType(file);
        
        Entry entry = new Entry();
        entry.setId(editMediaURI);
        entry.setTitle(file.getName());
        entry.setUpdated(new Date(file.lastModified()));
        
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
        
        return entry;
    }
    
    /**
     * Create a Roller weblog entry based on a Rome Atom entry object
     */
    private WeblogEntryData createRollerEntry(WebsiteData website, Entry entry)
    throws RollerException {
        
        Timestamp current = new Timestamp(System.currentTimeMillis());
        Timestamp pubTime = current;
        Timestamp updateTime = current;
        if (entry.getPublished() != null) {
            pubTime = new Timestamp( entry.getPublished().getTime() );
        }
        if (entry.getUpdated() != null) {
            updateTime = new Timestamp( entry.getUpdated().getTime() );
        }
        WeblogEntryData rollerEntry = new WeblogEntryData();
        rollerEntry.setTitle(entry.getTitle());
        if (entry.getContents() != null && entry.getContents().size() > 0) {
            Content content = (Content)entry.getContents().get(0);
            rollerEntry.setText(content.getValue());
        }
        rollerEntry.setPubTime(pubTime);
        rollerEntry.setUpdateTime(updateTime);
        rollerEntry.setWebsite(website);
        
        PubControlModule control =
                (PubControlModule)entry.getModule("http://purl.org/atom/app#");
        if (control!=null && control.getDraft()) {
            rollerEntry.setStatus(WeblogEntryData.DRAFT);
        } else {
            rollerEntry.setStatus(WeblogEntryData.PUBLISHED);
        }
        
        // Atom supports multiple cats, Roller supports one/entry
        // so here we take accept the first category that exists
        List categories = entry.getCategories();
        if (categories != null && categories.size() > 0) {
            for (int i=0; i<categories.size(); i++) {
                Category cat = (Category)categories.get(i);
                // Caller has no way of knowing our categories, so be lenient here
                String catString = cat.getTerm() != null ? cat.getTerm() : cat.getLabel();
                if (catString != null) {
                    WeblogCategoryData rollerCat =
                            mRoller.getWeblogManager().getWeblogCategoryByPath(
                            website, catString);
                    if (rollerCat != null) {
                        // Found a valid category, so break out
                        rollerEntry.setCategory(rollerCat);
                        break;
                    }
                }
            }
        }
        if (rollerEntry.getCategory() == null) {
            // no category? fall back to the default Blogger API category
            rollerEntry.setCategory(website.getBloggerCategory());
        }
        return rollerEntry;
    }
    
}
