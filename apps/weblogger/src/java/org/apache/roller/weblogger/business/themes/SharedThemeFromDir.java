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
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.WeblogTemplate;


/**
 * The Theme object encapsulates all elements of a single weblog theme.  It
 * is used mostly to contain all the templates for a theme, but does contain
 * other theme related attributes such as name, last modifed date, etc.
 */
public class SharedThemeFromDir extends SharedTheme {
    
    private static Log log = LogFactory.getLog(SharedThemeFromDir.class);
    
    // the filesystem directory where we should read this theme from
    private String themeDir = null;
    
    // the theme preview image
    private ThemeResource previewImage = null;
    
    // the theme stylesheet
    private ThemeTemplate stylesheet = null;
    
    // we keep templates in a Map for faster lookups by name
    // the Map contains ... (template name, ThemeTemplate)
    private Map templatesByName = new HashMap();
    
    // we keep templates in a Map for faster lookups by link
    // the Map contains ... (template link, ThemeTemplate)
    private Map templatesByLink = new HashMap();
    
    // we keep templates in a Map for faster lookups by action
    // the Map contains ... (template action, ThemeTemplate)
    private Map templatesByAction = new HashMap();
    
    // we keep resources in a Map for faster lookups by path
    // the Map contains ... (resource path, ThemeResource)
    private Map resources = new HashMap();
    
    
    public SharedThemeFromDir(String themeDirPath) 
            throws ThemeInitializationException {
        
        this.themeDir = themeDirPath;
        
        // load the theme elements and cache 'em
        loadThemeFromDisk();
    }

    
    /**
     * Get a resource representing the preview image for this theme.
     */
    public ThemeResource getPreviewImage() {
        return this.previewImage;
    }
    
    
    /**
     * Get the collection of all templates associated with this Theme.
     */
    public List getTemplates() {
        return new ArrayList(this.templatesByName.values());
    }
    
    
    /**
     * Lookup the stylesheet.
     * Returns null if no stylesheet defined.
     */
    public ThemeTemplate getStylesheet() {
        return this.stylesheet;
    }
    
    
    /**
     * Looup the default template, action = weblog.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getDefaultTemplate() {
        return (ThemeTemplate) this.templatesByAction.get(ThemeTemplate.ACTION_WEBLOG);
    }
    
    
    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByName(String name) {
        return (ThemeTemplate) this.templatesByName.get(name);
    }
    
    
    /**
     * Lookup the specified template by link.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByLink(String link) {
        return (ThemeTemplate) this.templatesByLink.get(link);
    }
    
    
    /**
     * Lookup the specified template by action.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByAction(String action) {
        return (ThemeTemplate) this.templatesByAction.get(action);
    }
    
    
    /**
     * Get the collection of all resources associated with this Theme.
     *
     * It is assured that the resources are returned sorted by pathname.
     */
    public List getResources() {
        
        // make sure resources are sorted.
        List myResources = new ArrayList(this.resources.values());
        Collections.sort(myResources);
        
        return myResources;
    }
    
    
    /**
     * Lookup the specified resource by path.
     * Returns null if the resource cannot be found.
     */
    public ThemeResource getResource(String path) {
        return (ThemeResource) this.resources.get(path);
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(name);
        sb.append("\n");
        
        Iterator it = this.templatesByName.values().iterator();
        while(it.hasNext()) {
            sb.append(it.next());
            sb.append("\n");
        }
        
        return sb.toString();
        
    }
    
    
    /**
     * Load all the elements of this theme from disk and cache them.
     */
    private void loadThemeFromDisk() throws ThemeInitializationException {
        
        log.debug("Parsing theme descriptor for "+this.themeDir);
        
        ThemeMetadata themeMetadata = null;
        try {
            // lookup theme descriptor and parse it
            ThemeMetadataParser parser = new ThemeMetadataParser();
            InputStream is = new FileInputStream(this.themeDir + File.separator + "theme.xml");
            themeMetadata = parser.unmarshall(is);
        } catch (Exception ex) {
            throw new ThemeInitializationException("Unable to parse theme descriptor for theme "+this.themeDir, ex);
        }
        
        log.debug("Loading Theme "+themeMetadata.getName());
        
        // use parsed theme descriptor to load Theme data
        setId(themeMetadata.getId());
        setName(themeMetadata.getName());
        setDescription(themeMetadata.getName());
        setAuthor(themeMetadata.getAuthor());
        setLastModified(new Date());
        setEnabled(true);
        
        // load resource representing preview image
        File previewFile = new File(this.themeDir + File.separator + themeMetadata.getPreviewImage());
        if(!previewFile.exists() || !previewFile.canRead()) {
            log.warn("Couldn't read preview image file ["+themeMetadata.getPreviewImage()+"]");
        } else {
            this.previewImage = new SharedThemeResourceFromDir(themeMetadata.getPreviewImage(), previewFile);
        }
        
        // load stylesheet if possible
        if(themeMetadata.getStylesheet() != null) {
            
            ThemeMetadataTemplate stylesheetTmpl = themeMetadata.getStylesheet();
            
            // construct File object from path
            File templateFile = new File(this.themeDir + File.separator + 
                    stylesheetTmpl.getContentsFile());
            
            // read stylesheet contents
            String contents = loadTemplateFile(templateFile);
            if(contents == null) {
                // if we don't have any contents then skip this one
                log.error("Couldn't load stylesheet template file ["+templateFile+"]");
            } else {
                
                // construct ThemeTemplate representing this file
                SharedThemeTemplate theme_template = new SharedThemeTemplate(
                        this,
                        themeMetadata.getId()+":"+stylesheetTmpl.getName(),
                        WeblogTemplate.ACTION_CUSTOM,
                        stylesheetTmpl.getName(),
                        stylesheetTmpl.getDescription(),
                        contents,
                        stylesheetTmpl.getLink(),
                        new Date(templateFile.lastModified()),
                        stylesheetTmpl.getTemplateLanguage(),
                        false,
                        false);
                
                // store it
                this.stylesheet = theme_template;
                
                // add it to templates list
                addTemplate(theme_template);
            }
        }
        
        // go through static resources and add them to the theme
        String resourcePath = null;
        Iterator resourcesIter = themeMetadata.getResources().iterator();
        while (resourcesIter.hasNext()) {
            resourcePath = (String) resourcesIter.next();
            
            // construct ThemeResource object from resource
            File resourceFile = new File(this.themeDir + File.separator + resourcePath);
            
            // Continue reading theme even if problem encountered with one file
            if(!resourceFile.exists() || !resourceFile.canRead()) {
                log.warn("Couldn't read theme resource file ["+resourcePath+"]");
                continue;
            }
            
            // add it to the theme
            setResource(resourcePath, new SharedThemeResourceFromDir(resourcePath, resourceFile));
        }
        
        // go through templates and read in contents to a ThemeTemplate
        ThemeTemplate theme_template = null;
        ThemeMetadataTemplate templateMetadata = null;
        Iterator templatesIter = themeMetadata.getTemplates().iterator();
        while (templatesIter.hasNext()) {
            templateMetadata = (ThemeMetadataTemplate) templatesIter.next();
            
            // construct File object from path
            File templateFile = new File(this.themeDir + File.separator + 
                    templateMetadata.getContentsFile());
            
            String contents = loadTemplateFile(templateFile);
            if(contents == null) {
                // if we don't have any contents then skip this one
                throw new ThemeInitializationException("Couldn't load theme template file ["+templateFile+"]");
            }
            
            // construct ThemeTemplate representing this file
            theme_template = new SharedThemeTemplate(
                    this,
                    themeMetadata.getId()+":"+templateMetadata.getName(),
                    templateMetadata.getAction(),
                    templateMetadata.getName(),
                    templateMetadata.getDescription(),
                    contents,
                    templateMetadata.getLink(),
                    new Date(templateFile.lastModified()),
                    templateMetadata.getTemplateLanguage(),
                    templateMetadata.isHidden(),
                    templateMetadata.isNavbar());

            // add it to the theme
            addTemplate(theme_template);
        }
    }
    
    
    /**
     * Load a single template file as a string, returns null if can't read file.
     */
    private String loadTemplateFile(File templateFile) {
        // Continue reading theme even if problem encountered with one file
        if(!templateFile.exists() && !templateFile.canRead()) {
            return null;
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
            return null;
        }
        
        return new String(chars, 0, length);
    }
    
    
    /**
     * Set the value for a given template name.
     */
    private void addTemplate(ThemeTemplate template) {
        this.templatesByName.put(template.getName(), template);
        this.templatesByLink.put(template.getLink(), template);
        if(!ThemeTemplate.ACTION_CUSTOM.equals(template.getAction())) {
            this.templatesByAction.put(template.getAction(), template);
        }
    }
    
    
    /**
     * Set the value for a given resource path.
     */
    private void setResource(String path, SharedThemeResourceFromDir resource) {
        this.resources.put(path, resource);
    }
    
}
