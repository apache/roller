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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;

import java.util.*;
import org.apache.roller.weblogger.pojos.Theme;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.ThemeTemplate.ComponentType;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogTheme;


/**
 * A WeblogTheme shared by many weblogs and backed by a SharedTheme.
 */
public class WeblogSharedTheme extends WeblogTheme {
    
    private static Log log = LogFactory.getLog(WeblogSharedTheme.class);
    
    private SharedTheme theme = null;

    public WeblogSharedTheme(WeblogManager manager, Weblog weblog, SharedTheme theme) {
        super(manager, weblog);
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
            for (ThemeTemplate template : weblogManager.getTemplates(this.weblog)) {
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
     * Lookup the default template.
     */
    public ThemeTemplate getDefaultTemplate() throws WebloggerException {
        return this.theme.getDefaultTemplate();
    }

    /**
     * Lookup the specified template by action.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByAction(ComponentType action) throws WebloggerException {
        ThemeTemplate template = theme.getTemplateByAction(action);

        if (action == ComponentType.STYLESHEET && template != null) {
            // see if user is doing shared theme with custom stylesheet
            ThemeTemplate override = weblogManager.getTemplateByAction(this.weblog, ComponentType.STYLESHEET);
            if (override != null) {
                template = override;
            }
        }

        // NOTE except for stylesheets, we do *not* return templates by action from the
        // weblog's custom templates if the weblog is using a shared theme because we
        // don't want any dormant templates to take effect
        return template;
    }
    
    
    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByName(String name) throws WebloggerException {
        ThemeTemplate template = this.theme.getTemplateByName(name);
        // if we didn't get the Template from a theme then look in the db
        if (template == null) {
            template = weblogManager.getTemplateByName(this.weblog, name);
        }
        return template;
    }
    
    
    /**
     * Lookup the specified template by link.
     * Returns null if the template cannot be found.
     */
     public ThemeTemplate getTemplateByLink(String link) throws WebloggerException {
         ThemeTemplate template = this.theme.getTemplateByLink(link);

         if (template != null) {
             if (template.getAction() == ComponentType.STYLESHEET) {
                 // see if user is using a custom stylesheet with his shared theme
                 ThemeTemplate override = weblogManager.getTemplateByAction(this.weblog, ComponentType.STYLESHEET);
                 if (override != null) {
                     template = override;
                 }
             }
         } else {
             // custom template not part of shared them?  Check in DB...
             template = weblogManager.getTemplateByLink(this.weblog, link);
         }

         return template;
    }

}
