package org.roller.presentation.atomapi04;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import org.roller.RollerTestBase;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.MockRollerContext;
import org.roller.presentation.atomapi.AtomCollection;
import org.roller.presentation.atomapi.AtomService;
import org.roller.presentation.atomapi.AtomServlet;
import org.roller.presentation.atomapi.WSSEUtilities;
import org.roller.util.Utilities;

import com.mockrunner.mock.web.ActionMockObjectFactory;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;
import com.mockrunner.struts.ActionTestModule;
import com.sun.syndication.feed.atom.Content;
import com.sun.syndication.feed.atom.Entry;

/**
 * Test new Rome based Atom API implementation.
 * @author David M Johnson
 */
public class AtomServletTest extends RollerTestBase
{    
    private ActionMockObjectFactory mockFactory;
    protected MockRollerContext rollerContext;
    protected ActionTestModule strutsModule;
    private ServletTestModule servletModule;
    private static SimpleDateFormat df =
        new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ssZ" );

    protected WebMockObjectFactory getMockFactory()
    {
        if (mockFactory == null) 
        {
            mockFactory = new ActionMockObjectFactory();
        }
        return mockFactory;
    }
    
    //------------------------------------------------------------------------------
    /** 
     * Test get of introspection URI
     */
    public void testGetIntrospection() throws Exception
    {
        UserData user = (UserData)mUsersCreated.get(0);        
                
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.addHeader("X-WSSE", 
                generateWSSEHeader(user.getUserName(), user.getPassword()));
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo("/" + user.getUserName());
        getServletModule().doGet();

        String output = getServletModule().getOutput();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(output));
        
