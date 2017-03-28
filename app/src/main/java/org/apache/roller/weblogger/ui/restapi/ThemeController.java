package org.apache.roller.weblogger.ui.restapi;

import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

@RestController
public class ThemeController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

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
    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/weblog/{weblogId}/switchtheme/{newThemeId}", method = RequestMethod.POST)
    public ResponseEntity switchTheme(@PathVariable String weblogId, @PathVariable String newThemeId, Principal p) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        SharedTheme newTheme = themeManager.getSharedTheme(newThemeId);
        User user = userManager.getEnabledUserByUserName(p.getName());
        I18nMessages messages = user.getI18NMessages();

        if (weblog != null && newTheme != null) {
            if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                ValidationError maybeError = advancedValidate(weblog, newTheme, messages);
                if (maybeError != null) {
                    return ResponseEntity.badRequest().body(maybeError);
                }

                // Remove old template overrides and their renditions
                List<WeblogTemplate> oldTemplates = weblogManager.getTemplates(weblog);

                for (WeblogTemplate template : oldTemplates) {
                    // Remove template overrides and their renditions
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
                String msg = messages.getString("themeEdit.setTheme.success", newTheme.getName());

                return ResponseEntity.ok(msg);
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }

    private ValidationError advancedValidate(Weblog weblog, SharedTheme newTheme, I18nMessages messages) {
        BindException be = new BindException(weblog, "new data object");

        WeblogTheme oldTheme = new WeblogTheme(weblogManager, weblog, themeManager.getSharedTheme(weblog.getTheme()));

        oldTheme.getTemplates().stream().filter(
                old -> old.getDerivation() == Template.TemplateDerivation.SPECIFICBLOG).forEach(old -> {
            if (old.getRole().isSingleton() && newTheme.getTemplateByAction(old.getRole()) != null) {
                be.addError(new ObjectError("Weblog object", messages.getString("themeEdit.conflicting.singleton.role",
                        old.getRole().getReadableName())));
            } else if (newTheme.getTemplateByName(old.getName()) != null) {
                be.addError(new ObjectError("Weblog object", messages.getString("themeEdit.conflicting.name",
                        old.getName())));
            } else {
                String maybePath = old.getRelativePath();
                if (maybePath != null && newTheme.getTemplateByPath(maybePath) != null) {
                    be.addError(new ObjectError("Weblog object", messages.getString("themeEdit.conflicting.link",
                            old.getRelativePath())));
                }
            }
        });

        return be.getErrorCount() > 0 ? ValidationError.fromBindingErrors(be) : null;
    }

}
