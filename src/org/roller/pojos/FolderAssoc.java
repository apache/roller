package org.roller.pojos;


/**
 * @author David M Johnson
 *
 * @ejb:bean name="FolderAssoc"
 * @hibernate.class lazy="false" table="folderassoc" 
 */
public class FolderAssoc extends PersistentObject
    implements Assoc
{
    static final long serialVersionUID = 882325251670705915L;
    public static final String PARENT = "PARENT";
    public static final String GRANDPARENT = "GRANDPARENT";

    protected String id;
    protected FolderData folder;
    protected FolderData ancestor;
    protected java.lang.String relation;
    
    public FolderAssoc()
    {
    }

    public FolderAssoc(
        String id,
        FolderData folder,
        FolderData ancestor,
        String relation)
    {
        this.id = id;
        this.folder = folder;
        this.ancestor = ancestor;
        this.relation = relation;
    }

    public FolderAssoc(FolderAssoc otherData)
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
        this.folder = ((FolderAssoc)otherData).getFolder();
        this.ancestor = ((FolderAssoc)otherData).getAncestorFolder();
        this.relation = ((FolderAssoc)otherData).getRelation();
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="ancestorid" cascade="none"
     */
    public FolderData getAncestorFolder()
    {
        return ancestor;
    }
    
    /** @ejb:persistent-field */ 
    public void setAncestorFolder(FolderData data)
    {
        ancestor = data;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="folderid" cascade="none" not-null="true"
     */
    public FolderData getFolder()
    {
        return folder;
    }

    /** @ejb:persistent-field */ 
    public void setFolder(FolderData data)
    {
        folder = data;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.property column="relation" non-null="true" unique="false"
     */
    public java.lang.String getRelation()
    {
        return relation;
    }

    /** @ejb:persistent-field */ 
    public void setRelation(java.lang.String string)
    {
        relation = string;
    }

    public HierarchicalPersistentObject getObject()
    {
        return getFolder();
    }

    public void setObject(HierarchicalPersistentObject hpo)
    {
        setFolder((FolderData)hpo);
    }

    public HierarchicalPersistentObject getAncestor()
    {
        return getAncestorFolder();
    }

    public void setAncestor(HierarchicalPersistentObject hpo)
    {
        setAncestorFolder((FolderData)hpo);
    }

}
