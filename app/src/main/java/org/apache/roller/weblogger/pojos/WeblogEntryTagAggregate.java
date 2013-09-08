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

package org.apache.roller.weblogger.pojos;

import java.io.Serializable;
import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * Tag aggregate data.
 */
public class WeblogEntryTagAggregate implements Serializable {
    
    public static final long serialVersionUID = -4343500268898106982L;
    
    private String id = UUIDGenerator.generateUUID();
    private String name = null;
    private Weblog website = null;
    private Timestamp lastUsed = null;
    private int total = 0;
    
    
    public WeblogEntryTagAggregate() {
    }
    
    public WeblogEntryTagAggregate(String id,
            Weblog website,
            String name, int total) {
        //this.id = id;
        this.website = website;
        this.name = name;
        this.total = total;
    }
    
    //------------------------------------------------------- Simple properties
    
    /**
     * Unique ID and primary key.
     */
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    
    public Weblog getWeblog() {
        return this.website;
    }
    
    public void setWeblog(Weblog website) {
        this.website = website;
    }
    
    
    /**
     * Tag value.
     */
    public String getName() {
        return this.name;
    }
    
    public void setName( String name ) {
        this.name = name;
    }
    
    
    public int getTotal() {
        return this.total;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    
    public Timestamp getLastUsed() {
        return this.lastUsed;
    }
    
    public void setLastUsed(Timestamp lastUsed) {
        this.lastUsed = lastUsed;
    }
    
    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append("{");
        buf.append(getId());
        buf.append(", ").append(getName());
        buf.append(", ").append(getTotal());
        buf.append(", ").append(getLastUsed());
        buf.append("}");
        return buf.toString();
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
