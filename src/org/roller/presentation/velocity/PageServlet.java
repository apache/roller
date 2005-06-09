
package org.roller.presentation.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.context.Context;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Extend RollerServlet to load proper resource loader for page execution.
 * 
 * @web.servlet name="PageServlet" load-on-startup="0"
 * @web.servlet-init-param name="org.apache.velocity.properties" 
 * 		                  value="/WEB-INF/velocity.properties"
 *  
 * @web.servlet-mapping url-pattern="/page/*"
 */ 
public class PageServlet extends BasePageServlet
{
    static final long serialVersionUID = 5083624357559616805L;
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(PageServlet.class);

	/** We are overriding the default Runtime Velocity
	 * singleton to gain control over the initialization
	 * and so that the PreviewResourceLoader is not set
	 * for the PageServlet.
	 */
	transient VelocityEngine ve = null;
	
    public Template handleRequest( HttpServletRequest request,
                                    HttpServletResponse response, 
                                    Context ctx ) throws Exception
    {
		return super.handleRequest(request, response, ctx);
    }
	
	/**
	 * Override initVelocity() so we can man-handle the list of
	 * resource loaders and remove "preview" if it is present.
	 * @see org.apache.velocity.servlet.VelocityServlet#initVelocity(ServletConfig)
	 */
	protected void initVelocity( ServletConfig config )
		 throws ServletException
	{
		try
		{
			/*
			 *  call the overridable method to allow the 
			 *  derived classes a shot at altering the configuration
			 *  before initializing Runtime
			 */
			Properties props = loadConfiguration( config );
			
			// remove "preview," from the beginning of the 
			// resource.loader list
			String resourceLoaders = (String)props.get("resource.loader");
			if (resourceLoaders != null &&
				resourceLoaders.indexOf("preview") > -1)
			{
				int begin = resourceLoaders.indexOf("preview");
				int length = "preview".length() + 1;
				String tempStr = "";
				if (begin > 0)
				{
					tempStr = resourceLoaders.substring(0,begin);
				}
				resourceLoaders = tempStr + resourceLoaders.substring(begin+length);

				//System.out.println("PageServlet RESOURCELOADERS: " + resourceLoaders);
				props.put("resource.loader", resourceLoaders);
			}
			
			// remove all trace of the PreviewResourceLoader
			props.remove("preview.resource.loader.public.name");
			props.remove("preview.resource.loader.description");
			props.remove("preview.resource.loader.class");
			props.remove("preview.resource.loader.cache");
			props.remove("preview.resource.loader.modificationCheckInterval");
			
			/** set custom logging file */
			props.setProperty( "runtime.log", "page_servlet.log" );
			
			// make user WebappResourceLoader has what it needs
			WebappResourceLoader.setServletContext( getServletContext() );
			
            if (mLogger.isDebugEnabled())
            {
                mLogger.debug("VelocityEngine props: " + props.toString());
            }
            
			ve = new VelocityEngine();
			ve.info("*******************************************");
			ve.info("Initializing VelocityEngine for PageServlet");
			ve.init( props );
			ve.info("Done initializing VelocityEngine for PageServlet");
			ve.info("************************************************");
		}
		catch( Exception e )
		{
			String msg = "Error initializing Velocity: " + e.toString();
            mLogger.error(msg, e);
			throw new ServletException(msg, e);
		}   
	}

	/**
	 * Override the parent getTemplate( String name ) method.
	 * @see org.apache.velocity.servlet.VelocityServlet#getTemplate(String, String)
	 */
	public Template getTemplate( String name )
		throws ResourceNotFoundException, ParseErrorException, Exception
	{
		return ve.getTemplate( name );
	}

	/**
	 * Override the parent getTemplate(String name, String encoding) method.
	 * @see org.apache.velocity.servlet.VelocityServlet#getTemplate(String, String)
	 */
	public Template getTemplate( String name, String encoding )
		throws ResourceNotFoundException, ParseErrorException, Exception
	{
		return ve.getTemplate( name, encoding );
	}
}

