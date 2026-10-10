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

package org.apache.roller.weblogger.webservices.atomprotocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.TemplateRuleException;
import org.apache.roller.weblogger.business.themes.WeblogTemplateEditor;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * The templates of a weblog that uses a custom theme, as an AtomPub
 * collection. Only weblog administrators may use it, and only while the
 * weblog's theme is custom and custom themes are allowed on the site;
 * otherwise the collection does not exist (404).
 *
 * <p>Each template is an entry: atom:title is the name, atom:summary the
 * description, atom:content (type text) the standard rendition, and
 * roller:rendition type="mobile" the mobile rendition. On PUT, a missing
 * mobile rendition is left as it is and an empty one is removed. roller:action,
 * roller:link, roller:navbar, roller:hidden and roller:contentType carry the
 * other properties; roller:required is read-only.
 */
public class TemplateCollection {

    private final Weblogger roller;
    private final User user;
    private final String atomURL;

    public TemplateCollection(User user, String atomURL) {
        this.user = user;
        this.atomURL = atomURL;
        this.roller = WebloggerFactory.getWeblogger();
    }

    /** True when the user may use the template collection of the weblog. */
    static boolean isAvailable(User user, Weblog weblog) {
        return WebloggerRuntimeConfig.getBooleanProperty("themes.customtheme.allowed")
                && WeblogTheme.CUSTOM.equals(weblog.getEditorTheme())
                && CollectionSupport.isAdmin(user, weblog);
    }

    public AtomFeed getCollection(AtomRequest areq) throws AtomException {
        Weblog weblog = requireWeblog(CollectionSupport.path(areq));
        try {
            AtomFeed feed = new AtomFeed();
            feed.setId(collectionURI(weblog));
            feed.setTitle(weblog.getName() + " templates");
            feed.setUpdated(new Date());
            feed.getLinks().add(CollectionSupport.link("self", collectionURI(weblog)));
            for (WeblogTemplate template : roller.getWeblogManager().getTemplates(weblog)) {
                feed.getEntries().add(createAtomEntry(weblog, template));
            }
            return feed;
        } catch (WebloggerException e) {
            throw new AtomException("Getting template collection", e);
        }
    }

