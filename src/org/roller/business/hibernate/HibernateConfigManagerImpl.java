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
 * Created on Jun 18, 2004
 */
package org.roller.business.hibernate;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.roller.RollerException;
import org.roller.pojos.RollerConfigData;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.model.ConfigManager;


/**
 * The *OLD* Roller configuration mechanism.
 *
 * This has been replaced by the PropertiesManager and the roller.properties
 * file.
 */
public class HibernateConfigManagerImpl implements ConfigManager {
    
    static final long serialVersionUID = -3674252864091781177L;
    
    private static Log log = LogFactory.getLog(HibernateConfigManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    
    public HibernateConfigManagerImpl(HibernatePersistenceStrategy strategy) {
        log.debug("Instantiating Hibernate Config Manager");
        
        this.strategy = strategy;
    }
    
    
    /**
     * @see org.roller.model.ConfigManager#storeRollerConfig(org.roller.pojos.RollerConfig)
     */
    public void storeRollerConfig(RollerConfigData data) throws RollerException {
        // no longer supported
    }
    
    
    /**
     * This isn't part of the ConfigManager Interface, because really
     * we shouldn't ever delete the RollerConfig.  This is mostly here
     * to assist with unit testing.
     */
    public void removeRollerConfig(String id) throws RollerException {
        // no longer supported
    }
    
    
    /**
     * Fetch all RollerConfigs and return the first one, if any.
     * Note: there should only be one!
     * @see org.roller.model.ConfigManager#getRollerConfig()
     */
    public RollerConfigData getRollerConfig() throws RollerException {
        
        log.error("Someone is trying to use the old config!!\n"+
                "This configuration mechanism has been deprecated\n"+
                "You should see this message only once when you first upgrade\n"+
                "your installation to roller 1.2\n\n"+
                "If you continue to see this message please shoot us an email\n"+
                "at roller-dev@incubator.apache.org with some output\n"+
                "from your log files.\n");
        
        try {
            Session session = this.strategy.getSession();
            Criteria criteria = session.createCriteria(RollerConfigData.class);
            criteria.setMaxResults(1);
            return (RollerConfigData) criteria.uniqueResult();
        } catch (HibernateException e) {
            throw new RollerException(e);
        }
    }
    
    
    public RollerConfigData readFromFile(String file) {
        return null;
    }
    
    
    public void release() {}
    
}
