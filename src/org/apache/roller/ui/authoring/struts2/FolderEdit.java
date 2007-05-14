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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.FolderData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Edit an existing folder.
 */
public class FolderEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(FolderEdit.class);
    
    // the folder we are editing
    private FolderData folder = null;
    
    // bean for managing form data
    private FolderBean bean = new FolderBean();
    
    
    public FolderEdit() {
        this.actionName = "folderEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "folderForm.edit.title";
    }
    
    
    // author perms required
    public short requiredWeblogPermissions() {
        return PermissionsData.AUTHOR;
    }
    
    
    // load folder to edit
    public void myPrepare() {
        try {
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            if(!StringUtils.isEmpty(getBean().getId())) {
                setFolder(bmgr.getFolder(getBean().getId()));
            }
        } catch (RollerException ex) {
            log.error("Error looking up folder", ex);
        }
    }
    
    
    /**
     * Show folder edit page.
     */
    public String execute() {
        
        if(getFolder() == null) {
            // TODO: i18n
            addError("Cannot edit null folder");
            return ERROR;
        }
        
        // make sure bean is properly loaded from pojo data
        getBean().copyFrom(getFolder());
        
        return INPUT;
    }

    
    /**
     * Save updated folder data.
     */
    public String save() {
        
        if(getFolder() == null) {
            // TODO: i18n
            addError("Cannot edit null folder");
            return ERROR;
        }
        
        // validation
        myValidate();
        
        if(!hasActionErrors()) try {
            
            // copy updated attributes
            getBean().copyTo(getFolder());
            
            // save changes
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            bmgr.saveFolder(getFolder());
            RollerFactory.getRoller().flush();
            
            // notify caches
            CacheManager.invalidate(getFolder());
            
            // TODO: i18n
            addMessage("folder updated");
            
        } catch(Exception ex) {
            log.error("Error saving folder", ex);
            // TODO: i18n
            addError("Error saving folder");
        }
        
        return INPUT;
    }

    
    // TODO: validation
    public void myValidate() {
        
        // name is required, has max length, no html
        
        // make sure new name is not a duplicate of an existing folder
        FolderData parent = getFolder().getParent();
        if(parent != null && parent.hasFolder(getBean().getName())) {
            addError("folderForm.error.duplicateName", getBean().getName());
        }
    }
    

    public FolderData getFolder() {
        return folder;
    }

    public void setFolder(FolderData folder) {
        this.folder = folder;
    }

    public FolderBean getBean() {
        return bean;
    }

    public void setBean(FolderBean bean) {
        this.bean = bean;
    }
    
}
