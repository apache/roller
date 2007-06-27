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

package org.apache.roller.weblogger.business.hibernate;

import java.util.Date;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.runnable.ThreadManagerImpl;
import org.apache.roller.weblogger.business.runnable.RollerTask;
import org.apache.roller.weblogger.pojos.TaskLock;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.criterion.Expression;


/**
 * Hibernate implementation of the TaskLockManager interface.
 *
 * This implementation extends the base ThreadManagerImpl class and provides
 * leasing abilities which are managed through the database.
 */
@com.google.inject.Singleton
public class HibernateThreadManagerImpl extends ThreadManagerImpl {
    
    private static Log log = LogFactory.getLog(HibernateThreadManagerImpl.class);
    
    private final Weblogger roller;
    private final HibernatePersistenceStrategy strategy;
    
    
    @com.google.inject.Inject    
    protected HibernateThreadManagerImpl(Weblogger roller, HibernatePersistenceStrategy strat) {
        super();
        
        log.debug("Instantiating Hibernate Thread Manager");
        this.roller = roller;
        this.strategy = strat;
    }
    
    
    /**
     * Try to aquire a lock for a given RollerTask.
     */
    public boolean registerLease(RollerTask task) {
        
        // query for existing lease record first
        TaskLock taskLock = null;
        try {
            taskLock = this.getTaskLockByName(task.getName());
            
            if(taskLock == null) {
                // insert an empty record, then we will actually acquire the
                // lease below using an update statement 
                taskLock = new TaskLock();
                taskLock.setName(task.getName());
                taskLock.setTimeAquired(new Date(0));
                taskLock.setTimeLeased(0);
                
                // save it and flush
                this.saveTaskLock(taskLock);
                roller.flush();
            }
            
        } catch (WebloggerException ex) {
            log.warn("Error getting or inserting TaskLock", ex);
            return false;
        }
        
        // try to acquire lease
        try {
            // calculate lease expiration time
            // expireTime = startTime + (timeLeased * 60sec/min) - 1 sec
            // we remove 1 second to adjust for precision differences
            long leaseExpireTime = taskLock.getTimeAquired().getTime()+
                    (60000*taskLock.getTimeLeased())-1000;
            
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            String queryHQL = "update TaskLock "+
                    "set client=:client, timeacquired=current_timestamp(), timeleased=:timeleased "+
                    "where name=:name and timeacquired=:timeacquired "+
                    "and :leaseends < current_timestamp()";
            Query query = session.createQuery(queryHQL);
            query.setString("client", task.getClientId());
            query.setInteger("timeleased", task.getLeaseTime());
            query.setString("name", task.getName());
            query.setTimestamp("timeacquired", taskLock.getTimeAquired());
            query.setTimestamp("leaseends", new Date(leaseExpireTime));
            int result = query.executeUpdate();
            
            // this may not be needed
            roller.flush();
            
            if(result == 1) {
                return true;
            }
            
        } catch (Exception e) {
            log.warn("Error obtaining lease, assuming race condition.", e);
            return false;
        }
        
        return false;
    }
    
    
    /**
     * Try to release the lock for a given RollerTask.
     */
    public boolean unregisterLease(RollerTask task) {
        
        // query for existing lease record first
        TaskLock taskLock = null;
        try {
            taskLock = this.getTaskLockByName(task.getName());
            
            if(taskLock == null) {
                return false;
            }
            
        } catch (WebloggerException ex) {
            log.warn("Error getting TaskLock", ex);
            return false;
        }
        
        // try to release lease, just set lease time to 0
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            String queryHQL = "update TaskLock set timeLeased=:interval "+
                    "where name=:name and client=:client";
            Query query = session.createQuery(queryHQL);
            query.setInteger("interval", task.getInterval());
            query.setString("name", task.getName());
            query.setString("client", task.getClientId());
            int result = query.executeUpdate();
            
            // this may not be needed
            roller.flush();
            
            if(result == 1) {
                return true;
            }
            
        } catch (Exception e) {
            log.warn("Error releasing lease.", e);
            return false;
        }
        
        return false;
    }
    
    
    private TaskLock getTaskLockByName(String name) throws WebloggerException {
        
        // do lookup
        try {
            Session session = ((HibernatePersistenceStrategy)this.strategy).getSession();
            Criteria criteria = session.createCriteria(TaskLock.class);
            
            criteria.add(Expression.eq("name", name));
            TaskLock taskLock = (TaskLock) criteria.uniqueResult();
            
            return taskLock;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    private void saveTaskLock(TaskLock data) throws WebloggerException {
        this.strategy.store(data);
    }
    
}
