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
package org.roller.presentation.velocity;

import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.roller.presentation.RollerContext;

/**
 * Tries to load Velocity resources from the Webapp.
 * This class borrows heavily from
 * org.apache.velocity.tools.view.servlet.WebappLoader
 * http://cvs.apache.org/viewcvs/jakarta-velocity-
 * tools/view/src/java/org/apache/velocity/tools/view/servlet/WebappLoader.java?
 * rev=1.1.1.1&content-type=text/vnd.viewcvs-markup
 * 
 * @author Lance Lavandowska
 */
public class WebappResourceLoader extends ResourceLoader
{
	private static Log mLogger = 
		LogFactory.getFactory().getInstance(WebappResourceLoader.class);
    
    private static ServletContext mContext = null; 
  
	/**
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#init(org.apache.commons.collections.ExtendedProperties)
	 */
	public void init(ExtendedProperties arg0)
	{
		rsvc.info("WebappResourceLoader : initialization starting.");

		this.getContext();
		if (mContext == null)
		{
			mLogger.warn("WebappResourceLoader : Unable to find ServletContext!");
		}

		rsvc.info("WebappResourceLoader : initialization complete.");
	}
	
	private ServletContext getContext()
	{
		if (mContext == null)
		{
			mContext = RollerContext.getServletContext();
		}
		return mContext;
	}
	
	public static void setServletContext(ServletContext context)
	{
		mContext = context;
	}
	
	/**
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getResourceStream(java.lang.String)
	 */
	public InputStream getResourceStream(String name)
		throws ResourceNotFoundException
	{
		InputStream result = null;
        
		if (name == null || name.length() == 0)
		{
			throw new ResourceNotFoundException ("No template name provided");
		}
        
		try 
		{
			if (!name.startsWith("/"))
				name = "/" + name;

			result = getContext().getResourceAsStream( name );
		}
		catch( NullPointerException npe)
		{
			String msg = "WebappResourceLoader.getResourceStream(): " + name;
			if (mContext == null)
			{
				mLogger.info("WebappResourceLoader("+name+"): ServletContext is null");
				msg += "\n\tServletContext is null";
			}
			throw new ResourceNotFoundException(msg);
		}
		catch( Exception fnfe )
		{
			/*
			 *  log and convert to a general Velocity ResourceNotFoundException
			 */            
			throw new ResourceNotFoundException( fnfe.getMessage() );
		}
        
		return result;
	}
	
	/**
	 * Defaults to return false.
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
	 */
	public boolean isSourceModified(Resource arg0)
	{
		return false;
	}
	
	/**
	 * Defaults to return 0.
	 * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
	 */
	public long getLastModified(Resource arg0)
	{
		return 0;
	}
}
