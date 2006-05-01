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
/*
 * HibernatePropertiesManagerImpl.java
 *
 * Created on April 21, 2005, 10:40 AM
 */

package org.apache.roller.business.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.config.runtime.ConfigDef;
import org.apache.roller.config.runtime.DisplayGroup;
import org.apache.roller.config.runtime.PropertyDef;
import org.apache.roller.config.runtime.RuntimeConfigDefs;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.RollerConfigData;
import org.apache.roller.pojos.RollerPropertyData;


/**
 * Hibernate implementation of the PropertiesManager.
 */
public class HibernatePropertiesManagerImpl implements PropertiesManager {
    
    static final long serialVersionUID = -4326713177137796936L;
    
    private static Log log = LogFactory.getLog(HibernatePropertiesManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    
    /** 
     * Creates a new instance of HibernatePropertiesManagerImpl
     */
    public HibernatePropertiesManagerImpl(HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate Properties Manager");
        
        this.strategy = strat;
        
        // TODO: and new method initialize(props)
        init();
    }
    
    
    /** 
     * Retrieve a single property by name.
     */
    public RollerPropertyData getProperty(String name) throws RollerException {
        try {
            return (RollerPropertyData) strategy.load(name, RollerPropertyData.class);
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    /** 
     * Retrieve all properties.
     *
     * Properties are returned in a Map to make them easy to lookup.  The Map
     * uses the property name as the key and the RollerPropertyData object
     * as the value.
     */
    public Map getProperties() throws RollerException {
        
        HashMap props = new HashMap();
        
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(RollerPropertyData.class);
            List list = criteria.list();
            
            /* 
             * for convenience sake we are going to put the list of props
             * into a map for users to access it.  The value element of the
             * hash still needs to be the RollerPropertyData object so that
             * we can save the elements again after they have been updated
             */
            RollerPropertyData prop = null;
            Iterator it = list.iterator();
            while(it.hasNext()) {
                prop = (RollerPropertyData) it.next();
                props.put(prop.getName(), prop);
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        
        return props;
    }
    
    
    /**
     * Save a single property.
     */
    public void saveProperty(RollerPropertyData property) throws RollerException {
        
        this.strategy.store(property);
    }

    
    /**
     * Save all properties.
     */
    public void saveProperties(Map properties) throws RollerException {
        
        // just go through the list and saveProperties each property
        Iterator props = properties.values().iterator();
        while (props.hasNext()) {
            this.strategy.store((RollerPropertyData) props.next());
        }
    }

    
    private void init() {
        Map props = null;
        try {
            props = this.getProperties();
            
            if(props.size() < 1) {
                // empty props table ... try migrating, then load defaults
                props = migrateOldRollerConfig(props);
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
     * Migrate data from the old roller config.
     * This is called only if the existing runtime properties are empty.
     */
    private Map migrateOldRollerConfig(Map props) {
        // try to get the old config
        Roller roller = RollerFactory.getRoller();
        RollerConfigData rollerConfig = null;
        
        try {
            rollerConfig = roller.getConfigManager().getRollerConfig();
        } catch (Exception e) {
            // We currently treat any exception obtaining the roller config
            // as if we had not found it.
            log.error(e);
        }
        
        if (rollerConfig != null) {
            log.info("Found old roller config ... doing migration to new runtime properties.");
            // copy over data
            props.put("site.name",
                    new RollerPropertyData("site.name", rollerConfig.getSiteName()));
            props.put("site.description",
                    new RollerPropertyData("site.description", rollerConfig.getSiteDescription()));
            props.put("site.adminemail",
                    new RollerPropertyData("site.adminemail", rollerConfig.getEmailAddress()));
            props.put("site.absoluteurl",
                    new RollerPropertyData("site.absoluteurl", rollerConfig.getAbsoluteURL()));
            props.put("site.linkbacks.enabled",
                    new RollerPropertyData("site.linkbacks.enabled", rollerConfig.getEnableLinkback().toString()));
            props.put("users.registration.enabled",
                    new RollerPropertyData("users.registration.enabled", rollerConfig.getNewUserAllowed().toString()));
            props.put("users.themes.path",
                    new RollerPropertyData("users.themes.path", rollerConfig.getUserThemes()));
            props.put("users.editor.pages",
                    new RollerPropertyData("users.editor.pages", rollerConfig.getEditorPages()));
            props.put("users.comments.enabled",
                    new RollerPropertyData("users.comments.enabled", "true"));
            props.put("users.comments.autoformat",
                    new RollerPropertyData("users.comments.autoformat", rollerConfig.getAutoformatComments().toString()));
            props.put("users.comments.escapehtml",
                    new RollerPropertyData("users.comments.escapehtml", rollerConfig.getEscapeCommentHtml().toString()));
            props.put("users.comments.emailnotify",
                    new RollerPropertyData("users.comments.emailnotify", rollerConfig.getEmailComments().toString()));
            props.put("uploads.enabled",
                    new RollerPropertyData("uploads.enabled", rollerConfig.getUploadEnabled().toString()));
            props.put("uploads.types.allowed",
                    new RollerPropertyData("uploads.types.allowed", rollerConfig.getUploadAllow()));
            props.put("uploads.types.forbid",
                    new RollerPropertyData("uploads.types.forbid", rollerConfig.getUploadForbid()));
            props.put("uploads.file.maxsize",
                    new RollerPropertyData("uploads.file.maxsize", rollerConfig.getUploadMaxFileMB().toString()));
            props.put("uploads.dir.maxsize",
                    new RollerPropertyData("uploads.dir.maxsize", rollerConfig.getUploadMaxDirMB().toString()));
            /* no longer part of runtime config
            props.put("aggregator.enabled",
                new RollerPropertyData("aggregator.enabled", rollerConfig.getEnableAggregator().toString()));
            props.put("aggregator.cache.enabled",
                new RollerPropertyData("aggregator.cache.enabled", rollerConfig.getRssUseCache().toString()));
            props.put("aggregator.cache.timeout",
                new RollerPropertyData("aggregator.cache.timeout", rollerConfig.getRssCacheTime().toString()));
            props.put("debug.memory.enabled",
                new RollerPropertyData("debug.memory.enabled", rollerConfig.getMemDebug().toString()));
             */
            props.put("spam.blacklist",
                    new RollerPropertyData("spam.blacklist", rollerConfig.getRefererSpamWords()));
        } else {
            log.info("Old roller config not found ... default values will be loaded");
        }
        
        return props;
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
                        RollerPropertyData newprop =
                                new RollerPropertyData(propDef.getName(), propDef.getDefaultValue());
                        
                        props.put(propDef.getName(), newprop);
                        
                        log.info("Found uninitialized property "+propDef.getName()+
                                " ... setting value to ["+propDef.getDefaultValue()+"]");
                    }
                }
            }
        }
        
        return props;
    }
    
    
    public void release() {}
    
}
