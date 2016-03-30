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
package org.apache.roller.weblogger.pojos;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.WebloggerCommon;

import javax.persistence.Basic;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * POJO that represents a single user defined template page.
 *
 * This template is different from the generic template because it also
 * contains a reference to the website it is part of.
 */
@Entity
@Table(name="weblog_template")
@NamedQueries({
    @NamedQuery(name="WeblogTemplate.getByWeblog",
            query="SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1"),
    @NamedQuery(name="WeblogTemplate.getByWeblogOrderByName",
            query="SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 ORDER BY w.name"),
    @NamedQuery(name="WeblogTemplate.getByWeblog&Link",
            query="SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 AND w.link = ?2"),
    @NamedQuery(name="WeblogTemplate.getByAction",
            query="SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 AND w.action = ?2"),
    @NamedQuery(name="WeblogTemplate.getByWeblog&Name",
        query="SELECT w FROM WeblogTemplate w WHERE w.weblog = ?1 AND w.name= ?2")
})
public class WeblogTemplate implements ThemeTemplate, Serializable {
    
    public static final long serialVersionUID = -613737191638263428L;

    // attributes
    private String id = WebloggerCommon.generateUUID();
    private ComponentType action = null;
    private String  name = null;
    private String  description = null;
    private String  link = null;
    private Date    lastModified = null;
    private boolean hidden = false;
    private boolean navbar = false;

    private String  contentsStandard = null;
    private String  contentsMobile = null;

    // associations
    private Weblog weblog = null;

    public WeblogTemplate() {}

    @Id
    public String getId() {
        return this.id;
    }
    
    public void setId( String id ) {
        this.id = id;
    }

    @Basic(optional=false)
    @Enumerated(EnumType.STRING)
    public ComponentType getAction() {
        return action;
    }

    public void setAction(ComponentType action) {
        this.action = action;
    }

    @Basic(optional=false)
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }
    
    public void setDescription( String description ) {
        this.description = description;
    }

    public String getLink() {
        return this.link;
    }
    
    public void setLink( String link ) {
        this.link = link;
    }

    @Column(name="updatetime", nullable=false)
    @Temporal(TemporalType.TIMESTAMP)
    public Date getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(final Date newtime ) {
        lastModified = newtime;
    }

    @Basic(optional=false)
    public boolean isNavbar() {
        return navbar;
    }

    public void setNavbar(boolean navbar) {
        this.navbar = navbar;
    }

    @Basic(optional=false)
    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean isHidden) {
        this.hidden = isHidden;
    }

    private List<WeblogTemplateRendition> templateRenditions = new ArrayList<WeblogTemplateRendition>();

    @ManyToOne
    @JoinColumn(name="weblogid", nullable=false)
    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog( Weblog website ) {
        this.weblog = website;
    }

    @OneToMany(targetEntity=WeblogTemplateRendition.class,
            cascade=CascadeType.ALL, mappedBy="weblogTemplate")
    public List<WeblogTemplateRendition> getTemplateRenditions() {
        return templateRenditions;
    }

    public void setTemplateRenditions(List<WeblogTemplateRendition> templateRenditions) {
        this.templateRenditions = templateRenditions;
    }

    /**
     * A convenience method for testing if this template represents a 'custom'
     * template, meaning a template with action = ACTION_CUSTOM.
     */
    @Transient
    public boolean isCustom() {
        return ComponentType.CUSTOM.equals(getAction());
    }

    public WeblogTemplateRendition getTemplateRendition(WeblogTemplateRendition.RenditionType desiredType) throws WebloggerException {
        for (WeblogTemplateRendition rnd : templateRenditions) {
            if (rnd.getType().equals(desiredType)) {
                return rnd;
            }
        }
        return null;
    }

    public void addTemplateRendition(WeblogTemplateRendition newRendition) {
        if (hasTemplateRendition(newRendition)) {
            throw new IllegalArgumentException("Rendition type '" + newRendition.getType()
                    + " for template '" + this.getName() + "' already exists.");
        }
        templateRenditions.add(newRendition);
    }

    public boolean hasTemplateRendition(WeblogTemplateRendition proposed) {
        for (WeblogTemplateRendition rnd : templateRenditions) {
            if(rnd.getType().equals(proposed.getType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ThemeTemplate templateCopy() {
        WeblogTemplate copy = new WeblogTemplate();
        copy.setId(id);
        copy.setName(name);
        copy.setDescription(description);
        copy.setLink(link);
        copy.setLastModified(lastModified);
        copy.setHidden(hidden);
        copy.setNavbar(navbar);
        return copy;
    }

    //------------------------------------------------------- Good citizenship

    public String toString() {
        return "{" + getId() + ", " + getName() + ", " + getLink() + "}";
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogTemplate)) {
            return false;
        }
        WeblogTemplate o = (WeblogTemplate)other;
        return new EqualsBuilder()
            .append(getName(), o.getName())
            .append(getWeblog(), o.getWeblog())
            .isEquals();
    }
    
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getName())
            .append(getWeblog())
            .toHashCode();
    }

    @Transient
    public String getContentsStandard() {
        return this.contentsStandard;
    }

    public void setContentsStandard( String contents ) {
        this.contentsStandard = contents;
    }

    @Transient
    public String getContentsMobile() {
        return this.contentsMobile;
    }

    public void setContentsMobile( String contents ) {
        this.contentsMobile = contents;
    }

}
