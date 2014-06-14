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

package org.apache.roller.planet.config;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefs;
import org.apache.roller.weblogger.config.runtime.RuntimeConfigDefsParser;
import org.apache.roller.weblogger.planet.ui.PlanetConfig;


/**
 * Planet specific runtime properties.
 */
public class PlanetRuntimeConfig {
    
    private static Log log = LogFactory.getLog(PlanetRuntimeConfig.class);
    
    private static String runtime_config = "/org/apache/roller/planet/config/planetRuntimeConfigDefs.xml";
    private static RuntimeConfigDefs configDefs = null;
    
    // special case for our context urls
    private static String relativeContextURL = null;
    private static String absoluteContextURL = null;
    
    
    // prevent instantiations
    private PlanetRuntimeConfig() {}
    
    
    /**
     * Retrieve a single property from the PropertiesManager ... returns null
     * if there is an error
     **/
    public static String getProperty(String name) {
        
        String value = null;
        
        try {
            PropertiesManager pmgr = WebloggerFactory.getWeblogger().getPropertiesManager();
            value = pmgr.getProperty(name).getValue();
        } catch(Exception e) {
            log.warn("Trouble accessing property: "+name, e);
        }
        
        log.debug("fetched property ["+name+"="+value+"]");

        return value;
    }
    
    
    /**
     * Retrieve a property as a boolean ... defaults to false if there is an error
     **/
    public static boolean getBooleanProperty(String name) {
        
        // get the value first, then convert
        String value = PlanetRuntimeConfig.getProperty(name);
        
        if(value == null)
            return false;
        
        return (new Boolean(value)).booleanValue();
    }
    
    
    /**
     * Retrieve a property as an int ... defaults to -1 if there is an error
     **/
    public static int getIntProperty(String name) {
        
        // get the value first, then convert
        String value = PlanetRuntimeConfig.getProperty(name);
        
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
                        PlanetRuntimeConfig.class.getResourceAsStream(runtime_config);
                
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
                    new InputStreamReader(PlanetConfig.class.getResourceAsStream(runtime_config));
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
    
}
