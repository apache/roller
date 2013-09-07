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
import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * WeblogEntry comment bean.
 */
public class WeblogEntryComment implements Serializable {
    
    public static final long serialVersionUID = -6668122596726478462L;
    
    // status options
    public static final String APPROVED = "APPROVED";
    public static final String DISAPPROVED = "DISAPPROVED";
    public static final String SPAM = "SPAM";
    public static final String PENDING = "PENDING";
    
    // attributes
    private String    id = UUIDGenerator.generateUUID();
    private String    name = null;
    private String    email = null;
    private String    url = null;
    private String    content = null;
    private Timestamp postTime = null;
    private String    status = APPROVED;
    private Boolean   notify = Boolean.FALSE;
    private String    remoteHost = null;
    private String    referrer = null;
    private String    userAgent = null;
    private String    plugins = null;
    private String    contentType = "text/plain";

    
    // associations
    private WeblogEntry weblogEntry = null;
    
    
    public WeblogEntryComment() {}
    
    
    /**
     * Database ID of comment 
     */
    public String getId() {
        return this.id;
    }
    
    /**
     * Database ID of comment
     */
    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * Weblog entry associated with comment.
     */
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }
    
    /**
     * Weblog entry assocaited with comment
     */
    public void setWeblogEntry(WeblogEntry entry) {
        weblogEntry = entry;
    }
    
    
    /**
     * Name of person who wrote comment.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Name of person who wrote comment.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    
    /**
     * Email of person who wrote comment.
     */
    public String getEmail() {
        return this.email;
    }
    
    /**
     * Email of person who wrote comment.
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    
    /**
     * URL of person who wrote comment.
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * URL of person who wrote comment.
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    
    /**
     * Content of comment.
     */
    public String getContent() {
        return this.content;
    }
    
    /**
     * Content of comment.
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    
    /**
     * Time that comment was posted.
     */
    public Timestamp getPostTime() {
        return this.postTime;
    }
    
    /**
     * Time that comment was posted.
     */
    public void setPostTime(Timestamp postTime) {
        this.postTime = postTime;
    }
    
    
    /**
     * Status of the comment, i.e. APPROVED, SPAM, PENDING, etc.
     */
    public String getStatus() {
        return status;
    }

    /**
     * Status of the comment, i.e. APPROVED, SPAM, PENDING, etc.
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    
    /**
     * True if person who wrote comment wishes to be notified of new comments
     * on the same weblog entry.
     */
    public Boolean getNotify() {
        return this.notify;
    }
    
    /**
     * True if person who wrote comment wishes to be notified of new comments
     * on the same weblog entry.
     */
    public void setNotify(Boolean notify) {
        this.notify = notify;
    }
    
    
    /**
     * Host name or IP of person who wrote comment.
     */
    public String getRemoteHost() {
        return this.remoteHost;
    }
    
    /**
     * Host name or IP of person who wrote comment.
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    
    
    /**
     * HTTP referrer from comment post request
     */
    public String getReferrer() {
        return referrer;
    }
    
    /**
     * HTTP referrer from comment post request
     */
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    
    
    /**
     * HTTP user-agent from comment post request
     */
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * HTTP user-agent from comment post request
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    
    /**
     * Comma separated list of comment plugins to apply.
     */
    public String getPlugins() {
        return plugins;
    }
    
    /**
     * Comma separated list of comment plugins to apply.
     */
    public void setPlugins(String plugins) {
        this.plugins = plugins;
    }
    
    
    /**
     * The content-type of the comment.
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * The content-type of the comment.
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    
    /**
     * Indicates that weblog owner considers this comment to be spam.
     */
    public Boolean getSpam() {
        return SPAM.equals(getStatus());
    }
    
    
    /**
     * True if comment has is pending moderator approval.
     */
    public Boolean getPending() {
        return PENDING.equals(getStatus());
    }
    
    
    /**
     * Indicates that comment has been approved for display on weblog.
     */
    public Boolean getApproved() {
        return APPROVED.equals(getStatus());
    }
    
    
    /**
     * Timestamp to be used to formulate comment permlink.
     */
    public String getTimestamp() {
        if (getPostTime() != null) {
            return Long.toString(getPostTime().getTime());
        }
        return null;
    }
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getName());
        buf.append(", ").append(getEmail());
        buf.append(", ").append(getPostTime());
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntryComment)) {
            return false;
        }
        WeblogEntryComment o = (WeblogEntryComment)other;
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
    
}
