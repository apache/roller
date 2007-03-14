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

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.planet.config.PlanetRuntimeConfig;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.planet.util.Technorati;


/**
 * Rank each subscription by populating Technorati inbound blog and link counts.
 */
public class TechnoratiRankingsTask implements Runnable {
    
    private static Log log = LogFactory.getLog(TechnoratiRankingsTask.class);
    
    
    /**
     * Loop through all subscriptions get get Technorati rankings for each
     */
    public void run() {
        
        int count = 0;
        int errorCount = 0;
        try {
            PlanetManager planet = PlanetFactory.getPlanet().getPlanetManager();
            Technorati technorati = null;
            try {
                String proxyHost = PlanetRuntimeConfig.getProperty("site.proxyhost");
                int proxyPort = PlanetRuntimeConfig.getIntProperty("site.proxyport");
                if (proxyHost != null && proxyPort != -1) {
                    technorati = new Technorati(proxyHost, proxyPort);
                } else {
                    technorati = new Technorati();
                }
            } catch (IOException e) {
                log.error("Aborting collection of Technorati rankings.\n"
                        +"technorati.license not found at root of classpath.\n"
                        +"Get license at http://technorati.com/developers/apikey.html\n"
                        +"Put the license string in a file called technorati.license.\n"
                        +"And place that file at the root of Roller's classpath.\n"
                        +"For example, in the /WEB-INF/classes directory.");
                return;
            }
            
            try {
                int limit = PlanetConfig.getIntProperty(
                        "planet.aggregator.technorati.limit", 500);
                int userCount = planet.getSubscriptionCount();
                int mod = (userCount / limit) + 1;
                
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date());
                int day = cal.get(Calendar.DAY_OF_YEAR);
                
                int start = (day % mod) * limit;
                int end = start + limit;
                end = end > userCount ? userCount : end;
                log.info("Updating subscriptions ["+start+":"+end+"]");
                
                Iterator subs = planet.getSubscriptions().iterator();
                while (subs.hasNext()) {
                    PlanetSubscriptionData sub =
                            (PlanetSubscriptionData)subs.next();
                    if (count >= start && count < end) {
                        try {
                            Technorati.Result result =
                                    technorati.getBloginfo(sub.getSiteURL());
                            if (result != null && result.getWeblog() != null) {
                                sub.setInboundblogs(
                                        result.getWeblog().getInboundblogs());
                                sub.setInboundlinks(
                                        result.getWeblog().getInboundlinks());
                                log.debug("Adding rank for "
                                        +sub.getFeedURL()+" ["+count+"|"
                                        +sub.getInboundblogs()+"|"
                                        +sub.getInboundlinks()+"]");
                            } else {
                                log.debug(
                                        "No ranking available for "
                                        +sub.getFeedURL()+" ["+count+"]");
                                sub.setInboundlinks(0);
                                sub.setInboundblogs(0);
                            }
                            planet.saveSubscription(sub);
                        } catch (Exception e) {
                            log.warn("WARN ranking subscription ["
                                    + count + "]: " + e.getMessage());
                            if (errorCount++ > 5) {
                                log.warn(
                                        "   Stopping ranking, too many errors");
                                break;
                            }
                        }
                    }
                    count++;
                }
                
                // all done, flush results to db
                PlanetFactory.getPlanet().flush();
                
            } finally {
                PlanetFactory.getPlanet().release();
            }
            
        } catch (Exception e) {
            log.error("ERROR ranking subscriptions", e);
        }
    }
    
}
