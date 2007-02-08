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

package org.apache.roller.planet.ui.admin.struts2;

import com.opensymphony.xwork2.Preparable;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.pojos.PropertyData;
import org.apache.roller.planet.ui.core.struts2.PlanetActionSupport;
import org.apache.struts2.interceptor.ParameterAware;


/**
 * Config Form Action.
 *
 * Handles editing of global runtime properties.
 *
 * TODO: validation and security.
 */
public class ConfigForm extends PlanetActionSupport 
        implements Preparable, ParameterAware {
    
    private static Log log = LogFactory.getLog(ConfigForm.class);
    
    // original request parameters
    private Map parameters = Collections.EMPTY_MAP;
    
    // runtime properties data
    private Map properties = Collections.EMPTY_MAP;
    
    
    public void prepare() throws Exception {
        // just grab our properties map and put it in the request
        PropertiesManager pMgr = PlanetFactory.getPlanet().getPropertiesManager();
        this.properties = pMgr.getProperties();
    }

    
    public String execute() {
        return INPUT;
    }
    
    
    public String save() {
        
        log.debug("Handling update request");
        
        try {
            // only set values for properties that are already defined
            String propName = null;
            PropertyData updProp = null;
            String incomingProp = null;
            Iterator propsIT = this.properties.keySet().iterator();
            while(propsIT.hasNext()) {
                propName = (String) propsIT.next();
                
                log.debug("Checking property ["+propName+"]");
                
                updProp = (PropertyData) this.properties.get(propName);
                String[] propValues = (String[]) this.parameters.get(updProp.getName());
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
            
        } catch (RollerException e) {
            log.error(e);
            setError("ConfigForm.error.saveFailed");
        }
        
        setSuccess("ConfigForm.message.saveSucceeded");
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
    
}
