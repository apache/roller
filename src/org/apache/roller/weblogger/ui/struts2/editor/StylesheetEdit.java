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

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;


/**
 * Action which handles editing for a weblog stylesheet override template.
 */
public class StylesheetEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(StylesheetEdit.class);
    
    // the template we are working on
    private WeblogTemplate template = null;
    
    // the contents of the stylesheet override
    private String contents = null;
    
    
    public StylesheetEdit() {
        this.actionName = "stylesheetEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "stylesheetEdit.title";
    }
    
    
    @Override
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    public void myPrepare() {
        String stylesheetPath = getActionWeblog().getTheme().getCustomStylesheet();
        log.debug("custom stylesheet path is - "+stylesheetPath);
        
        if(stylesheetPath != null) {
            try {
                UserManager mgr = RollerFactory.getRoller().getUserManager();
                setTemplate(mgr.getPageByLink(getActionWeblog(), stylesheetPath));
                
                if(getTemplate() == null) {
                    log.debug("custom stylesheet not found, creating it");
                    // template doesn't exist yet, so create it
                    WeblogTemplate stylesheet = new WeblogTemplate();
                    stylesheet.setWebsite(getActionWeblog());
                    stylesheet.setAction(stylesheet.ACTION_CUSTOM);
                    stylesheet.setName(stylesheetPath);
                    stylesheet.setDescription(stylesheetPath);
                    stylesheet.setLink(stylesheetPath);
                    stylesheet.setContents("");
                    stylesheet.setHidden(false);
                    stylesheet.setNavbar(false);
                    stylesheet.setLastModified(new Date());
                    
                    // all templates start out as velocity templates
                    stylesheet.setTemplateLanguage("velocity");
                    
                    mgr.savePage(stylesheet);
                    RollerFactory.getRoller().flush();
                }
            } catch (RollerException ex) {
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
        
        setContents(getTemplate().getContents());
        
        return SUCCESS;
    }
    
    
    /**
     * Save an existing template.
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
            stylesheet.setContents(getContents());
            
            // save template and flush
            UserManager mgr = RollerFactory.getRoller().getUserManager();
            mgr.savePage(stylesheet);
            RollerFactory.getRoller().flush();
            
            // notify caches
            CacheManager.invalidate(stylesheet);
            
            // success message
            addMessage("pageForm.save.success", stylesheet.getName());
            
        } catch (RollerException ex) {
            log.error("Error updating stylesheet template for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error saving template");
        }
        
        return SUCCESS;
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
    
}
