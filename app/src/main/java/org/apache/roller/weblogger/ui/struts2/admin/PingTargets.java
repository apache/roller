/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Admin action for managing global ping targets.
 */
@RestController
public class PingTargets extends UIAction {

    private static Logger log = LoggerFactory.getLogger(PingTargets.class);

    @Autowired
    private PingTargetManager pingTargetManager;

    public void setPingTargetManager(PingTargetManager pingTargetManager) {
        this.pingTargetManager = pingTargetManager;
    }

    public PingTargets() {
        this.actionName = "commonPingTargets";
        this.desiredMenu = "admin";
        this.pageTitle = "commonPingTargets.commonPingTargets";
    }

    // list of available ping targets
    private List<PingTarget> pingTargets = Collections.emptyList();

    // ping target we are working on, if any
    private PingTarget pingTarget = null;

    // id of the ping target to work on
    private String pingTargetId = null;

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    public void loadPingTargets() {
        setPingTargets(pingTargetManager.getCommonPingTargets());
    }

    // prepare method needs to set ping targets list
    public void prepare() {

        // load list of ping targets
        loadPingTargets();

        // load specified ping target if possible
        if (!StringUtils.isEmpty(getPingTargetId())) {
            setPingTarget(pingTargetManager.getPingTarget(getPingTargetId()));
        }
    }

    /**
     * Display the ping targets.
     */
    public String execute() {
        return LIST;
    }


    @RequestMapping(value = "/tb-ui/admin/rest/pingtarget/{id}", method = RequestMethod.PUT)
    public void updateBookmark(@PathVariable String id, @RequestBody PingTargetData newData,
                               HttpServletResponse response) throws ServletException {
        try {
            PingTarget pt = pingTargetManager.getPingTarget(id);
            if (pt != null) {
                pt.setName(newData.getName());
                pt.setPingUrl(newData.getUrl());
                try {
                    pingTargetManager.savePingTarget(pt);
                    WebloggerFactory.flush();
                    setPingTargets(pingTargetManager.getCommonPingTargets());
                } catch (RollbackException e) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
                response.setStatus(HttpServletResponse.SC_OK);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets", method = RequestMethod.PUT)
    public void addPingTarget(@RequestBody PingTargetData newData, HttpServletResponse response) throws ServletException {
        try {
            PingTarget pt = new PingTarget();
            pt.setId(WebloggerCommon.generateUUID());
            pt.setAutoEnabled(false);
            pt.setName(newData.getName());
            pt.setPingUrl(newData.getUrl());
            try {
                pingTargetManager.savePingTarget(pt);
                WebloggerFactory.flush();
                setPingTargets(pingTargetManager.getCommonPingTargets());
            } catch (RollbackException e) {
                response.setStatus(HttpServletResponse.SC_CONFLICT);
                return;
            }
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private static class PingTargetData {
        public PingTargetData() {
        }

        private String name;
        private String url;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets/enable/{id}", method = RequestMethod.POST)
    public boolean enable(@PathVariable String id, HttpServletResponse response) throws ServletException {
        return changeAutoEnableState(id, response, true);
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets/disable/{id}", method = RequestMethod.POST)
    public boolean disable(@PathVariable String id, HttpServletResponse response) throws ServletException {
        return changeAutoEnableState(id, response, false);
    }

    private boolean changeAutoEnableState(String pingTargetId, HttpServletResponse response, boolean state) throws ServletException {
        try {
            PingTarget ping = pingTargetManager.getPingTarget(pingTargetId);
            ping.setAutoEnabled(state);
            pingTargetManager.savePingTarget(ping);
            WebloggerFactory.flush();
            response.setStatus(HttpServletResponse.SC_OK);
            return state;
        } catch (Exception e) {
            log.warn("Error {} ping target: ", state ? "enabling" : "disabling", e.getMessage());
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtarget/{id}", method = RequestMethod.DELETE)
    public void deletePingTarget(@PathVariable String id, HttpServletResponse response) throws ServletException {
        try {
            PingTarget ping = pingTargetManager.getPingTarget(id);
            pingTargetManager.removePingTarget(ping);
            WebloggerFactory.flush();
            getPingTargets().remove(ping);
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.warn("Error deleting ping target: {}", e.getMessage());
            throw new ServletException(e.getMessage());
        }
    }

    public List<PingTarget> getPingTargets() {
        return pingTargets;
    }

    public void setPingTargets(List<PingTarget> pingTargets) {
        this.pingTargets = pingTargets;
    }

    public PingTarget getPingTarget() {
        return pingTarget;
    }

    public void setPingTarget(PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }

    public String getPingTargetId() {
        return pingTargetId;
    }

    public void setPingTargetId(String pingTargetId) {
        this.pingTargetId = pingTargetId;
    }
}
