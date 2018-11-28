
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */
package org.apache.roller.weblogger.business.jpa;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;
import java.util.Comparator;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.lang.StringUtils;

import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.StatCountCountComparator;
import org.apache.roller.weblogger.util.LinkbackExtractor;
import org.apache.roller.weblogger.util.Utilities;

/*
 * JPARefererManagerImpl.java
 */
@com.google.inject.Singleton
public class JPARefererManagerImpl implements RefererManager {

    private static Log log = LogFactory.getLog(
        JPARefererManagerImpl.class);

    protected static final String DAYHITS = "dayHits";
    protected static final String TOTALHITS = "totalHits";
    
    private static final Comparator statCountCountReverseComparator = 
            Collections.reverseOrder(StatCountCountComparator.getInstance());
    
    /** The strategy for this manager. */
    private final Weblogger roller;
    private final JPAPersistenceStrategy strategy;
    

    /**
     * Creates a new instance of JPARefererManagerImpl
     */
    @com.google.inject.Inject
    protected JPARefererManagerImpl(Weblogger roller, JPAPersistenceStrategy strategy) {
        log.debug("Instantiating JPA Referer Manager");
        this.roller = roller;
        this.strategy = strategy;
    }

    
    public void saveReferer(WeblogReferrer referer) throws WebloggerException {
        strategy.store(referer);
    }

    public void removeReferer(WeblogReferrer referer) throws WebloggerException {
        strategy.remove(referer);
    }

    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers() throws WebloggerException {
        clearDayHits();
        Query q = strategy.getNamedUpdate("WeblogReferrer.removeByNullOrEmptyExcerpt");
        q.executeUpdate();
    }

    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers(Weblog website) throws WebloggerException {
        clearDayHitsByWebsite(website);
        Query q = strategy.getNamedUpdate("WeblogReferrer.removeByNullOrEmptyExcerpt&Website");
        q.setParameter(1, website);
        q.executeUpdate();
    }

    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    public void applyRefererFilters() throws WebloggerException {
        String spamwords = WebloggerRuntimeConfig.getProperty("spam.blacklist");
        String[] blacklist = StringUtils.split(
                StringUtils.deleteWhitespace(spamwords),",");
        if (blacklist.length == 0) return;
        List referers = getBlackListedReferer(blacklist);
        for (Iterator iterator = referers.iterator(); iterator.hasNext();) {
            WeblogReferrer referer= (WeblogReferrer) iterator.next();
            this.strategy.remove(referer);
        }
    }

    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(Weblog website)
            throws WebloggerException {
        if (null == website) throw new WebloggerException("website is null");
        if (null == website.getBlacklist()) return;
        
        String[] blacklist = StringUtils.split(
                StringUtils.deleteWhitespace(website.getBlacklist()),",");
        if (blacklist.length == 0) return;
        List referers = getBlackListedReferer(website, blacklist);
        for (Iterator iterator = referers.iterator(); iterator.hasNext();) {
            WeblogReferrer referer= (WeblogReferrer) iterator.next();
            this.strategy.remove(referer);
        }
    }

    protected List getExistingReferers(Weblog website, String dateString,
            String permalink) throws WebloggerException {

        Query q = strategy.getNamedQuery( 
            "WeblogReferrer.getByWebsite&DateString&RefererPermalink");
        q.setParameter(1, website);
        q.setParameter(2, dateString);
        q.setParameter(3, permalink);
        return q.getResultList();
    }

    protected List getMatchingReferers(Weblog website, String requestUrl,
            String refererUrl) throws WebloggerException {

        Query q = strategy.getNamedQuery( 
            "WeblogReferrer.getByWebsite&RequestUrl&RefererUrl");
        q.setParameter(1, website);
        q.setParameter(2, requestUrl);
        q.setParameter(3, refererUrl);
        return q.getResultList();
    }

