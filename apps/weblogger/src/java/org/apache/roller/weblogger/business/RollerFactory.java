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

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.RollerConfig;


/**
 * Provides access to the Roller instance and bootstraps the business tier.
 */
public final class RollerFactory {
    
    private static final Log log = LogFactory.getLog(RollerFactory.class);
    
    // a reference to the bootstrapped Roller instance
    private static Roller rollerInstance = null;
    
    private static Injector injector = null;
  
    
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
    
    
    // non-instantiable
    private RollerFactory() {
        // hello all you beautiful people
    }
    
    
    /**
     * True if bootstrap process has been completed, False otherwise.
     */
    public static boolean isBootstrapped() {
        return (rollerInstance != null);
    }
    
    
    /**
     * Accessor to the Roller Weblogger business tier.
     *
     * @return Roller An instance of Roller.
     * @throws IllegalStateException If the app has not been properly bootstrapped yet.
     */
    public static final Roller getRoller() {
        if (rollerInstance == null) {
            throw new IllegalStateException("Roller Weblogger has not been bootstrapped yet");
        }
        
        return rollerInstance;
    }
    
    
    /**
     * Access to Guice injector so that developers can add new injected objects.
     */
    public static Injector getInjector() {
        return injector;
    }
    
    
    /**
     * Bootstrap the Roller Weblogger business tier.
     *
     * Bootstrapping the application effectively instantiates all the necessary
     * pieces of the business tier and wires them together so that the app is 
     * ready to run.
     *
     * @throws IllegalStateException If the app has not been properly prepared yet.
     * @throws BootstrapException If an error happens during the bootstrap process.
     */
    public static final void bootstrap() throws BootstrapException {
        
        // if the app hasn't been properly started so far then bail
        if (!WebloggerStartup.isPrepared()) {
            throw new IllegalStateException("Cannot bootstrap until application has been properly prepared");
        }
        
        log.info("Bootstrapping Roller Weblogger business tier");
        
        rollerInstance = injector.getInstance(Roller.class);
            
        log.info("Roller Weblogger business tier successfully bootstrapped");
    }
    
}
