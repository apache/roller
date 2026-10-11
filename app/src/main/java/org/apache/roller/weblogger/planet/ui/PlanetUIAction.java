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
 */

package org.apache.roller.weblogger.planet.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.Planet;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.ServletActionContext;


/**
 * An extension of the UIAction class specific to the Planet actions.
 */
public abstract class PlanetUIAction extends UIAction {
    
    private static Log log = LogFactory.getLog(PlanetUIAction.class);
    
    public static final String DEFAULT_PLANET_HANDLE = "default";
    
    // the planet used by all Planet actions
    private Planet planet = null;
    
    
    /**
     * Planet actions are only available while the Planet aggregator is enabled.
     */
    @Override
    public boolean isFeatureEnabled() {
        return WebloggerConfig.getBooleanProperty("planet.aggregator.enabled");
    }

    /**
     * Planet changes are made only by POST, which the CSRF salt filter
     * checks. Methods that change Planet state return DENIED otherwise.
     */
    protected boolean isPostRequest() {
        HttpServletRequest req = ServletActionContext.getRequest();
        return req != null && "POST".equalsIgnoreCase(req.getMethod());
    }

    public Planet getPlanet() {
        if(planet == null) {
            try {
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                planet = pmgr.getWeblogger(DEFAULT_PLANET_HANDLE);
            } catch(Exception ex) {
                log.error("Error loading weblogger planet - "+DEFAULT_PLANET_HANDLE, ex);
            }
        }
        return planet;
    }
    
}
