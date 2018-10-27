/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.business;

import org.tightblog.business.search.IndexManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;

/**
 * Subclass of Spring's ContextLoaderListener (http://stackoverflow.com/a/11817368/1207540)
 * used to initialize and configure the Spring web application context.  Also maintains
 * some globally accessible objects primarily for objects not instantiated by Spring
 * and hence not easily able to take advantage of DI features.
 */
public class WebloggerContext extends ContextLoaderListener {

    private static Logger log = LoggerFactory.getLogger(WebloggerContext.class);

    private static URLStrategy urlStrategy;

    /**
     * True if bootstrap process has been completed, false otherwise.
     */
    public static boolean isBootstrapped() {
        return urlStrategy != null;
    }

    public static URLStrategy getUrlStrategy() {
        if (!isBootstrapped()) {
            throw new IllegalStateException("TightBlog Weblogger has not been bootstrapped yet");
        }

        return urlStrategy;
    }

    /**
     * Bootstrap the Weblogger business tier.
     * <p>
     * There are two possible application contexts, the web-level defined in web.xml
     * (activated when running the WAR) and the unit tests configured in WebloggerTest
     *
     * @throws IllegalStateException If the app has not been properly prepared yet.
     * @throws RuntimeException      If the app cannot be bootstrapped.
     */
    public static void bootstrap(ApplicationContext context) {
        try {
            urlStrategy = context.getBean("urlStrategy", URLStrategy.class);
        } catch (BeansException e) {
            throw new RuntimeException("Error bootstrapping, exception message: " + e.getMessage(), e);
        }

        // IndexManager needs a functioning database, so delaying its initialization to this point.
        IndexManager indexManager = context.getBean("indexManager", IndexManager.class);
        indexManager.initialize();

        log.info("TightBlog Weblogger successfully bootstrapped");
        log.info("   Version: {}", context.getEnvironment().getProperty("weblogger.version", "Unknown"));
        log.info("   Revision: {}", context.getEnvironment().getProperty("weblogger.revision", "Unknown"));
    }
}
