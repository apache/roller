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

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.themes.SharedTheme;
import org.apache.roller.business.themes.ThemeManager;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.WeblogPermission;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.WeblogTheme;
import org.apache.roller.pojos.Weblog;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Action for controlling theme selection.
 */
public class ThemeEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(Templates.class);
    
    // list of available themes
    private List themes = Collections.EMPTY_LIST;
    
    // type of theme desired, either 'shared' or 'custom'
    private String themeType = null;
    
    // the chosen shared theme id
    private String themeId = null;
    
    // import the selected theme to the action weblog
    private boolean importTheme = false;
    
    // the chosen import theme id
    private String importThemeId = null;
    
    
    public ThemeEdit() {
        this.actionName = "themeEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "themeEditor.title";
    }
    
    
    public short requiredWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    public void myPrepare() {
        ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
        setThemes(themeMgr.getEnabledThemesList());
    }
    
    
    public String execute() {
        // set theme to current value
        if(WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
            setThemeId(null);
        } else {
            setThemeId(getActionWeblog().getTheme().getId());
        }
        
        if(!RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")) {
            return "input-sharedonly";
        } else {
            return INPUT;
        }
    }
    

    /**
     * Save new theme configuration.
     */
    public String save() {
        
        Weblog weblog = getActionWeblog();
        
        if(WeblogTheme.CUSTOM.equals(getThemeType())) {
            
            // only continue if custom themes are allowed
            if(RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")) {
                // do theme import if necessary
                if(isImportTheme() && !StringUtils.isEmpty(getImportThemeId())) try {
                    ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                    SharedTheme importTheme = themeMgr.getTheme(getImportThemeId());
                    themeMgr.importTheme(getActionWeblog(), importTheme);
                } catch(RollerException re) {
                    log.error("Error customizing theme for weblog - "+getActionWeblog().getHandle(), re);
                    // TODO: i18n
                    addError("Error importing theme");
                }
                
                if(!hasActionErrors()) {
                    weblog.setEditorTheme(WeblogTheme.CUSTOM);
                    log.debug("Saving custom theme for weblog "+weblog.getHandle());
                    
                    // reset import theme checkbox
                    setImportTheme(false);
                }
            } else {
                // TODO: i18n
                addError("Sorry, custom themes are not allowed");
            }
            
        } else if("shared".equals(getThemeType())) {
            // validation
            myValidate();
            
            if(!hasActionErrors()) {
                weblog.setEditorTheme(getThemeId());
                log.debug("Saving theme "+getThemeId()+" for weblog "+weblog.getHandle());
            }
        } else {
            // invalid theme type
            // TODO: i18n
            addError("no valid theme type submitted");
        }
        
        if(!hasActionErrors()) {
            try {
                // save updated weblog and flush
                UserManager userMgr = RollerFactory.getRoller().getUserManager();
                userMgr.saveWebsite(weblog);
                RollerFactory.getRoller().flush();
                
                // make sure to flush the page cache so ppl can see the change
                CacheManager.invalidate(weblog);
                
                // TODO: i18n
                addMessage("Successfully updated theme");
                
            } catch(RollerException re) {
                log.error("Error saving weblog - "+getActionWeblog().getHandle(), re);
                addError("Error setting theme");
            }
        }
        
        return execute();
    }
    
    
    // validation
    private void myValidate() {
        
        String newTheme = getThemeId();
        
        // make sure theme is valid and enabled
        if(newTheme == null) {
            // TODO: i18n
            addError("No theme specified");
            
        } else {
            try {
                ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                Theme newThemeObj = themeMgr.getTheme(getThemeId());
                
                if(!newThemeObj.isEnabled()) {
                    // TODO: i18n
                    addError("Theme not enabled");
                }
                
            } catch(Exception ex) {
                log.warn(ex);
                // TODO: i18n
                addError("Theme not found");
            }
        }
    }
    
    
    public boolean isCustomTheme() {
        return (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme()));
    }
    
    
    public List getThemes() {
        return themes;
    }

    public void setThemes(List themes) {
        this.themes = themes;
    }

    public String getThemeType() {
        return themeType;
    }

    public void setThemeType(String themeType) {
        this.themeType = themeType;
    }
    
    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String theme) {
        this.themeId = theme;
    }

    public boolean isImportTheme() {
        return importTheme;
    }

    public void setImportTheme(boolean importTheme) {
        this.importTheme = importTheme;
    }

    public String getImportThemeId() {
        return importThemeId;
    }

    public void setImportThemeId(String importThemeId) {
        this.importThemeId = importThemeId;
    }
    
}
