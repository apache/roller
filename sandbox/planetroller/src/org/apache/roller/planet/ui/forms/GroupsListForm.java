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

import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext; 
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.pojos.PlanetGroupData;

/**
 * UI bean for managing list of groups.
 */
public class GroupsListForm {
    private static Log log = LogFactory.getLog(GroupsListForm.class);
            
    public String deleteGroup() {
        log.info("Delete Group...");
        Planet planet = PlanetFactory.getPlanet();
        FacesContext fctx = FacesContext.getCurrentInstance(); 
        Map params = fctx.getExternalContext().getRequestParameterMap();
        String groupid = (String)params.get("groupid");
        try {
            PlanetGroupData group = planet.getPlanetManager().getGroupById(groupid);
            planet.getPlanetManager().deleteGroup(group);
            planet.flush();
        } catch (RollerException ex) {
            return "error";
        }        
        return "editGroups";
    }

    public List getGroups() throws Exception {
        return PlanetFactory.getPlanet().getPlanetManager().getGroups();
    }
    
    public String getSiteURL() throws Exception {
        return PlanetFactory.getPlanet().getPlanetManager().getConfiguration().getSiteURL();
    }
}
