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
import org.apache.xmlrpc.XmlRpcException;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Actions for setting up automatic ping configuration for a weblog.
 */
public class Pings extends UIAction {
    
    private static Log log = LogFactory.getLog(Pings.class);
    
    // ping target id to work on
    private String pingTargetId = null;
    
    // ping target object we are working on, if available
    private PingTarget pingTarget = null;
    
    // commong ping targets list
    private List commonPingTargets = Collections.EMPTY_LIST;
    
    // custom ping targets list for weblog
    private List customPingTargets = Collections.EMPTY_LIST;
    
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
            
            // load custom ping targets list for weblog, if applicable
            if(!PingConfig.getDisallowCustomTargets()) {
                setCustomPingTargets(pingTargetMgr.getCustomPingTargets(getActionWeblog()));
            }
            
        } catch (WebloggerException ex) {
            log.error("Error loading ping target lists for weblog - "+getActionWeblog().getHandle(), ex);
            // TODO: i18n
            addError("Error loading ping targets");
        }
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
                AutoPingManager autoPingMgr = WebloggerFactory.getWeblogger().getAutopingManager();
                AutoPing autoPing = new AutoPing(null, getPingTarget(), getActionWeblog());
                autoPingMgr.saveAutoPing(autoPing);
                WebloggerFactory.getWeblogger().flush();
            } catch(Exception ex) {
                log.error("Error saving auto ping for target - "+getPingTargetId(), ex);
                // TODO: i18n
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
                // TODO: i18n
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
        Map isEnabled = new HashMap();
        
        List autopings = Collections.EMPTY_LIST;
        try {
            autopings = autoPingMgr.getAutoPingsByWebsite(getActionWeblog());
        } catch (WebloggerException ex) {
            log.error("Error looking up auto pings for weblog - "+getActionWeblog().getHandle(), ex);
        }
        
        // Add the enabled auto ping configs with TRUE
        for (Iterator i = autopings.iterator(); i.hasNext();) {
            AutoPing autoPing = (AutoPing) i.next();
            isEnabled.put(autoPing.getPingTarget().getId(), Boolean.TRUE);
        }
        
        // Somewhat awkward, but the two loops save building a separate combined list.
        // Add disabled common ones with FALSE
        for (Iterator i = getCommonPingTargets().iterator(); i.hasNext();) {
            PingTarget inPingTarget = (PingTarget) i.next();
            if (isEnabled.get(inPingTarget.getId()) == null) {
                isEnabled.put(inPingTarget.getId(), Boolean.FALSE);
            }
        }
        
        // Add disabled custom ones with FALSE
        for (Iterator i = getCustomPingTargets().iterator(); i.hasNext();) {
            PingTarget inPingTarget = (PingTarget) i.next();
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

    public List getCommonPingTargets() {
        return commonPingTargets;
    }

    public void setCommonPingTargets(List commonPingTargets) {
        this.commonPingTargets = commonPingTargets;
    }

    public List getCustomPingTargets() {
        return customPingTargets;
    }

    public void setCustomPingTargets(List customPingTargets) {
        this.customPingTargets = customPingTargets;
    }

    public Map getPingStatus() {
        return pingStatus;
    }

    public void setPingStatus(Map pingStatus) {
        this.pingStatus = pingStatus;
    }
}
