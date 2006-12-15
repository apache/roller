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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.runnable.ThreadManagerImpl;
import org.apache.roller.business.runnable.RollerTask;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.TaskLockData;

/**
 * Datamapper implementation of the TaskLockManager interface.
 *
 * This implementation extends the base ThreadManagerImpl class and provides
 * locking abilities which are managed through the database.
 */
public class DatamapperThreadManagerImpl extends ThreadManagerImpl {

    private static Log log = LogFactory.getLog(
        DatamapperThreadManagerImpl.class);

    private DatamapperPersistenceStrategy strategy;


    public DatamapperThreadManagerImpl(DatamapperPersistenceStrategy strat) {
        super();

        log.debug("Instantiating Datamapper Thread Manager");

        this.strategy = strat;
    }


    /**
     * Try to aquire a lock for a given RollerTask.
     *
     * Remember, locks are only given if ...
     *   1. the task is not currently locked and it's past the next scheduled
     *      run time for the particular task
     *   2. the task *is* locked, but its lease has expired
     */
    public boolean acquireLock(RollerTask task) {

        boolean lockAcquired = false;

        TaskLockData taskLock = null;
        try {
            taskLock = this.getTaskLockByName(task.getName());

            // null here just means hasn't been initialized yet
            if(taskLock == null) {
                taskLock = new TaskLockData();
                taskLock.setName(task.getName());
                taskLock.setLocked(false);
            }
        } catch (RollerException ex) {
            log.warn("Error getting TaskLockData", ex);
            return false;
        }

        Date now = new Date();
        Date nextRun = taskLock.getNextRun(task.getInterval());
        if( !taskLock.isLocked() && (nextRun == null || now.after(nextRun))) {

            // set appropriate values for TaskLock and save it
            taskLock.setLocked(true);
            taskLock.setTimeAquired(now);
            taskLock.setTimeLeased(task.getLeaseTime());
            taskLock.setLastRun(now);

            try {
                // save it *and* flush
                this.saveTaskLock(taskLock);
                RollerFactory.getRoller().flush();
                lockAcquired = true;
            } catch (RollerException ex) {
                log.warn("Error saving TaskLockData", ex);
                lockAcquired = false;
            }
        }

        return lockAcquired;
    }


    /**
     * Try to release the lock for a given RollerTask.
     */
    public boolean releaseLock(RollerTask task) {

        boolean lockReleased = false;

        TaskLockData taskLock = null;
        try {
            taskLock = this.getTaskLockByName(task.getName());
        } catch (RollerException ex) {
            log.warn("Error getting TaskLockData", ex);
            return false;
        }

        if(taskLock != null && taskLock.isLocked()) {
            // set appropriate values for TaskLock and save it
            Date now = new Date();
            taskLock.setLocked(false);

            try {
                // save it *and* flush
                this.saveTaskLock(taskLock);
                RollerFactory.getRoller().flush();
                lockReleased = true;
            } catch (RollerException ex) {
                log.warn("Error saving TaskLockData", ex);
                lockReleased = false;
            }
        } else if(taskLock != null && !taskLock.isLocked()) {
            // if lock is already released then don't fret about it
            lockReleased = true;
        }

        return lockReleased;
    }


    /**
     * Is a task currently locked?
     */
    public boolean isLocked(RollerTask task) {

        // default is "true"!
        boolean locked = true;

        try {
            TaskLockData taskLock = this.getTaskLockByName(task.getName());
            if(taskLock != null) {
                locked = taskLock.isLocked();
            } else {
                // if taskLock is null, but we didn't get an exception then
                // that means this lock hasn't been initialized yet
                locked = false;
            }
        } catch (RollerException ex) {
            log.warn("Error getting TaskLockData", ex);
        }

        return locked;
    }

    private TaskLockData getTaskLockByName(String name) throws RollerException {
        // do lookup
        return (TaskLockData) strategy.newQuery(
                TaskLockData.class, "TaskLockData.getByName").setUnique()
                .execute(name);
    }

    private void saveTaskLock(TaskLockData data) throws RollerException {
        this.strategy.store(data);
    }

}
