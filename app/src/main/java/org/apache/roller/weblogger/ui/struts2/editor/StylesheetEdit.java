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
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

import java.util.Date;

/**
 * Action which handles editing for a weblog stylesheet override template.
 */
public class StylesheetEdit extends UIAction {

    private static final long serialVersionUID = 4657591015852311907L;

    private static Log log = LogFactory.getLog(StylesheetEdit.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    // the template we are working on
    private WeblogTemplate template = null;

    // the contents of the stylesheet override
    private String contentsStandard = null;
    private String contentsMobile = null;

    private boolean sharedTheme;

    // read by JSP to determine if user just deleted his shared theme customized stylesheet
    private boolean sharedStylesheetDeleted;

    // Do we have a custom stylesheet already for a shared theme
    private boolean sharedThemeCustomStylesheet = false;

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public StylesheetEdit() {
        this.actionName = "stylesheetEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "stylesheetEdit.title";
    }

    @Override
    public void myPrepare() {
        sharedTheme = !WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme());
        sharedStylesheetDeleted = false;

        ThemeTemplate stylesheet = null;
        try {
            stylesheet = getActionWeblog().getTheme().getTemplateByAction(ComponentType.STYLESHEET);
        } catch (WebloggerException ex) {
            log.error("Error looking up stylesheet on weblog - "
                    + getActionWeblog().getHandle(), ex);
        }

        if (stylesheet != null) {
            log.debug("custom stylesheet path is - " + stylesheet.getLink());
            try {
                setTemplate(weblogManager.getTemplateByLink(getActionWeblog(), stylesheet.getLink()));

                if (getTemplate() == null) {
                    log.debug("custom stylesheet not found, creating it");

                    // template doesn't exist yet, so create it
                    WeblogTemplate stylesheetTmpl = new WeblogTemplate();
                    stylesheetTmpl.setWeblog(getActionWeblog());
                    stylesheetTmpl.setAction(ThemeTemplate.ComponentType.STYLESHEET);
                    stylesheetTmpl.setName(stylesheet.getName());
                    stylesheetTmpl.setDescription(stylesheet.getDescription());
                    stylesheetTmpl.setLink(stylesheet.getLink());
                    stylesheetTmpl.setHidden(false);
                    stylesheetTmpl.setNavbar(false);
                    stylesheetTmpl.setLastModified(new Date());

                    // create renditions for available rendition types
                    TemplateRendition sCode = stylesheet.getTemplateRendition(RenditionType.STANDARD);
                    if (sCode != null) {
                        WeblogTemplateRendition standardRendition = new WeblogTemplateRendition(
                                stylesheetTmpl, RenditionType.STANDARD);
                        standardRendition.setTemplate(sCode.getTemplate());
                        standardRendition.setTemplateLanguage(sCode.getTemplateLanguage());
                        weblogManager.saveTemplateRendition(standardRendition);
                    }

                    TemplateRendition mCode = stylesheet.getTemplateRendition(RenditionType.MOBILE);
                    if (mCode != null) {
                        WeblogTemplateRendition mobileRendition = new WeblogTemplateRendition(
                                stylesheetTmpl, RenditionType.MOBILE);
                        mobileRendition.setTemplate(mCode.getTemplate());
                        mobileRendition.setTemplateLanguage(mCode
                                .getTemplateLanguage());
                        weblogManager.saveTemplateRendition(mobileRendition);
                    }

                    weblogManager.saveTemplate(stylesheetTmpl);
                    setTemplate(stylesheetTmpl);
                    WebloggerFactory.flush();


                    // success message
                    addMessage("stylesheetEdit.create.success");
                }

                // See if we're using a shared theme with a custom stylesheet
                if (!WeblogTheme.CUSTOM.equals(getActionWeblog()
                        .getEditorTheme())
                        && getActionWeblog().getTheme().getTemplateByAction(ComponentType.STYLESHEET) != null) {

                    ThemeTemplate override = weblogManager.getTemplateByLink(
                            getActionWeblog(),
                            getActionWeblog().getTheme()
                                    .getTemplateByAction(ComponentType.STYLESHEET).getLink());

                    if (override != null) {
                        sharedThemeCustomStylesheet = true;
                    }
                }

            } catch (WebloggerException ex) {
                log.error(
                        "Error finding/adding stylesheet template from weblog - "
                                + getActionWeblog().getHandle(), ex);
            }
        }
    }

