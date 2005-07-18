package org.roller.presentation.velocity;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.util.RequestUtils;
import org.apache.velocity.context.Context;
import org.apache.velocity.tools.view.context.ChainedContext;
import org.apache.velocity.tools.view.context.ToolboxContext;
import org.apache.velocity.tools.view.servlet.ServletToolboxManager;
import org.roller.RollerException;
import org.roller.config.RollerConfig;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.pojos.CommentData;
import org.roller.pojos.PageData;
import org.roller.pojos.RollerPropertyData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.LanguageUtil;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.newsfeeds.NewsfeedCache;
import org.roller.presentation.weblog.formbeans.CommentFormEx;
import org.roller.util.RegexUtil;
import org.roller.util.StringUtils;
import org.roller.util.Utilities;

/**
 * Load Velocity Context with Roller objects, values, and custom plugins.
 * 
 * @author llavandowska
 * @author David M Johnson
 */
public class ContextLoader
{   
    private RollerRequest mRollerReq = null;
    
    // List of PagePlugins for "transforming" WeblogEntries
    static List mPagePlugins = new ArrayList();
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(ContextLoader.class);

    //------------------------------------------------------------------------
    
    /**
     * Setup the a Velocity context by loading it with objects, values, and
     * RollerPagePlugins needed for Roller page execution.
     */
    public static void setupContext( Context ctx, 
        RollerRequest rreq, HttpServletResponse response ) 
        throws RollerException
    {
        mLogger.debug("setupContext( ctx = "+ctx+")");
        
        HttpServletRequest request = rreq.getRequest();
        RollerContext rollerCtx = RollerContext.getRollerContext( request );
        
        try 
        {    
            // Add page model object to context 
            String pageModelClassName = 
                RollerConfig.getProperty("velocity.pagemodel.classname");
            Class pageModelClass = Class.forName(pageModelClassName);
            PageModel pageModel = (PageModel)pageModelClass.newInstance();
            pageModel.init(rreq);
            ctx.put("pageModel", pageModel );
            ctx.put("pages", pageModel.getPages());
        }
        catch (Exception e)
        {
            throw new RollerException("ERROR creating Page Model",e);
        }
        
        // Add Velocity page helper to context
        PageHelper pageHelper = new PageHelper(rreq, response, ctx);
        pageHelper.initializePlugins(mPagePlugins);
        ctx.put("pageHelper", pageHelper );

        // Load standard Roller objects and values into the context 
        String handle = loadWebsiteValues(ctx, rreq, rollerCtx );
        loadWeblogValues( ctx, rreq, rollerCtx, handle );            
        loadPathValues( ctx, rreq, rollerCtx, handle );                                         
        loadRssValues( ctx, rreq, handle );                        
        loadUtilityObjects( ctx, rreq, rollerCtx, handle ); 
        loadRequestParamKeys(ctx);
        loadStatusMessage( ctx, rreq );
        
        // If single entry is specified, load comments too
        if ( rreq.getWeblogEntry() != null )
        {
            loadCommentValues( ctx, rreq, rollerCtx );
        }
        
        // add Velocity Toolbox tools to context
        loadToolboxContext(request, response, ctx);        
    }
    
    //------------------------------------------------------------------------
    
    /**
     * If there is an ERROR or STATUS message in the session,
     * place it into the Context for rendering later.
     * 
     * @param rreq
     */
    private static void loadStatusMessage(Context ctx, RollerRequest rreq)
    {
        HttpSession session = rreq.getRequest().getSession(false);
        String msg = null;
        if (session != null)
            msg = (String)session.getAttribute(RollerSession.ERROR_MESSAGE);
        if (msg != null)
        {
            ctx.put("errorMessage", msg);
            session.removeAttribute(RollerSession.ERROR_MESSAGE);
        }

        if (session != null)
            msg = (String)session.getAttribute(RollerSession.STATUS_MESSAGE);
        if (msg != null)
        {
            ctx.put("statusMessage", msg);
            session.removeAttribute(RollerSession.STATUS_MESSAGE);
        }
    }
    
