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
     * Weblog entry assocaited with comment
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="entryid" cascade="none" not-null="true"
     */
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }
    
    /**
     * Weblog entry assocaited with comment
     * @ejb:persistent-field
     */
    public void setWeblogEntry(WeblogEntry entry) {
        weblogEntry = entry;
    }
    
    /**
     * Name of person who wrote comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Name of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Email of person who wrote comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="email" non-null="true" unique="false"
     */
    public String getEmail() {
        return this.email;
    }
    
    /**
     * Email of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * URL of person who wrote comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="url" non-null="true" unique="false"
     */
    public String getUrl() {
        return this.url;
    }
    
    /**
     * URL of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setUrl(String url) {
        this.url = url;
    }
    
    /**
     * Content of comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="content" non-null="true" unique="false"
     */
    public String getContent() {
        return this.content;
    }
    
    /**
     * Content of comment.
     * @ejb:persistent-field
     */
    public void setContent(String content) {
        this.content = content;
    }
    
    /**
     * Time that comment was posted.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="posttime" non-null="true" unique="false"
     */
    public java.sql.Timestamp getPostTime() {
        return this.postTime;
    }
    
    /**
     * Time that comment was posted.
     * @ejb:persistent-field
     */
    public void setPostTime(java.sql.Timestamp postTime) {
        this.postTime = postTime;
    }
    
    /**
     * Status of the comment, i.e. APPROVED, SPAM, PENDING, etc.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="status" non-null="true" unique="false"
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
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="notify" non-null="false" unique="false"
     */
    public Boolean getNotify() {
        return this.notify;
    }
    
    /**
     * True if person who wrote comment wishes to be notified of new comments
     * on the same weblog entry.
     * @ejb:persistent-field
     */
    public void setNotify(Boolean notify) {
        this.notify = notify;
    }
    
    /**
     * Host name or IP of person who wrote comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="remotehost" non-null="true" unique="false"
     */
    public String getRemoteHost() {
        return this.remoteHost;
    }
    
    /**
     * Host name or IP of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }
    
    /**
     * HTTP referrer from comment post request
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="referrer" non-null="true" unique="false"
     */
    public String getReferrer() {
        return referrer;
    }
    
    /**
     * HTTP referrer from comment post request
     * @ejb:persistent-field
     */
    public void setReferrer(String referrer) {
        this.referrer = referrer;
    }
    
    /**
     * HTTP user-agent from comment post request
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="useragent" non-null="true" unique="false"
     */
    public String getUserAgent() {
        return userAgent;
    }
    
    /**
     * HTTP user-agent from comment post request
     * @ejb:persistent-field
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    /**
     * Indicates that weblog owner considers this comment to be spam.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public Boolean getSpam() {
        return new Boolean(SPAM.equals(this.status));
    }
    
    /**
     * True if comment has is pending moderator approval.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public Boolean getPending() {
        return new Boolean(PENDING.equals(this.status));
    }
    
    /**
     * Indicates that comment has been approved for display on weblog.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public Boolean getApproved() {
        return new Boolean(APPROVED.equals(this.status));
    }
    
    /**
     * Timestamp to be used to formulate comment permlink.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getTimestamp() {
        if (postTime != null) {
            return Long.toString(postTime.getTime());
        }
        return null;
    }
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.email);
        buf.append(", ").append(this.postTime);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogEntryComment != true) return false;
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
