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
package org.roller.pojos;

import org.roller.RollerException;
import org.roller.model.RollerFactory;

/**
 * <p>WeblogCategoryAssoc represents association between weblog categories 
 * in the weblog category hierarchy. For each category, there will be zero 
 * or one parent category association and zero or more grandparent 
 * associations.</p>
 * 
 * <p>Creating a new Cat</p>
 * WeblogManager creates new Cat, not a PO<br />
 * Cat has a parent Cat or null if parent is null. Parent must be PO.<br />
 * On save, CatAssoc will be created for Cat.<br />
 * 
 * <p>Saving an existing Cat</p>
 * If Cat has a new parent Cat, then update all of Cat's CatAssocs<br />
 * 
 * @author David M Johnson
 *
 * @ejb:bean name="WeblogCategoryAssoc"
 * @hibernate.class lazy="false" table="weblogcategoryassoc"
 * @hibernate.cache usage="read-write"
 */
public class WeblogCategoryAssoc extends PersistentObject
    implements Assoc
{
    static final long serialVersionUID = 674856287447472015L;
    
    private String id;
    private WeblogCategoryData category;
    private WeblogCategoryData ancestor;
    private java.lang.String relation;
    
    public WeblogCategoryAssoc()
    {
    }

    public WeblogCategoryAssoc(
        String id,
        WeblogCategoryData category,
        WeblogCategoryData ancestor,
        java.lang.String relation)
    {
        this.id = id;
        this.category = category;
        this.ancestor = ancestor;
        this.relation = relation;
    }

    public WeblogCategoryAssoc(WeblogCategoryAssoc otherData)
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
        this.category = ((WeblogCategoryAssoc)otherData).getCategory();
        this.ancestor = ((WeblogCategoryAssoc)otherData).getAncestorCategory();
        this.relation = ((WeblogCategoryAssoc)otherData).getRelation();
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="ancestorid" cascade="none"
     */
    public WeblogCategoryData getAncestorCategory()
    {
        return ancestor;
    }
    
    /** @ejb:persistent-field */ 
    public void setAncestorCategory(WeblogCategoryData data)
    {
        ancestor = data;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="categoryid" cascade="none" not-null="true"
     */
    public WeblogCategoryData getCategory()
    {
        return category;
    }

    /** @ejb:persistent-field */ 
    public void setCategory(WeblogCategoryData data)
    {
        category = data;
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
        return getCategory();
    }

    public void setObject(HierarchicalPersistentObject hpo)
    {
        setCategory((WeblogCategoryData)hpo);
    }

    public HierarchicalPersistentObject getAncestor()
    {
        return getAncestorCategory();
    }

    public void setAncestor(HierarchicalPersistentObject hpo)
    {
        setAncestorCategory((WeblogCategoryData)hpo);
    }
}
