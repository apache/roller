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
import javax.servlet.jsp.JspException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.core.RollerRequest;
import org.apache.roller.ui.core.RollerSession;


/**
 * Draws the most complete possible Roller navigation bar based on request
 * parameters userName, folderId and authenticated user (if there is one).
 * @jsp.tag name="EditorNavigationBar"
 */
public class EditorNavigationBarTag extends MenuTag 
{
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(EditorNavigationBarTag.class);

   	//------------------------------------------------------------- 
	public EditorNavigationBarTag()
	{
	}

   	//------------------------------------------------------------- 
	public String view()
	{
		return emit();
	}

   	//------------------------------------------------------------- 
    public int doEndTag(java.io.PrintWriter pw) throws JspException
    {
		try 
		{
			HttpServletRequest request = 
				(HttpServletRequest)pageContext.getRequest(); 
			RollerRequest rreq = RollerRequest.getRollerRequest(request);
			RollerSession rollerSession = RollerSession.getRollerSession(request);
			if ( rollerSession.isGlobalAdminUser() )
			{
				return super.doEndTag(pw);
			}
			else
			{
				return EVAL_PAGE;
			}
		}
		catch (Exception e)
		{
			mLogger.error("EditorNavigationBarTag exception",e);
			throw new JspException(e);
		}
    }
}

