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
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.PingConfig;
import org.apache.roller.weblogger.business.pings.AutoPingManager;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.AutoPing;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.business.pings.WeblogUpdatePinger;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.xmlrpc.XmlRpcException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Actions for setting up automatic ping configuration for a weblog.
 */
// TODO: make this work @AllowedMethods({"execute","enable","disable","pingNow"})
public class Pings extends UIAction {
    
    private static Log log = LogFactory.getLog(Pings.class);
    
    // ping target id to work on
    private String pingTargetId = null;
    
    // ping target object we are working on, if available
    private PingTarget pingTarget = null;
    
    // commong ping targets list
    private List<PingTarget> commonPingTargets = Collections.emptyList();
    
    // track the enabled/disabled status for pings
    private Map pingStatus = Collections.EMPTY_MAP;
    
    
    public Pings() {
        this.actionName = "pings";
        this.desiredMenu = "editor";
        this.pageTitle = "pings.title";
    }
    
    
    // admin perms required
    public String requireWeblogPermissions() {
        return WeblogPermission.ADMIN;
    }
    
    
    @Override
    public void myPrepare() {
        
        PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        
        // load selected ping target, if possible
        if(getPingTargetId() != null) {
            try {
                setPingTarget(pingTargetMgr.getPingTarget(getPingTargetId()));
            } catch (WebloggerException ex) {
                log.error("Error looking up ping target - "+getPingTargetId(), ex);
            }
        }
        
        try {
            // load common ping targets list
            setCommonPingTargets(pingTargetMgr.getCommonPingTargets());
        } catch (WebloggerException ex) {
            log.error("Error loading ping target lists for weblog - "+getActionWeblog().getHandle(), ex);
            addError("Error loading ping targets");
        }
    }
    
    
    /*
     * Display the common ping targets with page
     */
    @Override
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
                AutoPingManager autoPingMgr = WebloggerFactory.getWeblogger().getAutopingManager();
                AutoPing autoPing = new AutoPing(null, getPingTarget(), getActionWeblog());
                autoPingMgr.saveAutoPing(autoPing);
                WebloggerFactory.getWeblogger().flush();
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
                AutoPingManager autoPingMgr = WebloggerFactory.getWeblogger().getAutopingManager();
                autoPingMgr.removeAutoPing(getPingTarget(), getActionWeblog());
                WebloggerFactory.getWeblogger().flush();
            } catch (Exception ex) {
                log.error("Error removing auto ping for target - "+getPingTargetId(), ex);
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
                if (PingConfig.getSuspendPingProcessing()) {
                    log.debug("Ping processing is disabled.");
                    addError("ping.pingProcessingIsSuspended");
                } else {
                    WeblogUpdatePinger.PingResult pingResult = WeblogUpdatePinger.sendPing(getPingTarget(), getActionWeblog());
                    if (pingResult.isError()) {
                        log.debug("Ping Result: " + pingResult);
                        if (pingResult.getMessage() != null && pingResult.getMessage().trim().length() > 0) {
                            addError("ping.transmittedButError");
                            addError(pingResult.getMessage());
                        } else {
                            addError("ping.transmissionFailed");
                        }
                    } else {
                        addMessage("ping.successful");
                    }
                }
            } catch (IOException ex) {
                log.debug(ex);
                addError("ping.transmissionFailed");
                addSpecificMessages(ex);
            } catch (XmlRpcException ex) {
                log.debug(ex);
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
        
        AutoPingManager autoPingMgr = WebloggerFactory.getWeblogger().getAutopingManager();
        
        // Build isEnabled map (keyed by ping target id and values Boolean.TRUE/Boolean.FALSE)
        Map<String, Boolean> isEnabled = new HashMap<String, Boolean>();
        
        List<AutoPing> autoPings = Collections.emptyList();
        try {
            autoPings = autoPingMgr.getAutoPingsByWebsite(getActionWeblog());
        } catch (WebloggerException ex) {
            log.error("Error looking up auto pings for weblog - "+getActionWeblog().getHandle(), ex);
        }
        
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
