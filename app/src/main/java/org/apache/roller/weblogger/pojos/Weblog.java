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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.referrers.RefererManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.BookmarkManager;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.util.UUIDGenerator;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.util.I18nUtils;


/**
 * Website has many-to-many association with users. Website has one-to-many and
 * one-direction associations with weblog entries, weblog categories, folders and
 * other objects. Use UserManager to create, fetch, update and retrieve websites.
 *
 * @author David M Johnson
 */
public class Weblog implements Serializable {
    
    public static final long serialVersionUID = 206437645033737127L;
    
    private static Log log = LogFactory.getLog(Weblog.class);
    
    // Simple properties
    private String  id               = UUIDGenerator.generateUUID();
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
    private Boolean moderateComments = Boolean.FALSE;
    private int     entryDisplayCount = 15;
    private Date    lastModified     = new Date();
    private String  pageModels;
    private boolean enableMultiLang  = false;
    private boolean showAllLangs     = true;
    private String  customStylesheetPath = null;
    private String  iconPath         = null;
    private String  about            = null;
    private String  creator          = null;

    // Associated objects
    private WeblogCategory bloggerCategory = null;
    private WeblogCategory defaultCategory = null;
    
    private Map<String, WeblogEntryPlugin> initializedPlugins = null;
    
    public Weblog() {    
    }
    
