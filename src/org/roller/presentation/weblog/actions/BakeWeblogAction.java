package org.roller.presentation.weblog.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionError;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.DispatchAction;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.roller.RollerException;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.PageData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.RollerContext;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.RollerSession;
import org.roller.presentation.velocity.ContextLoader;
import org.roller.presentation.weblog.tags.WeblogEntryMacros;
import org.roller.util.DateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspFactory;
import javax.servlet.jsp.PageContext;

/////////////////////////////////////////////////////////////////////////////
/**
 * Generates static pages for each day containing Weblog entries.
 * @struts.action name="bakeWeblogForm" path="/bake" scope="session"
 * parameter="method"
 * 
 * @author llavandowska
 */
public class BakeWeblogAction extends DispatchAction
{
	private static Log mLogger = 
		LogFactory.getFactory().getInstance(BakeWeblogAction.class);
		
	public ActionForward load(
		ActionMapping       mapping,
		ActionForm          actionForm,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		RollerRequest rreq = RollerRequest.getRollerRequest(request);
		try {
			if ( !rreq.isUserAuthorizedToEdit() )
			{
				return mapping.findForward("access-denied");
			}
		}
		catch (Exception e)
		{
			return mapping.findForward("access-denied");
		}
		return mapping.findForward("baking.done");
	}
		
	public ActionForward bake(
		ActionMapping       mapping,
		ActionForm          actionForm,
		HttpServletRequest  request,
		HttpServletResponse response)
		throws IOException, ServletException
	{
		ActionForward forward = mapping.findForward("baking.done");
		ActionErrors errors = new ActionErrors();
		try
		{
			PageContext pageContext =
				JspFactory.getDefaultFactory().getPageContext(
					this.getServlet(), request, response,"", true, 8192, true);
			RollerRequest rreq = RollerRequest.getRollerRequest(pageContext);
			if ( !rreq.isUserAuthorizedToEdit() )
			{
				return mapping.findForward("access-denied");
			}			
			
			String pid = getDefaultPageId( rreq );
			
			VelocityContext context = new VelocityContext();
			ContextLoader.setupContext( context, rreq, response );

			// Get this months Entries
            WeblogManager mgr = rreq.getRoller().getWeblogManager();
            //Map entryMap = mgr.getWeblogEntryMonthMap( 
			    //rreq.getUser().getUserName(), 
                //rreq.getDate(true), 
                //null, 
                //false, 
                //true );

            Map entryMap = mgr.getWeblogEntryObjectMap(
                            rreq.getWebsite(),
                            null,                        // startDate
                            rreq.getDate(true),          // endDate
                            null,                        // catName
                            WeblogManager.PUB_ONLY,      // status
                            null);                      // maxEntries
            
            if (mLogger.isDebugEnabled())
            {
			    mLogger.debug("Num Days to Bake: " + entryMap.size());
            }
            
			Iterator iter = entryMap.keySet().iterator();
			if (iter.hasNext())
			{
				Date d = (Date)iter.next();
                
                if (mLogger.isDebugEnabled())
                {
				    mLogger.debug("Bake Weblog for date:" + d);
				}
                
				// Do we need this to continue supporting Macros?
				WeblogEntryMacros macros = 
					new WeblogEntryMacros( rreq.getPageContext(), d );
				context.put( "macros", macros );
				
				List entries = (List) entryMap.get( d ); 	
				String content = transformTemplate(pid, entries, context, rreq);
				
				String fileName = writeToFile(content, d, rreq);
				request.getSession().setAttribute(
					RollerSession.STATUS_MESSAGE,
						"Weblog baked to :" + fileName);			
			}

			/** put some user message **/
			String message = (String) request.getSession().getAttribute(
				RollerSession.STATUS_MESSAGE);
			if (message == null) message = "No Files Written";
			request.getSession().setAttribute(
				RollerSession.STATUS_MESSAGE,
					message);		
		}
		catch (Exception e)
		{
			forward = mapping.findForward("error");

			errors.add(ActionErrors.GLOBAL_ERROR,
				new ActionError("error.bake.weblog", e.toString()));
			saveErrors(request,errors);

			mLogger.error(getResources(request).getMessage("error.bake.weblog") 
				+ e.toString(),e);
		}		
		
		return forward;
	}
	
	/**
	 * Write the content generated by transformTemplate to a file in the Upload
	 * directory, where the user can reach it.
	 * 
	 * @param content
	 * @param d
	 * @param rreq
	 */
	private String writeToFile(String content, Date d, RollerRequest rreq) 
		throws IOException
	{
        if (mLogger.isDebugEnabled())
        {
		    mLogger.debug("Write file for:" + d);
        }
        
		String dir = RollerContext.getUploadDir( rreq.getServletContext() );
		String username = rreq.getUser().getUserName();
		File dirF = new File(dir + username + File.separator + "baked");
		if (!dirF.exists())
		{
			dirF.mkdirs();
		}
		
		SimpleDateFormat mFmt = DateUtil.get8charDateFormat();
		String dString = mFmt.format( d ) + ".html";
		File pFile = new File(dirF, dString);
		
		OutputStream bos = new FileOutputStream(pFile);
		bos.write( content.getBytes() );
		bos.close();
		
		return pFile.getName();
	}

	/**
	 * Transform the Page specified by the pageId using Velocity. Return the
	 * resulting String.
	 * 
	 * @param pid
	 * @param context
	 * @throws ResourceNotFoundException
	 * @throws ParseErrorException
	 * @throws MethodInvocationException
	 * @throws Exception
	 */
	private String transformTemplate(String pid, List entries, 
		VelocityContext context, RollerRequest rreq)
		throws
			ResourceNotFoundException,
			ParseErrorException,
			MethodInvocationException,
			Exception
	{              
		
		context.put( "entries", entries ); 
		
		StringWriter sw = new StringWriter();
		Velocity.mergeTemplate(pid, Velocity.ENCODING_DEFAULT, context, sw );
		return sw.toString();
	}
	
	/**
	 * Get the default pageId for this Website, this is assumed to be the Weblog
	 * page since we have no other way of declaring it.
	 * 
	 * @param rreq
	 * @return String
	 * @throws RollerException
	 */
	private String getDefaultPageId(RollerRequest rreq) throws RollerException
	{
		UserManager userMgr = rreq.getRoller().getUserManager();
		WebsiteData wd = rreq.getWebsite();
		PageData pd = userMgr.retrievePage(wd.getDefaultPageId());
		return pd.getId();
	}
	
}
