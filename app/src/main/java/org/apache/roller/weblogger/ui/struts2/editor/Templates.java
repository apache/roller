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
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.TemplateRuleException;
import org.apache.roller.weblogger.business.themes.WeblogTemplateEditor;
import org.apache.roller.weblogger.pojos.*;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.convention.annotation.AllowedMethods;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Templates listing page.
 */
// TODO: make this work @AllowedMethods({"execute","add"})
public class Templates extends UIAction {

    private static final Log log = LogFactory.getLog(Templates.class);

    // list of templates to display
    private List<WeblogTemplate> templates = Collections.emptyList();

    // list of template action types user is allowed to create
    private Map<ComponentType, String> availableActions = Collections.emptyMap();

    // name and action of new template if we are adding a template
    private String newTmplName = null;
    private ComponentType newTmplAction = null;

    // id of template to remove
    private String removeId = null;

    public Templates() {
        this.actionName = "templates";
        this.desiredMenu = "editor";
        this.pageTitle = "pagesForm.title";
    }

    @Override
    public String execute() {

        // query for templates list
        try {

            // get current list of templates, minus custom stylesheet
            List<WeblogTemplate> raw = WebloggerFactory.getWeblogger()
                .getWeblogManager().getTemplates(getActionWeblog());
            List<WeblogTemplate> pages = new ArrayList<>(raw);

            // Remove style sheet from list so not to show when theme is
            // selected in shared theme mode
            if (getActionWeblog().getTheme().getStylesheet() != null) {
                pages.remove(WebloggerFactory.getWeblogger().getWeblogManager()
                    .getTemplateByLink(getActionWeblog(), getActionWeblog().getTheme().getStylesheet().getLink()));
            }
            setTemplates(pages);

            // build list of action types that may be added
            Map<ComponentType, String> actionsMap = new EnumMap<>(ComponentType.class);
            for (ComponentType action : templateEditor().availableActions(getActionWeblog())) {
                addComponentTypeToMap(actionsMap, action);
            }
            if (!WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
                // Preselect the default web page while it can still be added
                if (actionsMap.containsKey(ComponentType.WEBLOG)) {
                    if (getNewTmplAction() == null) {
                        setNewTmplAction(ComponentType.WEBLOG);
                    }
                } else {
                    setNewTmplAction(null);
                }
            }
            setAvailableActions(actionsMap);

        } catch (WebloggerException ex) {
            log.error("Error getting templates for weblog - "
                + getActionWeblog().getHandle(), ex);
            addError("Error getting template list - check Roller logs");
        }

        return LIST;
    }

    private void addComponentTypeToMap(Map<ComponentType, String> map, ComponentType component) {
        map.put(component, component.getReadableName());
    }

    /**
     * Save a new template.
     */
    public String add() {

        try {
            templateEditor().create(getActionWeblog(), getNewTmplAction(), getNewTmplName(),
                    getText("pageForm.newTemplateContent"), null);

            // flush results to db
            WebloggerFactory.getWeblogger().flush();

            // reset form fields
            setNewTmplName(null);
            setNewTmplAction(null);

        } catch (TemplateRuleException ex) {
            addErrors(ex);
        } catch (WebloggerException ex) {
            log.error("Error adding new template for weblog - " + getActionWeblog().getHandle(), ex);
            addError("Error adding new template - check Roller logs");
        }

        return execute();
    }

    /**
     * Remove a new template.
     */
    public String remove() {

        WeblogTemplate template = null;
        try {
            template = WebloggerFactory.getWeblogger().getWeblogManager().getTemplate(getActionWeblog(), getRemoveId());
        } catch (WebloggerException e) {
            addError("Error deleting template - check Roller logs");
        }

        if (template != null) {
            try {
                templateEditor().remove(getActionWeblog(), template);
                WebloggerFactory.getWeblogger().flush();
            } catch (TemplateRuleException ex) {
                addErrors(ex);
            } catch (Exception ex) {
                log.error("Error removing page - " + getRemoveId(), ex);
                addError("editPages.remove.error");
            }
        } else {
            addError("editPages.remove.error");
        }

        return execute();
    }

    private WeblogTemplateEditor templateEditor() {
        return new WeblogTemplateEditor(WebloggerFactory.getWeblogger());
    }

    private void addErrors(TemplateRuleException ex) {
        for (TemplateRuleException.Violation violation : ex.getViolations()) {
            addError(violation.getMessageKey(), violation.getArgs());
        }
    }

    /**
     * Checks if is custom theme.
     *
     * @return true, if is custom theme
     */
    public boolean isCustomTheme() {
        return (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme()));
    }

    public List<WeblogTemplate> getTemplates() {
        return templates;
    }

    public void setTemplates(List<WeblogTemplate> templates) {
        this.templates = templates;
    }

    public Map<ComponentType, String> getAvailableActions() {
        return availableActions;
    }

    public void setAvailableActions(Map<ComponentType, String> availableActions) {
        this.availableActions = availableActions;
    }

    public String getNewTmplName() {
        return newTmplName;
    }

    public void setNewTmplName(String newTmplName) {
        this.newTmplName = newTmplName;
    }

    public ComponentType getNewTmplAction() {
        return newTmplAction;
    }

    public void setNewTmplAction(ComponentType newTmplAction) {
        this.newTmplAction = newTmplAction;
    }

    public String getRemoveId() {
        return removeId;
    }

    public void setRemoveId(String removeId) {
        this.removeId = removeId;
    }
}
