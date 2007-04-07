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

package org.apache.roller.webservices.xmlrpc;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.util.Utilities;
import org.apache.xmlrpc.XmlRpcException;

/**
 * Roller XML-RPC Handler for the Blogger v1 API.
 *
 * Blogger API spec can be found at http://plant.blogger.com/api/index.html
 * See also http://xmlrpc.free-conversant.com/docs/bloggerAPI
 *
 * @author David M Johnson
 */
public class BloggerAPIHandler extends BaseAPIHandler {
    
    static final long serialVersionUID = 2398898776655115019L;
    
    private static Log mLogger = LogFactory.getLog(BloggerAPIHandler.class);
    
    public BloggerAPIHandler() {
        super();
    }
    
    
    /**
     * Delete a Post
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param publish Ignored
     * @throws XmlRpcException
     * @return
     */
    public boolean deletePost(String appkey, String postid, String userid,
            String password, boolean publish) throws Exception {
        
        mLogger.debug("deletePost() Called =====[ SUPPORTED ]=====");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);
        
        Roller roller = RollerFactory.getRoller();
        WeblogManager weblogMgr = roller.getWeblogManager();
        WeblogEntryData entry = weblogMgr.getWeblogEntry(postid);
        
        // Return false if entry not found
        if (entry == null) return false;
        
        validate(entry.getWebsite().getHandle(), userid, password);
        