    /**
     * Show stylesheet edit page.
     */
    public String execute() {
        if (template != null) {
            try {
                if (getTemplate().getTemplateRendition(RenditionType.STANDARD) != null) {
                    setContentsStandard(getTemplate().getTemplateRendition(
                            RenditionType.STANDARD).getTemplate());
                } else {
                    setContentsStandard("");
                }
                if (getTemplate().getTemplateRendition(RenditionType.MOBILE) != null) {
                    setContentsMobile(getTemplate().getTemplateRendition(
                            RenditionType.MOBILE).getTemplate());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Standard: " + getContentsStandard() + " Mobile: "
                            + getContentsMobile());
                }
            } catch (WebloggerException e) {
                log.error("Error loading Weblog template codes for stylesheet", e);
            }
        }
        return INPUT;
    }

    /**
     * Save an existing stylesheet.
     */
    public String save() {
        if (!hasActionErrors()) {
            try {

                WeblogTemplate stylesheet = getTemplate();

                stylesheet.setLastModified(new Date());
                stylesheet.setAction(ComponentType.STYLESHEET);

                if (stylesheet.getTemplateRendition(RenditionType.STANDARD) != null) {
                    // if we have a template, then set it
                    WeblogTemplateRendition tc = stylesheet
                            .getTemplateRendition(RenditionType.STANDARD);
                    tc.setTemplate(getContentsStandard());
                    weblogManager.saveTemplateRendition(tc);
                } else {
                    // otherwise create it, then set it
                    WeblogTemplateRendition tc = new WeblogTemplateRendition(
                            stylesheet, RenditionType.STANDARD);
                    tc.setTemplate("");
                    weblogManager.saveTemplateRendition(tc);
                }

                if (stylesheet.getTemplateRendition(RenditionType.MOBILE) != null) {
                    WeblogTemplateRendition tc = stylesheet
                            .getTemplateRendition(RenditionType.MOBILE);
                    tc.setTemplate(getContentsMobile());
                    weblogManager.saveTemplateRendition(tc);
                }

                // save template and flush
                weblogManager.saveTemplate(stylesheet);

                WebloggerFactory.flush();

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
     * Revert the stylesheet to its original state.  UI provides this only for shared themes.
     */
    public String revert() {
        if (sharedTheme && !hasActionErrors()) {
            try {

                WeblogTemplate stylesheet = getTemplate();

                // lookup the theme used by this weblog
                Theme theme = themeManager.getTheme(getActionWeblog().getEditorTheme());

                stylesheet.setLastModified(new Date());

                if (stylesheet.getTemplateRendition(RenditionType.STANDARD) != null) {
                    TemplateRendition templateCode = theme.getTemplateByAction(ComponentType.STYLESHEET)
                            .getTemplateRendition(RenditionType.STANDARD);
                    // if we have a template, then set it
                    WeblogTemplateRendition existingTemplateCode = stylesheet
                            .getTemplateRendition(RenditionType.STANDARD);
                    existingTemplateCode
                            .setTemplate(templateCode.getTemplate());
                    weblogManager.saveTemplateRendition(existingTemplateCode);
                }
                if (stylesheet.getTemplateRendition(RenditionType.MOBILE) != null) {
                    TemplateRendition templateCode = theme.getTemplateByAction(ComponentType.STYLESHEET)
                            .getTemplateRendition(RenditionType.MOBILE);
                    WeblogTemplateRendition existingTemplateCode = stylesheet
                            .getTemplateRendition(RenditionType.MOBILE);
                    existingTemplateCode
                            .setTemplate(templateCode.getTemplate());
                }

                // save template and flush
                weblogManager.saveTemplate(stylesheet);
                WebloggerFactory.flush();

                // notify caches
                CacheManager.invalidate(stylesheet);

                // success message
                addMessage("stylesheetEdit.revert.success",
                        stylesheet.getName());

            } catch (WebloggerException ex) {
                log.error("Error updating stylesheet template for weblog - "
                        + getActionWeblog().getHandle(), ex);
                addError("generic.error.check.logs");
            }
        }
        return execute();
    }

    /**
     * set theme to default stylesheet, ie delete it.
     */
    public String delete() {
        if (template != null && sharedTheme && !hasActionErrors()) {
            try {
                // Delete template and flush

                // Remove template and page codes
                weblogManager.removeTemplate(template);

                Weblog weblog = getActionWeblog();

                // save updated weblog and flush
                weblogManager.saveWeblog(weblog);

                // notify caches
                CacheManager.invalidate(template);

                // Flush for operation
                WebloggerFactory.flush();

                // success message
                addMessage("stylesheetEdit.default.success",
                        template.getName());

                template = null;
                sharedStylesheetDeleted = true;

            } catch (Exception e) {
                log.error("Error deleting stylesheet template for weblog - "
                        + getActionWeblog().getHandle(), e);
                addError("generic.error.check.logs");
            }
        }
        return INPUT;
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
     * Checks if using a shared theme with a custom stylesheet.
     * 
     * @return true, if checks if shared theme and custom stylesheet
     */
    public boolean isSharedThemeCustomStylesheet() {
        return sharedThemeCustomStylesheet;
    }

    /**
     * Checks if user just deleted his custom shared stylesheet
     *
     * @return true, if custom shared stylesheet was deleted.
     */
    public boolean isSharedStylesheetDeleted() {
        return sharedStylesheetDeleted;
    }
}
