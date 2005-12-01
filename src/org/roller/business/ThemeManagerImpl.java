/*
 * ThemeManagerImpl.java
 *
 * Created on June 27, 2005, 1:33 PM
 */

package org.roller.business;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
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
import org.roller.RollerException;
import org.roller.ThemeNotFoundException;
import org.roller.config.RollerConfig;
import org.roller.model.ThemeManager;
import org.roller.pojos.Theme;
import org.roller.pojos.ThemeTemplate;


/**
 * Base implementation of a ThemeManager.
 * 
 * This particular implementation reads theme data off the filesystem 
 * and assumes that those themes are not changable at runtime.
 *
 * @author Allen Gilliland
 */
public class ThemeManagerImpl implements ThemeManager {
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(ThemeManagerImpl.class);
    
    private Map themes;
    
    
    protected ThemeManagerImpl() {
        
        // rather than be lazy we are going to load all themes from
        // the disk preemptively during initialization and cache them
        mLogger.debug("Initializing ThemeManagerImpl");
        
        this.themes = this.loadAllThemesFromDisk();
        mLogger.info("Loaded "+this.themes.size()+" themes from disk.");
    }
    
    
    /**
     * @see org.roller.model.ThemeManager#getTheme(java.lang.String)
     */
    public Theme getTheme(String name) 
        throws ThemeNotFoundException, RollerException {
        
        Theme theme = (Theme) this.themes.get(name);
        if(theme == null)
            throw new ThemeNotFoundException("Couldn't find theme ["+name+"]");
        
        return theme;
    }
    
    
    /**
     * @see org.roller.model.ThemeManager#getThemeById(java.lang.String)
     */
    public Theme getThemeById(String id) 
        throws ThemeNotFoundException, RollerException {
        
        // In this implementation where themes come from the filesystem we
        // know that the name and id for a theme are the same
        return this.getTheme(id);
    }
    
    
    /**
     * @see org.roller.model.ThemeManager#getThemesList()
     */
    public List getThemesList() {
        
        List themes = new ArrayList(this.themes.keySet());
        
        // sort 'em ... the natural sorting order for Strings is alphabetical
        Collections.sort(themes);
        
        return themes;
    }
    
    
    /**
     * @see org.roller.model.ThemeManager#getEnabledThemesList()
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
     * @see org.roller.model.ThemeManager#getTemplate(String, String)
     */
    public ThemeTemplate getTemplate(String theme_name, String template_name)
        throws ThemeNotFoundException, RollerException {
        
        // basically we just try and lookup the theme first, then template
        Theme theme = this.getTheme(theme_name);
        
        return theme.getTemplate(template_name);
    }
    
    
    /**
     * @see org.roller.model.ThemeManager#getTemplateById(java.lang.String)
     */
    public ThemeTemplate getTemplateById(String id)
        throws ThemeNotFoundException, RollerException {
        
        if(id == null)
            throw new ThemeNotFoundException("Theme id was null");
        
        // in our case we expect a template id to be <theme>:<template>
        // so extract each piece and do the lookup
        String[] split = id.split(":",  2);
        if(split.length != 2)
            throw new ThemeNotFoundException("Invalid theme id ["+id+"]");
        
        return this.getTemplate(split[0], split[1]);
    }
    
    
    /**
     * @see org.roller.model.ThemeManager#getTemplateByLink(java.lang.String)
     */
    public ThemeTemplate getTemplateByLink(String theme_name, String template_link)
        throws ThemeNotFoundException, RollerException {
        
        // basically we just try and lookup the theme first, then template
        Theme theme = this.getTheme(theme_name);
        
        return theme.getTemplateByLink(template_link);
    }
    
    
    /**
     * This is a convenience method which loads all the theme data from
     * themes stored on the filesystem in the roller webapp /themes/ directory.
     */
    private Map loadAllThemesFromDisk() {
        
        Map themes = new HashMap();
        
        // NOTE: we need to figure out how to get the roller context path
        String themespath = RollerConfig.getProperty("context.realpath");
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
                mLogger.error("Problem reading theme " + themenames[i], unexpected);
            }
        }
        
        return themes;
    }
        
    /**
     * Another convenience method which knows how to load a single theme
     * off the filesystem and return a Theme object
     */
    private Theme loadThemeFromDisk(String theme_name, String themepath) {
        
        mLogger.info("Loading theme "+theme_name);  
        
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
                mLogger.error("Couldn't " + msg);
                continue;
            }
            char[] chars = null;
            try {
                FileReader reader = new FileReader(template_file);
                chars = new char[(int) template_file.length()];
                reader.read(chars);            
            } catch (Exception noprob) {
                mLogger.error("Exception while attempting to " + msg);
                if (mLogger.isDebugEnabled()) mLogger.debug(noprob);
                continue;
            }

            // construct ThemeTemplate representing this file
            theme_template = new ThemeTemplate(
                    theme_name+":"+template_name,
                    template_name,
                    template_name,
                    new String(chars),
                    template_name,
                    new Date(template_file.lastModified()));

            // add it to the theme
            theme.setTemplate(template_name, theme_template);
        }
        
        // use the last mod date of the last template file
        // as the last mod date of the theme
        theme.setLastModified(theme_template.getLastModified());
        
        return theme;
    }

}