        try {
            // delete the entry
            weblogMgr.removeWeblogEntry(entry);
            roller.flush();
            
            // notify cache
            flushPageCache(entry.getWebsite());
        } catch (Exception e) {
            String msg = "ERROR in blogger.deletePost: "+e.getClass().getName();
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
        
        return true;
    }
    
    
    /**
     * Edits the main index template of a given blog. Roller only support
     * updating the main template, the default template of your weblog.
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param template The text for the new template (usually mostly HTML).
     * @param templateType Determines which of the blog's templates is to be set.
     * @throws XmlRpcException
     * @return
     */
    public boolean setTemplate(String appkey, String blogid, String userid,
            String password, String templateData,
            String templateType) throws Exception {
        
        mLogger.debug("setTemplate() Called =====[ SUPPORTED ]=====");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("   Template: " + templateData);
        mLogger.debug("       Type: " + templateType);
        
        validate(blogid, userid, password);
        
        if (! templateType.equals("main")) {
            throw new XmlRpcException(
                    UNKNOWN_EXCEPTION, "Roller only supports main template");
        }
        
        try {
            Roller roller = RollerFactory.getRoller();
            UserManager userMgr = roller.getUserManager();
            
            WeblogTemplate page = userMgr.getPage(templateType);
            page.setContents(templateData);
            userMgr.savePage(page);
            flushPageCache(page.getWebsite());
            
            return true;
        } catch (RollerException e) {
            String msg = "ERROR in BlooggerAPIHander.setTemplate";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION,msg);
        }
    }
    
    
    /**
     * Returns the main or archive index template of a given blog
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param templateType Determines which of the blog's templates will be returned. Currently, either "main" or "archiveIndex"
     * @throws XmlRpcException
     * @return
     */
    public String getTemplate(String appkey, String blogid, String userid,
            String password, String templateType)
            throws Exception {
        
        mLogger.debug("getTemplate() Called =====[ SUPPORTED ]=====");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("       Type: " + templateType);
        
        validate(blogid, userid,password);
        
        try {
            Roller roller = RollerFactory.getRoller();
            UserManager userMgr = roller.getUserManager();
            WeblogTemplate page = userMgr.getPage(templateType);
            
            if ( null == page ) {
                throw new XmlRpcException(UNKNOWN_EXCEPTION,"Template not found");
            } else {
                return page.getContents();
            }
        } catch (Exception e) {
            String msg = "ERROR in BlooggerAPIHander.getTemplate";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION,msg);
        }
    }
    
    
    /**
     * Authenticates a user and returns basic user info (name, email, userid, etc.)
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @throws XmlRpcException
     * @return
     */
    public Object getUserInfo(String appkey, String userid, String password)
    throws Exception {
        
        mLogger.debug("getUserInfo() Called =====[ SUPPORTED ]=====");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     UserId: " + userid);
        
        validateUser(userid, password);
        
        try {
            Roller roller = RollerFactory.getRoller();
            UserManager userMgr = roller.getUserManager();
            UserData user = userMgr.getUserByUserName(userid);
            
            // parses full name into two strings, firstname and lastname
            String firstname = "", lastname = "";
            StringTokenizer toker = new StringTokenizer(user.getFullName());
            
            if (toker.hasMoreTokens()) {
                firstname = toker.nextToken();
            }
            
            while (toker.hasMoreTokens()) {
                if ( !lastname.equals("") ) {
                    lastname += " ";
                }
                lastname += toker.nextToken();
            }
            
            // populates user information to return as a result
            Hashtable result = new Hashtable();
            result.put("nickname", user.getUserName());
            result.put("userid", user.getUserName());
            result.put("email", "");
            result.put("lastname", lastname);
            result.put("firstname", firstname);
            
            return result;
        } catch (RollerException e) {
            String msg = "ERROR in BlooggerAPIHander.getInfo";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION,msg);
        }
    }
    
    
    /**
     * Returns information on all the blogs a given user is a member of
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @throws XmlRpcException
     * @return
     */
    public Object getUsersBlogs(String appkey, String userid, String password)
    throws Exception {
        
        mLogger.debug("getUsersBlogs() Called ===[ SUPPORTED ]=======");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     UserId: " + userid);
        
        Vector result = new Vector();
        if (validateUser(userid, password)) {
            try {
                String contextUrl = RollerRuntimeConfig.getAbsoluteContextURL();
                
                UserManager umgr = RollerFactory.getRoller().getUserManager();
                UserData user = umgr.getUserByUserName(userid);
                // get list of user's enabled websites
                List websites = umgr.getWebsites(user, Boolean.TRUE, null, null, null, 0, -1);
                Iterator iter = websites.iterator();
                while (iter.hasNext()) {
                    WebsiteData website = (WebsiteData)iter.next();
                    Hashtable blog = new Hashtable(3);
                    blog.put("url", website.getURL());
                    blog.put("blogid", website.getHandle());
                    blog.put("blogName", website.getName());
                    result.add(blog);
                }
            } catch (Exception e) {
                String msg = "ERROR in BlooggerAPIHander.getUsersBlogs";
                mLogger.error(msg,e);
                throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
            }
        }
        return result;
    }
    
    
    /**
     * Edits a given post. Optionally, will publish the blog after making the edit
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param postid Unique identifier of the post to be changed
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param content Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws XmlRpcException
     * @return
     */
    public boolean editPost(String appkey, String postid, String userid,
            String password, String content, boolean publish)
            throws Exception {
        
        mLogger.debug("editPost() Called ========[ SUPPORTED ]=====");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     PostId: " + postid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);
        mLogger.debug("     Content:\n " + content);
        
        if (validateUser(userid, password)) {
            try {
                Timestamp current = new Timestamp(System.currentTimeMillis());
                
                Roller roller = RollerFactory.getRoller();
                WeblogManager weblogMgr = roller.getWeblogManager();
                WeblogEntryData entry = weblogMgr.getWeblogEntry(postid);
                entry.setText(content);
                entry.setUpdateTime(current);
                if (Boolean.valueOf(publish).booleanValue()) {
                    entry.setStatus(WeblogEntryData.PUBLISHED);
                } else {
                    entry.setStatus(WeblogEntryData.DRAFT);
                }
                
                // save the entry
                weblogMgr.saveWeblogEntry(entry);
                roller.flush();
                
                // notify cache
                flushPageCache(entry.getWebsite());
                
                return true;
            } catch (Exception e) {
                String msg = "ERROR in BlooggerAPIHander.editPost";
                mLogger.error(msg,e);
                throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
            }
        }
        return false;
    }
    
    
    /**
     * Makes a new post to a designated blog. Optionally, will publish the blog after making the post
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param content Contents of the post
     * @param publish If true, the blog will be published immediately after the post is made
     * @throws XmlRpcException
     * @return
     */
    public String newPost(String appkey, String blogid, String userid,
            String password, String content, boolean publish)
            throws Exception {
        
        mLogger.debug("newPost() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("    Publish: " + publish);
        mLogger.debug("    Content:\n " + content);
        
        WebsiteData website = validate(blogid, userid, password);
        
        // extract the title from the content
        String title = "";
        
        if (content.indexOf("<title>") != -1) {
            title =
                    content.substring(content.indexOf("<title>") + 7,
                    content.indexOf("</title>"));
            content = StringUtils.replace(content, "<title>"+title+"</title>", "");
        }
        if (StringUtils.isEmpty(title)) {
            title = Utilities.truncateNicely(content, 15, 15, "...");
        }
        
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager weblogMgr = roller.getWeblogManager();
            
            Timestamp current = new Timestamp(System.currentTimeMillis());
            
            WeblogEntryData entry = new WeblogEntryData();
            entry.setTitle(title);
            entry.setText(content);
            entry.setPubTime(current);
            entry.setUpdateTime(current);
            UserData user = roller.getUserManager().getUserByUserName(userid);
            entry.setCreator(user);
            entry.setWebsite(website);
            entry.setCategory(website.getBloggerCategory());
            entry.setCommentDays(new Integer(website.getDefaultCommentDays()));
            if (Boolean.valueOf(publish).booleanValue()) {
                entry.setStatus(WeblogEntryData.PUBLISHED);
            } else {
                entry.setStatus(WeblogEntryData.DRAFT);
            }
            
            // save the entry
            weblogMgr.saveWeblogEntry(entry);
            roller.flush();
            
            // notify cache
            flushPageCache(entry.getWebsite());
            
            return entry.getId();
        } catch (Exception e) {
            String msg = "ERROR in BlooggerAPIHander.newPost";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
    
    /**
     * This method was added to the Blogger 1.0 API via an Email from Evan
     * Williams to the Yahoo Group bloggerDev, see the email message for details -
     * http://groups.yahoo.com/group/bloggerDev/message/225
     *
     * @param appkey Unique identifier/passcode of the application sending the post
     * @param blogid Unique identifier of the blog the post will be added to
     * @param userid Login for a Blogger user who has permission to post to the blog
     * @param password Password for said username
     * @param numposts Number of Posts to Retrieve
     * @throws XmlRpcException
     * @return Vector of Hashtables, each containing dateCreated, userid, postid, content
     */
    public Object getRecentPosts(String appkey, String blogid, String userid,
            String password, int numposts)
            throws Exception {
        
        mLogger.debug("getRecentPosts() Called ===========[ SUPPORTED ]=====");
        mLogger.debug("     Appkey: " + appkey);
        mLogger.debug("     BlogId: " + blogid);
        mLogger.debug("     UserId: " + userid);
        mLogger.debug("     Number: " + numposts);
        
        WebsiteData website = validate(blogid, userid,password);
        
        try {
            Vector results = new Vector();
            
            Roller roller = RollerFactory.getRoller();
            WeblogManager weblogMgr = roller.getWeblogManager();
            if (website != null) {
                Map entries = weblogMgr.getWeblogEntryObjectMap(
                        website,                // website
                        null,                   // startDate
                        new Date(),             // endDate
                        null,                   // catName
                        null,                   // tags
                        null, null, 0, -1);
                
                Iterator iter = entries.values().iterator();
                while (iter.hasNext()) {
                    ArrayList list = (ArrayList) iter.next();
                    Iterator i = list.iterator();
                    while (i.hasNext()) {
                        WeblogEntryData entry = (WeblogEntryData) i.next();
                        Hashtable result = new Hashtable();
                        if (entry.getPubTime() != null) {
                            result.put("dateCreated", entry.getPubTime());
                        }
                        result.put("userid", userid);
                        result.put("postid", entry.getId());
                        result.put("content", entry.getText());
                        results.add(result);
                    }
                }
            }
            return results;
        } catch (Exception e) {
            String msg = "ERROR in BlooggerAPIHander.getRecentPosts";
            mLogger.error(msg,e);
            throw new XmlRpcException(UNKNOWN_EXCEPTION, msg);
        }
    }
    
}
