package org.roller.presentation.website.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.pojos.PageData;
import org.roller.pojos.UserData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.pagecache.PageCacheFilter;
import org.roller.presentation.velocity.PreviewResourceLoader;
import org.roller.presentation.website.ThemeCache;
import org.roller.presentation.website.formbeans.ThemeEditorForm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.roller.config.RollerRuntimeConfig;


/////////////////////////////////////////////////////////////////////////////

/**
 * Actions for theme chooser page.
 * 
 * @author llavandowska
 * 
 * @struts.action name="themeEditorForm" path="/editor/themeEditor"
 *    scope="session" parameter="method"
 * 
 * @struts.action-forward name="editTheme.page" path="/website/theme-editor.jsp"
 */
public class ThemeEditorAction extends DispatchAction
{
	private static final String SESSION_TEMPLATE = "weblog.template";
    private static final String LAST_THEME = "weblog.prev.theme";
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(ThemeEditorAction.class);
    private ThemeCache themeCache = ThemeCache.getInstance();
	
	/**
     * Take user to edit theme page.
     * 
     * @param mapping Struts action mapping.
     * @param form Theme editor form bean.
     * @param request Servlet request.
     * @param response Servlet response.
     * @return Forward to edit-website page.
     * @throws IOException
     * @throws ServletException
	 */
	//-----------------------------------------------------------------------
	public ActionForward edit(
		ActionMapping       mapping,
		ActionForm          form,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		ActionErrors errors = new ActionErrors();
		ActionForward forward = mapping.findForward("editTheme.page");
		try
		{
			RollerRequest rreq = RollerRequest.getRollerRequest(request);
			if ( rreq.isUserAuthorizedToEdit() )
			{
				loadThemes( rreq, errors, true);
				ThemeEditorForm teForm = (ThemeEditorForm)form;
				
				teForm.setThemeName(null); // start fresh
                
                if (mLogger.isDebugEnabled())
                {
                    mLogger.debug("loaded themes, form="+teForm);
                }  
                
//				// if a custom template has been previewed already,
//				// it'll be in memory.
//				String template = 
//                   (String)request.getSession().getAttribute(SESSION_TEMPLATE);
//                
//                if (mLogger.isDebugEnabled())
//                {
//                    mLogger.debug("got template="+template);
//                }
//                
//				if (StringUtils.isNotEmpty(template))
//				{
//					// load the template in memory
//					teForm.setThemeTemplate( template );
//                    
//                    if (mLogger.isDebugEnabled())
//                    {
//                        mLogger.debug("set template");
//                    }
//				}
//				// otherwise the "custom" template will need be loaded
//				// from the current page.
//				else
//				{
                
				// clear any previously set themes
                clearThemePages(rreq, 
					(String)request.getSession(true).getAttribute(LAST_THEME)); 
                                
			     // load the current default page
                PageData page = getDefaultPage( rreq );
					teForm.setThemeTemplate( page.getTemplate() );
					
					
				//}				
			}
			else
			{
				forward = mapping.findForward("access-denied");
			}
		}
		catch (Exception e)
		{
            mLogger.error("ERROR in action",e);
			throw new ServletException(e); 
		}
		return forward;
	}

