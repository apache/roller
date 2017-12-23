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
package org.tightblog.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.tightblog.pojos.WebloggerProperties.CommentPolicy;
import org.tightblog.business.WebloggerContext;
import org.tightblog.rendering.comment.BlacklistCommentValidator;
import org.tightblog.util.Utilities;
import org.hibernate.validator.constraints.NotBlank;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Weblogs have a many-to-many association with users. They also have one-to-many and
 * one-direction associations with weblog entries, weblog categories, bookmarks and
 * other objects.
 */
@Entity
@Table(name = "weblog")
@NamedQueries({
        @NamedQuery(name = "Weblog.getByHandle",
                query = "SELECT w FROM Weblog w WHERE w.handle = ?1"),
        @NamedQuery(name = "Weblog.getByLetterOrderByHandle",
                query = "SELECT w FROM Weblog w WHERE UPPER(w.handle) like ?1 ORDER BY w.handle"),
        @NamedQuery(name = "Weblog.getCountAllDistinct",
                query = "SELECT COUNT(w) FROM Weblog w"),
        @NamedQuery(name = "Weblog.getCountByHandleLike",
                query = "SELECT COUNT(w) FROM Weblog w WHERE UPPER(w.handle) like ?1"),
        @NamedQuery(name = "Weblog.getByWeblog&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc",
                query = "SELECT w FROM Weblog w WHERE w.visible = true AND w.lastModified > ?1 AND w.hitsToday > 0 ORDER BY w.hitsToday DESC"),
        @NamedQuery(name = "Weblog.updateDailyHitCountZero",
                query = "UPDATE Weblog w SET w.hitsToday = 0")
})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Weblog {

    private String id = Utilities.generateUUID();
    @NotBlank(message = "{weblogConfig.error.handleNull}")
    @Pattern(regexp = "[a-z0-9\\-]*", message = "{weblogConfig.error.invalidHandle}")
    private String handle = null;
    @NotBlank(message = "{weblogConfig.error.nameNull}")
    private String name = null;
    private String tagline = null;
    private EditFormat editFormat = EditFormat.HTML;
    private String blacklist = null;
    private CommentPolicy allowComments = CommentPolicy.MUSTMODERATE;
    private Boolean emailComments = Boolean.FALSE;
    @NotBlank(message = "{weblogConfig.error.themeNull}")
    private String theme = null;
    private String locale = null;
    private String timeZone = null;
    private Boolean visible = Boolean.TRUE;
    private Instant dateCreated = Instant.now();
    private int defaultCommentDays = -1;
    private int entriesPerPage = 12;
    private Instant lastModified = Instant.now();
    private String about = null;
    private User creator = null;
    private String analyticsCode = null;
    private int hitsToday = 0;
    private boolean applyCommentDefaults = false;

    // Transient, derived from and re-calculated each time blacklist property is set
    private List<java.util.regex.Pattern> blacklistRegexRules = new ArrayList<>();

    // is this weblog instance used for previewing a theme?
    private boolean usedForThemePreview = false;
    private Locale localeInstance = null;

    public enum EditFormat {
        HTML("weblogConfig.editFormat.html", true),
        COMMONMARK("weblogConfig.editFormat.commonMark", true),
        RICHTEXT("weblogConfig.editFormat.richText", false);

        private String descriptionKey;

        private boolean usesPlainEditor;

        EditFormat(String descriptionKey, boolean usesPlainEditor) {
            this.descriptionKey = descriptionKey;
            this.usesPlainEditor = usesPlainEditor;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }
    }

    // Associated objects
    @JsonIgnore
    private List<WeblogCategory> weblogCategories = new ArrayList<>();
    @JsonIgnore
    private List<WeblogBookmark> bookmarks = new ArrayList<>();
    @JsonIgnore
    private List<MediaDirectory> mediaDirectories = new ArrayList<>();

    public Weblog() {
    }

    public Weblog(
            String handle,
            User creator,
            String name,
            String theme) {

        this.handle = handle;
        this.creator = creator;
        this.name = name;
        this.theme = theme;
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
    @Basic(optional = false)
    public String getHandle() {
        return this.handle;
    }

    public void setHandle(String handle) {
        this.handle = handle;
    }

    /**
     * Weblog name (title)
     */
    @Basic(optional = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic(optional = false)
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

    @ManyToOne
    @JoinColumn(name = "creatorid", nullable = false)
    @JsonIgnore
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public EditFormat getEditFormat() {
        return this.editFormat;
    }

    public void setEditFormat(EditFormat editFormat) {
        this.editFormat = editFormat;
    }

    public String getBlacklist() {
        return this.blacklist;
    }

    public void setBlacklist(String blacklist) {
        this.blacklist = blacklist;
        blacklistRegexRules = BlacklistCommentValidator.populateSpamRules(blacklist);
    }

    @Transient
    @JsonIgnore
    public List<java.util.regex.Pattern> getBlacklistRegexRules() {
        return blacklistRegexRules;
    }

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public CommentPolicy getAllowComments() {
        return this.allowComments;
    }

    public void setAllowComments(CommentPolicy allowComments) {
        this.allowComments = allowComments;
    }

    @Column(name = "commentdays", nullable = false)
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }

    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }

    @Basic(optional = false)
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

    @Basic(optional = false)
    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant date) {
        dateCreated = date;
    }

    /**
     * Set bean properties based on other bean.
     */
    public void setData(Weblog other) {
        this.setId(other.getId());
        this.setName(other.getName());
        this.setHandle(other.getHandle());
        this.setTagline(other.getTagline());
        this.setCreator(other.getCreator());
        this.setEditFormat(other.getEditFormat());
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
        this.setUsedForThemePreview(other.isUsedForThemePreview());
        this.setBookmarks(other.getBookmarks());
    }

    /**
     * Parse locale value and instantiate a Locale object.
     *
     * @return Locale
     */
    @Transient
    @JsonIgnore
    public Locale getLocaleInstance() {
        if (localeInstance == null) {
            localeInstance = Locale.forLanguageTag(getLocale());
        }
        return localeInstance;
    }

    /**
     * Return TimeZone instance for value of timeZone, else return system default instance.
     *
     * @return TimeZone
     */
    @Transient
    @JsonIgnore
    public ZoneId getZoneId() {
        if (getTimeZone() == null) {
            this.setTimeZone(TimeZone.getDefault().getID());
        }
        return TimeZone.getTimeZone(getTimeZone()).toZoneId();
    }

    @Basic(optional = false)
    public int getEntriesPerPage() {
        return entriesPerPage;
    }

    public void setEntriesPerPage(int entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
    }

    /**
     * If false, weblog will be hidden from public view.
     */
    @Basic(optional = false)
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
    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Update weblog's last modified date to the current date, to trigger cache
     * refreshing so users can see new categories, bookmarks, etc.
     */
    public void invalidateCache() {
        setLastModified(Instant.now());
    }

    // Used in templates
    @Transient
    public String getURL() {
        return WebloggerContext.getWeblogger().getUrlStrategy().getWeblogURL(this, false);
    }

    // Used in a few JSP's
    @Transient
    public String getAbsoluteURL() {
        return WebloggerContext.getWeblogger().getUrlStrategy().getWeblogURL(this, true);
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
        this.about = Utilities.removeHTML(about);
    }

    /**
     * Add a category to this weblog.
     */
    public void addCategory(WeblogCategory category) {
        // make sure category is not null
        if (category == null || category.getName() == null) {
            throw new IllegalArgumentException("Category cannot be null and must have a valid name");
        }

        // make sure we don't already have a category with that name
        if (hasCategory(category.getName())) {
            throw new IllegalArgumentException("Duplicate category name '" + category.getName() + "'");
        }

        // add it to our list of categories
        getWeblogCategories().add(category);
    }

    @OneToMany(targetEntity = WeblogCategory.class,
            cascade = CascadeType.REMOVE, mappedBy = "weblog")
    @OrderBy("position")
    public List<WeblogCategory> getWeblogCategories() {
        return weblogCategories;
    }

    public void setWeblogCategories(List<WeblogCategory> cats) {
        this.weblogCategories = cats;
    }

    public boolean hasCategory(String name) {
        for (WeblogCategory cat : getWeblogCategories()) {
            if (name.equals(cat.getName())) {
                return true;
            }
        }
        return false;
    }

    @OneToMany(targetEntity = WeblogBookmark.class,
            cascade = {CascadeType.ALL}, mappedBy = "weblog", orphanRemoval = true)
    public List<WeblogBookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<WeblogBookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    @OneToMany(targetEntity = MediaDirectory.class,
            cascade = {CascadeType.ALL}, mappedBy = "weblog")
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
            if (name.toLowerCase().equals(bookmark.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether this weblog contains the specified media file directory
     *
     * @param name directory name
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
    public boolean isUsedForThemePreview() {
        return usedForThemePreview;
    }

    public void setUsedForThemePreview(boolean usedForThemePreview) {
        this.usedForThemePreview = usedForThemePreview;
    }

    // convenience methods for populating fields from forms

    @Transient
    public boolean isApplyCommentDefaults() {
        return applyCommentDefaults;
    }

    public void setApplyCommentDefaults(boolean applyCommentDefaults) {
        this.applyCommentDefaults = applyCommentDefaults;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        return "{" + getId() + ", " + getHandle() + ", " + getName() + "}";
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof Weblog)) {
            return false;
        }
        Weblog o = (Weblog) other;
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
