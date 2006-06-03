package org.roller.business.jdo;

import java.util.List;

import org.roller.RollerException;
import org.roller.business.RefererManagerImpl;
import org.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDORefererManagerImpl extends RefererManagerImpl {

    protected List getReferersWithSameTitle(WebsiteData website,
            String requestUrl, String title, String excerpt)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected List getExistingReferers(WebsiteData website, String dateString,
            String permalink) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected List getReferersToWebsite(WebsiteData website, String refererUrl)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected List getMatchingReferers(WebsiteData website, String requestUrl,
            String refererUrl) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    protected int getHits(WebsiteData website, String type)
            throws RollerException {
        // TODO Auto-generated method stub
        return 0;
    }

    public List getReferers(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getTodaysReferers(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getReferersToDate(WebsiteData website, String date)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getDaysPopularWebsites(int max) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getReferersToEntry(String entryid) throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public void removeReferersForEntry(String entryid) throws RollerException {
        // TODO Auto-generated method stub

    }

    public void applyRefererFilters() throws RollerException {
        // TODO Auto-generated method stub

    }

    public void applyRefererFilters(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub

    }

    public void clearReferrers() throws RollerException {
        // TODO Auto-generated method stub

    }

    public void clearReferrers(WebsiteData website) throws RollerException {
        // TODO Auto-generated method stub

    }

}