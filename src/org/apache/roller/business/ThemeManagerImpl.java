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
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.themes.ThemeMetadataTemplate;
import org.apache.roller.business.themes.ThemeMetadata;
import org.apache.roller.business.themes.ThemeMetadataParser;
import org.apache.roller.config.RollerConfig;
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
    
    // the Map contains ... (theme id, Theme)
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
    public Theme getTheme(String id) 
            throws ThemeNotFoundException, RollerException {
        
        // try to lookup theme from library
        Theme theme = (Theme) this.themes.get(id);
        
        // no theme?  throw exception.
        if(theme == null) {
            throw new ThemeNotFoundException("Couldn't find theme ["+id+"]");
        }
        
        return theme;
    }
    
    
    /**
     * @see org.apache.roller.model.ThemeManager#getEnabledThemesList()
     *
     * TODO: reimplement enabled vs. disabled logic once we support it
     */
    public List getEnabledThemesList() {
        
        List all_themes = new ArrayList(this.themes.values());
                
        // sort 'em ... default ordering for themes is by name
        Collections.sort(all_themes);
        
        return all_themes;
    }
    
    
    /**
     * @see org.apache.roller.model.ThemeManager#importTheme(website, theme)
     */
    public void importTheme(WebsiteData website, Theme theme)
            throws RollerException {
        
        log.debug("Importing theme "+theme.getName()+" to weblog "+website.getName());
        
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            
            Iterator iter = theme.getTemplates().iterator();
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
                    
                    // we just created and saved the default page for the first
                    // time so we need to set website.defaultpageid
                    if(theme_template.getName().equals(WeblogTemplate.DEFAULT_PAGE)) {
                        website.setDefaultPageId(template.getId());
                    }
                }
            }
            
            // always update this weblog's theme to custom and then save
            website.setEditorTheme(Theme.CUSTOM);
            userMgr.saveWebsite(website);
            
            
            // now lets import all the theme resources
            FileManager fileMgr = RollerFactory.getRoller().getFileManager();
            
            List resources = theme.getResources();
            Iterator iterat = resources.iterator();
            File resourceFile = null;
            while ( iterat.hasNext() ) {
                resourceFile = (File) iterat.next();
                
                String path = resourceFile.getAbsolutePath().substring(
                        this.themeDir.length()+theme.getId().length()+1);
                
                // make sure path isn't prefixed with a /
                if(path.startsWith("/")) {
                    path = path.substring(1);
                }
                
                log.debug("Importing resource "+resourceFile.getAbsolutePath()+" to "+path);
                
                try {
                    if(resourceFile.isDirectory()) {
                        fileMgr.createDirectory(website, path);
                    } else {
                        fileMgr.saveFile(website, path, "text/plain", 
                                resourceFile.length(), new FileInputStream(resourceFile));
                    }
                } catch (Exception ex) {
                    log.info(ex);
                }
            }
            
        } catch (Exception e) {
            log.error("ERROR importing theme", e);
            throw new RollerException( e );
        }
    }
    
    
    /**
     * This is a convenience method which loads all the theme data from
     * themes stored on the filesystem in the roller webapp /themes/ directory.
     */
    private Map loadAllThemesFromDisk() {
        
        Map themes = new HashMap();
        
        // first, get a list of the themes available
        File themesdir = new File(this.themeDir);
        FilenameFilter filter = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                File file =
                        new File(dir.getAbsolutePath() + File.separator + name);
                return file.isDirectory();
            }
        };
        String[] themenames = themesdir.list(filter);
        
        if(themenames == null) {
            log.warn("No themes loaded!  Perhaps you specified the wrong "+
                    "location for your themes directory?");
        }
        
        // now go through each theme and load it into a Theme object
        for(int i=0; i < themenames.length; i++) {
            try {
                Theme theme = loadThemeFromDisk(this.themeDir + File.separator + themenames[i]);
                if(theme != null) {
                    themes.put(theme.getId(), theme);
                }
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
    private Theme loadThemeFromDisk(String themepath) {
        
        log.debug("Parsing theme descriptor for "+themepath);
        
        ThemeMetadata themeMetadata = null;
        try {
            // lookup theme descriptor and parse it
            ThemeMetadataParser parser = new ThemeMetadataParser();
            InputStream is = new FileInputStream(themepath + File.separator + "theme.xml");
            themeMetadata = parser.unmarshall(is);
        } catch (Exception ex) {
            log.warn("Unable to parse theme descriptor for theme "+themepath, ex);
            return null;
        }
        
        log.debug("Loading Theme "+themeMetadata.getName());
        
        // use parsed theme descriptor to load Theme object
        Theme theme = new Theme();
        theme.setId(themeMetadata.getId());
        theme.setName(themeMetadata.getName());
        theme.setDescription(themeMetadata.getName());
        theme.setAuthor(themeMetadata.getAuthor());
        theme.setLastModified(new Date());
        theme.setEnabled(true);
        
        // go through static resources and add them to the theme
        String resourcePath = null;
        Iterator resourcesIter = themeMetadata.getResources().iterator();
        while (resourcesIter.hasNext()) {
            resourcePath = (String) resourcesIter.next();
            
            // construct File object from resource
            File resourceFile = new File(themepath + File.separator + resourcePath);
            
            // Continue reading theme even if problem encountered with one file
            if(!resourceFile.exists() || !resourceFile.canRead()) {
                log.warn("Couldn't read theme resource file ["+resourcePath+"]");
                continue;
            }
            
            // add it to the theme
            theme.setResource(resourcePath, resourceFile);
        }
        
        // go through templates and read in contents to a ThemeTemplate
        ThemeTemplate theme_template = null;
        ThemeMetadataTemplate templateMetadata = null;
        Iterator templatesIter = themeMetadata.getTemplates().iterator();
        while (templatesIter.hasNext()) {
            templateMetadata = (ThemeMetadataTemplate) templatesIter.next();
            
            // construct File object from path
            File templateFile = new File(themepath + File.separator + 
                    templateMetadata.getContentsFile());
            
            // Continue reading theme even if problem encountered with one file
            if(!templateFile.exists() && !templateFile.canRead()) {
                log.error("Couldn't read theme template file ["+templateFile+"]");
                continue;
            }
            
            char[] chars = null;
            int length;
            try {
                chars = new char[(int) templateFile.length()];
            	FileInputStream stream = new FileInputStream(templateFile);
            	InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
                length = reader.read(chars);            
            } catch (Exception noprob) {
                log.error("Exception reading template file ["+templateFile+"]");
                if (log.isDebugEnabled()) 
                    log.debug(noprob);
                continue;
            }
            
            String decorator = "_decorator";
            if("_decorator".equals(templateMetadata.getName())) {
                decorator = null;
            }
            
            // construct ThemeTemplate representing this file
            // a few restrictions for now:
            //   - decorator is always "_decorator" or null
            theme_template = new ThemeTemplate(
                    theme,
                    themeMetadata.getId()+":"+templateMetadata.getName(),
                    templateMetadata.getName(),
                    templateMetadata.getDescription(),
                    new String(chars, 0, length),
                    templateMetadata.getLink(),
                    new Date(templateFile.lastModified()),
                    templateMetadata.getTemplateLanguage(),
                    templateMetadata.isHidden(),
                    templateMetadata.isNavbar(),
                    decorator);

            // add it to the theme
            theme.addTemplate(theme_template);
        }
        
        return theme;
    }
    
}
