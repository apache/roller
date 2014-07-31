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

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogBookmarkFolder;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Add a new bookmark to a folder.
 */
public class BookmarkAdd extends UIAction {
    
    private static Log log = LogFactory.getLog(BookmarkAdd.class);
    
    // the id of the folder we are adding the bookmark into
    private String folderId = null;
    
    // the folder we are adding the bookmark into
    private WeblogBookmarkFolder folder = null;
    
    // bean for managing form data
    private BookmarkBean bean = new BookmarkBean();

    public BookmarkAdd() {
        this.actionName = "bookmarkAdd";
        this.desiredMenu = "editor";
        this.pageTitle = "bookmarkForm.add.title";
    }

    public void myPrepare() {
        try {
            BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
            if(!StringUtils.isEmpty(getFolderId())) {
                setFolder(bmgr.getFolder(getFolderId()));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up folder", ex);
        }
    }
    
    
    @SkipValidation
    public String execute() {
        
        if(getFolder() == null) {
            addError("Cannot add bookmark to null folder");
            return ERROR;
        }
        
        return INPUT;
    }

    
    public String save() {
        
        if(getFolder() == null) {
            addError("Cannot add bookmark to null folder");
            return ERROR;
        }
        
        // validation
        myValidate();
        
        if(!hasActionErrors()) {
            try {
                WeblogBookmark newBookmark = new WeblogBookmark(getFolder(),
                        getBean().getName(),
                        getBean().getDescription(),
                        getBean().getUrl(),
                        getBean().getFeedUrl(),
                        getBean().getImage());

                BookmarkManager bmgr = WebloggerFactory.getWeblogger().getBookmarkManager();
                bmgr.saveBookmark(newBookmark);
                WebloggerFactory.getWeblogger().flush();

                CacheManager.invalidate(newBookmark);

                addMessage("bookmarkForm.created");

                // Set for next action
                getBean().setId(newBookmark.getId());

                return SUCCESS;

            } catch(Exception ex) {
                log.error("Error saving new bookmark", ex);
                addError("bookmarkForm.error.saving");
            }
        }

        
        return INPUT;
    }

    /**
     * Cancel.
     * 
     * @return the string
     */
    public String cancel() {
        return CANCEL;
    }
    
    public void myValidate() {
        // name is required, max length, no html
        if (getFolder().hasBookmarkOfName(getBean().getName())) {
            addError("bookmarkForm.error.duplicateName", getBean().getUrl());
        }
    }
    
    public boolean validURL(String url) {
        boolean valid = false;
        try {
            URL test = new URL(url);
            valid = true;
        } catch (MalformedURLException intentionallyIgnored) {}
        return valid;
    }
    
    
    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public WeblogBookmarkFolder getFolder() {
        return folder;
    }

    public void setFolder(WeblogBookmarkFolder folder) {
        this.folder = folder;
    }

    public BookmarkBean getBean() {
        return bean;
    }

    public void setBean(BookmarkBean bean) {
        this.bean = bean;
    }
  
}
