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

package org.apache.roller.weblogger.business.themes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * The rules for adding, changing and removing a weblog's templates. The
 * template editor pages and the AtomPub template collection both use this
 * class, so they enforce the same rules. Callers flush the session.
 */
public class WeblogTemplateEditor {

    private final Weblogger roller;

    public WeblogTemplateEditor(Weblogger roller) {
        this.roller = roller;
    }

    /**
     * The template actions that may still be added to the weblog. A custom
     * theme may have one template per non-custom action; a shared theme may
     * add only custom templates and, if it has none yet, a Weblog template.
     */
    public Set<ComponentType> availableActions(Weblog weblog) throws WebloggerException {
        List<WeblogTemplate> templates = roller.getWeblogManager().getTemplates(weblog);
        Set<ComponentType> actions = EnumSet.of(ComponentType.CUSTOM);
        if (WeblogTheme.CUSTOM.equals(weblog.getEditorTheme())) {
            actions.addAll(EnumSet.of(ComponentType.PERMALINK, ComponentType.SEARCH,
                    ComponentType.WEBLOG, ComponentType.TAGSINDEX));
            for (WeblogTemplate template : templates) {
                if (!ComponentType.CUSTOM.equals(template.getAction())) {
                    actions.remove(template.getAction());
                }
            }
        } else {
            actions.add(ComponentType.WEBLOG);
            for (WeblogTemplate template : templates) {
                if (ComponentType.WEBLOG.equals(template.getAction())) {
                    actions.remove(ComponentType.WEBLOG);
                    break;
                }
            }
        }
        return actions;
    }

    /** Checks the name and action of a template that is about to be added. */
    public List<TemplateRuleException.Violation> validateNew(Weblog weblog,
            ComponentType action, String name) throws WebloggerException {
        List<TemplateRuleException.Violation> violations = new ArrayList<>();
        if (StringUtils.isEmpty(name)) {
            violations.add(invalid("Template.error.nameNull"));
        } else if (name.length() > RollerConstants.TEXTWIDTH_255) {
            violations.add(invalid("Template.error.nameSize"));
        }
        if (action == null) {
            violations.add(invalid("Template.error.actionNull"));
        } else if (!availableActions(weblog).contains(action)) {
            violations.add(new TemplateRuleException.Violation(
                    "Template.error.actionNotAvailable",
                    List.of(action.getReadableName()), true));
        }
        if (StringUtils.isNotEmpty(name)
                && roller.getWeblogManager().getTemplateByName(weblog, name) != null) {
            violations.add(conflict(name));
        }
        return violations;
    }

    /**
     * Adds a template with a standard rendition and, when
     * {@code mobileSource} is not null, a mobile rendition.
     */
    public WeblogTemplate create(Weblog weblog, ComponentType action, String name,
            String standardSource, String mobileSource)
            throws TemplateRuleException, WebloggerException {

        List<TemplateRuleException.Violation> violations = validateNew(weblog, action, name);
        if (!violations.isEmpty()) {
            throw new TemplateRuleException(violations);
        }

        WeblogManager mgr = roller.getWeblogManager();
        WeblogTemplate template = new WeblogTemplate();
        template.setWeblog(weblog);
        template.setAction(action);
        template.setName(name);
        template.setHidden(false);
        template.setNavbar(false);
        template.setLastModified(new Date());
        if (ComponentType.CUSTOM.equals(action)) {
            template.setLink(name);
        }
        // Always keep a Weblog main page
        if (ComponentType.WEBLOG.equals(action)) {
            template.setName(WeblogTemplate.DEFAULT_PAGE);
        }
        mgr.saveTemplate(template);

        saveRendition(template, RenditionType.STANDARD, standardSource);
        if (mobileSource != null) {
            saveRendition(template, RenditionType.MOBILE, mobileSource);
        }

        // A new Weblog template becomes the weblog's default page
        if (WeblogTemplate.DEFAULT_PAGE.equals(template.getName())) {
            mgr.saveWeblog(weblog);
        }
        return template;
    }

    /**
     * Checks a new name and link for an existing template: neither may be
     * used by another template of the same weblog.
     */
    public List<TemplateRuleException.Violation> validateUpdate(Weblog weblog,
            WeblogTemplate template, String newName, String newLink) throws WebloggerException {
        List<TemplateRuleException.Violation> violations = new ArrayList<>();
        WeblogManager mgr = roller.getWeblogManager();
        if (newName != null && !newName.equals(template.getName())
                && mgr.getTemplateByName(weblog, newName) != null) {
            violations.add(conflict(newName));
        }
        if (StringUtils.isNotEmpty(newLink) && !newLink.equals(template.getLink())
                && mgr.getTemplateByLink(weblog, newLink) != null) {
            violations.add(conflict(newLink));
        }
        return violations;
    }

    /**
     * Sets the source of a rendition, creating the rendition when the
     * template does not have it yet.
     */
    public void saveRendition(WeblogTemplate template, RenditionType type, String source)
            throws WebloggerException {
        CustomTemplateRendition rendition = template.getTemplateRendition(type);
        if (rendition == null) {
            rendition = new CustomTemplateRendition(template, type);
            rendition.setTemplateLanguage(TemplateLanguage.VELOCITY);
        }
        rendition.setTemplate(source != null ? source : "");
        roller.getWeblogManager().saveTemplateRendition(rendition);
    }

    /** Removes the template's mobile rendition, if it has one. */
    public void removeMobileRendition(WeblogTemplate template) throws WebloggerException {
        CustomTemplateRendition mobile = template.getTemplateRendition(RenditionType.MOBILE);
        if (mobile != null) {
            roller.getWeblogManager().removeTemplateRendition(mobile);
        }
    }

    /**
     * Removes a template. A required template of a custom theme cannot be
     * removed. Removing the Weblog template also removes the theme's custom
     * stylesheet.
     */
    public void remove(Weblog weblog, WeblogTemplate template)
            throws TemplateRuleException, WebloggerException {
        if (template.isRequired() && WeblogTheme.CUSTOM.equals(weblog.getEditorTheme())) {
            throw new TemplateRuleException(Collections.singletonList(
                    new TemplateRuleException.Violation(
                            "editPages.remove.requiredTemplate", List.of(), true)));
        }
        WeblogManager mgr = roller.getWeblogManager();
        if (WeblogTemplate.DEFAULT_PAGE.equals(template.getName())) {
            ThemeTemplate stylesheet = weblog.getTheme().getStylesheet();
            if (stylesheet != null) {
                WeblogTemplate css = mgr.getTemplateByLink(weblog, stylesheet.getLink());
                if (css != null) {
                    mgr.removeTemplate(css);
                }
            }
        }
        CacheManager.invalidate(template);
        mgr.removeTemplate(template);
    }

    private static TemplateRuleException.Violation invalid(String key) {
        return new TemplateRuleException.Violation(key, List.of(), false);
    }

    private static TemplateRuleException.Violation conflict(String value) {
        return new TemplateRuleException.Violation(
                "pagesForm.error.alreadyExists", List.of(value), true);
    }
}
