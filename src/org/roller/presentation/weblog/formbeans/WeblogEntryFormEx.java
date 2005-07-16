package org.roller.presentation.weblog.formbeans;

import org.apache.commons.lang.StringUtils;
import org.roller.RollerException;
import org.roller.pojos.CommentData;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.forms.WeblogEntryForm;
import org.roller.util.DateUtil;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.roller.pojos.EntryAttributeData;

/**
 * Extends the WeblogEntryForm so that additional properties may be added.
 * @struts.form name="weblogEntryFormEx"
 * @author dmj
 */
public class WeblogEntryFormEx extends WeblogEntryForm
{
    private String mCategoryId = null;
    private String mCreatorId = null;
    private Date mDate = new Date();    
    private String mDateString = null;        
    private Integer mHours = new Integer(0);
    private Integer mMinutes = new Integer(0);
    private Integer mSeconds = new Integer(0);
    private String[] mReplacementWords = null;
    private String[] pluginsArray = new String[0];
    private String[] deleteComments = new String[0];
    private String[] spamComments = new String[0];
    private String trackbackUrl = null;
    private Map attributes = new HashMap();
    
    public WeblogEntryFormEx()
    {
        super();
    }

    public WeblogEntryFormEx(WeblogEntryData entryData, java.util.Locale locale) 
        throws RollerException
    {
        copyFrom(entryData, locale);
    }
    
    /**
     * @param request
     * @param response
     */
    public void initNew(HttpServletRequest request, HttpServletResponse response) 
    {
        RollerRequest rreq = RollerRequest.getRollerRequest(request);
        if (rreq.getCurrentWebsite().getDefaultPlugins() != null)
        {
            setPluginsArray(StringUtils.split(
                    rreq.getCurrentWebsite().getDefaultPlugins(), ",") );
        }
        allowComments = Boolean.TRUE;
        updateTime = new Timestamp(new Date().getTime());
        pubTime = updateTime;
        initPubTimeDateString(request.getLocale());        
    }
    
    /**
     * Copy values from this Form to the WeblogEntryData.
     */
    public void copyTo(WeblogEntryData entry, Locale locale, Map paramMap) 
        throws RollerException
    {
        super.copyTo(entry, locale);
        final DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        final Date newDate;
        try
        {
            newDate = df.parse(getDateString());
        }
        catch (ParseException e)
        {
            throw new RollerException("ERROR parsing date.");
        }
        final Calendar cal = Calendar.getInstance(locale);
        cal.setTime(newDate);
        cal.set(Calendar.HOUR_OF_DAY, getHours().intValue());
        cal.set(Calendar.MINUTE, getMinutes().intValue());
        cal.set(Calendar.SECOND, getSeconds().intValue());
        entry.setPubTime(new Timestamp(cal.getTimeInMillis()));
        
        entry.setPlugins( StringUtils.join(this.pluginsArray,",") );
        
        // checkboxes don't send a value for unchecked
        if (entry.getPublishEntry() == null)
        {
            entry.setPublishEntry(Boolean.FALSE);
        }
        if (getCategoryId() != null) 
        {
            entry.setCategoryId(getCategoryId());
        }       
        
        if (getCreatorId() != null) 
        {
            entry.setCreatorId(getCreatorId());
        }       
        
        Iterator params = paramMap.keySet().iterator();
        while (params.hasNext())
        {
            String name = (String)params.next();
            String[] value = (String[])paramMap.get(name);
            if (name.startsWith("att_") && value.length > 0)
            {
                try
                {
                    entry.putEntryAttribute(name, value[0]);
                }
                catch (Exception e)
                {
                    throw new RollerException("ERROR setting attributes",e);
                }
            }
        }
    }
    
    /**
     * Copy values from WeblogEntryData to this Form.
     */
    public void copyFrom(WeblogEntryData entry, Locale locale) 
        throws RollerException
    {
        super.copyFrom(entry, locale);
        mCategoryId = entry.getCategory().getId();
        mCreatorId = entry.getCreator().getId();
        
        initPubTimeDateString(locale);
        
        if (entry.getPlugins() != null)
        {
            pluginsArray = StringUtils.split(entry.getPlugins(), ",");
        }
        
        attributes = new HashMap();
        Iterator atts = entry.getEntryAttributes().iterator();
        while (atts.hasNext())
        {
            EntryAttributeData att = (EntryAttributeData)atts.next();
            attributes.put(att.getName(), att.getValue());
        }
      
        populateSpamComments(entry);
    }
    
    public Map getAttributes()
    {
        return attributes;
    }
    
