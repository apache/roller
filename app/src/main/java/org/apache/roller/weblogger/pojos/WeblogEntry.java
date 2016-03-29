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

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.util.HTMLSanitizer;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;

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

/**
 * Represents a Weblog Entry.
 */
@Entity
@Table(name="weblog_entry")
@NamedQueries({
        @NamedQuery(name="WeblogEntry.getByCategory",
                query="SELECT w FROM WeblogEntry w WHERE w.category = ?1"),
        @NamedQuery(name="WeblogEntry.getByPinnedToMain&statusOrderByPubTimeDesc",
                query="SELECT w FROM WeblogEntry w WHERE w.pinnedToMain = ?1 AND w.status = ?2 ORDER BY w.pubTime DESC"),
        @NamedQuery(name="WeblogEntry.getByWeblog&AnchorOrderByPubTimeDesc",
                query="SELECT w FROM WeblogEntry w WHERE w.weblog = ?1 AND w.anchor = ?2 ORDER BY w.pubTime DESC"),
        @NamedQuery(name="WeblogEntry.getByWeblog&Anchor",
                query="SELECT w FROM WeblogEntry w WHERE w.weblog = ?1 AND w.anchor = ?2"),
        @NamedQuery(name="WeblogEntry.getByWeblog",
                query="SELECT w FROM WeblogEntry w WHERE w.weblog = ?1"),
        @NamedQuery(name="WeblogEntry.getCountDistinctByStatus",
                query="SELECT COUNT(e) FROM WeblogEntry e WHERE e.status = ?1"),
        @NamedQuery(name="WeblogEntry.getCountDistinctByStatus&Weblog",
                query="SELECT COUNT(e) FROM WeblogEntry e WHERE e.status = ?1 AND e.weblog = ?2"),
        @NamedQuery(name="WeblogEntry.updateCommentDaysByWeblog",
                query="UPDATE WeblogEntry e SET e.commentDays = ?1 WHERE e.weblog = ?2")
})
public class WeblogEntry implements Serializable {

    private static Log log = LogFactory.getFactory().getInstance(WeblogEntry.class);
    
    public static final long serialVersionUID = 2341505386843044125L;

    public enum PubStatus {DRAFT, PUBLISHED, PENDING, SCHEDULED}

    // Simple properies
    private String id;
    private String title;
    private String text;
    private String summary;
    private String notes;
    private String enclosureUrl;
    private String enclosureType;
    private Long enclosureLength;
    private String anchor;
    private Timestamp pubTime;
    private Timestamp updateTime;
    private String plugins;
    private Integer commentDays = 7;
    private Boolean rightToLeft = Boolean.FALSE;
    private Boolean pinnedToMain = Boolean.FALSE;
    private PubStatus status;
    private User creator;
    private String searchDescription;

    // set to true when switching between pending/draft/scheduled and published
    // either the aggregate table needs the entry's tags added (for published)
    // or subtracted (anything else)
    private Boolean refreshAggregates = Boolean.FALSE;

    // Associated objects
    private Weblog weblog;
    private Weblog wrappedWeblog;
    private WeblogCategory category;
    private WeblogCategory wrappedCategory;
    
    private Set<WeblogEntryTag> tagSet = new HashSet<WeblogEntryTag>();
    private Set<WeblogEntryTag> removedTags = new HashSet<WeblogEntryTag>();
    private Set<WeblogEntryTag> addedTags = new HashSet<WeblogEntryTag>();

    // temporary non-persisted fields used for form entry
    private int hours = 0;
    private int minutes = 0;
    private int seconds = 0;
    private String dateString;

    //----------------------------------------------------------- Construction
    
    public WeblogEntry() {
    }
    
