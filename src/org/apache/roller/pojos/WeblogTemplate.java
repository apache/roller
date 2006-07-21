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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;

/**
 * Pojo that represents a single user defined template page.
 *
 * This template is different from the generic template because it also
 * contains a reference to the website it is part of.
 *
 * @ejb:bean name="WeblogTemplate"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="webpage"
 * @hibernate.cache usage="read-write"
 */
public class WeblogTemplate extends PersistentObject
        implements Serializable, Template {
    
    public static final long serialVersionUID = -613737191638263428L;
    public static final String DEFAULT_PAGE = "Weblog";
    
    private static Log log = LogFactory.getLog(WeblogTemplate.class);
    private static Set requiredTemplates = null;
    
    private String id = null;
    private String name = null;
    private String description = null;
    private String link = null;
    private String contents = null;
    private Date lastModified = null;
    private String templateLanguage = null;
    private boolean hidden = false;
    private String decoratorName = null;
    
    private WebsiteData weblog = null;
    
    
    static {
        requiredTemplates = new HashSet();
        requiredTemplates.add("Weblog");
        requiredTemplates.add("_day");
        requiredTemplates.add("_css");
        requiredTemplates.add("_decorator");
    }
    
    public WeblogTemplate() {}
    
    public WeblogTemplate(
            java.lang.String id,
            WebsiteData website,
            java.lang.String name,
            java.lang.String description,
            java.lang.String link,
            java.lang.String template,
            java.util.Date updateTime,
            String tempLang,
            boolean hid,
            String decorator) {
        this.id = id;
        this.weblog = website;
        this.name = name;
        this.description = description;
        this.link = link;
        this.contents = template;
        this.lastModified = (Date)updateTime.clone();
        this.templateLanguage = tempLang;
        this.hidden = hid;
        this.decoratorName = decorator;
    }
    
    public WeblogTemplate( WeblogTemplate otherData ) {
        setData(otherData);
    }
    
    
    public Template getDecorator() {
        if(decoratorName != null && !id.equals(decoratorName)) {
            try {
                return weblog.getPageByName(decoratorName);
            } catch (RollerException ex) {
                log.error("Error getting decorator["+decoratorName+"] "+
                        "for template "+id);
            }
        }
        return null;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.id column="id"
     *  generator-class="uuid.hex" unsaved-value="null"
     */
    public java.lang.String getId() {
        return this.id;
    }
    
    /** @ejb:persistent-field */
    public void setId( java.lang.String id ) {
        this.id = id;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite() {
        return this.weblog;
    }
    
    /** @ejb:persistent-field */
    public void setWebsite( WebsiteData website ) {
        this.weblog = website;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public java.lang.String getName() {
        return this.name;
    }
    
    /** @ejb:persistent-field */
    public void setName( java.lang.String name ) {
        this.name = name;
    }
    
    
    /**
     * Description
     * @ejb:persistent-field
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public java.lang.String getDescription() {
        return this.description;
    }
    
    /** @ejb:persistent-field */
    public void setDescription( java.lang.String description ) {
        this.description = description;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="link" non-null="true" unique="false"
     */
    public java.lang.String getLink() {
        return this.link;
    }
    
    /** @ejb:persistent-field */
    public void setLink( java.lang.String link ) {
        this.link = link;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="template" non-null="true" unique="false"
     */
    public java.lang.String getContents() {
        return this.contents;
    }
    
    /** @ejb:persistent-field */
    public void setContents( java.lang.String template ) {
        this.contents = template;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="updatetime" non-null="true" unique="false"
     */
    public java.util.Date getLastModified() {
        return (Date)this.lastModified.clone();
    }
    
    /** @ejb:persistent-field */
    public void setLastModified(final java.util.Date newtime ) {
        if (newtime != null) {
            lastModified = (Date)newtime.clone();
        } else {
            lastModified = null;
        }
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="templatelang" non-null="true" unique="false"
     */
    public String getTemplateLanguage() {
        return templateLanguage;
    }

    /** @ejb:persistent-field */
    public void setTemplateLanguage(String templateLanguage) {
        this.templateLanguage = templateLanguage;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="hidden" non-null="true" unique="false"
     */
    public boolean isHidden() {
        return hidden;
    }

    /** @ejb:persistent-field */
    public void setHidden(boolean isHidden) {
        this.hidden = isHidden;
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="decorator" non-null="true" unique="false"
     */
    public String getDecoratorName() {
        return decoratorName;
    }

    /** @ejb:persistent-field */
    public void setDecoratorName(String decorator) {
        this.decoratorName = decorator;
    }
    
    
    /**
     * Determine if this WeblogTemplate is required or not.
     */
    public boolean isRequired() {
       /*
        * this is kind of hacky right now, but it's like that so we can be
        * reasonably flexible while we migrate old blogs which may have some
        * pretty strange customizations.
        *
        * my main goal starting now is to prevent further deviations from the
        * standardized templates as we move forward.
        *
        * eventually, the required flag should probably be stored in the db
        * and possibly applicable to any template.
        */
        return (requiredTemplates.contains(this.name) || "Weblog".equals(this.link));
    }
    
    
    public void setRequired(boolean req) {
        // this is an absurd workaround for our struts formbean generation stuff
    }
    
    
    public String toString() {
        StringBuffer str = new StringBuffer("{");
        
        str.append("id=" + id + " " + "name=" + name + " " + "description="
                + description + " " + "link=" + link + " " + "template=" + contents
                + " " + "updateTime=" + lastModified);
        str.append('}');
        
        return(str.toString());
    }
    
    
    public boolean equals( Object pOther ) {
        if( pOther instanceof WeblogTemplate ) {
            WeblogTemplate lTest = (WeblogTemplate) pOther;
            boolean lEquals = true;
            
            if( this.id == null ) {
                lEquals = lEquals && ( lTest.getId() == null );
            } else {
                lEquals = lEquals && this.id.equals( lTest.getId() );
            }
            if( this.weblog == null ) {
                lEquals = lEquals && ( lTest.getWebsite() == null );
            } else {
                lEquals = lEquals && this.weblog.equals( lTest.getWebsite() );
            }
            if( this.name == null ) {
                lEquals = lEquals && ( lTest.getName() == null );
            } else {
                lEquals = lEquals && this.name.equals( lTest.getName() );
            }
            if( this.description == null ) {
                lEquals = lEquals && ( lTest.getDescription() == null );
            } else {
                lEquals = lEquals && this.description.equals( lTest.getDescription() );
            }
            if( this.link == null ) {
                lEquals = lEquals && ( lTest.getLink() == null );
            } else {
                lEquals = lEquals && this.link.equals( lTest.getLink() );
            }
            if( this.contents == null ) {
                lEquals = lEquals && ( lTest.getContents() == null );
            } else {
                lEquals = lEquals && this.contents.equals( lTest.getContents() );
            }
            if( this.lastModified == null ) {
                lEquals = lEquals && ( lTest.getLastModified() == null );
            } else {
                lEquals = lEquals && this.lastModified.equals( lTest.getLastModified() );
            }
            
            return lEquals;
        } else {
            return false;
        }
    }
    
    
    public int hashCode() {
        int result = 17;
        result = 37*result + ((this.id != null) ? this.id.hashCode() : 0);
        result = 37*result + ((this.weblog != null) ? this.weblog.hashCode() : 0);
        result = 37*result + ((this.name != null) ? this.name.hashCode() : 0);
        result = 37*result + ((this.description != null) ? this.description.hashCode() : 0);
        result = 37*result + ((this.link != null) ? this.link.hashCode() : 0);
        result = 37*result + ((this.contents != null) ? this.contents.hashCode() : 0);
        result = 37*result + ((this.lastModified != null) ? this.lastModified.hashCode() : 0);
        return result;
    }
    
    
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData( org.apache.roller.pojos.PersistentObject otherData ) {
        WeblogTemplate other = (WeblogTemplate)otherData;
        this.weblog =     other.getWebsite();
        this.id =           other.getId();
        this.name =         other.getName();
        this.description =  other.getDescription();
        this.link =         other.getLink();
        this.contents =     other.getContents();
        this.lastModified = other.getLastModified()!=null ? (Date)other.getLastModified().clone() : null;
        this.templateLanguage = other.getTemplateLanguage();
        this.hidden = other.isHidden();
        this.decoratorName = other.getDecoratorName();
    }
    
}
