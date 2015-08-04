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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;


/**
 * Edit a new or existing bookmark (blogroll item).
 */
public class BookmarkEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(BookmarkEdit.class);

    // bean for managing form data
    private BookmarkBean bean = new BookmarkBean();

    // the bookmark we are adding or editing
    private WeblogBookmark bookmark = null;

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.POST;
    }

    public BookmarkEdit() {
        this.desiredMenu = "editor";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    public void myPrepare() {
        if (StringUtils.isEmpty(bean.getId())) {
            // Create and initialize new, not-yet-saved WeblogBookmark
            bookmark = new WeblogBookmark();
            bookmark.setWeblog(getActionWeblog());
        } else {
            // existing bookmark, retrieve its info from DB
            try {
                WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
                bookmark = wmgr.getBookmark(getBean().getId());
            } catch (WebloggerException ex) {
                addError("generic.error.check.logs");
                log.error("Error looking up bookmark" + getBean().getId(), ex);
            }
        }
    }
    
    
    @SkipValidation
    public String execute() {
        if (!isAdd()) {
            // load bean with database values during initial load
            getBean().copyFrom(getBookmark());
        }
        return INPUT;
    }

    
    public String save() {
        myValidate();

        if(!hasActionErrors()) {
            try {
                getBean().copyTo(bookmark);
                WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
                wmgr.saveBookmark(bookmark);
                WebloggerFactory.getWeblogger().flush();
                CacheManager.invalidate(bookmark);
                CacheManager.invalidate(bookmark.getWeblog());
                addMessage(isAdd() ? "bookmarkForm.created" : "bookmarkForm.updated",
                        getBookmark().getName());
                return SUCCESS;

            } catch(Exception ex) {
                log.error("Error saving bookmark", ex);
                addError("generic.error.check.logs");
            }
        }
        
        return INPUT;
    }

    public void myValidate() {
        // if name new or changed, check new name doesn't already exist
        if ((isAdd() || !getBean().getName().equals(bookmark.getName()))
             && bookmark.getWeblog().hasBookmark(getBean().getName())) {
                addError("bookmarkForm.error.duplicateName", getBean().getUrl());
        }
    }

    private boolean isAdd() {
        return actionName.equals("bookmarkAdd");
    }

    public BookmarkBean getBean() {
        return bean;
    }

    public void setBean(BookmarkBean bean) {
        this.bean = bean;
    }

    public WeblogBookmark getBookmark() {
        return bookmark;
    }
}
