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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.*;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.WebloggerUtils;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.util.I18nUtils;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;


/**
 * Weblogs have a many-to-many association with users. They also have one-to-many and
 * one-direction associations with weblog entries, weblog categories, bookmarks and
 * other objects. Use WeblogManager to create, fetch, update and retrieve weblogs.
 */
@Entity
@Table(name="weblog")
@NamedQueries({
        @NamedQuery(name="Weblog.getByHandle",
                query="SELECT w FROM Weblog w WHERE w.handle = ?1"),
        @NamedQuery(name="Weblog.getByLetterOrderByHandle",
                query="SELECT w FROM Weblog w WHERE UPPER(w.handle) like ?1 ORDER BY w.handle"),
        @NamedQuery(name="Weblog.getCountAllDistinct",
                query="SELECT COUNT(w) FROM Weblog w"),
        @NamedQuery(name="Weblog.getCountByHandleLike",
                query="SELECT COUNT(w) FROM Weblog w WHERE UPPER(w.handle) like ?1"),
        @NamedQuery(name="Weblog.getByWeblogEnabledTrueAndActiveTrue&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc",
                query="SELECT w FROM Weblog w WHERE w.visible = true AND w.active = true AND w.lastModified > ?1 AND w.hitsToday > 0 ORDER BY w.hitsToday DESC"),
        @NamedQuery(name="Weblog.updateDailyHitCountZero",
                query="UPDATE Weblog w SET w.hitsToday = 0")
})
public class Weblog implements Serializable {
    
    public static final long serialVersionUID = 206437645033737127L;
    
    private static Log log = LogFactory.getLog(Weblog.class);

    private static final int MAX_ENTRIES = 100;
    
    // Simple properties
    private String  id               = WebloggerUtils.generateUUID();
    private String  handle           = null;
    private String  name             = null;
    private String  tagline          = null;
    private Boolean enableBloggerApi = Boolean.TRUE;
    private String  editorPage       = null;
    private String  blacklist        = null;
    private Boolean allowComments    = Boolean.TRUE;
    private Boolean emailComments    = Boolean.FALSE;
    private String  emailAddress     = null;
    private String  editorTheme      = null;
    private String  locale           = null;
    private String  timeZone         = null;
    private String  defaultPlugins   = null;
    private Boolean visible          = Boolean.TRUE;
    private Boolean active           = Boolean.TRUE;
    private Date    dateCreated      = new java.util.Date();
    private Boolean defaultAllowComments = Boolean.TRUE;
    private int     defaultCommentDays = 0;
    private Boolean moderateComments = Boolean.FALSE;
    private int     entryDisplayCount = 15;
    private Date    lastModified     = new Date();
    private boolean enableMultiLang  = false;
    private boolean showAllLangs     = true;
    private String  iconPath         = null;
    private String  about            = null;
    private String  creator          = null;
    private String  analyticsCode    = null;
    private int     hitsToday        = 0;

    // Associated objects
    private WeblogCategory bloggerCategory = null;

    private Map<String, WeblogEntryPlugin> initializedPlugins = null;

    private List<WeblogCategory> weblogCategories = new ArrayList<WeblogCategory>();

    private List<WeblogBookmark> bookmarks = new ArrayList<WeblogBookmark>();

    private List<MediaFileDirectory> mediaFileDirectories = new ArrayList<MediaFileDirectory>();

    public Weblog() {
    }
    
    public Weblog(
            String handle,
            String creator,
            String name,
            String desc,
            String email,
            String editorTheme,
            String locale,
            String timeZone) {
        
        this.handle = handle;
        this.creator = creator;
        this.name = name;
        this.tagline = desc;
        this.emailAddress = email;
        this.editorTheme = editorTheme;
        this.locale = locale;
        this.timeZone = timeZone;
    }

    /**
     * Get the Theme object in use by this weblog, or null if no theme selected.
     */
    @Transient
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

