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
package org.roller.presentation.atomapi;
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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.model.FileManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.UserData;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.util.RollerMessages;
import org.roller.util.Utilities;

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.io.impl.Base64;
import javax.activation.FileTypeMap;
import org.roller.RollerException;
import org.roller.presentation.cache.CacheManager;
import org.roller.presentation.atomapi.PubControlModule;
import org.roller.presentation.atomapi.PubControlModuleImpl;

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
    private String             mUsername;
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
        mRollerContext = RollerContext.getRollerContext(request);
        
        // TODO: decide what to do about authentication, is WSSE going to fly?
        //mUsername = authenticateWSSE(request);
        mUsername = authenticateBASIC(request);
        
        if (mUsername != null) {
            try {
                UserData user = mRoller.getUserManager().getUser(mUsername);
                mRoller.setUser(user);
            } catch (Exception e) {
                mLogger.error("ERROR: setting user", e);
            }
        }
    }
    
    /**
     * Return weblogHandle of authenticated user or null if there is none.
     */
    public String getAuthenticatedUsername() {
        return mUsername;
    }
    
    //---------------------------------------------------------------- introspection
    
    /**
     * Return Atom service document for site, getting blog-name from pathInfo.
     * Since a user can (currently) have only one blog, one workspace is returned.
     * The workspace will contain collections for entries, categories and resources.
     */
    public AtomService getIntrospection(String[] pathInfo) throws Exception {
        if (pathInfo.length == 0) {
            String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
            AtomService service = new AtomService();
            UserData user = mRoller.getUserManager().getUser(mUsername);
            List perms = mRoller.getUserManager().getAllPermissions(user);
            if (perms != null) {
                for (Iterator iter=perms.iterator(); iter.hasNext();) {
                    PermissionsData perm = (PermissionsData)iter.next();
                    String handle = perm.getWebsite().getHandle();
                    AtomService.Workspace workspace = new AtomService.Workspace();
                    workspace.setTitle("Workspace: Collections for " + handle);
                    service.addWorkspace(workspace);
                    
                    AtomService.Collection entryCol = new AtomService.Collection();
                    entryCol.setTitle("Collection: Weblog Entries for " + handle);
                    entryCol.setMemberType("entry");
                    entryCol.setHref(absUrl + "/app/"+handle+"/entries");
                    workspace.addCollection(entryCol);
                                        
                    AtomService.Collection uploadCol = new AtomService.Collection();
                    uploadCol.setTitle("Collection: Resources for " + handle);
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
        int end = mMaxEntries;
        if (pathInfo.length > 2) {
            try { 
                String s = pathInfo[2].trim();
                start = Integer.parseInt(s);
                if (start > 0) end = start + mMaxEntries;
            } catch (Throwable t) {
                mLogger.warn("Unparsable range: " + pathInfo[2]);
            }
        }
        if (pathInfo.length > 0 && pathInfo[1].equals("entries")) {
            return getCollectionOfEntries(pathInfo, start, end);
        } else if (pathInfo.length > 0 && pathInfo[1].equals("resources")) {
            return getCollectionOfResources(pathInfo, start, end);
        }
        throw new Exception("ERROR: bad URL in getCollection()");
    }
    
    /**
     * Helper method that returns collection of entries, called by getCollection().
     */
    public Feed getCollectionOfEntries(
            String[] pathInfo, int start, int end) throws Exception {
        String handle = pathInfo[0];
        String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        List entries = null;
        if (canView(website)) {
            entries = mRoller.getWeblogManager().getWeblogEntries(
                    website,  // website
                    null,   // startDate
                    null,   // endDate
                    null,   // catName
                    null,   // status
                    start, // offset (for range paging)
                    end - start + 2);  // maxEntries
            Feed feed = new Feed();
            List atomEntries = new ArrayList();
            int count = 0;
            for (Iterator iter = entries.iterator(); iter.hasNext() && count < mMaxEntries; count++) {
                WeblogEntryData rollerEntry = (WeblogEntryData)iter.next();
                atomEntries.add(createAtomEntry(rollerEntry));
            }
            if (count > start - end) { // add next link
                int nextOffset = start + mMaxEntries; 
                String url = absUrl + "/app/" + website.getHandle() + "/entries/" + nextOffset;
                Link nextLink = new Link();
                nextLink.setRel("next");
                nextLink.setHref(url);
                List next = new ArrayList();
                next.add(nextLink);
                feed.setOtherLinks(next);
            }
            if (start > 0) { // add previous link
                int prevOffset = start > mMaxEntries ? start - mMaxEntries : 0;
                String url = absUrl + "/app/" +website.getHandle() + "/entries/" + prevOffset;
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
    
    /**
     * Helper method that returns collection of resources, called by getCollection().
     */
    public Feed getCollectionOfResources(
            String[] pathInfo, int start, int end) throws Exception {
        String handle = pathInfo[0];
        String absUrl = mRollerContext.getAbsoluteContextUrl(mRequest);
        WebsiteData website = mRoller.getUserManager().getWebsiteByHandle(handle);
        FileManager fmgr = mRoller.getFileManager();
        File[] files = fmgr.getFiles(website);
        if (canView(website)) {            
            Feed feed = new Feed();
            List atomEntries = new ArrayList();
            int count = 0;
            if (files != null && start < files.length) {
                end = (end > files.length) ? files.length : end;
                for (int i=start; i<end; i++) {                   
                    Entry entry = createAtomResourceEntry(website, files[i]);
                    atomEntries.add(entry);
                    count++;
                }
            }
            if (start + count < files.length) { // add next link
                int nextOffset = start + mMaxEntries; 
                String url = absUrl + "/app/" + website.getHandle() + "/resources/" + nextOffset;
                Link nextLink = new Link();
                nextLink.setRel("next");
                nextLink.setHref(url);
                List next = new ArrayList();
                next.add(nextLink);
                feed.setOtherLinks(next);
            }
            if (start > 0) { // add previous link
                int prevOffset = start > mMaxEntries ? start - mMaxEntries : 0;
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
        UserData creator = mRoller.getUserManager().getUser(mUsername);
        if (canEdit(website)) {
            // Save it and commit it
            WeblogEntryData rollerEntry = createRollerEntry(website, entry);
            rollerEntry.setCreator(creator);
            rollerEntry.save();
            mRoller.commit();
            
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
                mRoller.getWeblogManager().retrieveWeblogEntry(pathInfo[2]);
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
                    mRoller.getWeblogManager().retrieveWeblogEntry(pathInfo[2]);
            if (canEdit(rollerEntry)) {
                WeblogEntryData rawUpdate = createRollerEntry(rollerEntry.getWebsite(), entry);
                rollerEntry.setPubTime(rawUpdate.getPubTime());
                rollerEntry.setUpdateTime(rawUpdate.getUpdateTime());
                rollerEntry.setText(rawUpdate.getText());
                rollerEntry.setStatus(rawUpdate.getStatus());
                rollerEntry.setCategory(rawUpdate.getCategory());
                rollerEntry.setTitle(rawUpdate.getTitle());
                rollerEntry.save();
                mRoller.commit();
                
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
                    mRoller.getWeblogManager().retrieveWeblogEntry(pathInfo[2]);
            if (canEdit(rollerEntry)) {
                rollerEntry.remove();
                mRoller.commit();
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
                if (fmgr.canSave(website, name, tempFile.length(), msgs)) {
                    // Then save the file
                    FileInputStream fis = new FileInputStream(tempFile);
                    fmgr.saveFile(website, name, tempFile.length(), fis);
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
        String uploadPath = RollerContext.getUploadPath(
                mRequest.getSession(true).getServletContext());
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
                fmgr.deleteFile(website, pathInfo[2]);
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
            return entry.canSave();
        } catch (Exception e) {
            mLogger.error("ERROR: checking website.canSave()");
        }
        return false;
    }
    
    /**
     * Return true if user is allowed to edit a website.
     */
    private boolean canEdit(WebsiteData website) {
        try {
            return website.canSave();
        } catch (Exception e) {
            mLogger.error("ERROR: checking website.canSave()");
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
            UserData user = mRoller.getUserManager().getUser(userName);
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
                        String userPass = new String(Base64.decode(credentials));
                        int p = userPass.indexOf(":");
                        if (p != -1) {
                            userID = userPass.substring(0, p);
                            UserData user = mRoller.getUserManager().getUser(userID);
                            String realpassword = user.getPassword();
                            password = userPass.substring(p+1);
                            if (    (userID.trim().equals(user.getUserName()))
                            && (password.trim().equals(realpassword))) {
                                valid = true;
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
            new Boolean(!WeblogEntryData.PUBLISHED.equals(entry.getStatus())));
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
        if (control!=null && control.getDraft()!=null && control.getDraft().booleanValue()) {
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
                WeblogCategoryData rollerCat =
                    mRoller.getWeblogManager().getWeblogCategoryByPath(
                    website, cat.getTerm());
                if (rollerCat != null) {
                    rollerEntry.setCategory(rollerCat);
                    break;
                }
            }
        } else {
            rollerEntry.setCategory(website.getBloggerCategory());
        }
        return rollerEntry;
    }
}
