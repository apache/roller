
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
package org.apache.roller.business.datamapper;

import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Collections;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.roller.RollerException;

import org.apache.roller.business.Roller;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.StatCount;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.WebsiteDisplayData;
import org.apache.roller.util.LinkbackExtractor;
import org.apache.roller.util.Utilities;

/*
 * DatamapperReferrerManagerImpl.java
 *
 * Created on May 31, 2006, 4:06 PM
 *
 */
public class DatamapperReferrerManagerImpl implements RefererManager {

    private static Log log = LogFactory.getLog(
        DatamapperReferrerManagerImpl.class);

    protected static final String DAYHITS = "dayHits";
    protected static final String TOTALHITS = "totalHits";
    /** The strategy for this manager. */
    private DatamapperPersistenceStrategy strategy;

    /** Creates a new instance of DatamapperReferrerManagerImpl */
    public DatamapperReferrerManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        log.debug("Instantiating Datamapper Referer Manager");

        this.strategy = strategy;
    }

    /**
     * Store the referer.
     */
    public void saveReferer(RefererData referer) throws RollerException {
        strategy.store(referer);
    }

    /**
     * Remove a single referer.
     */
    public void removeReferer(RefererData referer) throws RollerException {
        strategy.remove(referer);
    }

    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers() throws RollerException {
        // TODO not implemented

//        Dialect currentDialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
//        String reset = "update RefererData set dayHits=0";
//        session.createQuery(reset).executeUpdate();
//        String delete = null;
//        if ( currentDialect instanceof SQLServerDialect || currentDialect instanceof OracleDialect ){
//            delete = "delete RefererData where excerpt is null or excerpt like ''";
//        } else {
//            delete = "delete RefererData where excerpt is null or excerpt=''";
//        }
//        session.createQuery(delete).executeUpdate();
    }

    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers(WebsiteData website) throws RollerException {
        // TODO not implemented

//        Dialect currentDialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
//        String reset = "update RefererData set dayHits=0 where website=:site";
//        session.createQuery(reset)
//        .setParameter("site",website).executeUpdate();
//        String delete = null;
//        if ( currentDialect instanceof SQLServerDialect || currentDialect instanceof OracleDialect ){
//            delete = "delete RefererData where website=:site and (excerpt is null or excerpt like '')";
//        } else {
//            delete = "delete RefererData where website=:site and (excerpt is null or excerpt='')";
//        }
//        session.createQuery(delete)
//        .setParameter("site",website).executeUpdate();
    }

    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    public void applyRefererFilters() throws RollerException {
        // TODO not implemented

//        Criteria criteria = session.createCriteria(RefererData.class);
//            
//        String spamwords = RollerRuntimeConfig.getProperty("spam.blacklist");
//            
//        String[] blacklist = StringUtils.split(
//                StringUtils.deleteWhitespace(spamwords),",");
//        Junction or = Expression.disjunction();
//        for (int i=0; i<blacklist.length; i++) {
//            String ignoreWord = blacklist[i].trim();
//            //log.debug("including ignore word - "+ignoreWord);
//            or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
//        }
//        criteria.add(Expression.conjunction()
//        .add(Expression.disjunction().add(Expression.isNull("excerpt")).add(Expression.eq("excerpt", "")))
//        .add(or)
//        );
//            
//        log.debug("removing spam referers - "+criteria.list().size());
//            
//        Iterator referer = criteria.list().iterator();
//        while (referer.hasNext()) {
//            this.strategy.remove((RefererData) referer.next());
//        }
    }

    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(WebsiteData website)
            throws RollerException {
        // TODO not implemented
        
        if (null == website) throw new RollerException("website is null");
        if (null == website.getBlacklist()) return;
        
//        Criteria criteria = session.createCriteria(RefererData.class);
//            
//        String[] blacklist = StringUtils.split(
//                StringUtils.deleteWhitespace(website.getBlacklist()),",");
//        if (blacklist.length == 0) return;
//            
//        Junction or = Expression.disjunction();
//        for (int i=0; i<blacklist.length; i++) {
//            String ignoreWord = blacklist[i].trim();
//            or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
//        }
//        criteria.add(Expression.conjunction()
//        .add(Expression.disjunction().add(Expression.isNull("excerpt")).add(Expression.eq("excerpt", "")))
//        .add(Expression.eq("website",website))
//        .add(or)
//        );
//            
//        Iterator referer = criteria.list().iterator();
//        while (referer.hasNext()) {
//            this.strategy.remove((RefererData) referer.next());
//        }        
    }

    protected List getExistingReferers(WebsiteData website, String dateString,
            String permalink) throws RollerException {

        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByWebsite&DateString&RefererPermalink").
                execute(new Object[] {website, dateString, permalink});
    }

    protected List getMatchingReferers(WebsiteData website, String requestUrl,
            String refererUrl) throws RollerException {

        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByWebsite&RequestUrl&RefererUrl").
                execute(new Object[] {website, requestUrl, refererUrl});
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
            throws RollerException {
        
        // TODO: ATLAS getDaysPopularWebsites DONE TESTED
        String msg = "Getting hot weblogs";
        List results = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
             
        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        DatamapperQuery query = strategy.newQuery(RefererData.class, 
            "RefererData.getHotWeblogsByWebsite.enabled&Website.active&Website.lastModifiedGreater");
        
        if (offset != 0 || length != -1) {
            query.setRange(offset, length);
        }
        
        List queryResults = (List) query.execute(
            new Object[] {Boolean.TRUE, Boolean.TRUE, startDate}); 
        for (Iterator it = queryResults.iterator(); it.hasNext(); ) {
            Object[] row = (Object[])it.next();
            Integer hits = (Integer)row[0];
            String websiteId = (String)row[1];
            String websiteName = (String)row[2];
            String websiteHandle = (String)row[3];
            results.add(new StatCount(
                websiteId,
                websiteHandle,
                websiteName,
                "statCount.weblogDayHits",
                hits.longValue()));              
        }
        //TODO Uncomment following once integrated with code
        //Collections.sort(results, StatCount.getComparator());
        Collections.reverse(results);
        return results;
    }

    /**
     * Get most popular websites based on referer day hits.
     * @param offset Offset into results (for paging)
     * @param length Maximum number of results to return (for paging)
     * @return List of WebsiteDisplayData objects.
     */
    public List getDaysPopularWebsites(int offset, int length)
            throws RollerException {

        // TODO: ATLAS getDaysPopularWebsites DONE TESTED
        String msg = "Getting popular websites";
        List results = new ArrayList();
        
        if (length == -1) {
            length = Integer.MAX_VALUE - offset;
        }

        DatamapperQuery query = strategy.newQuery(RefererData.class, 
            "RefererData.getDaysPopularWebsitesByWebsite.enabled&Website.active");
        
        if (offset != 0 || length != -1) {
            query.setRange(offset, length);
        }

        List queryResults = (List) query.execute(
            new Object[] {Boolean.TRUE, Boolean.TRUE}); 
        
        for (Iterator it = queryResults.iterator(); it.hasNext(); ) {
            Object[] row = (Object[])it.next();
            Integer hits = (Integer)row[0];
            String websiteId = (String)row[1];
            String websiteName = (String)row[2];
            String websiteHandle = (String)row[3];
            results.add(new WebsiteDisplayData(
                websiteId,
                websiteName,
                websiteHandle,
                hits));              
        }
        //TODO Uncomment following once integrated with code
        //Collections.sort(results, StatCount.getComparator());
        Collections.reverse(results);
        return results;
    }

    protected int getHits(WebsiteData website, String type) 
            throws RollerException {
        int hits = -1;
        if (log.isDebugEnabled()) {
            log.debug("getHits: " + website.getName());
        }
        
        List results = null;
            
        DatamapperQuery query = strategy.newQuery(RefererData.class, 
            "RefererData.getHitsByWebsite.enabled&Website.id");

        Object[] resultsArray = (Object[]) results.get(0);
        
        if (resultsArray.length > 0 && type.equals(DAYHITS)) {
            if ( resultsArray[0] != null ) {
                hits = ((Integer) resultsArray[0]).intValue();
            }
        } else if ( resultsArray.length > 0 ) {
            if ( resultsArray[0] != null ) {
                hits = ((Integer) resultsArray[1]).intValue();
            }
        } else {
            hits = 0;
        }
        
        return hits;
    }

    /**
     * Get all referers for specified weblog.
     * @param weblog
     * @return List of type RefererData
     */
    public List getReferers(WebsiteData weblog) throws RollerException {
        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByWebsiteOrderByTotalHitsDesc").execute(weblog);
    }

    /**
     * Get all referers for specified user that were made today.
     * @param website Web site.
     * @return List of type RefererData
     */
    public List getTodaysReferers(WebsiteData website) throws RollerException {
        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByWebsite&DayHitsGreaterZeroOrderByDayHitsDesc")
                .execute(website);
    }

    /**
     * Get referers for a specified date.
     * @param website Web site.
     * @param date YYYYMMDD format of day's date.
     * @return List of type RefererData.
     * @throws org.apache.roller.RollerException
     */
    public List getReferersToDate(WebsiteData website, String date)
            throws RollerException {

        if (website==null )
            throw new RollerException("website is null");
        
        if (date==null )
            throw new RollerException("Date is null");
        
        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByWebsite&DateString&DuplicateOrderByTotalHitsDesc").
                execute(new Object[] {website, date, Boolean.FALSE});
    }

    /**
     * Get referers that refer to a specific weblog entry.
     * @param entryid Weblog entry ID
     * @return List of RefererData objects.
     * @throws org.apache.roller.RollerException
     */
    public List getReferersToEntry(String entryid) throws RollerException {
        if (null == entryid)
            throw new RollerException("entryid is null");
        
        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByEntryId&TitleNotNull&ExcerptNotNullOrderByTotalHitsDesc").
                execute(entryid);
    }

    /**
     * Query for collection of referers.
     */
    protected List getReferersToWebsite(WebsiteData website, String refererUrl)
            throws RollerException {
        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByWebsite&RefererURL").
                execute(new Object[] {website, refererUrl});
    }

    /**
     * Query for collection of referers.
     */
    protected List getReferersWithSameTitle(WebsiteData website,
                                            String requestUrl,
                                            String title,
                                            String excerpt)
            throws RollerException {
        return (List) strategy.newQuery(RefererData.class, 
                "RefererData.getByWebsite&RequestURL&TitleOrExcerpt").
                execute(new Object[] {website, requestUrl, title, excerpt});
    }

    /**
     * Get user's day hits
     */
    public int getDayHits(WebsiteData website) throws RollerException {
        return getHits(website, DAYHITS);
    }

    /**
     * Get user's all-time total hits
     */
    public int getTotalHits(WebsiteData website) throws RollerException {
        return getHits(website, TOTALHITS);
    }

    /**
     * Retrieve referer by id.
     */
    public RefererData getReferer(String id) throws RollerException {
        return (RefererData)strategy.load(RefererData.class, id);
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
        WebsiteData weblog = null;
        WeblogEntryData entry = null;

        // lookup the weblog now
        try {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            weblog = userMgr.getWebsiteByHandle(weblogHandle);
            if (weblog == null) return;

            // now lookup weblog entry if possible
            if (entryAnchor != null) {
                WeblogManager weblogMgr = RollerFactory.getRoller().
                    getWeblogManager();
                entry = weblogMgr.getWeblogEntryByAnchor(weblog, entryAnchor);
            }
        } catch (RollerException re) {
            // problem looking up website, gotta bail
            log.error("Error looking up website object", re);
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

                    matchRef = getMatchingReferers(weblog, requestUrl, 
                        secondTryUrl);
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

                log.debug("Incrementing hit count on existing referer: " +
                    referrerUrl);

                saveReferer(ref);

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

                if (log.isDebugEnabled()) {
                    log.debug("newReferer="+ref.getRefererUrl());
                }

                String refurl = ref.getRefererUrl();

                // If not a direct or search engine then search for linkback
                boolean doLinkbackExtraction =
                    RollerRuntimeConfig.getBooleanProperty(
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
                        Roller mRoller = RollerFactory.getRoller();
                        mRoller.getThreadManager().executeInBackground(
                                new LinkbackExtractorRunnable(ref));
                    } catch (InterruptedException e) {
                        log.warn("Interrupted during linkback extraction",e);
                    }
                } else {
                    saveReferer(ref);
                }
            }
        } catch (RollerException pe) {
            log.error(pe);
        } catch (NullPointerException npe) {
            log.error(npe);
        }
    }

    /**
     * Use LinkbackExtractor to parse title and excerpt from referer
     */
    class LinkbackExtractorRunnable implements Runnable {

        private RefererData mReferer = null;

        public LinkbackExtractorRunnable( RefererData referer) {
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
                        RefererData chosen = null;
                        int maxweight = 0;
                        for (Iterator rdItr = refs.iterator();rdItr.hasNext();) {
                            RefererData referer = (RefererData) rdItr.next();

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
                            RefererData referer = (RefererData) rdItr.next();

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
}
