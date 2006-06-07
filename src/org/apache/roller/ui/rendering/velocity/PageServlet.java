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
package org.apache.roller.ui.rendering.velocity;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.servlet.VelocityServlet;
import org.apache.roller.RollerException;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.velocity.exception.ParseErrorException;


/**
 * The PageServlet handles all requests for weblog pages at /page/*
 * 
 * @web.servlet name="PageServlet" load-on-startup="0"
 * @web.servlet-init-param name="org.apache.velocity.properties" 
 * 		                  value="/WEB-INF/velocity.properties"
 *  
 * //web.servlet-mapping url-pattern="/page/*"
 *
 * NOTE: the webdoclet task allows some elements to be inherited when generating
 *   the web.xml file.  for this reason we can't put the servlet mapping here
 *   because it will be inherited by PreviewServlet.  instead we manually do 
 *   the servlet mapping for PageServlet in the servlet-mappings.xml
 *   file under metadata/xdoclet.
 *
 */ 
public class PageServlet extends VelocityServlet {
    
    private static Log mLogger = LogFactory.getLog(PageServlet.class);
        
    /**
     * Sets servletContext for WebappResourceLoader.
     */
    public void init( ServletConfig config )
        throws ServletException {
        
        super.init( config );
        WebappResourceLoader.setServletContext( getServletContext() );
    }
    
    
    /**
     * Process a request for a Weblog page.
     */
    public Template handleRequest(HttpServletRequest request,
                                HttpServletResponse response, 
                                Context ctx) 
        throws Exception {
        
        Template outty = null;
        RollerRequest rreq = null;
        WebsiteData website = null;
        
        PageContext pageContext =
            JspFactory.getDefaultFactory().getPageContext(
            this, request, response,"", true, 8192, true);

        // first off lets parse the incoming request and validate it
        try {
            rreq = RollerRequest.getRollerRequest(pageContext);
        } catch (Throwable t) {
            // NOTE: indicates real problem, not just a "not found" error
            throw new Exception("ERROR: creating RollerRequest");
        }

        // All pages exist within website, so website MUST be specified
        website = rreq.getWebsite();
        if (website == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
               
        // request appears to be valid, lets render
        try {
            org.apache.roller.pojos.Template page = null;
            
            // If this is a popup request, then deal with it specially
            if(request.getParameter("popup") != null) {
                try {
                    // Does user have a popupcomments page?
                    page = website.getPageByName("_popupcomments");
                } catch(Exception e ) {
                    // ignored ... considered page not found
                }
                
                // User doesn't have one so return the default
                if(page == null) {
                    page = new WeblogTemplate("/popupcomments.vm", website, 
                            "Comments", "Comments", "dummy_link", 
                            "dummy_template", new Date());
                }
                
                rreq.setPage(page);
                
            // If request specified the page, then go with that
            } else if (rreq.getPage() != null) {
                page = rreq.getPage();
                
            // If page not available from request, then use website's default
            } else {
                page = website.getDefaultPage();
                rreq.setPage(page);
            }
            
            // Still no page ID, then we have a problem
            if ( page == null ) {
                throw new ResourceNotFoundException("Page not found");
            }

            // this sets up the page we want to render
            outty = prepareForPageExecution(ctx, rreq, response, page);
            
            // if there is a decorator template then apply it
            if (website != null) {
                // parse/merge Page template
                StringWriter sw = new StringWriter();
                outty.merge(ctx, sw);
                ctx.put("decorator_body", sw.toString());
                
                // replace outty with decorator Template
                outty = findDecorator(website, (String) ctx.get("decorator"));
            }
            
        } catch (ResourceNotFoundException rnfe ) {            
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("DisplayException", rnfe);
            mLogger.warn("ResourceNotFound: "+ request.getRequestURL());
            mLogger.debug(rnfe);
            
        } catch (Throwable e) {            
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("DisplayException", e);
            mLogger.error("ERROR preparing for page execution", e);
        }
        
        return outty;
    }
    
    
    /**
     * Prepare the requested page for execution by setting content type
     * and populating velocity context.
     */
    protected Template prepareForPageExecution(Context ctx,
                                            RollerRequest rreq,
                                            HttpServletResponse response,
                                            org.apache.roller.pojos.Template page) 
            
         throws ResourceNotFoundException, RollerException {
        
        Template outty = null;
        
        // if page has an extension - use that to set the contentType
        String pageLink = page.getLink();
        String mimeType = getServletConfig().getServletContext().getMimeType(pageLink);
        if(mimeType != null) {
            // we found a match ... set the content type
            response.setContentType(mimeType);
        }
        
        // Made it this far, populate the Context
        ContextLoader.setupContext( ctx, rreq, response );
        
        try {
            outty = getTemplate(page.getId(), "UTF-8");
        } 
        catch (ParseErrorException parseError) {            
            // Error at this point indicates error in template, so let's show 
            // it to the template author so they can debug it.
            ctx.put("displayException", parseError);
            try {
                outty = getTemplate("error-page.vm", "UTF-8");
            } catch (Throwable totallyUnexpected) {
                throw new RuntimeException("ERROR parsing Velocity error page");
            }
        } 
        catch (ResourceNotFoundException notFound) {
            // just rethrow
            throw notFound;
        } 
        catch (Exception ex) { 
            // wrap this as a roller exception
            throw new RollerException("ERROR getting velocity template", ex);            
        }        
        return outty;
    }
    
    
    /**
     * Load the decorator template and apply it.  If there is no user specified
     * decorator then the default decorator is applied.
     */
    protected Template findDecorator(WebsiteData website, String decorator_name)
            throws ResourceNotFoundException, RollerException {
        
        Template decorator = null;
        org.apache.roller.pojos.Template decorator_template = null;
        
        // check for user-specified decorator
        if (decorator_name != null) {
            decorator_template = website.getPageByName(decorator_name);
        }
        
        // if no user-specified decorator try default page-name
        if (decorator_template == null) {
            decorator_template = website.getPageByName("_decorator");
        }
        
        // try loading Template
        if (decorator_template != null) {
            try {
                decorator = getTemplate(decorator_template.getId(), "UTF-8");
            } catch (Exception e) {
                // it may not exist, so this is okay
            }
        }
        
        // couldn't find Template, load default "no-op" decorator
        if (decorator == null) {
            try {
                decorator = getTemplate("/themes/noop_decorator.vm", "UTF-8");
            } catch (ResourceNotFoundException ex) {
                // just rethrow
                throw ex;
            } catch (Exception ex) {
                // wrap as a RollerException
                throw new RollerException("error getting no-op decorator", ex);
            }
        }
        
        return decorator;
    }
    
    
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
    
    
    /**
     * Override to prevent Velocity from putting "req" and "res" into the context.
     * Allowing users access to the underlying Servlet objects is a security risk.
     * If need access to request parameters, use $requestParameters.
     */
    protected Context createContext(
            HttpServletRequest req,
            HttpServletResponse res) {
        
        VelocityContext context = new VelocityContext();
        context.put(REQUEST, new RequestWrapper(req.getParameterMap()));
        return context;
        
    }
    
    /** Provide access to request params only, not actual request */
    public static class RequestWrapper {
        Map params = null;
        public RequestWrapper(Map params) {
            this.params = params;
        }
        public String getParameter(String key) {
            String ret = null;
            String[] array = (String[])params.get(key);
            if (array != null && array.length > 0) {
                ret = array[0];
            }
            return ret;
        }
    }
}