        AtomService service = AtomService.documentToService(doc);
        assertEquals(1, service.getWorkspaces().size());
    }

    //------------------------------------------------------------------------------
    /** 
     * Test get of entries collection URI
     */
    public void testGetEntriesCollection() throws Exception
    {
        UserData user = (UserData)mUsersCreated.get(0);        
                
        // get entries collection
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.addHeader("X-WSSE", 
                generateWSSEHeader(user.getUserName(), user.getPassword()));
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo("/" + user.getUserName() + "/entries");
        getServletModule().doGet();
        
        // assert that we got 20 entries
        String output = getServletModule().getOutput();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(output));
        AtomCollection col = AtomCollection.documentToCollection(doc);
        assertEquals(20, col.getMembers().size());
        
        // use collection next URI to get next batch of entries
        resetMocks();
        String[] next = Utilities.stringToStringArray(col.getNext(), "/");
        
        mockRequest = getMockFactory().getMockRequest();
        mockRequest.addHeader("X-WSSE", 
            generateWSSEHeader(user.getUserName(), user.getPassword()));
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo(
            "/" + user.getUserName() + "/entries/" + next[next.length-1]);
        getServletModule().doGet();

        // assert that we got another 20 entries
        output = getServletModule().getOutput();
        doc = builder.build(new StringReader(output));
        col = AtomCollection.documentToCollection(doc);
        assertEquals(10, col.getMembers().size());   
    }

    //------------------------------------------------------------------------------
    /** 
     * Test get of entries collection URI with a range
     */
    public void testGetEntriesCollectionRange() throws Exception
    {
        UserData user = (UserData)mUsersCreated.get(0);     
        
        Date end = new Date(); // now 
        Calendar cal = Calendar.getInstance();
        cal.setTime(end);
        cal.add(Calendar.DATE, -30);
        Date start = cal.getTime(); // one day ago
        String startString = df.format(start);
        String endString = df.format(end);
        
        // get entries collection
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.addHeader("X-WSSE", 
            generateWSSEHeader(user.getUserName(), user.getPassword()));
        mockRequest.addHeader("Range",
            "updated=" + startString + "/" + endString);
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo("/" + user.getUserName() + "/entries");
        getServletModule().doGet();
        
        // assert that we got 20 entries
        String output = getServletModule().getOutput();
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(output));
        AtomCollection col = AtomCollection.documentToCollection(doc);
        assertEquals(20, col.getMembers().size());
        
        // use collection next URI to get next batch of entries
        resetMocks();
        String[] next = Utilities.stringToStringArray(col.getNext(), "/");
        
        mockRequest = getMockFactory().getMockRequest();
        mockRequest.addHeader("X-WSSE", 
            generateWSSEHeader(user.getUserName(), user.getPassword()));
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo("/" + user.getUserName() + "/entries");
        mockRequest.setQueryString("Range=updated="+startString+"/"+endString);
        getServletModule().doGet();

        // assert that we got another 20 entries
        output = getServletModule().getOutput();
        doc = builder.build(new StringReader(output));
        col = AtomCollection.documentToCollection(doc);
        assertEquals(20, col.getMembers().size());   
    }

    //------------------------------------------------------------------------------
    /** 
     * Test that GET on the EditURI returns an entry.
     */
    public void testGetEntry() throws Exception
    {
        UserData user = (UserData)mUsersCreated.get(0);
        WebsiteData website = (WebsiteData)
            getRoller().getUserManager().getWebsites(user, null).get(0);
        WeblogEntryData entry = (WeblogEntryData) 
            getRoller().getWeblogManager().getWeblogEntries(
                website, null, null, null, null, new Integer(1)).get(0);

        Entry fetchedEntry = getEntry(user, entry.getId());
        assertEquals(entry.getId(), fetchedEntry.getId());
    }
    
    //------------------------------------------------------------------------------
    /** 
     * Test that POST to the PostURI returns an entry.
     */
    public void testPostEntry() throws Exception
    {
        UserData user = (UserData)mUsersCreated.get(0);        
        
        // Create an entry in a feed, so Rome can handle it
        Content content = new Content();
        content.setMode(Content.ESCAPED);
        content.setValue("test entry text");
        List contents = new ArrayList();
        contents.add(content);

        Entry entry = new Entry();
        entry.setTitle("test entry title");
        entry.setContents(contents);
        
        StringWriter entryWriter = new StringWriter();
        AtomServlet.serializeEntry(entry, entryWriter);
        
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo(user.getUserName() + "/entries/");
        mockRequest.addHeader("X-WSSE", 
                generateWSSEHeader(user.getUserName(), user.getPassword()));
        mockRequest.setBodyContent(entryWriter.toString());
        getServletModule().doPost();

        String output = getServletModule().getOutput();
        Entry returnedEntry = AtomServlet.parseEntry(new StringReader(output));        
        assertEquals(returnedEntry.getTitle(), entry.getTitle());
        
        MockHttpServletResponse mockResponse = getMockFactory().getMockResponse();
        assertEquals(HttpServletResponse.SC_CREATED, mockResponse.getStatusCode());
        assertTrue(mockResponse.containsHeader("Location"));
       
        getRoller().release();
        resetMocks();

        Entry fetchedEntry = getEntry(user, returnedEntry.getId());
        assertEquals(returnedEntry.getId(), fetchedEntry.getId());
    }
    
    //------------------------------------------------------------------------------
    /** 
     * Test that PUT on the EditURI updates an entry.
     */
    public void testPutEntry() throws Exception
    {
        UserData user = (UserData)mUsersCreated.get(0);
        WebsiteData website = (WebsiteData)
            getRoller().getUserManager().getWebsites(user, null).get(0);        
        
        WeblogEntryData entry = (WeblogEntryData) 
            getRoller().getWeblogManager().getWeblogEntries(
                website, null, null, null, null, new Integer(1)).get(0);
        
        // Fetch that entry using Atom
        Entry fetchedEntry = getEntry(user, entry.getId());
        assertEquals(entry.getId(), fetchedEntry.getId());
        
        // Make a change to the fetched entry
        fetchedEntry.setTitle("TEST TITLE");
        
        // Use Atom PUT to update the entry
        StringWriter entryWriter = new StringWriter();
        AtomServlet.serializeEntry(fetchedEntry, entryWriter);
        MockHttpServletRequest mockRequest2 = getMockFactory().getMockRequest();
        mockRequest2.setContextPath("/atom");
        mockRequest2.setPathInfo(user.getUserName() + "/entry/" + entry.getId());
        mockRequest2.addHeader("X-WSSE", 
           generateWSSEHeader(user.getUserName(), user.getPassword()));
        mockRequest2.setBodyContent(entryWriter.toString());
        getServletModule().doPut();      
        
        getRoller().release();
        resetMocks();

        // Get the entry again to make sure the update was made
        Entry fetchedEntry2 = getEntry(user, entry.getId());
        assertEquals(fetchedEntry.getTitle(), fetchedEntry2.getTitle());
    }
    
    //------------------------------------------------------------------------------
    /** 
     * Test that DELETE on EditURI deletes entry. 
     */
    public void testDeleteEntry() throws Exception
    {
        UserData user = (UserData)mUsersCreated.get(0);
        WebsiteData website = (WebsiteData)
            getRoller().getUserManager().getWebsites(user, null).get(0);        

        WeblogEntryData entry = (WeblogEntryData) 
            getRoller().getWeblogManager().getWeblogEntries(
                website, null, null, null, null, new Integer(1)).get(0);

        Entry fetchedEntry = getEntry(user, entry.getId());
        assertEquals(entry.getId(), fetchedEntry.getId());

        // Use Atom DELETE to delete the entry
        MockHttpServletRequest mockRequest2 = getMockFactory().getMockRequest();
        mockRequest2.setContextPath("/atom");
        mockRequest2.setPathInfo(user.getUserName() + "/entry/" + entry.getId());
        mockRequest2.addHeader("X-WSSE", 
                generateWSSEHeader(user.getUserName(), user.getPassword()));
        getServletModule().doDelete();      
        getRoller().release();
        resetMocks();
        try 
        {
            getEntry(user, entry.getId()).getId();
            fail(); // no exception was thrown!
        }
        catch (Exception expected) {}
    }

     //------------------------------------------------------------------------------
    private Entry getEntry(UserData user, String id) throws Exception
    {
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo(user.getUserName() + "/entry/" + id);
        mockRequest.addHeader("X-WSSE", 
                generateWSSEHeader(user.getUserName(), user.getPassword()));
        getServletModule().doGet();

        String output = getServletModule().getOutput();
        return AtomServlet.parseEntry(new StringReader(output));        
    }
    
    //------------------------------------------------------------------------------
    public void testPostResource() throws Exception 
    {    
        UserData user = (UserData)mUsersCreated.get(0);      
        
        // read test file into byte array
        String fileName = "rssbadge.gif";
        File testFile = new File(fileName);
        FileInputStream fis = new FileInputStream(testFile);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Utilities.copyInputToOutput(fis, baos);
        
        // post file as resource
        MockHttpServletRequest mockRequest = getMockFactory().getMockRequest();
        mockRequest.setContextPath("/atom");
        mockRequest.setPathInfo(user.getUserName() + "/resources/");
        mockRequest.addHeader("Name", fileName); 
        mockRequest.addHeader("X-WSSE", 
                generateWSSEHeader(user.getUserName(), user.getPassword()));        
        mockRequest.setBodyContent(baos.toByteArray());
        getServletModule().doPost();
        
        MockHttpServletResponse mockResponse = getMockFactory().getMockResponse();
        assertEquals(HttpServletResponse.SC_CREATED, mockResponse.getStatusCode());
        assertTrue(mockResponse.containsHeader("Location"));
        
        getRoller().release();
        resetMocks();
    }

    //------------------------------------------------------------------------------
    public void setUp() throws Exception
    {
        super.setUp();       
        super.setUpTestWeblogs();        
        resetMocks();
        MockServletContext ctx = getMockFactory().getMockServletContext();
        ctx.setRealPath("/", "");
        rollerContext = new MockRollerContext();
        rollerContext.init(ctx);
    }

    //------------------------------------------------------------------------------
    /** 
     * Really really reset mocks.
     */
    private void resetMocks() 
    {
        mockFactory = null;
        getMockFactory().refresh();
        getMockFactory().getMockRequest().clearParameters();
        getMockFactory().getMockRequest().clearAttributes();
        setServletModule(new ServletTestModule(getMockFactory()));        
        getServletModule().setServlet(
            getServletModule().createServlet(AtomServlet.class));
        getServletModule().clearOutput();
    }
    
    //------------------------------------------------------------------------------
    /**
     * @param servletModule The servletModule to set.
     */
    protected void setServletModule(ServletTestModule servletModule)
    {
        this.servletModule = servletModule;
    }

    //------------------------------------------------------------------------------
    /**
     * @return Returns the servletModule.
     */
    protected ServletTestModule getServletModule()
    {
        return servletModule;
    }   

    //------------------------------------------------------------------------------
    public void tearDown() throws Exception
    {
        super.tearDown();
        super.tearDownTestWeblogs();
    }   
    
    //------------------------------------------------------------------------
    public static Test suite()
    {
        return new TestSuite(AtomServletTest.class);
    }

    //------------------------------------------------------------------------------
    public static String generateWSSEHeader(String username, String password)
        throws Exception
    {  
        byte[] nonceBytes = Long.toString(new Date().getTime()).getBytes();
        String nonce = new String(WSSEUtilities.base64Encode(nonceBytes));
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String created = sdf.format(new Date());
        
        String digest = WSSEUtilities.generateDigest(
                nonceBytes, created.getBytes("UTF-8"), password.getBytes("UTF-8"));
        
        StringBuffer header = new StringBuffer("UsernameToken Username=\"");
        header.append(username);
        header.append("\", ");
        header.append("PasswordDigest=\"");
        header.append(digest);
        header.append("\", ");
        header.append("Nonce=\"");
        header.append(nonce);
        header.append("\", ");
        header.append("Created=\"");
        header.append(created);
        header.append("\"");
        return header.toString();
    }
}
