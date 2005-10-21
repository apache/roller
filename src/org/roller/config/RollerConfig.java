/*
 * RollerConfig.java
 *
 */

package org.roller.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Properties;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.util.PropertyExpander;


/**
 * This is the single entry point for accessing configuration properties
 * in Roller.
 *
 * @author Allen Gilliland
 */
public class RollerConfig {

    private static String default_config = "/roller.properties";
    private static String custom_config = "/roller-custom.properties";
    private static String custom_jvm_param = "roller.custom.config";
    private static File custom_config_file = null;

    private static Properties mConfig;

    private static Log mLogger =
            LogFactory.getFactory().getInstance(RollerConfig.class);


    /*
    * Static block run once at class loading
    *
    * We load the default properties and any custom properties we find
    */
    static {
        mConfig = new Properties();

        try {
            // we'll need this to get at our properties files in the classpath
            Class config_class = Class.forName("org.roller.config.RollerConfig");

            // first, lets load our default properties
            InputStream is = config_class.getResourceAsStream(default_config);
            mConfig.load(is);
            mLogger.info("successfully loaded default properties.");

            // now, see if we can find our custom config
            is = config_class.getResourceAsStream(custom_config);
            if(is != null) {
                mConfig.load(is);
                mLogger.info("successfully loaded custom properties file from classpath");
            } else {
                mLogger.info("no custom properties file found in classpath");
            }

            // finally, check for an external config file
            String env_file = System.getProperty(custom_jvm_param);
            if(env_file != null && env_file.length() > 0) {
                custom_config_file = new File(env_file);

                // make sure the file exists, then try and load it
                if(custom_config_file != null && custom_config_file.exists()) {
                    is = new FileInputStream(custom_config_file);
                    mConfig.load(is);
                    mLogger.info("successfully loaded custom properties from "+
                            custom_config_file.getAbsolutePath());
                } else {
                    mLogger.warn("failed to load custom properties from "+
                            custom_config_file.getAbsolutePath());
                }

            } else {
                mLogger.info("no custom properties file specified via jvm option");
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
                        if (mLogger.isDebugEnabled()) {
                            mLogger.info("Expanded value of " + propName + " from '" +
                                initialValue + "' to '" + expandedValue + "'");
                        }
                    }
                }
            }

            // some debugging for those that want it
            if(mLogger.isDebugEnabled()) {
                mLogger.debug("RollerConfig looks like this ...");

                String key = null;
                Enumeration keys = mConfig.keys();
                while(keys.hasMoreElements()) {
                    key = (String) keys.nextElement();
                    mLogger.debug(key+"="+mConfig.getProperty(key));
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
     *
     * @param     key Name of the property
     * @return    String Value of property requested, null if not found
     **/
    public static String getProperty(String key) {
        mLogger.debug("Fetching property ["+key+"="+mConfig.getProperty(key)+"]");
        return mConfig.getProperty(key);
    }


    /**
     * Retrieve a property as a boolean ... defaults to false if there is an error
     **/
    public static boolean getBooleanProperty(String name) {

        // get the value first, then convert
        String value = RollerConfig.getProperty(name);

        if(value == null)
            return false;

        return (new Boolean(value)).booleanValue();
    }


    /**
     * Retrieve all property keys
     *
     * @return Enumeration A list of all keys
     **/
    public static Enumeration keys() {
        return mConfig.keys();
    }


    /**
     * Set the "uploads.dir" property at runtime.
     *
     * Properties are meant to be read-only, but we make this one exception
     * for now because we know that some people are still writing their
     * uploads to the webapp context and we can only get that path at runtime.
     */
    public static void setUploadsDir(String path) {

        // only do this if the user wants to use the webapp context
        if("${webapp.context}".equals(mConfig.getProperty("uploads.dir")))
            mConfig.setProperty("uploads.dir", path);
    }


    /**
     * Set the "context.realpath" property at runtime.
     *
     * Properties are meant to be read-only, but we make this one exception
     * for now because there are some classes which rely on having filesystem
     * access to files in the roller webapp context.
     *
     * This property is *not* persisted in any way.
     */
    public static void setContextPath(String path) {

        mConfig.setProperty("context.realpath", path);
    }

}
