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
import org.tightblog.dao.WeblogEntryCommentDao;
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
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Weblog Entry.
 */
@Entity
@Table(name = "weblog_entry")
public class WeblogEntry {

    public enum PubStatus { DRAFT, PUBLISHED, PENDING, SCHEDULED }

    public enum CommentDayOption {
        UNLIMITED(-1, "entryEdit.unlimitedCommentDays"),
        ZERO(0, "entryEdit.days0"),
        THREE(3, "entryEdit.days3"),
        SEVEN(7, "entryEdit.days7"),
        FOURTEEN(14, "entryEdit.days14"),
        THIRTY(30, "entryEdit.days30"),
        SIXTY(60, "entryEdit.days60"),
        NINETY(90, "entryEdit.days90");

        int days;

        String descriptionKey;

        CommentDayOption(int days, String descriptionKey) {
            this.days = days;
            this.descriptionKey = descriptionKey;
        }

        public int getDays() {
            return days;
        }

        public String getDescriptionKey() {
            return descriptionKey;
        }
    }

    // Simple properties
    private String id = Utilities.generateUUID();
    private int hashCode;
    @NotBlank(message = "{entryEdit.error.titleNull}")
    private String title;
    @NotBlank(message = "{entryEdit.error.textNull}")
    private String text;
    private String summary;
    private String notes;
    private Weblog.EditFormat editFormat = Weblog.EditFormat.HTML;
    private String enclosureUrl;
    private String enclosureType;
    private Long enclosureLength;
    private String anchor;
    private Instant pubTime;
    private Instant updateTime;
    private Integer commentDays = CommentDayOption.SEVEN.getDays();
    private PubStatus status;
    private User creator;
    private String searchDescription;

    // Associated objects
    private Weblog weblog;
    private WeblogCategory category;

    private Set<WeblogEntryTag> tagSet = new HashSet<>();

    // temporary non-persisted fields used for form entry & retrieving associated data
    private WeblogEntryCommentDao weblogEntryCommentDao;
    private int hours;
    private int minutes;

    private String dateString;
    private String editUrl;
    private String commentsUrl;
    private String permalink;
    private String previewUrl;
    private String tagsAsString;

    public WeblogEntry() {
    }

