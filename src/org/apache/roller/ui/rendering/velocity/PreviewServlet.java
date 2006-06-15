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
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.roller.RollerException;
import org.apache.roller.ThemeNotFoundException;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ThemeManager;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerRequest;


/**
 * Allow users to preview what their blog would look like in a given theme.
 *
 * web.servlet name="PreviewServlet" load-on-startup="1"
 * web.servlet-mapping url-pattern="/preview/*"
 */
public class PreviewServlet extends PageServlet {
    
    private static Log mLogger = LogFactory.getLog(PreviewServlet.class);
    
    
    /**
     * We function exactly like the PageServlet except that we temporarily
     * modify the users theme for this request.
     */
    public Template handleRequest( HttpServletRequest request,
                                HttpServletResponse response,
                                Context ctx ) 
            throws Exception {
        
        Theme previewTheme = null;
        
        // try getting the preview theme
        String themeName = request.getParameter("theme");
        if (themeName != null) {
            try {
                Roller roller = RollerFactory.getRoller();
                ThemeManager themeMgr = roller.getThemeManager();
                previewTheme = themeMgr.getTheme(themeName);
                
            } catch(ThemeNotFoundException tnfe) {
                // bogus theme specified ... don't worry about it
                // possibly "custom", but we'll handle that below
            } catch(RollerException re) {
                
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setAttribute("DisplayException", re);
                mLogger.error("EXCEPTION: in RollerServlet", re);
            }
        }
        
        if((previewTheme == null || !previewTheme.isEnabled()) &&
                !themeName.equals(Theme.CUSTOM)) {
            
            // if we don't have a valid preview theme then
            // leave it up to our parent
            return super.handleRequest(request, response, ctx);
            
        }
        
        Template outty = null;
        RollerRequest rreq = null;
        
        // first off lets parse the incoming request and validate it
        try {
            PageContext pageContext =
                    JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", true, 8192, true);
            rreq = RollerRequest.getRollerRequest(pageContext);
        } catch (RollerException e) {
            
            // An error initializing the request is considered to be a 404
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            request.setAttribute("DisplayException", e);
            
            return null;
        }
        
        
        // request appears to be valid, lets render
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            
            WebsiteData website = null;
            if (request.getAttribute(RollerRequest.OWNING_WEBSITE) != null) 
            {
                UserData user = (UserData)
                    request.getAttribute(RollerRequest.OWNING_WEBSITE);
            } 
            else 
            {
                website = rreq.getWebsite();
            }
            
            // construct a temporary Website object for this request
            // and set the EditorTheme to our previewTheme
            WebsiteData tmpWebsite = new WebsiteData();
            tmpWebsite.setData(website);
            if(previewTheme != null)
                tmpWebsite.setEditorTheme(previewTheme.getName());
            else
                tmpWebsite.setEditorTheme(Theme.CUSTOM);
            
            org.apache.roller.pojos.Template page = null;
            
            page = tmpWebsite.getDefaultPage();
            
            // Still no page ID ... probably someone with no templates
            // trying to preview a "custom" theme
            if ( page == null ) {
                // lets just call it a 404 and return
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return null;
            }
            
            // update our roller request object
            rreq.setPage(page);
            rreq.setWebsite(tmpWebsite);

            // this sets up the page we want to render
            outty = prepareForPageExecution(ctx, rreq, response, page);
            
            // if there is a decorator template then apply it
            if (website != null) {
                // parse/merge Page template
                StringWriter sw = new StringWriter();
                outty.merge(ctx, sw);
                ctx.put("decorator_body", sw.toString());
                
                // replace outty with decorator Template
                outty = findDecorator(tmpWebsite, (String) ctx.get("decorator"));
            }
            
        } catch(ResourceNotFoundException rnfe ) {
            
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
}

