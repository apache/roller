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
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.*;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.convention.annotation.AllowedMethods;

import java.util.Date;

/**
 * Action which handles editing for a weblog stylesheet override template.
 */
// TODO: make this work @AllowedMethods({"execute","copyStylesheet","delete","revert"})
public class StylesheetEdit extends UIAction {

    private static final long serialVersionUID = 4657591015852311907L;

    private static Log log = LogFactory.getLog(StylesheetEdit.class);

    // the template we are working on
    private WeblogTemplate template = null;

    // the contents of the stylesheet override
    private String contentsStandard = null;
    private String contentsMobile = null;

    // if shared theme, is a stylesheet supported?
    private boolean sharedThemeStylesheet = false;

    public StylesheetEdit() {
        this.actionName = "stylesheetEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "stylesheetEdit.title";
    }

    @Override
    public void myPrepare() {

        sharedThemeStylesheet = false;

        WeblogManager weblogManager = WebloggerFactory.getWeblogger().getWeblogManager();
        ThemeManager themeManager = WebloggerFactory.getWeblogger().getThemeManager();

        if ( isSharedTheme() ) {
            try {
                SharedTheme themeName = themeManager.getTheme(getActionWeblog().getEditorTheme());
                sharedThemeStylesheet = themeName.getStylesheet() != null;

                ThemeTemplate themeStylesheet  = themeName.getStylesheet();

                WeblogTemplate weblogStylesheet =
                    weblogManager.getTemplateByLink(getActionWeblog(), themeStylesheet.getLink());

                setTemplate( weblogStylesheet );

            } catch (WebloggerException ex) {
                log.error("Error looking up shared theme name on weblog - " + getActionWeblog().getHandle(), ex);
            }
        }
    }

    /**
     * Show stylesheet edit page.
     */
    @Override
    public String execute() {

        if (template != null) {
            try {
                if (getTemplate().getTemplateRendition(RenditionType.STANDARD) != null) {
                    setContentsStandard(getTemplate().getTemplateRendition( RenditionType.STANDARD).getTemplate());
                } else {
                    setContentsStandard("");
                }
                if (getTemplate().getTemplateRendition(RenditionType.MOBILE) != null) {
                    setContentsMobile(getTemplate().getTemplateRendition( RenditionType.MOBILE).getTemplate());
                }
                if (log.isDebugEnabled()) {
                    log.debug("Standard: " + getContentsStandard() + " Mobile: " + getContentsMobile());
                }
            } catch (WebloggerException e) {
                log.error("Error loading Weblog template codes for stylesheet", e);
            }
        }
        return INPUT;
    }

    public String copyStylesheet() {

        WeblogManager weblogManager = WebloggerFactory.getWeblogger().getWeblogManager();
        ThemeManager themeManager = WebloggerFactory.getWeblogger().getThemeManager();

        ThemeTemplate stylesheet = null;

        try {
            SharedTheme themeName = themeManager.getTheme(getActionWeblog().getEditorTheme());
            stylesheet = themeName.getStylesheet();

        } catch (WebloggerException ex) {

        }

        log.debug("custom stylesheet path is - " + stylesheet.getLink());
        try {
            setTemplate( weblogManager.getTemplateByLink(getActionWeblog(), stylesheet.getLink()));

            if (getTemplate() == null) {
                log.debug("custom stylesheet not found, creating it");

                WeblogTemplate stylesheetTmpl = new WeblogTemplate();
                stylesheetTmpl.setWeblog(getActionWeblog());
                stylesheetTmpl.setAction(ComponentType.STYLESHEET);
                stylesheetTmpl.setName(stylesheet.getName());
                stylesheetTmpl.setDescription(stylesheet.getDescription());
                stylesheetTmpl.setLink(stylesheet.getLink());
                stylesheetTmpl.setHidden(false);
                stylesheetTmpl.setNavbar(false);
                stylesheetTmpl.setLastModified(new Date());

                // create renditions for available rendition types: standard and mobile

                TemplateRendition sCode = stylesheet.getTemplateRendition(RenditionType.STANDARD);
                if (sCode != null) {
                    CustomTemplateRendition standardRendition = new CustomTemplateRendition(
                        stylesheetTmpl, RenditionType.STANDARD);
                    standardRendition.setTemplate(sCode.getTemplate());
                    standardRendition.setTemplateLanguage(sCode.getTemplateLanguage());
                    weblogManager.saveTemplateRendition(standardRendition);
                }

                TemplateRendition mCode = stylesheet.getTemplateRendition(RenditionType.MOBILE);
                if (mCode != null) {
                    CustomTemplateRendition mobileRendition =
                        new CustomTemplateRendition(stylesheetTmpl, RenditionType.MOBILE);
                    mobileRendition.setTemplate(mCode.getTemplate());
                    mobileRendition.setTemplateLanguage(mCode.getTemplateLanguage());
                    weblogManager.saveTemplateRendition(mobileRendition);
                }

                weblogManager.saveTemplate(stylesheetTmpl);
                setTemplate(stylesheetTmpl);

                WebloggerFactory.getWeblogger().flush();

                // success message
                addMessage("stylesheetEdit.create.success");
            }

        } catch (WebloggerException ex) {
            log.error("Error finding/adding stylesheet template from weblog - "
                + getActionWeblog().getHandle(), ex);
            addError("generic.error.check.logs");
        }

        return revert();
    }

