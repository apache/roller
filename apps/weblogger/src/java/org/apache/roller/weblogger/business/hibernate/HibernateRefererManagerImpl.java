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

package org.apache.roller.weblogger.business.hibernate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.Order;
import org.hibernate.type.Type;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.RollerRuntimeConfig;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.pojos.WeblogReferrer;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.dialect.Dialect;
import org.apache.roller.weblogger.business.Roller;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.StatCount;
import org.apache.roller.weblogger.util.LinkbackExtractor;
import org.apache.roller.weblogger.util.Utilities;
import org.hibernate.dialect.DerbyDialect;


/**
 * Hibernate implementation of the RefererManager.
 */
@com.google.inject.Singleton
public class HibernateRefererManagerImpl implements RefererManager {
    
    static final long serialVersionUID = -4966091850482256435L;
    
    private static Log log = LogFactory.getLog(HibernateRefererManagerImpl.class);
    
    protected static final String DAYHITS = "dayHits";
    protected static final String TOTALHITS = "totalHits";
    
    private final Roller roller;
    private final HibernatePersistenceStrategy strategy;

    private Date mRefDate = new Date();
    
        
    @com.google.inject.Inject    
    protected HibernateRefererManagerImpl(Roller roller, HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate Referer Manager");
        this.strategy = strat;
        this.roller = roller;
    }
    
    
    public void saveReferer(WeblogReferrer referer) throws WebloggerException {
        strategy.store(referer);
    }
        
