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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.jpa;

import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.RuntimeConfigDefs;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.util.Blacklist;
import org.apache.roller.weblogger.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class implementation reads the application's runtime properties.  At installation
 * time, these properties are initially loaded from the runtimeConfigDefs.xml into the
 * properties database table, from where they are subsequently read during runtime.
 * In contrast to the static properties in WebloggerStaticConfig, these values may be changed
 * during runtime on the system administration page and take effect immediately.
 * <p>
 * This class also provides convenience methods for returning the String, int, or boolean
 * values of RuntimeConfigProperty objects.
 */
public class JPAPropertiesManagerImpl implements PropertiesManager {

    /**
     * The logger instance for this class.
     */
    private static Logger log = LoggerFactory.getLogger(JPAPersistenceStrategy.class);

    /**
     * List of valid runtime configuration properties that can be stored in the database.
     */
    private static RuntimeConfigDefs configDefs = null;

    private final JPAPersistenceStrategy strategy;

    private Blacklist siteBlacklist = null;

    /**
     * Creates a new instance of JPAPropertiesManagerImpl
     */
    protected JPAPropertiesManagerImpl(JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Properties Manager");
        this.strategy = strategy;
    }

    @Override
    public RuntimeConfigDefs getRuntimeConfigDefs() {
        if (configDefs == null) {
            try {
                configDefs = (RuntimeConfigDefs) Utilities.jaxbUnmarshall(
                        "/org/apache/roller/weblogger/config/runtimeConfigDefs.xsd",
                        "/org/apache/roller/weblogger/config/runtimeConfigDefs.xml",
                        false,
                        RuntimeConfigDefs.class);
            } catch (Exception e) {
                // error while parsing :(
                log.error("Error parsing runtime config defs", e);
            }
        }
        return configDefs;
    }

    @Override
    public void initialize() {
        Map<String, RuntimeConfigProperty> props;
        try {
            // retrieve properties from database
            props = getProperties();

            // if any default props missing from the properties DB table,
            // initialize them and save them to that table.
            initializeMissingProps(props);
            saveProperties(props);
            strategy.flush();
        } catch (Exception e) {
            log.error("Failed to initialize runtime configuration properties." +
                    "Please check that the database has been upgraded!", e);
            throw new RuntimeException(e);
        }

    }

    @Override
    public RuntimeConfigProperty getProperty(String name) {
        return strategy.load(RuntimeConfigProperty.class, name);
    }

    /**
     * Retrieve all properties.
     * <p>
     * Properties are returned in a Map to make them easy to lookup.  The Map
     * uses the property name as the key and the RuntimeConfigProperty object
     * as the value.
     */
    @Override
    public Map<String, RuntimeConfigProperty> getProperties() {

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

    @Override
    public void saveProperty(RuntimeConfigProperty property) {
        this.strategy.store(property);
    }

    @Override
    public void saveProperties(Map properties) {
        // just go through the list and saveProperties each property
        for (Object prop : properties.values()) {
            this.strategy.store(prop);
        }
        siteBlacklist = new Blacklist(getStringProperty("spam.blacklist"), null);
    }

    /**
     * This method compares the property definitions in the RuntimeConfigDefs
     * file with the properties in the given Map and initializes any properties
     * that were not found in the Map.
     * <p>
     * If the Map of props is empty/null then we will initialize all properties.
     **/
    private Map initializeMissingProps(Map<String, RuntimeConfigProperty> props) {

        if (props == null) {
            props = new HashMap<>();
        }

        // start by getting our runtimeConfigDefs
        RuntimeConfigDefs runtimeConfigDefs = getRuntimeConfigDefs();

        // can't do initialization without our config defs
        if (runtimeConfigDefs == null) {
            return props;
        }

        // iterate through all the definitions and add properties
        // that are not already in our props map

        log.info("Checking for new properties to add to the weblogger_properties database table...");
        for (RuntimeConfigDefs.ConfigGroup dGroup : runtimeConfigDefs.getConfigGroups()) {
            for (RuntimeConfigDefs.PropertyDef propDef : dGroup.getPropertyDefs()) {

                // do we already have this prop?  if not then add it
                if (!props.containsKey(propDef.getName())) {
                    RuntimeConfigProperty newprop =
                            new RuntimeConfigProperty(
                                    propDef.getName(), propDef.getDefaultValue());

                    props.put(propDef.getName(), newprop);

                    log.info("Adding new property {} = {}", propDef.getName(), propDef.getDefaultValue());
                }
            }
        }

        return props;
    }

    /**
     * Retrieve a single property from the PropertiesManager ... returns null
     * if there is an error
     **/
    @Override
    public String getStringProperty(String name) {
        String value = null;
        try {
            RuntimeConfigProperty prop = getProperty(name);
            if (prop != null) {
                value = prop.getValue();
            }
        } catch (Exception e) {
            log.warn("Trouble accessing property: {}", name, e);
        }
        log.debug("fetched property [{}={}]", name, value);
        return value;
    }

    /**
     * Retrieve a property as a boolean ... defaults to false if there is an error
     **/
    @Override
    public boolean getBooleanProperty(String name) {
        // get the value first, then convert
        String value = getStringProperty(name);
        if (value == null) {
            return false;
        }
        return Boolean.valueOf(value);
    }

    /**
     * Retrieve a property as an int ... defaults to -1 if there is an error
     **/
    @Override
    public int getIntProperty(String name) {
        // get the value first, then convert
        String value = getStringProperty(name);
        if (value == null || value.isEmpty()) {
            return -1;
        }
        int intval = -1;
        try {
            intval = Integer.parseInt(value);
        } catch (Exception e) {
            log.warn("Trouble converting to integer: {}", name, e);
        }
        return intval;
    }

    @Override
    public Blacklist getSiteBlacklist() {
        return siteBlacklist;
    }

}
