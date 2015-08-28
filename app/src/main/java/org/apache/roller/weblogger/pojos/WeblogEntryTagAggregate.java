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
import java.sql.Timestamp;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.weblogger.WebloggerUtils;

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
@Table(name="roller_weblogentrytagagg")
@NamedQueries({
        @NamedQuery(name="WeblogEntryTagAggregate.getByName&WeblogOrderByLastUsedDesc",
                query="SELECT w FROM WeblogEntryTagAggregate w WHERE w.name = ?1 AND w.weblog = ?2 ORDER BY w.lastUsed DESC"),
        @NamedQuery(name="WeblogEntryTagAggregate.getPopularTagsByWeblog",
                query="SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE w.weblog = ?1 GROUP BY w.name, w.total ORDER BY w.total DESC"),
        @NamedQuery(name="WeblogEntryTagAggregate.getPopularTagsByWeblog&StartDate",
                query="SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE w.weblog = ?1 AND w.lastUsed >= ?2 GROUP BY w.name, w.total ORDER BY w.total DESC"),
        @NamedQuery(name="WeblogEntryTagAggregate.removeByTotalLessEqual",
                query="DELETE FROM WeblogEntryTagAggregate w WHERE w.total <= ?1"),
        @NamedQuery(name="WeblogEntryTagAggregate.removeByWeblog",
                query="DELETE FROM WeblogEntryTagAggregate w WHERE w.weblog = ?1"),
        @NamedQuery(name="WeblogEntryTagAggregate.getByName&WeblogNullOrderByLastUsedDesc",
                query="SELECT w FROM WeblogEntryTagAggregate w WHERE w.name = ?1 AND w.weblog IS NULL ORDER BY w.lastUsed DESC"),
        @NamedQuery(name="WeblogEntryTagAggregate.getPopularTagsByWeblogNull",
                query="SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE w.weblog IS NULL GROUP BY w.name, w.total ORDER BY w.total DESC"),
        @NamedQuery(name="WeblogEntryTagAggregate.getPopularTagsByWeblogNull&StartDate",
                query="SELECT w.name, SUM(w.total) FROM WeblogEntryTagAggregate w WHERE w.weblog IS NULL AND w.lastUsed >= ?1 GROUP BY w.name, w.total ORDER BY w.total DESC")
})
public class WeblogEntryTagAggregate implements Serializable {
    
    public static final long serialVersionUID = -4343500268898106982L;
    
    private String id = WebloggerUtils.generateUUID();
    private String name = null;
    private Weblog weblog = null;
    private Timestamp lastUsed = null;
    private int total = 0;
    
    
    public WeblogEntryTagAggregate() {
    }
    
    public WeblogEntryTagAggregate(Weblog weblog,
            String name, int total) {
        this.weblog = weblog;
        this.name = name;
        this.total = total;
    }
    
    //------------------------------------------------------- Simple properties
    
    @Id
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name="weblogid", nullable=true)
    public Weblog getWeblog() {
        return this.weblog;
    }
    
    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Basic(optional=false)
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

    @Basic(optional=false)
    public Timestamp getLastUsed() {
        return this.lastUsed;
    }
    
    public void setLastUsed(Timestamp lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        return "{" + getId() + ", " + getName() + ", " + getTotal() + ", " + getLastUsed() + "}";
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
