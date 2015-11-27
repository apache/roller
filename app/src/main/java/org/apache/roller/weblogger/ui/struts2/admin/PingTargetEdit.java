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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.PingTargetManager;
import org.apache.roller.weblogger.pojos.PingTarget;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Add or modify a common ping target.
 */
public class PingTargetEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(PingTargetEdit.class);

    private PingTargetManager pingTargetManager;

    public void setPingTargetManager(PingTargetManager pingTargetManager) {
        this.pingTargetManager = pingTargetManager;
    }

    // a bean for managing submitted data
    private PingTarget bean = new PingTarget();

    // ping target we are working on, if any
    private PingTarget pingTarget = null;

    public PingTargetEdit() {
        this.desiredMenu = "admin";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    public void prepare() {
        if (isAdd()) {
            pingTarget = new PingTarget();
            pingTarget.setId(WebloggerCommon.generateUUID());
            pingTarget.setAutoEnabled(false);
        } else {
            // edit case
            try {
                pingTarget = pingTargetManager.getPingTarget(getBean().getId());
            } catch (WebloggerException ex) {
                log.error("Error looking up ping target - " + getBean().getId());
            }
            if (pingTarget == null) {
                addError("pingTarget.notFound", getBean().getId());
            }
        }
    }

    public String execute() {
        if (!isAdd()) {
            bean.setId(pingTarget.getId());
            bean.setName(pingTarget.getName());
            bean.setPingUrl(pingTarget.getPingUrl());
        }
        return INPUT;
    }

    /**
     * Save ping target.
     */
    public String save() {
        myValidate();

        if (!hasActionErrors()) {
            try {
                // copy data from form into ping target
                pingTarget.setName(bean.getName());
                pingTarget.setPingUrl(bean.getPingUrl());

                pingTargetManager.savePingTarget(pingTarget);
                WebloggerFactory.flush();

                addMessage(isAdd() ? "pingTarget.created" : "pingTarget.updated",
                        pingTarget.getName());

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
            if (StringUtils.isEmpty(bean.getName())) {
                addError("pingTarget.nameMissing");
            } else {
                if (isAdd() || !pingTarget.getName().equals(bean.getName())) {
                    if (pingTargetManager.targetNameExists(bean.getName())) {
                        addError("pingTarget.nameNotUnique");
                    }
                }
            }
            if (StringUtils.isEmpty(bean.getPingUrl())) {
                addError("pingTarget.pingUrlMissing");
            } else {
                if (!pingTargetManager.isUrlWellFormed(bean.getPingUrl())) {
                    addError("pingTarget.malformedUrl");
                } else if (!pingTargetManager.isHostnameKnown(bean.getPingUrl())) {
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

    public PingTarget getBean() {
        return bean;
    }

    public void setBean(PingTarget bean) {
        this.bean = bean;
    }
}
