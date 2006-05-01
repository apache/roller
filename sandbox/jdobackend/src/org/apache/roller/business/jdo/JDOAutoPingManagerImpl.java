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

import java.util.Collection;
import java.util.List;

import org.apache.roller.RollerException;
import org.apache.roller.business.AutoPingManagerImpl;
import org.apache.roller.business.PersistenceStrategy;
import org.apache.roller.pojos.AutoPingData;
import org.apache.roller.pojos.PingTargetData;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;

/**
 * @author Dave Johnson
 */
public class JDOAutoPingManagerImpl extends AutoPingManagerImpl {

    public JDOAutoPingManagerImpl(PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
        // TODO Auto-generated constructor stub
    }

    public void removeAutoPing(PingTargetData pingTarget, WebsiteData website)
            throws RollerException {
        // TODO Auto-generated method stub

    }

    public void removeAllAutoPings() throws RollerException {
        // TODO Auto-generated method stub

    }

    public List getAutoPingsByWebsite(WebsiteData website)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getAutoPingsByTarget(PingTargetData pingTarget)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public List getCategoryRestrictions(AutoPingData autoPing)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

    public void setCategoryRestrictions(AutoPingData autoPing,
            Collection newCategories) {
        // TODO Auto-generated method stub

    }

    public List getApplicableAutoPings(WeblogEntryData changedWeblogEntry)
            throws RollerException {
        // TODO Auto-generated method stub
        return null;
    }

}