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

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElements;
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

    @XmlAttribute
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @XmlElements(@XmlElement(name = "tab"))
    public List<ParsedTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<ParsedTab> tabs) {
        this.tabs = tabs;
    }

    /**
     * A parsed "tab" from an xml defined menu config.
     */
    protected static class ParsedTab {
        private String titleKey;
        private GlobalRole requiredGlobalRole;
        private WeblogRole requiredWeblogRole;
        private String enabledProperty;
        private List<ParsedTabItem> tabItems = new ArrayList<>();

        public void addItem(ParsedTabItem item) {
            this.tabItems.add(item);
        }

        @XmlAttribute
        public String getTitleKey() {
            return titleKey;
        }

        public void setTitleKey(String titleKey) {
            this.titleKey = titleKey;
        }

        @XmlAttribute(name = "globalRole")
        public GlobalRole getRequiredGlobalRole() {
            return requiredGlobalRole;
        }

        public void setRequiredGlobalRole(GlobalRole role) {
            this.requiredGlobalRole = role;
        }

        @XmlAttribute(name = "weblogRole")
        public WeblogRole getRequiredWeblogRole() {
            return requiredWeblogRole;
        }

        public void setRequiredWeblogRole(WeblogRole role) {
            this.requiredWeblogRole = role;
        }

        @XmlAttribute
        public String getEnabledProperty() {
            return enabledProperty;
        }

        public void setEnabledProperty(String enabledProperty) {
            this.enabledProperty = enabledProperty;
        }

        @XmlElements(@XmlElement(name = "tabItem"))
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
    protected static class ParsedTabItem {
        private String titleKey;
        private String action;
        private String actionPath;
        private Set<String> subActions;
        private GlobalRole requiredGlobalRole;
        private WeblogRole requiredWeblogRole;
        private String enabledProperty;

        @XmlAttribute
        public String getTitleKey() {
            return titleKey;
        }

        public void setTitleKey(String titleKey) {
            this.titleKey = titleKey;
        }

        @XmlAttribute
        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        @XmlAttribute
        public String getActionPath() {
            return actionPath;
        }

        public void setActionPath(String actionPath) {
            this.actionPath = actionPath;
        }

        @XmlElementWrapper(name = "subactions")
        @XmlElement(name = "subaction")
        public Set<String> getSubActions() {
            return subActions;
        }

        public void setSubActions(Set<String> subActions) {
            this.subActions = subActions;
        }

        @XmlAttribute(name = "globalRole")
        public GlobalRole getRequiredGlobalRole() {
            return requiredGlobalRole;
        }

        public void setRequiredGlobalRole(GlobalRole requiredGlobalRole) {
            this.requiredGlobalRole = requiredGlobalRole;
        }

        @XmlAttribute(name = "weblogRole")
        public WeblogRole getRequiredWeblogRole() {
            return requiredWeblogRole;
        }

        public void setRequiredWeblogRole(WeblogRole requiredWeblogRole) {
            this.requiredWeblogRole = requiredWeblogRole;
        }

        @XmlAttribute
        public String getEnabledProperty() {
            return enabledProperty;
        }

        public void setEnabledProperty(String enabledProperty) {
            this.enabledProperty = enabledProperty;
        }

    }
}
