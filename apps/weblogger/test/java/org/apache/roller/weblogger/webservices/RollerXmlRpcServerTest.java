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
 * Created on Jun 15, 2004
 */
package org.apache.roller.weblogger.webservices;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;

import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.TestUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.MockRollerContext;
import org.apache.roller.util.RegexUtil;
import org.apache.xmlrpc.webserver.XmlRpcServlet;


/**
 * Makes calls to the RollerXmlRpcServer, which should handle a
 * post just as it would with a real XML-RPC call.
 *
 * @author lance.lavandowska
 */
public class RollerXmlRpcServerTest extends TestCase {
    public static Log log = LogFactory.getLog(RollerXmlRpcServerTest.class);
    User testUser = null;
    Weblog testWeblog = null;
    private static HashMap typeMap = new HashMap();
    static {
        typeMap.put(Boolean.class, "boolean");
        typeMap.put(Double.class, "double");
        typeMap.put(Date.class, "dateTime.iso8601");
        typeMap.put(Integer.class, "int");
    }
    
    protected WebMockObjectFactory mockFactory;
    protected MockRollerContext rollerContext;
    protected MockHttpServletRequest mockRequest;
    protected ServletTestModule servletTestModule;
    
    public void testBloggerGetRecentPosts() {
        ArrayList params = new ArrayList();
        params.add("roller"); // appkey
        params.add(testWeblog.getHandle()); // blogid
        params.add(testUser.getUserName()); // userid
        params.add(testUser.getPassword()); // password
        params.add(new Integer(5)); // numposts
        String message = buildXmlRpcString("blogger.getRecentPosts", params);
        
        mockRequest.setBodyContent(message);
        servletTestModule.doPost();
        MockHttpServletResponse response = mockFactory.getMockResponse();
        String responseBody = response.getOutputStreamContent();
        
        // assert no fault code
        assertTrue(responseBody,
            responseBody.indexOf("<name>faultCode</name>") == -1);
        
        // make sure all/any userids returned belong to our test user
        Pattern userPattern =
                Pattern.compile("<name>userid</name><value>(.*?)</value>");
        ArrayList users = RegexUtil.getMatches(userPattern, responseBody, 1);
        Iterator it = users.iterator();
        while (it.hasNext()) {
            String user = (String)it.next();
            //System.out.println(user);
            if (user.equals(testUser.getUserName())) {
                continue;
            } else {
                fail("getRecentPosts() returned entry for a user ["
                        + user + "] other than " + testUser.getUserName());
            }
        }
    }
    
    /**
     * Build an XML-RPC message from methodName and params.
     *
     * @param methodName
     * @param params
     * @return
     */
    private String buildXmlRpcString(String methodName, ArrayList params) {
        StringBuffer buf = new StringBuffer("<?xml version=\"1.0\"?>");
        buf.append("<methodCall>");
        buf.append("<methodName>").append(methodName).append("</methodName>");
        buf.append("<params>");
        Iterator it = params.iterator();
        while (it.hasNext()) {
            buf.append("<param><value>");
            Object param = it.next();
            String paramType = (String)typeMap.get(param.getClass());
            if (paramType != null) {
                buf.append("<").append(paramType).append(">")
                .append(param)
                .append("</").append(paramType).append(">");
            } else {
                buf.append("<string>").append(param).append("</string>");
            }
            buf.append("</value></param>");
        }
        buf.append("</params> ");
        buf.append("</methodCall>");
        return buf.toString();
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * All tests in this suite require a user and a weblog.
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

            servletTestModule = new ServletTestModule(mockFactory);
            servletTestModule.createServlet(XmlRpcServlet.class);
        
            testUser = TestUtils.setupUser("entryTestUser");
            testWeblog = TestUtils.setupWeblog("entryTestWeblog", testUser);
            

            // create test data
            
            WeblogEntry testEntry1 = new WeblogEntry();
            testEntry1.setTitle("entryTestEntry1");
            testEntry1.setLink("testEntryLink1");
            testEntry1.setText("blah blah entry1");
            testEntry1.setAnchor("testEntryAnchor1");
            testEntry1.setPubTime(new Timestamp(new Date().getTime()));
            testEntry1.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry1.setWebsite(testWeblog);
            testEntry1.setCreatorUserName(testUser.getUserName());
            testEntry1.setCategory(testWeblog.getDefaultCategory());
            WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(testEntry1);

            WeblogEntry testEntry2 = new WeblogEntry();
            testEntry2.setTitle("entryTestEntry2");
            testEntry2.setLink("testEntryLink2");
            testEntry2.setText("blah blah entry2");
            testEntry2.setAnchor("testEntryAnchor2");
            testEntry2.setPubTime(new Timestamp(new Date().getTime()));
            testEntry2.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry2.setWebsite(testWeblog);
            testEntry2.setCreatorUserName(testUser.getUserName());
            testEntry2.setCategory(testWeblog.getDefaultCategory());
            WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(testEntry2);

            WeblogEntry testEntry3 = new WeblogEntry();
            testEntry3.setTitle("entryTestEntry3");
            testEntry3.setLink("testEntryLink3");
            testEntry3.setText("blah blah entry3");
            testEntry3.setAnchor("testEntryAnchor3");
            testEntry3.setPubTime(new Timestamp(new Date().getTime()));
            testEntry3.setUpdateTime(new Timestamp(new Date().getTime()));
            testEntry3.setWebsite(testWeblog);
            testEntry3.setCreatorUserName(testUser.getUserName());
            testEntry3.setCategory(testWeblog.getDefaultCategory());           
            WebloggerFactory.getWeblogger().getWeblogEntryManager().saveWeblogEntry(testEntry3);

            TestUtils.endSession(true);
            
        } catch (Exception ex) {
            log.error(ex);
            throw new Exception("Test setup failed", ex);
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
        return new TestSuite(RollerXmlRpcServerTest.class);
    }

}
