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
import org.apache.roller.planet.pojos.PlanetGroup;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.struts2.interceptor.ServletRequestAware;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


/**
 * Manage planet groups.
 */
// TODO: make this work @AllowedMethods({"execute","save","delete"})
public class PlanetGroups extends PlanetUIAction  implements ServletRequestAware {

    private static Log log = LogFactory.getLog(PlanetGroups.class);

    /** Group being deleted */
    private PlanetGroup group = null;

    public PlanetGroups() {
        this.actionName = "planetGroups";
        this.desiredMenu = "admin";
        this.pageTitle = "planetGroups.pagetitle";
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public void setServletRequest(HttpServletRequest request) {
        group = PlanetGroupSubs.getGroupFromRequest(request, getPlanet());
    }

    /**
     * Show planet groups page.
     */
    public String execute() {
        return LIST;
    }

    /**
     * Delete group
     */
    public String delete() {

        if (getGroup() != null) {
            try {
                PlanetManager pmgr = WebloggerFactory.getWeblogger().getPlanetManager();
                pmgr.deleteGroup(getGroup());
                WebloggerFactory.getWeblogger().flush();

                addMessage("planetSubscription.success.deleted");

                setGroup(null);

            } catch (Exception ex) {
                log.error("Error deleting planet group - " + getGroup().getId());
                addError("Error deleting planet group");
            }
        }

        return LIST;
    }

    public PlanetGroup getGroup() {
        return group;
    }

    public void setGroup(PlanetGroup group) {
        this.group = group;
    }

    public List<PlanetGroup> getGroups() {
        List<PlanetGroup> displayGroups = new ArrayList<PlanetGroup>();

        for (PlanetGroup planetGroup : getPlanet().getGroups()) {
            // The "all" group is considered a special group and cannot be managed independently
            if (!planetGroup.getHandle().equals("all")) {
                displayGroups.add(planetGroup);
            }
        }
        return displayGroups;
    }



}
