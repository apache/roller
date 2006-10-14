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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.PropertyExpander;


/**
 * This is the single entry point for accessing configuration properties in Roller.
 */
public class RollerConfig {
    
    private static String default_config = "/roller.properties";
    private static String custom_config = "/roller-custom.properties";
    private static String custom_jvm_param = "roller.custom.config";
    private static File custom_config_file = null;

    private static Properties mConfig;

    private static Log log = LogFactory.getLog(RollerConfig.class);
    
    
    /*
     * Static block run once at class loading
     *
     * We load the default properties and any custom properties we find
     */
    static {
        mConfig = new Properties();

        try {
            // we'll need this to get at our properties files in the classpath
            Class config_class = Class.forName("org.apache.roller.config.RollerConfig");

            // first, lets load our default properties
            InputStream is = config_class.getResourceAsStream(default_config);
            mConfig.load(is);
            log.info("successfully loaded default properties.");

            // now, see if we can find our custom config
            is = config_class.getResourceAsStream(custom_config);
            if(is != null) {
                mConfig.load(is);
                log.info("successfully loaded custom properties file from classpath");
            } else {
                log.info("no custom properties file found in classpath");
            }

            // finally, check for an external config file
            String env_file = System.getProperty(custom_jvm_param);
            if(env_file != null && env_file.length() > 0) {
                custom_config_file = new File(env_file);

                // make sure the file exists, then try and load it
                if(custom_config_file != null && custom_config_file.exists()) {
                    is = new FileInputStream(custom_config_file);
                    mConfig.load(is);
                    log.info("successfully loaded custom properties from "+
                            custom_config_file.getAbsolutePath());
                } else {
                    log.warn("failed to load custom properties from "+
                            custom_config_file.getAbsolutePath());
                }

            } else {
                log.info("no custom properties file specified via jvm option");
            }

            // Now expand system properties for properties in the config.expandedProperties list,
            // replacing them by their expanded values.
            String expandedPropertiesDef = (String) mConfig.get("config.expandedProperties");
            if (expandedPropertiesDef != null) {
                String[] expandedProperties = expandedPropertiesDef.split(",");
                for (int i = 0; i < expandedProperties.length; i++) {
                    String propName = expandedProperties[i].trim();
                    String initialValue = (String) mConfig.get(propName);
                    if (initialValue != null) {
                        String expandedValue = PropertyExpander.expandSystemProperties(initialValue);
                        mConfig.put(propName,expandedValue);
                        if (log.isDebugEnabled()) {
                            log.info("Expanded value of " + propName + " from '" +
                                initialValue + "' to '" + expandedValue + "'");
                        }
                    }
                }
            }

            // some debugging for those that want it
            if(log.isDebugEnabled()) {
                log.debug("RollerConfig looks like this ...");

                String key = null;
                Enumeration keys = mConfig.keys();
                while(keys.hasMoreElements()) {
                    key = (String) keys.nextElement();
                    log.debug(key+"="+mConfig.getProperty(key));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    // no, you may not instantiate this class :p
    private RollerConfig() {}


    /**
     * Retrieve a property value
     * @param     key Name of the property
     * @return    String Value of property requested, null if not found
     */
    public static String getProperty(String key) {
        log.debug("Fetching property ["+key+"="+mConfig.getProperty(key)+"]");
        return mConfig.getProperty(key);
    }
    
    /**
     * Retrieve a property value
     * @param     key Name of the property
     * @param     defaultValue Default value of property if not found     
     * @return    String Value of property requested or defaultValue
     */
    public static String getProperty(String key, String defaultValue) {
        log.debug("Fetching property ["+key+"="+mConfig.getProperty(key)+",defaultValue="+defaultValue+"]");
        String value = mConfig.getProperty(key);
        if(value == null)
          return defaultValue;
        
        return value;
    }

    /**
     * Retrieve a property as a boolean ... defaults to false if not present.
     */
    public static boolean getBooleanProperty(String name) {
        return getBooleanProperty(name,false);
    }

    /**
     * Retrieve a property as a boolean ... with specified default if not present.
     */
    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        // get the value first, then convert
        String value = RollerConfig.getProperty(name);

        if(value == null)
            return defaultValue;

        return (new Boolean(value)).booleanValue();
    }

    /**
     * Retrieve a property as an int ... defaults to 0 if not present.
     */
    public static int getIntProperty(String name) {
        return getIntProperty(name, 0);
    }

    /**
     * Retrieve a property as a int ... with specified default if not present.
     */
    public static int getIntProperty(String name, int defaultValue) {
        // get the value first, then convert
        String value = RollerConfig.getProperty(name);

        if (value == null)
            return defaultValue;

        return (new Integer(value)).intValue();
    }

    /**
     * Retrieve all property keys
     * @return Enumeration A list of all keys
     **/
    public static Enumeration keys() {
        return mConfig.keys();
    }
   

    /**
     * Set the "uploads.dir" property at runtime.
     * <p />
     * Properties are meant to be read-only, but we make this exception because  
     * we know that some people are still writing their uploads to the webapp  
     * context and we can only get that path at runtime (and for unit testing).
     * <p />
     * This property is *not* persisted in any way.
     */
    public static void setUploadsDir(String path) {
        // only do this if the user wants to use the webapp context
        if("${webapp.context}".equals(mConfig.getProperty("uploads.dir")))
            mConfig.setProperty("uploads.dir", path);
    }
    
    /**
     * Set the "themes.dir" property at runtime.
     * <p />
     * Properties are meant to be read-only, but we make this exception because  
     * we know that some people are still using their themes in the webapp  
     * context and we can only get that path at runtime (and for unit testing).
     * <p />
     * This property is *not* persisted in any way.
     */
    public static void setThemesDir(String path) {
        // only do this if the user wants to use the webapp context
        if("${webapp.context}".equals(mConfig.getProperty("themes.dir")))
            mConfig.setProperty("themes.dir", path);
    }
    
}
