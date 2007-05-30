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
import java.io.FileInputStream;
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
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.config.RollerConfig;
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
public class ThemeManagerImpl implements ThemeManager {
    
    private static Log log = LogFactory.getLog(ThemeManagerImpl.class);
    
    // directory where themes are kept
    private String themeDir = null;
    
    // the Map contains ... (theme id, Theme)
    private Map themes = null;
    
    
    public ThemeManagerImpl() {
        
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
     * @see org.apache.roller.weblogger.model.ThemeManager#getTheme(java.lang.String)
     */
    public SharedTheme getTheme(String id) 
            throws ThemeNotFoundException, RollerException {
        
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
    public WeblogTheme getTheme(Weblog weblog) throws RollerException {
        
        if(weblog == null)
            return null;
        
        WeblogTheme weblogTheme = null;
        
        // if theme is custom or null then return a WeblogCustomTheme
        if(weblog.getEditorTheme() == null || 
                WeblogTheme.CUSTOM.equals(weblog.getEditorTheme())) {
            weblogTheme = new WeblogCustomTheme(weblog);
            
        // otherwise we are returning a WeblogSharedTheme
        } else {
            ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
            SharedTheme staticTheme =
                    (SharedTheme) this.themes.get(weblog.getEditorTheme());
            if(staticTheme != null) {
                weblogTheme = new WeblogSharedTheme(weblog, staticTheme);
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
            throws RollerException {
        
        log.debug("Importing theme ["+theme.getName()+"] to weblog ["+website.getName()+"]");
        
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            
            Set importedActionTemplates = new HashSet();
            ThemeTemplate themeTemplate = null;
            Iterator iter = theme.getTemplates().iterator();
            while ( iter.hasNext() ) {
                themeTemplate = (ThemeTemplate) iter.next();
                
                WeblogTemplate template = null;
                
                // if template is an action, lookup by action
                if(themeTemplate.getAction() != null &&
                        !themeTemplate.getAction().equals(WeblogTemplate.ACTION_CUSTOM)) {
                    template = userMgr.getPageByAction(website, themeTemplate.getAction());
                    if(template != null) {
                        importedActionTemplates.add(themeTemplate.getAction());
                    }
                    
                // otherwise, lookup by name
                } else {
                    template = userMgr.getPageByName(website, themeTemplate.getName());
                }
                
                // Weblog does not have this template, so create it.
                if (template == null) {
                    template = new WeblogTemplate();
                    template.setWebsite(website);
                }

                // TODO: fix conflict situation
                // it's possible that someone has defined a theme template which
                // matches 2 existing templates, 1 by action, the other by name
                
                // update template attributes
                template.setAction(themeTemplate.getAction());
                template.setName(themeTemplate.getName());
                template.setDescription(themeTemplate.getDescription());
                template.setLink(themeTemplate.getLink());
                template.setContents(themeTemplate.getContents());
                template.setHidden(themeTemplate.isHidden());
                template.setNavbar(themeTemplate.isNavbar());
                template.setTemplateLanguage(themeTemplate.getTemplateLanguage());
                template.setDecoratorName(themeTemplate.getDecoratorName());
                template.setLastModified(new Date());
                
                // save it
                userMgr.savePage( template );
            }
            
            // now, see if the weblog has left over action templates that
            // need to be deleted because they aren't in their new theme
            for(int i=0; i < WeblogTemplate.ACTIONS.length; i++) {
                String action = WeblogTemplate.ACTIONS[i];
                
                // if we didn't import this action then see if it should be deleted
                if(!importedActionTemplates.contains(action)) {
                    WeblogTemplate toDelete = userMgr.getPageByAction(website, action);
                    if(toDelete != null) {
                        log.debug("Removing stale action template "+toDelete.getId());
                        userMgr.removePage(toDelete);
                    }
                }
            }
            
            
            // always update this weblog's theme and customStylesheet, then save
            website.setEditorTheme(WeblogTheme.CUSTOM);
            website.setCustomStylesheetPath(theme.getCustomStylesheet());
            userMgr.saveWebsite(website);
            
            
            // now lets import all the theme resources
            FileManager fileMgr = RollerFactory.getRoller().getFileManager();
            
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
                        fileMgr.saveFile(website, resource.getPath(), "text/plain", 
                                resource.getLength(), resource.getInputStream());
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
