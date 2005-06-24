package org.roller.pojos;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.roller.RollerException;
import org.roller.business.PersistenceStrategy;
import org.roller.model.BookmarkManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;

/**
 * <p>Folder that holds Bookmarks and other Folders. A Roller Website has a 
 * set of Folders (there is no one root folder) and each Folder may contain 
 * Folders or Bookmarks. Don't construct one of these yourself, instead use
 * the create method in your BookmarkManager implementation.</p>
 *
 * @struts.form include-all="true"
 *    extends="org.apache.struts.validator.ValidatorForm"
 * @ejb:bean name="FolderData"
 * 
 * @hibernate.class table="folder"
 * hibernate.jcs-cache usage="read-write"
 */
public class FolderData extends HierarchicalPersistentObject
    implements Serializable, Comparable
{
    static final long serialVersionUID = -6272468884763861944L;
    
    protected Set bookmarks = new TreeSet();
    protected List folders = null;
    protected WebsiteData website;
    
    protected String id;
    protected String name;
    protected String description;
    protected String path;
    
    //----------------------------------------------------------- Constructors
    
    /** For use by BookmarkManager implementations only. */
    public FolderData()
    {
    }
    
    public FolderData(
        FolderData parent,
        String name, 
        String desc, 
        WebsiteData website)
    {
        mNewParent = parent;
        this.name = name;
        this.description = desc;
        this.website = website;
    }

    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        mNewParent = ((FolderData) otherData).mNewParent;
        this.bookmarks = ((FolderData) otherData).bookmarks;
        this.id = ((FolderData) otherData).id;
        this.name = ((FolderData) otherData).name;
        this.description = ((FolderData) otherData).description;
        this.website = ((FolderData) otherData).website;
    }

    public void save() throws RollerException
    {   
        if (RollerFactory.getRoller().getBookmarkManager().isDuplicateFolderName(this))
        {
            throw new RollerException("Duplicate folder name");
        }
        super.save(); 
    }
    
    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAssocClass()
     */
    public Class getAssocClass()
    {
        return FolderAssoc.class;
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getObjectPropertyName()
     */
    public String getObjectPropertyName()
    {
        return "folder";
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAncestorPropertyName()
     */
    public String getAncestorPropertyName()
    {
        return "ancestorFolder";
    }

    public boolean isInUse()
    {
        try
        {
            return RollerFactory.getRoller().getBookmarkManager().isFolderInUse(this); 
        }
        catch (RollerException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    public boolean descendentOf(FolderData ancestor) 
        throws RollerException
    {
        return RollerFactory.getRoller().getBookmarkManager().isDescendentOf(this, ancestor);
    }

    //------------------------------------------------------------- Attributes
    
    /** 
     * @ejb:persistent-field 
     * 
     * @hibernate.id column="id" type="string"
     *     generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId()
    {
        return this.id;
    }

    /** @ejb:persistent-field */
    public void setId(String id)
    {
        this.id = id;
    }

    /** 
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator type="mask" msgkey="errors.noslashes"
     * @struts.validator-var name="mask" value="${noslashes}"
     * @struts.validator-args arg0resource="folderForm.name"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName()
    {
        return this.name;
    }

    /** @ejb:persistent-field */
    public void setName(String name)
    {
        this.name = name;
    }

    /** 
     * Description
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public String getDescription()
    {
        return this.description;
    }

    /** @ejb:persistent-field */
    public void setDescription(String description)
    {
        this.description = description;
    }

    //---------------------------------------------------------- Relationships
    
    /** Get path to this bookmark folder. */
    public String getPath() throws RollerException
    {
        if (mNewParent != null) 
        {
            throw new RollerException(
                "Folder has a new parent and must be saved before getPath() will work");
        }
        
        if (null == path)
        {
            path = RollerFactory.getRoller().getBookmarkManager().getPath(this);
        }
        return path;
    }
        
    /** 
     * @ejb:persistent-field 
     * 
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite()
    {
        return website;
    }

    /** @ejb:persistent-field */
    public void setWebsite( WebsiteData website )
    {
        this.website = website;
    }

    /** Return parent category, or null if category is root of hierarchy. */
    public FolderData getParent() throws RollerException
    {
        if (mNewParent != null)
        {
            // Category has new parent, so return that
            return (FolderData)mNewParent;
        }
        else if (getParentAssoc() != null)
        {
            // Return parent found in database
            return ((FolderAssoc)getParentAssoc()).getAncestorFolder();
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
    public List getFolders() throws RollerException
    {
        if (folders == null)
        {
            folders = new LinkedList();
            List childAssocs = getChildAssocs();
            Iterator childIter = childAssocs.iterator();
            while (childIter.hasNext())
            {
                FolderAssoc assoc =
                    (FolderAssoc) childIter.next();
                folders.add(assoc.getFolder());
            }
        }
        return folders;
    }

    //------------------------------------------------------ Bookmark children
    
    /** 
      * @ejb:persistent-field
     * 
     * @hibernate.set lazy="true" order-by="name" inverse="true" cascade="delete" 
     * @hibernate.collection-key column="folderid" 
     * @hibernate.collection-one-to-many class="org.roller.pojos.BookmarkData"
     */
    public Set getBookmarks()    
    {
        return this.bookmarks;
    }

    /** @ejb:persistent-field */
    public void setBookmarks(Set bookmarks)
    {
        this.bookmarks = bookmarks;
    }
    
    /** Store bookmark and add to folder */
    public void addBookmark(BookmarkData bookmark) throws RollerException
    {
        bookmark.setFolder(this);
        bookmarks.add(bookmark);  
        bookmark.save();      
    }

    /** Remove boomkark from folder */
    public void removeBookmark(BookmarkData bookmark)
    {
        bookmarks.remove(bookmark);
        bookmark.setFolder(null);
    }

    /**
     * @param subfolders
     */
    public List retrieveBookmarks(boolean subfolders) throws RollerException
    {
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        return bmgr.retrieveBookmarks(this, subfolders);
    }

    /** 
     * Move all bookmarks that exist in this folder and all
     * subfolders of this folder to a single new folder.
     */ 
    public void moveContents(FolderData dest) throws RollerException
    {
        Iterator entries = retrieveBookmarks(true).iterator();
        while (entries.hasNext())
        {
            BookmarkData bookmark = (BookmarkData) entries.next();
            bookmark.setFolder(dest);
            bookmark.save();
        }
    }

    //------------------------------------------------------------------------

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#createAssoc(
     *    org.roller.pojos.HierarchicalPersistentObject, 
     *    org.roller.pojos.HierarchicalPersistentObject, java.lang.String)
     */
    public Assoc createAssoc(
        HierarchicalPersistentObject object, 
        HierarchicalPersistentObject associatedObject, 
        String relation) throws RollerException
    {
        BookmarkManager bmgr = RollerFactory.getRoller().getBookmarkManager();
        return bmgr.createFolderAssoc(
            (FolderData)object, 
            (FolderData)associatedObject, 
            relation);
    }

    //------------------------------------------------------- Good citizenship

    public String toString()
    {
        StringBuffer str = new StringBuffer("{");
        str.append(
              "bookmarks=" + bookmarks + " " 
            + "id=" + id + " " 
            + "name=" + name + " " 
            + "description=" + description);
        str.append('}');
        return (str.toString());
    }

    public boolean equals(Object pOther)
    {
        if (pOther instanceof FolderData)
        {
            FolderData lTest = (FolderData) pOther;
            boolean lEquals = true;

//            if (this.bookmarks == null)
//            {
//                lEquals = lEquals && (lTest.bookmarks == null);
//            }
//            else
//            {
//                lEquals = lEquals && this.bookmarks.equals(lTest.bookmarks);
//            }

            if (this.id == null)
            {
                lEquals = lEquals && (lTest.id == null);
            }
            else
            {
                lEquals = lEquals && this.id.equals(lTest.id);
            }

            if (this.name == null)
            {
                lEquals = lEquals && (lTest.name == null);
            }
            else
            {
                lEquals = lEquals && this.name.equals(lTest.name);
            }

            if (this.description == null)
            {
                lEquals = lEquals && (lTest.description == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.description.equals(lTest.description);
            }

            if (this.website == null)
            {
                lEquals = lEquals && (lTest.website == null);
            }
            else
            {
                lEquals = lEquals && this.website.equals(lTest.website);
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
                         
        result = (37 * result) + 
                 ((this.id != null) ? this.id.hashCode() : 0);
        result = (37 * result) + 
                 ((this.name != null) ? this.name.hashCode() : 0);
        result = (37 * result) + 
                 ((this.description != null) ? this.description.hashCode() : 0);
        result = (37 * result) + 
                 ((this.website != null) ? this.website.hashCode() : 0);

        return result;
    }

    /** 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        FolderData other = (FolderData)o;
        return getName().compareTo(other.getName());
    }

    /** TODO: fix Struts form generation template so this is not needed. */
    public void setAssocClassName(String dummy) {};
    /** TODO: fix Struts form generation template so this is not needed. */
    public void setObjectPropertyName(String dummy) {};
    /** TODO: fix Struts form generation template so this is not needed. */
    public void setAncestorPropertyName(String dummy) {};
    /** TODO: fix formbean generation so this is not needed. */
    public void setPath(String string) {}
    /** TODO: fix formbean generation so this is not needed. */
    public void setInUse(boolean flag) {}

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getParentAssoc()
     */
    protected Assoc getParentAssoc() throws RollerException
    {
        return RollerFactory.getRoller().getBookmarkManager().getFolderParentAssoc(this);
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getChildAssocs()
     */
    protected List getChildAssocs() throws RollerException
    {
        return RollerFactory.getRoller().getBookmarkManager().getFolderChildAssocs(this);
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAllDescendentAssocs()
     */
    public List getAllDescendentAssocs() throws RollerException
    {
        return RollerFactory.getRoller().getBookmarkManager().getAllFolderDecscendentAssocs(this);
    }

    /** 
     * @see org.roller.pojos.HierarchicalPersistentObject#getAncestorAssocs()
     */
    public List getAncestorAssocs() throws RollerException
    {
        return RollerFactory.getRoller().getBookmarkManager().getFolderAncestorAssocs(this);
    }
    
    protected void removeDescendent(
            PersistenceStrategy pstrategy, PersistentObject po)  throws RollerException
    {
        /*FolderData folder = (FolderData)po;
        Iterator bookmarks = folder.getBookmarks().iterator();
        while (bookmarks.hasNext()) {
            bookmarks.remove();
        }*/
        pstrategy.remove(po);
    }

}
