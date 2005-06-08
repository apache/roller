
   /**
	* Setter is needed in RollerImpl.storeValueObject()
    */
   public void setData( org.roller.model.ValueObject otherData )
   {
    <XDtEjbPersistent:forAllPersistentFields>
     <XDtEjbDataObj:ifIsAggregate>
      this.<XDtMethod:propertyName/> = new <XDtEjbPersistent:pushClass value="return-type"></XDtEjbPersistent:pushClass><XDtEjbDataObj:dataObjectClass/><XDtEjbPersistent:popClass></XDtEjbPersistent:popClass>( otherData.<XDtMethod:propertyName/> );
     </XDtEjbDataObj:ifIsAggregate>

     <XDtEjbDataObj:ifIsNotAggregate>
      this.<XDtMethod:propertyName/> = ((<XDtClass:classOf><XDtEjbDataObj:dataObjectClass/></XDtClass:classOf>)otherData).<XDtMethod:propertyName/>;
     </XDtEjbDataObj:ifIsNotAggregate>
    </XDtEjbPersistent:forAllPersistentFields>
   }

