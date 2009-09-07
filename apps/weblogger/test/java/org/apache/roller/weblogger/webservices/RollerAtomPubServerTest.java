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

package org.apache.roller.weblogger.webservices;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;

import com.sun.syndication.feed.WireFeed;
import com.sun.syndication.feed.atom.Feed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.WireFeedInput;
import com.sun.syndication.propono.atom.server.AtomServlet;
import java.io.InputStream;
import java.io.StringReader;
import java.sql.Timestamp;
import org.apache.commons.codec.binary.Base64;

import java.util.Date;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.MockRollerContext;


/**
 * Test AtomPub servlet via mock request, context, etc.
 * @author Dave Johnson
 */
public class RollerAtomPubServerTest extends TestCase {
    public static Log log = LogFactory.getLog(RollerAtomPubServerTest.class);
    User testUser = null;
    Weblog testWeblog = null;
    
    protected WebMockObjectFactory   mockFactory;
    protected MockRollerContext      rollerContext;
    protected MockHttpServletRequest mockRequest;
    protected ServletTestModule      servletTestModule;

    
    //-----------------------------------------------------------------------

    /**
     * Set up Servlet mocks and test data
     */
    public void setUp() throws Exception {

        try {
            TestUtils.setupWeblogger();

            mockFactory = new WebMockObjectFactory();

            // create mock RollerContext
            MockServletContext ctx = mockFactory.getMockServletContext();

            ctx.setRealPath("/", ".");
            ctx.setInitParameter(
                "contextConfigLocation", "/WEB-INF/security.xml");

            ctx.setResourceAsStream("/WEB-INF/security.xml",
                this.getClass().getResourceAsStream("/WEB-INF/security.xml"));
            ctx.setResourceAsStream("/WEB-INF/velocity.properties",
                this.getClass().getResourceAsStream("/WEB-INF/velocity.properties"));

            rollerContext = new MockRollerContext();
            rollerContext.init(ctx);

            // setup mock request
            mockRequest = mockFactory.getMockRequest();
            mockRequest.setContextPath("/roller");

            // create MockRunner test module
            servletTestModule = new ServletTestModule(mockFactory);
            servletTestModule.createServlet(AtomServlet.class);


            // create test data

            testUser = TestUtils.setupUser("entryTestUser");
            testWeblog = TestUtils.setupWeblog("entryTestWeblog", testUser);
            TestUtils.endSession(true);


            testWeblog = TestUtils.getManagedWebsite(testWeblog);

            createWeblogEntry(testWeblog, testUser,
                    "Test Entry #1", "Content for test entry #1");

            createWeblogEntry(testWeblog, testUser,
                    "Test Entry #2", "Content for test entry #2");

            createWeblogEntry(testWeblog, testUser,
                    "Test Entry #3", "Content for test entry #3");

            TestUtils.endSession(true);

            MediaFileManager fm = WebloggerFactory.getWeblogger().getMediaFileManager();
            MediaFileDirectory root = fm.getMediaFileRootDirectory(testWeblog);

            root.getId();

            testWeblog = TestUtils.getManagedWebsite(testWeblog);

            createMediaFile(testWeblog, "p47-thunderbolt.jpg",
                "P47 Thunderbolt", "image/png", root,
                getClass().getResourceAsStream("/uploadsdir/testblog2/p47-thunderbolt.jpg"));

            createMediaFile(testWeblog, "amsterdam.jpg",
                "View of Amsterdam", "image/jpeg", root,
                getClass().getResourceAsStream("/uploadsdir/testblog2/amsterdam.jpg"));

            TestUtils.endSession(true);

        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
        }
    }


    /**
     * Test get recent posts
     */
    public void testAtomPubGetRecentPosts() {
        try {
            mockRequest.setPathInfo("/entryTestWeblog/entries");

            String creds = testUser.getUserName() +":"+ testUser.getPassword();
            mockRequest.setHeader("Authorization",
                "Basic " + new String(new Base64().encode((creds).getBytes())));

            servletTestModule.doGet();

            MockHttpServletResponse response = mockFactory.getMockResponse();
            WireFeedInput input = new WireFeedInput();
            WireFeed wireFeed = input.build(
                    new StringReader(response.getOutputStreamContent()));

            Feed feed = (Feed)wireFeed;
            assertEquals(3, feed.getEntries().size());

        } catch (IllegalArgumentException ex) {
            log.debug("error fetching or parsing feed", ex);
            fail();
        } catch (FeedException ex) {
            log.debug("error fetching or parsing feed", ex);
            fail();
        }
    }


    /**
     * Test get recent posts
     */
    public void testAtomPubGetRecentMediaFiles() {
        try {
            mockRequest.setPathInfo("/entryTestWeblog/resources");

            String creds = testUser.getUserName() +":"+ testUser.getPassword();
            mockRequest.setHeader("Authorization",
                "Basic " + new String(new Base64().encode((creds).getBytes())));

            servletTestModule.doGet();

            MockHttpServletResponse response = mockFactory.getMockResponse();
            WireFeedInput input = new WireFeedInput();
            WireFeed wireFeed = input.build(
                    new StringReader(response.getOutputStreamContent()));

            Feed feed = (Feed)wireFeed;
            assertEquals(2, feed.getEntries().size());

    } catch (IllegalArgumentException ex) {
            log.debug("error fetching or parsing feed", ex);
            fail();
        } catch (FeedException ex) {
            log.debug("error fetching or parsing feed", ex);
            fail();
        }
    }


    public void tearDown() throws Exception {
        
        try {
            TestUtils.teardownWeblog(testWeblog.getId());
            TestUtils.teardownUser(testUser.getUserName());
            TestUtils.endSession(true);
            
            mockRequest = null;
            servletTestModule.clearOutput();
            servletTestModule.releaseFilters();
            servletTestModule = null;
            rollerContext = null;
            mockFactory = null;
        
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test teardown failed", ex);
        }
    }


    public static Test suite() {
        return new TestSuite(RollerAtomPubServerTest.class);
    }


    private void createWeblogEntry(Weblog weblog, User user,
            String title, String text) throws WebloggerException {
        
        WeblogEntry testEntry1 = new WeblogEntry();
        testEntry1.setTitle(title);
        testEntry1.setText(text);
        testEntry1.setPubTime(new Timestamp(new Date().getTime()));
        testEntry1.setUpdateTime(new Timestamp(new Date().getTime()));
        testEntry1.setWebsite(weblog);
        testEntry1.setCreatorUserName(user.getUserName());
        testEntry1.setCategory(weblog.getDefaultCategory());
        WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(testEntry1);
    }


    private void createMediaFile(
            Weblog weblog, String name, String desc, String contentType,
            MediaFileDirectory parent, InputStream is) throws WebloggerException {

        MediaFile mediaFile = new MediaFile();
        mediaFile.setName(name);
        mediaFile.setDescription(desc);
        mediaFile.setSharedForGallery(false);
        mediaFile.setDirectory(parent);
        mediaFile.setWeblog(weblog);
        mediaFile.setContentType(contentType);
        mediaFile.setInputStream(is);
        WebloggerFactory.getWeblogger().getMediaFileManager().createMediaFile(weblog, mediaFile);
    }

}
