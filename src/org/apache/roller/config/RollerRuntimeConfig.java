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

package org.apache.roller.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.runtime.RuntimeConfigDefs;
import org.apache.roller.config.runtime.RuntimeConfigDefsParser;
import org.apache.roller.model.PropertiesManager;
import org.apache.roller.model.RollerFactory;


/**
 * This class acts as a convenience gateway for getting property values
 * via the PropertiesManager.  We do this because most calls to the
 * PropertiesManager are just to get the value of a specific property and
 * thus the caller doesn't need the full RollerPropertyData object.
 *
 * We also provide some methods for converting to different data types.
 */
public class RollerRuntimeConfig {
    
    private static Log log = LogFactory.getLog(RollerRuntimeConfig.class);
    
    private static String runtime_config = "/rollerRuntimeConfigDefs.xml";
    private static RuntimeConfigDefs configDefs = null;
    
    // for properties that we are storing locally which aren't persisted
    private static Properties localConfig = new Properties();
    
    
    // prevent instantiations
    private RollerRuntimeConfig() {}
    
    
    /**
     * Retrieve a single property from the PropertiesManager ... returns null
     * if there is an error
     **/
    public static String getProperty(String name) {
        
        String value = null;
        
        // try local config first
        value = localConfig.getProperty(name);
        
        // next try db
        // i don't like this special case for the absoluteurl, but right now
        // i can't seem to see a better way.  at some point we should come
        // back to this and see if there is a cleaner way to handle this  -- AG
        if(value == null || "site.absoluteurl".equals(name)) {
            try {
                PropertiesManager pmgr = RollerFactory.getRoller().getPropertiesManager();
                value = pmgr.getProperty(name).getValue();
            } catch(Exception e) {
                log.warn("Trouble accessing property: "+name, e);
            }
        }
        
        log.debug("fetched property ["+name+"="+value+"]");
        
        return value;
    }
    
    
    /**
     * Retrieve a property as a boolean ... defaults to false if there is an error
     **/
    public static boolean getBooleanProperty(String name) {
        
        // get the value first, then convert
        String value = RollerRuntimeConfig.getProperty(name);
        
        if(value == null)
            return false;
        
        return (new Boolean(value)).booleanValue();
    }
    
    
    /**
     * Retrieve a property as an int ... defaults to -1 if there is an error
     **/
    public static int getIntProperty(String name) {
        
        // get the value first, then convert
        String value = RollerRuntimeConfig.getProperty(name);
        
        if(value == null)
            return -1;
        
        int intval = -1;
        try {
            intval = Integer.parseInt(value);
        } catch(Exception e) {
            log.warn("Trouble converting to int: "+name, e);
        }
        
        return intval;
    }
    
    
    public static RuntimeConfigDefs getRuntimeConfigDefs() {
        
        if(configDefs == null) {
            
            // unmarshall the config defs file
            try {
                InputStream is = 
                        RollerRuntimeConfig.class.getResourceAsStream(runtime_config);
                
                RuntimeConfigDefsParser parser = new RuntimeConfigDefsParser();
                configDefs = parser.unmarshall(is);
                
            } catch(Exception e) {
                // error while parsing :(
                log.error("Error parsing runtime config defs", e);
            }
            
        }
        
        return configDefs;
    }
    
    
    /**
     * Get the runtime configuration definitions XML file as a string.
     *
     * This is basically a convenience method for accessing this file.
     * The file itself contains meta-data about what configuration
     * properties we change at runtime via the UI and how to setup
     * the display for editing those properties.
     */
    public static String getRuntimeConfigDefsAsString() {
        
        log.debug("Trying to load runtime config defs file");
        
        try {
            InputStreamReader reader =
                    new InputStreamReader(RollerConfig.class.getResourceAsStream(runtime_config));
            StringWriter configString = new StringWriter();
            
            char[] buf = new char[8196];
            int length = 0;
            while((length = reader.read(buf)) > 0)
                configString.write(buf, 0, length);
            
            reader.close();
            
            return configString.toString();
        } catch(Exception e) {
            log.error("Error loading runtime config defs file", e);
        }
        
        return "";
    }
    
    
    /**
     * Set the "site.absoluteurl" property locally.
     *
     * Local properties are a kind of bastardized attempt to maintain some
     * properties which can only be determined at runtime but which should
     * not be persisted.
     *
     * This property is *not* persisted in any way.
     */
    public static void setAbsoluteContextPath(String path) {
        localConfig.setProperty("site.absoluteurl", path);
    }
    
    
    /**
     * Set the "site.relativeurl" property locally.
     *
     * Local properties are a kind of bastardized attempt to maintain some
     * properties which can only be determined at runtime but which should
     * not be persisted.
     *
     * This property is *not* persisted in any way.
     */
    public static void setRelativeContextPath(String path) {
        localConfig.setProperty("site.relativeurl", path);
    }
    
}
