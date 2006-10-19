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
package org.apache.roller.planet.ui.forms;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.model.PlanetManager;
import org.apache.roller.planet.pojos.PlanetConfigData;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.ui.utils.LoadableForm;

/**
 * UI bean for editing planet config data.
 */
public class ConfigForm implements LoadableForm {
    private static Log log = LogFactory.getLog(ConfigForm.class);
    private String groupHandle = null;
    private PlanetConfigData planetConfig = new PlanetConfigData();
            
    public String load(HttpServletRequest request) throws Exception { 
        log.info("Loading PlanetConfig...");
        Planet planet = PlanetFactory.getPlanet();
        if (planet.getPlanetManager().getConfiguration() != null) {
            planetConfig = planet.getPlanetManager().getConfiguration();
        }
        return "editConfig";
    }
    
    public String save() throws Exception {
        log.info("Saving PlanetConfig...");
        Planet planet = PlanetFactory.getPlanet();
        PlanetManager pmgr= PlanetFactory.getPlanet().getPlanetManager(); 
        if (StringUtils.isNotEmpty(planetConfig.getId())) {
            PlanetConfigData dbconfig = pmgr.getConfiguration();
            dbconfig.setTitle(planetConfig.getTitle());
            dbconfig.setDescription(planetConfig.getDescription());
            dbconfig.setAdminName(  planetConfig.getAdminName());
            dbconfig.setAdminEmail( planetConfig.getAdminEmail());
            dbconfig.setProxyHost(  planetConfig.getProxyHost());
            dbconfig.setProxyPort(  planetConfig.getProxyPort());
            if (StringUtils.isNotEmpty(groupHandle)) {
                dbconfig.setDefaultGroup(pmgr.getGroup(groupHandle));
            }
            planet.getPlanetManager().saveConfiguration(dbconfig);
        } else {
            planet.getPlanetManager().saveConfiguration(planetConfig);
        }         
        planet.flush();
        return "editConfig";
    }
    
    public Map getGroupHandles() throws Exception { 
        PlanetManager pmgr= PlanetFactory.getPlanet().getPlanetManager(); 
        Map map = new HashMap(); 
        map.put("",""); // allow no-choice
        List groups = pmgr.getGroups();
        for (Iterator iter = groups.iterator(); iter.hasNext();) {
            PlanetGroupData group = (PlanetGroupData)iter.next();
            map.put(group.getHandle(), group.getHandle());
        }
        return map;
    }
        
    public PlanetConfigData getPlanetConfig() {
        return planetConfig;
    }
    
    public void setPlanetConfig(PlanetConfigData planetConfig) {
        this.planetConfig = planetConfig;
    }        

    public String getGroupHandle() {
        if (groupHandle == null && planetConfig.getDefaultGroup() != null) {
            groupHandle = planetConfig.getDefaultGroup().getHandle();
        }
        return groupHandle;
    }

    public void setGroupHandle(String groupHandle) {
        this.groupHandle = groupHandle;
    }
}
