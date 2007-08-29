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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Base permission class for Roller. 
 */
public abstract class ObjectPermission extends RollerPermission {
    private static Log log = LogFactory.getLog(ObjectPermission.class);

    protected String       id = UUIDGenerator.generateUUID();;
    protected String       userName;
    protected String       actions;
    protected String       objectType;
    protected String       objectId;
    protected Date         dateCreated = new Date();
    
    
    public ObjectPermission() {
        super("");
    }
    
    public ObjectPermission(String actions) {
        super(actions);
        this.actions = actions;
    }
    
    public boolean hasAction(String action) {
        List<String> actionList = getActionsAsList();
        return actionList.contains(action);
    }
    
    /**
     * Merge actions into this permission.
     */
    public void addActions(ObjectPermission perm) {
        List<String> newActions = perm.getActionsAsList();
        List<String> updatedActions = getActionsAsList();
        for (String newAction : newActions) {
            if (!updatedActions.contains(newAction)) {
                updatedActions.add(newAction);
            }
        }
        setActionsAsList(updatedActions);
    }
    
    /**
     * Merge actions into this permission.
     */
    public void removeActions(ObjectPermission perm) {
        List<String> actionsToRemove = perm.getActionsAsList();
        List<String> updatedActions = getActionsAsList();
        for (String actionToRemove : actionsToRemove) {
            updatedActions.remove(actionToRemove);
        }
        log.debug("updatedActions2: " + updatedActions);
        setActionsAsList(updatedActions);
    }
    
    /**
     * True if permission specifies no actions
     */
    public boolean isEmpty() {
        if (actions == null || actions.trim().length() == 0) {
            return true;
        }
        return false;
    }
    
    public boolean implies(Permission perm) {
        return false;
    }

    public String getActions() {
        return actions;
    }
    
    public List<String> getActionsAsList() {
        List<String> list = new ArrayList<String>();
        List<String> rolist = Arrays.asList(Utilities.stringToStringArray(getActions(), ","));
        list.addAll(rolist);
        return list;
    }
    
    public void setActionsAsList(List<String> actionsList) {
        if (actionsList.size() == 0) {
            setActions("");
        }
        setActions(Utilities.stringArrayToString(actionsList.toArray(new String[0]), ","));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String username) {
        this.userName = username;
    }

    public void setActions(String actions) {
        this.actions = actions;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

}
