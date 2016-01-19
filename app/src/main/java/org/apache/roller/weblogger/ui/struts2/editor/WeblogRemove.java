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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Action for removing a weblog.
 */
public class WeblogRemove extends UIAction {

    private static Log log = LogFactory.getLog(WeblogRemove.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    public WeblogRemove() {
        this.actionName = "weblogRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "websiteRemove.title";
    }

    /**
     * Show weblog remove confirmation.
     */
    public String execute() {
        return "confirm";
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    /**
     * Remove a weblog.
     */
    public String remove() {

        try {
            // remove website
            weblogManager.removeWeblog(getActionWeblog());
            persistenceStrategy.flushAndInvalidateWeblog(getActionWeblog());
            addMessage("websiteRemove.success", getActionWeblog().getName());
            return SUCCESS;
        } catch (Exception ex) {
            log.error("Error removing weblog - " + getActionWeblog().getHandle(), ex);
            addError("websiteRemove.error", getActionWeblog().getName());
        }

        return "confirm";
    }
}
