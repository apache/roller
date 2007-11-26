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

package org.apache.roller.weblogger.ui.core.util.menu;

import java.util.List;


/**
 * A parsed "tab-item" from an xml defined menu config.
 */
public class ParsedTabItem {
    
    private String name = null;
    private String action = null;
    private String[] subActions = null;
    private List<String> weblogPermissionActions = null;
    private List<String> globalPermissionActions = null;
    private String enabledProperty = null;
    private String disabledProperty = null;
    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String[] getSubActions() {
        return subActions;
    }

    public void setSubActions(String[] subActions) {
        this.subActions = subActions;
    }

    public List<String> getWeblogPermissionActions() {
        return weblogPermissionActions;
    }

    public void setWeblogPermissionActions(List<String> perms) {
        this.weblogPermissionActions = perms;
    }

    public List<String> getGlobalPermissionActions() {
        return globalPermissionActions;
    }

    public void setGlobalPermissionActions(List<String> perms) {
        this.globalPermissionActions = perms;
    }

    public String getEnabledProperty() {
        return enabledProperty;
    }

    public void setEnabledProperty(String enabledProperty) {
        this.enabledProperty = enabledProperty;
    }

    public String getDisabledProperty() {
        return disabledProperty;
    }

    public void setDisabledProperty(String disabledProperty) {
        this.disabledProperty = disabledProperty;
    }    
}