    //------------------------------------------------------------------------

    /**
     * @param ctx
     * @param rreq
     * @param rollerCtx
     * @param userName
     */
    private static void loadWeblogValues(
       Context ctx, RollerRequest rreq, RollerContext rollerCtx, String handle)
       throws RollerException
    {
        // if there is an "_entry" page, only load it once
        WebsiteData website = 
            RollerFactory.getRoller().getUserManager().getWebsiteByHandle(handle);
        PageModel pageModel = (PageModel)ctx.get("pageModel");
        if (website != null && pageModel != null) 
        {
            /* alternative display pages - customization */
            PageData entryPage = pageModel.getUsersPageByName(website, "_entry");
            if (entryPage != null)
            {
                ctx.put("entryPage", entryPage);
            }
            PageData descPage = pageModel.getUsersPageByName(website, "_desc");
            if (descPage != null)
            {
                ctx.put("descPage", descPage);
            }
        }
    }

    private static String figureResourcePath( RollerRequest rreq )
    {
        /*  old way -- Allen G
        HttpServletRequest request = rreq.getRequest();
        RollerContext rCtx = RollerContext.getRollerContext( request );
        RollerConfigData  rollerConfig = rCtx.getRollerConfig();
    
        StringBuffer sb = new StringBuffer();
        String uploadPath = rollerConfig.getUploadPath();
        if ( uploadPath != null && uploadPath.trim().length() > 0 )
        {
            sb.append( uploadPath );
        }
        else
        {
            sb.append( request.getContextPath() );
            sb.append( RollerContext.USER_RESOURCES );
        }
        return sb.toString();
        */
        
        String uploadurl = null;
        try {
            uploadurl = RollerFactory.getRoller().getFileManager().getUploadUrl();
        } catch(Exception e) {}
        
        return uploadurl;
    }

    //------------------------------------------------------------------------
    
    public boolean isUserAuthorizedToEdit()
    {
        try
        {
            RollerSession rollerSession = RollerSession.getRollerSession(
                    mRollerReq.getRequest());
            return rollerSession.isUserAuthorizedToEdit();
        }
        catch (Exception e)
        {
            mLogger.warn("PageHelper.isUserAuthorizedToEdit)", e);
        }
        return false;
    }
    
    //------------------------------------------------------------------------
    
    protected static void loadCommentValues(
       Context ctx, RollerRequest rreq, RollerContext rollerCtx ) 
       throws RollerException
    {
        HttpServletRequest request = rreq.getRequest();
        
        String escapeHtml = RollerRuntimeConfig.getProperty("users.comments.escapehtml");
        String autoFormat = RollerRuntimeConfig.getProperty("users.comments.autoformat");
        
        // Add comments related values to context
        ctx.put("isCommentPage", Boolean.TRUE);
        ctx.put("escapeHtml", new Boolean(escapeHtml) );
        ctx.put("autoformat", new Boolean(autoFormat) );
        
        // Make sure comment form object is available in context
        CommentFormEx commentForm = 
            (CommentFormEx)request.getAttribute("commentForm");
        if ( commentForm == null )
        {
            commentForm = new CommentFormEx();
        
            // Set fields to spaces to please Velocity
            commentForm.setName("");
            commentForm.setEmail("");
            commentForm.setUrl("");
            commentForm.setContent("");
        }        
        ctx.put("commentForm",commentForm); 
            
        // Either put a preview comment in to context          
        if ( request.getAttribute("previewComments")!=null )
        {
            ArrayList list = new ArrayList();
            CommentData cd = new CommentData();
            commentForm.copyTo(cd, request.getLocale());
            list.add(cd);
            ctx.put("previewComments",list);            
        }
        WeblogEntryData entry = rreq.getWeblogEntry();
        ctx.put("entry",entry);            
    }   

    //------------------------------------------------------------------------
    
