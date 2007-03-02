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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Weblogentry Comment bean.
 *
 * @ejb:bean name="CommentData"
 * @struts.form include-all="true"
 *
 * @hibernate.class lazy="true" table="roller_comment"
 * @hibernate.cache usage="read-write"
 */
public class CommentData implements Serializable {
    
    public static final long serialVersionUID = -6668122596726478462L;
    
    public static final String APPROVED = "APPROVED";
    public static final String DISAPPROVED = "DISAPPROVED";
    public static final String SPAM = "SPAM";
    public static final String PENDING = "PENDING";
    
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
    
    private WeblogEntryData weblogEntry = null;
    
    
    public CommentData() {}
    
    public CommentData(WeblogEntryData entry, String name, String email,
                       String url, String content, Timestamp postTime, 
                       String status, Boolean notify) {
        this.name = name;
        this.email = email;
        this.url = url;
        this.content = content;
        this.postTime = postTime;
        this.notify = notify;
        
        this.weblogEntry = entry;
    }
    
    public CommentData(CommentData otherData) {
        this.setData(otherData);
    }
    
    /**
     * Database ID of comment
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.id column="id"
     *    generator-class="assigned"  
     */
    public java.lang.String getId() {
        return this.id;
    }
    
    /**
     * Database ID of comment
     * @ejb:persistent-field
     */
    public void setId(java.lang.String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }
    
    /**
     * Weblog entry assocaited with comment
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="entryid" cascade="none" not-null="true"
     */
    public WeblogEntryData getWeblogEntry() {
        return weblogEntry;
    }
    
    /**
     * Weblog entry assocaited with comment
     * @ejb:persistent-field
     */
    public void setWeblogEntry(WeblogEntryData entry) {
        weblogEntry = entry;
    }
    
    /**
     * Name of person who wrote comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public java.lang.String getName() {
        return this.name;
    }
    
    /**
     * Name of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setName(java.lang.String name) {
        this.name = name;
    }
    
    /**
     * Email of person who wrote comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="email" non-null="true" unique="false"
     */
    public java.lang.String getEmail() {
        return this.email;
    }
    
    /**
     * Email of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setEmail(java.lang.String email) {
        this.email = email;
    }
    
    /**
     * URL of person who wrote comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="url" non-null="true" unique="false"
     */
    public java.lang.String getUrl() {
        return this.url;
    }
    
    /**
     * URL of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setUrl(java.lang.String url) {
        this.url = url;
    }
    
    /**
     * Content of comment.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="content" non-null="true" unique="false"
     */
    public java.lang.String getContent() {
        return this.content;
    }
    
    /**
     * Content of comment.
     * @ejb:persistent-field
     */
    public void setContent(java.lang.String content) {
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
    
    /** No-op to please XDoclet */
    public void setSpam(Boolean b) {}
    
    /**
     * True if comment has is pending moderator approval.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public Boolean getPending() {
        return new Boolean(PENDING.equals(this.status));
    }
    
    /** No-op to please XDoclet */
    public void setPending(Boolean b) {}
    
    /**
     * Indicates that comment has been approved for display on weblog.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public Boolean getApproved() {
        return new Boolean(APPROVED.equals(this.status));
    }
    
    /** No-op to please XDoclet */
    public void setApproved(Boolean b) {}
    
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
    
    /** No-op to please XDoclet */
    public void setTimestamp(String timeStamp) {}
    
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
        if (other instanceof CommentData != true) return false;
        CommentData o = (CommentData)other;
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
    
    /**
     * Set bean properties based on other bean.
     */
    public void setData(CommentData otherComment) {
        
        this.id = otherComment.getId();
        this.weblogEntry = otherComment.getWeblogEntry();
        this.name = otherComment.getName();
        this.email = otherComment.getEmail();
        this.url = otherComment.getUrl();
        this.content = otherComment.getContent();
        this.postTime = otherComment.getPostTime();
        this.notify = otherComment.getNotify();
        this.setStatus(otherComment.getStatus());
        this.setReferrer(otherComment.getReferrer());
        this.setUserAgent(otherComment.getUserAgent());
    }
    
}
