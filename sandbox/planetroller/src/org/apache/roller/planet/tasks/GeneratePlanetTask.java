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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.model.Planet;
import org.apache.roller.planet.model.PlanetFactory;
import org.apache.roller.planet.model.PlanetManager;
import org.apache.roller.util.Utilities;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.texen.Generator;


/**
 * Fetch feeds as needed and regenerate Planet pages based.
 */
public class GeneratePlanetTask implements Runnable {
    private static Log logger = LogFactory.getLog(GeneratePlanetTask.class);
    
    public void run() {
        try {            
            // Update all feeds in planet
            Planet planet = PlanetFactory.getPlanet();
            planet.getPlanetManager().refreshEntries();
            planet.flush();
            planet.release();
            
            // Run the planet generation templates
            generatePlanet(); 
            
        } catch (RollerException e) {
            logger.error("ERROR refreshing entries", e);
        }
    }
    
    
    public void generatePlanet() throws RollerException {
        try {
            Planet planet = PlanetFactory.getPlanet();
            PlanetManager planetManager = planet.getPlanetManager();
            
            // Fire up Velocity engine, point it at templates and init
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty("resource.loader","file");
            engine.setProperty("file.resource.loader.class",
                    "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            engine.setProperty("file.resource.loader.path",
                    planetManager.getConfiguration().getTemplateDir());
            engine.init();
            
            // Build context with current date 
            VelocityContext context = new VelocityContext();
            context.put("date", new Date());
            context.put("utilities", new Utilities());
            context.put("planet", new StaticPlanetModel());
            
            File outputDir = new File(planetManager.getConfiguration().getOutputDir());
            if (!outputDir.exists()) outputDir.mkdirs();
            
            Generator generator = Generator.getInstance();
            generator.setVelocityEngine(engine);
            generator.setOutputEncoding("utf-8");
            generator.setInputEncoding("utf-8");
            generator.setOutputPath(planetManager.getConfiguration().getOutputDir());
            generator.setTemplatePath(planetManager.getConfiguration().getTemplateDir());
            generator.parse(planetManager.getConfiguration().getMainPage(), context);
            generator.shutdown();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new RollerException("Writing planet files",e);
        }
    }    
}
