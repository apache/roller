/*
 * Created on Feb 3, 2006
 */
package org.roller.presentation.weblog.formbeans;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.servlet.ServletRequest;

import org.apache.struts.action.ActionMapping;

/**
 * Form allows user to set Weblog Entry query and export parameters.
 * 
 * @struts.form name="weblogEntryManagementForm"
 * @author Dave Johnson
 */
public class WeblogEntryManagementForm
    extends    org.apache.struts.action.ActionForm
    implements java.io.Serializable
{    
    private String mEndDateString;
    private String mStartDateString;
    private String mFileBy = "month";
    private String mExportFormat = "rss";
    private String mCategoryId = null;
    private String mStatus = "ALL";
    private String mSortby = "pubTime";
    
    /** max entries displayed per page */
    private int count = 30; 
    
     /** offset into current query results */
    private int offset = 0;
    
    // TODO : Implement option for RSS2 or Atom

    public void reset( ActionMapping mapping, ServletRequest request)
    {                
        // reset time fields to current time
        Calendar cal = Calendar.getInstance(request.getLocale());
        Date now = new Date();
        cal.setTime(now);        
    
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, request.getLocale());
        mEndDateString = df.format( now );
        
        cal.set(Calendar.DAY_OF_MONTH, 1);
        mStartDateString = df.format( cal.getTime() );
    }
    
    /**
     * @return Returns the mStartDateString.
     */
    public String getStartDateString()
    {
        return this.mStartDateString;
    }
    /**
     * @param startDateString The mStartDateString to set.
     */
    public void setStartDateString(String startDateString)
    {
        this.mStartDateString = startDateString;
    }
    
    /**
     * @return Returns the mDateString.
     */
    public String getEndDateString()
    {
        return this.mEndDateString;
    }
    
    /**
     * @param dateString The mDateString to set.
     */
    public void setEndDateString(String dateString)
    {
        this.mEndDateString = dateString;
    }
    
    /**
     * @return Returns the mFileBy.
     */
    public String getFileBy()
    {
        return this.mFileBy;
    }
    
    /**
     * @param fileBy The mFileBy to set.
     */
    public void setFileBy(String fileBy)
    {
        this.mFileBy = fileBy;
    }
    
    /**
     * @return Returns the mExportFormat.
     */
    public String getExportFormat()
    {
        return this.mExportFormat;
    }
    
    /**
     * @param exportFormat The mExportFormat to set.
     */
    public void setExportFormat(String exportFormat)
    {
        this.mExportFormat = exportFormat;
    }
    /**
     * @return Returns the category.
     */
    public String getCategoryId()
    {
        return mCategoryId;
    }
    /**
     * @param category The category to set.
     */
    public void setCategoryId(String category)
    {
        mCategoryId = category;
    }
    /**
     * @return Returns the status.
     */
    public String getStatus()
    {
        return mStatus;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(String status)
    {
        mStatus = status;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    } 
    
    public String getSortby() {
        return mSortby;
    }
    
    public void setSortby(String sortby) {
        mSortby = sortby;
    }
}
