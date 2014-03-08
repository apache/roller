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
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Add a new subfolder to an existing folder.
 */
public class FolderAdd extends UIAction {
    
    private static Log log = LogFactory.getLog(FolderAdd.class);
    
    // bean for managing form data
    private FolderBean bean = new FolderBean();
    
    
    public FolderAdd() {
        this.actionName = "folderAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "folderForm.add.title";
    }
    
    
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.ADMIN);
    }
    
    
    @SkipValidation
    public String execute() {
        return INPUT;
    }

    
    public String save() {
        // validation
        myValidate();
        
        if(!hasActionErrors())  {
            try {
                WeblogBookmarkFolder newFolder = new WeblogBookmarkFolder(
                        getActionWeblog().getBookmarkFolder(null),
                        getBean().getName(),
                        getBean().getDescription(),
                        getActionWeblog());

                // add new folder to weblog
                getActionWeblog().getBookmarkFolder(null).addFolder(newFolder);

                // save changes
                BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
                bmgr.saveFolder(newFolder);
                WebloggerFactory.getWeblogger().flush();

                // notify caches
                CacheManager.invalidate(newFolder);

                // TODO: i18n
                addMessage("folder added");

                // Set for next action
                getBean().setId(newFolder.getId());

                return SUCCESS;

            } catch(Exception ex) {
                log.error("Error saving new folder", ex);
                // TODO: i18n
                addError("Error saving new folder");
            }
        }

        
        return INPUT;
    }

    
    // TODO: validation
    public void myValidate() {
        
        // name is required, has max length, no html
        
        // make sure new name is not a duplicate of an existing folder
        if(getActionWeblog().getBookmarkFolder(null).hasFolder(getBean().getName())) {
            addError("folderForm.error.duplicateName", getBean().getName());
        }
    }
    
    
    public FolderBean getBean() {
        return bean;
    }

    public void setBean(FolderBean bean) {
        this.bean = bean;
    }
    
}
