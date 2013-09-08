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

package org.apache.roller.weblogger.pojos; 

import java.io.Serializable;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;


/**
 * Permission for one specific weblog
 * @ejb:bean name="WeblogPermission"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true" table="roller_user_permissions"
 * @hibernate.cache usage="read-write"
 *
 * @author Dave Johnson
 */
public class WeblogPermission extends ObjectPermission implements Serializable {
    public static final String EDIT_DRAFT = "edit_draft";
    public static final String POST = "post";
    public static final String ADMIN = "admin";
    public static final List<String> ALL_ACTIONS = new ArrayList<String>();
    
    static {
        ALL_ACTIONS.add(EDIT_DRAFT);
        ALL_ACTIONS.add(POST);
        ALL_ACTIONS.add(ADMIN);
    }

    public WeblogPermission() {
        // required by JPA
    }

    public WeblogPermission(Weblog weblog, User user, String actions) {
        super("WeblogPermission user: " + user.getUserName());
        setActions(actions);
        objectType = "Weblog";
        objectId = weblog.getHandle();
        userName = user.getUserName();
    }
    
    public WeblogPermission(Weblog weblog, User user, List<String> actions) {
        super("WeblogPermission user: " + user.getUserName());
        setActionsAsList(actions); 
        objectType = "Weblog";
        objectId = weblog.getHandle();
        userName = user.getUserName();
    }
    
    public WeblogPermission(Weblog weblog, List<String> actions) {
        super("WeblogPermission user: N/A");
        setActionsAsList(actions); 
        objectType = "Weblog";
        objectId = weblog.getHandle();
    }
    
    public Weblog getWeblog() throws WebloggerException {
        if (objectId != null) {
            return WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(objectId, null);
        }
        return null;
    }

    public User getUser() throws WebloggerException {
        if (userName != null) {
            return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(userName);
        }
        return null;
    }

    public boolean implies(Permission perm) {
        if (perm instanceof WeblogPermission) {
            WeblogPermission rperm = (WeblogPermission)perm;
            
            if (hasAction(ADMIN)) {
                // admin implies all other permissions
                return true;
            } else if (hasAction(POST)) {
                // Best we've got is POST, so make sure perm doesn't specify ADMIN
                for (String action : rperm.getActionsAsList()) {
                    if (action.equals(ADMIN)) {
                        return false;
                    }
                }
            } else if (hasAction(EDIT_DRAFT)) {
                // Best we've got is EDIT_DRAFT, so make sure perm doesn't specify anything else
                for (String action : rperm.getActionsAsList()) {
                    if (action.equals(POST)) {
                        return false;
                    }
                    if (action.equals(ADMIN)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("GlobalPermission: ");
        for (String action : getActionsAsList()) { 
            sb.append(" ").append(action).append(" ");
        }
        return sb.toString();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogPermission)) {
            return false;
        }
        WeblogPermission o = (WeblogPermission)other;
        return new EqualsBuilder()
                .append(getUserName(), o.getUserName())
                .append(getObjectId(), o.getObjectId())
                .append(getActions(), o.getActions())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getUserName())
                .append(getObjectId())
                .append(getActions())
                .toHashCode();
    }
}




