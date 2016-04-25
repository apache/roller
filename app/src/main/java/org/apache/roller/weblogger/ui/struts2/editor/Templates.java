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
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.Template.ComponentType;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Templates listing page.
 */
@RestController
public class Templates extends UIAction {

	private static Log log = LogFactory.getLog(Templates.class);

	// list of templates to display
	private List<Template> templates = Collections.emptyList();

	// list of template action types user is allowed to create
	private List<Pair<String, String>> availableRoles;

	// name and action of new template if we are adding a template
	private String newTmplName = null;
	private ComponentType newTmplAction = null;

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

	private ThemeManager themeManager;

	public void setThemeManager(ThemeManager themeManager) {
		this.themeManager = themeManager;
	}

    public Templates() {
		this.actionName = "templates";
		this.desiredMenu = "editor";
		this.pageTitle = "templates.title";
	}

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

	public String execute() {

		// query for templates list
		try {

			// get current list of templates defined for the blog
			WeblogTheme theme = new WeblogTheme(weblogManager, getActionWeblog(),
					themeManager.getSharedTheme(getActionWeblog().getTheme()));

			List<? extends Template> raw = theme.getTemplates();
			List<Template> pages = new ArrayList<>();
			pages.addAll(raw);
			setTemplates(pages);

			// build list of action types that may be added
            availableRoles = new ArrayList<>();
            for (ComponentType type : ComponentType.values()) {
                availableRoles.add(Pair.of(type.name(), type.getReadableName()));
            }

            // remove from above list any already existing for the theme
            for (Template tmpPage : getTemplates()) {
                if (tmpPage.getRole().isSingleton()) {
                    availableRoles.removeIf(p -> p.getLeft().equals(tmpPage.getRole().name()));
                }
            }
			setAvailableRoles(availableRoles);

		} catch (WebloggerException ex) {
			log.error("Error getting templates for weblog - " + getActionWeblog().getHandle(), ex);
			addError("Error getting template list - check server logfiles");
		}

		return LIST;
	}

	/**
	 * Save a new template.
	 */
	public String add() {

		// validation
		myValidate();

		if (!hasActionErrors()) {
            try {
                WeblogTemplate newTemplate = new WeblogTemplate();
				newTemplate.setId(WebloggerCommon.generateUUID());
				newTemplate.setWeblog(getActionWeblog());
                newTemplate.setRole(getNewTmplAction());
                newTemplate.setName(getNewTmplName());
                newTemplate.setLastModified(new Date());

                // save the new Template
                weblogManager.saveTemplate(newTemplate);

                // Create weblog template codes for available types.
                WeblogTemplateRendition standardRendition = new WeblogTemplateRendition(
                        newTemplate, RenditionType.NORMAL);
				if (newTmplAction != ComponentType.STYLESHEET && newTmplAction != ComponentType.JAVASCRIPT) {
					standardRendition.setTemplate(getText("templateEdit.newTemplateContent"));
				}
                standardRendition.setTemplateLanguage(TemplateLanguage.VELOCITY);
                weblogManager.saveTemplateRendition(standardRendition);

                // flush results to db
                WebloggerFactory.flush();

                // reset form fields
                setNewTmplName(null);
                setNewTmplAction(null);

            } catch (WebloggerException ex) {
                log.error("Error adding new template for weblog - "
                        + getActionWeblog().getHandle(), ex);
                addError("Error adding new template - check TightBlog logs");
            }
        }

		return execute();
	}

	// validation when adding a new template
	private void myValidate() {

		// make sure name is non-null and within proper size
		if (StringUtils.isEmpty(getNewTmplName())) {
			addError("templates.error.nameNull");
		} else if (getNewTmplName().length() > WebloggerCommon.TEXTWIDTH_255) {
			addError("templates.error.nameSize");
		}

		// make sure action is a valid
		if (getNewTmplAction() == null) {
			addError("templates.error.actionNull");
		}

		// check if template by that name already exists
		try {
			WeblogTheme testTheme = new WeblogTheme(weblogManager, getActionWeblog(),
					themeManager.getSharedTheme(getActionWeblog().getTheme()));

			if (testTheme.getTemplateByName(getNewTmplName()) != null) {
				addError("templates.error.nameAlreadyExists", getNewTmplName());
			}
		} catch (WebloggerException ex) {
			log.error("Error checking for existing template", ex);
		}

	}

	public List<Template> getTemplates() {
		return templates;
	}

	public void setTemplates(List<Template> templates) {
		this.templates = templates;
	}

	public List<Pair<String, String>> getAvailableRoles() {
		return availableRoles;
	}

	public void setAvailableRoles(List<Pair<String, String>> availableRoles) {
		this.availableRoles = availableRoles;
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

    @RequestMapping(value = "/tb-ui/authoring/rest/templateDescriptions/{role}", method = RequestMethod.GET)
    public String getTemplateComponentTypeDescription(@PathVariable String role) {
        ComponentType desiredType = ComponentType.valueOf(role);
        ResourceBundle rb = ResourceBundle.getBundle("ApplicationResources");
        return rb.getString(desiredType.getDescriptionProperty());
    }
}
