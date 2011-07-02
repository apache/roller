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
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

import java.util.Collections;
import java.util.List;


/**
 * Remove a template.
 */
public class TemplateRemove extends UIAction {
    
    private static Log log = LogFactory.getLog(TemplateRemove.class);
    
    // id of template to remove
    private String removeId = null;

    private String removeMobileId = null;
    
    // template object that we will remove
    private WeblogTemplate template = null;

    private WeblogTemplate mobileTemplate = null;
    
    
    public TemplateRemove() {
        this.actionName = "templateRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "editPages.title.removeOK";
    }
    
    
    // must be a weblog admin to use this action
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }
    
    
    public void myPrepare() {
        if(getRemoveId() != null) try {
            setTemplate(WebloggerFactory.getWeblogger().getWeblogManager().getPage(getRemoveId()));
            setRemoveMobileId(getMobileID());
            setMobileTemplate(WebloggerFactory.getWeblogger().getWeblogManager().getPage(getRemoveMobileId()));
        } catch (WebloggerException ex) {
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
                UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();

                // notify cache
                CacheManager.invalidate(getTemplate());

                WebloggerFactory.getWeblogger().getWeblogManager().removePage(getTemplate());

                if (getMobileTemplate() != null) {
                    CacheManager.invalidate(getMobileTemplate());

                    WebloggerFactory.getWeblogger().getWeblogManager().removePage(getMobileTemplate());
                }

                WebloggerFactory.getWeblogger().flush();
                                
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

 //get the ID for mobile tempalte
    private String getMobileID(){

        List<WeblogTemplate> templates = null;
        try {
            templates = WebloggerFactory.getWeblogger().getWeblogManager().
                    getPagesByLink(getActionWeblog(), template.getLink()) ;
        } catch (WebloggerException e) {
            log.error("error while getting template list." , e);
        }

        if (templates != null && templates.size() >1){
            for(WeblogTemplate template : templates){
                  if("mobile".equals(template.getType())){
                      return template.getId();
                  }
            }
        }
        return null;
    }


    public String getRemoveMobileId() {
        return removeMobileId;
    }

    public void setRemoveMobileId(String removeMobileId) {
        this.removeMobileId = removeMobileId;
    }

    public WeblogTemplate getMobileTemplate() {
        return mobileTemplate;
    }

    public void setMobileTemplate(WeblogTemplate mobileTemplate) {
        this.mobileTemplate = mobileTemplate;
    }
}
