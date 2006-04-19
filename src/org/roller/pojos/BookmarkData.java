package org.roller.pojos;

import org.roller.RollerException;
import org.roller.model.BookmarkManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;

import java.io.Serializable;

/**
 * <p>Represents a single URL in a user's favorite web-bookmarks collection.
 * Don't construct one of these yourself, instead use the create method in 
 * the your BookmarkManager implementation.</p>
 *
 * @ejb:bean name="BookmarkData"
 * 
 * @struts.form include-all="true"
 *    extends="org.apache.struts.validator.ValidatorForm"
 * 
 * @hibernate.class lazy="false" table="bookmark"
 * @hibernate.cache usage="read-write"
 */
public class BookmarkData extends PersistentObject
    implements Serializable, Comparable
{
    static final long serialVersionUID = 2315131256728236003L;
    
    private FolderData folder;

    private String id = null;
    private String name;
    private String description;
    private String url;
    private Integer weight;
    private Integer priority;
    private String image;
    private String feedUrl;  
    
    private BookmarkManager bookmarkManager = null;

    //----------------------------------------------------------- Constructors
    
    /** Default constructor, for use in form beans only. */
    public BookmarkData()
    {
    }
    
    public BookmarkData(
        FolderData parent,
        String name, 
        String desc, 
        String url, 
        String feedUrl,
        Integer weight, 
        Integer priority, 
        String image)
    {
        this.folder = parent;
        this.name = name;
        this.description = desc;
        this.url = url;
        this.feedUrl = feedUrl;
        this.weight = weight;
        this.priority = priority;
        this.image = image;   
    }

    /** For use by BookmarkManager implementations only. */
    public BookmarkData(BookmarkManager bmgr)
    {
        bookmarkManager = bmgr;
    }

    //------------------------------------------------------------- Attributes
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     *
     * @ejb:persistent-field 
     * 
     * @hibernate.id column="id"
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
     * Name of bookmark.
     * 
     * @roller.wrapPojoMethod type="simple"
     *
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator-args arg0resource="bookmarkForm.name"
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
     * Description of bookmark.
     *
     * @roller.wrapPojoMethod type="simple"
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

    /** 
     * URL of bookmark.
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="url" non-null="true" unique="false"
     */
    public String getUrl()
    {
        return this.url;
    }

    /** @ejb:persistent-field */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /** 
     * Weight indicates prominence of link
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator type="integer" msgkey="errors.integer"
     * @struts.validator-args arg0resource="bookmarkForm.weight"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="weight" non-null="true" unique="false"
     */
    public java.lang.Integer getWeight()
    {
        return this.weight;
    }

    /** @ejb:persistent-field */
    public void setWeight(java.lang.Integer weight)
    {
        this.weight = weight;
    }

    /** 
     * Priority determines order of display 
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @struts.validator type="required" msgkey="errors.required"
     * @struts.validator type="integer" msgkey="errors.integer"
     * @struts.validator-args arg0resource="bookmarkForm.priority"
     * 
     * @ejb:persistent-field 
     * 
     * @hibernate.property column="priority" non-null="true" unique="false"
     */
    public java.lang.Integer getPriority()
    {
        return this.priority;
    }

    /** @ejb:persistent-field */
    public void setPriority(java.lang.Integer priority)
    {
        this.priority = priority;
    }

    /** 
     * @ejb:persistent-field 
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @hibernate.property column="image" non-null="true" unique="false"
     */
    public String getImage()
    {
        return this.image;
    }

    /** @ejb:persistent-field */
    public void setImage(String image)
    {
        this.image = image;
    }

    /** 
     * @ejb:persistent-field 
     *
     * @roller.wrapPojoMethod type="simple"
     * 
     * @hibernate.property column="feedurl" non-null="true" unique="false"
     */
    public String getFeedUrl()
    {
        return this.feedUrl;
    }

    /** @ejb:persistent-field */
    public void setFeedUrl(String feedUrl)
    {
        this.feedUrl = feedUrl;
    }

    //---------------------------------------------------------- Relationships
    
    /** 
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="folderid" cascade="none" not-null="true"
     */
    public org.roller.pojos.FolderData getFolder()
    {
        return this.folder;
    }

    /** @ejb:persistent-field */
    public void setFolder(org.roller.pojos.FolderData folder)
    {
        this.folder = folder;
    }

    //------------------------------------------------------- Good citizenship

    public String toString()
    {
        StringBuffer str = new StringBuffer("{");

        str.append("id=" + id + " " + "name=" + name + " " + "description=" + 
                   description + " " + "url=" + url + " " + "weight=" + 
                   weight + " " + "priority=" + priority + " " + "folderId=" + 
                   "image=" + image + " " + "feedUrl=" + 
                   feedUrl);
        str.append('}');

        return (str.toString());
    }

    public boolean equals(Object pOther)
    {
        if (pOther instanceof BookmarkData)
        {
            BookmarkData lTest = (BookmarkData) pOther;
            boolean lEquals = true;

            if (this.id == null)
            {
                lEquals = lEquals && (lTest.getId() == null);
            }
            else
            {
                lEquals = lEquals && this.id.equals(lTest.getId());
            }

            if (this.name == null)
            {
                lEquals = lEquals && (lTest.getName() == null);
            }
            else
            {
                lEquals = lEquals && this.name.equals(lTest.getName());
            }

            if (this.description == null)
            {
                lEquals = lEquals && (lTest.getDescription() == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.description.equals(lTest.getDescription());
            }

            if (this.url == null)
            {
                lEquals = lEquals && (lTest.getUrl() == null);
            }
            else
            {
                lEquals = lEquals && this.url.equals(lTest.getUrl());
            }

            if (this.weight == null)
            {
                lEquals = lEquals && (lTest.getWeight() == null);
            }
            else
            {
                lEquals = lEquals && this.weight.equals(lTest.getWeight());
            }

            if (this.priority == null)
            {
                lEquals = lEquals && (lTest.getPriority() == null);
            }
            else
            {
                lEquals = lEquals && this.priority.equals(lTest.getPriority());
            }

//            if (this.mFolder == null)
//            {
//                lEquals = lEquals && (lTest.mFolder == null);
//            }
//            else
//            {
//                lEquals = lEquals && this.mFolder.equals(lTest.mFolder);
//            }
//
            if (this.image == null)
            {
                lEquals = lEquals && (lTest.getImage() == null);
            }
            else
            {
                lEquals = lEquals && this.image.equals(lTest.getImage());
            }

            if (this.feedUrl == null)
            {
                lEquals = lEquals && (lTest.getFeedUrl() == null);
            }
            else
            {
                lEquals = lEquals && this.feedUrl.equals(lTest.getFeedUrl());
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
                 ((this.url != null) ? this.url.hashCode() : 0);
        result = (37 * result) + 
                 ((this.weight != null) ? this.weight.hashCode() : 0);
        result = (37 * result) + 
                 ((this.priority != null) ? this.priority.hashCode() : 0);
        result = (37 * result) + 
                 ((this.folder != null) ? this.folder.hashCode() : 0);
        result = (37 * result) + 
                 ((this.image != null) ? this.image.hashCode() : 0);
        result = (37 * result) + 
                 ((this.feedUrl != null) ? this.feedUrl.hashCode() : 0);

        return result;
    }

    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        BookmarkData other = (BookmarkData)otherData;
        this.id = other.getId();
        this.name = other.getName();
        this.description = other.getDescription();
        this.url = other.getUrl();
        this.weight = other.getWeight();
        this.priority = other.getPriority();
        this.folder = other.getFolder();
        this.image = other.getImage();
        this.feedUrl = other.getUrl();
    }

    /** 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object o)
    {
        return bookmarkComparator.compare(this, o);
    }
    
    private BookmarkComparator bookmarkComparator = new BookmarkComparator();

    /**
     * @param impl
     */
    public void setBookmarkManager(BookmarkManager bmgr)
    {
        bookmarkManager = bmgr;
    }

    public WebsiteData getWebsite()
    {
        return this.folder.getWebsite();
    }

}