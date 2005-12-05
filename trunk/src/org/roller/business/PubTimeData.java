package org.roller.business;

/**
 * Pub date bean.
 * @author David M Johnson
 *
 * @castor:class name="PubTime" table="weblogentry" id="id" key-generator="UUID"
 */
public class PubTimeData
   extends org.roller.pojos.PersistentObject
   implements java.io.Serializable
{
   static final long serialVersionUID = -5669288520200229343L;

   protected java.lang.String id;
   protected java.sql.Timestamp pubTime;

   public PubTimeData()
   {
   }

   public PubTimeData( java.lang.String id,java.sql.Timestamp pubTime )
   {
      this.id = id;
      this.pubTime = pubTime;
   }

   public PubTimeData( PubTimeData otherData )
   {
      this.id = otherData.id;
      this.pubTime = otherData.pubTime;

   }

   /** @castor:field set-method="setId"
     * @castor:field-sql name="id" sql-dirty="check" dirty="check"
     * @castor:field-xml node="attribute"
     */
   public java.lang.String getId()
   {
      return this.id;
   }
   public void setId( java.lang.String id )
   {
      this.id = id;
   }

   /** Pub date.
     * @castor:field set-method="setPubTime"
     * @castor:field-sql name="pubtime" sql-dirty="check" dirty="check" 
     */
   public java.sql.Timestamp getPubTime()
   {
      return this.pubTime;
   }
   public void setPubTime( java.sql.Timestamp pubTime )
   {
      this.pubTime = pubTime;
   }

   public String toString()
   {
      StringBuffer str = new StringBuffer("{");

      str.append("id=" + id + " " + "pubTime=" + pubTime);
      str.append('}');

      return(str.toString());
   }

   public boolean equals( Object pOther )
   {
      if( pOther instanceof PubTimeData )
      {
         PubTimeData lTest = (PubTimeData) pOther;
         boolean lEquals = true;

         if( this.id == null )
         {
            lEquals = lEquals && ( lTest.id == null );
         }
         else
         {
            lEquals = lEquals && this.id.equals( lTest.id );
         }
         if( this.pubTime == null )
         {
            lEquals = lEquals && ( lTest.pubTime == null );
         }
         else
         {
            lEquals = lEquals && this.pubTime.equals( lTest.pubTime );
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
      result = 37*result + ((this.pubTime != null) ? this.pubTime.hashCode() : 0);
      return result;
      }

   /**
	* Setter is needed in RollerImpl.storePersistentObject()
    */
   public void setData( org.roller.pojos.PersistentObject otherData )
   {

      this.id = ((PubTimeData)otherData).id;

      this.pubTime = ((PubTimeData)otherData).pubTime;
   }

}
