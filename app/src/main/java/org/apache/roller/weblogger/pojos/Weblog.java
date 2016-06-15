/*
* Licensed to the Apache Software Foundation (ASF) under one or more
* contributor license agreements.  The ASF licenses this file to You
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

import java.time.LocalDateTime;
import java.util.*;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;


/**
 * Weblogs have a many-to-many association with users. They also have one-to-many and
 * one-direction associations with weblog entries, weblog categories, bookmarks and
 * other objects.
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
        @NamedQuery(name="Weblog.getByWeblog&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc",
                query="SELECT w FROM Weblog w WHERE w.visible = true AND w.lastModified > ?1 AND w.hitsToday > 0 ORDER BY w.hitsToday DESC"),
        @NamedQuery(name="Weblog.updateDailyHitCountZero",
                query="UPDATE Weblog w SET w.hitsToday = 0")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Weblog {

    private String  id               = WebloggerCommon.generateUUID();
    @NotBlank(message = "{createWeblog.error.handleNull}")
    @Pattern(regexp = "[a-z0-9\\-]*", message = "{createWeblog.error.invalidHandle}")
    private String  handle           = null;
    @NotBlank(message = "{createWeblog.error.nameNull}")
    private String  name             = null;
    private String  tagline          = null;
    private String  editorPage       = null;
    private String  blacklist        = null;
    private Boolean allowComments    = Boolean.TRUE;
    private Boolean emailComments    = Boolean.FALSE;
    private Boolean approveComments  = Boolean.TRUE;
    @NotBlank(message = "{createWeblog.error.themeNull}")
    private String  theme            = null;
    private String  locale           = null;
    private String  timeZone         = null;
    private String  defaultPlugins   = null;
    private Boolean visible          = Boolean.TRUE;
    private LocalDateTime dateCreated = LocalDateTime.now();
    private int     defaultCommentDays = -1;
    private int     entriesPerPage   = 15;
    private LocalDateTime lastModified = LocalDateTime.now();
    private String  about            = null;
    /*
     * String creatorId used instead of User object to prevent models from having access to sensitive User
     * info (passwords etc.) via this object.  Transient SafeUser object without sensitive fields used instead.
     * (Avoiding direct use of SafeUser in a @ManyToOne relationship to avoid JPA trying to persist the SafeUser
     * when this Weblog object is persisted.)
     */
    private String  creatorId        = null;
    private SafeUser creator         = null;
    private String  analyticsCode    = null;
    private int     hitsToday        = 0;
    private boolean applyCommentDefaults = false;

    // needed when viewing theme previews, to ensure proper templates being called
    private boolean tempPreviewWeblog = false;

    // Associated objects
    @JsonIgnore
    private List<WeblogCategory> weblogCategories = new ArrayList<>();
    @JsonIgnore
    private List<WeblogBookmark> bookmarks = new ArrayList<>();
    @JsonIgnore
    private List<MediaDirectory> mediaDirectories = new ArrayList<>();

    public Weblog() {}
    
    public Weblog(
            String handle,
            User creator,
            String name,
            String desc,
            String theme,
            String locale,
            String timeZone) {
        
        this.handle = handle;
        this.creatorId = creator.getId();
        this.name = name;
        this.tagline = desc;
        this.theme = theme;
        this.locale = locale;
        this.timeZone = timeZone;
    }

    @Id
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * Short URL safe string that uniquely identifies the weblog.
     */
    @Basic(optional=false)
    public String getHandle() {
        return this.handle;
    }
    
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /**
     * Weblog name (title)
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
     * Weblog subtitle
     */
    public String getTagline() {
        return this.tagline;
    }
    
    public void setTagline(String tagline) {
        this.tagline = tagline;
    }

    @Column(name="creatorid", nullable=false)
    public String getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(String creatorId) {
        this.creatorId = creatorId;
    }

    @Transient
    public SafeUser getCreator() {
        if (creator == null) {
            creator = WebloggerFactory.getWeblogger().getUserManager().getSafeUser(creatorId);
        }
        return creator;
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
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }
    
    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }

    @Basic(optional=false)
    public Boolean getApproveComments() {
        return approveComments;
    }
    
    public void setApproveComments(Boolean approveComments) {
        this.approveComments = approveComments;
    }

    @Basic(optional=false)
    public Boolean getEmailComments() {
        return this.emailComments;
    }
    
    public void setEmailComments(Boolean emailComments) {
        this.emailComments = emailComments;
    }

    public String getTheme() {
        return this.theme;
    }
    
    public void setTheme(String theme) {
        this.theme = theme;
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
    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime date) {
        dateCreated = date;
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
        this.setCreatorId(other.getCreatorId());
        this.setEditorPage(other.getEditorPage());
        this.setBlacklist(other.getBlacklist());
        this.setAllowComments(other.getAllowComments());
        this.setEmailComments(other.getEmailComments());
        this.setTheme(other.getTheme());
        this.setLocale(other.getLocale());
        this.setTimeZone(other.getTimeZone());
        this.setVisible(other.getVisible());
        this.setDateCreated(other.getDateCreated());
        this.setEntriesPerPage(other.getEntriesPerPage());
        this.setLastModified(other.getLastModified());
        this.setWeblogCategories(other.getWeblogCategories());
        this.setAnalyticsCode(other.getAnalyticsCode());
        this.setTempPreviewWeblog(other.isTempPreviewWeblog());
        this.setBookmarks(other.getBookmarks());
    }
    
    /**
     * Parse locale value and instantiate a Locale object.
     * @return Locale
     */
    @Transient
    @JsonIgnore
    public Locale getLocaleInstance() {
        return Locale.forLanguageTag(getLocale());
    }

    /**
     * Return TimeZone instance for value of timeZone, else return system default instance.
     * @return TimeZone
     */
    @Transient
    @JsonIgnore
    public TimeZone getTimeZoneInstance() {
        if (getTimeZone() == null) {
            this.setTimeZone( TimeZone.getDefault().getID() );
        }
        return TimeZone.getTimeZone(getTimeZone());
    }

    @Basic(optional=false)
    public int getEntriesPerPage() {
        return entriesPerPage;
    }
    
    public void setEntriesPerPage(int entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
    }
    
    /**
     * If false, weblog will be hidden from public view.
     */
    @Basic(optional=false)
    public Boolean getVisible() {
        return this.visible;
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    
    /**
     * The last time any visible part of this weblog was modified.
     * This includes a change to weblog settings, entries, themes, templates, 
     * comments, categories, bookmarks, etc.  This can be used by cache managers
     * to determine if blog content should be invalidated and reloaded.
     */
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Update weblog's last modified date to the current date, to trigger cache
     * refreshing so users can see new categories, bookmarks, etc.
     */
    public void invalidateCache() {
        setLastModified(LocalDateTime.now());
    }

    // Used in templates
    @Transient
    public String getURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(this, false);
    }

    // Used in a few JSP's
    @Transient
    public String getAbsoluteURL() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(this, true);
    }

    public String getAnalyticsCode() {
        return analyticsCode;
    }

    public void setAnalyticsCode(String analyticsCode) {
        this.analyticsCode = analyticsCode;
    }

    /**
     * A description for the weblog (its purpose, authors, etc.), perhaps a paragraph or so in length.
     */
    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = HTMLSanitizer.conditionallySanitize(about);
    }

    /**
     * Add a category to this weblog.
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

        // add it to our list of categories
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

    @OneToMany(targetEntity=MediaDirectory.class,
            cascade={CascadeType.ALL}, mappedBy="weblog")
    public List<MediaDirectory> getMediaDirectories() {
        return mediaDirectories;
    }

    public void setMediaDirectories(List<MediaDirectory> mediaDirectories) {
        this.mediaDirectories = mediaDirectories;
    }

    /**
     * Add a bookmark to this weblog.
     */
    public void addBookmark(WeblogBookmark item) {
        // make sure blogroll item is not null
        if (item == null || item.getName() == null) {
            throw new IllegalArgumentException("Bookmark cannot be null and must have a valid name");
        }

        if (this.hasBookmark(item.getName())) {
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
    public boolean hasMediaDirectory(String name) {
        for (MediaDirectory directory : this.getMediaDirectories()) {
            if (directory.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    public MediaDirectory getMediaDirectory(String name) {
        for (MediaDirectory dir : this.getMediaDirectories()) {
            if (name.equals(dir.getName())) {
                return dir;
            }
        }
        return null;
    }

    @Transient
    public boolean isTempPreviewWeblog() {
        return tempPreviewWeblog;
    }

    public void setTempPreviewWeblog(boolean tempPreviewWeblog) {
        this.tempPreviewWeblog = tempPreviewWeblog;
    }

    // convenience methods for populating fields from forms

    @Transient
    public String[] getDefaultPluginsArray() {
        return StringUtils.split(defaultPlugins, ",");
    }

    public void setDefaultPluginsArray(String[] strings) {
        defaultPlugins = StringUtils.join(strings, ",");
    }

    @Transient
    public String getDefaultCommentDaysString() {
        return "" + defaultCommentDays;
    }

    public void setDefaultCommentDaysString(String dcd) {
        defaultCommentDays = Integer.parseInt(dcd);
    }

    @Transient
    public boolean isApplyCommentDefaults() {
        return applyCommentDefaults;
    }

    public void setApplyCommentDefaults(boolean applyCommentDefaults) {
        this.applyCommentDefaults = applyCommentDefaults;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        return  "{" + getId() + ", " + getHandle() + ", " + getName() + "}";
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
