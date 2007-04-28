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

package org.apache.roller.ui.authoring.struts2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.UserData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WeblogTheme;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.util.menu.Menu;
import org.apache.roller.ui.core.util.menu.MenuHelper;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Templates listing page.
 */
public class Templates extends UIAction {
    
    private static Log log = LogFactory.getLog(Templates.class);
    
    // list of templates to display
    private List templates = Collections.EMPTY_LIST;
    
    // template id to remove
    private String removeId = null;
    
    
    public Templates() {
        this.actionName = "templates";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }
    
    
    // must be a weblog admin to use this action
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    public String execute() {
        return SUCCESS;
    }
    
    
    /**
     * This action method is called by an ajax enabled 'div' tag on the page
     * and is used to list the templates available for the given weblog.
     */
    public String list() {
        
        // query for templates list
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            setTemplates(mgr.getPages(getActionWeblog()));
            
        } catch (RollerException ex) {
            log.error("Error getting pages for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error getting template list");
        }
        
        return "list-ajax";
    }
    
    
    public String remove() {
        
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplate template = mgr.getPage(getRemoveId());
            
            if(!template.isRequired()) {
                
                // remove it and flush
                mgr.removePage(template);
                RollerFactory.getRoller().flush();
                
                // notify cache
                CacheManager.invalidate(template);
                
                return execute();
            } else {
                // TODO: i18n
                addError("Cannot remove required template");
            }
            
        } catch (RollerException e) {
            log.error("Error removing template", e);
            addError("error.internationalized", e.getRootCauseMessage());
        }
        
        return "remove-fail";
    }
    
    
    public List getTemplates() {
        return templates;
    }

    public void setTemplates(List templates) {
        this.templates = templates;
    }

    public String getRemoveId() {
        return removeId;
    }

    public void setRemoveId(String removeId) {
        this.removeId = removeId;
    }
    
}
