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

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.pojos.BookmarkData;
import org.apache.roller.pojos.PermissionsData;
import org.apache.roller.ui.core.util.struts2.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Add a new bookmark to a folder.
 */
public class BookmarkEdit extends UIAction {
    
    private static Log log = LogFactory.getLog(BookmarkEdit.class);
    
    // the bookmark we are editing
    private BookmarkData bookmark = null;
    
    // bean for managing form data
    private BookmarkBean bean = new BookmarkBean();
    
    
    public BookmarkEdit() {
        this.actionName = "bookmarkEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "bookmarkForm.edit.title";
    }
    
    
    public short requiredWeblogPermissions() {
        return PermissionsData.ADMIN;
    }
    
    
    public void myPrepare() {
        try {
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            if(!StringUtils.isEmpty(getBean().getId())) {
                setBookmark(bmgr.getBookmark(getBean().getId()));
            }
        } catch (RollerException ex) {
            log.error("Error looking up bookmark - "+getBean().getId(), ex);
        }
    }
    
    
    public String execute() {
        
        if(getBookmark() == null) {
            // TODO: i18n
            addError("Cannot edit null bookmark");
            return ERROR;
        }
        
        // make sure bean is properly loaded with pojo data
        getBean().copyFrom(getBookmark());
        
        return INPUT;
    }

    
    public String save() {
        
        if(getBookmark() == null) {
            // TODO: i18n
            addError("Cannot edit null bookmark");
            return ERROR;
        }
        
        // validation
        myValidate();
        
        if(!hasActionErrors()) try {
            
            getBean().copyTo(getBookmark());
            
            BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
            bmgr.saveBookmark(getBookmark());
            RollerFactory.getRoller().flush();
            
            CacheManager.invalidate(getBookmark());
            
            // TODO: i18n
            addMessage("bookmark updated");
            
        } catch(Exception ex) {
            log.error("Error saving bookmark", ex);
            // TODO: i18n
            addError("Error saving bookmark");
        }
        
        return INPUT;
    }

    
    public void myValidate() {
        if (StringUtils.isNotEmpty(getBean().getUrl()) && !validURL(getBean().getUrl())) {
            addError("bookmarkgetBean().error.invalidURL", getBean().getUrl());
        }
        if (StringUtils.isNotEmpty(getBean().getFeedUrl()) && !validURL(getBean().getFeedUrl())) {
            addError("bookmarkgetBean().error.invalidURL", getBean().getFeedUrl());
        }
        if (StringUtils.isNotEmpty(getBean().getImage()) && !validURL(getBean().getImage())) {
            addError("bookmarkgetBean().error.invalidURL", getBean().getImage());
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
    
    
    public BookmarkData getBookmark() {
        return bookmark;
    }

    public void setBookmark(BookmarkData bookmark) {
        this.bookmark = bookmark;
    }

    public BookmarkBean getBean() {
        return bean;
    }

    public void setBean(BookmarkBean bean) {
        this.bean = bean;
    }
    
}
