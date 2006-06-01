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

package org.apache.roller;

import java.sql.Timestamp;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ThemeManager;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.PlanetConfigData;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.RollerPropertyData;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

/**
 * Base class for Roller backend unit test.
 * Responsible for creating the appropriate Roller implementation.
 * Provides setup methods to create website, user, blog entries, bookmarks, etc.
 */
public abstract class RollerTestBase extends TestCase {
    public static Log mLogger =
            LogFactory.getFactory().getInstance(RollerTestBase.class);
    
    private Roller mRoller = null;
    
    /** Simple user created */
    protected UserData mUser = null;
    /** User name of simple user */
    protected String testUsername = "testuser";
    /** Simple website created (no entryes, categories, bookmarks, etc.) */
    protected WebsiteData mWebsite = null;
    
    /** Full websites created, each with entries, cats, bookmarks, etc. */
    protected List mWebsitesCreated = new LinkedList();
    
    /** Users created, user X has permissions in full website X */
    protected List mUsersCreated = new ArrayList();
    
    /** Number of full website/users to create */
    protected int mBlogCount = 3;
    /** Number of categories to create in each category of tree. */
    protected int mCatCount = 2;
    /** Depth of created category tree. */
    protected int mCatDepth = 2;
    /** Number of weblog entries per category, half will be published status */
    protected int mEntriesPerCatCount = 6; // 3 of 6 published
    /** Number of comments to creaate per weblog entry */
    protected int mCommentCount = 2;
    
    /** Total number of entries created */
    protected int mExpectedEntryCount =
            mEntriesPerCatCount +
            mEntriesPerCatCount*mCatCount +
            mEntriesPerCatCount*(mCatCount*mCatDepth);
    
    /** Total number of entries created in published status */
    protected int mExpectedPublishedEntryCount =
            (int)(mEntriesPerCatCount*0.5) +
            (int)(mEntriesPerCatCount*0.5)*mCatCount +
            (int)(mEntriesPerCatCount*0.5)*(mCatCount*mCatDepth);
    
    /** Store categories for use in asserts. */
    protected List mCategoriesCreated = new ArrayList();
    /** Store entries for use in asserts. */
    protected List mEntriesCreated = new ArrayList();
    /** Store comments for use in asserts. */
    protected List mCommentsCreated = new ArrayList();
    
    /** Used to walk back through time as entries are created, one per day. */
    protected Calendar mCalendar = null;
    
    //------------------------------------------------------------------------
    public RollerTestBase() {
        super();
    }
    
    //------------------------------------------------------------------------
    public RollerTestBase(String name) {
        super(name);
    }
    
