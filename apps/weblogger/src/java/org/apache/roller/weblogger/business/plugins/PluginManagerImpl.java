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

package org.apache.roller.weblogger.business.plugins;

import java.util.ArrayList;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;


/**
 * Plugin management for business layer and more generally applied plugins.
 */
public class PluginManagerImpl implements PluginManager {
    
    private static Log log = LogFactory.getLog(PluginManagerImpl.class);
    
    // Plugin classes keyed by plugin name
    static Map mPagePlugins = new LinkedHashMap();
    
    // Comment plugins
    private List<WeblogEntryCommentPlugin> commentPlugins = new ArrayList();
    
    
    /**
     * Creates a new instance of PluginManagerImpl
     */
    public PluginManagerImpl() {
        // load weblog entry plugins
        loadPagePluginClasses();
        
        // load weblog entry comment plugins
        loadCommentPlugins();
    }
    
    
    public boolean hasPagePlugins() {
        log.debug("mPluginClasses.size(): " + mPagePlugins.size());
        return (mPagePlugins != null && mPagePlugins.size() > 0);
    }
    
    
    /**
     * Create and init plugins for processing entries in a specified website.
     */
    public Map getWeblogEntryPlugins(Weblog website) {
        Map ret = new LinkedHashMap();
        Iterator it = this.mPagePlugins.values().iterator();
        while (it.hasNext()) {
            try {
                Class pluginClass = (Class)it.next();
                WeblogEntryPlugin plugin = (WeblogEntryPlugin)pluginClass.newInstance();
                plugin.init(website);
                ret.put(plugin.getName(), plugin);
            } catch (Exception e) {
                log.error("Unable to init() PagePlugin: ", e);
            }
        }
        return ret;
    }
    
    
    public String applyWeblogEntryPlugins(Map pagePlugins,WeblogEntry entry, String str) {
        String ret = str;
        WeblogEntry copy = new WeblogEntry(entry);
        List entryPlugins = copy.getPluginsList();
        if (entryPlugins != null && !entryPlugins.isEmpty()) {
            Iterator iter = entryPlugins.iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                WeblogEntryPlugin pagePlugin = (WeblogEntryPlugin)pagePlugins.get(key);
                if (pagePlugin != null) {
                    ret = pagePlugin.render(entry, ret);
                } else {
                    log.error("ERROR: plugin not found: " + key);
                }
            }
        }
        return ret;
    }
    
    
    /**
     * @inheritDoc
     */
    public List<WeblogEntryCommentPlugin> getCommentPlugins() {
        return commentPlugins;
    }
    
    
    /**
     * @inheritDoc
     */
    public String applyCommentPlugins(WeblogEntryComment comment, String text) {
        
        if(comment == null || text == null) {
            throw new IllegalArgumentException("comment cannot be null");
        }
        
        String content = text;
        
        if (commentPlugins.size() > 0) {
            for( WeblogEntryCommentPlugin plugin : commentPlugins ) {
                if(comment.getPlugins() != null &&
                        comment.getPlugins().indexOf(plugin.getName()) != -1) {
                    log.debug("Invoking comment plugin "+plugin.getName());
                    content = plugin.render(comment, content);
                }
            }
        }
        
        return content;
    }
    
    
    /**
     * Initialize PagePlugins declared in roller.properties.
     * By using the full class name we also allow for the implementation of
     * "external" Plugins (maybe even packaged seperately). These classes are
     * then later instantiated by PageHelper.
     */
    private void loadPagePluginClasses() {
        log.debug("Initializing page plugins");
        
        String pluginStr = WebloggerConfig.getProperty("plugins.page");
        if (log.isDebugEnabled()) log.debug(pluginStr);
        if (pluginStr != null) {
            String[] plugins = StringUtils.stripAll(
                    StringUtils.split(pluginStr, ",") );
            for (int i=0; i<plugins.length; i++) {
                if (log.isDebugEnabled()) log.debug("try " + plugins[i]);
                try {
                    Class pluginClass = Class.forName(plugins[i]);
                    if (isPagePlugin(pluginClass)) {
                        WeblogEntryPlugin plugin = (WeblogEntryPlugin)pluginClass.newInstance();
                        mPagePlugins.put(plugin.getName(), pluginClass);
                    } else {
                        log.warn(pluginClass + " is not a PagePlugin");
                    }
                } catch (ClassNotFoundException e) {
                    log.error("ClassNotFoundException for " + plugins[i]);
                } catch (InstantiationException e) {
                    log.error("InstantiationException for " + plugins[i]);
                } catch (IllegalAccessException e) {
                    log.error("IllegalAccessException for " + plugins[i]);
                }
            }
        }
    }
    
    
    /**
     * Initialize all comment plugins defined in weblogger config.
     */
    private void loadCommentPlugins() {
        
        log.debug("Initializing comment plugins");
        
        String pluginStr = WebloggerConfig.getProperty("comment.formatter.classnames");
        if (pluginStr != null) {
            String[] plugins = StringUtils.stripAll(StringUtils.split(pluginStr, ","));
            for (int i=0; i < plugins.length; i++) {
                log.debug("trying " + plugins[i]);
                
                try {
                    Class pluginClass = Class.forName(plugins[i]);
                    WeblogEntryCommentPlugin plugin = 
                            (WeblogEntryCommentPlugin) pluginClass.newInstance();
                    
                    // make sure and maintain ordering
                    commentPlugins.add(i, plugin);
                    
                    log.debug("Configured comment plugin: "+plugins[i]);
                    
                } catch (ClassCastException e) {
                    log.error("ClassCastException for " + plugins[i]);
                } catch (ClassNotFoundException e) {
                    log.error("ClassNotFoundException for " + plugins[i]);
                } catch (InstantiationException e) {
                    log.error("InstantiationException for " + plugins[i]);
                } catch (IllegalAccessException e) {
                    log.error("IllegalAccessException for " + plugins[i]);
                }
            }
        }
        
    }
    
    
    private static boolean isPagePlugin(Class pluginClass) {
        Class[] interfaces = pluginClass.getInterfaces();
        for (int i=0; i<interfaces.length; i++) {
            if (interfaces[i].equals(WeblogEntryPlugin.class)) return true;
        }
        return false;
    }
    
    
    public void release() {
        // no op
    }
    
}
