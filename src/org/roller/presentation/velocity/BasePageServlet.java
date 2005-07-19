package org.roller.presentation.velocity;

import java.io.IOException;
import java.io.StringWriter;
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
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerRequest;

/**
 * Base Servlet for Servlets that render user page templates. Loads the
 * Velocity context using the ContextLoader and runs the page template
 * selected by the request.
 *
 * @author llavandowska
 * @author David M Johnson
 * @author Allen Gilliland
 */
public abstract class BasePageServlet extends VelocityServlet {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(BasePageServlet.class);
    
    
    /**
     * Sets servletContext for WebappResourceLoader.
     */
    public void init( ServletConfig config )
        throws ServletException {
        
        super.init( config );
        WebappResourceLoader.setServletContext( getServletContext() );
    }
    
    
    public Template handleRequest( HttpServletRequest request,
            HttpServletResponse response,
            Context ctx ) throws Exception {
        
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
            
            org.roller.pojos.Template page = null;
            
            // If request specified the page, then go with that
            if (rreq.getPage() != null &&
                    rreq.getRequest().getAttribute(RollerRequest.OWNING_USER) == null) {
                page = rreq.getPage();
                
                // If page not available from request, then use website's default
            } else if (website != null) {
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
    
    
    /**
     * Prepare the requested page for execution by setting content type
     * and populating velocity context.
     */
    protected Template prepareForPageExecution(Context ctx,
            RollerRequest rreq,
            HttpServletResponse response,
            org.roller.pojos.Template page) throws Exception {
        
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
        
        return getTemplate( page.getId(), "UTF-8" );
    }
    
    
    /**
     * Load the decorator template and apply it.  If there is no user specified
     * decorator then the default decorator is applied.
     */
    protected Template findDecorator(WebsiteData website, String decorator_name)
        throws Exception {
        
        Template decorator = null;
        org.roller.pojos.Template decorator_template = null;
        
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
            decorator = getTemplate("/themes/noop_decorator.vm", "UTF-8");
        }
        
        return decorator;
    }
    
    
    /**
     * Handle error in Velocity processing.
     */
    protected void error( HttpServletRequest req, HttpServletResponse res,
            Exception e) throws ServletException, IOException {
        mLogger.warn("ERROR in VelocityServlet",e);
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
