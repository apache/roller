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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Add a new subCategory to an existing Category.
 */
public class CategoryAdd extends UIAction {
    
    private static Log log = LogFactory.getLog(CategoryAdd.class);
    
    // the id of the Category we are adding the new subCategory into
    private String categoryId = null;
    
    // the category we are adding the new subcategory into
    private WeblogCategory category = null;
    
    // bean for managing form data
    private CategoryBean bean = new CategoryBean();
    
    
    public CategoryAdd() {
        this.actionName = "categoryAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "categoryForm.add.title";
    }
    
    
    // author perms required
    public short requiredWeblogPermissions() {
        return WeblogPermission.AUTHOR;
    }
    
    
    public void myPrepare() {
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            if(!StringUtils.isEmpty(getCategoryId())) {
                setCategory(wmgr.getWeblogCategory(getCategoryId()));
            }
        } catch (RollerException ex) {
            log.error("Error looking up category", ex);
        }
    }
    
    
    /**
     * Show category form.
     */
    @SkipValidation
    public String execute() {
        
        if(getCategory() == null) {
            // TODO: i18n
            addError("Cannot add category to null parent category");
            return ERROR;
        }
        
        return INPUT;
    }

    
    /**
     * Save new category.
     */
    public String save() {
        
        if(getCategory() == null) {
            // TODO: i18n
            addError("Cannot add category to null parent category");
            return ERROR;
        }
        
        // validation
        myValidate();
        
        if(!hasActionErrors()) try {
            
            WeblogCategory newCategory = new WeblogCategory(
                    getActionWeblog(),
                    getCategory(),
                    getBean().getName(),
                    getBean().getDescription(),
                    getBean().getImage());
            
            // add new folder to parent
            getCategory().addCategory(newCategory);
            
            // save changes
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            wmgr.saveWeblogCategory(newCategory);
            RollerFactory.getRoller().flush();
            
            // notify caches
            CacheManager.invalidate(newCategory);
            
            // TODO: i18n
            addMessage("category added");
            
            return SUCCESS;
            
        } catch(Exception ex) {
            log.error("Error saving new category", ex);
            // TODO: i18n
            addError("Error saving new category");
        }
        
        return INPUT;
    }

    
    // TODO: validation
    public void myValidate() {
        
        // name is required, has max length, no html
        
        // make sure new name is not a duplicate of an existing folder
        if(getCategory().hasCategory(getBean().getName())) {
            addError("categoryForm.error.duplicateName", getBean().getName());
        }
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

    public CategoryBean getBean() {
        return bean;
    }

    public void setBean(CategoryBean bean) {
        this.bean = bean;
    }
    
}
