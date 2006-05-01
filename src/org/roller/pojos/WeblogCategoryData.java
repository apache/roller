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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.util.PojoUtil;

/**
 * WeblogCategory bean.
 * @author David M Johnson
 *
 * @ejb:bean name="WeblogCategoryData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false" table="weblogcategory"
 * @hibernate.cache usage="read-write"
 */
public class WeblogCategoryData extends HierarchicalPersistentObject
{
    public static final long serialVersionUID = 1435782148712018954L;

    private String id = null;
    private String name = null;
    private String description = null;
    private String image = null;
    
    private String cachedPath = null;

    private WebsiteData website = null;
    private List weblogCategories = null;
    

    public WeblogCategoryData()
    {
    }

    public WeblogCategoryData(
        java.lang.String id,
        WebsiteData website,
        WeblogCategoryData parent,
        java.lang.String name,
        java.lang.String description,
        java.lang.String image)
    {
        this.id = id;
        this.website = website;
        this.mNewParent = parent;
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public WeblogCategoryData(WeblogCategoryData otherData)
    {
        this.setData(otherData);
    }

    /** Setter is needed in RollerImpl.storePersistentObject(). */
    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        WeblogCategoryData other = (WeblogCategoryData) otherData;
        
        this.id = other.getId();
        this.website = other.getWebsite();
        this.name = other.getName();
        this.description = other.getDescription();
        this.image = other.getImage();
        
        try {
            this.mNewParent = other.getParent();
        } catch(RollerException re) {
            // why does this throw an exception?
        }
    }
    
    
    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAssocClass()
     */
    public Class getAssocClass()
    {
        return WeblogCategoryAssoc.class;
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getObjectPropertyName()
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getObjectPropertyName()
    {
        return "category";
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAncestorPropertyName()
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getAncestorPropertyName()
    {
        return "ancestorCategory";
    }
    
    //------------------------------------------------------- Simple properties

    /**
     * @roller.wrapPojoMethod type="simple"
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
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public java.lang.String getName()
    {
        return this.name;
    }
    /** @ejb:persistent-field */
    public void setName(java.lang.String name)
    {
        this.name = name;
    }

    /** 
     * Description
     * 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public java.lang.String getDescription()
    {
        return this.description;
    }
    /** @ejb:persistent-field */
    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }

    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="image" non-null="true" unique="false"
     */
    public java.lang.String getImage()
    {
        return this.image;
    }
    /** @ejb:persistent-field */
    public void setImage(java.lang.String image)
    {
        this.image = image;
    }

    /**
     * Get path in category hierarhcy.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public String getPath()
    {
        if (null == cachedPath)
        {
            try
            {
                cachedPath = RollerFactory.getRoller().getWeblogManager().getPath(this);
            }
            catch (RollerException e)
            {
                throw new RuntimeException(e);
            }
        }
        return cachedPath;
    }

    //------------------------------------------------------------ Associations

    /** 
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     *  
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite()
    {
        return website;
    }
    /** @ejb:persistent-field */
    public void setWebsite(WebsiteData website)
    {
        this.website = website;
    }


    /** 
     * Return parent category, or null if category is root of hierarchy.
     *
     * @roller.wrapPojoMethod type="pojo"
     */
    public WeblogCategoryData getParent() throws RollerException
    {
        if (mNewParent != null)
        {
            // Category has new parent, so return that
            return (WeblogCategoryData)mNewParent;
        }
        else if (getParentAssoc() != null)
        {
            // Return parent found in database
            return ((WeblogCategoryAssoc)getParentAssoc()).getAncestorCategory();
        }
        else 
        {
            return null;
        }
    }

    /** Set parent category, database will be updated when object is saved. */
    public void setParent(HierarchicalPersistentObject parent)
    {
        mNewParent = parent;
    }

    /**
     * Query to get child categories of this category.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.roller.pojos.WeblogCategoryData"
     */
    public List getWeblogCategories() throws RollerException
    {
        if (weblogCategories == null)
        {
            weblogCategories = new LinkedList();
            List childAssocs = getChildAssocs();
            Iterator childIter = childAssocs.iterator();
            while (childIter.hasNext())
            {
                WeblogCategoryAssoc assoc =
                    (WeblogCategoryAssoc) childIter.next();
                weblogCategories.add(assoc.getCategory());
            }
        }
        return weblogCategories;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean descendentOf(WeblogCategoryData ancestor) 
        throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().isDescendentOf(this, ancestor);
    }
    
    /** 
     * Determine if category is in use. Returns true if any weblog entries 
     * use this category or any of it's subcategories.
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public boolean isInUse() 
    {
        try
        {
            return RollerFactory.getRoller().getWeblogManager().isWeblogCategoryInUse(this);
        }
        catch (RollerException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    /** TODO: fix form generation so this is not needed. */
    public void setInUse(boolean dummy) {}

    //------------------------------------------------------------------------

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#createAssoc(
     * org.roller.pojos.HierarchicalPersistentObject, 
     * org.roller.pojos.HierarchicalPersistentObject, java.lang.String)
     */
    public Assoc createAssoc(
        HierarchicalPersistentObject object, 
        HierarchicalPersistentObject associatedObject, 
        String relation) throws RollerException
    {
        return new WeblogCategoryAssoc(null,
                (WeblogCategoryData)object,
                (WeblogCategoryData)associatedObject,
                relation);
    }
    
    
    /** 
     * Retrieve all weblog entries in this category and, optionally, include
     * weblog entries all sub-categories.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.roller.pojos.WeblogEntryData"
     *
     * @param subcats True if entries from sub-categories are to be returned.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */ 
    public List retrieveWeblogEntries(boolean subcats) 
        throws RollerException
    {
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        return wmgr.getWeblogEntries(this, subcats);
    }

