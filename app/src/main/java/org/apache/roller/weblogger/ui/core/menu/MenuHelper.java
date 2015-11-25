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
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.Utilities;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A helper class for dealing with UI menus.
 * 
 * Note : Debug logging disabled here as it is too expensive time wise.
 * 
 */
public final class MenuHelper {

    private static Log log = LogFactory.getLog(MenuHelper.class);

    private static Map<String, ParsedMenu> menus = new HashMap<>();

    private MenuHelper() {
    }

    static {
        try {
            // parse menus and cache so we can efficiently reuse them
            ParsedMenu editorMenu = unmarshall(
                    MenuHelper.class.getResourceAsStream("/org/apache/roller/weblogger/ui/struts2/editor/editor-menu.xml"));
            menus.put("editor", editorMenu);

            ParsedMenu adminMenu = unmarshall(
                    MenuHelper.class.getResourceAsStream("/org/apache/roller/weblogger/ui/struts2/admin/admin-menu.xml"));
            menus.put("admin", adminMenu);
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
    public static Menu getMenu(String menuId, String currentAction, User user, Weblog weblog) {
        if (menuId == null) {
            return null;
        }

        Menu menu = null;

        // do we know the specified menu config?
        ParsedMenu menuConfig = menus.get(menuId);
        if (menuConfig != null) {
            try {
                menu = buildMenu(menuConfig, currentAction, user, weblog);
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
            String currentAction, User user, Weblog weblog)
            throws WebloggerException {

        Menu tabMenu = new Menu();
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();

        // iterate over tabs from parsed config
        for (ParsedTab configTab : menuConfig.getTabs()) {

            // log.debug("config tab = " + configTab.getName());

            // does this tab have an enabledProperty?
            boolean includeTab = true;
            if (configTab.getEnabledProperty() != null) {
                includeTab = getBooleanProperty(configTab.getEnabledProperty());
            } else if (configTab.getDisabledProperty() != null) {
                includeTab = !getBooleanProperty(configTab.getDisabledProperty());
            }

            // global role check
            if (includeTab && !user.hasEffectiveGlobalRole(configTab.getRequiredGlobalRole())) {
                includeTab = false;
            }

            // weblog role check
            if (includeTab && weblog != null) {
                includeTab = umgr.checkWeblogRole(user, weblog, configTab.getRequiredWeblogRole());
            }

            if (includeTab) {
                // all checks passed, tab should be included
                MenuTab tab = new MenuTab();
                tab.setKey(configTab.getName());

                // setup tab items
                boolean firstItem = true;
                boolean selectable = true;

                // now check if each tab item should be included
                for (ParsedTabItem configTabItem : configTab.getTabItems()) {
                    boolean includeItem = true;

                    if (configTabItem.getEnabledProperty() != null) {
                        includeItem = getBooleanProperty(configTabItem.getEnabledProperty());
                    } else if (configTabItem.getDisabledProperty() != null) {
                        includeItem = !getBooleanProperty(configTabItem.getDisabledProperty());
                    }

                    // global role check
                    if (includeItem && (configTabItem.getRequiredGlobalRole() != null)) {
                        if (!user.hasEffectiveGlobalRole(configTabItem.getRequiredGlobalRole())) {
                            includeItem = false;
                        }
                    }

                    // weblog role check
                    if (includeItem && weblog != null && (configTabItem.getRequiredWeblogRole() != null)) {
                        includeItem = umgr.checkWeblogRole(user, weblog, configTabItem.getRequiredWeblogRole());
                    }

                    if (includeItem) {
                        // all checks passed, item should be included
                        MenuTabItem tabItem = new MenuTabItem();
                        tabItem.setKey(configTabItem.getName());
                        tabItem.setAction(configTabItem.getAction());

                        // is this the selected item? Only one can be selected
                        // so skip the rest
                        if (currentAction != null && selectable && isSelected(currentAction, configTabItem)) {
                            tabItem.setSelected(true);
                            tab.setSelected(true);
                            selectable = false;
                        }

                        // the url for the tab is the url of the first tab item
                        if (firstItem) {
                            tab.setAction(tabItem.getAction());
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
     * @param propertyName the property name
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
     * @param currentAction the current action
     * @param tabItem the tab item
     * @return true, if is selected
     */
    private static boolean isSelected(String currentAction, ParsedTabItem tabItem) {
        if (currentAction.equals(tabItem.getAction())) {
            return true;
        }
        // an item is also considered selected if it's a subforward of the current action
        Set<String> subActions = tabItem.getSubActions();
        return subActions != null && subActions.contains(currentAction);
    }

    /**
     * Unmarshall the given input stream into our defined set of Java objects.
     * 
     * @param instream the instream
     * 
     * @return the parsed menu
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     * @throws JDOMException
     *             the jDOM exception
     */
    private static ParsedMenu unmarshall(InputStream instream)
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
            config.addTab(elementToParsedTab(e));
        }

        return config;
    }

    /**
     * Element to parsed tab.
     *
     * @param element
     *            the element
     * 
     * @return the parsed tab
     */
    private static ParsedTab elementToParsedTab(Element element) {
        ParsedTab tab = new ParsedTab();

        tab.setName(element.getAttributeValue("name"));
        tab.setRequiredWeblogRole(WeblogRole.valueOf(element.getAttributeValue("weblogRole")));
        tab.setRequiredGlobalRole(GlobalRole.valueOf(element.getAttributeValue("globalRole")));
        tab.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        tab.setDisabledProperty(element.getAttributeValue("disabledProperty"));

        List<Element> menuItems = element.getChildren("menu-item");

        for (Element e : menuItems) {
            ParsedTabItem tabItem = elementToParsedTabItem(e);
            tab.addItem(tabItem);
        }

        // Save relation
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
            Set<String> set = new HashSet<>();
            for (String string : Utilities.stringToStringList(subActions, ",")) {
                set.add(string);
            }
            tabItem.setSubActions(set);
        }

        if (element.getAttributeValue("globalRole") != null) {
            tabItem.setRequiredGlobalRole(GlobalRole.valueOf(
                    element.getAttributeValue("globalRole")));
        }

        if (element.getAttributeValue("weblogRole") != null) {
            tabItem.setRequiredWeblogRole(WeblogRole.valueOf(
                element.getAttributeValue("weblogRole")));
        }

        tabItem.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        tabItem.setDisabledProperty(element
                .getAttributeValue("disabledProperty"));

        return tabItem;
    }
}
