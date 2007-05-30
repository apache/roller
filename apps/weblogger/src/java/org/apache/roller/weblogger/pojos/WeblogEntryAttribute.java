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
 * @author David M Johnson
 * @ejb:bean name="WeblogEntryAttribute"
 * @hibernate.class lazy="true" table="entryattribute"
 * @hibernate.cache usage="read-write"
 */
public class WeblogEntryAttribute implements java.lang.Comparable
{
    private String id = UUIDGenerator.generateUUID();
    private WeblogEntry entry;
    private String name;
    private String value;
    
    public WeblogEntryAttribute()
    {
    }

    public WeblogEntryAttribute(
        
        String id,WeblogEntry entry,
        String name,
        String value)
    {
        //this.id = id;
        this.entry = entry;
        this.name = name;
        this.value = value;
    }

    public WeblogEntryAttribute(WeblogEntryAttribute otherData)
    {
        setData(otherData);
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.id column="id" 
     *    generator-class="assigned"  
     */
    public java.lang.String getId()
    {
        return this.id;
    }
    /** @ejb:persistent-field */
    public void setId(java.lang.String id)
    {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 

        this.id = id;
    }

    /**
     * Set bean properties based on other bean.
     */
    public void setData(WeblogEntryAttribute otherData)
    {
        this.id = otherData.getId();
        this.entry = otherData.getEntry();
        this.name = otherData.getName();
        this.value = otherData.getValue();
    }

    /** 
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="entryid" cascade="none" not-null="true"
     */
    public WeblogEntry getEntry()
    {
        return entry;
    }
    /** @ejb:persistent-field */ 
    public void setEntry(WeblogEntry entry)
    {
        this.entry = entry;
    }

    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName()
    {
        return name;
    }
    /** @ejb:persistent-field */ 
    public void setName(String name)
    {
        this.name = name;
    }
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="value" non-null="true" unique="false"
     */
    public String getValue()
    {
        return value;
    }
    /** @ejb:persistent-field */ 
    public void setValue(String value)
    {
        this.value = value;
    }

    public int compareTo(Object o) {
        WeblogEntryAttribute att = (WeblogEntryAttribute)o;
        return getName().compareTo(att.getName());
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
}
