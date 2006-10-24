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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ResourceBundle;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.PlanetFactory;
import org.apache.roller.planet.business.PlanetManager;
import org.apache.roller.planet.pojos.PlanetGroupData;
import org.apache.roller.planet.pojos.PlanetSubscriptionData;
import org.apache.roller.planet.ui.utils.LoadableForm;

/**
 * UI bean for editing subscription data.
 */
public class SubscriptionForm implements LoadableForm {
    private ResourceBundle bundle = 
        ResourceBundle.getBundle("ApplicationResources");
    private static Log log = LogFactory.getLog(GroupsListForm.class);
    private PlanetSubscriptionData subscription = new PlanetSubscriptionData();
    private String groupid = null;
    private String subid = null;
    
    public SubscriptionForm() {}
    
    public String load(HttpServletRequest request) throws Exception {
        log.info("Loading Subscription...");
        subid = request.getParameter("subid");
        groupid = request.getParameter("groupid");
        if (StringUtils.isNotEmpty(subid)) {
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            subscription = pmgr.getSubscriptionById(subid);
        } else {
            subscription = new PlanetSubscriptionData();            
        }
        groupid = request.getParameter("groupid");        
        return "editSubscription";
    }
    
    public String edit() throws Exception {
        FacesContext fctx = FacesContext.getCurrentInstance();
        return load((HttpServletRequest)fctx.getExternalContext().getRequest());
    }
    
    public String add() throws Exception {
        FacesContext fctx = FacesContext.getCurrentInstance();
        return load((HttpServletRequest)fctx.getExternalContext().getRequest());
    } 
    
    public String save() throws Exception {
        log.info("Saving Subscription...");                
        PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
        if (StringUtils.isNotEmpty(getSubscription().getId())) {
            PlanetSubscriptionData dbsub = pmgr.getSubscriptionById(getSubscription().getId());
            dbsub.setTitle(subscription.getTitle());
            dbsub.setName(subscription.getName());
            dbsub.setFeedURL(subscription.getFeedURL());
            dbsub.setSiteURL(subscription.getSiteURL());
            pmgr.saveSubscription(dbsub); 
        } else {
            PlanetSubscriptionData existingSub = pmgr.getSubscription(subscription.getFeedURL());
            if (existingSub != null) {
                subscription = existingSub;
            }
            else { 
                pmgr.saveSubscription(subscription);
            }
            PlanetGroupData group = pmgr.getGroupById(groupid);
            group.getSubscriptions().add(subscription);
            pmgr.saveGroup(group);
        }
        PlanetFactory.getPlanet().flush();
        return "editSubscription";
    }    

    public PlanetSubscriptionData getSubscription() throws Exception {
        if (subscription == null && subid != null) {
            PlanetManager pmgr = PlanetFactory.getPlanet().getPlanetManager();
            subscription = pmgr.getSubscriptionById(groupid);
        }
        return subscription;
    }

    public String getGroupid() {
        return groupid;
    }

    public void setGroupid(String groupid) {
        this.groupid = groupid;
    }
    
    public void checkURL(FacesContext context, UIComponent component, Object value) {
        if (value == null || !(value instanceof String)) return;
        try {
            URL url = new URL((String)value);
        } catch (MalformedURLException ex) {
            FacesMessage msg = new FacesMessage();
            msg.setDetail(bundle.getString("errorBadURL"));
            msg.setSeverity(FacesMessage.SEVERITY_ERROR);
            throw new ValidatorException(msg);
        }
        return;
    }
}
