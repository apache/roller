package org.roller.business;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;
import org.roller.model.WeblogManager;
import org.roller.pojos.RefererData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.util.DateUtil;
import org.roller.util.LinkbackExtractor;
import org.roller.util.SpamChecker;
import org.roller.util.Utilities;


/**
 * Abstract base implementation using PersistenceStrategy.
 * @author Dave Johnson
 * @author Lance Lavandowska
 */
public abstract class RefererManagerImpl implements RefererManager
{
    static Log mLogger =
        LogFactory.getFactory().getInstance(RefererManagerImpl.class);

    protected static final String DAYHITS = "dayHits";
    protected static final String TOTALHITS = "totalHits";

    protected PersistenceStrategy mStrategy;
    protected Date mRefDate = new Date();
    protected SimpleDateFormat mDateFormat = DateUtil.get8charDateFormat();

    protected abstract List getReferersWithSameTitle(
                    WebsiteData website, 
                    String requestUrl, 
                    String title, 
                    String excerpt)
                    throws RollerException;
                    
    protected abstract List getExistingReferers(
                    WebsiteData website, 
                    String dateString,
                    String permalink) throws RollerException;

    protected abstract List getReferersToWebsite(
                    WebsiteData website, 
                    String refererUrl) throws RollerException;

    protected abstract List getMatchingReferers(
                    WebsiteData website, 
                    String requestUrl,
                    String refererUrl) throws RollerException;

    //-----------------------------------------------------------------------

    public RefererManagerImpl()
    {
    }

    //-----------------------------------------------------------------------

    protected abstract int getHits(WebsiteData website, String type)
        throws RollerException;

    //------------------------------------------------------------------------

    public void release()
    {
    }

    //--------------------------------------------------------- Get hit counts

    public int getDayHits(WebsiteData website) throws RollerException
    {
        return getHits(website, DAYHITS);
    }

    //-----------------------------------------------------------------------

    public int getTotalHits(WebsiteData website) throws RollerException
    {
        return getHits(website, TOTALHITS);
    }


    //------------------------------------------------------- Referer Storage

    /**
     * @see org.roller.pojos.RefererManager#removeReferer(java.lang.String)
     */
    public void removeReferer(String id) throws RollerException
    {
        mStrategy.remove(id, RefererData.class);
    }

    //-----------------------------------------------------------------------

    /**
     * @see org.roller.pojos.RefererManager#retrieveReferer(java.lang.String)
     */
    public RefererData retrieveReferer(String id) throws RollerException
    {
        return (RefererData)mStrategy.load(id,RefererData.class);
    }

    //-----------------------------------------------------------------------

    /**
     * @see org.roller.pojos.RefererManager#storeReferer(
     * org.roller.pojos.RefererData)
     */
    public void storeReferer(RefererData data) throws RollerException
    {
        mStrategy.store(data);
    }

    //-----------------------------------------------------------------------
    public List getEntryReferers(String entryId, boolean authorized)
        throws RollerException
    {
        //TODO: Redesign this so this is performed using the DB query, and
        // not in java code for perf/memory reasons
        List authorizedvisible = new ArrayList();
        List referers = getReferersToEntry(entryId);
        for (Iterator rItr = referers.iterator(); rItr.hasNext();) 
        {
            RefererData referer = (RefererData) rItr.next();
            if ( referer.getVisible().booleanValue() || authorized )
            {
                authorizedvisible.add( referer );
            }
        }

        return authorizedvisible;
    }

    //------------------------------------------------------------------------

    public void processReferrer(
            String requestUrl, 
            String referrerUrl,
            String weblogHandle, 
            String entryAnchor, 
            String dateString) {
        
        mLogger.debug("processing referrer ["+referrerUrl+
                "] accessing ["+requestUrl+"]");
        
        if (weblogHandle == null)
            return;
        
        String selfSiteFragment = "/page/"+weblogHandle;
        WebsiteData weblog = null;
        WeblogEntryData entry = null;
        
        // lookup the weblog now
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            weblog = userMgr.getWebsiteByHandle(weblogHandle);            
            if (weblog == null) return;
            
            // now lookup weblog entry if possible
            if (entryAnchor != null) {
                WeblogManager weblogMgr = RollerFactory.getRoller().getWeblogManager();
                entry = weblogMgr.getWeblogEntryByAnchor(weblog, entryAnchor);
            }
        } catch (RollerException re) {
            // problem looking up website, gotta bail
            mLogger.error("Error looking up website object", re);
            return;
        }
        
