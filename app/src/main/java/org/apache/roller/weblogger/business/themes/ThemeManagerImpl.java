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
package org.apache.roller.weblogger.business.themes;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.FileTypeMap;
import javax.activation.MimetypesFileTypeMap;
import javax.annotation.PostConstruct;
import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogTemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;

/**
 * Base implementation of a ThemeManager.
 * 
 * This particular implementation reads theme data off the filesystem and
 * assumes that those themes are not changeable at runtime.
 */
public class ThemeManagerImpl implements ThemeManager {

	static FileTypeMap map = null;
	static {
		// TODO: figure out why PNG is missing from Java MIME types
		map = FileTypeMap.getDefaultFileTypeMap();
		if (map instanceof MimetypesFileTypeMap) {
			try {
				((MimetypesFileTypeMap) map).addMimeTypes("image/png png PNG");
			} catch (Exception ignored) {
			}
		}
	}

	private static Log log = LogFactory.getLog(ThemeManagerImpl.class);
	private final WeblogManager weblogManager;

	// directory where themes are kept
	private String themeDir = null;
	// the Map contains ... (theme id, Theme)
	private Map<String, SharedTheme> themes = null;

	protected ThemeManagerImpl(WeblogManager wm) {
		this.weblogManager = wm;

		// get theme directory from config and verify it
		this.themeDir = WebloggerConfig.getProperty("themes.dir");
		if (themeDir == null || themeDir.trim().length() < 1) {
			throw new RuntimeException("couldn't get themes directory from config");
		} else {
			// chop off trailing slash if it exists
			if (themeDir.endsWith("/")) {
				themeDir = themeDir.substring(0, themeDir.length() - 1);
			}

			// make sure it exists and is readable
			File themeDirFile = new File(themeDir);
			if (!themeDirFile.exists() || !themeDirFile.isDirectory()
					|| !themeDirFile.canRead()) {
				throw new RuntimeException("couldn't access theme dir [" + themeDir + "]");
			}
		}
	}

	@Override
    @PostConstruct
    public void initialize() throws WebloggerException {

		log.debug("Initializing Theme Manager");

		if (themeDir != null) {
			// rather than be lazy we are going to load all themes from
			// the disk preemptive and cache them
			this.themes = loadAllThemesFromDisk();

			log.info("Successfully loaded " + this.themes.size() + " themes from disk.");
		}
	}

	/**
	 * @see org.apache.roller.weblogger.business.themes.ThemeManager#getTheme(java.lang.String)
	 */
	public SharedTheme getTheme(String id) throws WebloggerException {

		// try to lookup theme from library
		SharedTheme theme = this.themes.get(id);

		// no theme? throw exception.
		if (theme == null) {
			throw new IllegalArgumentException("Couldn't find theme [" + id + "]");
		}

		return theme;
	}

	/**
	 * @see org.apache.roller.weblogger.business.themes.ThemeManager#getTheme(Weblog)
	 */
	public WeblogTheme getTheme(Weblog weblog) throws WebloggerException {

		if (weblog == null) {
			return null;
		}

		WeblogTheme weblogTheme = null;

        SharedTheme staticTheme = this.themes.get(weblog.getEditorTheme());
        if (staticTheme != null) {
            weblogTheme = new WeblogTheme(weblogManager, weblog, staticTheme);
        } else {
            log.warn("Unable to lookup theme " + weblog.getEditorTheme());
        }

		// TODO: if somehow the theme is still null should we provide some
		// kind of fallback option like a default theme?

		return weblogTheme;
	}

	/**
	 * @see org.apache.roller.weblogger.business.themes.ThemeManager#getEnabledThemesList()
	 */
	public List<SharedTheme> getEnabledThemesList() {
		List<SharedTheme> allThemes = new ArrayList<>(this.themes.values());

		// sort 'em ... default ordering for themes is by name
		Collections.sort(allThemes);

		return allThemes;
	}

