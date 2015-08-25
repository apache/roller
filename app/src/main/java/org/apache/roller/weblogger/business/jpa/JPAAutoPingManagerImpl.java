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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.business.jpa;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.OutgoingPingQueue;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.WeblogUpdatePinger;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.xmlrpc.XmlRpcException;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

/*
 * JPAAutoPingManagerImpl.java
 *
 * Created on May 29, 2006, 11:29 AM
 *
 */
public class JPAAutoPingManagerImpl implements AutoPingManager {

    private final JPAPersistenceStrategy strategy;
    /**
     * The logger instance for this class.
     */
    private static Log logger = LogFactory.getFactory().getInstance(JPAAutoPingManagerImpl.class);

    /**
     * Creates a new instance of JPAAutoPingManagerImpl
     */
    protected JPAAutoPingManagerImpl(JPAPersistenceStrategy strategy) {
        this.strategy = strategy;
    }

    public AutoPing getAutoPing(String id) throws WebloggerException {
        return strategy.load(AutoPing.class, id);
    }

    public void saveAutoPing(AutoPing autoPing) throws WebloggerException {
        strategy.store(autoPing);
    }

    public void removeAutoPing(AutoPing autoPing) throws WebloggerException {
        strategy.remove(autoPing);
    }

    public void removeAutoPing(PingTarget pingTarget, Weblog website) throws WebloggerException {
        Query q = strategy.getNamedUpdate("AutoPing.removeByPingTarget&Weblog");
        q.setParameter(1, pingTarget);
        q.setParameter(2, website);
        q.executeUpdate();
    }

    public void removeAutoPings(Collection<AutoPing> autopings) throws WebloggerException {
        strategy.removeAll(autopings);
    }

    public void removeAllAutoPings() throws WebloggerException {
        TypedQuery<AutoPing> q = strategy.getNamedQueryCommitFirst("AutoPing.getAll", AutoPing.class);
        removeAutoPings(q.getResultList());
    }

    public void queueApplicableAutoPings(WeblogEntry changedWeblogEntry) throws WebloggerException {
        if (WebloggerRuntimeConfig.getBooleanProperty("pings.suspendPingProcessing")) {
            if (logger.isDebugEnabled()) {
                logger.debug("Ping processing is suspended." + " No auto pings will be queued.");
            }
            return;
        }

        List<AutoPing> applicableAutopings = getApplicableAutoPings(changedWeblogEntry);
        OutgoingPingQueue queue = OutgoingPingQueue.getInstance();

        for (AutoPing autoPing : applicableAutopings) {
            queue.addPing(autoPing);
        }
    }

    public List<AutoPing> getAutoPingsByWebsite(Weblog website) throws WebloggerException {
        TypedQuery<AutoPing> q = strategy.getNamedQuery("AutoPing.getByWeblog", AutoPing.class);
        q.setParameter(1, website);
        return q.getResultList();
    }

    public List<AutoPing> getAutoPingsByTarget(PingTarget pingTarget) throws WebloggerException {
        TypedQuery<AutoPing> q = strategy.getNamedQuery("AutoPing.getByPingTarget", AutoPing.class);
        q.setParameter(1, pingTarget);
        return q.getResultList();
    }

    public List<AutoPing> getApplicableAutoPings(WeblogEntry changedWeblogEntry) throws WebloggerException {
        return getAutoPingsByWebsite(changedWeblogEntry.getWeblog());
    }

    @Override
    public void sendPings() throws WebloggerException {
        logger.debug("ping task started");

        OutgoingPingQueue opq = OutgoingPingQueue.getInstance();

        List<AutoPing> pings = opq.getPings();

        // reset queue for next execution
        opq.clearPings();

        if (WebloggerRuntimeConfig.getBooleanProperty("pings.suspendPingProcessing")) {
            logger.info("Ping processing suspended on admin settings page, no pings are being generated.");
            return;
        }

        String absoluteContextUrl = WebloggerRuntimeConfig.getAbsoluteContextURL();
        if (absoluteContextUrl == null) {
            logger.warn("WARNING: Skipping current ping queue processing round because we cannot yet determine the site's absolute context url.");
            return;
        }

        Boolean logOnly = WebloggerConfig.getBooleanProperty("pings.logOnly", false);

        if (logOnly) {
            logger.info("pings.logOnly set to true in properties file to no actual pinging will occur." +
                    " To see logged pings, make sure logging at DEBUG for this class.");
        }

        for (AutoPing ping : pings) {
            try {
                if (logOnly) {
                    logger.debug("Would have pinged:" + ping);
                } else {
                    WeblogUpdatePinger.sendPing(ping.getPingTarget(), ping.getWeblog());
                }
            } catch (IOException |XmlRpcException ex) {
                logger.debug(ex);
            }
        }

        logger.info("ping task completed, pings processed = " + pings.size());
    }

    public void release() {
    }
}