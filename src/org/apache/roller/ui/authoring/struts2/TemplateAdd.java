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

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.ui.core.util.struts2.UIAction;


/**
 * Action used for adding a new template.
 */
public class TemplateAdd extends UIAction {
    
    private static Log log = LogFactory.getLog(TemplateAdd.class);
    
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
     * Save a new template.
     */
    public String save() {
        
        // validation
        myValidate();
        
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
        
        try {
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
            
        } catch (RollerException ex) {
            log.error("Error adding new template for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error adding new template");
        }
        
        return SUCCESS;
    }
    
    
    // TODO: validation
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
