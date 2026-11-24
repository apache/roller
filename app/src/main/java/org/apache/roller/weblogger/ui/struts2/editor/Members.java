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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.struts2.action.ParametersAware;
import org.apache.struts2.dispatcher.HttpParameters;


/**
 * Allows weblog admin to list/modify member permissions.
 *
 * TODO: fix bug in UserManager which doesn't remove permissions from the
 * website.permissions collection when a permission is deleted.
 */
// TODO: make this work @AllowedMethods({"execute","save"})
public class Members extends UIAction implements ParametersAware {
    
    private static final Log log = LogFactory.getLog(Members.class);
    
    // raw parameters from request
    private HttpParameters parameters = HttpParameters.create().build();

    @Override
    public void withParameters(HttpParameters parameters) {
        this.parameters = parameters;
    }
    
    
    public Members() {
        log.debug("Instantiating members action");

        this.actionName = "members";
        this.desiredMenu = "editor";
        this.pageTitle = "memberPermissions.title";
    }

    @Override
    public void myPrepare() {
    }

    @Override
    public String execute() {
        log.debug("Showing weblog members page");
        return LIST;
    }
    
    
    public String save() {
        
        log.debug("Attempting to process weblog permissions updates");
        int numAdmins = 0; // make sure at least one admin
        int removed = 0;
        int changed = 0;
        List<WeblogPermission> permsList = new ArrayList<>();
        try {
            UserManager userMgr = WebloggerFactory.getWeblogger().getUserManager();   
            List<WeblogPermission> permsFromDB = userMgr.getWeblogPermissionsIncludingPending(getActionWeblog());

            // we have to copy the permissions list so that when we remove permissions
            // below we don't get ConcurrentModificationExceptions
            for (WeblogPermission perm : permsFromDB) {
                permsList.add(perm);
            }

            /* Check to see at least one admin would remain defined as a result of the save.
             * Not normally a problem, as only a blog admin can access this page and admins can't
             * demote themselves. However, the blog server admin can always access this page and
             * remove everyone even if not a member of the blog, causing orphan blogs unless this
             * check is in place.
             *
             * Also checking here to make sure an Admin is not demoting or removing himself.
             */
            User user = getAuthenticatedUser();
            boolean error = false;
            for (WeblogPermission perms : permsList) {
                // each row is keyed by its permission, so a disabled member's
                // row can be changed too
                String sval = getParameter("perm-" + perms.getId());
                if (sval != null) {
                    if (sval.equals(WeblogPermission.ADMIN) && !perms.isPending()) {
                        numAdmins++;
                    }
                    if (perms.getUserName().equals(user.getUserName())) {
                        // can't modify self
                        if (!sval.equals(WeblogPermission.ADMIN)) {
                            error = true;
                            addError("memberPermissions.noSelfModifications");
                        }
                    }
                }
            }
            if (numAdmins == 0) {
                addError("memberPermissions.oneAdminRequired");
                error = true;
            }
            if (!error && isAdminMembershipLocked()) {
                for (WeblogPermission perms : permsList) {
                    String sval = getParameter("perm-" + perms.getUser().getId());
                    if (sval != null && !perms.hasAction(sval)
                            && (perms.hasAction(WeblogPermission.ADMIN)
                                || Utilities.stringToStringList(sval, ",").contains(WeblogPermission.ADMIN))) {
                        addError("memberPermissions.frontPageAdminsGlobalOnly");
                        error = true;
                        break;
                    }
                }
            }
            // one iteration for each line (user) in the members table
            Map<String, List<String>> changes = new LinkedHashMap<>();
            for (WeblogPermission perms : permsList) {

                String sval = getParameter("perm-" + perms.getId());
                if (sval != null) {
                    if (!error && !perms.hasAction(sval)) {
                        if ("-1".equals(sval)) {
                            changes.put(perms.getUserName(), null);
                            removed++;
                        } else {
                            changes.put(perms.getUserName(), Utilities.stringToStringList(sval, ","));
                            changed++;
                        }
                    }
                }
            }

            // the admin check is repeated with the changes, as one operation,
            // so concurrent membership changes can't leave no admin
            if (!changes.isEmpty()) {
                if (userMgr.updateWeblogMembership(getActionWeblog(), changes)) {
                    log.debug("Weblog permissions updated, flushing changes");
                    WebloggerFactory.getWeblogger().flush();
                } else {
                    addError("memberPermissions.oneAdminRequired");
                    removed = 0;
                    changed = 0;
                }
            }

        } catch (Exception ex) {
            log.error("Error saving permissions on weblog - "+getActionWeblog().getHandle(), ex);
            addError("memberPermissions.saveError");
            // nothing was saved
            removed = 0;
            changed = 0;
        }
        
        if (removed > 0) {
            addMessage("memberPermissions.membersRemoved", Integer.toString(removed));
        }
        if (changed > 0) {
            addMessage("memberPermissions.membersChanged", Integer.toString(changed));
        }
        
        return LIST;
    }
    
    
    // convenience for accessing a single parameter with a single value
    public String getParameter(String key) {
        return parameters.get(key).getValue();
    }
    
    
    public HttpParameters getParameters() {
        return parameters;
    }
    
    public List<WeblogPermission> getWeblogPermissions() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getWeblogPermissionsIncludingPending(getActionWeblog());
        } catch (WebloggerException ex) {
            // serious problem, but not much we can do here
            log.error("ERROR getting weblog permissions", ex);
        }
        return new ArrayList<>();
    }
}
