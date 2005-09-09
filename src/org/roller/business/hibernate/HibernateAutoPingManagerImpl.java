/*
 * Copyright (c) 2005
 * Anil R. Gangolli. All rights reserved.
 *
 * Distributed with the Roller Weblogger Project under the terms of the Roller Software
 * License
 */

package org.roller.business.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.AutoPingManagerImpl;
import org.roller.pojos.AutoPingData;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class HibernateAutoPingManagerImpl extends AutoPingManagerImpl
{
    static final long serialVersionUID = 5420615676256979199L;

    public HibernateAutoPingManagerImpl(PersistenceStrategy persistenceStrategy)
    {
        super(persistenceStrategy);
    }

    public void removeAutoPing(PingTargetData pingTarget, WebsiteData website) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("pingTarget", pingTarget));
            criteria.add(Expression.eq("website", website));
            List matches = criteria.list();
            // This should have at most one element, but we remove them all regardless.
            for (Iterator i = matches.iterator(); i.hasNext(); ) {
                ((AutoPingData) i.next()).remove();
            }
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getAutoPingsByWebsite(WebsiteData website) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("website", website));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getAutoPingsByTarget(PingTargetData pingTarget) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("pingTarget", pingTarget));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public void removeAllAutoPings() throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            List allAutoPings = criteria.list();
            removeAutoPings(allAutoPings);
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    public List getCategoryRestrictions(AutoPingData autoPing) throws RollerException
    {
        return Collections.EMPTY_LIST;
    }

    public void setCategoryRestrictions(AutoPingData autoPing, Collection newCategories)
    {
        // NOT YET IMPLEMENTED
        return;
    }

    public List getApplicableAutoPings(WeblogEntryData changedWeblogEntry) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(AutoPingData.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("website", changedWeblogEntry.getWebsite()));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }
}
