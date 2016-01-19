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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.util.Comparator;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerCommon;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Tag bean.
 */
@Entity
@Table(name="weblog_entry_tag")
@NamedQueries({
        @NamedQuery(name="WeblogEntryTag.getByWeblog",
                query="SELECT w FROM WeblogEntryTag w WHERE w.weblog = ?1")
})
public class WeblogEntryTag implements Serializable {
    private static final long serialVersionUID = -2602052289337573384L;
    
    private String id = WebloggerCommon.generateUUID();
    private Weblog website = null;
    private WeblogEntry weblogEntry = null;
    private String name = null;

    
    public WeblogEntryTag() {
    }
    
    public WeblogEntryTag(
            Weblog website,
            WeblogEntry weblogEntry,
            String name) {
        this.website = website;
        this.weblogEntry = weblogEntry;
        this.name = name;
    }

    /**
     * A read-only copy for usage within templates, with fields limited
     * to just those we wish to provide to those templates.
     */
    public WeblogEntryTag templateCopy() {
        WeblogEntryTag copy = new WeblogEntryTag();
        copy.setId(null);
        copy.setName(this.name);
        return copy;
    }

    //------------------------------------------------------- Simple properties
    
    /**
     * Unique ID and primary key.
     */
    @Id
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    /**
     * ID of website that this tag refers to.
     */
    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return this.website;
    }
    
    public void setWeblog(Weblog website) {
        this.website = website;
    }


    @ManyToOne
    @JoinColumn(name="entryid",nullable=false)
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }
    
    public void setWeblogEntry(WeblogEntry data) {
        weblogEntry = data;
    }
    
    
    /**
     * Tag value.
     */
    @Basic(optional=false)
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }

    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        return ("{" + getId() + ", " + getName() + "}");
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntryTag)) {
            return false;
        }
        WeblogEntryTag o = (WeblogEntryTag)other;
        return new EqualsBuilder()
        .append(getName(), o.getName())
        .append(getWeblogEntry(), o.getWeblogEntry())
        .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getName())
        .append(getWeblogEntry())
        .toHashCode();
    }

    public static Comparator<WeblogEntryTag> Comparator = new Comparator<WeblogEntryTag>() {
        public int compare(WeblogEntryTag o1, WeblogEntryTag o2) {
            return o1.getName().compareTo(o2.getName());
        }
    };

}
