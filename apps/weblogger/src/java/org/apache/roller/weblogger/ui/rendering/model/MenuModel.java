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

package org.apache.roller.weblogger.ui.rendering.model;

import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.ui.core.util.menu.Menu;
import org.apache.roller.weblogger.ui.core.util.menu.MenuHelper;
import org.apache.roller.weblogger.ui.rendering.util.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.util.WeblogRequest;


/**
 * Model which provides methods for displaying editor menu/navigation-bar.
 * 
 * Implemented by calling hybrid JSP tag.
 */
public class MenuModel implements Model {
    
    private static Log logger = LogFactory.getLog(MenuModel.class);
    
    private WeblogPageRequest pageRequest = null;
    
    
    /** Template context name to be used for model */
    public String getModelName() {
        return "menuModel";
    }
    
    
    /** Init page model based on request */
    public void init(Map initData) throws WebloggerException {
        
        // we expect the init data to contain a weblogRequest object
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if(weblogRequest == null) {
            throw new WebloggerException("expected weblogRequest from init data");
        }
        
        // MenuModel only works on page requests, so cast weblogRequest
        // into a WeblogPageRequest and if it fails then throw exception
        if(weblogRequest instanceof WeblogPageRequest) {
            this.pageRequest = (WeblogPageRequest) weblogRequest;
        } else {
            throw new WebloggerException("weblogRequest is not a WeblogPageRequest."+
                    "  MenuModel only supports page requests.");
        }
    }
    
    
    /**
     * Get a Menu representing the admin UI action menu, if the user is 
     * currently logged in and is an admin.
     */
    public Menu getAdminMenu() {
        if(pageRequest.isLoggedIn() && pageRequest.getUser().hasRole("admin")) {
            return MenuHelper.getMenu("admin", "noAction", pageRequest.getUser(), pageRequest.getWeblog());
        }
        return null;
    }
    
    
    /**
     * Get a Menu representing the author UI action menu, if the use is
     * currently logged in.
     */
    public Menu getAuthorMenu() {
        if(pageRequest.isLoggedIn()) {
            return MenuHelper.getMenu("editor", "noAction", pageRequest.getUser(), pageRequest.getWeblog());
        }
        return null;
    }
    
}