	/**
	 * @see org.apache.roller.weblogger.business.themes.ThemeManager#importTheme(Weblog,
	 *      SharedTheme, boolean)
	 */
	public void importTheme(Weblog weblog, SharedTheme theme, boolean skipStylesheet)
			throws WebloggerException {

		log.debug("Importing theme [" + theme.getName() + "] to weblog ["
				+ weblog.getName() + "]");

		Set<ComponentType> importedActionTemplates = new HashSet<>();
		ThemeTemplate stylesheetTemplate = theme.getTemplateByAction(ComponentType.STYLESHEET);
		for (ThemeTemplate themeTemplate : theme.getTemplates()) {
			WeblogTemplate template;

			// if template is an action, lookup by action
			if (themeTemplate.getAction() != null
					&& !themeTemplate.getAction().equals(ComponentType.CUSTOM)) {
				importedActionTemplates.add(themeTemplate.getAction());
				template = weblogManager.getTemplateByAction(weblog,
                        themeTemplate.getAction());

				// otherwise, lookup by name
			} else {
				template = weblogManager.getTemplateByName(weblog, themeTemplate.getName());
			}

			// Weblog does not have this template, so create it.
			if (template == null) {
				template = new WeblogTemplate();
				template.setWeblog(weblog);
			}

			// update template attributes except leave existing custom stylesheets as-is
			if (!themeTemplate.equals(stylesheetTemplate) || !skipStylesheet) {
				template.setAction(themeTemplate.getAction());
				template.setName(themeTemplate.getName());
				template.setDescription(themeTemplate.getDescription());
				template.setLink(themeTemplate.getLink());
				template.setHidden(themeTemplate.isHidden());
				template.setNavbar(themeTemplate.isNavbar());
                template.setOutputContentType(themeTemplate.getOutputContentType());
				template.setLastModified(new Date());

				// save it
				weblogManager.saveTemplate(template);

                // create weblog template code objects and save them
                for (RenditionType type : RenditionType.values()) {

                    // See if we already have some code for this template already (eg previous theme)
                    WeblogTemplateRendition weblogTemplateCode = template.getTemplateRendition(type);

                    // Get the template for the new theme
                    TemplateRendition templateCode = themeTemplate.getTemplateRendition(type);
                    if (templateCode != null) {

                        // Check for existing template
                        if (weblogTemplateCode == null) {
                            // Does not exist so create a new one
                            weblogTemplateCode = new WeblogTemplateRendition(template, type);
                        }
                        weblogTemplateCode.setType(type);
                        weblogTemplateCode.setTemplate(templateCode.getTemplate());
                        weblogTemplateCode.setTemplateLanguage(templateCode
                                .getTemplateLanguage());
                        weblogManager.saveTemplateRendition(weblogTemplateCode);
                    }

                }
            }
		}

		// now, see if the weblog has left over non-custom action templates that
		// need to be deleted because they aren't in their new theme
        for (ComponentType action : ComponentType.values()) {
            if (action == ComponentType.CUSTOM) {
                continue;
            }
			// if we didn't import this action then see if it should be deleted
			if (!importedActionTemplates.contains(action)) {
				WeblogTemplate toDelete = weblogManager.getTemplateByAction(weblog, action);
				if (toDelete != null) {
					log.debug("Removing stale action template " + toDelete.getId());
					weblogManager.removeTemplate(toDelete);
				}
			}
		}

		weblogManager.saveWeblog(weblog);
	}