    public void removeReferer(WeblogReferrer referer) throws WebloggerException {
        strategy.remove(referer);
    }
        
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     *
     * TODO: do we really need dialect specific queries?
     */
    public void clearReferrers() throws WebloggerException {
        
        if (log.isDebugEnabled()) {
            log.debug("clearReferrers");
        }
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Dialect currentDialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
            String reset = "update WeblogReferrer set dayHits=0";
            session.createQuery(reset).executeUpdate();
            String delete = null;
            // Some databases can't handle comparing CLOBs, use like as a workaround
            if (   currentDialect instanceof SQLServerDialect 
                || currentDialect instanceof OracleDialect 
                || currentDialect instanceof DerbyDialect) {
                delete = "delete WeblogReferrer where excerpt is null or excerpt like ''";
            } else {
                delete = "delete WeblogReferrer where excerpt is null or excerpt=''";
            }
            session.createQuery(delete).executeUpdate();
        } catch (Exception e) {
            log.error("EXCEPTION resetting referers",e);
        }
    }
        
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     *
     * TODO: do we really need dialect specific queries?
     */
    public void clearReferrers(Weblog website) throws WebloggerException {
        
        if (log.isDebugEnabled()) {
            log.debug("clearReferrers");
        }
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Dialect currentDialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
            String reset = "update WeblogReferrer set dayHits=0 where website=:site";
            session.createQuery(reset)
            .setParameter("site",website).executeUpdate();
            String delete = null;
            if ( currentDialect instanceof SQLServerDialect || currentDialect instanceof OracleDialect ){
                delete = "delete WeblogReferrer where website=:site and (excerpt is null or excerpt like '')";
            } else {
                delete = "delete WeblogReferrer where website=:site and (excerpt is null or excerpt='')";
            }
            session.createQuery(delete)
            .setParameter("site",website).executeUpdate();
        } catch (Exception e) {
            log.error("EXCEPTION resetting referers",e);
        }
    }
        
    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    public void applyRefererFilters() throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            
            String spamwords = RollerRuntimeConfig.getProperty("spam.blacklist");
            
            String[] blacklist = StringUtils.split(
                    StringUtils.deleteWhitespace(spamwords),",");
            Junction or = Expression.disjunction();
            for (int i=0; i<blacklist.length; i++) {
                String ignoreWord = blacklist[i].trim();
                //log.debug("including ignore word - "+ignoreWord);
                or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
            }
            criteria.add(or);
            
            log.debug("removing spam referers - "+criteria.list().size());
            
            Iterator referer = criteria.list().iterator();
            while (referer.hasNext()) {
                this.strategy.remove((WeblogReferrer) referer.next());
            }

        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(Weblog website) throws WebloggerException {
        
        if (null == website) throw new WebloggerException("website is null");
        if (null == website.getBlacklist()) return;
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            
            String[] blacklist = StringUtils.split(
                    StringUtils.deleteWhitespace(website.getBlacklist()),",");
            if (blacklist.length == 0) return;
            
            Junction or = Expression.disjunction();
            for (int i=0; i<blacklist.length; i++) {
                String ignoreWord = blacklist[i].trim();
                or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
            }
            criteria.add(Expression.eq("website",website)).add(or);
            
            Iterator referer = criteria.list().iterator();
            while (referer.hasNext()) {
                this.strategy.remove((WeblogReferrer) referer.next());
            }
            
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }   
    
    /**
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getExistingReferers(Weblog website, String dateString,
            String permalink) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            criteria.add(Expression.conjunction()
            .add(Expression.eq("website",website))
            .add(Expression.eq("dateString",dateString))
            .add(Expression.eq("refererPermalink",permalink)));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    /**
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getMatchingReferers(Weblog website, String requestUrl,
            String refererUrl) throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            criteria.add(Expression.conjunction()
            .add(Expression.eq("website",website))
            .add(Expression.eq("requestUrl",requestUrl))
            .add(Expression.eq("refererUrl",refererUrl)));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
       
    /**
     * Returns hot weblogs as StatCount objects, in descending order by today's hits.
     */
    public List getHotWeblogs(int sinceDays, int offset, int length)
        throws WebloggerException {
        // TODO: ATLAS getDaysPopularWebsites DONE TESTED
        String msg = "Getting hot weblogs";
        ArrayList result = new ArrayList();
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1 * sinceDays);
        Date startDate = cal.getTime();
        try {      
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();            
            Query query = session.createQuery(
                "select sum(r.dayHits) as s, w.id, w.name, w.handle  "
               +"from Weblog w, WeblogReferrer r "
               +"where r.website=w and w.enabled=true and w.active=true and w.lastModified > :startDate "
               +"group by w.name, w.handle, w.id order by col_0_0_ desc"); 
            query.setParameter("startDate", startDate);
            
              // +"group by w.name, w.handle, w.id order by s desc");
              // The above would be *much* better but "HQL parser does not   
              // resolve alias in ORDER BY clause" (See Hibernate issue HHH-892)
            
            if (offset != 0) {
                query.setFirstResult(offset);
            }
            if (length != -1) {
                query.setMaxResults(length);
            }
            Iterator rawResults = query.list().iterator();
            for (Iterator it = query.list().iterator(); it.hasNext();) {
                Object[] row = (Object[])it.next();
                Number hits =          (Number)row[0];
                String websiteId =     (String)row[1];
                String websiteName =   (String)row[2];
                String websiteHandle = (String)row[3];
                StatCount statCount = new StatCount(
                    websiteId,
                    websiteHandle,
                    websiteName,
                    "statCount.weblogDayHits",
                    hits.longValue());
                statCount.setWeblogHandle(websiteHandle);
                result.add(statCount);
            }
            return result;
            
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new WebloggerException(msg, pe);
        }
    }
    
        
    /**
     * Use raw SQL because Hibernate can't handle the query.
     */
    protected int getHits(Weblog website, String type) throws WebloggerException {
        int hits = 0;
        if (log.isDebugEnabled()) {
            log.debug("getHits: " + website.getName());
        }
        
        Object[] args = { Boolean.TRUE, website.getId() };
        Type[] types = { Hibernate.BOOLEAN, Hibernate.STRING };
        
        // For a query like this, Hibernate returns a list of lists
        Session session = ((HibernatePersistenceStrategy)strategy).getSession();
        List results;
        try {
            // begin transaction
            this.strategy.getSession().beginTransaction();
            
            Query q = session.createQuery(
                    "select sum(h.dayHits),sum(h.totalHits) from h in class " +
                    "org.apache.roller.weblogger.pojos.WeblogReferrer " +
                    "where h.website.enabled=? and h.website.id=? ");
            q.setParameters(args, types);
            results = q.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
        Object[] resultsArray = (Object[]) results.get(0);
        
        if (resultsArray.length > 0 && type.equals(DAYHITS)) {
            if ( resultsArray[0] != null ) {
                hits = ((Number) resultsArray[0]).intValue();
            }
        } else if ( resultsArray.length > 0 ) {
            if ( resultsArray[0] != null ) {
                hits = ((Number) resultsArray[1]).intValue();
            }
        } else {
            hits = 0;
        }
        
        return hits;
    }
        
    /**
     * @see org.apache.roller.weblogger.pojos.RefererManager#getReferers(java.lang.String)
     */
    public List getReferers(Weblog website) throws WebloggerException {
        if (website==null )
            throw new WebloggerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            criteria.add(Expression.eq("website",website));
            criteria.addOrder(Order.desc("totalHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    /**
     * @see org.apache.roller.weblogger.pojos.RefererManager#getTodaysReferers(String)
     */
    public List getTodaysReferers(Weblog website) throws WebloggerException {
        if (website==null )
            throw new WebloggerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.gt("dayHits", new Integer(0)));
            criteria.addOrder(Order.desc("dayHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
       
    /**
     * Returns referers for a specified day. Duplicate enties are not
     * included in this list so the hit counts may not be accurate.
     * @see org.apache.roller.weblogger.pojos.RefererManager#getReferersToDate(
     * org.apache.roller.weblogger.pojos.WebsiteData, java.lang.String)
     */
    public List getReferersToDate(Weblog website, String date)
            throws WebloggerException {
        if (website==null )
            throw new WebloggerException("website is null");
        
        if (date==null )
            throw new WebloggerException("Date is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("dateString", date));
            criteria.add(Expression.eq("duplicate", Boolean.FALSE));
            criteria.addOrder(Order.desc("totalHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    /**
     * @see org.apache.roller.weblogger.pojos.RefererManager#getReferersToEntry(
     * java.lang.String, java.lang.String)
     */
    public List getReferersToEntry(String entryid) throws WebloggerException {
        if (null == entryid)
            throw new WebloggerException("entryid is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            criteria.createAlias("weblogEntry","e");
            
            criteria.add(Expression.eq("e.id", entryid));
            criteria.add(Expression.isNotNull("title"));
            criteria.add(Expression.isNotNull("excerpt"));
            
            criteria.addOrder(Order.desc("totalHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    /**
     * Query for collection of referers.
     */
    protected List getReferersToWebsite(Weblog website, String refererUrl)
            throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("refererUrl", refererUrl));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    /**
     * Query for collection of referers.
     */
    protected List getReferersWithSameTitle(Weblog website,
                                            String requestUrl,
                                            String title,
                                            String excerpt)
            throws WebloggerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(WeblogReferrer.class);
            
            Junction conjunction = Expression.conjunction();
            conjunction.add(Expression.eq("website", website));
            conjunction.add(Expression.eq("requestUrl", requestUrl));
            
            Junction disjunction = Expression.conjunction();
            disjunction.add(Expression.eq("title", title));
            disjunction.add(Expression.eq("excerpt", excerpt));
            
            criteria.add(conjunction);
            criteria.add(disjunction);
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
        
    public int getDayHits(Weblog website) throws WebloggerException {
        return getHits(website, DAYHITS);
    }
        
    public int getTotalHits(Weblog website) throws WebloggerException {
        return getHits(website, TOTALHITS);
    }
        
    /**
     * @see org.apache.roller.weblogger.pojos.RefererManager#retrieveReferer(java.lang.String)
     */
    public WeblogReferrer getReferer(String id) throws WebloggerException {
        return (WeblogReferrer)strategy.load(id,WeblogReferrer.class);
    }
    
    
    public void processReferrer(String requestUrl,
                                String referrerUrl,
                                String weblogHandle,
                                String entryAnchor,
                                String dateString) {
        
        log.debug("processing referrer ["+referrerUrl+
                "] accessing ["+requestUrl+"]");
        
        if (weblogHandle == null)
            return;
        
        String selfSiteFragment = "/"+weblogHandle;
        Weblog weblog = null;
        WeblogEntry entry = null;
        
        // lookup the weblog now
        try {
            UserManager userMgr = roller.getUserManager();
            weblog = userMgr.getWebsiteByHandle(weblogHandle);
            if (weblog == null) return;
            
            // now lookup weblog entry if possible
            if (entryAnchor != null) {
                WeblogManager weblogMgr = roller.getWeblogManager();
                entry = weblogMgr.getWeblogEntryByAnchor(weblog, entryAnchor);
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
                    
                    matchRef = getMatchingReferers(weblog, requestUrl, secondTryUrl);
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
                
                log.debug("Incrementing hit count on existing referer: "+referrerUrl);
                
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
                        roller.getThreadManager().executeInBackground(
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
       
    public void release() {}    
}



