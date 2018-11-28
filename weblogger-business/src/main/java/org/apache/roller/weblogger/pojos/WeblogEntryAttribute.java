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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;


/**
 * Weblog entry attribute.
 */
public class WeblogEntryAttribute implements Comparable {
    
    private String id = UUIDGenerator.generateUUID();
    private WeblogEntry entry;
    private String name;
    private String value;
    
    
    public WeblogEntryAttribute() {
    }
    
    
    public String getId() {
        return this.id;
    }
    
    public void setId(String id) {    
        this.id = id;
    }
    
    
    public WeblogEntry getEntry() {
        return entry;
    }
    
    public void setEntry(WeblogEntry entry) {
        this.entry = entry;
    }
    
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    
    public String getValue() {
        return value;
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    
    //------------------------------------------------------- Good citizenship
    
    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.value);
        buf.append("}");
        return buf.toString();
    }
    
    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogEntryAttribute != true) return false;
        WeblogEntryAttribute o = (WeblogEntryAttribute)other;
        return new EqualsBuilder()
        .append(getName(), o.getName())
        .append(getEntry(), o.getEntry())
        .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder()
        .append(getName())
        .append(getEntry())
        .toHashCode();
    }
    
    public int compareTo(Object o) {
        WeblogEntryAttribute att = (WeblogEntryAttribute)o;
        return getName().compareTo(att.getName());
    }
    
}
