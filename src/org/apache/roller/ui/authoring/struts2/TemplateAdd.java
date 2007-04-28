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
 * Action used for adding a new template.
 */
public class TemplateAdd extends UIAction {
    
    private static Log log = LogFactory.getLog(TemplateAdd.class);
    
    // list of actions available for creation
    private List availableActions = Collections.EMPTY_LIST;
    
    // type of template being added, used by showAdd() method
    private String addType = null;
    
    // name and action of new template if we are adding a template
    private String newTmplName = null;
    private String newTmplAction = null;
    
    
    public TemplateAdd() {
        this.actionName = "templateAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }
    
    
    // must be a weblog admin to use this action
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    /**
     * Display the add template form.
     */
    public String execute() {
        
        try {
            List actions = new ArrayList();
            if(WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
                // if the weblog is using a custom theme then determine which
                // action templates are still available to be created
                actions.add(WeblogTemplate.ACTION_PERMALINK);
                actions.add(WeblogTemplate.ACTION_SEARCH);
                actions.add(WeblogTemplate.ACTION_WEBLOG);
                actions.add(WeblogTemplate.ACTION_TAGSINDEX);
                
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                List<WeblogTemplate> templates = mgr.getPages(getActionWeblog());
                for(WeblogTemplate tmpPage : templates) {
                    if(!WeblogTemplate.ACTION_CUSTOM.equals(tmpPage.getAction())) {
                        actions.remove(tmpPage.getAction());
                    }
                }
            }
            
            setAvailableActions(actions);
        } catch (RollerException ex) {
            log.error("Error accessing templates for weblog -"+getActionWeblog().getHandle(), ex);
        }

        return SUCCESS;
    }
    
    
    /**
     * Save a new template.
     */
    public String save() {
        
        WebsiteData weblog = getActionWeblog();
        
        // validation
        myValidate();
        
        WeblogTemplate page = new WeblogTemplate();
        page.setWebsite(weblog);
        page.setAction(getNewTmplAction());
        page.setName(getNewTmplName());
        page.setDescription(page.getName());
        page.setContents(getText("pageForm.newTemplateContent"));
        page.setHidden(false);
        page.setNavbar(false);
        page.setLastModified( new Date() );
        
        // all templates start out as velocity templates
        page.setTemplateLanguage("velocity");
        
        // for now, all templates just use _decorator
        if(!"_decorator".equals(page.getName())) {
            page.setDecoratorName("_decorator");
        }
        
        try {
            // save the page
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            mgr.savePage( page );
            
            // if this person happened to create a Weblog template from
            // scratch then make sure and set the defaultPageId
            if(WeblogTemplate.DEFAULT_PAGE.equals(page.getName())) {
                weblog.setDefaultPageId(page.getId());
                mgr.saveWebsite(weblog);
            }
            
            // flush results to db
            RollerFactory.getRoller().flush();
            
            return "addSuccess-ajax";
            
        } catch (RollerException ex) {
            log.error("Error adding new page for weblog - "+weblog.getHandle(), ex);
            // TODO: i18n
            addError("Error adding new page");
        }
        
        return "addForm-ajax";
    }
    
    
    private void myValidate() {
        
        // make sure that we have an appropriate name value
        
        // make sure that we have an appropriate action value
        
        // first off, check if template already exists
//        WeblogTemplate existingPage = mgr.getPageByName(website, getNewTmplName());
//        if(existingPage != null) {
//            addError("pagesForm.error.alreadyExists", getNewTmplName());
//            return INPUT;
//        }
        
    }
    

    public List getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(List availableActions) {
        this.availableActions = availableActions;
    }

    public String getNewTmplName() {
        return newTmplName;
    }

    public void setNewTmplName(String newTmplName) {
        this.newTmplName = newTmplName;
    }

    public String getNewTmplAction() {
        return newTmplAction;
    }

    public void setNewTmplAction(String newTmplAction) {
        this.newTmplAction = newTmplAction;
    }

    public String getAddType() {
        return addType;
    }

    public void setAddType(String addType) {
        this.addType = addType;
    }
    
}
