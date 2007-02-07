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


package org.apache.roller.business.jpa;

import org.apache.roller.business.runnable.RollerTask;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.datamapper.DatamapperThreadManagerImpl;
import org.apache.roller.business.datamapper.DatamapperPersistenceStrategy;
import org.apache.roller.pojos.TaskLockData;
import org.apache.roller.RollerException;

import java.util.Date;

/**
 * @author Mitesh Meswani
 */
public class JPAThreadManagerImpl extends DatamapperThreadManagerImpl {

    public JPAThreadManagerImpl(JPAPersistenceStrategy  strat) {
        super(strat);
    }

    protected boolean acquireLeaseInDatabase(RollerTask task, TaskLockData taskLock,
            long leaseExpireTime) throws RollerException {
        int rowsUpdted = ((JPAPersistenceStrategy) strategy).newUpdateQuery(
            "TaskLockData.updateClient&Timeacquired&TimeleasedByName&Timeacquired")
            .updateAll(new Object[] {task.getClientId(), Integer.valueOf(task.getLeaseTime()),
                                     task.getName(), taskLock.getTimeAquired(),
                                     new Date(leaseExpireTime) } );
        return rowsUpdted == 1;
    }

    protected boolean releaseLeaseInDatabase(RollerTask task)
            throws RollerException {
        int rowsUpdted = ((JPAPersistenceStrategy) strategy).newUpdateQuery(
            "TaskLockData.updateTimeLeasedByName&Client")
            .updateAll(new Object[] {Integer.valueOf(task.getInterval()),
                                     task.getName(), task.getClientId()} );
        return rowsUpdted == 1;
    }

}
