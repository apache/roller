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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogCategoryPathComparator;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;


/**
 * Remove a category.
 */
public class CategoryRemove extends UIAction {
    
    private static Log log = LogFactory.getLog(CategoryRemove.class);
    
    // id of category to remove
    private String removeId = null;
    
    // category object that we will remove
    private WeblogCategory category = null;
    
    // category id of the category to move to
    private String targetCategoryId = null;
    
    // all categories from the action weblog
    private Set allCategories = Collections.EMPTY_SET;
    
    
    public CategoryRemove() {
        this.actionName = "categoryRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "categoriesForm.rootTitle";
    }
    
    
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }
    
    
    public void myPrepare() {
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            if(!StringUtils.isEmpty(getRemoveId())) {
                setCategory(wmgr.getWeblogCategory(getRemoveId()));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up category", ex);
        }
    }
    
    
    /**
     * Display the remove template confirmation.
     */
    public String execute() {
        
        // build list of categories for display
        TreeSet allCategories = new TreeSet(new WeblogCategoryPathComparator());
        
        try {
            // Build list of all categories, except for current one, sorted by path.
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            List<WeblogCategory> cats = wmgr.getWeblogCategories(getActionWeblog(), true);
            for(WeblogCategory cat : cats) {
                if (!cat.getId().equals(getRemoveId())) {
                    allCategories.add(cat);
                }
            }
        } catch (WebloggerException ex) {
            log.error("Error building categories list", ex);
            // TODO: i18n
            addError("Error building categories list");
        }
        
        if (allCategories.size() > 0) {
            setAllCategories(allCategories);
        }
        
        return INPUT;
    }
    
    
    /**
     * Remove a new template.
     */
    public String remove() {
        
        if(getCategory() != null) try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            
            if(getTargetCategoryId() != null) {
                WeblogCategory target = wmgr.getWeblogCategory(getTargetCategoryId());
                wmgr.moveWeblogCategoryContents(getCategory(), target);
                WebloggerFactory.getWeblogger().flush();
            }
            
            // notify cache
            String id = getCategory().getId();
            CacheManager.invalidate(getCategory());
            
            wmgr.removeWeblogCategory(getCategory());
            WebloggerFactory.getWeblogger().flush();
            
            // set category id to parent for next page
            setRemoveId(id);
            
            return SUCCESS;
            
        } catch(Exception ex) {
            log.error("Error removing category - "+getRemoveId(), ex);
            // TODO: i18n
            addError("Error removing category");
        }
        
        return execute();
    }

    
    public String getRemoveId() {
        return removeId;
    }

    public void setRemoveId(String categoryId) {
        this.removeId = categoryId;
    }

    public WeblogCategory getCategory() {
        return category;
    }

    public void setCategory(WeblogCategory category) {
        this.category = category;
    }

    public String getTargetCategoryId() {
        return targetCategoryId;
    }

    public void setTargetCategoryId(String targetCategoryId) {
        this.targetCategoryId = targetCategoryId;
    }

    public Set getAllCategories() {
        return allCategories;
    }

    public void setAllCategories(Set allCategories) {
        this.allCategories = allCategories;
    }
    
}