    /**
     * Returns hot weblogs as StatCount objects, in descending order by today's
     * hits.
     * @param sinceDays Restrict to last X days (or -1 for all)
     * @param offset Offset into results (for paging)
     * @param length Maximum number of results to return (for paging)
     * @return List of StatCount objects.
     */
    public List getHotWeblogs(int sinceDays, int offset, int length)
            throws WebloggerException {
        
        String msg = "Getting hot weblogs";
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
             
        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        Query q = strategy.getNamedQuery( 
            "WeblogReferrer.getHotWeblogsByWebsite.enabled&Website.active&Website.lastModifiedGreater");
        
        if (offset != 0 || length != -1) {
            q.setFirstResult(offset);
            q.setMaxResults(length);
        }
        Timestamp start = new Timestamp(startDate.getTime());
        q.setParameter(1, Boolean.TRUE);
        q.setParameter(2, Boolean.TRUE);
        q.setParameter(3, start);
        List queryResults = (List)q.getResultList();
        for (Iterator it = queryResults.iterator(); it.hasNext(); ) {
            Object[] row = (Object[])it.next();
            long hits = ((Number)row[0]).longValue();
            String websiteId = (String)row[1];
            String websiteName = (String)row[2];
            String websiteHandle = (String)row[3];
            results.add(new StatCount(
                websiteId,
                websiteHandle,
                websiteName,
                "statCount.weblogDayHits",
                hits));              
        }
        // Original query ordered by desc hits.
        // JPA QL doesn't allow queries to be ordered by agregates; do it in memory
        Collections.sort(results, statCountCountReverseComparator);

        return results;
    }

    protected int getHits(Weblog website, String type) 
            throws WebloggerException {
        int hits = -1;
        if (log.isDebugEnabled()) {
            log.debug("getHits: " + website.getName());
        }
        //TODO: JPAPort. This query retrieves both SUM(r.dayHits), SUM(r.totalHits)
        //The method only comsumes one of them. We can optimize the logic to retrieve only the 
        //requied SUM
        Query query = strategy.getNamedQuery(
            "WeblogReferrer.getHitsByWebsite.enabled&Website.id");
        query.setParameter(1, Boolean.TRUE);
        query.setParameter(2, website.getId());
        List results = query.getResultList();

        Object[] resultsArray = (Object[]) results.get(0);
        
        if (resultsArray.length > 0 && type.equals(DAYHITS)) {
            if ( resultsArray[0] != null ) {
                hits = ((Long) resultsArray[0]).intValue();
            }
        } else if ( resultsArray.length > 0 ) {
            if ( resultsArray[0] != null ) {
                hits = ((Long) resultsArray[1]).intValue();
            }
        } else {
            hits = 0;
        }
        
        return hits;
    }

    /**
     * Get all referers for specified weblog.
     * @param weblog
     * @return List of type WeblogReferrer
     */
    public List getReferers(Weblog weblog) throws WebloggerException {
        Query q = strategy.getNamedQuery(
            "WeblogReferrer.getByWebsiteOrderByTotalHitsDesc");
        q.setParameter(1, weblog);
        return q.getResultList();
    }

    /**
     * Get all referers for specified user that were made today.
     * @param website Web site.
     * @return List of type WeblogReferrer
     */
    public List getTodaysReferers(Weblog website) throws WebloggerException {
        Query q = strategy.getNamedQuery(
            "WeblogReferrer.getByWebsite&DayHitsGreaterZeroOrderByDayHitsDesc");
        q.setParameter(1, website);
        return q.getResultList();
    }

    /**
     * Get referers for a specified date.
     * @param website Web site.
     * @param date YYYYMMDD format of day's date.
     * @return List of type WeblogReferrer.
     * @throws org.apache.roller.weblogger.WebloggerException
     */
    public List getReferersToDate(Weblog website, String date)
            throws WebloggerException {

        if (website==null )
            throw new WebloggerException("website is null");
        
        if (date==null )
            throw new WebloggerException("Date is null");
        
        Query q = strategy.getNamedQuery(
            "WeblogReferrer.getByWebsite&DateString&DuplicateOrderByTotalHitsDesc");
        q.setParameter(1, website);
        q.setParameter(2, date);
        q.setParameter(3, Boolean.FALSE);
        return q.getResultList();
    }

    /**
     * Get referers that refer to a specific weblog entry.
     * @param entryid Weblog entry ID
     * @return List of WeblogReferrer objects.
     * @throws org.apache.roller.weblogger.WebloggerException
     */
    public List getReferersToEntry(String entryid) throws WebloggerException {
        if (null == entryid)
            throw new WebloggerException("entryid is null");
        //TODO: DataMapperPort: Change calling code to pass WeblogEntry instead of id
        // we should change calling code to pass instance of WeblogEntry instead
        // of extracting and passing id. Once that is done, change the code below to
        // skip the load (Please note that the load below will always find the enty in cache)
        Query q = strategy.getNamedQuery(
            "WeblogReferrer.getByWeblogEntry&TitleNotNull&ExcerptNotNullOrderByTotalHitsDesc");
        q.setParameter(1, strategy.load(WeblogEntry.class, entryid));
        return q.getResultList();
    }

