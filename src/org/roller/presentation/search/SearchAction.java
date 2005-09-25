/*
 * SearchAction.java
 *
 * Created on September 21, 2005, 11:36 AM
 */
package org.roller.presentation.search;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.config.RollerConfig;

/**
 * Executes site wide search.
 * @author Dave Johnson
 *
 * @struts.action name="search" path="/sitesearch" scope="request"
 * @struts.action-forward name="search.page" path=".search"
 */
public class SearchAction extends Action {
    
    public ActionForward execute(
            ActionMapping mapping, ActionForm form,
            HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String enabled = RollerConfig.getProperty("search.enabled");
        if("false".equalsIgnoreCase(enabled)) {
            return mapping.findForward("main");
        }           
        // search model executes search, makes results available to page
        SearchResultsPageModel model = new SearchResultsPageModel(
                "search.title", request, response, mapping);
        request.setAttribute("searchResults", model);
        return mapping.findForward("search.page");
    }
}
