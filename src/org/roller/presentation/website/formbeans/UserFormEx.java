
package org.roller.presentation.website.formbeans;

import org.roller.RollerException;
import org.roller.pojos.UserData;
import org.roller.presentation.forms.UserForm;
import org.roller.util.DateUtil;

import java.sql.Timestamp;
import java.util.Date;

/**
 * @struts.form name="userFormEx"
 */ 
public class UserFormEx extends UserForm
{
	private String mTheme = null;
    private String mLocale = null;
    private String mTimezone = null;

	public UserFormEx()
	{
		super();
	}

	public UserFormEx( UserData userData, java.util.Locale locale ) throws RollerException
	{
		super(userData, locale);
	}

	public String getTheme()
	{
		return mTheme; 
	}

	public void setTheme( String theme )
	{
		mTheme = theme;
	}
	public String getLocale() {
		return mLocale;
	}

	public void setLocale(String locale) {
		mLocale = locale;
	}

	public String getTimezone() {
		return mTimezone;
	}

	public void setTimezone(String timezone) {
		mTimezone = timezone;
	}
    
    /**
     * Utility to convert from String to Date.
     */
    public void setDateCreatedAsString(String value)
    {
        if ( value == null || value.trim().length() == 0 )
        {
            this.setDateCreated(null);   
        }
        else
        {
            try
            {
                Date pubDate = DateUtil.parse(
                        value, DateUtil.friendlyTimestampFormat());
                this.setDateCreated(new Timestamp(pubDate.getTime()));
            }
            catch (java.text.ParseException pe)
            {
                // wasn't proper format, try others
                Date pubDate = DateUtil.parseFromFormats(value);
                this.setDateCreated( new Timestamp(pubDate.getTime()) );
            }
        }
    }

    /**
     * Returns a formatted pubTime string.
     */
    public String getDateCreatedAsString()
    {
        return DateUtil.friendlyTimestamp(this.getDateCreated());
    }

    
}

