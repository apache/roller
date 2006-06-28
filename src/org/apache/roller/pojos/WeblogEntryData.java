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
 */

package org.apache.roller.pojos;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.apache.commons.lang.StringEscapeUtils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.model.RollerFactory;
import org.apache.roller.model.UserManager;
import org.apache.roller.model.WeblogEntryPlugin;
import org.apache.roller.model.WeblogManager;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.Utilities;

/**
 * Represents a Weblog Entry.
 *
 * @ejb:bean name="WeblogEntryData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="weblogentry"
 * @hibernate.cache usage="read-write"
 */
public class WeblogEntryData extends PersistentObject implements Serializable {
    private static Log mLogger =
            LogFactory.getFactory().getInstance(WeblogEntryData.class);
    
    public static final long serialVersionUID = 2341505386843044125L;
    
    public static final String DRAFT     = "DRAFT";
    public static final String PUBLISHED = "PUBLISHED";
    public static final String PENDING   = "PENDING";
    
    // Simple properies
    private String    id            = null;
    private String    title         = null;
    private String    link          = null;
    private String    summary       = null;
    private String    text          = null;
    private String    contentType   = null;
    private String    contentSrc    = null;
    private String    anchor        = null;
    private Timestamp pubTime       = null;
    private Timestamp updateTime    = null;
    private String    plugins       = null;
    private Boolean   allowComments = Boolean.TRUE;
    private Integer   commentDays   = new Integer(7);
    private Boolean   rightToLeft   = Boolean.FALSE;
    private Boolean   pinnedToMain  = Boolean.FALSE;
    private String    status        = DRAFT;
    
    // Associated objects
    private UserData           creator  = null;
    private WebsiteData        website  = null;
    private WeblogCategoryData category = null;
    
    // Collection of name/value entry attributes
    private Map attMap = new HashMap();
    private Set attSet = new TreeSet();
    
    //----------------------------------------------------------- Construction
    
    public WeblogEntryData() {
    }
    
    public WeblogEntryData(
            String id,
            WeblogCategoryData category,
            WebsiteData website,
            UserData creator,
            String title,
            String link,
            String text,
            String anchor,
            Timestamp pubTime,
            Timestamp updateTime,
            String status) {
        this.id = id;
        this.category = category;
        this.website = website;
        this.creator = creator;
        this.title = title;
        this.link = link;
        this.text = text;
        this.anchor = anchor;
        this.pubTime = pubTime;
        this.updateTime = updateTime;
        this.status = status;
    }
    
    public WeblogEntryData(WeblogEntryData otherData) {
        this.setData(otherData);
    }
    
    //---------------------------------------------------------- Initializaion
    
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(PersistentObject otherData) {
        WeblogEntryData other = (WeblogEntryData)otherData;
        
        this.id = other.getId();
        this.category = other.getCategory();
        this.website = other.getWebsite();
        this.creator = other.getCreator();
        this.title = other.getTitle();
        this.link = other.getLink();
        this.text = other.getText();
        this.anchor = other.getAnchor();
        this.pubTime = other.getPubTime();
        this.updateTime = other.getUpdateTime();
        this.status = other.getStatus();
        this.plugins = other.getPlugins();
        this.allowComments = other.getAllowComments();
        this.commentDays = other.getCommentDays();
        this.rightToLeft = other.getRightToLeft();
        this.pinnedToMain = other.getPinnedToMain();
    }
    
    //------------------------------------------------------ Simple properties
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId() {
        return this.id;
    }
    
