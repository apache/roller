/*
 * Created on Feb 14, 2004
 */
package org.roller.presentation.website.formbeans;

import org.apache.commons.lang.StringUtils;
import org.roller.RollerException;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.forms.WebsiteForm;

/**
 * @struts.form name="websiteFormEx"
 */ 
public class WebsiteFormEx extends WebsiteForm
{
    private String bloggerCategoryId;
    private String defaultCategoryId;
    private String[] defaultPluginsArray;
    
    /**
     * @return Returns the bloggerCategoryId.
     */
    public String getBloggerCategoryId()
    {
        return bloggerCategoryId;
    }

    /**
     * @param bloggerCategoryId The bloggerCategoryId to set.
     */
    public void setBloggerCategoryId(String bloggerCategoryId)
    {
        this.bloggerCategoryId = bloggerCategoryId;
    }

    /**
     * @return Returns the defeaultCategoryId.
     */
    public String getDefaultCategoryId()
    {
        return defaultCategoryId;
    }

    /**
     * @param defeaultCategoryId The defeaultCategoryId to set.
     */
    public void setDefaultCategoryId(String defeaultCategoryId)
    {
        this.defaultCategoryId = defeaultCategoryId;
    }

    /**
     * @return
     */
    public String[] getDefaultPluginsArray()
    {
        return defaultPluginsArray;
    }

    /**
     * @param strings
     */
    public void setDefaultPluginsArray(String[] strings)
    {
        defaultPluginsArray = strings;
    }


    /** 
     * @see org.roller.presentation.forms.WebsiteForm#copyFrom(org.roller.pojos.WebsiteData)
     */
    public void copyFrom(WebsiteData dataHolder, java.util.Locale locale) throws RollerException
    {
        super.copyFrom(dataHolder, locale);
        if (dataHolder.getDefaultCategory() != null)
        {
            defaultCategoryId = dataHolder.getDefaultCategory().getId();
        }
        if (dataHolder.getBloggerCategory() != null)
        {
            bloggerCategoryId = dataHolder.getBloggerCategory().getId();
        }
        if (dataHolder.getDefaultPlugins() != null)
        {
            defaultPluginsArray = StringUtils.split(dataHolder.getDefaultPlugins(), ",");
        }
    }

    /** 
     * @see org.roller.presentation.forms.WebsiteForm#copyTo(org.roller.pojos.WebsiteData)
     */
    public void copyTo(WebsiteData dataHolder, java.util.Locale locale) throws RollerException
    {
        super.copyTo(dataHolder, locale);
        dataHolder.setDefaultPlugins( StringUtils.join(this.defaultPluginsArray,",") );
    }
    
    public void reset(
        org.apache.struts.action.ActionMapping mapping, 
        javax.servlet.ServletRequest request)
    {
        doReset(mapping, request);
        defaultPluginsArray = new String[0];
    }
    
    public void reset(
        org.apache.struts.action.ActionMapping mapping, 
        javax.servlet.http.HttpServletRequest request)
    {
        doReset(mapping, request);
        defaultPluginsArray = new String[0];
    }
}
