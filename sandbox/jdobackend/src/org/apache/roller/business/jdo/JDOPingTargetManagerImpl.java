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
import org.apache.roller.business.PingTargetManagerImpl;
import org.apache.roller.pojos.WebsiteData;

public class JDOPingTargetManagerImpl extends PingTargetManagerImpl {
    public JDOPingTargetManagerImpl(PersistenceStrategy persistenceStrategy) {
        super(persistenceStrategy);
    }

    public List getCommonPingTargets() throws RollerException {
        return null;
    }

    public List getCustomPingTargets(WebsiteData website)
            throws RollerException {
        return null;
    }

    public void removeCustomPingTargets(WebsiteData website)
            throws RollerException {
    }

    public void removeAllCustomPingTargets() throws RollerException {
    }
}