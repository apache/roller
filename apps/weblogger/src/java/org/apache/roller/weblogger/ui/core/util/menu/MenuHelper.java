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
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
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
import org.apache.roller.weblogger.util.Utilities;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * A helper class for dealing with UI menus.
 */
public class MenuHelper {
    
    private static Log log = LogFactory.getLog(MenuHelper.class);
    
    private static Hashtable menus = new Hashtable();
    
    
    static {
        try {
            // parse menus and cache so we can efficiently reuse them
            // TODO: there is probably a better way than putting the whole path
            ParsedMenu editorMenu = unmarshall(
                MenuHelper.class.getResourceAsStream(
                "/org/apache/roller/weblogger/ui/struts2/editor/editor-menu.xml"));
            menus.put("editor", editorMenu);
            
            ParsedMenu adminMenu = unmarshall(
                MenuHelper.class.getResourceAsStream(
                "/org/apache/roller/weblogger/ui/struts2/admin/admin-menu.xml"));
            menus.put("admin", adminMenu);
            
        } catch (Exception ex) {
            log.error("Error parsing menu configs", ex);
        }
    }
    
    
    public static Menu getMenu(String menuId, String currentAction,
                               User user, Weblog weblog) {
        
        if(menuId == null) {
            return null;
        }
        
        Menu menu = null;
        
        // do we know the specified menu config?
        ParsedMenu menuConfig = (ParsedMenu) menus.get(menuId);
        if(menuConfig != null) {
            try {
                menu = buildMenu(menuConfig, currentAction, user, weblog);
            } catch (WebloggerException ex) {
                log.debug("ERROR: fethcing user roles", ex);
            }
        }
        
        return menu;
    }
    
    
    private static Menu buildMenu(ParsedMenu menuConfig, String currentAction, 
                                  User user, Weblog weblog) throws WebloggerException {
        
        log.debug("creating menu for action - "+currentAction);
        
        Menu tabMenu = new Menu();
        UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
        
        // iterate over tabs from parsed config
        ParsedTab configTab = null;
        Iterator tabsIter = menuConfig.getTabs().iterator();
        while (tabsIter.hasNext()) {
            configTab = (ParsedTab) tabsIter.next();
            
            log.debug("config tab = "+configTab.getName());
            
            // does this tab have an enabledProperty?
            boolean includeTab = true;
            if(configTab.getEnabledProperty() != null) {
                includeTab = getBooleanProperty(configTab.getEnabledProperty());
            } else if(configTab.getDisabledProperty() != null) {
                includeTab = ! getBooleanProperty(configTab.getDisabledProperty());
            }
            
            if (includeTab) {
                // user roles check
                if (configTab.getGlobalPermissionActions() != null
                        && !configTab.getGlobalPermissionActions().isEmpty()) {
                    try {
                        GlobalPermission perm = 
                            new GlobalPermission(configTab.getGlobalPermissionActions());
                        if (!umgr.checkPermission(perm, user)) {
                            includeTab = false;
                        }
                    } catch (WebloggerException ex) {
                        log.debug("ERROR: fetching user roles", ex);
                        includeTab = false;
                    }
                }
            }
            
            if (includeTab) {
                // weblog permissions check
                if (configTab.getWeblogPermissionActions() != null 
                        && !configTab.getWeblogPermissionActions().isEmpty()) {
                    WeblogPermission perm = 
                        new WeblogPermission(weblog, configTab.getWeblogPermissionActions());
                    includeTab = umgr.checkPermission(perm, user);
                }
            }
            
            if (includeTab) {
                log.debug("tab allowed - "+configTab.getName());
                
                // all checks passed, tab should be included
                MenuTab tab = new MenuTab();
                tab.setKey(configTab.getName());
                
                // setup tab items
                boolean firstItem = true;
                ParsedTabItem configTabItem = null;
                Iterator itemsIter = configTab.getTabItems().iterator();
                while (itemsIter.hasNext()) {
                    configTabItem = (ParsedTabItem) itemsIter.next();
                    
                    log.debug("config tab item = "+configTabItem.getName());
                    
                    boolean includeItem = true;
                    if (configTabItem.getEnabledProperty() != null) {
                        includeItem = getBooleanProperty(configTabItem.getEnabledProperty());
                    } else if (configTabItem.getDisabledProperty() != null) {
                        includeItem = ! getBooleanProperty(configTabItem.getDisabledProperty());
                    }
                    
                    if (includeItem) {
                        // user roles check
                        if (configTabItem.getGlobalPermissionActions() != null
                                && !configTabItem.getGlobalPermissionActions().isEmpty()) {
                            GlobalPermission perm = 
                                new GlobalPermission(configTabItem.getGlobalPermissionActions());
                            if (!umgr.checkPermission(perm, user)) {
                                includeItem = false;
                            }
                        }
                    }
                    
                    if (includeItem) {
                        // weblog permissions check
                        if (configTab.getWeblogPermissionActions() != null 
                                && !configTab.getWeblogPermissionActions().isEmpty()) {                        
                            WeblogPermission perm = new WeblogPermission(weblog, configTab.getWeblogPermissionActions());
                            includeTab = umgr.checkPermission(perm, user);
                        }
                    }
                    
                    if (includeItem) {
                        log.debug("tab item allowed - "+configTabItem.getName());
                        
                        // all checks passed, item should be included
                        MenuTabItem tabItem = new MenuTabItem();
                        tabItem.setKey(configTabItem.getName());
                        tabItem.setAction(configTabItem.getAction());
                        
                        // is this the selected item?
                        if (isSelected(currentAction, configTabItem)) {
                            tabItem.setSelected(true);
                            tab.setSelected(true);
                        }
                        
                        // the url for the tab is the url of the first item of the tab
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
    
    /** Check enabled property, prefers runtime properties */
    private static boolean getBooleanProperty(String propertyName) {
        if (WebloggerRuntimeConfig.getProperty(propertyName) != null) {
            return WebloggerRuntimeConfig.getBooleanProperty(propertyName);
        }
        return WebloggerConfig.getBooleanProperty(propertyName);
    }
    
    private static boolean isSelected(String currentAction, ParsedTabItem tabItem) {
        
        if (currentAction.equals(tabItem.getAction())) {
            return true;
        }
        
        // an item is also considered selected if it's subforwards are the current action
        String[] subActions = tabItem.getSubActions();
        if (subActions != null && subActions.length > 0) {
            for(int i=0; i < subActions.length; i++) {
                if (currentAction.equals(subActions[i])) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    
    /**
     * Unmarshall the given input stream into our defined
     * set of Java objects.
     **/
    private static ParsedMenu unmarshall(InputStream instream) 
        throws IOException, JDOMException {
        
        if (instream == null)
            throw new IOException("InputStream is null!");
        
        ParsedMenu config = new ParsedMenu();
        
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(instream);
        
        Element root = doc.getRootElement();
        List menus = root.getChildren("menu");
        Iterator iter = menus.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            config.addTab(elementToParsedTab(e));
        }
        
        return config;
    }
    
    
    private static ParsedTab elementToParsedTab(Element element) {
        
        ParsedTab tab = new ParsedTab();
        
        tab.setName(element.getAttributeValue("name"));
        if (element.getAttributeValue("weblogPerms") != null) {
            tab.setWeblogPermissionActions(Utilities.stringToStringList(element.getAttributeValue("weblogPerms"),","));
        }
        if (element.getAttributeValue("globalPerms") != null) {
            tab.setGlobalPermissionActions(Utilities.stringToStringList(element.getAttributeValue("globalPerms"),","));
        }
        tab.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        tab.setDisabledProperty(element.getAttributeValue("disabledProperty"));
        
        List menuItems = element.getChildren("menu-item");
        Iterator iter = menuItems.iterator();
        while (iter.hasNext()) {
            Element e = (Element) iter.next();
            tab.addItem(elementToParsedTabItem(e));
        }
        
        return tab;
    }
    
    
    private static ParsedTabItem elementToParsedTabItem(Element element) {
        
        ParsedTabItem tabItem = new ParsedTabItem();
        
        tabItem.setName(element.getAttributeValue("name"));
        tabItem.setAction(element.getAttributeValue("action"));
        
        String subActions = element.getAttributeValue("subactions");
        if (subActions != null) {
            tabItem.setSubActions(subActions.split(","));
        }
        
        if (element.getAttributeValue("weblogPerms") != null) {
            tabItem.setWeblogPermissionActions(Utilities.stringToStringList(element.getAttributeValue("weblogPerms"), ","));
        }
        if (element.getAttributeValue("globalPerms") != null) {
            tabItem.setGlobalPermissionActions(Utilities.stringToStringList(element.getAttributeValue("globalPerms"), ","));
        }
        tabItem.setEnabledProperty(element.getAttributeValue("enabledProperty"));
        tabItem.setDisabledProperty(element.getAttributeValue("disabledProperty"));
        
        return tabItem;
    }
    
}

