/*
 * Created on Feb 23, 2003
 */
package org.roller.business.hibernate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
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
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.RefererManagerImpl;
import org.roller.config.RollerRuntimeConfig;
import org.roller.model.RefererManager;
import org.roller.pojos.RefererData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.WebsiteDisplayData;


/**
 * Hibernate queries.
 * @author David M Johnson
 */
public class HibernateRefererManagerImpl extends RefererManagerImpl
        implements RefererManager {
    static final long serialVersionUID = -4966091850482256435L;
    
    private static Log mLogger =
            LogFactory.getFactory().getInstance(HibernateRefererManagerImpl.class);
    
    //-------------------------------------------------- Startup and Shutdown
    public HibernateRefererManagerImpl(PersistenceStrategy support)
    throws RollerException {
        super();
        mStrategy = (HibernateStrategy)support;
        mLogger.debug("Instantiating Referer Manager");
    }
    
    //-----------------------------------------------------------------------
    /**
     * Apply ignoreWord/spam filters to all referers in system.
     */
    public void applyRefererFilters() throws RollerException {
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            
            String spamwords = RollerRuntimeConfig.getProperty("spam.blacklist");
            
            String[] blacklist = StringUtils.split(
                    StringUtils.deleteWhitespace(spamwords),",");
            Junction or = Expression.disjunction();
            for (int i=0; i<blacklist.length; i++) {
                String ignoreWord = blacklist[i].trim();
                or.add(Expression.ilike("refererUrl","%"+ignoreWord+"%"));
            }
            criteria.add(Expression.conjunction()
            .add(Expression.isNull("excerpt"))
            .add(or)
            );
            
            Iterator referers = criteria.list().iterator();
            while (referers.hasNext()) {
                removeReferer( ((RefererData)referers.next()).getId() );
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    /**
     * Apply ignoreWord/spam filters to all referers in website.
     */
    public void applyRefererFilters(WebsiteData website) throws RollerException {
        if (null == website) throw new RollerException("website is null");
        if (null == website.getBlacklist()) return;
        
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
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
            .add(Expression.isNull("excerpt"))
            .add(Expression.eq("website",website))
            .add(or)
            );
            
            Iterator referers = criteria.list().iterator();
            while (referers.hasNext()) {
                removeReferer( ((RefererData)referers.next()).getId() );
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getExistingReferers(WebsiteData website, String dateString,
            String permalink) throws RollerException {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.conjunction()
        .add(Expression.eq("website",website))
        .add(Expression.eq("dateString",dateString))
        .add(Expression.eq("refererPermalink",permalink)));
        try {
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getMatchingReferers(WebsiteData website, String requestUrl,
            String refererUrl) throws RollerException {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.conjunction()
        .add(Expression.eq("website",website))
        .add(Expression.eq("requestUrl",requestUrl))
        .add(Expression.eq("refererUrl",refererUrl)));
        try {
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Use raw SQL because Hibernate can't handle sorting by sum.
     */
    public List getDaysPopularWebsites(int max) throws RollerException {
        // TODO Hibernate version of getDaysPopularWebsites
        // TODO Move to full use of mSupport
        String msg = "Getting popular websites";
        Session ses = null; // the session will eventually be release by RequestFilter
        Connection con = null;
        try {
            List list = new ArrayList();
            
            ses = ((HibernateStrategy)mStrategy).getSession();
            con = ses.connection();
            
            final PreparedStatement stmt;
            if (con.getMetaData().getDriverName().startsWith("HSQL")) {
                // special handling for HSQLDB
                stmt = con.prepareStatement(
                        "select top ? w.id,w.name,w.handle,sum(r.dayhits) as s "+
                        "from website as w, referer as r "+
                        "where r.websiteid=w.id and w.isenabled=? " +
                        "group by w.name,w.handle,w.id order by s desc");
                stmt.setInt(1, max);
                stmt.setBoolean(2, true);
            } else if(con.getMetaData().getDriverName().startsWith("Apache Derby")) {
	           // special handling for Derby
				stmt = con.prepareStatement(
				    "select w.name, w.handle, w.id, sum(r.dayhits) as s "+
				    "from website as w, referer as r "+
				    "where r.websiteid=w.id and w.isenabled=? " +
				    "group by w.name, w.handle, w.id order by s desc");                      
				stmt.setBoolean(1, true);
                stmt.setMaxRows(max);
            } else if(con.getMetaData().getDriverName().startsWith("IBM DB2")) {
                // special handling for IBM DB2
                stmt = con.prepareStatement(
                        "select u.username,w.name,w.name,sum(r.dayhits) as s "+
                        "from rolleruser as u, website as w, referer as r "+
                        "where r.websiteid=w.id and w.userid=u.id and w.isenabled= ? " +
                        "group by u.username,w.handle,w.id order by s desc fetch first " +
                        Integer.toString(max) + " rows only");
                stmt.setBoolean(1, true);
            } else if (con.getMetaData().getDriverName().startsWith("Oracle")) {
				String sql = "select u.username,w.name,w.handle,sum(r.dayhits) as s "+
                "from rolleruser u, website w, referer r "+
                "where r.websiteid=w.id and w.userid=u.id and w.isenabled= ? and rownum <= ? " +
                "group by u.username,w.name,w.handle order by s desc";
				stmt = con.prepareStatement(sql);
				stmt.setBoolean(1, true);
				stmt.setInt(2, max );				
            } else {
                stmt = con.prepareStatement(
                        "select w.id,w.name,w.handle,sum(r.dayhits) as s "+
                        "from website as w, referer as r "+
                        "where r.websiteid=w.id and w.isenabled= ? " +
                        // Ben Walding (a Postgres SQL user): Basically, you have
                        // to have all non-aggregated columns that exist in your
                        // 'SELECT' section, in the 'GROUP BY' section as well:
                        "group by w.name,w.handle,w.id order by s desc limit ?");
                // and not this: "group by w.id order by s desc");
                stmt.setBoolean(1, true);
                stmt.setInt(2, max);
            }
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() ) {
                do
                {
                    String websiteId = rs.getString(1);
                    String websiteName = rs.getString(2);
                    String websiteHandle = rs.getString(3);
                    Integer hits = new Integer(rs.getInt(4));
                    list.add(new WebsiteDisplayData(
                            websiteId,
                            websiteName,
                            websiteHandle,
                            hits));
                    if(list.size() >= max) {
                    	rs.close();
                    	break;
                    }
                }
                while ( rs.next() );
            }
            return list;
        } catch (Throwable pe) {
            mLogger.error(msg, pe);
            throw new RollerException(msg, pe);
        }        
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Use raw SQL because Hibernate can't handle the query.
     */
    protected int getHits(WebsiteData website, String type)
    throws RollerException {
        int hits = 0;
        if (mLogger.isDebugEnabled()) {
            mLogger.debug("getHits: " + website.getName());
        }
        
        Object[] args = { Boolean.TRUE, website.getId() };
        Type[] types = { Hibernate.BOOLEAN, Hibernate.STRING };
        
        // For a query like this, Hibernate returns a list of lists
        Session session = ((HibernateStrategy)mStrategy).getSession();
        List results;
        try {
            Query q = session.createQuery(
                    "select sum(h.dayHits),sum(h.totalHits) from h in class " +
                    "org.roller.pojos.RefererData " +
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
     * @see org.roller.pojos.RefererManager#getReferers(java.lang.String)
     */
    public List getReferers(WebsiteData website) throws RollerException {
        if (website==null )
            throw new RollerException("website is null");
        
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.eq("website",website));
        criteria.addOrder(Order.desc("totalHits"));
        try {
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
        
    //-----------------------------------------------------------------------
    
    /**
     * @see org.roller.pojos.RefererManager#getTodaysReferers(String)
     */
    public List getTodaysReferers(WebsiteData website)
    throws RollerException {
        if (website==null )
            throw new RollerException("website is null");
        
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.gt("dayHits", new Integer(0)));
        criteria.addOrder(Order.desc("dayHits"));
        try {
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Returns referers for a specified day. Duplicate enties are not
     * included in this list so the hit counts may not be accurate.
     * @see org.roller.pojos.RefererManager#getReferersToDate(
     * org.roller.pojos.WebsiteData, java.lang.String)
     */
    public List getReferersToDate(WebsiteData website, String date)
    throws RollerException {
        if (website==null )
            throw new RollerException("website is null");
        
        if (date==null )
            throw new RollerException("Date is null");
        
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.eq("dateString", date));
        criteria.add(Expression.eq("duplicate", Boolean.FALSE));
        criteria.addOrder(Order.desc("totalHits"));
        try {
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * @see org.roller.pojos.RefererManager#getReferersToEntry(
     * java.lang.String, java.lang.String)
     */
    public List getReferersToEntry(String entryid) throws RollerException {
        if (null == entryid)
            throw new RollerException("entryid is null");
        
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
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
    
    //-----------------------------------------------------------------------
    
    /**
     * @see org.roller.pojos.RefererManager#getReferersToEntry(
     * java.lang.String, java.lang.String)
     */
    public void removeReferersForEntry(String entryid) throws RollerException {
        if (null == entryid)
            throw new RollerException("entryid is null");
        
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.createAlias("weblogEntry","e");
            criteria.add(Expression.eq("e.id", entryid));
            
            Iterator referers = criteria.list().iterator();
            while (referers.hasNext()) {
                removeReferer( ((RefererData)referers.next()).getId() );
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Query for collection of referers.
     */
    protected List getReferersToWebsite(WebsiteData website, String refererUrl)
    throws RollerException {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.eq("refererUrl", refererUrl));
        try {
            return criteria.list();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------
    
    /**
     * Query for collection of referers.
     */
    protected List getReferersWithSameTitle(
            WebsiteData website,
            String requestUrl,
            String title,
            String excerpt)
            throws RollerException {
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
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
    
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers() throws RollerException {

        if (mLogger.isDebugEnabled()) {
            mLogger.debug("clearReferrers");
        }       
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            String reset = "update RefererData set dayHits=0";
            session.createQuery(reset).executeUpdate();
            String delete = "delete RefererData where excerpt is null or excerpt=''";
            session.createQuery(delete).executeUpdate();
        } catch (Exception e) {
            mLogger.error("EXCEPTION resetting referers",e);
        }
    }  
    
    /**
     * Clear referrer dayhits and remove referrers without excerpts.
     */
    public void clearReferrers(WebsiteData website) throws RollerException {

        if (mLogger.isDebugEnabled()) {
            mLogger.debug("clearReferrers");
        }       
        try {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            String reset = "update RefererData set dayHits=0 where website=:site";
            session.createQuery(reset)
                .setParameter("site",website).executeUpdate();
            String delete = "delete RefererData where website=:site and (excerpt is null or excerpt='')";
            session.createQuery(delete)
                .setParameter("site",website).executeUpdate();
        } catch (Exception e) {
            mLogger.error("EXCEPTION resetting referers",e);
        }
    }  
}