    //------------------------------------------------------------------------
    /**
     * @see TestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();
        String FS = File.separator;
        RollerConfig.setContextRealPath(".." + FS + "roller");
        RollerConfig.setUploadsDir("." + FS + "roller_dat" + FS + "uploads");       
        
        mUser = createUser(
                testUsername,
                "password",
                "TestUser",
                "testuser@example.com");
        UserManager umgr = getRoller().getUserManager();
        mWebsite = (WebsiteData)umgr.getWebsites(mUser, null, null, null, null, 0, -1).get(0);
        
        PropertiesManager propmgr = getRoller().getPropertiesManager();
        Map props = propmgr.getProperties();
        RollerPropertyData prop =
                (RollerPropertyData)props.get("site.absoluteurl");
        prop.setValue("http://localhost:8080/roller");
        propmgr.saveProperties(props);
        
        PlanetManager planet = getRoller().getPlanetManager();
        PlanetConfigData config = config = new PlanetConfigData();
        config.setCacheDir("");
        config.setTitle("Test");
        config.setAdminEmail("admin@example.com");
        planet.saveConfiguration(config);
        
        PlanetGroupData group = new PlanetGroupData();
        group.setHandle("external");
        group.setTitle("external");
        planet.saveGroup(group);
    }
    
    //-----------------------------------------------------------------------
    protected UserData createUser(
            String username,
            String password,
            String fullName,
            String email) throws RollerException {
        UserManager umgr = getRoller().getUserManager();
        WeblogManager wmgr = getRoller().getWeblogManager();
        
        // Create and add new new user
        UserData ud = new UserData(null,
                username,      // userName
                password,      // password
                fullName,      // fullName
                email,         // emailAddress
                "en_US_WIN",
                "America/Los_Angeles",
                new java.util.Date(), // dateCreated
                Boolean.TRUE);
        umgr.addUser(ud);
        
        WebsiteData website = new WebsiteData(
                    username,              // handle
                    ud,                // creator
                    username,              // name
                    username,         // description
                    "dummy@example.com",               // emailAddress
                    "",             // emailFrom
                    "basic",        // theme
                    "en_US_WIN",    // locale
                    "America/Los_Angeles" // timezone
                    );
        
        ThemeManager themeMgr = getRoller().getThemeManager();
        Theme usersTheme = themeMgr.getTheme(website.getEditorTheme());
        themeMgr.saveThemePages(website, usersTheme);
        
        return ud;
    }
    
    //-----------------------------------------------------------------------
    /** If you use this, call tearDownTestWeblogs() */
    public void setUpTestWeblogs() throws Exception {
        UserManager umgr = getRoller().getUserManager();
        WeblogManager wmgr = getRoller().getWeblogManager();
        
        // Loop to create weblogs
        for (int i=0; i<mBlogCount; i++) {
            
            UserData ud = createUser(
                    "testuser"+i,         // userName
                    "password",           // password
                    "Test User #"+i,      // fullName
                    "test"+i+"@test.com"  // emailAddress
                    );
            ud.setEnabled(new Boolean(i%2 == 0)); // half of users are disabled
            WebsiteData website = (WebsiteData)umgr.getWebsites(ud, null, null, null, null, 0, -1).get(0);
            mWebsitesCreated.add(website);
            mUsersCreated.add(ud);
            
            mLogger.debug("Created user "+ud.getUserName());
            
            // ensure that the first weblog entry created is the newest
            mCalendar = Calendar.getInstance();
            mCalendar.setTime(new Date());
            
            // create categories
            website  = umgr.getWebsite(website.getId());
            WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(website);
            createCategoryPostsAndComments(0, wmgr, ud, website, rootCat);
            
        }
        
        // commit all the objects
    }
    
