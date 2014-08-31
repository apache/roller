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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.util.Utilities;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * A helper class for dealing with UI menus.
 * 
 * Note : Debug logging disabled here as it is too expensive time wise.
 * 
 */
public final class MenuHelper {

    private static Log log = LogFactory.getLog(MenuHelper.class);

    private static Map<String, ParsedMenu> menus = new HashMap<String, ParsedMenu>();

    // menu, menuName, tabName action/subaction check
    private static Map<String, HashMap<String, HashSet<String>>> itemMenu = new HashMap<String, HashMap<String, HashSet<String>>>();

    private MenuHelper() {
    }

    static {
        try {

            // parse menus and cache so we can efficiently reuse them
            String menu = "editor";
            ParsedMenu editorMenu = unmarshall(
                    menu,
                    MenuHelper.class
                            .getResourceAsStream("/org/apache/roller/weblogger/ui/struts2/editor/editor-menu.xml"));
            menus.put(menu, editorMenu);

            menu = "admin";
            ParsedMenu adminMenu = unmarshall(
                    menu,
                    MenuHelper.class
                            .getResourceAsStream("/org/apache/roller/weblogger/ui/struts2/admin/admin-menu.xml"));
            menus.put(menu, adminMenu);

        } catch (Exception ex) {
            log.error("Error parsing menu configs", ex);
        }
    }

    /**
     * Gets the menu.
     * 
     * @param menuId
     *            the menu id
     * @param currentAction
     *            the current action. Null to ignore.
     * @param user
     *            the user
     * @param weblog
     *            the weblog
     * 
     * @return the menu
     */
    public static Menu getMenu(String menuId, String currentAction, User user,
            Weblog weblog) {

        if (menuId == null) {
            return null;
        }

        Menu menu = null;

        // do we know the specified menu config?
        ParsedMenu menuConfig = menus.get(menuId);
        if (menuConfig != null) {
            try {
                menu = buildMenu(menuId, menuConfig, currentAction, user,
                        weblog);
            } catch (WebloggerException ex) {
                log.error("ERROR: fethcing user roles", ex);
            }
        }

        return menu;
    }

