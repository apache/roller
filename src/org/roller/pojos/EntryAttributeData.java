package org.roller.pojos;
/**
 * @author David M Johnson
 * @ejb:bean name="EntryAttribute"
 * @hibernate.class lazy="false" table="entryattribute" 
 */
public class EntryAttributeData extends PersistentObject implements java.lang.Comparable
{
    protected String id;
    protected WeblogEntryData entry;
    protected String name;
    protected String value;
    
    public EntryAttributeData()
    {
    }

    public EntryAttributeData(
        String id,
        WeblogEntryData entry,
        String name,
        String value)
    {
        this.id = id;
        this.entry = entry;
        this.name = name;
        this.value = value;
    }

    public EntryAttributeData(EntryAttributeData otherData)
    {
        this.id = otherData.id;
        this.entry = otherData.entry;
        this.name = otherData.name;
        this.value = otherData.value;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.id column="id" 
     *    generator-class="uuid.hex" unsaved-value="null"
     */
    public java.lang.String getId()
    {
        return this.id;
    }
    /** @ejb:persistent-field */
    public void setId(java.lang.String id)
    {
        this.id = id;
    }

    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        this.id = otherData.getId();
        this.entry = ((EntryAttributeData) otherData).entry;
        this.name = ((EntryAttributeData) otherData).name;
        this.value = ((EntryAttributeData) otherData).value;
    }

    /** 
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="entryid" cascade="none" not-null="true"
     */
    public WeblogEntryData getEntry()
    {
        return entry;
    }
    /** @ejb:persistent-field */ 
    public void setEntry(WeblogEntryData entry)
    {
        this.entry = entry;
    }

    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName()
    {
        return name;
    }
    /** @ejb:persistent-field */ 
    public void setName(String name)
    {
        this.name = name;
    }
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="value" non-null="true" unique="false"
     */
    public String getValue()
    {
        return value;
    }
    /** @ejb:persistent-field */ 
    public void setValue(String value)
    {
        this.value = value;
    }

    public int compareTo(Object o) {
        EntryAttributeData att = (EntryAttributeData)o;
        return getName().compareTo(att.getName());
    }
}
