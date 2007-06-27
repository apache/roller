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

import java.util.Date;
import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FileManager;
import org.apache.roller.weblogger.business.FileNotFoundException;
import org.apache.roller.weblogger.business.FilePathException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.ThemeResource;
import org.apache.roller.weblogger.pojos.ThemeTemplate;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * A WeblogTheme custom defined by the weblog owner.
 */
public class WeblogCustomTheme extends WeblogTheme {
    
    
    public WeblogCustomTheme(Weblog weblog) {
        super(weblog);
    }
    
    
    public String getId() {
        return CUSTOM;
    }
    
    public String getName() {
        return CUSTOM;
    }
    
    public String getDescription() {
        return CUSTOM;
    }

    public String getAuthor() {
        return "N/A";
    }
    
    public Date getLastModified() {
        return this.weblog.getLastModified();
    }
    
    public boolean isEnabled() {
        return true;
    }
    
    
    /**
     * Get the collection of all templates associated with this Theme.
     */
    public List getTemplates() throws WebloggerException {
        UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
        return userMgr.getPages(this.weblog);
    }
    
    
    /**
     * Lookup the stylesheet template for this theme.
     * Returns null if no stylesheet can be found.
     */
    public ThemeTemplate getStylesheet() throws WebloggerException {
        return getTemplateByLink(this.weblog.getCustomStylesheetPath());
    }
    
    
    /**
     * Lookup the default template.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getDefaultTemplate() throws WebloggerException {
        UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
        return userMgr.getPageByAction(this.weblog, ThemeTemplate.ACTION_WEBLOG);
    }
    
    
    /**
     * Lookup the specified template by action.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByAction(String action) throws WebloggerException {
        if(action == null)
            return null;
        
        UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
        return userMgr.getPageByAction(this.weblog, action);
    }
    
    
    /**
     * Lookup the specified template by name.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByName(String name) throws WebloggerException {
        if(name == null)
            return null;
        
        UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
        return userMgr.getPageByName(this.weblog, name);
    }
    
    
    /**
     * Lookup the specified template by link.
     * Returns null if the template cannot be found.
     */
    public ThemeTemplate getTemplateByLink(String link) throws WebloggerException {
        if(link == null)
            return null;
        
        UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();
        return userMgr.getPageByLink(this.weblog, link);
    }
    
    
    /**
     * Lookup the specified resource by path.
     * Returns null if the resource cannot be found.
     */
    public ThemeResource getResource(String path) {
        
        ThemeResource resource = null;
        
        try {
            FileManager fileMgr = WebloggerFactory.getWeblogger().getFileManager();
            resource = fileMgr.getFile(this.weblog, path);
        } catch (WebloggerException ex) {
            // ignored, resource considered not found
        }
        
        return resource;
    }
    
}
