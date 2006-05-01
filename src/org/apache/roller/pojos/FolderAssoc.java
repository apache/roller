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
 * @author David M Johnson
 *
 * @ejb:bean name="FolderAssoc"
 * @hibernate.class lazy="false" table="folderassoc"
 * @hibernate.cache usage="read-write"
 */
public class FolderAssoc extends PersistentObject
    implements Assoc
{
    static final long serialVersionUID = 882325251670705915L;
    public static final String PARENT = "PARENT";
    public static final String GRANDPARENT = "GRANDPARENT";

    private String id;
    private FolderData folder;
    private FolderData ancestor;
    private java.lang.String relation;
    
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
    public void setData(org.apache.roller.pojos.PersistentObject otherData)
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
