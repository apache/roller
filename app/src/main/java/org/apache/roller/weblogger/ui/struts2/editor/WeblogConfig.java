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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.plugins.UIPluginManager;
import org.apache.roller.weblogger.ui.core.plugins.WeblogEntryEditor;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Bannedwordslist;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Action for modifying weblog configuration.
 */
// TODO: make this work @AllowedMethods({"execute","save"})
public class WeblogConfig extends UIAction {
    
    private static final Log log = LogFactory.getLog(WeblogConfig.class);
    
    // bean for managing submitted data
    private WeblogConfigBean bean = new WeblogConfigBean();
    
    // categories list
    private List<WeblogCategory> weblogCategories = Collections.emptyList();
    
    // list of available editors
    private List<WeblogEntryEditor> editorsList = Collections.emptyList();
    
    // list of available plugins
    private List<WeblogEntryPlugin> pluginsList = Collections.emptyList();
    
    
    public WeblogConfig() {
        this.actionName = "weblogConfig";
        this.desiredMenu = "editor";
        this.pageTitle = "websiteSettings.title";
    }

    @Override
    public void myPrepare() {
        
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            
            // set categories list
            setWeblogCategories(wmgr.getWeblogCategories(getActionWeblog()));
            
            // set the Editor Page list
            UIPluginManager pmgr = RollerContext.getUIPluginManager();
            List<WeblogEntryEditor> editorList = pmgr.getWeblogEntryEditors();
            if(editorList != null) {
                setEditorsList(editorList);
            }
            
            // set plugins list
            PluginManager ppmgr = WebloggerFactory.getWeblogger().getPluginManager();
            Map<String, WeblogEntryPlugin> pluginsMap = ppmgr.getWeblogEntryPlugins(getActionWeblog());
            List<WeblogEntryPlugin> plugins = new ArrayList<>();
            for (WeblogEntryPlugin entryPlugin : pluginsMap.values()) {
                plugins.add(entryPlugin);
            }

            // sort
            setPluginsList(plugins);

        } catch (Exception ex) {
            log.error("Error preparing weblog config action", ex);
        }
    }
    
    
    @SkipValidation
    @Override
    public String execute() {
        
        // load bean with data from weblog
        getBean().copyFrom(getActionWeblog());
        
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
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

                Weblog weblog = getActionWeblog();

                if (getBean().getAnalyticsCode() != null) {
                    getBean().setAnalyticsCode(getBean().getAnalyticsCode().trim());
                }

                getBean().copyTo(weblog);

                // if blogger category changed then lookup new cat and set it
                if(getBean().getBloggerCategoryId() != null &&
                        !weblog.getBloggerCategory().getId().equals(getBean().getBloggerCategoryId())) {
                    weblog.setBloggerCategory(wmgr.getWeblogCategory(getBean().getBloggerCategoryId()));
                }

                // ROL-485: comments not allowed on inactive weblogs
                if(!weblog.getActive()) {
                    weblog.setAllowComments(Boolean.FALSE);
                    addMessage("websiteSettings.commentsOffForInactiveWeblog");
                }

                // if blog has unchecked 'show all langs' then we must make sure
                // the multi-language blogging option is enabled.
                // TODO: this should be properly reflected via the UI
                if(!weblog.isShowAllLangs() && !weblog.isEnableMultiLang()) {
                    weblog.setEnableMultiLang(true);
                }

                // save config
                WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(weblog);

                // ROL-1050: apply comment defaults to existing entries
                if(getBean().getApplyCommentDefaults()) {
                    wmgr.applyCommentDefaultsToEntries(weblog);
                }

                // flush
                WebloggerFactory.getWeblogger().flush();

                addMessage("websiteSettings.savedChanges");

                // Clear cache entries associated with website
                CacheManager.invalidate(weblog);

            } catch (Exception ex) {
                log.error("Error updating weblog config", ex);
                addError("Error updating configuration");
            }
        }
        
        return  INPUT;
    }
    
    
    // validation
    private void myValidate() {
        
        // make sure user didn't enter an invalid entry display count
        int maxEntries = WebloggerRuntimeConfig.getIntProperty("site.pages.maxEntries");
        if(getBean().getEntryDisplayCount() > maxEntries) {
            addError("websiteSettings.error.entryDisplayCount");
        }
        
        // check bannedwordslist
        List<Pattern> regexRules = new ArrayList<>();
        List<String> stringRules = new ArrayList<>();
        try {
            // just for testing/counting, this does not persist rules in any way
            Bannedwordslist.populateSpamRules(getBean().getBannedwordslist(), stringRules, regexRules, null);
            addMessage("websiteSettings.acceptedBannedwordslist", List.of(""+stringRules.size(), ""+regexRules.size()));
        } catch (Exception e) {
            addError("websiteSettings.error.processingBannedwordslist", e.getMessage());
        }
    }
    
    
    public WeblogConfigBean getBean() {
        return bean;
    }

    public void setBean(WeblogConfigBean bean) {
        this.bean = bean;
    }

    public List<WeblogCategory> getWeblogCategories() {
        return weblogCategories;
    }

    public void setWeblogCategories(List<WeblogCategory> weblogCategories) {
        this.weblogCategories = weblogCategories;
    }

    public List<WeblogEntryEditor> getEditorsList() {
        return editorsList;
    }
    
    public void setEditorsList(List<WeblogEntryEditor> editorsList) {
        this.editorsList = editorsList;
    }

    public List<WeblogEntryPlugin> getPluginsList() {
        return pluginsList;
    }

    public void setPluginsList(List<WeblogEntryPlugin> pluginsList) {
        this.pluginsList = pluginsList;
    }
    
}
