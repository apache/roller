
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
package org.apache.roller.business.datamapper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.pings.AutoPingManager;
import org.apache.roller.business.pings.PingQueueManager;
import org.apache.roller.config.PingConfig;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/*
 * DatamapperAutoPingManagerImpl.java
 *
 * Created on May 29, 2006, 11:29 AM
 *
 */
public class DatamapperAutoPingManagerImpl implements AutoPingManager {

    private DatamapperPersistenceStrategy strategy;
    
    /**
     * The logger instance for this class.
     */
    private static Log logger = LogFactory
            .getFactory().getInstance(DatamapperAutoPingManagerImpl.class);

    /** Creates a new instance of DatamapperAutoPingManagerImpl */
    public DatamapperAutoPingManagerImpl
            (DatamapperPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public AutoPingData getAutoPing(String id) throws RollerException {
        return (AutoPingData)strategy.load(AutoPingData.class, id);
    }

    public void saveAutoPing(AutoPingData autoPing) throws RollerException {
        strategy.store(autoPing);
    }

    public void removeAutoPing(AutoPingData autoPing) throws RollerException {
        strategy.remove(autoPing);
    }

    public void removeAutoPing(PingTargetData pingTarget, WebsiteData website)
            throws RollerException {
        strategy.newRemoveQuery(AutoPingData.class, "AutoPingData.removeByPingTarget&Website")
                .removeAll(new Object[]{pingTarget, website});
    }

    public void removeAutoPings(Collection autopings) 
            throws RollerException {
        strategy.removeAll(autopings);
    }

    public void removeAllAutoPings() 
            throws RollerException {
        strategy.removeAll(AutoPingData.class);
    }

    public void queueApplicableAutoPings(WeblogEntryData changedWeblogEntry)
            throws RollerException {
        if (PingConfig.getSuspendPingProcessing()) {
            if (logger.isDebugEnabled())
                logger.debug("Ping processing is suspended.  No auto pings will be queued.");
            return;
        }

        PingQueueManager pingQueueMgr = RollerFactory.getRoller().getPingQueueManager();
        List applicableAutopings = getApplicableAutoPings(changedWeblogEntry);
        for (Iterator i = applicableAutopings.iterator(); i.hasNext(); ) {
            AutoPingData autoPing = (AutoPingData) i.next();
            pingQueueMgr.addQueueEntry(autoPing);
        }
    }

    public List getAutoPingsByWebsite(WebsiteData website)
            throws RollerException {
        return (List)strategy.newQuery(AutoPingData.class, "AutoPingData.getByWebsite")
            .execute(website);
    }

    public List getAutoPingsByTarget(PingTargetData pingTarget) 
            throws RollerException {
        return (List)strategy.newQuery(AutoPingData.class, "AutoPingData.getByPingTarget")
            .execute(pingTarget);
    }

    public List getApplicableAutoPings(WeblogEntryData changedWeblogEntry) 
            throws RollerException {
        return getAutoPingsByWebsite(changedWeblogEntry.getWebsite());
        //        return (List)strategy.newQuery(AutoPingData.class, "AutoPingData.getByWebsite")
        //            .execute(changedWeblogEntry.getWebsite());
    }

    public List getCategoryRestrictions(AutoPingData autoPing)
            throws RollerException {
        return Collections.EMPTY_LIST;
    }

    public void setCategoryRestrictions
            (AutoPingData autoPing, Collection newCategories) {
        // NOT YET IMPLEMENTED
    }

    public void release() {}
    
}
