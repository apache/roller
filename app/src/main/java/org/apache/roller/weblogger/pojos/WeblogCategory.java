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

import java.io.Serializable;
import java.util.List;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.WebloggerUtils;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;


/**
 * Weblog Category.
 */
@Entity
@Table(name="weblogcategory")
@NamedQueries({
        @NamedQuery(name="WeblogCategory.getByWeblog",
                query="SELECT w FROM WeblogCategory w WHERE w.weblog = ?1 order by w.position"),

        @NamedQuery(name="WeblogCategory.getByWeblog&Name",
                query="SELECT w FROM WeblogCategory w WHERE w.weblog = ?1 AND w.name = ?2"),

        @NamedQuery(name="WeblogCategory.removeByWeblog",
                query="DELETE FROM WeblogCategory w WHERE w.weblog = ?1")
})
public class WeblogCategory implements Serializable, Comparable<WeblogCategory> {
    
    public static final long serialVersionUID = 1435782148712018954L;
    
    // unique internal ID of object
    private String id = WebloggerUtils.generateUUID();
    // category name
    private String name = null;
    // category description
    private String description = null;
    // left-to-right comparative ordering of category, higher numbers go to the right
    private int position;
    // parent weblog of category
    private Weblog weblog = null;

    public WeblogCategory() {
    }
    
    public WeblogCategory(
            Weblog weblog,
            String name,
            String description) {
        
        this.name = name;
        this.description = description;

        this.weblog = weblog;
        weblog.getWeblogCategories().add(this);
        calculatePosition();
    }

    public void calculatePosition() {
        int size = weblog.getWeblogCategories().size();
        if (size == 1) {
            this.position = 0;
        } else {
            this.position = weblog.getWeblogCategories().get(size - 2).getPosition() + 1;
        }
    }


    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic(optional = false)
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    /**
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(WeblogCategory other) {
        return getName().compareTo(other.getName());
    }

    /**
     * Retrieve all weblog entries in this category.
     *
     * @param publishedOnly True if desired to return only published entries
     * @return List of WeblogEntryData objects.
     * @throws WebloggerException
     */
    public List<WeblogEntry> retrieveWeblogEntries(boolean publishedOnly) throws WebloggerException {
        WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(weblog);
        wesc.setCatName(this.getName());
        if (publishedOnly) {
            wesc.setStatus(PubStatus.PUBLISHED);
        }
        return wmgr.getWeblogEntries(wesc);
    }
    
    /**
     * Returns true if category is in use.
     */
    @Transient
    public boolean isInUse() {
        try {
            return WebloggerFactory.getWeblogger().getWeblogEntryManager().isWeblogCategoryInUse(this);
        } catch (WebloggerException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString() {
        return "{" + getId() + ", " + getName() + "}";
    }

    @Override
    public boolean equals(Object other) {

        if (other == null) {
            return false;
        }

        if (other instanceof WeblogCategory) {
            WeblogCategory o = (WeblogCategory) other;
            return new EqualsBuilder()
                    .append(getName(), o.getName())
                    .append(getWeblog(), o.getWeblog())
                    .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getName())
                .append(getWeblog())
                .toHashCode();
    }
}
