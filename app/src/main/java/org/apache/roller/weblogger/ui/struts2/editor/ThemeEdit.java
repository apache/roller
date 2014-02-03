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

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Action for controlling theme selection.
 */
public class ThemeEdit extends UIAction {

    private static final long serialVersionUID = 4644653507344432426L;

    private static Log log = LogFactory.getLog(Templates.class);

    // list of available themes
    private List<Theme> themes = Collections.emptyList();

    // type of theme desired, either 'shared' or 'custom'
    private String themeType = null;

    // the chosen shared theme id
    private String themeId = null;

    // import the selected theme to the action weblog
    private boolean importTheme = false;

    // the chosen import theme id
    private String importThemeId = null;

    // Do we have a custom stylesheet already
    private boolean customStylesheet = false;

    public ThemeEdit() {
        this.actionName = "themeEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "themeEditor.title";
    }

    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }

    public void myPrepare() {

        ThemeManager themeMgr = WebloggerFactory.getWeblogger()
                .getThemeManager();
        setThemes(themeMgr.getEnabledThemesList());
    }

    public String execute() {

        // set theme to current value
        if (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
            setThemeId(null);
        } else {
            setThemeId(getActionWeblog().getTheme().getId());
            setImportThemeId(getActionWeblog().getTheme().getId());
        }

        // See if we have a custom style sheet from a custom theme.
        try {
            if (!WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())
                    && getActionWeblog().getTheme().getStylesheet() != null) {

                ThemeTemplate override = WebloggerFactory
                        .getWeblogger()
                        .getWeblogManager()
                        .getPageByLink(
                                getActionWeblog(),
                                getActionWeblog().getTheme().getStylesheet()
                                        .getLink());
                if (override != null) {
                    customStylesheet = true;
                }
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up stylesheet on weblog - "
                    + getActionWeblog().getHandle(), ex);
        }

        if (!WebloggerRuntimeConfig
                .getBooleanProperty("themes.customtheme.allowed")) {
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

        // we are dealing with a custom theme scenario
        if (WeblogTheme.CUSTOM.equals(getThemeType())) {

            // only continue if custom themes are allowed
            if (WebloggerRuntimeConfig
                    .getBooleanProperty("themes.customtheme.allowed")) {

                // do theme import if necessary
                SharedTheme t = null;
                if (isImportTheme() && !StringUtils.isEmpty(getImportThemeId())) {
                    try {
                        ThemeManager themeMgr = WebloggerFactory.getWeblogger()
                                .getThemeManager();
                        t = themeMgr.getTheme(getImportThemeId());
                        themeMgr.importTheme(getActionWeblog(), t);
                    } catch (Exception re) {
                        log.error("Error customizing theme for weblog - "
                                + getActionWeblog().getHandle(), re);
                        // TODO: i18n
                        addError("Error importing theme");
                    }
                }

                if (!hasActionErrors()) {
                    try {
                        weblog.setEditorTheme(WeblogTheme.CUSTOM);
                        log.debug("Saving custom theme for weblog "
                                + weblog.getHandle());

                        // save updated weblog and flush
                        WebloggerFactory.getWeblogger().getWeblogManager()
                                .saveWeblog(weblog);
                        WebloggerFactory.getWeblogger().flush();

                        // make sure to flush the page cache so ppl can see the
                        // change
                        CacheManager.invalidate(weblog);

                        // TODO: i18n
                        addMessage("Successfully set theme to - "
                                + WeblogTheme.CUSTOM);
                        if (t != null) {
                            addMessage("Successfully copied templates from theme - "
                                    + t.getName());
                        }

                        // reset import theme options
                        setImportTheme(false);
                        setImportThemeId(null);

                    } catch (WebloggerException re) {
                        log.error("Error saving weblog - "
                                + getActionWeblog().getHandle(), re);
                        addError("Error setting theme");
                    }
                }
            } else {
                // TODO: i18n
                addError("Sorry, custom themes are not allowed");
            }

            // we are dealing with a shared theme scenario
        } else if ("shared".equals(getThemeType())) {

            // make sure theme is valid and enabled
            Theme newTheme = null;
            if (getThemeId() == null) {
                // TODO: i18n
                addError("No theme specified");

            } else {
                try {
                    ThemeManager themeMgr = WebloggerFactory.getWeblogger()
                            .getThemeManager();
                    newTheme = themeMgr.getTheme(getThemeId());

                    if (!newTheme.isEnabled()) {
                        // TODO: i18n
                        addError("Theme not enabled");
                    }

                } catch (Exception ex) {
                    log.warn(ex);
                    // TODO: i18n
                    addError("Theme not found");
                }
            }

            if (!hasActionErrors()) {
                try {

                    String originalTheme = weblog.getEditorTheme();

                    WeblogManager mgr = WebloggerFactory.getWeblogger()
                            .getWeblogManager();

                    // Remove old style sheet
                    if (!originalTheme.equals(getThemeId())
                            && getActionWeblog().getTheme().getStylesheet() != null) {

                        WeblogTemplate stylesheet = mgr.getPageByLink(
                                getActionWeblog(), getActionWeblog().getTheme()
                                        .getStylesheet().getLink());

                        if (stylesheet != null) {
                            // Remove template and page codes
                            mgr.removePage(stylesheet);
                            // Reset
                            weblog.setCustomStylesheetPath(null);
                        }
                    }

                    weblog.setEditorTheme(getThemeId());

                    log.debug("Saving theme " + getThemeId() + " for weblog "
                            + weblog.getHandle());

                    // save updated weblog and flush
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveWeblog(weblog);
                    WebloggerFactory.getWeblogger().flush();

                    // make sure to flush the page cache so ppl can see the
                    // change
                    CacheManager.invalidate(weblog);

                    // Theme set to..
                    if (!originalTheme.equals(getThemeId())) {
                        addMessage("themeEditor.settheme", newTheme.getName());
                    }

                } catch (WebloggerException re) {
                    log.error("Error saving weblog - "
                            + getActionWeblog().getHandle(), re);
                    addError("Error setting theme");
                }
            }

            // unknown theme scenario, error
        } else {
            // invalid theme type
            // TODO: i18n
            addError("no valid theme type submitted");
        }

        return execute();
    }

    public boolean isCustomTheme() {
        return (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme()));
    }

    // has this weblog had a custom theme before?
    public boolean isFirstCustomization() {
        try {
            return (WebloggerFactory
                    .getWeblogger()
                    .getWeblogManager()
                    .getPageByAction(getActionWeblog(),
                            WeblogTemplate.ACTION_WEBLOG) == null);
        } catch (WebloggerException ex) {
            log.error("Error looking up weblog template", ex);
        }
        return false;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
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

    /**
     * Checks if we have a custom stylesheet.
     * 
     * @return true, if checks if is custom stylesheet
     */
    public boolean isCustomStylesheet() {
        return customStylesheet;
    }

    /**
     * Sets the custom stylesheet.
     * 
     * @param customStylesheet
     *            the custom stylesheet
     */
    public void setCustomStylesheet(boolean customStylesheet) {
        this.customStylesheet = customStylesheet;
    }

}
