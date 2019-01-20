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
package org.tightblog.rendering.processors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.domain.SharedTheme;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template;
import org.tightblog.domain.Template.ComponentType;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogRole;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.thymeleaf.ThymeleafRenderer;
import org.tightblog.rendering.cache.CachedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.repository.WeblogRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Responsible for rendering weblog page previews, for either of two purposes:
 *
 * - Preview of what a weblog will look like with a given shared theme, used
 *   when blogger is evaluating switching themes.  Here, a URL parameter with
 *   the shared theme name will be provided.  (Although a preview is normally
 *   given just for the home page, any preview URL with the theme name parameter
 *   will provide a preview of that URL--preview by category, date, blog entry, etc.)
 *
 * - Preview of a blog entry prior to publishing, with the user's current theme.
 *   No URL parameter for the theme provided.
 *
 * Previews are obtainable only through the authoring interface by a logged-in user
 * having at least EDIT_DRAFT rights on the blog being previewed.
 */
@RestController
@RequestMapping(path = "/tb-ui/authoring/preview/**")
public class PreviewProcessor extends AbstractProcessor {

    private static Logger log = LoggerFactory.getLogger(PreviewProcessor.class);

    private WeblogRepository weblogRepository;

    private ThymeleafRenderer thymeleafRenderer;
    protected ThemeManager themeManager;
    protected UserManager userManager;
    private WeblogEntryManager weblogEntryManager;
    private PageModel pageModel;
    private Function<WeblogPageRequest, SiteModel> siteModelFactory;

    @Autowired
    PreviewProcessor(WeblogRepository weblogRepository, @Qualifier("blogRenderer") ThymeleafRenderer thymeleafRenderer,
                            ThemeManager themeManager, UserManager userManager, PageModel pageModel,
                            WeblogEntryManager weblogEntryManager, Function<WeblogPageRequest, SiteModel> siteModelFactory) {
        this.weblogRepository = weblogRepository;
        this.thymeleafRenderer = thymeleafRenderer;
        this.themeManager = themeManager;
        this.userManager = userManager;
        this.pageModel = pageModel;
        this.weblogEntryManager = weblogEntryManager;
        this.siteModelFactory = siteModelFactory;
    }

    @RequestMapping(method = RequestMethod.GET)
    void getPreviewPage(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WeblogPageRequest incomingRequest = WeblogPageRequest.Creator.createPreview(request, pageModel);

        Weblog weblog = weblogRepository.findByHandleAndVisibleTrue(incomingRequest.getWeblogHandle());
        if (weblog == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        } else {
            incomingRequest.setWeblog(weblog);
        }

        // User must have access rights on blog being previewed
        if (!userManager.checkWeblogRole(incomingRequest.getAuthenticatedUser(), weblog, WeblogRole.EDIT_DRAFT)) {
            log.warn("User {} attempting to preview blog {} without access rights, blocking",
                    incomingRequest.getAuthenticatedUser(), weblog.getHandle());
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // if theme provided, indicates a theme preview rather than an entry draft preview
        String previewThemeName = request.getParameter("theme");
        if (previewThemeName != null) {
            try {
                SharedTheme previewTheme = themeManager.getSharedTheme(previewThemeName);
                Weblog previewWeblog = new Weblog(weblog);
                previewWeblog.setTheme(previewTheme.getId());
                previewWeblog.setUsedForThemePreview(true);
                incomingRequest.setWeblog(previewWeblog);
                weblog = previewWeblog;
            } catch (IllegalArgumentException tnfe) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }

        // figure out what template to use
        if (incomingRequest.getCustomPageName() != null) {
            Template template = themeManager.getWeblogTheme(weblog).getTemplateByPath(incomingRequest.getCustomPageName());

            // block internal custom pages from appearing directly
            if (template != null && !ComponentType.CUSTOM_INTERNAL.equals(template.getRole())) {
                incomingRequest.setTemplate(template);
            }
        } else {
            boolean invalid = false;

            if (incomingRequest.getWeblogEntryAnchor() != null) {
                WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, incomingRequest.getWeblogEntryAnchor());

                if (entry == null) {
                    invalid = true;
                } else {
                    incomingRequest.setWeblogEntry(entry);
                    incomingRequest.setTemplate(themeManager.getWeblogTheme(weblog).getTemplateByAction(ComponentType.PERMALINK));
                }
            }

            // use default template for other contexts (or, for entries, if PERMALINK template is undefined)
            if (!invalid && incomingRequest.getTemplate() == null) {
                incomingRequest.setTemplate(themeManager.getWeblogTheme(weblog).getTemplateByAction(ComponentType.WEBLOG));
            }
        }

        if (incomingRequest.getTemplate() == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", incomingRequest);

        // Load models for page previewing
        Map<String, Object> model = getModelMap("previewModelSet", initData);
        model.put("model", incomingRequest);

        // Load special models for site-wide blog
        if (themeManager.getSharedTheme(weblog.getTheme()).isSiteWide()) {
            model.put("site", siteModelFactory.apply(incomingRequest));
        }

        try {
            CachedContent rendererOutput = thymeleafRenderer.render(incomingRequest.getTemplate(), model);
            response.setContentType(rendererOutput.getComponentType().getContentType());
            response.setContentLength(rendererOutput.getContent().length);
            response.setHeader("Cache-Control", "no-cache");
            response.getOutputStream().write(rendererOutput.getContent());
        } catch (Exception e) {
            log.error("Error during rendering for page {}", incomingRequest.getTemplate().getId(), e);
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
