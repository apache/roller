/*
 * Copyright 2005 Sun Microsystems, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Manage planet subscriptions
 */
public class PlanetSubscriptions extends UIAction {
    
    private static final Log LOGGER = LogFactory.getLog(PlanetSubscriptions.class);

    private PlanetManager planetManager;

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

    // planet handle we are working in
    private String planetHandle = null;
    
    // the planet we are working in
    private Planet bean = null;
    
    // the subscription to deal with
    private String subUrl = null;

    // full list of subscriptions for the planet
    private List<Subscription> subscriptions = new ArrayList<>();

    public PlanetSubscriptions() {
        this.actionName = "planetSubscriptions";
        this.desiredMenu = "admin";
        this.pageTitle = "planetSubscriptions.title";
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    @Override
    public void prepare() {
        try {
            bean = planetManager.getPlanet(getPlanetHandle());
        } catch (WebloggerException ex) {
            LOGGER.error("Error looking up planet group - " + getPlanetHandle(), ex);
        }

        if (bean != null) {
            Set<Subscription> subsSet = bean.getSubscriptions();

            // iterate over list and build display list
            for (Subscription sub : subsSet) {
                // only include external subs for display
                if(!sub.getFeedURL().startsWith("weblogger:")) {
                    subscriptions.add(sub);
                }
            }
        }
    }
    
    
    /**
     * Populate page model and forward to subscription page
     */
    public String execute() {
        return LIST;
    }

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public String getPlanetHandle() {
        return planetHandle;
    }

    public void setPlanetHandle(String planetHandle) {
        this.planetHandle = planetHandle;
    }
    
    public Planet getBean() {
        return bean;
    }

    public void setBean(Planet bean) {
        this.bean = bean;
    }

    public String getSubUrl() {
        return subUrl;
    }

    public void setSubUrl(String subUrl) {
        this.subUrl = subUrl;
    }    
}
