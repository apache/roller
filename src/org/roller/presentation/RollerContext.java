package org.roller.presentation;

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
import net.sf.acegisecurity.providers.ProviderManager;
import net.sf.acegisecurity.providers.dao.DaoAuthenticationProvider;
import net.sf.acegisecurity.providers.encoding.Md5PasswordEncoder;
import net.sf.acegisecurity.providers.encoding.PasswordEncoder;
import net.sf.acegisecurity.providers.encoding.ShaPasswordEncoder;
import net.sf.acegisecurity.securechannel.ChannelProcessingFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.business.utils.UpgradeDatabase;
import org.roller.config.PingConfig;
import org.roller.config.RollerConfig;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.RollerSpellCheck;
import org.roller.model.ScheduledTask;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.pings.PingQueueTask;
import org.roller.presentation.velocity.CommentAuthenticator;
import org.roller.presentation.velocity.DefaultCommentAuthenticator;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.WebApplicationContextUtils;
import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.SecurityConfig;
import net.sf.acegisecurity.intercept.web.PathBasedFilterInvocationDefinitionMap;
import net.sf.acegisecurity.ui.webapp.AuthenticationProcessingFilterEntryPoint;


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
    private boolean mMemDebug = false;
    
    
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
    public static RollerContext getRollerContext(ServletContext scc) {
        // get roller from servlet context
        ServletContext sc = RollerContext.getServletContext();
        return (RollerContext) sc.getAttribute(ROLLER_CONTEXT);
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
    }
    
    
    /**
     * Responds to context initialization event by processing context
     * paramters for easy access by the rest of the application.
     */
    public void contextInitialized(ServletContextEvent sce) {
        
        mLogger.debug("RollerContext initializing");
        
        // Save context in self and self in context
        mContext = sce.getServletContext();
        mContext.setAttribute(ROLLER_CONTEXT, this);
        
        try {
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
            
            // always upgrade database first
            upgradeDatabaseIfNeeded();
            
            Roller roller = RollerFactory.getRoller();
            roller.begin(UserData.SYSTEM_USER);
            
            setupRollerProperties();
            roller.getThemeManager();
            setupSpellChecker();
            setupIndexManager(roller);
            initializePingFeatures(roller);
            setupPingQueueTask(roller);
            setupScheduledTasks(mContext, roller);
            
            // call Spring's context ContextLoaderListener to initialize
            // all the context files specified in web.xml. This is necessary
            // because listeners don't initialize in the order specified in
            // 2.3 containers
            super.contextInitialized(sce);
            
            initializeSecurityFeatures(mContext);
            
            roller.commit();
            roller.release();
            
            String flag = RollerConfig.getProperty("debug.memory.enabled");
            if (flag != null && !flag.equalsIgnoreCase("false")) {
                mMemDebug = true;
            }
            
            mLogger.debug("RollerContext initialization complete");
        } catch (Throwable t) {
            mLogger.fatal("RollerContext initialization failed", t);
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
            defmap.addSecureUrl("/**/*.do*", insecureDef);
        }
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
        if (mMemDebug) {
            mSessionCount.increment();
            mContext.log(
                    "Roller:SESSION_CREATED:count="
                    + mSessionCount
                    + ":freemem="
                    + Runtime.getRuntime().freeMemory()
                    + ":totmem="
                    + Runtime.getRuntime().totalMemory());
        }
    }
    
    
    public void sessionDestroyed(HttpSessionEvent se) {
        if (mMemDebug) {
            mSessionCount.decrement();
            mContext.log(
                    "Roller:SESSION_DESTROY:count="
                    + mSessionCount
                    + ":freemem="
                    + Runtime.getRuntime().freeMemory()
                    + ":totalmem="
                    + Runtime.getRuntime().totalMemory());
        }
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
     *  Gets the hard-drive location of the upload directory.
     */
    public static String getUploadDir(ServletContext app) {
        // ACK ... this really isn't the right place for this lookup!!
        String uploaddir = null;
        try {
            uploaddir = RollerFactory.getRoller().getFileManager().getUploadDir();
        } catch(Exception e) {}
        
        return uploaddir;
    }
    
    
    /**
     * Gets the base url for the upload directory.
     */
    public static String getUploadPath(ServletContext app) {
        // ACK ... why do this here??
        String uploadurl = null;
        try {
            uploadurl = RollerFactory.getRoller().getFileManager().getUploadUrl();
        } catch(Exception e) {}
        
        return uploadurl;
    }
    
    
    /**
     * RollerSpellCheck must be initialized with a dictionary file
     * so that it can return valid a SpellChecker.
     */
    private void setupSpellChecker() {
        InputStream is = null;
        try {
            is = mContext.getResourceAsStream("/WEB-INF/english.0");
            RollerSpellCheck.init(is);
        } catch (Exception e) {
            mContext.log("ERROR reading dictionary file");
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
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
        
        String url = RollerRuntimeConfig.getProperty("site.absoluteurl");
        
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
        
        mContext.setAttribute("org.roller.absoluteContextURL", url);
        return url;
    }
    
    
    /**
     * For use by MetaWeblog API.
     *
     * @return Context URL or null if not initialized yet.
     */
    public String getAbsoluteContextUrl() {
        return (String) mContext.getAttribute("org.roller.absoluteContextURL");
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
            link = Utilities.escapeHTML(baseUrl + entry.getPermaLink());
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
