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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.validation.SkipValidation;

import javax.servlet.http.HttpServletResponse;


/**
 * Edit a new or existing folder.
 */
// TODO: make this work @AllowedMethods({"execute","save"})
public class FolderEdit extends UIAction implements ServletResponseAware {

    private static Log log = LogFactory.getLog(FolderEdit.class);

    // bean for managing form data
    private FolderBean bean = new FolderBean();

    // the id of the folder we are working with
    private String folderId = null;

    // the folder we are adding or editing
    private WeblogBookmarkFolder folder = null;

    private HttpServletResponse httpServletResponse;


    public FolderEdit() {
        this.desiredMenu = "editor";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    // load folder to edit
    @Override
    public void myPrepare() {
        if (StringUtils.isEmpty(bean.getId())) {
            // Create and initialize new folder but don't save yet
            folder = new WeblogBookmarkFolder();
            folder.setWeblog(getActionWeblog());
        } else {
            // retrieve existing folder data from DB
            try {
                BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
                folder = bmgr.getFolder(getBean().getId());
            } catch (WebloggerException ex) {
                log.error("Error looking up folder", ex);
            }
        }
    }

    @Override
    public void setServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    /**
     * Show folder edit page.
     */
    @SkipValidation
    @Override
    public String execute() {
        if (!isAdd()) {
            // load bean with database values during initial load
            getBean().copyFrom(folder);
        }
        return INPUT;
    }

    /**
     * Save updated folder data.
     */
    public String save() {
        myValidate();
        
        if(!hasActionErrors()) {
            try {
                // copy updated attributes
                getBean().copyTo(folder);

                // save changes
                BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
                bmgr.saveFolder(folder);
                WebloggerFactory.getWeblogger().flush();

                // notify caches
                CacheManager.invalidate(folder);

                if (isAdd()) {
                    // if adding, move to new folder upon save.
                    folderId = folder.getId();
                } else {
                    // create message added in Bookmarks class as it's reached via a
                    // redirect in struts.xml instead of a chain.
                    addMessage("folderForm.updated");
                }

                httpServletResponse.addHeader("folderId", folderId );

                return SUCCESS;

            } catch(Exception ex) {
                log.error("Error saving folder", ex);
                addError("generic.error.check.logs");
            }
        }
        
        return INPUT;
    }

    public void myValidate() {
        // make sure new name is not a duplicate of an existing folder
        if ( isAdd() || !getBean().getName().equals(folder.getName()) ) {
            if (folder.getWeblog().hasBookmarkFolder(getBean().getName())) {
                addError("folderForm.error.duplicateName", getBean().getName());
            }
        }
    }


    private boolean isAdd() {
        return actionName.equals("folderAdd");
    }

    public FolderBean getBean() {
        return bean;
    }

    public void setBean(FolderBean bean) {
        this.bean = bean;
    }
    
    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }
}
