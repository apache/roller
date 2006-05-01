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
package org.apache.roller.presentation.tags;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.presentation.RollerSession;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagSupport;


/**
 * If there is an status message, then print it in red.
 * @jsp.tag name="StatusMessage"
 */
public class StatusMessageTag extends TagSupport
{
    static final long serialVersionUID = -1086963203859216226L;
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(StatusMessageTag.class);

    public int doStartTag() throws JspException
	{
		try
		{
			JspWriter pw = pageContext.getOut();
			HttpServletRequest req = 
				(HttpServletRequest)pageContext.getRequest();

			String msg = null;

			msg = (String)req.getSession().getAttribute(
                RollerSession.ERROR_MESSAGE);
			if (msg != null)
			{
				pw.println("<span class=\"error\">");
				pw.println(msg);
				pw.println("</span>");
				req.getSession().removeAttribute(RollerSession.ERROR_MESSAGE);
			}

			msg = (String)req.getSession().getAttribute(
                RollerSession.STATUS_MESSAGE);
			if (msg != null)
			{
				pw.println("<span class=\"statusMsg\">");
				pw.println(msg);
				pw.println("</span>");
				req.getSession().removeAttribute(RollerSession.STATUS_MESSAGE);
			}
		}
		catch (IOException e)
		{
            mLogger.error("Exception",e);
			throw new JspException(e);
		}
		return Tag.SKIP_BODY;
    }
}

