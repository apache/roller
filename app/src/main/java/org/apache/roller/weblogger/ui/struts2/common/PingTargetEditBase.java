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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Base implementation for action that can edit an existing ping target.
 */
public abstract class PingTargetEditBase extends UIAction {
    
    // ping target we are working on, if any
    private PingTarget pingTarget = null;
    
    // a bean for managing submitted data
    private PingTargetFormBean bean = new PingTargetFormBean();
    
    
    // get logger
    protected abstract Log getLogger();
    
    
    /**
     * Prepare action by loading ping target to work on.
     */
    public void myPrepare() {
        
        PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
        if(!StringUtils.isEmpty(getBean().getId())) {
            
            try {
                setPingTarget(pingTargetMgr.getPingTarget(getBean().getId()));
            } catch (WebloggerException ex) {
                getLogger().error("Error looking up ping target - "+getBean().getId());
            }
            
            if(getPingTarget() == null) {
                addError("pingTarget.notFound", getBean().getId());
            }
            
        } else {
            addError("pingTarget.unspecified");
        }
    }
    
    
    public String execute() {
        
        if(!hasActionErrors()) {
            // load bean with data from ping target
            getBean().copyFrom(getPingTarget());
        } else {
            // if we already have an error then that means we couldn't load
            // an existing ping target to work on, so return ERROR result
            return ERROR;
        }
        
        return INPUT;
    }
    
    
    /**
     * Save ping target.
     */
    public String save() {
        
        if(hasActionErrors()) {
            // if we already have an error then that means we couldn't load
            // an existing ping target to work on, so return ERROR result
            return INPUT;
        }
        
        // copy data from form into ping target
        getBean().copyTo(getPingTarget());
        
        // Call private helper to validate ping target
        // If there are errors, go back to the target edit page.
        myValidate(getPingTarget());
        
        if (!hasActionErrors()) {
            try {
                // Appears to be ok.  Save it and flush.
                PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
                pingTargetMgr.savePingTarget(getPingTarget());
                WebloggerFactory.getWeblogger().flush();

                addMessage("pingTarget.saved");
            } catch (WebloggerException ex) {
                getLogger().error("Error saving ping target", ex);
                // TODO: i18n
                addError("Error saving ping target.");
            }
        }
        
        return INPUT;
    }
    
    
    /**
     * Private helper to validate a ping target.
     */
    protected void myValidate(PingTarget pingTarget) {
        
        try {
            PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
            if (!pingTargetMgr.isNameUnique(pingTarget)) {
                addError("pingTarget.nameNotUnique");
            }
            
            if (!pingTargetMgr.isUrlWellFormed(pingTarget)) {
                addError("pingTarget.malformedUrl");
            } else if (!pingTargetMgr.isHostnameKnown(pingTarget)) {
                addError("pingTarget.unknownHost");
            }
        } catch (WebloggerException ex) {
            getLogger().error("Error validating ping target", ex);
            // TODO: i18n
            addError("Error doing ping target validation");
        }
    }
    
    
    public PingTarget getPingTarget() {
        return pingTarget;
    }

    public void setPingTarget(PingTarget pingTarget) {
        this.pingTarget = pingTarget;
    }
    
    public PingTargetFormBean getBean() {
        return bean;
    }

    public void setBean(PingTargetFormBean bean) {
        this.bean = bean;
    }
    
}
