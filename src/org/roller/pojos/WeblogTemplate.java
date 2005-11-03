package org.roller.pojos;

import java.io.Serializable;
import java.util.Date;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;



/**
 * Pojo that represents a single user defined template page.
 *
 * This template is different from the generic template because it also
 * contains a reference to the website it is part of.
 *
 * @author David M Johnson
 * 
 * @ejb:bean name="WeblogTemplate"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="webpage" hibernate.jcs-cache usage="read-write"
 */
public class WeblogTemplate extends PersistentObject
   implements Serializable, Template
{
   static final long serialVersionUID = -613737191638263428L;

   private java.lang.String id;
   private java.lang.String name;
   private java.lang.String description;
   private java.lang.String link;
   private java.lang.String contents;
   private java.util.Date lastModified;

   private WebsiteData mWebsite = null;

   public WeblogTemplate()
   {
   }

   public WeblogTemplate( 
       java.lang.String id,
       WebsiteData website,
       java.lang.String name,
       java.lang.String description,
       java.lang.String link,
       java.lang.String template,
       java.util.Date updateTime )
   {
      this.id = id;
      this.mWebsite = website;
      this.name = name;
      this.description = description;
      this.link = link;
      this.contents = template;
      this.lastModified = (Date)updateTime.clone();
   }

   public WeblogTemplate( WeblogTemplate otherData )
   {
       setData(otherData);
   }

   /** 
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
    * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
    */
   public WebsiteData getWebsite()
   {
      return this.mWebsite;
   }
   /** @ejb:persistent-field */ 
   public void setWebsite( WebsiteData website )
   {
      this.mWebsite = website;
   }

   /** 
    * @ejb:persistent-field 
    * @hibernate.property column="name" non-null="true" unique="false"
    */
   public java.lang.String getName()
   {
      return this.name;
   }
   /** @ejb:persistent-field */ 
   public void setName( java.lang.String name )
   {
      this.name = name;
   }

   /** 
    * Description
    * @ejb:persistent-field 
    * @hibernate.property column="description" non-null="true" unique="false"
    */
   public java.lang.String getDescription()
   {
      return this.description;
   }
   /** @ejb:persistent-field */ 
   public void setDescription( java.lang.String description )
   {
      this.description = description;
   }

   /** 
    * @ejb:persistent-field 
    * @hibernate.property column="link" non-null="true" unique="false"
    */
   public java.lang.String getLink()
   {
      return this.link;
   }
   /** @ejb:persistent-field */ 
   public void setLink( java.lang.String link )
   {
      this.link = link;
   }

   /** 
    * @ejb:persistent-field 
    * @hibernate.property column="template" non-null="true" unique="false"
    */
   public java.lang.String getContents()
   {
      return this.contents;
   }
   /** @ejb:persistent-field */ 
   public void setContents( java.lang.String template )
   {
      this.contents = template;
   }

   /** 
    * @ejb:persistent-field 
    * @hibernate.property column="updatetime" non-null="true" unique="false"
    */
   public java.util.Date getLastModified()
   {
      return (Date)this.lastModified.clone();
   }   
   /** @ejb:persistent-field */ 
   public void setLastModified(final java.util.Date newtime )
   {
      if (newtime != null)	
      {
      	 lastModified = (Date)newtime.clone();
      }
      else 
      {
      	 lastModified = null;
      }
   }

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + id + " " + "name=" + name + " " + "description=" 
      + description + " " + "link=" + link + " " + "template=" + contents 
      + " " + "updateTime=" + lastModified);
      str.append('}');

      return(str.toString());
   }

   public boolean equals( Object pOther )
   {
      if( pOther instanceof WeblogTemplate )
      {
         WeblogTemplate lTest = (WeblogTemplate) pOther;
         boolean lEquals = true;

         if( this.id == null )
         {
            lEquals = lEquals && ( lTest.id == null );
         }
         else
         {
            lEquals = lEquals && this.id.equals( lTest.id );
         }
         if( this.mWebsite == null )
         {
            lEquals = lEquals && ( lTest.mWebsite == null );
         }
         else
         {
            lEquals = lEquals && this.mWebsite.equals( lTest.mWebsite );
         }
         if( this.name == null )
         {
            lEquals = lEquals && ( lTest.name == null );
         }
         else
         {
            lEquals = lEquals && this.name.equals( lTest.name );
         }
         if( this.description == null )
         {
            lEquals = lEquals && ( lTest.description == null );
         }
         else
         {
            lEquals = lEquals && this.description.equals( lTest.description );
         }
         if( this.link == null )
         {
            lEquals = lEquals && ( lTest.link == null );
         }
         else
         {
            lEquals = lEquals && this.link.equals( lTest.link );
         }
         if( this.contents == null )
         {
            lEquals = lEquals && ( lTest.contents == null );
         }
         else
         {
            lEquals = lEquals && this.contents.equals( lTest.contents );
         }
         if( this.lastModified == null )
         {
            lEquals = lEquals && ( lTest.lastModified == null );
         }
         else
         {
            lEquals = lEquals && this.lastModified.equals( lTest.lastModified );
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
      result = 37*result + ((this.mWebsite != null) ? this.mWebsite.hashCode() : 0);
      result = 37*result + ((this.name != null) ? this.name.hashCode() : 0);
      result = 37*result + ((this.description != null) ? this.description.hashCode() : 0);
      result = 37*result + ((this.link != null) ? this.link.hashCode() : 0);
      result = 37*result + ((this.contents != null) ? this.contents.hashCode() : 0);
      result = 37*result + ((this.lastModified != null) ? this.lastModified.hashCode() : 0);
      return result;
      }

   /**
	* Setter is needed in RollerImpl.storePersistentObject()
    */
   public void setData( org.roller.pojos.PersistentObject otherData )
   {
       WeblogTemplate other = (WeblogTemplate)otherData;
      this.mWebsite =     other.getWebsite();
      this.id =           other.getId();
      this.name =         other.getName();
      this.description =  other.getDescription();
      this.link =         other.getLink();
      this.contents =     other.getContents();
      this.lastModified = other.getLastModified()!=null ? (Date)other.getLastModified().clone() : null;
   }

   public boolean canSave() throws RollerException
   {
       Roller roller = RollerFactory.getRoller();
       if (roller.getUser().equals(UserData.SYSTEM_USER)) 
       {
           return true;
       }
       if (getWebsite().hasUserPermissions(roller.getUser(), PermissionsData.ADMIN))
       {
           return true;
       }
       return false;
   }

}
