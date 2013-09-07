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

import java.sql.Timestamp;
import java.util.Date;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.DateUtil;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.runnable.ThreadManagerImpl;
import org.apache.roller.weblogger.business.runnable.RollerTask;
import org.apache.roller.weblogger.pojos.TaskLock;


/**
 * JPA implementation of the TaskLockManager interface.
 *
 * This implementation extends the base ThreadManagerImpl class and provides
 * locking abilities which are managed through the database.
 */
@com.google.inject.Singleton
public class JPAThreadManagerImpl extends ThreadManagerImpl {

    private static final Log LOG = LogFactory.getLog(JPAThreadManagerImpl.class);

    private final JPAPersistenceStrategy strategy;


    @com.google.inject.Inject
    protected JPAThreadManagerImpl(Weblogger roller, JPAPersistenceStrategy strat) {
        super();

        LOG.debug("Instantiating JPA Thread Manager");

        this.strategy = strat;
    }


    /**
     * Try to aquire a lock for a given RollerTask.
     */
    @Override
    public boolean registerLease(RollerTask task) {
        
        LOG.debug("Attempting to register lease for task - " + task.getName());
        
        // keep a copy of the current time
        Date currentTime = new Date();
        
        // query for existing lease record first
        TaskLock taskLock = null;
        try {
            taskLock = getTaskLockByName(task.getName());
            if(taskLock == null) {
                LOG.warn("Cannot acquire lease when no tasklock record exists for task - " + task.getName());
            }
        } catch (WebloggerException ex) {
            LOG.warn("Error getting TaskLock", ex);
            return false;
        }

        // try to acquire lease
        if(taskLock != null) {
            try {
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

                if(LOG.isDebugEnabled()) {
                    LOG.debug("last run = "+taskLock.getLastRun());
                    LOG.debug("new run time = "+runTime);
                    LOG.debug("last acquired = "+taskLock.getTimeAcquired());
                    LOG.debug("time leased = "+taskLock.getTimeLeased());
                    LOG.debug("lease expiration = "+leaseExpiration);
                }

                Query q = strategy.getNamedUpdate(
                        "TaskLock.updateClient&Timeacquired&Timeleased&LastRunByName&Timeacquired");
                q.setParameter(1, task.getClientId());
                q.setParameter(2, Integer.valueOf(task.getLeaseTime()));
                q.setParameter(3, new Timestamp(runTime.getTime()));
                q.setParameter(4, task.getName());
                q.setParameter(5, taskLock.getTimeAcquired());
                q.setParameter(6, new Timestamp(leaseExpiration.getTime()));
                int result = q.executeUpdate();

                if(result == 1) {
                    strategy.flush();
                    return true;
                }

            } catch (Exception e) {
                LOG.warn("Error obtaining lease, assuming race condition.", e);
                return false;
            }
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
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error getting TaskLock", ex);
            } else {
                LOG.warn("Error getting TaskLock, enable debug for more info");
            }
            return false;
        }

        // try to release lease, just set lease time to 0
        try {
            Query q = strategy.getNamedUpdate(
                    "TaskLock.updateTimeLeasedByName&Client");
            q.setParameter(1, Integer.valueOf(0));
            q.setParameter(2, task.getName());
            q.setParameter(3, task.getClientId());
            int result = q.executeUpdate();
            
            if(result == 1) {
                strategy.flush();
                return true;
            }

        } catch (Exception e) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Error releasing lease", e);
            } else {
                LOG.warn("Error releasing lease, enable debug for more info");
            }
            return false;
        }

        return false;

    }
    
    
    /**
     * @inheritDoc
     */
    public TaskLock getTaskLockByName(String name) throws WebloggerException {
        // do lookup
        Query q = strategy.getNamedQuery("TaskLock.getByName");
        q.setParameter(1, name);
        try {
            return (TaskLock)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    
    /**
     * @inheritDoc
     */
    public void saveTaskLock(TaskLock data) throws WebloggerException {
        this.strategy.store(data);
    }

}
