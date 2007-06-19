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

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
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
public abstract class RollerFactory implements Module {
    private static Log log = LogFactory.getLog(RollerFactory.class);
    private static Injector injector = null;
    private static Roller rollerInstance = null;
    private static boolean bootstrapped = false;
    
    
    static {
        String moduleClassname = RollerConfig.getProperty("guice.backend.module");
        try {
            Class moduleClass = Class.forName(moduleClassname);
            Module module = (Module)moduleClass.newInstance();
            injector = Guice.createInjector(module);
        } catch (Throwable e) {
            // Fatal misconfiguration, cannot recover
            throw new RuntimeException("Error instantiating backend module" + moduleClassname, e);
        }
    }
    
    
    /**
     * True if bootstrap process was completed
     */
    public static boolean isBootstrapped() {
        return bootstrapped;
    }
    
    /**
     * Access to Guice injector so that developers can add new injected objects
     * to Roller, e.g. new managers that are not part of the Roller interface.
     */
    public static Injector getInjector() {
        return injector;
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
            
            RollerFactory.getRoller().getIndexManager().bootstrap();
            
        } catch (WebloggerException e) {
            log.error("ERROR configing ping managers", e);
        }
        
        bootstrapped = true;
    }
    
    public static Roller getRoller() {
        return injector.getInstance(Roller.class);
    }
    
}

