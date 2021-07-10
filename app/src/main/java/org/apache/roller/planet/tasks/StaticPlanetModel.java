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
package org.apache.roller.planet.tasks;

import java.util.Collections;
import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.planet.pojos.SubscriptionEntry;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;

/**
 * Simple planet model for use static planet generation, designed
 * to be duck-type compatible with old PlanetTool templates.
 */
public class StaticPlanetModel {
    PlanetManager planetManager = null;
    
    
    public StaticPlanetModel() throws RollerException {
        Weblogger planet = WebloggerFactory.getWeblogger();
        planetManager = planet.getPlanetManager();
    }
        
    // TODO: replace this with something equivalent
//    public PlanetConfigData getConfiguration() throws RollerException {
//        return planetManager.getConfiguration();
//    }
       
       
    public Subscription getSubscription(String feedUrl) throws Exception {
        return planetManager.getSubscription(feedUrl); 
    }
    
    
    public List<SubscriptionEntry> getFeedEntries(String feedUrl, int maxEntries) throws Exception {
        try {
            Subscription sub = planetManager.getSubscription(feedUrl);
            if(sub != null) {
                return planetManager.getEntries(sub, 0, maxEntries);
            } else {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
       
    public List<SubscriptionEntry> getAggregation(PlanetGroup group, int maxEntries) throws RollerException {
        return planetManager.getEntries(group, 0, maxEntries);
    }
    
    public int getSubscriptionCount() throws RollerException {
        return planetManager.getSubscriptionCount();
    } 
}

