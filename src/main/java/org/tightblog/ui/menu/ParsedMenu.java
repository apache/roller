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
package org.tightblog.ui.menu;

import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.WeblogRole;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A parsed xml defined menu.  A menu consists of one or more ParsedTabs,
 * each of which have one or more ParsedTabItems.
 */
public class ParsedMenu {
    private String id;
    private List<ParsedTab> tabs = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ParsedTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<ParsedTab> tabs) {
        this.tabs = tabs;
    }

    /**
     * A parsed "tab" from an xml defined menu config.
     */
    public static class ParsedTab {
        private String titleKey;
        private GlobalRole globalRole;
        private WeblogRole weblogRole;
        private String enabledProperty;
        private List<ParsedTabItem> tabItems = new ArrayList<>();

        public void addItem(ParsedTabItem item) {
            this.tabItems.add(item);
        }

        public String getTitleKey() {
            return titleKey;
        }

        public void setTitleKey(String titleKey) {
            this.titleKey = titleKey;
        }

        public GlobalRole getGlobalRole() {
            return globalRole;
        }

        public void setGlobalRole(GlobalRole role) {
            this.globalRole = role;
        }

        public WeblogRole getWeblogRole() {
            return weblogRole;
        }

        public void setWeblogRole(WeblogRole role) {
            this.weblogRole = role;
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

    }

    /**
     * An individual clickable menu item underneath a ParsedTab.
     */
    public static class ParsedTabItem {
        private String titleKey;
        private String action;
        private String actionPath;
        private Set<String> subActions;
        private GlobalRole globalRole;
        private WeblogRole weblogRole;
        private String enabledProperty;

        public String getTitleKey() {
            return titleKey;
        }

        public void setTitleKey(String titleKey) {
            this.titleKey = titleKey;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public String getActionPath() {
            return actionPath;
        }

        public void setActionPath(String actionPath) {
            this.actionPath = actionPath;
        }

        public Set<String> getSubActions() {
            return subActions;
        }

        public void setSubActions(Set<String> subActions) {
            this.subActions = subActions;
        }

        public GlobalRole getGlobalRole() {
            return globalRole;
        }

        public void setGlobalRole(GlobalRole globalRole) {
            this.globalRole = globalRole;
        }

        public WeblogRole getWeblogRole() {
            return weblogRole;
        }

        public void setWeblogRole(WeblogRole weblogRole) {
            this.weblogRole = weblogRole;
        }

        public String getEnabledProperty() {
            return enabledProperty;
        }

        public void setEnabledProperty(String enabledProperty) {
            this.enabledProperty = enabledProperty;
        }

    }
}
