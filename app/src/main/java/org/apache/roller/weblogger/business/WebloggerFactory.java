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
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * Provides access to the Weblogger instance and bootstraps the business tier.
 */
public final class WebloggerFactory {
    
    private static final Log LOG = LogFactory.getLog(WebloggerFactory.class);
    
    // our configured weblogger provider
    private static WebloggerProvider webloggerProvider = null;

    // non-instantiable
    private WebloggerFactory() {
        // hello all you beautiful people
    }

    /**
     * True if bootstrap process has been completed, False otherwise.
     */
    public static boolean isBootstrapped() {
        return (webloggerProvider != null);
    }
    
    
    /**
     * Accessor to the Weblogger Weblogger business tier.
     * 
     * @return Weblogger An instance of Weblogger.
     * @throws IllegalStateException If the app has not been properly bootstrapped yet.
     */
    public static Weblogger getWeblogger() {
        if (webloggerProvider == null) {
            throw new IllegalStateException("Roller Weblogger has not been bootstrapped yet");
        }
        
        return webloggerProvider.getWeblogger();
    }
    
    
    /**
     * Bootstrap the Roller Weblogger business tier, uses default WebloggerProvider.
     *
     * Bootstrapping the application effectively instantiates all the necessary
     * pieces of the business tier and wires them together so that the app is 
     * ready to run.
     *
     * @throws IllegalStateException If the app has not been properly prepared yet.
     * @throws BootstrapException If an error happens during the bootstrap process.
     */
    public static void bootstrap() throws BootstrapException {
        
        // if the app hasn't been properly started so far then bail
        if (!WebloggerStartup.isPrepared()) {
            throw new IllegalStateException("Cannot bootstrap until application has been properly prepared");
        }
        
        // lookup our default provider and instantiate it
        WebloggerProvider defaultProvider;
        String providerClassname = WebloggerConfig.getProperty("weblogger.provider.class");
        if(providerClassname != null) {
            try {
                Class providerClass = Class.forName(providerClassname);
                defaultProvider = (WebloggerProvider) providerClass.newInstance();
            } catch (Exception ex) {
                throw new BootstrapException("Error instantiating default provider: " + providerClassname + "; Exception message: " + ex.getMessage(), ex);
            }
        } else {
            throw new NullPointerException("No provider specified in config property 'weblogger.provider.class'");
        }

        // now just bootstrap using our default provider
        bootstrap(defaultProvider);
    }
    
    
    /**
     * Bootstrap the Roller Weblogger business tier, uses specified WebloggerProvider.
     *
     * Bootstrapping the application effectively instantiates all the necessary
     * pieces of the business tier and wires them together so that the app is 
     * ready to run.
     *
     * @param provider A WebloggerProvider to use for bootstrapping.
     * @throws IllegalStateException If the app has not been properly prepared yet.
     * @throws BootstrapException If an error happens during the bootstrap process.
     */
    public static void bootstrap(WebloggerProvider provider)
            throws BootstrapException {
        
        // if the app hasn't been properly started so far then bail
        if (!WebloggerStartup.isPrepared()) {
            throw new IllegalStateException("Cannot bootstrap until application has been properly prepared");
        }
        
        if (provider == null) {
            throw new NullPointerException("WebloggerProvider is null");
        }
        
        LOG.info("Bootstrapping Roller Weblogger business tier");
        
        LOG.info("Weblogger Provider = " + provider.getClass().getName());
        
        // save reference to provider
        webloggerProvider = provider;
        
        // bootstrap weblogger provider
        webloggerProvider.bootstrap();
        
        // make sure we are all set
        if(webloggerProvider.getWeblogger() == null) {
            throw new BootstrapException("Bootstrapping failed, Weblogger instance is null");
        }
        
        LOG.info("Roller Weblogger business tier successfully bootstrapped");
        LOG.info("   Version: " + webloggerProvider.getWeblogger().getVersion());
        LOG.info("   Revision: " + webloggerProvider.getWeblogger().getRevision());
    }
    
}
