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
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.sql.DataSource;

import org.acegisecurity.providers.ProviderManager;
import org.acegisecurity.providers.dao.DaoAuthenticationProvider;
import org.acegisecurity.providers.encoding.Md5PasswordEncoder;
import org.acegisecurity.providers.encoding.PasswordEncoder;
import org.acegisecurity.providers.encoding.ShaPasswordEncoder;
import org.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.utils.UpgradeDatabase;
import org.apache.roller.config.PingConfig;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.ScheduledTask;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.pings.PingQueueTask;
import org.apache.roller.util.cache.CacheManager;
import org.apache.struts.util.RequestUtils;
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
        
        // set the roller context real path in RollerConfig
        // NOTE: it seems that a few backend classes do actually need
        //       to know what the real path to the roller context is,
        //       so we set this property to give them the info they need.
        //
        //       this is really not a best practice and we should try to
        //       remove these dependencies on the webapp context if possible
        RollerConfig.setContextRealPath(mContext.getRealPath("/"));
        
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
            setupPingQueueTask(roller);
            setupScheduledTasks(mContext, roller);
            
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
            InputStream instream = this.mContext.getResourceAsStream("/WEB-INF/velocity.properties");
            
            velocityProps.load(instream);
            
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
    
    
    /** Setup daily and hourly tasks specified in web.xml */
    private void setupScheduledTasks(ServletContext context, Roller roller)
            throws RollerException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        
        // setup the hourly tasks
        String hourlyString = RollerConfig.getProperty("tasks.hourly");
        if (hourlyString != null && hourlyString.trim().length() > 0) {
            String[] hourlyTasks = StringUtils.stripAll(
                    StringUtils.split(hourlyString, ",") );
            for (int i=0; i<hourlyTasks.length; i++) {
                mLogger.info("Setting hourly task: "+hourlyTasks[i]);
                ScheduledTask task =
                        (ScheduledTask)Class.forName(hourlyTasks[i]).newInstance();
                task.init(roller, mContext.getRealPath("/"));
                roller.getThreadManager().scheduleHourlyTimerTask((TimerTask)task);
            }
        }
        
        // setup the daily tasks
        String dailyString = RollerConfig.getProperty("tasks.daily");
        if (dailyString != null && dailyString.trim().length() > 0) {
            String[] dailyTasks = StringUtils.stripAll(
                    StringUtils.split(dailyString, ",") );
            for (int j=0; j<dailyTasks.length; j++) {
                mLogger.info("Setting daily task: "+dailyTasks[j]);
                ScheduledTask task =
                        (ScheduledTask)Class.forName(dailyTasks[j]).newInstance();
                task.init(roller, mContext.getRealPath("/"));
                roller.getThreadManager().scheduleDailyTimerTask((TimerTask)task);
            }
        }
    }
    
    
    // Initialize ping features
    private void initializePingFeatures(Roller roller) throws RollerException {
        
        // Initialize common targets from the configuration
        PingConfig.initializeCommonTargets();
        // Remove csutom ping targets if they have been disallowed
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
    
    
    // Set up the ping queue processing task
    private void setupPingQueueTask(Roller roller) throws RollerException {
        
        long intervalMins = PingConfig.getQueueProcessingIntervalMins();
        if (intervalMins == 0) {
            // Ping queue processing interval of 0 indicates that ping queue processing is disabled on this host.
            // This provides a crude  way to disable running the ping queue task on some servers if there are
            // multiple servers in a cluster sharing a db.  Exclusion should really be handled dynamically but isn't.
            mLogger.warn("Ping queue processing interval is zero; processing from the ping queue will be disabled on this server.");
            mLogger.warn("Please make sure that ping queue processing is configured to run on one server in the cluster.");
            return;
        }
        
        // Set up the task
        PingQueueTask pingQueueTask = new PingQueueTask();
        pingQueueTask.init(this, intervalMins);
        
        // Schedule it at the appropriate interval, delay start for one interval.
        mLogger.info("Scheduling ping queue task to run at " + intervalMins + " minute intervals.");
        roller.getThreadManager().scheduleFixedRateTimerTask(pingQueueTask, intervalMins, intervalMins);
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
     * Returns the full url for the website of the specified username.
     */
    public String getContextUrl(HttpServletRequest request, WebsiteData website) {
        String url = this.getContextUrl(request);
        if (website != null) {
            url = url + "/page/" + website.getHandle();
        }
        return url;
    }
    
    
    /** Get absolute URL of Roller context */
    public String getContextUrl(HttpServletRequest request) {
        String url = request.getContextPath();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
    
    
    /** Get absolute URL of Roller context */
    public String getAbsoluteContextUrl(HttpServletRequest request) {
        String url = RollerConfig.getProperty("context.absPath");
        if (url == null) {
            url = RollerRuntimeConfig.getProperty("site.absoluteurl");
            if (url == null || url.trim().length() == 0) {
                try {
                    URL absURL = RequestUtils.absoluteURL(request, "/");
                    url = absURL.toString();
                } catch (MalformedURLException e) {
                    url = "/";
                    mLogger.error("ERROR: forming absolute URL", e);
                }
            }
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }   
            RollerConfig.setAbsoluteContextPath(url);
        }
        return url;
    }
    
    
    /**
     * For use by MetaWeblog API.
     *
     * @return Context URL or null if not initialized yet.
     */
    public String getAbsoluteContextUrl() {
        return (String) mContext.getAttribute("org.apache.roller.absoluteContextURL");
    }
    
    
    public String createEntryPermalink(
            WeblogEntryData entry,
            HttpServletRequest request,
            boolean absolute) {
        String link = null;
        try {
            String baseUrl = null;
            if (absolute) {
                baseUrl = getAbsoluteContextUrl(request);
            } else {
                baseUrl = getContextUrl(request);
            }
            link = StringEscapeUtils.escapeHtml(baseUrl + entry.getPermaLink());
        } catch (Exception e) {
            mLogger.error("Unexpected exception", e);
        }
        
        return link;
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

}
