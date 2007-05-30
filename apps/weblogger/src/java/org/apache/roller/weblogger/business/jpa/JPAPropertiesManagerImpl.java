
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
package org.apache.roller.weblogger.business.jpa;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.jpa.JPAPersistenceStrategy;

import org.apache.roller.RollerException;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.config.runtime.ConfigDef;
import org.apache.roller.weblogger.config.runtime.DisplayGroup;
import org.apache.roller.weblogger.config.runtime.PropertyDef;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefs;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.business.jpa.JPAPersistenceStrategy;


/*
 * JPAPropertiesManagerImpl.java
 *
 * Created on May 29, 2006, 2:06 PM
 *
 */
public class JPAPropertiesManagerImpl implements PropertiesManager {
    
    /** The logger instance for this class. */
    private static Log log = LogFactory.getLog(
        JPAPropertiesManagerImpl.class);

    private JPAPersistenceStrategy strategy;
    
    /**
     * Creates a new instance of JPAPropertiesManagerImpl
     */
    public JPAPropertiesManagerImpl (
            JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Properties Manager");

        this.strategy = strategy;

        // TODO: and new method initialize(props)
        init();
    }

    /**
     * Retrieve a single property by name.
     */
    public RuntimeConfigProperty getProperty(String name) throws RollerException {
        return (RuntimeConfigProperty) strategy
            .load(RuntimeConfigProperty.class,name);
    }


    /**
     * Retrieve all properties.
     * 
     * Properties are returned in a Map to make them easy to lookup.  The Map
     * uses the property name as the key and the RuntimeConfigProperty object
     * as the value.
     */
    public Map getProperties() throws RollerException {

        HashMap props = new HashMap();
        List list = (List) strategy.getNamedQuery("RuntimeConfigProperty.getAll").getResultList();
        /*
         * for convenience sake we are going to put the list of props
         * into a map for users to access it.  The value element of the
         * hash still needs to be the RuntimeConfigProperty object so that
         * we can save the elements again after they have been updated
         */
        RuntimeConfigProperty prop = null;
        Iterator it = list.iterator();
        while(it.hasNext()) {
            prop = (RuntimeConfigProperty) it.next();
            props.put(prop.getName(), prop);
        }

        return props;
    }


    /**
     * Save a single property.
     */
    public void saveProperty(RuntimeConfigProperty property) 
            throws RollerException {
        this.strategy.store(property);
    }


    /**
     * Save all properties.
     */
    public void saveProperties(Map properties) throws RollerException {

        // just go through the list and saveProperties each property
        Iterator props = properties.values().iterator();
        while (props.hasNext()) {
            this.strategy.store((RuntimeConfigProperty) props.next());
        }
    }


    private void init() {
        Map props = null;
        try {
            props = this.getProperties();

            if(props.size() < 1) {
                // empty props table ... load defaults
                props = initializeMissingProps(props);
            } else {
                // found existing props ... check for new props
                props = initializeMissingProps(props);
            }

            // save our changes
            this.saveProperties(props);
        } catch (Exception e) {
            log.fatal("Failed to initialize runtime configuration properties."+
                    "Please check that the database has been upgraded!", e);
            throw new RuntimeException(e);
        }

    }

    /**
     * This method compares the property definitions in the RuntimeConfigDefs
     * file with the properties in the given Map and initializes any properties
     * that were not found in the Map.
     *
     * If the Map of props is empty/null then we will initialize all properties.
     **/
    private Map initializeMissingProps(Map props) {

        if(props == null)
            props = new HashMap();

        // start by getting our runtimeConfigDefs
        RuntimeConfigDefs runtimeConfigDefs =
                RollerRuntimeConfig.getRuntimeConfigDefs();

        // can't do initialization without our config defs
        if(runtimeConfigDefs == null)
            return props;

        // iterator through all the definitions and add properties
        // that are not already in our props map
        ConfigDef configDef = null;
        DisplayGroup dGroup = null;
        PropertyDef propDef = null;
        Iterator defs = runtimeConfigDefs.getConfigDefs().iterator();
        while(defs.hasNext()) {
            configDef = (ConfigDef) defs.next();

            Iterator groups = configDef.getDisplayGroups().iterator();
            while(groups.hasNext()) {
                dGroup = (DisplayGroup) groups.next();

                Iterator propdefs = dGroup.getPropertyDefs().iterator();
                while(propdefs.hasNext()) {
                    propDef = (PropertyDef) propdefs.next();

                    // do we already have this prop?  if not then add it
                    if(!props.containsKey(propDef.getName())) {
                        RuntimeConfigProperty newprop =
                            new RuntimeConfigProperty(
                                propDef.getName(), propDef.getDefaultValue());

                        props.put(propDef.getName(), newprop);

                        log.info("Found uninitialized property " +
                            propDef.getName() +
                            " ... setting value to [" + 
                            propDef.getDefaultValue() + "]");
                    }
                }
            }
        }

        return props;
    }


    public void release() {}

}
