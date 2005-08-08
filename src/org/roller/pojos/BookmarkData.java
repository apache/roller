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
 * @hibernate.class table="bookmark"
 * hibernate.jcs-cache usage="read-write" 
 */
public class BookmarkData extends WebsiteObject
    implements Serializable, Comparable
{
    static final long serialVersionUID = 2315131256728236003L;
    
    protected FolderData folder;

    protected String id = null;
    protected String name;
    protected String description;
    protected String url;
    protected Integer weight;
    protected Integer priority;
    protected String image;
    protected String feedUrl;  
    
    protected BookmarkManager bookmarkManager = null;

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

            if (this.url == null)
            {
                lEquals = lEquals && (lTest.url == null);
            }
            else
            {
                lEquals = lEquals && this.url.equals(lTest.url);
            }

            if (this.weight == null)
            {
                lEquals = lEquals && (lTest.weight == null);
            }
            else
            {
                lEquals = lEquals && this.weight.equals(lTest.weight);
            }

            if (this.priority == null)
            {
                lEquals = lEquals && (lTest.priority == null);
            }
            else
            {
                lEquals = lEquals && this.priority.equals(lTest.priority);
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
                lEquals = lEquals && (lTest.image == null);
            }
            else
            {
                lEquals = lEquals && this.image.equals(lTest.image);
            }

            if (this.feedUrl == null)
            {
                lEquals = lEquals && (lTest.feedUrl == null);
            }
            else
            {
                lEquals = lEquals && this.feedUrl.equals(lTest.feedUrl);
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
        this.id = ((BookmarkData) otherData).id;
        this.name = ((BookmarkData) otherData).name;
        this.description = ((BookmarkData) otherData).description;
        this.url = ((BookmarkData) otherData).url;
        this.weight = ((BookmarkData) otherData).weight;
        this.priority = ((BookmarkData) otherData).priority;
        this.folder = ((BookmarkData) otherData).folder;
        this.image = ((BookmarkData) otherData).image;
        this.feedUrl = ((BookmarkData) otherData).feedUrl;
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