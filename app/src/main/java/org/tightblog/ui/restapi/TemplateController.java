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
package org.tightblog.ui.restapi;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.pojos.SharedTheme;
import org.tightblog.business.ThemeManager;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.pojos.WeblogTheme;
import org.tightblog.util.I18nMessages;
import org.tightblog.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
import javax.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

@RestController
public class TemplateController {

    private static Logger log = LoggerFactory.getLogger(TemplateController.class);

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

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

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{id}/templates", method = RequestMethod.GET)
    public WeblogTemplateData getWeblogTemplates(@PathVariable String id, Principal principal,
                                                 HttpServletResponse response) {

        Weblog weblog = weblogManager.getWeblog(id);
        User user = userManager.getEnabledUserByUserName(principal.getName());

        if (weblog != null && user != null && userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
            I18nMessages messages = user.getI18NMessages();

            WeblogTemplateData wtd = new WeblogTemplateData();

            WeblogTheme theme = new WeblogTheme(weblogManager, weblog,
                    themeManager.getSharedTheme(weblog.getTheme()));

            List<? extends Template> raw = theme.getTemplates();
            List<Template> pages = new ArrayList<>();
            pages.addAll(raw);
            wtd.templates = pages;

            // build list of action types that may be added
            List<Template.ComponentType> availableRoles = new ArrayList<>(Arrays.asList(Template.ComponentType.values()));

            // remove from above list any already existing for the theme
            pages.stream().filter(p -> p.getRole().isSingleton()).forEach(p ->
                    availableRoles.removeIf(r -> r.name().equals(p.getRole().name())));

            availableRoles.forEach(role -> wtd.availableTemplateRoles.put(role.getName(), role.getReadableName()));
            availableRoles.forEach(role -> wtd.templateRoleDescriptions.put(role.getName(), messages.getString(role.getDescriptionProperty())));
            return wtd;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public class WeblogTemplateData {
        List<Template> templates;
        Map<String, String> availableTemplateRoles = new HashMap<>();
        Map<String, String> templateRoleDescriptions = new HashMap<>();

        public List<Template> getTemplates() {
            return templates;
        }

        public void setTemplates(List<Template> templates) {
            this.templates = templates;
        }

        public Map<String, String> getAvailableTemplateRoles() {
            return availableTemplateRoles;
        }

        public Map<String, String> getTemplateRoleDescriptions() {
            return templateRoleDescriptions;
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

    @RequestMapping(value = "/tb-ui/authoring/rest/template/{id}", method = RequestMethod.GET)
    public WeblogTemplate getWeblogTemplate(@PathVariable String id, Principal p, HttpServletResponse response) {

        WeblogTemplate template = weblogManager.getTemplate(id);

        boolean permitted = template != null &&
                userManager.checkWeblogRole(p.getName(), template.getWeblog().getHandle(), WeblogRole.POST);
        if (permitted) {
            if (themeManager.getSharedTheme(template.getWeblog().getTheme()).getTemplateByName(template.getName()) != null) {
                template.setDerivation(Template.TemplateDerivation.OVERRIDDEN);
            }
            template.setContents(template.getTemplate());
            return template;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    // need to add / at end of URL due to template name possibly having a period in it (e.g., basic-custom.css).
    // none of other solutions (http://stackoverflow.com/questions/16332092/spring-mvc-pathvariable-with-dot-is-getting-truncated)
    // seemed to work.
    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templatename/{templateName}/", method = RequestMethod.GET)
    public WeblogTemplate getWeblogTemplateByName(@PathVariable String weblogId, @PathVariable String templateName, Principal p, HttpServletResponse response) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        // First-time override of a shared template
        SharedTheme sharedTheme = themeManager.getSharedTheme(weblog.getTheme());
        Template sharedTemplate = sharedTheme.getTemplateByName(templateName);

        boolean permitted = sharedTemplate != null &&
                userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.POST);
        if (permitted) {
            WeblogTemplate template = themeManager.createWeblogTemplate(weblog, sharedTemplate);
            template.setContents(template.getTemplate());
            return template;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templates", method = RequestMethod.POST)
    public ResponseEntity postTemplate(@PathVariable String weblogId, @Valid @RequestBody WeblogTemplate templateData,
                                      Principal p) throws ServletException {
        try {

            boolean createNew = false;
            WeblogTemplate templateToSave = weblogManager.getTemplate(templateData.getId());

            // Check user permissions
            User user = userManager.getEnabledUserByUserName(p.getName());
            I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();

            Weblog weblog = (templateToSave == null) ? weblogManager.getWeblog(weblogId) : templateToSave.getWeblog();

            if (weblog != null && userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {

                // create new?
                if (templateToSave == null) {
                    createNew = true;
                    templateToSave = new WeblogTemplate();
                    templateToSave.setWeblog(weblog);
                    if (templateData.getRole() != null) {
                        templateToSave.setRole(templateData.getRole());
                    } else {
                        templateToSave.setRole(Template.ComponentType.valueOf(templateData.getRoleName()));
                    }
                    templateToSave.setTemplate(templateData.getContents());
                } else {
                    templateToSave.setTemplate(templateData.getContents());
                }

                // some properties relevant only for certain template roles
                if (!templateToSave.getRole().isSingleton()) {
                    templateToSave.setDescription(templateData.getDescription());
                }

                if (templateToSave.getRole().isAccessibleViaUrl()) {
                    templateToSave.setRelativePath(templateData.getRelativePath());
                }

                if (Template.TemplateDerivation.SPECIFICBLOG.equals(templateToSave.getDerivation())) {
                    templateToSave.setName(templateData.getName());
                }

                templateToSave.setLastModified(Instant.now());

                ValidationError maybeError = advancedValidate(templateToSave, createNew);
                if (maybeError != null) {
                    return ResponseEntity.badRequest().body(maybeError);
                }

                weblogManager.saveTemplate(templateToSave);
                persistenceStrategy.flush();

                return ResponseEntity.ok(templateToSave.getId());
            } else {
                return ResponseEntity.status(403).body(messages.getString("error.title.403"));
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private ValidationError advancedValidate(WeblogTemplate templateToCheck, boolean initialSave) {
        BindException be = new BindException(templateToCheck, "new data object");

        // make sure relative path exists if required
        if (!initialSave && templateToCheck.getRole().isAccessibleViaUrl()) {
            if (StringUtils.isEmpty(templateToCheck.getRelativePath())) {
                be.addError(new ObjectError("WeblogTemplate", bundle.getString("templateEdit.error.relativePathRequired")));
            } else {
                WeblogTemplate test = weblogManager.getTemplateByPath(templateToCheck.getWeblog(), templateToCheck.getRelativePath());
                if (test != null && !test.getId().equals(templateToCheck.getId())) {
                    be.addError(new ObjectError("WeblogTemplate", bundle.getString("templates.error.pathAlreadyExists")));
                }
            }
        }

        WeblogTemplate template = weblogManager.getTemplateByName(templateToCheck.getWeblog(), templateToCheck.getName());
        if (template != null && !template.getId().equals(templateToCheck.getId())) {
            be.addError(new ObjectError("WeblogTemplate", bundle.getString("templates.error.nameAlreadyExists")));
        }

        if (templateToCheck.getRole().isSingleton()) {
            template = weblogManager.getTemplateByAction(templateToCheck.getWeblog(), templateToCheck.getRole());
            if (template != null && !template.getId().equals(templateToCheck.getId())) {
                be.addError(new ObjectError("WeblogTemplate",
                        bundle.getString("templates.error.singletonActionAlreadyExists")));
            }
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }

}
