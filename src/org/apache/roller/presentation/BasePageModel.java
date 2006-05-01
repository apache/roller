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
/*
 * Created on Mar 10, 2004
 */
package org.apache.roller.presentation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionMapping;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.presentation.util.StrutsUtil;

/**
 * Re-usable base for page models.
 * @author David M Johnson
 */
public class BasePageModel
{
    protected static ResourceBundle bundle = 
        ResourceBundle.getBundle("ApplicationResources");  
    
    protected String titleKey = null;
    protected HttpServletRequest request = null;
    protected HttpServletResponse response = null;
    protected ActionMapping mapping = null;
    protected WebsiteData website = null;
    
    public BasePageModel(
            String titleKey,
            HttpServletRequest request,
            HttpServletResponse response,
            ActionMapping mapping)
    {
        this.request = request;
        this.response = response;
        this.mapping = mapping;
        this.titleKey = titleKey;
        request.setAttribute("locales", StrutsUtil.getLocaleBeans());        
        request.setAttribute("timeZones", StrutsUtil.getTimeZoneBeans());        
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        website = rreq.getWebsite();
    }

    public WebsiteData getWebsite() 
    {
        return website;
    }
    
    public void setWebsite(WebsiteData website) 
    {
        this.website = website;
    }
    
    public String getTitle() 
    {
        return bundle.getString(titleKey);
    }
    
    public String getBaseURL()
    {
        RollerContext rctx = RollerContext.getRollerContext();
		return rctx.getAbsoluteContextUrl(request);
	}

    public String getShortDateFormat()
    {
        DateFormat sdf = DateFormat.getDateInstance(
                DateFormat.SHORT, request.getLocale());
        if (sdf instanceof SimpleDateFormat)
        {
            return ((SimpleDateFormat)sdf).toLocalizedPattern();
        }
        return "yyyy/MM/dd";
    }

    public String getMediumDateFormat()
    {
        DateFormat sdf = DateFormat.getDateInstance(
                DateFormat.MEDIUM, request.getLocale());
        if (sdf instanceof SimpleDateFormat)
        {
            return ((SimpleDateFormat)sdf).toLocalizedPattern();
        }
        return "MMM dd, yyyy";
    }

    /**
     * @return Returns the mapping.
     */
    public ActionMapping getMapping() 
    {
        return mapping;
    }
    
    /**
     * @return Returns the request.
     */
    public HttpServletRequest getRequest() 
    {
        return request;
    }
    
    /**
     * @return Returns the response.
     */
    public HttpServletResponse getResponse() 
    {
        return response;
    }
    
    public RollerSession getRollerSession()
    {
        return RollerSession.getRollerSession(request);
    }

    public List getLocales()
    {
        return StrutsUtil.getLocaleBeans();
    }

    public List getTimeZones()
    {
        return StrutsUtil.getTimeZoneBeans();
    }

}
