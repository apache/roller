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

package org.apache.roller.weblogger.ui.core.menu;

import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.WeblogRole;

import java.util.ArrayList;
import java.util.List;


/**
 * A parsed "tab" from an xml defined menu config.
 */
public class ParsedTab {
    
    private String name;
    private GlobalRole requiredGlobalRole;
    private WeblogRole requiredWeblogRole;
    private String enabledProperty;
    private String disabledProperty;
    
    private List<ParsedTabItem> tabItems = new ArrayList<ParsedTabItem>();
    
    public void addItem(ParsedTabItem item) {
        this.tabItems.add(item);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GlobalRole getRequiredGlobalRole() {
        return requiredGlobalRole;
    }

    public void setRequiredGlobalRole(GlobalRole role) {
        this.requiredGlobalRole = role;
    }

    public WeblogRole getRequiredWeblogRole() {
        return requiredWeblogRole;
    }

    public void setRequiredWeblogRole(WeblogRole role) {
        this.requiredWeblogRole = role;
    }

    public String getEnabledProperty() {
        return enabledProperty;
    }

    public void setEnabledProperty(String enabledProperty) {
        this.enabledProperty = enabledProperty;
    }

    public List<ParsedTabItem> getTabItems() {
        return tabItems;
    }

    public void setTabItems(List<ParsedTabItem> tabItems) {
        this.tabItems = tabItems;
    }

    public String getDisabledProperty() {
        return disabledProperty;
    }

    public void setDisabledProperty(String disabledProperty) {
        this.disabledProperty = disabledProperty;
    }
    
}
