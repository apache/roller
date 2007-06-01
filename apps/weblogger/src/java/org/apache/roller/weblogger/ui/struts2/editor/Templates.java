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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Templates listing page.
 */
public class Templates extends UIAction {
    
    private static Log log = LogFactory.getLog(Templates.class);
    
    // list of templates to display
    private List templates = Collections.EMPTY_LIST;
    
    // list of template action types user is allowed to create
    private List availableActions = Collections.EMPTY_LIST;
    
    // name and action of new template if we are adding a template
    private String newTmplName = null;
    private String newTmplAction = null;
    
    
    public Templates() {
        this.actionName = "templates";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }
    
    
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
    
    
    /**
     * Save a new template.
     */
    public String add() {
        
        // validation
        myValidate();
        
        if(!hasActionErrors()) try {
            
            WeblogTemplate newTemplate = new WeblogTemplate();
            newTemplate.setWebsite(getActionWeblog());
            newTemplate.setAction(getNewTmplAction());
            newTemplate.setName(getNewTmplName());
            newTemplate.setDescription(newTemplate.getName());
            newTemplate.setContents(getText("pageForm.newTemplateContent"));
            newTemplate.setHidden(false);
            newTemplate.setNavbar(false);
            newTemplate.setLastModified( new Date() );
            
            // all templates start out as velocity templates
            newTemplate.setTemplateLanguage("velocity");
            
            // for now, all templates just use _decorator
            if(!"_decorator".equals(newTemplate.getName())) {
                newTemplate.setDecoratorName("_decorator");
            }
            
            // save the new Template
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            mgr.savePage( newTemplate );
            
            // if this person happened to create a Weblog template from
            // scratch then make sure and set the defaultPageId
            if(WeblogTemplate.DEFAULT_PAGE.equals(newTemplate.getName())) {
                getActionWeblog().setDefaultPageId(newTemplate.getId());
                mgr.saveWebsite(getActionWeblog());
            }
            
            // flush results to db
            RollerFactory.getRoller().flush();
            
            // reset form fields
            setNewTmplName(null);
            setNewTmplAction(null);
            
        } catch (RollerException ex) {
            log.error("Error adding new template for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error adding new template");
        }
        
        return execute();
    }
    
    
    // validation when adding a new template
    private void myValidate() {
        
        // make sure name is non-null and within proper size
        if(StringUtils.isEmpty(getNewTmplName())) {
            addError("TemplateEdit.error.nameNull");
        } else if(getNewTmplName().length() > 255) {
            addError("TemplateEdit.error.nameSize");
        }
        
        // make sure action is a valid
        if(StringUtils.isEmpty(getNewTmplAction())) {
            addError("TemplateEdit.error.actionNull");
        }
        
        // check if template by that name already exists
        try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            WeblogTemplate existingPage = umgr.getPageByName(getActionWeblog(), getNewTmplName());
            if(existingPage != null) {
                addError("pagesForm.error.alreadyExists", getNewTmplName());
            }
        } catch (RollerException ex) {
            log.error("Error checking for existing template", ex);
        }
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
    
}
