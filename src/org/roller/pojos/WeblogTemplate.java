package org.roller.pojos;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
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
 * @hibernate.class lazy="false" table="webpage"
 * @hibernate.cache usage="read-write"
 */
public class WeblogTemplate extends PersistentObject
   implements Serializable, Template
{
   public static final long serialVersionUID = -613737191638263428L;

   public static final String DEFAULT_PAGE = "Weblog";
   
   private static Set requiredTemplates = null;
   
   static {
       requiredTemplates = new HashSet();
       requiredTemplates.add("Weblog");
       requiredTemplates.add("_day");
       requiredTemplates.add("_css");
       requiredTemplates.add("_decorator");
   }
   
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
    * Determine if this WeblogTemplate is required or not.
    */
   public boolean isRequired() {
       /* 
        * this is kind of hacky right now, but it's like that so we can be
        * reasonably flexible while we migrate old blogs which may have some
        * pretty strange customizations.
        *
        * my main goal starting now is to prevent further deviations from the
        * standardized templates as we move forward.
        *
        * eventually, the required flag should probably be stored in the db
        * and possibly applicable to any template.
        */
       return (requiredTemplates.contains(this.name) || "Weblog".equals(this.link));
   }

   
   public void setRequired(boolean req) {
       // this is an absurd workaround for our struts formbean generation stuff
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
            lEquals = lEquals && ( lTest.getId() == null );
         }
         else
         {
            lEquals = lEquals && this.id.equals( lTest.getId() );
         }
         if( this.mWebsite == null )
         {
            lEquals = lEquals && ( lTest.getWebsite() == null );
         }
         else
         {
            lEquals = lEquals && this.mWebsite.equals( lTest.getWebsite() );
         }
         if( this.name == null )
         {
            lEquals = lEquals && ( lTest.getName() == null );
         }
         else
         {
            lEquals = lEquals && this.name.equals( lTest.getName() );
         }
         if( this.description == null )
         {
            lEquals = lEquals && ( lTest.getDescription() == null );
         }
         else
         {
            lEquals = lEquals && this.description.equals( lTest.getDescription() );
         }
         if( this.link == null )
         {
            lEquals = lEquals && ( lTest.getLink() == null );
         }
         else
         {
            lEquals = lEquals && this.link.equals( lTest.getLink() );
         }
         if( this.contents == null )
         {
            lEquals = lEquals && ( lTest.getContents() == null );
         }
         else
         {
            lEquals = lEquals && this.contents.equals( lTest.getContents() );
         }
         if( this.lastModified == null )
         {
            lEquals = lEquals && ( lTest.getLastModified() == null );
         }
         else
         {
            lEquals = lEquals && this.lastModified.equals( lTest.getLastModified() );
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


}
