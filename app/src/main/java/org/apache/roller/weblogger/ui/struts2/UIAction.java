/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.ui.struts2;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.apache.roller.weblogger.ui.core.menu.Menu;
import org.apache.roller.weblogger.ui.core.menu.MenuHelper;

/**
 * Extends the Struts2 ActionSupport class to add in support for handling an
 * error and status success.  Other actions extending this one only need to
 * call setError() and setSuccess() accordingly.
 * <p>
 */
public class UIAction extends ActionSupport {

    private MenuHelper menuHelper;

    public void setMenuHelper(MenuHelper menuHelper) {
        this.menuHelper = menuHelper;
    }

    // the authenticated user accessing this action, or null if client is not logged in
    private User authenticatedUser = null;

    // the weblog this action is intended to work on, or null if no weblog specified
    private Weblog actionWeblog = null;

    // the role the user has with this weblog
    private WeblogRole actionWeblogRole = null;

    // the weblog id of the action weblog
    private String weblogId = null;

    // action name (used by tabbed menu utility)
    private String actionName = null;

    // page title, called by some Tiles JSPs (e.g., tiles-simplepage.jsp)
    private String pageTitle = null;

    // the required minimum global role the user must have for the action to be allowed
    private GlobalRole requiredGlobalRole = GlobalRole.ADMIN;

    // the required minimum weblog role
    private WeblogRole requiredWeblogRole = WeblogRole.OWNER;

    public GlobalRole getRequiredGlobalRole() {
        return requiredGlobalRole;
    }

    public void setRequiredGlobalRole(GlobalRole requiredGlobalRole) {
        this.requiredGlobalRole = requiredGlobalRole;
    }

    public WeblogRole getRequiredWeblogRole() {
        return requiredWeblogRole;
    }

    public void setRequiredWeblogRole(WeblogRole requiredWeblogRole) {
        this.requiredWeblogRole = requiredWeblogRole;
    }

    public boolean isUserIsAdmin() {
        return getAuthenticatedUser() != null && GlobalRole.ADMIN.equals(getAuthenticatedUser().getGlobalRole());
    }

    public WebloggerStaticConfig.AuthMethod getAuthenticationMethod() {
        return WebloggerStaticConfig.getAuthMethod();
    }

    public String getSiteURL() {
        return WebloggerStaticConfig.getRelativeContextURL();
    }

    public WebloggerProperties.RegistrationPolicy getRegistrationPolicy() {
        return WebloggerContext.getWebloggerProperties().getRegistrationPolicy();
    }

    public User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public void setAuthenticatedUser(User authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public Weblog getActionWeblog() {
        return actionWeblog;
    }

    public void setActionWeblog(Weblog workingWeblog) {
        this.actionWeblog = workingWeblog;
    }

    public WeblogRole getActionWeblogRole() {
        return actionWeblogRole;
    }

    public void setActionWeblogRole(WeblogRole actionWeblogRole) {
        this.actionWeblogRole = actionWeblogRole;
    }

    public String getWeblogId() {
        return weblogId;
    }

    public void setWeblogId(String weblogId) {
        this.weblogId = weblogId;
    }

    public String getPageTitle() {
        return getText(pageTitle);
    }

    public void setPageTitle(String pageTitle) {
        // disabled by default as it causes page titles not
        // to update on chain actions defined in struts.xml
        // use setPageTitleReal where you want this to occur.
    }

    public void setPageTitleReal(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public String getActionName() {
        return this.actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Menu getMenu() {
        return menuHelper.getMenu(getAuthenticatedUser().getGlobalRole(), getActionWeblogRole(), getActionName(),
                true);
    }
}
