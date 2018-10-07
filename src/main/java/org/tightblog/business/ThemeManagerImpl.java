/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.business;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.tightblog.pojos.SharedTemplate;
import org.tightblog.pojos.SharedTheme;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.Template.ComponentType;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.pojos.WeblogTheme;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ServletContextAware;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Base implementation of a ThemeManager.
 * <p>
 * This particular implementation reads theme data off the filesystem and
 * assumes that those themes are not changeable at runtime.
 */
@Component("themeManager")
public class ThemeManagerImpl implements ThemeManager, ServletContextAware {

    private static Logger log = LoggerFactory.getLogger(ThemeManagerImpl.class);

    private ServletContext servletContext;

    @Autowired
    private ObjectMapper objectMapper;

    static {
        // TODO: figure out why PNG is missing from Java MIME types
        FileTypeMap map = FileTypeMap.getDefaultFileTypeMap();
        if (map instanceof MimetypesFileTypeMap) {
            try {
                ((MimetypesFileTypeMap) map).addMimeTypes("image/png png PNG");
            } catch (Exception ignored) {
            }
        }
    }

    @Autowired
    private WeblogManager weblogManager;

    // map of themes in format (theme id, Theme)
    private Map<String, SharedTheme> themeMap = new HashMap<>();

    // list of themes
    private List<SharedTheme> themeList = new ArrayList<>();

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ThemeManagerImpl() {
    }

    @Override
    @PostConstruct
    public void initialize() {
        String blogThemePath = "/blogthemes";

        Set<String> paths = servletContext.getResourcePaths(blogThemePath);

        if (paths != null && paths.size() > 0) {
            log.info("{} shared blog themes detected, loading...", paths.size());
            for (String path : paths) {
                try {
                    SharedTheme theme = loadThemeData(path);
                    themeMap.put(theme.getId(), theme);
                } catch (Exception unexpected) {
                    // shouldn't happen, so let's learn why it did
                    log.error("Exception processing theme {}, will be skipped", path, unexpected);
                }
            }

            // for convenience create an alphabetized list also
            themeList = new ArrayList<>(this.themeMap.values());
            themeList.sort(Comparator.comparing(SharedTheme::getName));
            log.info("Successfully loaded {} shared blog themes.", themeList.size());
        } else {
            log.info("No shared blog themes detected at path {}, none will be loaded", blogThemePath);
        }
    }

    @Override
    public SharedTheme getSharedTheme(String id) {
        SharedTheme theme = this.themeMap.get(id);
        if (theme == null) {
            throw new IllegalArgumentException("Couldn't find theme [" + id + "]");
        }
        return theme;
    }

    @Override
    public WeblogTheme getWeblogTheme(Weblog weblog) {
        WeblogTheme weblogTheme = null;

        SharedTheme staticTheme = this.themeMap.get(weblog.getTheme());
        if (staticTheme != null) {
            weblogTheme = new WeblogTheme(weblogManager, weblog, staticTheme);
        } else {
            log.warn("Unable to find shared theme {}", weblog.getTheme());
        }

        return weblogTheme;
    }

    @Override
    public List<SharedTheme> getEnabledSharedThemesList() {
        return themeList;
    }

    @Override
    public WeblogTemplate createWeblogTemplate(Weblog weblog, Template sharedTemplate) {
        WeblogTemplate weblogTemplate = new WeblogTemplate();
        weblogTemplate.setDerivation(Template.TemplateDerivation.OVERRIDDEN);
        weblogTemplate.setWeblog(weblog);
        weblogTemplate.setRole(sharedTemplate.getRole());
        weblogTemplate.setName(sharedTemplate.getName());
        weblogTemplate.setDescription(sharedTemplate.getDescription());
        weblogTemplate.setRelativePath(sharedTemplate.getRelativePath());
        weblogTemplate.setLastModified(Instant.now());
        weblogTemplate.setTemplate(sharedTemplate.getTemplate());

        return weblogTemplate;
    }

    private SharedTheme loadThemeData(String themePath) throws Exception {
        SharedTheme sharedTheme = null;
        String themeJson = themePath + "theme.json";

        try (InputStream is = servletContext.getResourceAsStream(themeJson)) {
            if (is != null) {
                sharedTheme = objectMapper.readValue(is, SharedTheme.class);
                sharedTheme.setThemeDir(themePath);

                // load resource representing preview image
                String previewFilePath = sharedTheme.getThemeDir() + sharedTheme.getPreviewImagePath();

                try (InputStream test = servletContext.getResourceAsStream(previewFilePath)) {
                    if (test == null) {
                        log.warn("Couldn't find theme [{}] thumbnail at path [{}]", sharedTheme.getName(), previewFilePath);
                    }
                }

                // create the templates based on the theme descriptor data
                boolean hasWeblogTemplate = false;
                for (SharedTemplate template : sharedTheme.getTemplates()) {

                    // one and only one template with action "weblog" allowed
                    if (ComponentType.WEBLOG.equals(template.getRole())) {
                        if (hasWeblogTemplate) {
                            throw new IllegalStateException("Theme has more than one template with action of 'weblog'");
                        } else {
                            hasWeblogTemplate = true;
                        }
                    }

                    if (!loadTemplateSource(sharedTheme.getThemeDir(), template)) {
                        throw new IllegalStateException("Couldn't load template [" + template.getContentsFile() + "]");
                    }

                    // this template ID used by template resolvers to retrieve the template.
                    template.setId(sharedTheme.getId() + ":" + template.getName());
                }

                if (!hasWeblogTemplate) {
                    throw new IllegalStateException("Theme " + sharedTheme.getName() + " has no template with 'weblog' action");
                }

                log.info("Loaded shared theme {}", sharedTheme);
            } else {
                throw new IllegalStateException("Theme JSON " + themeJson + " not found");
            }
        }
        return sharedTheme;
    }

    private boolean loadTemplateSource(String loadThemeDir, SharedTemplate sharedTemplate) throws IOException {
        String resourcePath = loadThemeDir + sharedTemplate.getContentsFile();
        InputStream stream = servletContext.getResourceAsStream(resourcePath);
        if (stream == null) {
            return false;
        }

        String contents = IOUtils.toString(stream);
        if (contents == null) {
            log.error("Couldn't load template resource [{}]", resourcePath);
            sharedTemplate.setTemplate("");
        } else {
            sharedTemplate.setTemplate(contents);
        }
        return contents != null;
    }
}
