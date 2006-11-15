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
/*
 * Generated file - Do not edit!
 */
package org.apache.roller.pojos;

import org.apache.commons.lang.StringEscapeUtils;


/**
 * Referer bean.
 * @author David M Johnson
 *
 * @ejb:bean name="RefererData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true" table="referer"
 * @hibernate.cache usage="read-write"
 */
public class RefererData extends PersistentObject
    implements java.io.Serializable
{
    static final long serialVersionUID = -1817992900602131316L;
    private java.lang.String id = null;
    private org.apache.roller.pojos.WebsiteData website = null;
    private org.apache.roller.pojos.WeblogEntryData weblogEntry = null;
    private java.lang.String dateString = null;
    private java.lang.String refererUrl = null;
    private java.lang.String refererPermalink = null;
    private java.lang.String requestUrl = null;
    private java.lang.String title = null;
    private java.lang.String excerpt = null;
    private java.lang.Boolean visible = null;
    private java.lang.Boolean duplicate = null;
    private java.lang.Integer dayHits = null;
    private java.lang.Integer totalHits = null;

    public RefererData()
    {
    }

    public RefererData(java.lang.String id, 
                       org.apache.roller.pojos.WebsiteData website, 
                       org.apache.roller.pojos.WeblogEntryData weblogEntry, 
                       java.lang.String dateString, java.lang.String refererUrl, 
                       java.lang.String refererPermalink, 
                       java.lang.String requestUrl, java.lang.String title, 
                       java.lang.String excerpt, java.lang.Boolean visible, 
                       java.lang.Boolean duplicate, java.lang.Integer dayHits, 
                       java.lang.Integer totalHits)
    {
        this.id = id;
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
    }

    public RefererData(RefererData otherData)
    {
        setData(otherData);
    }

    //------------------------------------------------------- Simple properties

    /** 
     * Unique ID and primary key of this Referer.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.id column="id"
     *  generator-class="uuid.hex" unsaved-value="null"
     */
    public java.lang.String getId()
    {
        return this.id;
    }

    public void setId(java.lang.String id)
    {
        this.id = id;
    }

    /** 
     * ID of website that this referer refers to.
     *
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public org.apache.roller.pojos.WebsiteData getWebsite()
    {
        return this.website;
    }

    public void setWebsite(org.apache.roller.pojos.WebsiteData website)
    {
        this.website = website;
    }

    /**
     * @roller.wrapPojoMethod type="pojo"
     * @hibernate.many-to-one column="entryid" cascade="none"
     */
    public org.apache.roller.pojos.WeblogEntryData getWeblogEntry()
    {
        return weblogEntry;
    }

    /**
     * @param data
     */
    public void setWeblogEntry(org.apache.roller.pojos.WeblogEntryData data)
    {
        weblogEntry = data;
    }

    /** 
     * Date string in YYYYMMDD format.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="datestr" non-null="true" unique="false"
     */
    public java.lang.String getDateString()
    {
        return this.dateString;
    }

    public void setDateString(java.lang.String dateString)
    {
        this.dateString = dateString;
    }

    /** 
     * URL of the refering page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="refurl" non-null="true" unique="false"
     */
    public java.lang.String getRefererUrl()
    {
        return this.refererUrl;
    }

    public void setRefererUrl(java.lang.String refererUrl)
    {
        this.refererUrl = refererUrl;
    }

    /** 
     * Requested URL, the URL linked to by the refering page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="refpermalink" non-null="true" unique="false"
     */
    public java.lang.String getRefererPermalink()
    {
        return this.refererPermalink;
    }

    public void setRefererPermalink(java.lang.String refererPermalink)
    {
        this.refererPermalink = refererPermalink;
    }

    /** 
     * Requested URL, the URL linked to by the refering page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="requrl" non-null="true" unique="false"
     */
    public java.lang.String getRequestUrl()
    {
        return this.requestUrl;
    }

    public void setRequestUrl(java.lang.String requestUrl)
    {
        this.requestUrl = requestUrl;
    }

    /** 
     * The text on the refering page that surrounds the refering link. 
     *
     * @roller.wrapPojoMethod type="simple" 
     * @hibernate.property column="title" non-null="true" unique="false"
     */
    public java.lang.String getTitle()
    {
        return this.title;
    }

    public void setTitle(java.lang.String title)
    {
        this.title = title;
    }

    /** 
     * The text on the refering page that surrounds the refering link.  
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="excerpt" non-null="true" unique="false"
     */
    public java.lang.String getExcerpt()
    {
        return this.excerpt;
    }

    public void setExcerpt(java.lang.String excerpt)
    {
        this.excerpt = excerpt;
    }

    /** 
     * Should this referer be displayed?
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="visible" non-null="true" unique="false"
     */
    public java.lang.Boolean getVisible()
    {
        return this.visible;
    }

    public void setVisible(java.lang.Boolean visible)
    {
        this.visible = visible;
    }

    /** 
     * Is this referer a duplicate?
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="duplicate" non-null="true" unique="false"
     */
    public java.lang.Boolean getDuplicate()
    {
        return this.duplicate;
    }

    public void setDuplicate(java.lang.Boolean duplicate)
    {
        this.duplicate = duplicate;
    }

    /** 
     * Hits received today from this referer.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="dayhits" non-null="true" unique="false"
     */
    public java.lang.Integer getDayHits()
    {
        return this.dayHits;
    }

    public void setDayHits(java.lang.Integer dayHits)
    {
        this.dayHits = dayHits;
    }

    /** 
     * Total hits received from this referer.
     *
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="totalhits" non-null="true" unique="false"
     */
    public java.lang.Integer getTotalHits()
    {
        return this.totalHits;
    }

    public void setTotalHits(java.lang.Integer totalHits)
    {
        this.totalHits = totalHits;
    }

    //-------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getDisplayUrl(int maxWidth, boolean includeHits)
    {
        StringBuffer sb = new StringBuffer();

        String url = StringEscapeUtils.escapeHtml(getUrl());
        String displayUrl = url.trim();
        String restOfUrl = null;

        if (displayUrl.startsWith("http://"))
        {
            displayUrl = displayUrl.substring(7);
        }

        if (displayUrl.length() > maxWidth)
        {
            restOfUrl = "..." + 
                        displayUrl.substring(maxWidth, displayUrl.length());
            displayUrl = displayUrl.substring(0, maxWidth) + "...";
        }

        if (url.startsWith("http://"))
        {
            sb.append("<a href=\"");
            sb.append(url);
        }

        // add a title with the rest of the url if it exists
        if (restOfUrl != null)
        {
            sb.append("\" title=\"");
            sb.append(restOfUrl);
        }

        if (sb.length() > 0)
        {
            sb.append("\">");
        }

        sb.append(displayUrl);

        if (includeHits)
        {
            sb.append(" (");
            sb.append(getDayHits());
            sb.append(")");
        }

        if (url.startsWith("http://"))
        {
            sb.append("</a>");
        }

        return sb.toString();
    }

    //-------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getUrl()
    {
        if (getRefererPermalink() != null)
        {
            return getRefererPermalink();
        }
        else
        {
            return getRefererUrl();
        }
    }

    //-------------------------------------------------------------------------
    
    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public String getDisplayUrl()
    {
        return getDisplayUrl(50, false);
    }

    //-------------------------------------------------------------------------
    public String toString()
    {
        StringBuffer str = new StringBuffer("{");

        str.append("id=" + id + " " + "website=" + website + " " + 
                   "dateString=" + 
                   dateString + " " + "refererUrl=" + refererUrl + " " + 
                   "refererPermalink=" + refererPermalink + " " + 
                   "requestUrl=" + requestUrl + " " + "title=" + title + " " + 
                   "excerpt=" + excerpt + " " + "visible=" + visible + " " + 
                   "duplicate=" + duplicate + " " + "dayHits=" + dayHits + 
                   " " + "totalHits=" + totalHits);
        str.append('}');

        return (str.toString());
    }

    public boolean equals(Object pOther)
    {
        if (pOther instanceof RefererData)
        {
            RefererData lTest = (RefererData) pOther;
            boolean lEquals = true;

            if (this.id == null)
            {
                lEquals = lEquals && (lTest.getId() == null);
            }
            else
            {
                lEquals = lEquals && this.id.equals(lTest.getId());
            }

            if (this.website == null)
            {
                lEquals = lEquals && (lTest.getWebsite() == null);
            }
            else
            {
                lEquals = lEquals && this.website.equals(lTest.getWebsite());
            }

            if (this.weblogEntry == null)
            {
                lEquals = lEquals && (lTest.getWeblogEntry() == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.weblogEntry.equals(lTest.getWeblogEntry());
            }

            if (this.dateString == null)
            {
                lEquals = lEquals && (lTest.getDateString() == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.dateString.equals(lTest.getDateString());
            }

            if (this.refererUrl == null)
            {
                lEquals = lEquals && (lTest.getRefererUrl() == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.refererUrl.equals(lTest.getRefererUrl());
            }

            if (this.refererPermalink == null)
            {
                lEquals = lEquals && (lTest.getRefererPermalink() == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.refererPermalink.equals(lTest.getRefererPermalink());
            }

            if (this.requestUrl == null)
            {
                lEquals = lEquals && (lTest.getRequestUrl() == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.requestUrl.equals(lTest.getRequestUrl());
            }

            if (this.title == null)
            {
                lEquals = lEquals && (lTest.getTitle() == null);
            }
            else
            {
                lEquals = lEquals && this.title.equals(lTest.getTitle());
            }

            if (this.excerpt == null)
            {
                lEquals = lEquals && (lTest.getExcerpt() == null);
            }
            else
            {
                lEquals = lEquals && this.excerpt.equals(lTest.getExcerpt());
            }

            if (this.visible == null)
            {
                lEquals = lEquals && (lTest.getVisible() == null);
            }
            else
            {
                lEquals = lEquals && this.visible.equals(lTest.getVisible());
            }

            if (this.duplicate == null)
            {
                lEquals = lEquals && (lTest.getDuplicate() == null);
            }
            else
            {
                lEquals = lEquals && this.duplicate.equals(lTest.getDuplicate());
            }

            if (this.dayHits == null)
            {
                lEquals = lEquals && (lTest.getDayHits() == null);
            }
            else
            {
                lEquals = lEquals && this.dayHits.equals(lTest.getDayHits());
            }

            if (this.totalHits == null)
            {
                lEquals = lEquals && (lTest.getTotalHits() == null);
            }
            else
            {
                lEquals = lEquals && this.totalHits.equals(lTest.getTotalHits());
            }

            return lEquals;
        }
        else
        {
            return false;
        }
    }

    public int hashCode()
    {
        int result = 17;
        result = (37 * result) + 
                 ((this.id != null) ? this.id.hashCode() : 0);
        result = (37 * result) + 
                 ((this.website != null) ? this.website.hashCode() : 0);
        result = (37 * result) + 
                 ((this.weblogEntry != null) ? this.weblogEntry.hashCode() : 0);
        result = (37 * result) + 
                 ((this.dateString != null) ? this.dateString.hashCode() : 0);
        result = (37 * result) + 
                 ((this.refererUrl != null) ? this.refererUrl.hashCode() : 0);
        result = (37 * result) + 
                 ((this.refererPermalink != null)
                  ? this.refererPermalink.hashCode() : 0);
        result = (37 * result) + 
                 ((this.requestUrl != null) ? this.requestUrl.hashCode() : 0);
        result = (37 * result) + 
                 ((this.title != null) ? this.title.hashCode() : 0);
        result = (37 * result) + 
                 ((this.excerpt != null) ? this.excerpt.hashCode() : 0);
        result = (37 * result) + 
                 ((this.visible != null) ? this.visible.hashCode() : 0);
        result = (37 * result) + 
                 ((this.duplicate != null) ? this.duplicate.hashCode() : 0);
        result = (37 * result) + 
                 ((this.dayHits != null) ? this.dayHits.hashCode() : 0);
        result = (37 * result) + 
                 ((this.totalHits != null) ? this.totalHits.hashCode() : 0);

        return result;
    }

    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.apache.roller.pojos.PersistentObject otherData)
    {
        this.id = ((RefererData) otherData).getId();
        this.website = ((RefererData) otherData).getWebsite();
        this.weblogEntry = ((RefererData) otherData).getWeblogEntry();
        this.dateString = ((RefererData) otherData).getDateString();
        this.refererUrl = ((RefererData) otherData).getRefererUrl();
        this.refererPermalink = ((RefererData) otherData).getRefererPermalink();
        this.requestUrl = ((RefererData) otherData).getRequestUrl();
        this.title = ((RefererData) otherData).getTitle();
        this.excerpt = ((RefererData) otherData).getExcerpt();
        this.visible = ((RefererData) otherData).getVisible();
        this.duplicate = ((RefererData) otherData).getDuplicate();
        this.dayHits = ((RefererData) otherData).getDayHits();
        this.totalHits = ((RefererData) otherData).getTotalHits();
    }

    /**
     * A no-op.
     * TODO: fix formbean generation so this is not needed. 
     * @param string
     */
    public void setUrl(String string)
    {
    }

    /**
     * A no-op
     */
    public void setDisplayUrl(String string)
    {
    }

}