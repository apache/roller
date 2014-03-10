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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Remove templates.
 */
public class TemplatesRemove extends UIAction {

    private static final long serialVersionUID = 895186156151331087L;
    private static Log log = LogFactory.getLog(TemplatesRemove.class);

    // Templates to remove
    private String[] idSelections = null;

    // Limit updates to just this set of comma-separated IDs
    private String ids = null;

    // list of templates to display
    private List<WeblogTemplate> templates = Collections.emptyList();

    public TemplatesRemove() {
        this.actionName = "templatesRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "editPages.title.removeOK";
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }

    public void myPrepare() {

        if (getIdSelections() != null) {

            // query for templates list
            try {

                WeblogManager mgr = WebloggerFactory.getWeblogger()
                        .getWeblogManager();

                List<WeblogTemplate> pages = new ArrayList<WeblogTemplate>();
                WeblogTemplate template = null;

                String[] idsToDelete = getIdSelections();
                if (idsToDelete != null && idsToDelete.length > 0) {

                    for (int i = 0; i < idsToDelete.length; i++) {
                        if (!idsToDelete[i].equals("")) {
                            template = mgr.getPage(idsToDelete[i]);
                            if (template != null) {
                                pages.add(template);
                            }
                        }
                    }

                }

                // Set page data
                setTemplates(pages);
                setIds(Utilities.stringArrayToString(idsToDelete, ","));

                // Flush for operation
                WebloggerFactory.getWeblogger().flush();

            } catch (Exception ex) {
                log.error("Error getting templates for weblog - "
                        + getActionWeblog().getHandle(), ex);
                addError("error.unexpected");
            }

        }
    }

    /**
     * Display the remove template confirmation.
     */
    public String execute() {

        if (getIds() != null && getTemplates() != null
                && getTemplates().size() > 0) {
            return "confirm";
        } else {
            return SUCCESS;
        }

    }

    /**
     * Remove Selected templates
     */
    public String remove() {

        if (getIds() != null) {
            try {

                String[] idsToDelete = Utilities.stringToStringArray(getIds(),
                        ",");
                if (idsToDelete != null && idsToDelete.length > 0) {

                    WeblogManager mgr = WebloggerFactory.getWeblogger()
                            .getWeblogManager();

                    Weblog weblog = getActionWeblog();
                    WeblogTemplate template = null;

                    for (int i = 0; i < idsToDelete.length; i++) {
                        if (!idsToDelete[i].equals("")) {

                            template = mgr.getPage(idsToDelete[i]);
                            if (!template.isRequired()
                                    || !WeblogTemplate.ACTION_CUSTOM
                                            .equals(getActionWeblog()
                                                    .getEditorTheme())) {

                                // if weblog template remove custom style sheet
                                // also
                                if (template.getName().equals(
                                        WeblogTemplate.DEFAULT_PAGE)) {

                                    ThemeTemplate stylesheet = getActionWeblog()
                                            .getTheme().getStylesheet();

                                    // Delete style sheet if the same name
                                    if (stylesheet != null
                                            && getActionWeblog().getTheme()
                                                    .getStylesheet() != null
                                            && stylesheet.getLink().equals(
                                                    getActionWeblog()
                                                            .getTheme()
                                                            .getStylesheet()
                                                            .getLink())) {
                                        // Same so OK to delete
                                        WeblogTemplate css = mgr.getPageByLink(
                                                getActionWeblog(),
                                                stylesheet.getLink());

                                        if (css != null) {
                                            mgr.removePage(css);
                                        }
                                    }

                                    // Clear for next custom theme
                                    weblog.setCustomStylesheetPath(null);
                                    weblog.setDefaultPageId(null);

                                }

                                mgr.removePage(template);

                            }
                        }
                    }

                    // Save for changes
                    mgr.saveWeblog(weblog);

                    WebloggerFactory.getWeblogger().flush();

                    // notify caches
                    CacheManager.invalidate(getActionWeblog());

                }

                return SUCCESS;

            } catch (Exception e) {
                log.error("Error deleting templates for weblog - "
                        + getActionWeblog().getHandle(), e);

                addError("error.unexpected");

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
        return "cancel";
    }

    /**
     * Gets the templates.
     * 
     * @return the templates
     */
    public List<WeblogTemplate> getTemplates() {
        return templates;
    }

    /**
     * Sets the templates.
     * 
     * @param templates
     *            the new templates
     */
    public void setTemplates(List<WeblogTemplate> templates) {
        this.templates = templates;
    }

    /**
     * Select check boxes for deleting records
     */
    public String[] getIdSelections() {
        return idSelections;
    }

    /**
     * Select check boxes for deleting records
     */
    public void setIdSelections(String[] idSelections) {
        this.idSelections = idSelections;
    }

    /**
     * Comma separated list if ids to remove
     */
    public String getIds() {
        return ids;
    }

    /**
     * Comma separated list if ids to remove
     */
    public void setIds(String ids) {
        this.ids = ids;
    }

}
