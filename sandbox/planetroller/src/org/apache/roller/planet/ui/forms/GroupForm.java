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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext; 
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest; 
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.business.Planet;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.planet.ui.utils.LoadableForm;

/**
 * UI bean for editing group data, designed for request scope.
 */
public class GroupForm implements LoadableForm {
    private ResourceBundle bundle = 
        ResourceBundle.getBundle("ApplicationResources");
    private static Log log = LogFactory.getLog(GroupsListForm.class);
    private PlanetGroupData group = new PlanetGroupData();
    private Date lastSave = null;
    
    public GroupForm() {}
    
    public String load(HttpServletRequest request) throws Exception {
        log.info("Loading Group...");
        String groupid = request.getParameter("groupid");
        if (StringUtils.isNotEmpty(groupid)) {
            Planet planet = PlanetFactory.getPlanet();
            group = planet.getPlanetManager().getGroupById(groupid);
        }
        return "editGroup";
    }
    
    public String edit() throws Exception {
        lastSave = null;
        FacesContext fctx = FacesContext.getCurrentInstance();
        return load((HttpServletRequest)fctx.getExternalContext().getRequest());
    }
    
    public String add() throws Exception {
        lastSave = null;
        group = new PlanetGroupData();
        return "editGroup";
    }
    
    public String save() throws Exception {
        log.info("Saving Group...");
        Planet planet = PlanetFactory.getPlanet();
        if (StringUtils.isNotEmpty(group.getId())) {
            PlanetGroupData dbgroup = planet.getPlanetManager().getGroupById(group.getId());
            dbgroup.setTitle(group.getTitle());
            dbgroup.setHandle(group.getHandle());
            dbgroup.setDescription(group.getDescription());
            planet.getPlanetManager().saveGroup(dbgroup);
        } else {
            planet.getPlanetManager().saveGroup(group); 
        }
        planet.flush();
        setLastSave(new Date());
        return "editGroup";
    }   
    
    public String removeSubscription() throws Exception {
        log.info("Deleting subscription...");
        
        FacesContext fctx = FacesContext.getCurrentInstance();
        Map params = fctx.getExternalContext().getRequestParameterMap();
        String subid = (String)params.get("subid");
        
        Planet planet = PlanetFactory.getPlanet();
        PlanetManager pmgr = planet.getPlanetManager();
        if (StringUtils.isNotEmpty(subid)) {
            PlanetGroupData dbgroup = pmgr.getGroupById(group.getId());
            PlanetSubscriptionData sub = pmgr.getSubscriptionById(subid);
            dbgroup.getSubscriptions().remove(sub);
            planet.getPlanetManager().saveGroup(dbgroup); 
            planet.flush();
        } 
        return "editGroup";
    }
        
    public PlanetGroupData getGroup() throws Exception {
        return group;
    }
    
    public List getSubscriptions() throws Exception {
        if (group.getId() != null) {
            Planet planet = PlanetFactory.getPlanet();
            group = planet.getPlanetManager().getGroupById(group.getId());
        }
        return new ArrayList(group.getSubscriptions());
    }

    public Date getLastSave() {
        return lastSave;
    }

    public void setLastSave(Date lastSave) {
        this.lastSave = lastSave;
    }

    public void checkHandle(FacesContext context, UIComponent component, Object value) throws Exception {
        if (value == null || !(value instanceof String)) return;
        if (group != null && StringUtils.isNotEmpty(group.getId())) return;
        Planet planet = PlanetFactory.getPlanet();
        PlanetGroupData dbgroup = planet.getPlanetManager().getGroup((String) value);
        if (dbgroup != null) {
            FacesMessage msg = new FacesMessage();
            msg.setDetail(bundle.getString("groupErrorDuplicateHandle"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
    }
}
