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
package org.apache.roller.business.jdo;

import java.util.List;

import org.apache.roller.RollerException;
import org.apache.roller.business.PersistenceStrategy;
import org.apache.roller.business.PingQueueManagerImpl;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDOPingQueueManagerImpl extends PingQueueManagerImpl {
    public JDOPingQueueManagerImpl(PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
    }

    public void addQueueEntry(AutoPingData autoPing) throws RollerException {
    }

    public void dropQueue() throws RollerException {
    }

    public List getAllQueueEntries() throws RollerException {
        return null;
    }

    public void removeQueueEntriesByPingTarget(PingTargetData pingTarget)
            throws RollerException {
    }

    public void removeQueueEntriesByWebsite(WebsiteData website)
            throws RollerException {
    }

}