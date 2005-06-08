package org.roller.presentation.atom;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.servlet.ServletTestModule;

import org.osjava.atom4j.pojo.Content;
import org.osjava.atom4j.pojo.Entry;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.ServletTestBase;

import java.sql.Timestamp;

import javax.servlet.http.HttpServletResponse;

/**
 * This class tests interations with RollerAtomServlet
 * which require authentication.
 * 
 * Created on Mar 3, 2004
 *
 * @author lance.lavandowska
 */
public class AtomServletTest extends ServletTestBase
{    
    protected MockHttpServletRequest mkRequest;
    protected ServletTestModule servletTestModule;

    //-----------------------------------------------------------------------
    public void testPostEntry() throws Exception
    {
        String username = authenticateUser();

        // set pathinfo
        mkRequest.setPathInfo("/" + username + "/entry");

        // set payload of new Atom Entry
        Entry atomEntry = newEntry();
        mkRequest.setBodyContent(atomEntry.toString());

        servletTestModule.doPost();

        MockHttpServletResponse response = mockFactory.getMockResponse();

        // make sure it didn't return an error
        //mLogger.debug("ResponseCode:" + response.getStatusCode());
        assertFalse("Returned SC_BAD_REQUEST (Error)\n" + servletTestModule.getOutput(),
                    HttpServletResponse.SC_BAD_REQUEST == response.getStatusCode());
        assertTrue("Did not return SC_SEE_OTHER status",
                   HttpServletResponse.SC_SEE_OTHER == response.getStatusCode());

        // test that AtomServlet returned a proper Location header
        String locationHeader = response.getHeader("Location");
        assertNotNull("Should return a Location header", locationHeader);
        String locWanted = "/atom/" + username + "/entry/";
        int locIndex = locationHeader.indexOf(locWanted);
        if (locIndex == -1)
        {
            fail("Location header does not contain URL of new Entry.");
        }

        // test that the Entry was persisted
        String entryId = locationHeader.substring(locIndex+locWanted.length());
        assertNotNull("New Entry Id not returned", entryId);

        WeblogEntryData rEntry = getRoller().getWeblogManager().retrieveWeblogEntry(entryId);
        assertNotNull("New Entry not found in database", rEntry);

        assertEquals("Entry titles don't match",
                     atomEntry.getTitle().getText(), rEntry.getTitle());

        rEntry = null;
        response = null;
    }

    //-----------------------------------------------------------------------
    public void testPutEntry() throws Exception
    {
        String newTitle = "Entry edited with Atom";
        String testAnchor = "test_new_entry";
        String username = authenticateUser();
        
        // create an Entry for to be updated
        WeblogCategoryData category = mWebsite.getBloggerCategory();
        //getRoller().begin();
        WeblogEntryData rEntry = new WeblogEntryData(
            (String)null, category, mWebsite, "Test New Entry", "", 
            "This is a test", testAnchor, 
            new Timestamp(new java.util.Date().getTime()), 
            (Timestamp)null, Boolean.FALSE);
        rEntry.save();
        getRoller().commit();
        //getRoller().begin();
        rEntry = getRoller().getWeblogManager().getWeblogEntryByAnchor(mWebsite, testAnchor);

        // set pathinfo
        mkRequest.setPathInfo("/" + username + "/entry/" + rEntry.getId());

        // set payload of Atom Entry, with changed title
        Entry atomEntry = AtomAssistant.convertToAtomEntry(rEntry, true);
        atomEntry.getTitle().setText(newTitle);
        mkRequest.setBodyContent(atomEntry.toString());

        servletTestModule.doPut();

        MockHttpServletResponse response = mockFactory.getMockResponse();

        // make sure it didn't return an error
        //mLogger.debug("ResponseCode:" + response.getStatusCode());
        assertFalse("Returned SC_BAD_REQUEST (Error)\n" + servletTestModule.getOutput(),
                    HttpServletResponse.SC_BAD_REQUEST == response.getStatusCode());
        assertTrue("Did not return SC_RESET_CONTENT status",
                   HttpServletResponse.SC_RESET_CONTENT == response.getStatusCode());
        
        // refetch Entry from db, confirm title changed
        rEntry = getRoller().getWeblogManager().retrieveWeblogEntry(rEntry.getId());
        assertEquals(newTitle, rEntry.getTitle());
    }

    //-----------------------------------------------------------------------
    /**
     * Create a new Atom Entry
     * @return
     */
    private Entry newEntry()
    {
        Entry atomEntry = new Entry();
        atomEntry.setTitle( new Content() );
        atomEntry.getTitle().setText("Atom Test Post");
        //atomEntry.setIssued( weblogEntry.getPubTime() ); // TODO: should test set pubTime

        atomEntry.setContent( new Content() );
        atomEntry.getContent().setText( "And all the world's a stage." );
        atomEntry.getContent().setMimeType( "application/xhtml+xml" );
        atomEntry.getContent().setLanguage( "en-us" );

        return atomEntry;
    }

    //-----------------------------------------------------------------------
    public void setUp() throws Exception
    {
        super.setUp();
        
        mkRequest = mockFactory.getMockRequest();
        
        servletTestModule = new ServletTestModule(mockFactory);
        servletTestModule.createServlet(RollerAtomServlet.class);
    }

    //-----------------------------------------------------------------------
    public void tearDown() throws Exception
    {
        mkRequest = null;
        servletTestModule.clearOutput();
        servletTestModule.releaseFilters();
        servletTestModule = null;
        super.tearDown();
    }
}
