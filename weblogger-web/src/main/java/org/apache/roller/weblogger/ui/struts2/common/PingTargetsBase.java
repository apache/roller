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

package org.apache.roller.weblogger.ui.struts2.common;

import java.util.Collections;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Base implementation for action that lists available ping targets and can
 * handle deletion of a single ping target (with confirmation).
 */
public abstract class PingTargetsBase extends UIAction {
    
    // list of available ping targets
    private List pingTargets = Collections.EMPTY_LIST;
    
    // ping target we are working on, if any
    private PingTarget pingTarget = null;
    
    // id of the ping target to work on
    private String pingTargetId = null;
    
    
    // get logger
    protected abstract Log getLogger();
    
    // load up list of ping targets
    protected abstract void loadPingTargets();
    
    
    // prepare method needs to set ping targets list
    public void myPrepare() {
        
        // load list of ping targets
        loadPingTargets();
        
        // load specified ping target if possible
        if(!StringUtils.isEmpty(getPingTargetId())) {
            try {
                PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
                setPingTarget(pingTargetMgr.getPingTarget(getPingTargetId()));
            } catch (WebloggerException ex) {
                getLogger().error("Error looking up ping target - "+getPingTargetId(), ex);
            }
        }
    }
    
    
    /**
     * Display the ping targets.
     */
    public String execute() {
        return LIST;
    }

    
    /**
     * Delete a ping target (load delete confirmation view).
     */
    public String deleteConfirm() {
        
        if(getPingTarget() != null) {
            setPageTitle("pingTarget.confirmRemoveTitle");
            
            return "confirm";
        } else {
            addError("pingTarget.notFound",getPingTargetId());
        }
        
        return LIST;
    }
    
    
    /**
     * Delete a ping target.
     */
    public String delete() {
        
        if(getPingTarget() != null) {
            
            try {
                PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
                pingTargetMgr.removePingTarget(getPingTarget());
                WebloggerFactory.getWeblogger().flush();
                
                // remove deleted target from list
                getPingTargets().remove(getPingTarget());
                
                addMessage("pingTarget.successfullyDeleted", getPingTarget().getName());
                
            } catch (WebloggerException ex) {
                getLogger().error("Error deleting ping target - "+getPingTargetId(), ex);
                addError("pingTarget.errorDeleting", getPingTargetId());
            }
        } else {
            addError("pingTarget.notFound", getPingTargetId());
        }
        
        return LIST;
    }
    
    
    public List getPingTargets() {
        return pingTargets;
    }

    public void setPingTargets(List pingTargets) {
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