    @Id
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Short URL safe string that uniquely identifies the website.
     */
    @Basic(optional=false)
    public String getHandle() {
        return this.handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /**
     * Name of the Website.
     */
    @Basic(optional=false)
    public String getName() {
        return this.name;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    @Basic(optional=false)
    public int getHitsToday() {
        return hitsToday;
    }

    public void setHitsToday(int hitsToday) {
        this.hitsToday = hitsToday;
    }

    /**
     * Description
     *
     */
    public String getTagline() {
        return this.tagline;
    }
    
    public void setTagline(String tagline) {
        this.tagline = tagline;
    }
    
    /**
     * Original creator of website.
     */
    @Transient
    public org.apache.roller.weblogger.pojos.User getCreator() {
        try {
            return WebloggerFactory.getWeblogger().getUserManager().getUserByUserName(creator);
        } catch (Exception e) {
            log.error("ERROR fetching user object for username: " + creator, e);
        }
        return null;
    }


    @Column(name="creator")
    public String getCreatorUserName() {
        return creator;
    }
    
    public void setCreatorUserName(String creatorUserName) {
        creator = creatorUserName;
    }


    @Basic(optional=false)
    public Boolean getEnableBloggerApi() {
        return this.enableBloggerApi;
    }
    
    public void setEnableBloggerApi(Boolean enableBloggerApi) {
        this.enableBloggerApi = enableBloggerApi;
    }

    @ManyToOne
    @JoinColumn(name="bloggercatid")
    public WeblogCategory getBloggerCategory() { return bloggerCategory; }
    
    public void setBloggerCategory(WeblogCategory bloggerCategory) {
        this.bloggerCategory = bloggerCategory;
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

    @Basic(optional=false)
    public Boolean getAllowComments() {
        return this.allowComments;
    }
    
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }

    @Basic(optional=false)
    public Boolean getDefaultAllowComments() {
        return defaultAllowComments;
    }
    
    public void setDefaultAllowComments(Boolean defaultAllowComments) {
        this.defaultAllowComments = defaultAllowComments;
    }

    @Basic(optional=false)
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }
    
    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }

    @Column(name="commentmod", nullable=false)
    public Boolean getModerateComments() {
        return moderateComments;
    }
    
    public void setModerateComments(Boolean moderateComments) {
        this.moderateComments = moderateComments;
    }

    @Basic(optional=false)
    public Boolean getEmailComments() {
        return this.emailComments;
    }
    
    public void setEmailComments(Boolean emailComments) {
        this.emailComments = emailComments;
    }

    @Basic(optional=false)
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    public String getEditorTheme() {
        return this.editorTheme;
    }
    
    public void setEditorTheme(String editorTheme) {
        this.editorTheme = editorTheme;
    }
    