    /**
     * Save an existing stylesheet.
     */
    public String save() {

        WeblogManager weblogManager = WebloggerFactory.getWeblogger().getWeblogManager();

        if (!hasActionErrors()) {
            try {

                WeblogTemplate stylesheet = getTemplate();

                stylesheet.setLastModified(new Date());
                stylesheet.setAction(ComponentType.STYLESHEET);

                if (stylesheet.getTemplateRendition(RenditionType.STANDARD) != null) {
                    // if we have a template, then set it
                    CustomTemplateRendition tc = stylesheet.getTemplateRendition(RenditionType.STANDARD);
                    tc.setTemplate(getContentsStandard());
                    weblogManager.saveTemplateRendition(tc);

                } else {
                    // otherwise create it, then set it
                    CustomTemplateRendition tc = new CustomTemplateRendition( stylesheet, RenditionType.STANDARD);
                    tc.setTemplate("");
                    weblogManager.saveTemplateRendition(tc);
                }

                if (stylesheet.getTemplateRendition(RenditionType.MOBILE) != null) {
                    CustomTemplateRendition tc = stylesheet.getTemplateRendition(RenditionType.MOBILE);
                    tc.setTemplate(getContentsMobile());
                    weblogManager.saveTemplateRendition(tc);
                }

                // save template and flush
                WebloggerFactory.getWeblogger().getWeblogManager().saveTemplate(stylesheet);

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
     * Revert the stylesheet to its original state.  UI provides this only for shared themes.
     */
    public String revert() {

        WeblogManager weblogManager = WebloggerFactory.getWeblogger().getWeblogManager();

        if (isSharedTheme() && !hasActionErrors()) {
            try {

                WeblogTemplate stylesheet = getTemplate();

                // lookup the theme used by this weblog
                ThemeManager tmgr = WebloggerFactory.getWeblogger() .getThemeManager();
                Theme theme = tmgr.getTheme(getActionWeblog().getEditorTheme());

                stylesheet.setLastModified(new Date());

                if (stylesheet.getTemplateRendition(RenditionType.STANDARD) != null) {

                    TemplateRendition templateCode =
                        theme.getStylesheet().getTemplateRendition(RenditionType.STANDARD);

                    // if we have a template, then set it
                    CustomTemplateRendition existingTemplateCode =
                        stylesheet.getTemplateRendition(RenditionType.STANDARD);

                    existingTemplateCode.setTemplate(templateCode.getTemplate());
                    weblogManager.saveTemplateRendition(existingTemplateCode);
                }
                if (stylesheet.getTemplateRendition(RenditionType.MOBILE) != null) {

                    TemplateRendition templateCode =
                        theme.getStylesheet().getTemplateRendition(RenditionType.MOBILE);
                    CustomTemplateRendition existingTemplateCode =
                        stylesheet.getTemplateRendition(RenditionType.MOBILE);

                    existingTemplateCode.setTemplate(templateCode.getTemplate());
                }

                // save template and flush
                weblogManager.saveTemplate(stylesheet);
                WebloggerFactory.getWeblogger().flush();

                // notify caches
                CacheManager.invalidate(stylesheet);

                // success message
                addMessage("stylesheetEdit.revert.success", stylesheet.getName());

            } catch (WebloggerException ex) {
                log.error("Error updating stylesheet template for weblog - " + getActionWeblog().getHandle(), ex);
                addError("generic.error.check.logs");
            }
        }
        return execute();
    }

    /**
     * set theme to default stylesheet, ie delete it.
     */
    public String delete() {
        if (template != null && isSharedTheme() && !hasActionErrors()) {
            try {
                // Delete template and flush
                WeblogManager weblogManager = WebloggerFactory.getWeblogger().getWeblogManager();

                // Remove template and page codes
                weblogManager.removeTemplate(template);

                Weblog weblog = getActionWeblog();

                // save updated weblog and flush
                weblogManager.saveWeblog(weblog);

                // notify caches
                CacheManager.invalidate(template);

                // Flush for operation
                WebloggerFactory.getWeblogger().flush();

                // success message
                addMessage("stylesheetEdit.default.success", template.getName());

                template = null;

            } catch (Exception e) {
                log.error("Error deleting stylesheet template for weblog - " + getActionWeblog().getHandle(), e);
                addError("generic.error.check.logs");
            }
        }
        return INPUT;
    }

    /**
     * Checks if is custom theme.
     * @return true, if is custom theme
     */
    public boolean isCustomTheme() {
        return (WeblogTheme.CUSTOM.equals(getActionWeblog().getEditorTheme()));
    }

    /**
     * Checks if is shared theme.
     * @return true, if is shared theme
     */
    public boolean isSharedTheme() {
        return !isCustomTheme();
    }

    public boolean isSharedThemeStylesheet() {
        return sharedThemeStylesheet;
    }

    /**
     * Gets the template.
     * @return the template
     */
    public WeblogTemplate getTemplate() {
        return template;
    }

    /**
     * Sets the template.
     * @param template the new template
     */
    public void setTemplate(WeblogTemplate template) {
        this.template = template;
    }

    /**
     * Gets the contents standard.
     * @return the contents standard
     */
    public String getContentsStandard() {
        return this.contentsStandard;
    }

    /**
     * Sets the contents standard.
     * @param contents the new contents standard
     */
    public void setContentsStandard(String contents) {
        this.contentsStandard = contents;
    }

    /**
     * Gets the contents mobile.
     * @return the contents mobile
     */
    public String getContentsMobile() {
        return this.contentsMobile;
    }

    /**
     * Sets the contents mobile.
     * @param contents the new contents mobile
     */
    public void setContentsMobile(String contents) {
        this.contentsMobile = contents;
    }
}