    /**
     * Query for collection of referers.
     */
    protected List getReferersToWebsite(Weblog website, String refererUrl)
            throws WebloggerException {
        Query q = strategy.getNamedQuery( 
            "WeblogReferrer.getByWebsite&RefererUrl");
        q.setParameter(1, website);
        q.setParameter(2, refererUrl);
        return q.getResultList();
    }

    /**
     * Query for collection of referers.
     */
    protected List getReferersWithSameTitle(Weblog website,
                                            String requestUrl,
                                            String title,
                                            String excerpt)
            throws WebloggerException {
        Query q = strategy.getNamedQuery( 
            "WeblogReferrer.getByWebsite&RequestURL&TitleOrExcerpt");
        q.setParameter(1, website);
        q.setParameter(2, requestUrl);
        q.setParameter(3, title);
        q.setParameter(4, excerpt);
        return q.getResultList();
    }

    /**
     * Get user's day hits
     */
    public int getDayHits(Weblog website) throws WebloggerException {
        return getHits(website, DAYHITS);
    }

    /**
     * Get user's all-time total hits
     */
    public int getTotalHits(Weblog website) throws WebloggerException {
        return getHits(website, TOTALHITS);
    }

    /**
     * Retrieve referer by id.
     */
    public WeblogReferrer getReferer(String id) throws WebloggerException {
        return (WeblogReferrer)strategy.load(WeblogReferrer.class, id);
    }

    /**
     * Process an incoming referer.
     */
    public void processReferrer(String requestUrl, String referrerUrl,
            String weblogHandle, String entryAnchor, String dateString) {
        log.debug("processing referrer ["+referrerUrl+
                "] accessing ["+requestUrl+"]");

        if (weblogHandle == null)
            return;

        String selfSiteFragment = "/"+weblogHandle;
        Weblog weblog = null;
        WeblogEntry entry = null;

        // lookup the weblog now
        try {
            weblog = roller.getWeblogManager().getWeblogByHandle(weblogHandle);
            if (weblog == null) return;

            // now lookup weblog entry if possible
            if (entryAnchor != null) {
                // sanitize the anchor to avoid "Illegal mix of collations"
                entryAnchor = Utilities.replaceNonAlphanumeric(entryAnchor, ' ').trim();
                entry = roller.getWeblogEntryManager().getWeblogEntryByAnchor(weblog, entryAnchor);
            }
        } catch (WebloggerException re) {
            // problem looking up website, gotta bail
            log.error("Error looking up website object", re);
            return;
        }

        try {
            List matchRef = null;

            // try to find existing WeblogReferrer for referrerUrl
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

                    matchRef = getMatchingReferers(weblog, requestUrl, 
                        secondTryUrl);
                    if ( matchRef.size() == 1 ) {
                        referrerUrl = secondTryUrl;
                    }
                }
            }