    /** @ejb:persistent-field */
    public void setId(String id) {
        this.id = id;
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="categoryid" cascade="none" not-null="true"
     */
    public WeblogCategoryData getCategory() {
        return this.category;
    }
    
    /** @ejb:persistent-field */
    public void setCategory(WeblogCategoryData category) {
        this.category = category;
    }
    
    /**
     * Set weblog category via weblog category ID.
     * @param id Weblog category ID.
     */
    public void setCategoryId(String id) throws RollerException {
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        setCategory(wmgr.getWeblogCategory(id));
    }
    
    /**
     * Return collection of WeblogCategoryData objects of this entry.
     * Added for symetry with PlanetEntryData object.
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogCategoryData"
     */
    public List getCategories() {
        List cats = new ArrayList();
        cats.add(getCategory());
        return cats;
    }
    
    /** No-op method to please XDoclet */
    public void setCategories(List cats) {
        // no-op
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite() {
        return this.website;
    }
    
    /** @ejb:persistent-field */
    public void setWebsite(WebsiteData website) {
        this.website = website;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="userid" cascade="none" not-null="true"
     */
    public UserData getCreator() {
        return this.creator;
    }
    
    /** @ejb:persistent-field */
    public void setCreator(UserData creator) {
        this.creator = creator;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="title" non-null="true" unique="false"
     */
    public String getTitle() {
        return this.title;
    }
    
    /** @ejb:persistent-field */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Get summary for weblog entry (maps to RSS description and Atom summary).
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="summary" non-null="false" unique="false"
     */
    public String getSummary() {
        return summary;
    }
    
    /**
     * Set summary for weblog entry (maps to RSS description and Atom summary).
     * @ejb:persistent-field
     */
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    /**
     * Get content text for weblog entry (maps to RSS content:encoded and Atom content).
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="text" non-null="true" unique="false"
     */
    public String getText() {
        return this.text;
    }
    
    /**
     * Set content text for weblog entry (maps to RSS content:encoded and Atom content).
     * @ejb:persistent-field
     */
    public void setText(String text) {
        this.text = text;
    }
    
    /**
     * Get content type (text, html, xhtml or a MIME content type)
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="content_type" non-null="false" unique="false"
     */
    public String getContentType() {
        return contentType;
    }
    
    /**
     * Set content type (text, html, xhtml or a MIME content type)
     * @ejb:persistent-field
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    
    /**
     * Get URL for out-of-line content.
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="content_src" non-null="false" unique="false"
     */
    public String getContentSrc() {
        return contentSrc;
    }
    
    /**
     * Set URL for out-of-line content.
     * @ejb:persistent-field
     */
    public void setContentSrc(String contentSrc) {
        this.contentSrc = contentSrc;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="anchor" non-null="true" unique="false"
     */
    public String getAnchor() {
        return this.anchor;
    }
    
    /** @ejb:persistent-field */
    public void setAnchor(String anchor) {
        this.anchor = anchor;
    }
    
    //-------------------------------------------------------------------------
    /**
     * Map attributes as set because XDoclet 1.2b4 map support is broken.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.EntryAttributeData"
     * @ejb:persistent-field
     * @hibernate.set lazy="true" order-by="name" inverse="true" cascade="all-delete-orphan"
     * @hibernate.collection-key column="entryid" type="String"
     * @hibernate.collection-one-to-many class="org.apache.roller.pojos.EntryAttributeData"
     */
    public Set getEntryAttributes() {
        return attSet;
    }
    /** @ejb:persistent-field */
    public void setEntryAttributes(Set attSet) {
        this.attSet = attSet;
        
        // copy set to map
        if (attSet != null) {
            this.attSet = attSet;
            this.attMap = new HashMap();
            Iterator iter = this.attSet.iterator();
            while (iter.hasNext()) {
                EntryAttributeData att = (EntryAttributeData)iter.next();
                attMap.put(att.getName(), att);
            }
        } else {
            this.attSet = new TreeSet();
            this.attMap = new HashMap();
        }
    }
    
    
    /**
     * Would be named getEntryAttribute, but that would set off XDoclet
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String findEntryAttribute(String name) {
        EntryAttributeData att = ((EntryAttributeData)attMap.get(name));
        return (att != null) ? att.getValue() : null;
    }
    
    
    public void putEntryAttribute(String name, String value) throws Exception {
        EntryAttributeData att = (EntryAttributeData)attMap.get(name);
        if (att == null) {
            att = new EntryAttributeData();
            att.setEntry(this);
            att.setName(name);
            att.setValue(value);
            attMap.put(name, att);
            attSet.add(att);
        } else {
            att.setValue(value);
        }
    }
    public void removeEntryAttribute(String name) throws RollerException {
        EntryAttributeData att = (EntryAttributeData)attMap.get(name);
        if (att != null) {
            attMap.remove(att);
            attSet.remove(att);
        }
    }
    //-------------------------------------------------------------------------
    
    /**
     * <p>Publish time is the time that an entry is to be (or was) made available
     * for viewing by newsfeed readers and visitors to the Roller site.</p>
     *
     * <p>Roller stores time using the timeZone of the server itself. When
     * times are displayed  in a user's weblog they must be translated
     * to the user's timeZone.</p>
     *
     * <p>NOTE: Times are stored using the SQL TIMESTAMP datatype, which on
     * MySQL has only a one-second resolution.</p>
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="pubtime" non-null="true" unique="false"
     */
    public Timestamp getPubTime() {
        return this.pubTime;
    }
    
    /** @ejb:persistent-field */
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
     *
     * <p>NOTE: Times are stored using the SQL TIMESTAMP datatype, which on
     * MySQL has only a one-second resolution.</p>
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="updatetime" non-null="true" unique="false"
     */
    public Timestamp getUpdateTime() {
        return this.updateTime;
    }
    
    /** @ejb:persistent-field */
    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="status" non-null="true" unique="false"
     */
    public String getStatus() {
        return this.status;
    }
    
    /** @ejb:persistent-field */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * Some weblog entries are about one specific link.
     * @return Returns the link.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="link" non-null="false" unique="false"
     */
    public String getLink() {
        return link;
    }
    
    /**
     * @ejb:persistent-field
     * @param link The link to set.
     */
    public void setLink(String link) {
        this.link = link;
    }
    
    /**
     * Comma-delimited list of this entry's Plugins.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="plugins" non-null="false" unique="false"
     */
    public String getPlugins() {
        return plugins;
    }
    
    /** @ejb:persistent-field */
    public void setPlugins(String string) {
        plugins = string;
    }
    
    
    /**
     * True if comments are allowed on this weblog entry.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="allowcomments" non-null="true" unique="false"
     */
    public Boolean getAllowComments() {
        return allowComments;
    }
    /**
     * True if comments are allowed on this weblog entry.
     * @ejb:persistent-field
     */
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }
    
    /**
     * Number of days after pubTime that comments should be allowed, or 0 for no limit.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="commentdays" non-null="true" unique="false"
     */
    public Integer getCommentDays() {
        return commentDays;
    }
    /**
     * Number of days after pubTime that comments should be allowed, or 0 for no limit.
     * @ejb:persistent-field
     */
    public void setCommentDays(Integer commentDays) {
        this.commentDays = commentDays;
    }
    
    /**
     * True if this entry should be rendered right to left.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="righttoleft" non-null="true" unique="false"
     */
    public Boolean getRightToLeft() {
        return rightToLeft;
    }
    /**
     * True if this entry should be rendered right to left.
     * @ejb:persistent-field
     */
    public void setRightToLeft(Boolean rightToLeft) {
        this.rightToLeft = rightToLeft;
    }
    
    /**
     * True if story should be pinned to the top of the Roller site main blog.
     * @return Returns the pinned.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="pinnedtomain" non-null="true" unique="false"
     */
    public Boolean getPinnedToMain() {
        return pinnedToMain;
    }
    /**
     * True if story should be pinned to the top of the Roller site main blog.
     * @param pinnedToMain The pinned to set.
     *
     * @ejb:persistent-field
     */
    public void setPinnedToMain(Boolean pinnedToMain) {
        this.pinnedToMain = pinnedToMain;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * True if comments are still allowed on this entry considering the
     * allowComments and commentDays fields.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean getCommentsStillAllowed() {
        if(DRAFT.equals(this.status) || PENDING.equals(this.status))
            return false;
        
        boolean ret = false;
        if (getAllowComments() == null || getAllowComments().booleanValue()) {
            if (getCommentDays() == null || getCommentDays().intValue() == 0) {
                ret = true;
            } else {
                Calendar expireCal = Calendar.getInstance(
                        getWebsite().getLocaleInstance());
                expireCal.setTime(getPubTime());
                expireCal.add(Calendar.DATE, getCommentDays().intValue());
                Date expireDay = expireCal.getTime();
                Date today = new Date();
                if (today.before(expireDay)) {
                    ret = true;
                }
            }
        }
        return ret;
    }
    public void setCommentsStillAllowed(boolean ignored) {
        // no-op
    }
    
    
    //------------------------------------------------------------------------
    
    /**
     * Format the publish time of this weblog entry using the specified pattern.
     * See java.text.SimpleDateFormat for more information on this format.
     *
     * @roller.wrapPojoMethod type="simple"
     * @see java.text.SimpleDateFormat
     * @return Publish time formatted according to pattern.
     */
    public String formatPubTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern,
                    this.getWebsite().getLocaleInstance());
            
            return format.format(getPubTime());
        } catch (RuntimeException e) {
            mLogger.error("Unexpected exception", e);
        }
        
        return "ERROR: formatting date";
    }
    
    //------------------------------------------------------------------------
    
    /**
     * Format the update time of this weblog entry using the specified pattern.
     * See java.text.SimpleDateFormat for more information on this format.
     *
     * @roller.wrapPojoMethod type="simple"
     * @see java.text.SimpleDateFormat
     * @return Update time formatted according to pattern.
     */
    public String formatUpdateTime(String pattern) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            
            return format.format(getUpdateTime());
        } catch (RuntimeException e) {
            mLogger.error("Unexpected exception", e);
        }
        
        return "ERROR: formatting date";
    }
    
