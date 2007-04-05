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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.RollerException;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.RollerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.PluginManager;
import org.apache.roller.business.Roller;
import org.apache.roller.business.themes.ThemeManager;
import org.apache.roller.business.WeblogManager;

/**
 * Website has many-to-many association with users. Website has one-to-many and
 * one-direction associations with weblog entries, weblog categories, folders and
 * other objects. Use UserManager to create, fetch, update and retreive websites.
 *
 * @author David M Johnson
 *
 * @ejb:bean name="WebsiteData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true"  table="website"
 * @hibernate.cache usage="read-write"
 */
public class WebsiteData implements Serializable {
    public static final long serialVersionUID = 206437645033737127L;
    
    private static Log log = LogFactory.getLog(WebsiteData.class);
    
    // Simple properties
    private String  id               = null;
    private String  handle           = null;
    private String  name             = null;
    private String  description      = null;
    private String  defaultPageId    = "dummy";
    private String  weblogDayPageId  = "dummy";
    private Boolean enableBloggerApi = Boolean.TRUE;
    private String  editorPage       = null;
    private String  blacklist        = null;
    private Boolean allowComments    = Boolean.TRUE;
    private Boolean emailComments    = Boolean.FALSE;
    private String  emailFromAddress = null;
    private String  emailAddress     = null;
    private String  editorTheme      = null;
    private String  locale           = null;
    private String  timeZone         = null;
    private String  defaultPlugins   = null;
    private Boolean enabled          = Boolean.TRUE;
    private Boolean active           = Boolean.TRUE;
    private Date    dateCreated      = new java.util.Date();
    private Boolean defaultAllowComments = Boolean.TRUE;
    private int     defaultCommentDays = 0;
    private Boolean moderateComments  = Boolean.FALSE;
    private int     entryDisplayCount = 15;
    private Date    lastModified     = new Date();
    private String  pageModels       = new String();
    private boolean enableMultiLang = false;
    private boolean showAllLangs = true;
    private String customStylesheetPath = null;
    
    
    // Associated objects
    private UserData           creator = null; 
    private List               permissions = new ArrayList();
    private WeblogCategoryData bloggerCategory = null;
    private WeblogCategoryData defaultCategory = null;
    
    private Map initializedPlugins = null;
    
    public WebsiteData() {    
    }
    
    public WebsiteData(
            String handle,
            UserData creator,
            String name,
            String desc,
            String email,
            String emailFrom,
            String editorTheme,
            String locale,
            String timeZone) {
        
        this.handle = handle;
        this.creator = creator;
        this.name = name;
        this.description = desc;
        this.emailAddress = email;
        this.emailFromAddress = emailFrom;
        this.editorTheme = editorTheme;
        this.locale = locale;
        this.timeZone = timeZone;
    }
    
