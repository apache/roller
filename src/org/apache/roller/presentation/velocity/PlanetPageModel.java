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

package org.apache.roller.presentation.velocity;
import java.util.ArrayList;
import java.util.List;

import org.apache.roller.model.PlanetManager;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.pojos.PlanetGroupData;
import org.apache.roller.pojos.PlanetSubscriptionData;
import org.apache.roller.presentation.RollerRequest;

/**
 * Allow Roller page templates to get the main Planet aggregation (the 'all'
 * and 'external' group), custom aggregations, specified by handle, and 
 * subscription entries (specified by feedUrl). 
 * @author Dave Johnson
 */
public class PlanetPageModel extends PageModel 
{
    PlanetManager planetManager = null;
    public void init(RollerRequest rreq)
    {
        super.init(rreq);
        try 
        {
            planetManager = RollerFactory.getRoller().getPlanetManager();
        }
        catch (Exception e)
        {
            mLogger.error("ERROR initializing page model",e);
        }
    }
    /** 
     * Get main aggregation (of 'all' and 'external' groups) 
     * @returns List of PlanetEntryData objects
     */
    public List getPlanetAggregation(int max) throws Exception
    {
        return planetManager.getAggregation(max);
    }
    /** 
     * Get aggregation by group handle 
     * @returns List of PlanetEntryData objects
     */
    public List getPlanetAggregation(String groupHandle, int max) throws Exception
    {
        List list = new ArrayList();
        PlanetGroupData group = planetManager.getGroup(groupHandle);
        if (group != null) 
        {
            list = planetManager.getAggregation(group, max);
        }
        return list;
    }
    /** 
     * Get entries in a subscription specified by feedUrl.
     * @returns List of PlanetEntryData objects
     */
    public List getPlanetSubscriptionEntries(String feedUrl, int max) throws Exception 
    {
        List list = new ArrayList();
        PlanetSubscriptionData sub = planetManager.getSubscription(feedUrl);
        if (sub != null)
        {
            list = sub.getEntries();
        }
        return list;
    }
}
