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

package org.apache.roller.weblogger.ui.struts2.core;

import org.apache.commons.lang3.CharSetUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.validation.SkipValidation;

import java.util.List;
import org.apache.roller.weblogger.business.themes.SharedTheme;


/**
 * Allows user to create a new website.
 */
// TODO: make this work @AllowedMethods({"execute","save"})
public class CreateWeblog extends UIAction {
    
    private static final Log log = LogFactory.getLog(CreateWeblog.class);
    private static final String DISABLED_RETURN_CODE = "disabled";

    private CreateWeblogBean bean = new CreateWeblogBean();
    

    public CreateWeblog() {
        this.pageTitle = "createWebsite.title";
    }
    
    
    // override default security, we do not require an action weblog
    @Override
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    @SkipValidation
    @Override
    public String execute() {

        // check if blog administrator has enabled creation of new blogs
        if(!WebloggerRuntimeConfig.getBooleanProperty("site.allowUserWeblogCreation")) {
            addError("createWebsite.disabled");
            return DISABLED_RETURN_CODE;
        }

        User user = getAuthenticatedUser();

        try {
            if (!WebloggerConfig.getBooleanProperty("groupblogging.enabled")) {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                List<WeblogPermission> permissions = mgr.getWeblogPermissions(user);
                if (!permissions.isEmpty()) {
                    // sneaky user trying to get around 1 blog limit that applies
                    // only when group blogging is disabled
                    addError("createWebsite.oneBlogLimit");
                    return DISABLED_RETURN_CODE;
                }
            }
        } catch (WebloggerException ex) {
            log.error("error checking for existing weblogs count", ex);
            addError("generic.error.check.logs");
            return DISABLED_RETURN_CODE;
        }
        
        // pre-populate with some logical defaults
        getBean().setLocale(user.getLocale());
        getBean().setTimeZone(user.getTimeZone());
        getBean().setEmailAddress(user.getEmailAddress());
        
        return INPUT;
    }
    
    
    public String save() {
        
        User user = getAuthenticatedUser();
        try {
            if (!WebloggerConfig.getBooleanProperty("groupblogging.enabled")) {
                UserManager mgr = WebloggerFactory.getWeblogger().getUserManager();
                List<WeblogPermission> permissions = mgr.getWeblogPermissions(user);
                if (!permissions.isEmpty()) {
                    // sneaky user trying to get around 1 blog limit that applies
                    // only when group blogging is disabled
                    addError("createWebsite.oneBlogLimit");
                    return "menu";
                }
            }
        } catch (WebloggerException ex) {
            log.error("error checking for existing weblogs count", ex);
        }
        
        myValidate();
        
        if(!hasActionErrors()) {
            
            Weblog wd = new Weblog(
                    getBean().getHandle(),
                    user.getUserName(),
                    getBean().getName(),
                    getBean().getDescription(),
                    getBean().getEmailAddress(),
                    getBean().getTheme(),
                    getBean().getLocale(),
                    getBean().getTimeZone());
            
            // pick a weblog editor for this weblog
            String def = WebloggerRuntimeConfig.getProperty("users.editor.pages");
            String[] defs = Utilities.stringToStringArray(def,",");
            wd.setEditorPage(defs[0]);

            try {
                // add weblog and flush
                WebloggerFactory.getWeblogger().getWeblogManager().addWeblog(wd);
                WebloggerFactory.getWeblogger().flush();
                
                // tell the user their weblog was created
                addMessage("createWebsite.created", getBean().getHandle());
                
                return SUCCESS;
                
            } catch (WebloggerException e) {
                log.error("ERROR adding weblog", e);
                // TODO: error handling
                addError(e.getMessage());
            }
            
        }
        
        return INPUT;
    }
    
    
    public void myValidate()  {
        
        String allowed = WebloggerConfig.getProperty("username.allowedChars");
        if(allowed == null || allowed.isBlank()) {
            allowed = Register.DEFAULT_ALLOWED_CHARS;
        }
        
        // make sure handle only contains safe characters
        String safe = CharSetUtils.keep(getBean().getHandle(), allowed);
        if (!safe.equals(getBean().getHandle()) ) {
            addError("createWeblog.error.invalidHandle");
        }
        
        // make sure theme was specified and is a valid value
        
        // make sure handle isn't already taken
        if(!StringUtils.isEmpty(getBean().getHandle())) {
            try {
                if (WebloggerFactory.getWeblogger().getWeblogManager()
                        .getWeblogByHandle(getBean().getHandle()) != null) {
                    addError("createWeblog.error.handleExists");
                    // reset handle
                    getBean().setHandle(null);
                }
            } catch (WebloggerException ex) {
                log.error("error checking for weblog", ex);
                addError("Unexpected error validating weblog -- check Roller logs");
            }
        }
    }
    
    
    public List<SharedTheme> getThemes() {
        ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
        return themeMgr.getEnabledThemesList();
    }

    public CreateWeblogBean getBean() {
        return bean;
    }

    public void setBean(CreateWeblogBean bean) {
        this.bean = bean;
    }
    
}
