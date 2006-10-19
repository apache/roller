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
/*
 * TestUtils.java
 *
 * Created on April 6, 2006, 8:38 PM
 */

package org.apache.roller;

import org.apache.roller.model.AutoPingManager;
import org.apache.roller.model.PingTargetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;


/**
 * Utility class for unit test classes.
 */
public final class TestUtils {
    
    
    /**
     * Convenience method that simulates the end of a typical session.
     *
     * Normally this would be triggered by the end of the response in the webapp
     * but for unit tests we need to do this explicitly.
     *
     * @param flush true if you want to flush changes to db before releasing
     */
    public static void endSession(boolean flush) throws Exception {
        
        if(flush) {
            RollerFactory.getRoller().flush();
        }
        
        RollerFactory.getRoller().release();
    }
    
    
    /**
     * Convenience method that creates a user and stores it.
     */
    public static UserData setupUser(String username) throws Exception {
        
        UserData testUser = new UserData();
        testUser.setUserName(username);
        testUser.setPassword("password");
        testUser.setFullName("Test User");
        testUser.setEmailAddress("TestUser@dev.null");
        testUser.setLocale("en_US");
        testUser.setTimeZone("America/Los_Angeles");
        testUser.setDateCreated(new java.util.Date());
        testUser.setEnabled(Boolean.TRUE);
        
        // store the user
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        mgr.addUser(testUser);
        
        // query for the user to make sure we return the persisted object
        UserData user = mgr.getUserByUserName(username);
        
        if(user == null)
            throw new RollerException("error inserting new user");
        
        return user;
    }
    
    
    /**
     * Convenience method for removing a user.
     */
    public static void teardownUser(String id) throws Exception {
        
        // lookup the user
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        UserData user = mgr.getUser(id);
        
        // remove the user
        mgr.removeUser(user);
    }
    
    
    /**
     * Convenience method that creates a weblog and stores it.
     */
    public static WebsiteData setupWeblog(String handle, UserData creator) throws Exception {
        
        WebsiteData testWeblog = new WebsiteData();
        testWeblog.setName("Test Weblog");
        testWeblog.setDescription("Test Weblog");
        testWeblog.setHandle(handle);
        testWeblog.setEmailAddress("testweblog@dev.null");
        testWeblog.setEditorPage("editor-text.jsp");
        testWeblog.setBlacklist("");
        testWeblog.setEmailFromAddress("");
        testWeblog.setEditorTheme("basic");
        testWeblog.setLocale("en_US");
        testWeblog.setTimeZone("America/Los_Angeles");
        testWeblog.setDateCreated(new java.util.Date());
        testWeblog.setCreator(creator);
        
        // add weblog
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        mgr.addWebsite(testWeblog);
        
        // query for the new weblog and return it
        WebsiteData weblog = mgr.getWebsiteByHandle(handle);
        
        if(weblog == null)
            throw new RollerException("error setting up weblog");
        
        return weblog;
    }
    
    
    /**
     * Convenience method for removing a weblog.
     */
    public static void teardownWeblog(String id) throws Exception {
        
        // lookup the weblog
        UserManager mgr = RollerFactory.getRoller().getUserManager();
        WebsiteData weblog = mgr.getWebsite(id);
        
        // remove the weblog
        mgr.removeWebsite(weblog);
    }
 
    
    /**
     * Convenience method for creating a weblog entry.
     */
    public static WeblogEntryData setupWeblogEntry(String anchor,
                                                   WeblogCategoryData cat,
                                                   WebsiteData weblog,
                                                   UserData user)
            throws Exception {
        
        WeblogEntryData testEntry = new WeblogEntryData();
        testEntry.setTitle(anchor);
        testEntry.setLink("testEntryLink");
        testEntry.setText("blah blah entry");
        testEntry.setAnchor(anchor);
        testEntry.setPubTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testEntry.setUpdateTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testEntry.setStatus(WeblogEntryData.PUBLISHED);
        testEntry.setWebsite(weblog);
        testEntry.setCreator(user);
        testEntry.setCategory(cat);
        
        // store entry
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        mgr.saveWeblogEntry(testEntry);
        
        // query for object
        WeblogEntryData entry = mgr.getWeblogEntry(testEntry.getId());
        
        if(entry == null)
            throw new RollerException("error setting up weblog entry");
        
        return entry;
    }
    
    
    /**
     * Convenience method for removing a weblog entry.
     */
    public static void teardownWeblogEntry(String id) throws Exception {
        
        // lookup the entry
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        WeblogEntryData entry = mgr.getWeblogEntry(id);
        
        // remove the entry
        mgr.removeWeblogEntry(entry);
    }
    
    
    /**
     * Convenience method for creating a comment.
     */
    public static CommentData setupComment(String content, WeblogEntryData entry)
            throws Exception {
        
        CommentData testComment = new CommentData();
        testComment.setName("test");
        testComment.setEmail("test");
        testComment.setUrl("test");
        testComment.setRemoteHost("foofoo");
        testComment.setContent("this is a test comment");
        testComment.setPostTime(new java.sql.Timestamp(new java.util.Date().getTime()));
        testComment.setWeblogEntry(entry);
        testComment.setPending(Boolean.FALSE);
        testComment.setApproved(Boolean.TRUE);
        
        // store testComment
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        mgr.saveComment(testComment);
        
        // query for object
        CommentData comment = mgr.getComment(testComment.getId());
        
        if(comment == null)
            throw new RollerException("error setting up comment");
        
        return comment;
    }
    
    
    /**
     * Convenience method for removing a comment.
     */
    public static void teardownComment(String id) throws Exception {
        
        // lookup the comment
        WeblogManager mgr = RollerFactory.getRoller().getWeblogManager();
        CommentData comment = mgr.getComment(id);
        
        // remove the comment
        mgr.removeComment(comment);
    }
    
    
    /**
     * Convenience method for creating a ping target.
     */
    public static PingTargetData setupPingTarget(String name, String url) 
            throws Exception {
        
        PingTargetData testPing = new PingTargetData();
        testPing.setName("testCommonPing");
        testPing.setPingUrl("http://localhost/testCommonPing");
        
        // store ping
        PingTargetManager pingMgr = RollerFactory.getRoller().getPingTargetManager();
        pingMgr.savePingTarget(testPing);
        
        // query for it
        PingTargetData ping = pingMgr.getPingTarget(testPing.getId());
        
        if(ping == null)
            throw new RollerException("error setting up ping target");
        
        return ping;
    }
    
    
    /**
     * Convenience method for removing a ping target.
     */
    public static void teardownPingTarget(String id) throws Exception {
        
        // query for it
        PingTargetManager pingMgr = RollerFactory.getRoller().getPingTargetManager();
        PingTargetData ping = pingMgr.getPingTarget(id);
        
        // remove the ping
        pingMgr.removePingTarget(ping);
    }
    
    
    /**
     * Convenience method for creating an auto ping.
     */
    public static AutoPingData setupAutoPing(PingTargetData ping, WebsiteData weblog)
            throws Exception {
        
        AutoPingManager mgr = RollerFactory.getRoller().getAutopingManager();
        
        // store auto ping
        AutoPingData autoPing = new AutoPingData(null, ping, weblog);
        mgr.saveAutoPing(autoPing);
        
        // query for it
        autoPing = mgr.getAutoPing(autoPing.getId());
        
        if(autoPing == null)
            throw new RollerException("error setting up auto ping");
        
        return autoPing;
    }
    
    
    /**
     * Convenience method for removing an auto ping.
     */
    public static void teardownAutoPing(String id) throws Exception {
        
        // query for it
        AutoPingManager mgr = RollerFactory.getRoller().getAutopingManager();
        AutoPingData autoPing = mgr.getAutoPing(id);
        
        // remove the auto ping
        mgr.removeAutoPing(autoPing);
    }
    
}
