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

package org.apache.roller.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WeblogPermission;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WeblogTheme;
import org.apache.roller.ui.struts2.util.UIAction;


/**
 * Templates listing page.
 */
public class Templates extends UIAction {
    
    private static Log log = LogFactory.getLog(Templates.class);
    
    // list of templates to display
    private List templates = Collections.EMPTY_LIST;
    
    // list of template action types user is allowed to create
    private List availableActions = Collections.EMPTY_LIST;
    
    
    public Templates() {
        this.actionName = "templates";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }
    
    
    // must be a weblog admin to use this action
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    public String execute() {
        
        // query for templates list
        try {
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            setTemplates(mgr.getPages(getActionWeblog()));
            
            // build list of action types that may be added
            List availableActions = new ArrayList();
            availableActions.add(WeblogTemplate.ACTION_CUSTOM);
            
            if(WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
                // if the weblog is using a custom theme then determine which
                // action templates are still available to be created
                availableActions.add(WeblogTemplate.ACTION_PERMALINK);
                availableActions.add(WeblogTemplate.ACTION_SEARCH);
                availableActions.add(WeblogTemplate.ACTION_WEBLOG);
                availableActions.add(WeblogTemplate.ACTION_TAGSINDEX);
                
                List<WeblogTemplate> pages = getTemplates();
                for(WeblogTemplate tmpPage : pages) {
                    if(!WeblogTemplate.ACTION_CUSTOM.equals(tmpPage.getAction())) {
                        availableActions.remove(tmpPage.getAction());
                    }
                }
            }
            setAvailableActions(availableActions);

        } catch (RollerException ex) {
            log.error("Error getting templates for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error getting template list");
        }
        
        return LIST;
    }
    
    
    public List getTemplates() {
        return templates;
    }

    public void setTemplates(List templates) {
        this.templates = templates;
    }

    public List getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List availableActions) {
        this.availableActions = availableActions;
    }
    
}
