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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Template.TemplateDerivation;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;

import java.util.Date;
import java.util.EnumMap;
import java.util.Map;


/**
 * Action which handles editing for a single WeblogTemplate.
 */
public class TemplateEdit extends UIAction {

    private static Log log = LogFactory.getLog(TemplateEdit.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private boolean nameChangeable = false;

    // form bean for collection all template properties
    private WeblogTemplate bean = new WeblogTemplate();

    // the template we are working on
    private WeblogTemplate template = null;

    public TemplateEdit() {
        this.actionName = "templateEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "templates.title";
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public void prepare() {
        try {
            if (bean.getId() != null && bean.getId().length() > 0) {
                // Template is overridden or blog-only
                setTemplate(weblogManager.getTemplate(getBean().getId()));
                // name can be changed only on blog-only templates
                SharedTheme sharedTheme = themeManager.getSharedTheme(getActionWeblog().getTheme());
                if (sharedTheme.getTemplateByName(template.getName()) == null) {
                    nameChangeable = true;
                }
            }

        } catch (WebloggerException ex) {
            log.error("Error looking up template: " + getBean(), ex);
        }
    }


    /**
     * Show template edit page.
     */
    @SkipValidation
    public String execute() {
        try {
            if (getTemplate() == null) {
                // First-time override of a shared template
                SharedTheme sharedTheme = themeManager.getSharedTheme(getActionWeblog().getTheme());
                Template template = sharedTheme.getTemplateByName(bean.getName());
                WeblogTemplate newTemplate = themeManager.createWeblogTemplate(getActionWeblog(), template);
                setTemplate(newTemplate);
                bean.setDerivation(TemplateDerivation.OVERRIDDEN);
            }

            bean.setId(template.getId());
            bean.setName(template.getName());
            bean.setRole(template.getRole());
            bean.setDescription(template.getDescription());
            bean.setRelativePath(template.getRelativePath());
            bean.setWeblog(getActionWeblog());

            WeblogTemplateRendition maybeTemplate = template.getTemplateRendition(RenditionType.NORMAL);
            if (maybeTemplate != null) {
                bean.setContentsStandard(maybeTemplate.getTemplate());
            } else {
                bean.setContentsStandard("");
            }

            maybeTemplate = template.getTemplateRendition(RenditionType.MOBILE);
            if (maybeTemplate != null) {
                bean.setContentsMobile(maybeTemplate.getTemplate());
            }

        } catch (WebloggerException ex) {
           log.error("Error updating page - " + getBean().getId(), ex);
           addError("Error saving template - check TightBlog logs");
        }

        return INPUT;
    }

    /**
     * Save an existing template.
     */
    public String save() {
        // validation
        myValidate();

        if (!hasActionErrors()) {

            // starting from a shared template (first override)?
            if (template == null) {
                template = new WeblogTemplate();
                template.setId(WebloggerCommon.generateUUID());
                template.setWeblog(getActionWeblog());
                template.setRole(bean.getRole());
                template.setName(bean.getName());
            }

            // some properties relevant only for certain template roles
            if (!template.getRole().isSingleton()) {
                template.setName(bean.getName());
                template.setDescription(bean.getDescription());
            }

            if (template.getRole().isAccessibleViaUrl()) {
                template.setRelativePath(bean.getRelativePath());
            }

            template.setLastModified(new Date());

            try {
                weblogManager.saveTemplate(template);
                log.debug("Saved template: " + template.getId());

                WeblogTemplateRendition wtr = template.getTemplateRendition(RenditionType.NORMAL);

                if (wtr != null) {
                    // if we have a template, then set it
                    wtr.setTemplate(bean.getContentsStandard());
                    weblogManager.saveTemplateRendition(wtr);
                } else {
                    // otherwise create it, then set it
                    wtr = new WeblogTemplateRendition(template, RenditionType.NORMAL);
                    wtr.setTemplate(bean.getContentsStandard());
                    wtr.setTemplateLanguage(TemplateLanguage.VELOCITY);
                    weblogManager.saveTemplateRendition(wtr);
                }

                wtr = template.getTemplateRendition(RenditionType.MOBILE);
                if (wtr != null) {
                    wtr.setTemplate(bean.getContentsMobile());
                    wtr.setTemplateLanguage(TemplateLanguage.VELOCITY);
                    weblogManager.saveTemplateRendition(wtr);
                }

                WebloggerFactory.flush();

                // notify caches
                cacheManager.invalidate(template);

                // success message
                addMessage("templateEdit.save.success", template.getName());
                bean.setId(template.getId());

            } catch (Exception ex) {
                log.error("Error updating page - " + getBean().getId(), ex);
                addError("Error updating template - check TightBlog logs");
            }
        }

        log.debug("Leaving save()");
        return INPUT;
    }

    private void myValidate() {
        boolean earlyExit = false;

        if (StringUtils.isEmpty(bean.getName())) {
            addError("templates.error.nameNull");
            earlyExit = true;
        }

        // make sure relative path exists if required
        if (bean.getRole().isAccessibleViaUrl() && StringUtils.isEmpty(bean.getRelativePath())) {
            addError("templateEdit.error.relativePathRequired");
            earlyExit = true;
        }

        if (!earlyExit) {
            try {
                WeblogTheme testTheme = new WeblogTheme(weblogManager, getActionWeblog(),
                        themeManager.getSharedTheme(getActionWeblog().getTheme()));

                // if initial save or name changed make sure there isn't a conflict
                if ((template == null && !TemplateDerivation.OVERRIDDEN.equals(getBean().getDerivation()))
                        || (template !=null && !getBean().getName().equals(getTemplate().getName()))) {
                    if (testTheme.getTemplateByName(getBean().getName()) != null) {
                        addError("templates.error.nameAlreadyExists", getBean().getName());
                    }
                }

                // same check for path
                if (bean.getRole().isAccessibleViaUrl() &&
                        ((template == null && !TemplateDerivation.OVERRIDDEN.equals(getBean().getDerivation()))
                                || (template != null && !StringUtils.isEmpty(getBean().getRelativePath()) &&
                        !getBean().getRelativePath().equals(getTemplate().getRelativePath())))) {
                    if (testTheme.getTemplateByPath(getBean().getRelativePath()) != null) {
                        addError("templates.error.pathAlreadyExists", getBean().getRelativePath());
                    }
                }

            } catch (WebloggerException ex) {
                addError("Error validating save -- check application logs.");
                log.error("Error validating data", ex);
            }
        }
    }

    public Map<TemplateLanguage, String> getTemplateLanguages() {
        Map<TemplateLanguage, String> langMap = new EnumMap<>(TemplateLanguage.class);
        for (TemplateLanguage lang : TemplateLanguage.values()) {
            langMap.put(lang, lang.getReadableName());
        }
        return langMap;
    }

    public String delete() {
        if (template != null && !hasActionErrors()) {
            try {
                // Remove template and its renditions
                weblogManager.removeTemplate(template);
                weblogManager.saveWeblog(getActionWeblog());
                WebloggerFactory.flush();

                // notify caches
                cacheManager.invalidate(template);

                // success message
                addMessage("templateEdit.delete.success", template.getName());

                return LIST;
            } catch (Exception e) {
                log.error("Error deleting template for weblog - " + getActionWeblog().getHandle(), e);
                addError("generic.error.check.logs");
            }
        }
        return INPUT;
    }
    public WeblogTemplate getBean() {
        return bean;
    }

    public void setBean(WeblogTemplate bean) {
        this.bean = bean;
    }

    public WeblogTemplate getTemplate() {
        return template;
    }

    public void setTemplate(WeblogTemplate template) {
        this.template = template;
    }

    public boolean isNameChangeable() {
        return nameChangeable;
    }

}
