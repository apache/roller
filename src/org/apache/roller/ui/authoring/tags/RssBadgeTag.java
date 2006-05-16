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

package org.apache.roller.ui.authoring.tags;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.Tag;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerRequest;

/** 
  * @jsp.tag name="RssBadge"
  */
public class RssBadgeTag extends org.apache.roller.ui.core.tags.HybridTag 
{
    static final long serialVersionUID = 8569693454388788128L;
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(RssBadgeTag.class);

  	//------------------------------------------------------------------------ 
    /**
     * Process start tag.
     * @return EVAL_SKIP_BODY
     */
    public int doStartTag( PrintWriter pw ) throws JspException 
	{
		try
		{
			HttpServletRequest req = 
				(HttpServletRequest)pageContext.getRequest();
			RollerRequest rreq = RollerRequest.getRollerRequest(req);

			WebsiteData website = rreq.getWebsite();
			pw.println(
        		   "<a href="+"\"" + req.getContextPath() + "/rss/"
                + website.getHandle() + "\">"
                + "<img "+"src=\"" + req.getContextPath() + "/images/rssbadge.gif\" "
                + "class=\"rssbadge\" "
                + "alt=\"XML\""
                + "/>"
                + "</a>");
         }
		catch (Exception e)
		{
            mLogger.error("Exception",e);
			throw new JspException(
				e.getClass().toString()+": "+e.getMessage(),e);
		}
		return Tag.SKIP_BODY;
    }
}

