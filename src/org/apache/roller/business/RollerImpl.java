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
 * RollerImpl.java
 *
 * Created on April 29, 2005, 5:33 PM
 */
package org.apache.roller.business;

import java.sql.Connection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.referrers.ReferrerQueueManager;
import org.apache.roller.business.referrers.ReferrerQueueManagerImpl;
import org.apache.roller.business.utils.UpgradeDatabase;
import org.apache.roller.model.FileManager;
import org.apache.roller.model.IndexManager;
import org.apache.roller.model.PluginManager;
import org.apache.roller.model.Roller;
import org.apache.roller.model.ThemeManager;
import org.apache.roller.model.ThreadManager;


/**
 * The abstract version of the Roller implementation.
 * Here we put code that pertains to *all* implementations of the Roller
 * interface, regardless of their persistence strategy.
 *
 * @author Allen Gilliland
 */
public abstract class RollerImpl implements Roller {
    
    private static Log mLogger = LogFactory.getLog(RollerImpl.class);
    
    private FileManager fileManager = null;
    private IndexManager indexManager = null;
    private ThreadManager threadManager = null;
    private ThemeManager themeManager = null;
    private PluginManager pluginManager = null;
    
    
    public RollerImpl() {
        // nothing to do here yet
    }
    
    
    /**
     * @see org.apache.roller.model.Roller#getFileManager()
     */
    public FileManager getFileManager() throws RollerException {
        if (fileManager == null) {
            fileManager = new FileManagerImpl();
        }
        return fileManager;
    }
    
    
    /**
     * @see org.apache.roller.model.Roller#getThreadManager()
     */
    public ThreadManager getThreadManager() throws RollerException {
        if (threadManager == null) {
            threadManager = new ThreadManagerImpl();
        }
        return threadManager;
    }
    
    
    /**
     * @see org.apache.roller.model.Roller#getIndexManager()
     */
    public IndexManager getIndexManager() throws RollerException {
        if (indexManager == null) {
            indexManager = new IndexManagerImpl();
        }
        return indexManager;
    }
    
    
    /**
     * @see org.apache.roller.model.Roller#getThemeManager()
     */
    public ThemeManager getThemeManager() throws RollerException {
        if (themeManager == null) {
            themeManager = new ThemeManagerImpl();
        }
        return themeManager;
    }
    
    
    /**
     * @see org.apache.roller.business.referrers.ReferrerQueueManager
     */
    public ReferrerQueueManager getReferrerQueueManager() {
        return ReferrerQueueManagerImpl.getInstance();
    }
    
    
    /**
     * @see org.apache.roller.model.Roller#getPluginManager()
     */
    public PluginManager getPagePluginManager() throws RollerException {
        if (pluginManager == null) {
            pluginManager = new PluginManagerImpl();
        }
        return pluginManager;
    }
    
    
    /**
     * @see org.apache.roller.model.Roller#upgradeDatabase(java.sql.Connection)
     */
    public void upgradeDatabase(Connection con) throws RollerException {
        UpgradeDatabase.upgradeDatabase(con);
    }
    
    
    public void release() {
        try {
            if (fileManager != null) fileManager.release();
            if (threadManager != null) threadManager.release();
            if (pluginManager != null) pluginManager.release();
        } catch(Throwable e) {
            mLogger.error("Error calling Roller.release()", e);
        }
    }
    
    
    public void shutdown() {
        try {
            if (getReferrerQueueManager() != null) getReferrerQueueManager().shutdown();
            if (indexManager != null) indexManager.shutdown();
            if (threadManager != null) threadManager.shutdown();
        } catch(Throwable e) {
            mLogger.error("Error calling Roller.shutdown()", e);
        }
    }
    
}