    public WeblogEntry(
            WeblogCategory category,
            Weblog weblog,
            User creator,
            String title,
            String text,
            String anchor,
            Timestamp pubTime,
            Timestamp updateTime,
            PubStatus status) {
        this.id = WebloggerCommon.generateUUID();
        this.category = category;
        this.weblog = weblog;
        this.creator = creator;
        this.title = title;
        this.text = text;
        this.anchor = anchor;
        this.pubTime = pubTime;
        this.updateTime = updateTime;
        this.status = status;
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
        this.setCategory(other.getCategory());
        this.setWeblog(other.getWeblog());
        this.setCreator(other.getCreator());
        this.setTitle(other.getTitle());
        this.setText(other.getText());
        this.setSummary(other.getSummary());
        this.setNotes(other.getNotes());
        this.setSearchDescription(other.getSearchDescription());
        this.setAnchor(other.getAnchor());
        this.setPubTime(other.getPubTime());
        this.setUpdateTime(other.getUpdateTime());
        this.setStatus(other.getStatus());
        this.setPlugins(other.getPlugins());
        this.setCommentDays(other.getCommentDays());
        this.setRightToLeft(other.getRightToLeft());
        this.setPinnedToMain(other.getPinnedToMain());
        this.setEnclosureUrl(other.getEnclosureUrl());
        this.setEnclosureType(other.getEnclosureType());
        this.setEnclosureLength(other.getEnclosureLength());
        this.setTags(other.getTags());
    }
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(this.getAnchor());
        buf.append(", ").append(this.getTitle());
        buf.append(", ").append(this.getPubTime());
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntry)) {
            return false;
        }
        WeblogEntry o = (WeblogEntry)other;
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
    
   //------------------------------------------------------ Simple properties
    
    @Id
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) {
            return;
        }
        this.id = id;
    }


    @ManyToOne
    @JoinColumn(name="categoryid",nullable=false)
    public WeblogCategory getCategory() {
        return this.category;
    }
    
    public void setCategory(WeblogCategory category) {
        this.category = category;
    }
       
    /**
     * Return collection of WeblogCategory objects of this entry.
     * Added for symmetry with PlanetEntryData object.
     */
    @Transient
    public List<WeblogCategory> getCategories() {
        List<WeblogCategory> cats = new ArrayList<>();
        cats.add(getCategory());
        return cats;
    }

    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return this.weblog;
    }
    
    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @ManyToOne
    @JoinColumn(name="creatorid",nullable=false)
    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    @Basic(optional=false)
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
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
    @Column(name="search_description")
    public String getSearchDescription() {
        return searchDescription;
    }
    
    public void setSearchDescription(String searchDescription) {
        this.searchDescription = searchDescription;
    }

    /**
     * Content text for weblog entry (maps to RSS content:encoded and Atom content).
     */
    @Basic(optional=false)
    public String getText() {
        return this.text;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    @Column(name="enclosure_url")
    public String getEnclosureUrl() {
        return enclosureUrl;
    }

    public void setEnclosureUrl(String enclosureUrl) {
        this.enclosureUrl = enclosureUrl;
    }

    @Column(name="enclosure_type")
    public String getEnclosureType() {
        return enclosureType;
    }

    public void setEnclosureType(String enclosureType) {
        this.enclosureType = enclosureType;
    }

    @Column(name="enclosure_length")
    public Long getEnclosureLength() {
        return enclosureLength;
    }

    public void setEnclosureLength(Long enclosureLength) {
        this.enclosureLength = enclosureLength;
    }

    @Basic(optional=false)
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
     *
     * <p>TightBlog stores time using the timezone of the server itself. When
     * times are displayed in a user's weblog they must be translated
     * to the user's timeZone.</p>
     */
    public Timestamp getPubTime() {
        return this.pubTime;
    }
    
    public void setPubTime(Timestamp pubTime) {
        this.pubTime = pubTime;
    }
    
    /**
     * <p>Update time is the last time that an weblog entry was saved in the
     * Roller weblog editor or via web services API (XML-RPC or Atom).</p>
     *
     * <p>Roller stores time using the timeZone of the server itself. When
     * times are displayed  in a user's weblog they must be translated
     * to the user's timeZone.</p>
     */
    @Basic(optional=false)
    public Timestamp getUpdateTime() {
        return this.updateTime;
    }
    
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Basic(optional=false)
    @Enumerated(EnumType.STRING)
    public PubStatus getStatus() {
        return this.status;
    }
    
    public void setStatus(PubStatus status) {
        this.status = status;
    }
    
    /**
     * Comma-delimited list of this entry's Plugins.
     */
    public String getPlugins() {
        return plugins;
    }
    
    public void setPlugins(String string) {
        plugins = string;
    }

    /**
     * Number of days after pubTime that comments should be allowed, -1 for no limit.
     */
    @Basic(optional=false)
    public Integer getCommentDays() {
        return commentDays;
    }

    public void setCommentDays(Integer commentDays) {
        this.commentDays = commentDays;
    }
    
    /**
     * True for entries that should be rendered right to left.
     */
    @Basic(optional=false)
    public Boolean getRightToLeft() {
        return rightToLeft;
    }

    public void setRightToLeft(Boolean rightToLeft) {
        this.rightToLeft = rightToLeft;
    }
    
    /**
     * True if blog entry should be pinned to the top of the site main blog.
     */
    @Basic(optional=false)
    public Boolean getPinnedToMain() {
        return pinnedToMain;
    }

    public void setPinnedToMain(Boolean pinnedToMain) {
        this.pinnedToMain = pinnedToMain;
    }

    @OneToMany(targetEntity=org.apache.roller.weblogger.pojos.WeblogEntryTag.class,
            cascade={CascadeType.PERSIST, CascadeType.REMOVE}, mappedBy="weblogEntry")
    @OrderBy("name")
    public Set<WeblogEntryTag> getTags() {
         return tagSet;
    }

    @SuppressWarnings("unused")
    private void setTags(Set<WeblogEntryTag> tagSet) {
         this.tagSet = tagSet;
         this.removedTags = new HashSet<>();
         this.addedTags = new HashSet<>();
    }
     
    /**
     * TightBlog lowercases all tags based on locale because there's not a 1:1 mapping
     * between uppercase/lowercase characters across all languages.  
     * @param name tag name
     * @throws WebloggerException
     */
    public void addTag(String name) throws WebloggerException {
        Locale localeObject = getWeblog() != null ? getWeblog().getLocaleInstance() : Locale.getDefault();
        name = Utilities.normalizeTag(name, localeObject);
        if (name.length() == 0) {
            return;
        }
        
        for (WeblogEntryTag tag : getTags()) {
            if (tag.getName().equals(name)) {
                return;
            }
        }

        WeblogEntryTag tag = new WeblogEntryTag();
        tag.setName(name);
        tag.setWeblog(getWeblog());
        tag.setWeblogEntry(this);
        tagSet.add(tag);
        
        addedTags.add(tag);
    }

    @Transient
    public Set<WeblogEntryTag> getAddedTags() {
        return addedTags;
    }

    @Transient
    public Set<WeblogEntryTag> getRemovedTags() {
        return removedTags;
    }

    @Transient
    public String getTagsAsString() {
        StringBuilder sb = new StringBuilder();
        // Sort by name
        Set<WeblogEntryTag> tmp = new TreeSet<>(WeblogEntryTag.Comparator);
        tmp.addAll(getTags());
        for (WeblogEntryTag entryTag : tmp) {
            sb.append(entryTag.getName()).append(" ");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return sb.toString();
    }

    public void setTagsAsString(String tags) throws WebloggerException {
        if (StringUtils.isEmpty(tags)) {
            removedTags.addAll(tagSet);
            tagSet.clear();
            return;
        }

        List<String> updatedTags = Utilities.splitStringAsTags(tags);
        Set<String> newTags = new HashSet<>(updatedTags.size());
        Locale localeObject = getWeblog() != null ? getWeblog().getLocaleInstance() : Locale.getDefault();

        for (String name : updatedTags) {
            newTags.add(Utilities.normalizeTag(name, localeObject));
        }

        // remove old ones no longer passed.
        for (Iterator it = tagSet.iterator(); it.hasNext();) {
            WeblogEntryTag tag = (WeblogEntryTag) it.next();
            if (!newTags.contains(tag.getName())) {
                // tag no longer listed in UI, needs removal from DB
                removedTags.add(tag);
                it.remove();
            } else {
                // already in persisted set, therefore isn't new
                newTags.remove(tag.getName());
            }
        }

        for (String newTag : newTags) {
            addTag(newTag);
        }
    }

    /**
     * True if comments are still allowed on this entry considering the
     * allowComments and commentDays fields as well as the weblog and 
     * site-wide configs.
     */
    @Transient
    public boolean getCommentsStillAllowed() {
        if (!WebloggerFactory.getWeblogger().getPropertiesManager().getBooleanProperty("users.comments.enabled")) {
            return false;
        }
        if (!Boolean.TRUE.equals(getWeblog().getAllowComments())) {
            return false;
        }
        if (getCommentDays() == null || getCommentDays() == 0) {
            return false;
        }
        if (getCommentDays() < 0) {
            return true;
        }
        boolean ret = false;

        Date inPubTime = getPubTime();
        if (inPubTime != null) {
            Calendar expireCal = Calendar.getInstance(
                    getWeblog().getLocaleInstance());
            expireCal.setTime(inPubTime);
            expireCal.add(Calendar.DATE, getCommentDays());
            Date expireDay = expireCal.getTime();
            Date today = new Date();
            if (today.before(expireDay)) {
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Format the publish time of this weblog entry using the specified pattern.
     * See java.text.SimpleDateFormat for more information on this format.
     *
     * @see java.text.SimpleDateFormat
     * @return Publish time formatted according to pattern.
     */
    public String formatPubTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern,
                    this.getWeblog().getLocaleInstance());
            
            return format.format(getPubTime());
        } catch (RuntimeException e) {
            log.error("Unexpected exception", e);
        }
        
        return "ERROR: formatting date";
    }
    
    /**
     * Format the update time of this weblog entry using the specified pattern.
     * See java.text.SimpleDateFormat for more information on this format.
     *
     * @see java.text.SimpleDateFormat
     * @return Update time formatted according to pattern.
     */
    public String formatUpdateTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            
            return format.format(getUpdateTime());
        } catch (RuntimeException e) {
            log.error("Unexpected exception", e);
        }
        
        return "ERROR: formatting date";
    }
    
    @Transient
    public List<WeblogEntryComment> getComments() {
        return getComments(true, true);
    }
    
    /**
     * TODO: why is this method exposed to users with ability to get spam/non-approved comments?
     */
    public List<WeblogEntryComment> getComments(boolean ignoreSpam, boolean approvedOnly) {
        List<WeblogEntryComment> list = new ArrayList<>();
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();

            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(getWeblog());
            csc.setEntry(this);
            csc.setStatus(approvedOnly ? WeblogEntryComment.ApprovalStatus.APPROVED : null);
            return wmgr.getComments(csc);
        } catch (WebloggerException alreadyLogged) {}
        return list;
    }

    @Transient
    public int getCommentCount() {
        List comments = getComments(true, true);
        return comments.size();
    }
    
    /**
     * Returns absolute entry permalink.
     */
    @Transient
    public String getPermalink() {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogEntryURL(getWeblog(), getAnchor(), true);
    }
    
    /**
     * Return the Title of this post, or the first 255 characters of the entry's text.
     * @return String
     */
    @Transient
    public String getDisplayTitle() {
        if ( getTitle()==null || getTitle().trim().equals("") ) {
            return StringUtils.left(Utilities.removeHTML(getText()), WebloggerCommon.TEXTWIDTH_255);
        }
        return Utilities.removeHTML(getTitle());
    }
    
    /**
     * A no-op. TODO: fix formbean generation so this is not needed.
     */
    public void setPermalink(String string) {}
    
    /**
     * A no-op. TODO: fix formbean generation so this is not needed.
     */
    public void setPermaLink(String string) {}
    
    /**
     * A no-op.
     * TODO: fix formbean generation so this is not needed.
     * @param string
     */
    public void setDisplayTitle(String string) {
    }
    
    /**
     * A no-op.
     * TODO: fix formbean generation so this is not needed.
     * @param string
     */
    public void setRss09xDescription(String string) {
    }
    
    /**
     * Convenience method to transform mPlugins to a List
     * @return
     */
    @Transient
    public List<String> getPluginsList() {
        if (getPlugins() != null) {
            return Arrays.asList( StringUtils.split(getPlugins(), ",") );
        }
        return new ArrayList<String>();
    }

    // Struts Checkboxlist control needs a String[] to specify selected values
    @Transient
    public String[] getPluginsArray() {
        return StringUtils.split(plugins, ",");
    }

    public void setPluginsArray(String[] strings) {
        plugins = StringUtils.join(strings, ",");
    }

    /** Convenience method for checking status */
    @Transient
    public boolean isDraft() {
        return PubStatus.DRAFT.equals(getStatus());
    }

    /** Convenience method for checking status */
    @Transient
    public boolean isPending() {
        return PubStatus.PENDING.equals(getStatus());
    }

    /** Convenience method for checking status */
    @Transient
    public boolean isScheduled() {
        return PubStatus.SCHEDULED.equals(getStatus());
    }

    /** Convenience method for checking status */
    @Transient
    public boolean isPublished() {
        return PubStatus.PUBLISHED.equals(getStatus());
    }

    /**
     * Get entry text, transformed by plugins enabled for entry.
     */
    @Transient
    public String getTransformedText() {
        return render(getText());
    }

    /**
     * Get entry summary, transformed by plugins enabled for entry.
     */
    @Transient
    public String getTransformedSummary() {
        return render(getSummary());
    }

    /**
     * Transform string based on plugins enabled for this weblog entry.
     */
    private String render(String str) {
        log.debug("Applying page plugins to string");
        WeblogEntryManager mgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        return mgr.applyWeblogEntryPlugins(this, str);
    }

    /**
     * Get the right transformed display content depending on the situation.
     *
     * If the readMoreLink is specified then we assume the caller wants to
     * prefer summary over content and we include a "Read More" link at the
     * end of the summary if it exists.  Otherwise, if the readMoreLink is
     * empty or null then we assume the caller prefers content over summary.
     */
    public String displayContent(String readMoreLink) {
        
        String displayContent = null;
        
        if(readMoreLink == null || readMoreLink.trim().length() < 1 || 
                "nil".equals(readMoreLink)) {
            
            // no readMore link means permalink, so prefer text over summary
            if(StringUtils.isNotEmpty(this.getText())) {
                displayContent = this.getTransformedText();
            } else {
                displayContent = this.getTransformedSummary();
            }
        } else {
            // not a permalink, so prefer summary over text
            // include a "read more" link if needed
            if(StringUtils.isNotEmpty(this.getSummary())) {
                displayContent = this.getTransformedSummary();
                if(StringUtils.isNotEmpty(this.getText())) {
                    // add read more
                    List<String> args = new ArrayList<String>();
                    args.add(readMoreLink);
                    
                    // TODO: we need a more appropriate way to get the view locale here
                    String readMore = I18nMessages.getMessages(getWeblog().getLocaleInstance()).getString("macro.weblog.readMoreLink", args);
                    
                    displayContent += readMore;
                }
            } else {
                displayContent = this.getTransformedText();
            }
        }
        
        return HTMLSanitizer.conditionallySanitize(displayContent);
    }
    
    
    /**
     * Get the right transformed display content.
     */
    @Transient
    public String getDisplayContent() {
        return displayContent(null);
    }

    @Transient
    public Boolean getRefreshAggregates() {
        return refreshAggregates;
    }

    public void setRefreshAggregates(Boolean refreshAggregates) {
        this.refreshAggregates = refreshAggregates;
    }

    @Transient
    public String getCategoryId() {
        return category.getId();
    }

    public void setCategoryId(String categoryId) {
        try {
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            setCategory(wmgr.getWeblogCategory(categoryId));
        } catch (WebloggerException e) {
            log.error("Error setting category for blog entry", e);
        }
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
    public int getSeconds() {
        return seconds;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    @Transient
    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    /**
     * A read-only copy for usage within templates, with fields limited
     * to just those we wish to provide to those templates.
     */
    public WeblogEntry templateCopy() {
        WeblogEntry copy = new WeblogEntry();
        copy.setData(this);
        copy.setId(null);
        copy.setCategory(getWrappedCategory());
        copy.setWeblog(getWrappedWeblog());
        copy.setSearchDescription(HTMLSanitizer.conditionallySanitize(searchDescription));
        return copy;
    }

    @Transient
    public Weblog getWrappedWeblog() {
        if (wrappedWeblog == null) {
            wrappedWeblog = weblog.templateCopy();
            log.info("Weblog: miss on " + weblog.getHandle());
        } else {
            log.info("Weblog: hit on " + weblog.getHandle());
        }
        return wrappedWeblog;
    }

    @Transient
    public WeblogCategory getWrappedCategory() {
        if (wrappedCategory == null) {
            wrappedCategory = category.templateCopy();
            log.info("Weblog Category: miss on " + weblog.getHandle());
        } else {
            log.info("Weblog Category: hit on " + weblog.getHandle());
        }
        return wrappedCategory;
    }

    public static java.util.Comparator<WeblogEntry> Comparator = new Comparator<WeblogEntry>() {
        public int compare(WeblogEntry val1, WeblogEntry val2) {

            long pubTime1 = val1.getPubTime().getTime();
            long pubTime2 = val2.getPubTime().getTime();

            if (pubTime1 > pubTime2) {
                return -1;
            }
            else if (pubTime1 < pubTime2) {
                return 1;
            }

            // if pubTimes are the same, return results of String.compareTo() on Title
            return val1.getTitle().compareTo(val2.getTitle());
        }
    };
}