    /**
     * Populate the spamComments array for this entry.
     * @param entry
     */
    private void populateSpamComments(WeblogEntryData entry)
    {
        ArrayList spamList = new ArrayList();
        Iterator it = entry.getComments(false).iterator();
        while (it.hasNext()) {
            CommentData comment = (CommentData)it.next();
            if (comment.getSpam().booleanValue())
            {
                spamList.add(comment.getId());
            }
        }
        spamComments = (String[])spamList.toArray(new String[spamList.size()]);
    }

    /**
     * Localize the PubTime date string.
     * @param locale
     */
    private void initPubTimeDateString(Locale locale)
    {
        Calendar cal = Calendar.getInstance(locale);
        cal.setTime(getPubTime());        
        mHours = new Integer(cal.get(Calendar.HOUR_OF_DAY));
        mMinutes = new Integer(cal.get(Calendar.MINUTE));
        mSeconds = new Integer(cal.get(Calendar.SECOND));        
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        mDateString = df.format(getPubTime());
    }

    /**
     * Returns the category ID.
     * @return String
     */
    public String getCategoryId()
    {
        return mCategoryId;
    }
    
    public Date getDate()
    {
        return mDate;
    }

    /**
     * Date string formatted using SHORT format from user's locale.
     * @return Returns the dateString.
     */
    public String getDateString()
    {
        return mDateString;
    }

    /**
     * @return Returns the hours.
     */
    public Integer getHours()
    {
        return mHours;
    }

    /**
     * @return Returns the minutes.
     */
    public Integer getMinutes()
    {
        return mMinutes;
    }
    
    /**
     * Returns the array of replacement words.
     * @return String[]
     */ 
    public String[] getReplacementWords()
    {
        return mReplacementWords;
    }

    /**
     * @return Returns the seconds.
     */
    public Integer getSeconds()
    {
        return mSeconds;
    }
    
    /**
     * Sets the category id.
     * @param categoryId  the category id to set
     */
    public void setCategoryId(String categoryId)
    {
        this.mCategoryId = categoryId;
    }

    /**
     * Date string formatted using SHORT format from user's locale.
     * @param dateString The dateString to set.
     */
    public void setDateString(String dateString) throws ParseException
    {
        mDateString = dateString;
    }

    /**
     * @param hours The hours to set.
     */
    public void setHours(Integer hours)
    {
        mHours = hours;
    }

    /**
     * @param minutes The minutes to set.
     */
    public void setMinutes(Integer minutes)
    {
        mMinutes = minutes;
    }

    public void setReplacementWords(String[] words)
    {
        this.mReplacementWords = words;
    }

    /**
     * @param seconds The seconds to set.
     */
    public void setSeconds(Integer seconds)
    {
        mSeconds = seconds;
    }

    public String getDay()
    {
        java.util.Date theDay = getPubTime();
        theDay = theDay!=null ? theDay : new java.util.Date();
        return DateUtil.format8chars(theDay);
    }

    /**
     * @return
     */
    public String[] getPluginsArray()
    {
        return pluginsArray;
    }

    /**
     * @param strings
     */
    public void setPluginsArray(String[] strings)
    {
        pluginsArray = strings;
    }
    
    public void doReset(
            org.apache.struts.action.ActionMapping mapping, 
            javax.servlet.ServletRequest request)
    {
        super.doReset(mapping, request);
        
        pluginsArray = new String[0];
        
        // reset time fields to current time
        Calendar cal = Calendar.getInstance(request.getLocale());
        Date now = new Date();
        cal.setTime(now);        
        mHours = new Integer(cal.get(Calendar.HOUR_OF_DAY));
        mMinutes = new Integer(cal.get(Calendar.MINUTE));
        mSeconds = new Integer(cal.get(Calendar.SECOND));        
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
        mDateString = df.format(now);
    }

    /**
     * @return Returns the selectedComments.
     */
    public String[] getDeleteComments() {
        return deleteComments;
    }
    
    /**
     * @param selectedComments The selectedComments to set.
     */
    public void setDeleteComments(String[] selectedComments) {
        this.deleteComments = selectedComments;
    }
    
    /**
     * @return Returns the commentsToMarkAsSpam.
     */
    public String[] getSpamComments() {
        return spamComments;
    }
    
    /**
     * @param commentsToMarkAsSpam The commentsToMarkAsSpam to set.
     */
    public void setSpamComments(String[] commentsToMarkAsSpam) {
        this.spamComments = commentsToMarkAsSpam;
    }
    
    /**
     * @return Returns the trackbackUrl.
     */
    public String getTrackbackUrl()
    {
        return trackbackUrl;
    }
    /**
     * @param trackbackUrl The trackbackUrl to set.
     */
    public void setTrackbackUrl(String trackbackUrl)
    {
        this.trackbackUrl = trackbackUrl;
    }

    public String getCreatorId()
    {
        return mCreatorId;
    }
    public void setCreatorId(String creatorId)
    {
        mCreatorId = creatorId;
    }
}