    /**
     * Builds the menu.
     * 
     * @param menuId
     *            the menu id
     * @param menuConfig
     *            the menu config
     * @param currentAction
     *            the current action
     * @param user
     *            the user
     * @param weblog
     *            the weblog
     * 
     * @return the menu
     * 
     * @throws WebloggerException
     *             the weblogger exception
     */
    private static Menu buildMenu(String menuId, ParsedMenu menuConfig,
            String currentAction, User user, Weblog weblog)
            throws WebloggerException {

        // log.debug("creating menu for action - " + currentAction);

        Menu tabMenu = new Menu();
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();


        // Hack - for blogger convenience, the design tab of the edit
        // menu defaults to the templates tab item (instead of theme edit)
        // if the weblog is using a custom theme.
        boolean customThemeOverride = "editor".equals(menuId)
                && WeblogTheme.CUSTOM.equals(weblog.getEditorTheme());

        // iterate over tabs from parsed config
        for (ParsedTab configTab : menuConfig.getTabs()) {

            // log.debug("config tab = " + configTab.getName());

            // does this tab have an enabledProperty?
            boolean includeTab = true;
            if (configTab.getEnabledProperty() != null) {
                includeTab = getBooleanProperty(configTab.getEnabledProperty());
            } else if (configTab.getDisabledProperty() != null) {
                includeTab = !getBooleanProperty(configTab
                        .getDisabledProperty());
            }

            // user roles check
            if (includeTab && configTab.getGlobalPermissionActions() != null
                    && !configTab.getGlobalPermissionActions().isEmpty()) {
                try {
                    GlobalPermission perm = new GlobalPermission(
                            configTab.getGlobalPermissionActions());
                    if (!umgr.checkPermission(perm, user)) {
                        includeTab = false;
                    }
                } catch (WebloggerException ex) {
                    log.error("ERROR: fetching user roles", ex);
                    includeTab = false;
                }
            }

            // weblog permissions check
            if (includeTab && configTab.getWeblogPermissionActions() != null
                    && !configTab.getWeblogPermissionActions().isEmpty()) {
                WeblogPermission perm = new WeblogPermission(weblog,
                        configTab.getWeblogPermissionActions());
                includeTab = umgr.checkPermission(perm, user);
            }

            if (includeTab) {

                // log.debug("tab allowed - " + configTab.getName());

                // all checks passed, tab should be included
                MenuTab tab = new MenuTab();
                tab.setKey(configTab.getName());

                // setup tab items
                boolean firstItem = true;
                boolean selectable = true;

                for (ParsedTabItem configTabItem : configTab.getTabItems()) {

                    boolean includeItem = true;

                    if (configTabItem.getEnabledProperty() != null) {
                        includeItem = getBooleanProperty(configTabItem
                                .getEnabledProperty());
                    } else if (configTabItem.getDisabledProperty() != null) {
                        includeItem = !getBooleanProperty(configTabItem
                                .getDisabledProperty());
                    }

                    // user roles check
                    if (includeItem
                            && configTabItem.getGlobalPermissionActions() != null
                            && !configTabItem.getGlobalPermissionActions()
                                    .isEmpty()) {
                        GlobalPermission perm = new GlobalPermission(
                                configTabItem.getGlobalPermissionActions());
                        if (!umgr.checkPermission(perm, user)) {
                            includeItem = false;
                        }
                    }

                    // weblog permissions check
                    if (includeItem
                            && configTabItem.getWeblogPermissionActions() != null
                            && !configTabItem.getWeblogPermissionActions()
                                    .isEmpty()) {
                        WeblogPermission perm = new WeblogPermission(weblog,
                                configTabItem.getWeblogPermissionActions());
                        includeItem = umgr.checkPermission(perm, user);
                    }

                    if (includeItem) {

                        // log.debug("tab item allowed - "
                        // + configTabItem.getName());

                        // all checks passed, item should be included
                        MenuTabItem tabItem = new MenuTabItem();
                        tabItem.setKey(configTabItem.getName());
                        tabItem.setAction(configTabItem.getAction());

                        // is this the selected item? Only one can be selected
                        // so skip the rest
                        if (currentAction != null && selectable
                                && isSelected(currentAction, configTabItem)) {
                            tabItem.setSelected(true);
                            tab.setSelected(true);
                            selectable = false;
                        }

                        // the url for the tab is the url of the first tab item
                        if (firstItem) {
                            if (customThemeOverride && "tabbedmenu.design".equals(tab.getKey())) {
                                tab.setAction("templates");
                            } else {
                                tab.setAction(tabItem.getAction());
                            }
                            firstItem = false;
                        }

                        // add the item
                        tab.addItem(tabItem);
                    }
                }

                // add the tab
                tabMenu.addTab(tab);
            }
        }

        return tabMenu;
    }

    /**
     * Check enabled property, prefers runtime properties.
     * 
     * @param propertyName
     *            the property name
     * 
     * @return the boolean property
     */
    private static boolean getBooleanProperty(String propertyName) {
        if (WebloggerRuntimeConfig.getProperty(propertyName) != null) {
            return WebloggerRuntimeConfig.getBooleanProperty(propertyName);
        }
        return WebloggerConfig.getBooleanProperty(propertyName);
    }

    /**
     * Checks if is selected.
     * 
     * @param currentAction
     *            the current action
     * @param tabItem
     *            the tab item
     * 
     * @return true, if is selected
     */
    private static boolean isSelected(String currentAction,
            ParsedTabItem tabItem) {

        if (currentAction.equals(tabItem.getAction())) {
            return true;
        }

        // an item is also considered selected if it's a subforward of the
        // current action
        Set<String> subActions = tabItem.getSubActions();

        return subActions != null && subActions.contains(currentAction);
    }

