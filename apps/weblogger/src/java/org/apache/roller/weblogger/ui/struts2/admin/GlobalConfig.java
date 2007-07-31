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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.config.runtime.ConfigDef;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefs;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.interceptor.ParameterAware;


/**
 * Action which handles editing of global configuration.
 */
public class GlobalConfig extends UIAction implements ParameterAware {
    
    private static Log log = LogFactory.getLog(GlobalConfig.class);
    
    // the request parameters as <String, String[]>
    private Map params = Collections.EMPTY_MAP;
    
    // map of config properties
    private Map properties = Collections.EMPTY_MAP;
    
    // the runtime config def used to populate the display
    private ConfigDef globalConfigDef = null;
    
    // list of comment plugins
    private List<WeblogEntryCommentPlugin> pluginsList = Collections.EMPTY_LIST;
    
    // comment plugins that are enabled.  this is what the html form submits to
    private String[] commentPlugins = new String[0];
    
    
    public GlobalConfig() {
        this.actionName = "globalConfig";
        this.desiredMenu = "admin";
        this.pageTitle = "configForm.title";
    }
    
    
    @Override
    public boolean isWeblogRequired() {
        return false;
    }
    
    @Override
    public String requiredUserRole() {
        return "admin";
    }
    
    
    /**
     * Prepare action by loading runtime properties map.
     */
    @Override
    public void myPrepare() {
        try {
            // just grab our properties map and make it available to the action
            PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            setProperties(mgr.getProperties());
        } catch (WebloggerException ex) {
            log.error("Error getting runtime properties map", ex);
            // TODO: i18n
            addError("Unexpected error accessing Roller properties");
        }
        
        // set config def used to draw the view
        RuntimeConfigDefs defs = WebloggerRuntimeConfig.getRuntimeConfigDefs();
        List<ConfigDef> configDefs = defs.getConfigDefs();
        for(ConfigDef configDef : configDefs) {
            if("global-properties".equals(configDef.getName())) {
                setGlobalConfigDef(configDef);
            }
        }
        
        // load plugins list
        PluginManager pmgr = WebloggerFactory.getWeblogger().getPluginManager();
        setPluginsList(pmgr.getCommentPlugins());
    }
    
    
    /**
     * Display global properties editor form.
     */
    @Override
    public String execute() {
        
        // setup array of configured plugins
        if (!StringUtils.isEmpty(WebloggerRuntimeConfig.getProperty("users.comments.plugins"))) {
            setCommentPlugins(StringUtils.split(WebloggerRuntimeConfig.getProperty("users.comments.plugins"), ","));
        }
        
        return SUCCESS;
    }
    
    
    /**
     * Save global properties.
     */
    public String save() {
        
        // only set values for properties that are already defined
        String propName = null;
        RuntimeConfigProperty updProp = null;
        String incomingProp = null;
        Iterator propsIT = getProperties().keySet().iterator();
        while(propsIT.hasNext()) {
            propName = (String) propsIT.next();
            updProp = (RuntimeConfigProperty) getProperties().get(propName);
            incomingProp = this.getParameter(updProp.getName());
            
            log.debug("Checking property ["+propName+"]");
            log.debug("Request value is ["+incomingProp+"]");
            
            // some special treatment for booleans
            // this is a bit hacky since we are assuming that any prop
            // with a value of "true" or "false" is meant to be a boolean
            // it may not always be the case, but we should be okay for now
            if( updProp.getValue() != null // null check needed w/Oracle
                    && (updProp.getValue().equals("true") || updProp.getValue().equals("false"))) {
                
                if(incomingProp == null || !incomingProp.equals("on"))
                    incomingProp = "false";
                else
                    incomingProp = "true";
            }
            
            // only work on props that were submitted with the request
            if(incomingProp != null) {
                log.debug("Setting new value for ["+propName+"]");
                
                // NOTE: the old way had some locale sensitive way to do this??
                updProp.setValue(incomingProp.trim());
            }
        }
        
        // special handling for comment plugins
        String enabledPlugins = "";
        if(getCommentPlugins().length > 0) {
            enabledPlugins = StringUtils.join(getCommentPlugins(), ",");
        }
        RuntimeConfigProperty prop = 
                    (RuntimeConfigProperty) getProperties().get("users.comments.plugins");
        prop.setValue(enabledPlugins);
            
        try {
            // save 'em and flush
            PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            mgr.saveProperties(getProperties());
            WebloggerFactory.getWeblogger().flush();
            
            // notify user of our success
            addMessage("weblogEdit.changesSaved");
            
        } catch (WebloggerException ex) {
            log.error("Error saving roller properties", ex);
            // TODO: i18n
            addError("error.update.rollerConfig");
        }
                
        return SUCCESS;
    }
    
    
    public void setParameters(Map parameters) {
        this.params = parameters;
        
        if(log.isDebugEnabled()) {
            log.debug("Parameter map:");
            Set<String> keys = parameters.keySet();
            for(String key : keys) {
                log.debug(key+" = "+parameters.get(key));
            }
        }
    }
    
    // convenience method for getting a single parameter as a String
    private String getParameter(String key) {
        
        String[] p = (String[]) this.params.get(key);
        if(p != null && p.length > 0) {
            return p[0];
        }
        return null;
    }
    
    
    public Map getProperties() {
        return properties;
    }

    public void setProperties(Map properties) {
        this.properties = properties;
    }

    public ConfigDef getGlobalConfigDef() {
        return globalConfigDef;
    }

    public void setGlobalConfigDef(ConfigDef globalConfigDef) {
        this.globalConfigDef = globalConfigDef;
    }
    
    public List<WeblogEntryCommentPlugin> getPluginsList() {
        return pluginsList;
    }

    public void setPluginsList(List<WeblogEntryCommentPlugin> pluginsList) {
        this.pluginsList = pluginsList;
    }
    
    public String[] getCommentPlugins() {
        return commentPlugins;
    }

    public void setCommentPlugins(String[] commentPlugins) {
        this.commentPlugins = commentPlugins;
    }
    
}
