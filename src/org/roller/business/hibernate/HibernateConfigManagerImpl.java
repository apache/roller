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
import org.roller.pojos.RollerConfigData;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author David M Johnson
 */
public class HibernateConfigManagerImpl extends ConfigManagerImpl
{
    static final long serialVersionUID = -3674252864091781177L;
    
    private static Log mLogger =
        LogFactory.getFactory().getInstance(HibernateConfigManagerImpl.class);
    
    /**
     * @param strategy
     * @param roller
     */
    public HibernateConfigManagerImpl(PersistenceStrategy strategy)
    {
        super(strategy);
        mLogger.debug("Instantiating Config Manager");
    }

    /**
     * Fetch all RollerConfigs and return the first one, if any.
     * Note: there should only be one!
     * @see org.roller.model.ConfigManager#getRollerConfig()
     */
    public RollerConfigData getRollerConfig() throws RollerException
    {
        mLogger.error("Someone is trying to use the old config!!\n"+
                "This configuration mechanism has been deprecated\n"+
                "You should see this message only once when you first upgrade\n"+
                "your installation to roller 1.2\n\n"+
                "If you continue to see this message please shoot us an email\n"+
                "at roller-development@lists.sourceforge.net with some output\n"+
                "from your log files.\n");
        try
        {
            Session session = ((HibernateStrategy)mStrategy).getSession();
            Criteria criteria = session.createCriteria(RollerConfigData.class);
            criteria.setMaxResults(1);
            List list = criteria.list();
            return list.size()!=0 ? (RollerConfigData)list.get(0) : null;
        }
        catch (HibernateException e)
        {
            throw new RollerException(e);
        }
    }

}