    public AtomEntry getEntry(AtomRequest areq) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            return createAtomEntry(weblog, requireTemplate(weblog, pathInfo));
        } catch (WebloggerException e) {
            throw new AtomException("Getting template", e);
        }
    }

    public AtomEntry postEntry(AtomRequest areq, AtomEntry entry) throws AtomException {
        Weblog weblog = requireWeblog(CollectionSupport.path(areq));
        String actionName = entry.getExtension("action");
        ComponentType action = actionName == null ? ComponentType.CUSTOM : parseAction(actionName);
        try {
            String mobile = StringUtils.isBlank(entry.getMobileRendition())
                    ? null : entry.getMobileRendition();
            WeblogTemplate template = editor().create(weblog, action, entry.getTitle(),
                    CollectionSupport.value(entry.getContent()), mobile);
            applyProperties(weblog, template, entry);
            roller.getWeblogManager().saveTemplate(template);
            roller.flush();
            CacheManager.invalidate(template);
            return createAtomEntry(weblog, template);
        } catch (TemplateRuleException e) {
            throw ruleError(weblog, e);
        } catch (WebloggerException e) {
            throw new AtomException("Creating template", e);
        }
    }

    public void putEntry(AtomRequest areq, AtomEntry entry) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            WeblogTemplate template = requireTemplate(weblog, pathInfo);
            WeblogTemplateEditor editor = editor();
            if (entry.getContent() != null) {
                editor.saveRendition(template, RenditionType.STANDARD,
                        entry.getContent().getValue());
            }
            if (entry.getMobileRendition() != null) {
                // An empty mobile rendition removes it; mobile visitors then
                // see the standard rendition
                if (entry.getMobileRendition().isBlank()) {
                    editor.removeMobileRendition(template);
                } else {
                    editor.saveRendition(template, RenditionType.MOBILE, entry.getMobileRendition());
                }
            }
            applyProperties(weblog, template, entry);
            template.setLastModified(new Date());
            roller.getWeblogManager().saveTemplate(template);
            roller.flush();
            CacheManager.invalidate(template);
        } catch (TemplateRuleException e) {
            throw ruleError(weblog, e);
        } catch (WebloggerException e) {
            throw new AtomException("Updating template", e);
        }
    }

    public void deleteEntry(AtomRequest areq) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            editor().remove(weblog, requireTemplate(weblog, pathInfo));
            roller.flush();
        } catch (TemplateRuleException e) {
            throw ruleError(weblog, e);
        } catch (WebloggerException e) {
            throw new AtomException("Removing template", e);
        }
    }

    /**
     * Copies the name, description, link, navbar and hidden flags (custom
     * templates only, as in the template editor) and the content type.
     */
    private void applyProperties(Weblog weblog, WeblogTemplate template, AtomEntry entry)
            throws TemplateRuleException, AtomException, WebloggerException {
        String link = entry.getExtension("link");
        if (template.isCustom()) {
            String name = StringUtils.isNotEmpty(entry.getTitle()) ? entry.getTitle() : null;
            List<TemplateRuleException.Violation> violations =
                    editor().validateUpdate(weblog, template, name, link);
            if (!violations.isEmpty()) {
                throw new TemplateRuleException(violations);
            }
            if (name != null) {
                template.setName(name);
            }
            if (entry.getSummary() != null) {
                template.setDescription(entry.getSummary().getValue());
            }
            if (link != null) {
                template.setLink(link);
            }
            Boolean navbar = CollectionSupport.parseBoolean(entry.getExtension("navbar"));
            if (navbar != null) {
                template.setNavbar(navbar);
            }
            Boolean hidden = CollectionSupport.parseBoolean(entry.getExtension("hidden"));
            if (hidden != null) {
                template.setHidden(hidden);
            }
        }
        if (entry.getExtensions().containsKey("contentType")) {
            // empty content type means automatic detection, as in the editor
            template.setOutputContentType(
                    StringUtils.trimToNull(entry.getExtension("contentType")));
        }
    }

    private AtomEntry createAtomEntry(Weblog weblog, WeblogTemplate template)
            throws WebloggerException {
        String editURI = atomURL + "/" + weblog.getHandle() + "/template/" + template.getId();
        AtomEntry entry = new AtomEntry();
        entry.setId(editURI);
        entry.setTitle(template.getName());
        if (template.getDescription() != null) {
            entry.setSummary(CollectionSupport.text("text", template.getDescription()));
        }
        Date updated = template.getLastModified() != null ? template.getLastModified() : new Date();
        entry.setUpdated(updated);
        entry.setEdited(updated);

        CustomTemplateRendition standard = template.getTemplateRendition(RenditionType.STANDARD);
        entry.setContent(CollectionSupport.text("text",
                standard != null && standard.getTemplate() != null ? standard.getTemplate() : ""));
        CustomTemplateRendition mobile = template.getTemplateRendition(RenditionType.MOBILE);
        if (mobile != null) {
            entry.setMobileRendition(mobile.getTemplate() != null ? mobile.getTemplate() : "");
        }

        entry.setExtension("action", template.getAction().name().toLowerCase(Locale.ROOT));
        entry.setExtension("link", template.getLink());
        entry.setExtension("navbar", Boolean.toString(template.isNavbar()));
        entry.setExtension("hidden", Boolean.toString(template.isHidden()));
        entry.setExtension("contentType", template.getOutputContentType());
        entry.setExtension("required", Boolean.toString(template.isRequired()));

        List<AtomLink> links = new ArrayList<>();
        links.add(CollectionSupport.link("edit", editURI));
        entry.setLinks(links);
        return entry;
    }

    private Weblog requireWeblog(String[] pathInfo) throws AtomException {
        Weblog weblog = CollectionSupport.requireWeblog(
                roller, user, pathInfo, WeblogPermission.ADMIN);
        if (!isAvailable(user, weblog)) {
            // Templates of shared themes are not editable; do not reveal them
            throw new AtomNotFoundException("Cannot find template collection");
        }
        return weblog;
    }

    private WeblogTemplate requireTemplate(Weblog weblog, String[] pathInfo)
            throws AtomException, WebloggerException {
        WeblogManager mgr = roller.getWeblogManager();
        WeblogTemplate template = pathInfo.length > 2 ? mgr.getTemplate(weblog, pathInfo[2]) : null;
        if (template == null) {
            throw new AtomNotFoundException("Cannot find template");
        }
        return template;
    }

    private static ComponentType parseAction(String name) throws AtomException {
        try {
            return ComponentType.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw CollectionSupport.badRequest("Unknown template action: " + name);
        }
    }

    /** 409 when another template is in the way, otherwise 400, with the editor's message. */
    private static AtomException ruleError(Weblog weblog, TemplateRuleException e) {
        I18nMessages messages = I18nMessages.getMessages(weblog.getLocaleInstance());
        StringBuilder text = new StringBuilder();
        for (TemplateRuleException.Violation violation : e.getViolations()) {
            if (text.length() > 0) {
                text.append(' ');
            }
            text.append(messages.getString(violation.getMessageKey(), violation.getArgs()));
        }
        return e.isConflict() ? CollectionSupport.conflict(text.toString())
                : CollectionSupport.badRequest(text.toString());
    }

    private WeblogTemplateEditor editor() {
        return new WeblogTemplateEditor(roller);
    }

    private String collectionURI(Weblog weblog) {
        return atomURL + "/" + weblog.getHandle() + "/templates";
    }
}
