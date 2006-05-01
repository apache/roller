/*
 * Created on Jun 15, 2004
 */
package org.apache.roller.presentation.xmlrpc;

import com.mockrunner.mock.web.MockHttpServletRequest;
import com.mockrunner.mock.web.MockHttpServletResponse;
import com.mockrunner.mock.web.MockServletContext;
import com.mockrunner.mock.web.WebMockObjectFactory;
import com.mockrunner.servlet.ServletTestModule;

import org.apache.roller.RollerTestBase;
import org.apache.roller.presentation.MockRollerContext;
import org.apache.roller.presentation.RollerRequest;
import org.apache.roller.presentation.webservices.xmlrpc.RollerXMLRPCServlet;
import org.apache.roller.util.RegexUtil;
 
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Makes calls to the RollerXmlRpcServer, which should handle a
 * post just as it would with a real XML-RPC call.
 * 
 * @author lance.lavandowska
 */
public class RollerXmlRpcServerTest extends RollerTestBase
{
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

    public void testBloggerGetRecentPosts()
    {
        ArrayList params = new ArrayList();
        params.add("roller"); // appkey
        params.add("testuser0"); // blogid
        params.add("testuser0"); // userid
        params.add("password"); // password
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
        while (it.hasNext()) 
        {
            String user = (String)it.next();
            //System.out.println(user);
            if (user.equals("testuser0"))
            {
                continue;
            }
            else
            {
                fail("getRecentPosts() returned entry for a user [" 
                        + user + "] other than " + testUsername);
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
    private String buildXmlRpcString(String methodName, ArrayList params)
    {
        StringBuffer buf = new StringBuffer("<?xml version=\"1.0\"?>");
        buf.append("<methodCall>");
        buf.append("<methodName>").append(methodName).append("</methodName>");
        buf.append("<params>");
        Iterator it = params.iterator();
        while (it.hasNext()) {
            buf.append("<param><value>");
            Object param = it.next();
            String paramType = (String)typeMap.get(param.getClass());
            if (paramType != null)
            {
                buf.append("<").append(paramType).append(">")
                   .append(param)
                   .append("</").append(paramType).append(">");                    
            }
            else
            {    
                buf.append("<string>").append(param).append("</string>");
            }
            buf.append("</value></param>");
        }
        buf.append("</params> ");
        buf.append("</methodCall>");
        return buf.toString();
    }
    
    //-----------------------------------------------------------------------
    public void setUp() throws Exception
    {
        // must do super.setup() before creating MockRollerContext
        super.setUp();
        setUpTestWeblogs();

        mockFactory = new WebMockObjectFactory();

        // create mock RollerContext
        MockServletContext ctx = mockFactory.getMockServletContext();
        ctx.setRealPath("/", ".");
        rollerContext = new MockRollerContext();
        rollerContext.init(ctx);

        mockRequest = mockFactory.getMockRequest();
        mockRequest.setContextPath("/roller");
        RollerRequest.getRollerRequest(
                mockRequest, mockFactory.getMockServletContext());

        servletTestModule = new ServletTestModule(mockFactory);
        servletTestModule.createServlet(RollerXMLRPCServlet.class);
    }

    //-----------------------------------------------------------------------
    public void tearDown() throws Exception
    {
        super.tearDown();
        mockRequest = null;
        servletTestModule.clearOutput();
        servletTestModule.releaseFilters();
        servletTestModule = null;
        rollerContext = null;
        mockFactory = null;
    }

    public static Test suite() 
    {
        return new TestSuite(RollerXmlRpcServerTest.class);
    }
}