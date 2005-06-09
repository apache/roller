/*
 * Created on Apr 14, 2003
 */
package org.roller.presentation.weblog.formbeans;

import org.apache.struts.action.ActionForm;

/**
 * Data to be sent in a Trackback.
 * @author David M Johnson
 * @struts.form name="trackbackForm"
 */
public class TrackbackForm extends ActionForm
{
    private String mTrackbackURL = null;
    private String mEntryId = null;
    
    /**
     * 
     */
    public TrackbackForm()
    {
        super();
    }

    /**
     * @return
     */
    public String getEntryId()
    {
        return mEntryId;
    }

    /**
     * @return
     */
    public String getTrackbackURL()
    {
        return mTrackbackURL;
    }

    /**
     * @param string
     */
    public void setEntryId(String string)
    {
        mEntryId = string;
    }

    /**
     * @param string
     */
    public void setTrackbackURL(String string)
    {
        mTrackbackURL = string;
    }

}
