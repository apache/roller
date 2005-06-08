
package org.roller.pojos;

import org.apache.commons.lang.StringUtils;
import org.roller.util.PojoUtil;

import java.io.File;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration object for Roller.  Reads and writes roller-config.xml.
 * RollerConfig extends Struts ActionForm so that it can be extended by
 * RollerConfigForm in the administrator pages.
 * <p />
 * This is where you set default Roller configuration values.
 *
 * @ejb:bean name="RollerConfig"
 * @struts.form include-all="true"
 * @hibernate.class table="rollerconfig"
 * hibernate.jcs-cache usage="read-write"
 */
public class RollerConfig
    extends org.roller.pojos.PersistentObject
    implements java.io.Serializable
{
    static final long serialVersionUID = -6354583200913127875L;

    protected java.lang.String id = null;
    
    /**
     * Roller database version.
     */
    protected String mDatabaseVersion = "1.0";
    
    /**
     * Absolute URL for site, for cases where infered absolute URL doesn't work.
     */
    protected String mAbsoluteURL = null;

    /**
     * Should Roller cache return RSS pages.
     */
    protected Boolean mRssUseCache = Boolean.FALSE;

    /**
     * Duration to cache RSS pages (in seconds).
     */
    protected Integer mRssCacheTime = new Integer(3000);

    /**
     * Does Roller allow the creation of new users.
     */
    protected Boolean mNewUserAllowed = Boolean.TRUE;

    /**
     * List of usernames with Admin priviledges
     * seperated with commas.
     */
    protected String mAdminUsers = "admin";

    /**
     * Where to get Themes presented to new users.
     */
    protected String mUserThemes = "/themes";

    /**
     * List of "editor pages" for the Weblog entry editor
     * seperated with commas.
     */
    protected String mEditorPages = 
    	   "editor-text.jsp,editor-text-js.jsp,editor-midas.jsp,editor-dhtml.jsp,editor-ekit.jsp";

    /**
     * Dis/enble RSS aggregation capabilities.
     */
    protected Boolean mEnableAggregator = Boolean.FALSE;

    /**
     * Are file uploads enabled.
     */
    protected Boolean mUploadEnabled = Boolean.TRUE;

    /**
     * The maximum size of each user's upload directory.
     */
    protected BigDecimal mUploadMaxDirMB = new BigDecimal("2");

    /**
     * The maximum size allowed per uploaded file.
     */
    protected BigDecimal mUploadMaxFileMB = new BigDecimal(".5");

    /**
     * List of permitted file extensions (not including the "dot")
     * seperated with commas.
     * This attribute is mutually exclusive with uploadForbid.
     */
    protected String mUploadAllow = "";

    /**
     * List of forbidden file extensions (not including the "dot")
     * seperated with commas.
     * This attribute is mutually exclusive with uploadAllow.
     */
    protected String mUploadForbid = "";

    /**
     * Directory where uploaded files will be stored.
     * May end with a slash.  Optional, this value will
     * default to RollerContext.USER_RESOURCES.  If specified,
     * should be a full path on the system harddrive or
     * relative to the WebApp.
     */
    protected String mUploadDir = "";

    /**
     * The path from which the webserver will serve upload files.
     * This values must not end in a slash.
     */
    protected String uploadPath = "/resources";

    protected Boolean mMemDebug = Boolean.FALSE;

    /**
     * Determines if the Comment page will "autoformat"
     * comments.  That is, replace carriage-returns with <br />.
     */
    protected Boolean mAutoformatComments = Boolean.FALSE;

    /**
     * Determines if the Comment page will escape html in comments.
     */
    protected Boolean mEscapeCommentHtml = Boolean.FALSE;

    /**
     * Determines if e-mailing comments is enabled.
     */
    protected Boolean mEmailComments = Boolean.FALSE;

    /**
     * Enable linkback extraction.
     */
    protected Boolean mEnableLinkback = Boolean.FALSE;

    /**
     * Name of this site
     */
    protected String mSiteName = "Roller-based Site";

    /**
     * Description of this site
     */
    protected String mSiteDescription = "Roller-based Site";

    /**
     * Site administrator's email address
     */
    protected String mEmailAddress = "";

    /**
     * Lucene index directory
     */
    protected String mIndexDir = "${user.home}" + File.separator
    						     + "roller-index";
    
    /**
     * Flag for encrypting passwords
     */
    protected Boolean mEncryptPasswords = Boolean.FALSE;
    
    protected String mAlgorithm = "SHA";

    //-------------------------------------- begin requisite getters & setters

    /**
     * Not remote since primary key may be extracted by other means.
     *
     * @struts.validator type="required" msgkey="errors.required"
     * @ejb:persistent-field
     * @hibernate.id column="id" type="string"
     *  generator-class="uuid.hex" unsaved-value="null"
     */
    public String getId() {
        return this.id;
    }

    /** @ejb:persistent-field */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="absoluteurl" non-null="false" unique="false"
     */
    public String getAbsoluteURL()
    {
        return mAbsoluteURL;
    }
    /** @ejb:persistent-field */
    public void setAbsoluteURL(String string)
    {
        mAbsoluteURL = string;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="rssusecache" non-null="true" unique="false"
     */
    public Boolean getRssUseCache()
    {
        return mRssUseCache;
    }
    /** @ejb:persistent-field */
    public void setRssUseCache(Boolean use)
    {
        mRssUseCache = use;
    }

    /**
     * Cache time for Newsfeed display (not for Roller's RSS feeds)
     * @ejb:persistent-field
     * @hibernate.property column="rsscachetime" non-null="true" unique="false"
     */
    public Integer getRssCacheTime()
    {
        return mRssCacheTime;
    }
    /** @ejb:persistent-field */
    public void setRssCacheTime(Integer cacheTime)
    {
        mRssCacheTime = cacheTime;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="newuserallowed" non-null="true" unique="false"
    */
    public Boolean getNewUserAllowed()
    {
        return mNewUserAllowed;
    }
    /** @ejb:persistent-field */
    public void setNewUserAllowed(Boolean use)
    {
        mNewUserAllowed = use;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="adminusers" non-null="false" unique="false"
     */
    public String getAdminUsers()
    {
        return mAdminUsers;
    }
    /** @ejb:persistent-field */
    public void setAdminUsers(String _adminUsers)
    {
        mAdminUsers = _adminUsers;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="userthemes" non-null="true" unique="false"
     */
    public String getUserThemes()
    {
        return mUserThemes;
    }
    /** @ejb:persistent-field */
    public void setUserThemes(String str)
    {
        mUserThemes = str;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="editorpages" non-null="false" unique="false"
     */
    public String getEditorPages()
    {
        return mEditorPages;
    }
    /** @ejb:persistent-field */
    public void setEditorPages(String _editorPages)
    {
        mEditorPages = _editorPages;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="enableaggregator" non-null="true" unique="false"
    */
    public Boolean getEnableAggregator()
    {
        return mEnableAggregator;
    }
    /** @ejb:persistent-field */
    public void setEnableAggregator(Boolean use)
    {
        mEnableAggregator = use;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="uploadenabled" non-null="true" unique="false"
    */
    public Boolean getUploadEnabled()
    {
        return mUploadEnabled;
    }
    /** @ejb:persistent-field */
    public void setUploadEnabled(Boolean use)
    {
        mUploadEnabled = use;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="uploadmaxdirmb"
    *   non-null="true" unique="false" type="big_decimal"
    */
    public BigDecimal getUploadMaxDirMB()
    {
        return mUploadMaxDirMB;
    }
    /** @ejb:persistent-field */
    public void setUploadMaxDirMB(BigDecimal use)
    {
        mUploadMaxDirMB = use;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="uploadmaxfilemb"
    *   non-null="true" unique="false" type="big_decimal"
    */
    public BigDecimal getUploadMaxFileMB()
    {
        return mUploadMaxFileMB;
    }
    /** @ejb:persistent-field */
    public void setUploadMaxFileMB(BigDecimal use)
    {
        mUploadMaxFileMB = use;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="uploadallow" non-null="true" unique="false"
     */
    public String getUploadAllow()
    {
        return mUploadAllow;
    }
    /** @ejb:persistent-field */
    public void setUploadAllow(String _uploadAllow)
    {
        mUploadAllow = _uploadAllow;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="uploadforbid" non-null="true" unique="false"
     */
    public String getUploadForbid()
    {
        return mUploadForbid;
    }
    /** @ejb:persistent-field */
    public void setUploadForbid(String _uploadForbid)
    {
        mUploadForbid = _uploadForbid;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="uploaddir" non-null="true" unique="false"
     */
    public String getUploadDir()
    {
        return mUploadDir;
    }
    /** @ejb:persistent-field */
    public void setUploadDir(String str)
    {
        mUploadDir = str;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="uploadpath" non-null="true" unique="false"
     */
    public String getUploadPath()
    {
        return uploadPath;
    }
    /** @ejb:persistent-field */
    public void setUploadPath(String str)
    {
        uploadPath = str;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="memdebug" non-null="true" unique="false"
    */
    public Boolean getMemDebug() { return mMemDebug; }

    /**
     * Set memory debugging on or off.
     * @param mMemDebug The mMemDebug to set
     * @ejb:persistent-field
     */
    public void setMemDebug(Boolean memDebug)
    {
        mMemDebug = memDebug;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="autoformatcomments" non-null="true" unique="false"
    */
    public Boolean getAutoformatComments()
    {
        return mAutoformatComments;
    }
    /** @ejb:persistent-field */
    public void setAutoformatComments(Boolean value)
    {
        mAutoformatComments = value;
    }

    /**
    * @ejb:persistent-field
    * @hibernate.property column="escapecommenthtml" non-null="true" unique="false"
    */
    public Boolean getEscapeCommentHtml()
    {
        return mEscapeCommentHtml;
    }
    /** @ejb:persistent-field */
    public void setEscapeCommentHtml(Boolean value)
    {
        mEscapeCommentHtml = value;
    }

    /**
     * Should Comments be emailed to participants?
     *
     * @ejb:persistent-field
     * @hibernate.property column="emailcomments" non-null="true" unique="false"
     */
    public Boolean getEmailComments()
    {
        return mEmailComments;
    }

    /**
     * @ejb:persistent-field
     */
    public void setEmailComments(Boolean emailComments)
    {
        this.mEmailComments = emailComments;
    }

    /**
     * Enable linkback.
     * @ejb:persistent-field
     * @hibernate.property column="enablelinkback" non-null="true" unique="false"
    */
    public Boolean getEnableLinkback()
    {
        return mEnableLinkback;
    }

    /**
     * Enable linkback.
     * @ejb:persistent-field
     */
    public void setEnableLinkback(Boolean b)
    {
        mEnableLinkback = b;
    }


    /**
     * @ejb:persistent-field
     * @hibernate.property column="sitedescription" non-null="false" unique="false"
     */
    public String getSiteDescription()
    {
        return mSiteDescription;
    }

    /**
     * @param string
     * @ejb:persistent-field
     */
    public void setSiteDescription(String string)
    {
        mSiteDescription = string;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="sitename" non-null="false" unique="false"
     */
    public String getSiteName()
    {
        return mSiteName;
    }

    /**
     * @param string
     * @ejb:persistent-field
     */
    public void setSiteName(String string)
    {
        mSiteName = string;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="emailaddress" non-null="false" unique="false"
     */
    public String getEmailAddress()
    {
        return mEmailAddress;
    }

    /**
     * @param string
     * @ejb:persistent-field
     */
    public void setEmailAddress(String emailAddress)
    {
        mEmailAddress = emailAddress;
    }

	/**
     * @ejb:persistent-field
     * @hibernate.property column="indexdir" non-null="false" unique="false"
     */
    public String getIndexDir() {

        return mIndexDir;
    }

	/**
	 * @param the new index directory
	 * @ejb:persistent-field
	 */
	public void setIndexDir(String indexDir)
	{
		mIndexDir = indexDir;
	}

    /**
     * @ejb:persistent-field
     * @hibernate.property column="encryptpasswords" non-null="true" unique="false"
     */
    public Boolean getEncryptPasswords()
    {
        return mEncryptPasswords;
    }
    /** @ejb:persistent-field */
    public void setEncryptPasswords(Boolean value)
    {
        mEncryptPasswords = value;
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="algorithm" non-null="false" unique="false"
     */
    public String getAlgorithm() {

        return mAlgorithm;
    }

    /**
     * @param the new algorithm for encrypting passwords
     * @ejb:persistent-field
     */
    public void setAlgorithm(String algorithm)
    {
        mAlgorithm = algorithm;
    }
        
	/**
	 * @return Returns the mDatabaseVersion.
     * @hibernate.property column="dbversion" non-null="false" unique="false"
	 */
	public String getDatabaseVersion() {
		return mDatabaseVersion;
	}
	/**
	 * @param databaseVersion The mDatabaseVersion to set.
     * @ejb:persistent-field
	 */
	public void setDatabaseVersion(String databaseVersion) {
		mDatabaseVersion = databaseVersion;
	}

	//---------------------------------------- end requisite getters & setters

    /**
     * Convenience method for getAdminUsers.
     **/
    public String[] adminUsersArray()
    {
        return StringUtils.split(StringUtils.deleteWhitespace(mAdminUsers), ",");
    }

    /**
     * Convenience method for getEditorPages.
     **/
    public List getEditorPagesList()
    {
        return Arrays.asList(
            StringUtils.split(StringUtils.deleteWhitespace(mEditorPages), ","));
    }

    /**
     * Convenience method for getUploadAllow.
     **/
    public String[] uploadAllowArray()
    {
        return StringUtils.split(StringUtils.deleteWhitespace(mUploadAllow), ",");
    }

    /**
     * Convenience method for getUploadForbid.
     **/
    public String[] uploadForbidArray()
    {
        return StringUtils.split(StringUtils.deleteWhitespace(mUploadForbid), ",");
    }

    public void updateValues( RollerConfig child )
    {
        this.mAbsoluteURL = child.getAbsoluteURL();
        this.mRssUseCache = child.getRssUseCache();
        this.mRssCacheTime = child.getRssCacheTime();
        this.mNewUserAllowed = child.getNewUserAllowed();
        this.mAdminUsers = child.getAdminUsers();
        this.mDatabaseVersion = child.getDatabaseVersion();
        this.mUserThemes = child.getUserThemes();
        this.mEditorPages = child.getEditorPages();
        this.mEnableAggregator = child.getEnableAggregator();
        this.mUploadEnabled = child.getUploadEnabled();
        this.mUploadMaxDirMB = child.getUploadMaxDirMB();
        this.mUploadMaxFileMB = child.getUploadMaxFileMB();
        this.mUploadAllow = child.getUploadAllow();
        this.mUploadForbid = child.getUploadForbid();
        this.mUploadDir = child.getUploadDir();
        this.uploadPath = child.getUploadPath();
        this.mMemDebug = child.getMemDebug();
        this.mAutoformatComments = child.getAutoformatComments();
        this.mEscapeCommentHtml = child.getEscapeCommentHtml();
        this.mEmailComments = child.getEmailComments();
        this.mEnableLinkback = child.getEnableLinkback();
        this.mSiteName = child.getSiteName();
        this.mSiteDescription = child.getSiteDescription();
        this.mEmailAddress = child.getEmailAddress();
        this.mIndexDir = child.getIndexDir();
        this.mEncryptPasswords = child.getEncryptPasswords();
        this.mAlgorithm = child.getAlgorithm();
    }

    /** nice output for debugging */
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("RollerConfig \n");
        Class clazz = getClass();
        Field[] fields = clazz.getDeclaredFields();

        try {
            AccessibleObject.setAccessible(fields, true);
            for (int i = 0; i < fields.length; i++) {
                buf.append(
                    "\t["
                        + fields[i].getName()
                        + "="
                        + fields[i].get(this)
                        + "], \n");
            }
        } catch (Exception e) {
            // ignored!
        }

        return buf.toString();
    }


	public void setData(PersistentObject vo) {
		if (vo instanceof RollerConfig) {
      		this.id = ((RollerConfig)vo).id;
			updateValues((RollerConfig)vo);
        }
	}

    public boolean equals(Object pOther)
    {
        if (pOther instanceof WebsiteData)
        {
            RollerConfig lTest = (RollerConfig) pOther;
            boolean lEquals = true;

            lEquals = PojoUtil.equals(lEquals, this.id, lTest.id);
			lEquals = PojoUtil.equals(lEquals, this.mAbsoluteURL, lTest.getAbsoluteURL());
			lEquals = PojoUtil.equals(lEquals, this.mRssUseCache, lTest.getRssUseCache());
			lEquals = PojoUtil.equals(lEquals, this.mRssCacheTime, lTest.getRssCacheTime());
			lEquals = PojoUtil.equals(lEquals, this.mNewUserAllowed, lTest.getNewUserAllowed());
			lEquals = PojoUtil.equals(lEquals, this.mAdminUsers, lTest.getAdminUsers());
			lEquals = PojoUtil.equals(lEquals, this.mUserThemes, lTest.getUserThemes());
			lEquals = PojoUtil.equals(lEquals, this.mEditorPages, lTest.getEditorPages());
			lEquals = PojoUtil.equals(lEquals, this.mEnableAggregator, lTest.getEnableAggregator());
			lEquals = PojoUtil.equals(lEquals, this.mUploadEnabled, lTest.getUploadEnabled());
			lEquals = PojoUtil.equals(lEquals, this.mUploadMaxDirMB, lTest.getUploadMaxDirMB());
			lEquals = PojoUtil.equals(lEquals, this.mUploadMaxFileMB, lTest.getUploadMaxFileMB());
			lEquals = PojoUtil.equals(lEquals, this.mUploadAllow, lTest.getUploadAllow());
			lEquals = PojoUtil.equals(lEquals, this.mUploadForbid, lTest.getUploadForbid());
			lEquals = PojoUtil.equals(lEquals, this.mUploadDir, lTest.getUploadDir());
			lEquals = PojoUtil.equals(lEquals, this.uploadPath, lTest.getUploadPath());
			lEquals = PojoUtil.equals(lEquals, this.mMemDebug, lTest.getMemDebug());
			lEquals = PojoUtil.equals(lEquals, this.mAutoformatComments, lTest.getAutoformatComments());
			lEquals = PojoUtil.equals(lEquals, this.mEscapeCommentHtml, lTest.getEscapeCommentHtml());
			lEquals = PojoUtil.equals(lEquals, this.mEmailComments, lTest.getEmailComments());
			lEquals = PojoUtil.equals(lEquals, this.mEnableLinkback, lTest.getEnableLinkback());
			lEquals = PojoUtil.equals(lEquals, this.mSiteName, lTest.getSiteName());
			lEquals = PojoUtil.equals(lEquals, this.mSiteDescription, lTest.getSiteDescription());
			lEquals = PojoUtil.equals(lEquals, this.mEmailAddress, lTest.getEmailAddress());
			lEquals = PojoUtil.equals(lEquals, this.mIndexDir, lTest.getIndexDir());
            lEquals = PojoUtil.equals(lEquals, this.mEncryptPasswords, lTest.getEncryptPasswords());
            lEquals = PojoUtil.equals(lEquals, this.mAlgorithm, lTest.getAlgorithm());

		 	return lEquals;
	  	}
	  	else
	  	{
		 	return false;
      	}
	}
}
