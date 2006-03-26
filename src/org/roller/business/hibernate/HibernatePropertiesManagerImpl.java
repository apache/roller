/*
 * HibernatePropertiesManagerImpl.java
 *
 * Created on April 21, 2005, 10:40 AM
 */

package org.roller.business.hibernate;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.business.PropertiesManagerImpl;
import org.roller.pojos.RollerPropertyData;


/**
 * A hibernate specific implementation of the properties manager.
 *
 * @author Allen Gilliland
 */
public class HibernatePropertiesManagerImpl extends PropertiesManagerImpl {
    
    static final long serialVersionUID = -4326713177137796936L;
    
    private static Log mLogger =
            LogFactory.getLog(HibernatePropertiesManagerImpl.class);
    
    
    /** 
     * Creates a new instance of HibernatePropertiesManagerImpl
     */
    public HibernatePropertiesManagerImpl(PersistenceStrategy strategy) {
        super(strategy);
        mLogger.debug("Instantiating Hibernate Properties Manager");
    }
    
    
    /** 
     * Retrieve a single property by name
     */
    public RollerPropertyData getProperty(String name) throws RollerException {
        try {
            Session session = ((HibernateStrategy) mStrategy).getSession();
            return (RollerPropertyData) mStrategy.load(name, RollerPropertyData.class);
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    /** 
     * Retrieve all properties 
     */
    public Map getProperties() throws RollerException {
        
        HashMap props = new HashMap();
        
        try {
            Session session = ((HibernateStrategy) mStrategy).getSession();
            Criteria criteria = session.createCriteria(RollerPropertyData.class);
            List list = criteria.list();
            
            /* 
             * for convenience sake we are going to put the list of props
             * into a map for users to access it.  The value element of the
             * hash still needs to be the RollerPropertyData object so that
             * we can save the elements again after they have been updated
             */
            RollerPropertyData prop = null;
            Iterator it = list.iterator();
            while(it.hasNext()) {
                prop = (RollerPropertyData) it.next();
                props.put(prop.getName(), prop);
            }
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
        
        return props;
    }
    
}
