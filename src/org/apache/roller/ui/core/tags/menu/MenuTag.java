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

package org.apache.roller.ui.core.tags.menu;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.VelocityContext;
import org.apache.roller.RollerException;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.tags.VelocityTag;
import org.apache.roller.ui.rendering.velocity.ContextLoader;
//import javax.servlet.jsp.tagext.*;


/**
 * @jsp.tag name="Menu"
 */
public class MenuTag extends VelocityTag 
{
	private static Log mLogger = 
		LogFactory.getFactory().getInstance(RollerRequest.class);

	/** Unique ID for this menu within the user's session. 
	  * @jsp.attribute 
	  */
	public String getId() { return mMenuId; }
    public void setId( String v ) { mMenuId= v; }
	private String mMenuId;

	/** Name of the view to be used to render the menu.
      * The view is a Velocity template and it must be in the classpath. 
	  * Values: tabbed, vertical, horizontal.
	  * @jsp.attribute  required="true"
	  */
	public String getView() { return mView; }
    public void setView( String v ) { mView = v; }
	private String mView;

	/** Name of the model to be used.
	  * Must correspond to name of XML file in WEB-INF directory.
	  * @jsp.attribute required="true"
	  */
	public String getModel() { return mModel; }
    public void setModel( String v ) { mModel = v; }
	private String mModel;

    public String getTemplateClasspath()
    {
        return mView;
    }

   	//------------------------------------------------------------- 

	public void prepareContext( VelocityContext ctx )
	{
		HttpServletRequest req = (HttpServletRequest)pageContext.getRequest();
		HttpServletResponse res = (HttpServletResponse)pageContext.getResponse();

		RollerMenuModel model = new RollerMenuModel( 
			mMenuId, "/WEB-INF/"+mModel, pageContext.getServletContext() );
		ctx.put("menuModel", model );
		ctx.put("ctx", pageContext );
		ctx.put("req", req );
		ctx.put("res", res );
		
                ContextLoader.loadToolboxContext(req, res, ctx);
	}

}

