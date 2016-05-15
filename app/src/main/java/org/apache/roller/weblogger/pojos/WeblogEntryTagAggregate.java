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

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Tag aggregate data.
 */
@Entity
@Table(name="weblog_entry_tag_agg")
@NamedQueries({
        @NamedQuery(name="WeblogEntryTagAggregate.getPopularTagsByWeblog",
                query="SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE w.weblog = ?1 " +
                        "GROUP BY w.name ORDER BY SUM(w.total) DESC")
})
public class WeblogEntryTagAggregate {
    
    private String name = null;
    private Weblog weblog = null;
    private int total = 0;
    
    public WeblogEntryTagAggregate() {
    }

    //------------------------------------------------------- Simple properties
    
    @ManyToOne
    @JoinColumn(name="weblogid", nullable=true)
    @Id
    public Weblog getWeblog() {
        return this.weblog;
    }
    
    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Basic(optional=false)
    @Id
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }

    @Basic(optional=false)
    public int getTotal() {
        return this.total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }

    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        return "{" + getWeblog().getHandle() + ", " + getName() + ", " + getTotal() + "}";
    }
    
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof WeblogEntryTagAggregate)) {
            return false;
        }
        WeblogEntryTagAggregate o = (WeblogEntryTagAggregate)other;
        return new EqualsBuilder()
        .append(getName(), o.getName())
        .append(this.getWeblog(), o.getWeblog())
        .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getName())
        .append(getWeblog())
        .toHashCode();
    }
    
}
