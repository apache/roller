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
package org.apache.roller.ui.rendering.search;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.rendering.velocity.PageServlet;

/**
 * This servlet retrieves (and displays) search results.
 *
 * @web.servlet name="SearchServlet" load-on-startup="5"
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
            org.apache.roller.pojos.Template page)             
        throws ResourceNotFoundException, RollerException {
        
        // search model executes search, makes results available to page
        SearchResultsPageModel model = 
            new SearchResultsPageModel(rreq.getRequest(), true);
        ctx.put("searchResults", model);
        return super.prepareForPageExecution(ctx, rreq, response, page);
    }
}