    protected static void loadPathValues(
        Context ctx, RollerRequest rreq, RollerContext rollerCtx, String userName) 
        throws RollerException
    {
        HttpServletRequest request = rreq.getRequest();
        String url = null;
        if ( userName != null && !userName.equals("zzz_none_zzz"))
        {
            url = Utilities.escapeHTML( 
                rollerCtx.getAbsoluteContextUrl(request)+"/page/"+userName);
        }
        else
        {
            url= Utilities.escapeHTML(rollerCtx.getAbsoluteContextUrl(request));
        }
        ctx.put("websiteURL", url);
        ctx.put("baseURL",    rollerCtx.getContextUrl( request ) );
        ctx.put("absBaseURL", rollerCtx.getAbsoluteContextUrl( request ) );
        ctx.put("ctxPath",    request.getContextPath() );
        ctx.put("uploadPath", ContextLoader.figureResourcePath( rreq ) );
        
        try
        {
            URL absUrl = RequestUtils.absoluteURL(request, "/");
            ctx.put("host", absUrl.getHost());
        }
        catch (MalformedURLException e)
        {
            throw new RollerException(e);
        }
    }

    //------------------------------------------------------------------------
    
    protected static void loadRequestParamKeys(Context ctx)
    {
        // Since Velocity *requires* accessor methods, these values from
        // RollerRequest are not available to it, put them into the context
        ctx.put("USERNAME_KEY",           RollerRequest.USERNAME_KEY);
        ctx.put("WEBSITEID_KEY",          RollerRequest.WEBSITEID_KEY);
        ctx.put("FOLDERID_KEY",           RollerRequest.FOLDERID_KEY);
        ctx.put("NEWSFEEDID_KEY",         RollerRequest.NEWSFEEDID_KEY);
        ctx.put("PAGEID_KEY",             RollerRequest.PAGEID_KEY);
        ctx.put("PAGELINK_KEY",           RollerRequest.PAGELINK_KEY);
        ctx.put("ANCHOR_KEY",             RollerRequest.ANCHOR_KEY);
        ctx.put("EXCERPTS_KEY",           RollerRequest.EXCERPTS_KEY);
        ctx.put("BOOKMARKID_KEY",         RollerRequest.BOOKMARKID_KEY);
        ctx.put("REFERERID_KEY",          RollerRequest.REFERERID_KEY);
        ctx.put("WEBLOGENTRYID_KEY",      RollerRequest.WEBLOGENTRYID_KEY);
        ctx.put("WEBLOGCATEGORYNAME_KEY", RollerRequest.WEBLOGCATEGORYNAME_KEY);
        ctx.put("WEBLOGCATEGORYID_KEY",   RollerRequest.WEBLOGENTRIES_KEY);
        ctx.put("WEBLOGENTRIES_KEY",      RollerRequest.WEBLOGENTRIES_KEY);
        ctx.put("WEBLOGDAY_KEY",          RollerRequest.WEBLOGDAY_KEY);
        ctx.put("WEBLOGCOMMENTID_KEY",    RollerRequest.WEBLOGCOMMENTID_KEY);
    }

    //------------------------------------------------------------------------
    
    protected static void loadRssValues(
       Context ctx, RollerRequest rreq, String handle) throws RollerException
    {
        HttpServletRequest request = rreq.getRequest();
        
        int entryLength = -1;
        String sExcerpts = request.getParameter("excerpts");
        if ( sExcerpts!=null && sExcerpts.equalsIgnoreCase("true"))
        {
            entryLength = 150;
        }
        ctx.put("entryLength",  new Integer(entryLength));
        
        int entryCount = 15;
        String sCount = request.getParameter("count");
        if ( sCount!=null && sExcerpts.trim().equals(""))
        {
            try
            {
                entryCount = Integer.parseInt(sCount);
            }
            catch (NumberFormatException e)
            {
                mLogger.warn("Improperly formatted count parameter");
            }
            if ( entryCount > 50 ) entryCount = 50;
            if ( entryCount < 0 ) entryCount = 15;
        }
        ctx.put("entryCount",  new Integer(entryCount));
            
        String catname = null;
        String catPath = null;
        if ( rreq.getWeblogCategory() != null )
        {
            catname = rreq.getWeblogCategory().getName();
            catPath = rreq.getWeblogCategory().getPath();
        } 
        ctx.put("catname", (catname!=null) ? catname : "");
        ctx.put("catPath", (catPath != null) ? catPath : "");
        ctx.put("updateTime", request.getAttribute("updateTime"));
        ctx.put("now", new Date());
    }

