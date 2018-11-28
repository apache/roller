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
import java.util.LinkedList;
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


/**
 * Manage weblog categories.
 */
public class Categories extends UIAction {
    
    private static Log log = LogFactory.getLog(Categories.class);
    
    // the id of the category we are viewing
    private String categoryId = null;
    
    // the category we are viewing
    private WeblogCategory category = null;
    
    // list of category ids to move
    private String[] selectedCategories = null;
    
    // category id of the category to move to
    private String targetCategoryId = null;
    
    // all categories from the action weblog
    private Set allCategories = Collections.EMPTY_SET;
    
    // path of categories representing selected categories hierarchy
    private List categoryPath = Collections.EMPTY_LIST;
    
    
    public Categories() {
        this.actionName = "categories";
        this.desiredMenu = "editor";
        this.pageTitle = "categoriesForm.rootTitle";
    }
    
    
    // author perms required
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }
    
    
    public void myPrepare() {
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            if(!StringUtils.isEmpty(getCategoryId()) && 
                    !"/".equals(getCategoryId())) {
                setCategory(wmgr.getWeblogCategory(getCategoryId()));
            } else {
                setCategory(wmgr.getRootWeblogCategory(getActionWeblog()));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up category", ex);
        }
    }
    
    
    public String execute() {
        
        // build list of categories for display
        TreeSet allCategories = new TreeSet(new WeblogCategoryPathComparator());
        
        try {
            // Build list of all categories, except for current one, sorted by path.
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            List<WeblogCategory> cats = wmgr.getWeblogCategories(getActionWeblog(), true);
            for(WeblogCategory cat : cats) {
                if (!cat.getId().equals(getCategoryId())) {
                    allCategories.add(cat);
                }
            }
            
            // build category path
            WeblogCategory parent = getCategory().getParent();
            if(parent != null) {
                List categoryPath = new LinkedList();
                categoryPath.add(0, getCategory());
                while (parent != null) {
                    categoryPath.add(0, parent);
                    parent = parent.getParent();
                }
                setCategoryPath(categoryPath);
            }
        } catch (WebloggerException ex) {
            log.error("Error building categories list", ex);
            // TODO: i18n
            addError("Error building categories list");
        }
        
        if (allCategories.size() > 0) {
            setAllCategories(allCategories);
        }
        
        return LIST;
    }
    
    
    public String move() {
        
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            
            log.debug("Moving categories to category - "+getTargetCategoryId());
            
            // Move subCategories to new category.
            String[] cats = getSelectedCategories();
            WeblogCategory parent = wmgr.getWeblogCategory(getTargetCategoryId());
            if(cats != null) {
                for (int i = 0; i < cats.length; i++) {
                    WeblogCategory cd =
                            wmgr.getWeblogCategory(cats[i]);
                    
                    // Don't move category into itself.
                    if (!cd.getId().equals(parent.getId()) && 
                            !parent.descendentOf(cd)) {
                        wmgr.moveWeblogCategory(cd, parent);
                    } else {
                        addMessage("categoriesForm.warn.notMoving", cd.getName());
                    }
                }
                
                // flush changes
                WebloggerFactory.getWeblogger().flush();
            }
            
        } catch (WebloggerException ex) {
            log.error("Error moving categories", ex);
            addError("categoriesForm.error.move");
        }
        
        return execute();
    }
    

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public WeblogCategory getCategory() {
        return category;
    }

    public void setCategory(WeblogCategory category) {
        this.category = category;
    }

    public String[] getSelectedCategories() {
        return selectedCategories;
    }

    public void setSelectedCategories(String[] selectedCategories) {
        this.selectedCategories = selectedCategories;
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

    public List getCategoryPath() {
        return categoryPath;
    }

    public void setCategoryPath(List categoryPath) {
        this.categoryPath = categoryPath;
    }
    
}
