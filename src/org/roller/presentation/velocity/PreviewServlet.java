
package org.roller.presentation.velocity;

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
import org.roller.ThemeNotFoundException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ThemeManager;
import org.roller.model.UserManager;
import org.roller.pojos.Theme;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;


/**
 * Allow users to preview what their blog would look like in a given theme.
 *
 * @web.servlet name="PreviewServlet" load-on-startup="1"
 * @web.servlet-init-param name="properties" value="/WEB-INF/velocity.properties"
 * @web.servlet-mapping url-pattern="/preview/*"
 */
public class PreviewServlet extends BasePageServlet {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(PreviewServlet.class);
    
    
    /**
     * We function exactly like the PageServlet except that we temporarily
     * modify the users theme for this request.
     */
    public Template handleRequest( HttpServletRequest request,
            HttpServletResponse response,
            Context ctx ) throws Exception {
        
        Theme previewTheme = null;
        
        // try getting the preview theme
        String themeName = request.getParameter("theme");
        if(themeName != null) {
            try {
                Roller roller = RollerFactory.getRoller();
                ThemeManager themeMgr = roller.getThemeManager();
                previewTheme = themeMgr.getTheme(themeName);
                
            } catch(ThemeNotFoundException tnfe) {
                // bogus theme specified ... don't worry about it
                // possibly "custom", but we'll handle that below
            }
        }
        
        if((previewTheme == null || !previewTheme.isEnabled()) &&
                !themeName.equals(Theme.CUSTOM)) {
            
            // if we don't have a valid preview theme then
            // leave it up to our parent
            return super.handleRequest(request, response, ctx);
        }
        
        
        Template outty = null;
        Exception pageException = null;
        
        try {
            PageContext pageContext =
                    JspFactory.getDefaultFactory().getPageContext(
                    this, request, response,"", true, 8192, true);
            // Needed to init request attributes, etc.
            RollerRequest rreq = RollerRequest.getRollerRequest(pageContext);
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            
            WebsiteData website = null;
            if (request.getAttribute(RollerRequest.OWNING_USER) != null) {
                UserData user = (UserData)
                    request.getAttribute(RollerRequest.OWNING_USER);
                website = userMgr.getWebsite(user.getUserName());
            } else {
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
            
            org.roller.model.Template page = null;
            
            // If request specified the page, then go with that
            page = tmpWebsite.getDefaultPage();
            rreq.setPage(page);
            rreq.setWebsite(tmpWebsite);
            
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
                outty = findDecorator(tmpWebsite, (String) ctx.get("decorator"));
            }
            
        } catch( Exception e ) {
            pageException = e;
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        if (pageException != null) {
            mLogger.error("EXCEPTION: in RollerServlet", pageException);
            request.setAttribute("DisplayException", pageException);
        }
        
        return outty;
    }
}

