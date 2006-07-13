/*
 * Copyright 2005 David M Johnson (For RSS and Atom In Action)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import javax.activation.FileTypeMap;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.util.cache.CacheManager;

/**
 * Roller's Atom Protocol implementation.
 * <pre>
 * Here are the URIs suppored:
 *
 *    URI type             URI form                        Handled by
 *    --------             --------                        ----------
 *    Introspection URI    /                               getIntrosection()
 *    Collection URI       /blog-name/<collection-name>    getCollection()
 *    Member URI           /blog-name/<object-name>        post<object-name>()
 *    Member URI           /blog-name/<object-name>/id     get<object-name>()
 *    Member URI           /blog-name/<object-name>/id     put<object-name>()
 *    Member URI           /blog-name/<object-name>/id     delete<object-name>()
 *
 *    Until group blogging is supported weblogHandle == blogname.
 *
 *    Collection-names   Object-names
 *    ----------------   ------------
 *       entries           entry
 *       resources         resource
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
        //mUsername = authenticateWSSE(request);
        String mUsername = authenticateBASIC(request);
        
        if (mUsername != null) {
            try {
                this.user = mRoller.getUserManager().getUserByUserName(mUsername);
            } catch (Exception e) {
                mLogger.error("ERROR: setting user", e);
            }
        }
    }
    
    /**
     * Return weblogHandle of authenticated user or null if there is none.
     */
    public String getAuthenticatedUsername() {
        return this.user.getUserName();
    }
    
    //---------------------------------------------------------------- introspection
    
    /**
     * Return Atom service document for site, getting blog-name from pathInfo.
     * The workspace will contain collections for entries, categories and resources.
     */
    public AtomService getIntrospection(String[] pathInfo) throws Exception {
        if (pathInfo.length == 0) {
            String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
            AtomService service = new AtomService();
            List perms = mRoller.getUserManager().getAllPermissions(user);
            if (perms != null) {
                for (Iterator iter=perms.iterator(); iter.hasNext();) {
                    PermissionsData perm = (PermissionsData)iter.next();
                    String handle = perm.getWebsite().getHandle();
                    AtomService.Workspace workspace = new AtomService.Workspace();
                    workspace.setTitle(Utilities.removeHTML(perm.getWebsite().getName()));
                    service.addWorkspace(workspace);
                    
                    AtomService.Collection entryCol = new AtomService.Collection();
                    entryCol.setTitle("Weblog Entries");
                    entryCol.setMemberType("entry");
                    entryCol.setHref(absUrl + "/app/"+handle+"/entries");
                    workspace.addCollection(entryCol);
                                        
                    AtomService.Collection uploadCol = new AtomService.Collection();
                    uploadCol.setTitle("Media Files");
                    uploadCol.setMemberType("media");
                    uploadCol.setHref(absUrl + "/app/"+handle+"/resources");
                    workspace.addCollection(uploadCol);
                }
            }
            return service;
        }
        throw new Exception("ERROR: bad URL in getIntrospection()");
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
    public Feed getCollection(String[] pathInfo) throws Exception {
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
        throw new Exception("ERROR: bad URL in getCollection()");
    }
    
    /**
     * Helper method that returns collection of entries, called by getCollection().
     */
    public Feed getCollectionOfEntries(
            String[] pathInfo, int start, int max) throws Exception {
        String handle = pathInfo[0];
        String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        List entries = null;
        if (canView(website)) {
            entries = mRoller.getWeblogManager().getWeblogEntries(
                    website,           // website
                    null,
                    null,              // startDate
                    null,              // endDate
                    null,              // catName
                    null,              // status
                    "updateTime",      // sortby
                    start,             // offset (for range paging)
                    max + 1,           // maxEntries
                    null);             // locale
            Feed feed = new Feed();
            feed.setTitle("Entries for blog[" + handle + "]");
            List atomEntries = new ArrayList();
            int count = 0;
            for (Iterator iter = entries.iterator(); iter.hasNext() && count < mMaxEntries; count++) {
                WeblogEntryData rollerEntry = (WeblogEntryData)iter.next();
                atomEntries.add(createAtomEntry(rollerEntry));
            }
            List links = new ArrayList();
            if (entries.size() > max) { // add next link
                int nextOffset = start + max; 
                String url = absUrl + "/app/" + website.getHandle() + "/entries/" + nextOffset;
                Link nextLink = new Link();
                nextLink.setRel("next");
                nextLink.setHref(url);
                links.add(nextLink);
            }
            if (start > 0) { // add previous link
                int prevOffset = start > max ? start - max : 0;
                String url = absUrl + "/app/" +website.getHandle() + "/entries/" + prevOffset;
                Link prevLink = new Link();
                prevLink.setRel("previous");
                prevLink.setHref(url);
                links.add(prevLink);
            }
            if (links.size() > 0) feed.setOtherLinks(links);
            feed.setEntries(atomEntries);
            return feed;
        }
        throw new Exception("ERROR: not authorized");
    }
    
    /**
     * Helper method that returns collection of resources, called by getCollection().
     */
    public Feed getCollectionOfResources(
            String[] pathInfo, int start, int max) throws Exception {
        String handle = pathInfo[0];
        String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        FileManager fmgr = mRoller.getFileManager();
        File[] files = fmgr.getFiles(website.getHandle());
        if (canView(website)) {            
            Feed feed = new Feed();
            List atomEntries = new ArrayList();
            int count = 0;
            if (files != null && start < files.length) {
                for (int i=start; i<(start + max) && i<(files.length); i++) {                   
                    Entry entry = createAtomResourceEntry(website, files[i]);
                    atomEntries.add(entry);
                    count++;
                }
            }
            if (start + count < files.length) { // add next link
                int nextOffset = start + max; 
                String url = absUrl + "/app/" + website.getHandle() + "/resources/" + nextOffset;
                Link nextLink = new Link();
                nextLink.setRel("next");
                nextLink.setHref(url);
                List next = new ArrayList();
                next.add(nextLink);
                feed.setOtherLinks(next);
            }
            if (start > 0) { // add previous link
                int prevOffset = start > max ? start - max : 0;
                String url = absUrl + "/app/" +website.getHandle() + "/resources/" + prevOffset;
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
        throw new Exception("ERROR: not authorized");
    }   
    
    //--------------------------------------------------------------------- entries
    
    /**
     * Create entry in the entry collection (a Roller blog has only one).
     */
    public Entry postEntry(String[] pathInfo, Entry entry) throws Exception {
        // authenticated client posted a weblog entry
        String handle = pathInfo[0];
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        if (canEdit(website)) {
            // Save it and commit it
            WeblogManager mgr = mRoller.getWeblogManager();
            WeblogEntryData rollerEntry = createRollerEntry(website, entry);
            rollerEntry.setCreator(this.user);
            mgr.saveWeblogEntry(rollerEntry);    
            mRoller.flush();
            
            // Throttle one entry per second
            // (MySQL timestamp has 1 sec resolution, damnit)
            Thread.sleep(1000);
            
            CacheManager.invalidate(website);
            if (rollerEntry.isPublished()) {
                mRoller.getIndexManager().addEntryReIndexOperation(rollerEntry);
            }            
            return createAtomEntry(rollerEntry);
        }
        throw new Exception("ERROR not authorized to edit website");
    }
    
    /**
     * Retrieve entry, URI like this /blog-name/entry/id
     */
    public Entry getEntry(String[] pathInfo) throws Exception {
        if (pathInfo.length == 3) // URI is /blogname/entries/entryid
        {
            WeblogEntryData entry =
                mRoller.getWeblogManager().getWeblogEntry(pathInfo[2]);
            if (entry != null && !canView(entry)) {
                throw new Exception("ERROR not authorized to view entry");
            } else if (entry != null) {
                return createAtomEntry(entry);
            }
            return null;
        }
        throw new Exception("ERROR: bad URI");
    }
    
    /**
     * Update entry, URI like this /blog-name/entry/id
     */
    public Entry putEntry(String[] pathInfo, Entry entry) throws Exception {
        if (pathInfo.length == 3) // URI is /blogname/entries/entryid
        {
            WeblogEntryData rollerEntry =
                    mRoller.getWeblogManager().getWeblogEntry(pathInfo[2]);
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
            throw new Exception("ERROR not authorized to put entry");
        }
        throw new Exception("ERROR: bad URI");
    }
    
    /**
     * Delete entry, URI like this /blog-name/entry/id
     */
    public void deleteEntry(String[] pathInfo) throws Exception {
        if (pathInfo.length == 3) // URI is /blogname/entries/entryid
        {
            WeblogEntryData rollerEntry =
                    mRoller.getWeblogManager().getWeblogEntry(pathInfo[2]);
            if (canEdit(rollerEntry)) {
                WeblogManager mgr = mRoller.getWeblogManager();
                mgr.removeWeblogEntry(rollerEntry);
                mRoller.flush();
                CacheManager.invalidate(rollerEntry.getWebsite());
                mRoller.getIndexManager().removeEntryIndexOperation(rollerEntry);
                return;
            }
            throw new Exception("ERROR not authorized to delete entry");
        }
        throw new Exception("ERROR: bad URI");
    }
    
    //-------------------------------------------------------------------- resources
    
    /**
     * Create new resource in generic collection (a Roller blog has only one).
     * TODO: can we avoid saving temporary file?
     * TODO: do we need to handle mutli-part MIME uploads?
     * TODO: use Jakarta Commons File-upload?
     */
    public Entry postMedia(String[] pathInfo, 
            String name, String contentType, InputStream is)
            throws Exception {
        // authenticated client posted a weblog entry
        File tempFile = null;
        RollerMessages msgs = new RollerMessages();
        String handle = pathInfo[0];
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        if (canEdit(website) && pathInfo.length > 1) {
            // save to temp file
            if (name == null) {
                throw new Exception("ERROR[postResource]: No 'name' present in HTTP headers");
            }
            try {
                FileManager fmgr = mRoller.getFileManager();
                tempFile = File.createTempFile(name,"tmp");
                FileOutputStream fos = new FileOutputStream(tempFile);
                Utilities.copyInputToOutput(is, fos);
                fos.close();
                
                // If save is allowed by Roller system-wide policies
                if (fmgr.canSave(website.getHandle(), name, tempFile.length(), msgs)) {
                    // Then save the file
                    FileInputStream fis = new FileInputStream(tempFile);
                    fmgr.saveFile(website.getHandle(), name, tempFile.length(), fis);
                    fis.close();
                    
                    File resource = new File(fmgr.getUploadDir() + File.separator + name);
                    return createAtomResourceEntry(website, resource);
                }

            } catch (Exception e) {
                String msg = "ERROR in atom.postResource";
                mLogger.error(msg,e);
                throw new Exception(msg);
            } finally {
                if (tempFile != null) tempFile.delete();
            }
        }
        throw new Exception("File upload denied because:" + msgs.toString());
    }
    
    /**
     * Get absolute path to resource specified by path info.
     */
    public Entry getMedia(String[] pathInfo) throws Exception {
        String handle = pathInfo[0];
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        String uploadPath = RollerFactory.getRoller().getFileManager().getUploadUrl();
        File resource = new File(uploadPath + File.separator + pathInfo[2]);
        return createAtomResourceEntry(website, resource);
    }
    
    /**
     * Update resource specified by pathInfo using data from input stream.
     * Expects pathInfo of form /blog-name/resources/name
     */
    public Entry putMedia(String[] pathInfo,
            String contentType, InputStream is) throws Exception {
        if (pathInfo.length > 2) {
            String name = pathInfo[2];
            return postMedia(pathInfo, name, contentType, is);
        }
        throw new Exception("ERROR: bad pathInfo");
    }
    
    /**
     * Delete resource specified by pathInfo.
     * Expects pathInfo of form /blog-name/resources/name
     */
    public void deleteMedia(String[] pathInfo) throws Exception {
        // authenticated client posted a weblog entry
        String handle = pathInfo[0];
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        if (canEdit(website) && pathInfo.length > 1) {
            try {
                FileManager fmgr = mRoller.getFileManager();
                fmgr.deleteFile(website.getHandle(), pathInfo[2]);
            } catch (Exception e) {
                String msg = "ERROR in atom.deleteResource";
                mLogger.error(msg,e);
                throw new Exception(msg);
            }
        }
        else throw new Exception("ERROR not authorized to edit website");
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
        if (pathInfo.length > 1 && pathInfo[1].equals("entry")) return true;
        return false;
    }
    
    /**
     * True if URL is a resource URI.
     */
    public boolean isMediaURI(String[] pathInfo) {
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
    
    /**
     * True if URL is a entry collection URI.
     */
    public boolean isEntryCollectionURI(String[] pathInfo) {
        if (pathInfo.length > 1 && pathInfo[1].equals("entries")) return true;
        return false;
    }
    
    /**
     * True if URL is a resource collection URI.
     */
    public boolean isMediaCollectionURI(String[] pathInfo) {
        if (pathInfo.length > 1 && pathInfo[1].equals("resources")) return true;
        return false;
    }
    
    /**
     * True if URL is a category collection URI.
     */
    public boolean isCategoryCollectionURI(String[] pathInfo) {
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
     * Untested (and currently unused) implementation of BASIC authentication
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
        
        String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
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
        editlink.setHref(absUrl + "/app/"
            + entry.getWebsite().getHandle() + "/entry/" + entry.getId());
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
        String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
        String editURI = absUrl
                + "/app/" + website.getHandle()
                + "/resource/" + file.getName();
        String viewURI = absUrl
                + "/resources/" + website.getHandle()
                + "/" + file.getName();
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
        String contentType = map.getContentType(file);

        Entry entry = new Entry();
        entry.setTitle(file.getName());
        entry.setUpdated(new Date(file.lastModified()));

        Link editlink = new Link();
        editlink.setRel("edit");
        editlink.setHref(editURI);
        List otherlinks = new ArrayList();
        otherlinks.add(editlink);
        entry.setOtherLinks(otherlinks);

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
        } else {
            // or we fall back to the default Blogger API category
            rollerEntry.setCategory(website.getBloggerCategory());
        }
        return rollerEntry;
    }
}
