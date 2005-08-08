
package org.roller.model;
import org.roller.RollerException;
import org.roller.business.ThreadManagerImpl;
import org.roller.pojos.RefererData;
import org.roller.pojos.WebsiteData;

import java.io.Serializable;
import java.util.List;


/////////////////////////////////////////////////////////////////////////////
/**
 * Interface to Referer management.
 * @author David M Johnson
 */
public interface RefererManager extends Serializable
{
    //------------------------------------------ Access to Referer information

    /**
     * Get all referers for specified user.
     * @param userName
     * @return List of type RefererData
     * @throws RollerException
     */
    public List getReferers(WebsiteData website)
		throws RollerException;

    /**
     * Get all referers for specified user that were made today.
     * @param userName Name of user.
     * @return List of type RefererData
     * @throws RollerException
     */
    public List getTodaysReferers(WebsiteData website)
        throws RollerException;

    /**
     * Get referers for a specified date.
     * @param userName Name of user.
     * @param date YYYYMMDD format of day's date.
     * @return List of type RefererData.
     * @throws RollerException
     */
    public List getReferersToDate(WebsiteData website, String date)
        throws RollerException;

    /**
     * Get most popular websites based on referer day hits.
     * @return List of WebsiteDisplayData objects.
     */
    public List getDaysPopularWebsites(int max) throws RollerException;

    /**
     * Get referers that refer to a specific weblog entry.
     * @param entryid Weblog entry ID
     * @return List of RefererData objects.
     * @throws RollerException
     */
    public List getReferersToEntry(String entryid)
        throws RollerException;

    /**
     * Remove all referers for the specific weblog entry.
	 * @param entryId Weblog entry ID
	 * @throws RollerException
     */
    public void removeReferersForEntry(String entryid) throws RollerException;
    
    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    public void applyRefererFilters() throws RollerException;

    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(WebsiteData website) throws RollerException;

	/**
	 * Get referers that refer to a specific weblog entry.
	 * @param entryId Weblog entry ID
	 * @param authorized Is the current user authorized to edit these Referers.
	 * @return List of RefererData objects.
	 * @throws RollerException
	 */
	public List getEntryReferers(String entryId, boolean authorized)
		throws RollerException;

    /** Get user's day hits */
    public int getDayHits(WebsiteData website) throws RollerException;

    /** Get user's all-time total hits */
    public int getTotalHits(WebsiteData website) throws RollerException;

    //--------------------------------------------- Referer processing methods

    /**
     * Process request for incoming referers.
     * @param request Request to be processed.
     * @return boolean True if the referer header contains an ignore/spam word.
     */
    public boolean processRequest(ParsedRequest request);

    //---------------------------------------------- Referer tracking turnover

    /**
     * Force refer of referer hit counts and deletes all referers that do nto have excerpts.
     */
    public void forceTurnover(String websiteId) throws RollerException;

    /**
     * Check to see if it's time for turnover.
     */
    public void checkForTurnover(boolean forceTurnover, String websiteId)
            throws RollerException;

    //----------------------------------------------- Standard manager methods

    /**
     * Retrieve referer specifie by ID.
     */
    public RefererData retrieveReferer(String id)
        throws RollerException;

    /**
     * Remove referer specified by ID.
     */
    public void removeReferer( String id )
        throws RollerException;

    /**
     * Release all resources associated with Roller session.
     */
    public void release();
}

