
package org.roller.pojos;

import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.UserManager;

import java.util.Date;
import java.util.Iterator;


/**
 * User bean.
 * @author David M Johnson
 *
 * @ejb:bean name="UserData"
 * @struts.form include-all="true"
 * @hibernate.class table="rolleruser"  
 * hibernate.jcs-cache usage="read-write"
 */
public class UserData
   extends org.roller.pojos.PersistentObject
   implements java.io.Serializable
{
   static final long serialVersionUID = -6354583200913127874L;

   protected java.lang.String id;
   protected java.lang.String userName;
   protected java.lang.String password;
   protected java.lang.String fullName;
   protected java.lang.String emailAddress;
   protected java.util.Date dateCreated;

   public UserData()
   {
   }

	public UserData( java.lang.String id, java.lang.String userName,
                     java.lang.String password, java.lang.String fullName,
                     java.lang.String emailAddress, java.util.Date dateCreated)
	{
		this.id = id;
		this.userName = userName;
		this.password = password;
		this.fullName = fullName;
		this.emailAddress = emailAddress;
		this.dateCreated = dateCreated;
	}

	public UserData( UserData otherData )
	{
		this.id = otherData.id;
		this.userName = otherData.userName;
		this.password = otherData.password;
		this.fullName = otherData.fullName;
		this.emailAddress = otherData.emailAddress;
		this.dateCreated = otherData.dateCreated;

	}

   /** Id of the User.
     * Not remote since primary key may be extracted by other means.
     * 
     * @struts.validator type="required" msgkey="errors.required"
     * @ejb:persistent-field 
     * @hibernate.id column="id" type="string"
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

   /** User name of the user.
     * @ejb:persistent-field 
     * @hibernate.property column="username" non-null="true" unique="true"
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

   /** Password of the user.
     * @ejb:persistent-field 
     * @hibernate.property column="passphrase" non-null="true"
     */
   public java.lang.String getPassword()
   {
      return this.password;
   }
   /** @ejb:persistent-field */ 
   public void setPassword( java.lang.String password )
   {
      this.password = password;
   }

   /** Full name of the user.
     * @ejb:persistent-field 
     * @hibernate.property column="fullname" non-null="true" unique="true"
     */
   public java.lang.String getFullName()
   {
      return this.fullName;
   }
   /** @ejb:persistent-field */ 
   public void setFullName( java.lang.String fullName )
   {
      this.fullName = fullName;
   }

   /** E-mail address of the user.
     * @ejb:persistent-field 
     * @hibernate.property column="emailaddress" non-null="true" unique="true"
     */
   public java.lang.String getEmailAddress()
   {
      return this.emailAddress;
   }
   /** @ejb:persistent-field */ 
   public void setEmailAddress( java.lang.String emailAddress )
   {
      this.emailAddress = emailAddress;
   }

   /** 
	* @ejb:persistent-field 
	* @hibernate.property column="datecreated" non-null="true" unique="false"
	*/
   public java.util.Date getDateCreated()
   {
	   return dateCreated;
   }
   /** @ejb:persistent-field */ 
   public void setDateCreated(java.util.Date date)
   {
	   dateCreated= date;
   }

   public String toString()
   {
		StringBuffer str = new StringBuffer("{");

		str.append("id=" + id + " ");
		str.append("userName=" + userName + " ");
		str.append("password=" + password + " ");
		str.append("fullName=" + fullName + " ");
		str.append("emailAddress=" + emailAddress + " ");
		str.append("dateCreated=" + dateCreated + " ");
		str.append('}');

		return(str.toString());
   }

   public boolean equals( Object pOther )
   {
      if( pOther instanceof UserData )
      {
         UserData lTest = (UserData) pOther;
         boolean lEquals = true;

         if( this.id == null )
         {
            lEquals = lEquals && ( lTest.id == null );
         }
         else
         {
            lEquals = lEquals && this.id.equals( lTest.id );
         }
         if( this.userName == null )
         {
            lEquals = lEquals && ( lTest.userName == null );
         }
         else
         {
            lEquals = lEquals && this.userName.equals( lTest.userName );
         }
         if( this.password == null )
         {
            lEquals = lEquals && ( lTest.password == null );
         }
         else
         {
            lEquals = lEquals && this.password.equals( lTest.password );
         }
         if( this.fullName == null )
         {
            lEquals = lEquals && ( lTest.fullName == null );
         }
         else
         {
            lEquals = lEquals && this.fullName.equals( lTest.fullName );
         }
         if( this.emailAddress == null )
         {
            lEquals = lEquals && ( lTest.emailAddress == null );
         }
         else
         {
            lEquals = lEquals && this.emailAddress.equals( lTest.emailAddress );
         }
         
		if( this.dateCreated == null )
		{
		   lEquals = lEquals && ( lTest.dateCreated == null );
		}
		else
		{
		   lEquals = lEquals && datesEquivalent(this.dateCreated, lTest.dateCreated);
		}

        return lEquals;
      }
      else
      {
         return false;
      }
   }
   
    private boolean datesEquivalent(Date d1, Date d2)
    {
        boolean equiv = true;
        equiv = equiv && d1.getHours() == d1.getHours();
        equiv = equiv && d1.getMinutes() == d1.getMinutes();
        equiv = equiv && d1.getSeconds() == d1.getSeconds();
        equiv = equiv && d1.getMonth() == d1.getMonth();
        equiv = equiv && d1.getDay() == d1.getDay();
        equiv = equiv && d1.getYear() == d1.getYear();
        return equiv;
    }

   public int hashCode()
   {
      int result = 17;
      result = 37*result + ((this.id != null) ? this.id.hashCode() : 0);
      result = 37*result + ((this.userName != null) ? this.userName.hashCode() : 0);
      result = 37*result + ((this.password != null) ? this.password.hashCode() : 0);
      result = 37*result + ((this.fullName != null) ? this.fullName.hashCode() : 0);
      result = 37*result + ((this.emailAddress != null) ? this.emailAddress.hashCode() : 0);
      result = 37*result + ((this.dateCreated != null) ? this.dateCreated.hashCode() : 0);
      return result;
      }

   /**
	* Setter is needed in RollerImpl.storePersistentObject()
    */
   public void setData( org.roller.pojos.PersistentObject otherData )
   {
      this.id = ((UserData)otherData).id;
      this.userName = ((UserData)otherData).userName;
      this.password = ((UserData)otherData).password;
      this.fullName = ((UserData)otherData).fullName;
      this.emailAddress = ((UserData)otherData).emailAddress;
      this.dateCreated = ((UserData)otherData).dateCreated;
   }

    /** 
     * Removing a user also removes his/her website.
     * @see org.roller.pojos.PersistentObject#remove()
     */
    public void remove() throws RollerException
    {
        UserManager uMgr = RollerFactory.getRoller().getUserManager();
        uMgr.removeUserWebsites(this);
        
        // remove user roles
        Iterator roles = uMgr.getUserRoles(this).iterator();
        while (roles.hasNext()) 
        {
            ((RoleData)roles.next()).remove();
        }
        
        super.remove();
    }
}
