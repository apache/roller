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
/*
 * Generated file - Do not edit!
 */
package org.apache.roller.weblogger.pojos;

import java.sql.Timestamp;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * 
 * 
 * @author Elias Torres
 * @ejb:bean name="WeblogEntryTagAggregate"
 * @hibernate.cache usage="read-write"
 * @hibernate.class lazy="true" table="roller_weblogentrytagagg"
 * @struts.form include-all="true"
 */
public class WeblogEntryTagAggregate
    implements java.io.Serializable
{
    private static final long serialVersionUID = -4343500268898106982L;
    private java.lang.String id = UUIDGenerator.generateUUID();
    private java.lang.String name = null;    
    private Weblog website = null;
    private Timestamp lastUsed = null;
    private int total = 0;

    public WeblogEntryTagAggregate()
    {
    }

    public WeblogEntryTagAggregate(java.lang.String id,
                       Weblog website,
                       java.lang.String name, int total)
    {
        //this.id = id;
        this.website = website;
        this.name = name;
        this.total = total;
    }

    public WeblogEntryTagAggregate(WeblogEntryTagAggregate otherData)
    {
        setData(otherData);
    }

    //------------------------------------------------------- Simple properties

    /** 
     * Unique ID and primary key of this Referer.
     *
     * @hibernate.id column="id" generator-class="assigned"  
     */
    public java.lang.String getId()
    {
        return this.id;
    }

    public void setId(java.lang.String id)
    {
        // Form bean workaround: empty string is never a valid id
        if (id != null && id.trim().length() == 0) return; 
        this.id = id;
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="false"
     */
    public Weblog getWeblog() {
        return this.website;
    }
    
    /** @ejb:persistent-field */
    public void setWeblog(Weblog website) {
        this.website = website;
    }    

    /**
     * Tag value
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName() {
        return this.name;
    }
    /** @ejb:persistent-field */
    public void setName( String name ) {
        this.name = name;
    }   
    
    /**
    *
    * @roller.wrapPojoMethod type="simple"
    * @ejb:persistent-field
    * @hibernate.property column="total" non-null="true" unique="false"
    */
   public int getTotal()
   {
       return this.total;
   }
   
   /**
   *
   * @roller.wrapPojoMethod type="simple"
   * @ejb:persistent-field
   * @hibernate.property column="lastused" non-null="true" unique="false"
   */   
   public Timestamp getLastUsed() {
       return this.lastUsed;
   }
   
   /** @ejb:persistent-field */
   public void setLastUsed(Timestamp lastUsed) {
       this.lastUsed = lastUsed;
   }   

   /** @ejb:persistent-field */
   public void setTotal(int total)
   {
       this.total = total;
   }    

    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.name);
        buf.append(", ").append(this.total);
        buf.append(", ").append(this.lastUsed);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof WeblogEntryTagAggregate != true) return false;
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
 
    /**
     * Set bean properties based on other bean.
     */
    public void setData(WeblogEntryTagAggregate data)
    {
        this.id = data.getId();
        this.website = data.getWeblog();
        this.name = data.getName();
        this.total = data.getTotal();
        this.lastUsed = data.getLastUsed();
    }

}
