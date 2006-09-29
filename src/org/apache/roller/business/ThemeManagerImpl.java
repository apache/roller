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

package org.apache.roller.business;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.ThemeNotFoundException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ThemeManager;
import org.apache.roller.model.UserManager;
import org.apache.roller.pojos.Theme;
import org.apache.roller.pojos.ThemeTemplate;
import org.apache.roller.pojos.WeblogTemplate;
import org.apache.roller.pojos.WebsiteData;


/**
 * Base implementation of a ThemeManager.
 * 
 * This particular implementation reads theme data off the filesystem 
 * and assumes that those themes are not changable at runtime.
 */
public class ThemeManagerImpl implements ThemeManager {
    
    private static Log log = LogFactory.getLog(ThemeManagerImpl.class);
    
    // directory where themes are kept
    private String themeDir = null;
    
    // the Map contains ... (theme name, Theme)
    private Map themes = null;
    
    
    protected ThemeManagerImpl() {
        
        log.debug("Initializing ThemeManagerImpl");
        
        // get theme directory from config and verify it
        this.themeDir = RollerConfig.getProperty("themes.dir");
        if(themeDir == null || themeDir.trim().length() < 1) {
            throw new RuntimeException("couldn't get themes directory from config");
        } else {
            // chop off trailing slash if it exists
            if(themeDir.endsWith("/")) {
                themeDir = themeDir.substring(0, themeDir.length()-1);
            }
            
            // make sure it exists and is readable
            File themeDirFile = new File(themeDir);
            if(!themeDirFile.exists() || 
                    !themeDirFile.isDirectory() || 
                    !themeDirFile.canRead()) {
                throw new RuntimeException("couldn't access theme dir ["+themeDir+"]");
            }
            
            // rather than be lazy we are going to load all themes from
            // the disk preemptively during initialization and cache them
            this.themes = loadAllThemesFromDisk();
            
            log.info("Loaded "+this.themes.size()+" themes from disk.");
        }
    }
    
    
    /**
     * @see org.apache.roller.model.ThemeManager#getTheme(java.lang.String)
     */
    public Theme getTheme(String name) 
        throws ThemeNotFoundException, RollerException {
        
        Theme theme = (Theme) this.themes.get(name);
        if(theme == null)
            throw new ThemeNotFoundException("Couldn't find theme ["+name+"]");
        
        return theme;
    }
    
    
    /**
     * @see org.apache.roller.model.ThemeManager#getEnabledThemesList()
     */
    public List getEnabledThemesList() {
        
        Collection all_themes = this.themes.values();
        
        // make a new list of only the enabled themes
        List enabled_themes = new ArrayList();
        Iterator it = all_themes.iterator();
        Theme theme = null;
        while(it.hasNext()) {
            theme = (Theme) it.next();
            if(theme.isEnabled())
                enabled_themes.add(theme.getName());
        }
                
        // sort 'em ... the natural sorting order for Strings is alphabetical
        Collections.sort(enabled_themes);
        
        return enabled_themes;
    }
    
    
    /**
     * This is a convenience method which loads all the theme data from
     * themes stored on the filesystem in the roller webapp /themes/ directory.
     */
    private Map loadAllThemesFromDisk() {
        
        Map themes = new HashMap();
        
        // NOTE: we need to figure out how to get the roller context path
        String themespath = RollerConfig.getProperty("context.realPath");
        if(themespath.endsWith(File.separator))
            themespath += "themes";
        else
            themespath += File.separator + "themes";
        
        // first, get a list of the themes available
        File themesdir = new File(themespath);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file =
                        new File(dir.getAbsolutePath() + File.separator + name);
                return file.isDirectory();
            }
        };
        String[] themenames = themesdir.list(filter);
        
        if(themenames == null)
            themenames = new String[0];
        
        // now go through each theme and read all it's templates
        Theme theme = null;
        for(int i=0; i < themenames.length; i++) {
            try {
                theme = this.loadThemeFromDisk(themenames[i], 
                            themespath + File.separator + themenames[i]);            
                themes.put(theme.getName(), theme);
            } catch (Throwable unexpected) {
                // shouldn't happen, so let's learn why it did
                log.error("Problem reading theme " + themenames[i], unexpected);
            }
        }
        
        return themes;
    }
    
    
    /**
     * Another convenience method which knows how to load a single theme
     * off the filesystem and return a Theme object
     */
    private Theme loadThemeFromDisk(String theme_name, String themepath) {
        
        log.info("Loading theme "+theme_name);  
        
        Theme theme = new Theme();
        theme.setName(theme_name);
        theme.setAuthor("Roller");
        theme.setLastEditor("Roller");
        theme.setEnabled(true);
        
        // start by getting a list of the .vm files for this theme
        File themedir = new File(themepath);
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".vm");
            }
        };
        String[] templates = themedir.list(filter);
        
        // go through each .vm file and read in its contents to a ThemeTemplate
        String template_name = null;
        ThemeTemplate theme_template = null;
        for (int i=0; i < templates.length; i++) {
            // strip off the .vm part
            template_name = templates[i].substring(0, templates[i].length() - 3);            
            File template_file = new File(themepath + File.separator + templates[i]);
            
            // Continue reading theme even if problem encountered with one file
            String msg = "read theme template file ["+template_file+"]";
            if(!template_file.exists() && !template_file.canRead()) {
                log.error("Couldn't " + msg);
                continue;
            }
            char[] chars = null;
            int length;
            try {
//                FileReader reader = new FileReader(template_file);
                chars = new char[(int) template_file.length()];
            	FileInputStream stream = new FileInputStream(template_file);
            	InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
                length = reader.read(chars);            
            } catch (Exception noprob) {
                log.error("Exception while attempting to " + msg);
                if (log.isDebugEnabled()) log.debug(noprob);
                continue;
            }
            
            // Strip "_" from name to form link
            boolean navbar = true;
            String template_link = template_name;
            if (template_name.startsWith("_") && template_name.length() > 1) {
                navbar = false;
                template_link = template_link.substring(1);
                log.debug("--- " + template_link);
            }
            
            String decorator = "_decorator";
            if("_decorator".equals(template_name)) {
                decorator = null;
            }
            
            // construct ThemeTemplate representing this file
            // a few restrictions for now:
            //   - we only allow "velocity" for the template language
            //   - decorator is always "_decorator" or null
            //   - all theme templates are considered not hidden
            theme_template = new ThemeTemplate(
                    theme,
                    theme_name+":"+template_name,
                    template_name,
                    template_name,
                    new String(chars, 0, length),
                    template_link,
                    new Date(template_file.lastModified()),
                    "velocity",
                    false,
                    navbar,
                    decorator);

            // add it to the theme
            theme.setTemplate(template_name, theme_template);
        }
        
        // use the last mod date of the theme dir
        theme.setLastModified(new Date(themedir.lastModified()));
        
        // load up resources as well
        loadThemeResources(theme, themedir, themedir.getAbsolutePath());
        
        return theme;
    }
    
    
    /**
     * Convenience method for loading a theme's resource files.
     * This method works recursively to load from subdirectories.
     */
    private void loadThemeResources(Theme theme, File workPath, String basePath) {
        
        // now go through all static resources for this theme
        FilenameFilter resourceFilter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return !name.endsWith(".vm");
            }
        };
        File[] resources = workPath.listFiles(resourceFilter);
        
        // go through each resource file and add it to the theme
        String resourcePath = null;
        File resourceFile = null;
        for (int i=0; i < resources.length; i++) {
            resourceFile = resources[i];
            resourcePath = resourceFile.getAbsolutePath().substring(basePath.length()+1);
            
            log.debug("handling resource ["+resourcePath+"]");
            
            // Continue reading theme even if problem encountered with one file
            if(!resourceFile.exists() || !resourceFile.canRead()) {
                log.warn("Couldn't read theme resource file ["+resourcePath+"]");
                continue;
            }
            
            // if its a directory, recurse
            if(resourceFile.isDirectory()) {
                log.debug("resource is a directory, recursing");
                loadThemeResources(theme, resourceFile, basePath);
            }
            
            // otherwise just add the File to the theme
            theme.setResource(resourcePath, resourceFile);
        }
    }
    
    
    /**
     * Helper method that copies down the pages from a given theme into a
     * users weblog templates.
     *
     * @param rreq Request wrapper.
     * @param theme Name of theme to save.
     * @throws RollerException
     */
    public void saveThemePages(WebsiteData website, Theme theme)
        throws RollerException {
        
        log.debug("Setting custom templates for website: "+website.getName());
        
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            
            Collection templates = theme.getTemplates();
            Iterator iter = templates.iterator();
            ThemeTemplate theme_template = null;
            while ( iter.hasNext() ) {
                theme_template = (ThemeTemplate) iter.next();
                
                WeblogTemplate template = null;
                
                if(theme_template.getName().equals(WeblogTemplate.DEFAULT_PAGE)) {
                    // this is the main Weblog template
                    try {
                        template = userMgr.getPage(website.getDefaultPageId());
                    } catch(Exception e) {
                        // user may not have a default page yet
                    }
                } else {
                    // any other template
                    template = userMgr.getPageByName(website, theme_template.getName());
                }
                
                
                if (template != null) {
                    // User already has page by that name, so overwrite it.
                    template.setContents(theme_template.getContents());
                    template.setLink(theme_template.getLink());
                    
                } else {
                    // User does not have page by that name, so create new page.
                    template = new WeblogTemplate(
                            null,                               // id
                            website,                            // website
                            theme_template.getName(),           // name
                            theme_template.getDescription(),    // description
                            theme_template.getLink(),           // link
                            theme_template.getContents(),       // contents
                            new Date(),                         // last mod
                            theme_template.getTemplateLanguage(), // temp lang
                            theme_template.isHidden(),          // hidden
                            theme_template.isNavbar(),          // navbar
                            theme_template.getDecoratorName()   // decorator
                            );
                    userMgr.savePage( template );
                }
            }
            
            // now update this website's theme to custom
            website.setEditorTheme(Theme.CUSTOM);
            
            // if this is the first time someone is customizing a theme then
            // we need to set a default page
            if(website.getDefaultPageId() == null ||
                    website.getDefaultPageId().trim().equals("") ||
                    website.getDefaultPageId().equals("dummy")) {
                // we have to go back to the db to figure out the id
                WeblogTemplate template = userMgr.getPageByName(website, "Weblog");
                if(template != null) {
                    log.debug("Setting default page to "+template.getId());
                    website.setDefaultPageId(template.getId());
                }
            }
            
            // we also want to set the weblogdayid
            WeblogTemplate dayTemplate = userMgr.getPageByName(website, "_day");
            if(dayTemplate != null) {
                log.debug("Setting default day page to "+dayTemplate.getId());
                website.setWeblogDayPageId(dayTemplate.getId());
            }
            
            // save our updated website
            userMgr.saveWebsite(website);
            
        } catch (Exception e) {
            log.error("ERROR in action",e);
            throw new RollerException( e );
        }       
    }
}
