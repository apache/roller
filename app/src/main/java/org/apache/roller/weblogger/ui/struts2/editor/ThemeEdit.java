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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Action for controlling theme selection.
 */
@RestController
public class ThemeEdit extends UIAction {

    private static final long serialVersionUID = 4644653507344432426L;

    private static Log log = LogFactory.getLog(Templates.class);

    @Autowired
    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

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

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    public ThemeEdit() {
        this.actionName = "themeEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "themeEditor.title";
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public void prepare() {
        themes = themeManager.getEnabledThemesList();

        // See if we're using a shared theme with a custom template
        try {
            if (getActionWeblog().getTheme().getTemplateByAction(ComponentType.STYLESHEET) != null) {

                ThemeTemplate override = weblogManager.getTemplateByLink(getActionWeblog(),
                                getActionWeblog().getTheme().getTemplateByAction(ComponentType.STYLESHEET).getLink());
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
        setThemeId(getActionWeblog().getEditorTheme());
        setSelectedThemeId(getThemeId());
        return INPUT;
    }

    /**
     * Save new theme configuration.
     */
    public String save() {

        Weblog weblog = getActionWeblog();

        // Are we dealing with a custom theme scenario?
        if ("custom".equals(getThemeType())) {
            SharedTheme t = null;

            // do theme import if requested
            if (importTheme) {
                try {
                    if (!StringUtils.isEmpty(selectedThemeId)) {
                        t = themeManager.getTheme(selectedThemeId);
                        // if moving from shared w/custom SS to custom import of same shared theme,
                        // keep the custom stylesheet.
                        boolean skipStylesheet = (sharedThemeCustomStylesheet && selectedThemeId.equals(weblog.getEditorTheme()));
                        themeManager.importTheme(getActionWeblog(), t, skipStylesheet);
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
                    if (t != null) {
                        weblog.setEditorTheme(t.getId());
                    }
                    weblogManager.saveWeblog(weblog);
                    persistenceStrategy.flushAndInvalidateWeblog(weblog);

                    addMessage("themeEditor.setTheme.success", t != null ? t.getName() : "custom");
                    addMessage("themeEditor.setCustomTheme.instructions");

                } catch (WebloggerException re) {
                    log.error("Error saving weblog - " + getActionWeblog().getHandle(), re);
                    addError("generic.error.check.logs");
                }
            }

        } else if ("shared".equals(getThemeType())) {

            // make sure theme is valid and enabled
            Theme newTheme = null;

            try {
                newTheme = themeManager.getTheme(selectedThemeId);
            } catch (Exception ex) {
                log.warn(ex);
                addError("Theme not found");
            }

            if (!hasActionErrors()) {
                try {
                    String originalTheme = weblog.getEditorTheme();

                    // Remove old style sheet
                    if (!originalTheme.equals(selectedThemeId) && getActionWeblog().getTheme().getTemplateByAction(ComponentType.STYLESHEET) != null) {
                        WeblogTemplate stylesheet = weblogManager.getTemplateByAction(getActionWeblog(),
                                ComponentType.STYLESHEET);

                        if (stylesheet != null) {
                            // Remove template and its renditions
                            weblogManager.removeTemplate(stylesheet);
                            sharedThemeCustomStylesheet = false;
                        }
                    }

                    weblog.setEditorTheme(selectedThemeId);

                    log.debug("Saving theme " + selectedThemeId + " for weblog "
                            + weblog.getHandle());

                    // save updated weblog and flush
                    weblogManager.saveWeblog(weblog);
                    persistenceStrategy.flushAndInvalidateWeblog(weblog);

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
        return false;
    }

    // has this weblog had a custom theme before?
    public boolean isFirstCustomization() {
        try {
            return (weblogManager.getTemplateByAction(getActionWeblog(), ComponentType.WEBLOG) == null);
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

    @RequestMapping(value = "/roller-ui/authoring/rest/themes", method = RequestMethod.GET)
    public List<SharedThemeData> getSharedThemes() {
        List<SharedTheme> list = themeManager.getEnabledThemesList();
        List<SharedThemeData> to = new ArrayList<>();
        for (SharedTheme item : list) {
            SharedThemeData temp = new SharedThemeData();
            temp.setId(item.getId());
            temp.setName(item.getName());
            temp.setDescription(item.getDescription());
            temp.setPreviewPath("/themes/" + item.getId() + "/" + item.getPreviewImagePath());
            to.add(temp);
        }
        return to;
    }

    private static class SharedThemeData {
        public SharedThemeData() {}

        private String id;
        private String name;
        private String description;
        private String previewPath;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getPreviewPath() {
            return previewPath;
        }

        public void setPreviewPath(String previewPath) {
            this.previewPath = previewPath;
        }
    }

}

