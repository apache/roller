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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.FeedManager;
import org.apache.roller.weblogger.business.PlanetManager;
import org.apache.roller.weblogger.pojos.Planet;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Subscription;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Manage planets.
 */
@RestController
public class Planets extends UIAction {

    private static Logger log = LoggerFactory.getLogger(Planets.class);
    
    @Autowired
    private PlanetManager planetManager;

    public void setPlanetManager(PlanetManager planetManager) {
        this.planetManager = planetManager;
    }

    @Autowired
    private FeedManager feedManager;

    public void setFeedManager(FeedManager feedManager) {
        this.feedManager = feedManager;
    }

    public Planets() {
        this.actionName = "planets";
        this.desiredMenu = "admin";
        this.pageTitle = "planets.pagetitle";
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    @RequestMapping(value = "/tb-ui/admin/rest/planet/{id}", method = RequestMethod.PUT)
    public void updatePlanet(@PathVariable String id, @RequestBody Planet newData,
                               HttpServletResponse response) throws ServletException {
        Planet planet = planetManager.getPlanetById(id);
        savePlanet(planet, newData, response);
    }

    @RequestMapping(value = "/tb-ui/admin/rest/planets", method = RequestMethod.PUT)
    public String addPlanet(@RequestBody Planet newData, HttpServletResponse response) throws ServletException {
        Planet planet = new Planet();
        planet.setId(WebloggerCommon.generateUUID());
        savePlanet(planet, newData, response);
        return response.getStatus() == HttpServletResponse.SC_OK ? planet.getId() : null;
    }

    private void savePlanet(Planet planet, Planet newData, HttpServletResponse response) throws ServletException {
        try {
            if (planet != null) {
                if ("all".equals(newData.getTitle())) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                planet.setTitle(newData.getTitle());
                planet.setHandle(newData.getHandle());
                planet.setDescription(newData.getDescription());
                try {
                    planetManager.savePlanet(planet);
                    WebloggerFactory.flush();
                } catch (RollbackException e) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/planets", method = RequestMethod.GET)
    public List<Planet> getPlanets() throws ServletException {
        return planetManager.getPlanets().stream()
                // The "all" planet is considered a special planet and cannot be managed independently
                .filter(planet -> !planet.getHandle().equals("all"))
                .collect(Collectors.toList());
    }

    @RequestMapping(value = "/tb-ui/admin/rest/planets/{id}", method = RequestMethod.DELETE)
    public void deletePlanet(@PathVariable String id, HttpServletResponse response) throws ServletException {
        try {
            Planet planetToDelete = planetManager.getPlanetById(id);
            planetManager.deletePlanet(planetToDelete);
            WebloggerFactory.flush();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.error("Error deleting planet - {}", id);
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/planet/{id}", method = RequestMethod.GET)
    public Planet getPlanet(@PathVariable String id) throws ServletException {
        return planetManager.getPlanetById(id);
    }

    @RequestMapping(value = "/tb-ui/admin/rest/planetsubscriptions/{id}", method = RequestMethod.DELETE)
    public void deletePlanetSubscription(@PathVariable String id, HttpServletResponse response) throws ServletException {
        try {
            Subscription subToDelete = planetManager.getSubscriptionById(id);
            if (subToDelete != null) {
                planetManager.deleteSubscription(subToDelete);
                subToDelete.getPlanet().getSubscriptions().remove(subToDelete);
                WebloggerFactory.flush();
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.error("Error deleting subscription - {}", id);
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/planetsubscriptions", method = RequestMethod.PUT)
    public void addPlanetSubscription(@RequestParam(name="planetId") String planetId, @RequestParam String feedUrl,
                                      HttpServletResponse response) throws ServletException {
        try {
            Planet planet = planetManager.getPlanetById(planetId);
            if (planet == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Subscription sub = planetManager.getSubscription(planet, feedUrl);
            if (sub == null) {
                sub = feedManager.fetchSubscription(feedUrl);
                if (sub != null) {
                    sub.setPlanet(planet);
                    planetManager.saveSubscription(sub);
                    planet.getSubscriptions().add(sub);
                    WebloggerFactory.flush();
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(422);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }
}
