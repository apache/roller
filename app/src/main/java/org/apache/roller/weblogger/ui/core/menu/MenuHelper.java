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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.jpa.JPAPropertiesManagerImpl;
import org.apache.roller.weblogger.business.RuntimeConfigDefs;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.menu.Menu.MenuTab;
import org.apache.roller.weblogger.ui.core.menu.Menu.MenuTabItem;
import org.apache.roller.weblogger.ui.core.menu.ParsedMenu.ParsedTab;
import org.apache.roller.weblogger.ui.core.menu.ParsedMenu.ParsedTabItem;
import org.apache.roller.weblogger.util.Utilities;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * A helper class for dealing with UI menus.
 */
public final class MenuHelper {
    private static Log log = LogFactory.getLog(MenuHelper.class);
    private static Map<String, ParsedMenu> menuMap = new HashMap<>(2);
    private static Set<String> propertyDefNames = new TreeSet<>();

    private MenuHelper() {}

    private static boolean hasPropertyDef(String propertyName) {
        return propertyDefNames.contains(propertyName);
    }

    @XmlRootElement(name="menus")
    private static class MenuListHolder {
        private List<ParsedMenu> menuList = new ArrayList<>(2);

        @XmlElements(@XmlElement(name="menu"))
        public List<ParsedMenu> getMenuList() {
            return menuList;
        }

        public void setMenuList(List<ParsedMenu> list) {
            this.menuList = list;
        }
    }

    static {
        // parse and cache menus so we can efficiently reuse them
        try {
            MenuListHolder menus = (MenuListHolder) Utilities.jaxbUnmarshall(
                    "/org/apache/roller/weblogger/config/menus.xsd",
                    "/org/apache/roller/weblogger/config/menus.xml",
                    false,
                    MenuListHolder.class);

            for (ParsedMenu menu : menus.getMenuList()) {
                menuMap.put(menu.getId(), menu);
            }
        } catch (Exception ex) {
            log.error("Error parsing menu configs", ex);
        }

        // Cache runtime configurable property names
        RuntimeConfigDefs rcd = JPAPropertiesManagerImpl.getRuntimeConfigDefs();
        if (rcd != null) {
            for (RuntimeConfigDefs.DisplayGroup group : rcd.getDisplayGroups()) {
                for (RuntimeConfigDefs.PropertyDef def : group.getPropertyDefs()) {
                    propertyDefNames.add(def.getName());
                }
            }
        }
    }

    /**
     * Creates and returns a Menu object with tabs and tab items removed and active tab and item
     * highlighted as appropriate for the User, Weblog, and Action being performed.
     * 
     * @param menuId unique identifier for the menu (e.g., "editor", "admin")
     *
     * @param currentAction the current action being invoked. Null to ignore.
     *
     * @param user the User
     *
     * @param weblog Weblog instance User is working with
     *
     * @return Menu object
     */
    public static Menu generateMenu(String menuId, String currentAction, User user, Weblog weblog, WeblogRole weblogRole) {
        Menu menu = null;

        // do we know the specified menu config?
        ParsedMenu menuConfig = menuMap.get(menuId);
        if (menuConfig != null) {
            try {
                menu = buildMenu(menuConfig, currentAction, user, weblog, weblogRole);
            } catch (WebloggerException ex) {
                log.error("ERROR: fetching user roles", ex);
            }
        }
        return menu;
    }

    /**
     * Builds the menu.
     * 
     * @param menuConfig the menu config
     * @param currentAction the current action
     * @param user the user
     * @param weblog the weblog
     * @return the menu
     * @throws WebloggerException the weblogger exception
     */
    private static Menu buildMenu(ParsedMenu menuConfig,
            String currentAction, User user, Weblog weblog, WeblogRole weblogRole)
            throws WebloggerException {

        Menu tabMenu = new Menu();

        // iterate over tabs from parsed config
        for (ParsedTab configTab : menuConfig.getTabs()) {
            // does this tab have an enabledProperty?
            boolean includeTab = true;
            if (configTab.getEnabledProperty() != null) {
                includeTab = getBooleanProperty(configTab.getEnabledProperty());
            } else if (configTab.getDisabledProperty() != null) {
                includeTab = !getBooleanProperty(configTab.getDisabledProperty());
            }

            if (!includeTab
               || (configTab.getRequiredGlobalRole() != null
                    && !user.hasEffectiveGlobalRole(configTab.getRequiredGlobalRole()))
               || (weblog != null && !checkWeblogRole(weblogRole, configTab.getRequiredWeblogRole()))
               ) {
                continue;
            }

            // all checks passed, tab should be included
            MenuTab tab = new MenuTab();
            tab.setKey(configTab.getTitleKey());

            // setup tab items
            boolean firstItem = true;
            boolean selectable = true;

            // now check if each tab item should be included
            for (ParsedTabItem tabItem : configTab.getTabItems()) {
                boolean includeItem = true;

                if (tabItem.getEnabledProperty() != null) {
                    includeItem = getBooleanProperty(tabItem.getEnabledProperty());
                } else if (tabItem.getDisabledProperty() != null) {
                    includeItem = !getBooleanProperty(tabItem.getDisabledProperty());
                }

                // disabled and global role check
                if (!includeItem ||
                        (tabItem.getRequiredGlobalRole() != null
                        && !user.hasEffectiveGlobalRole(tabItem.getRequiredGlobalRole()))) {
                    continue;
                }

                // weblog role check
                if (weblog != null
                        && (tabItem.getRequiredWeblogRole() != null
                            && !checkWeblogRole(weblogRole, tabItem.getRequiredWeblogRole()))) {
                    continue;
                }

                // all checks passed, item should be included
                MenuTabItem newTabItem = new MenuTabItem();
                newTabItem.setKey(tabItem.getTitleKey());
                newTabItem.setAction(tabItem.getAction());

                // is this the selected item? Only one can be selected
                // so skip the rest
                if (currentAction != null && selectable && isSelected(currentAction, tabItem)) {
                    newTabItem.setSelected(true);
                    tab.setSelected(true);
                    selectable = false;
                }

                // the url for the tab is the url of the first tab item
                if (firstItem) {
                    tab.setAction(newTabItem.getAction());
                    firstItem = false;
                }

                // add the item
                tab.addItem(newTabItem);
            }

            // add the tab
            tabMenu.addTab(tab);
        }

        return tabMenu;
    }

    private static boolean checkWeblogRole(WeblogRole wr, WeblogRole requiredRole) {
        return wr != null && wr.hasEffectiveRole(requiredRole);
    }

    /**
     * Check enabled property, prefers runtime properties.
     * 
     * @param propertyName the property name
     * @return the boolean property
     */
    private static boolean getBooleanProperty(String propertyName) {
        if (hasPropertyDef(propertyName)) {
            return WebloggerFactory.getWeblogger().getPropertiesManager().getBooleanProperty(propertyName);
        }
        return WebloggerConfig.getBooleanProperty(propertyName);
    }

    /**
     * Checks if is selected.
     * 
     * @param currentAction the current action
     * @param tabItem the tab item
     * @return true, if is selected
     */
    private static boolean isSelected(String currentAction, ParsedTabItem tabItem) {
        if (currentAction.equals(tabItem.getAction())) {
            return true;
        }
        // an item is also considered selected if it's a subaction of the current action
        Set<String> subActions = tabItem.getSubActions();
        return subActions != null && subActions.contains(currentAction);
    }

}
