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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Remove a template.
 */
public class TemplateRemove extends UIAction {

	private static Log log = LogFactory.getLog(TemplateRemove.class);

	// id of template to remove
	private String removeId = null;

	// template object that we will remove
	private WeblogTemplate template = null;

	public TemplateRemove() {
		this.actionName = "templateRemove";
		this.desiredMenu = "editor";
		this.pageTitle = "editPages.title.removeOK";
	}

	// must be a weblog admin to use this action
	public List<String> requiredWeblogPermissionActions() {
		return Collections.singletonList(WeblogPermission.ADMIN);
	}

	public void myPrepare() {
		if (getRemoveId() != null) {
            try {
                setTemplate(WebloggerFactory.getWeblogger().getWeblogManager()
                        .getTemplate(getRemoveId()));
            } catch (WebloggerException ex) {
                log.error("Error looking up template by id - " + getRemoveId(),
                        ex);
                addError("editPages.remove.notFound", getRemoveId());
            }
        }
	}

	/**
	 * Display the remove template confirmation.
	 */
	public String execute() {
		return "confirm";
	}

	/**
	 * Remove a new template.
	 */
	public String remove() {

		if (getTemplate() != null) {
            try {
                if (!getTemplate().isRequired()
                        || !WeblogTheme.CUSTOM.equals(getActionWeblog()
                        .getEditorTheme())) {

                    WeblogManager mgr = WebloggerFactory.getWeblogger()
                            .getWeblogManager();

                    // if weblog template remove custom style sheet also
                    if (getTemplate().getName().equals(
                            WeblogTemplate.DEFAULT_PAGE)) {

                        Weblog weblog = getActionWeblog();

                        ThemeTemplate stylesheet = getActionWeblog().getTheme()
                                .getStylesheet();

                        // Delete style sheet if the same name
                        if (stylesheet != null
                                && getActionWeblog().getTheme().getStylesheet() != null
                                && stylesheet.getLink().equals(
                                getActionWeblog().getTheme()
                                        .getStylesheet().getLink())) {
                            // Same so OK to delete
                            WeblogTemplate css = mgr.getTemplateByLink(
                                    getActionWeblog(), stylesheet.getLink());

                            if (css != null) {
                                mgr.removeTemplate(css);
                            }
                        }

                        // Clear for next custom theme
                        weblog.setCustomStylesheetPath(null);

                    }

                    // notify cache
                    CacheManager.invalidate(getTemplate());
                    mgr.removeTemplate(getTemplate());
                    WebloggerFactory.getWeblogger().flush();

                    return SUCCESS;
                } else {
                    addError("editPages.remove.requiredTemplate");
                }

            } catch (Exception ex) {
                log.error("Error removing page - " + getRemoveId(), ex);
                addError("editPages.remove.error");
            }
        }

		return "confirm";
	}
	
    /**
     * Cancel.
     * 
     * @return the string
     */
    public String cancel() {
        return CANCEL;
    }

	public String getRemoveId() {
		return removeId;
	}

	public void setRemoveId(String removeId) {
		this.removeId = removeId;
	}

	public WeblogTemplate getTemplate() {
		return template;
	}

	public void setTemplate(WeblogTemplate template) {
		this.template = template;
	}

}
