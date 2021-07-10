
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
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.config.runtime.ConfigDef;
import org.apache.roller.weblogger.config.runtime.DisplayGroup;
import org.apache.roller.weblogger.config.runtime.PropertyDef;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefs;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;


/*
 * JPAPropertiesManagerImpl.java
 *
 * Created on May 29, 2006, 2:06 PM
 *
 */
@com.google.inject.Singleton
public class JPAPropertiesManagerImpl implements PropertiesManager {
    
    /** The logger instance for this class. */
    private static Log log = LogFactory.getLog(
        JPAPropertiesManagerImpl.class);

    private final JPAPersistenceStrategy strategy;
    
    
    /**
     * Creates a new instance of JPAPropertiesManagerImpl
     */
    @com.google.inject.Inject
    protected JPAPropertiesManagerImpl(JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Properties Manager");
        this.strategy = strategy;
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public void initialize() throws InitializationException {
        
        Map<String, RuntimeConfigProperty> props;
        try {
            // retrieve properties from database
            props = this.getProperties();

            // if any default props missing from the properties DB table,
            // initialize them and save them to that table.
            initializeMissingProps(props);
            this.saveProperties(props);

        } catch (Exception e) {
            log.fatal("Failed to initialize runtime configuration properties."+
                    "Please check that the database has been upgraded!", e);
            throw new RuntimeException(e);
        }
        
    }
    
    
    /**
     * Retrieve a single property by name.
     */
    @Override
    public RuntimeConfigProperty getProperty(String name) throws WebloggerException {
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
    @Override
    public Map<String, RuntimeConfigProperty> getProperties() throws WebloggerException {

        HashMap<String, RuntimeConfigProperty> props = new HashMap<>();
        List<RuntimeConfigProperty> list = strategy.getNamedQuery("RuntimeConfigProperty.getAll",
                RuntimeConfigProperty.class).getResultList();
        /*
         * for convenience sake we are going to put the list of props
         * into a map for users to access it.  The value element of the
         * hash still needs to be the RuntimeConfigProperty object so that
         * we can save the elements again after they have been updated
         */
        for (RuntimeConfigProperty prop : list) {
            props.put(prop.getName(), prop);
        }
        return props;
    }


    /**
     * Save a single property.
     */
    @Override
    public void saveProperty(RuntimeConfigProperty property) 
            throws WebloggerException {
        this.strategy.store(property);
    }


    /**
     * Save all properties.
     */
    @Override
    public void saveProperties(Map<String, RuntimeConfigProperty> properties) throws WebloggerException {

        // just go through the list and saveProperties each property
        for (Object prop : properties.values()) {
            this.strategy.store(prop);
        }
    }
    

    /**
     * This method compares the property definitions in the RuntimeConfigDefs
     * file with the properties in the given Map and initializes any properties
     * that were not found in the Map.
     *
     * If the Map of props is empty/null then we will initialize all properties.
     **/
    private Map<String, RuntimeConfigProperty> initializeMissingProps(Map<String, RuntimeConfigProperty> props) {

        if(props == null) {
            props = new HashMap<>();
        }

        // start by getting our runtimeConfigDefs
        RuntimeConfigDefs runtimeConfigDefs =
                WebloggerRuntimeConfig.getRuntimeConfigDefs();

        // can't do initialization without our config defs
        if(runtimeConfigDefs == null) {
            return props;
        }

        // iterate through all the definitions and add properties
        // that are not already in our props map

        for (ConfigDef configDef : runtimeConfigDefs.getConfigDefs()) {
            for (DisplayGroup dGroup : configDef.getDisplayGroups()) {
                for (PropertyDef propDef : dGroup.getPropertyDefs()) {

                    // do we already have this prop?  if not then add it
                    if(!props.containsKey(propDef.getName())) {
                        RuntimeConfigProperty newprop =
                                new RuntimeConfigProperty(
                                        propDef.getName(), propDef.getDefaultValue());

                        props.put(propDef.getName(), newprop);

                        log.info("Property " + propDef.getName() +
                            " not yet in roller_properties database table, will store with " +
                            "default value of [" + propDef.getDefaultValue() + "`]");
                    }
                }
            }
        }

        return props;
    }


    @Override
    public void release() {}

}
