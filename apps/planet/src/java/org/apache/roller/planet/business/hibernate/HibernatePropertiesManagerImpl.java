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

package org.apache.roller.planet.business.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.PlanetException;
import org.apache.roller.planet.business.AbstractManagerImpl;
import org.apache.roller.planet.business.InitializationException;
import org.apache.roller.planet.business.PropertiesManager;
import org.apache.roller.planet.config.PlanetRuntimeConfig;
import org.apache.roller.planet.config.runtime.ConfigDef;
import org.apache.roller.planet.config.runtime.DisplayGroup;
import org.apache.roller.planet.config.runtime.PropertyDef;
import org.apache.roller.planet.config.runtime.RuntimeConfigDefs;
import org.apache.roller.planet.pojos.RuntimeConfigProperty;


/**
 * Hibernate implementation of the PropertiesManager.
 */
@com.google.inject.Singleton
public class HibernatePropertiesManagerImpl extends AbstractManagerImpl
        implements PropertiesManager {
    
    private static Log log = LogFactory.getLog(HibernatePropertiesManagerImpl.class);
    
    private final HibernatePersistenceStrategy strategy;
    
    
    /**
     * Creates a new instance of HibernatePropertiesManagerImpl
     */
    @com.google.inject.Inject 
    protected HibernatePropertiesManagerImpl(HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate Properties Manager");
        
        this.strategy = strat;
    }
    
    
    /**
     * Retrieve a single property by name.
     */
    public RuntimeConfigProperty getProperty(String name) throws PlanetException {
        try {
            return (RuntimeConfigProperty) strategy.load(name, RuntimeConfigProperty.class);
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
    }
    
    
    /**
     * Retrieve all properties.
     * 
     * Properties are returned in a Map to make them easy to lookup.  The Map
     * uses the property name as the key and the RuntimeConfigProperty object
     * as the value.
     */
    public Map getProperties() throws PlanetException {
        
        HashMap props = new HashMap();
        
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(RuntimeConfigProperty.class);
            List list = criteria.list();
            
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
        } catch (HibernateException e) {
            throw new PlanetException(e);
        }
        
        return props;
    }
    
    
    /**
     * Save a single property.
     */
    public void saveProperty(RuntimeConfigProperty property) throws PlanetException {
        
        this.strategy.store(property);
    }
    
    
    /**
     * Save all properties.
     */
    public void saveProperties(Map properties) throws PlanetException {
        
        // just go through the list and saveProperties each property
        Iterator props = properties.values().iterator();
        while (props.hasNext()) {
            this.strategy.store((RuntimeConfigProperty) props.next());
        }
    }
    
    
    /**
     * @inheritDoc
     */
    @Override
    public void initialize() throws InitializationException {
        Map props = null;
        try {
            props = this.getProperties();
            
            // initialize any new props
            props = initializeMissingProps(props);
            
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
                PlanetRuntimeConfig.getRuntimeConfigDefs();
        
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
                                new RuntimeConfigProperty(propDef.getName(), propDef.getDefaultValue());
                        
                        props.put(propDef.getName(), newprop);
                        
                        log.info("Found uninitialized property "+propDef.getName()+
                                " ... setting value to ["+propDef.getDefaultValue()+"]");
                    }
                }
            }
        }
        
        return props;
    }
    
}