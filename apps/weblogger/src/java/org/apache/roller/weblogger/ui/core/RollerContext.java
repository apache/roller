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

package org.apache.roller.weblogger.ui.core;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.providers.encoding.ShaPasswordEncoder;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.utils.DatabaseScriptProvider;
import org.apache.roller.weblogger.business.utils.DatabaseUpgrader;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.weblogger.ui.core.plugins.UIPluginManager;
import org.apache.roller.weblogger.ui.core.plugins.UIPluginManagerImpl;
import org.apache.roller.weblogger.ui.core.security.AutoProvision;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Initialize the Roller web application/context.
 */
public class RollerContext extends ContextLoaderListener  
        implements ServletContextListener, DatabaseScriptProvider { 
    
    private static Log log = LogFactory.getLog(RollerContext.class);
    
    private static ServletContext servletContext = null;
    
    
    public RollerContext() {
        super();
    }
    
    
    /**
     * Responds to app-init event and triggers startup procedures.
     */
    public void contextInitialized(ServletContextEvent sce) {
        
        log.info("Apache Roller Weblogger Initializing ...");
            
        try {
            
            // First, initialize everything that requires no database 

            // Keep a reverence to ServletContext object
            this.servletContext = sce.getServletContext();

            // Save self to context as DatabaseScriptProvider
            this.servletContext.setAttribute("DatabaseScriptProvider", this);

            // Call Spring's context ContextLoaderListener to initialize all the 
            // context files specified in web.xml. This is necessary because 
            // listeners don't initialize in the order specified in 2.3 containers
            super.contextInitialized(sce);

            // get the *real* path to <context>/resources
            String ctxPath = servletContext.getRealPath("/");
            if(!ctxPath.endsWith(File.separator))
                ctxPath += File.separator + "resources";
            else
                ctxPath += "resources";

            // try setting the uploads path to <context>/resources
            // NOTE: this should go away at some point
            // we leave it here for now to allow users to keep writing
            // uploads into their webapp context, but this is a bad idea
            //
            // also, the RollerConfig.setUploadsDir() method is smart
            // enough to disregard this call unless the uploads.path
            // is set to ${webapp.context}
            RollerConfig.setUploadsDir(ctxPath);

            // try setting the themes path to <context>/themes
            // NOTE: this should go away at some point
            // we leave it here for now to allow users to keep using
            // themes in their webapp context, but this is a bad idea
            //
            // also, the RollerConfig.setThemesDir() method is smart
            // enough to disregard this call unless the themes.dir
            // is set to ${webapp.context}
            RollerConfig.setThemesDir(servletContext.getRealPath("/")+File.separator+"themes");
        
            // Initialize Acegi based on Roller configuration
            initializeSecurityFeatures(servletContext);
            
            // Setup Velocity template engine
            setupVelocity();
           

            // Now, the database dependent part...

            // If installation type is manual, then don't run migraton scripts
            if ("manual".equals(RollerConfig.getProperty("installation.type"))) {
                if (DatabaseUpgrader.isUpgradeRequired()) {
                    DatabaseUpgrader upgrader = new DatabaseUpgrader(this); 
                    upgrader.upgradeDatabase(false);
                }
            }
            
            // trigger bootstrapping process
            RollerFactory.bootstrap();
            
            // flush any changes made during bootstrapping
            RollerFactory.getRoller().flush();
            
        } catch (Throwable t) {
            log.fatal("Roller Weblogger initialization failed", t);
        }
        
        // Initialize Planet if necessary
        if (RollerConfig.getBooleanProperty("planet.aggregator.enabled")) {
            try {
                Planet planet = PlanetFactory.getPlanet();                
                PlanetFactory.getPlanet().getPropertiesManager();                
                planet.flush();
                planet.release();
                
            } catch (Throwable t) {
                log.fatal("Roller Planet initialization failed", t);
            }
        }
        
        log.info("Apache Roller Weblogger Initialization Complete");
    }
    
    
    /** 
     * Responds to app-destroy event and triggers shutdown sequence.
     */
    public void contextDestroyed(ServletContextEvent sce) {        
        RollerFactory.getRoller().shutdown();        
        // do we need a more generic mechanism for presentation layer shutdown?
        CacheManager.shutdown();
    }
    
    
    /**
     * Initialize the Velocity rendering engine.
     */
    private void setupVelocity() throws Exception {        
        log.info("Initializing Velocity");
        
        // initialize the Velocity engine
        Properties velocityProps = new Properties();
        
        try {
            InputStream instream = servletContext.getResourceAsStream("/WEB-INF/velocity.properties");
            
            velocityProps.load(instream);
            
            // need to dynamically add old macro libraries if they are enabled
            if(RollerConfig.getBooleanProperty("rendering.legacyModels.enabled")) {
                String macroLibraries = (String) velocityProps.get("velocimacro.library");
                String oldLibraries = RollerConfig.getProperty("velocity.oldMacroLibraries");
                
                // set the new value
                velocityProps.setProperty("velocimacro.library", oldLibraries+","+macroLibraries);
            }
            
            log.debug("Velocity props = "+velocityProps);
            
            // init velocity
            RuntimeSingleton.init(velocityProps);
            
        } catch (Exception e) {
            throw new WebloggerException(e);
        }
        
    }
         
    /**
     * Setup Acegi security features.
     */
    protected void initializeSecurityFeatures(ServletContext context) throws WebloggerException { 

        ApplicationContext ctx =
                WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        

        String rememberMe = RollerConfig.getProperty("rememberme.enabled");
        boolean rememberMeEnabled = Boolean.valueOf(rememberMe).booleanValue();
        
        log.info("Remember Me enabled: " + rememberMeEnabled);
        
        context.setAttribute("rememberMeEnabled", rememberMe);
        
        if (rememberMeEnabled) {
            ProviderManager provider = (ProviderManager) ctx.getBean("authenticationManager");
            provider.getProviders().add(ctx.getBean("rememberMeAuthenticationProvider"));
        }
        

        String encryptPasswords = RollerConfig.getProperty("passwds.encryption.enabled");
        boolean doEncrypt = Boolean.valueOf(encryptPasswords).booleanValue();
        
        if (doEncrypt) {
            DaoAuthenticationProvider provider =
                    (DaoAuthenticationProvider) ctx.getBean("daoAuthenticationProvider");
            String algorithm = RollerConfig.getProperty("passwds.encryption.algorithm");
            PasswordEncoder encoder = null;
            if (algorithm.equalsIgnoreCase("SHA")) {
                encoder = new ShaPasswordEncoder();
            } else if (algorithm.equalsIgnoreCase("MD5")) {
                encoder = new Md5PasswordEncoder();
            } else {
                log.error("Encryption algorithm '" + algorithm +
                        "' not supported, disabling encryption.");
            }
            if (encoder != null) {
                provider.setPasswordEncoder(encoder);
                log.info("Password Encryption Algorithm set to '" + algorithm + "'");
            }
        }
        

        if (RollerConfig.getBooleanProperty("securelogin.enabled")) {
            AuthenticationProcessingFilterEntryPoint entryPoint =
                (AuthenticationProcessingFilterEntryPoint)
                    ctx.getBean("authenticationProcessingFilterEntryPoint");
            entryPoint.setForceHttps(true);
        }
                
        /*
        if (RollerConfig.getBooleanProperty("schemeenforcement.enabled")) {
            
            ChannelProcessingFilter procfilter =
                    (ChannelProcessingFilter)ctx.getBean("channelProcessingFilter");
            ConfigAttributeDefinition secureDef = new ConfigAttributeDefinition();
            secureDef.addConfigAttribute(new SecurityConfig("REQUIRES_SECURE_CHANNEL"));
            ConfigAttributeDefinition insecureDef = new ConfigAttributeDefinition();
            insecureDef.addConfigAttribute(new SecurityConfig("REQUIRES_INSECURE_CHANNEL"));
            PathBasedFilterInvocationDefinitionMap defmap =
                    (PathBasedFilterInvocationDefinitionMap)procfilter.getFilterInvocationDefinitionSource();
            
            // add HTTPS URL path patterns to Acegi config
            String httpsUrlsProp = RollerConfig.getProperty("schemeenforcement.https.urls");
            if (httpsUrlsProp != null) {
                String[] httpsUrls = StringUtils.stripAll(StringUtils.split(httpsUrlsProp, ",") );
                for (int i=0; i<httpsUrls.length; i++) {
                    defmap.addSecureUrl(httpsUrls[i], secureDef);
                }
            }
            // all other action URLs are non-HTTPS
            defmap.addSecureUrl("/**<!-- need to remove this when uncommenting -->/*.do*", insecureDef);
        }
        */
    }
    
    /**
     * Get the ServletContext.
     * @return ServletContext
     */
    public static ServletContext getServletContext() {
        return servletContext;
    }
    

    /**
     * Get database script as stream, path is relative to dbscripts directory.
     */
    public InputStream getDatabaseScript(String path) throws Exception {
        return getServletContext().getResourceAsStream("/WEB-INF/dbscripts/" + path);
    }


    /**
     * Get an instance of AutoProvision, if available in roller.properties
     * @return AutoProvision
     */
    public static AutoProvision getAutoProvision() {        
        String clazzName = RollerConfig.getProperty("users.sso.autoProvision.className");
        
        if (null == clazzName) {
            return null;
        }
        
        Class clazz;
        try {
            clazz = Class.forName(clazzName);
        } catch (ClassNotFoundException e) {
            log.warn("Unable to found specified Auto Provision class.", e);
            return null;
        }
        
        if(null == clazz) {
            return null;
        }
        
        Class[] interfaces = clazz.getInterfaces();
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(AutoProvision.class)) {
                try {
                    return (AutoProvision) clazz.newInstance();
                } catch (InstantiationException e) {
                    log.warn("InstantiationException while creating: " + clazzName, e);
                } catch (IllegalAccessException e) {
                    log.warn("IllegalAccessException while creating: " + clazzName, e);
                }
            }
        }        
        return null;        
    }

    
    /**
     * Access to the plugin manager for the UI layer. TODO: we may want 
     * something similar to the Roller interface for the UI layer if we dont 
     * want methods like this here in RollerContext.
     */
    public static UIPluginManager getUIPluginManager() {
        return UIPluginManagerImpl.getInstance();
    }
}
