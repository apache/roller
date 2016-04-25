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
package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Blacklist;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Action for modifying weblog configuration.
 */
public class WeblogConfig extends UIAction {
    
    private static Log log = LogFactory.getLog(WeblogConfig.class);

    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy strategy) {
        this.persistenceStrategy = strategy;
    }

    private PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    // bean for managing submitted data
    private Weblog bean = new Weblog();
    
    // list of available plugins
    private List<WeblogEntryPlugin> weblogEntryPlugins;

    // list of available editors
    private static List<Pair<String, String>> editorsList = new ArrayList<>(2);

    static {
        editorsList.add(Pair.of("editor-text.jsp", "editor.text.name"));
        editorsList.add(Pair.of("editor-xinha.jsp", "editor.xinha.name"));
    }

    public WeblogConfig() {
        this.actionName = "weblogConfig";
        this.desiredMenu = "editor";
        this.pageTitle = "websiteSettings.title";
    }
    
    
    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @SkipValidation
    public String execute() {
        
        // load bean with data from weblog
        Weblog currentWeblog = getActionWeblog();
        bean.setHandle(currentWeblog.getHandle());
        bean.setName(currentWeblog.getName());
        bean.setTagline(currentWeblog.getTagline());
        bean.setEditorPage(currentWeblog.getEditorPage());
        bean.setBlacklist(currentWeblog.getBlacklist());
        bean.setAllowComments(currentWeblog.getAllowComments());
        bean.setDefaultCommentDays(currentWeblog.getDefaultCommentDays());
        bean.setApproveComments(currentWeblog.getApproveComments());
        bean.setEmailComments(currentWeblog.getEmailComments());
        bean.setLocale(currentWeblog.getLocale());
        bean.setTimeZone(currentWeblog.getTimeZone());
        bean.setDefaultPlugins(currentWeblog.getDefaultPlugins());
        bean.setEntriesPerPage(currentWeblog.getEntriesPerPage());
        bean.setAnalyticsCode(currentWeblog.getAnalyticsCode());
        bean.setAbout(currentWeblog.getAbout());
        return INPUT;
    }
    
    
    /**
     * Save weblog configuration.
     */
    public String save() {
        
        // run validation
        myValidate();
        
        if(!hasActionErrors()) {
            try {
                Weblog weblog = getActionWeblog();

                if (bean.getAnalyticsCode() != null) {
                    bean.setAnalyticsCode(bean.getAnalyticsCode().trim());
                }

                weblog.setName(bean.getName());
                weblog.setTagline(bean.getTagline());
                weblog.setEditorPage(bean.getEditorPage());
                weblog.setBlacklist(bean.getBlacklist());
                weblog.setAllowComments(bean.getAllowComments());
                weblog.setApproveComments(bean.getApproveComments());
                weblog.setEmailComments(bean.getEmailComments());
                weblog.setLocale(bean.getLocale());
                weblog.setTimeZone(bean.getTimeZone());
                weblog.setDefaultPlugins(bean.getDefaultPlugins());
                weblog.setEntriesPerPage(bean.getEntriesPerPage());
                weblog.setAbout(bean.getAbout());
                weblog.setAnalyticsCode(bean.getAnalyticsCode());
                weblog.setDefaultCommentDays(bean.getDefaultCommentDays());

                // save config
                weblogManager.saveWeblog(weblog);

                // ROL-1050: apply comment defaults to existing entries
                if(bean.isApplyCommentDefaults()) {
                    weblogEntryManager.applyCommentDefaultsToEntries(weblog);
                }

                // flush and clear cache
                persistenceStrategy.flushAndInvalidateWeblog(weblog);
                addMessage("websiteSettings.savedChanges");

            } catch (Exception ex) {
                log.error("Error updating weblog config", ex);
                addError("Error updating configuration");
            }
        }
        
        return  INPUT;
    }
    
    
    private void myValidate() {
        if (StringUtils.isEmpty(bean.getName())) {
            addError("WeblogConfig.error.nameNull");
        }

        // make sure user didn't enter an invalid entry display count
        int maxEntries = propertiesManager.getIntProperty("site.pages.maxEntries");
        if(bean.getEntriesPerPage() > maxEntries) {
            addError("websiteSettings.error.entryDisplayCount");
        }
        
        // check blacklist
        try {
            // just for testing/counting, this does not persist rules in any way
            Blacklist testBlacklist = new Blacklist(bean.getBlacklist(), null);
            addMessage("websiteSettings.acceptedBlacklist",
                    Arrays.asList(new String[] {"" + testBlacklist.getStringRulesCount(),
                            "" + testBlacklist.getRegexRulesCount()}));
        } catch (Exception e) {
            addError("websiteSettings.error.processingBlacklist", e.getMessage());
        }
    }
    
    
    public Weblog getBean() {
        return bean;
    }

    public void setBean(Weblog bean) {
        this.bean = bean;
    }

    public List getEditorsList() {
        return editorsList;
    }
    
    public List<WeblogEntryPlugin> getWeblogEntryPlugins() {
        return weblogEntryPlugins;
    }

    public void setWeblogEntryPlugins(List<WeblogEntryPlugin> weblogEntryPlugins) {
        this.weblogEntryPlugins = weblogEntryPlugins;
    }

}
