package org.tightblog.ui.restapi;

import org.springframework.context.MessageSource;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;
import java.util.Locale;

@RestController
public class ThemeController {

    private static Logger log = LoggerFactory.getLogger(ThemeController.class);

    private WeblogRepository weblogRepository;
    private WeblogTemplateRepository weblogTemplateRepository;
    private UserRepository userRepository;
    private ThemeManager themeManager;
    private WeblogManager weblogManager;
    private UserManager userManager;
    private MessageSource messages;

    @Autowired
    public ThemeController(WeblogRepository weblogRepository, WeblogTemplateRepository weblogTemplateRepository,
                           UserRepository userRepository, ThemeManager themeManager, WeblogManager weblogManager,
                           UserManager userManager, MessageSource messages) {
        this.weblogRepository = weblogRepository;
        this.weblogTemplateRepository = weblogTemplateRepository;
        this.userRepository = userRepository;
        this.themeManager = themeManager;
        this.weblogManager = weblogManager;
        this.userManager = userManager;
        this.messages = messages;
    }

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/switchtheme/{newThemeId}", produces = "text/plain")
    public ResponseEntity switchTheme(@PathVariable String weblogId, @PathVariable String newThemeId, Principal p,
                                      Locale locale) {

        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
        SharedTheme newTheme = themeManager.getSharedTheme(newThemeId);
        User user = userRepository.findEnabledByUserName(p.getName());

        if (weblog != null && newTheme != null) {
            if (userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
                ValidationError maybeError = advancedValidate(weblog, newTheme, locale);
                if (maybeError != null) {
                    return ResponseEntity.badRequest().body(maybeError);
                }

                // Remove old template overrides
                List<WeblogTemplate> oldTemplates = weblogTemplateRepository.getWeblogTemplateMetadata(weblog);

                for (WeblogTemplate template : oldTemplates) {
                    if (template.getDerivation() == Template.Derivation.OVERRIDDEN) {
                        weblogTemplateRepository.deleteById(template.getId());
                        weblogManager.evictWeblogTemplateCaches(template.getWeblog(), template.getName(), template.getRole());
                    }
                }

                weblog.setTheme(newThemeId);

                log.debug("Saving theme {} for weblog {}", newThemeId, weblog.getHandle());

                // save updated weblog so its cached pages will expire
                weblogManager.saveWeblog(weblog, true);
                weblogTemplateRepository.evictWeblogTemplates(weblog);

                // Theme set to..
                String msg = messages.getMessage("themeEdit.setTheme.success", new Object[] {newTheme.getName()},
                        locale);

                return ResponseEntity.ok(msg);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    private ValidationError advancedValidate(Weblog weblog, SharedTheme newTheme, Locale locale) {
        BindException be = new BindException(weblog, "new data object");

        WeblogTheme oldTheme = new WeblogTheme(weblogTemplateRepository, weblog,
                themeManager.getSharedTheme(weblog.getTheme()));

        oldTheme.getTemplates().stream().filter(
                old -> old.getDerivation() == Template.Derivation.SPECIFICBLOG).forEach(old -> {
                    if (old.getRole().isSingleton() && newTheme.getTemplateByRole(old.getRole()) != null) {
                        be.addError(new ObjectError("Weblog object", messages.getMessage("themeEdit.conflicting.singleton.role",
                                new Object[]{old.getRole().getReadableName()}, locale)));
                    } else if (newTheme.getTemplateByName(old.getName()) != null) {
                        be.addError(new ObjectError("Weblog object", messages.getMessage("themeEdit.conflicting.name",
                                new Object[]{old.getName()}, locale)));
                    }
        });

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }
}
