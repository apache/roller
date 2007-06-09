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

import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.DateUtil;


/**
 * Bean used to manage data submitted to WeblogConfig action.
 */
public class WeblogConfigBean {
    
    private String id = null;
    private String handle = null;
    private String name = null;
    private String description = null;
    private boolean enableBloggerApi = false;
    private String editorPage = null;
    private String blacklist = null;
    private boolean allowComments = false;
    private boolean defaultAllowComments = false;
    private String defaultCommentDays = "0";
    private boolean moderateComments = false;
    private boolean emailComments = false;
    private String emailFromAddress = null;
    private String emailAddress = null;
    private String locale = null;
    private String timeZone = null;
    private String defaultPlugins = null;
    private int entryDisplayCount = 15;
    private boolean active = true;
    private boolean commentModerationRequired = false;
    private boolean enableMultiLang = false;
    private boolean showAllLangs = true;
    private String pageModels = null;
    private String icon = null;
    private String about = null;
    
    private String bloggerCategoryId = null;
    private String defaultCategoryId = null;
    private String[] defaultPluginsArray = null;
    private boolean applyCommentDefaults = false;
    
    
    public void setId( String id ) {
        this.id = id;
    }
    
    public String getHandle() {
        return this.handle;
    }
    
    public void setHandle( String handle ) {
        this.handle = handle;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }
    
    public boolean getEnableBloggerApi() {
        return this.enableBloggerApi;
    }
    
    public void setEnableBloggerApi( boolean enableBloggerApi ) {
        this.enableBloggerApi = enableBloggerApi;
    }
    
    public String getEditorPage() {
        return this.editorPage;
    }
    
    public void setEditorPage( String editorPage ) {
        this.editorPage = editorPage;
    }
    
    public String getBlacklist() {
        return this.blacklist;
    }
    
    public void setBlacklist( String blacklist ) {
        this.blacklist = blacklist;
    }
    
    public boolean getAllowComments() {
        return this.allowComments;
    }
    
    public void setAllowComments( boolean allowComments ) {
        this.allowComments = allowComments;
    }
    
    public boolean getDefaultAllowComments() {
        return this.defaultAllowComments;
    }
    
    public void setDefaultAllowComments( boolean defaultAllowComments ) {
        this.defaultAllowComments = defaultAllowComments;
    }
    
    public String getDefaultCommentDays() {
        return this.defaultCommentDays;
    }
    
    public void setDefaultCommentDays( String defaultCommentDays ) {
        this.defaultCommentDays = defaultCommentDays;
    }
    
    public boolean getModerateComments() {
        return this.moderateComments;
    }
    
    public void setModerateComments( boolean moderateComments ) {
        this.moderateComments = moderateComments;
    }
    
    public boolean getEmailComments() {
        return this.emailComments;
    }
    
    public void setEmailComments( boolean emailComments ) {
        this.emailComments = emailComments;
    }
    
    public String getEmailFromAddress() {
        return this.emailFromAddress;
    }
    
    public void setEmailFromAddress( String emailFromAddress ) {
        this.emailFromAddress = emailFromAddress;
    }
    
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    public void setEmailAddress( String emailAddress ) {
        this.emailAddress = emailAddress;
    }
    
    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale( String locale ) {
        this.locale = locale;
    }
    
    public String getTimeZone() {
        return this.timeZone;
    }
    
    public void setTimeZone( String timeZone ) {
        this.timeZone = timeZone;
    }
    
    public int getEntryDisplayCount() {
        return this.entryDisplayCount;
    }
    
    public void setEntryDisplayCount( int entryDisplayCount ) {
        this.entryDisplayCount = entryDisplayCount;
    }
    
    public boolean getCommentModerationRequired() {
        return this.commentModerationRequired;
    }
    
    public void setCommentModerationRequired( boolean commentModerationRequired ) {
        this.commentModerationRequired = commentModerationRequired;
    }
    
    public boolean isEnableMultiLang() {
        return this.enableMultiLang;
    }
    
    public void setEnableMultiLang( boolean enableMultiLang ) {
        this.enableMultiLang = enableMultiLang;
    }
    
    public boolean isShowAllLangs() {
        return this.showAllLangs;
    }
    
    public void setShowAllLangs( boolean showAllLangs ) {
        this.showAllLangs = showAllLangs;
    }
    
    public String getPageModels() {
        return this.pageModels;
    }
    
    public void setPageModels( String pageModels ) {
        this.pageModels = pageModels;
    }
    
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
    
