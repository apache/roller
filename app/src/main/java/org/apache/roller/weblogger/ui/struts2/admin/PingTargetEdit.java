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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.pings.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.convention.annotation.AllowedMethods;

/**
 * Add or modify a common ping target.
 */
// TODO: make this work @AllowedMethods({"execute","save"})
public class PingTargetEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(PingTargetEdit.class);

    // a bean for managing submitted data
    private PingTargetBean bean = new PingTargetBean();

    // ping target we are working on, if any
    private PingTarget pingTarget = null;

    public PingTargetEdit() {
        this.desiredMenu = "admin";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    // no weblog required
    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public void myPrepare() {
        PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();

        if (!StringUtils.isEmpty(getBean().getId())) {
            // edit case
            try {
                pingTarget = pingTargetMgr.getPingTarget(getBean().getId());
            } catch (WebloggerException ex) {
                log.error("Error looking up ping target - " + getBean().getId());
            }
            if (pingTarget == null) {
                addError("pingTarget.notFound", getBean().getId());
            }
        } else {
            // add case
            pingTarget = new PingTarget();
            pingTarget.setConditionCode(PingTarget.CONDITION_OK);
            pingTarget.setAutoEnabled(false);
        }
    }

    /**
     * Save ping target.
     */
    @Override
    public String execute() {
        myValidate();

        if (!hasActionErrors()) {
            try {
                // copy data from form into ping target
                getBean().copyTo(pingTarget);
                PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
                pingTargetMgr.savePingTarget(pingTarget);
                WebloggerFactory.getWeblogger().flush();

                addMessage(isAdd() ? "pingTarget.created" : "pingTarget.updated", pingTarget.getName());

                return SUCCESS;
            } catch (WebloggerException ex) {
                log.error("Error adding/editing ping target", ex);
                addError("generic.error.check.logs");
            }
        }

        return INPUT;
    }

    /**
     * Private helper to validate a ping target.
     */
    protected void myValidate() {
        try {
            PingTargetManager pingTargetMgr = WebloggerFactory.getWeblogger().getPingTargetManager();
            if (StringUtils.isEmpty(bean.getName())) {
                addError("pingTarget.nameMissing");
            } else {
                if (isAdd() || !pingTarget.getName().equals(bean.getName())) {
                    if (pingTargetMgr.targetNameExists(bean.getName())) {
                        addError("pingTarget.nameNotUnique");
                    }
                }
            }
            if (StringUtils.isEmpty(bean.getPingUrl())) {
                addError("pingTarget.pingUrlMissing");
            } else {
                if (!pingTargetMgr.isUrlWellFormed(bean.getPingUrl())) {
                    addError("pingTarget.malformedUrl");
                } else if (!pingTargetMgr.isHostnameKnown(bean.getPingUrl())) {
                    addError("pingTarget.unknownHost");
                }
            }
        } catch (WebloggerException ex) {
            log.error("Error validating ping target", ex);
            addError("generic.error.check.logs");
        }
    }

    private boolean isAdd() {
        return actionName.equals("commonPingTargetAdd");
    }

    public PingTargetBean getBean() {
        return bean;
    }

    public void setBean(PingTargetBean bean) {
        this.bean = bean;
    }
}