        try {
            List matchRef = null;
            
            // try to find existing RefererData for referrerUrl
            if (referrerUrl == null || referrerUrl.trim().length() < 8) {
                referrerUrl = "direct";
                
                // Get referer specified by referer URL of direct
                matchRef = getReferersToWebsite(weblog, referrerUrl);
            } else {
                referrerUrl = Utilities.stripJsessionId(referrerUrl);
                
                // Query for referer with same referer and request URLs
                matchRef = getMatchingReferers(weblog, requestUrl, referrerUrl);
                
                // If referer was not found, try adding or leaving off 'www'
                if ( matchRef.size() == 0 ) {
                    String secondTryUrl = null;
                    if ( referrerUrl.startsWith("http://www") ) {
                        secondTryUrl = "http://"+referrerUrl.substring(11);
                    } else {
                        secondTryUrl = "http://www"+referrerUrl.substring(7);
                    }
                    
                    matchRef = getMatchingReferers(weblog, requestUrl, secondTryUrl);
                    if ( matchRef.size() == 1 ) {
                        referrerUrl = secondTryUrl;
                    }
                }
            }
            
            if (matchRef.size() == 1) {
                // Referer was found in database, so bump up hit count
                RefererData ref = (RefererData)matchRef.get(0);
                
                ref.setDayHits(new Integer(ref.getDayHits().intValue() + 1));
                ref.setTotalHits(new Integer(ref.getTotalHits().intValue() + 1));
                
                mLogger.debug("Incrementing hit count on existing referer: "+referrerUrl);
                
                storeReferer(ref);
                mStrategy.commit();
                
            } else if (matchRef.size() == 0) {
                
                // Referer was not found in database, so new Referer object
                Integer one = new Integer(1);
                RefererData ref =
                    new RefererData(
                        null,
                        weblog,
                        entry,
                        dateString,
                        referrerUrl,
                        null,
                        requestUrl,
                        null,
                        "", // Read comment above regarding Derby bug
                        Boolean.FALSE,
                        Boolean.FALSE,
                        one,
                        one);
                
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug("newReferer="+ref.getRefererUrl());
                }
                
                String refurl = ref.getRefererUrl();
                
                // If not a direct or search engine then search for linkback
                boolean doLinkbackExtraction = 
                    RollerRuntimeConfig.getBooleanProperty("site.linkbacks.enabled");
                if (doLinkbackExtraction
                    && entry != null
                    && !refurl.equals("direct")
                    && !refurl.startsWith("http://google")
                    && !refurl.startsWith("http://www.google")
                    && !refurl.startsWith("http://search.netscape")
                    && !refurl.startsWith("http://www.blinkpro")
                    && !refurl.startsWith("http://search.msn")
                    && !refurl.startsWith("http://search.yahoo")
                    && !refurl.startsWith("http://uk.search.yahoo")
                    && !refurl.startsWith("http://www.javablogs.com")
                    && !refurl.startsWith("http://www.teoma")
                ) {
                    // Launch thread to extract referer linkback
                    
                    try {
                        Roller mRoller = RollerFactory.getRoller();
                        mRoller.getThreadManager().executeInBackground(
                            new LinkbackExtractorRunnable(ref));
                    } catch (InterruptedException e) {
                        mLogger.warn("Interrupted during linkback extraction",e);
                    }
                } else {
                    storeReferer(ref);
                    mStrategy.commit();
                }
            }
        } catch (RollerException pe) {
            mLogger.error(pe);
        } catch (NullPointerException npe) {
            mLogger.error(npe);
        }
    }
        
    /**
     * Use LinkbackExtractor to parse title and excerpt from referer
     */
    class LinkbackExtractorRunnable implements Runnable
    {

        private RefererData mReferer = null;

        public LinkbackExtractorRunnable( RefererData referer)
        {
            mReferer = referer;
        }

        public void run()
        {

            try
            {
                LinkbackExtractor lb = new LinkbackExtractor(
                    mReferer.getRefererUrl(),mReferer.getRequestUrl());

                if ( lb.getTitle()!=null && lb.getExcerpt()!=null )
                {
                    mReferer.setTitle(lb.getTitle());
                    mReferer.setExcerpt(lb.getExcerpt());

                    if ( lb.getPermalink() != null )
                    {
                        // The presence of a permalink indicates that this
                        // linkback was parsed out of an RSS feed and is
                        // presumed to be a good linkback.

                        mReferer.setRefererPermalink(lb.getPermalink());

                        // See if this request/permalink is in the DB
                        List matchRef = getExistingReferers(
                            mReferer.getWebsite(),
                            mReferer.getDateString(),
                            mReferer.getRefererPermalink());

                        // If it is the first, then set it to be visible
                        if ( matchRef.size() == 0 )
                        {
                            mReferer.setVisible(Boolean.TRUE);
                        }
                        else
                        {
                            // We can't throw away duplicates or we will
                            // end up reparsing them everytime a hit comes
                            // in from one of them, but we can mark them
                            // as duplicates.
                            mReferer.setDuplicate(Boolean.TRUE);
                        }

                        storeReferer(mReferer);
                        mStrategy.commit();

                    }

                    else
                    {
                        // Store the new referer
                        storeReferer(mReferer);

                        // Hacky Referer URL weighting kludge:
                        //
                        // If there are multple referers to a request URL,
                        // then we want to pick the best one. The others
                        // are marked as duplicates. To do this we use a
                        // weight. The weight formula is:
                        //
                        // w = URL length + (100 if URL contains anchor)

                        // LOOP: find the referer with the highest weight
                        Boolean visible = Boolean.FALSE;
                        List refs= getReferersWithSameTitle(
                            mReferer.getWebsite(),
                            mReferer.getRequestUrl(),
                            lb.getTitle(),
                            lb.getExcerpt());
                        RefererData chosen = null;
                        int maxweight = 0;
                        for (Iterator rdItr = refs.iterator();rdItr.hasNext();)
                        {
                            RefererData referer = (RefererData) rdItr.next();

                            int weight = referer.getRefererUrl().length();
                            if (referer.getRefererUrl().indexOf('#') != -1)
                            {
                                weight += 100;
                            }

                            if ( weight > maxweight )
                            {
                                chosen = referer;
                                maxweight = weight;
                            }

                            if (referer.getVisible().booleanValue())
                            {
                                // If any are visible then chosen
                                // replacement must be visible as well.
                                visible = Boolean.TRUE;
                            }

                        }

                        // LOOP: to mark all of the lower weight ones
                        // as duplicates
                        for (Iterator rdItr = refs.iterator();rdItr.hasNext();) {
                            RefererData referer = (RefererData) rdItr.next();

                            if (referer != chosen)
                            {
                                referer.setDuplicate(Boolean.TRUE);
                            }
                            else
                            {
                                referer.setDuplicate(Boolean.FALSE);
                                referer.setVisible(visible);
                            }
                            storeReferer(referer);
                            mStrategy.commit();
                        }


                    }
                }
                else
                {
                    // It is not a linkback, but store it anyway
                    storeReferer(mReferer);
                    mStrategy.commit();

                    mLogger.info("No excerpt found at refering URL "
                        + mReferer.getRefererUrl());
                }
            }
            catch (Exception e)
            {
                mLogger.error("Processing linkback",e);
            }
            finally
            {
                try {
                    mStrategy.release();
                }
                catch (RollerException e) {
                    mLogger.error(
                    "Exception logged by ManagerSupport.releaseDatabase()");
                }
            }

        }

    }

}


