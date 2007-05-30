
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
package org.apache.roller.weblogger.business.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;
import org.apache.roller.weblogger.config.PingConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.persistence.Query;
import org.apache.roller.business.jpa.JPAPersistenceStrategy;

/*
 * JPAAutoPingManagerImpl.java
 *
 * Created on May 29, 2006, 11:29 AM
 *
 */
public class JPAAutoPingManagerImpl implements AutoPingManager {

    private JPAPersistenceStrategy strategy;
    
    /**
     * The logger instance for this class.
     */
    private static Log logger = LogFactory
            .getFactory().getInstance(JPAAutoPingManagerImpl.class);

    /**
     * Creates a new instance of JPAAutoPingManagerImpl
     */
    public JPAAutoPingManagerImpl
            (JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public AutoPing getAutoPing(String id) throws RollerException {
        return (AutoPing)strategy.load(AutoPing.class, id);
    }

    public void saveAutoPing(AutoPing autoPing) throws RollerException {
        strategy.store(autoPing);
    }

    public void removeAutoPing(AutoPing autoPing) throws RollerException {
        strategy.remove(autoPing);
    }

    public void removeAutoPing(PingTarget pingTarget, Weblog website)
            throws RollerException {
        Query q = strategy.getNamedUpdate("AutoPing.removeByPingTarget&Website");
        q.setParameter(1, pingTarget);
        q.setParameter(2, website);
        q.executeUpdate();
    }

    public void removeAutoPings(Collection autopings) 
            throws RollerException {
        strategy.removeAll(autopings);
    }

    public void removeAllAutoPings() 
            throws RollerException {
        Query q = strategy.getNamedUpdate("AutoPing.getAll");
        removeAutoPings(q.getResultList());
    }

    public void queueApplicableAutoPings(WeblogEntry changedWeblogEntry)
            throws RollerException {
        if (PingConfig.getSuspendPingProcessing()) {
            if (logger.isDebugEnabled())
                logger.debug("Ping processing is suspended." +
                    " No auto pings will be queued.");
            return;
        }

        PingQueueManager pingQueueMgr = RollerFactory.getRoller().
            getPingQueueManager();
        List applicableAutopings = getApplicableAutoPings(changedWeblogEntry);
        for (Iterator i = applicableAutopings.iterator(); i.hasNext(); ) {
            AutoPing autoPing = (AutoPing) i.next();
            pingQueueMgr.addQueueEntry(autoPing);
        }
    }

    public List getAutoPingsByWebsite(Weblog website)
            throws RollerException {
        Query q = strategy.getNamedQuery("AutoPing.getByWebsite");
        q.setParameter(1, website);
        return q.getResultList();
    }

    public List getAutoPingsByTarget(PingTarget pingTarget) 
            throws RollerException {
        Query q = strategy.getNamedQuery("AutoPing.getByPingTarget");
        q.setParameter(1, pingTarget);
        return q.getResultList();
    }

    public List getApplicableAutoPings(WeblogEntry changedWeblogEntry) 
            throws RollerException {
        return getAutoPingsByWebsite(changedWeblogEntry.getWebsite());
        //        return (List)strategy.newQuery(AutoPing.class, "AutoPing.getByWebsite")
        //            .execute(changedWeblogEntry.getWebsite());
    }

    public List getCategoryRestrictions(AutoPing autoPing)
            throws RollerException {
        return Collections.EMPTY_LIST;
    }

    public void setCategoryRestrictions
            (AutoPing autoPing, Collection newCategories) {
        // NOT YET IMPLEMENTED
    }

    public void release() {}
    
}
