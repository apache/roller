/*
 * RollerXMLRPCServlet.java
 */

package org.roller.presentation.xmlrpc;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlrpc.XmlRpcServer;

import java.io.OutputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Roller's XML RPC Servlet sets up XmlRpcHandler for Blogger/ API.
 * 
 * @author David M Johnson
 * 
 * @web.servlet name="RollerXMLRPCServlet"
 * @web.servlet-mapping url-pattern="/xmlrpc"
 */
public class RollerXMLRPCServlet extends HttpServlet
{
    static final long serialVersionUID = -4424719615968330852L;
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(RollerXMLRPCServlet.class);
        
    private transient XmlRpcServer mXmlRpcServer = new XmlRpcServer();
    private BloggerAPIHandler mBloggerHandler = null;
    private MetaWeblogAPIHandler mMetaWeblogHandler = null;

    //------------------------------------------------------------------------
    
    /** 
     * Initializes the servlet.
     */
    public void init(ServletConfig config) throws ServletException
    {
        super.init(config);
        try
        {
            mBloggerHandler = new BloggerAPIHandler();
            mXmlRpcServer.addHandler("blogger", mBloggerHandler);
            
            mMetaWeblogHandler = new MetaWeblogAPIHandler();
            mXmlRpcServer.addHandler("metaWeblog", mMetaWeblogHandler);
        }
        catch (Exception e)
        {
            mLogger.error("Initialization of XML-RPC servlet failed", e);
        }
    }

    //------------------------------------------------------------------------
    
    protected void service(HttpServletRequest request,
                                  HttpServletResponse response)
        throws ServletException, java.io.IOException
    {
        byte[] result = mXmlRpcServer.execute(request.getInputStream());

        response.setContentType("text/xml");
        response.setContentLength(result.length);

        OutputStream output = response.getOutputStream();
        output.write(result);
        output.flush();
    }
}
