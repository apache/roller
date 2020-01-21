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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.tightblog.domain.WebloggerProperties.CommentPolicy;
import org.tightblog.domain.WebloggerProperties.SpamPolicy;
import org.tightblog.rendering.service.CommentSpamChecker;
import org.tightblog.util.Utilities;

import javax.validation.constraints.NotBlank;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.Pattern;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Weblogs have a many-to-many association with users. They also have one-to-many and
 * one-direction associations with weblog entries, weblog categories, bookmarks and
 * other objects.
 */
@Entity
@Table(name = "weblog")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Weblog implements WeblogOwned {

    private String id = Utilities.generateUUID();
    private int hashCode;
    @NotBlank(message = "{weblogConfig.error.handleNull}")
    @Pattern(regexp = "[a-z0-9\\-]*", message = "{weblogConfig.error.invalidHandle}")
    private String handle;
    @NotBlank(message = "{weblogConfig.error.nameNull}")
    private String name;
    private String tagline;
    private EditFormat editFormat = EditFormat.HTML;
    private String blacklist;
    private CommentPolicy allowComments = CommentPolicy.MODERATE_NONPUB;
    private SpamPolicy spamPolicy = SpamPolicy.NO_EMAIL;
    @NotBlank(message = "{weblogConfig.error.themeNull}")
    private String theme;
    private String locale;
    private String timeZone;
    private Boolean visible = Boolean.TRUE;
    private Instant dateCreated = Instant.now();
    private int defaultCommentDays = -1;
    private int entriesPerPage = 12;
    private Instant lastModified = Instant.now();
    private String about;
    private User creator;
    private String analyticsCode;
    private int hitsToday;
    private boolean applyCommentDefaults;

    // Transient, derived from and re-calculated each time blacklist property is set
    private List<java.util.regex.Pattern> blacklistRegexRules = new ArrayList<>();

    // temporary non-persisted fields used for form entry & retrieving associated data
    private int unapprovedComments;

    private Locale localeInstance;

    private String absoluteURL;

    public enum EditFormat {
        HTML("weblogConfig.editFormat.html"),
        COMMONMARK("weblogConfig.editFormat.commonMark");

        private String descriptionKey;

        EditFormat(String descriptionKey) {
            this.descriptionKey = descriptionKey;
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

    public Weblog(Weblog other) {
        this.setId(other.getId());
        this.setName(other.getName());
        this.setHandle(other.getHandle());
        this.setTagline(other.getTagline());
        this.setCreator(other.getCreator());
        this.setEditFormat(other.getEditFormat());
        this.setBlacklist(other.getBlacklist());
        this.setAllowComments(other.getAllowComments());
        this.setSpamPolicy(other.getSpamPolicy());
        this.setTheme(other.getTheme());
        this.setLocale(other.getLocale());
        this.setTimeZone(other.getTimeZone());
        this.setVisible(other.getVisible());
        this.setDateCreated(other.getDateCreated());
        this.setEntriesPerPage(other.getEntriesPerPage());
        this.setLastModified(other.getLastModified());
        this.setWeblogCategories(other.getWeblogCategories());
        this.setAnalyticsCode(other.getAnalyticsCode());
        this.setBookmarks(other.getBookmarks());
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
        blacklistRegexRules = CommentSpamChecker.compileBlacklist(blacklist);
    }

    @Transient
    @JsonIgnore
    public List<java.util.regex.Pattern> getBlacklistRegexRules() {
        return blacklistRegexRules;
    }

    @Column(name = "comment_policy", nullable = false)
    @Enumerated(EnumType.STRING)
    public CommentPolicy getAllowComments() {
        return this.allowComments;
    }

    public void setAllowComments(CommentPolicy allowComments) {
        this.allowComments = allowComments;
    }

    @Column(name = "spam_policy", nullable = false)
    @Enumerated(EnumType.STRING)
    public SpamPolicy getSpamPolicy() {
        return this.spamPolicy;
    }

    public void setSpamPolicy(SpamPolicy spamPolicy) {
        this.spamPolicy = spamPolicy;
    }

    @Column(name = "commentdays", nullable = false)
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }

    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
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

    // Used in templates and a few JSP's
    @Transient
    public String getAbsoluteURL() {
        return absoluteURL;
    }

    public void setAbsoluteURL(String absoluteURL) {
        this.absoluteURL = absoluteURL;
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
            cascade = CascadeType.ALL, mappedBy = "weblog", orphanRemoval = true)
    @OrderBy("position")
    public List<WeblogCategory> getWeblogCategories() {
        return weblogCategories;
    }

    public void setWeblogCategories(List<WeblogCategory> cats) {
        this.weblogCategories = cats;
    }

    public boolean hasCategory(String categoryName) {
        for (WeblogCategory cat : getWeblogCategories()) {
            if (categoryName.equals(cat.getName())) {
                return true;
            }
        }
        return false;
    }

    @OneToMany(targetEntity = WeblogBookmark.class,
            cascade = CascadeType.ALL, mappedBy = "weblog", orphanRemoval = true)
    public List<WeblogBookmark> getBookmarks() {
        return bookmarks;
    }

    public void setBookmarks(List<WeblogBookmark> bookmarks) {
        this.bookmarks = bookmarks;
    }

    @OneToMany(targetEntity = MediaDirectory.class,
            cascade = CascadeType.ALL, mappedBy = "weblog", orphanRemoval = true)
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
     * @param bookmarkName The name of the bookmark to check for.
     * @return boolean true if exists, false otherwise.
     */
    public boolean hasBookmark(String bookmarkName) {
        for (WeblogBookmark bookmark : this.getBookmarks()) {
            if (bookmarkName.toLowerCase().equals(bookmark.getName().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Indicates whether this weblog contains the specified media file directory
     *
     * @param directoryName directory name
     * @return true if directory is present, false otherwise.
     */
    public boolean hasMediaDirectory(String directoryName) {
        for (MediaDirectory directory : this.getMediaDirectories()) {
            if (directory.getName().equals(directoryName)) {
                return true;
            }
        }
        return false;
    }

    public MediaDirectory getMediaDirectory(String directoryName) {
        for (MediaDirectory dir : this.getMediaDirectories()) {
            if (directoryName.equals(dir.getName())) {
                return dir;
            }
        }
        return null;
    }

    // convenience methods for populating fields from forms

    @Transient
    public boolean isApplyCommentDefaults() {
        return applyCommentDefaults;
    }

    public void setApplyCommentDefaults(boolean applyCommentDefaults) {
        this.applyCommentDefaults = applyCommentDefaults;
    }

    @Override
    public String toString() {
        return String.format("Weblog: handle=%s, name=%s, id=%s", handle, name, id);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof Weblog && Objects.equals(id, ((Weblog) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }

    static final Comparator<Weblog> HANDLE_COMPARATOR = Comparator.comparing(Weblog::getHandle);

    @Transient
    public int getUnapprovedComments() {
        return unapprovedComments;
    }

    public void setUnapprovedComments(int unapprovedComments) {
        this.unapprovedComments = unapprovedComments;
    }

    @Override
    @JsonIgnore
    public Weblog getWeblog() {
        return this;
    }
}