    public WeblogEntry(String title, String editUrl) {
        this.title = title;
        this.editUrl = editUrl;
    }

    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "categoryid", nullable = false)
    public WeblogCategory getCategory() {
        return this.category;
    }

    public void setCategory(WeblogCategory category) {
        this.category = category;
    }

    @ManyToOne
    @JoinColumn(name = "weblogid", nullable = false)
    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @ManyToOne
    @JoinColumn(name = "creatorid", nullable = false)
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Basic(optional = false)
    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = Utilities.removeHTML(title);
    }

    /**
     * Summary for weblog entry (maps to RSS description and Atom summary).
     */
    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    /**
     * Blogger's notes for weblog entry
     */
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    /**
     * Search description for weblog entry (intended for HTML header).
     */
    @Column(name = "search_description")
    public String getSearchDescription() {
        return searchDescription;
    }

    public void setSearchDescription(String searchDescription) {
        this.searchDescription = Utilities.removeHTML(searchDescription);
    }

    /**
     * Content text for weblog entry (maps to RSS content:encoded and Atom content).
     */
    @Basic(optional = false)
    public String getText() {
        return this.text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public Weblog.EditFormat getEditFormat() {
        return this.editFormat;
    }

    public void setEditFormat(Weblog.EditFormat editFormat) {
        this.editFormat = editFormat;
    }

    @Column(name = "enclosure_url")
    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    @Column(name = "enclosure_type")
    public String getEnclosureType() {
        return enclosureType;
    }

    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    @Column(name = "enclosure_length")
    public Long getEnclosureLength() {
        return enclosureLength;
    }

    public void setEnclosureLength(Long enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    @Basic(optional = false)
    public String getAnchor() {
        return this.anchor;
    }

    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }

    /**
     * <p>Publish time is the time that an entry is to be (or was) made available
     * for viewing by newsfeed readers and visitors to the weblogger site.</p>
     * <p>
     * <p>TightBlog stores time using the timezone of the server itself. When
     * times are displayed in a user's weblog they must be translated
     * to the user's timeZone.</p>
     */
    public Instant getPubTime() {
        return this.pubTime;
    }

    public void setPubTime(Instant pubTime) {
        this.pubTime = pubTime;
    }

    /**
     * <p>Update time is the last time that an weblog entry was saved in the
     * weblog editor.</p>
     * <p>
     * <p>TightBlog stores time using UTC. When times are displayed in a user's weblog
     * they must be translated to the weblog's time zone.</p>
     */
    @Basic(optional = false)
    public Instant getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Instant updateTime) {
        this.updateTime = updateTime;
    }

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public PubStatus getStatus() {
        return this.status;
    }

    public void setStatus(PubStatus status) {
        this.status = status;
    }

    /**
     * Number of days after pubTime that comments should be allowed, -1 for no limit.
     */
    @Basic(optional = false)
    public Integer getCommentDays() {
        return commentDays;
    }

    public void setCommentDays(Integer commentDays) {
        this.commentDays = commentDays;
    }

    @OneToMany(targetEntity = WeblogEntryTag.class,
            cascade = CascadeType.ALL, mappedBy = "weblogEntry", orphanRemoval = true)
    @OrderBy("name")
    public Set<WeblogEntryTag> getTags() {
        return tagSet;
    }

    public void setTags(Set<WeblogEntryTag> tags) {
        this.tagSet = tags;
    }

    /**
     * Replace the current set of tags with those in the new list.  Any WeblogEntryTags
     * already attached to the instance and remaining in the new list will be reused.
     */
    public void updateTags(Set<String> newTags) {
        Locale localeObject = getWeblog().getLocaleInstance();
        Set<WeblogEntryTag> newTagSet = new HashSet<>();
        for (String tagStr : newTags) {
            String normalizedString = Utilities.normalizeTag(tagStr, localeObject);
            boolean found = false;
            for (WeblogEntryTag currentTag : getTags()) {
                if (currentTag.getName().equals(normalizedString)) {
                    // reuse currently existing
                    newTagSet.add(currentTag);
                    found = true;
                }
            }
            if (!found) {
                // new tag added by user, has to be created.
                WeblogEntryTag tag = new WeblogEntryTag();
                tag.setName(normalizedString);
                tag.setWeblog(getWeblog());
                tag.setWeblogEntry(this);
                newTagSet.add(tag);
            }
        }
        // will erase tags not in the new list, JPA will delete them from DB.
        setTags(newTagSet);
    }

    @Transient
    public String getTagsAsString() {
        if (tagsAsString == null) {
            tagsAsString = String.join(" ", getTags().stream().map(WeblogEntryTag::getName).collect(Collectors.toSet()));
        }
        return tagsAsString;
    }

    public void setTagsAsString(String tagsAsString) {
        this.tagsAsString = tagsAsString;
    }

    public void setWeblogEntryCommentDao(WeblogEntryCommentDao weblogEntryCommentDao) {
        this.weblogEntryCommentDao = weblogEntryCommentDao;
    }

    @Transient
    @JsonIgnore
    public List<WeblogEntryComment> getComments() {
        return weblogEntryCommentDao != null ? weblogEntryCommentDao.findByWeblogEntryAndStatusApproved(this)
                : new ArrayList<>();
    }

    @Transient
    public int getCommentCount() {
        return weblogEntryCommentDao != null ? weblogEntryCommentDao.countByWeblogEntryAndStatusApproved(this) : 0;
    }

    @Transient
    public int getCommentCountIncludingUnapproved() {
        return weblogEntryCommentDao != null ? weblogEntryCommentDao.countByWeblogEntry(this) : 0;
    }

    /**
     * Returns absolute entry permalink.
     */
    @Transient
    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    /**
     * Convenience method for checking published status
     */
    @Transient
    public boolean isPublished() {
        return PubStatus.PUBLISHED.equals(getStatus());
    }

    @Transient
    public int getHours() {
        return hours;
    }

    public void setHours(int hours) {
        this.hours = hours;
    }

    @Transient
    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    @Transient
    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    @Transient
    public String getEditUrl() {
        return editUrl;
    }

    public void setEditUrl(String editUrl) {
        this.editUrl = editUrl;
    }

    @Transient
    public String getCommentsUrl() {
        return commentsUrl;
    }

    public void setCommentsUrl(String commentsUrl) {
        this.commentsUrl = commentsUrl;
    }

    @Transient
    public String getPreviewUrl() {
        return previewUrl;
    }

    public void setPreviewUrl(String previewUrl) {
        this.previewUrl = previewUrl;
    }

    @Override
    public String toString() {
        return "WeblogEntry: id=" + id + ", weblog=" + ((weblog == null) ? "(null)" : weblog.getHandle()) +
                ", anchor=" + anchor + ", pub time=" + pubTime;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof WeblogEntry && Objects.equals(id, ((WeblogEntry) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }
}
