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
 */

package org.apache.roller.pojos;

import java.io.Serializable;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents hit count data for a weblog.
 *
 * @ejb:bean name="HitCountData"
 * @hibernate.class lazy="true" table="roller_hitcounts"
 * @hibernate.cache usage="read-write"
 */
public class HitCountData implements Serializable {
    
    private String id = null;
    private WebsiteData weblog = null;
    private int dailyHits = 0;
    
    
    public HitCountData() {}

    
    public void setData(HitCountData other) {
        this.id = other.getId();
        this.weblog = other.getWeblog();
        this.dailyHits = other.getDailyHits();
    }
    
    
    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.dailyHits);
        buf.append("}");
        return buf.toString();
    }

    //------------------------------------------------------------------------
    
    public boolean equals(Object other) {
        
        if(this == other) return true;
        if( !(other instanceof HitCountData) ) return false;
        
        // our natural key, or business key, is our weblog
        final HitCountData that = (HitCountData) other;
        return this.getWeblog().equals(that.getWeblog());
    }
       
    public int hashCode() { 
        return new HashCodeBuilder()
            .append(getWeblog())
            .toHashCode();
    }
    
    
    /**
     * @ejb:persistent-field
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" non-null="true"
     */
    public WebsiteData getWeblog() {
        return weblog;
    }

    public void setWeblog(WebsiteData weblog) {
        this.weblog = weblog;
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="dailyhits" non-null="true" unique="false"
     */
    public int getDailyHits() {
        return dailyHits;
    }

    public void setDailyHits(int dailyHits) {
        this.dailyHits = dailyHits;
    }
    
}
