package org.roller.pojos;

import java.io.Serializable;
import java.util.Date;


/** Page bean.
 * @author David M Johnson
 *
 * @ejb:bean name="PageData"
 * @struts.form include-all="true"
 * @hibernate.class table="webpage" 
 * hibernate.jcs-cache usage="read-write"
 */
public class PageData extends WebsiteObject implements Serializable
{
   static final long serialVersionUID = -613737191638263428L;

   protected java.lang.String id;
   protected java.lang.String name;
   protected java.lang.String description;
   protected java.lang.String link;
   protected java.lang.String template;
   protected java.util.Date updateTime;

   protected WebsiteData mWebsite = null;

   public PageData()
   {
   }

   public PageData( 
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
      this.template = template;
      this.updateTime = (Date)updateTime.clone();
   }

   public PageData( PageData otherData )
   {
      this.id = otherData.id;
      this.mWebsite = otherData.mWebsite;
      this.name = otherData.name;
      this.description = otherData.description;
      this.link = otherData.link;
      this.template = otherData.template;
      this.updateTime = otherData.updateTime;

   }

   /** 
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
   public java.lang.String getTemplate()
   {
      return this.template;
   }
   /** @ejb:persistent-field */ 
   public void setTemplate( java.lang.String template )
   {
      this.template = template;
   }

   /** 
    * @ejb:persistent-field 
    * @hibernate.property column="updatetime" non-null="true" unique="false"
    */
   public java.util.Date getUpdateTime()
   {
      return (Date)this.updateTime.clone();
   }   
   /** @ejb:persistent-field */ 
   public void setUpdateTime(final java.util.Date newtime )
   {
      if (newtime != null)	
      {
      	 updateTime = (Date)newtime.clone();
      }
      else 
      {
      	 updateTime = null;
      }
   }

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + id + " " + "name=" + name + " " + "description=" 
      + description + " " + "link=" + link + " " + "template=" + template 
      + " " + "updateTime=" + updateTime);
      str.append('}');

      return(str.toString());
   }

   public boolean equals( Object pOther )
   {
      if( pOther instanceof PageData )
      {
         PageData lTest = (PageData) pOther;
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
         if( this.template == null )
         {
            lEquals = lEquals && ( lTest.template == null );
         }
         else
         {
            lEquals = lEquals && this.template.equals( lTest.template );
         }
         if( this.updateTime == null )
         {
            lEquals = lEquals && ( lTest.updateTime == null );
         }
         else
         {
            lEquals = lEquals && this.updateTime.equals( lTest.updateTime );
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
      result = 37*result + ((this.template != null) ? this.template.hashCode() : 0);
      result = 37*result + ((this.updateTime != null) ? this.updateTime.hashCode() : 0);
      return result;
      }

   /**
	* Setter is needed in RollerImpl.storePersistentObject()
    */
   public void setData( org.roller.pojos.PersistentObject otherData )
   {

      this.id = ((PageData)otherData).id;

      this.mWebsite = ((PageData)otherData).mWebsite;

      this.name = ((PageData)otherData).name;

      this.description = ((PageData)otherData).description;

      this.link = ((PageData)otherData).link;

      this.template = ((PageData)otherData).template;

      this.updateTime = ((PageData)otherData).updateTime;
   }

}
