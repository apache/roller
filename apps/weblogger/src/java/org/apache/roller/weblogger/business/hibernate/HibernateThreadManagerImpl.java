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
import org.apache.roller.util.DateUtil;
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
     * Try to aquire a lease for a given RollerTask.
     */
    @Override
    public boolean registerLease(RollerTask task) {
        
        log.debug("Attempting to register lease for task - "+task.getName());
        
        // keep a copy of the current time
        Date currentTime = new Date();
        
        // query for existing lease record first
        TaskLock taskLock = null;
        try {
            taskLock = getTaskLockByName(task.getName());
            if(taskLock == null) {
                log.warn("Cannot acquire lease when no tasklock record exists for task - "+task.getName());
            }
        } catch (WebloggerException ex) {
            log.warn("Error getting TaskLock", ex);
            return false;
        }
        
        // try to acquire lease
        if(taskLock != null) try {
            // calculate lease expiration time
            Date leaseExpiration = taskLock.getLeaseExpiration();
            
            // calculate run time for task, this is expected time, not actual time
            // i.e. if a task is meant to run daily at midnight this should
            // reflect 00:00:00 on the current day
            Date runTime = currentTime;
            if("startOfDay".equals(task.getStartTimeDesc())) {
                // start of today
                runTime = DateUtil.getStartOfDay(currentTime);
            } else if("startOfHour".equals(task.getStartTimeDesc())) {
                // start of this hour
                runTime = DateUtil.getStartOfHour(currentTime);
            } else {
                // start of this minute
                runTime = DateUtil.getStartOfMinute(currentTime);
            }
            
            if(log.isDebugEnabled()) {
                log.debug("last run = "+taskLock.getLastRun());
                log.debug("new run time = "+runTime);
                log.debug("last acquired = "+taskLock.getTimeAquired());
                log.debug("time leased = "+taskLock.getTimeLeased());
                log.debug("lease expiration = "+leaseExpiration);
            }
            
            Session session = strategy.getSession();
            String queryHQL = "update TaskLock "+
                    "set client=:client, timeacquired=current_timestamp(), timeleased=:timeleased, lastrun=:runTime "+
                    "where name=:name and timeacquired=:timeacquired and current_timestamp() > :leaseends";
            Query query = session.createQuery(queryHQL);
            query.setString("client", task.getClientId());
            query.setInteger("timeleased", task.getLeaseTime());
            query.setTimestamp("runTime", runTime);
            query.setString("name", task.getName());
            query.setTimestamp("timeacquired", taskLock.getTimeAquired());
            query.setTimestamp("leaseends", leaseExpiration);
            int result = query.executeUpdate();
            
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
    @Override
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
            Session session = strategy.getSession();
            String queryHQL = "update TaskLock set timeLeased=:interval "+
                    "where name=:name and client=:client";
            Query query = session.createQuery(queryHQL);
            query.setInteger("interval", 0);
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
    
    
    /**
     * @inheritDoc
     */
    public TaskLock getTaskLockByName(String name) throws WebloggerException {
        
        // do lookup
        try {
            Session session = strategy.getSession();
            Criteria criteria = session.createCriteria(TaskLock.class);
            
            criteria.add(Expression.eq("name", name));
            TaskLock taskLock = (TaskLock) criteria.uniqueResult();
            
            return taskLock;
        } catch (HibernateException e) {
            throw new WebloggerException(e);
        }
    }
    
    
    /**
     * @inheritDoc
     */
    public void saveTaskLock(TaskLock data) throws WebloggerException {
        this.strategy.store(data);
    }
    
}
