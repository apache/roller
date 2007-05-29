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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.pojos.WeblogPermission;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Remove a template.
 */
public class TemplateRemove extends UIAction {
    
    private static Log log = LogFactory.getLog(TemplateRemove.class);
    
    // id of template to remove
    private String removeId = null;
    
    // template object that we will remove
    private WeblogTemplate template = null;
    
    
    public TemplateRemove() {
        this.actionName = "templateRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "editPages.title.removeOK";
    }
    
    
    // must be a weblog admin to use this action
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    public void myPrepare() {
        if(getRemoveId() != null) try {
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            setTemplate(umgr.getPage(getRemoveId()));
        } catch (RollerException ex) {
            log.error("Error looking up template by id - "+getRemoveId(), ex);
            // TODO: i18n
            addError("Could not find template to remove - "+getRemoveId());
        }
    }
    
    
    /**
     * Display the remove template confirmation.
     */
    public String execute() {
        return "confirm";
    }
    
    
    /**
     * Remove a new template.
     */
    public String remove() {
        
        if(getTemplate() != null) try {
            if(!getTemplate().isRequired()) {
                UserManager umgr = RollerFactory.getRoller().getUserManager();
                umgr.removePage(getTemplate());
                RollerFactory.getRoller().flush();
                
                // notify cache
                CacheManager.invalidate(getTemplate());
                
                return SUCCESS;
            } else {
                // TODO: i18n
                addError("Cannot remove required template");
            }
            
        } catch(Exception ex) {
            log.error("Error removing page - "+getRemoveId(), ex);
            // TODO: i18n
            addError("Error removing page");
        }
        
        return "confirm";
    }

    
    public String getRemoveId() {
        return removeId;
    }

    public void setRemoveId(String removeId) {
        this.removeId = removeId;
    }

    public WeblogTemplate getTemplate() {
        return template;
    }

    public void setTemplate(WeblogTemplate template) {
        this.template = template;
    }
    
}
