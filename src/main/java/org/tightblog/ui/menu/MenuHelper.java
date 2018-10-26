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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.tightblog.pojos.GlobalRole;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.repository.WebloggerPropertiesRepository;
import org.tightblog.ui.menu.Menu.MenuTab;
import org.tightblog.ui.menu.Menu.MenuTabItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A helper class for dealing with UI menus.
 */
@Component
public final class MenuHelper {

    private static Logger log = LoggerFactory.getLogger(MenuHelper.class);

    private static Map<String, ParsedMenu> menuMap = new HashMap<>(2);
    private static Map<String, String> actionToMenuIdMap = new HashMap<>(25);

    private WebloggerPropertiesRepository webloggerPropertiesRepository;
    private Environment env;
    private ObjectMapper objectMapper;
    private Cache<String, Menu> menuCache;

    @Autowired
    public MenuHelper(WebloggerPropertiesRepository webloggerPropertiesRepository, Environment env,
                      ObjectMapper objectMapper) {
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
        this.env = env;
        this.objectMapper = objectMapper;
        menuCache = Caffeine.newBuilder()
                .expireAfterWrite(2, TimeUnit.DAYS)
                .maximumSize(100)
                .build();
    }

    @PostConstruct
    void init() {
        // parse and cache menus
        try {
            List<ParsedMenu> menus = objectMapper.readValue(MenuHelper.class.getResourceAsStream("/menus.json"),
                    new TypeReference<List<ParsedMenu>>() { });

            for (ParsedMenu menu : menus) {
                menuMap.put(menu.getId(), menu);

                for (ParsedMenu.ParsedTab tab : menu.getTabs()) {
                    for (ParsedMenu.ParsedTabItem item : tab.getTabItems()) {
                        actionToMenuIdMap.put(item.getAction(), menu.getId());
                        if (item.getSubActions() != null) {
                            for (String subAction : item.getSubActions()) {
                                actionToMenuIdMap.put(subAction, menu.getId());
                            }
                        }
                    }
                }

            }

        } catch (Exception ex) {
            log.error("Error parsing menu configs", ex);
        }
    }

    /**
     * Creates and returns a Menu object with tabs and tab items removed and active tab and item
     * highlighted as appropriate for the User, Weblog, and Action being performed.
     *
     * @param userGlobalRole - user's global role
     * @param userWeblogRole - user's role within the weblog being displayed
     * @param currentAction  the current action being invoked. Null to ignore.
     * @return Menu object
     */
    public Menu getMenu(GlobalRole userGlobalRole, WeblogRole userWeblogRole, String currentAction) {
        Menu menu = null;

        String menuId = actionToMenuIdMap.get(currentAction);

        if (menuId != null) {
            String cacheKey = generateMenuCacheKey(menuId, userGlobalRole.name(),
                    userWeblogRole == null ? null : userWeblogRole.name(), currentAction);
            menu = menuCache.get(cacheKey, v -> buildMenu(menuId, userGlobalRole, userWeblogRole, currentAction));
        }
        return menu;
    }

    /**
     * Creates menu according to logged-in user's permissions
     *
     * @param menuId         - ID of desired menu
     * @param userGlobalRole - user's global role
     * @param userWeblogRole - user's role within the weblog being displayed
     * @param currentAction  the current action
     * @return menu
     */
    private Menu buildMenu(String menuId, GlobalRole userGlobalRole, WeblogRole userWeblogRole,
                           String currentAction) {

        ParsedMenu menuConfig = menuMap.get(menuId);

        if (menuConfig == null) {
            log.error("Invalid menuId {} provided", menuId);
            return null;
        }

        Menu tabMenu = new Menu();

        // iterate over tabs from parsed config
        for (ParsedMenu.ParsedTab configTab : menuConfig.getTabs()) {
            // does this tab have an enabledProperty?
            boolean includeTab = true;
            if (configTab.getEnabledProperty() != null) {
                includeTab = getBooleanProperty(configTab.getEnabledProperty());
            }

            if (!includeTab || userGlobalRole.getWeight() < configTab.getGlobalRole().getWeight() ||
                    !checkWeblogRole(userWeblogRole, configTab.getWeblogRole())) {
                continue;
            }

            // all checks passed, tab should be included
            MenuTab tab = new MenuTab();
            tab.setKey(configTab.getTitleKey());

            // setup tab items
            boolean selectable = true;

            // now check if each tab item should be included
            for (ParsedMenu.ParsedTabItem tabItem : configTab.getTabItems()) {
                boolean includeItem = true;

                if (tabItem.getEnabledProperty() != null) {
                    includeItem = getBooleanProperty(tabItem.getEnabledProperty());
                }

                // disabled and global role check
                if (!includeItem ||
                        (tabItem.getGlobalRole() != null &&
                                userGlobalRole.getWeight() < configTab.getGlobalRole().getWeight())) {
                    continue;
                }

                // weblog role check
                if (tabItem.getWeblogRole() != null &&
                        !checkWeblogRole(userWeblogRole, tabItem.getWeblogRole())) {
                    continue;
                }

                // all checks passed, item should be included
                MenuTabItem newTabItem = new MenuTabItem();
                newTabItem.setKey(tabItem.getTitleKey());
                newTabItem.setAction(tabItem.getAction());
                newTabItem.setActionPath(tabItem.getActionPath());

                // is this the selected item? Only one can be selected so skip the rest
                if (selectable && currentAction != null && isSelected(currentAction, tabItem)) {
                    newTabItem.setSelected(true);
                    tab.setSelected(true);
                    selectable = false;
                }

                // add the item
                tab.addItem(newTabItem);
            }

            // add the tab
            tabMenu.addTab(tab);
        }

        return tabMenu;
    }

    /**
     * Check is user's role sufficient for tab/tab item to be displayed.
     *
     * @param usersRole    the user's weblog role for the current weblog, or null if no roles or no weblog
     * @param requiredRole the minimum role required for the action.
     * @return true if usersRole sufficient for requiredRole or no role is required, false otherwise
     */
    private static boolean checkWeblogRole(WeblogRole usersRole, WeblogRole requiredRole) {
        return requiredRole == WeblogRole.NOBLOGNEEDED ||
                (usersRole != null && usersRole.hasEffectiveRole(requiredRole));
    }

    /**
     * Check enabled property
     *
     * @param propertyName the property name
     * @return the boolean property
     */
    private boolean getBooleanProperty(String propertyName) {
        if ("themes.customtheme.allowed".equals(propertyName)) {
            return webloggerPropertiesRepository.findOrNull().isUsersCustomizeThemes();
        }
        return "true".equalsIgnoreCase(env.getProperty(propertyName));
    }

    /**
     * Checks if is selected.
     *
     * @param currentAction the current action
     * @param tabItem       the tab item
     * @return true, if is selected
     */
    private static boolean isSelected(String currentAction, ParsedMenu.ParsedTabItem tabItem) {
        if (currentAction.equals(tabItem.getAction())) {
            return true;
        }
        // an item is also considered selected if it's a subaction of the current action
        Set<String> subActions = tabItem.getSubActions();
        return subActions != null && subActions.contains(currentAction);
    }

    private String generateMenuCacheKey(String menuName, String globalRole, String weblogRole, String actionName) {
        return "menu/" + menuName + "/global/" + globalRole + "/weblog/" + weblogRole +
                (actionName != null ? "/action/" + actionName : "");
    }

}
