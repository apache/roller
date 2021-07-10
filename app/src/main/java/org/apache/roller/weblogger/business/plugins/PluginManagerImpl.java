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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.roller.weblogger.util.Reflection;


/**
 * Plugin management for business layer and more generally applied plugins.
 */
public class PluginManagerImpl implements PluginManager {
    
    private static final Log log = LogFactory.getLog(PluginManagerImpl.class);
    
    // Plugin classes keyed by plugin name
    private static final Map<String, Class<? extends WeblogEntryPlugin>> mPagePlugins = new LinkedHashMap<>();
    
    // Comment plugins
    private final List<WeblogEntryCommentPlugin> commentPlugins = new ArrayList<>();
    
    
    /**
     * Creates a new instance of PluginManagerImpl
     */
    public PluginManagerImpl() {
        // load weblog entry plugins
        loadPagePluginClasses();
        
        // load weblog entry comment plugins
        loadCommentPlugins();
    }
    
    
    @Override
    public boolean hasPagePlugins() {
        log.debug("mPluginClasses.size(): " + mPagePlugins.size());
        return mPagePlugins != null && !mPagePlugins.isEmpty();
    }
    
    
    /**
     * Create and init plugins for processing entries in a specified website.
     */
    @Override
    public Map<String, WeblogEntryPlugin> getWeblogEntryPlugins(Weblog website) {
        
        Map<String, WeblogEntryPlugin> ret = new LinkedHashMap<>();
        
        for (Class<? extends WeblogEntryPlugin> pluginClass : mPagePlugins.values()) {
            try {
                WeblogEntryPlugin plugin = Reflection.newInstance(pluginClass);
                plugin.init(website);
                ret.put(plugin.getName(), plugin);
            } catch (ReflectiveOperationException | WebloggerException e) {
                log.error("Unable to init() PagePlugin: ", e);
            }
        }
        return ret;
    }
    
    @Override
    public String applyWeblogEntryPlugins(Map<String, WeblogEntryPlugin> pagePlugins, WeblogEntry entry, String str) {

        String ret = str;
        List<String> plugins = entry.getPluginsList();

        for (String key : plugins) {
            WeblogEntryPlugin pagePlugin = pagePlugins.get(key);
            if (pagePlugin != null) {
                ret = pagePlugin.render(entry, ret);
            } else {
                log.warn("plugin not found: " + key);
            }
        }

        return HTMLSanitizer.conditionallySanitize(ret);
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public List<WeblogEntryCommentPlugin> getCommentPlugins() {
        return commentPlugins;
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public String applyCommentPlugins(WeblogEntryComment comment, String text) {
        
        if(comment == null || text == null) {
            throw new IllegalArgumentException("comment cannot be null");
        }
        
        String content = text;
        
        if (!commentPlugins.isEmpty()) {
            for (WeblogEntryCommentPlugin plugin : commentPlugins) {
                if(comment.getPlugins() != null &&
                        comment.getPlugins().contains(plugin.getId())) {
                    log.debug("Invoking comment plugin "+plugin.getId());
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
        if (log.isDebugEnabled()) {
            log.debug(pluginStr);
        }
        if (pluginStr != null) {
            String[] plugins = StringUtils.stripAll(StringUtils.split(pluginStr, ","));
            for (String plugin : plugins) {
                if (log.isDebugEnabled()) {
                    log.debug("try " + plugin);
                }
                try {
                    Class<?> clazz = Class.forName(plugin);
                    
                    if (Reflection.implementsInterface(clazz, WeblogEntryPlugin.class)) {
                        @SuppressWarnings("unchecked")
                        Class<? extends WeblogEntryPlugin> pluginClass = (Class<? extends WeblogEntryPlugin>)clazz;
                        WeblogEntryPlugin weblogEntryPlugin = Reflection.newInstance(pluginClass);
                        mPagePlugins.put(weblogEntryPlugin.getName(), pluginClass);
                    } else {
                        log.warn(clazz + " is not a PagePlugin");
                    }
                } catch (ReflectiveOperationException e) {
                    log.error("unable to create " + plugin);
                }
            }
        }
    }
    
    
    /**
     * Initialize all comment plugins defined in weblogger config.
     */
    private void loadCommentPlugins() {
        
        try {
            commentPlugins.addAll(Reflection.newInstancesFromProperty("comment.formatter.classnames"));
        } catch (ReflectiveOperationException e) {
            log.error("unable to create comment plugins", e);
        }
        
        log.info("Configured comment plugins");
        log.info(commentPlugins.stream().map(t -> t.getClass().toString()).collect(Collectors.joining(",", "[", "]")));
    }
    
    @Override
    public void release() {
        // no op
    }
    
}
