/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.business.hibernate;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.hibernate.criterion.Order;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;
import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingQueueEntry;


/**
 * Hibernate implementation of the PingTargetManager.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
@com.google.inject.Singleton
public class HibernatePingTargetManagerImpl implements PingTargetManager {
    
    static final long serialVersionUID = 121008492583382718L;
    
    private static Log log = LogFactory.getLog(HibernatePingTargetManagerImpl.class);
    
    private final Weblogger roller;
    private final HibernatePersistenceStrategy strategy;
    
    
    @com.google.inject.Inject    
    protected HibernatePingTargetManagerImpl(Weblogger roller, HibernatePersistenceStrategy strat) {
        
        log.debug("Instantiating Hibernate Ping Target Manager");
        this.roller = roller;
        this.strategy = strat;
    }
    
    
    public void removePingTarget(PingTarget pingTarget) throws WebloggerException {
        // remove contents and then target
        this.removePingTargetContents(pingTarget);
        this.strategy.remove(pingTarget);
    }
    
    
    /**
     * Convenience method which removes any queued pings or auto pings that
     * reference the given ping target.
     */
    private void removePingTargetContents(PingTarget ping) 
            throws WebloggerException {
        
        Session session = this.strategy.getSession();
        
        // Remove the website's ping queue entries
        Criteria criteria = session.createCriteria(PingQueueEntry.class);
        criteria.add(Expression.eq("pingTarget", ping));
        List queueEntries = criteria.list();
        Iterator qIT = queueEntries.iterator();
        while(qIT.hasNext()) {
            this.strategy.remove((PingQueueEntry) qIT.next());
        }
        
        // Remove the website's auto ping configurations
        AutoPingManager autoPingMgr = roller.getAutopingManager();
        List autopings = autoPingMgr.getAutoPingsByTarget(ping);
        Iterator it = autopings.iterator();
        while(it.hasNext()) {
            this.strategy.remove((AutoPing) it.next());
        }
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.PingTargetManager#removeAllCustomPingTargets()
     */
    public void removeAllCustomPingTargets() throws WebloggerException {
        
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(PingTarget.class);
            criteria.add(Expression.isNotNull("website"));
            List allCustomTargets = criteria.list();
            removeTargets(allCustomTargets);
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    // Private helper to remove a collection of targets.
    private void removeTargets(Collection customTargets) throws WebloggerException {
        
        // just go through the list and remove each auto ping
        Iterator targets = customTargets.iterator();
        while (targets.hasNext()) {
            this.strategy.remove((PingTarget) targets.next());
        }
    }
    
    
    public void savePingTarget(PingTarget pingTarget) throws WebloggerException {
        strategy.store(pingTarget);
    }
    
    
    public PingTarget getPingTarget(String id) throws WebloggerException {
        return (PingTarget) strategy.load(id, PingTarget.class);
    }

    
    public boolean isNameUnique(PingTarget pingTarget) throws WebloggerException {
        String name = pingTarget.getName();
        if (name == null || name.trim().length() == 0) return false;
        
        String id = pingTarget.getId();
        
        // Determine the set of "brother" targets (custom or common) among which this name should be unique.
        List brotherTargets = null;
        Weblog website = pingTarget.getWebsite();
        if (website == null) {
            brotherTargets = getCommonPingTargets();
        } else {
            brotherTargets = getCustomPingTargets(website);
        }
        
        // Within that set of targets, fail if there is a target with the same name and that target doesn't
        // have the same id.
        for (Iterator i = brotherTargets.iterator(); i.hasNext();) {
            PingTarget brother = (PingTarget) i.next();
            // Fail if it has the same name but not the same id.
            if (brother.getName().equals(name) && (id == null || !brother.getId().equals(id))) {
                return false;
            }
        }
        // No conflict found
        return true;
    }
    
    
    public boolean isUrlWellFormed(PingTarget pingTarget) throws WebloggerException {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) return false;
        try {
            URL parsedUrl = new URL(url);
            // OK.  If we get here, it parses ok.  Now just check that the protocol is http and there is a host portion.
            boolean isHttp = parsedUrl.getProtocol().equals("http");
            boolean hasHost = (parsedUrl.getHost() != null) && (parsedUrl.getHost().trim().length() > 0);
            return isHttp && hasHost;
        } catch (MalformedURLException e) {
            return false;
        }
    }
    
    
    public boolean isHostnameKnown(PingTarget pingTarget) throws WebloggerException {
        String url = pingTarget.getPingUrl();
        if (url == null || url.trim().length() == 0) return false;
        try {
            URL parsedUrl = new URL(url);
            String host = parsedUrl.getHost();
            if (host == null || host.trim().length() == 0) return false;
            InetAddress addr = InetAddress.getByName(host);
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (UnknownHostException e) {
            return false;
        }
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.PingTargetManager#getCommonPingTargets()
     */
    public List getCommonPingTargets() throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(PingTarget.class);
            criteria.add(Expression.isNull("website"));
            criteria.addOrder(Order.asc("name"));
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
        
    }
    
    
    /**
     * @see org.apache.roller.weblogger.model.PingTargetManager#getCustomPingTargets(org.apache.roller.weblogger.pojos.WebsiteData)
     */
    public List getCustomPingTargets(Weblog website) throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(PingTarget.class);
            criteria.add(Expression.eq("website", website));
            criteria.addOrder(Order.asc("name"));
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public void release() {}
    
}
