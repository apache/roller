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
import org.apache.roller.planet.config.PlanetConfig;
import org.apache.roller.planet.model.Planet;
import org.apache.roller.planet.model.PlanetFactory;
import org.apache.roller.planet.model.PlanetManager;
import org.apache.roller.util.Utilities;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.texen.Generator;


/**
 * Updates Planet aggregator's database of feed entries and generates Planet
 * files based on those entries and the Planet configuration. 
 *
 * - Calls Roller business layer to refresh entries
 * - Uses Velocity Texen to generate the static files
 * - Designed to be run outside of Roller via the TaskRunner class
 */
public class GeneratePlanetTask implements Runnable {
    private static Log log = LogFactory.getLog(GeneratePlanetTask.class);
    
    
    public void run() {
        try {            
            // Update all feeds in planet
            log.info("Refreshing Planet entries");
            Planet planet = PlanetFactory.getPlanet();
            planet.getPlanetManager().refreshEntries();
            planet.flush();
            planet.release();
            
            // Run the planet generation templates
            log.info("Generating Planet files");
            generatePlanet(); 
            
        } catch (RollerException e) {
            log.error("ERROR refreshing entries", e);
        }
    }
    
    
    public void generatePlanet() throws RollerException {
        try {
            Planet planet = PlanetFactory.getPlanet();
            PlanetManager planetManager = planet.getPlanetManager();
                        
            // Ignore values from database
            //String mainPage = planetManager.getConfiguration().getMainPage();
            //String templateDir = planetManager.getConfiguration().getTemplateDir();
            //String outputDir = planetManager.getConfiguration().getMainPage();
            
            // Use values from PlanetConfig instead
            String mainPage =    
                PlanetConfig.getProperty("planet.aggregator.mainPage");
            String templateDir = 
                PlanetConfig.getProperty("planet.aggregator.template.dir"); 
            String outputDir =   
                PlanetConfig.getProperty("planet.aggregator.output.dir");
            
            log.info("Calling Velocity Texen to generate Planet files");
            log.info("   Control file       ["+mainPage+"]");
            log.info("   Template directory ["+templateDir+"]"); 
            log.info("   Output directory   ["+outputDir+"]");

            // Fire up Velocity engine, point it at templates and init
            VelocityEngine engine = new VelocityEngine();
            engine.setProperty("resource.loader","file");
            engine.setProperty("file.resource.loader.class",
              "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
            engine.setProperty("file.resource.loader.path", templateDir);
            engine.init();
            
            // Build context with current date 
            VelocityContext context = new VelocityContext();
            context.put("date", new Date());
            context.put("utilities", new Utilities());
            context.put("planet", new StaticPlanetModel());
            
            // Ensure that output directory exists
            File outputDirObj = new File(outputDir);
            if (!outputDirObj.exists()) outputDirObj.mkdirs();
            
            // Execute mainPage Texen control template
            Generator generator = Generator.getInstance();
            generator.setVelocityEngine(engine);
            generator.setOutputEncoding("utf-8");
            generator.setInputEncoding("utf-8");
            generator.setOutputPath(outputDir);
            generator.setTemplatePath(templateDir);
            generator.parse(mainPage, context);
            generator.shutdown();
            
        } catch (Exception e) {
            throw new RollerException("Writing planet files",e);
        }
    }    
}
