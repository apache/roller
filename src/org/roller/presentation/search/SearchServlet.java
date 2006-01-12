package org.roller.presentation.search;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.roller.config.RollerConfig;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.ContextLoader;
import org.roller.util.StringUtils;


/**
 * This servlet retrieves (and displays) search results.
 *
 * @web.servlet name="SearchServlet" load-on-startup="5"
 * @web.servlet-init-param name="properties" value="/WEB-INF/velocity.properties"
 * @web.servlet-mapping url-pattern="/search/*"
 */
public class SearchServlet extends VelocityServlet {
    
    static final long serialVersionUID = -2150090108300585670L;
    
    private static Log mLogger = LogFactory.getLog(SearchServlet.class);
    
    private boolean searchEnabled = true;
    
    
    public void init(ServletConfig config) throws ServletException {
        
        super.init(config);
        
        // lookup if search is enabled
        this.searchEnabled = RollerConfig.getBooleanProperty("search.enabled");
    }
    
    
    public Template handleRequest(HttpServletRequest request,
            HttpServletResponse response, Context ctx) {

        Template outty = null;
        try {
            if(! this.searchEnabled) {
                Exception pageException = null;
                try {
                    ContextLoader.setupContext(
                        ctx, RollerRequest.getRollerRequest(request), response );
                    outty = getTemplate( "searchdisabled.vm", "UTF-8" );
                } catch (Exception e) {
                   pageException = e;
                   response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                }

                if (pageException != null) {
                    mLogger.error("EXCEPTION: in RollerServlet", pageException);
                    request.setAttribute("DisplayException", pageException);
                }
                return outty;
            }

            // do no work if query term is empty
            if (StringUtils.isEmpty(request.getParameter("q"))) {
                return generalSearchResults(request, response, ctx);
            }

            // search model executes search, makes results available to page
            SearchResultsPageModel model = 
                    new SearchResultsPageModel("", request, response, null);
            ctx.put("searchResults", model);

            // load standard Velocity stff
            ContextLoader.setupContext(
                ctx, RollerRequest.getRollerRequest(request), response );

            request.setAttribute("zzz_VelocityContext_zzz", ctx); // for testing
            
            outty = getTemplate( "searchresults.vm", "UTF-8" );
        
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            request.setAttribute("DisplayException", e);
            mLogger.error("EXCEPTION: in SearchServlet", e);
        }      
        return outty;
    }
    
    
    /**
     * If this is not a user-specific search, we need to display the
     * "generic" search results list.
     */
    private Template generalSearchResults(
            HttpServletRequest request, 
            HttpServletResponse response, Context ctx) {
        Template outty = null;
        Exception pageException = null;
        try {
            ContextLoader.setupContext(
                ctx, RollerRequest.getRollerRequest(request), response );
            outty = getTemplate( "searchresults.vm", "UTF-8" );
        } catch (Exception e) {
            pageException = e;
            response.setStatus( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
        }
        
        if (pageException != null) {
            mLogger.error("EXCEPTION: in RollerServlet", pageException);
            request.setAttribute("DisplayException", pageException);
        }
        return outty;
    }
    
}


