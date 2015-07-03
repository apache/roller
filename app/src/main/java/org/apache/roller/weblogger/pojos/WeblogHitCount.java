/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;


/**
 * Represents hit count data for a weblog.
 */
@Entity
@Table(name="roller_hitcounts")
@NamedQueries({
        @NamedQuery(name="WeblogHitCount.getByWeblog",
                query="SELECT h FROM WeblogHitCount h WHERE h.weblog = ?1"),
        @NamedQuery(name="WeblogHitCount.getByWeblogEnabledTrueAndActiveTrue&DailyHitsGreaterThenZero&WeblogLastModifiedGreaterOrderByDailyHitsDesc",
                query="SELECT h FROM WeblogHitCount h WHERE h.weblog.visible = true AND h.weblog.active = true AND h.weblog.lastModified > ?1 AND h.dailyHits > 0 ORDER BY h.dailyHits DESC"),
        @NamedQuery(name="WeblogHitCount.updateDailyHitCountZero",
                query="UPDATE WeblogHitCount h SET h.dailyHits = 0")
})
public class WeblogHitCount implements Serializable {
    
    private String id = UUIDGenerator.generateUUID();
    private Weblog weblog = null;
    private int dailyHits = 0;
    
    public WeblogHitCount() {}

    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name="weblogid",nullable=false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public int getDailyHits() {
        return dailyHits;
    }

    public void setDailyHits(int dailyHits) {
        this.dailyHits = dailyHits;
    }

    public String toString() {
        return "{" + getId() + ", " + getDailyHits() + "}";
    }
    
    public boolean equals(Object other) {
        
        if(this == other) {
            return true;
        }
        if( !(other instanceof WeblogHitCount) ) {
            return false;
        }
        
        // our natural key, or business key, is our weblog
        final WeblogHitCount that = (WeblogHitCount) other;
        return this.getWeblog().equals(that.getWeblog());
    }
       
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getWeblog())
            .toHashCode();
    }
}
