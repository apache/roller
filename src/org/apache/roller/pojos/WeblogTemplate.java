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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;


/**
 * POJO that represents a single user defined template page.
 *
 * This template is different from the generic template because it also
 * contains a reference to the website it is part of.
 *
 * @ejb:bean name="WeblogTemplate"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true" table="webpage"
 * @hibernate.cache usage="read-write"
 */
public class WeblogTemplate implements Serializable, Template {
    
    public static final long serialVersionUID = -613737191638263428L;
    public static final String DEFAULT_PAGE = "Weblog";
    
    private static Log log = LogFactory.getLog(WeblogTemplate.class);
    private static Set requiredTemplates = null;
    
    private String  id = null;
    private String  action = null;
    private String  name = null;
    private String  description = null;
    private String  link = null;
    private String  contents = null;
    private Date    lastModified = null;
    private String  templateLanguage = null;
    private boolean hidden = false;
    private boolean navbar = false;
    private String  decoratorName = null;
    private String  outputContentType = null;
    
    private WebsiteData weblog = null;
    
    
    static {
        requiredTemplates = new HashSet();
        requiredTemplates.add("Weblog");
        requiredTemplates.add("_day");
        requiredTemplates.add("_css");
        requiredTemplates.add("_decorator");
    }
    
    
    public WeblogTemplate() {}
    
    
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
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
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
     * @hibernate.property column="action" non-null="true" unique="false"
     */
    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
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
     * @hibernate.property column="navbar" non-null="true" unique="false"
     */
    public boolean isNavbar() {
        return navbar;
    }

    /** @ejb:persistent-field */
    public void setNavbar(boolean navbar) {
        this.navbar = navbar;
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
     * @hibernate.property column="decorator" non-null="false" unique="false"
     */
    public String getDecoratorName() {
        return decoratorName;
    }

    /** @ejb:persistent-field */
    public void setDecoratorName(String decorator) {
        this.decoratorName = decorator;
    }
    
    /** 
     * Content-type rendered by template or null for auto-detection by link extension.
     * @ejb:persistent-field
     * @hibernate.property column="outputtype" non-null="false" unique="false"
     */
    public String getOutputContentType() {
        return outputContentType;
    }

    /** @ejb:persistent-field */
    public void setOutputContentType(String outputContentType) {
        this.outputContentType = outputContentType;
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
    
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.link);
        buf.append(", ").append(this.decoratorName);
        buf.append(", ").append(this.templateLanguage);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogTemplate != true) return false;
        WeblogTemplate o = (WeblogTemplate)other;
        return new EqualsBuilder()
            .append(name, o.getName()) 
            .append(getWebsite(), o.getWebsite()) 
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getName())
            .append(getWebsite())
            .toHashCode();
    }    
    
    /**
     * Set bean properties based on other bean.
     */
    public void setData( WeblogTemplate otherData ) {
        WeblogTemplate other = (WeblogTemplate)otherData;
        this.weblog =     other.getWebsite();
        this.id =           other.getId();
        this.name =         other.getName();
        this.description =  other.getDescription();
        this.link =         other.getLink();
        this.navbar =         other.isNavbar();
        this.contents =     other.getContents();
        this.lastModified = other.getLastModified()!=null ? (Date)other.getLastModified().clone() : null;
        this.templateLanguage = other.getTemplateLanguage();
        this.hidden = other.isHidden();
        this.decoratorName = other.getDecoratorName();
    }
    
}
