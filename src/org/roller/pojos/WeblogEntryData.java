
package org.roller.pojos;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.util.DateUtil;
import org.roller.util.Utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;


/**
 * Represents a Weblog Entry.
 * 
 * @author David M Johnson
 *
 * @ejb:bean name="WeblogEntryData"
 * @struts.form include-all="true"
 * @hibernate.class table="weblogentry"
 * hibernate.jcs-cache usage="read-write"
 */
public class WeblogEntryData extends org.roller.pojos.PersistentObject
    implements java.io.Serializable
{
    private static Log mLogger = LogFactory.getFactory()
                                           .getInstance(WeblogEntryData.class);
                                           
    static final long serialVersionUID = 2341505386843044125L;
    
    protected String id=null;
    protected org.roller.pojos.WeblogCategoryData category=null;
    protected String title=null;
    protected String link=null;
    protected String text=null;
    protected String anchor=null;
    protected Timestamp pubTime=null;
    protected Timestamp updateTime=null;
    protected Boolean publishEntry=null;
    protected WebsiteData mWebsite=null;
    protected String mPlugins;
    protected Boolean allowComments = Boolean.TRUE;
    protected Integer commentDays = new Integer(7);
    protected Boolean rightToLeft = Boolean.FALSE;
    protected Boolean pinnedToMain = Boolean.FALSE;
    
    //----------------------------------------------------------- Construction

    public WeblogEntryData()
    {
    }

    public WeblogEntryData(
       java.lang.String id, 
       org.roller.pojos.WeblogCategoryData category, 
       WebsiteData website, 
       java.lang.String title, 
       java.lang.String link,
       java.lang.String text, 
       java.lang.String anchor, 
       java.sql.Timestamp pubTime, 
       java.sql.Timestamp updateTime, 
       java.lang.Boolean publishEntry)
    {
        this.id = id;
        this.category = category;
        this.mWebsite = website;
        this.title = title;
        this.link = link;
        this.text = text;
        this.anchor = anchor;
        this.pubTime = pubTime;
        this.updateTime = updateTime;
        this.publishEntry = publishEntry;
    }

    public WeblogEntryData(WeblogEntryData otherData)
    {
        setData(otherData);
    }

    //---------------------------------------------------------- Initializaion

    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        WeblogEntryData other = (WeblogEntryData)otherData;
        this.id = other.id;
        this.category = other.category;
        this.mWebsite = other.mWebsite;
        this.title = other.title;
        this.link = other.link;
        this.text = other.text;
        this.anchor = other.anchor;
        this.pubTime = other.pubTime;
        this.updateTime = other.updateTime;
        this.publishEntry = other.publishEntry;
        this.mPlugins = other.mPlugins;
        this.allowComments = other.allowComments;
        this.commentDays = other.commentDays;
        this.rightToLeft = other.rightToLeft;
        this.pinnedToMain = other.pinnedToMain;
    }

    //------------------------------------------------------ Simple properties
    
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
     * @hibernate.many-to-one column="categoryid" cascade="none" not-null="true"
     */
    public org.roller.pojos.WeblogCategoryData getCategory()
    {
        return this.category;
    }

    /** @ejb:persistent-field */
    public void setCategory(org.roller.pojos.WeblogCategoryData category)
    {
        this.category = category;
    }

    /**
     * Set weblog category via weblog category ID.
     * @param id Weblog category ID.
     */
    public void setCategoryId(String id) throws RollerException
    {
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        setCategory(wmgr.retrieveWeblogCategory(id));
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.many-to-one column="websiteid" cascade="none" not-null="true"
     */
    public WebsiteData getWebsite()
    {
        return this.mWebsite;
    }

    /** @ejb:persistent-field */
    public void setWebsite(WebsiteData website)
    {
        this.mWebsite = website;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.property column="title" non-null="true" unique="false"
     */
    public java.lang.String getTitle()
    {
        return this.title;
    }

    /** @ejb:persistent-field */
    public void setTitle(java.lang.String title)
    {
        this.title = title;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.property column="text" non-null="true" unique="false"
     */
    public java.lang.String getText()
    {
        return this.text;
    }

    /** @ejb:persistent-field */
    public void setText(java.lang.String text)
    {
        this.text = text;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.property column="anchor" non-null="true" unique="false"
     */
    public java.lang.String getAnchor()
    {
        return this.anchor;
    }

    /** @ejb:persistent-field */
    public void setAnchor(java.lang.String anchor)
    {
        this.anchor = anchor;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.property column="pubtime" non-null="true" unique="false"
     */
    public java.sql.Timestamp getPubTime()
    {
        return this.pubTime;
    }

    /** @ejb:persistent-field */
    public void setPubTime(java.sql.Timestamp pubTime)
    {
        this.pubTime = pubTime;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.property column="updatetime" non-null="true" unique="false"
     */
    public java.sql.Timestamp getUpdateTime()
    {
        return this.updateTime;
    }

    /** @ejb:persistent-field */
    public void setUpdateTime(java.sql.Timestamp updateTime)
    {
        this.updateTime = updateTime;
    }

    /** 
     * @ejb:persistent-field 
     * @hibernate.property column="publishentry" non-null="true" unique="false"
     */
    public java.lang.Boolean getPublishEntry()
    {
        return this.publishEntry;
    }

    /** @ejb:persistent-field */
    public void setPublishEntry(java.lang.Boolean publishEntry)
    {
        this.publishEntry = publishEntry;
    }

    /**
     * Some weblog entries are about one specific link.
     * @return Returns the link.
     *
     * @ejb:persistent-field 
     * @hibernate.property column="link" non-null="false" unique="false"
     */
    public java.lang.String getLink()
    {
        return link;
    }

    /**
     * @ejb:persistent-field
     * @param link The link to set.
     */
    public void setLink(java.lang.String link)
    {
        this.link = link;
    }

    /**
     * Comma-delimited list of this entry's Plugins.
     * @ejb:persistent-field
     * @hibernate.property column="plugins" non-null="false" unique="false"
     */
    public java.lang.String getPlugins()
    {
        return mPlugins;
    }

    /** @ejb:persistent-field */
    public void setPlugins(java.lang.String string)
    {
        mPlugins = string;
    }

	/**
	 * True if comments are allowed on this weblog entry.
     * @ejb:persistent-field 
     * @hibernate.property column="allowcomments" non-null="true" unique="false"
	 */
	public Boolean getAllowComments() {
		return allowComments;
	}
	/**
	 * True if comments are allowed on this weblog entry.
     * @ejb:persistent-field 
	 */
	public void setAllowComments(Boolean allowComments) {
		this.allowComments = allowComments;
	}
	
	/**
	 * Number of days after pubTime that comments should be allowed, or 0 for no limit.
     * @ejb:persistent-field 
     * @hibernate.property column="commentdays" non-null="true" unique="false"
	 */
	public Integer getCommentDays() {
		return commentDays;
	}
	/**
	 * Number of days after pubTime that comments should be allowed, or 0 for no limit.
     * @ejb:persistent-field 
	 */
	public void setCommentDays(Integer commentDays) {
		this.commentDays = commentDays;
	}
	
	/**
	 * True if this entry should be rendered right to left.
     * @ejb:persistent-field 
     * @hibernate.property column="righttoleft" non-null="true" unique="false"
	 */
	public Boolean getRightToLeft() {
		return rightToLeft;
	}
	/**
	 * True if this entry should be rendered right to left.
     * @ejb:persistent-field 
	 */
	public void setRightToLeft(Boolean rightToLeft) {
		this.rightToLeft = rightToLeft;
	}

    /**
     * True if story should be pinned to the top of the Roller site main blog.
     * @return Returns the pinned.
     * 
     * @ejb:persistent-field 
     * @hibernate.property column="pinnedtomain" non-null="true" unique="false"
     */
    public Boolean getPinnedToMain()
    {
        return pinnedToMain;
    }
    /**
     * True if story should be pinned to the top of the Roller site main blog.
     * @param pinned The pinned to set.
     * 
     * @ejb:persistent-field 
     */
    public void setPinnedToMain(Boolean pinnedToMain)
    {
        this.pinnedToMain = pinnedToMain;
    }

    //------------------------------------------------------------------------

    /** 
     * @see org.roller.pojos.PersistentObject#save()
     */
    public void save() throws RollerException
    {
        // If no anchor then create one
        if (getAnchor()==null || getAnchor().trim().equals(""))
        {
            setAnchor(createAnchor());
        }
        super.save();
    }
    
    //------------------------------------------------------------------------
    
    /** 
     * True if comments are still allowed on this entry considering the 
     * allowComments and commentDays fields. 
     */
    public boolean getCommentsStillAllowed() 
    {
    		boolean ret = false;
    		if (getAllowComments() == null || getAllowComments().booleanValue()) 
    		{
    			if (getCommentDays() == null || getCommentDays().intValue() == 0)
    			{
    				ret = true;
    			}
    			else 
    			{
    				Calendar expireCal = Calendar.getInstance(getWebsite().getLocaleInstance());
    				expireCal.setTime(getPubTime());
    				expireCal.add(Calendar.DATE, getCommentDays().intValue());
    				Date expireDay = expireCal.getTime();
    				Date today = new Date();
    				if (today.before(expireDay))
    				{
    					ret = true;
    				}
    			}
    		}
    		return ret;
    }
    public void setCommentsStillAllowed(boolean ignored) {
        // no-op
    }

    
    //------------------------------------------------------------------------
    
    /** 
     * Format the publish time of this weblog entry using the specified pattern.
     * See java.text.SimpleDateFormat for more information on this format.
     * @see java.text.SimpleDateFormat
     * @return Publish time formatted according to pattern.
     */
    public String formatPubTime(String pattern)
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(pattern, 
                    this.getWebsite().getLocaleInstance());

            return format.format(getPubTime());
        }
        catch (RuntimeException e)
        {
            mLogger.error("Unexpected exception", e);
        }

        return "ERROR: formatting date";
    }

    //------------------------------------------------------------------------
    
    /** 
     * Format the update time of this weblog entry using the specified pattern.
     * See java.text.SimpleDateFormat for more information on this format.
     * @see java.text.SimpleDateFormat
     * @return Update time formatted according to pattern.
     */
    public String formatUpdateTime(String pattern)
    {
        try
        {
            SimpleDateFormat format = new SimpleDateFormat(pattern);

            return format.format(getUpdateTime());
        }
        catch (RuntimeException e)
        {
            mLogger.error("Unexpected exception", e);
        }

        return "ERROR: formatting date";
    }

    //------------------------------------------------------------------------
    
    public List getComments()
    {
        return getComments(true);
    }
    
    public List getComments(boolean ignoreSpam)
    {
        List list = new ArrayList();
        try
        {
            return RollerFactory.getRoller().getWeblogManager().getComments(getId(), ignoreSpam);
        }
        catch (RollerException alreadyLogged) {}
        return list;
    }

    //------------------------------------------------------------------------
    
    public List getReferers()
    {
        List referers = null;
        try {
            referers = RollerFactory.getRoller().getRefererManager().getReferersToEntry(getId());
        } catch (RollerException e) {
            mLogger.error("Unexpected exception", e);
        }
        return referers;
    }

    //------------------------------------------------------------------------
    
    /**
     * @param entry
     * @param url
     * @param title
     * @param excerpt
     * @param blogName
     */
    public void addTrackback(
        String url, String title, String excerpt, String blogName) 
        throws RollerException
    {
        String modTitle = blogName + ": "  + title;
        if (modTitle.length() >= 250)
        {
            modTitle = modTitle.substring(0, 257);
            modTitle += "...";
        }
        
        // Track trackbacks as comments
        CommentData comment = new CommentData();
        comment.setContent("[Trackback] "+excerpt);
        comment.setName(blogName);
        comment.setUrl(url);
        comment.setWeblogEntry(this); 
        comment.setPostTime(new Timestamp(new Date().getTime()));
        comment.save();
         
        // Alternative: treat trackbacks as referers
        //RefererData ref = new RefererData();
        //ref.setWebsite(getWebsite());
        //ref.setWeblogEntry(this);
        //ref.setRequestUrl("(trackback)");
        //ref.setRefererUrl(url);
        //ref.setTitle(modTitle);
        //ref.setExcerpt(excerpt);
        //ref.setVisible(Boolean.TRUE);
        //ref.setDayHits(new Integer(0));
        //ref.setTotalHits(new Integer(0));
        //ref.setDuplicate(Boolean.FALSE);        
        //ref.setDateString(formatPubTime("yyyyMMdd"));        
        //mRoller.getRefererManager().storeReferer(ref);
    }
    
    /**
     * Convenience method for getPermalink(category)
     * where no category is necessary.
     * @return
     */
    public String getPermaLink()
    {
        return getPermaLink(null);
    }
    
    /**
     * Get the "relative" URL to this entry.  Proper use of this will 
     * require prepending the baseURL (either the full root 
     * [http://server.com/context] or at least the context
     * [/context]) in order to generate a functional link.
     * @param category The category name to insert into the permalink.
     * @return String
     */
    public String getPermaLink(String categoryPath)
    {
        SimpleDateFormat formatter = DateUtil.get8charDateFormat();
        String dayString = formatter.format(this.getPubTime());
        String lAnchor = this.getAnchor();
        if ("".equals(categoryPath)) categoryPath = null;
        String lCategory = categoryPath;
        try
        {
            lAnchor = URLEncoder.encode(anchor, "UTF-8");
            if (categoryPath != null) 
            {
                lCategory = URLEncoder.encode(categoryPath, "UTF-8");
            }
        }
        catch (UnsupportedEncodingException e)
        {
            // go with the "no encoding" version
        }
        
        String plink = "/page/" + this.getWebsite().getUser().getUserName() + "/" + dayString;
        if (lCategory != null) 
        {
            plink += "?catname=" + lCategory;
        }
        plink += "#" + lAnchor;
        
        return plink;
    }
    
    /**
     * Return the Title of this post, or the first 255 characters of the
     * entry's text.
     * 
     * @return String
     */
    public String getDisplayTitle()
    {
        if ( getTitle()==null || getTitle().trim().equals("") )
        {
            return StringUtils.left(Utilities.removeHTML(text),255);
        }
        return Utilities.removeHTML(getTitle());
    }
    
    //------------------------------------------------------------------------
    
    public String toString()
    {
        StringBuffer str = new StringBuffer("{");

        str.append("id=" + id + " " + 
                   "category=" + category + " " + 
                   "title=" + title + " " + 
                    "text=" + text + " " + 
                    "anchor=" + anchor + " " + 
                    "pubTime=" + pubTime + " " + 
                    "updateTime=" + updateTime + " " + 
                    "publishEntry=" + publishEntry + " " + 
                    "plugins=" + mPlugins);
        str.append('}');

        return (str.toString());
    }

    //------------------------------------------------------------------------
    
    public boolean equals(Object pOther)
    {
        if (pOther instanceof WeblogEntryData)
        {
            WeblogEntryData lTest = (WeblogEntryData) pOther;
            boolean lEquals = true;

            if (this.id == null)
            {
                lEquals = lEquals && (lTest.id == null);
            }
            else
            {
                lEquals = lEquals && this.id.equals(lTest.id);
            }

            if (this.category == null)
            {
                lEquals = lEquals && (lTest.category == null);
            }
            else
            {
                lEquals = lEquals && this.category.equals(lTest.category);
            }

            if (this.mWebsite == null)
            {
                lEquals = lEquals && (lTest.mWebsite == null);
            }
            else
            {
                lEquals = lEquals && this.mWebsite.equals(lTest.mWebsite);
            }

            if (this.title == null)
            {
                lEquals = lEquals && (lTest.title == null);
            }
            else
            {
                lEquals = lEquals && this.title.equals(lTest.title);
            }

            if (this.text == null)
            {
                lEquals = lEquals && (lTest.text == null);
            }
            else
            {
                lEquals = lEquals && this.text.equals(lTest.text);
            }

            if (this.anchor == null)
            {
                lEquals = lEquals && (lTest.anchor == null);
            }
            else
            {
                lEquals = lEquals && this.anchor.equals(lTest.anchor);
            }

            if (this.pubTime == null)
            {
                lEquals = lEquals && (lTest.pubTime == null);
            }
            else
            {
                lEquals = lEquals && this.pubTime.equals(lTest.pubTime);
            }

            if (this.updateTime == null)
            {
                lEquals = lEquals && (lTest.updateTime == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.updateTime.equals(lTest.updateTime);
            }

            if (this.publishEntry == null)
            {
                lEquals = lEquals && (lTest.publishEntry == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.publishEntry.equals(lTest.publishEntry);
            }

            if (this.mPlugins == null)
            {
                lEquals = lEquals && (lTest.mPlugins == null);
            }
            else
            {
                lEquals = lEquals && 
                          this.mPlugins.equals(lTest.mPlugins);
            }


            return lEquals;
        }
        else
        {
            return false;
        }
    }

    //------------------------------------------------------------------------
    
    public int hashCode()
    {
        int result = 17;
        result = (37 * result) + 
                 ((this.id != null) ? this.id.hashCode() : 0);
        result = (37 * result) + 
                 ((this.category != null) ? this.category.hashCode() : 0);
        result = (37 * result) + 
                 ((this.mWebsite != null) ? this.mWebsite.hashCode() : 0);
        result = (37 * result) + 
                 ((this.title != null) ? this.title.hashCode() : 0);
        result = (37 * result) + 
                 ((this.text != null) ? this.text.hashCode() : 0);
        result = (37 * result) + 
                 ((this.anchor != null) ? this.anchor.hashCode() : 0);
        result = (37 * result) + 
                 ((this.pubTime != null) ? this.pubTime.hashCode() : 0);
        result = (37 * result) + 
                 ((this.updateTime != null) ? this.updateTime.hashCode() : 0);
        result = (37 * result) + 
                 ((this.publishEntry != null) ? this.publishEntry.hashCode() : 0);
        result = (37 * result) + 
                 ((this.mPlugins != null) ? this.mPlugins.hashCode() : 0);

        return result;
    }
    
    /** Return RSS 09x style description (escaped HTML version of entry text) */
    public String getRss09xDescription()
    {
        return getRss09xDescription(-1);
    }
    
    /** Return RSS 09x style description (escaped HTML version of entry text) */
    public String getRss09xDescription(int maxLength)
    {
        String ret = Utilities.escapeHTML(text);
        if (maxLength != -1 && ret.length() > maxLength) 
        {  
            ret = ret.substring(0,maxLength-3)+"..."; 
        }
        return ret;     
    }

    /** Create anchor for weblog entry, based on title or text */
    protected String createAnchor() throws RollerException
    {
        return RollerFactory.getRoller().getWeblogManager().createAnchor(this);
    }

    /** Create anchor for weblog entry, based on title or text */
    public String createAnchorBase()
    {
        // Use title or text for base anchor
        String base = getTitle();
        if (base == null || base.trim().equals(""))
        {
            base = getText();
        }
        if (base != null && !base.trim().equals(""))
        {
            base = Utilities.replaceNonAlphanumeric(base, ' ');

            // Use only the first 4 words
            StringTokenizer toker = new StringTokenizer(base);
            String tmp = null;
            int count = 0;
            while (toker.hasMoreTokens() && count < 5)
            {
                String s = (String) toker.nextToken();
                s = s.toLowerCase();
                tmp = (tmp == null) ? s : tmp + "_" + s;
                count++;
            }
            base = tmp;
        }
        // No title or text, so instead we will use the items date
        // in YYYYMMDD format as the base anchor
        else
        {
            base = DateUtil.format8chars(getPubTime());
        }

        return base;
    }

    /**
     * A no-op.
     * TODO: fix formbean generation so this is not needed. 
     * @param string
     */
    public void setPermaLink(String string)
    {
    }

    /**
     * A no-op.
     * TODO: fix formbean generation so this is not needed. 
     * @param string
     */
    public void setDisplayTitle(String string)
    {
    }

    /**
     * A no-op.
     * TODO: fix formbean generation so this is not needed. 
     * @param string
     */
    public void setRss09xDescription(String string)
    {
    }
    
    /** 
     * @see org.roller.pojos.PersistentObject#remove()
     */
    public void remove() throws RollerException
    {
        RollerFactory.getRoller().getWeblogManager().removeWeblogEntryContents(this);
        super.remove();
    }
    
    /**
     * Convenience method to transform mPlugins to a List
     * @return
     */
    public List getPluginsList()
    {
        if (mPlugins != null)
        {
            return Arrays.asList( StringUtils.split(mPlugins, ",") );
        }
        return new ArrayList();
    }

}
