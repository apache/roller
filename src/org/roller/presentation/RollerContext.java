package org.roller.presentation;

import EDU.oswego.cs.dl.util.concurrent.SynchronizedInt;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.roller.RollerException;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.RollerSpellCheck;
import org.roller.pojos.RollerConfig;
import org.roller.pojos.UserData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.velocity.ContextLoader;
import org.roller.presentation.weblog.search.IndexManager;
import org.roller.presentation.weblog.search.operations.RebuildUserIndexOperation;
import org.roller.presentation.website.ThemeCache;
import org.roller.util.DateUtil;
import org.roller.util.RollerConfigFile;
import org.roller.util.StringUtils;
import org.roller.util.ThreadManager;
import org.roller.util.Utilities;

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

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSessionEvent;
import javax.sql.DataSource;

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

    private IndexManager mIndexManager = null;
    private ThreadManager mThreadManager = null;
    private static String mCastorXML = null;

    public static final String ROLLER_KEY = "session.roller";
    public static final String ROLLER_CONTEXT = "roller.context";
    public static final String USER_RESOURCES = "/resources";
    public static final String VERSION_KEY = "org.roller.version";
    public static final String USER_DISPLAY_BEAN_KEY = "org.roller.users";
    public static final String MEMDEBUG_KEY = "org.roller.memdebug";
    public static final String INDEX_MGR_KEY = "org.roller.indexMgr";
    private static final String ROLLER_IMPL_KEY = "org.roller.persistence";

    private static ServletContext mContext = null;
    private static Authenticator mAuthenticator = null;
    private RollerConfig mConfig = null;
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
        mIndexManager.dispose();
        mThreadManager.shutdown();
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
            mCastorXML = "";
            String rollerImplClass = mContext.getInitParameter(ROLLER_IMPL_KEY);
            if (rollerImplClass != null && rollerImplClass.indexOf("castor") > -1) 
            {
                mCastorXML = mContext.getResource("/WEB-INF/database.xml").toString();
            }
            RollerFactory.setRoller(rollerImplClass, mCastorXML);

            setupRollerConfig();

            upgradeDatabaseIfNeeded();

            setupSpellChecker();

            setupThreadManager();

            setupIndexManager();

            setupPagePlugins();

            setupRefererManager();
            
            setupBlacklistUpdater();

            String flag = mContext.getInitParameter(MEMDEBUG_KEY);
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

    private void upgradeDatabaseIfNeeded() throws RollerException
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
    private void setupRefererManager()
    {
        try
        {
            final RefererManager refManager =
                RollerFactory.getRoller().getRefererManager();

            refManager.setThreadManager(mThreadManager);

            // Check for turnover when we first start
            refManager.checkForTurnover(false, null);

            // Schedule a check every day, starting at end of today
            mThreadManager.scheduleDailyTimerTask(
                new TurnoverReferersTask(refManager));

        }
        catch (RollerException e)
        {
            mLogger.warn("Couldn't schedule referer turnover task", e);
        }
    }

    //------------------------------------------------------------------------
    /**
     * Add BlacklistUpdateTask to run on a schedule.
     */
    private void setupBlacklistUpdater()
    {
        mThreadManager.scheduleDailyTimerTask(new BlacklistUpdateTask(mConfig.getUploadDir()));
    }

    //------------------------------------------------------------------------

    private void setupThreadManager()
    {
        mThreadManager = new ThreadManager();
    }

    private void setupIndexManager()
    {
        String indexDir = getRollerConfig().getIndexDir();

        if (indexDir.indexOf("${user.home}") != -1)
        {
            indexDir =
                StringUtils.replace(
                    indexDir,
                    "${user.home}",
                    System.getProperty("user.home"));
        }

        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("index dir: " + indexDir);
        }

        mIndexManager = new IndexManager(indexDir, mThreadManager);

        if (mIndexManager.isInconsistentAtStartup())
        {
            mLogger.info(
                "Index was inconsistent. Rebuilding index in the background...");
            mIndexManager.scheduleIndexOperation(
                new RebuildUserIndexOperation(null));
        }

    }

    public IndexManager getIndexManager()
    {
        return mIndexManager;
    }

    public ThreadManager getThreadManager()
    {
        return mThreadManager;
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
        Roller roller = RollerFactory.getRoller();
        if (roller == null)
        {
            RollerFactory.setRoller(
                mContext.getInitParameter(ROLLER_IMPL_KEY),
                mCastorXML);
            roller = RollerFactory.getRoller();
        }
        return roller;
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
                    Class.forName(
                        mContext.getInitParameter(
                            "org.roller.authenticatorClass"));
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

    //-----------------------------------------------------------------------
    /**
     * Returns RollerConfig object. Changed so that it always tries
     * to fetch from the database first. If it should error, return
     * the stored copy.
     */
    public RollerConfig getRollerConfig()
    {
        if (mConfig == null)
        {
            try 
            {
    			mConfig = getRoller(null).getConfigManager().getRollerConfig();
    		} 
            catch (RollerException e) 
            {
    			mLogger.error("Unable to get RollerConfig from database");
    		}
        }
        return mConfig;
    }

    //-----------------------------------------------------------------------
    /**
     *  Gets the hard-drive location of the upload directory.
     */
    public static String getUploadDir(ServletContext app)
    {
        RollerConfig rollerConfig =
            RollerContext.getRollerContext(app).getRollerConfig();
        // if the app doesn't have a value, figure it out
        // from the ServletContext
        if (StringUtils.isEmpty(rollerConfig.getUploadDir()))
        {
            try
            {
                // ServletContext getRealPath() vs. getResource()
                //
                // The getResource() returns a paths rooted at c:\localhost
                // on my machine and I cannot figure out how to configure
                // it to do otherwise (I'm using Tomcat 4.0.4).
                //
                // Is getResource() the right thing and if so, how do you
                // configure getResource() to point at your out-of-context
                // resources? Do you use the Tomcat server.xml <Resources> tag?
                // My attempts at this failed and I couldn't Google an example.
                //
                // Anyhow...
                //
                // getRealPath() should work for everybody since we are
                // already assuming (for now) that we are not deployed-as
                // and running from a WAR.

                String dir =
                    app.getRealPath(RollerContext.USER_RESOURCES) + "/";

                //java.net.URL path = app.getResource(
                //RollerContext.USER_RESOURCES );
                // String dir = path.getPath() + "/";

                return dir;
            }
            //catch (java.net.MalformedURLException mue)
            catch (Exception mue)
            {
                System.err.println("Unable to load app.getResource()");
            }
            return null;
        }

        // otherwise return the value in app
        String path = rollerConfig.getUploadDir();
        if (!path.endsWith("/") && !path.endsWith("\\"))
        {
            path = path + "/";
        }
        return path;
    }

    //-----------------------------------------------------------------------
    /**
     * Gets the base url for the upload directory.
     */
    public static String getUploadPath(ServletContext app)
    {
        RollerConfig rollerConfig =
            RollerContext.getRollerContext(app).getRollerConfig();
        // if the app doesn't have a value, figure it out
        // from the ServletContext
        if (StringUtils.isEmpty(rollerConfig.getUploadPath()))
        {
            return RollerContext.USER_RESOURCES;
        }

        // otherwise return the value in app
        String path = rollerConfig.getUploadPath();
        if (path.endsWith("/") || path.endsWith("\\"))
        {
            path = path.substring(0, path.length() - 1);
        }
        return path;
    }

    //-----------------------------------------------------------------------
	/**
	 * Read Roller configuration parameters from roller-config.xml.
	 */
	private void setupRollerConfig() throws RollerException
	{
	    mConfig = getRoller(null).getConfigManager().getRollerConfig();
	    
	    if (mConfig == null)
	    {
	        mConfig = getRoller(null).getConfigManager().readFromFile(getConfigPath());
            saveRollerConfig(mConfig);
	    }	    
	}

    //-----------------------------------------------------------------------
    public void saveRollerConfig(RollerConfig rConfig) throws RollerException
    {
        mConfig = rConfig;
        
        // save database copy
        //getRoller(null).begin(); // begin already called by RequestFilter
        getRoller(null).getConfigManager().storeRollerConfig(rConfig);
        getRoller(null).commit();
        
        // save file copy
        RollerConfigFile rConfigFile = new RollerConfigFile(rConfig);
        rConfigFile.writeConfig(getConfigPath());
    }

    //-----------------------------------------------------------------------

    /**
     * Determine where we should read/write roller-config.xml to.
     * A file in ${user.home} overrides one in WEB-INF.
     */
    public String getConfigPath()
    {
        String configPath =
            System.getProperty("user.home")
                + File.separator
                + "roller-config.xml";

        if (mLogger.isDebugEnabled())
        {
            mLogger.debug(
                "Looking for roller-config.xml at '" + configPath + "'");
        }

        if (!new File(configPath).exists())
        {
            // No config found in user.home, store it in WEB-INF instead.
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("File not found: '" + configPath);
            }

            String root = mContext.getRealPath("/");
            configPath =
                root
                    + File.separator
                    + "WEB-INF"
                    + File.separator
                    + "roller-config.xml";
        }

        mLogger.info("Using roller-config.xml at '" + configPath + "'");

        return configPath;
    }

    //-----------------------------------------------------------------------

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
        String url = getRollerConfig().getAbsoluteURL();
        if (url == null || url.trim().length() == 0)
        {
            try
            {
                URL absURL = RequestUtils.absoluteURL(request, "/");
                url = absURL.toString();
            }
            catch (MalformedURLException e)
            {
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

            String dayString = DateUtil.format8chars(entry.getPubTime());

            UserData ud = entry.getWebsite().getUser();

            link =
                Utilities.escapeHTML(
                    baseUrl
                        + "/page/"
                        + ud.getUserName()
                        + "/"
                        + dayString
                        + "#"
                        + entry.getAnchor());
        }
        catch (Exception e)
        {
            mLogger.error("Unexpected exception", e);
        }
        return link;
    }

    //-----------------------------------------------------------------------

    public void refreshUserDisplayBeans()
    {
        mContext.removeAttribute(USER_DISPLAY_BEAN_KEY);
    }

    //-----------------------------------------------------------------------
    /**
     * Return the real filepath to the theme.
     *
     * @param theme
     * @return String
     */
    public String getThemePath(String theme)
    {
        RollerConfig rollerConfig = this.getRollerConfig();

        // Figure path to new user theme
        return mContext.getRealPath(
            "/" + rollerConfig.getUserThemes() + "/" + theme);
    }

    //-----------------------------------------------------------------------
    /**
     * Get the list of Theme names.  This consists
     * of the directories under the /themes directory.
     *
     * @return String[]
     */
    public String[] getThemeNames()
    {
        RollerConfig rollerConfig = this.getRollerConfig();
        String themesPath =
            mContext.getRealPath("/" + rollerConfig.getUserThemes());
        File themeDir = new File(themesPath);
        return themeDir.list(new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                File file =
                    new File(dir.getAbsolutePath() + File.separator + name);
                return file.isDirectory();
            }
        });
    }

    //-----------------------------------------------------------------------
    /**
     * Returns the mContext.
     * @return ServletContext
     */
    public static ServletContext getServletContext()
    {
        return mContext;
    }

    //-----------------------------------------------------------------------
    /**
     * Reads the Theme files from harddisk (if necessary) and places them into a
     * ThemeCache.  If the requested Theme's pages are already in the cache,
     * return them instead.
     *
     * @param themeName
     * @return HashMap
     * @throws FileNotFoundException
     * @throws IOException
     */
    public HashMap readThemeMacros(String themeName)
        throws FileNotFoundException, IOException
    {
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("themeName=" + themeName);
        }

        // Load all Velocity templates from root directory of the theme
        String pageName = null;
        String themeDir = this.getThemePath(themeName);
        String[] children = getThemeFilenames(themeDir);
        HashMap pages = new HashMap();
        ThemeCache themeCache = ThemeCache.getInstance();
        for (int i = 0; i < children.length; i++)
        {
            pageName = children[i].substring(0, children[i].length() - 3);

            if (themeCache.getFromCache(themeName, pageName) != null)
            {
                pages.put(
                    pageName,
                    themeCache.getFromCache(themeName, pageName));
            }
            else
            {
                BufferedReader rdr =
                    new BufferedReader(
                        new FileReader(
                            themeDir + File.separator + children[i]));
                String line = null;
                StringBuffer sb = new StringBuffer();
                while (null != (line = rdr.readLine()))
                {
                    sb.append(line);
                    sb.append("\n");
                }

                pages.put(pageName, sb.toString());
                themeCache.putIntoCache(themeName, pageName, sb.toString());
            }
        }
        return pages;
    }

    //-----------------------------------------------------------------------

    public static String[] getThemeFilenames(String themeDir)
    {
        ThemeCache themeCache = ThemeCache.getInstance();
        if (themeCache.getFileList(themeDir) != null)
        {
            return themeCache.getFileList(themeDir);
        }

        File dir = new File(themeDir);
        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".vm");
            }
        };
        String[] children = dir.list(filter);
        themeCache.setFileList(themeDir, children);
        return children;
    }

    //-----------------------------------------------------------------------

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


}
