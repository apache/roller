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
 * @hibernate.class table="weblogcategory" 
 * hibernate.jcs-cache usage="read-write"
 */
public class WeblogCategoryData extends HierarchicalPersistentObject
{
    static final long serialVersionUID = 1435782148712018954L;

    protected java.lang.String id = null;
    protected java.lang.String name;
    protected java.lang.String description;
    protected java.lang.String image;
    
    protected String cachedPath = null;

    protected WebsiteData mWebsite;
    protected List mWeblogCategories;

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
        this.mWebsite = website;
        this.mNewParent = parent;
        this.name = name;
        this.description = description;
        this.image = image;
    }

    public WeblogCategoryData(WeblogCategoryData otherData)
    {
        this.id = otherData.id;
        this.mWebsite = otherData.mWebsite;
        this.mNewParent = otherData.mNewParent;
        this.name = otherData.name;
        this.description = otherData.description;
        this.image = otherData.image;
    }

    /** Setter is needed in RollerImpl.storePersistentObject(). */
    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        this.id = ((WeblogCategoryData) otherData).id;
        this.mWebsite = ((WeblogCategoryData) otherData).mWebsite;
        this.mNewParent = ((WeblogCategoryData) otherData).mNewParent;
        this.name = ((WeblogCategoryData) otherData).name;
        this.description = ((WeblogCategoryData) otherData).description;
        this.image = ((WeblogCategoryData) otherData).image;
    }

    public void save() throws RollerException
    {   
        if (RollerFactory.getRoller().getWeblogManager().isDuplicateWeblogCategoryName(this))
        {
            throw new RollerException("Duplicate category name");
        }
        super.save();
    }
   
    /**
     * Remove this category and recategorize all entries in this category and
     * in all subcategories to a specified destination category (destCat).
     * @param destCat New category for entries in remove categories (or null if none).
     */
    public void remove(WeblogCategoryData destCat) throws RollerException
    {
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        
        // recategorize entries in this category
        if (destCat != null) 
        {
            wmgr.moveWeblogCategoryContents(getId(), destCat.getId());
        }
        // delete this category
        super.remove();
        
        if (getWebsite().getBloggerCategory().equals(this))
        {
            WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(getWebsite());
            getWebsite().setBloggerCategory(rootCat);
        }
        
        if (getWebsite().getDefaultCategory().equals(this))
        {
            WeblogCategoryData rootCat = wmgr.getRootWeblogCategory(getWebsite());
            getWebsite().setDefaultCategory(rootCat);
        }
        
        getWebsite().save();
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
     */
    public String getObjectPropertyName()
    {
        return "category";
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAncestorPropertyName()
     */
    public String getAncestorPropertyName()
    {
        return "ancestorCategory";
    }
    
    //------------------------------------------------------- Simple properties

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
    public void setId(java.lang.String id)
    {
        this.id = id;
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
    public void setName(java.lang.String name)
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
    public void setDescription(java.lang.String description)
    {
        this.description = description;
    }

    /** 
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
     * @ejb:persistent-field
     *  
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite()
    {
        return mWebsite;
    }
    /** @ejb:persistent-field */
    public void setWebsite(WebsiteData website)
    {
        mWebsite = website;
    }

//    /** 
//     * @ejb:persistent-field
//     *  
//     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
//     */
//    public WeblogCategoryAssoc getWeblogCategoryAssoc()
//    {
//        return mWeblogCategoryAssoc;
//    }
//    /** @ejb:persistent-field */
//    public void setWeblogCategoryAssoc(WebsiteData website)
//    {
//        WeblogCategoryAssoc = weblogCategoryAssoc;
//    }

    /** Return parent category, or null if category is root of hierarchy. */
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

    /** Query to get child categories of this category. */
    public List getWeblogCategories() throws RollerException
    {
        if (mWeblogCategories == null)
        {
            mWeblogCategories = new LinkedList();
            List childAssocs = getChildAssocs();
            Iterator childIter = childAssocs.iterator();
            while (childIter.hasNext())
            {
                WeblogCategoryAssoc assoc =
                    (WeblogCategoryAssoc) childIter.next();
                mWeblogCategories.add(assoc.getCategory());
            }
        }
        return mWeblogCategories;
    }

    public boolean descendentOf(WeblogCategoryData ancestor) 
        throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().isDescendentOf(this, ancestor);
    }
    
    /** 
     * Determine if category is in use. Returns true if any weblog entries 
     * use this category or any of it's subcategories.
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
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        return wmgr.createWeblogCategoryAssoc(
            (WeblogCategoryData)object, 
            (WeblogCategoryData)associatedObject, 
            relation);
    }

    //------------------------------------------------------------------------
    
    /** 
     * Move all weblog entries that exist in this category and all
     * subcategories of this category to a single new category.
     */ 
    public void moveContents(WeblogCategoryData dest) throws RollerException
    {
        Iterator entries = retrieveWeblogEntries(true).iterator();
        while (entries.hasNext())
        {
            WeblogEntryData entry = (WeblogEntryData) entries.next();
            entry.setCategory(dest);
            entry.save();
        }
    }
    
    /** 
     * Retrieve all weblog entries in this category and, optionally, include
     * weblog entries all sub-categories.
     * @param subcats True if entries from sub-categories are to be returned.
     * @return List of WeblogEntryData objects.
     * @throws RollerException
     */ 
    public List retrieveWeblogEntries(boolean subcats) 
        throws RollerException
    {
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        return wmgr.retrieveWeblogEntries(this, subcats);
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
            lEquals = PojoUtil.equals(lEquals, this.id, lTest.id);
            lEquals = PojoUtil.equals(lEquals, this.mWebsite.getId(), lTest.mWebsite.getId());
            lEquals = PojoUtil.equals(lEquals, this.name, lTest.name);
            lEquals = PojoUtil.equals(lEquals, this.description, lTest.description);
            lEquals = PojoUtil.equals(lEquals, this.image, lTest.image);
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
                + ((this.mWebsite != null) ? this.mWebsite.hashCode() : 0);
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
    protected Assoc getParentAssoc() throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().getWeblogCategoryParentAssoc(this);
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getChildAssocs()
     */
    protected List getChildAssocs() throws RollerException
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
