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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;

import java.util.*;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTheme;


/**
 * A WeblogTheme shared by many weblogs and backed by a SharedTheme.
 */
public class WeblogSharedTheme extends WeblogTheme {
    
    private static Log log = LogFactory.getLog(WeblogSharedTheme.class);
    
    private SharedTheme theme = null;

    public WeblogSharedTheme(Weblog weblog, SharedTheme theme) {
        super(weblog);
        this.theme = theme;
    }
    
    
    public String getId() {
        return this.theme.getId();
    }
    
    public String getName() {
        return this.theme.getName();
    }

    public String getDescription() {
        return this.theme.getDescription();
    }
    
    public Date getLastModified() {
        return this.theme.getLastModified();
    }
    
    public boolean isEnabled() {
        return this.theme.isEnabled();
    }

    public int compareTo(Theme other) {
        return theme.compareTo(other);
    }

    /**
     * Get the collection of all templates associated with this Theme.
     */
    public List<ThemeTemplate> getTemplates() throws WebloggerException {
        
        Map<String, ThemeTemplate> pages = new TreeMap<String, ThemeTemplate>();
        
        // first get the pages from the db
        try {
            for (ThemeTemplate template : WebloggerFactory.getWeblogger().getWeblogManager().getPages(this.weblog)) {
                pages.put(template.getName(), template);
            }
        } catch(Exception e) {
            // db error
            log.error(e);
        }
        
        
        // now get theme pages if needed and put them in place of db pages
        try {
            for (ThemeTemplate template : this.theme.getTemplates()) {
                // note that this will put theme pages over custom
                // pages in the pages list, which is what we want
                pages.put(template.getName(), template);
            }
        } catch(Exception e) {
            // how??
            log.error(e);
        }
        
        return new ArrayList<ThemeTemplate>(pages.values());
    }
    
    
    /**
     * Lookup the stylesheet template for this theme.
     * Returns null if no stylesheet can be found.
     */
     public ThemeTemplate getStylesheet() throws WebloggerException {
        // stylesheet is handled differently than other templates because with
        // the stylesheet we want to return the weblog custom version if it
        // exists, otherwise we return the shared theme version

        // load from theme first to see if we even support a stylesheet
        ThemeTemplate stylesheet = this.theme.getStylesheet();
        if(stylesheet != null) {
            // now try getting custom version from weblog
            ThemeTemplate override = WebloggerFactory.getWeblogger()
                    .getWeblogManager().getPageByLink(this.weblog, stylesheet.getLink());
            if(override != null) {
                stylesheet = override;
            }
        }
        
        return stylesheet;
    }
    
    
    /**
     * Lookup the default template.
     */
    public ThemeTemplate getDefaultTemplate() throws WebloggerException {
        return this.theme.getDefaultTemplate();
    }
    
    
    /**
     * Lookup the specified template by action.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByAction(String action) throws WebloggerException {
        
        if (action == null) {
            return null;
        }
        
        // NOTE: we specifically do *NOT* return templates by action from the
        // weblog's custom templates if the weblog is using a theme because we
        // don't want old templates to take effect when using a specific theme
        return this.theme.getTemplateByAction(action);
    }
    
    
    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByName(String name) throws WebloggerException {
        
        if (name == null) {
            return null;
        }
        
        ThemeTemplate template;
        
        // if name refers to the stylesheet then return result of getStylesheet()
        ThemeTemplate stylesheet = getStylesheet();
        if (stylesheet != null && name.equals(stylesheet.getName())) {
            return stylesheet;
        }
        
        // first check if this user has selected a theme
        // if so then return the proper theme template
        template = this.theme.getTemplateByName(name);
        
        // if we didn't get the Template from a theme then look in the db
        if (template == null) {
            template = WebloggerFactory.getWeblogger().getWeblogManager().getPageByName(this.weblog, name);
        }
        
        return template;
    }
    
    
    /**
     * Lookup the specified template by link.
     * Returns null if the template cannot be found.
     */
     public ThemeTemplate getTemplateByLink(String link) throws WebloggerException {

        if (link == null) {
            return null;
        }

        ThemeTemplate template;

        // if name refers to the stylesheet then return result of getStylesheet()
        ThemeTemplate stylesheet = getStylesheet();
        if(stylesheet != null && link.equals(stylesheet.getLink())) {
            return stylesheet;
        }

        // first check if this user has selected a theme
        // if so then return the proper theme template
        template = this.theme.getTemplateByLink(link);

        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            template = WebloggerFactory.getWeblogger()
                    .getWeblogManager().getPageByLink(this.weblog, link);
        }

        return template;
    }
    
    
    /**
     * Lookup the specified resource by path.
     * Returns null if the resource cannot be found.
     */
    public ThemeResource getResource(String path) {
        
        if (path == null) {
            return null;
        }
        
        ThemeResource resource;
        
        // first check in our shared theme
        resource = this.theme.getResource(path);
        
        // if we didn't find it in our theme then look in weblog uploads
        if(resource == null) {
            try {
                MediaFileManager mmgr =
                    WebloggerFactory.getWeblogger().getMediaFileManager();
                MediaFile mf = mmgr.getMediaFileByOriginalPath(
                    this.weblog, path);

            } catch (WebloggerException ex) {
                // ignored, resource considered not found
            }
        }
        
        return resource;
    }
    
}
