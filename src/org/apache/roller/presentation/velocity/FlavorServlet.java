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
package org.apache.roller.presentation.velocity;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.apache.roller.RollerException;
import org.apache.roller.presentation.RollerRequest;
import java.io.IOException;
import java.util.Date;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;

/////////////////////////////////////////////////////////////////////////////
/**
 * <p>Responsible for rendering RSS feeds and other "flavors" of output for a
 * weblog.</p>
 *
 * <p>If Servlet is mapped to <code>/rss</code> and user has defined
 * an RSS override page (i.e. a page named "_rss"), then that Velocity
 * template will be used for rendering.</p>
 *
 * <p>If there is a request parameter named "flavor", then the Velocity
 * template specified by that parameter will be used for rendering. For
 * example if the flavor is "rss092" then the template at classpath
 * "/flavors/rss092.vm" will be used for rendering.</p>
 *
 * <p>Otherwise, the template /flavors/rss.vm" will be used for rendering.</p>
 *
 * <p>Assumes that If-Modified-Since has already been handled.</p>
 *
 * @author David M Johnson
 *
 * @web.servlet name="RssServlet"
 * @web.servlet-mapping url-pattern="/rss/*"
 * @web.servlet-mapping url-pattern="/atom/*"
 * @web.servlet-mapping url-pattern="/flavor/*"
 */
public class FlavorServlet extends VelocityServlet {
    
    static final long serialVersionUID = -2720532269434186051L;
    
    private static Log mLogger = LogFactory.getLog(FlavorServlet.class);
    
    
    public Template handleRequest(HttpServletRequest request,
                                HttpServletResponse response, 
                                Context ctx) 
            throws IOException {
        
        RollerRequest rreq = null;
        Template outty = null;
        
        // first off lets parse the incoming request and validate it
        try {
            PageContext pageContext =
                    JspFactory.getDefaultFactory().getPageContext(
                    this, request,  response, "", true, 8192, true);
            rreq = RollerRequest.getRollerRequest(pageContext);
            
            // This is an ugly hack to fix the following bug:
            // ROL-547: "Site wide RSS feed is your own if you are logged in"
            String[] pathInfo = StringUtils.split(rreq.getPathInfo(),"/");
            if (pathInfo.length < 1) {
                // If website not specified in URL, set it to null
                rreq.setWebsite(null);
            }
        } catch (RollerException e) {
            
            // An error initializing the request is considered to be a 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("DisplayException", e);
            
            return null;
        }
        
        
        // request appears to be valid, lets render
        try {
            // get update time before loading context
            // TODO: this should really be handled elsewhere
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            String catname = request.getParameter(RollerRequest.WEBLOGCATEGORYNAME_KEY);
            Date updateTime = wmgr.getWeblogLastPublishTime(rreq.getWebsite(), catname);
            request.setAttribute("updateTime", updateTime);
            
            ContextLoader.setupContext(ctx, rreq, response);
            
            String useTemplate;
            PageModel pageModel = (PageModel)ctx.get("pageModel");
            if (request.getServletPath().endsWith("rss")) {
                if (pageModel.getPageByName("_rss") != null)
                    // If the request specified the "/rss" mapping and the
                    // user has defined an RSS override page, we will use that.
                    useTemplate = pageModel.getPageByName("_rss").getId();
                else
                    useTemplate = "/flavors/rss.vm";
            } else if (request.getServletPath().endsWith("atom")) {
                if (pageModel.getPageByName("_atom") != null)
                    // If the request specified the "/atom" mapping and the
                    // user has defined an Atom override page, we will use that.
                    useTemplate = pageModel.getPageByName("_atom").getId();
                else
                    useTemplate = "/flavors/atom.vm";
            } else if (request.getParameter("flavor") != null) {
                // If request specifies a "flavor" then use that.
                String flavor = request.getParameter("flavor");
                useTemplate = "/flavors/" + flavor + ".vm";
            } else {
                // Fall through to default RSS page template.
                useTemplate = "/flavors/rss.vm";
            }
            
            outty = getTemplate(useTemplate);
            
        } catch(ResourceNotFoundException rnfe) {
            
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("DisplayException", rnfe);
            mLogger.warn("ResourceNotFound: "+ request.getRequestURL());
            mLogger.debug(rnfe);
        } catch(Exception e) {
            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("DisplayException", e);
            mLogger.error("Unexpected exception", e);
        }
        
        return outty;
    }
    
    //------------------------------------------------------------------------
    /**
     * Handle error in Velocity processing.
     */
    protected void error( HttpServletRequest req, HttpServletResponse res,
            Exception e) throws ServletException, IOException {
        
        // this means there was an exception outside of the handleRequest()
        // method which seems to always be some variant of SocketException
        // so we just ignore it
        
        // make sure anyone downstream knows about the exception
        req.setAttribute("DisplayException", e);
    }
}

