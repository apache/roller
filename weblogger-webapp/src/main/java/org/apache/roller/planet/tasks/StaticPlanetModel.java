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
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.planet.pojos.Subscription;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;

/**
 * Simple planet model for use static planet generation, designed
 * to be duck-type compatible with old PlanetTool templates.
 */
public class StaticPlanetModel {
    private static Log logger = LogFactory.getFactory().getInstance(StaticPlanetModel.class);
    PlanetManager planetManager = null;
    
    
    public StaticPlanetModel() throws RollerException {
        Weblogger planet = WebloggerFactory.getWeblogger();
        planetManager = planet.getWebloggerManager();
    }
        
    // TODO: replace this with something equivalent
//    public PlanetConfigData getConfiguration() throws RollerException {
//        return planetManager.getConfiguration();
//    }
       
       
    public Subscription getSubscription(String feedUrl) throws Exception {
        return planetManager.getSubscription(feedUrl); 
    }
    
    
    public List getFeedEntries(String feedUrl, int maxEntries) throws Exception {
        try {
            Subscription sub = planetManager.getSubscription(feedUrl);
            if(sub != null) {
                return planetManager.getEntries(sub, 0, maxEntries);
            } else {
                return Collections.EMPTY_LIST;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
       
    // removed now that groups must be part of a planet, this method no longer makes sense
//    public List getGroups() throws RollerException {
//        return planetManager.getGroups();
//    }
    
    // removed now that groups must be part of a planet, this method no longer makes sense
//    public PlanetGroup getGroup(String handle) throws RollerException {
//        return planetManager.getGroup(handle);
//    }
    
    
    public List getAggregation(
            PlanetGroup group, int maxEntries) throws RollerException {
        return planetManager.getEntries(group, 0, maxEntries);
    }
    
    
    public Iterator getAllSubscriptions() throws RollerException {
        return planetManager.getSubscriptions().iterator();
    }
    
    
    public int getSubscriptionCount() throws RollerException {
        return planetManager.getSubscriptionCount();
    } 
}