    //-------------------------------------------------------- Good citizenship

    public String toString()
    {
        StringBuffer str = new StringBuffer("{");

        str.append(
            "id="
                + id
                + " "
                + "name="
                + name
                + " "
                + "description="
                + description
                + " "
                + "image="
                + image);
        str.append('}');

        return (str.toString());
    }

    public boolean equals(Object pOther)
    {
        if (pOther == null) return false;
        if (pOther instanceof WeblogCategoryData)
        {
            WeblogCategoryData lTest = (WeblogCategoryData) pOther;
            boolean lEquals = true;
            lEquals = PojoUtil.equals(lEquals, this.getId(), lTest.getId());
            lEquals = PojoUtil.equals(lEquals, this.getName(), lTest.getName());
            lEquals = PojoUtil.equals(lEquals, this.getDescription(), lTest.getDescription());
            lEquals = PojoUtil.equals(lEquals, this.getImage(), lTest.getImage());
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
        result = 37 * result + ((this.id != null) ? this.id.hashCode() : 0);
        result =
            37 * result
                + ((this.website != null) ? this.website.hashCode() : 0);
        result = 37 * result + ((this.name != null) ? this.name.hashCode() : 0);
        result =
            37 * result
                + ((this.description != null) ? this.description.hashCode() : 0);
        result =
            37 * result + ((this.image != null) ? this.image.hashCode() : 0);
        return result;
    }
    
    /** TODO: fix Struts form generation template so this is not needed. */
    public void setAssocClassName(String dummy) {};
    
    /** TODO: fix Struts form generation template so this is not needed. */
    public void setObjectPropertyName(String dummy) {};
    
    /** TODO: fix Struts form generation template so this is not needed. */
    public void setAncestorPropertyName(String dummy) {};
    
    /** TODO: fix formbean generation so this is not needed. */
    public void setPath(String string) {}

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getParentAssoc()
     */
    public Assoc getParentAssoc() throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().getWeblogCategoryParentAssoc(this);
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getChildAssocs()
     */
    public List getChildAssocs() throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().getWeblogCategoryChildAssocs(this);
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAllDescendentAssocs()
     */
    public List getAllDescendentAssocs() throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().getAllWeblogCategoryDecscendentAssocs(this);
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAncestorAssocs()
     */
    public List getAncestorAssocs() throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().getWeblogCategoryAncestorAssocs(this);
    }

}
