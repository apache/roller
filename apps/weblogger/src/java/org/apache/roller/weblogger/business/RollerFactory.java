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

package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.utils.DatabaseCreator;
import org.apache.roller.weblogger.business.utils.DatabaseUpgrader;
import org.apache.roller.weblogger.config.PingConfig;
import org.apache.roller.weblogger.config.RollerConfig;


/**
 * Provides access to the Roller instance.
 */
public final class RollerFactory {
    
    private static final Log log = LogFactory.getLog(RollerFactory.class);
    
    private static final String DEFAULT_IMPL =
        "org.apache.roller.weblogger.business.jpa.JPARollerImpl";
        //"org.apache.roller.weblogger.business.hibernate.HibernateRollerImpl";
        //"org.apache.roller.weblogger.business.datamapper.jpa.JPARollerImpl";
    
    private static Roller rollerInstance = null;
    private static boolean bootstrapped = false;
    
    
    // non-instantiable
    private RollerFactory() {
        // hello all you beautiful people
    }
    
    
    /**
     * True if bootstrap process was completed
     */
    public static boolean isBootstrapped() {
        return bootstrapped;
    }
    

    /**
     * Bootstrap the Roller Weblogger business tier.
     */
    public static final void bootstrap() throws WebloggerException {
        
        if ("manual".equals(RollerConfig.getProperty("installation.type"))) {
            if (DatabaseCreator.isCreationRequired() 
             || DatabaseUpgrader.isUpgradeRequired()) { 
                return;
            }
        }        
        
        // This will cause instantiation and initialziation of Roller impl
        Roller roller = getRoller();
        
        // TODO: this initialization process should probably be controlled by
        // a more generalized application lifecycle event framework
        
        // Now that Roller has been instantiated, initialize individual managers
        roller.getPropertiesManager();
        roller.getIndexManager();
        roller.getThemeManager();
        
        // And this will schedule all configured tasks
        roller.getThreadManager().startTasks();
        
        // Initialize ping systems
        try {
            // Initialize common targets from the configuration
            PingConfig.initializeCommonTargets();
            
            // Initialize ping variants
            PingConfig.initializePingVariants();
            
            // Remove custom ping targets if they have been disallowed
            if (PingConfig.getDisallowCustomTargets()) {
                log.info("Custom ping targets have been disallowed.  Removing any existing custom targets.");
                RollerFactory.getRoller().getPingTargetManager().removeAllCustomPingTargets();
            }
            
            // Remove all autoping configurations if ping usage has been disabled.
            if (PingConfig.getDisablePingUsage()) {
                log.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
                RollerFactory.getRoller().getAutopingManager().removeAllAutoPings();
            }
        } catch (WebloggerException e) {
            log.error("ERROR configing ping managers", e);
        }
        
        bootstrapped = true;
    }
    
    
    /**
     * Static accessor for the instance of Roller.
     */
    public static final Roller getRoller() {
        
        // check to see if we need to instantiate
        if(rollerInstance == null) {
            
            // lookup value for the roller classname to use
            String roller_classname =
                    RollerConfig.getProperty("persistence.roller.classname");
            if(roller_classname == null || roller_classname.trim().length() < 1)
                roller_classname = DEFAULT_IMPL;
            
            try {
                Class rollerClass = Class.forName(roller_classname);
                java.lang.reflect.Method instanceMethod =
                        rollerClass.getMethod("instantiate", (Class[])null);
                
                // do the invocation
                rollerInstance = (Roller) instanceMethod.invoke(rollerClass, (Object[])null);
                
                log.info("Using Roller Impl: " + roller_classname);
                
            } catch (Throwable e) {
                
                // uh oh
                log.error("Error instantiating " + roller_classname, e);
                
                try {
                    // if we didn't already try DEFAULT_IMPL then try it now
                    if( ! DEFAULT_IMPL.equals(roller_classname)) {
                        
                        log.info("** Trying DEFAULT_IMPL "+DEFAULT_IMPL+" **");
                        
                        Class rollerClass = Class.forName(DEFAULT_IMPL);
                        java.lang.reflect.Method instanceMethod =
                                rollerClass.getMethod("instantiate", (Class[])null);
                        
                        // do the invocation
                        rollerInstance = (Roller) instanceMethod.invoke(rollerClass, (Object[])null);
                    } else {
                        // we just do this so that the logger gets the message
                        throw new Exception("Doh! Couldn't instantiate a roller class");
                    }
                    
                } catch (Exception re) {
                    log.fatal("Failed to instantiate fallback roller impl", re);
                }
            }
        }
        
        return rollerInstance;
    }

}
