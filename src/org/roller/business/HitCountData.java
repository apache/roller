package org.roller.business;

/**
 * Hit count bean.
 * @author David M Johnson
 *
 * @castor:class name="HitCount" table="referer" id="id" key-generator="UUID"
 * @hibernate.class lazy="false" table="referer"
 */
public class HitCountData
   extends org.roller.pojos.PersistentObject
   implements java.io.Serializable
{
   static final long serialVersionUID = -7998453141931097201L;

   protected java.lang.String id;
   protected java.lang.Integer dayHitCount;
   protected java.lang.Integer totalHitCount;
   protected org.roller.pojos.WebsiteData website;

   public HitCountData()
   {
   }

   public HitCountData( java.lang.String id,
        java.lang.Integer dayHitCount, java.lang.Integer totalHitCount )
   {
      this.id = id;
      this.totalHitCount = totalHitCount;
      this.dayHitCount = dayHitCount;
   }

   public HitCountData( HitCountData otherData )
   {
      this.id = otherData.id;
      this.totalHitCount = otherData.totalHitCount;
      this.dayHitCount = otherData.dayHitCount;
   }

   /** @castor:field set-method="setId"
     * @castor:field-sql name="id" sql-dirty="check" dirty="check"
     * @castor:field-xml node="attribute"
     * @hibernate.id column="id" generator-class="uuid.hex" unsaved-value="null"
     */
   public java.lang.String getId()
   {
      return this.id;
   }
   public void setId( java.lang.String id )
   {
      this.id = id;
   }

   /** Day hit count.
     * @castor:field set-method="setDayHitCount"
     * @castor:field-sql name="dayhits" sql-dirty="check" dirty="check" 
     * @hibernate.property column="dayhits" non-null="true" unique="false"
     */
   public java.lang.Integer getDayHitCount()
   {
      return this.dayHitCount;
   }
   public void setDayHitCount( java.lang.Integer dayHitCount )
   {
      this.dayHitCount = dayHitCount;
   }

   /** Total hit count.
     * @castor:field set-method="setTotalHitCount"
     * @castor:field-sql name="totalhits" sql-dirty="check" dirty="check" 
     * @hibernate.property column="totalhits" non-null="true" unique="false"
     */
   public java.lang.Integer getTotalHitCount()
   {
      return this.totalHitCount;
   }   
   public void setTotalHitCount( java.lang.Integer hitCount )
   {
      this.totalHitCount = hitCount;
   }

   /** 
    * ID of website that this referer refers to.
    * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
    */
   public org.roller.pojos.WebsiteData getWebsite()
   {
      return this.website;
   }
   public void setWebsite( org.roller.pojos.WebsiteData website )
   {
      this.website = website;
   }

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + id + " " + "dayHitCount=" + dayHitCount);
      str.append('}');

      return(str.toString());
   }

   public boolean equals( Object pOther )
   {
      if( pOther instanceof HitCountData )
      {
         HitCountData lTest = (HitCountData) pOther;
         boolean lEquals = true;

         if( this.id == null )
         {
            lEquals = lEquals && ( lTest.id == null );
         }
         else
         {
            lEquals = lEquals && this.id.equals( lTest.id );
         }
         if( this.dayHitCount == null )
         {
            lEquals = lEquals && ( lTest.dayHitCount == null );
         }
         else
         {
            lEquals = lEquals && this.dayHitCount.equals( lTest.dayHitCount );
         }
         if( this.totalHitCount == null )
         {
            lEquals = lEquals && ( lTest.totalHitCount == null );
         }
         else
         {
            lEquals= lEquals && this.totalHitCount.equals(lTest.totalHitCount);
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
      result = 37*result + ((this.dayHitCount != null) ? this.dayHitCount.hashCode() : 0);
      result = 37*result + ((this.totalHitCount != null) ? this.totalHitCount.hashCode() : 0);
      return result;
      }

   /**
	* Setter is needed in RollerImpl.storePersistentObject()
    */
   public void setData( org.roller.pojos.PersistentObject otherData )
   {

      this.id = ((HitCountData)otherData).id;

      this.dayHitCount = ((HitCountData)otherData).dayHitCount;
      this.totalHitCount = ((HitCountData)otherData).totalHitCount;
   }

}
