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
package org.apache.roller.pojos;

import org.apache.roller.util.PojoUtil;


/**
 * @author Elias Torres
 *
 * @ejb:bean name="WeblogTagAggregateData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="weblogtagagg"
 * @hibernate.cache usage="read-write"
 */
public class WeblogTagAggregateData extends PersistentObject
    implements java.io.Serializable
{
    private static final long serialVersionUID = -4343500268898106982L;
    private java.lang.String id = null;
    private java.lang.String name = null;    
    private WebsiteData website = null;
    private int count = 0;

    public WeblogTagAggregateData()
    {
    }

    public WeblogTagAggregateData(java.lang.String id,
                       WebsiteData website,
                       java.lang.String name, int count)
    {
        this.id = id;
        this.website = website;
        this.name = name;
        this.count = count;
    }

    public WeblogTagAggregateData(WeblogTagAggregateData otherData)
    {
        setData(otherData);
    }

    //------------------------------------------------------- Simple properties

    /** 
     * Unique ID and primary key of this Referer.
     *
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
    public java.lang.String getId()
    {
        return this.id;
    }

    public void setId(java.lang.String id)
    {
        this.id = id;
    }
    
    /**
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="false"
     */
    public WebsiteData getWebsite() {
        return this.website;
    }
    
    /** @ejb:persistent-field */
    public void setWebsite(WebsiteData website) {
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
    * @hibernate.property column="count" non-null="true" unique="false"
    */
   public int getCount()
   {
       return this.count;
   }

   /** @ejb:persistent-field */
   public void setCount(int count)
   {
       this.count = count;
   }    

   public String toString() {
     StringBuffer str = new StringBuffer("{");
     
     str.append("id=" + id + " " +
             "website=" + website +
             "name=" + name + " " +
             "count=" + count);
     str.append('}');
     
     return (str.toString());
 }
 
   public boolean equals(Object pOther) {
       if (pOther instanceof WeblogTagAggregateData) {
           WeblogTagAggregateData lTest = (WeblogTagAggregateData) pOther;
           boolean lEquals = true;
           
           lEquals = PojoUtil.equals(lEquals, this.id, lTest.getId());
           lEquals = PojoUtil.equals(lEquals, this.website, lTest.getWebsite());
           lEquals = PojoUtil.equals(lEquals, this.name, lTest.getName());
           lEquals = this.count == lTest.getCount();
           return lEquals;
       } else {
           return false;
       }
   }
 
   public int hashCode() {
       int result = 17;
       result = PojoUtil.addHashCode(result, this.id);
       result = PojoUtil.addHashCode(result, this.website);
       result = PojoUtil.addHashCode(result, this.name);
       result = PojoUtil.addHashCode(result, new Integer(this.count));
       
       return result;
   }
 
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.apache.roller.pojos.PersistentObject otherData)
    {
        WeblogTagAggregateData data = (WeblogTagAggregateData) otherData;
        this.id = data.getId();
        this.website = data.getWebsite();
        this.name = data.getName();
        this.count = data.getCount();
    }

}