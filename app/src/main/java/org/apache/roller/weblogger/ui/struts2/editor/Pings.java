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

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.business.PingResult;
import org.apache.xmlrpc.XmlRpcException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Actions for setting up automatic ping configuration for a weblog.
 */
public class Pings extends UIAction {

    private static Logger log = LoggerFactory.getLogger(Pings.class);

    private PingTargetManager pingTargetManager;

    public void setPingTargetManager(PingTargetManager pingTargetManager) {
        this.pingTargetManager = pingTargetManager;
    }

    // ping target id to work on
    private String pingTargetId = null;
    
    // ping target object we are working on, if available
    private PingTarget pingTarget = null;
    
    // common ping targets list
    private List<PingTarget> commonPingTargets = Collections.emptyList();
    
    // track the enabled/disabled status for pings
    private Map pingStatus = Collections.EMPTY_MAP;

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    public Pings() {
        this.actionName = "pings";
        this.desiredMenu = "editor";
        this.pageTitle = "pings.title";
    }
    
    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    public void prepare() {
        
        // load selected ping target, if possible
        if(getPingTargetId() != null) {
            setPingTarget(pingTargetManager.getPingTarget(getPingTargetId()));
        }
        
        // load common ping targets list
        setCommonPingTargets(pingTargetManager.getCommonPingTargets());
    }
    
    
    /*
     * Display the common ping targets with page
     */
    public String execute() {
        
        // load map of enabled auto pings
        buildIsEnabledMap();
        
        return LIST;
    }
    
    
    /**
     * Enable a ping target.
     */
    public String enable() {
        
        if(getPingTarget() != null) {
            try {
                AutoPing autoPing = new AutoPing(getPingTarget(), getActionWeblog());
                pingTargetManager.saveAutoPing(autoPing);
                WebloggerFactory.flush();
            } catch(Exception ex) {
                log.error("Error saving auto ping for target - "+getPingTargetId(), ex);
                addError("Error enabling auto ping");
            }
        }
        
        return execute();
    }
    
    
    /**
     * Disable a ping target.
     */
    public String disable() {
        
        if(getPingTarget() != null) {
            try {
                pingTargetManager.removeAutoPing(getPingTarget(), getActionWeblog());
                WebloggerFactory.flush();
            } catch (Exception ex) {
                log.error("Error removing auto ping for target - " + getPingTargetId(), ex);
                addError("Error disabling auto ping");
            }
        }
        
        return execute();
    }
    
    
    /**
     * Ping the selected target now.
     */
    public String pingNow() {
        
        if(getPingTarget() != null) {
            try {
                if (propertiesManager.getBooleanProperty("pings.suspendPingProcessing")) {
                    log.debug("Ping processing is disabled.");
                    addError("ping.pingProcessingIsSuspended");
                } else {
                    PingResult pingResult = pingTargetManager.sendPing(getPingTarget(), getActionWeblog());
                    if (pingResult.isError()) {
                        log.debug("Ping Result: " + pingResult);
                        if (pingResult.getMessage() != null && pingResult.getMessage().trim().length() > 0) {
                            addError("ping.transmittedButError");
                            addError(pingResult.getMessage());
                        } else {
                            addError("ping.transmissionFailed");
                        }
                    } else {
                        if (pingResult.getMessage() != null) {
                            addMessage("ping.successfulWithMessage", getPingTarget().getName());
                            addMessage(pingResult.getMessage());
                        } else {
                            addMessage("ping.successful");
                        }
                    }
                }
            } catch (IOException|XmlRpcException ex) {
                log.debug("exception:", ex);
                addError("ping.transmissionFailed");
                addSpecificMessages(ex);
            }
        }
        
        return execute();
    }
    
    
    // some extra error messaging
    private void addSpecificMessages(Exception ex) {
        if (ex instanceof UnknownHostException) {
            addError("ping.unknownHost");
        } else if (ex instanceof SocketException) {
            addError("ping.networkConnectionFailed");
        }
    }

    
    /**
     * Private helper to build a map indexed by ping target id with values Boolean.TRUE and Boolean.FALSE
     * based on whether the ping target is enabled (has a corresponding auto ping configuration).
     */
    private void buildIsEnabledMap() {
        
        // Build isEnabled map (keyed by ping target id and values Boolean.TRUE/Boolean.FALSE)
        Map<String, Boolean> isEnabled = new HashMap<String, Boolean>();
        
        List<AutoPing> autoPings = pingTargetManager.getAutoPingsByWeblog(getActionWeblog());

        // Add the enabled auto ping configs with TRUE
        for (AutoPing autoPing : autoPings) {
            isEnabled.put(autoPing.getPingTarget().getId(), Boolean.TRUE);
        }
        
        // Add disabled ping targets ones with FALSE
        for (PingTarget inPingTarget : getCommonPingTargets()) {
            if (isEnabled.get(inPingTarget.getId()) == null) {
                isEnabled.put(inPingTarget.getId(), Boolean.FALSE);
            }
        }

        if (isEnabled.size() > 0) {
            setPingStatus(isEnabled);
        }
    }
    
    
    public String getPingTargetId() {
        return pingTargetId;
    }

    public void setPingTargetId(String pingTargetId) {
        this.pingTargetId = pingTargetId;
    }

    public PingTarget getPingTarget() {
        return pingTarget;
    }

    public void setPingTarget(PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }

    public List<PingTarget> getCommonPingTargets() {
        return commonPingTargets;
    }

    public void setCommonPingTargets(List<PingTarget> commonPingTargets) {
        this.commonPingTargets = commonPingTargets;
    }

    public Map getPingStatus() {
        return pingStatus;
    }

    public void setPingStatus(Map pingStatus) {
        this.pingStatus = pingStatus;
    }
}
