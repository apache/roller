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
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetData;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * An extension of the UIAction class specific to the Planet actions.
 */
public abstract class PlanetUIAction extends UIAction {
    
    private static Log log = LogFactory.getLog(PlanetUIAction.class);
    
    public static final String PLANET_HANDLE = "zzz_default_planet_zzz";
    
    // the planet used by all Planet actions
    private PlanetData planet = null;
    
    
    public PlanetData getPlanet() {
        if(planet == null) {
            try {
                PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
                planet = pmgr.getPlanetById(PLANET_HANDLE);
            } catch(Exception ex) {
                log.error("Error loading weblogger planet - "+PLANET_HANDLE, ex);
            }
        }
        return planet;
    }
    
}