	//-----------------------------------------------------------------------
	/**
	 * Load the template/theme to be previewed.  The template must be stashed
	 * in PreviewResourceLoader so that PreviewServlet can find it.
     * 
     * @param mapping Struts action mapping.
     * @param form Theme editor form bean.
     * @param request Servlet request.
     * @param response Servlet response.
     * @return Forward to edit-website page.
     * @throws IOException
     * @throws ServletException
	 */
	public ActionForward preview(
		ActionMapping       mapping,
		ActionForm          form,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		ActionErrors errors = new ActionErrors();
		ActionForward forward = mapping.findForward("editTheme.page");
		try
		{
			RollerRequest rreq = RollerRequest.getRollerRequest(request);
			if ( rreq.isUserAuthorizedToEdit() )
			{
                HttpSession session = request.getSession();
				ThemeEditorForm teForm = (ThemeEditorForm)form;
				
                String theme = teForm.getThemeName();
                ServletContext ctx = rreq.getServletContext();
                RollerContext rollerContext =
                    RollerContext.getRollerContext( ctx );
                                      
                boolean showCustom = false;
				if ( !"Custom".equals( theme ) )
				{
                    // if it isn't a custom template, load it from file
					String sb = this.readTheme(rollerContext, theme);
					teForm.setThemeTemplate( sb );

					// clear any previously set themes
					clearThemePages(rreq, 
						(String) session.getAttribute(LAST_THEME)); 
						                   
                    setThemePages(rreq, theme);
					session.setAttribute(LAST_THEME, theme);
				}
                else
                {
                	showCustom = true;
                    clearThemePages(rreq, 
                        (String) session.getAttribute(LAST_THEME));
                        
                    session.removeAttribute(LAST_THEME);
					//session.removeAttribute(SESSION_TEMPLATE);
                    
				   //UserData ud = rreq.getUser();
				   //PreviewResourceLoader.clearAllTemplates( ud.getUserName());
                }
				loadThemes( rreq, errors, showCustom);

				// put the template where PreviewServlet
				// will be able to find it
				PageData page = getDefaultPage( rreq );			
				PreviewResourceLoader.setTemplate(page.getId(), 
					teForm.getThemeTemplate(), rreq.getCurrentWebsite().getHandle() );
				
				// save the template in session for later editing
				session.setAttribute(SESSION_TEMPLATE,
					teForm.getThemeTemplate() );
			}
			else
			{
				forward = mapping.findForward("access-denied");
			}
		}
		catch (Exception e)
		{
            mLogger.error("ERROR in action",e);
			throw new ServletException(e);
		}
		return forward;
	}	

	//-----------------------------------------------------------------------
	/**
	 * Save the selected Theme or edited template as the Weblog pages template.
     * 
     * @param mapping Struts action mapping.
     * @param form Theme editor form bean.
     * @param request Servlet request.
     * @param response Servlet response.
     * @return Forward to edit-website page.
     * @throws IOException
     * @throws ServletException
	 */
	public ActionForward save(
		ActionMapping       mapping,
		ActionForm          form,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		ActionErrors errors = new ActionErrors();
		ActionForward forward = mapping.findForward("editTheme.page");
		try
		{
			RollerRequest rreq = RollerRequest.getRollerRequest(request);
			if ( rreq.isUserAuthorizedToEdit() )
			{
                 loadThemes( rreq, errors, true);
                 ThemeEditorForm teForm = (ThemeEditorForm)form;
                 String theme = teForm.getThemeName();
                 ServletContext ctx = rreq.getServletContext();
                 RollerContext rollerContext = 
                                RollerContext.getRollerContext( ctx );
                 
                 WebsiteData website = rreq.getCurrentWebsite();
                	
				// load the template either from the Form
				// or from the disk (if its a stock Theme).
				String template = "";
				if ( "Custom".equals( theme ) )
				{
					// use the edited template
					template = teForm.getThemeTemplate();
				}
				else
				{
					// Figure path to new user theme
					String sb = this.readTheme(rollerContext, theme);
					template = sb;
				}

                // clear the places holding onto the template
                PreviewResourceLoader.clearAllTemplates(website.getHandle());
                request.getSession().removeAttribute(SESSION_TEMPLATE);

				// store the template in the page
				UserManager mgr = rreq.getRoller().getUserManager();
				PageData page = getDefaultPage( rreq );

				page.setTemplate( template );
				mgr.storePage( page);
                
                saveThemePages( rreq, theme);
                
                // put them into the PreviewResourceLoader also
                setThemePages(rreq, theme);

				// clear the page cache
				PageCacheFilter.removeFromCache(request, rreq.getCurrentWebsite());
				teForm.setThemeName("Custom");
			}
			else
			{
				forward = mapping.findForward("access-denied");
			}
		}
		catch (Exception e)
		{
            mLogger.error("ERROR in action",e);
			throw new ServletException(e);
		}
		return forward;
	}
	

