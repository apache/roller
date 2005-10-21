/*
 * Created on Mar 10, 2004
 */
package org.roller.presentation;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.ResourceBundle;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionMapping;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.util.StrutsUtil;

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
        RollerContext rctx = RollerContext.getRollerContext(request);
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
