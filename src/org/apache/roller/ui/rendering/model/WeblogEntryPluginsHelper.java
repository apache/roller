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
package org.apache.roller.ui.rendering.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.WeblogEntryPlugin;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.wrapper.WeblogEntryDataWrapper;

/**
 * Holds plugins configured for rendering.
 */
public class WeblogEntryPluginsHelper {
    private Map initializedPlugins = null;
    
    protected static Log log = 
        LogFactory.getFactory().getInstance(WeblogEntryPluginsHelper.class);
        
    public WeblogEntryPluginsHelper(WebsiteData weblog, Map model) throws RollerException {
        if (weblog != null) {
            // If we've got a weblog upfront, we can init plugins upfront
            Roller roller = RollerFactory.getRoller();
            PluginManager ppmgr = roller.getPagePluginManager();
            initializedPlugins = ppmgr.getWeblogEntryPlugins(weblog, model);
        } // else 
            // we're at site-wide scope and cannot init upfront
    }
    
    /**
     * Pass the String through any PagePlugins that have been
     * assigned to the OldPageHelper, as selected by the Entry.
     * 
     * @param entry  Entry being rendered.
     * @param str    String to which plugins are to be applied.
     * @param single True if single entry page being displayed.
     * @return       Result of applying plugins to str.
     */
    public String render(WeblogEntryDataWrapper entry, String str, boolean singleEntry) {
        String ret = str;
        log.debug("Applying page plugins to string");
        
        Map plugins = null;
        if (initializedPlugins != null) {
            plugins = initializedPlugins;
        } else {
            // Couldn't init weblog's plugins upfront, so we must init them now
            Roller roller = RollerFactory.getRoller();
            try {
                PluginManager ppmgr = roller.getPagePluginManager();
                // TODO: figure out what to do about plugins in site-wide scope
                plugins = ppmgr.getWeblogEntryPlugins(entry.getPojo().getWebsite(), new HashMap());
            } catch (RollerException e) {
                log.error("ERROR: accessing plugins for entry: " + entry.getId());
            }
        }
        
        if (plugins != null) {
            List entryPlugins = entry.getPluginsList();
            
            // if no Entry plugins, don't bother looping.
            if (entryPlugins != null && !entryPlugins.isEmpty()) {
                
                // now loop over mPagePlugins, matching
                // against Entry plugins (by name):
                // where a match is found render Plugin.
                Iterator iter = plugins.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    if (entryPlugins.contains(key)) {
                        WeblogEntryPlugin pagePlugin = (WeblogEntryPlugin)plugins.get(key);
                        try {
                            ret = pagePlugin.render(entry.getPojo(), ret);
                        } catch (Throwable t) {
                            log.error("ERROR from plugin: " + pagePlugin.getName(), t);
                        }
                    }
                }
            }
        }        
        return ret;
    }    
}
