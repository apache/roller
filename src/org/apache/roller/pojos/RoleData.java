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

package org.apache.roller.pojos;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.roller.util.UUIDGenerator;

/**
 * Role bean.
 * @author David M Johnson
 *
 * @ejb:bean name="RoleData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="true" table="userrole"
 * @hibernate.cache usage="read-write"
 */
public class RoleData
   implements java.io.Serializable
{
   static final long serialVersionUID = -4254083071697970972L;

   private java.lang.String id = UUIDGenerator.generateUUID();
   private java.lang.String userName;
   private UserData user;
   private java.lang.String role;

   public RoleData()
   {
   }

   public RoleData(String id, UserData user, String role)
   {
      //this.id = id;
      this.userName = user.getUserName();
      this.user = user;
      this.role = role;
   }

   public RoleData( RoleData otherData )
   {
       setData(otherData);
   }

   /** 
    * @ejb:pk-field
    * @ejb:persistent-field 
    * @hibernate.id column="id"
    *  generator-class="assigned"  
    */
   public java.lang.String getId()
   {
      return this.id;
   }
   /** @ejb:persistent-field */ 
   public void setId( java.lang.String id )
   {
      // Form bean workaround: empty string is never a valid id
      if (id != null && id.trim().length() == 0) return; 
      this.id = id;
   }

   /** 
    * @ejb:persistent-field 
    * @hibernate.property column="username" non-null="true" unique="false"
    */
   public java.lang.String getUserName()
   {
      return this.userName;
   }
   /** @ejb:persistent-field */ 
   public void setUserName( java.lang.String userName )
   {
      this.userName = userName;
   }

   /** 
    * @hibernate.many-to-one column="userid" cascade="none" not-null="true"
    * @ejb:persistent-field 
    */
   public UserData getUser()
   {
      return this.user;
   }
   /** @ejb:persistent-field */ 
   public void setUser( UserData user )
   {
      this.user = user;
   }

   /** 
    * @ejb:persistent-field 
    * @hibernate.property column="rolename" non-null="true" unique="false"
    */
   public java.lang.String getRole()
   {
      return this.role;
   }
   /** @ejb:persistent-field */ 
   public void setRole( java.lang.String role )
   {
      this.role = role;
   }


    //------------------------------------------------------- Good citizenship

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        buf.append(this.id);
        buf.append(", ").append(this.userName);
        buf.append(", ").append(this.role);
        buf.append("}");
        return buf.toString();
    }

    public boolean equals(Object other) {
        if (other == this) return true;
        if (other instanceof RoleData != true) return false;
        RoleData o = (RoleData)other;
        return new EqualsBuilder()
            .append(getRole(), o.getRole())
            .append(getUserName(), o.getUserName())
            .isEquals();
    }
    
    public int hashCode() {
        return new HashCodeBuilder().append(getUserName()).append(getRole()).toHashCode();
    }

    /**
     * Set bean properties based on other bean.
     */
   public void setData( RoleData otherData )
   {
      this.id = otherData.getId();
      this.userName = otherData.getUserName();
      this.user =     otherData.getUser();
      this.role =     otherData.getRole();
   }

}
