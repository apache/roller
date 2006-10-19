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

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import java.io.File;
import java.io.IOException;
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
import javax.servlet.http.HttpSessionEvent;
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
import org.apache.roller.model.ThreadManager;
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
public class RollerContext extends ContextLoaderListener implements ServletContextListener {
    
    private static Log mLogger = LogFactory.getLog(RollerContext.class);
    
    private String mVersion = null;
    private String mBuildTime = null;
    private String mBuildUser = null;
    
    public static final String ROLLER_CONTEXT = "roller.context";
    
    private static ServletContext mContext = null;
    private static Authenticator mAuthenticator = null;
    private final SynchronizedInt mSessionCount = new SynchronizedInt(0);
    
    
    /**
     * Constructor for RollerContext.
     */
    public RollerContext() {
        super();
        
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/version.properties"));
        } catch (IOException e) {
            mLogger.error("version.properties not found", e);
        }
        
        mVersion = props.getProperty("ro.version", "UNKNOWN");
        mBuildTime = props.getProperty("ro.buildTime", "UNKNOWN");
        mBuildUser = props.getProperty("ro.buildUser", "UNKNOWN");
    }
    
    
    /* Returns Roller instance for specified app */
    public static RollerContext getRollerContext() {
        // get roller from servlet context
        ServletContext sc = RollerContext.getServletContext();
        return (RollerContext) sc.getAttribute(ROLLER_CONTEXT);
    }
    
    
    /** Responds to app-destroy by saving the indexManager's information */
    public void contextDestroyed(ServletContextEvent sce) {
        RollerFactory.getRoller().shutdown();
        
        // do we need a more generic mechanism for presentation layer shutdown?
        CacheManager.shutdown();
    }
    
    
    /**
     * Responds to context initialization event by processing context
     * paramters for easy access by the rest of the application.
     */
    public void contextInitialized(ServletContextEvent sce) {
        
        try {
            Class.forName("org.hibernate.Session");
        } catch (Throwable t) {
            // if Hibernate is not available, we're hosed
            throw new RuntimeException(
               "FATAL ERROR: Hibernate not found, please refer to the Roller Installation Guide for instructions on how to install the required Hibernate jars");
        }
        
        mLogger.debug("RollerContext initializing");
        
        // Save context in self and self in context
        mContext = sce.getServletContext();
        mContext.setAttribute(ROLLER_CONTEXT, this);
        
        // get the *real* path to <context>/resources
        String ctxPath = mContext.getRealPath("/");
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
        RollerConfig.setThemesDir(mContext.getRealPath("/")+File.separator+"themes");
        
        try {
            // always upgrade database first
            upgradeDatabaseIfNeeded();
            
            Roller roller = RollerFactory.getRoller();
            
            setupRollerProperties();
            
            // call Spring's context ContextLoaderListener to initialize
            // all the context files specified in web.xml. This is necessary
            // because listeners don't initialize in the order specified in
            // 2.3 containers
            super.contextInitialized(sce);
            
            initializeSecurityFeatures(mContext);
            
            setupVelocity();
            roller.getThemeManager();
            setupIndexManager(roller);
            initializePingFeatures(roller);
            setupTasks();
            
            roller.flush();
            roller.release();
            
        } catch (Throwable t) {
            mLogger.fatal("RollerContext initialization failed", t);
        }
        
        mLogger.debug("RollerContext initialization complete");
    }
    
    
    private void setupVelocity() throws RollerException {
        
        mLogger.info("Initializing Velocity");
        
        // initialize the Velocity engine
        Properties velocityProps = new Properties();
        
        try {
            InputStream instream = mContext.getResourceAsStream("/WEB-INF/velocity.properties");
            
            velocityProps.load(instream);
            
            // need to dynamically add old macro libraries if they are enabled
            if(RollerConfig.getBooleanProperty("rendering.legacyModels.enabled")) {
                String macroLibraries = (String) velocityProps.get("velocimacro.library");
                String oldLibraries = RollerConfig.getProperty("velocity.oldMacroLibraries");
                
                // set the new value
                velocityProps.setProperty("velocimacro.library", oldLibraries+","+macroLibraries);
            }
            
            mLogger.debug("Velocity props = "+velocityProps);
            
            // init velocity
            RuntimeSingleton.init(velocityProps);
            
        } catch (Exception e) {
            throw new RollerException(e);
        }
        
    }
    
    
    private void setupRollerProperties() throws RollerException {
        // init property manager by creating it
        Roller mRoller = RollerFactory.getRoller();
        mRoller.getPropertiesManager();
    }
    
    
    private void setupTasks() throws RollerException {
        
        ThreadManager tmgr = RollerFactory.getRoller().getThreadManager();
        
        Date now = new Date();
        
        // okay, first we look for what tasks have been enabled
        String tasksStr = RollerConfig.getProperty("tasks.enabled");
        String[] tasks = StringUtils.stripAll(StringUtils.split(tasksStr, ","));
        for (int i=0; i < tasks.length; i++) {
            
            String taskClassName = RollerConfig.getProperty("tasks."+tasks[i]+".class");
            if(taskClassName != null) {
                mLogger.info("Initializing task: "+tasks[i]);
                
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
                    mLogger.warn("Task does not extend RollerTask class", ex);
                } catch (RollerException ex) {
                    mLogger.error("Error scheduling task", ex);
                } catch (Exception ex) {
                    mLogger.error("Error instantiating task", ex);
                }
            }
        }
    }
    
    
    // Initialize ping features
    private void initializePingFeatures(Roller roller) throws RollerException {
        
        // Initialize common targets from the configuration
        PingConfig.initializeCommonTargets();
        // Initialize ping variants
        PingConfig.initializePingVariants();
        // Remove custom ping targets if they have been disallowed
        if (PingConfig.getDisallowCustomTargets()) {
            mLogger.info("Custom ping targets have been disallowed.  Removing any existing custom targets.");
            roller.getPingTargetManager().removeAllCustomPingTargets();
        }
        // Remove all autoping configurations if ping usage has been disabled.
        if (PingConfig.getDisablePingUsage()) {
            mLogger.info("Ping usage has been disabled.  Removing any existing auto ping configurations.");
            roller.getAutopingManager().removeAllAutoPings();
        }
    }
    
    
    protected void initializeSecurityFeatures(ServletContext context) {
        
        ApplicationContext ctx =
                WebApplicationContextUtils.getRequiredWebApplicationContext(context);
        
        String rememberMe = RollerConfig.getProperty("rememberme.enabled");
        boolean rememberMeEnabled = Boolean.valueOf(rememberMe).booleanValue();
        
        mLogger.info("Remember Me enabled: " + rememberMeEnabled);
        
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
                mLogger.error("Encryption algorithm '" + algorithm +
                        "' not supported, disabling encryption.");
            }
            if (encoder != null) {
                provider.setPasswordEncoder(encoder);
                mLogger.info("Password Encryption Algorithm set to '" + algorithm + "'");
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
    
    
    protected void upgradeDatabaseIfNeeded() throws RollerException {
        
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:comp/env/jdbc/rollerdb");
            Connection con = ds.getConnection();
            UpgradeDatabase.upgradeDatabase(con, mVersion);
            con.close();
        } catch (NamingException e) {
            mLogger.warn("Unable to access DataSource", e);
        } catch (SQLException e) {
            mLogger.warn(e);
        }
    }
    
    
    private void setupIndexManager(Roller roller) throws RollerException {
        roller.getIndexManager();
    }
    
    
    public void sessionCreated(HttpSessionEvent se) {
        mSessionCount.increment();
        
        mLogger.debug("sessions="+ mSessionCount
                    + ":freemem=" + Runtime.getRuntime().freeMemory()
                    + ":totmem=" + Runtime.getRuntime().totalMemory());
    }
    
    
    public void sessionDestroyed(HttpSessionEvent se) {
        mSessionCount.decrement();
        
        mLogger.debug("sessions=" + mSessionCount
                    + ":freemem=" + Runtime.getRuntime().freeMemory()
                    + ":totalmem=" + Runtime.getRuntime().totalMemory());
    }
    
    
    /**
     * Get authenticator
     */
    public Authenticator getAuthenticator() {
        if (mAuthenticator == null) {
            try {
                Class authClass =
                        Class.forName(RollerConfig.getProperty("authenticator.classname"));
                mAuthenticator = (Authenticator) authClass.newInstance();
            } catch (Exception e) {
                // this isn't an ERROR if no authenticatorClass was specified
                if (!(e instanceof NullPointerException)) {
                    mLogger.error("ERROR creating authenticator, using default", e);
                } else {
                    mLogger.debug("No authenticator specified, using DefaultAuthenticator");
                }
                mAuthenticator = new DefaultAuthenticator();
            }
        }
        return mAuthenticator;
    }
    
    
    /**
     * Get the ServletContext.
     *
     * @return ServletContext
     */
    public static ServletContext getServletContext() {
        return mContext;
    }
    
    
    /** Roller version */
    public String getRollerVersion() {
        return mVersion;
    }
    
    
    /** Roller build time */
    public String getRollerBuildTime() {
        return mBuildTime;
    }
    
    
    /** Get username that built Roller */
    public String getRollerBuildUser() {
        return mBuildUser;
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
        mLogger.warn("Unable to found specified Auto Provision class.", e);
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
              mLogger.warn("InstantiationException while creating: " + clazzName, e);
            } catch (IllegalAccessException e) {
              mLogger.warn("IllegalAccessException while creating: " + clazzName, e);
            }
          }
      }
      
      return null;
      
    }

}
