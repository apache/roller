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

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.ui.rendering.model.UtilitiesModel;
import org.apache.roller.ui.rendering.velocity.deprecated.OldUtilities;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.texen.Generator;


/**
 * Updates Planet aggregator's database of feed entries.
 * <pre>
 * - Designed to be run outside of Roller via the TaskRunner class
 * - Calls Planet business layer to refresh entries
 * </pre>
 */
public class RefreshPlanetTask implements Runnable {
    private static Log log = LogFactory.getLog(GeneratePlanetTask.class);
    
    public void run() {
        try {            
            // Update all feeds in planet
            log.info("Refreshing Planet entries");
            Planet planet = PlanetFactory.getPlanet();
            planet.getPlanetManager().refreshEntries(
                PlanetConfig.getProperty("planet.aggregator.cache.dir"));                        
            planet.flush();
            planet.release();
            
        } catch (RollerException e) {
            log.error("ERROR refreshing planet", e);
        }
    }
}
