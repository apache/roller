package org.roller.business;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

///////////////////////////////////////////////////////////////////////////////
/**
 * Some example code in the form of a test.
 */
public class WebsiteTest extends TestCase
{
    public WebsiteTest(String name)
    {
        super(name);
    }
    public void testWebsiteLifecycle() throws Exception
    {
        Roller roller = RollerFactory.getRoller();
        UserManager umgr = roller.getUserManager();
        WeblogManager wmgr = roller.getWeblogManager();

        roller.begin(UserData.SYSTEM_USER);
        
        // Create and add new new user
        UserData user = new UserData(null,
            "testuser",             // userName
            "testuser",             // password
            "Test User",            // fullName
            "testuser@example.com", // emailAddress
            "en_US_WIN",            // locale
            "America/Los_Angeles",  // timeZone
            new java.util.Date(),   // dateCreated
            Boolean.TRUE);          // enabled
        umgr.addUser(user);

        // Create list of pages to be loaded into website
        Map pages = new HashMap();
        pages.put("Weblog","Weblog page content");
        pages.put("_day","Day page content");
        pages.put("css","CSS page content");
        
        // Create website for user with those pages
        WebsiteData website = umgr.createWebsite(
           user, pages, "testuser", "testuser", "testuser","dummy@example.com","basic", 
           "en_US_WIN", "America/Los_Angeles");
        
        Timestamp day = new Timestamp(new Date().getTime());
        WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(website);
        WeblogEntryData entry = new WeblogEntryData(
                null,          // id
                rootCat,       // category
                website,       // website
                user,          // creator
                "Title of post in category " + rootCat.getName(), // title
                null,          // 
                "Text of post in category " + rootCat.getName(), // text
                null,          // anchor
                day,           // pubTime
                day,           // updateTime
                WeblogEntryData.PUBLISHED); // publishEntry */
        entry.save();
        
        roller.commit();
        
        roller.begin(UserData.SYSTEM_USER);
        
        // remove user
        website = umgr.retrieveWebsite(website.getId());
        website.remove();

        // remove site
        user = umgr.retrieveUser(user.getId());
        user.remove();
        
        roller.commit();
    }
    public static Test suite() 
    {
        return new TestSuite(WebsiteTest.class);
    }    
}
