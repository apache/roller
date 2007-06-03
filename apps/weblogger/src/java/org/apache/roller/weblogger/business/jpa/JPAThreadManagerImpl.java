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
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.runnable.ThreadManagerImpl;
import org.apache.roller.weblogger.business.runnable.RollerTask;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.pojos.TaskLock;


/**
 * JPA implementation of the TaskLockManager interface.
 *
 * This implementation extends the base ThreadManagerImpl class and provides
 * locking abilities which are managed through the database.
 */
public class JPAThreadManagerImpl extends ThreadManagerImpl {

    private static final Log log = LogFactory.getLog(JPAThreadManagerImpl.class);

    private final JPAPersistenceStrategy strategy;


    public JPAThreadManagerImpl(JPAPersistenceStrategy strat) {
        super();

        log.debug("Instantiating JPA Thread Manager");

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
                RollerFactory.getRoller().flush();
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

            Query q = strategy.getNamedUpdate(
                    "TaskLock.updateClient&Timeacquired&TimeleasedByName&Timeacquired");
            q.setParameter(1, task.getClientId());
            q.setParameter(2, Integer.valueOf(task.getLeaseTime()));
            q.setParameter(3, task.getName());
            q.setParameter(4, taskLock.getTimeAquired());
            q.setParameter(5, new Timestamp(leaseExpireTime));
            int result = q.executeUpdate();
            
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
            Query q = strategy.getNamedUpdate(
                    "TaskLock.updateTimeLeasedByName&Client");
            q.setParameter(1, Integer.valueOf(task.getInterval()));
            q.setParameter(2, task.getName());
            q.setParameter(3, task.getClientId());
            int result = q.executeUpdate();
            
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
        Query q = strategy.getNamedQuery("TaskLock.getByName");
        q.setParameter(1, name);
        try {
            return (TaskLock)q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    
    private void saveTaskLock(TaskLock data) throws WebloggerException {
        this.strategy.store(data);
    }

}