    private void createCategoryPostsAndComments(
            int depth,
            WeblogManager wmgr,
            UserData user,
            WebsiteData website,
            WeblogCategoryData rootCat) throws RollerException {
        Calendar commentCalendar = Calendar.getInstance();
        
        Timestamp day;
        WeblogEntryData wd = null;
        if (depth == 0) {
            // roll calendar forward one day
            mCalendar.roll(Calendar.DATE, true);
            day = new Timestamp(mCalendar.getTime().getTime());
            day.setNanos(0); // kludge
            wd = new WeblogEntryData(
                    null,      // id
                    rootCat,    // category
                    website,    // websiteId
                    user,
                    "Future Blog", // title
                    null,
                    "Blog to the Future", // text
                    null,      // anchor
                    day,        // pubTime
                    day,        // updateTime
                    WeblogEntryData.PUBLISHED ); // publishEntry
            wmgr.saveWeblogEntry(wd);
            
            // roll calendar back to today
            mCalendar.roll(Calendar.DATE, false);
        }
        
        // create entries under the category passed in
        for ( int k=0; k<mEntriesPerCatCount; k++ ) {
            day = new Timestamp(mCalendar.getTime().getTime());
            day.setNanos(0); // kludge
            
            boolean published = k%2==0 ? true : false;
            String status = published
                    ? WeblogEntryData.PUBLISHED : WeblogEntryData.DRAFT;
            
            wd = new WeblogEntryData(
                    null,      // id
                    rootCat,    // category
                    website,    // websiteId
                    user,
                    rootCat.getName() + ":entry"+k, // title
                    null,
                    rootCat.getName() + ":entry"+k, // text
                    null,      // anchor
                    day,        // pubTime
                    day,        // updateTime
                    status ); // publishEntry
            wmgr.saveWeblogEntry(wd);
            
            // add at beginning of list
            mEntriesCreated.add(0, wd);
            
            Timestamp now = wd.getPubTime();
            for ( int l=0; l<mCommentCount; l++ ) {
                // need to seperate comments in time
                // it took alot of trial & error to get this working!
                commentCalendar.setTime(now);
                commentCalendar.add(Calendar.HOUR, l);
                now = new Timestamp(commentCalendar.getTime().getTime());
                CommentData comment = new CommentData();
                comment.setWeblogEntry(wd);
                comment.setName("name"+l);
                comment.setEmail("test"+l+"@test.com");
                comment.setContent("This is my comment");
                comment.setPostTime(now);
                comment.setApproved(Boolean.TRUE);
                comment.setPending(Boolean.FALSE);
                comment.setSpam(Boolean.FALSE);
                comment.setNotify(Boolean.FALSE);
                wmgr.saveComment(comment);
                mCommentsCreated.add(comment);
                mLogger.debug("         Created comment ["
                        +comment.getId()+"]"+ comment.getName());
            }
            
            mCalendar.add(Calendar.DATE, -1);
        }
        
        // create categories under the category passed in
        for ( int j=0; j<mCatCount; j++ ) {
            WeblogCategoryData cat = new WeblogCategoryData(
                    null,                           // id
                    website,                       // website
                    rootCat,                       // parent
                    rootCat.getName()+"-cat"+j,    // name
                    "desc",                        // description
                    null );                       // image
            wmgr.saveWeblogCategory(cat);
            mCategoriesCreated.add(cat);
            mLogger.debug("   Created cat ["+cat.getId()+"]"+cat.getName());
            
            if (depth < mCatDepth) {
                createCategoryPostsAndComments(depth+1, wmgr, user, website, cat);
            }
        }
    }
    
    //-----------------------------------------------------------------------
    /** Tear down weblogs created in setupTestWeblogs() */
    public void tearDownTestWeblogs() throws Exception {
        UserManager umgr = getRoller().getUserManager();
        for (Iterator siteIter = mWebsitesCreated.iterator(); siteIter.hasNext();) {
            WebsiteData site = (WebsiteData) siteIter.next();
            site = umgr.getWebsite(site.getId());
            if (site != null) {
                umgr.removeWebsite(site);
            }
        }
        
        for (Iterator userIter = mUsersCreated.iterator(); userIter.hasNext();) {
            UserData user = (UserData) userIter.next();
            user = umgr.getUser(user.getId());
            if (user != null) umgr.removeUser(user);
        }
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Child TestCases should take care to tearDown() their own resources
     * (including their own implementation).  RollerTestBase will clean up the
     * getRoller() instance.
     *
     * @see TestCase#tearDown()
     */
    public void tearDown() throws Exception {
        try {
            PlanetManager planet = getRoller().getPlanetManager();
            PlanetConfigData config = planet.getConfiguration();
            PlanetGroupData group = planet.getGroup("external");
            planet.deleteGroup(group);
            
            deleteWebsite(testUsername);
        } catch (RollerException e) {
            mLogger.error("Tearing down",e);
        }
        super.tearDown();
    }
    
    /**
     * Delete the website created for this test.
     *
     * @throws RollerException
     */
    private void deleteWebsite(String deleteMe) throws RollerException {
        mLogger.debug("try to delete " + deleteMe);
        UserManager umgr = getRoller().getUserManager();
        
        UserData user = umgr.getUserByUserName(deleteMe);
        
        WebsiteData website = (WebsiteData)umgr.getWebsites(user, null, null, null, null, 0, -1).get(0);
        umgr.removeWebsite(website);
        
        umgr.removeUser(user);
        
    }
    
    //------------------------------------------------------------------------
    /**
     * Get Roller implementation to be used in tests.
     */
    public Roller getRoller() throws RollerException {
        if ( mRoller == null ) {
            mRoller = RollerFactory.getRoller();
        }
        return mRoller;
    }
}