    public WebsiteData(WebsiteData otherData) {
        this.setData(otherData);
    }
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.handle);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.emailAddress);
        buf.append(", ").append(this.locale);
        buf.append(", ").append(this.timeZone);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WebsiteData != true) return false;
        WebsiteData o = (WebsiteData)other;
        return new EqualsBuilder()
            .append(getHandle(), o.getHandle()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getHandle())
            .toHashCode();
    } 
    
    /**
     * @hibernate.bag lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="website_id"
     * @hibernate.collection-one-to-many
     *    class="org.apache.roller.pojos.PermissionsData"
     */
    public List getPermissions() {
        return permissions;
    }
    public void setPermissions(List perms) {
        permissions = perms;
    }
    /**
     * Remove permission from collection.
     */
    public void removePermission(PermissionsData perms) {
        permissions.remove(perms);
    }
    
    
    /**
     * Get the Theme object in use by this weblog, or null if no theme selected.
     */
    public WeblogTheme getTheme() throws RollerException {
        
        // let the ThemeManager handle it
        ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
        return themeMgr.getTheme(this);
    }
    
    
    /**
     * Lookup the default page for this website.
     */
    public ThemeTemplate getDefaultPage() throws RollerException {
        
        // look for the page in our Theme
        Theme weblogTheme = getTheme();
        if(weblogTheme != null) {
            return weblogTheme.getDefaultTemplate();
        }
        
        return null;
    }
    
    
    /**
     * Lookup a Template for this website by action.
     * 
     * @roller.wrapPojoMethod type="pojo"
     */
    public ThemeTemplate getPageByAction(String action) throws RollerException {
        
        if(action == null)
            return null;
        
        // look for the page in our Theme
        Theme weblogTheme = getTheme();
        if(weblogTheme != null) {
            return weblogTheme.getTemplateByAction(action);
        }
        
        return null;
    }
    
    
    /**
     * Lookup a Template for this website by name.
     * @roller.wrapPojoMethod type="pojo"
     */
    public ThemeTemplate getPageByName(String name) throws RollerException {
        
        if(name == null)
            return null;
        
        // look for the page in our Theme
        Theme weblogTheme = getTheme();
        if(weblogTheme != null) {
            return weblogTheme.getTemplateByName(name);
        }
        
        return null;
    }
    
    
    /**
     * Lookup a template for this website by link.
     * @roller.wrapPojoMethod type="pojo"
     */
    public ThemeTemplate getPageByLink(String link) throws RollerException {
        
        if(link == null)
            return null;
        
        // look for the page in our Theme
        Theme weblogTheme = getTheme();
        if(weblogTheme != null) {
            return weblogTheme.getTemplateByLink(link);
        }
        
        return null;
    }
    
    
    /**
     * Get a list of all pages that are part of this website.
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.Template"
     */
    public List getPages() throws RollerException {
        
        // look for the pages in our Theme
        Theme weblogTheme = getTheme();
        if(weblogTheme != null) {
            return weblogTheme.getTemplates();
        }
        
        return Collections.EMPTY_LIST;
    }
    
    
    /**
     * Id of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.id column="id"
     *  generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId() {
        return this.id;
    }
    
    /** @ejb:persistent-field */
    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }
    
    /**
     * Short URL safe string that uniquely identifies the website.
     * @ejb:persistent-field
     * @hibernate.property column="handle" non-null="true" unique="true"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getHandle() {
        return this.handle;
    }
    
    /** @ejb:persistent-field */
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /**
     * Name of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName() {
        return this.name;
    }
    
    /** @ejb:persistent-field */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Description
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public String getDescription() {
        return this.description;
    }
    
    /** @ejb:persistent-field */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Original creator of website
     *
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="userid" cascade="none" not-null="true"
     */
    public org.apache.roller.pojos.UserData getCreator() {
        return creator;
    }
    
    /** @ejb:persistent-field */
    public void setCreator( org.apache.roller.pojos.UserData ud ) {
        creator = ud;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultpageid" non-null="true" unique="false"
     */
    public String getDefaultPageId() {
        return this.defaultPageId;
    }
    
    /**
     * @ejb:persistent-field
     */
    public void setDefaultPageId(String defaultPageId) {
        this.defaultPageId = defaultPageId;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @deprecated
     * @ejb:persistent-field
     * @hibernate.property column="weblogdayid" non-null="true" unique="false"
     */
    public String getWeblogDayPageId() {
        return this.weblogDayPageId;
    }
    
    /**
     * @deprecated
     * @ejb:persistent-field
     */
    public void setWeblogDayPageId(String weblogDayPageId) {
        this.weblogDayPageId = weblogDayPageId;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="enablebloggerapi" non-null="true" unique="false"
     */
    public Boolean getEnableBloggerApi() {
        return this.enableBloggerApi;
    }
    
    /** @ejb:persistent-field */
    public void setEnableBloggerApi(Boolean enableBloggerApi) {
        this.enableBloggerApi = enableBloggerApi;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="bloggercatid" non-null="false" cascade="none"
     */
    public WeblogCategoryData getBloggerCategory() {
        return bloggerCategory;
    }
    
    /** @ejb:persistent-field */
    public void setBloggerCategory(WeblogCategoryData bloggerCategory) {
        this.bloggerCategory = bloggerCategory;
    }
    
    /**
     * By default,the default category for a weblog is the root and all macros
     * work with the top level categories that are immediately under the root.
     * Setting a different default category allows you to partition your weblog.
     *
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="defaultcatid" non-null="false" cascade="none"
     */
    public WeblogCategoryData getDefaultCategory() {
        return defaultCategory;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultCategory(WeblogCategoryData defaultCategory) {
        this.defaultCategory = defaultCategory;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="editorpage" non-null="true" unique="false"
     */
    public String getEditorPage() {
        return this.editorPage;
    }
    
    /** @ejb:persistent-field */
    public void setEditorPage(String editorPage) {
        this.editorPage = editorPage;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="blacklist" non-null="true" unique="false"
     */
    public String getBlacklist() {
        return this.blacklist;
    }
    
    /** @ejb:persistent-field */
    public void setBlacklist(String blacklist) {
        this.blacklist = blacklist;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="allowcomments" non-null="true" unique="false"
     */
    public Boolean getAllowComments() {
        return this.allowComments;
    }
    
    /** @ejb:persistent-field */
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultallowcomments" non-null="true" unique="false"
     */
    public Boolean getDefaultAllowComments() {
        return defaultAllowComments;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultAllowComments(Boolean defaultAllowComments) {
        this.defaultAllowComments = defaultAllowComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultcommentdays" non-null="true" unique="false"
     */
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="commentmod" non-null="true" unique="false"
     */
    public Boolean getModerateComments() {
        return moderateComments;
    }
    
    /** @ejb:persistent-field */
    public void setModerateComments(Boolean moderateComments) {
        this.moderateComments = moderateComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="emailcomments" non-null="true" unique="false"
     */
    public Boolean getEmailComments() {
        return this.emailComments;
    }
    
    /** @ejb:persistent-field */
    public void setEmailComments(Boolean emailComments) {
        this.emailComments = emailComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="emailfromaddress" non-null="true" unique="false"
     */
    public String getEmailFromAddress() {
        return this.emailFromAddress;
    }
    
    /** @ejb:persistent-field */
    public void setEmailFromAddress(String emailFromAddress) {
        this.emailFromAddress = emailFromAddress;
    }
    
    /**
     * @ejb:persistent-field
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="emailaddress" non-null="true" unique="false"
     */
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    /** @ejb:persistent-field */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    /**
     * EditorTheme of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="editortheme" non-null="true" unique="false"
     */
    public String getEditorTheme() {
        return this.editorTheme;
    }
    
    /** @ejb:persistent-field */
    public void setEditorTheme(String editorTheme) {
        this.editorTheme = editorTheme;
    }
    
    /**
     * Locale of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="locale" non-null="true" unique="false"
     */
    public String getLocale() {
        return this.locale;
    }
    
    /** @ejb:persistent-field */
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Timezone of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="timeZone" non-null="true" unique="false"
     */
    public String getTimeZone() {
        return this.timeZone;
    }
    
    /** @ejb:persistent-field */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="datecreated" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public Date getDateCreated() {
        if (dateCreated == null) {
            return null;
        } else {
            return (Date)dateCreated.clone();
        }
    }
    /** @ejb:persistent-field */
    public void setDateCreated(final Date date) {
        if (date != null) {
            dateCreated = (Date)date.clone();
        } else {
            dateCreated = null;
        }
    }
    
    /**
     * Comma-delimited list of user's default Plugins.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultplugins" non-null="false" unique="false"
     */
    public String getDefaultPlugins() {
        return defaultPlugins;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultPlugins(String string) {
        defaultPlugins = string;
    }
    
    
    /**
     * Set bean properties based on other bean.
     */
    public void setData(WebsiteData other) {
        
        this.id = other.getId();
        this.name = other.getName();
        this.handle = other.getHandle();
        this.description = other.getDescription();
        this.creator = other.getCreator();
        this.defaultPageId = other.getDefaultPageId();
        this.weblogDayPageId = other.getWeblogDayPageId();
        this.enableBloggerApi = other.getEnableBloggerApi();
        this.bloggerCategory = other.getBloggerCategory();
        this.defaultCategory = other.getDefaultCategory();
        this.editorPage = other.getEditorPage();
        this.blacklist = other.getBlacklist();
        this.allowComments = other.getAllowComments();
        this.emailComments = other.getEmailComments();
        this.emailAddress = other.getEmailAddress();
        this.emailFromAddress = other.getEmailFromAddress();
        this.editorTheme = other.getEditorTheme();
        this.locale = other.getLocale();
        this.timeZone = other.getTimeZone();
        this.defaultPlugins = other.getDefaultPlugins();
        this.enabled = other.getEnabled();
        this.dateCreated = other.getDateCreated();
        this.entryDisplayCount = other.getEntryDisplayCount();
        this.active = other.getActive();
        this.lastModified = other.getLastModified();
    }
    
    /**
     * Parse locale value and instantiate a Locale object,
     * otherwise return default Locale.
     *
     * @roller.wrapPojoMethod type="simple"
     * @return Locale
     */
    public Locale getLocaleInstance() {
        if (locale != null) {
            String[] localeStr = StringUtils.split(locale,"_");
            if (localeStr.length == 1) {
                if (localeStr[0] == null) localeStr[0] = "";
                return new Locale(localeStr[0]);
            } else if (localeStr.length == 2) {
                if (localeStr[0] == null) localeStr[0] = "";
                if (localeStr[1] == null) localeStr[1] = "";
                return new Locale(localeStr[0], localeStr[1]);
            } else if (localeStr.length == 3) {
                if (localeStr[0] == null) localeStr[0] = "";
                if (localeStr[1] == null) localeStr[1] = "";
                if (localeStr[2] == null) localeStr[2] = "";
                return new Locale(localeStr[0], localeStr[1], localeStr[2]);
            }
        }
        return Locale.getDefault();
    }
    
    /**
     * Return TimeZone instance for value of timeZone,
     * otherwise return system default instance.
     *
     * @roller.wrapPojoMethod type="simple"
     * @return TimeZone
     */
    public TimeZone getTimeZoneInstance() {
        if (timeZone == null) {
            if (TimeZone.getDefault() != null) {
                this.setTimeZone( TimeZone.getDefault().getID() );
            } else {
                this.setTimeZone("America/New_York");
            }
        }
        return TimeZone.getTimeZone(timeZone);
    }
    
    
    /**
     * Returns true if user has all permissions specified by mask.
     */
    public boolean hasUserPermissions(UserData user, short mask) {
        // look for user in website's permissions
        PermissionsData userPerms = null;
        Iterator iter = getPermissions().iterator();
        while (iter.hasNext()) {
            PermissionsData perms = (PermissionsData) iter.next();
            if (perms.getUser().getId().equals(user.getId())) {
                userPerms = perms;
                break;
            }
        }
        // if we found one, does it satisfy the mask?
        if (userPerms != null && !userPerms.isPending()) {
            if (userPerms != null && (userPerms.getPermissionMask() & mask) == mask) {
                return true;
            }
        }
        // otherwise, check to see if user is a global admin
        if (user != null && user.hasRole("admin")) return true;
        return false;
    }
    
    /** Get number of users associated with website */
    public int getUserCount() {
        return getPermissions().size();
    }
    
    /** No-op needed to please XDoclet generated code */
    private int userCount = 0;
    public void setUserCount(int userCount) {
        // no-op
    }
    
    public int getAdminUserCount() {
        int count = 0;
        PermissionsData userPerms = null;
        Iterator iter = getPermissions().iterator();
        while (iter.hasNext()) {
            PermissionsData perms = (PermissionsData) iter.next();
            if (perms.getPermissionMask() == PermissionsData.ADMIN) {
                count++;
            }
        }
        return count;
    }
    
    /** No-op needed to please XDoclet generated code */
    private int adminUserCount = 0;
    public void setAdminUserCount(int adminUserCount) {
        // no-op
    }
    
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="displaycnt" not-null="true"
     */
    public int getEntryDisplayCount() {
        return entryDisplayCount;
    }
    
    /**
     * @ejb:persistent-field
     */
    public void setEntryDisplayCount(int entryDisplayCount) {
        this.entryDisplayCount = entryDisplayCount;
    }
    
    /**
     * Set to FALSE to completely disable and hide this weblog from public view.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="isenabled" non-null="true" unique="false"
     */
    public Boolean getEnabled() {
        return this.enabled;
    }
    
    /** @ejb:persistent-field */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Set to FALSE to exclude this weblog from community areas such as the
     * front page and the planet page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="isactive" not-null="true"
     */
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    /**
     * Returns true if comment moderation is required by website or config.
     */ 
    public boolean getCommentModerationRequired() { 
        return (getModerateComments().booleanValue()
         || RollerRuntimeConfig.getBooleanProperty("users.moderation.required"));
    }
    
    /** No-op */
    public void setCommentModerationRequired(boolean modRequired) {}    

    
    /**
     * The last time any visible part of this weblog was modified.
     * This includes a change to weblog settings, entries, themes, templates, 
     * comments, categories, bookmarks, folders, etc.
     *
     * Pings and Referrers are explicitly not included because pings to not
     * affect visible changes to a weblog, and referrers change so often that
     * it would diminish the usefulness of the attribute.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="lastmodified" not-null="true"
     */
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
  
    
    /**
     * Is multi-language blog support enabled for this weblog?
     *
     * If false then urls with various locale restrictions should fail.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="enablemultilang" not-null="true"
     */
    public boolean isEnableMultiLang() {
        return enableMultiLang;
    }

    public void setEnableMultiLang(boolean enableMultiLang) {
        this.enableMultiLang = enableMultiLang;
    }
    
    
    /**
     * Should the default weblog view show entries from all languages?
     *
     * If false then the default weblog view only shows entry from the
     * default locale chosen for this weblog.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="showalllangs" not-null="true"
     */
    public boolean isShowAllLangs() {
        return showAllLangs;
    }

    public void setShowAllLangs(boolean showAllLangs) {
        this.showAllLangs = showAllLangs;
    }
    
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     */
    public String getURL() {
        // TODO: ATLAS reconcile entry.getPermaLink() with new URLs
        String relPath = RollerRuntimeConfig.getRelativeContextURL();
        return relPath + "/" + getHandle();
        //return URLUtilities.getWeblogURL(this, null, false);
    }
    public void setURL(String url) {
        // noop
    }
    
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     */
    public String getAbsoluteURL() {
        // TODO: ATLAS reconcile entry.getPermaLink() with new URLs
        String relPath = RollerRuntimeConfig.getAbsoluteContextURL();
        return relPath + "/" + getHandle();
        //return URLUtilities.getWeblogURL(this, null, true);
    }
    public void setAbsoluteURL(String url) {
        // noop
    }
    
    
    /**
     * Comma-separated list of additional page models to be created when this
     * weblog is rendered.
     *
     * @ejb:persistent-field
     * @hibernate.property column="pagemodels" not-null="false"
     */
    public String getPageModels() {
        return pageModels;
    }
    public void setPageModels(String pageModels) {
        this.pageModels = pageModels;
    }

    
    /**
     * The path under the weblog's resources to a stylesheet override.
     *
     * @hibernate.property column="customstylesheet" not-null="false"
     */
    public String getCustomStylesheetPath() {
        return customStylesheetPath;
    }

    public void setCustomStylesheetPath(String customStylesheetPath) {
        this.customStylesheetPath = customStylesheetPath;
    }
    
    
    public String getCustomStylesheet() {
        try {
            Theme weblogTheme = getTheme();
            if(weblogTheme != null) {
                return weblogTheme.getCustomStylesheet();
            } else {
                return getCustomStylesheetPath();
            }
        } catch(RollerException re) {
            // hmmm, some exception getting theme
            return null;
        }
    }
    
    // no-op to please xdoclet
    public void setCustomStylesheet(String noop) {}
    
    /**
     * Get initialized plugins for use during rendering process.
     */
    public Map getInitializedPlugins() {
        if (initializedPlugins == null) {
            try {
                Roller roller = RollerFactory.getRoller();
                PluginManager ppmgr = roller.getPagePluginManager();
                initializedPlugins = ppmgr.getWeblogEntryPlugins(this); 
            } catch (Exception e) {
                this.log.error("ERROR: initializing plugins");
            }
        }
        return initializedPlugins;
    }
    
    /** 
     * Get weblog entry specified by anchor or null if no such entry exists.
     * @param anchor Weblog entry anchor
     * @return Weblog entry specified by anchor
     * @roller.wrapPojoMethod type="simple"
     */
    public WeblogEntryData getWeblogEntry(String anchor) {
        WeblogEntryData entry = null;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            entry = wmgr.getWeblogEntryByAnchor(this, anchor);
        } catch (RollerException e) {
            this.log.error("ERROR: getting entry by anchor");
        }
        return entry;
    }
    
    /**
     * Returns categories under the default category of the weblog.
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogCategoryData"
     */
    public Set getWeblogCategories() {
        Set ret = new HashSet();
//        try {           
            WeblogCategoryData category = this.getDefaultCategory();
            ret = category.getWeblogCategories();
//        } catch (RollerException e) {
//            log.error("ERROR: fetching categories", e);
//        }
        return ret;
    }
    
    
    /**
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogCategoryData"
     */
    public Set getWeblogCategories(String categoryPath) {
        Set ret = new HashSet();
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();            
            WeblogCategoryData category = null;
            if (categoryPath != null && !categoryPath.equals("nil")) {
                category = wmgr.getWeblogCategoryByPath(this, categoryPath);
            } else {
                category = this.getDefaultCategory();
            }
            ret = category.getWeblogCategories();
        } catch (RollerException e) {
            log.error("ERROR: fetching categories for path: " + categoryPath, e);
        }
        return ret;
    }

    
    /**
     * @roller.wrapPojoMethod type="pojo" class="org.apache.roller.pojos.WeblogCategoryData"
     */
    public WeblogCategoryData getWeblogCategory(String categoryPath) {
        WeblogCategoryData category = null;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            if (categoryPath != null && !categoryPath.equals("nil")) {
                category = wmgr.getWeblogCategoryByPath(this, categoryPath);
            } else {
                category = this.getDefaultCategory();
            }
        } catch (RollerException e) {
            log.error("ERROR: fetching category at path: " + categoryPath, e);
        }
        return category;
    }
    
    
    /**
     * Get up to 100 most recent published entries in weblog.
     * @param cat Category path or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogEntryData"
     */
    public List getRecentWeblogEntries(String cat, int length) {  
        if (cat != null && "nil".equals(cat)) cat = null;
        if (length > 100) length = 100;
        List recentEntries = new ArrayList();
        if (length < 1) return recentEntries;
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            recentEntries = wmgr.getWeblogEntries(
                    this, 
                    null,       // user
                    null,       // startDate
                    null,       // endDate
                    cat,        // cat or null
                    null, 
                    WeblogEntryData.PUBLISHED, 
                    null,       // text
                    "pubTime",  // sortby
                    null,
                    null, 
                    0,
                    length); 
        } catch (RollerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return recentEntries;
    }
    
    /**
     * Get up to 100 most recent published entries in weblog.
     * @param cat Category path or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogEntryData"
     */
    public List getRecentWeblogEntriesByTag(String tag, int length) {  
        if (tag != null && "nil".equals(tag)) tag = null;
        if (length > 100) length = 100;
        List recentEntries = new ArrayList();
        List tags = new ArrayList();
        if (tag != null) {
            tags.add(tag);
        }
        if (length < 1) return recentEntries;
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            recentEntries = wmgr.getWeblogEntries(
                    this, 
                    null,       // user
                    null,       // startDate
                    null,       // endDate
                    null,       // cat or null
                    tags,       //  
                    WeblogEntryData.PUBLISHED, 
                    null,       // text
                    "pubTime",  // sortby
                    null,
                    null, 
                    0,
                    length); 
        } catch (RollerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return recentEntries;
    }   
    
    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     * @param length Max entries to return (1-100)
     * @return List of comment objects.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.CommentData"
     */
    public List getRecentComments(int length) {   
        if (length > 100) length = 100;
        List recentComments = new ArrayList();
        if (length < 1) return recentComments;
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            recentComments = wmgr.getComments(
                    this,
                    null,          // weblog entry
                    null,          // search String
                    null,          // startDate
                    null,          // endDate
                    CommentData.APPROVED, // approved comments only
                    true,          // we want reverse chrono order
                    0,             // offset
                    length);       // length
        } catch (RollerException e) {
            log.error("ERROR: getting recent comments", e);
        }
        return recentComments;
    }

    
    /**
     * Get bookmark folder by name.
     * @param folderName Name or path of bookmark folder to be returned (null for root)
     * @return Folder object requested.
     *
     * @roller.wrapPojoMethod type="pojo" class="org.apache.roller.pojos.FolderData"
     */
    public FolderData getBookmarkFolder(String folderName) {
        FolderData ret = null;
        try {
            Roller roller = RollerFactory.getRoller();
            BookmarkManager bmgr = roller.getBookmarkManager();
            if (folderName == null || folderName.equals("nil") || folderName.trim().equals("/")) {
                return bmgr.getRootFolder(this);
            } else {
                return bmgr.getFolder(this, folderName);
            }
        } catch (RollerException re) {
            log.error("ERROR: fetching folder for weblog", re);
        }
        return ret;
    }

    
    /** 
     * Return collection of referrers for current day.
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.RefererData"
     */
    public List getTodaysReferrers() {
        List referers = null;
        try {
            Roller roller = RollerFactory.getRoller();
            RefererManager rmgr = roller.getRefererManager();
            return rmgr.getTodaysReferers(this);
            
        } catch (RollerException e) {
            log.error("PageModel getTodaysReferers()", e);
        }
        return (referers == null ? Collections.EMPTY_LIST : referers);        
    }
    
    /** No-op method to please XDoclet */
    public void setTodaysReferrers(List ignored) {}
    
    /**
     * Get number of hits counted today.
     * @roller.wrapPojoMethod type="simple"
     */
    public int getTodaysHits() {
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager mgr = roller.getWeblogManager();
            HitCountData hitCount = mgr.getHitCountByWeblog(this);
            
            return (hitCount != null) ? hitCount.getDailyHits() : 0;
            
        } catch (RollerException e) {
            log.error("Error getting weblog hit count", e);
        }
        return 0;
    }
    
    /** No-op method to please XDoclet */
    public void setTodaysHits(int ignored) {}

        
    /**
     * Get a list of TagStats objects for the most popular tags
     *
     * @param sinceDays Number of days into past (or -1 for all days)
     * @param length    Max number of tags to return.
     * @return          Collection of WeblogEntryTag objects
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public List getPopularTags(int sinceDays, int length) {
        List results = new ArrayList();
        Date startDate = null;
        if(sinceDays > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * sinceDays);        
            startDate = cal.getTime();     
        }        
        try {            
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            results = wmgr.getPopularTags(this, startDate, length);
        } catch (Exception e) {
            log.error("ERROR: fetching popular tags for weblog " + this.getName(), e);
        }
        return results;
    }      

    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public long getCommentCount() {
        long count = 0;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager mgr = roller.getWeblogManager();
            count = mgr.getCommentCount(this);            
        } catch (RollerException e) {
            log.error("Error getting comment count for weblog " + this.getName(), e);
        }
        return count;
    }
    
    /** No-op method to please XDoclet */
    public void setCommentCount(int ignored) {}
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public long getEntryCount() {
        long count = 0;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager mgr = roller.getWeblogManager();
            count = mgr.getEntryCount(this);            
        } catch (RollerException e) {
            log.error("Error getting entry count for weblog " + this.getName(), e);
        }
        return count;
    }

    /** No-op method to please XDoclet */
    public void setEntryCount(int ignored) {}
    
}
