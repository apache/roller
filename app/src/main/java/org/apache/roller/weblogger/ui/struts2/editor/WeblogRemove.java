/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.convention.annotation.AllowedMethods;


/**
 * Action for removing a weblog.
 */
// TODO: make this work @AllowedMethods({"execute","remove"})
public class WeblogRemove extends UIAction {

    private static Log log = LogFactory.getLog(WeblogRemove.class);

    public WeblogRemove() {
        this.actionName = "weblogRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "websiteRemove.title";
    }

    /**
     * Show weblog remove confirmation.
     */
    @Override
    public String execute() {
        return "confirm";
    }

    /**
     * Remove a weblog.
     */
    public String remove() {

        try {

            // remove website
            WebloggerFactory.getWeblogger().getWeblogManager().removeWeblog(getActionWeblog());
            WebloggerFactory.getWeblogger().flush();

            CacheManager.invalidate(getActionWeblog());

            addMessage("websiteRemove.success", getActionWeblog().getName());

            return SUCCESS;
        } catch (Exception ex) {
            log.error("Error removing weblog - " + getActionWeblog().getHandle(), ex);
            addError("websiteRemove.error", getActionWeblog().getName());
        }

        return "confirm";
    }
}
