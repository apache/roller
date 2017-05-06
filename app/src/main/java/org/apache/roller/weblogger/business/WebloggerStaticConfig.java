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
package org.apache.roller.weblogger.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Class for accessing static configuration properties, those in tightblog.properties
 * and its tightblog-custom.properties override file. These properties are
 * read only at application startup, are not stored in any database table and
 * require an application restart in order to read any changed values.
 */
public final class WebloggerStaticConfig {

    private static Logger log = LoggerFactory.getLogger(WebloggerStaticConfig.class);

    private static String defaultConfig = "/tightblog.properties";
    private static String customConfig = "/tightblog-custom.properties";
    private static String customJvmParam = "tightblog.custom.config";
    private static File customConfigFile = null;

    private static Properties config;

    // special case for our context urls
    private static String relativeContextURL = null;
    private static String absoluteContextURL = null;

    // no, you may not instantiate this class :p
    private WebloggerStaticConfig() {
    }

    // enum constant for properties file-configured authentication option (Database username/passwords or LDAP).
    public enum AuthMethod {
        DB,
        LDAP
    }

    /*
     * Static block run once at class loading
     *
     * We load the default properties and any custom properties we find
     */
    static {
        config = new Properties();

        try {
            // we'll need this to get at our properties files in the classpath
            Class configClass = Class.forName("org.apache.roller.weblogger.business.WebloggerStaticConfig");

            // first, lets load our default properties
            InputStream is = configClass.getResourceAsStream(defaultConfig);
            config.load(is);

            // now, see if we can find our custom config
            is = configClass.getResourceAsStream(customConfig);

            if (is != null) {
                config.load(is);
                System.out.println("TightBlog Weblogger: Successfully loaded custom properties file from classpath");
                System.out.println("File path : " + configClass.getResource(customConfig).getFile());
            } else {
                System.out.println("TightBlog Weblogger: No custom properties file found in classpath");
            }

            // finally, check for an external config file
            String envFile = System.getProperty(customJvmParam);
            if (envFile != null && envFile.length() > 0) {
                customConfigFile = new File(envFile);

                // make sure the file exists, then try and load it
                if (customConfigFile.exists()) {
                    is = new FileInputStream(customConfigFile);
                    config.load(is);
                    System.out.println("WebloggerStaticConfig: Successfully loaded custom properties from " + customConfigFile.getAbsolutePath());
                } else {
                    System.out.println("WebloggerStaticConfig: Failed to load custom properties from " + customConfigFile.getAbsolutePath());
                }

            }

            // some debugging for those that want it
            if (log.isDebugEnabled()) {
                log.debug("WebloggerStaticConfig looks like this ...");

                String key;
                Enumeration keys = config.keys();
                while (keys.hasMoreElements()) {
                    key = (String) keys.nextElement();
                    log.debug(key + " = {}", config.getProperty(key));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Retrieve a property value
     *
     * @param key Name of the property
     * @return String Value of property requested, null if not found
     */
    public static String getProperty(String key) {
        log.debug("Fetching property [{} = {}]", key, config.getProperty(key));
        String value = config.getProperty(key);
        return value == null ? null : value.trim();
    }

    /**
     * Retrieve a property value
     *
     * @param key          Name of the property
     * @param defaultValue Default value of property if not found
     * @return String Value of property requested or defaultValue
     */
    public static String getProperty(String key, String defaultValue) {
        log.debug("Fetching property [{} = {}], default value = {}", key, config.getProperty(key), defaultValue);
        String value = config.getProperty(key);
        if (value == null) {
            return defaultValue;
        }

        return value.trim();
    }

    /**
     * Retrieve a property as a boolean ... defaults to false if not present.
     */
    public static boolean getBooleanProperty(String name) {
        return getBooleanProperty(name, false);
    }

    /**
     * Retrieve a property as a boolean ... with specified default if not present.
     */
    public static boolean getBooleanProperty(String name, boolean defaultValue) {
        // get the value first, then convert
        String value = WebloggerStaticConfig.getProperty(name);

        if (value == null) {
            return defaultValue;
        }

        return Boolean.valueOf(value);
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
        String value = WebloggerStaticConfig.getProperty(name);

        if (value == null) {
            return defaultValue;
        }

        return Integer.valueOf(value);
    }

    /**
     * Special method which sets the non-persisted absolute url to this site.
     * <p>
     * This property is *not* persisted in any way.
     */
    public static void setAbsoluteContextURL(String url) {
        absoluteContextURL = url;
    }

    /**
     * Get the absolute url to this site.
     * <p>
     * This method will just return the value of the "site.absoluteurl"
     * property if it is set, otherwise it will return the non-persisted
     * value which is set by the InitFilter.
     */
    public static String getAbsoluteContextURL() {
        // db prop takes priority if it exists
        String absURL = getProperty("site.absoluteurl");
        if (absURL != null && absURL.trim().length() > 0) {
            return absURL;
        }
        return absoluteContextURL;
    }

    /**
     * Special method which sets the non-persisted relative url to this site.
     * <p>
     * This property is *not* persisted in any way.
     */
    public static void setRelativeContextURL(String url) {
        relativeContextURL = url;
    }

    public static String getRelativeContextURL() {
        return relativeContextURL;
    }

    /**
     * Retrieve all property keys
     *
     * @return Enumeration A list of all keys
     **/
    public static Enumeration keys() {
        return config.keys();
    }

    /**
     * Return the value of the authentication.method property as an AuthMethod
     * enum value.  Matching is done by checking the propertyName of each AuthMethod
     * enum object.
     * <p/>
     *
     * @throws IllegalArgumentException if property value defined in the properties
     *                                  file is missing or not the property name of any AuthMethod enum object.
     */
    public static AuthMethod getAuthMethod() {
        return AuthMethod.valueOf(getProperty("authentication.method", "DB"));
    }

}
