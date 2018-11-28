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

package org.apache.roller.weblogger.planet.ui;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.config.PlanetRuntimeConfig;
import org.apache.roller.planet.config.runtime.ConfigDef;
import org.apache.roller.planet.config.runtime.RuntimeConfigDefs;
import org.apache.roller.planet.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.struts2.interceptor.ParameterAware;


/**
 * Planet Config Action.
 *
 * Handles editing of planet global runtime properties.
 */
public class PlanetConfig extends PlanetUIAction implements ParameterAware {
    
    private static Log log = LogFactory.getLog(PlanetConfig.class);
    
    // original request parameters
    private Map parameters = Collections.EMPTY_MAP;
    
    // runtime properties data
    private Map properties = Collections.EMPTY_MAP;
    
    // the runtime config def used to populate the display
    private ConfigDef globalConfigDef = null;
    
    
    public PlanetConfig() {
        this.actionName = "planetConfig";
        this.desiredMenu = "admin";
        this.pageTitle = "planetConfig.title";
    }
    
    
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }

    
    @Override
    public boolean isWeblogRequired() {
        return false;
    }
    
    
    @Override
    public void myPrepare() {
        try {
            // just grab our properties map
            PropertiesManager pMgr = PlanetFactory.getPlanet().getPropertiesManager();
            setProperties(pMgr.getProperties());
        } catch (RollerException ex) {
            log.error("Error loading planet properties");
        }
        
        // set config def used to draw the view
        RuntimeConfigDefs defs = PlanetRuntimeConfig.getRuntimeConfigDefs();
        List<ConfigDef> configDefs = defs.getConfigDefs();
        for(ConfigDef configDef : configDefs) {
            if("global-properties".equals(configDef.getName())) {
                setGlobalConfigDef(configDef);
            }
        }
    }

    
    public String execute() {
        return INPUT;
    }
    
    
    public String save() {
        
        try {
            // only set values for properties that are already defined
            String propName = null;
            RuntimeConfigProperty updProp = null;
            String incomingProp = null;
            Iterator propsIT = getProperties().keySet().iterator();
            while(propsIT.hasNext()) {
                propName = (String) propsIT.next();
                
                log.debug("Checking property ["+propName+"]");
                
                updProp = (RuntimeConfigProperty) getProperties().get(propName);
                String[] propValues = (String[]) getParameters().get(updProp.getName());
                if(propValues != null && propValues.length > 0) {
                    // we don't deal with multi-valued props
                    incomingProp = propValues[0];
                }
                
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
                    
                    updProp.setValue(incomingProp.trim());
                }
            }
            
            // save it
            PropertiesManager pMgr = PlanetFactory.getPlanet().getPropertiesManager();
            pMgr.saveProperties(this.properties);
            PlanetFactory.getPlanet().flush();
            
            addMessage("ConfigForm.message.saveSucceeded");
            
        } catch (RollerException e) {
            log.error(e);
            addError("ConfigForm.error.saveFailed");
        }
        
        return INPUT;
    }

    
    public Map getParameters() {
        return parameters;
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
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
    
}
