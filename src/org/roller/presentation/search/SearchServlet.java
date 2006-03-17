package org.roller.presentation.search;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.ContextLoader;
import org.roller.presentation.velocity.PageServlet;

/**
 * This servlet retrieves (and displays) search results.
 *
 * @web.servlet name="SearchServlet" load-on-startup="5"
 * @web.servlet-init-param name="properties" value="/WEB-INF/velocity.properties"
 * @web.servlet-mapping url-pattern="/search/*"
 */
public class SearchServlet extends PageServlet {
    
    static final long serialVersionUID = -2150090108300585670L;
    
    private static Log mLogger = LogFactory.getLog(SearchServlet.class);
    
    private boolean searchEnabled = true;
    
    
    public void init(ServletConfig config) throws ServletException {
        
        super.init(config);
        
        // lookup if search is enabled
        this.searchEnabled = RollerConfig.getBooleanProperty("search.enabled");
    }
    
    /**
     * Prepare the requested page for execution by setting content type
     * and populating velocity context.
     */
    protected Template prepareForPageExecution(
            Context ctx,
            RollerRequest rreq,
            HttpServletResponse response,
            org.roller.pojos.Template page)             
        throws ResourceNotFoundException, RollerException {
        
        // search model executes search, makes results available to page
        SearchResultsPageModel model = 
            new SearchResultsPageModel(rreq.getRequest(), true);
        ctx.put("searchResults", model);
        return super.prepareForPageExecution(ctx, rreq, response, page);
    }
}


