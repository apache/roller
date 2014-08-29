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
 */
package org.apache.roller.weblogger.business.themes;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.CustomTemplateRendition;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Base implementation of a ThemeManager.
 * 
 * This particular implementation reads theme data off the filesystem and
 * assumes that those themes are not changeable at runtime.
 */
@com.google.inject.Singleton
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
	private final Weblogger roller;
	// directory where themes are kept
	private String themeDir = null;
	// the Map contains ... (theme id, Theme)
	private Map<String, SharedTheme> themes = null;

	@com.google.inject.Inject
	protected ThemeManagerImpl(Weblogger roller) {

		this.roller = roller;

		// get theme directory from config and verify it
		this.themeDir = WebloggerConfig.getProperty("themes.dir");
		if (themeDir == null || themeDir.trim().length() < 1) {
			throw new RuntimeException(
					"couldn't get themes directory from config");
		} else {
			// chop off trailing slash if it exists
			if (themeDir.endsWith("/")) {
				themeDir = themeDir.substring(0, themeDir.length() - 1);
			}

			// make sure it exists and is readable
			File themeDirFile = new File(themeDir);
			if (!themeDirFile.exists() || !themeDirFile.isDirectory()
					|| !themeDirFile.canRead()) {
				throw new RuntimeException("couldn't access theme dir ["
						+ themeDir + "]");
			}
		}
	}

	public void initialize() throws InitializationException {

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
		SharedTheme theme = (SharedTheme) this.themes.get(id);

		// no theme? throw exception.
		if (theme == null) {
			throw new ThemeNotFoundException("Couldn't find theme [" + id + "]");
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

		// if theme is custom or null then return a WeblogCustomTheme
		if (weblog.getEditorTheme() == null
				|| WeblogTheme.CUSTOM.equals(weblog.getEditorTheme())) {
			weblogTheme = new WeblogCustomTheme(weblog);

			// otherwise we are returning a WeblogSharedTheme
		} else {
			SharedTheme staticTheme = (SharedTheme) this.themes.get(weblog
					.getEditorTheme());
			if (staticTheme != null) {
				weblogTheme = new WeblogSharedTheme(weblog, staticTheme);
			} else {
				log.warn("Unable to lookup theme " + weblog.getEditorTheme());
			}
		}

		// TODO: if somehow the theme is still null should we provide some
		// kind of fallback option like a default theme?

		return weblogTheme;
	}

	/**
	 * @see org.apache.roller.weblogger.business.themes.ThemeManager#getEnabledThemesList()
	 */
	public List<SharedTheme> getEnabledThemesList() {
		List<SharedTheme> allThemes = new ArrayList<SharedTheme>(this.themes.values());

		// sort 'em ... default ordering for themes is by name
		Collections.sort(allThemes);

		return allThemes;
	}

	/**
	 * @see org.apache.roller.weblogger.business.themes.ThemeManager#importTheme(Weblog,
	 *      SharedTheme)
	 */
	public void importTheme(Weblog weblog, SharedTheme theme)
			throws WebloggerException {

		log.debug("Importing theme [" + theme.getName() + "] to weblog ["
				+ weblog.getName() + "]");

		WeblogManager wmgr = roller.getWeblogManager();
		MediaFileManager fileMgr = roller.getMediaFileManager();

		MediaFileDirectory root = fileMgr.getDefaultMediaFileDirectory(weblog);
        if (root == null) {
            log.warn("Weblog " + weblog.getHandle()
                    + " does not have a root MediaFile directory");
        }

		Set<ComponentType> importedActionTemplates = new HashSet<ComponentType>();
		ThemeTemplate stylesheetTemplate = theme.getStylesheet();
		for (ThemeTemplate themeTemplate : theme.getTemplates()) {
			WeblogTemplate template;

			// if template is an action, lookup by action
			if (themeTemplate.getAction() != null
					&& !themeTemplate.getAction().equals(ComponentType.CUSTOM)) {
				importedActionTemplates.add(themeTemplate.getAction());
				template = wmgr.getTemplateByAction(weblog,
                        themeTemplate.getAction());

				// otherwise, lookup by name
			} else {
				template = wmgr.getTemplateByName(weblog, themeTemplate.getName());
			}

			// Weblog does not have this template, so create it.
			boolean newTmpl = false;
			if (template == null) {
				template = new WeblogTemplate();
				template.setWeblog(weblog);
				newTmpl = true;
			}

			// TODO: fix conflict situation
			// it's possible that someone has defined a theme template which
			// matches 2 existing templates, 1 by action, the other by name

			// update template attributes
			// NOTE: we don't want to copy the template data for an existing
			// stylesheet
			if (newTmpl || !themeTemplate.equals(stylesheetTemplate)) {
				template.setAction(themeTemplate.getAction());
				template.setName(themeTemplate.getName());
				template.setDescription(themeTemplate.getDescription());
				template.setLink(themeTemplate.getLink());
				template.setHidden(themeTemplate.isHidden());
				template.setNavbar(themeTemplate.isNavbar());
                template.setOutputContentType(themeTemplate.getOutputContentType());
				template.setLastModified(new Date());

				// save it
				wmgr.saveTemplate(template);
			}

			// create weblog template code objects and save them
			for (RenditionType type : RenditionType.values()) {

				// See if we already have some code for this template already (eg previous theme)
				CustomTemplateRendition weblogTemplateCode = template.getTemplateRendition(type);

				// Get the template for the new theme
				TemplateRendition templateCode = themeTemplate.getTemplateRendition(type);
				if (templateCode != null) {
					
					// Check for existing template
					if (weblogTemplateCode == null) {
						// Does not exist so create a new one
						weblogTemplateCode = new CustomTemplateRendition(
								template, type);
					}
					weblogTemplateCode.setType(type);
					weblogTemplateCode.setTemplate(templateCode.getTemplate());
					weblogTemplateCode.setTemplateLanguage(templateCode
                            .getTemplateLanguage());
					WebloggerFactory.getWeblogger().getWeblogManager()
							.saveTemplateRendition(weblogTemplateCode);
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
				WeblogTemplate toDelete = wmgr.getTemplateByAction(weblog, action);
				if (toDelete != null) {
					log.debug("Removing stale action template "
							+ toDelete.getId());
					wmgr.removeTemplate(toDelete);
				}
			}
		}

		// set weblog's theme to custom, then save
		weblog.setEditorTheme(WeblogTheme.CUSTOM);
		wmgr.saveWeblog(weblog);

		// now lets import all the theme resources
        for (ThemeResource resource : theme.getResources()) {

			log.debug("Importing resource " + resource.getPath());

			if (resource.isDirectory()) {
				MediaFileDirectory mdir = fileMgr.getMediaFileDirectoryByName(
						weblog, resource.getPath());
				if (mdir == null) {
					log.debug("    Creating directory: " + resource.getPath());
					fileMgr.createMediaFileDirectory(weblog, resource.getPath());
					roller.flush();
				} else {
					log.debug("    No action: directory already exists");
				}

			} else {
				String resourcePath = resource.getPath();

				MediaFileDirectory mdir;
				String justName;
				String justPath;

				if (resourcePath.indexOf('/') == -1) {
					mdir = fileMgr.getDefaultMediaFileDirectory(weblog);
					justPath = "";
					justName = resourcePath;

				} else {
					justPath = resourcePath.substring(0,
							resourcePath.lastIndexOf('/'));
					if (!justPath.startsWith("/")) {
                        justPath = "/" + justPath;
                    }
					justName = resourcePath.substring(resourcePath
							.lastIndexOf('/') + 1);
					mdir = fileMgr.getMediaFileDirectoryByName(weblog,
							justPath);
					if (mdir == null) {
						log.debug("    Creating directory: " + justPath);
						mdir = fileMgr.createMediaFileDirectory(weblog,
								justPath);
						roller.flush();
					}
				}

				MediaFile oldmf = fileMgr.getMediaFileByOriginalPath(weblog,
						justPath + "/" + justName);
				if (oldmf != null) {
					fileMgr.removeMediaFile(weblog, oldmf);
				}

				// save file without file-type, quota checks, etc.
				InputStream is = resource.getInputStream();
				MediaFile mf = new MediaFile();
				mf.setDirectory(mdir);
				mf.setWeblog(weblog);
				mf.setName(justName);
				mf.setOriginalPath(justPath + "/" + justName);
				mf.setContentType(map.getContentType(justName));
				mf.setInputStream(is);
				mf.setLength(resource.getLength());

				log.debug("    Saving file: " + justName);
				log.debug("    Saving in directory = " + mf.getDirectory());
				RollerMessages errors = new RollerMessages();
				fileMgr.createMediaFile(weblog, mf, errors);
				try {
					resource.getInputStream().close();
				} catch (IOException ex) {
					errors.addError("error.closingStream");
					log.debug("ERROR closing inputstream");
				}
				if (errors.getErrorCount() > 0) {
					throw new WebloggerException(errors.toString());
				}
				roller.flush();
			}
		}
	}

	/**
	 * This is a convenience method which loads all the theme data from themes
	 * stored on the filesystem in the roller webapp /themes/ directory.
	 */
	private Map<String, SharedTheme> loadAllThemesFromDisk() {

		Map<String, SharedTheme> themeMap = new HashMap<String, SharedTheme>();

		// first, get a list of the themes available
		File themesdir = new File(this.themeDir);
		FilenameFilter filter = new FilenameFilter() {

			public boolean accept(File dir, String name) {
				File file = new File(dir.getAbsolutePath() + File.separator
						+ name);
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
                    SharedTheme theme = new SharedThemeFromDir(this.themeDir
                            + File.separator + themeName);
                    themeMap.put(theme.getId(), theme);
                    log.info("Loaded theme '" + themeName + "'");
                } catch (Exception unexpected) {
                    // shouldn't happen, so let's learn why it did
                    log.error("Problem processing theme '" + themeName + "':", unexpected);
                }
            }
        }

		return themeMap;
	}

	/**
	 * @see ThemeManager#reLoadThemeFromDisk(String)
	 */
	public boolean reLoadThemeFromDisk(String reloadTheme) {

		boolean reloaded = false;

		try {

            SharedTheme theme = new SharedThemeFromDir(this.themeDir + File.separator
					+ reloadTheme);

            Theme loadedTheme = themes.get(theme.getId());

            if (loadedTheme != null
                    && theme.getLastModified().after(
                            loadedTheme.getLastModified())) {
                themes.remove(theme.getId());
                themes.put(theme.getId(), theme);
                reloaded = true;
            }

		} catch (Exception unexpected) {
			// shouldn't happen, so let's learn why it did
			log.error("Problem reloading theme " + reloadTheme, unexpected);
		}

		return reloaded;

	}
}
