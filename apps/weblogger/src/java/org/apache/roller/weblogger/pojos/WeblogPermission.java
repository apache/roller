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

import java.security.Permission;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;

/**
 * Permission for one specific weblog
 */
public class WeblogPermission extends ObjectPermission { 
    public static final String EDIT_DRAFT = "editdraft";
    public static final String POST = "post";
    public static final String ADMIN = "admin";
    public static final String ALL_ACTIONS = EDIT_DRAFT + "," + POST + "," + ADMIN;

    
    public WeblogPermission(Weblog weblog, User user, String actions) {
        super("WeblogPermission user: " + user.getUserName());
        setActions(actions);
        objectType = "Weblog";
        objectId = weblog.getHandle();
        userName = user.getUserName();
    }
    
    public Weblog getWeblog() throws WebloggerException {
        return WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle((objectId));
    }

    public User getUser() throws WebloggerException {
        return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(userName);
    }

    public boolean equals(Object arg0) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int hashCode() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean implies(Permission perm) {
        if (perm instanceof WeblogPermission) {
            WeblogPermission weblogPerm = (WeblogPermission)perm;
            if (getObjectId().equals(weblogPerm.getObjectId())) {
                if (hasAction(ADMIN)) {
                    return true;
                }
                if (hasAction(POST) && weblogPerm.hasAction(EDIT_DRAFT)) {
                    return true;
                }
            }
        }
        return false;
    }
}




