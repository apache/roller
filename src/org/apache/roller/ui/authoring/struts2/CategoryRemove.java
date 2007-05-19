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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.pojos.WeblogCategoryData;
import org.apache.roller.pojos.WeblogCategoryPathComparator;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Remove a category.
 */
public class CategoryRemove extends UIAction {
    
    private static Log log = LogFactory.getLog(CategoryRemove.class);
    
    // id of category to remove
    private String removeId = null;
    
    // category object that we will remove
    private WeblogCategoryData category = null;
    
    // category id of the category to move to
    private String targetCategoryId = null;
    
    // all categories from the action weblog
    private Set allCategories = Collections.EMPTY_SET;
    
    
    public CategoryRemove() {
        this.actionName = "categoryRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "categoriesForm.rootTitle";
    }
    
    
    public short requiredWeblogPermissions() {
        return PermissionsData.AUTHOR;
    }
    
    
    public void myPrepare() {
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            if(!StringUtils.isEmpty(getRemoveId())) {
                setCategory(wmgr.getWeblogCategory(getRemoveId()));
            }
        } catch (RollerException ex) {
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
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List<WeblogCategoryData> cats = wmgr.getWeblogCategories(getActionWeblog(), true);
            for(WeblogCategoryData cat : cats) {
                if (!cat.getId().equals(getRemoveId())) {
                    allCategories.add(cat);
                }
            }
        } catch (RollerException ex) {
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
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            
            if(getTargetCategoryId() != null) {
                WeblogCategoryData target = wmgr.getWeblogCategory(getTargetCategoryId());
                wmgr.moveWeblogCategoryContents(getCategory(), target);
                RollerFactory.getRoller().flush();
            }
            
            wmgr.removeWeblogCategory(getCategory());
            RollerFactory.getRoller().flush();
            
            // notify cache
            CacheManager.invalidate(getCategory());
            
            // set category id to parent for next page
            setRemoveId(getCategory().getParent().getId());
            
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

    public WeblogCategoryData getCategory() {
        return category;
    }

    public void setCategory(WeblogCategoryData category) {
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
