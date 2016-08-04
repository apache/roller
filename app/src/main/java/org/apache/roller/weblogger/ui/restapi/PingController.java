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
package org.apache.roller.weblogger.ui.restapi;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.xmlrpc.XmlRpcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.RollbackException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

@RestController
public class PingController {

    private static Logger log = LoggerFactory.getLogger(PingController.class);

    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

    @Autowired
    private PingTargetManager pingTargetManager;

    public void setPingTargetManager(PingTargetManager pingTargetManager) {
        this.pingTargetManager = pingTargetManager;
    }

    @Autowired
    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    public PingController() {
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets", method = RequestMethod.GET)
    public List<PingTarget> getPingTargets() throws ServletException {
        return pingTargetManager.getPingTargets();
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtarget/{id}", method = RequestMethod.PUT)
    public PingTarget updatePingData(@PathVariable String id, @RequestBody PingTarget newData,
                               HttpServletResponse response) throws ServletException {
        try {
            PingTarget pingTarget = pingTargetManager.getPingTarget(id);
            if (pingTarget != null) {
                return savePingTarget(pingTarget, newData, response);
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                return null;
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets", method = RequestMethod.PUT)
    public PingTarget addPingTarget(@RequestBody PingTarget newData, HttpServletResponse response) throws ServletException {
        try {
            PingTarget pingTarget = new PingTarget();
            pingTarget.setId(Utilities.generateUUID());
            pingTarget.setEnabled(false);
            return savePingTarget(pingTarget, newData, response);
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private PingTarget savePingTarget(PingTarget pingTarget, PingTarget newData, HttpServletResponse response) throws ServletException {
        try {
            if (pingTarget != null) {
                pingTarget.setName(newData.getName());
                pingTarget.setPingUrl(newData.getPingUrl());
                pingTargetManager.savePingTarget(pingTarget);
                persistenceStrategy.flush();
                response.setStatus(HttpServletResponse.SC_OK);
                return pingTarget;
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (RollbackException e) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
        }
        return null;
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets/enable/{id}", method = RequestMethod.POST)
    public PingTarget enable(@PathVariable String id, HttpServletResponse response) throws ServletException {
        return changeAutoEnableState(id, response, true);
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets/test/{id}", method = RequestMethod.POST)
    public ResponseEntity testPingTarget(@PathVariable String id, HttpServletResponse response) throws ServletException {

        PingTarget target = pingTargetManager.getPingTarget(id);
        String handle = propertiesManager.getStringProperty("site.frontpage.weblog.handle");
        Weblog frontPageWeblog = !StringUtils.isEmpty(handle) ? weblogManager.getWeblogByHandle(handle) : null;

        if (target != null && frontPageWeblog != null) {
            try {
                return ResponseEntity.ok(pingTargetManager.sendPing(target, frontPageWeblog));
            } catch (IOException | XmlRpcException ex) {
                String message = bundle.getString(ex instanceof UnknownHostException ? "ping.unknownHost" : "ping.networkConnectionFailed");
                return ResponseEntity.badRequest().body(message);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    @RequestMapping(value = "/tb-ui/admin/rest/pingtargets/disable/{id}", method = RequestMethod.POST)
    public PingTarget disable(@PathVariable String id, HttpServletResponse response) throws ServletException {
        return changeAutoEnableState(id, response, false);
    }

    private PingTarget changeAutoEnableState(String pingTargetId, HttpServletResponse response, boolean state) throws ServletException {
        try {
            PingTarget ping = pingTargetManager.getPingTarget(pingTargetId);
            ping.setEnabled(state);
            pingTargetManager.savePingTarget(ping);
            persistenceStrategy.flush();
            response.setStatus(HttpServletResponse.SC_OK);
            return ping;
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
            persistenceStrategy.flush();
            response.setStatus(HttpServletResponse.SC_OK);
        } catch (Exception e) {
            log.warn("Error deleting ping target: {}", e.getMessage());
            throw new ServletException(e.getMessage());
        }
    }

}
