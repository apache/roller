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

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;  
import org.apache.commons.logging.LogFactory;  
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.Roller;
import org.apache.roller.weblogger.config.PingConfig;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingQueueManager;


/**
 * Hibernate implementation of the AutoPingManager.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 */
@com.google.inject.Singleton
public class HibernateAutoPingManagerImpl implements AutoPingManager {
    
    private Roller roller;
    
    static final long serialVersionUID = 5420615676256979199L;
    
    private static Log log = LogFactory.getLog(HibernateAutoPingManagerImpl.class);
    
    private HibernatePersistenceStrategy strategy = null;
    
    @com.google.inject.Inject    
    protected HibernateAutoPingManagerImpl(Roller roller, HibernatePersistenceStrategy strat) {
        this.roller = roller;
        this.strategy = strat;
    }
    
    
    public AutoPing getAutoPing(String id) throws WebloggerException {
        return (AutoPing) strategy.load(id, AutoPing.class);
    }
    
    
    public void saveAutoPing(AutoPing autoPing) throws WebloggerException {
        strategy.store(autoPing);
    }
    
    
    public void removeAutoPing(AutoPing autoPing)  throws WebloggerException {
        //TODO: first remove all related category restrictions (category restrictions are not yet implemented)
        strategy.remove(autoPing);
    }
    
    
    public void removeAutoPing(PingTarget pingTarget, Weblog website) throws WebloggerException {
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(AutoPing.class);
            
            // Currently category restrictions are not yet implemented, so we 
            // return all auto ping configs for the website.
            criteria.add(Expression.eq("pingTarget", pingTarget));
            criteria.add(Expression.eq("website", website));
            List matches = criteria.list();
            
            // This should have at most one element, but we remove them all regardless.
            this.removeAutoPings(matches);
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public void removeAutoPings(Collection autopings) throws WebloggerException {
        
        // just go through the list and remove each auto ping
        Iterator pings = autopings.iterator();
        while (pings.hasNext()) {
            this.strategy.remove((AutoPing) pings.next());
        }
    }
    
    
    public void removeAllAutoPings() throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPing.class);
            List allAutoPings = criteria.list();
            this.removeAutoPings(allAutoPings);
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public void queueApplicableAutoPings(WeblogEntry changedWeblogEntry) throws WebloggerException {
        if (PingConfig.getSuspendPingProcessing()) {
            if (log.isDebugEnabled()) log.debug("Ping processing is suspended.  No auto pings will be queued.");
            return;
        }
        
        // TODO: new manager method for addQueueEntries(list)?
        PingQueueManager pingQueueMgr = roller.getPingQueueManager();
        List applicableAutopings = getApplicableAutoPings(changedWeblogEntry);
        for (Iterator i = applicableAutopings.iterator(); i.hasNext(); ) {
            AutoPing autoPing = (AutoPing) i.next();
            pingQueueMgr.addQueueEntry(autoPing);
        }
    }
    
    
    public List getAutoPingsByWebsite(Weblog website) throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPing.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("website", website));
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getAutoPingsByTarget(PingTarget pingTarget) throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPing.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("pingTarget", pingTarget));
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getApplicableAutoPings(WeblogEntry changedWeblogEntry) throws WebloggerException {
        try {
            Session session = ((HibernatePersistenceStrategy) strategy).getSession();
            Criteria criteria = session.createCriteria(AutoPing.class);
            // Currently category restrictions are not yet implemented, so we return all auto ping configs for the
            // website.
            criteria.add(Expression.eq("website", changedWeblogEntry.getWebsite()));
            return criteria.list();
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    public List getCategoryRestrictions(AutoPing autoPing) throws WebloggerException {
        return Collections.EMPTY_LIST;
    }
    
    
    public void setCategoryRestrictions(AutoPing autoPing, Collection newCategories) {
        // NOT YET IMPLEMENTED
        return;
    }
    
    
    public void release() {}
    
}
