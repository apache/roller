/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.pings.PingTargetManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.PingTarget;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This may need to move to a different package, but it seems appropriate here in the current structure.
// Previous placement in the presentation.pings package introduced the undesirable dependency of the
// business package on the presentation package.

/**
 * Thin wrapper around RollerConfig and RollerRuntimeConfig for centralizing access to the many configurable
 * settings for pings.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
public class PingConfig {
    private static final Log logger = LogFactory.getLog(PingConfig.class);


    // Config property for maximim ping attempts.
    static final String MAX_PING_ATTEMPTS_PROP = "pings.maxPingAttempts";
    private static final int MAX_PING_ATTEMPTS_DEFAULT = 3;
    private static final int MAX_PING_ATTEMPTS_MIN = 1;
    private static final int MAX_PING_ATTEMPTS_MAX = 10;

    // Config property for queue processing interval
    private static final String QUEUE_PROCESSING_INTERVAL_PROP = "pings.queueProcessingIntervalMins";
    private static final int QUEUE_PROCESSING_INTERVAL_DEFAULT = 5;
    private static final int QUEUE_PROCESSING_INTERVAL_MIN = 0;
    private static final int QUEUE_PROCESSING_INTERVAL_MAX = 120;

    // PingConfig property for logging pings (not actually performing them).  Used for debugging.
    private static final String PINGS_LOG_ONLY_PROP = "pings.logOnly";
    private static final boolean PINGS_LOG_ONLY_DEFAULT = false;

    // PingConfig property for controlling whether or not to allow custom ping targets
    // ("Weblog:Custom Ping Targets" page and actions).  If absent, this defaults to false.
    // with the enabledProperty behavior in editor-menu.xml.
    // NOTE: If this property name is changed, editor-menu.xml must also be adjusted.
    private static final String PINGS_DISALLOW_CUSTOM_TARGETS_PROP = "pings.disallowCustomTargets";
    private static final boolean PINGS_DISALLOW_CUSTOM_TARGETS_DEFAULT = false;

    // PingConfig property for controlling whether or not to allow usage of pings
    // ("Weblog:Pings" page and actions).  If absent, this defaults to false
    // NOTE: If this property name is changed, editor-menu.xml must also be adjusted.
    private static final String PINGS_DISABLE_PING_USAGE_PROP = "pings.disablePingUsage";
    private static final boolean PINGS_DISABLE_PING_USAGE_DEFAULT = false;

    // PingConfig property for controlling suspending the processing of pings.  If true,
    // new auto ping requests are not queued, any existing queued requests are not processed,
    // and sending a manual ping results in a  message saying pings have been disabled.
    // NOTE: This is a "runtime" property settable on the Admin:PingConfig page, default is false.
    private static final String PINGS_SUSPEND_PING_PROCESSING_PROP = "pings.suspendPingProcessing";

    // PingConfig property determining the initial common ping targets.  If the list of common
    // ping targets is empty on startup, the value of this property is used to populate initial values.
    // The value takes the form of comma-separated ping targets where each ping target is specified in
    // the form {{name}{pingurl}}.  If an administrator wants to disable this initialization, in order to
    // maintain an empty list of common targets, the administrator can disable the initialization by
    // commenting out this property in the config file.
    private static final String PINGS_INITIAL_COMMON_TARGETS_PROP = "pings.initialCommonTargets";


    // PingConfig property determining the known WeblogUpdates.ping variants/bugs
    // in popular ping targets, which we are used when invoking pings on those targets.
    // The value takes the form of a comma separated list of ping target urls and
    // variant options, where each one is in the form {{pingurl}{option[[,option]...]}}.
    private static final String PINGS_VARIANT_OPTIONS_PROP = "pings.variantOptions";
    // Map of configured ping variants.  Maps a ping target hostname to a set of
    // Strings representing variant options to be used when pinging this target.
    // This was introduced in order to support certain buggy (but popular) ping
    // targets that implement minor variants of the WeblogUpdates.ping call.
    // This is initialized once at startup, and referenced when pings are made.
    private static final Map configuredVariants = new HashMap();
    
    
    static {
        try {
            // Initialize common targets from the configuration
            initializeCommonTargets();
            // Initialize ping variants
            initializePingVariants();
            // Remove custom ping targets if they have been disallowed
            if (getDisallowCustomTargets()) {
                logger.info("Custom ping targets have been disallowed.  Removing any existing custom targets.");
                RollerFactory.getRoller().getPingTargetManager().removeAllCustomPingTargets();
            }
            // Remove all autoping configurations if ping usage has been disabled.
            if (PingConfig.getDisablePingUsage()) {
                logger.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
                RollerFactory.getRoller().getAutopingManager().removeAllAutoPings();
            }
        } catch (RollerException e) {
            logger.error("ERROR configing ping managers", e);
        }
    }


    // Inhibit construction
    private PingConfig() {
    }

    /**
     * Get the maximum number of ping attempts that should be made for each ping queue entry before we give up. If we
     * get apparently transient failures while trying to perform the ping, the entry is requeued for processing on later
     * passes through the queue until this number of attempts has been reached.
     *
     * @return the configured (or default) maximum number of ping attempts
     */
    public static int getMaxPingAttempts() {
        return getIntegerProperty(MAX_PING_ATTEMPTS_PROP, MAX_PING_ATTEMPTS_DEFAULT, MAX_PING_ATTEMPTS_MIN, MAX_PING_ATTEMPTS_MAX);
    }

    /**
     * Get the ping queue processing interval in minutes.
     *
     * @return the configured (or default) queue processing interval in minutes.
     */
    public static int getQueueProcessingIntervalMins() {
        return getIntegerProperty(QUEUE_PROCESSING_INTERVAL_PROP, QUEUE_PROCESSING_INTERVAL_DEFAULT, QUEUE_PROCESSING_INTERVAL_MIN, QUEUE_PROCESSING_INTERVAL_MAX);
    }


    /**
     * Get the logs only setting.  Get configuration value determining whether pings are to be logged only (not sent).
     * This configuration setting is used for development and debugging.
     *
     * @return the configured (or default) value of the logs only setting.
     */
    public static boolean getLogPingsOnly() {
        return getBooleanProperty(PINGS_LOG_ONLY_PROP, PINGS_LOG_ONLY_DEFAULT);
    }

    /**
     * Determine whether the configuration disallows custom ping targets.  If this is true, users are not allowed to
     * create or edit custom ping targets, and any auto ping configs that use them are ignored.
     *
     * @return the configured (or default) value of the "disallow custom targets" setting.
     */
    public static boolean getDisallowCustomTargets() {
        return getBooleanProperty(PINGS_DISALLOW_CUSTOM_TARGETS_PROP, PINGS_DISALLOW_CUSTOM_TARGETS_DEFAULT);
    }

    /**
     * Determine whether the configuration disables ping usage (configuration of auto pings and sending of manual
     * pings).  If this is true, all auto ping configus are removed at startup, the Weblog:Pings UI and the associated
     * actions are disabled.
     *
     * @return the configured (or default) value of the enable ping usage setting.
     */
    public static boolean getDisablePingUsage() {
        return getBooleanProperty(PINGS_DISABLE_PING_USAGE_PROP, PINGS_DISABLE_PING_USAGE_DEFAULT);
    }

    /**
     * Determine whether ping processing is suspended.  If this is true, new auto ping requests are not
     * queued, any existing queued requests are not processed, and sending a manual ping results in a message saying
     * pings have been disabled.
     *
     * @return the configured (or default) value of the suspend ping processing setting.
     */
    public static boolean getSuspendPingProcessing() {
        return RollerRuntimeConfig.getBooleanProperty(PINGS_SUSPEND_PING_PROCESSING_PROP);
    }

    // Pattern used to parse common ping targets as well as ping variants.
    // Each initial commmon ping target is specified in the format {{name}{url}}
    // Ping variants are also specified in a nested brace format {{url}{options}}
    private static final Pattern NESTED_BRACE_PAIR = Pattern.compile("\\{\\{(.*?)\\}\\{(.*?)\\}\\}");

    /**
     * Initialize the common ping targets from the configuration properties. If the current list of common ping targets
     * is empty, and the <code>PINGS_INITIAL_COMMON_TARGETS_PROP</code> property is present in the configuration then,
     * this method will use that value to initialize the common targets.  This is called on each server startup.
     * <p/>
     * Note: this is expected to be called during initialization  with transaction demarcation being handled by the
     * caller.
     *
     * @see org.apache.roller.ui.core.RollerContext#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public static void initializeCommonTargets() throws RollerException {
        String configuredVal = RollerConfig.getProperty(PINGS_INITIAL_COMMON_TARGETS_PROP);
        if (configuredVal == null || configuredVal.trim().length() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No (or empty) value of " + PINGS_INITIAL_COMMON_TARGETS_PROP + " present in the configuration.  Skipping initialization of commmon targets.");
            }
            return;
        }
        PingTargetManager pingTargetMgr = RollerFactory.getRoller().getPingTargetManager();
        if (!pingTargetMgr.getCommonPingTargets().isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Some common ping targets are present in the database already.  Skipping initialization.");
            }
            return;
        }

        String[] configuredTargets = configuredVal.trim().split(",");
        for (int i = 0; i < configuredTargets.length; i++) {
            // Trim space around the target spec
            String thisTarget = configuredTargets[i].trim();
            // skip empty ones
            if (thisTarget.length() == 0) continue;
            // parse the ith target and store it
            Matcher m = NESTED_BRACE_PAIR.matcher(thisTarget);
            if (m.matches() && m.groupCount() == 2) {
                String name = m.group(1).trim();
                String url = m.group(2).trim();
                logger.info("Creating common ping target '" + name + "' from configuration properties.");
                PingTarget pingTarget = new PingTarget(null, name, url, null, false);
                pingTargetMgr.savePingTarget(pingTarget);
            } else {
                logger.error("Unable to parse configured initial ping target '" + thisTarget + "'. Skipping this target. Check your setting of the property " + PINGS_INITIAL_COMMON_TARGETS_PROP);
            }
        }
    }

    /**
     * Initialize known ping variants from the configuration.
     */
    public static void initializePingVariants() {
        String configuredVal = RollerConfig.getProperty(PINGS_VARIANT_OPTIONS_PROP);
        if (configuredVal == null || configuredVal.trim().length() == 0) {
            if (logger.isDebugEnabled()) {
                logger.debug("No (or empty) value of " + PINGS_VARIANT_OPTIONS_PROP + " present in the configuration.  Skipping initialization of ping variants.");
            }
            return;
        }
        String[] variants = configuredVal.trim().split(",");
        for (int i = 0; i < variants.length; i++) {
            String thisVariant = variants[i].trim();
            if (thisVariant.length() == 0) continue;
            Matcher m = NESTED_BRACE_PAIR.matcher(thisVariant);
            if (m.matches() && m.groupCount() == 2) {
                String url = m.group(1).trim();
                String optionsList = m.group(2).trim();
                Set variantOptions = new HashSet();
                String[] options = optionsList.split(",");
                for (int j = 0; j < options.length; j++) {
                    String option = options[j].trim().toLowerCase();
                    if (option.length() > 0) {
                        variantOptions.add(option);
                    }
                }
                if (!variantOptions.isEmpty()) {
                    configuredVariants.put(url, variantOptions);
                } else {
                    logger.warn("Ping variant entry for url '" + url + "' has an empty variant options list.  Ignored.");
                }
            } else {
                logger.error("Unable to parse configured ping variant '" + thisVariant + "'. Skipping this variant. Check your setting of the property " + PINGS_VARIANT_OPTIONS_PROP);
            }
        }
    }

    /**
     * Get the set of variant options configured for the given ping target url.
     *
     * @param pingTargetUrl
     * @return the set of variant options configured for the given ping target url, or
     *         the empty set if there are no variants configured.
     */
    public static Set getVariantOptions(String pingTargetUrl) {
        Set variantOptions = (Set) configuredVariants.get(pingTargetUrl);
        if (variantOptions == null) {
            variantOptions = Collections.EMPTY_SET;
        }
        return variantOptions;
    }


    // TODO: Refactor functionality below to RollerConfig?

    /**
     * Get the value of an integer configuration property.
     *
     * @param propName     the property name
     * @param defaultValue the default value if the property is not present
     * @param min          the minimum allowed value
     * @param max          the maximum allowed value
     * @return the value as an integer; the default value if no configured value is present or if the configured value
     *         is out of the specified range.
     */
    private static int getIntegerProperty(String propName, int defaultValue, int min, int max) {
        String configuredVal = RollerConfig.getProperty(propName);
        if (configuredVal == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("PingConfig property '" + propName + "' is not present in the configuration.  Using default value: " + defaultValue);
            }
            return defaultValue;
        }

        int val;
        try {
            val = Integer.parseInt(configuredVal);
        } catch (NumberFormatException ex) {
            logger.error("ERROR: PingConfig property '" + propName + "' is not an integer value.  Using default value: " + defaultValue);
            return defaultValue;
        }

        if (val < min || val > max) {
            logger.error("ERROR: PingConfig property '" + propName + "' is outside the required range (" + min + ", " + max + ").  Using default value: " + defaultValue);
            return defaultValue;
        }

        return val;
    }

    /**
     * Get the value of a boolean property with specified default.
     *
     * @param propName     the property name
     * @param defaultValue the default value if the property is not present
     * @return the configured value or the default if it the configured value is not present.
     */
    private static boolean getBooleanProperty(String propName, boolean defaultValue) {
        String configuredVal = RollerConfig.getProperty(propName);
        if (configuredVal == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("PingConfig property '" + propName + "' is not present in the configuration.  Using default value: " + defaultValue);
            }
            return defaultValue;
        }
        return Boolean.valueOf(configuredVal).booleanValue();
    }


}
