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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.WeblogTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Base implementation of a ThemeManager.
 * 
 * This particular implementation reads theme data off the filesystem 
 * and assumes that those themes are not changable at runtime.
 */
@com.google.inject.Singleton
public class ThemeManagerImpl implements ThemeManager {
    
    private static Log log = LogFactory.getLog(ThemeManagerImpl.class);
    
    private final Weblogger roller;
    
    // directory where themes are kept
    private String themeDir = null;
    
    // the Map contains ... (theme id, Theme)
    private Map themes = null;
    
    
    @com.google.inject.Inject
    protected ThemeManagerImpl(Weblogger roller) {
        
        this.roller = roller;
        
        // get theme directory from config and verify it
        this.themeDir = WebloggerConfig.getProperty("themes.dir");
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
        }
    }
    
    
    public void initialize() throws InitializationException {
        
        log.debug("Initializing Theme Manager");
        
        if(themeDir != null) {
            // rather than be lazy we are going to load all themes from
            // the disk preemptively and cache them
            this.themes = loadAllThemesFromDisk();
            
            log.info("Loaded "+this.themes.size()+" themes from disk.");
        }
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.ThemeManager#getTheme(java.lang.String)
     */
    public SharedTheme getTheme(String id) 
            throws ThemeNotFoundException, WebloggerException {
        
        // try to lookup theme from library
        SharedTheme theme = (SharedTheme) this.themes.get(id);
        
        // no theme?  throw exception.
        if(theme == null) {
            throw new ThemeNotFoundException("Couldn't find theme ["+id+"]");
        }
        
        return theme;
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.ThemeManager#getTheme(weblog)
     */
    public WeblogTheme getTheme(Weblog weblog) throws WebloggerException {
        
        if(weblog == null)
            return null;
        
        WeblogTheme weblogTheme = null;
        
        // if theme is custom or null then return a WeblogCustomTheme
        if(weblog.getEditorTheme() == null || 
                WeblogTheme.CUSTOM.equals(weblog.getEditorTheme())) {
            weblogTheme = new WeblogCustomTheme(weblog);
            
        // otherwise we are returning a WeblogSharedTheme
        } else {
            ThemeManager themeMgr = roller.getThemeManager();
            SharedTheme staticTheme =
                    (SharedTheme) this.themes.get(weblog.getEditorTheme());
            if(staticTheme != null) {
                weblogTheme = new WeblogSharedTheme(weblog, staticTheme);
            } else {
                log.warn("Unable to lookup theme "+weblog.getEditorTheme());
            }
        }
        
        // TODO: if somehow the theme is still null should we provide some
        // kind of fallback option like a default theme?
        
        return weblogTheme;
    }

    
    /**
     * @see org.apache.roller.weblogger.model.ThemeManager#getEnabledThemesList()
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
     * @see org.apache.roller.weblogger.model.ThemeManager#importTheme(website, theme)
     */
    public void importTheme(Weblog website, SharedTheme theme)
            throws WebloggerException {
        
        log.debug("Importing theme ["+theme.getName()+"] to weblog ["+website.getName()+"]");
        
        try {
            WeblogManager wmgr = roller.getWeblogManager();
            
            Set importedActionTemplates = new HashSet();
            ThemeTemplate themeTemplate = null;
            ThemeTemplate stylesheetTemplate = theme.getStylesheet();
            Iterator iter = theme.getTemplates().iterator();
            while ( iter.hasNext() ) {
                themeTemplate = (ThemeTemplate) iter.next();
                
                WeblogTemplate template = null;
                
                // if template is an action, lookup by action
                if(themeTemplate.getAction() != null &&
                        !themeTemplate.getAction().equals(WeblogTemplate.ACTION_CUSTOM)) {
                    template = wmgr.getPageByAction(website, themeTemplate.getAction());
                    if(template != null) {
                        importedActionTemplates.add(themeTemplate.getAction());
                    }
                    
                // otherwise, lookup by name
                } else {
                    template = wmgr.getPageByName(website, themeTemplate.getName());
                }
                
                // Weblog does not have this template, so create it.
                boolean newTmpl = false;
                if (template == null) {
                    template = new WeblogTemplate();
                    template.setWebsite(website);
                    newTmpl = true;
                }

                // TODO: fix conflict situation
                // it's possible that someone has defined a theme template which
                // matches 2 existing templates, 1 by action, the other by name
                
                // update template attributes
                // NOTE: we don't want to copy the template data for an existing stylesheet
                if(newTmpl || !themeTemplate.equals(stylesheetTemplate)) {
                    template.setAction(themeTemplate.getAction());
                    template.setName(themeTemplate.getName());
                    template.setDescription(themeTemplate.getDescription());
                    template.setLink(themeTemplate.getLink());
                    template.setContents(themeTemplate.getContents());
                    template.setHidden(themeTemplate.isHidden());
                    template.setNavbar(themeTemplate.isNavbar());
                    template.setTemplateLanguage(themeTemplate.getTemplateLanguage());
                    // NOTE: decorators are deprecated starting in 4.0
                    template.setDecoratorName(null);
                    template.setLastModified(new Date());
                    
                    // save it
                    wmgr.savePage( template );
                }
            }
            
            // now, see if the weblog has left over action templates that
            // need to be deleted because they aren't in their new theme
            for(int i=0; i < WeblogTemplate.ACTIONS.length; i++) {
                String action = WeblogTemplate.ACTIONS[i];
                
                // if we didn't import this action then see if it should be deleted
                if(!importedActionTemplates.contains(action)) {
                    WeblogTemplate toDelete = wmgr.getPageByAction(website, action);
                    if(toDelete != null) {
                        log.debug("Removing stale action template "+toDelete.getId());
                        wmgr.removePage(toDelete);
                    }
                }
            }
            
            
            // always update this weblog's theme and customStylesheet, then save
            website.setEditorTheme(WeblogTheme.CUSTOM);
            if(theme.getStylesheet() != null) {
                website.setCustomStylesheetPath(theme.getStylesheet().getLink());
            }
            wmgr.saveWeblog(website);
            
            
            // now lets import all the theme resources
            FileManager fileMgr = roller.getFileManager();
            
            List resources = theme.getResources();
            Iterator iterat = resources.iterator();
            ThemeResource resource = null;
            while ( iterat.hasNext() ) {
                resource = (ThemeResource) iterat.next();
                
                log.debug("Importing resource to "+resource.getPath());
                
                try {
                    if(resource.isDirectory()) {
                        fileMgr.createDirectory(website, resource.getPath());
                    } else {
                        // save file without file-type, quota checks, etc.
                        fileMgr.saveFile(website, resource.getPath(), "text/plain", 
                                resource.getLength(), resource.getInputStream(), false);
                    }
                } catch (Exception ex) {
                    log.info(ex);
                }
            }
            
        } catch (Exception e) {
            log.error("ERROR importing theme", e);
            throw new WebloggerException( e );
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
                Theme theme = new SharedThemeFromDir(this.themeDir + File.separator + themenames[i]);
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
    
}
