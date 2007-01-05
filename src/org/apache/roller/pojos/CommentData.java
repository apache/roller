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

import java.sql.Timestamp;
import org.apache.roller.util.PojoUtil;

/**
 * Weblogentry Comment bean.
 * @author Lance Lavandowska
 *
 * @ejb:bean name="CommentData"
 * @struts.form include-all="true"
 *
 * @hibernate.class lazy="true" table="roller_comment"
 * @hibernate.cache usage="read-write"
 */
public class CommentData extends org.apache.roller.pojos.PersistentObject
        implements java.io.Serializable {
    public static final long serialVersionUID = -6668122596726478462L;
    
    private String    id = null;
    
    private String    name = null;
    private String    email = null;
    private String    url = null;
    private String    content = null;
    private Timestamp postTime = null;
    
    private Boolean   spam = Boolean.FALSE;
    private Boolean   notify = Boolean.FALSE;
    private Boolean   pending = Boolean.TRUE;
    private Boolean   approved = Boolean.FALSE;        
    
    private String    remoteHost = null;
    private String    referrer = null;
    private String    userAgent = null;
         
    private WeblogEntryData weblogEntry = null;
    
    
    public CommentData() {
        spam = Boolean.FALSE;
    }
    
    public CommentData(java.lang.String id, WeblogEntryData entry,
            java.lang.String name, java.lang.String email,
            java.lang.String url, java.lang.String content,
            java.sql.Timestamp postTime,
            Boolean spam, Boolean notify,
            Boolean pending, Boolean approved) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.url = url;
        this.content = content;
        this.postTime = postTime;
        this.spam = spam;
        this.notify = notify;
        this.pending = pending;
        this.setApproved(approved);
        
        weblogEntry = entry;
    }
    
    public CommentData(CommentData otherData) {
        this.setData(otherData);
    }
    
    /**
     * Database ID of comment
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.id column="id"
     *    generator-class="uuid.hex" unsaved-value="null"
     */
    public java.lang.String getId() {
        return this.id;
    }
    
    /** 
     * Database ID of comment
     * @ejb:persistent-field 
     */
    public void setId(java.lang.String id) {
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
     * Indicates that weblog owner considers this comment to be spam.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="spam" non-null="false" unique="false"
     */
    public Boolean getSpam() {
        return this.spam;
    }
    
    /** 
     * Indicates that weblog owner considers this comment to be spam.
     * @ejb:persistent-field 
     */
    public void setSpam(Boolean spam) {
        this.spam = spam;
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
     * True if comment has is pending approval.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="pending" non-null="false" unique="false"
     */
    public Boolean getPending() {
        return this.pending;
    }
    
    /** 
     * True if comment has is pending approval.
     * @ejb:persistent-field 
     */
    public void setPending(Boolean pending) {
        this.pending = pending;
    }
    
    /**
     * Indicates that comment has been approved for display on weblog.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="approved" non-null="false" unique="false"
     */
    public Boolean getApproved() {
        return this.approved;
    }
    
    /**
     * Indicates that comment has been approved for display on weblog.
     * @ejb:persistent-field 
     */
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    
    /**
     * Host name or IP of person who wrote comment.
     * @ejb:persistent-field
     */
    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
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
     * Timestamp to be used to formulate comment permlink.
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

    public String toString() {
        StringBuffer str = new StringBuffer("{");
        
        str.append("id=" + id + " " +
                "name=" + name + " " +
                "email=" + email + " " +
                "url=" + url + " " +
                "content=" + content + " " +
                "postTime=" + postTime + " " +
                "spam=" + spam +
                "notify=" + notify +
                "pending" + pending +
                "approved" + getApproved() + 
                "referrer" + getReferrer() + 
                "userAgent" + getUserAgent());
        str.append('}');
        
        return (str.toString());
    }
    
    public boolean equals(Object pOther) {
        if (pOther instanceof CommentData) {
            CommentData lTest = (CommentData) pOther;
            boolean lEquals = true;
            
            lEquals = PojoUtil.equals(lEquals, this.id, lTest.getId());
            lEquals = PojoUtil.equals(lEquals, this.weblogEntry, lTest.getWeblogEntry());
            lEquals = PojoUtil.equals(lEquals, this.name, lTest.getName());
            lEquals = PojoUtil.equals(lEquals, this.email, lTest.getEmail());
            lEquals = PojoUtil.equals(lEquals, this.url, lTest.getUrl());
            lEquals = PojoUtil.equals(lEquals, this.content, lTest.getContent());
            lEquals = PojoUtil.equals(lEquals, this.postTime, lTest.getPostTime());
            lEquals = PojoUtil.equals(lEquals, this.spam, lTest.getSpam());
            lEquals = PojoUtil.equals(lEquals, this.notify, lTest.getNotify());
            lEquals = PojoUtil.equals(lEquals, this.pending, lTest.getPending());
            lEquals = PojoUtil.equals(lEquals, this.getApproved(), lTest.getApproved());
            lEquals = PojoUtil.equals(lEquals, this.getReferrer(), lTest.getApproved());
            lEquals = PojoUtil.equals(lEquals, this.getUserAgent(), lTest.getUserAgent());
            return lEquals;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        int result = 17;
        result = PojoUtil.addHashCode(result, this.id);
        result = PojoUtil.addHashCode(result, this.weblogEntry);
        result = PojoUtil.addHashCode(result, this.name);
        result = PojoUtil.addHashCode(result, this.email);
        result = PojoUtil.addHashCode(result, this.url);
        result = PojoUtil.addHashCode(result, this.content);
        result = PojoUtil.addHashCode(result, this.postTime);
        result = PojoUtil.addHashCode(result, this.spam);
        result = PojoUtil.addHashCode(result, this.notify);
        result = PojoUtil.addHashCode(result, this.pending);
        result = PojoUtil.addHashCode(result, this.getApproved());               
        result = PojoUtil.addHashCode(result, this.getReferrer());        
        result = PojoUtil.addHashCode(result, this.getUserAgent());        
        return result;
    }
    
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.apache.roller.pojos.PersistentObject otherData) {
        CommentData otherComment = (CommentData) otherData;
        
        this.id = otherComment.getId();
        this.weblogEntry = otherComment.getWeblogEntry();
        this.name = otherComment.getName();
        this.email = otherComment.getEmail();
        this.url = otherComment.getUrl();
        this.content = otherComment.getContent();
        this.postTime = otherComment.getPostTime();
        this.spam = otherComment.getSpam();
        this.notify = otherComment.getNotify();
        this.pending = otherComment.getPending();
        this.setApproved(otherComment.getApproved());
        this.setReferrer(otherComment.getReferrer());
        this.setUserAgent(otherComment.getUserAgent());
    }
    
}

