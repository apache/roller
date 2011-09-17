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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.*;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

import java.util.Collections;
import java.util.Date;
import java.util.List;


/**
 * Action which handles editing for a weblog stylesheet override template.
 */
public class StylesheetEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(StylesheetEdit.class);
    
    // the template we are working on
    private WeblogTemplate template = null;
    
    // the contents of the stylesheet override
    private String contents = null;

    // type of the stylesheet template code in active
    private String type = "standard";
    
    
    public StylesheetEdit() {
        this.actionName = "stylesheetEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "stylesheetEdit.title";
    }
    
    
    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }
    
    
    @Override
    public void myPrepare() {
        
        ThemeTemplate stylesheet = null;
        try {
            stylesheet = getActionWeblog().getTheme().getStylesheet();
        } catch (WebloggerException ex) {
            log.error("Error looking up stylesheet on weblog - "+getActionWeblog().getHandle(), ex);
        }
        
        if(stylesheet != null) {
            log.debug("custom stylesheet path is - "+stylesheet.getLink());
            try {
                 setTemplate(WebloggerFactory.getWeblogger().getWeblogManager()
                        .getPageByLink(getActionWeblog(), stylesheet.getLink()));

                if(getTemplate() == null) {
                    log.debug("custom stylesheet not found, creating it");
                    
                    // template doesn't exist yet, so create it
                    WeblogTemplate stylesheetTmpl = new WeblogTemplate();
                    stylesheetTmpl.setWebsite(getActionWeblog());
                    stylesheetTmpl.setAction(stylesheet.ACTION_CUSTOM);
                    stylesheetTmpl.setName(stylesheet.getName());
                    stylesheetTmpl.setDescription(stylesheet.getDescription());
                    stylesheetTmpl.setLink(stylesheet.getLink());
                    stylesheetTmpl.setContents(stylesheet.getContents());
                    stylesheetTmpl.setHidden(false);
                    stylesheetTmpl.setNavbar(false);
                    stylesheetTmpl.setLastModified(new Date());
                    stylesheetTmpl.setTemplateLanguage(stylesheet.getTemplateLanguage());

                    // create template codes for available template code Types
                    WeblogTemplateCode standardTemplateCode = new WeblogTemplateCode(stylesheetTmpl.getId(),"standard");
                    standardTemplateCode.setTemplate(stylesheetTmpl.getContents());
                    standardTemplateCode.setTemplateLanguage(stylesheetTmpl.getTemplateLanguage());

                    WeblogTemplateCode mobileTemplateCode = new WeblogTemplateCode(stylesheetTmpl.getId(),"mobile");
                    mobileTemplateCode.setTemplate(stylesheetTmpl.getContents());
                    mobileTemplateCode.setTemplateLanguage(stylesheetTmpl.getTemplateLanguage());

                    WebloggerFactory.getWeblogger().getWeblogManager().saveTemplateCode(standardTemplateCode);
                    WebloggerFactory.getWeblogger().getWeblogManager().saveTemplateCode(mobileTemplateCode);

                    WebloggerFactory.getWeblogger().getWeblogManager().savePage(stylesheetTmpl);
                    WebloggerFactory.getWeblogger().flush();
                    
                    setTemplate(stylesheetTmpl);
                }
            } catch (WebloggerException ex) {
                log.error("Error finding/adding stylesheet tempalate from weblog - "+getActionWeblog().getHandle(), ex);
            }
        }
    }
    
    
    /**
     * Show stylesheet edit page.
     */
    public String execute() {
        
        if(getTemplate() == null) {
            return ERROR;
        }
        WeblogTemplateCode templateCode = null;
        try {
            templateCode = getTemplate().getTemplateCode(getType());
        } catch (WebloggerException e) {
            log.error("Error loading Weblog template code for stylesheet", e);
        }
         // if there is a template code load that template
        if(templateCode != null) {
            setContents(templateCode.getTemplate());
        }
        // if not fall back for default template code
        else{
           setContents(getTemplate().getContents());
        }
        
        return INPUT;
    }
    
    
    /**
     * Save an existing stylesheet.
     */
    public String save() {
        
        if(getTemplate() == null) {
            // TODO: i18n
            addError("Unable to locate stylesheet template");
            return ERROR;
        }
        
        if(!hasActionErrors()) try {
            
            WeblogTemplate stylesheet = getTemplate();
            
            stylesheet.setLastModified(new Date());
            WeblogTemplateCode templateCode = stylesheet.getTemplateCode(getType());
            templateCode.setTemplate(getContents());

            //  stylesheet.setContents(getContents());
            
            // save template and flush
            WebloggerFactory.getWeblogger().getWeblogManager().savePage(stylesheet);
            WebloggerFactory.getWeblogger().getWeblogManager().saveTemplateCode(templateCode);
            WebloggerFactory.getWeblogger().flush();
            
            // notify caches
            CacheManager.invalidate(stylesheet);
            
            // success message
            addMessage("stylesheetEdit.save.success", stylesheet.getName());
            
        } catch (WebloggerException ex) {
            log.error("Error updating stylesheet template for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error saving template");
        }
        
        return INPUT;
    }
    
    
    /**
     * Revert the stylesheet to its original state.
     */
    public String revert() {
        
        if(getTemplate() == null) {
            // TODO: i18n
            addError("Unable to locate stylesheet template");
            return ERROR;
        }
        
        // make sure we are still using a shared theme so that reverting is possible
        if(WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
            // TODO: i18n
            addError("stylesheetEdit.error.customTheme");
        }
        
        if(!hasActionErrors()) try {
            
            WeblogTemplate stylesheet = getTemplate();
            
            // lookup the theme used by this weblog
            ThemeManager tmgr = WebloggerFactory.getWeblogger().getThemeManager();
            Theme theme = tmgr.getTheme(getActionWeblog().getEditorTheme());

            //get weblogTemplateCode
            WeblogTemplateCode templateCode = theme.getStylesheet().getTemplateCode(type);
            stylesheet.setContents(templateCode.getTemplate());
            
            // lookup 
            stylesheet.setLastModified(new Date());
            //stylesheet.setContents(theme.getStylesheet().getContents());

            //save template code which was persisted in DB
            WeblogTemplateCode existingTemplateCode = stylesheet.getTemplateCode(getType());
            existingTemplateCode.setTemplate(templateCode.getTemplate());
            WebloggerFactory.getWeblogger().getWeblogManager().saveTemplateCode(existingTemplateCode);
            // save template and flush
            WebloggerFactory.getWeblogger().getWeblogManager().savePage(stylesheet);
            WebloggerFactory.getWeblogger().flush();
            
            // notify caches
            CacheManager.invalidate(stylesheet);
            
            // success message
            addMessage("stylesheetEdit.revert.success", stylesheet.getName());
            
        } catch (WebloggerException ex) {
            log.error("Error updating stylesheet template for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error saving template");
        }
        
        return execute();
    }
    
    
    public boolean isCustomTheme() {
        return (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme()));
    }
    
    
    public WeblogTemplate getTemplate() {
        return template;
    }

    public void setTemplate(WeblogTemplate template) {
        this.template = template;
    }

    public String getContents() {
        return contents;
    }

    public void setContents(String contents) {
        this.contents = contents;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
