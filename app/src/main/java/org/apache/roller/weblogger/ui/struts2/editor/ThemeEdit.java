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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Template.TemplateDerivation;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
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

    // list of available shared themes
    private List<SharedTheme> themes = Collections.emptyList();

    // the currently selected theme
    private String themeId = null;

    // a potentially new selected theme
    private String selectedThemeId = null;

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
        themes = themeManager.getEnabledSharedThemesList();
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

        // make sure theme is valid and enabled
        SharedTheme newTheme;

        try {
            // before switching themes, ensure no conflict with already existing blog-only themes.
            newTheme = themeManager.getSharedTheme(selectedThemeId);

            WeblogTheme oldTheme = new WeblogTheme(weblogManager, getActionWeblog(),
                    themeManager.getSharedTheme(getActionWeblog().getEditorTheme()));

            oldTheme.getTemplates().stream().filter(
                    old -> old.getDerivation() == TemplateDerivation.NONSHARED).forEach(old -> {
                if (newTheme.getTemplateByName(old.getName()) != null) {
                    addError("themeEditor.conflicting.name", old.getName());
                }
                String maybePath = old.getRelativePath();
                if (maybePath != null && newTheme.getTemplateByPath(maybePath) != null) {
                    addError("themeEditor.conflicting.link", old.getName());
                }
            });

            if (!hasActionErrors()) {
                try {
                    // Remove old template overrides and their renditions
                    List<WeblogTemplate> oldTemplates = weblogManager.getTemplates(getActionWeblog());

                    for (WeblogTemplate template : oldTemplates) {
                        // Remove template overrides and their renditions
                        if (template.getDerivation() == TemplateDerivation.OVERRIDDEN) {
                            weblogManager.removeTemplate(template);
                        }
                    }

                    weblog.setEditorTheme(selectedThemeId);

                    log.debug("Saving theme " + selectedThemeId + " for weblog " + weblog.getHandle());

                    // save updated weblog and flush
                    weblogManager.saveWeblog(weblog);
                    persistenceStrategy.flushAndInvalidateWeblog(weblog);

                    // Theme set to..
                    addMessage("themeEditor.setTheme.success", newTheme.getName());

                } catch (WebloggerException re) {
                    log.error("Error saving weblog - " + getActionWeblog().getHandle(), re);
                    addError("generic.error.check.logs");
                }
            }
        } catch (Exception ex) {
            log.warn(ex);
            addError("Theme not found");
        }

        return execute();
    }

    public List<SharedTheme> getThemes() {
        return themes;
    }

    public String getThemeId() {
        return themeId;
    }

    public void setThemeId(String theme) {
        this.themeId = theme;
    }

    public String getSelectedThemeId() {
        return selectedThemeId;
    }

    public void setSelectedThemeId(String importThemeId) {
        this.selectedThemeId = importThemeId;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/themes", method = RequestMethod.GET)
    public List<SharedThemeData> getSharedThemes() {
        List<SharedTheme> list = themeManager.getEnabledSharedThemesList();
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