	/**
     * Cancel choosing of theme.
     * 
	 * @param mapping Struts action mapping.
	 * @param form Theme editor form bean.
	 * @param request Servlet request.
	 * @param response Servlet response.
	 * @return Forward to edit-website page.
	 * @throws IOException
	 * @throws ServletException
	 */
	public ActionForward cancel(
		ActionMapping       mapping,
		ActionForm          form,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		ActionForward forward = mapping.findForward("editTheme");
		try
		{
			RollerRequest rreq = RollerRequest.getRollerRequest(request);
			if ( rreq.isUserAuthorizedToEdit() )
			{
				// clear the page cache
				WebsiteData website = rreq.getCurrentWebsite();
				PageCacheFilter.removeFromCache( request, website );
                 ThemeEditorForm teForm = (ThemeEditorForm)form;
								
				// clear the places holding onto the template
				PreviewResourceLoader.clearAllTemplates( website.getHandle() );
				request.getSession().removeAttribute(SESSION_TEMPLATE);
				teForm.setThemeName("Custom");
			}
			else
			{
				forward = mapping.findForward("access-denied");
			}
		}
		catch (Exception e)
		{
            mLogger.error("ERROR in action",e);
			throw new ServletException(e);
		}
		return forward;
	}
	
	/**
	 * Load the Themes from disk ONCE per user session.
     * 
	 * @param rreq
	 * @param errors
	 */
	private void loadThemes( 
        RollerRequest rreq, ActionErrors errors, boolean listCustom)
	{
		HttpSession session = rreq.getRequest().getSession(false);
		try
		{
			// Figure path to new user templates
			ServletContext ctx = rreq.getServletContext();
			String[] themes = null;			
			if (ctx.getAttribute("themeStore") != null)
			{
				themes = (String[]) ctx.getAttribute("themeStore");
			}
			else
			{
				RollerContext rollerContext = 
								RollerContext.getRollerContext( ctx );
				themes = rollerContext.getThemeNames();
				ctx.setAttribute("themeStore", themes);
			}
			
			// need to insert "Custom" as the top theme.
			// "Custom" means the hand-edited template.
			if (listCustom)
			{
				// probably should use arraycopy here?
				String[] themes2 = new String[ themes.length+1 ];
				themes2[0] = "Custom";
				for (int i=1; i<themes2.length; i++)
				{
					themes2[i] = themes[i-1];
				}
				themes = themes2;
			}
			session.setAttribute( "themes", themes );
		}
		catch (Exception e)
		{
			errors.add(ActionErrors.GLOBAL_ERROR,
				new ActionError("error.editing.user", e.toString()));
		}
	}
	
	/**
	 * Get the Default Page for the website specified by request.
     * 
	 * @param rreq
	 * @return PageData
	 */
	private PageData getDefaultPage(RollerRequest rreq) throws RollerException
	{
		try
		{
			UserManager mgr = rreq.getRoller().getUserManager();
			WebsiteData wd = rreq.getCurrentWebsite();
			String defaultPageId = wd.getDefaultPageId();
			return mgr.retrievePage( defaultPageId );
		}
		catch (Exception e)
		{
            mLogger.error("ERROR in action",e);
			throw new RollerException( e );
		}
	}
    
    /**
     * Loads theme into preview resource loader.
     * 
     * @param rreq
     * @param theme
     * @throws RollerException
     */
    private void setThemePages( RollerRequest rreq, String theme )
       throws RollerException
    {
        RollerContext rollerContext = 
           RollerContext.getRollerContext(rreq.getRequest());
           
        try
        {        
            HashMap pages = rollerContext.readThemeMacros(theme);
            Iterator iter = pages.keySet().iterator();
            while ( iter.hasNext() )
            {
                String pageName = (String) iter.next();
                String sb = (String)pages.get( pageName );
                UserManager umgr = rreq.getRoller().getUserManager();
                WebsiteData website = rreq.getCurrentWebsite();
                String handle = website.getHandle();
                PageData page = umgr.getPageByName( rreq.getCurrentWebsite(), pageName );
                if (page != null)
                {
                    PreviewResourceLoader.setTemplate(page.getId(),sb, handle);
                }
                else
                {
                    PreviewResourceLoader.setTemplate(pageName, sb, handle);
                }
            }
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new RollerException( e );
        }
        
    }
    
