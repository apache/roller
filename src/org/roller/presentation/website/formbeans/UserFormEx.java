
package org.roller.presentation.website.formbeans;

import org.roller.RollerException;
import org.roller.pojos.UserData;
import org.roller.presentation.forms.UserForm;
import org.roller.util.DateUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Locale;

/**
 * Extend form to add extra properties not in generated base.
 * @struts.form name="userFormEx"
 */ 
public class UserFormEx extends UserForm
{
	private String mTheme = null;
    private String mLocale = null;
    private String mTimezone = null;
    private String mPasswordText = null;
    private String mPasswordConfirm = null;
    private boolean adminCreated = false;

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
	public String getLocale() 
	{
		return mLocale;
	}

	public void setLocale(String locale) 
	{
		mLocale = locale;
	}

	public String getTimezone() 
	{
		return mTimezone;
	}

	public void setTimezone(String timezone) 
	{
		mTimezone = timezone;
	}
    
    /**
     * Don't call it "password" because browser will autofill.
     * @return Returns the passwordText.
     */
    public String getPasswordText()
    {
        return mPasswordText;
    }
    
    /**
     * Don't call it "password" because browser will autofill.
     * @param passwordText The passwordText to set.
     */
    public void setPasswordText(String passwordText)
    {
        mPasswordText = passwordText;
    }
    
    /**
     * @return Returns the passwordConfirm.
     */
    public String getPasswordConfirm()
    {
        return mPasswordConfirm;
    }
    
    /**
     * @param passwordConfirm The passwordConfirm to set.
     */
    public void setPasswordConfirm(String passwordConfirm)
    {
        mPasswordConfirm = passwordConfirm;
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
    
    
    /** 
     * Override to prevent password or dateCreated being copied over by form
     * @see org.roller.presentation.forms.UserForm#copyTo(
     * 		org.roller.pojos.UserData, java.util.Locale)
     */
    public void copyTo(UserData dataHolder, Locale locale)
                    throws RollerException
    {
        String password = dataHolder.getPassword();
        Date dateCreated = dataHolder.getDateCreated();
        
        super.copyTo(dataHolder, locale);
        
        dataHolder.setPassword(password);
        dataHolder.setDateCreated(dateCreated);
    }

    /** True if user is being created by an admin */
    public void setAdminCreated(boolean b) {
        adminCreated = b;
    }
    /** True if user is being created by an admin */
    public boolean getAdminCreated() {
        return adminCreated;
    }
}

