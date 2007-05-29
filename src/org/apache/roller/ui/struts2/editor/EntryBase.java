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

package org.apache.roller.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.PluginManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogEntryPlugin;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.pojos.WeblogPermission;
import org.apache.roller.pojos.WeblogEntry;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.plugins.UIPluginManager;
import org.apache.roller.ui.core.plugins.WeblogEntryEditor;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.roller.util.URLUtilities;


/**
 * A collection of base functionality used by entry actions.
 */
public abstract class EntryBase extends UIAction {
   
    private static Log log = LogFactory.getLog(EntryBase.class);
    
    
    /**
     * Trigger reindexing of modified entry.
     */
    protected void reindexEntry(WeblogEntry entry) {
        IndexManager manager = RollerFactory.getRoller().getIndexManager();
        
        // if published, index the entry
        if (entry.isPublished()) {
            try {
                manager.addEntryReIndexOperation(entry);
            } catch (RollerException ex) {
                log.warn("Trouble triggering entry indexing", ex);
            }
        }
    }
    
    
    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List<WeblogEntry> getRecentPublishedEntries() {
        List<WeblogEntry> entries = Collections.EMPTY_LIST;
        try {
            entries = RollerFactory.getRoller().getWeblogManager().getWeblogEntries(
                    
                    getActionWeblog(), // userName
                    null,
                    null,              // startDate
                    null,              // endDate
                    null,              // catName
                    null,WeblogEntry.PUBLISHED, // status
                    null,              // text
                    null,              // sortby (null for pubTime)
                    null,
                    null,
                    0, 20);
        } catch (RollerException ex) {
            log.error("Error getting entries list", ex);
        }
        return entries;
    }
    
    
    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List<WeblogEntry> getRecentScheduledEntries() {
        List<WeblogEntry> entries = Collections.EMPTY_LIST;
        try {
            entries = RollerFactory.getRoller().getWeblogManager().getWeblogEntries(
                    
                    getActionWeblog(), // userName
                    null,
                    null,              // startDate
                    null,              // endDate
                    null,              // catName
                    null,WeblogEntry.SCHEDULED, // status
                    null,              // text
                    null,              // sortby (null for pubTime)
                    null,
                    null,
                    0, 20);
        } catch (RollerException ex) {
            log.error("Error getting entries list", ex);
        }
        return entries;
    }
    
    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List<WeblogEntry> getRecentDraftEntries() {
        List<WeblogEntry> entries = Collections.EMPTY_LIST;
        try {
            entries = RollerFactory.getRoller().getWeblogManager().getWeblogEntries(
                    
                    getActionWeblog(),
                    null,
                    null,              // startDate
                    null,              // endDate
                    null,              // catName
                    null,WeblogEntry.DRAFT, // status
                    null,              // text
                    "updateTime",      // sortby
                    null,
                    null,
                    0, 20);  // maxEntries
        } catch (RollerException ex) {
            log.error("Error getting entries list", ex);
        }
        return entries;
    }
    
    /**
     * Get recent weblog entries using request parameters to determine
     * username, date, and category name parameters.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */
    public List<WeblogEntry> getRecentPendingEntries() {
        List<WeblogEntry> entries = Collections.EMPTY_LIST;
        try {
            entries = RollerFactory.getRoller().getWeblogManager().getWeblogEntries(
                    
                    getActionWeblog(),
                    null,
                    null,              // startDate
                    null,              // endDate
                    null,              // catName
                    null,WeblogEntry.PENDING, // status
                    null,              // text
                    "updateTime",      // sortby
                    null,
                    null,
                    0, 20);
        } catch (RollerException ex) {
            log.error("Error getting entries list", ex);
        }
        return entries;
    }
    
    
    public List<WeblogEntryPlugin> getEntryPlugins() {
        List<WeblogEntryPlugin> availablePlugins = Collections.EMPTY_LIST;
        try {
            PluginManager ppmgr = RollerFactory.getRoller().getPagePluginManager();
            Map<String, WeblogEntryPlugin> plugins = ppmgr.getWeblogEntryPlugins(getActionWeblog());
            
            if(plugins.size() > 0) {
                availablePlugins = new ArrayList();
                for(WeblogEntryPlugin plugin : plugins.values()) {
                    availablePlugins.add(plugin);
                }
            }
        } catch (Exception ex) {
            log.error("Error getting plugins list", ex);
        }
        return availablePlugins;
    }
    
    
    public WeblogEntryEditor getEditor() {
        UIPluginManager pmgr = RollerContext.getUIPluginManager();
        return pmgr.getWeblogEntryEditor(getActionWeblog().getEditorPage());
    }
    
    
    public boolean isUserAnAuthor() {
        return getActionWeblog().hasUserPermissions(getAuthenticatedUser(),WeblogPermission.AUTHOR);
    }
    
    
    public String getJsonAutocompleteUrl() {
        return URLUtilities.getWeblogTagsJsonURL(getActionWeblog(), false);
    }
    
}
