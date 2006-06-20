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
/*
 * Created on Feb 23, 2003
 */
package org.apache.roller.business.hibernate;

import java.text.SimpleDateFormat;
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
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.RefererManager;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.pojos.WebsiteDisplayData;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.dialect.Dialect;
import org.apache.roller.model.Roller;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.pojos.StatCount;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.LinkbackExtractor;
import org.apache.roller.util.Utilities;


/**
 * Hibernate implementation of the RefererManager.
 */
public class HibernateRefererManagerImpl implements RefererManager {
    
    static final long serialVersionUID = -4966091850482256435L;
    
    private static Log log = LogFactory.getLog(HibernateRefererManagerImpl.class);
    
    protected static final String DAYHITS = "dayHits";
    protected static final String TOTALHITS = "totalHits";
    
    private HibernatePersistenceStrategy strategy = null;
    private Date mRefDate = new Date();
    private SimpleDateFormat mDateFormat = DateUtil.get8charDateFormat();
        
    public HibernateRefererManagerImpl(HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate Referer Manager");
        
        strategy = strat;
    }
    
    public void saveReferer(RefererData referer) throws RollerException {
        strategy.store(referer);
    }
        
    public void removeReferer(RefererData referer) throws RollerException {
        strategy.remove(referer);
    }
        
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     *
     * TODO: do we really need dialect specific queries?
     */
    public void clearReferrers() throws RollerException {
        
        if (log.isDebugEnabled()) {
            log.debug("clearReferrers");
        }
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Dialect currentDialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
            String reset = "update RefererData set dayHits=0";
            session.createQuery(reset).executeUpdate();
            String delete = null;
            if ( currentDialect instanceof SQLServerDialect || currentDialect instanceof OracleDialect ){
                delete = "delete RefererData where excerpt is null or excerpt like ''";
            } else {
                delete = "delete RefererData where excerpt is null or excerpt=''";
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
    public void clearReferrers(WebsiteData website) throws RollerException {
        
        if (log.isDebugEnabled()) {
            log.debug("clearReferrers");
        }
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Dialect currentDialect = ((SessionFactoryImplementor)session.getSessionFactory()).getDialect();
            String reset = "update RefererData set dayHits=0 where website=:site";
            session.createQuery(reset)
            .setParameter("site",website).executeUpdate();
            String delete = null;
            if ( currentDialect instanceof SQLServerDialect || currentDialect instanceof OracleDialect ){
                delete = "delete RefererData where website=:site and (excerpt is null or excerpt like '')";
            } else {
                delete = "delete RefererData where website=:site and (excerpt is null or excerpt='')";
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
    public void applyRefererFilters() throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            
            String spamwords = RollerRuntimeConfig.getProperty("spam.blacklist");
            
            String[] blacklist = StringUtils.split(
                    StringUtils.deleteWhitespace(spamwords),",");
            Junction or = Expression.disjunction();
            for (int i=0; i<blacklist.length; i++) {
                String ignoreWord = blacklist[i].trim();
                //log.debug("including ignore word - "+ignoreWord);
                or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
            }
            criteria.add(Expression.conjunction()
            .add(Expression.disjunction().add(Expression.isNull("excerpt")).add(Expression.eq("excerpt", "")))
            .add(or)
            );
            
            log.debug("removing spam referers - "+criteria.list().size());
            
            Iterator referer = criteria.list().iterator();
            while (referer.hasNext()) {
                this.strategy.remove((RefererData) referer.next());
            }

        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(WebsiteData website) throws RollerException {
        
        if (null == website) throw new RollerException("website is null");
        if (null == website.getBlacklist()) return;
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            
            String[] blacklist = StringUtils.split(
                    StringUtils.deleteWhitespace(website.getBlacklist()),",");
            if (blacklist.length == 0) return;
            
            Junction or = Expression.disjunction();
            for (int i=0; i<blacklist.length; i++) {
                String ignoreWord = blacklist[i].trim();
                or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
            }
            criteria.add(Expression.conjunction()
            .add(Expression.disjunction().add(Expression.isNull("excerpt")).add(Expression.eq("excerpt", "")))
            .add(Expression.eq("website",website))
            .add(or)
            );
            
            Iterator referer = criteria.list().iterator();
            while (referer.hasNext()) {
                this.strategy.remove((RefererData) referer.next());
            }
            
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }    
    
    /**
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getExistingReferers(WebsiteData website, String dateString,
            String permalink) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.add(Expression.conjunction()
            .add(Expression.eq("website",website))
            .add(Expression.eq("dateString",dateString))
            .add(Expression.eq("refererPermalink",permalink)));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    /**
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getMatchingReferers(WebsiteData website, String requestUrl,
            String refererUrl) throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.add(Expression.conjunction()
            .add(Expression.eq("website",website))
            .add(Expression.eq("requestUrl",requestUrl))
            .add(Expression.eq("refererUrl",refererUrl)));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
       
    /**
     * Returns hot weblogs as StatCount objects, in descending order by today's hits.
     */
    public List getHotWeblogs(int sinceDays, int offset, int length)
        throws RollerException {
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
               +"from WebsiteData w, RefererData r "
               +"where r.website=w and w.enabled=true and w.active=true and w.lastModified > :startDate"
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
                Integer hits = (Integer)row[0];
                String websiteId = (String)row[1];
                String websiteName = (String)row[2];
                String websiteHandle = (String)row[3];
                result.add(new StatCount(
                    websiteId,
                    websiteHandle,
                    websiteName,
                    "statCount.weblogDayHits",
                    hits.longValue()));              
            }
            return result;
            
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new RollerException(msg, pe);
        }
    }
    
    /**
     * @deprecated Replaced by getHotWeblogs().
     */
    public List getDaysPopularWebsites(int offset, int length) 
        throws RollerException {
        // TODO: ATLAS getDaysPopularWebsites DONE TESTED
        String msg = "Getting popular websites";
        ArrayList result = new ArrayList();
        try {      
            Session session = 
                ((HibernatePersistenceStrategy)strategy).getSession();            
            Query query = session.createQuery(
                "select sum(r.dayHits) as s, w.id, w.name, w.handle  "
               +"from WebsiteData w, RefererData r "
               +"where r.website=w and w.enabled=true and w.active=true "
               +"group by w.name, w.handle, w.id order by col_0_0_ desc"); 
            
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
                Integer hits = (Integer)row[0];
                String websiteId = (String)row[1];
                String websiteName = (String)row[2];
                String websiteHandle = (String)row[3];
                result.add(new WebsiteDisplayData(
                    websiteId,
                    websiteName,
                    websiteHandle,
                    hits));              
            }
            return result;
            
        } catch (Throwable pe) {
            log.error(msg, pe);
            throw new RollerException(msg, pe);
        }
    }
        
    /**
     * Use raw SQL because Hibernate can't handle the query.
     */
    protected int getHits(WebsiteData website, String type) throws RollerException {
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
                    "org.apache.roller.pojos.RefererData " +
                    "where h.website.enabled=? and h.website.id=? ");
            q.setParameters(args, types);
            results = q.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
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
     * @see org.apache.roller.pojos.RefererManager#getReferers(java.lang.String)
     */
    public List getReferers(WebsiteData website) throws RollerException {
        if (website==null )
            throw new RollerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.add(Expression.eq("website",website));
            criteria.addOrder(Order.desc("totalHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    /**
     * @see org.apache.roller.pojos.RefererManager#getTodaysReferers(String)
     */
    public List getTodaysReferers(WebsiteData website) throws RollerException {
        if (website==null )
            throw new RollerException("website is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.gt("dayHits", new Integer(0)));
            criteria.addOrder(Order.desc("dayHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
       
    /**
     * Returns referers for a specified day. Duplicate enties are not
     * included in this list so the hit counts may not be accurate.
     * @see org.apache.roller.pojos.RefererManager#getReferersToDate(
     * org.apache.roller.pojos.WebsiteData, java.lang.String)
     */
    public List getReferersToDate(WebsiteData website, String date)
            throws RollerException {
        if (website==null )
            throw new RollerException("website is null");
        
        if (date==null )
            throw new RollerException("Date is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("dateString", date));
            criteria.add(Expression.eq("duplicate", Boolean.FALSE));
            criteria.addOrder(Order.desc("totalHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    /**
     * @see org.apache.roller.pojos.RefererManager#getReferersToEntry(
     * java.lang.String, java.lang.String)
     */
    public List getReferersToEntry(String entryid) throws RollerException {
        if (null == entryid)
            throw new RollerException("entryid is null");
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.createAlias("weblogEntry","e");
            
            criteria.add(Expression.eq("e.id", entryid));
            criteria.add(Expression.isNotNull("title"));
            criteria.add(Expression.isNotNull("excerpt"));
            
            criteria.addOrder(Order.desc("totalHits"));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    /**
     * Query for collection of referers.
     */
    protected List getReferersToWebsite(WebsiteData website, String refererUrl)
            throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.add(Expression.eq("website", website));
            criteria.add(Expression.eq("refererUrl", refererUrl));
            
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    /**
     * Query for collection of referers.
     */
    protected List getReferersWithSameTitle(WebsiteData website,
                                            String requestUrl,
                                            String title,
                                            String excerpt)
            throws RollerException {
        
        try {
            Session session = ((HibernatePersistenceStrategy)strategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            
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
            throw new RollerException(e);
        }
    }
        
    public int getDayHits(WebsiteData website) throws RollerException {
        return getHits(website, DAYHITS);
    }
        
    public int getTotalHits(WebsiteData website) throws RollerException {
        return getHits(website, TOTALHITS);
    }
        
    /**
     * @see org.apache.roller.pojos.RefererManager#retrieveReferer(java.lang.String)
     */
    public RefererData getReferer(String id) throws RollerException {
        return (RefererData)strategy.load(id,RefererData.class);
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
                
                log.debug("Incrementing hit count on existing referer: "+referrerUrl);
                
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
       
    public void release() {}    
}



