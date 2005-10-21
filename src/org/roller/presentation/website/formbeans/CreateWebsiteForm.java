package org.roller.presentation.website.formbeans;

import java.util.Locale;

import org.apache.struts.action.ActionForm;
import org.roller.pojos.WebsiteData;

/**
 * @struts.form name="createWebsiteForm"
 * @author Dave M Johnson
 */
public class CreateWebsiteForm extends ActionForm
{
    private String handle;
    private String name;
    private String description;
    private String emailAddress;
    private String locale;
    private String timeZone;
    private String theme;
    
    public CreateWebsiteForm()
    {
        
    }
    public String getDescription()
    {
        return description;
    }
    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getEmailAddress()
    {
        return emailAddress;
    }
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }
    public String getHandle()
    {
        return handle;
    }
    public void setHandle(String handle)
    {
        this.handle = handle;
    }
    public String getLocale()
    {
        return locale;
    }
    public void setLocale(String locale)
    {
        this.locale = locale;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getTheme()
    {
        return theme;
    }
    public void setTheme(String theme)
    {
        this.theme = theme;
    }
    public String getTimeZone()
    {
        return timeZone;
    }
    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }
    
    /**
     * @param wd
     * @param locale2
     */
    public void copyTo(WebsiteData wd, Locale locale)
    {
        wd.setHandle(handle);
        wd.setName(name);
        wd.setDescription(description);
        wd.setLocale(this.locale);
        wd.setTimeZone(timeZone);
    }
}
