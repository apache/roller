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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    // directory where themes are kept, override required when running tests (no servlet then)
    @Value("${themes.dir:#{null}}")
    private String themeDir;

    // map of themes in format (theme id, Theme)
    private Map<String, SharedTheme> themeMap;

    // list of themes
    private List<SharedTheme> themeList;

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public ThemeManagerImpl() {
    }

    @Override
    @PostConstruct
    public void initialize() {
        log.info("Initializing Theme Manager");
        if (themeDir == null) {
            // default theme location
            themeDir = servletContext.getRealPath("/blogthemes");
        }

        if (!StringUtils.isEmpty(themeDir)) {
            // chop off trailing slash if it exists
            if (themeDir.endsWith("/")) {
                themeDir = themeDir.substring(0, themeDir.length() - 1);
            }

            // make sure it exists and is readable
            File themeDirFile = new File(themeDir);
            if (!themeDirFile.exists() || !themeDirFile.isDirectory() || !themeDirFile.canRead()) {
                throw new RuntimeException("couldn't access theme dir [" + themeDir + "]");
            }
        }

        if (themeDir != null) {
            // load all themes from disk and cache them
            themeMap = loadAllThemesFromDisk();

            // for convenience create an alphabetized list also
            themeList = new ArrayList<>(this.themeMap.values());
            themeList.sort(Comparator.comparing(SharedTheme::getName));
            log.info("Successfully loaded {} themes from disk.", this.themeMap.size());
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

        // TODO: if theme is not found should we provide default theme?
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

    /**
     * This is a convenience method which loads all the theme data from themes
     * stored on the filesystem in the webapp/blogthemes/ directory.
     */
    private Map<String, SharedTheme> loadAllThemesFromDisk() {

        Map<String, SharedTheme> sharedThemeMap = new HashMap<>();

        // first, get a list of the themes available
        File themesdir = new File(this.themeDir);
        FilenameFilter filter = (dir, name) -> {
            File file = new File(dir.getAbsolutePath() + File.separator + name);
            return file.isDirectory() && !file.getName().startsWith(".");
        };
        String[] themenames = themesdir.list(filter);

        if (themenames == null) {
            log.warn("No shared weblog themes found! Looking in location: " + themeDir);
        } else {
            log.info("Loading themes from " + themesdir.getAbsolutePath() + "...");

            // now go through each theme and load it into a Theme object
            for (String themeName : themenames) {
                try {
                    SharedTheme theme = loadThemeData(themeName);
                    sharedThemeMap.put(theme.getId(), theme);
                    log.info("Loaded theme '{}'", themeName);
                } catch (Exception unexpected) {
                    // shouldn't happen, so let's learn why it did
                    log.error("Unable to process theme '{}'", themeName, unexpected);
                    System.out.println("Unable to process theme " + themeName + " error: " + unexpected.getMessage());
                    unexpected.printStackTrace(System.out);
                }
            }
        }

        return sharedThemeMap;
    }

    private SharedTheme loadThemeData(String themeName) throws Exception {
        String themePath = this.themeDir + File.separator + themeName;
        log.debug("Parsing theme descriptor for {}", themePath);

        String themeJson = themePath + File.separator + "theme.json";
        log.debug("Loading Theme JSON at path {}", themeJson);

        SharedTheme sharedTheme = objectMapper.readValue(new FileInputStream(themeJson), SharedTheme.class);
        sharedTheme.setThemeDir(themePath);

        // load resource representing preview image
        File previewFile = new File(sharedTheme.getThemeDir() + File.separator + sharedTheme.getPreviewImagePath());
        if (!previewFile.exists() || !previewFile.canRead()) {
            log.warn("Couldn't read theme [{}] thumbnail at path [{}]", sharedTheme.getName(),
                    sharedTheme.getPreviewImagePath());
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

        return sharedTheme;
    }

    private boolean loadTemplateSource(String loadThemeDir, SharedTemplate sharedTemplate) {
        File templateFile = new File(loadThemeDir + File.separator + sharedTemplate.getContentsFile());
        String contents = loadTemplateSource(templateFile);
        if (contents == null) {
            log.error("Couldn't load template file [{}]", templateFile);
            sharedTemplate.setTemplate("");
        } else {
            sharedTemplate.setTemplate(contents);
        }
        return contents != null;
    }

    /**
     * Load a single template file as a string, returns null if can't read file.
     */
    private String loadTemplateSource(File templateFile) {
        // Continue reading theme even if problem encountered with one file
        if (!templateFile.exists() && !templateFile.canRead()) {
            return null;
        }

        char[] chars;
        int length;
        try {
            chars = new char[(int) templateFile.length()];
            FileInputStream stream = new FileInputStream(templateFile);
            InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
            length = reader.read(chars);
        } catch (Exception noprob) {
            log.error("Exception reading theme template file [{}]", templateFile, noprob);
            return null;
        }

        return new String(chars, 0, length);
    }

}
