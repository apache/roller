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
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WeblogTemplate;
import org.tightblog.domain.WeblogTheme;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WeblogTemplateRepository;
import org.tightblog.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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
import java.util.stream.Collectors;

@RestController
public class TemplateController {

    private static Logger log = LoggerFactory.getLogger(TemplateController.class);

    private WeblogRepository weblogRepository;
    private WeblogTemplateRepository weblogTemplateRepository;
    private UserRepository userRepository;
    private UserManager userManager;
    private WeblogManager weblogManager;
    private ThemeManager themeManager;
    private MessageSource messages;

    @Autowired
    public TemplateController(WeblogRepository weblogRepository, WeblogTemplateRepository weblogTemplateRepository,
                              UserRepository userRepository, UserManager userManager, WeblogManager weblogManager,
                              ThemeManager themeManager, MessageSource messages) {
        this.weblogRepository = weblogRepository;
        this.weblogTemplateRepository = weblogTemplateRepository;
        this.userRepository = userRepository;
        this.userManager = userManager;
        this.weblogManager = weblogManager;
        this.themeManager = themeManager;
        this.messages = messages;
    }

    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{id}/templates")
    public WeblogTemplateData getWeblogTemplates(@PathVariable String id, Principal principal,
                                                 Locale locale, HttpServletResponse response) {

        Weblog weblog = weblogRepository.findById(id).orElse(null);
        User user = userRepository.findEnabledByUserName(principal.getName());

        if (weblog != null && user != null && userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
            WeblogTemplateData wtd = new WeblogTemplateData();

            WeblogTheme theme = new WeblogTheme(weblogTemplateRepository, weblog,
                    themeManager.getSharedTheme(weblog.getTheme()));

            wtd.templates = new ArrayList<>(theme.getTemplates());

            // build list of action types that may be added
            List<Template.ComponentType> availableRoles = Arrays.stream(Template.ComponentType.values()).
                    filter(Template.ComponentType::isBlogComponent).
                    collect(Collectors.toList());

            // remove from above list any already existing for the theme
            wtd.templates.stream().filter(p -> p.getRole().isSingleton()).forEach(p ->
                    availableRoles.removeIf(r -> r.name().equals(p.getRole().name())));

            availableRoles.forEach(role -> wtd.availableTemplateRoles.put(role.getName(), role.getReadableName()));
            availableRoles.forEach(role -> wtd.templateRoleDescriptions.put(role.getName(),
                    messages.getMessage(role.getDescriptionProperty(), null, locale)));
            return wtd;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class WeblogTemplateData {
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

    private void deleteTemplate(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogTemplate template = weblogTemplateRepository.findById(id).orElse(null);
            if (template != null) {
                if (userManager.checkWeblogRole(p.getName(), template.getWeblog(), WeblogRole.OWNER)) {
                    weblogTemplateRepository.delete(template);
                    weblogTemplateRepository.evictWeblogTemplates(template.getWeblog());
                    weblogManager.saveWeblog(template.getWeblog(), true);
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

    @PostMapping(value = "/tb-ui/authoring/rest/templates/delete")
    public void deleteTemplates(@RequestBody List<String> ids, Principal p, HttpServletResponse response)
            throws ServletException {
        if (ids != null && ids.size() > 0) {
            for (String id : ids) {
                deleteTemplate(id, p, response);
            }
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @GetMapping(value = "/tb-ui/authoring/rest/template/{id}")
    public WeblogTemplate getWeblogTemplate(@PathVariable String id, Principal p, HttpServletResponse response) {

        WeblogTemplate template = weblogTemplateRepository.findById(id).orElse(null);

        boolean permitted = template != null &&
                userManager.checkWeblogRole(p.getName(), template.getWeblog(), WeblogRole.POST);
        if (permitted) {
            if (themeManager.getSharedTheme(template.getWeblog().getTheme()).getTemplateByName(template.getName()) != null) {
                template.setDerivation(Template.TemplateDerivation.OVERRIDDEN);
            }
            return template;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    // Used to obtain shared templates not yet customized for a particular weblog
    // need to add / at end of URL due to template name possibly having a period in it (e.g., rolling.css).
    // none of other solutions (http://stackoverflow.com/questions/16332092/spring-mvc-pathvariable-with-dot-is-getting-truncated)
    // seemed to work.
    @GetMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templatename/{templateName}/")
    public WeblogTemplate getWeblogTemplateByName(@PathVariable String weblogId, @PathVariable String templateName, Principal p,
                                                  HttpServletResponse response) {

        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
        // First-time override of a shared template
        SharedTheme sharedTheme = themeManager.getSharedTheme(weblog.getTheme());
        Template sharedTemplate = sharedTheme.getTemplateByName(templateName);

        boolean permitted = sharedTemplate != null &&
                userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST);
        if (permitted) {
            return themeManager.createWeblogTemplate(weblog, sharedTemplate);
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/templates", consumes = { "application/json" },
            produces = { "text/plain", "application/json" })
    public ResponseEntity postTemplate(@PathVariable String weblogId, @Valid @RequestBody WeblogTemplate templateData,
                                      Principal p, Locale locale) throws ServletException {
        try {
            boolean createNew = false;
            WeblogTemplate templateToSave = weblogTemplateRepository.findById(templateData.getId()).orElse(null);

            // Check user permissions
            User user = userRepository.findEnabledByUserName(p.getName());
            Weblog weblog = (templateToSave == null) ? weblogRepository.findById(weblogId).orElse(null)
                    : templateToSave.getWeblog();

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
                    templateToSave.setTemplate(templateData.getTemplate());
                } else {
                    templateToSave.setTemplate(templateData.getTemplate());
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

                ValidationError maybeError = advancedValidate(templateToSave, createNew, locale);
                if (maybeError != null) {
                    return ResponseEntity.badRequest().body(maybeError);
                }

                weblogTemplateRepository.save(templateToSave);
                weblogTemplateRepository.evictWeblogTemplates(templateToSave.getWeblog());
                weblogManager.saveWeblog(templateToSave.getWeblog(), true);

                return ResponseEntity.ok().contentType(MediaType.TEXT_PLAIN).body(templateToSave.getId());
            } else {
                return ResponseEntity.status(403).body(messages.getMessage("error.title.403", null,
                        locale));
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private ValidationError advancedValidate(WeblogTemplate templateToCheck, boolean initialSave, Locale locale) {
        BindException be = new BindException(templateToCheck, "new data object");

        // make sure relative path exists if required
        if (!initialSave && templateToCheck.getRole().isAccessibleViaUrl()) {
            if (StringUtils.isEmpty(templateToCheck.getRelativePath())) {
                be.addError(new ObjectError("WeblogTemplate",
                        messages.getMessage("templateEdit.error.relativePathRequired", null, locale)));
            } else {
                WeblogTemplate test = weblogTemplateRepository.findByWeblogAndRelativePath(templateToCheck.getWeblog(),
                        templateToCheck.getRelativePath());
                if (test != null && !test.getId().equals(templateToCheck.getId())) {
                    be.addError(new ObjectError("WeblogTemplate",
                            messages.getMessage("templates.error.pathAlreadyExists", null, locale)));
                }
            }
        }

        WeblogTemplate template = weblogTemplateRepository.findByWeblogAndName(templateToCheck.getWeblog(),
                templateToCheck.getName());
        if (template != null && !template.getId().equals(templateToCheck.getId())) {
            be.addError(new ObjectError("WeblogTemplate", messages.getMessage("templates.error.nameAlreadyExists",
                null, locale)));
        }

        if (templateToCheck.getRole().isSingleton()) {
            template = weblogTemplateRepository.findByWeblogAndRole(templateToCheck.getWeblog(), templateToCheck.getRole());
            if (template != null && !template.getId().equals(templateToCheck.getId())) {
                be.addError(new ObjectError("WeblogTemplate",
                        messages.getMessage("templates.error.singletonActionAlreadyExists", null, locale)));
            }
        }

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }
}
