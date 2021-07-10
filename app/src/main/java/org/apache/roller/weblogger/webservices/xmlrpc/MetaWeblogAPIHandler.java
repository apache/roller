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

package org.apache.roller.weblogger.webservices.xmlrpc;

import java.io.ByteArrayInputStream;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.xmlrpc.XmlRpcException;


/**
 * Weblogger XML-RPC Handler for the MetaWeblog API.
 * 
 * MetaWeblog API spec can be found at http://www.xmlrpc.com/metaWeblogApi
 * 
 * @author David M Johnson
 */
public class MetaWeblogAPIHandler extends BloggerAPIHandler {
    
    static final long serialVersionUID = -1364456614935668629L;
    
    private static Log mLogger = LogFactory.getLog(MetaWeblogAPIHandler.class);
    
    public MetaWeblogAPIHandler() {
        super();
    }
    
    
    /**
     * Authenticates a user and returns the categories available in the website
     * 
     * @param blogid Dummy Value for Weblogger
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @return 
     * @throws Exception
     */
    public Object getCategories(String blogid, String userid, String password)
    throws Exception {
        
        mLogger.debug("getCategories() Called =====[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        
        Weblog website = validate(blogid, userid,password);
        Weblogger roller = WebloggerFactory.getWeblogger();
        try {
            Hashtable<String, Object> result = new Hashtable<>();
            WeblogEntryManager weblogMgr = roller.getWeblogEntryManager();
            List<WeblogCategory> cats = weblogMgr.getWeblogCategories(website);
            for (WeblogCategory category : cats) {
                result.put(category.getName(),
                        createCategoryStruct(category, userid));
            }
            return result;
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getCategories";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Edits a given post. Optionally, will publish the blog after making the edit
     *
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws org.apache.xmlrpc.XmlRpcException
     * @return
     */
    public boolean editPost(String postid, String userid, String password,
            Hashtable<String, ?> struct, int publish) throws Exception {
        
        return editPost(postid, userid, password, struct, publish > 0);
    }
    
    
    public boolean editPost(String postid, String userid, String password,
            Hashtable<String, ?> struct, boolean publish) throws Exception {
        
        mLogger.debug("editPost() Called ========[ SUPPORTED ]=====");
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);
        
        Weblogger roller = WebloggerFactory.getWeblogger();
        WeblogEntryManager weblogMgr = roller.getWeblogEntryManager();
        WeblogEntry entry = weblogMgr.getWeblogEntry(postid);
        
        validate(entry.getWebsite().getHandle(), userid,password);
        
        Hashtable<String, ?> postcontent = struct;
        String description = (String)postcontent.get("description");
        String title = (String)postcontent.get("title");
        if (title == null) {
            title = "";
        }
        
        Date dateCreated = (Date)postcontent.get("dateCreated");
        if (dateCreated == null) {
            dateCreated = (Date)postcontent.get("pubDate");
        }
        
        String cat = null;
        if ( postcontent.get("categories") != null ) {
            Object[] cats = (Object[])postcontent.get("categories");
            if (cats.length > 0) {
            	cat = (String)cats[0];
            }
        }
        mLogger.debug("      Title: " + title);
        mLogger.debug("   Category: " + cat);
        
        try {
            
            Timestamp current =
                    new Timestamp(System.currentTimeMillis());
            
            if ( !title.isEmpty() ) {
                entry.setTitle(title);
            }
            entry.setText(description);
            entry.setUpdateTime(current);
            if (publish) {
                entry.setStatus(PubStatus.PUBLISHED);
            } else {
                entry.setStatus(PubStatus.DRAFT);
            }
            if (dateCreated != null) {
                entry.setPubTime(new Timestamp(dateCreated.getTime()));
            }
            
            if ( cat != null ) {
                // Use first category specified by request
                WeblogCategory cd =
                        weblogMgr.getWeblogCategoryByName(entry.getWebsite(), cat);
                entry.setCategory(cd);
            }
            
            // save the entry
            weblogMgr.saveWeblogEntry(entry);
            roller.flush();
            
            // notify cache
            flushPageCache(entry.getWebsite());
            
            // TODO: Weblogger timestamps need better than 1 second accuracy
            // Until then, we can't allow more than one post per second
            Thread.sleep(RollerConstants.SEC_IN_MS);
            
            return true;
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.editPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog after making the post
     *
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a MetaWeblog user who has permission to post to the blog
     * @param password Password for said username
     * @param struct Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws org.apache.xmlrpc.XmlRpcException
     * @return
     */
    public String newPost(String blogid, String userid, String password,
            Hashtable<String, ?> struct, int publish) throws Exception {
        
        return newPost(blogid, userid, password, struct, publish > 0);
    }
    
    
    public String newPost(String blogid, String userid, String password,
            Hashtable<String, ?> struct, boolean publish) throws Exception {
        
        mLogger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);
        
        Weblog website = validate(blogid, userid, password);
        
        Hashtable<String, ?> postcontent = struct;
        String description = (String)postcontent.get("description");
        String title = (String)postcontent.get("title");
        if (StringUtils.isEmpty(title) && StringUtils.isEmpty(description)) {
            throw new XmlRpcException(
                    BLOGGERAPI_INCOMPLETE_POST, "Must specify title or description");
        }
        if (StringUtils.isEmpty(title)) {
            title = Utilities.truncateNicely(description, 15, 15, "...");
        }
        
        Date dateCreated = (Date)postcontent.get("dateCreated");
        if (dateCreated == null) {
            dateCreated = (Date)postcontent.get("pubDate");
        }
        if (dateCreated == null) {
            dateCreated = new Date();
        }
        mLogger.debug("      Title: " + title);
        
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager weblogMgr = roller.getWeblogEntryManager();
            User user = roller.getUserManager().getUserByUserName(userid);
            Timestamp current = new Timestamp(System.currentTimeMillis());
            
            WeblogEntry entry = new WeblogEntry();
            entry.setTitle(title);
            entry.setText(description);
            entry.setLocale(website.getLocale());
            entry.setPubTime(new Timestamp(dateCreated.getTime()));
            entry.setUpdateTime(current);
            entry.setWebsite(website);
            entry.setCreatorUserName(user.getUserName());
            entry.setCommentDays(website.getDefaultCommentDays());
            entry.setAllowComments(website.getDefaultAllowComments());
        
            if (publish) {
                entry.setStatus(PubStatus.PUBLISHED);
            } else {
                entry.setStatus(PubStatus.DRAFT);
            }
            
            // MetaWeblog supports multiple cats, Weblogger supports one/entry
            // so here we take accept the first category that exists
            WeblogCategory rollerCat = null;
            if ( postcontent.get("categories") != null ) {
                Object[] cats = (Object[])postcontent.get("categories");
                if (cats != null && cats.length > 0) {
                    mLogger.debug("cats type - "+cats[0].getClass().getName());
                    mLogger.debug("cat to string - "+cats[0].toString());
                    for (int i=0; i<cats.length; i++) {
                        Object cat = cats[i];
                        rollerCat = weblogMgr.getWeblogCategoryByName(website, (String)cat);
                        if (rollerCat != null) {
                            entry.setCategory(rollerCat);
                            break;
                        }
                    }
                }
            }
            if (rollerCat == null) {
                // or we fall back to the default Blogger API category
                entry.setCategory(website.getBloggerCategory());
            }
            
            // save the entry
            weblogMgr.saveWeblogEntry(entry);
            roller.flush();
            
            // notify cache
            flushPageCache(entry.getWebsite());
            
            // TODO: Weblogger timestamps need better than 1 second accuracy
            // Until then, we can't allow more than one post per second
            Thread.sleep(RollerConstants.SEC_IN_MS);
            
            return entry.getId();
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.newPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     *
     * @param postid
     * @param userid
     * @param password
     * @return
     * @throws Exception
     */
    public Object getPost(String postid, String userid, String password)
    throws Exception {
        
        mLogger.debug("getPost() Called =========[ SUPPORTED ]=====");
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);
        
        Weblogger roller = WebloggerFactory.getWeblogger();
        WeblogEntryManager weblogMgr = roller.getWeblogEntryManager();
        WeblogEntry entry = weblogMgr.getWeblogEntry(postid);
        
        if (entry == null) {
            throw new XmlRpcException(INVALID_POSTID, INVALID_POSTID_MSG);
        }
        validate(entry.getWebsite().getHandle(), userid, password);
        
        try {
            return createPostStruct(entry, userid);
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getPost";
            mLogger.error(msg, e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Allows user to post a binary object, a file, to Weblogger. If the file is
     * allowed by the RollerConfig file-upload settings, then the file will be
     * placed in the user's upload diretory.
     */
    public Object newMediaObject(String blogid, String userid, String password,
            Hashtable<String, ?> struct) throws Exception {
        
        mLogger.debug("newMediaObject() Called =[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("   Password: *********");
        
        Weblog website = validate(blogid, userid, password);
        try {
            String name = (String) struct.get("name");
            name = name.replace("/","_");
            String type = (String) struct.get("type");
            mLogger.debug("newMediaObject name: " + name);
            mLogger.debug("newMediaObject type: " + type);
            
            byte[] bits = (byte[]) struct.get("bits");
            
            Weblogger roller = WebloggerFactory.getWeblogger();
            MediaFileManager fmgr = roller.getMediaFileManager();
            MediaFileDirectory root = fmgr.getDefaultMediaFileDirectory(website);
 
            // Try to save file
            MediaFile mf = new MediaFile();
            mf.setDirectory(root);
            mf.setWeblog(website);
            mf.setName(name);
            mf.setContentType(type);
            mf.setInputStream(new ByteArrayInputStream(bits));
            mf.setLength(bits.length);
            String fileLink = mf.getPermalink();
            
            RollerMessages errors = new RollerMessages();
            fmgr.createMediaFile(website, mf, errors);
            
            if (errors.getErrorCount() > 0) {
                throw new Exception(errors.toString());
            }

            roller.flush();
            
            Hashtable<String, String> returnStruct = new Hashtable<>(1);
            returnStruct.put("url", fileLink);
            return returnStruct;
            
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.newMediaObject";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * Get a list of recent posts for a category
     *
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param numposts Number of Posts to Retrieve
     * @throws XmlRpcException
     * @return
     */
    public Object getRecentPosts(String blogid, String userid, String password,
            int numposts) throws Exception {
        
        mLogger.debug("getRecentPosts() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("     Number: " + numposts);
        
        Weblog website = validate(blogid, userid,password);
        
        try {
            Vector<Object> results = new Vector<>();
            
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager weblogMgr = roller.getWeblogEntryManager();
            if (website != null) {
                WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
                wesc.setWeblog(website);
                wesc.setSortBy(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME);
                wesc.setMaxResults(numposts);
                List<WeblogEntry> entries = weblogMgr.getWeblogEntries(wesc);

                for (WeblogEntry entry : entries) {
                    results.addElement(createPostStruct(entry, userid));
                }
            }
            return results;
            
        } catch (Exception e) {
            String msg = "ERROR in MetaWeblogAPIHandler.getRecentPosts";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    private Hashtable<String, Object> createPostStruct(WeblogEntry entry, String userid) {
        
        String permalink =
            WebloggerRuntimeConfig.getAbsoluteContextURL() + entry.getPermaLink();
        
        Hashtable<String, Object> struct = new Hashtable<>();
        struct.put("title", entry.getTitle());
        if (entry.getLink() != null) {
            struct.put("link", Utilities.escapeHTML(entry.getLink()));
        }
        struct.put("description", entry.getText());
        if (entry.getPubTime() != null) {
            struct.put("pubDate", entry.getPubTime());
            struct.put("dateCreated", entry.getPubTime());
        }
        struct.put("guid", Utilities.escapeHTML(permalink));
        struct.put("permaLink", Utilities.escapeHTML(permalink));
        struct.put("postid", entry.getId());
        
        struct.put("userid", entry.getCreator().getUserName());
        struct.put("author", entry.getCreator().getEmailAddress());
       
        if ( entry.getCategory() != null ) {
            Vector<Object> catArray = new Vector<>();
            catArray.addElement(entry.getCategory().getName());
            struct.put("categories", catArray);

        } else {
            mLogger.warn("Entry " + entry.getId() + " has null category");
        }

        return struct;
    }
    
    
    private Hashtable<String, String> createCategoryStruct(WeblogCategory category, String userid) {
        
        Hashtable<String, String> struct = new Hashtable<>();
        struct.put("title", category.getName());
        struct.put("description", category.getName());
        
        Weblogger roller = WebloggerFactory.getWeblogger();
        URLStrategy strategy = roller.getUrlStrategy();
        
        String catUrl = strategy.getWeblogCollectionURL(category.getWeblog(),
        		null, category.getName(), null, null, 0, true);
        struct.put("htmlUrl", catUrl);
        
        String rssUrl = strategy.getWeblogFeedURL(category.getWeblog(),
               null, "entries", "rss", category.getName(), null, null, false, true);
        struct.put("rssUrl",rssUrl);
        
        return struct;
    }
    
}
