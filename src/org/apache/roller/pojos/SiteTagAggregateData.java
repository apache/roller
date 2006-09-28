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
 * @ejb:bean name="SiteTagAggregateData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="sitetagagg"
 * @hibernate.cache usage="read-write"
 */
public class SiteTagAggregateData extends PersistentObject
    implements java.io.Serializable
{
    private static final long serialVersionUID = -7766410727897537118L;
    private java.lang.String id = null;
    private java.lang.String name = null;    
    private int count = 0;

    public SiteTagAggregateData()
    {
    }

    public SiteTagAggregateData(java.lang.String id, 
                       java.lang.String name, int count)
    {
        this.id = id;
        this.name = name;
        this.count = count;
    }

    public SiteTagAggregateData(SiteTagAggregateData otherData)
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
             "name=" + name + " " +
             "count=" + count);
     str.append('}');
     
     return (str.toString());
 }
 
   public boolean equals(Object pOther) {
       if (pOther instanceof SiteTagAggregateData) {
           SiteTagAggregateData lTest = (SiteTagAggregateData) pOther;
           boolean lEquals = true;
           
           lEquals = PojoUtil.equals(lEquals, this.id, lTest.getId());
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
       result = PojoUtil.addHashCode(result, this.name);
       result = PojoUtil.addHashCode(result, new Integer(this.count));
       
       return result;
   }
 
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.apache.roller.pojos.PersistentObject otherData)
    {
        SiteTagAggregateData data = (SiteTagAggregateData) otherData;
        this.id = data.getId();
        this.name = data.getName();
        this.count = data.getCount();
    }

}