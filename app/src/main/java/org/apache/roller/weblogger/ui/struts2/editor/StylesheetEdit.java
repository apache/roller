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
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Action which handles editing for a weblog stylesheet override template.
 */
public class StylesheetEdit extends UIAction {

    private static final long serialVersionUID = 4657591015852311907L;

    private static Log log = LogFactory.getLog(StylesheetEdit.class);
    private static final String MOBILE_THEME_TYPE = "mobile";
    private static final String STANDARD_THEME_TYPE = "standard";

    // the template we are working on
    private WeblogTemplate template = null;

    // the contents of the stylesheet override
    private String contentsStandard = null;
    private String contentsMobile = null;

    // Do we have a custom stylesheet already for a custom theme
    private boolean customStylesheet = false;

    public StylesheetEdit() {
        this.actionName = "stylesheetEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "stylesheetEdit.title";
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }

    @Override
    public void myPrepare() {

        ThemeTemplate stylesheet = null;
        try {
            stylesheet = getActionWeblog().getTheme().getStylesheet();
        } catch (WebloggerException ex) {
            log.error("Error looking up stylesheet on weblog - "
                    + getActionWeblog().getHandle(), ex);
        }

        if (stylesheet != null) {
            log.debug("custom stylesheet path is - " + stylesheet.getLink());
            try {
                setTemplate(WebloggerFactory.getWeblogger().getWeblogManager()
                        .getPageByLink(getActionWeblog(), stylesheet.getLink()));

                if (getTemplate() == null) {
                    log.debug("custom stylesheet not found, creating it");

                    // template doesn't exist yet, so create it
                    WeblogTemplate stylesheetTmpl = new WeblogTemplate();
                    stylesheetTmpl.setWebsite(getActionWeblog());
                    stylesheetTmpl.setAction(ThemeTemplate.ACTION_CUSTOM);
                    stylesheetTmpl.setName(stylesheet.getName());
                    stylesheetTmpl.setDescription(stylesheet.getDescription());
                    stylesheetTmpl.setLink(stylesheet.getLink());
                    stylesheetTmpl.setContents(stylesheet.getContents());
                    stylesheetTmpl.setHidden(false);
                    stylesheetTmpl.setNavbar(false);
                    stylesheetTmpl.setLastModified(new Date());
                    stylesheetTmpl.setTemplateLanguage(stylesheet
                            .getTemplateLanguage());

                    // create template codes for available template code Types
                    CustomTemplateRendition standardRendition = new CustomTemplateRendition(
                            stylesheetTmpl.getId(), STANDARD_THEME_TYPE);
                    standardRendition.setTemplate(stylesheetTmpl
                            .getContents());
                    standardRendition.setTemplateLanguage(stylesheetTmpl
                            .getTemplateLanguage());
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveTemplateRendition(standardRendition);

                    TemplateRendition tCode = stylesheet.getTemplateRendition(MOBILE_THEME_TYPE);
                    if (tCode != null) {
                        CustomTemplateRendition mobileRendition = new CustomTemplateRendition(
                                stylesheetTmpl.getId(), MOBILE_THEME_TYPE);
                        mobileRendition.setTemplate(tCode.getTemplate());
                        mobileRendition.setTemplateLanguage(tCode
                                .getTemplateLanguage());
                        WebloggerFactory.getWeblogger().getWeblogManager()
                                .saveTemplateRendition(mobileRendition);
                    }

                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .savePage(stylesheetTmpl);
                    setTemplate(stylesheetTmpl);
                    WebloggerFactory.getWeblogger().flush();


                    // success message
                    addMessage("stylesheetEdit.create.success");
                }

                // See if we have a custom style sheet from a custom theme.
                if (!WeblogTheme.CUSTOM.equals(getActionWeblog()
                        .getEditorTheme())
                        && getActionWeblog().getTheme().getStylesheet() != null) {

                    ThemeTemplate override = WebloggerFactory
                            .getWeblogger()
                            .getWeblogManager()
                            .getPageByLink(
                                    getActionWeblog(),
                                    getActionWeblog().getTheme()
                                            .getStylesheet().getLink());

                    if (override != null) {
                        customStylesheet = true;
                    }
                }

            } catch (WebloggerException ex) {
                log.error(
                        "Error finding/adding stylesheet tempalate from weblog - "
                                + getActionWeblog().getHandle(), ex);
            }
        }
    }

