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

package org.apache.roller.ui.core;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.providers.encoding.ShaPasswordEncoder;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.runnable.RollerTask;
import org.apache.roller.business.utils.UpgradeDatabase;
import org.apache.roller.config.PingConfig;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.runnable.ThreadManager;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.ui.core.plugins.UIPluginManager;
import org.apache.roller.ui.core.plugins.UIPluginManagerImpl;
import org.apache.roller.ui.core.security.AutoProvision;
import org.apache.roller.util.cache.CacheManager;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;


/**
 * Responds to app init/destroy events and holds Roller instance.
 *
 * @web.listener
 */
public class RollerContext extends ContextLoaderListener 
        implements ServletContextListener {
    
    private static Log log = LogFactory.getLog(RollerContext.class);
    
    private static ServletContext servletContext = null;
    
    
    public RollerContext() {
        super();
    }
    
    
    /**
     * Responds to app-init event and triggers startup procedures.
     */
    public void contextInitialized(ServletContextEvent sce) {
        
        log.info("Roller Weblogger Initializing ...");
        
        // check that Hibernate code is in place
        try {
            Class.forName("org.hibernate.Session");
        } catch (Throwable t) {
            // if Hibernate is not available, we're hosed
            throw new RuntimeException(
               "FATAL ERROR: Hibernate not found, please refer to the Roller Installation Guide for instructions on how to install the required Hibernate jars");
        }
        
        // keep a reverence to ServletContext object
        this.servletContext = sce.getServletContext();
        
        // call Spring's context ContextLoaderListener to initialize
        // all the context files specified in web.xml. This is necessary
        // because listeners don't initialize in the order specified in
        // 2.3 containers
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
        
        try {
            // always upgrade database first
            upgradeDatabaseIfNeeded();
            
            Roller roller = RollerFactory.getRoller();
            
            setupRuntimeProperties();
            initializeSecurityFeatures(servletContext);
            setupSearch();
            initializePingFeatures();
            setupThemeLibrary();
            setupScheduledTasks();
            setupVelocity();
            
            roller.flush();
            roller.release();
            
        } catch (Throwable t) {
            log.fatal("Roller Weblogger initialization failed", t);
            throw new RuntimeException(t);
        }
        
        // Initialize Planet if necessary
        if(RollerConfig.getBooleanProperty("planet.aggregator.enabled")) {
            try {
                Planet planet = PlanetFactory.getPlanet();
                
                setupPlanetProperties();
                
                planet.flush();
                planet.release();
                
            } catch (Throwable t) {
                log.fatal("Roller Planet initialization failed", t);
                throw new RuntimeException(t);
            }
        }
        
        log.info("Roller Weblogger Initialization Complete");
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
     * Trigger any database upgrade work that needs to be done.
     */
    private void upgradeDatabaseIfNeeded() throws Exception {
        
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:comp/env/jdbc/rollerdb");
            Connection con = ds.getConnection();
            UpgradeDatabase.upgradeDatabase(con, RollerFactory.getRoller().getVersion());
            con.close();
        } catch (NamingException e) {
            log.warn("Unable to access DataSource", e);
        } catch (SQLException e) {
            log.warn(e);
        }
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
            throw new RollerException(e);
        }
        
    }
    
    
    /**
     * Initialize the runtime configuration.
     */
    private void setupRuntimeProperties() throws Exception {
        // PropertiesManager initializes itself
        RollerFactory.getRoller().getPropertiesManager();
    }
    
    
    /**
     * Initialize the built-in search engine.
     */
    private void setupSearch() throws Exception {
        // IndexManager initializes itself
        RollerFactory.getRoller().getIndexManager();
    }
    
    
    /**
     * Initialize the Theme Library.
     */
    private void setupThemeLibrary() throws Exception {
        // theme manager initializes itself
        RollerFactory.getRoller().getThemeManager();
    }
    
    
    /**
     * Start up any scheduled background jobs.
     */
    private void setupScheduledTasks() throws Exception {
        
        ThreadManager tmgr = RollerFactory.getRoller().getThreadManager();
        
        Date now = new Date();
        
        // okay, first we look for what tasks have been enabled
        String tasksStr = RollerConfig.getProperty("tasks.enabled");
        String[] tasks = StringUtils.stripAll(StringUtils.split(tasksStr, ","));
        for (int i=0; i < tasks.length; i++) {
            
            String taskClassName = RollerConfig.getProperty("tasks."+tasks[i]+".class");
            if(taskClassName != null) {
                log.info("Initializing task: "+tasks[i]);
                
                try {
                    Class taskClass = Class.forName(taskClassName);
                    RollerTask task = (RollerTask) taskClass.newInstance();
                    task.init();
                    
                    Date startTime = task.getStartTime(now);
                    if(startTime == null || now.after(startTime)) {
                        startTime = now;
                    }
                    
                    // schedule it
                    tmgr.scheduleFixedRateTimerTask(task, startTime, task.getInterval());
                    
                } catch (ClassCastException ex) {
                    log.warn("Task does not extend RollerTask class", ex);
                } catch (RollerException ex) {
                    log.error("Error scheduling task", ex);
                } catch (Exception ex) {
                    log.error("Error instantiating task", ex);
                }
            }
        }
    }
    
    
    // Initialize ping features
    private void initializePingFeatures() throws RollerException {
        
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
    }
    
    
    /**
     * Setup Acegi security features.
     */
    protected void initializeSecurityFeatures(ServletContext context) { 
        
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
                    (AuthenticationProcessingFilterEntryPoint)ctx.getBean("authenticationProcessingFilterEntryPoint");
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
     * Initialize the Roller Planet runtime configuration.
     */
    private void setupPlanetProperties() throws Exception {
        // Planet PropertiesManager initializes itself
        PlanetFactory.getPlanet().getPropertiesManager();
    }
    
    
    /**
     * Get the ServletContext.
     *
     * @return ServletContext
     */
    public static ServletContext getServletContext() {
        return servletContext;
    }
    
    
    /**
     * Get an instance of AutoProvision, if available in roller.properties
     * 
     * @return AutoProvision
     */
    public static AutoProvision getAutoProvision() {
      
      String clazzName = RollerConfig.getProperty("users.sso.autoProvision.className");
      
      if(null == clazzName) {
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
          if (interfaces[i].equals(AutoProvision.class))
          {
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
     * Access to the plugin manager for the UI layer.
     *
     * TODO: we may want something similar to the Roller interface for the
     *  ui layer if we dont' want methods like this here in RollerContext.
     */
    public static UIPluginManager getUIPluginManager() {
        // TODO: we may want to do this another way
        return UIPluginManagerImpl.getInstance();
    }

}
