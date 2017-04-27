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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.roller.weblogger.util.Utilities;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Represents a Weblog Entry.
 */
@Entity
@Table(name = "weblog_entry")
@NamedQueries({
        @NamedQuery(name = "WeblogEntry.getByCategory",
                query = "SELECT w FROM WeblogEntry w WHERE w.category = ?1"),
        @NamedQuery(name = "WeblogEntry.getByWeblog&AnchorOrderByPubTimeDesc",
                query = "SELECT w FROM WeblogEntry w WHERE w.weblog = ?1 AND w.anchor = ?2 ORDER BY w.pubTime DESC"),
        @NamedQuery(name = "WeblogEntry.getByWeblog&Anchor",
                query = "SELECT w FROM WeblogEntry w WHERE w.weblog = ?1 AND w.anchor = ?2"),
        @NamedQuery(name = "WeblogEntry.getByWeblog",
                query = "SELECT w FROM WeblogEntry w WHERE w.weblog = ?1"),
        @NamedQuery(name = "WeblogEntry.updateCommentDaysByWeblog",
                query = "UPDATE WeblogEntry e SET e.commentDays = ?1 WHERE e.weblog = ?2")
})
public class WeblogEntry {

    private static Logger log = LoggerFactory.getLogger(WeblogEntry.class);

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
    private String id;
    @NotBlank(message = "{Entry.error.titleNull}")
    private String title;
    @NotBlank(message = "{Entry.error.textNull}")
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
    private User creator = null;
    private String searchDescription;

    // Associated objects
    private Weblog weblog;
    private WeblogCategory category;

    private Set<WeblogEntryTag> tagSet = new HashSet<>();

    // temporary non-persisted fields used for form entry
    private int hours = 0;
    private int minutes = 0;
    private String tagsAsString;
    private String dateString;
    private String editUrl;
    private String commentsUrl;
    private String permalink;
    private String previewUrl;

    //----------------------------------------------------------- Construction

    public WeblogEntry() {
    }

    public WeblogEntry(String title, String editUrl) {
        this.title = title;
        this.editUrl = editUrl;
    }

    public WeblogEntry(WeblogEntry otherData) {
        this.setData(otherData);
    }

    //---------------------------------------------------------- Initialization

    /**
     * Set bean properties based on other bean.
     */
    public void setData(WeblogEntry other) {
        this.setId(other.getId());
        this.setCreator(other.getCreator());
        this.setCategory(other.getCategory());
        this.setWeblog(other.getWeblog());
        this.setEditFormat(other.getEditFormat());
        this.setTitle(other.getTitle());
        this.setText(other.getText());
        this.setSummary(other.getSummary());
        this.setNotes(other.getNotes());
        this.setSearchDescription(other.getSearchDescription());
        this.setAnchor(other.getAnchor());
        this.setPubTime(other.getPubTime());
        this.setUpdateTime(other.getUpdateTime());
        this.setStatus(other.getStatus());
        this.setCommentDays(other.getCommentDays());
        this.setEnclosureUrl(other.getEnclosureUrl());
        this.setEnclosureType(other.getEnclosureType());
        this.setEnclosureLength(other.getEnclosureLength());
        this.setTags(other.getTags());
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        String result = "{" + getId() + ", ";
        result += this.getAnchor() + ", ";
        result += this.getTitle() + ", ";
        result += this.getPubTime() + "}";
        return result;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntry)) {
            return false;
        }
        WeblogEntry o = (WeblogEntry) other;
        return new EqualsBuilder()
                .append(getAnchor(), o.getAnchor())
                .append(getWeblog(), o.getWeblog())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getAnchor())
                .append(getWeblog())
                .toHashCode();
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

    //-------------------------------------------------------------------------

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
     * Roller weblog editor or via web services API (XML-RPC or Atom).</p>
     * <p>
     * <p>Roller stores time using the timeZone of the server itself. When
     * times are displayed  in a user's weblog they must be translated
     * to the user's timeZone.</p>
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

    @OneToMany(targetEntity = org.apache.roller.weblogger.pojos.WeblogEntryTag.class,
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy = "weblogEntry", orphanRemoval = true)
    @OrderBy("name")
    public Set<WeblogEntryTag> getTags() {
        return tagSet;
    }

    public void setTags(Set<WeblogEntryTag> tagSet) {
        this.tagSet = tagSet;
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
        return tagsAsString;
    }

    public void setTagsAsString(String tags) {
        tagsAsString = tags;
    }

    /**
     * True if comments are still allowed on this entry considering the
     * commentDays field as well as the weblog and site-wide configs.
     */
    @Transient
    @JsonIgnore
    // Not serialized into JSON as necessary weblog object not always present.
    public boolean getCommentsStillAllowed() {
        if (WebloggerProperties.CommentPolicy.NONE.equals(
                WebloggerContext.getWebloggerProperties().getCommentPolicy())) {
            return false;
        }
        if (WebloggerProperties.CommentPolicy.NONE.equals(getWeblog().getAllowComments())) {
            return false;
        }
        if (getCommentDays() == 0) {
            return false;
        }
        if (getCommentDays() < 0) {
            return true;
        }
        boolean ret = false;

        Instant inPubTime = getPubTime();
        if (inPubTime != null) {
            Instant lastCommentDay = inPubTime.plus(getCommentDays(), ChronoUnit.DAYS);
            if (Instant.now().isBefore(lastCommentDay)) {
                ret = true;
            }
        }
        return ret;
    }

    @Transient
    @JsonIgnore
    public List<WeblogEntryComment> getComments() {
        WeblogEntryManager wmgr = WebloggerContext.getWeblogger().getWeblogEntryManager();
        return wmgr.getComments(CommentSearchCriteria.approvedComments(this, true));
    }

    @Transient
    public long getCommentCount() {
        WeblogEntryManager wmgr = WebloggerContext.getWeblogger().getWeblogEntryManager();
        return wmgr.getCommentCount(CommentSearchCriteria.approvedComments(this, true));
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
}