    /**
     * Show stylesheet edit page.
     */
    public String execute() {

        if (getTemplate() == null) {
            return ERROR;
        }

        try {

            if (getTemplate().getTemplateRendition(STANDARD_THEME_TYPE) != null) {
                setContentsStandard(getTemplate().getTemplateRendition(
                        STANDARD_THEME_TYPE).getTemplate());
            } else {
                setContentsStandard(getTemplate().getContents());
            }
            if (getTemplate().getTemplateRendition(MOBILE_THEME_TYPE) != null) {
                setContentsMobile(getTemplate().getTemplateRendition(
                        MOBILE_THEME_TYPE).getTemplate());
            }

            if (log.isDebugEnabled()) {
                log.debug("Standard: " + getContentsStandard() + " Mobile: "
                        + getContentsMobile());
            }

        } catch (WebloggerException e) {
            log.error("Error loading Weblog template codes for stylesheet", e);
        }

        return INPUT;
    }

    /**
     * Save an existing stylesheet.
     */
    public String save() {

        if (getTemplate() == null) {
            addError("stylesheetEdit.error.cannotFind");
            return ERROR;
        }

        if (!hasActionErrors()) {
            try {

                WeblogTemplate stylesheet = getTemplate();

                stylesheet.setLastModified(new Date());

                if (stylesheet.getTemplateRendition(STANDARD_THEME_TYPE) != null) {
                    // if we have a template, then set it
                    CustomTemplateRendition tc = stylesheet
                            .getTemplateRendition(STANDARD_THEME_TYPE);
                    tc.setTemplate(getContentsStandard());
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveTemplateRendition(tc);
                } else {
                    // otherwise create it, then set it
                    CustomTemplateRendition tc = new CustomTemplateRendition(
                            stylesheet.getId(), STANDARD_THEME_TYPE);
                    tc.setTemplate(stylesheet.getContents());
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveTemplateRendition(tc);
                }

                if (stylesheet.getTemplateRendition(MOBILE_THEME_TYPE) != null) {
                    CustomTemplateRendition tc = stylesheet
                            .getTemplateRendition(MOBILE_THEME_TYPE);
                    tc.setTemplate(getContentsMobile());
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveTemplateRendition(tc);
                }

                // TODO do we still want to set the contents here?
                stylesheet.setContents(getContentsStandard());

                // save template and flush
                WebloggerFactory.getWeblogger().getWeblogManager()
                        .savePage(stylesheet);

                WebloggerFactory.getWeblogger().flush();

                // notify caches
                CacheManager.invalidate(stylesheet);

                // success message
                addMessage("stylesheetEdit.save.success", stylesheet.getName());

            } catch (WebloggerException ex) {
                log.error("Error updating stylesheet template for weblog - "
                        + getActionWeblog().getHandle(), ex);
                addError("Error saving template - check Roller logs");
            }
        }

        return INPUT;
    }

