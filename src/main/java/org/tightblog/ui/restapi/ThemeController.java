package org.tightblog.ui.restapi;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.PostMapping;
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

    @Autowired
    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @Autowired
    private MessageSource messages;

    @PostMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/switchtheme/{newThemeId}", produces = "text/plain")
    public ResponseEntity switchTheme(@PathVariable String weblogId, @PathVariable String newThemeId, Principal p,
                                      Locale locale) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        SharedTheme newTheme = themeManager.getSharedTheme(newThemeId);
        User user = userManager.getEnabledUserByUserName(p.getName());

        if (weblog != null && newTheme != null) {
            if (userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
                ValidationError maybeError = advancedValidate(weblog, newTheme, locale);
                if (maybeError != null) {
                    return ResponseEntity.badRequest().body(maybeError);
                }

                // Remove old template overrides
                List<WeblogTemplate> oldTemplates = weblogManager.getTemplates(weblog);

                for (WeblogTemplate template : oldTemplates) {
                    if (template.getDerivation() == Template.TemplateDerivation.OVERRIDDEN) {
                        weblogManager.removeTemplate(template);
                    }
                }

                weblog.setTheme(newThemeId);

                log.debug("Saving theme {} for weblog {}", newThemeId, weblog.getHandle());

                // save updated weblog and flush
                weblogManager.saveWeblog(weblog);
                persistenceStrategy.flush();

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

        WeblogTheme oldTheme = new WeblogTheme(weblogManager, weblog, themeManager.getSharedTheme(weblog.getTheme()));

        oldTheme.getTemplates().stream().filter(
                old -> old.getDerivation() == Template.TemplateDerivation.SPECIFICBLOG).forEach(old -> {
                    if (old.getRole().isSingleton() && newTheme.getTemplateByAction(old.getRole()) != null) {
                        be.addError(new ObjectError("Weblog object", messages.getMessage("themeEdit.conflicting.singleton.role",
                                new Object[]{old.getRole().getReadableName()}, locale)));
                    } else if (newTheme.getTemplateByName(old.getName()) != null) {
                        be.addError(new ObjectError("Weblog object", messages.getMessage("themeEdit.conflicting.name",
                                new Object[]{old.getName()}, locale)));
                    } else {
                        String maybePath = old.getRelativePath();
                        if (maybePath != null && newTheme.getTemplateByPath(maybePath) != null) {
                            be.addError(new ObjectError("Weblog object", messages.getMessage("themeEdit.conflicting.link",
                                    new Object[]{old.getRelativePath()}, locale)));
                        }
                    }
        });

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }

}
