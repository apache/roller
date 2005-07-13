
package org.roller;

import java.sql.Timestamp;
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
import org.roller.business.BookmarkManagerTest;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.CommentData;
import org.roller.pojos.PermissionsData;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

/**
 * Base class for Roller backend unit test.
 * Responsible for creating the appropriate Roller implementation.
 * Provides setup methods to create website, user, blog entries, bookmarks, etc.
 */
public abstract class RollerTestBase extends TestCase
{
    public static Log mLogger =
        LogFactory.getFactory().getInstance(BookmarkManagerTest.class);

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
    protected int mBlogCount = 2;
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
    public RollerTestBase()
    {
        super();
    }

    //------------------------------------------------------------------------
    public RollerTestBase(String name)
    {
        super(name);
    }

    //------------------------------------------------------------------------
    /**
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
        getRoller().begin(UserData.SYSTEM_USER);
        mUser = createUser(
                   testUsername,
                   "password",
                   "TestUser",
                   "testuser@example.com");
        UserManager umgr = getRoller().getUserManager();
        mWebsite = (WebsiteData)umgr.getWebsites(mUser, null).get(0);
        getRoller().commit();
    }

    //-----------------------------------------------------------------------
    protected UserData createUser(
                    String username,
                    String password,
                    String fullName,
                    String email) throws RollerException
    {
        UserManager umgr = getRoller().getUserManager();
        WeblogManager wmgr = getRoller().getWeblogManager();

        // Create and add new new user
        UserData ud = new UserData(null,
            username,      // userName
            password,      // password
            fullName,      // fullName
            email,         // emailAddress
            new java.util.Date(), // dateCreated
            Boolean.TRUE);
        umgr.addUser(ud);

        // Create list of pages to be loaded into website
        Map pages = new HashMap();
        pages.put("Weblog","Weblog page content");
        pages.put("_day","Day page content");
        pages.put("css","CSS page content");
        
        // Create website for user with those pages
        umgr.createWebsite(
           ud, pages, "basic", "en_US_WIN", "America/Los_Angeles");
        
        return ud;
    }

    //-----------------------------------------------------------------------
    /** If you use this, call tearDownTestWeblogs() */
    public void setUpTestWeblogs() throws Exception
    {
        UserManager umgr = getRoller().getUserManager();
        WeblogManager wmgr = getRoller().getWeblogManager();

        // Loop to create weblogs
        for (int i=0; i<mBlogCount; i++)
        {
            getRoller().begin(UserData.SYSTEM_USER);

            UserData ud = createUser(
                "testuser"+i,         // userName
                "password",           // password
                "Test User #"+i,      // fullName
                "test"+i+"@test.com"  // emailAddress
                );
            ud.setIsEnabled(new Boolean(i%2 == 0)); // half of users are disabled
            WebsiteData website = (WebsiteData)umgr.getWebsites(ud, null).get(0);
            mWebsitesCreated.add(website);
            mUsersCreated.add(ud);

            getRoller().commit();

            mLogger.debug("Created user "+ud.getUserName());

            // ensure that the first weblog entry created is the newest
            mCalendar = Calendar.getInstance();
            mCalendar.setTime(new Date());

            // create categories
            getRoller().begin(UserData.SYSTEM_USER);
            website  = umgr.retrieveWebsite(website.getId());
            WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(website);
            createCategoryPostsAndComments(0, wmgr, ud, website, rootCat);
            getRoller().commit();
        }

        // commit all the objects
    }

    private void createCategoryPostsAndComments(
            int depth,
            WeblogManager wmgr,
            UserData user,
            WebsiteData website,
            WeblogCategoryData rootCat) throws RollerException
    {
        Calendar commentCalendar = Calendar.getInstance();

        Timestamp day;
        WeblogEntryData wd;
        if (depth == 0) 
        {
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
                 Boolean.TRUE ); // publishEntry
            wd.save();
            
            // roll calendar back to today
            mCalendar.roll(Calendar.DATE, false);
        }
        