    /**
     * Revert the stylesheet to its original state.
     */
    public String revert() {

        if (getTemplate() == null) {
            addError("stylesheetEdit.error.cannotFind");
            return ERROR;
        }

        // make sure we are still using a shared theme so that reverting is
        // possible
        if (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
            addError("stylesheetEdit.error.customTheme");
        }

        if (!hasActionErrors()) {
            try {

                WeblogTemplate stylesheet = getTemplate();

                // lookup the theme used by this weblog
                ThemeManager tmgr = WebloggerFactory.getWeblogger()
                        .getThemeManager();
                Theme theme = tmgr.getTheme(getActionWeblog().getEditorTheme());

                stylesheet.setLastModified(new Date());

                if (stylesheet.getTemplateRendition(STANDARD_THEME_TYPE) != null) {
                    TemplateRendition templateCode = theme.getStylesheet()
                            .getTemplateRendition(STANDARD_THEME_TYPE);
                    // if we have a template, then set it
                    CustomTemplateRendition existingTemplateCode = stylesheet
                            .getTemplateRendition(STANDARD_THEME_TYPE);
                    existingTemplateCode
                            .setTemplate(templateCode.getTemplate());
                    WebloggerFactory.getWeblogger().getWeblogManager()
                            .saveTemplateRendition(existingTemplateCode);

                    // TODO do we still want to set the contents here?
                    stylesheet.setContents(templateCode.getTemplate());
                }
                if (stylesheet.getTemplateRendition(MOBILE_THEME_TYPE) != null) {
                    TemplateRendition templateCode = theme.getStylesheet()
                            .getTemplateRendition(MOBILE_THEME_TYPE);
                    CustomTemplateRendition existingTemplateCode = stylesheet
                            .getTemplateRendition(MOBILE_THEME_TYPE);
                    existingTemplateCode
                            .setTemplate(templateCode.getTemplate());
                }

                // save template and flush
                WebloggerFactory.getWeblogger().getWeblogManager()
                        .savePage(stylesheet);
                WebloggerFactory.getWeblogger().flush();

                // notify caches
                CacheManager.invalidate(stylesheet);

                // success message
                addMessage("stylesheetEdit.revert.success",
                        stylesheet.getName());

            } catch (WebloggerException ex) {
                log.error("Error updating stylesheet template for weblog - "
                        + getActionWeblog().getHandle(), ex);
                addError("stylesheetEdit.revert.failure");
            }
        }

        return execute();
    }

    /**
     * set theme to default stylesheet, ie delete it.
     */
    public String delete() {

        if (getTemplate() == null) {
            log.error("Unable to locate stylesheet template");
            addError("error.recordnotfound");
            return ERROR;
        }

        // make sure we are still using a shared theme so that deleting is
        // possible
        if (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme())) {
            log.error("Unable to delete stylesheet");
            addError("stylesheetEdit.error.customTheme");
        }

        if (!hasActionErrors()) {
            try {

                WeblogTemplate stylesheet = getTemplate();

                // Delete template and flush
                WeblogManager mgr = WebloggerFactory.getWeblogger()
                        .getWeblogManager();

                // Remove template and page codes
                mgr.removePage(stylesheet);

                Weblog weblog = getActionWeblog();

                // Clear for next custom theme
                weblog.setCustomStylesheetPath(null);

                // save updated weblog and flush
                mgr.saveWeblog(weblog);

                // notify caches
                CacheManager.invalidate(stylesheet);

                // Flush for operation
                WebloggerFactory.getWeblogger().flush();

                // success message
                addMessage("stylesheetEdit.default.success",
                        stylesheet.getName());

            } catch (Exception e) {
                log.error("Error deleting stylesheet template for weblog - "
                        + getActionWeblog().getHandle(), e);

                return ERROR;
            }
        }

        return "delete";

    }

    /**
     * Checks if is custom theme.
     * 
     * @return true, if is custom theme
     */
    public boolean isCustomTheme() {
        return (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme()));
    }

    /**
     * Gets the template.
     * 
     * @return the template
     */
    public WeblogTemplate getTemplate() {
        return template;
    }

    /**
     * Sets the template.
     * 
     * @param template
     *            the new template
     */
    public void setTemplate(WeblogTemplate template) {
        this.template = template;
    }

    /**
     * Gets the contents standard.
     * 
     * @return the contents standard
     */
    public String getContentsStandard() {
        return this.contentsStandard;
    }

    /**
     * Sets the contents standard.
     * 
     * @param contents
     *            the new contents standard
     */
    public void setContentsStandard(String contents) {
        this.contentsStandard = contents;
    }

    /**
     * Gets the contents mobile.
     * 
     * @return the contents mobile
     */
    public String getContentsMobile() {
        return this.contentsMobile;
    }

    /**
     * Sets the contents mobile.
     * 
     * @param contents
     *            the new contents mobile
     */
    public void setContentsMobile(String contents) {
        this.contentsMobile = contents;
    }

    /**
     * Checks if is custom stylesheet.
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
