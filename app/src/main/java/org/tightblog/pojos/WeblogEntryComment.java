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
package org.tightblog.pojos;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
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

@Entity
@Table(name = "weblog_entry_comment")
public class WeblogEntryComment {

    // approval status states
    // Reason for having both PENDING and DISAPPROVED is that the former triggers
    // email notifications upon subsequent approval while the latter does not.
    public enum ApprovalStatus {
        APPROVED, DISAPPROVED, SPAM, PENDING
    }

    // attributes
    private String id = Utilities.generateUUID();
    private String name = null;
    private String email = null;
    private String url = null;
    private String content = null;
    private Instant postTime = null;
    private ApprovalStatus status = ApprovalStatus.DISAPPROVED;
    private Boolean notify = Boolean.FALSE;
    private String remoteHost = null;
    private String referrer = null;
    private String userAgent = null;

    // associations
    private WeblogEntry weblogEntry = null;

    public WeblogEntryComment() {
    }

    // transient fields involved during comment submittal
    private boolean error = false;
    private boolean preview = false;
    private String submitResponseMessage = null;

    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "entryid", nullable = false)
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry entry) {
        weblogEntry = entry;
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
     * Indicates that weblog owner considers this comment to be spam.
     */
    @Transient
    public Boolean getSpam() {
        return ApprovalStatus.SPAM.equals(getStatus());
    }

    /**
     * True if comment is pending moderator approval.
     */
    @Transient
    public Boolean getPending() {
        return ApprovalStatus.PENDING.equals(getStatus());
    }

    /**
     * Indicates that comment has been approved for display on weblog.
     */
    @Transient
    public Boolean getApproved() {
        return ApprovalStatus.APPROVED.equals(getStatus());
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

    public String toString() {
        return "{ id = " + id + ", weblog = "
                + (weblogEntry != null && weblogEntry.getWeblog() != null ? weblogEntry.getWeblog().getHandle() : "(no weblog)")
                + ", entry = " + (weblogEntry != null && weblogEntry.getAnchor() != null ? weblogEntry.getAnchor() : "(no weblog entry)")
                + ", name = " + name + ", email = " + email + ", postTime = " + postTime + ", notify = " + notify + "}";
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntryComment)) {
            return false;
        }
        WeblogEntryComment o = (WeblogEntryComment) other;
        return new EqualsBuilder()
                .append(getName(), o.getName())
                .append(getPostTime(), o.getPostTime())
                .append(getWeblogEntry(), o.getWeblogEntry())
                .isEquals();
    }

    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(getPostTime())
                .append(getWeblogEntry())
                .toHashCode();
    }

    public void setData(WeblogEntryComment other) {
        this.id = other.getId();
        this.name = other.getName();
        this.email = other.getEmail();
        this.url = other.getUrl();
        this.content = other.getContent();
        this.notify = other.getNotify();
        this.postTime = other.getPostTime();
        this.status = other.getStatus();
        this.remoteHost = other.getRemoteHost();
        this.referrer = other.getReferrer();
        this.userAgent = other.getUserAgent();
        this.weblogEntry = other.getWeblogEntry();
    }

    // fields involved with rendering comments on blogs

    @Transient
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    @Transient
    public String getSubmitResponseMessage() {
        return submitResponseMessage;
    }

    public void setSubmitResponseMessage(String submitResponseMessage) {
        this.submitResponseMessage = submitResponseMessage;
    }

    @Transient
    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    public void initializeFormFields() {
        // Velocity templates output the variable name if it is null.
        setName("");
        setEmail("");
        setUrl("");
        setContent("");
    }
}
