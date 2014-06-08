/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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

import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Action for resigning from a weblog.
 */
public class MemberResign extends UIAction {

    private static Log log = LogFactory.getLog(MemberResign.class);

    public MemberResign() {
        this.actionName = "memberResign";
        this.desiredMenu = "editor";
        this.pageTitle = "yourWebsites.resign";
    }

    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.EDIT_DRAFT);
    }

    public boolean isWeblogRequired() {
        return false;
    }

    /**
     * Show member resign confirmation
     */
    public String execute() {
        return INPUT;
    }

    /**
     * Resign from a weblog
     */
    public String resign() {
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            // TODO: notify website members that user has resigned
            // TODO EXCEPTIONS: better exception handling
            umgr.revokeWeblogPermission(getActionWeblog(), getAuthenticatedUser(), WeblogPermission.ALL_ACTIONS);
            WebloggerFactory.getWeblogger().flush();
            addMessage("yourWebsites.resigned", getWeblog());
        } catch (WebloggerException ex) {
            log.error("Error doing weblog resign - " + getActionWeblog().getHandle(), ex);
            addError("Resignation failed - check system logs");
        }
        return SUCCESS;
    }
}