    public String getLocale() {
        return this.locale;
    }
    
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    public String getTimeZone() {
        return this.timeZone;
    }
    
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Basic(optional=false)
    @Temporal(TemporalType.TIMESTAMP)
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
        this.setTagline(other.getTagline());
        this.setCreatorUserName(other.getCreatorUserName());
        this.setEnableBloggerApi(other.getEnableBloggerApi());
        this.setBloggerCategory(other.getBloggerCategory());
        this.setEditorPage(other.getEditorPage());
        this.setBlacklist(other.getBlacklist());
        this.setAllowComments(other.getAllowComments());
        this.setEmailComments(other.getEmailComments());
        this.setEmailAddress(other.getEmailAddress());
        this.setEditorTheme(other.getEditorTheme());
        this.setLocale(other.getLocale());
        this.setTimeZone(other.getTimeZone());
        this.setVisible(other.getVisible());
        this.setDateCreated(other.getDateCreated());
        this.setEntryDisplayCount(other.getEntryDisplayCount());
        this.setActive(other.isActive());
        this.setLastModified(other.getLastModified());
        this.setWeblogCategories(other.getWeblogCategories());
    }
    
    
    /**
     * Parse locale value and instantiate a Locale object,
     * otherwise return default Locale.
     *
     * @return Locale
     */
    @Transient
    public Locale getLocaleInstance() {
        return I18nUtils.toLocale(getLocale());
    }
    
    
    /**
     * Return TimeZone instance for value of timeZone,
     * otherwise return system default instance.
     * @return TimeZone
     */
    @Transient
    public TimeZone getTimeZoneInstance() {
        if (getTimeZone() == null) {
            this.setTimeZone( TimeZone.getDefault().getID() );
        }
        return TimeZone.getTimeZone(getTimeZone());
    }

    /**
     * Returns true if user has all permissions actions specified in the weblog.
     */
    public boolean userHasWeblogRole(User user, WeblogRole weblogRole) {
        try {
            // look for user in website's permissions
            UserManager umgr = WebloggerFactory.getWeblogger().getUserManager();
            WeblogPermission userPerms = new WeblogPermission(this, weblogRole);
            return umgr.checkPermission(userPerms, user);
            
        } catch (WebloggerException ex) {
            // something is going seriously wrong, not much we can do here
            log.error("ERROR checking user permssion", ex);
        }
        return false;
    }

    @Column(name="displaycnt", nullable=false)
    public int getEntryDisplayCount() {
        return entryDisplayCount;
    }
    
    public void setEntryDisplayCount(int entryDisplayCount) {
        this.entryDisplayCount = entryDisplayCount;
    }
    
    /**
     * Set to FALSE to disable and hide this weblog from public view.
     */
    @Basic(optional=false)
    public Boolean getVisible() {
        return this.visible;
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Set to FALSE to exclude this weblog from community areas such as the
     * front page and the planet page.
     */
    @Column(name="isactive", nullable=false)
    public Boolean isActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    /**
     * Returns true if comment moderation is required by website or config.
     */
    @Transient
    public boolean getCommentModerationRequired() {
        return (getModerateComments()
         || WebloggerRuntimeConfig.getBooleanProperty("users.moderation.required"));
    }
    
    /** No-op */
    public void setCommentModerationRequired(boolean modRequired) {}    

    
    /**
     * The last time any visible part of this weblog was modified.
     * This includes a change to weblog settings, entries, themes, templates, 
     * comments, categories, bookmarks, etc.
     *
     * Pings are explicitly not included because pings do not
     * affect visible changes to a weblog.
     *
     */
    @Temporal(TemporalType.TIMESTAMP)
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
    @Basic(optional=false)
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
    @Basic(optional=false)
    public boolean isShowAllLangs() {
        return showAllLangs;
    }

    public void setShowAllLangs(boolean showAllLangs) {
        this.showAllLangs = showAllLangs;
    }

    @Transient
    public String getURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(this, null, false);
    }

    @Transient
    public String getAbsoluteURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(this, null, true);
    }

    /**
     * The path under the weblog's resources to an icon image.
     */
    @Column(name="icon")
    public String getIconPath() {
        return iconPath;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public String getAnalyticsCode() {
        return analyticsCode;
    }

    public void setAnalyticsCode(String analyticsCode) {
        this.analyticsCode = analyticsCode;
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
    @Transient
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

    public WeblogCategory getWeblogCategory(String categoryName) {
        WeblogCategory category = null;
        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogEntryManager wmgr = roller.getWeblogEntryManager();
            if (categoryName != null && !categoryName.equals("nil")) {
                category = wmgr.getWeblogCategoryByName(this, categoryName);
            } else {
                category = getWeblogCategories().iterator().next();
            }
        } catch (WebloggerException e) {
            log.error("ERROR: fetching category: " + categoryName, e);
        }
        return category;
    }

    
    /**
     * Get up to 100 most recent published entries in weblog.
     * @param cat Category name or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntries(String cat, int length) {
        if (cat != null && "nil".equals(cat)) {
            cat = null;
        }
        if (length > MAX_ENTRIES) {
            length = MAX_ENTRIES;
        }
        List<WeblogEntry> recentEntries = new ArrayList<WeblogEntry>();
        if (length < 1) {
            return recentEntries;
        }
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(this);
            wesc.setCatName(cat);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setMaxResults(length);
            recentEntries = wmgr.getWeblogEntries(wesc);
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
        if (length > MAX_ENTRIES) {
            length = MAX_ENTRIES;
        }
        List<WeblogEntry> recentEntries = new ArrayList<WeblogEntry>();
        List<String> tags = new ArrayList<String>();
        if (tag != null) {
            tags.add(tag);
        }
        if (length < 1) {
            return recentEntries;
        }
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(this);
            wesc.setTags(tags);
            wesc.setStatus(PubStatus.PUBLISHED);
            wesc.setMaxResults(length);
            recentEntries = wmgr.getWeblogEntries(wesc);
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
        if (length > MAX_ENTRIES) {
            length = MAX_ENTRIES;
        }
        List<WeblogEntryComment> recentComments = new ArrayList<WeblogEntryComment>();
        if (length < 1) {
            return recentComments;
        }
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(this);
            csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
            csc.setMaxResults(length);
            recentComments = wmgr.getComments(csc);
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent comments", e);
        }
        return recentComments;
    }

    /**
     * Get a list of TagStats objects for the most popular tags
     *
     * @param sinceDays Number of days into past (or -1 for all days)
     * @param length    Max number of tags to return.
     * @return          Collection of WeblogEntryTag objects
     */
    public List<TagStat> getPopularTags(int sinceDays, int length) {
        List<TagStat> results = new ArrayList<TagStat>();
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

    @Transient
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

    @Transient
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
     * Add a category as a child of this category.
     */
    public void addCategory(WeblogCategory category) {

        // make sure category is not null
        if(category == null || category.getName() == null) {
            throw new IllegalArgumentException("Category cannot be null and must have a valid name");
        }

        // make sure we don't already have a category with that name
        if(hasCategory(category.getName())) {
            throw new IllegalArgumentException("Duplicate category name '"+category.getName()+"'");
        }

        // add it to our list of child categories
        getWeblogCategories().add(category);
    }

    @OneToMany(targetEntity=org.apache.roller.weblogger.pojos.WeblogCategory.class,
            cascade=CascadeType.REMOVE, mappedBy="weblog")
    @OrderBy("position")
    public List<WeblogCategory> getWeblogCategories() {
        return weblogCategories;
    }

    public void setWeblogCategories(List<WeblogCategory> cats) {
        this.weblogCategories = cats;
    }

    public boolean hasCategory(String name) {
        for (WeblogCategory cat : getWeblogCategories()) {
            if(name.equals(cat.getName())) {
                return true;
            }
        }
        return false;
    }

    @OneToMany(targetEntity=org.apache.roller.weblogger.pojos.WeblogBookmark.class,
            cascade={CascadeType.ALL}, mappedBy="weblog", orphanRemoval=true)
    public List<WeblogBookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<WeblogBookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    @OneToMany(targetEntity=org.apache.roller.weblogger.pojos.MediaFileDirectory.class,
            cascade={CascadeType.REMOVE, CascadeType.PERSIST}, mappedBy="weblog")
    public List<MediaFileDirectory> getMediaFileDirectories() {
        return mediaFileDirectories;
    }

    public void setMediaFileDirectories(List<MediaFileDirectory> mediaFileDirectories) {
        this.mediaFileDirectories = mediaFileDirectories;
    }

    /**
     * Add a bookmark to this weblog.
     */
    public void addBookmark(WeblogBookmark item) {

        // make sure blogroll item is not null
        if(item == null || item.getName() == null) {
            throw new IllegalArgumentException("Bookmark cannot be null and must have a valid name");
        }

        if(this.hasBookmark(item.getName())) {
            throw new IllegalArgumentException("Duplicate bookmark name '" + item.getName() + "'");
        }

        // add it to our blogroll
        getBookmarks().add(item);
    }

    /**
     * Does this Weblog have a bookmark with the same name?
     *
     * @param name The name of the bookmark to check for.
     * @return boolean true if exists, false otherwise.
     */
    public boolean hasBookmark(String name) {
        for (WeblogBookmark bookmark : this.getBookmarks()) {
            if(name.toLowerCase().equals(bookmark.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether this weblog contains the specified media file directory
     *
     * @param name directory name
     *
     * @return true if directory is present, false otherwise.
     */
    public boolean hasMediaFileDirectory(String name) {
        for (MediaFileDirectory directory : this.getMediaFileDirectories()) {
            if (directory.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public MediaFileDirectory getMediaFileDirectory(String name) {
        for (MediaFileDirectory dir : this.getMediaFileDirectories()) {
            if (name.equals(dir.getName())) {
                return dir;
            }
        }
        return null;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        return  "{" + getId() + ", " + getHandle()
                + ", " + getName() + ", " + getEmailAddress()
                + ", " + getLocale() + ", " + getTimeZone() + "}";
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

}
