package org.roller.presentation.search;

import java.io.IOException;
import java.util.Date;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

import org.apache.commons.collections.comparators.ReverseComparator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Hits;
import org.apache.struts.action.ActionMapping;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.roller.RollerException;
import org.roller.business.search.FieldConstants;
import org.roller.business.search.operations.SearchOperation;
import org.roller.config.RollerConfig;
import org.roller.model.IndexManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogEntryComparator;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.BasePageModel;
import org.roller.presentation.RollerRequest;
import org.roller.util.DateUtil;
import org.roller.util.StringUtils;
import org.roller.presentation.velocity.*;

/**
 * This servlet retrieves (and displays) search results.
 *
 * @web.servlet name="SearchServlet" load-on-startup="5"
 * @web.servlet-init-param name="properties" value="/WEB-INF/velocity.properties"
 * @web.servlet-mapping url-pattern="/search/*"
 */
public class SearchServlet extends BasePageServlet {
    
    static final long serialVersionUID = -2150090108300585670L;
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(SearchServlet.class);
    
    private boolean searchEnabled = true;
    
    public Template handleRequest(HttpServletRequest request,
        HttpServletResponse response, Context ctx) throws Exception {
        
        // Note: Removed request character encoding here; was too late; 
        // it is now set uniformly in CharEncodingFilter. See ROL-760.
        
        String enabled = RollerConfig.getProperty("search.enabled");
        if("false".equalsIgnoreCase(enabled))
            this.searchEnabled = false;
        
        if(! this.searchEnabled) {
            Template outty = null;
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
        
        return getTemplate( "searchresults.vm", "UTF-8" );
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


