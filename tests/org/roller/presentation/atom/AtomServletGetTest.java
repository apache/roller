package org.roller.presentation.atom;

import com.mockrunner.base.VerifyFailedException;
import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;

import org.osjava.atom4j.pojo.Feed;
import org.roller.RollerException;
import org.roller.business.RollerTestBase;
import org.roller.model.WeblogManager;
import org.roller.pojos.UserData;
import org.roller.presentation.MockRollerContext;
import org.roller.presentation.RollerRequest;

import java.util.Date;
import java.util.List;

/**
 * This class tests GET interations with RollerAtomServlet.
 * 
 * Created on Mar 5, 2004
 * 
 * @author lance.lavandowska
 */
public class AtomServletGetTest extends RollerTestBase
{
    protected WebMockObjectFactory mockFactory;
    protected MockRollerContext rollerContext;
    protected MockHttpServletRequest mkRequest;
    protected ServletTestModule servletTestModule;

    //-----------------------------------------------------------------------
    public void testGetFeed() throws RollerException
    {
        String username = getUser().getUserName();

        // set pathinfo
        mkRequest.setPathInfo("/" + username + "/feed/" + mWebsite.getId());

        servletTestModule.doGet();

        // returns list of Atom Entries
        List entries = getLatestEntries(username,
            RollerRequest.getRollerRequest().getWeblogEntryCount());
        Feed expectedFeed = AtomAssistant.convertToAtomFeed(mWebsite, entries, true);

        try
        {
            // the output will contain XML header junk, so use Contains
            servletTestModule.verifyOutputContains(expectedFeed.toString());
        }
        catch (VerifyFailedException vfe)
        {
            fail("Expected feed not returned");
        }
    }

    //-----------------------------------------------------------------------
    protected UserData getUser()
    {
        return mWebsite.getUser();
    }

    //-----------------------------------------------------------------------
    protected List getLatestEntries(String username, int i) throws RollerException
    {
        return getRoller().getWeblogManager().getWeblogEntries(
                       mWebsite,               // userName
                       null,                  // startDate
                       new Date(),            // endDate
                       null,                  // catName
                       WeblogManager.PUB_ONLY, // status
                       new Integer(RollerRequest.getRollerRequest().getWeblogEntryCount()));
        
    }
    
    //-----------------------------------------------------------------------
    public void setUp() throws Exception
    {
        // must do super.setup() before creating MockRollerContext
        super.setUp();

        mockFactory = new WebMockObjectFactory();

        // create mock RollerContext
        rollerContext = new MockRollerContext();
        rollerContext.init(mockFactory.getMockServletContext());

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
        rollerContext = null;
        mockFactory = null;
        super.tearDown();
    }
}
