/*
 * Created on Jun 18, 2004
 */
package org.roller.business.hibernate;

import net.sf.hibernate.Criteria;
import net.sf.hibernate.HibernateException;
import net.sf.hibernate.Session;

import org.roller.RollerException;
import org.roller.business.ConfigManagerImpl;
import org.roller.business.PersistenceStrategy;
import org.roller.model.Roller;
import org.roller.pojos.RollerConfig;

import java.util.List;

/**
 * @author David M Johnson
 */
public class HibernateConfigManagerImpl extends ConfigManagerImpl
{
    /**
     * @param strategy
     * @param roller
     */
    public HibernateConfigManagerImpl(PersistenceStrategy strategy, Roller roller)
    {
        super(strategy, roller);
    }

    /**
     * Fetch all RollerConfigs and return the first one, if any.
     * Note: there should only be one!
     * @see org.roller.model.ConfigManager#getRollerConfig()
     */
    public RollerConfig getRollerConfig() throws RollerException
    {
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(RollerConfig.class);
            criteria.setMaxResults(1);
            List list = criteria.list();
            return list.size()!=0 ? (RollerConfig)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

}