        // create entries under the category passed in
        for ( int k=0; k<mEntriesPerCatCount; k++ )
        {
            day = new Timestamp(mCalendar.getTime().getTime());
            day.setNanos(0); // kludge

            boolean published = k%2==0 ? true : false;

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
                    new Boolean(published) ); // publishEntry
            wd.save();

            // add at beginning of list
            mEntriesCreated.add(0, wd);

            Timestamp now = wd.getPubTime();
            for ( int l=0; l<mCommentCount; l++ )
            {
                // need to seperate comments in time
                // it took alot of trial & error to get this working!
                commentCalendar.setTime(now);
                commentCalendar.add(Calendar.HOUR, l);
                now = new Timestamp(commentCalendar.getTime().getTime());
                CommentData comment = new CommentData(null,
                        wd,                   // entry
                        "name"+l,             // name
                        "test"+l+"@test.com", // email
                        "url"+l,              // url
                        "This is my comment", // content
                        now,                // postTime
                        Boolean.FALSE,      // spam
                        Boolean.FALSE);     // notify
                comment.save();
                mCommentsCreated.add(comment);
                mLogger.debug("         Created comment ["
                        +comment.getId()+"]"+ comment.getName());
            }

            mCalendar.add(Calendar.DATE, -1);
        }

        // create categories under the category passed in
        for ( int j=0; j<mCatCount; j++ )
        {
            WeblogCategoryData cat = wmgr.createWeblogCategory(
                website,                       // website
                rootCat,                       // parent
                rootCat.getName()+"-cat"+j,    // name
                "desc",                        // description
                null );                       // image
            cat.save();
            mCategoriesCreated.add(cat);
            mLogger.debug("   Created cat ["+cat.getId()+"]"+cat.getName());

            if (depth < mCatDepth)
            {
                createCategoryPostsAndComments(depth+1, wmgr, user, website, cat);
            }
        }
    }

    //-----------------------------------------------------------------------
    /** Tear down weblogs created in setupTestWeblogs() */
    public void tearDownTestWeblogs() throws Exception
    {
        getRoller().begin(UserData.SYSTEM_USER);
        UserManager umgr = getRoller().getUserManager();
        for (Iterator siteIter = mWebsitesCreated.iterator(); siteIter.hasNext();)
        {
            WebsiteData site = (WebsiteData) siteIter.next();
            site = umgr.retrieveWebsite(site.getId());
            if (site != null) site.remove();
        }
        
        for (Iterator userIter = mUsersCreated.iterator(); userIter.hasNext();)
        {
            UserData user = (UserData) userIter.next();
            user = umgr.retrieveUser(user.getId());
            if (user != null) user.remove();
        }
        getRoller().commit();
    }

    //------------------------------------------------------------------------

    /**
     * Child TestCases should take care to tearDown() their own resources
     * (including their own implementation).  RollerTestBase will clean up the
     * getRoller() instance.
     *
     * @see TestCase#tearDown()
     */
    public void tearDown() throws Exception
    {
        try
        {
            deleteWebsite(testUsername);
            //getRoller().release();
        }
        catch (RollerException e)
        {
            mLogger.error("Tearing down",e);
        }
        super.tearDown();
    }

    /**
     * Delete the website created for this test.
     *
     * @throws RollerException
     */
    private void deleteWebsite(String deleteMe) throws RollerException
    {
        mLogger.debug("try to delete " + deleteMe);
        getRoller().begin(UserData.SYSTEM_USER);
        UserManager umgr = getRoller().getUserManager();
        UserData user = umgr.getUser(deleteMe);
        WebsiteData website = (WebsiteData)umgr.getWebsites(user, null).get(0);
        website.remove();
        user.remove();
        getRoller().commit();
    }

    //------------------------------------------------------------------------
    /**
     * Get Roller implementation to be used in tests.
     */
    public Roller getRoller() throws RollerException
    {
        if ( mRoller == null )
        {
            mRoller = RollerFactory.getRoller();
        }
        return mRoller;
    }
}
