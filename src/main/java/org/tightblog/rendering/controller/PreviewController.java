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
package org.tightblog.rendering.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mobile.device.Device;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.model.SiteModel;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Template.Role;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogRole;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.rendering.service.ThymeleafRenderer;
import org.tightblog.rendering.cache.CachedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tightblog.dao.WeblogDao;
import org.tightblog.util.Utilities;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Shows preview of a blog entry prior to publishing.
 *
 * Previews are obtainable only through the authoring interface by a logged-in user
 * having at least EDIT_DRAFT rights on the blog being previewed.
 */
@RestController
@RequestMapping(path = "/tb-ui/authoring/preview")
public class PreviewController extends AbstractController {

    private static Logger log = LoggerFactory.getLogger(PreviewController.class);

    private WeblogDao weblogDao;

    private ThymeleafRenderer thymeleafRenderer;
    protected ThemeManager themeManager;
    protected UserManager userManager;
    private WeblogEntryManager weblogEntryManager;
    private PageModel pageModel;
    private Function<WeblogPageRequest, SiteModel> siteModelFactory;

    @Autowired
    PreviewController(WeblogDao weblogDao, @Qualifier("blogRenderer") ThymeleafRenderer thymeleafRenderer,
                      ThemeManager themeManager, UserManager userManager, PageModel pageModel,
                      WeblogEntryManager weblogEntryManager, Function<WeblogPageRequest, SiteModel> siteModelFactory) {
        this.weblogDao = weblogDao;
        this.thymeleafRenderer = thymeleafRenderer;
        this.themeManager = themeManager;
        this.userManager = userManager;
        this.pageModel = pageModel;
        this.weblogEntryManager = weblogEntryManager;
        this.siteModelFactory = siteModelFactory;
    }

    @GetMapping(path = "/{weblogHandle}/entry/{anchor}")
    ResponseEntity<Resource> getEntryPreview(@PathVariable String weblogHandle, @PathVariable String anchor,
                                            Principal principal, Device device) throws IOException {

        Weblog weblog = weblogDao.findByHandleAndVisibleTrue(weblogHandle);
        if (weblog == null) {
            return ResponseEntity.notFound().build();
        }

        // User must have access rights on blog being previewed
        if (!userManager.checkWeblogRole(principal.getName(), weblog, WeblogRole.EDIT_DRAFT)) {
            log.warn("User {} attempting to preview blog {} without access rights, blocking",
                    principal.getName(), weblog.getHandle());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        WeblogPageRequest incomingRequest = new WeblogPageRequest(weblogHandle, principal, pageModel, true);
        incomingRequest.setNoIndex(true);
        incomingRequest.setWeblog(weblog);
        incomingRequest.setWeblogEntryAnchor(Utilities.decode(anchor));
        incomingRequest.setDeviceType(Utilities.getDeviceType(device));

        WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog,
                incomingRequest.getWeblogEntryAnchor());

        if (entry == null) {
            log.warn("For weblog {}, invalid entry {} requested, returning 404", weblogHandle, anchor);
            return ResponseEntity.notFound().build();
        } else {
            incomingRequest.setWeblogEntry(entry);
            incomingRequest.setTemplate(
                    themeManager.getWeblogTheme(weblog).getTemplateByRole(Role.PERMALINK));

            if (incomingRequest.getTemplate() == null) {
                incomingRequest.setTemplate(themeManager.getWeblogTheme(weblog).getTemplateByRole(Role.WEBLOG));
            }

            if (incomingRequest.getTemplate() == null) {
                log.warn("For weblog {}, entry {}, no template available, returning 404", weblogHandle, anchor);
                return ResponseEntity.notFound().build();
            }
        }

        // populate the rendering model
        Map<String, Object> initData = new HashMap<>();
        initData.put("parsedRequest", incomingRequest);

        // Load models for page previewing
        Map<String, Object> model = getModelMap("pageModelSet", initData);
        model.put("model", incomingRequest);

        // Load special models for site-wide blog
        if (themeManager.getSharedTheme(weblog.getTheme()).isSiteWide()) {
            model.put("site", siteModelFactory.apply(incomingRequest));
        }

        CachedContent rendererOutput = thymeleafRenderer.render(incomingRequest.getTemplate(), model);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf(rendererOutput.getRole().getContentType()))
                .contentLength(rendererOutput.getContent().length)
                // no-store: must pull each time from server when requested
                .cacheControl(CacheControl.noStore())
                .body(new ByteArrayResource(rendererOutput.getContent()));
    }
}
