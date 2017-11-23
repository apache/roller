package org.tightblog.rendering.thymeleaf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.IEngineConfiguration;
import org.thymeleaf.templateresolver.AbstractConfigurableTemplateResolver;
import org.thymeleaf.templateresource.ITemplateResource;
import org.thymeleaf.templateresource.StringTemplateResource;
import org.tightblog.business.WebloggerContext;
import org.tightblog.business.themes.SharedTheme;
import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.Template;

import java.util.Map;

public class ThemeTemplateResolver extends AbstractConfigurableTemplateResolver {

    private static Logger logger = LoggerFactory.getLogger(ThemeTemplateResolver.class);

    @Override
    protected ITemplateResource computeTemplateResource(IEngineConfiguration configuration, String ownerTemplate,
            String resourceId, String resourceName, String characterEncoding, Map<String, Object> templateResolutionAttributes) {

        logger.debug("Looking for: {}", resourceId);

        if (resourceId == null || resourceId.length() < 1) {
            logger.error("No resourceId provided");
            return null;
        }

        Template template;

        if (resourceId.contains(":")) {
            // shared theme, stored in a file
            String[] sharedThemeParts = resourceId.split(":", 2);
            if (sharedThemeParts.length != 2) {
                logger.error("Invalid Theme resource key {}", resourceId);
                return null;
            }
            ThemeManager themeMgr = WebloggerContext.getWeblogger().getThemeManager();
            SharedTheme theme = themeMgr.getSharedTheme(sharedThemeParts[0]);
            template = theme.getTemplateByName(sharedThemeParts[1]);
        } else {
            // weblog-only theme in database
            template = WebloggerContext.getWeblogger().getWeblogManager().getTemplate(resourceId);
        }

        if (template == null) {
            logger.error("Template {} not found", resourceId);
            return null;
        }

        final String contents = template.getTemplate();

        logger.debug("Resource found!");

        return new StringTemplateResource(contents);
    }
}
