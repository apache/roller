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

import java.util.*;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.comment.WeblogEntryCommentPlugin;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.config.runtime.ConfigDef;
import org.apache.roller.weblogger.config.runtime.PropertyDef;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefs;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.struts2.interceptor.ParameterAware;
import org.apache.struts2.interceptor.ServletRequestAware;


/**
 * Action which handles editing of global configuration.
 */
// TODO: make this work @AllowedMethods({"execute","save"})
public class GlobalConfig extends UIAction implements ParameterAware, ServletRequestAware {

    private static Log log = LogFactory.getLog(GlobalConfig.class);

    // the request parameters
    private Map<String, String[]> params = Collections.emptyMap();

    // map of config properties
    private Map<String, RuntimeConfigProperty> properties = Collections.emptyMap();

    // the runtime config def used to populate the display
    private ConfigDef globalConfigDef = null;

    // list of comment plugins
    private List<WeblogEntryCommentPlugin> pluginsList = Collections.emptyList();

    // comment plugins that are enabled.  this is what the html form submits to
    private String[] commentPlugins = new String[0];

    // work around checkbox issue in cases where user inadvertently does a
    // GET on the GlobalConfig!save URL and thus sets all checkboxes to false
    private String httpMethod = "GET";

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

    // weblogs for frontpage blog chooser
    private Collection<Weblog> weblogs;


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
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
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
            addError("Unexpected error accessing Roller properties");
        }

        try {
            WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
            setWeblogs(mgr.getWeblogs(true, null, null, null, 0, -1));
        } catch (WebloggerException ex) {
            log.error("Error getting weblogs", ex);
            addError("frontpageConfig.weblogs.error");
        }

        // set config def used to draw the view
        RuntimeConfigDefs defs = WebloggerRuntimeConfig.getRuntimeConfigDefs();
        List<ConfigDef> configDefs = defs.getConfigDefs();
        for (ConfigDef configDef : configDefs) {
            if ("global-properties".equals(configDef.getName())) {
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
        if (!"POST".equals(httpMethod)) {
            return ERROR;
        }

        // only set values for properties that are already defined
        RuntimeConfigProperty updProp;
        String incomingProp;
        for (String propName : getProperties().keySet()) {
            updProp = getProperties().get(propName);
            incomingProp = this.getParameter(updProp.getName());

            PropertyDef propertyDef = globalConfigDef.getPropertyDef( propName );
            if ( propertyDef == null) {
                // we're only processing defined properties, i.e. ones shown in the UI
                continue;
            }

            if ( propertyDef.getType().equals("boolean") ) {

                try {
                    if (incomingProp == null) {
                        updProp.setValue("false");
                    } else {
                        boolean value = Boolean.parseBoolean(incomingProp);
                        updProp.setValue(Boolean.toString(value));
                    }
                    log.debug("Set boolean " + propName + " = " + incomingProp);
                } catch ( Exception nfe ) {
                    String propDesc = bundle.getString( propertyDef.getKey() );
                    addError("ConfigForm.invalidBooleanProperty",
                            Arrays.asList(propDesc, propName));
                }

            } else if ( incomingProp != null && propertyDef.getType().equals("integer") ) {

                try {
                    Integer.parseInt(incomingProp);
                    updProp.setValue(incomingProp);
                    log.debug("Set integer " + propName + " = " + incomingProp);
                } catch ( NumberFormatException nfe ) {
                    String propDesc = bundle.getString( propertyDef.getKey() );
                    addError("ConfigForm.invalidIntegerProperty",
                            Arrays.asList(propDesc, propName));
                }

            } else if ( incomingProp != null && propertyDef.getType().equals("float") ) {

                try {
                    Float.parseFloat(incomingProp);
                    updProp.setValue(incomingProp);
                    log.debug("Set float " + propName + " = " + incomingProp);
                } catch ( NumberFormatException nfe ) {
                    String propDesc = bundle.getString(propertyDef.getKey());
                    addError("ConfigForm.invalidFloatProperty",
                        Arrays.asList(propDesc, propName));
                }

            } else if ( incomingProp != null ){
                updProp.setValue( incomingProp.trim() );
                log.debug("Set something " + propName + " = " + incomingProp);

            } else if ( propertyDef.getName().equals("users.comments.plugins") ) {
                // not a problem

            } else {
                addError("ConfigForm.invalidProperty", propName);
            }

        }

        if ( this.hasActionErrors() ) {
            return ERROR;
        }

        // special handling for comment plugins
        String enabledPlugins = "";
        if (getCommentPlugins().length > 0) {
            enabledPlugins = StringUtils.join(getCommentPlugins(), ",");
        }
        RuntimeConfigProperty prop = getProperties().get("users.comments.plugins");
        prop.setValue(enabledPlugins);

        try {
            // save 'em and flush
            PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            mgr.saveProperties(getProperties());
            WebloggerFactory.getWeblogger().flush();

            // notify user of our success
            addMessage("generic.changes.saved");

        } catch (WebloggerException ex) {
            log.error("Error saving roller properties", ex);
            addError("generic.error.check.logs");
        }

        return SUCCESS;
    }


    public void setParameters(Map<String, String[]> parameters) {
        this.params = parameters;

        if (log.isDebugEnabled()) {
            log.debug("Parameter map:");

            for (Map.Entry<String, String[]> entry : parameters.entrySet()) {
                log.debug(entry.getKey() + " = " + Utilities.stringArrayToString(entry.getValue(),","));
            }
        }
    }

    // convenience method for getting a single parameter as a String
    private String getParameter(String key) {

        String[] p = this.params.get(key);
        if (p != null && p.length > 0) {
            return p[0];
        }
        return null;
    }


    public Map<String, RuntimeConfigProperty> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, RuntimeConfigProperty> properties) {
        this.properties = properties;
        for (Map.Entry<String, RuntimeConfigProperty> entry : properties.entrySet()) {
            log.debug("Got " + entry.getKey() + " = " + entry.getValue().getValue());
        }
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
        return commentPlugins.clone();
    }

    public void setCommentPlugins(String[] commentPlugins) {
        this.commentPlugins = commentPlugins.clone();
    }

    public void setServletRequest(HttpServletRequest req) {
        httpMethod = req.getMethod();
    }

    public Collection<Weblog> getWeblogs() {
        return weblogs;
    }

    public void setWeblogs(Collection<Weblog> weblogs) {
        this.weblogs = weblogs;
    }
}
