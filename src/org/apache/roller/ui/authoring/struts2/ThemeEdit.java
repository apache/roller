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

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.themes.SharedTheme;
import org.apache.roller.business.themes.ThemeManager;
import org.apache.roller.business.themes.ThemeNotFoundException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.WeblogTheme;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;
import org.apache.struts.action.ActionMessage;


/**
 * Action for controlling theme selection.
 */
public class ThemeEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(Templates.class);
    
    private static TmpCustomTheme customThemeOption = new TmpCustomTheme();
    
    // list of available themes
    private List themes = Collections.EMPTY_LIST;
    
    // the chosen theme on a save() or customize()
    private String themeId = null;
    
    
    public ThemeEdit() {
        this.actionName = "themeEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "themeEditor.title";
    }
    
    
    // must be a weblog admin to use this action
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    // prepare list of themes based on action weblog
    public void myPrepare() {
        
        ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
        List themes = themeMgr.getEnabledThemesList();
        
        // should we also include the CUSTOM theme?
        boolean allowCustomTheme = false;
        if(getActionWeblog().getDefaultPageId() != null
                && !getActionWeblog().getDefaultPageId().equals("dummy")
                && !getActionWeblog().getDefaultPageId().trim().equals("")) {
            allowCustomTheme = true;
        }
        
        // if we allow custom themes then add it to the end of the list
        if(allowCustomTheme &&
                RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")) {
            
            themes.add(customThemeOption);
        }
        
        setThemes(themes);
    }
    
    
    public String execute() {
        try {
            // set theme to current value
            setThemeId(getActionWeblog().getTheme().getId());
        } catch (RollerException ex) {
            log.error("Error getting theme for weblog - "+getActionWeblog().getHandle(), ex);
        }
        
        return SUCCESS;
    }

    
    /**
     * Update chosen theme.
     */
    public String save() {
        
        // validation
        myValidate();
        
        // update theme for weblog and save
        if(!hasActionErrors()) {
            try {
                WebsiteData weblog = getActionWeblog();
                
                weblog.setEditorTheme(getThemeId());
                
                UserManager userMgr = RollerFactory.getRoller().getUserManager();
                userMgr.saveWebsite(weblog);
                RollerFactory.getRoller().flush();
                
                log.debug("Saved theme "+getThemeId()+" for weblog "+weblog.getHandle());
                
                // make sure to flush the page cache so ppl can see the change
                CacheManager.invalidate(weblog);
                
            } catch(RollerException re) {
                log.error("Error saving weblog - "+getActionWeblog().getHandle(), re);
                addError("Error setting theme");
            }
        }
        
        return SUCCESS;
    }
    
    
    /**
     * Customize a theme by copying it's templates.
     */
    public String customize() {
        
        ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
        
        // only if custom themes are allowed
        if(RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")) {
            try {
                SharedTheme usersTheme = themeMgr.getTheme(getActionWeblog().getEditorTheme());
                themeMgr.importTheme(getActionWeblog(), usersTheme);
                RollerFactory.getRoller().flush();
                
                // make sure to flush the page cache so ppl can see the change
                //PageCacheFilter.removeFromCache(request, weblog);
                CacheManager.invalidate(getActionWeblog());
                
            } catch(ThemeNotFoundException tnfe) {
                // this catches the potential case where someone customizes
                // a theme and has their theme as "custom" but then hits the
                // browser back button and presses the button again, so
                // they are basically trying to customize a "custom" theme
                
                // log this as a warning just in case
                log.warn(tnfe);
                
                // TODO: i18n
                addError("Oops!  You already have a custom theme.");
            } catch(RollerException re) {
                log.error("Error customizing theme for weblog - "+getActionWeblog().getHandle(), re);
                // TODO: i18n
                addError("Error customizing theme");
            }
        } else {
            // TODO: i18n
            addError("Sorry, custom themes are not allowed");
        }
            
        return SUCCESS;
    }
    
    
    // validation
    private void myValidate() {
        
        String newTheme = getThemeId();
        
        // make sure theme is valid and enabled
        if(newTheme == null) {
            // TODO: i18n
            addError("No theme specified");
            
        } else if(WeblogTheme.CUSTOM.equals(newTheme)) {
            if(!RollerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")) {
                // TODO: i18n
                addError("Sorry, custom themes are not allowed");
            }
            
        } else {
            try {
                Roller roller = RollerFactory.getRoller();
                ThemeManager themeMgr = roller.getThemeManager();
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
    
    
    public List getThemes() {
        return themes;
    }

    public void setThemes(List themes) {
        this.themes = themes;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String theme) {
        this.themeId = theme;
    }
    
    
    static class TmpCustomTheme {
        public String getId() { return WeblogTheme.CUSTOM; }
        public String getName() { return WeblogTheme.CUSTOM; }
    }
    
}
