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


/**
 * Role bean.
 * @author David M Johnson
 *
 * @ejb:bean name="RoleData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="userrole"
 * @hibernate.cache usage="read-write"
 */
public class RoleData
   extends org.apache.roller.pojos.PersistentObject
   implements java.io.Serializable
{
   static final long serialVersionUID = -4254083071697970972L;

   private java.lang.String id;
   private java.lang.String userName;
   private UserData user;
   private java.lang.String role;

   public RoleData()
   {
   }

   public RoleData(String id, UserData user, String role)
   {
      this.id = id;
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
    *  generator-class="uuid.hex" unsaved-value="null"
    */
   public java.lang.String getId()
   {
      return this.id;
   }
   /** @ejb:persistent-field */ 
   public void setId( java.lang.String id )
   {
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

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + id + " " + "userName=" + userName + " " + "user=" + user + " " + "role=" + role);
      str.append('}');

      return(str.toString());
   }

   public boolean equals( Object pOther )
   {
      if( pOther instanceof RoleData )
      {
         RoleData lTest = (RoleData) pOther;
         boolean lEquals = true;

         if( this.userName == null )
         {
            lEquals = lEquals && ( lTest.getUserName() == null );
         }
         else
         {
            lEquals = lEquals && this.userName.equals( lTest.getUserName() );
         }
         if( this.user == null )
         {
            lEquals = lEquals && ( lTest.getUser() == null );
         }
         else
         {
            lEquals = lEquals && this.user.equals( lTest.getUser() );
         }
         if( this.role == null )
         {
            lEquals = lEquals && ( lTest.getRole() == null );
         }
         else
         {
            lEquals = lEquals && this.role.equals( lTest.getRole() );
         }

         return lEquals;
      }
      else
      {
         return false;
      }
   }

   public int hashCode()
   {
      int result = 17;
      result = 37*result + ((this.id != null) ? this.id.hashCode() : 0);
      result = 37*result + ((this.userName != null) ? this.userName.hashCode() : 0);
      result = 37*result + ((this.user != null) ? this.user.hashCode() : 0);
      result = 37*result + ((this.role != null) ? this.role.hashCode() : 0);
      return result;
      }

   /**
	* Setter is needed in RollerImpl.storePersistentObject()
    */
   public void setData( org.apache.roller.pojos.PersistentObject otherData )
   {

      this.id = ((RoleData)otherData).getId();
      this.userName = ((RoleData)otherData).getUserName();
      this.user = ((RoleData)otherData).getUser();
      this.role = ((RoleData)otherData).getRole();
   }

}
