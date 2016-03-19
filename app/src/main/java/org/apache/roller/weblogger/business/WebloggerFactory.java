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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.startup.StartupException;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;

/**
 * Provides access to the Weblogger instance and bootstraps the business tier.
 */
public final class WebloggerFactory {
    
    private static final Log log = LogFactory.getLog(WebloggerFactory.class);

    // Spring Application Context
    private static ApplicationContext context = null;

    // maintain our own singleton instance of Weblogger
    private static Weblogger webloggerInstance = null;

    private static JPAPersistenceStrategy strategy = null;

    private static MailProvider mailProvider = null;

    // non-instantiable
    private WebloggerFactory() {
    }

    /**
     * Get a reference to the currently configured MailProvider, if available.
     *
     * @return MailProvider The configured mail provider, or null if none configured.
     */
    public static MailProvider getMailProvider() {
        return mailProvider;
    }

    /**
     * True if bootstrap process has been completed, False otherwise.
     */
    public static boolean isBootstrapped() {
        return webloggerInstance != null;
    }

    /**
     * Accessor to the Weblogger Weblogger business tier.
     * 
     * @return Weblogger An instance of Weblogger.
     * @throws IllegalStateException If the app has not been properly bootstrapped yet.
     */
    public static Weblogger getWeblogger() {
        if (!isBootstrapped()) {
            throw new IllegalStateException("TightBlog Weblogger has not been bootstrapped yet");
        }
        
        return webloggerInstance;
    }

    public static ApplicationContext getContext() {
        if (!isBootstrapped()) {
            throw new IllegalStateException("TightBlog Weblogger has not been bootstrapped yet");
        }

        return context;
    }

    /**
     * Variant of bootstrap() used by the running WAR (not JUnit testing).
     */
    public static void bootstrap() throws WebloggerException {
        ServletContext servletContext = RollerContext.getServletContext();
        if (servletContext == null) {
            throw new RuntimeException("Error bootstrapping Weblogger; no web context available.");
        }
        bootstrap(WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext));
    }

    /**
     * Bootstrap the Roller Weblogger business tier.
     *
     * There are two possible application contexts, the web-level defined in web.xml
     * (activated when running the WAR) and the unit tests configured in WebloggerTest
     *
     * @throws IllegalStateException If the app has not been properly prepared yet.
     * @throws RuntimeException If the app cannot be bootstrapped.
     * @throws WebloggerException if any manager cannot be initialized
     */
    public static void bootstrap(ApplicationContext inContext) throws WebloggerException {
        try {
            context = inContext;
            webloggerInstance = context.getBean("webloggerBean", Weblogger.class);
            strategy = context.getBean("persistenceStrategy", JPAPersistenceStrategy.class);
            // TODO:  Move below to @PostConstruct in IndexManagerImpl (presently requires webloggerInstance to be active)
            PropertiesManager propertiesManager = context.getBean("propertiesManager", PropertiesManager.class);
            propertiesManager.initialize();
            IndexManager indexManager = context.getBean("indexManager", IndexManager.class);
            indexManager.initialize();
            PingTargetManager pingTargetManager = context.getBean("pingTargetManager", PingTargetManager.class);
            pingTargetManager.initialize();
        } catch (BeansException e) {
            throw new RuntimeException("Error bootstrapping Weblogger; exception message: " + e.getMessage(), e);
        }

        strategy.flush();

        // setup mail provider, if configured
        try {
            mailProvider = new MailProvider();
        } catch(StartupException ex) {
            log.warn("Failed to setup mail provider, continuing anyways.\n"
                    + "Reason: " + ex.getMessage());
            log.info("The cause of setting up mail provider error was: ", ex);
        }

        log.info("TightBlog Weblogger business tier successfully bootstrapped");
        log.info("   Version: " + WebloggerConfig.getProperty("weblogger.version", "Unknown"));
        log.info("   Revision: " + WebloggerConfig.getProperty("weblogger.revision", "Unknown"));
    }

    public static void flush() throws WebloggerException {
        strategy.flush();
    }

    public static void release() {
        strategy.release();
    }

}