	/**
	 * This is a convenience method which loads all the theme data from themes
	 * stored on the filesystem in the roller webapp /themes/ directory.
	 */
	private Map<String, SharedTheme> loadAllThemesFromDisk() {

		Map<String, SharedTheme> themeMap = new HashMap<>();

		// first, get a list of the themes available
		File themesdir = new File(this.themeDir);
		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				File file = new File(dir.getAbsolutePath() + File.separator + name);
				return file.isDirectory() && !file.getName().startsWith(".");
			}
		};
		String[] themenames = themesdir.list(filter);

		if (themenames == null) {
			log.warn("No themes found!  Perhaps wrong directory for themes specified?  "
					+ "(Check themes.dir setting in roller[-custom].properties file.)");
		} else {
            log.info("Loading themes from " + themesdir.getAbsolutePath() + "...");

            // now go through each theme and load it into a Theme object
            for (String themeName : themenames) {
                try {
                    SharedTheme theme = loadThemeData(themeName);
                    themeMap.put(theme.getId(), theme);
                    log.info("Loaded theme '" + themeName + "'");
                } catch (Exception unexpected) {
                    // shouldn't happen, so let's learn why it did
                    log.error("Unable to process theme '" + themeName + "':", unexpected);
                }
            }
        }

		return themeMap;
	}

    private SharedTheme loadThemeData(String themeName) throws WebloggerException {
        String themePath = this.themeDir + File.separator + themeName;
        log.debug("Parsing theme descriptor for " + themePath);

        //ThemeMetadata themeMetadata2;
        SharedTheme sharedTheme;
        try {
            // lookup theme descriptor and parse it
            SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(new StreamSource(
                    SharedTheme.class.getResourceAsStream("/theme.xsd")));

            InputStream is = new FileInputStream(themePath + File.separator + "theme.xml");
            JAXBContext jaxbContext = JAXBContext.newInstance(SharedTheme.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            jaxbUnmarshaller.setSchema(schema);
            jaxbUnmarshaller.setEventHandler(new ValidationEventHandler() {
                public boolean handleEvent(ValidationEvent event) {
                    log.error("Theme parsing error: " +
                            event.getMessage() + "; Line #" +
                            event.getLocator().getLineNumber() + "; Column #" +
                            event.getLocator().getColumnNumber());
                    return false;
                }
            });
            sharedTheme = (SharedTheme) jaxbUnmarshaller.unmarshal(is);
            sharedTheme.setThemeDir(themePath);
        } catch (Exception ex) {
            throw new WebloggerException(
                    "Unable to parse theme.xml for theme " + themePath, ex);
        }

        log.debug("Loading Theme " + sharedTheme.getName());

        // load resource representing preview image
        File previewFile = new File(sharedTheme.getThemeDir() + File.separator + sharedTheme.getPreviewImagePath());
        if (!previewFile.exists() || !previewFile.canRead()) {
            log.warn("Couldn't read theme [" + sharedTheme.getName()
                    + "] preview image file ["
                    + sharedTheme.getPreviewImagePath() + "]");
        }

        // create the templates based on the theme descriptor data
        boolean hasWeblogTemplate = false;
        for (SharedThemeTemplate template : sharedTheme.getTempTemplates()) {

            // one and only one template with action "weblog" allowed
            if (ComponentType.WEBLOG.equals(template.getAction())) {
                if (hasWeblogTemplate) {
                    throw new WebloggerException("Theme has more than one template with action of 'weblog'");
                } else {
                    hasWeblogTemplate = true;
                }
            }

            // get the template's available renditions
            SharedThemeTemplateRendition standardRendition = template.getRenditionMap().get(RenditionType.STANDARD);

            if (standardRendition == null) {
                throw new WebloggerException("Cannot retrieve required standard rendition for template " + template.getName());
            } else {
                if (!loadRenditionSource(sharedTheme.getThemeDir(), standardRendition)) {
                    throw new WebloggerException("Couldn't load template rendition [" + standardRendition.getContentsFile() + "]");
                }
            }

            template.setId(sharedTheme.getId() + ":" + template.getName());

            // see if a mobile rendition needs adding
            if (sharedTheme.getDualTheme()) {
                SharedThemeTemplateRendition mobileRendition = template.getRenditionMap().get(RenditionType.MOBILE);

                // cloning the standard template code if no mobile is present
                if (mobileRendition == null) {
                    mobileRendition = new SharedThemeTemplateRendition();
                    mobileRendition.setContentsFile(standardRendition.getContentsFile());
                    mobileRendition.setTemplateLanguage(standardRendition.getTemplateLanguage());
                    mobileRendition.setType(RenditionType.MOBILE);
                }

                loadRenditionSource(sharedTheme.getThemeDir(), mobileRendition);
                template.addTemplateRendition(mobileRendition);
            }

            // add it to the theme
            sharedTheme.addTemplate(template);
        }

        if (!hasWeblogTemplate) {
            throw new WebloggerException("Theme " + sharedTheme.getName() + " has no template with 'weblog' action");
        }

        sharedTheme.getTempTemplates().clear();
        return sharedTheme;
    }

    private boolean loadRenditionSource(String themeDir, SharedThemeTemplateRendition rendition) {
        File renditionFile = new File(themeDir + File.separator + rendition.getContentsFile());
        String contents = loadTemplateRendition(renditionFile);
        if (contents == null) {
            log.error("Couldn't load rendition file [" + renditionFile + "]");
            rendition.setTemplate("");
        } else {
            rendition.setTemplate(contents);
        }
        return contents != null;
    }

    /**
     * Load a single template file as a string, returns null if can't read file.
     */
    private String loadTemplateRendition(File templateFile) {
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
            log.error("Exception reading theme template file [" + templateFile + "]");
            if (log.isDebugEnabled()) {
                log.debug(noprob);
            }
            return null;
        }

        return new String(chars, 0, length);
    }

}