    //------------------------------------------------------------------------
    
    protected static void loadUtilityObjects(
        Context ctx, RollerRequest rreq, RollerContext rollerCtx, String handle)
        throws RollerException
    {

        // date formatter for macro's
        // set this up with the Locale to make sure we can reuse it with other patterns
        // in the macro's
        Locale viewLocale = (Locale) ctx.get("viewLocale");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", viewLocale);
        WebsiteData website = rreq.getWebsite();
        if (website != null)
        {
            sdf.setTimeZone(website.getTimeZoneInstance());
        }
        // add formatter to context
        ctx.put("dateFormatter", sdf );

        // Note: in the macro's, the formats are taken from the ResourceBundles.
        // Only the plainFormat is specified here, because it is used to render
        // the Entry Day link.
        ctx.put("plainFormat", "yyyyMMdd");

        ctx.put("page",            rreq.getPage() );
        ctx.put("utilities",       new Utilities() );
        ctx.put("stringUtils",     new StringUtils() );        
        ctx.put("rollerVersion",   rollerCtx.getRollerVersion() );
        ctx.put("rollerBuildTime", rollerCtx.getRollerBuildTime() );
        ctx.put("rollerBuildUser", rollerCtx.getRollerBuildUser() );
        ctx.put("newsfeedCache",   NewsfeedCache.getInstance() );
        
        ctx.put("requestParameters", rreq.getRequest().getParameterMap());
    }
    
    //------------------------------------------------------------------------
    
    protected static String loadWebsiteValues(
        Context ctx, RollerRequest rreq, RollerContext rollerCtx )
        throws RollerException
    {
        String handle = null;
        WebsiteData website = null;
        
        Roller mRoller = RollerFactory.getRoller();
        Map props = mRoller.getPropertiesManager().getProperties();
        
        if ( rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE) != null)
        {
            website = (WebsiteData)
                rreq.getRequest().getAttribute(RollerRequest.OWNING_WEBSITE);
        }
        else if ( rreq.getWebsite() != null )
        {
            website = rreq.getWebsite();
        }
        
        if ( website != null )
        {
            handle = website.getHandle();
            ctx.put("userName",      handle);
            ctx.put("fullName",      website.getName() );
            ctx.put("emailAddress",  website.getEmailAddress() );

            ctx.put("encodedEmail",  RegexUtil.encode(website.getEmailAddress()));
            ctx.put("obfuscatedEmail",  RegexUtil.obfuscateEmail(website.getEmailAddress()));
            
            // setup Locale for future rendering
            ctx.put("locale", website.getLocaleInstance());
           
            // setup Timezone for future rendering
            ctx.put("timezone", website.getTimeZoneInstance());
        }
        else
        {
            website = new WebsiteData();
            website.setName(((RollerPropertyData)props.get("site.name")).getValue());
            website.setAllowComments(Boolean.FALSE);
            website.setDescription(((RollerPropertyData)props.get("site.description")).getValue());
            handle = "zzz_none_zzz";
            ctx.put("userName", handle );
            ctx.put("fullName","zzz_none_zzz");
            ctx.put("emailAddress",
                ((RollerPropertyData)props.get("site.adminemail")).getValue());
            ctx.put("locale", Locale.getDefault());
            ctx.put("timezone", TimeZone.getDefault());
        }
        ctx.put("website", website );

        String siteName = ((RollerPropertyData)props.get("site.name")).getValue();
        if ("Roller-based Site".equals(siteName)) siteName = "Main";
        ctx.put("siteName", siteName);        

