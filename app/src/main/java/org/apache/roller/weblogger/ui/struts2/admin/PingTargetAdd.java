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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.ui.struts2.common.PingTargetFormBean;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Add or modify a common ping target.
 */
public class PingTargetAdd extends UIAction {
    private static Log log = LogFactory.getLog(PingTargetAdd.class);

    // a bean for managing submitted data
    private PingTargetFormBean bean = new PingTargetFormBean();

    public PingTargetAdd() {
        this.desiredMenu = "admin";
        this.pageTitle = "pingTarget.pingTarget";
    }

    public String execute() {
        return INPUT;
    }

    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }

    public String save() {
        PingTarget pingTarget = new PingTarget(null, getBean().getName(),
                getBean().getPingUrl(), false);

        myValidate(pingTarget);

        if(!hasActionErrors()) {
            try {
                // Appears to be ok.  Save it and flush.
                PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
                pingTargetMgr.savePingTarget(pingTarget);
                WebloggerFactory.getWeblogger().flush();
                addMessage("pingTarget.saved");
                return SUCCESS;
            } catch (WebloggerException ex) {
                log.error("Error adding ping target", ex);
                addError("pingTarget.saved.error");
            }
        }
        return INPUT;
    }

    // no weblog required
    public boolean isWeblogRequired() {
        return false;
    }
    
    /**
     * Private helper to validate a ping target.
     */
    private void myValidate(PingTarget pingTarget) {

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
            log.error("Error validating ping target", ex);
            addError("generic.error.check.logs");
        }
    }

    public PingTargetFormBean getBean() {
        return bean;
    }

    public void setBean(PingTargetFormBean bean) {
        this.bean = bean;
    }
}