            if (matchRef.size() == 1) {
                // Referer was found in database, so bump up hit count
                WeblogReferrer ref = (WeblogReferrer)matchRef.get(0);

                ref.setDayHits(new Integer(ref.getDayHits().intValue() + 1));
                ref.setTotalHits(new Integer(ref.getTotalHits().intValue() + 1));

                log.debug("Incrementing hit count on existing referer: " +
                    referrerUrl);

                saveReferer(ref);

            } else if (matchRef.size() == 0) {

                // Referer was not found in database, so new Referer object
                Integer one = new Integer(1);
                WeblogReferrer ref =
                        new WeblogReferrer(
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

                if (log.isDebugEnabled()) {
                    log.debug("newReferer="+ref.getRefererUrl());
                }

                String refurl = ref.getRefererUrl();

                // If not a direct or search engine then search for linkback
                boolean doLinkbackExtraction =
                    WebloggerRuntimeConfig.getBooleanProperty(
                        "site.linkbacks.enabled");
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
                        Weblogger mRoller = roller;
                        mRoller.getThreadManager().executeInBackground(
                                new LinkbackExtractorRunnable(ref));
                    } catch (InterruptedException e) {
                        log.warn("Interrupted during linkback extraction",e);
                    }
                } else {
                    saveReferer(ref);
                }
            }
        } catch (WebloggerException pe) {
            log.error(pe);
        } catch (NullPointerException npe) {
            log.error(npe);
        }
    }

    /**
     * Use LinkbackExtractor to parse title and excerpt from referer
     */
    class LinkbackExtractorRunnable implements Runnable {

        private WeblogReferrer mReferer = null;

        public LinkbackExtractorRunnable( WeblogReferrer referer) {
            mReferer = referer;
        }

        public void run() {

            try {
                LinkbackExtractor lb = new LinkbackExtractor(
                        mReferer.getRefererUrl(),mReferer.getRequestUrl());

                if ( lb.getTitle()!=null && lb.getExcerpt()!=null ) {
                    mReferer.setTitle(lb.getTitle());
                    mReferer.setExcerpt(lb.getExcerpt());

                    if ( lb.getPermalink() != null ) {
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
                        if ( matchRef.size() == 0 ) {
                            mReferer.setVisible(Boolean.TRUE);
                        } else {
                            // We can't throw away duplicates or we will
                            // end up reparsing them everytime a hit comes
                            // in from one of them, but we can mark them
                            // as duplicates.
                            mReferer.setDuplicate(Boolean.TRUE);
                        }

                        saveReferer(mReferer);

                    }

                    else {
                        // Store the new referer
                        saveReferer(mReferer);

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
                        WeblogReferrer chosen = null;
                        int maxweight = 0;
                        for (Iterator rdItr = refs.iterator();rdItr.hasNext();) {
                            WeblogReferrer referer = (WeblogReferrer) rdItr.next();

                            int weight = referer.getRefererUrl().length();
                            if (referer.getRefererUrl().indexOf('#') != -1) {
                                weight += 100;
                            }

                            if ( weight > maxweight ) {
                                chosen = referer;
                                maxweight = weight;
                            }

                            if (referer.getVisible().booleanValue()) {
                                // If any are visible then chosen
                                // replacement must be visible as well.
                                visible = Boolean.TRUE;
                            }

                        }

                        // LOOP: to mark all of the lower weight ones
                        // as duplicates
                        for (Iterator rdItr = refs.iterator();rdItr.hasNext();) {
                            WeblogReferrer referer = (WeblogReferrer) rdItr.next();

                            if (referer != chosen) {
                                referer.setDuplicate(Boolean.TRUE);
                            } else {
                                referer.setDuplicate(Boolean.FALSE);
                                referer.setVisible(visible);
                            }
                            saveReferer(referer);
                        }


                    }
                } else {
                    // It is not a linkback, but store it anyway
                    saveReferer(mReferer);

                    log.info("No excerpt found at refering URL "
                            + mReferer.getRefererUrl());
                }
            } catch (Exception e) {
                log.error("Processing linkback",e);
            } finally {
                strategy.release();
            }

        }

    }
    
    /**
     * Release all resources held by manager.
     */
    public void release() {}
    
    protected void clearDayHits() throws WebloggerException {
        Query query = strategy.getNamedUpdate("WeblogReferrer.clearDayHits");
        query.executeUpdate();
    }

    protected void clearDayHitsByWebsite(Weblog website) throws WebloggerException {
        Query query = strategy.getNamedUpdate("WeblogReferrer.clearDayHitsByWebsite");
        query.setParameter(1, website);
        query.executeUpdate();
    }

    protected List getBlackListedReferer(String[] blacklist) throws
            WebloggerException {
        StringBuffer queryString = getQueryStringForBlackList(blacklist);
        Query query = strategy.getDynamicQuery(queryString.toString());
        return (List) query.getResultList();
    }

    protected List getBlackListedReferer(Weblog website, String[] blacklist) 
            throws WebloggerException {
        StringBuffer queryString = getQueryStringForBlackList(blacklist);
        queryString.append(" AND r.website = ?1 ");
        Query query = strategy.getDynamicQuery(queryString.toString());
        query.setParameter(1, website);
        return query.getResultList();
    }

    /**
     * Generates a JPQL query of form
     * SELECT r FROM WeblogReferrer r WHERE
     *     ( refererUrl like %blacklist[1] ..... OR refererUrl like %blacklist[n])
     * @param blacklist
     * @return
     */
    private StringBuffer getQueryStringForBlackList(String[] blacklist) {
        assert blacklist.length > 0;
        StringBuffer queryString = new StringBuffer("SELECT r FROM WeblogReferrer r WHERE (");
        //Search for any matching entry from blacklist[]
        final String OR = " OR ";
        for (int i = 0; i < blacklist.length; i++) {
            String ignoreWord = blacklist[i];
            //TODO: DataMapper port: original code use "like ignore case" as follows
            // or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
            // There is no equivalent for it in JPA
            queryString.append("r.refererUrl like '%").append(ignoreWord.trim()).append("%'").append(OR);
        }
        // Get rid of last OR
        queryString.delete(queryString.length() - OR.length(), queryString.length());
        queryString.append(" ) ");
        return queryString;
    }
}
