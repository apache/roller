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

package org.apache.roller.ui.authoring.struts2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.PluginManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.ui.core.RollerContext;
import org.apache.roller.ui.core.plugins.UIPluginManager;
import org.apache.roller.ui.core.util.struts2.KeyValueObject;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.Blacklist;
import org.apache.roller.util.cache.CacheManager;


/**
 * Action for modifying weblog configuration.
 */
public class WeblogConfig extends UIAction {
    
    private static Log log = LogFactory.getLog(WeblogConfig.class);
    
    // bean for managing submitted data
    private WeblogConfigBean bean = new WeblogConfigBean();
    
    // categories list
    private List weblogCategories = Collections.EMPTY_LIST;
    
    // list of available editors
    private List editorsList = Collections.EMPTY_LIST;
    
    // list of available plugins
    private List pluginsList = Collections.EMPTY_LIST;
    
    
    public WeblogConfig() {
        this.actionName = "weblogConfig";
        this.desiredMenu = "editor";
        this.pageTitle = "websiteSettings.title";
    }
    
    
    // admin perms required
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    public void myPrepare() {
        
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            
            // set categories list
            setWeblogCategories(wmgr.getWeblogCategories(getActionWeblog(), false));
            
            // set the Editor Page list
            UIPluginManager pmgr = RollerContext.getUIPluginManager();
            List editorsList = pmgr.getWeblogEntryEditors();
            if(editorsList != null) {
                setEditorsList(editorsList);
            }
            
            // set plugins list
            PluginManager ppmgr = RollerFactory.getRoller().getPagePluginManager();
            Map pluginsMap = ppmgr.getWeblogEntryPlugins(getActionWeblog());
            List plugins = new ArrayList();
            Iterator iter = pluginsMap.values().iterator();
            while(iter.hasNext()) {
                plugins.add(iter.next());
            }
            // sort
            setPluginsList(plugins);

        } catch (Exception ex) {
            log.error("Error preparing weblog config action", ex);
        }
    }
    
    
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
        
        if(!hasActionErrors()) try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            
            WebsiteData weblog = getActionWeblog();
            
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
            
            // save config
            umgr.saveWebsite(weblog);
            
            // ROL-1050: apply comment defaults to existing entries
            if(getBean().getApplyCommentDefaults()) {
                wmgr.applyCommentDefaultsToEntries(weblog);
            }
            
            // apply referer filters
            RollerFactory.getRoller().getRefererManager().applyRefererFilters(weblog);
            
            // flush
            RollerFactory.getRoller().flush();
            
            addMessage("websiteSettings.savedChanges");
            
            // Clear cache entries associated with website
            CacheManager.invalidate(weblog);
            
        } catch (Exception ex) {
            log.error("Error updating weblog config", ex);
            // TODO: i18n
            addError("Error updating configuration");
        }
        
        return  INPUT;
    }
    
    
    // validation
    private void myValidate() {
        
        // make sure user didn't enter an invalid entry display count
        int maxEntries = RollerRuntimeConfig.getIntProperty("site.pages.maxEntries");
        if(getBean().getEntryDisplayCount() > maxEntries) {
            addError("websiteSettings.error.entryDisplayCount");
        }
        
        // check blacklist
        List regexRules = new ArrayList();
        List stringRules = new ArrayList();
        try {
            // just for testing/counting, this does not persist rules in any way
            Blacklist.populateSpamRules(getBean().getBlacklist(), stringRules, regexRules, null);
            addMessage("websiteSettings.acceptedBlacklist",
                    Arrays.asList(new String[] {""+stringRules.size(), ""+regexRules.size()}));
        } catch (Throwable e) {
            addError("websiteSettings.error.processingBlacklist", e.getMessage());
        }
    }
    

    public List getCommentDaysOptions() {
        
        List opts = new ArrayList();
        
        opts.add(new KeyValueObject("0", getText("weblogEdit.unlimitedCommentDays")));
        opts.add(new KeyValueObject("1", getText("weblogEdit.days1")));
        opts.add(new KeyValueObject("2", getText("weblogEdit.days2")));
        opts.add(new KeyValueObject("3", getText("weblogEdit.days3")));
        opts.add(new KeyValueObject("4", getText("weblogEdit.days4")));
        opts.add(new KeyValueObject("5", getText("weblogEdit.days5")));
        opts.add(new KeyValueObject("7", getText("weblogEdit.days7")));
        opts.add(new KeyValueObject("10", getText("weblogEdit.days10")));
        opts.add(new KeyValueObject("20", getText("weblogEdit.days20")));
        opts.add(new KeyValueObject("30", getText("weblogEdit.days30")));
        opts.add(new KeyValueObject("60", getText("weblogEdit.days60")));
        opts.add(new KeyValueObject("90", getText("weblogEdit.days90")));
        
        return opts;
    }
    
    
    public WeblogConfigBean getBean() {
        return bean;
    }

    public void setBean(WeblogConfigBean bean) {
        this.bean = bean;
    }

    public List getWeblogCategories() {
        return weblogCategories;
    }

    public void setWeblogCategories(List weblogCategories) {
        this.weblogCategories = weblogCategories;
    }

    public List getEditorsList() {
        return editorsList;
    }
    
    public void setEditorsList(List editorsList) {
        this.editorsList = editorsList;
    }

    public List getPluginsList() {
        return pluginsList;
    }

    public void setPluginsList(List pluginsList) {
        this.pluginsList = pluginsList;
    }
    
}
