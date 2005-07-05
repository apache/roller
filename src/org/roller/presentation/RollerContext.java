package org.roller.presentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.RollerSpellCheck;
import org.roller.model.ScheduledTask;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.velocity.CommentAuthenticator;
import org.roller.presentation.velocity.ContextLoader;
import org.roller.presentation.velocity.DefaultCommentAuthenticator;
import org.roller.presentation.website.ThemeCache;
import org.roller.presentation.pings.PingQueueTask;
import org.roller.util.DateUtil;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;
import org.roller.config.RollerRuntimeConfig;
import org.roller.config.PingConfig;

//////////////////////////////////////////////////////////////////////////////
/**
 * Responds to app init/destroy events and holds Roller instance.
 * @web.listener
 */
public class RollerContext implements ServletContextListener
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(RollerContext.class);

    private String mVersion = null;
    private String mBuildTime = null;
    private String mBuildUser = null;

    public static final String ROLLER_KEY = "session.roller";
    public static final String ROLLER_CONTEXT = "roller.context";
    public static final String USER_RESOURCES = "/resources";
    public static final String VERSION_KEY = "org.roller.version";
    public static final String MEMDEBUG_KEY = "org.roller.memdebug";
    public static final String INDEX_MGR_KEY = "org.roller.indexMgr";
    public static final String HTTP_PORT = "http_port";
    public static final String HTTPS_PORT = "https_port";
    public static final String HTTPS_HEADER_NAME = "https_header_name";
    public static final String HTTPS_HEADER_VALUE = "https_header_value";
    private static final String HOURLY_TASK_KEY = "roller.hourly.tasks";
    private static final String DAILY_TASK_KEY = "roller.daily.tasks";
    
    private static final String ROLLER_IMPL_KEY 
        = "org.roller.persistence";
    
    private static final String ROLLER_COMMENT_AUTH_KEY 
        = "org.roller.commentAuthenticatorClass";

    private static ServletContext mContext = null;
    private static Authenticator mAuthenticator = null;
    private static CommentAuthenticator mCommentAuthenticator = null;
    private final SynchronizedInt mSessionCount = new SynchronizedInt(0);
    private boolean mMemDebug = false;

    /**
     * Constructor for RollerContext.
     */
    public RollerContext()
    {
        super();
        Properties props = new Properties();
        try
        {
            props.load(getClass().getResourceAsStream("/version.properties"));
        }
        catch (IOException e)
        {
            mLogger.error("version.properties not found", e);
        }
        mVersion = props.getProperty("ro.version", "UNKNOWN");
        mBuildTime = props.getProperty("ro.buildTime", "UNKNOWN");
        mBuildUser = props.getProperty("ro.buildUser", "UNKNOWN");
    }

    //-----------------------------------------------------------------------
    /* Returns Roller instance for specified app */
    public static RollerContext getRollerContext(ServletContext sc)
    {
        // get roller from servlet context
        return (RollerContext) sc.getAttribute(ROLLER_CONTEXT);
    }

    //-----------------------------------------------------------------------
    /* Returns Roller instance for specified app */
    public static RollerContext getRollerContext(HttpServletRequest req)
    {
        // get roller from servlet context
        ServletContext sc = RollerContext.getServletContext();
        return (RollerContext) sc.getAttribute(ROLLER_CONTEXT);
    }

    //-----------------------------------------------------------------------
    /** Responds to app-destroy by saving the indexManager's information */
    public void contextDestroyed(ServletContextEvent sce)
    {
        RollerFactory.getRoller().shutdown();
    }

    //-----------------------------------------------------------------------
    /**
     * Responds to context initialization event by processing context
     * paramters for easy access by the rest of the application.
     */
    public void contextInitialized(ServletContextEvent sce)
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("RollerContext initializing");
        }

        // Save context in self and self in context
        mContext = sce.getServletContext();
        mContext.setAttribute(ROLLER_CONTEXT, this);

        try
        {
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
            RollerConfig.setContextPath(mContext.getRealPath("/"));
            
            Roller roller = RollerFactory.getRoller();
            
            roller.begin(UserData.SYSTEM_USER);
            
            //setupRollerConfig();
            setupRollerProperties();
            roller.getThemeManager();
            upgradeDatabaseIfNeeded();
            setupSpellChecker();
            setupPagePlugins();
            setupIndexManager(roller);
            setupRefererManager(roller);
            initializePingFeatures(roller);
            setupPingQueueTask(roller);
            setupScheduledTasks(mContext, roller);
            
            roller.commit();
            roller.release();
            
            String flag = RollerConfig.getProperty("debug.memory.enabled");
            if (flag != null && !flag.equalsIgnoreCase("false"))
            {
                mMemDebug = true;
            }

            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("RollerContext initialization complete");
            }
        }
        catch (Throwable t)
        {
            mLogger.fatal("RollerContext initialization failed", t);
        }
    }

    
    private void setupRollerProperties() throws RollerException
    {
        // init property manager by creating it
        Roller mRoller = RollerFactory.getRoller();
        mRoller.getPropertiesManager();
    }

    /** Setup daily and hourly tasks specified in web.xml */
    private void setupScheduledTasks(ServletContext context, Roller roller) 
        throws RollerException, InstantiationException, 
               IllegalAccessException, ClassNotFoundException 
    {
        // setup the hourly tasks
        String hourlyString = RollerConfig.getProperty("tasks.hourly");
        if (hourlyString != null && hourlyString.trim().length() > 0)
        {
            String[] hourlyTasks = StringUtils.stripAll(
                    StringUtils.split(hourlyString, ",") );
            for (int i=0; i<hourlyTasks.length; i++)
            {
                mLogger.info("Setting hourly task: "+hourlyTasks[i]);
                ScheduledTask task = 
                    (ScheduledTask)Class.forName(hourlyTasks[i]).newInstance();
                task.init(roller, mContext.getRealPath("/"));
                roller.getThreadManager().scheduleHourlyTimerTask((TimerTask)task);
            }
        }
        
        // setup the daily tasks
        String dailyString = RollerConfig.getProperty("tasks.daily");
        if (dailyString != null && dailyString.trim().length() > 0)
        {
            String[] dailyTasks = StringUtils.stripAll(
                    StringUtils.split(dailyString, ",") );
            for (int j=0; j<dailyTasks.length; j++)
            {
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
    private void setupPingQueueTask(Roller roller) throws RollerException
    {
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

    protected void upgradeDatabaseIfNeeded() throws RollerException
    {
        try
        {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:comp/env/jdbc/rollerdb");
            Connection con = ds.getConnection();
            RollerFactory.getRoller().upgradeDatabase(con);
            con.close();
        }
        catch (NamingException e)
        {
            mLogger.warn("Unable to access DataSource", e);
        }
        catch (SQLException e)
        {
            mLogger.warn(e);
        }
    }

    //------------------------------------------------------------------------
    /**
     * Initialize the configured PagePlugins
     * @param mContext
     */
    private void setupPagePlugins()
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("Initialize PagePlugins");
        }
        ContextLoader.initializePagePlugins(mContext);

    }

    /**
     * Add TurnoverReferersTask to run on a schedule.
     */
    private void setupRefererManager(Roller roller)
    {
        try
        {
            // Check for turnover when we first start
            final RefererManager refManager = roller.getRefererManager();
            refManager.checkForTurnover(false, null);

            // Schedule a check every day, starting at end of today
            //TurnoverReferersTask task = new TurnoverReferersTask();
            //task.init(roller, mContext.getRealPath("/"));
            //roller.getThreadManager().scheduleDailyTimerTask(task);
        }
        catch (RollerException e)
        {
            mLogger.warn("Couldn't schedule referer turnover task", e);
        }
    }

    //------------------------------------------------------------------------

    private void setupIndexManager(Roller roller) throws RollerException
    {
        roller.getIndexManager();        
    }

    public void sessionCreated(HttpSessionEvent se)
    {
        if (mMemDebug)
        {
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

    //------------------------------------------------------------------------

    public void sessionDestroyed(HttpSessionEvent se)
    {
        if (mMemDebug)
        {
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

    //------------------------------------------------------------------------

    /**
     * Get an instance of Roller from the RollerFactory.
     * If one does not exist, tell Factory to create new one.
     */
    public static Roller getRoller(HttpServletRequest req)
    {
        return RollerFactory.getRoller();
    }

    //------------------------------------------------------------------------

    /**
     * Get authenticator
     */
    public Authenticator getAuthenticator()
    {
        if (mAuthenticator == null)
        {
            try
            {
                Class authClass =
                    Class.forName(RollerConfig.getProperty("authenticator.classname"));
                mAuthenticator = (Authenticator) authClass.newInstance();
            }
            catch (Exception e)
            {
                // this isn't an ERROR if no authenticatorClass was specified
                if (!(e instanceof NullPointerException))
                {    
                    mLogger.error("ERROR creating authenticator, using default", e);
                }
                else
                {
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
    public static String getUploadDir(ServletContext app)
    {
        // ACK ... this really isn't the right place for this lookup!!
        String uploaddir = null;
        try 
        {
            uploaddir = RollerFactory.getRoller().getFileManager().getUploadDir();
        } 
        catch(Exception e) {}
        
        return uploaddir;
    }


    /**
     * Gets the base url for the upload directory.
     */
    public static String getUploadPath(ServletContext app)
    {
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
    private void setupSpellChecker()
    {
        InputStream is = null;
        try
        {
            is = mContext.getResourceAsStream("/WEB-INF/english.0");
            RollerSpellCheck.init(is);
        }
        catch (Exception e)
        {
            mContext.log("ERROR reading dictionary file");
        }
        finally
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the full url for the website of the specified username.
     */
    public String getContextUrl(HttpServletRequest request, String username)
    {
        String url = this.getContextUrl(request);
        if (username != null)
        {
            url = url + "/page/" + username;
        }
        return url;
    }

    //-----------------------------------------------------------------------
    /** Get absolute URL of Roller context */
    public String getContextUrl(HttpServletRequest request)
    {
        String url = request.getContextPath();
        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    //-----------------------------------------------------------------------
    /** Get absolute URL of Roller context */
    public String getAbsoluteContextUrl(HttpServletRequest request)
    {
        String url = RollerRuntimeConfig.getProperty("site.absoluteurl");
        
        if (url == null || url.trim().length() == 0)
        {
            try
            {
                URL absURL = RequestUtils.absoluteURL(request, "/");
                url = absURL.toString();
            }
            catch (MalformedURLException e)
            {
                url = "/";
                mLogger.error("ERROR: forming absolute URL", e);
            }
        }
        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        mContext.setAttribute("org.roller.absoluteContextURL", url);
        return url;
    }

    //-----------------------------------------------------------------------

    /**
     * For use by MetaWeblog API.
     * @return Context URL or null if not initialized yet.
     */
    public String getAbsoluteContextUrl()
    {
        return (String) mContext.getAttribute("org.roller.absoluteContextURL");
    }

    //-----------------------------------------------------------------------

    public String createEntryPermalink(
        WeblogEntryData entry,
        HttpServletRequest request,
        boolean absolute)
    {
        String link = null;
        try
        {
            String baseUrl = null;
            if (absolute)
            {
                baseUrl = getAbsoluteContextUrl(request);
            }
            else
            {
                baseUrl = getContextUrl(request);
            }

            link = Utilities.escapeHTML(baseUrl + entry.getPermaLink());
        }
        catch (Exception e)
        {
            mLogger.error("Unexpected exception", e);
        }
        
        return link;
    }


    /**
     * Returns the mContext.
     * @return ServletContext
     */
    public static ServletContext getServletContext()
    {
        return mContext;
    }

    
    /** Roller version */
    public String getRollerVersion()
    {
        return mVersion;
    }

    //-----------------------------------------------------------------------

    /** Roller build time */
    public String getRollerBuildTime()
    {
        return mBuildTime;
    }

    //-----------------------------------------------------------------------

    /** Get username that built Roller */
    public String getRollerBuildUser()
    {
        return mBuildUser;
    }

    //-----------------------------------------------------------------------

    public static CommentAuthenticator getCommentAuthenticator() 
    {
        if (mCommentAuthenticator == null) 
        {
            String name = RollerConfig.getProperty("comment.authenticator.classname");
            try 
            {
                Class clazz = Class.forName(name);
                mCommentAuthenticator=(CommentAuthenticator)clazz.newInstance();
            }
            catch (Exception e)
            {
                mLogger.error(e);
                mCommentAuthenticator = new DefaultCommentAuthenticator();
            }
        }        
        return mCommentAuthenticator;
    }
}