    public String getBloggerCategoryId() {
        return bloggerCategoryId;
    }
    
    public void setBloggerCategoryId(String bloggerCategoryId) {
        this.bloggerCategoryId = bloggerCategoryId;
    }
    
    public String getDefaultCategoryId() {
        return defaultCategoryId;
    }
    
    public void setDefaultCategoryId(String defeaultCategoryId) {
        this.defaultCategoryId = defeaultCategoryId;
    }
    
    public String[] getDefaultPluginsArray() {
        return defaultPluginsArray;
    }
    
    public void setDefaultPluginsArray(String[] strings) {
        defaultPluginsArray = strings;
    }
    
    public boolean getApplyCommentDefaults() {
        return applyCommentDefaults;
    }
    
    public void setApplyCommentDefaults(boolean applyCommentDefaults) {
        this.applyCommentDefaults = applyCommentDefaults;
    }
    
    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    
    public void copyFrom(Weblog dataHolder) {
        
        this.id = dataHolder.getId();
        this.handle = dataHolder.getHandle();
        this.name = dataHolder.getName();
        this.description = dataHolder.getDescription();
        this.enableBloggerApi = dataHolder.getEnableBloggerApi();
        this.editorPage = dataHolder.getEditorPage();
        this.blacklist = dataHolder.getBlacklist();
        this.allowComments = dataHolder.getAllowComments();
        this.defaultAllowComments = dataHolder.getDefaultAllowComments();
        this.defaultCommentDays = ""+dataHolder.getDefaultCommentDays();
        this.moderateComments = dataHolder.getModerateComments();
        this.emailComments = dataHolder.getEmailComments();
        this.emailFromAddress = dataHolder.getEmailFromAddress();
        this.emailAddress = dataHolder.getEmailAddress();
        this.locale = dataHolder.getLocale();
        this.timeZone = dataHolder.getTimeZone();
        this.defaultPlugins = dataHolder.getDefaultPlugins();
        this.entryDisplayCount = dataHolder.getEntryDisplayCount();
        setActive(dataHolder.getActive());
        this.commentModerationRequired = dataHolder.getCommentModerationRequired();
        this.enableMultiLang = dataHolder.isEnableMultiLang();
        this.showAllLangs = dataHolder.isShowAllLangs();
        this.pageModels = dataHolder.getPageModels();
        setIcon(dataHolder.getIconPath());
        setAbout(dataHolder.getAbout());
        
        if (dataHolder.getDefaultCategory() != null) {
            defaultCategoryId = dataHolder.getDefaultCategory().getId();
        }
        if (dataHolder.getBloggerCategory() != null) {
            bloggerCategoryId = dataHolder.getBloggerCategory().getId();
        }
        if (dataHolder.getDefaultPlugins() != null) {
            defaultPluginsArray = StringUtils.split(dataHolder.getDefaultPlugins(), ",");
        }
    }
    
    
    public void copyTo(Weblog dataHolder) {
        
        dataHolder.setName(this.name);
        dataHolder.setDescription(this.description);
        dataHolder.setEnableBloggerApi(this.enableBloggerApi);
        dataHolder.setEditorPage(this.editorPage);
        dataHolder.setBlacklist(this.blacklist);
        dataHolder.setAllowComments(this.allowComments);
        dataHolder.setDefaultAllowComments(this.defaultAllowComments);
        dataHolder.setModerateComments(this.moderateComments);
        dataHolder.setEmailComments(this.emailComments);
        dataHolder.setEmailFromAddress(this.emailFromAddress);
        dataHolder.setEmailAddress(this.emailAddress);
        dataHolder.setLocale(this.locale);
        dataHolder.setTimeZone(this.timeZone);
        dataHolder.setDefaultPlugins(this.defaultPlugins);
        dataHolder.setEntryDisplayCount(this.entryDisplayCount);
        dataHolder.setActive(this.getActive());
        dataHolder.setCommentModerationRequired(this.commentModerationRequired);
        dataHolder.setEnableMultiLang(this.enableMultiLang);
        dataHolder.setShowAllLangs(this.showAllLangs);
        dataHolder.setPageModels(this.pageModels);
        dataHolder.setIconPath(getIcon());
        dataHolder.setAbout(getAbout());
        
        dataHolder.setDefaultPlugins( StringUtils.join(this.defaultPluginsArray,",") );
        
        dataHolder.setDefaultCommentDays(Integer.parseInt(this.defaultCommentDays));
        
        dataHolder.setDefaultPlugins(StringUtils.join(this.defaultPluginsArray, ","));
    }
    
}