    //------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.CommentData"
     */
    public List getComments() {
        return getComments(true, true);
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.CommentData"
     */
    public List getComments(boolean ignoreSpam, boolean approvedOnly) {
        List list = new ArrayList();
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            return wmgr.getComments(
                    getWebsite(),
                    this,
                    null,  // search String
                    null,  // startDate
                    null,  // endDate
                    null,  // pending
                    approvedOnly ? Boolean.TRUE : null, // approved
                    ignoreSpam ? Boolean.FALSE : null,  // spam
                    false, // we want chrono order
                    0,    // offset
                    -1);   // no limit
        } catch (RollerException alreadyLogged) {}
        return list;
    }
    
    //------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.RefererData"
     */
    public List getReferers() {
        List referers = null;
        try {
            referers = RollerFactory.getRoller().getRefererManager().getReferersToEntry(getId());
        } catch (RollerException e) {
            mLogger.error("Unexpected exception", e);
        }
        return referers;
    }
    
    //------------------------------------------------------------------------
        
    /**
     * Returns absolute entry permalink.
     * @roller.wrapPojoMethod type="simple"
     */
    public String getPermalink() {
        String absPath = RollerRuntimeConfig.getProperty("site.absoluteurl");
        return absPath + getPermaLink();       
    }
    
    /**
     * Returns entry permalink, relative to Roller context.
     * @deprecated Use getPermalink() instead.
     * @roller.wrapPojoMethod type="simple"
     */
    public String getPermaLink() {
        // TODO: ATLAS reconcile entry.getPermaLink() with new URLs
        String lAnchor = this.getAnchor();        
        try {
            lAnchor = URLEncoder.encode(anchor, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // go with the "no encoding" version
        }        
        WebsiteData website = this.getWebsite();
        return "/" + getWebsite().getHandle() + "/entry/" + lAnchor;        
    }
    
    /**
     * Get relative URL to comments page.
     * @roller.wrapPojoMethod type="simple"
     * @deprecated Use commentLink() instead
     */
    public String getCommentsLink() {
        return getPermaLink() + "#comments";
    }
    /** 
     * to please XDoclet 
     * @deprecated Use commentLink() instead
     */
    public void setCommentsLink(String ignored) {}
    
    /**
     * Get absolute URL of comment page.
     * @roller.wrapPojoMethod type="simple"
     */
    public String getCommentLink() {
        String absPath = RollerRuntimeConfig.getProperty("site.absoluteurl");
        return absPath + getCommentsLink(); 
    }
    /** to please XDoclet */
    public void setCommentLink(String ignored) {}
    
    
    /**
     * Return the Title of this post, or the first 255 characters of the
     * entry's text.
     *
     * @roller.wrapPojoMethod type="simple"
     * @return String
     */
    public String getDisplayTitle() {
        if ( getTitle()==null || getTitle().trim().equals("") ) {
            return StringUtils.left(Utilities.removeHTML(text),255);
        }
        return Utilities.removeHTML(getTitle());
    }
    
    //------------------------------------------------------------------------
    
    public String toString() {
        StringBuffer str = new StringBuffer("{");
        
        str.append("id=" + id + " " +
                "category=" + category + " " +
                "title=" + title + " " +
                "text=" + text + " " +
                "anchor=" + anchor + " " +
                "pubTime=" + pubTime + " " +
                "updateTime=" + updateTime + " " +
                "status=" + status + " " +
                "plugins=" + plugins);
        str.append('}');
        
        return (str.toString());
    }
    
    //------------------------------------------------------------------------
    
    public boolean equals(Object pOther) {
        if (pOther instanceof WeblogEntryData) {
            WeblogEntryData lTest = (WeblogEntryData) pOther;
            boolean lEquals = true;
            
            if (this.id == null) {
                lEquals = lEquals && (lTest.getId() == null);
            } else {
                lEquals = lEquals && this.id.equals(lTest.getId());
            }
            
            if (this.category == null) {
                lEquals = lEquals && (lTest.getCategory() == null);
            } else {
                lEquals = lEquals && this.category.equals(lTest.getCategory());
            }
            
            if (this.website == null) {
                lEquals = lEquals && (lTest.getWebsite() == null);
            } else {
                lEquals = lEquals && this.website.equals(lTest.getWebsite());
            }
            
            if (this.title == null) {
                lEquals = lEquals && (lTest.getTitle() == null);
            } else {
                lEquals = lEquals && this.title.equals(lTest.getTitle());
            }
            
            if (this.text == null) {
                lEquals = lEquals && (lTest.getText() == null);
            } else {
                lEquals = lEquals && this.text.equals(lTest.getText());
            }
            
            if (this.anchor == null) {
                lEquals = lEquals && (lTest.getAnchor() == null);
            } else {
                lEquals = lEquals && this.anchor.equals(lTest.getAnchor());
            }
            
            if (this.pubTime == null) {
                lEquals = lEquals && (lTest.getPubTime() == null);
            } else {
                lEquals = lEquals && this.pubTime.equals(lTest.getPubTime());
            }
            
            if (this.updateTime == null) {
                lEquals = lEquals && (lTest.getUpdateTime() == null);
            } else {
                lEquals = lEquals &&
                        this.updateTime.equals(lTest.getUpdateTime());
            }
            
            if (this.status == null) {
                lEquals = lEquals && (lTest.getStatus() == null);
            } else {
                lEquals = lEquals &&
                        this.status.equals(lTest.getStatus());
            }
            
            if (this.plugins == null) {
                lEquals = lEquals && (lTest.getPlugins() == null);
            } else {
                lEquals = lEquals &&
                        this.plugins.equals(lTest.getPlugins());
            }
            
            
            return lEquals;
        } else {
            return false;
        }
    }
    
    //------------------------------------------------------------------------
    
    public int hashCode() {
        int result = 17;
        result = (37 * result) +
                ((this.id != null) ? this.id.hashCode() : 0);
        result = (37 * result) +
                ((this.category != null) ? this.category.hashCode() : 0);
        result = (37 * result) +
                ((this.website != null) ? this.website.hashCode() : 0);
        result = (37 * result) +
                ((this.title != null) ? this.title.hashCode() : 0);
        result = (37 * result) +
                ((this.text != null) ? this.text.hashCode() : 0);
        result = (37 * result) +
                ((this.anchor != null) ? this.anchor.hashCode() : 0);
        result = (37 * result) +
                ((this.pubTime != null) ? this.pubTime.hashCode() : 0);
        result = (37 * result) +
                ((this.updateTime != null) ? this.updateTime.hashCode() : 0);
        result = (37 * result) +
                ((this.status != null) ? this.status.hashCode() : 0);
        result = (37 * result) +
                ((this.plugins != null) ? this.plugins.hashCode() : 0);
        
        return result;
    }
    
    /**
     * Return RSS 09x style description (escaped HTML version of entry text)
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getRss09xDescription() {
        return getRss09xDescription(-1);
    }
    
    /**
     * Return RSS 09x style description (escaped HTML version of entry text)
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getRss09xDescription(int maxLength) {
        String ret = StringEscapeUtils.escapeHtml(text);
        if (maxLength != -1 && ret.length() > maxLength) {
            ret = ret.substring(0,maxLength-3)+"...";
        }
        return ret;
    }
    
    /** Create anchor for weblog entry, based on title or text */
    protected String createAnchor() throws RollerException {
        return RollerFactory.getRoller().getWeblogManager().createAnchor(this);
    }
    
    /** Create anchor for weblog entry, based on title or text */
    public String createAnchorBase() {
        // Use title or text for base anchor
        String base = getTitle();
        if (base == null || base.trim().equals("")) {
            base = getText();
        }
        if (base != null && !base.trim().equals("")) {
            base = Utilities.replaceNonAlphanumeric(base, ' ');
            
            // Use only the first 4 words
            StringTokenizer toker = new StringTokenizer(base);
            String tmp = null;
            int count = 0;
            while (toker.hasMoreTokens() && count < 5) {
                String s = toker.nextToken();
                s = s.toLowerCase();
                tmp = (tmp == null) ? s : tmp + "_" + s;
                count++;
            }
            base = tmp;
        }
        // No title or text, so instead we will use the items date
        // in YYYYMMDD format as the base anchor
        else {
            base = DateUtil.format8chars(getPubTime());
        }
        
        return base;
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
     *
     * @roller.wrapPojoMethod type="simple"
     * @return
     */
    public List getPluginsList() {
        if (plugins != null) {
            return Arrays.asList( StringUtils.split(plugins, ",") );
        }
        return new ArrayList();
    }
    
    /**
     * Set creator by user id (for use in form's copyTo method)
     * @param creatorId
     */
    public void setCreatorId(String creatorId) throws RollerException {
        UserManager umgr = RollerFactory.getRoller().getUserManager();
        setCreator(umgr.getUser(creatorId));
    }
    
    /** Convenience method for checking status */
    public boolean isDraft() {
        return status.equals(DRAFT);
    }
    /** no-op: needed only to satisfy XDoclet, use setStatus() instead */
    public void setDraft(boolean value) {
    }
    
    /** Convenience method for checking status */
    public boolean isPending() {
        return status.equals(PENDING);
    }
    /** no-op: needed only to satisfy XDoclet, use setStatus() instead */
    public void setPending(boolean value) {
    }
    
    /** Convenience method for checking status */
    public boolean isPublished() {
        return status.equals(PUBLISHED);
    }
    /** no-op: needed only to satisfy XDoclet, use setStatus() instead */
    public void setPublished(boolean value) {
    }
        
    /**
     * Get entry text, transformed by plugins enabled for entry.
     */
    public String getTransformedText() {
        return render(text);
    }
    /**
     * No-op to please XDoclet.
     */
    public void setTransformedText(String t) {
        // no-op
    }
    
    /**
     * Get entry summary, transformed by plugins enabled for entry.
     */
    public String getTransformedSummary() {
        return render(summary);
    }
    /**
     * No-op to please XDoclet.
     */
    public void setTransformedSummary(String t) {
        // no-op
    }    
    
    /**
     * Determine if the specified user has permissions to edit this entry.
     */
    public boolean hasWritePermissions(UserData user) throws RollerException {
        
        // global admins can hack whatever they want
        if(user.hasRole("admin")) {
            return true;
        }
        
        boolean author = getWebsite().hasUserPermissions(
                user, (short)(PermissionsData.AUTHOR));
        boolean limited = getWebsite().hasUserPermissions(
                user, (short)(PermissionsData.LIMITED));
        
        if (author || (limited && isDraft()) || (limited && isPending())) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Transform string based on plugins enabled for this weblog entry.
     */
    private String render(String str) {
        String ret = str;
        mLogger.debug("Applying page plugins to string");
        Map plugins = this.website.getInitializedPlugins();
        if (str != null && plugins != null) {
            List entryPlugins = getPluginsList();
            
            // if no Entry plugins, don't bother looping.
            if (entryPlugins != null && !entryPlugins.isEmpty()) {
                
                // now loop over mPagePlugins, matching
                // against Entry plugins (by name):
                // where a match is found render Plugin.
                Iterator iter = plugins.keySet().iterator();
                while (iter.hasNext()) {
                    String key = (String)iter.next();
                    if (entryPlugins.contains(key)) {
                        WeblogEntryPlugin pagePlugin = (WeblogEntryPlugin)plugins.get(key);
                        try {
                            ret = pagePlugin.render(this, ret);
                        } catch (Throwable t) {
                            mLogger.error("ERROR from plugin: " + pagePlugin.getName(), t);
                        }
                    }
                }
            }
        }        
        return ret;
    } 
}
