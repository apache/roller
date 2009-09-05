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

import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.feed.atom.Link;
import com.sun.syndication.propono.atom.common.rome.AppModule;
import com.sun.syndication.propono.atom.common.rome.AppModuleImpl;
import com.sun.syndication.propono.atom.server.AtomException;
import com.sun.syndication.propono.atom.server.AtomMediaResource;
import com.sun.syndication.propono.atom.server.AtomNotAuthorizedException;
import com.sun.syndication.propono.atom.server.AtomNotFoundException;
import com.sun.syndication.propono.atom.server.AtomRequest;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileIOException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Collection of media resources.
 * @author davidm.johnson@sun.com
 */
public class MediaCollection {
    private Weblogger      roller;
    private User           user;
    private int            maxEntries = 20;    
    private final String   atomURL;    
    
    private static Log log =
            LogFactory.getFactory().getInstance(EntryCollection.class);
    
    
    public MediaCollection(User user, String atomURL) {
        this.user = user;
        this.atomURL = atomURL;
        this.roller = WebloggerFactory.getWeblogger();
    }  
    
    
    public String postMedia(AtomRequest areq, Entry entry) throws AtomException {
        log.debug("Entering");
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");

        try {
            // get incoming slug from HTTP header
            String slug = areq.getHeader("Slug");

            Content content = (Content)entry.getContents().get(0); 
            String contentType = content.getType();
            InputStream is = areq.getInputStream();
            String title = entry.getTitle() != null ? entry.getTitle() : slug;
            
            // authenticated client posted a weblog entry
            File tempFile = null;
            String handle = pathInfo[0];
            MediaFileManager fileMgr = roller.getMediaFileManager();
            Weblog website = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(handle);
            if (!RollerAtomHandler.canEdit(user, website)) {
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
                    String justPath = path.substring(path.lastIndexOf("/"));
                    String justName = path.substring(0, path.lastIndexOf("/"));
                    MediaFileDirectory mdir =
                        fileMgr.getMediaFileDirectoryByPath(website, justPath);

                    if (mdir.hasMediaFile(fileName)) {
                        throw new AtomException("Duplicate file name");
                    }

                    if (path.length() > 0) path = path + File.separator;
                    FileInputStream fis = new FileInputStream(tempFile);

                    MediaFile mf = new MediaFile();
                    mf.setDirectory(mdir);
                    mf.setName(fileName);
                    mf.setOriginalPath(justPath);
                    mf.setContentType(contentType);
                    mf.setInputStream(fis);
                    mf.setLength(tempFile.length());

                    fileMgr.createMediaFile(website, mf);
                    roller.flush();
                    
                    fis.close();
                                      
                    MediaFile stored = fileMgr.getMediaFile(mf.getId());
                    Entry mediaEntry = createAtomResourceEntry(website, stored);
                    for (Iterator it = mediaEntry.getOtherLinks().iterator(); it.hasNext();) {
                        Link link = (Link)it.next();
                        if ("edit".equals(link.getRel())) {
                            log.debug("Exiting");
                            return link.getHrefResolved();
                        }
                    }
                    log.error("ERROR: no edit link found in saved media entry");
                    
                } catch (FileIOException fie) {
                    throw new AtomException(
                        "File upload disabled, over-quota or other error", fie);
                } finally {
                    if (tempFile != null) tempFile.delete();
                }
            }
            throw new AtomException("Error saving media entry");
        
        } catch (WebloggerException re) {
            throw new AtomException("Posting media", re);
        } catch (IOException ioe) {
            throw new AtomException("Posting media", ioe);
        }
    }
    
    
    public Entry getEntry(AtomRequest areq) throws AtomException {
        try {
            String[] pathInfo = Utilities.stringToStringArray(areq.getPathInfo(), "/");

            String filePath = filePathFromPathInfo(pathInfo);
            filePath = filePath.substring(0, filePath.length() - ".media-link".length());
            String handle = pathInfo[0];
            Weblog website = roller.getWeblogManager().getWeblogByHandle(handle);

            MediaFileManager fileMgr = roller.getMediaFileManager();
            MediaFile mf = fileMgr.getMediaFileByPath(website, filePath);

            log.debug("Exiting");
            if (mf != null) {
                return createAtomResourceEntry(website, mf);
            }
            
        } catch (WebloggerException ex) {
            throw new AtomException("ERROR fetching entry",ex);
        }
        throw new AtomNotFoundException("ERROR resource not found");
    }
    
    
    public AtomMediaResource getMediaResource(AtomRequest areq) throws AtomException {
        log.debug("Entering");
        String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
        try {
            // authenticated client posted a weblog entry
            File tempFile = null;
            String handle = pathInfo[0];
            MediaFileManager fmgr = roller.getMediaFileManager();
            Weblog website = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(handle);
            if (!RollerAtomHandler.canEdit(user, website)) {
                throw new AtomNotAuthorizedException("Not authorized to edit weblog: " + handle);
            }
            if (pathInfo.length > 1) {
                try {                                        
                    // Parse pathinfo to determine file path
                    String filePath = filePathFromPathInfo(pathInfo);
                    MediaFile mf = fmgr.getMediaFileByOriginalPath(website, filePath);
                    return new AtomMediaResource(
                            mf.getName(),
                            mf.getLength(),
                            new Date(mf.getLastModified()),
                            mf.getInputStream());
                } catch (Exception e) {
                    throw new AtomException(
                        "Unexpected error during file upload", e);
                }
            }
            throw new AtomException("Incorrect path information");
        
        } catch (WebloggerException re) {
            throw new AtomException("Posting media");
        }
    }
    
    
    public Feed getCollection(AtomRequest areq) throws AtomException {
        log.debug("Entering");
        String[] rawPathInfo = StringUtils.split(areq.getPathInfo(),"/");
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
            Weblog website = roller.getWeblogManager().getWeblogByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("Cannot find weblog: " + handle);
            }
            if (!RollerAtomHandler.canView(user, website)) {
                throw new AtomNotAuthorizedException("Not authorized to access website");
            }

            Feed feed = new Feed();
            feed.setId(atomURL
                +"/"+website.getHandle() + "/resources/" + path + start);                
            feed.setTitle(website.getName());

            Link link = new Link();
            link.setHref(absUrl + "/" + website.getHandle());
            link.setRel("alternate");
            link.setType("text/html");
            feed.setAlternateLinks(Collections.singletonList(link));

            MediaFileManager fmgr = roller.getMediaFileManager();
            MediaFileDirectory dir = null;
            if (StringUtils.isNotEmpty(path)) {
                log.debug("Fetching resource collection from weblog " + handle + " at path: " + path);
                dir = fmgr.getMediaFileDirectoryByPath(website, path);
            } else {
                log.debug("Fetching root resource collection from weblog " + handle);
                dir = fmgr.getMediaFileRootDirectory(website);
            }
            Set<MediaFile> files = dir.getMediaFiles();

            SortedSet sortedSet = new TreeSet(new Comparator() {
                public int compare(Object o1, Object o2) {
                    MediaFile f1 = (MediaFile)o1;
                    MediaFile f2 = (MediaFile)o2;
                    if (f1.getLastModified() < f2.getLastModified()) return 1;
                    else if (f1.getLastModified() == f2.getLastModified()) return 0;
                    else return -1;
                }
            });
                                    
            if (files != null && start < files.size()) {
                for (MediaFile mf : files) {
                    sortedSet.add(mf);
                }
                int count = 0;
                MediaFile[] sortedResources =
                   (MediaFile[])sortedSet.toArray(new MediaFile[sortedSet.size()]);
                List atomEntries = new ArrayList();
                for (int i=start; i<(start + max) && i<(sortedResources.length); i++) {
                    Entry entry = createAtomResourceEntry(website, sortedResources[i]);
                    atomEntries.add(entry);
                    if (count == 0) {
                        // first entry is most recent
                        feed.setUpdated(entry.getUpdated());
                    }
                    count++;
                }

                List otherLinks = new ArrayList();
                if (start + count < files.size()) { // add next link
                    int nextOffset = start + max;
                    String url = atomURL
                        +"/"+ website.getHandle() + "/resources/" + path + nextOffset;
                    Link nextLink = new Link();
                    nextLink.setRel("next");
                    nextLink.setHref(url);
                    otherLinks.add(nextLink);
                }
                if (start > 0) { // add previous link
                    int prevOffset = start > max ? start - max : 0;
                    String url = atomURL
                        +"/"+website.getHandle() + "/resources/" + path + prevOffset;
                    Link prevLink = new Link();
                    prevLink.setRel("previous");
                    prevLink.setHref(url);
                    otherLinks.add(prevLink);
                }
                feed.setOtherLinks(otherLinks);
                feed.setEntries(atomEntries);

                log.debug("Collection contains: " + count);

            } else {
                log.debug("Returning empty collection");
            }
            

            log.debug("Exiting");
            return feed;
       
        } catch (WebloggerException re) {
            throw new AtomException("Getting resource collection", re);
        }
    }
    
    
    public void putMedia(AtomRequest areq) throws AtomException {
       String[] pathInfo = StringUtils.split(areq.getPathInfo(),"/");
       String contentType = areq.getContentType();
       try {
            InputStream is = areq.getInputStream();
     
            // authenticated client posted a weblog entry
            File tempFile = null;
            String handle = pathInfo[0];
            MediaFileManager fmgr = roller.getMediaFileManager();
            WeblogManager wmgr = roller.getWeblogManager();
            Weblog website = wmgr.getWeblogByHandle(handle);
            if (!RollerAtomHandler.canEdit(user, website)) {
                throw new AtomNotAuthorizedException("Not authorized to edit weblog: " + handle);
            }
            if (pathInfo.length > 1) {
                // Save to temp file
                try {
                    tempFile = File.createTempFile(UUID.randomUUID().toString(), "tmp");
                    FileOutputStream fos = new FileOutputStream(tempFile);
                    Utilities.copyInputToOutput(is, fos);
                    fos.close();
                                        
                    FileInputStream fis = new FileInputStream(tempFile);

                    // Parse pathinfo to determine file path
                    String path = filePathFromPathInfo(pathInfo);
                    
                    // Attempt to load file, to ensure it exists
                    MediaFile mf = fmgr.getMediaFileByPath(website, path);
                    mf.setContentType(contentType);
                    mf.setInputStream(fis);
                    mf.setLength(tempFile.length());

                    fmgr.updateMediaFile(website, mf, fis);

                    roller.flush();

                    fis.close();
                    
                    log.debug("Exiting");
                    return;

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
        } catch (IOException ioe) {
            throw new AtomException("Posting media", ioe);
        }
    }
    
    
    public void deleteEntry(AtomRequest areq) throws AtomException {
        try {
            String[] pathInfo = StringUtils.split(areq.getPathInfo(), "/");
            String handle = pathInfo[0];
            Weblog website = roller.getWeblogManager().getWeblogByHandle(handle);
            if (website == null) {
                throw new AtomNotFoundException("cannot find specified weblog");
            }
            if (RollerAtomHandler.canEdit(user, website) && pathInfo.length > 1) {
                try {
                    String path = filePathFromPathInfo(pathInfo);
                    String fileName = path.substring(0, path.length() - ".media-link".length());
                    MediaFileManager fmgr = roller.getMediaFileManager();
                    MediaFile mf = fmgr.getMediaFileByPath(website, path);
                    fmgr.removeMediaFile(website, mf);
                    log.debug("Deleted media entry: " + fileName);
                    return;
                    
                } catch (Exception e) {
                    String msg = "ERROR deleting media entry";
                    log.error(msg, e);
                    throw new AtomException(msg);
                }
            }
            log.debug("Not authorized to delete media entry"); 
            log.debug("Exiting via exception"); 

        } catch (WebloggerException ex) {
            throw new AtomException("ERROR deleting media entry",ex);
        }
        throw new AtomNotAuthorizedException("Not authorized to delete entry");
    }
    
    
    private String filePathFromPathInfo(String[] pathInfo) {
        String path = null;
        if (pathInfo.length > 2) {
            for (int i = 2; i < pathInfo.length; i++) {
                if (path != null && path.length() > 0)
                    path = path + File.separator + pathInfo[i];
                else
                    path = pathInfo[i];
            }
        } if (pathInfo.length == 2) {
            path = "";
        }
        return path;
    }
    
    private Entry createAtomResourceEntry(Weblog website, MediaFile file) {
        String absUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
        String filePath = 
            file.getPath().startsWith("/") ? file.getPath().substring(1) : file.getPath();
        String editURI = 
                atomURL+"/"+website.getHandle()
                + "/resource/" + filePath + ".media-link";
        String editMediaURI = 
                atomURL+"/"+ website.getHandle()
                + "/resource/" + filePath;
        URLStrategy urlStrategy = WebloggerFactory.getWeblogger().getUrlStrategy();
        String viewURI = urlStrategy.getWeblogResourceURL(website, filePath, true);
        
        String contentType = Utilities.getContentTypeFromFileName(file.getName());
        
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
            if (!tmp.endsWith("." + ext)) {
                fileName = tmp + "." + ext;
            } else {
                fileName = tmp;
            }            
        } else {            
            // No title or text, so instead we'll use the item's date
            // in YYYYMMDD format to form the file name
            SimpleDateFormat sdf = new SimpleDateFormat();
            sdf.applyPattern("yyyyMMddHHSS");
            fileName = weblog.getHandle()+"-"+sdf.format(new Date())+"."+ext;
        }
        
        return fileName;
    }
}
