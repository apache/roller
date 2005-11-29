/* Created on November 26, 2005, 9:04 AM */
package org.roller.business;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.roller.config.RollerConfig;
import org.roller.model.PagePlugin;
import org.roller.model.PagePluginManager;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.StringUtils;

/**
 * Centralized page plugin management.
 * @author Dave Johnson
 */
public class PagePluginManagerImpl implements PagePluginManager {
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(PagePluginManagerImpl.class);
    
    // Plugin classes keyed by plugin name
    static Map mPagePlugins = new LinkedHashMap();
    
    /**
     * Creates a new instance of PagePluginManagerImpl
     */
    public PagePluginManagerImpl() {
        loadPagePluginClasses();
    }
    
    public boolean hasPagePlugins() {
        mLogger.debug("mPluginClasses.size(): " + mPagePlugins.size());
        return (mPagePlugins != null && mPagePlugins.size() > 0);
    }
    
    /**
     * Initialize PagePlugins declared in roller.properties.
     * By using the full class name we also allow for the implementation of
     * "external" Plugins (maybe even packaged seperately). These classes are
     * then later instantiated by PageHelper.
     */
    private void loadPagePluginClasses() {
        mLogger.debug("Initializing page plugins");
        
        String pluginStr = RollerConfig.getProperty("plugins.page");
        if (mLogger.isDebugEnabled()) mLogger.debug(pluginStr);
        if (pluginStr != null) {
            String[] plugins = StringUtils.stripAll(
                    StringUtils.split(pluginStr, ",") );
            for (int i=0; i<plugins.length; i++) {
                if (mLogger.isDebugEnabled()) mLogger.debug("try " + plugins[i]);
                try {
                    Class pluginClass = Class.forName(plugins[i]);
                    if (isPagePlugin(pluginClass)) {
                        PagePlugin plugin = (PagePlugin)pluginClass.newInstance();
                        mPagePlugins.put(plugin.getName(), pluginClass);
                    } else {
                        mLogger.warn(pluginClass + " is not a PagePlugin");
                    }
                } catch (ClassNotFoundException e) {
                    mLogger.error("ClassNotFoundException for " + plugins[i]);
                } catch (InstantiationException e) {
                    mLogger.error("InstantiationException for " + plugins[i]);
                } catch (IllegalAccessException e) {
                    mLogger.error("IllegalAccessException for " + plugins[i]);
                }
            }
        }
    }

    /**
     * Create and init plugins for processing entries in a specified website. 
     */
    public Map createAndInitPagePlugins(
            WebsiteData website,
            Object servletContext,
            String contextPath,
            Context ctx) {
        Map ret = new LinkedHashMap();
        Iterator it = getPagePluginClasses().values().iterator();
        while (it.hasNext()) {
            try {
                Class pluginClass = (Class)it.next();
                PagePlugin plugin = (PagePlugin)pluginClass.newInstance();
                plugin.init(website, servletContext, contextPath, ctx);
                ret.put(plugin.getName(), plugin);
            } catch (Exception e) {
                mLogger.error("Unable to init() PagePlugin: ", e);
            }
        }
        return ret;
    }
    
    public WeblogEntryData applyPagePlugins(
            WeblogEntryData entry, Map pagePlugins, boolean skipFlag) { 
        WeblogEntryData copy = new WeblogEntryData(entry);        
        List entryPlugins = copy.getPluginsList();
        if (entryPlugins != null && !entryPlugins.isEmpty()) {    
            Iterator iter = entryPlugins.iterator();
            while (iter.hasNext()) {
                String key = (String)iter.next();
                PagePlugin pagePlugin = (PagePlugin)pagePlugins.get(key);
                if (pagePlugin != null) {
                    copy.setText((pagePlugin).render(copy, skipFlag));
                } else {
                    mLogger.error("ERROR: plugin not found: " + key);
                }
            }
        }
        return copy;
    }
    
    private static boolean isPagePlugin(Class pluginClass) {
        Class[] interfaces = pluginClass.getInterfaces();
        for (int i=0; i<interfaces.length; i++) {
            if (interfaces[i].equals(PagePlugin.class)) return true;
        }
        return false;
    }
    
    private Map getPagePluginClasses() {
        return mPagePlugins;
    }
    
    public void release() {
        // no op
    }
}
