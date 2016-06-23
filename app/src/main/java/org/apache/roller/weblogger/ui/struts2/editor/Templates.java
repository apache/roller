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

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.Parser;
import org.apache.roller.weblogger.pojos.Template.ComponentType;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Templates listing page.
 */
@RestController
public class Templates extends UIAction {

	private static Logger log = LoggerFactory.getLogger(Templates.class);

	private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

	// name and action of new template if we are adding a template
	private String newTmplName = null;
	private ComponentType newTmplAction = null;

	@Autowired
	private JPAPersistenceStrategy persistenceStrategy = null;

	public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
		this.persistenceStrategy = persistenceStrategy;
	}

	@Autowired
	private UserManager userManager;

	public void setUserManager(UserManager userManager) {
		this.userManager = userManager;
	}

    @Autowired
	private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

	@Autowired
	private ThemeManager themeManager;

	public void setThemeManager(ThemeManager themeManager) {
		this.themeManager = themeManager;
	}

    public Templates() {
		this.actionName = "templates";
		this.desiredMenu = "editor";
		this.pageTitle = "templates.title";
		this.requiredGlobalRole = GlobalRole.BLOGGER;
	}

	@RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}/templates", method = RequestMethod.GET)
	public WeblogTemplateData getWeblogTemplates(@PathVariable String id, Principal principal,
												 HttpServletResponse response) {

		Weblog weblog = weblogManager.getWeblog(id);
		if (weblog != null && userManager.checkWeblogRole(principal.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

			WeblogTemplateData wtd = new WeblogTemplateData();

			WeblogTheme theme = new WeblogTheme(weblogManager, weblog,
					themeManager.getSharedTheme(weblog.getTheme()));

			List<? extends Template> raw = theme.getTemplates();
			List<Template> pages = new ArrayList<>();
			pages.addAll(raw);
			wtd.templates = pages;

			// build list of action types that may be added
			List<ComponentType> availableRoles = new ArrayList<>(Arrays.asList(ComponentType.values()));

			// remove from above list any already existing for the theme
			pages.stream().filter(p -> p.getRole().isSingleton()).forEach(p ->
				availableRoles.removeIf(r -> r.name().equals(p.getRole().name())));

			wtd.availableTemplateRoles = availableRoles;
			return wtd;
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			return null;
		}
	}


	@JsonInclude(JsonInclude.Include.NON_NULL)
	public class WeblogTemplateData {
		List<Template> templates;
		List<ComponentType> availableTemplateRoles;

		public List<Template> getTemplates() {
			return templates;
		}

		public void setTemplates(List<Template> templates) {
			this.templates = templates;
		}

		public List<ComponentType> getAvailableTemplateRoles() {
			return availableTemplateRoles;
		}

		public void setAvailableTemplateRoles(List<ComponentType> availableTemplateRoles) {
			this.availableTemplateRoles = availableTemplateRoles;
		}
	}

	@RequestMapping(value = "/tb-ui/authoring/rest/template/{id}", method = RequestMethod.DELETE)
	public void deleteTemplate(@PathVariable String id, Principal p, HttpServletResponse response)
			throws ServletException {

		try {
			WeblogTemplate template = weblogManager.getTemplate(id);
			if (template != null) {
				if (userManager.checkWeblogRole(p.getName(), template.getWeblog().getHandle(), WeblogRole.OWNER)) {
					weblogManager.removeTemplate(template);
					persistenceStrategy.flush();
					response.setStatus(HttpServletResponse.SC_OK);
				} else {
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				}
			} else {
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (Exception e) {
			log.error("Error deleting template with ID: ", id, e);
			throw new ServletException(e.getMessage());
		}
	}

	@RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templates", method = RequestMethod.PUT)
	public ResponseEntity addTemplate(@PathVariable String weblogId, @RequestBody WeblogTemplate incomingTemplateData,
							Principal p, HttpServletResponse response)
			throws ServletException {
		try {
			Weblog weblog = weblogManager.getWeblog(weblogId);
			if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
				ValidationError maybeError = advancedValidate(incomingTemplateData);
				if (maybeError != null) {
					return ResponseEntity.badRequest().body(maybeError);
				}

				WeblogTemplate newTemplate = new WeblogTemplate();
				newTemplate.setId(WebloggerCommon.generateUUID());
				newTemplate.setWeblog(weblog);
				newTemplate.setRole(incomingTemplateData.getRole());
				newTemplate.setName(incomingTemplateData.getName());
				newTemplate.setLastModified(LocalDateTime.now());

				// save the new Template
				weblogManager.saveTemplate(newTemplate);

				// Create weblog template codes for available types.
				WeblogTemplateRendition standardRendition = new WeblogTemplateRendition(
						newTemplate, RenditionType.NORMAL);
				if (newTmplAction != ComponentType.STYLESHEET && newTmplAction != ComponentType.JAVASCRIPT) {
					standardRendition.setRendition(getText("templateEdit.newTemplateContent"));
				}
				standardRendition.setParser(Parser.VELOCITY);
				weblogManager.saveTemplateRendition(standardRendition);

				// flush results to db
				WebloggerFactory.flush();

				return ResponseEntity.ok(newTemplate);
			} else {
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			}
		} catch (Exception e) {
			throw new ServletException(e.getMessage());
		}
	}

	/**
	 * Save a new template.
	 */
	public String add() {

		if (!hasActionErrors()) {
			WeblogTemplate newTemplate = new WeblogTemplate();
			newTemplate.setId(WebloggerCommon.generateUUID());
			newTemplate.setWeblog(getActionWeblog());
			newTemplate.setRole(getNewTmplAction());
			newTemplate.setName(getNewTmplName());
			newTemplate.setLastModified(LocalDateTime.now());

			// save the new Template
			weblogManager.saveTemplate(newTemplate);

			// Create weblog template codes for available types.
			WeblogTemplateRendition standardRendition = new WeblogTemplateRendition(
					newTemplate, RenditionType.NORMAL);
			if (newTmplAction != ComponentType.STYLESHEET && newTmplAction != ComponentType.JAVASCRIPT) {
				standardRendition.setRendition(getText("templateEdit.newTemplateContent"));
			}
			standardRendition.setParser(Parser.VELOCITY);
			weblogManager.saveTemplateRendition(standardRendition);

			// flush results to db
			WebloggerFactory.flush();

			// reset form fields
			setNewTmplName(null);
			setNewTmplAction(null);
		}

		return SUCCESS;
	}

	private ValidationError advancedValidate(WeblogTemplate data) {
		BindException be = new BindException(data, "new data object");

		WeblogTemplate template = weblogManager.getTemplateByName(data.getWeblog(), data.getName());
		if (template != null && !template.getId().equals(data.getId())) {
			be.addError(new ObjectError("WeblogTemplate", bundle.getString("templates.error.nameAlreadyExists")));
		}

		if (data.getRole().isSingleton()) {
			template = weblogManager.getTemplateByAction(data.getWeblog(), data.getRole());
			if (template != null && !template.getId().equals(data.getId())) {
				be.addError(new ObjectError("WeblogTemplate",
						bundle.getString("templates.error.singletonActionAlreadyExists")));
			}
		}

		return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
	}

	public String newTheme() {
		SharedTheme theme = themeManager.getSharedTheme(getActionWeblog().getTheme());
		addMessage("themeEditor.setTheme.success", theme.getName());
		return SUCCESS;
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

}
