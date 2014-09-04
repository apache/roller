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
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;
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
    private List<SharedTheme> themes = Collections.emptyList();

    // type of theme desired, either 'shared' or 'custom'
    private String themeType = null;

    // the currently selected theme, shared or custom
    private String themeId = null;

    // import a shared theme into the blog's custom templates
    private boolean importTheme = false;

    // a potentially new selected theme
    private String selectedThemeId = null;

    // Are we using a shared theme with a custom stylesheet
    private boolean sharedThemeCustomStylesheet = false;

    public ThemeEdit() {
        this.actionName = "themeEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "themeEditor.title";
    }

    public void myPrepare() {
        ThemeManager themeMgr = WebloggerFactory.getWeblogger()
                .getThemeManager();
        themes = themeMgr.getEnabledThemesList();

        // See if we're using a shared theme with a custom template
        try {
            if (!WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())
                    && getActionWeblog().getTheme().getStylesheet() != null) {

                ThemeTemplate override = WebloggerFactory
                        .getWeblogger()
                        .getWeblogManager()
                        .getTemplateByLink(
                                getActionWeblog(),
                                getActionWeblog().getTheme().getStylesheet()
                                        .getLink());
                if (override != null) {
                    sharedThemeCustomStylesheet = true;
                }
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up stylesheet on weblog - "
                    + getActionWeblog().getHandle(), ex);
        }
    }

    public String execute() {

        // set theme to current value
        if (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
            setThemeId(null);
            setSelectedThemeId(null);
            setImportTheme(false);
        } else {
            setThemeId(getActionWeblog().getTheme().getId());
            setSelectedThemeId(getThemeId());
        }

        return INPUT;
    }

    /**
     * Save new theme configuration.
     */
    public String save() {

        Weblog weblog = getActionWeblog();

        // Are we dealing with a custom theme scenario?
        if (WeblogTheme.CUSTOM.equals(getThemeType())) {

            // do theme import if requested
            if (importTheme) {
                try {
                    ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
                    if (!StringUtils.isEmpty(selectedThemeId)) {
                        SharedTheme t = themeMgr.getTheme(selectedThemeId);
                        // if moving from shared w/custom SS to custom import of same shared theme,
                        // keep the custom stylesheet.
                        boolean skipStylesheet = (sharedThemeCustomStylesheet && selectedThemeId.equals(weblog.getEditorTheme()));
                        themeMgr.importTheme(getActionWeblog(), t, skipStylesheet);
                        addMessage("themeEditor.setCustomTheme.success", t.getName());
                    }
                } catch (Exception re) {
                    log.error("Error customizing theme for weblog - "
                            + getActionWeblog().getHandle(), re);
                    addError("generic.error.check.logs");
                    return execute();
                }
            }

            if (!hasActionErrors()) {
                try {
                    // save updated weblog and flush
                    weblog.setEditorTheme(WeblogTheme.CUSTOM);
                    WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(weblog);
                    WebloggerFactory.getWeblogger().flush();

                    // make sure to flush the page cache so ppl can see the change
                    CacheManager.invalidate(weblog);

                    addMessage("themeEditor.setTheme.success", WeblogTheme.CUSTOM);
                    addMessage("themeEditor.setCustomTheme.instructions");

                } catch (WebloggerException re) {
                    log.error("Error saving weblog - "
                            + getActionWeblog().getHandle(), re);
                    addError("generic.error.check.logs");
                }
            }

        } else if ("shared".equals(getThemeType())) {

            // make sure theme is valid and enabled
            Theme newTheme = null;

            try {
                ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
                newTheme = themeMgr.getTheme(selectedThemeId);
            } catch (Exception ex) {
                log.warn(ex);
                addError("Theme not found");
            }

            if (!hasActionErrors()) {
                try {
                    String originalTheme = weblog.getEditorTheme();

                    WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();

                    // Remove old style sheet
                    if (!originalTheme.equals(selectedThemeId) && getActionWeblog().getTheme().getStylesheet() != null) {
                        WeblogTemplate stylesheet = mgr.getTemplateByAction(getActionWeblog(),
                                ComponentType.STYLESHEET);

                        if (stylesheet != null) {
                            // Remove template and its renditions
                            mgr.removeTemplate(stylesheet);
                            sharedThemeCustomStylesheet = false;
                        }
                    }

                    weblog.setEditorTheme(selectedThemeId);

                    log.debug("Saving theme " + selectedThemeId + " for weblog "
                            + weblog.getHandle());

                    // save updated weblog and flush
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveWeblog(weblog);
                    WebloggerFactory.getWeblogger().flush();

                    // make sure to flush the page cache so ppl can see the change
                    CacheManager.invalidate(weblog);

                    // Theme set to..
                    if (!originalTheme.equals(selectedThemeId)) {
                        addMessage("themeEditor.setTheme.success", newTheme.getName());
                    }

                } catch (WebloggerException re) {
                    log.error("Error saving weblog - " + getActionWeblog().getHandle(), re);
                    addError("generic.error.check.logs");
                }
            }
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
                    .getTemplateByAction(getActionWeblog(),
                            ComponentType.WEBLOG) == null);
        } catch (WebloggerException ex) {
            log.error("Error looking up weblog template", ex);
        }
        return false;
    }

    public List<SharedTheme> getThemes() {
        return themes;
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

    public String getSelectedThemeId() {
        return selectedThemeId;
    }

    public void setSelectedThemeId(String importThemeId) {
        this.selectedThemeId = importThemeId;
    }

    /**
     * Checks if we are using a shared theme with a custom stylesheet.
     * 
     * @return true, if using a shared theme with a custom stylesheet; false otherwise.
     */
    public boolean isSharedThemeCustomStylesheet() {
        return sharedThemeCustomStylesheet;
    }

}
