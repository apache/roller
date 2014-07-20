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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Templates listing page.
 */
public class Templates extends UIAction {

	private static Log log = LogFactory.getLog(Templates.class);

	// list of templates to display
	private List<WeblogTemplate> templates = Collections.emptyList();

	// list of template action types user is allowed to create
	private List availableActions = Collections.emptyList();

	// name and action of new template if we are adding a template
	private String newTmplName = null;
	private String newTmplAction = null;

	public Templates() {
		this.actionName = "templates";
		this.desiredMenu = "editor";
		this.pageTitle = "pagesForm.title";
	}

	public List<String> requiredWeblogPermissionActions() {
		return Collections.singletonList(WeblogPermission.ADMIN);
	}

	public String execute() {

		// query for templates list
		try {

			// get current list of templates, minus custom stylesheet
			List<WeblogTemplate> raw = WebloggerFactory.getWeblogger()
					.getWeblogManager().getPages(getActionWeblog());
			List<WeblogTemplate> pages = new ArrayList<WeblogTemplate>();
			pages.addAll(raw);
			// Remove style sheet from list so not to show when theme is
			// selected in shared theme mode
			if (getActionWeblog().getTheme().getStylesheet() != null) {
				pages.remove(WebloggerFactory
						.getWeblogger()
						.getWeblogManager()
						.getPageByLink(
								getActionWeblog(),
								getActionWeblog().getTheme().getStylesheet()
										.getLink()));
			}
			setTemplates(pages);

			// build list of action types that may be added
			List<String> actionsList = new ArrayList<String>();
			actionsList.add(WeblogTemplate.ACTION_CUSTOM);

			if (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
				// if the weblog is using a custom theme then determine which
				// action templates are still available to be created
				actionsList.add(WeblogTemplate.ACTION_PERMALINK);
				actionsList.add(WeblogTemplate.ACTION_SEARCH);
				actionsList.add(WeblogTemplate.ACTION_WEBLOG);
				actionsList.add(WeblogTemplate.ACTION_TAGSINDEX);

				for (WeblogTemplate tmpPage : getTemplates()) {
					if (!WeblogTemplate.ACTION_CUSTOM.equals(tmpPage
							.getAction())) {
						actionsList.remove(tmpPage.getAction());
					}
				}
			} else {
				// Make sure we have an option for the default web page
				actionsList.add(WeblogTemplate.ACTION_WEBLOG);
				if (StringUtils.isEmpty(getNewTmplAction())) {
					setNewTmplAction(WeblogTemplate.ACTION_WEBLOG);
				}
				for (WeblogTemplate tmpPage : getTemplates()) {
					if (WeblogTemplate.ACTION_WEBLOG
							.equals(tmpPage.getAction())) {
						actionsList.remove(WeblogTemplate.ACTION_WEBLOG);
						setNewTmplAction(null);
						break;
					}
				}
			}
			setAvailableActions(actionsList);

		} catch (WebloggerException ex) {
			log.error("Error getting templates for weblog - "
					+ getActionWeblog().getHandle(), ex);
			addError("Error getting template list - check Roller logs");
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
                newTemplate.setWebsite(getActionWeblog());
                newTemplate.setAction(getNewTmplAction());
                newTemplate.setName(getNewTmplName());
                newTemplate.setDescription(newTemplate.getName());
                newTemplate.setHidden(false);
                newTemplate.setNavbar(false);
                newTemplate.setLastModified(new Date());

                if (WeblogTemplate.ACTION_CUSTOM.equals(getNewTmplAction())) {
                    newTemplate.setLink(getNewTmplName());
                }

                // Make sure we have always have a Weblog main page. Stops
                // deleting main page in custom theme mode also.
                if (WeblogTemplate.ACTION_WEBLOG.equals(getNewTmplAction())) {
                    newTemplate.setName(WeblogTemplate.DEFAULT_PAGE);
                }

                // save the new Template
                WebloggerFactory.getWeblogger().getWeblogManager()
                        .savePage(newTemplate);

                // Create weblog template codes for available types.
                CustomTemplateRendition standardRendition = new CustomTemplateRendition(
                        newTemplate.getId(), RenditionType.STANDARD);
                standardRendition.setTemplate(getText("pageForm.newTemplateContent"));
                standardRendition.setTemplateLanguage(TemplateLanguage.VELOCITY);
                WebloggerFactory.getWeblogger().getWeblogManager()
                        .saveTemplateRendition(standardRendition);

                /* TBI -- need a way for user to specify dual or single template
                CustomTemplateRendition mobileRendition = new CustomTemplateRendition(
                        newTemplate.getId(), RenditionType.MOBILE);
                mobileRendition.setTemplate(newTemplate.getContents());
                mobileRendition.setTemplateLanguage(TemplateLanguage.VELOCITY);
                WebloggerFactory.getWeblogger().getWeblogManager()
                        .saveTemplateRendition(mobileRendition);
                */

                // if this person happened to create a Weblog template from
                // scratch then make sure and set the defaultPageId. What does
                // this do????
                if (WeblogTemplate.DEFAULT_PAGE.equals(newTemplate.getName())) {
                    getActionWeblog().setDefaultPageId(newTemplate.getId());
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveWeblog(getActionWeblog());
                }

                // flush results to db
                WebloggerFactory.getWeblogger().flush();

                // reset form fields
                setNewTmplName(null);
                setNewTmplAction(null);

            } catch (WebloggerException ex) {
                log.error("Error adding new template for weblog - "
                        + getActionWeblog().getHandle(), ex);
                addError("Error adding new template - check Roller logs");
            }
        }

		return execute();
	}

	// validation when adding a new template
	private void myValidate() {

		// make sure name is non-null and within proper size
		if (StringUtils.isEmpty(getNewTmplName())) {
			addError("Template.error.nameNull");
		} else if (getNewTmplName().length() > RollerConstants.TEXTWIDTH_255) {
			addError("Template.error.nameSize");
		}

		// make sure action is a valid
		if (StringUtils.isEmpty(getNewTmplAction())) {
			addError("Template.error.actionNull");
		}

		// check if template by that name already exists
		try {
			WeblogTemplate existingPage = WebloggerFactory.getWeblogger()
					.getWeblogManager()
					.getPageByName(getActionWeblog(), getNewTmplName());
			if (existingPage != null) {
				addError("pagesForm.error.alreadyExists", getNewTmplName());
			}
		} catch (WebloggerException ex) {
			log.error("Error checking for existing template", ex);
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

	public List getAvailableActions() {
		return availableActions;
	}

	public void setAvailableActions(List availableActions) {
		this.availableActions = availableActions;
	}

	public String getNewTmplName() {
		return newTmplName;
	}

	public void setNewTmplName(String newTmplName) {
		this.newTmplName = newTmplName;
	}

	public String getNewTmplAction() {
		return newTmplAction;
	}

	public void setNewTmplAction(String newTmplAction) {
		this.newTmplAction = newTmplAction;
	}

}