        // add language of the session (using locale of viewer set by Struts)
        ctx.put(
            "viewLocale",
            LanguageUtil.getViewLocale(rreq.getRequest()));
        mLogger.debug("context viewLocale = "+ctx.get( "viewLocale"));

        return handle;
    }
    
    //------------------------------------------------------------------------

    /**
     * Initialize PagePlugins declared in web.xml.  By using the full class
     * name we also allow for the implementation of "external" Plugins
     * (maybe even packaged seperately).  These classes are then later 
     * instantiated by PageHelper.
     * 
     * @param mContext
     */
    public static void initializePagePlugins(ServletContext mContext)
    {
        String pluginStr = RollerConfig.getProperty("plugins.page");
        if (mLogger.isDebugEnabled()) mLogger.debug(pluginStr);
        if (pluginStr != null)
        {
            String[] plugins = StringUtils.stripAll(
                                   StringUtils.split(pluginStr, ",") );
            for (int i=0; i<plugins.length; i++)
            {
                if (mLogger.isDebugEnabled()) mLogger.debug("try " + plugins[i]);
                try
                {
                    Class pluginClass = Class.forName(plugins[i]);
                    if (isPagePlugin(pluginClass))
                    {
                        mPagePlugins.add(pluginClass.newInstance());
                    }
                    else
                    {
                        mLogger.warn(pluginClass + " is not a PagePlugin");
                    }
                } 
                catch (ClassNotFoundException e)
                {
                    mLogger.error("ClassNotFoundException for " + plugins[i]);
                }
                catch (InstantiationException e)
                {
                    mLogger.error("InstantiationException for " + plugins[i]);
                }
                catch (IllegalAccessException e)
                {
                    mLogger.error("IllegalAccessException for " + plugins[i]);
                }
            }
        }
    }
    
    /**
     * @param pluginClass
     * @return
     */
    private static boolean isPagePlugin(Class pluginClass)
    {
        Class[] interfaces = pluginClass.getInterfaces();
        for (int i=0; i<interfaces.length; i++)
        {
            if (interfaces[i].equals(PagePlugin.class)) return true;
        }
        return false;
    }

    public static boolean hasPlugins()
    {
        mLogger.debug("mPluginClasses.size(): " + mPagePlugins.size());
        return (mPagePlugins != null && mPagePlugins.size() > 0);
    }
    
    public static List getPagePlugins()
    {
        return mPagePlugins;
    }


    private static final String TOOLBOX_KEY = 
        "org.roller.presentation.velocity.toolbox";

    private static final String TOOLBOX_MANAGER_KEY = 
        "org.roller.presentation.velocity.toolboxManager";

    private static ToolboxContext loadToolboxContext(
                    HttpServletRequest request, HttpServletResponse response, Context ctx) 
    {
        ServletContext servletContext = RollerContext.getServletContext();

        // get the toolbox manager
        ServletToolboxManager toolboxManager = 
            (ServletToolboxManager)servletContext.getAttribute(TOOLBOX_MANAGER_KEY);
        if (toolboxManager==null) 
        {
            String file = RollerConfig.getProperty("velocity.toolbox.file");
            mLogger.debug("Creating new toolboxContext using config-file: "+file);
            toolboxManager = ServletToolboxManager.getInstance(servletContext, file);
            servletContext.setAttribute(TOOLBOX_MANAGER_KEY, toolboxManager);
        }
        
        // load a toolbox context
        ChainedContext chainedContext = 
            new ChainedContext(ctx, request, response, servletContext);
        ToolboxContext toolboxContext = 
            toolboxManager.getToolboxContext(chainedContext);

        if (toolboxContext != null)
        {
            // add MessageTool to VelocityContext
            ctx.put("text", toolboxContext.internalGet("text"));
            
            /*          
            Object[] keys = toolboxContext.internalGetKeys();
            for (int i=0;i<keys.length;i++) {
                String key = (String)keys[i];
                System.out.println("key = "+key);
                Object tool = toolboxContext.get(key);
                System.out.println("tool = "+tool);
                ctx.put(key, tool);
            }
            */  
        }
        
        return toolboxContext;  
    }
}
