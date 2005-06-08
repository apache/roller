/*
 * Created on Mar 4, 2004
 */
package org.roller.model;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.persistence.MockPersistenceStrategy;
import org.roller.pojos.RollerConfig;

import java.util.Map;

/**
 * @author lance.lavandowska
 */
public class MockConfigManager implements ConfigManager
{
    private static Log mLogger =
        LogFactory.getFactory().getInstance(MockUserManager.class);
    
    private PersistenceStrategy mStrategy = null;

    /**
     * @param strategy
     * @param roller
     */
    public MockConfigManager(PersistenceStrategy strategy, MockRoller roller)
    {
        mStrategy = strategy;
    }

    /* 
     * @see org.roller.model.ConfigManager#release()
     */
    public void release()
    {
        // no-op
    }

    /* 
     * @see org.roller.model.ConfigManager#storeRollerConfig(org.roller.pojos.RollerConfig)
     */
    public void storeRollerConfig(RollerConfig data) throws RollerException
    {
        mStrategy.store(data);
    }

    /* 
     * @see org.roller.model.ConfigManager#getRollerConfig()
     */
    public RollerConfig getRollerConfig() throws RollerException
    {
        Map configMap = ((MockPersistenceStrategy)mStrategy).getObjectStore(RollerConfig.class);
        if (configMap.isEmpty())
        {
            RollerConfig config = new RollerConfig();
            storeRollerConfig(config);
            return config;
        }
        
        return (RollerConfig)configMap.values().iterator().next();
    }

    /* 
     * @see org.roller.model.ConfigManager#readFromFile(java.lang.String)
     */
    public RollerConfig readFromFile(String filePath) throws RollerException
    {
        return getRollerConfig();
    }

}
