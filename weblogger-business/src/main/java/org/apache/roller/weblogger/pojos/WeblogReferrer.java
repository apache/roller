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
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * Referrer bean.
 *
 * @author David M Johnson
 */
public class WeblogReferrer implements Serializable {
    
    public static final long serialVersionUID = -1817992900602131316L;
    
    private String id = UUIDGenerator.generateUUID();
    private Weblog website = null;
    private WeblogEntry weblogEntry = null;
    private String dateString = null;
    private String refererUrl = null;
    private String refererPermalink = null;
    private String requestUrl = null;
    private String title = null;
    private String excerpt = null;
    private Boolean visible = null;
    private Boolean duplicate = null;
    private Integer dayHits = null;
    private Integer totalHits = null;
    
    
    public WeblogReferrer() {
    }
    
    public WeblogReferrer(String id,
            org.apache.roller.weblogger.pojos.Weblog website,org.apache.roller.weblogger.pojos.WeblogEntry weblogEntry,
            String dateString, String refererUrl,
            String refererPermalink,
            String requestUrl, String title,
            String excerpt, Boolean visible,
            Boolean duplicate, Integer dayHits,
            Integer totalHits) {
        
        //this.id = id;
        this.website = website;
        this.weblogEntry = weblogEntry;
        this.dateString = dateString;
        this.refererUrl = refererUrl;
        this.refererPermalink = refererPermalink;
        this.requestUrl = requestUrl;
        this.title = title;
        this.excerpt = excerpt;
        this.visible = visible;
        this.duplicate = duplicate;
        this.dayHits = dayHits;
        this.totalHits = totalHits;

        if (this.refererUrl != null && this.refererUrl.length() > 255) {
            this.refererUrl = this.refererUrl.substring(0, 254);
        }
    }
    
    //------------------------------------------------------- Simple properties
    
    /**
     * Unique ID and primary key of this Referer.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * ID of website that this referer refers to.
     *
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public org.apache.roller.weblogger.pojos.Weblog getWebsite() {
        return this.website;
    }
    
    public void setWebsite(org.apache.roller.weblogger.pojos.Weblog website) {
        this.website = website;
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="entryid" cascade="none"
     */
    public org.apache.roller.weblogger.pojos.WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }
    
    /**
     * @param data
     */
    public void setWeblogEntry(org.apache.roller.weblogger.pojos.WeblogEntry data) {
        weblogEntry = data;
    }
    
    /**
     * Date string in YYYYMMDD format.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="datestr" non-null="true" unique="false"
     */
    public String getDateString() {
        return this.dateString;
    }
    
    public void setDateString(String dateString) {
        this.dateString = dateString;
    }
    
    /**
     * URL of the refering page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="refurl" non-null="true" unique="false"
     */
    public String getRefererUrl() {
        return this.refererUrl;
    }
    
    public void setRefererUrl(String refererUrl) {
        this.refererUrl = refererUrl;
        if (this.refererUrl != null && this.refererUrl.length() > 255) {
            this.refererUrl = this.refererUrl.substring(0, 255);
        }
    }
    
    /**
     * Requested URL, the URL linked to by the refering page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="refpermalink" non-null="true" unique="false"
     */
    public String getRefererPermalink() {
        return this.refererPermalink;
    }
    
    public void setRefererPermalink(String refererPermalink) {
        this.refererPermalink = refererPermalink;
    }
    
    /**
     * Requested URL, the URL linked to by the refering page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="requrl" non-null="true" unique="false"
     */
    public String getRequestUrl() {
        return this.requestUrl;
    }
    
    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }
    
    /**
     * The text on the refering page that surrounds the refering link.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="title" non-null="true" unique="false"
     */
    public String getTitle() {
        return this.title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * The text on the refering page that surrounds the refering link.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="excerpt" non-null="true" unique="false"
     */
    public String getExcerpt() {
        return this.excerpt;
    }
    
    public void setExcerpt(String excerpt) {
        this.excerpt = excerpt;
    }
    
    /**
     * Should this referer be displayed?
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="visible" non-null="true" unique="false"
     */
    public Boolean getVisible() {
        return this.visible;
    }
    
    public void setVisible(Boolean visible) {
        this.visible = visible;
    }
    
    /**
     * Is this referer a duplicate?
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="duplicate" non-null="true" unique="false"
     */
    public Boolean getDuplicate() {
        return this.duplicate;
    }
    
    public void setDuplicate(Boolean duplicate) {
        this.duplicate = duplicate;
    }
    
    /**
     * Hits received today from this referer.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="dayhits" non-null="true" unique="false"
     */
    public Integer getDayHits() {
        return this.dayHits;
    }
    
    public void setDayHits(Integer dayHits) {
        this.dayHits = dayHits;
    }
    
    /**
     * Total hits received from this referer.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="totalhits" non-null="true" unique="false"
     */
    public Integer getTotalHits() {
        return this.totalHits;
    }
    
    public void setTotalHits(Integer totalHits) {
        this.totalHits = totalHits;
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getDisplayUrl(int maxWidth, boolean includeHits) {
        StringBuffer sb = new StringBuffer();
        
        String url = StringEscapeUtils.escapeHtml(getUrl());
        String displayUrl = url.trim();
        String restOfUrl = null;
        
        if (displayUrl.startsWith("http://")) {
            displayUrl = displayUrl.substring(7);
        }
        
        if (displayUrl.length() > maxWidth) {
            restOfUrl = "..." +
                    displayUrl.substring(maxWidth, displayUrl.length());
            displayUrl = displayUrl.substring(0, maxWidth) + "...";
        }
        
        if (url.startsWith("http://")) {
            sb.append("<a href=\"");
            sb.append(url);
        }
        
        // add a title with the rest of the url if it exists
        if (restOfUrl != null) {
            sb.append("\" title=\"");
            sb.append(restOfUrl);
        }
        
        if (sb.length() > 0) {
            sb.append("\">");
        }
        
        sb.append(displayUrl);
        
        if (includeHits) {
            sb.append(" (");
            sb.append(getDayHits());
            sb.append(")");
        }
        
        if (url.startsWith("http://")) {
            sb.append("</a>");
        }
        
        return sb.toString();
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getUrl() {
        if (getRefererPermalink() != null) {
            return getRefererPermalink();
        } else {
            return getRefererUrl();
        }
    }
    
    //-------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getDisplayUrl() {
        return getDisplayUrl(50, false);
    }
    
    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.requestUrl);
        buf.append(", ").append(this.refererUrl);
        buf.append(", ").append(this.dayHits);
        buf.append(", ").append(this.totalHits);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogReferrer != true) return false;
        WeblogReferrer o = (WeblogReferrer)other;
        return new EqualsBuilder()
        .append(getRefererUrl(), o.getRefererUrl())
        .append(getWeblogEntry(), o.getWeblogEntry())
        .append(getWebsite(),o.getWebsite())
        .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getRefererUrl())
        .append(getWeblogEntry())
        .append(getWebsite())
        .toHashCode();
    }
    
}
