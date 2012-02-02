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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogThemeTemplateCode;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Templates listing page.
 */
public class Templates extends UIAction {
    
    private static Log log = LogFactory.getLog(Templates.class);
    
    // list of templates to display
    private List<WeblogTemplate> templates = Collections.EMPTY_LIST;
    
    // list of template action types user is allowed to create
    private List availableActions = Collections.EMPTY_LIST;
    
    // name and action of new template if we are adding a template
    private String newTmplName = null;
    private String newTmplAction = null;
    private String type = null;
    
    public Templates() {
        this.actionName = "templates";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }
    
    
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }
    
    
    public String execute() {
        
        // query for templates list
        try {
            
             // get current list of templates, minus custom stylesheet
            List<WeblogTemplate> raw = WebloggerFactory.getWeblogger().getWeblogManager().getPages(getActionWeblog());
            List<WeblogTemplate> pages = new ArrayList<WeblogTemplate>();
            pages.addAll(raw);
            if(getActionWeblog().getTheme().getStylesheet() != null) {
                pages.remove(WebloggerFactory.getWeblogger().getWeblogManager().getPageByLink(getActionWeblog(),
                        getActionWeblog().getTheme().getStylesheet().getLink()));
            }
            setTemplates(pages);
            
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
                
                for(WeblogTemplate tmpPage : getTemplates()) {
                    if(!WeblogTemplate.ACTION_CUSTOM.equals(tmpPage.getAction())) {
                        availableActions.remove(tmpPage.getAction());
                    }
                }
            } else if (pages.isEmpty()) {
                availableActions.add(WeblogTemplate.ACTION_WEBLOG);
            }
            setAvailableActions(availableActions);

        } catch (WebloggerException ex) {
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

            if(WeblogTemplate.ACTION_CUSTOM.equals(getNewTmplAction())){
                newTemplate.setLink(getNewTmplName());
            }
            
            // all templates start out as velocity templates
            newTemplate.setTemplateLanguage("velocity");
            
            // for now, all templates just use _decorator
            if(!"_decorator".equals(newTemplate.getName())) {
                newTemplate.setDecoratorName("_decorator");
            }
            
            // save the new Template
            WebloggerFactory.getWeblogger().getWeblogManager().savePage( newTemplate );

            //Create weblog template codes for available types.
            WeblogThemeTemplateCode standardTemplCode = new WeblogThemeTemplateCode(newTemplate.getId(),"standard");
            WeblogThemeTemplateCode mobileTemplCode = new WeblogThemeTemplateCode(newTemplate.getId(),"mobile");

            standardTemplCode.setTemplate(newTemplate.getContents());
            standardTemplCode.setTemplateLanguage("velocity");

            mobileTemplCode.setTemplate(newTemplate.getContents());
            mobileTemplCode.setTemplateLanguage("velocity");

            WebloggerFactory.getWeblogger().getWeblogManager().saveTemplateCode(standardTemplCode);
            WebloggerFactory.getWeblogger().getWeblogManager().saveTemplateCode(mobileTemplCode);

            // if this person happened to create a Weblog template from
            // scratch then make sure and set the defaultPageId
            if(WeblogTemplate.DEFAULT_PAGE.equals(newTemplate.getName())) {
                getActionWeblog().setDefaultPageId(newTemplate.getId());
                WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(getActionWeblog());
            }
            
            // flush results to db
            WebloggerFactory.getWeblogger().flush();
            
            // reset form fields
            setNewTmplName(null);
            setNewTmplAction(null);
            
        } catch (WebloggerException ex) {
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
            addError("Template.error.nameNull");
        } else if(getNewTmplName().length() > 255) {
            addError("Template.error.nameSize");
        }
        
        // make sure action is a valid
        if(StringUtils.isEmpty(getNewTmplAction())) {
            addError("Template.error.actionNull");
        }
        
        // check if template by that name already exists
        try {
            WeblogTemplate existingPage = WebloggerFactory.getWeblogger().getWeblogManager().getPageByName(getActionWeblog(), getNewTmplName());
            if(existingPage != null) {
                addError("pagesForm.error.alreadyExists", getNewTmplName());
            }
        } catch (WebloggerException ex) {
            log.error("Error checking for existing template", ex);
        }


    }
    
    /**
     * Checks if is custom theme.
     *
     * @return true, if is custom theme
     */
    public boolean isCustomTheme() {
        return (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme()));
    }
    
    public List<WeblogTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<WeblogTemplate> templates) {
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
