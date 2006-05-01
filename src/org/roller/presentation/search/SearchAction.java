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
/*
 * SearchAction.java
 *
 * Created on September 21, 2005, 11:36 AM
 */
package org.roller.presentation.search;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.presentation.BasePageModel;

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
        PageModel model = new PageModel("search.title", request, response, mapping);
        request.setAttribute("model", model);
        return mapping.findForward("search.page");
    }
    
    public class PageModel extends BasePageModel {
        private SearchResultsPageModel searchModel = null;
        public PageModel(
            String titleKey,
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping) throws RollerException, IOException {        
            super(titleKey, request, response, mapping);
            setSearchModel(new SearchResultsPageModel(request, false));
        }

        public SearchResultsPageModel getSearchModel() {
            return searchModel;
        }

        public void setSearchModel(SearchResultsPageModel searchModel) {
            this.searchModel = searchModel;
        }
    }
}
