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
package org.tightblog.ui.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * The top-level menu for a UI, consisting of MenuTabs and below those
 * MenuTabItems.
 */
public class Menu {
    private List<MenuTab> tabs = new ArrayList<>();

    void addTab(MenuTab tab) {
        this.tabs.add(tab);
    }

    public List<MenuTab> getTabs() {
        return tabs;
    }

    public void setTabs(List<MenuTab> menus) {
        this.tabs = menus;
    }

    public static class MenuTab {
        private String key;
        private boolean selected;
        private List<MenuTabItem> items = new ArrayList<>();

        void addItem(MenuTabItem item) {
            this.items.add(item);
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public List<MenuTabItem> getItems() {
            return items;
        }

        public void setItems(List<MenuTabItem> items) {
            this.items = items;
        }
    }

    public static class MenuTabItem {
        private String key;
        private String action;
        private String actionPath;
        private boolean selected;
        private boolean hasWeblogId;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getAction() {
            return action;
        }

        public void setAction(String url) {
            this.action = url;
        }

        public String getActionPath() {
            return actionPath;
        }

        public void setActionPath(String actionPath) {
            this.actionPath = actionPath;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public boolean getHasWeblogId() {
            return hasWeblogId;
        }

        public void setHasWeblogId(boolean hasWeblogId) {
            this.hasWeblogId = hasWeblogId;
        }

    }
}
