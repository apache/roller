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

package org.apache.roller.weblogger.planet.ui;

import org.apache.roller.planet.pojos.PlanetSubscriptionData;


/**
 * A simple bean for managing the form data used by the PlanetSubscriptions.
 */
public class PlanetSubscriptionsBean {
    
    private String id = null;
    private String title = null;
    private String newsfeedURL = null;
    private String websiteURL = null;
    
    
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNewsfeedURL() {
        return newsfeedURL;
    }

    public void setNewsfeedURL(String newsfeedURL) {
        this.newsfeedURL = newsfeedURL;
    }

    public String getWebsiteURL() {
        return websiteURL;
    }

    public void setWebsiteURL(String websiteURL) {
        this.websiteURL = websiteURL;
    }
    
    
    public void copyTo(PlanetSubscriptionData dataHolder) {
        
        dataHolder.setTitle(getTitle());
        dataHolder.setFeedURL(getNewsfeedURL());
        dataHolder.setSiteURL(getWebsiteURL());
    }
    
    
    public void copyFrom(PlanetSubscriptionData dataHolder) {
        
        setId(dataHolder.getId());
        setTitle(dataHolder.getTitle());
        setNewsfeedURL(dataHolder.getFeedURL());
        setWebsiteURL(dataHolder.getSiteURL());
    }
    
}