    /**
     * Clears users preview theme from the preview resource loader.
     * 
     * @param rreq
     * @param theme
     * @throws RollerException
     */
    private void clearThemePages( RollerRequest rreq, String theme )
       throws RollerException
    {
        if (mLogger.isDebugEnabled()) 
        {
            mLogger.debug("theme="+theme);
        }
        
    	if (theme == null) return;
    	
        RollerContext rollerContext = 
           RollerContext.getRollerContext(rreq.getRequest());
           
        try
        {
            //UserData ud = rreq.getUser();
            UserManager mgr = rreq.getRoller().getUserManager();
            //String username = ud.getUserName();
        
            String themeDir = rollerContext.getThemePath(theme);        
            String[] children = RollerContext.getThemeFilenames(themeDir);
            
            // Custom theme won't have any files
            if (children == null) return;
                    
            for (int i = 0; i < children.length; i++)
            {
                String pageName = children[i].substring(
                    0,children[i].length()-3);
    
                PageData page = mgr.getPageByName(rreq.getCurrentWebsite(), pageName);
                if (page != null)
                {
                    PreviewResourceLoader.clearTemplate( page.getId() );
                }
                else
                {
                    PreviewResourceLoader.clearTemplate( pageName );
                }
            }
        }
        catch (Exception e)
        {
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("clearThemePages error: ", e);
            }
            
            throw new RollerException( e );
        }
        
    }
    
    /**
     * Reads theme pages from disk and saves them as pages in website of
     * the user specified by the RollerRequest. 
     * 
     * @param rreq Request wrapper.
     * @param theme Name of theme to save.
     * @throws RollerException
     */
    private void saveThemePages( RollerRequest rreq, String theme )
       throws RollerException
    {
        RollerContext rollerContext = 
           RollerContext.getRollerContext(rreq.getRequest());
           
        try
        {
            UserManager mgr = rreq.getRoller().getUserManager();
            WebsiteData website = rreq.getCurrentWebsite();
        
            HashMap pages = rollerContext.readThemeMacros(theme);
            Iterator iter = pages.keySet().iterator();
            while ( iter.hasNext() )
            {
                String pageName = (String) iter.next();
                String pageContent = (String)pages.get( pageName );
    
                PageData page = mgr.getPageByName( rreq.getCurrentWebsite(), pageName );
                if (page != null)
                {
                    // User already has page by that name, so overwrite it.
                    page.setTemplate( pageContent );
                }
                else
                {
                    // User does not have page by that name, so create new page.
                    page = new PageData( null,
                        website,         // website
                        pageName,        // name
                        pageName,        // description
                        pageName,        // link
                        pageContent,     // template
                        new Date()       // updateTime                
                    );
                    mgr.storePage( page );
                }
            }
            rreq.getRoller().commit();
        }
        catch (Exception e)
        {
            mLogger.error("ERROR in action",e);
            throw new RollerException( e );
        }
        
    }
    
    /**
     * Read the 'Weblog.vm' file for a theme and return it as a String.
     * 
     * @param ctx Roller context.
     * @param theme Name of theme.
     * @return Theme in the form of a string.
     * @throws RollerException
     */
    public String readTheme(RollerContext ctx, String theme)
        throws RollerException
    {
        String fileName = "Weblog.vm";
        if (themeCache.getFromCache(theme, fileName) != null)
        {
            return themeCache.getFromCache(theme, fileName);
        }
        
        String themesDir = RollerRuntimeConfig.getProperty("users.themes.path");
        
        String themeFile = RollerContext.getServletContext(
            ).getRealPath( "/" + themesDir
            + "/" + theme + "/" + fileName );
                        
        // Import weblog page template from specified theme
        StringBuffer sb = new StringBuffer();
        BufferedReader rdr = null;
        try
        {
            rdr = new BufferedReader(
                                    new FileReader(themeFile));
            String line = null;
            while ( null != (line = rdr.readLine()) )
            {
                sb.append( line );
                sb.append("\n");
            }
            themeCache.putIntoCache(theme, fileName, sb.toString());
        }
        catch (Exception e)
        {
            mLogger.error("themeFile:" + themeFile, e);
            throw new RollerException( e );
        }
        finally
        {
            try {
                if (rdr != null) rdr.close();
            } catch (IOException ioe) {
                mLogger.warn("unable to close " + themeFile);
            }
        }
        
        return sb.toString();
    }
}
