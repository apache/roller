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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.text.StringEscapeUtils;
import org.tightblog.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "weblog_entry_comment")
public class WeblogEntryComment implements WeblogOwned {

    public enum ApprovalStatus {
        // Comment missing required fields like name or email.  Not serialized to database.
        INVALID,
        // Comment identified as spam, either deleted or subject to moderation depending on blog config.
        SPAM,
        // Comment not identified as spam, subject to moderation.
        PENDING,
        // Comment approved and visible (published)
        APPROVED,
        // Approved comment subsequently disapproved and not viewable on blog.  No email notification
        // sent to commenter if re-approved.
        DISAPPROVED
    }

    public enum SpamCheckResult {
        SPAM, NOT_SPAM
    }

    // attributes
    private String id = Utilities.generateUUID();
    private int hashCode;
    private String name;
    private String email;
    private String url;
    private String content;
    private Instant postTime;
    private ApprovalStatus status = ApprovalStatus.DISAPPROVED;
    private Boolean notify = Boolean.FALSE;
    private String remoteHost;
    private String referrer;
    private String userAgent;

    // associations
    private Weblog weblog;
    private WeblogEntry weblogEntry;

    private User blogger;

    public WeblogEntryComment() {
    }

    public WeblogEntryComment(String content) {
        this.content = content;
    }

    // transient field involved during comment submittal
    private String submitResponseMessage;

    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "weblogid", nullable = false)
    @JsonIgnore
    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog website) {
        this.weblog = website;
    }

    @ManyToOne
    @JoinColumn(name = "entryid", nullable = false)
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry entry) {
        weblogEntry = entry;
    }

    @ManyToOne
    @JoinColumn(name = "bloggerid")
    public User getBlogger() {
        return blogger;
    }

    public void setBlogger(User blogger) {
        this.blogger = blogger;
    }

    @Basic(optional = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic(optional = false)
    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic(optional = false)
    public Instant getPostTime() {
        return this.postTime;
    }

    public void setPostTime(Instant postTime) {
        this.postTime = postTime;
    }

    @Basic(optional = false)
    @Enumerated(EnumType.STRING)
    public ApprovalStatus getStatus() {
        return status;
    }

    public void setStatus(ApprovalStatus status) {
        this.status = status;
    }

    /**
     * True if person who wrote comment wishes to be notified of new comments
     * on the same weblog entry.
     */
    @Basic(optional = false)
    public Boolean getNotify() {
        return this.notify;
    }

    public void setNotify(Boolean notify) {
        this.notify = notify;
    }

    /**
     * Host name or IP of person who wrote comment.
     */
    public String getRemoteHost() {
        return this.remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public String getReferrer() {
        return referrer;
    }

    public void setReferrer(String referrer) {
        this.referrer = StringEscapeUtils.escapeHtml4(referrer);
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    /**
     * True if comment is pending moderator approval.
     */
    @Transient
    public Boolean isPending() {
        return ApprovalStatus.PENDING.equals(getStatus());
    }

    /**
     * Indicates that comment has been approved for display on weblog.
     */
    @Transient
    public Boolean isApproved() {
        return ApprovalStatus.APPROVED.equals(getStatus());
    }

    @Transient
    public boolean isInvalid() {
        return ApprovalStatus.INVALID.equals(getStatus());
    }

    /**
     * Timestamp to be used to formulate comment permlink.
     */
    @Transient
    public String getTimestamp() {
        if (getPostTime() != null) {
            return Long.toString(getPostTime().toEpochMilli());
        }
        return null;
    }

    // fields involved with rendering comments on blogs
    @Transient
    public String getSubmitResponseMessage() {
        return submitResponseMessage;
    }

    public void setSubmitResponseMessage(String submitResponseMessage) {
        this.submitResponseMessage = submitResponseMessage;
    }

    public void initializeFormFields() {
        setName("");
        setEmail("");
        setUrl("");
        setContent("");
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof WeblogEntryComment && Objects.equals(id, ((WeblogEntryComment) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }

    @Override
    public String toString() {
        return String.format("WeblogEntryComment: id=%s, weblog=%s, entry=%s, name=%s, email=%s, postTime=%s", id,
                (weblogEntry != null && weblogEntry.getWeblog() != null) ? weblogEntry.getWeblog().getHandle() : "(no weblog)",
                (weblogEntry != null && weblogEntry.getAnchor() != null) ? weblogEntry.getAnchor() : "(no weblog entry)",
                name, email, postTime);
    }

}
