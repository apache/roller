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
package org.apache.roller.weblogger.ui.core;

import java.io.File;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.startup.DatabaseInstaller;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.apache.roller.weblogger.business.startup.StartupException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Initialize the Roller web application/context.
 */
public class RollerContext extends ContextLoaderListener  
        implements ServletContextListener {

    private static Logger log = LoggerFactory.getLogger(RollerContext.class);
    
    private static ServletContext servletContext = null;

    public RollerContext() {
        super();
    }
    
    /**
     * Get the ServletContext.
     * @return ServletContext
     */
    public static ServletContext getServletContext() {
        return servletContext;
    } 
    
    
    /**
     * Responds to app-init event and triggers startup procedures.
     */
    public void contextInitialized(ServletContextEvent sce) {

        // First, initialize everything that requires no database

        // Keep a reference to ServletContext object
        RollerContext.servletContext = sce.getServletContext();

        // try setting the themes path to <context>/themes
        // NOTE: this should go away at some point
        // we leave it here for now to allow users to keep using
        // themes in their webapp context, but this is a bad idea
        //
        // also, the WebloggerStaticConfig.setThemesDir() method is smart
        // enough to disregard this call unless the themes.dir
        // is set to ${webapp.context}
        WebloggerStaticConfig.setThemesDir(servletContext.getRealPath("/")+File.separator+"themes");

        // Call Spring's context ContextLoaderListener to initialize all the
        // context files specified in web.xml. This is necessary because
        // listeners don't initialize in the order specified in 2.3 containers
        super.contextInitialized(sce);

        // get the *real* path to <context>/resources
        String ctxPath = servletContext.getRealPath("/");
        if (ctxPath == null) {
            log.error("TightBlog requires an exploded WAR file to run.");
            return;
        }

        // Now prepare the core services of the app so we can bootstrap
        boolean autoDatabaseWorkNeeded = false;
        try {
            autoDatabaseWorkNeeded = prepare();
        } catch (StartupException ex) {
            log.error("TightBlog Weblogger startup failed during app preparation", ex);
            return;
        }
        
        
        // if preparation incomplete (e.g., database tables need creating)
        // continue on - BootstrapFilter will start the database install/upgrade process
        // otherwise bootstrap the business tier
        if (log.isInfoEnabled() && autoDatabaseWorkNeeded) {
            String output = "\n-------------------------------------------------------------------";
            output +=       "\nTightBlog Weblogger startup INCOMPLETE, user interaction commencing";
            output +=       "\n-------------------------------------------------------------------";
            log.info(output);
        } else {
            WebloggerFactory.bootstrap();
		}
            
        try {
            // Initialize Spring Security based on Roller configuration
            initializeSecurityFeatures(servletContext);
        } catch (Exception ex) {
            log.error("Error initializing TightBlog Weblogger web tier", ex);
        }
        
    }
    
    
    /** 
     * Responds to app-destroy event and triggers shutdown sequence.
     */
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Shutting down");
        closeWebApplicationContext(servletContext);
    }

    /**
     * Setup Spring Security security features.
     */
    protected void initializeSecurityFeatures(ServletContext context) { 

        ApplicationContext ctx =
                WebApplicationContextUtils.getRequiredWebApplicationContext(context);

        /*String[] beanNames = ctx.getBeanDefinitionNames();
        for (String name : beanNames)
            System.out.println(name);*/

        String encryptPasswords = WebloggerStaticConfig.getProperty("passwds.encryption.enabled");
        boolean doEncrypt = Boolean.valueOf(encryptPasswords);
        
        String daoBeanName = "org.springframework.security.authentication.dao.DaoAuthenticationProvider#0";

        // for LDAP-only authentication, no daoBeanName (i.e., UserDetailsService) may be provided in security.xml.
        if (doEncrypt && ctx.containsBean(daoBeanName)) {
            DaoAuthenticationProvider provider = (DaoAuthenticationProvider) ctx.getBean(daoBeanName);
            String algorithm = WebloggerStaticConfig.getProperty("passwds.encryption.algorithm");
            PasswordEncoder encoder;
            if ("SHA".equalsIgnoreCase(algorithm)) {
                encoder = new ShaPasswordEncoder();
            } else if ("MD5".equalsIgnoreCase(algorithm)) {
                encoder = new Md5PasswordEncoder();
            } else {
                throw new IllegalArgumentException("Encryption algorithm '" + algorithm + "' not supported, choose SHA or MD5.");
            }
            provider.setPasswordEncoder(encoder);
            log.info("Password Encryption Algorithm set to '{}'", algorithm);
        }

    }
    
    
    /**
     * Flush user from any caches maintained by security system.
     */
    public static void flushAuthenticationUserCache(String userName) {                                
        ApplicationContext ctx = 
            WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
		try {
			UserCache userCache = ctx.getBean("userCache", UserCache.class);
			if (userCache != null) {
				userCache.removeUserFromCache(userName);
			}
		} catch (NoSuchBeanDefinitionException exc) {
			log.debug("No userCache bean in context", exc);
		}
    }

    /**
     * Run the weblogger preparation sequence.
     *
     * This sequence is what prepares the core services of the application such
     * as setting up the database and mail providers.
     *
     * @return Whether automatic DB install or version upgrade needed
     */
    private static boolean prepare() throws StartupException {
        boolean autoDatabaseWorkNeeded = true;
        DatabaseProvider dbProvider = new DatabaseProvider();

        // now we need to deal with database install/upgrade logic
        DatabaseInstaller dbInstaller = new DatabaseInstaller(dbProvider);
        if("manual".equals(WebloggerStaticConfig.getProperty("installation.type"))) {
            if (dbInstaller.isUpgradeRequired()) {
                // if we are doing manual install then all that is needed is the app to
                // update the version number in weblogger_properties, not run any db scripts
                dbInstaller.upgradeDatabase(false);
            }
            autoDatabaseWorkNeeded = false;
        } else {
            // we are in auto install mode, so see if there is any work to do
            if (!dbInstaller.isCreationRequired() && !dbInstaller.isUpgradeRequired()) {
                autoDatabaseWorkNeeded = false;
            }
        }
        return autoDatabaseWorkNeeded;
    }


}