    public Weblog(
            String handle,
            String creator,
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
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getHandle());
        buf.append(", ").append(getName());
        buf.append(", ").append(getEmailAddress());
        buf.append(", ").append(getLocale());
        buf.append(", ").append(getTimeZone());
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Weblog)) {
            return false;
        }
        Weblog o = (Weblog)other;
        return new EqualsBuilder()
            .append(getHandle(), o.getHandle()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getHandle())
            .toHashCode();
    } 
    
    /*public List getPermissions() {
        return permissions;
    }
    public void setPermissions(List perms) {
        permissions = perms;
    }*/
    
    
    /**
     * Get the Theme object in use by this weblog, or null if no theme selected.
     */
    public WeblogTheme getTheme() {
        try {
            // let the ThemeManager handle it
            ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
            return themeMgr.getTheme(this);
        } catch (WebloggerException ex) {
            log.error("Error getting theme for weblog - "+getHandle(), ex);
        }
        
        // TODO: maybe we should return a default theme in this case?
        return null;
    }
    
    
    /**
     * Lookup the default page for this website.
     */
    public ThemeTemplate getDefaultPage() throws WebloggerException {
        
        // look for the page in our Theme
        Theme weblogTheme = getTheme();
        if(weblogTheme != null) {
            return weblogTheme.getDefaultTemplate();
        }
        
        return null;
    }
    
    
    /**
     * Id of the Website.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Short URL safe string that uniquely identifies the website.
     */
    public String getHandle() {
        return this.handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /**
     * Name of the Website.
     */
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Description
     *
     */
    public String getDescription() {
        return this.description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Original creator of website.
     */
    public org.apache.roller.weblogger.pojos.User getCreator() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(creator);
        } catch (Exception e) {
            log.error("ERROR fetching user object for username: " + creator, e);
        }
        return null;
    }
    
    /**
     * Username of original creator of website.
     */
    public String getCreatorUserName() {
        return creator;
    }
    
    public void setCreatorUserName(String creatorUserName) {
        creator = creatorUserName;
    }
    
    public String getDefaultPageId() {
        return this.defaultPageId;
    }
    
    public void setDefaultPageId(String defaultPageId) {
        this.defaultPageId = defaultPageId;
    }
    
    /**
     * @deprecated
     */
    public String getWeblogDayPageId() {
        return this.weblogDayPageId;
    }
    
    /**
     * @deprecated
     */
    public void setWeblogDayPageId(String weblogDayPageId) {
        this.weblogDayPageId = weblogDayPageId;
    }
    
    public Boolean getEnableBloggerApi() {
        return this.enableBloggerApi;
    }
    
    public void setEnableBloggerApi(Boolean enableBloggerApi) {
        this.enableBloggerApi = enableBloggerApi;
    }
    
    public WeblogCategory getBloggerCategory() {
        return bloggerCategory;
    }
    
    public void setBloggerCategory(WeblogCategory bloggerCategory) {
        this.bloggerCategory = bloggerCategory;
    }
    
    /**
     * By default,the default category for a weblog is the root and all macros
     * work with the top level categories that are immediately under the root.
     * Setting a different default category allows you to partition your weblog.
     */
    public WeblogCategory getDefaultCategory() {
        return defaultCategory;
    }
    
    public void setDefaultCategory(WeblogCategory defaultCategory) {
        this.defaultCategory = defaultCategory;
    }
    
    public String getEditorPage() {
        return this.editorPage;
    }
    
    public void setEditorPage(String editorPage) {
        this.editorPage = editorPage;
    }
    
    public String getBlacklist() {
        return this.blacklist;
    }
    
    public void setBlacklist(String blacklist) {
        this.blacklist = blacklist;
    }
    
    public Boolean getAllowComments() {
        return this.allowComments;
    }
    
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }
    
    public Boolean getDefaultAllowComments() {
        return defaultAllowComments;
    }
    
    public void setDefaultAllowComments(Boolean defaultAllowComments) {
        this.defaultAllowComments = defaultAllowComments;
    }
    
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }
    
    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }
    
    public Boolean getModerateComments() {
        return moderateComments;
    }
    
    public void setModerateComments(Boolean moderateComments) {
        this.moderateComments = moderateComments;
    }
    
    public Boolean getEmailComments() {
        return this.emailComments;
    }
    
    public void setEmailComments(Boolean emailComments) {
        this.emailComments = emailComments;
    }
    
    public String getEmailFromAddress() {
        return this.emailFromAddress;
    }
    
    public void setEmailFromAddress(String emailFromAddress) {
        this.emailFromAddress = emailFromAddress;
    }
    
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    /**
     * EditorTheme of the Website.
     */
    public String getEditorTheme() {
        return this.editorTheme;
    }
    
    public void setEditorTheme(String editorTheme) {
        this.editorTheme = editorTheme;
    }
    
    /**
     * Locale of the Website.
     */
    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Timezone of the Website.
     */
    public String getTimeZone() {
        return this.timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    public Date getDateCreated() {
        if (dateCreated == null) {
            return null;
        } else {
            return (Date)dateCreated.clone();
        }
    }

    public void setDateCreated(final Date date) {
        if (date != null) {
            dateCreated = (Date)date.clone();
        } else {
            dateCreated = null;
        }
    }
    
    /**
     * Comma-delimited list of user's default Plugins.
     */
    public String getDefaultPlugins() {
        return defaultPlugins;
    }
    
    public void setDefaultPlugins(String string) {
        defaultPlugins = string;
    }

    /**
     * Set bean properties based on other bean.
     */
    public void setData(Weblog other) {
        
        this.setId(other.getId());
        this.setName(other.getName());
        this.setHandle(other.getHandle());
        this.setDescription(other.getDescription());
        this.setCreatorUserName(other.getCreatorUserName());
        this.setDefaultPageId(other.getDefaultPageId());
        this.setWeblogDayPageId(other.getWeblogDayPageId());
        this.setEnableBloggerApi(other.getEnableBloggerApi());
        this.setBloggerCategory(other.getBloggerCategory());
        this.setDefaultCategory(other.getDefaultCategory());
        this.setEditorPage(other.getEditorPage());
        this.setBlacklist(other.getBlacklist());
        this.setAllowComments(other.getAllowComments());
        this.setEmailComments(other.getEmailComments());
        this.setEmailAddress(other.getEmailAddress());
        this.setEmailFromAddress(other.getEmailFromAddress());
        this.setEditorTheme(other.getEditorTheme());
        this.setLocale(other.getLocale());
        this.setTimeZone(other.getTimeZone());
        this.setDefaultPlugins(other.getDefaultPlugins());
        this.setEnabled(other.getEnabled());
        this.setDateCreated(other.getDateCreated());
        this.setEntryDisplayCount(other.getEntryDisplayCount());
        this.setActive(other.getActive());
        this.setLastModified(other.getLastModified());
    }
    
    
    /**
     * Parse locale value and instantiate a Locale object,
     * otherwise return default Locale.
     *
     * @return Locale
     */
    public Locale getLocaleInstance() {
        return I18nUtils.toLocale(getLocale());
    }
    
    
    /**
     * Return TimeZone instance for value of timeZone,
     * otherwise return system default instance.
     * @return TimeZone
     */
    public TimeZone getTimeZoneInstance() {
        if (getTimeZone() == null) {
            this.setTimeZone( TimeZone.getDefault().getID() );
        }
        return TimeZone.getTimeZone(getTimeZone());
    }
    
    /**
     * Returns true if user has all permission action specified.
     */
    public boolean hasUserPermission(User user, String action) {
        return hasUserPermissions(user, Collections.singletonList(action));
    }
    
    
    /**
     * Returns true if user has all permissions actions specified in the weblog.
     */
    public boolean hasUserPermissions(User user, List<String> actions) {
        try {
            // look for user in website's permissions
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            WeblogPermission userPerms = new WeblogPermission(this, user, actions);
            return umgr.checkPermission(userPerms, user);
            
        } catch (WebloggerException ex) {
            // something is going seriously wrong, not much we can do here
            log.error("ERROR checking user permssion", ex);
        }
        return false;
    }
    
    /** Get number of users associated with website */
    public int getUserCount() {
        int count = 0;
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            for (WeblogPermission perm : umgr.getWeblogPermissions(this)) {
                count++;
            }
        } catch (WebloggerException ex) {
            // something is seriously wrong, not me we can do here
            log.error("ERROR error getting admin user count", ex);
        }
        return count;
    }
    
    public int getAdminUserCount() {
        int count = 0;
        try {
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            for (WeblogPermission perm : umgr.getWeblogPermissions(this)) {
                if (perm.hasAction(WeblogPermission.ADMIN)) {
                    count++;
                }
            }
            
        } catch (WebloggerException ex) {
            // something is seriously wrong, not me we can do here
            log.error("ERROR error getting admin user count", ex);
        }
        return count;
    }
    
    public int getEntryDisplayCount() {
        return entryDisplayCount;
    }
    
    public void setEntryDisplayCount(int entryDisplayCount) {
        this.entryDisplayCount = entryDisplayCount;
    }
    
    /**
     * Set to FALSE to completely disable and hide this weblog from public view.
     */
    public Boolean getEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Set to FALSE to exclude this weblog from community areas such as the
     * front page and the planet page.
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
        return (getModerateComments()
         || WebloggerRuntimeConfig.getBooleanProperty("users.moderation.required"));
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
     */
    public boolean isShowAllLangs() {
        return showAllLangs;
    }

    public void setShowAllLangs(boolean showAllLangs) {
        this.showAllLangs = showAllLangs;
    }
    
    public String getURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(this, null, false);
    }

    public String getAbsoluteURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(this, null, true);
    }

    /**
     * Comma-separated list of additional page models to be created when this
     * weblog is rendered.
     */
    public String getPageModels() {
        return pageModels;
    }
    public void setPageModels(String pageModels) {
        this.pageModels = pageModels;
    }

    
    /**
     * The path under the weblog's resources to a stylesheet override.
     */
    public String getCustomStylesheetPath() {
        return customStylesheetPath;
    }

    public void setCustomStylesheetPath(String customStylesheetPath) {
        this.customStylesheetPath = customStylesheetPath;
    }
    
    
    /**
     * The path under the weblog's resources to an icon image.
     */
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    
    /**
     * A description for the weblog (its purpose, authors, etc.)
     *
     * This field is meant to hold a paragraph or two describing the weblog, in contrast
     * to the short sentence or two 'description' attribute meant for blog taglines
     * and HTML header META description tags.
     *
     */
    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }
    
    
    /**
     * Get initialized plugins for use during rendering process.
     */
    public Map<String, WeblogEntryPlugin> getInitializedPlugins() {
        if (initializedPlugins == null) {
            try {
                Weblogger roller = WebloggerFactory.getWeblogger();
                PluginManager ppmgr = roller.getPluginManager();
                initializedPlugins = ppmgr.getWeblogEntryPlugins(this);
            } catch (Exception e) {
                log.error("ERROR: initializing plugins");
            }
        }
        return initializedPlugins;
    }
    
    /** 
     * Get weblog entry specified by anchor or null if no such entry exists.
     * @param anchor Weblog entry anchor
     * @return Weblog entry specified by anchor
     */
    public WeblogEntry getWeblogEntry(String anchor) {
        WeblogEntry entry = null;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager wmgr = roller.getWeblogEntryManager();
            entry = wmgr.getWeblogEntryByAnchor(this, anchor);
        } catch (WebloggerException e) {
            log.error("ERROR: getting entry by anchor");
        }
        return entry;
    }
    
    /**
     * Returns categories under the default category of the weblog.
     */
    public Set<WeblogCategory> getWeblogCategories() {
        WeblogCategory category = this.getDefaultCategory();
        return category.getWeblogCategories();
    }
    
    
    public Set<WeblogCategory> getWeblogCategories(String categoryPath) {
        Set<WeblogCategory> ret = new HashSet<WeblogCategory>();
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager wmgr = roller.getWeblogEntryManager();            
            WeblogCategory category = null;
            if (categoryPath != null && !categoryPath.equals("nil")) {
                category = wmgr.getWeblogCategoryByPath(this, categoryPath);
            } else {
                category = this.getDefaultCategory();
            }
            ret = category.getWeblogCategories();
        } catch (WebloggerException e) {
            log.error("ERROR: fetching categories for path: " + categoryPath, e);
        }
        return ret;
    }

    public WeblogCategory getWeblogCategory(String categoryPath) {
        WeblogCategory category = null;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager wmgr = roller.getWeblogEntryManager();
            if (categoryPath != null && !categoryPath.equals("nil")) {
                category = wmgr.getWeblogCategoryByPath(this, categoryPath);
            } else {
                category = this.getDefaultCategory();
            }
        } catch (WebloggerException e) {
            log.error("ERROR: fetching category at path: " + categoryPath, e);
        }
        return category;
    }
    
    
    /**
     * Get up to 100 most recent published entries in weblog.
     * @param cat Category path or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntries(String cat, int length) {
        if (cat != null && "nil".equals(cat)) {
            cat = null;
        }
        if (length > 100) {
            length = 100;
        }
        List<WeblogEntry> recentEntries = new ArrayList<WeblogEntry>();
        if (length < 1) {
            return recentEntries;
        }
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            recentEntries = wmgr.getWeblogEntries(
                    
                    this, 
                    null,       // user
                    null,       // startDate
                    null,       // endDate
                    cat,        // cat or null
                    null,WeblogEntry.PUBLISHED, 
                    null,       // text
                    "pubTime",  // sortby
                    null,
                    null, 
                    0,
                    length); 
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return recentEntries;
    }
    
    /**
     * Get up to 100 most recent published entries in weblog.
     * @param tag Blog entry tag to query by
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntriesByTag(String tag, int length) {
        if (tag != null && "nil".equals(tag)) {
            tag = null;
        }
        if (length > 100) {
            length = 100;
        }
        List recentEntries = new ArrayList();
        List tags = new ArrayList();
        if (tag != null) {
            tags.add(tag);
        }
        if (length < 1) {
            return recentEntries;
        }
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            recentEntries = wmgr.getWeblogEntries(
                    
                    this, 
                    null,       // user
                    null,       // startDate
                    null,       // endDate
                    null,       // cat or null
                    tags,WeblogEntry.PUBLISHED, 
                    null,       // text
                    "pubTime",  // sortby
                    null,
                    null, 
                    0,
                    length); 
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return recentEntries;
    }   
    
    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     * @param length Max entries to return (1-100)
     * @return List of comment objects.
     */
    public List<WeblogEntryComment> getRecentComments(int length) {
        if (length > 100) {
            length = 100;
        }
        List recentComments = new ArrayList();
        if (length < 1) {
            return recentComments;
        }
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            recentComments = wmgr.getComments(
                    
                    this,
                    null,          // weblog entry
                    null,          // search String
                    null,          // startDate
                    null,WeblogEntryComment.APPROVED, // approved comments only
                    true,          // we want reverse chrono order
                    0,             // offset
                    length);       // length
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent comments", e);
        }
        return recentComments;
    }

    
    /**
     * Get bookmark folder by name.
     * @param folderName Name or path of bookmark folder to be returned (null for root)
     * @return Folder object requested.
     */
    public WeblogBookmarkFolder getBookmarkFolder(String folderName) {
        WeblogBookmarkFolder ret = null;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            BookmarkManager bmgr = roller.getBookmarkManager();
            if (folderName == null || folderName.equals("nil") || folderName.trim().equals("/")) {
                return bmgr.getRootFolder(this);
            } else {
                return bmgr.getFolder(this, folderName);
            }
        } catch (WebloggerException re) {
            log.error("ERROR: fetching folder for weblog", re);
        }
        return ret;
    }

    
    /** 
     * Return collection of referrers for current day.
     */
    public List<WeblogReferrer> getTodaysReferrers() {
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            RefererManager rmgr = roller.getRefererManager();
            return rmgr.getTodaysReferers(this);
        } catch (WebloggerException e) {
            log.error("PageModel getTodaysReferers()", e);
        }
        return Collections.EMPTY_LIST;
    }
    
    /**
     * Get number of hits counted today.
     */
    public int getTodaysHits() {
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager mgr = roller.getWeblogEntryManager();
            WeblogHitCount hitCount = mgr.getHitCountByWeblog(this);
            
            return (hitCount != null) ? hitCount.getDailyHits() : 0;
            
        } catch (WebloggerException e) {
            log.error("Error getting weblog hit count", e);
        }
        return 0;
    }

    /**
     * Get a list of TagStats objects for the most popular tags
     *
     * @param sinceDays Number of days into past (or -1 for all days)
     * @param length    Max number of tags to return.
     * @return          Collection of WeblogEntryTag objects
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
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager wmgr = roller.getWeblogEntryManager();
            results = wmgr.getPopularTags(this, startDate, 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching popular tags for weblog " + this.getName(), e);
        }
        return results;
    }      

    public long getCommentCount() {
        long count = 0;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager mgr = roller.getWeblogEntryManager();
            count = mgr.getCommentCount(this);            
        } catch (WebloggerException e) {
            log.error("Error getting comment count for weblog " + this.getName(), e);
        }
        return count;
    }
    
    public long getEntryCount() {
        long count = 0;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager mgr = roller.getWeblogEntryManager();
            count = mgr.getEntryCount(this);            
        } catch (WebloggerException e) {
            log.error("Error getting entry count for weblog " + this.getName(), e);
        }
        return count;
    }

    /**
     * @return  mobileTheme
     */
    public Theme getTheme(String type){
        WeblogThemeAssoc themeAssoc;
        Theme theme = null;
        try {
            //get Theme Associativity object so we can get the ThemeName for the type given
            themeAssoc = WebloggerFactory.getWeblogger().getWeblogManager().getThemeAssoc(this ,type);
            if (themeAssoc != null) {
                // get the Theme from theme manager
                theme = WebloggerFactory.getWeblogger().getThemeManager().getTheme(themeAssoc.getName());
            }
        } catch (WebloggerException e) {
            log.error("Error getting Weblog Theme type -"+type+"for weblog "+getHandle(),e);
        }
        return theme;
    }
}
