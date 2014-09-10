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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Edit a new or existing weblog category.
 */
public class CategoryEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(CategoryEdit.class);

    // bean for managing form data
    private CategoryBean bean = new CategoryBean();

    // the (new or already existing) category we are editing
    private WeblogCategory category = null;

    public CategoryEdit() {
        this.desiredMenu = "editor";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }
    
    
    public void myPrepare() {
        if (StringUtils.isEmpty(bean.getId())) {
            // Create and initialize new, not-yet-saved category
            category = new WeblogCategory();
            category.setWeblog(getActionWeblog());
        } else {
            try {
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                category = wmgr.getWeblogCategory(getBean().getId());
            } catch (WebloggerException ex) {
                log.error("Error looking up category", ex);
            }
        }
    }
    
    
    /**
     * Show category form.
     */
    @SkipValidation
    public String execute() {
        if (!isAdd()) {
            // make sure bean is properly loaded from pojo data
            getBean().copyFrom(category);
        }
        return INPUT;
    }

    private boolean isAdd() {
        return actionName.equals("categoryAdd");
    }

    /**
     * Save new category.
     */
    public String save() {
        myValidate();
        
        if(!hasActionErrors()) {
            try {

                // copy updated attributes
                getBean().copyTo(category);

                // save changes
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
                if (isAdd()) {
                    getActionWeblog().addCategory(category);
                    category.calculatePosition();
                }
                wmgr.saveWeblogCategory(category);
                WebloggerFactory.getWeblogger().flush();

                // notify caches
                CacheManager.invalidate(getActionWeblog());

                addMessage(isAdd()? "categoryForm.created"
                        : "categoryForm.changesSaved",
                        category.getName());

                return SUCCESS;
            } catch(Exception ex) {
                log.error("Error saving category", ex);
                addError("generic.error.check.logs");
            }
        }
        
        return INPUT;
    }

    public void myValidate() {
        // make sure new name is not a duplicate of an existing category
        if ((isAdd() || !category.getName().equals(bean.getName())) &&
            category.getWeblog().hasCategory(bean.getName())) {
            addError("categoryForm.error.duplicateName", bean.getName());
        }
    }

    public CategoryBean getBean() {
        return bean;
    }

    public void setBean(CategoryBean bean) {
        this.bean = bean;
    }
    
}
