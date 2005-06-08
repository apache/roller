/*
 * Created on Feb 23, 2003
 */
package org.roller.business.hibernate;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.Hibernate;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;
import net.sf.hibernate.expression.Expression;
import net.sf.hibernate.expression.Junction;
import net.sf.hibernate.expression.Order;
import net.sf.hibernate.type.Type;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.RefererManagerImpl;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.pojos.RefererData;
import org.roller.pojos.WebsiteData;
import org.roller.pojos.WebsiteDisplayData;


/**
 * Hibernate queries.
 * @author David M Johnson
 */
public class HibernateRefererManagerImpl extends RefererManagerImpl
    implements RefererManager, Serializable
{
    private static Log mLogger =
         LogFactory.getFactory().getInstance(HibernateRefererManagerImpl.class);

    //-------------------------------------------------- Startup and Shutdown
    public HibernateRefererManagerImpl(Roller roller, PersistenceStrategy support)
        throws RollerException
	{
        super(roller);
		mStrategy = (HibernateStrategy)support;
	}

    //-----------------------------------------------------------------------

    /** 
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getExistingReferers(WebsiteData website, String dateString,
                    String permalink) throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.conjunction()
                        .add(Expression.eq("website",website))
                        .add(Expression.eq("dateString",dateString))
                        .add(Expression.eq("refererPermalink",permalink)));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------

    /** 
     * Use Hibernate directly because Roller's Query API does too much allocation.
     */
    protected List getMatchingReferers(WebsiteData website, String requestUrl,
                    String refererUrl) throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.conjunction()
                        .add(Expression.eq("website",website))
                        .add(Expression.eq("requestUrl",requestUrl))
                        .add(Expression.eq("refererUrl",refererUrl)));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    //-----------------------------------------------------------------------

    /**
     * Use raw SQL because Hibernate can't handle sorting by sum.
     */
    public List getDaysPopularWebsites(int max) throws RollerException
    {
        // TODO Hibernate version of getDaysPopularWebsites
        // TODO Move to full use of mSupport
        String msg = "Getting popular websites";
        Session ses = null; // the session will eventually be release by RequestFilter
        Connection con = null;
        try
        {
            List list = new ArrayList();

            ses = ((HibernateStrategy)mStrategy).getSession();
            con = ses.connection();

            final PreparedStatement stmt;
            if (con.getMetaData().getDriverName().startsWith("HSQL"))
            {
            		// special handling for HSQLDB
                stmt = con.prepareStatement(
                        "select top ? u.username,w.name,w.name,sum(r.dayhits) as s "+
                        "from rolleruser as u, website as w, referer as r "+
                        "where r.websiteid=w.id and w.userid=u.id and w.isenabled=? " +
                        "group by u.username,w.name,w.id order by s desc");
                stmt.setInt(1, max);
                stmt.setBoolean(2, true);
            }
            else 
            {
               stmt = con.prepareStatement(
                    "select u.username,w.name,w.name,sum(r.dayhits) as s "+
                    "from rolleruser as u, website as w, referer as r "+
                    "where r.websiteid=w.id and w.userid=u.id and w.isenabled= ? " +
                    // Ben Walding (a Postgres SQL user): Basically, you have
                    // to have all non-aggregated columns that exist in your
                    // 'SELECT' section, in the 'GROUP BY' section as well:
                    "group by u.username,w.name,w.id order by s desc limit ?");
                    // and not this: "group by w.id order by s desc");
                stmt.setBoolean(1, true);
                stmt.setInt(2, max);
            }
            ResultSet rs = stmt.executeQuery();
            if ( rs.next() )
            {
                do
                {
                    String userName = rs.getString(1);
                    String name = rs.getString(2);
                    String websiteName = rs.getString(3);
                    Integer hits = new Integer(rs.getInt(4));
                    list.add(new WebsiteDisplayData(
                       name, 
                       userName, 
                       websiteName, 
                       hits));
                }
                while ( rs.next() );
            }
            return list;
        }
        catch (Throwable pe)
        {
            mLogger.error(msg, pe);
            throw new RollerException(msg, pe);
        }
        
// Don't close connection, Hibernate is holding it
//        finally 
//        {
//            try 
//            {
//                if (con != null) con.close();
//            }
//            catch (Throwable t)
//            {
//                mLogger.error("Closing connection",t);
//            }
//        }
        
    }
    
    //-----------------------------------------------------------------------

    /**
     * Use raw SQL because Hibernate can't handle the query.
     */
    protected int getHits(WebsiteData website, String type)
        throws RollerException
    {
        int hits = 0;
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("getHits: " + website.getName());
        }

        //Question: why not use website.id instead to reduce joins?
        Object[] args = { Boolean.TRUE, website.getUser().getUserName() };
        Type[] types = { Hibernate.BOOLEAN, Hibernate.STRING };

        // For a query like this, Hibernate returns a list of lists
        Session session = ((HibernateStrategy)mStrategy).getSession();
        List results;
        try
        {
            results = session.find(
               "select sum(h.dayHits),sum(h.totalHits) from h in class " +
               "org.roller.pojos.RefererData " +
               "where h.website.isEnabled=? and h.website.user.userName=? ",
               args, types);
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
        Object[] resultsArray = (Object[]) results.get(0);

        if (resultsArray.length > 0 && type.equals(DAYHITS))
        {
            if ( resultsArray[0] != null )
            {
                hits = ((Integer) resultsArray[0]).intValue();
            }
        }
        else if ( resultsArray.length > 0 )
        {
            if ( resultsArray[0] != null )
            {
                hits = ((Integer) resultsArray[1]).intValue();
            }
        }
        else
        {
            hits = 0;
        }

        return hits;
    }
    
    /**
     * @see org.roller.pojos.RefererManager#getReferers(java.lang.String)
     */
    public List getReferers(WebsiteData website) throws RollerException
    {
        if (website==null )
            throw new RollerException("website is null");

        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.eq("website",website));
        criteria.addOrder(Order.desc("totalHits"));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    //-----------------------------------------------------------------------

    /**
     * @see org.roller.pojos.RefererManager#getTodaysReferers(String)
     */
    public List getTodaysReferers(WebsiteData website)
        throws RollerException
    {
        if (website==null )
            throw new RollerException("website is null");

        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.gt("dayHits", new Integer(0)));
        criteria.addOrder(Order.desc("dayHits"));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
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
        throws RollerException
    {
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
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    //-----------------------------------------------------------------------

    /**
     * @see org.roller.pojos.RefererManager#getReferersToEntry(
     * java.lang.String, java.lang.String)
     */
    public List getReferersToEntry(String entryid) throws RollerException
    {
        if (null == entryid)
            throw new RollerException("entryid is null");

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.createAlias("weblogEntry","e");
            
            criteria.add(Expression.eq("e.id", entryid));
            criteria.add(Expression.isNotNull("title"));
            criteria.add(Expression.isNotNull("excerpt"));
            
            criteria.addOrder(Order.desc("totalHits"));
        
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    //-----------------------------------------------------------------------

    /**
     * @see org.roller.pojos.RefererManager#getReferersToEntry(
     * java.lang.String, java.lang.String)
     */
    public void removeReferersForEntry(String entryid) throws RollerException
    {
        if (null == entryid)
            throw new RollerException("entryid is null");

        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(RefererData.class);
            criteria.createAlias("weblogEntry","e");            
            criteria.add(Expression.eq("e.id", entryid));
        
            Iterator referers = criteria.list().iterator();
            while (referers.hasNext())
            {
                removeReferer( ((RefererData)referers.next()).getId() );
            }
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    //-----------------------------------------------------------------------

    /**
     * Query for collection of referers.
     */
    protected List getReferersToWebsite(WebsiteData website, String refererUrl)
        throws RollerException
    {
        Session session = ((HibernateStrategy)mStrategy).getSession();
        Criteria criteria = session.createCriteria(RefererData.class);
        criteria.add(Expression.eq("website", website));
        criteria.add(Expression.eq("refererUrl", refererUrl));
        try
        {
            return criteria.list();
        }
        catch (HibernateException e)
        {
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
        throws RollerException
    {
        try
        {
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
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
    
    /**
     * Purge referers at midnight. Zero out all dayHits and remove all
     * referers that do not have excerpts.
     */
    public void checkForTurnover( boolean forceTurnover, String websiteId )
        throws RollerException
    {
        // Note, this method doesn't need to be synchronized anymore since
        // it's called from the timer task now, and will never be executed
        // by two threads simultaneously.
        if (mLogger.isDebugEnabled())
        {
            mLogger.debug("checkForTurnover");
        }

        Date now = new Date();

        if (forceTurnover ||
                !mDateFormat.format(now).equals(mDateFormat.format(mRefDate)))
        {
            try
            {
                if (websiteId == null) mRefDate = now;

                List refs;
                try
                {
                    Session session = ((HibernateStrategy)mStrategy).getSession();
                    Criteria criteria = session.createCriteria(RefererData.class);
                    criteria.add(Expression.gt("dayHits", new Integer(0)));
                    if (websiteId != null)
                    {
                        criteria.add(Expression.eq("website.id", websiteId));
                    }
                    refs = criteria.list();
                }
                catch (HibernateException e1)
                {
                    throw new RollerException(e1);
                }

                Integer zero = new Integer(0);
                for (Iterator rdItr = refs.iterator(); rdItr.hasNext();) {
                    RefererData referer = (RefererData) rdItr.next();

                    if (   (referer.getExcerpt() != null) &&
                            (referer.getExcerpt().trim().length() > 0))
                    {
                        // Zero out dayHits of referers with excerpts
                        referer.setDayHits(zero);
                        storeReferer(referer);
                    }
                    else
                    {
                        // Throw away referers without excerpts
                        removeReferer(referer.getId());
                    }
                }
            }
            catch (RollerException e)
            {
                mLogger.error("EXCEPTION resetting referers",e);
            }
        }
    }


}
