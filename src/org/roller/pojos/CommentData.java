package org.roller.pojos;

import org.roller.util.PojoUtil;

/**
 * Weblogentry Comment bean.
 * @author Lance Lavandowska
 *
 * @ejb:bean name="CommentData"
 * @struts.form include-all="true"
 * 
 * @hibernate.class lazy="false" table="comment"  
 */
public class CommentData extends org.roller.pojos.PersistentObject
    implements java.io.Serializable
{
    static final long serialVersionUID = -6668122596726478462L;
    protected java.lang.String id;
    protected java.lang.String name;
    protected java.lang.String email;
    protected java.lang.String url;
    protected java.lang.String content;
    protected java.sql.Timestamp postTime;
    protected WeblogEntryData mWeblogEntry;
    protected Boolean spam;
    protected Boolean notify;
	protected String remoteHost;

    public CommentData()
    {
        spam = Boolean.FALSE;
    }

    public CommentData(java.lang.String id, WeblogEntryData entry, 
                       java.lang.String name, java.lang.String email, 
                       java.lang.String url, java.lang.String content, 
                       java.sql.Timestamp postTime, 
                       Boolean spam, Boolean notify)
    {
        this.id = id;
        this.name = name;
        this.email = email;
        this.url = url;
        this.content = content;
        this.postTime = postTime;
        this.spam = spam;
        this.notify = notify;

        mWeblogEntry = entry;
    }

    public CommentData(CommentData otherData)
    {
        this.id = otherData.id;
        this.name = otherData.name;
        this.email = otherData.email;
        this.url = otherData.url;
        this.content = otherData.content;
        this.postTime = otherData.postTime;
        this.spam = otherData.spam;
        this.notify = otherData.notify;
        
        mWeblogEntry = otherData.mWeblogEntry;
    }

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
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="entryid" cascade="none" not-null="true"
     */
    public WeblogEntryData getWeblogEntry()
    {
        return mWeblogEntry;
    }

    /** @ejb:persistent-field */
    public void setWeblogEntry(WeblogEntryData entry)
    {
        mWeblogEntry = entry;
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
     * Email
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="email" non-null="true" unique="false"
     */
    public java.lang.String getEmail()
    {
        return this.email;
    }

    /** @ejb:persistent-field */
    public void setEmail(java.lang.String email)
    {
        this.email = email;
    }

    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="url" non-null="true" unique="false"
     */
    public java.lang.String getUrl()
    {
        return this.url;
    }

    /** @ejb:persistent-field */
    public void setUrl(java.lang.String url)
    {
        this.url = url;
    }

    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="content" non-null="true" unique="false"
     */
    public java.lang.String getContent()
    {
        return this.content;
    }

    /** @ejb:persistent-field */
    public void setContent(java.lang.String content)
    {
        this.content = content;
    }

    /** 
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="posttime" non-null="true" unique="false"
     */
    public java.sql.Timestamp getPostTime()
    {
        return this.postTime;
    }

    /** @ejb:persistent-field */
    public void setPostTime(java.sql.Timestamp postTime)
    {
        this.postTime = postTime;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="spam" non-null="false" unique="false"
     */
    public Boolean getSpam()
    {
        return this.spam;
    }

    /** @ejb:persistent-field */
    public void setSpam(Boolean spam)
    {
        this.spam = spam;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="notify" non-null="false" unique="false"
     */
    public Boolean getNotify()
    {
        return this.notify;
    }

    /** @ejb:persistent-field */
    public void setNotify(Boolean notify)
    {
        this.notify = notify;
    }

	/**
     * @ejb:persistent-field 
	 */
	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
    }
	
	/**
         * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field 
     * @hibernate.property column="remotehost" non-null="true" unique="false"
	 */
	public String getRemoteHost() {
		return this.remoteHost;
	}

	public String toString()
    {
        StringBuffer str = new StringBuffer("{");

        str.append("id=" + id + " " + 
                   "name=" + name + " " + 
                   "email=" + email + " " + 
                   "url=" + url + " " + 
                   "content=" + content + " " + 
                   "postTime=" + postTime + " " + 
                   "spam=" + spam +
                   "notify=" + notify);
        str.append('}');

        return (str.toString());
    }

    public boolean equals(Object pOther)
    {
        if (pOther instanceof CommentData)
        {
            CommentData lTest = (CommentData) pOther;
            boolean lEquals = true;

            lEquals = PojoUtil.equals(lEquals, this.id, lTest.id);
            lEquals = PojoUtil.equals(lEquals, this.mWeblogEntry, lTest.mWeblogEntry);
            lEquals = PojoUtil.equals(lEquals, this.name, lTest.name);
            lEquals = PojoUtil.equals(lEquals, this.email, lTest.email);
            lEquals = PojoUtil.equals(lEquals, this.url, lTest.url);
            lEquals = PojoUtil.equals(lEquals, this.content, lTest.content);
            lEquals = PojoUtil.equals(lEquals, this.postTime, lTest.postTime);
            lEquals = PojoUtil.equals(lEquals, this.spam, lTest.spam);
            lEquals = PojoUtil.equals(lEquals, this.notify, lTest.notify);

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
        result = PojoUtil.addHashCode(result, this.id);
        result = PojoUtil.addHashCode(result, this.mWeblogEntry);
        result = PojoUtil.addHashCode(result, this.name);
        result = PojoUtil.addHashCode(result, this.email);
        result = PojoUtil.addHashCode(result, this.url);
        result = PojoUtil.addHashCode(result, this.content);
        result = PojoUtil.addHashCode(result, this.postTime);
        result = PojoUtil.addHashCode(result, this.spam);
        result = PojoUtil.addHashCode(result, this.notify);

        return result;
    }

    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        CommentData otherComment = (CommentData) otherData;
        this.id = otherComment.id;
        this.mWeblogEntry = otherComment.mWeblogEntry;
        this.name = otherComment.name;
        this.email = otherComment.email;
        this.url = otherComment.url;
        this.content = otherComment.content;
        this.postTime = otherComment.postTime;
        this.spam = otherComment.spam;
        this.notify = otherComment.notify;
    }

}