    /**
     * Unmarshall the given input stream into our defined set of Java objects.
     * 
     * @param menuId
     *            the menu id
     * @param instream
     *            the instream
     * 
     * @return the parsed menu
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JDOMException
     *             the jDOM exception
     */
    private static ParsedMenu unmarshall(String menuId, InputStream instream)
            throws IOException, JDOMException {

        if (instream == null) {
            throw new IOException("InputStream is null!");
        }

        ParsedMenu config = new ParsedMenu();

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(instream);

        Element root = doc.getRootElement();
        List<Element> parsedMenus = root.getChildren("menu");
        for (Element e : parsedMenus) {
            config.addTab(elementToParsedTab(menuId, e));
        }

        return config;
    }

    /**
     * Element to parsed tab.
     * 
     * @param menuId
     *            the menu id
     * @param element
     *            the element
     * 
     * @return the parsed tab
     */
    private static ParsedTab elementToParsedTab(String menuId, Element element) {

        ParsedTab tab = new ParsedTab();

        tab.setName(element.getAttributeValue("name"));
        if (element.getAttributeValue("weblogPerms") != null) {
            tab.setWeblogPermissionActions(Utilities.stringToStringList(
                    element.getAttributeValue("weblogPerms"), ","));
        }
        if (element.getAttributeValue("globalPerms") != null) {
            tab.setGlobalPermissionActions(Utilities.stringToStringList(
                    element.getAttributeValue("globalPerms"), ","));
        }
        tab.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        tab.setDisabledProperty(element.getAttributeValue("disabledProperty"));

        List<Element> menuItems = element.getChildren("menu-item");

        // Build our tab action relation
        HashMap<String, HashSet<String>> menu = itemMenu.get(menuId);
        if (menu == null) {
            menu = new HashMap<String, HashSet<String>>();
        }

        for (Element e : menuItems) {

            ParsedTabItem tabItem = elementToParsedTabItem(e);

            HashSet<String> item = menu.get(tab.getName());
            if (item != null) {
                if (!item.contains(tabItem.getAction())) {
                    item.add(tabItem.getAction());
                }
            } else {
                item = new HashSet<String>();
                item.add(tabItem.getAction());
            }

            // Add subaction items
            Set<String> subActions = tabItem.getSubActions();
            if (subActions != null) {
                for (String subAction : subActions) {
                    if (!item.contains(subAction)) {
                        item.add(subAction);
                    }
                }
            }

            // save our tab action relation
            menu.put(tab.getName(), item);

            tab.addItem(tabItem);

        }

        // Save relation
        itemMenu.put(menuId, menu);

        return tab;
    }

    /**
     * Element to parsed tab item.
     * 
     * @param element
     *            the element
     * 
     * @return the parsed tab item
     */
    private static ParsedTabItem elementToParsedTabItem(Element element) {

        ParsedTabItem tabItem = new ParsedTabItem();

        tabItem.setName(element.getAttributeValue("name"));
        tabItem.setAction(element.getAttributeValue("action"));

        String subActions = element.getAttributeValue("subactions");
        if (subActions != null) {
            Set<String> set = new HashSet<String>();
            for (String string : Utilities.stringToStringList(subActions, ",")) {
                set.add(string);
            }
            tabItem.setSubActions(set);
        }

        if (element.getAttributeValue("weblogPerms") != null) {
            tabItem.setWeblogPermissionActions(Utilities.stringToStringList(
                    element.getAttributeValue("weblogPerms"), ","));
        }
        if (element.getAttributeValue("globalPerms") != null) {
            tabItem.setGlobalPermissionActions(Utilities.stringToStringList(
                    element.getAttributeValue("globalPerms"), ","));
        }
        tabItem.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        tabItem.setDisabledProperty(element
                .getAttributeValue("disabledProperty"));

        return tabItem;
    }

}
