/*
 * Created on Mar 4, 2004
 */
package org.roller.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.pojos.RefererData;
import org.roller.pojos.WebsiteData;
import org.roller.util.ThreadManager;

import java.util.List;

/**
 * @author lance.lavandowska
 */
public class MockRefererManager implements RefererManager
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MockUserManager.class);
    
    private PersistenceStrategy mStrategy = null;

    /**
     * @param strategy
     * @param roller
     */
    public MockRefererManager(PersistenceStrategy strategy, UserManager userMgr)
    {
        mStrategy = strategy;
    }

    /* 
     * @see org.roller.model.RefererManager#getReferers(org.roller.pojos.WebsiteData)
     */
    public List getReferers(WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.RefererManager#getTodaysReferers(org.roller.pojos.WebsiteData)
     */
    public List getTodaysReferers(WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.RefererManager#getReferersToDate(org.roller.pojos.WebsiteData, java.lang.String)
     */
    public List getReferersToDate(WebsiteData website, String date)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.RefererManager#getDaysPopularWebsites(int)
     */
    public List getDaysPopularWebsites(int max) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.RefererManager#getReferersToEntry(java.lang.String)
     */
    public List getReferersToEntry(String entryid) throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.RefererManager#getEntryReferers(java.lang.String, boolean)
     */
    public List getEntryReferers(String entryId, boolean authorized)
            throws RollerException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /* 
     * @see org.roller.model.RefererManager#getDayHits(org.roller.pojos.WebsiteData)
     */
    public int getDayHits(WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* 
     * @see org.roller.model.RefererManager#getTotalHits(org.roller.pojos.WebsiteData)
     */
    public int getTotalHits(WebsiteData website) throws RollerException
    {
        // TODO Auto-generated method stub
        return 0;
    }

    /* 
     * @see org.roller.model.RefererManager#processRequest(org.roller.model.ParsedRequest)
     */
    public void processRequest(ParsedRequest request)
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.RefererManager#setThreadManager(org.roller.util.ThreadManager)
     */
    public void setThreadManager(ThreadManager threadManager)
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.RefererManager#forceTurnover(java.lang.String)
     */
    public void forceTurnover(String websiteId) throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.RefererManager#checkForTurnover(boolean, java.lang.String)
     */
    public void checkForTurnover(boolean forceTurnover, String websiteId)
            throws RollerException
    {
        // TODO Auto-generated method stub

    }

    /* 
     * @see org.roller.model.RefererManager#retrieveReferer(java.lang.String)
     */
    public RefererData retrieveReferer(String id) throws RollerException
    {
        return (RefererData)mStrategy.load(id, RefererData.class);
    }

    /* 
     * @see org.roller.model.RefererManager#removeReferer(java.lang.String)
     */
    public void removeReferer(String id) throws RollerException
    {
        mStrategy.remove(id, RefererData.class);
    }

    /* 
     * @see org.roller.model.RefererManager#release()
     */
    public void release()
    {
    }

    /* (non-Javadoc)
     * @see org.roller.model.RefererManager#removeReferersForEntry(java.lang.String)
     */
    public void removeReferersForEntry(String entryid) throws RollerException
    {
        // TODO Auto-generated method stub
        
    }

}
