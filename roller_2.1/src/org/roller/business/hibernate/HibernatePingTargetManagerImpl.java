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
import org.hibernate.criterion.Order;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.PingTargetManagerImpl;
import org.roller.pojos.PingTargetData;
import org.roller.pojos.WebsiteData;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;

public class HibernatePingTargetManagerImpl extends PingTargetManagerImpl
{
    static final long serialVersionUID = 121008492583382718L;

    public HibernatePingTargetManagerImpl(PersistenceStrategy persistenceStrategy)
    {
        super(persistenceStrategy);
    }

    /**
     * @see org.roller.model.PingTargetManager#getCommonPingTargets()
     */
    public List getCommonPingTargets() throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(PingTargetData.class);
            criteria.add(Expression.isNull("website"));
            criteria.addOrder(Order.asc("name"));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }

    }

    /**
     * @see org.roller.model.PingTargetManager#getCustomPingTargets(org.roller.pojos.WebsiteData)
     */
    public List getCustomPingTargets(WebsiteData website) throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(PingTargetData.class);
            criteria.add(Expression.eq("website", website));
            criteria.addOrder(Order.asc("name"));
            return criteria.list();
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    /**
     * @see org.roller.model.PingTargetManager#removeCustomPingTargets(org.roller.pojos.WebsiteData) 
     */
    public void removeCustomPingTargets(WebsiteData website) throws RollerException
    {
        List customTargets = getCustomPingTargets(website);
        removeTargets(customTargets);
    }

    /**
     * @see org.roller.model.PingTargetManager#removeAllCustomPingTargets()
     */
    public void removeAllCustomPingTargets() throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy) persistenceStrategy).getSession();
            Criteria criteria = session.createCriteria(PingTargetData.class);
            criteria.add(Expression.isNotNull("website"));
            List allCustomTargets = criteria.list();
            removeTargets(allCustomTargets);
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

    // Private helper to remove a collection of targets.
    private void removeTargets(Collection customTargets)
        throws RollerException
    {
        for (Iterator i = customTargets.iterator(); i.hasNext();)
        {
            PingTargetData pt = (PingTargetData) i.next();
            pt.remove();
        }
    }

}
