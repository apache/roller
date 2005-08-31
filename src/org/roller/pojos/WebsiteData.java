package org.roller.pojos;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang.StringUtils;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.util.PojoUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.ThemeNotFoundException;
import org.roller.model.ThemeManager;
import org.roller.model.UserManager;


/**
 * Website has many-to-many association with users. Website has one-to-many and 
 * one-direction associations with weblog entries, weblog categories, folders and
 * other objects. Use UserManager to create, fetch, update and retreive websites.
 * 
 * @author David M Johnson
 *
 * @ejb:bean name="WebsiteData"
 * @struts.form include-all="true"
 * @hibernate.class table="website"
 * 
 * hibernate.jcs-cache usage="read-write"
 */
public class WebsiteData extends org.roller.pojos.PersistentObject
    implements java.io.Serializable
{
    static final long serialVersionUID = 206437645033737127L;
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(WebsiteData.class);
    
    // Simple properties
    protected String  id;
    protected String  handle;
    protected String  name;
    protected String  description;
    protected String  defaultPageId;
    protected String  weblogDayPageId;
    protected Boolean enableBloggerApi;
    protected String  editorPage;
    protected String  ignoreWords;
    protected Boolean allowComments;
    protected Boolean emailComments;
    protected String  emailFromAddress;
    protected String  emailAddress;
    protected String  editorTheme;
    protected String  locale;
    protected String  timeZone;
    protected String  mDefaultPlugins;
    protected Boolean enabled;
    protected Date dateCreated;
       
    // Associated objects
    protected UserData mUser = null; // TODO: decide if website.user is needed
    protected List     permissions = new ArrayList();    
    protected WeblogCategoryData bloggerCategory;
    protected WeblogCategoryData defaultCategory;


    public WebsiteData()
    {
    }

    public WebsiteData(final String   id, 
                       final String   name,
                       final String   handle,
                       final String   description,
                       final UserData user,
                       final String   defaultPageId,
                       final String   weblogDayPageId,
                       final Boolean  enableBloggerApi,
                       final WeblogCategoryData bloggerCategory,
                       final WeblogCategoryData defaultCategory,
                       final String   editorPage,
                       final String   ignoreWords,
                       final Boolean  allowComments,
                       final Boolean  emailComments,
                       final String   emailFromAddress,
                       final Boolean  enabled,
                       final String   emailAddress,
                       final Date     dateCreated)
    {
        this.id = id;
        this.name = name;
        this.handle = handle;
        this.description = description;
        this.mUser = user;
        this.defaultPageId = defaultPageId;
        this.weblogDayPageId = weblogDayPageId;
        this.enableBloggerApi = enableBloggerApi;
        this.bloggerCategory = bloggerCategory;
        this.defaultCategory = defaultCategory;
        this.editorPage = editorPage;
        this.ignoreWords = ignoreWords;
        this.allowComments = allowComments;
        this.emailComments = emailComments;
        this.emailFromAddress = emailFromAddress;
        this.enabled = enabled;
        this.emailAddress = emailAddress;
        this.dateCreated = dateCreated;
    }

    public WebsiteData(WebsiteData otherData)
    {
        this.setData(otherData);
    }

    /** 
     * @hibernate.bag lazy="true" inverse="true" cascade="delete" 
     * @hibernate.collection-key column="website_id"
     * @hibernate.collection-one-to-many 
     *    class="org.roller.pojos.PermissionsData"
     */
    public List getPermissions() 
    {
        return permissions;
    }
    public void setPermissions(List perms)
    {
        permissions = perms;
    }
    /** 
     * Remove permission from collection.
     */
    public void removePermission(PermissionsData perms)
    {
        permissions.remove(perms);
    }
    
    /**
     * Lookup the default page for this website.
     */
    public Template getDefaultPage() throws RollerException {
        
        Template template = null;
        
        // first check if this user has selected a theme
        // if so then return the themes Weblog template
        if(this.editorTheme != null && !this.editorTheme.equals(Theme.CUSTOM)) {
            try {
                ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                Theme usersTheme = themeMgr.getTheme(this.editorTheme);
                
                // this is a bit iffy :/
                // we assume that all theme use "Weblog" for a default template
                template = usersTheme.getTemplate("Weblog");
                
            } catch(ThemeNotFoundException tnfe) {
                // i sure hope not!
                mLogger.error(tnfe);
            }
        }
        
        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            template = userMgr.retrievePage(this.defaultPageId);
        }
        
        if(template != null)
            mLogger.debug("returning default template id ["+template.getId()+"]");
        
        return template;
    }
    
    
    /**
     * Lookup a Template for this website by id.
     */
    public Template getPageById(String id) throws RollerException {
        
        if(id == null)
            return null;
        
        Template template = null;
        
        // first check if this user has selected a theme
        // if so then return the proper theme template
        if(this.editorTheme != null && !this.editorTheme.equals(Theme.CUSTOM)) {
            
            // we don't actually expect to get lookups for theme pages by id
            // but we have to be thorough and check anyways
            String[] split = id.split(":",  2);
            
            // only continue if this looks like a theme id
            // and the theme name matches this users current theme
            if(split.length == 2 && split[0].equals(this.editorTheme)) {
                try {
                    ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                    Theme usersTheme = themeMgr.getTheme(this.editorTheme);
                    template = usersTheme.getTemplate(split[1]);
                    
                } catch(ThemeNotFoundException tnfe) {
                    // i sure hope not!
                    mLogger.error(tnfe);
                }
            }
            
        }
        
        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            template = userMgr.getPageByName(this, name);
        }
        
        return template;
    }
    
    
    /**
     * Lookup a Template for this website by name.
     */
    public Template getPageByName(String name) throws RollerException {
        
        if(name == null)
            return null;
        
        mLogger.debug("looking up template ["+name+"]");
        
        Template template = null;
        
        // first check if this user has selected a theme
        // if so then return the proper theme template
        if(this.editorTheme != null && !this.editorTheme.equals(Theme.CUSTOM)) {
            
            try {
                ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                Theme usersTheme = themeMgr.getTheme(this.editorTheme);
                template = usersTheme.getTemplate(name);

            } catch(ThemeNotFoundException tnfe) {
                // i sure hope not!
                mLogger.error(tnfe);
            }
            
        }
        
        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            template = userMgr.getPageByName(this, name);
        }
        
        if(template != null)
            mLogger.debug("returning template ["+template.getId()+"]");
        
        return template;
    }
    
    
    /**
     * Lookup a template for this website by link.
     */
    public Template getPageByLink(String link) throws RollerException {
        
        if(link == null)
            return null;
        
        mLogger.debug("looking up template ["+link+"]");
        
        Template template = null;
        
        // first check if this user has selected a theme
        // if so then return the proper theme template
        if(this.editorTheme != null && !this.editorTheme.equals(Theme.CUSTOM)) {
            
            try {
                ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                Theme usersTheme = themeMgr.getTheme(this.editorTheme);
                template = usersTheme.getTemplateByLink(link);

            } catch(ThemeNotFoundException tnfe) {
                // i sure hope not!
                mLogger.error(tnfe);
            }
            
        }
        
        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            template = userMgr.getPageByLink(this, link);
        }
        
        if(template != null)
            mLogger.debug("returning template ["+template.getId()+"]");
        
        return template;
    }
    
    
    /**
     * Get a list of all pages that are part of this website.
     */
    public List getPages() {
        
        Map pages = new HashMap();
        
        // first get the pages from the db
        try {
            Template template = null;
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            Iterator dbPages = userMgr.getPages(this).iterator();
            while(dbPages.hasNext()) {
                template = (Template) dbPages.next();
                pages.put(template.getName(), template);
            }
        } catch(Exception e) {
            // db error
            mLogger.error(e);
        }
        
            
        // now get theme pages if needed and put them in place of db pages
        if(this.editorTheme != null && !this.editorTheme.equals(Theme.CUSTOM)) {
            try {
                Template template = null;
                ThemeManager themeMgr = RollerFactory.getRoller().getThemeManager();
                Theme usersTheme = themeMgr.getTheme(this.editorTheme);
                Iterator themePages = usersTheme.getTemplates().iterator();
                while(themePages.hasNext()) {
                    template = (Template) themePages.next();
                    
                    // note that this will put theme pages over custom
                    // pages in the pages list, which is what we want
                    pages.put(template.getName(), template);
                }
            } catch(Exception e) {
                // how??
                mLogger.error(e);
            }
        }
        
        return new ArrayList(pages.values());
    }
    
    
    /**
     * Id of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.id column="id" type="string"
     *  generator-class="uuid.hex" unsaved-value="null"
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
     * Short URL safe string that uniquely identifies the website.
     * @ejb:persistent-field
     * @hibernate.property column="handle" non-null="true" unique="true"
     */
    public String getHandle()
    {
        return this.handle;
    }

    /** @ejb:persistent-field */
    public void setHandle(String handle)
    {
        this.handle = handle;
    }

    /**
     * Name of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
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
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
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
     * Original creator of website
     *
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="userid" cascade="none" not-null="true"
     */
    public org.roller.pojos.UserData getCreator()
    {
        return mUser;
    }

    /** @ejb:persistent-field */
    public void setCreator( org.roller.pojos.UserData ud )
    {
        mUser = ud;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultpageid" non-null="true" unique="false"
     */
    public String getDefaultPageId()
    {
        return this.defaultPageId;
    }

    /**
     * @ejb:persistent-field
     */
    public void setDefaultPageId(String defaultPageId)
    {
        this.defaultPageId = defaultPageId;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @deprecated
     * @ejb:persistent-field
     * @hibernate.property column="weblogdayid" non-null="true" unique="false"
     */
    public String getWeblogDayPageId()
    {
        return this.weblogDayPageId;
    }

    /**
     * @deprecated
     * @ejb:persistent-field
     */
    public void setWeblogDayPageId(String weblogDayPageId)
    {
        this.weblogDayPageId = weblogDayPageId;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="enablebloggerapi" non-null="true" unique="false"
     */
    public Boolean getEnableBloggerApi()
    {
        return this.enableBloggerApi;
    }

    /** @ejb:persistent-field */
    public void setEnableBloggerApi(Boolean enableBloggerApi)
    {
        this.enableBloggerApi = enableBloggerApi;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="bloggercatid" non-null="false"
     */
    public WeblogCategoryData getBloggerCategory()
    {
        return bloggerCategory;
    }

    /** @ejb:persistent-field */
    public void setBloggerCategory(WeblogCategoryData bloggerCategory)
    {
        this.bloggerCategory = bloggerCategory;
    }

    /**
     * By default,the default category for a weblog is the root and all macros
     * work with the top level categories that are immediately under the root.
     * Setting a different default category allows you to partition your weblog.
     * 
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="defaultcatid" non-null="false"
     */
    public WeblogCategoryData getDefaultCategory() 
    {
        return defaultCategory;
    }

    /** @ejb:persistent-field */
    public void setDefaultCategory(WeblogCategoryData defaultCategory)
    {
        this.defaultCategory = defaultCategory;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="editorpage" non-null="true" unique="false"
     */
    public String getEditorPage()
    {
        return this.editorPage;
    }

    /** @ejb:persistent-field */
    public void setEditorPage(String editorPage)
    {
        this.editorPage = editorPage;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="ignorewords" non-null="true" unique="false"
     */
    public String getIgnoreWords()
    {
        return this.ignoreWords;
    }

    /** @ejb:persistent-field */
    public void setIgnoreWords(String ignoreWords)
    {
        this.ignoreWords = ignoreWords;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="allowcomments" non-null="true" unique="false"
     */
    public Boolean getAllowComments()
    {
        return this.allowComments;
    }

    /** @ejb:persistent-field */
    public void setAllowComments(Boolean allowComments)
    {
        this.allowComments = allowComments;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="emailcomments" non-null="true" unique="false"
     */
    public Boolean getEmailComments()
    {
        return this.emailComments;
    }

    /** @ejb:persistent-field */
    public void setEmailComments(Boolean emailComments)
    {
        this.emailComments = emailComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="emailfromaddress" non-null="true" unique="false"
     */
    public String getEmailFromAddress()
    {
        return this.emailFromAddress;
    }

    /** @ejb:persistent-field */
    public void setEmailFromAddress(String emailFromAddress)
    {
        this.emailFromAddress = emailFromAddress;
    }
    
    /**
     * @ejb:persistent-field
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="emailaddress" non-null="true" unique="false"
     */
    public String getEmailAddress()
    {
        return this.emailAddress;
    }

    /** @ejb:persistent-field */
    public void setEmailAddress(String emailAddress)
    {
        this.emailAddress = emailAddress;
    }
    
    /**
     * EditorTheme of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="editortheme" non-null="true" unique="false"
     */
    public String getEditorTheme()
    {
        return this.editorTheme;
    }

    /** @ejb:persistent-field */
    public void setEditorTheme(String editorTheme)
    {
        this.editorTheme = editorTheme;
    }

    /**
     * Locale of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="locale" non-null="true" unique="false"
     */
    public String getLocale()
    {
        return this.locale;
    }

    /** @ejb:persistent-field */
    public void setLocale(String locale)
    {
        this.locale = locale;
    }

    /**
     * Timezone of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="timeZone" non-null="true" unique="false"
     */
    public String getTimeZone()
    {
        return this.timeZone;
    }

    /** @ejb:persistent-field */
    public void setTimeZone(String timeZone)
    {
        this.timeZone = timeZone;
    }

    /** 
    * @ejb:persistent-field 
    * @hibernate.property column="datecreated" non-null="true" unique="false"
    */
   public Date getDateCreated()
   {
       if (dateCreated == null) 
       {
           return null;
       }
       else 
       {
           return (Date)dateCreated.clone();
       }
   }
   /** @ejb:persistent-field */ 
   public void setDateCreated(final Date date)
   {
       if (date != null) 
       {
           dateCreated = (Date)date.clone();
       }
       else
       {
           dateCreated = null;
       }
   }

    /**
     * Comma-delimited list of user's default Plugins.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultplugins" non-null="false" unique="false"
     */
    public String getDefaultPlugins()
    {
        return mDefaultPlugins;
    }

    /** @ejb:persistent-field */
    public void setDefaultPlugins(String string)
    {
        mDefaultPlugins = string;
    }

    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="isenabled" non-null="true" unique="false"
     */
    public Boolean getEnabled()
    {
        return this.enabled;
    }
    
    /** @ejb:persistent-field */ 
    public void setEnabled(Boolean enabled)
    {
        this.enabled = enabled;
    }

    public String toString()
    {
        StringBuffer str = new StringBuffer("{");

        str.append("id=" + id + " " + "name=" + name + " " + "description=" +
                   description + " " +
                   "defaultPageId=" + defaultPageId + " " +
                   "weblogDayPageId=" + weblogDayPageId + " " +
                   "enableBloggerApi=" + enableBloggerApi + " " +
                   "bloggerCategory=" + bloggerCategory + " " +
                   "defaultCategory=" + defaultCategory + " " +
                   "editorPage=" + editorPage + " " +
                   "ignoreWords=" + ignoreWords + " " +
                   "allowComments=" + allowComments + " " +
                   "emailAddress=" + emailAddress + " " + 
                   "emailComments=" + emailComments + " " + 
                   "emailFromAddress=" + emailFromAddress + " " +
                   "editorTheme=" + editorTheme + " " +
                   "locale=" + locale + " " +
                   "timeZone=" + timeZone + " " +
                   "defaultPlugins=" + mDefaultPlugins);
        str.append('}');

        return (str.toString());
    }

    public boolean equals(Object pOther)
    {
        if (pOther instanceof WebsiteData)
        {
            WebsiteData lTest = (WebsiteData) pOther;
            boolean lEquals = true;

            lEquals = PojoUtil.equals(lEquals, this.id, lTest.id);

            lEquals = PojoUtil.equals(lEquals, this.name, lTest.name);

            lEquals = PojoUtil.equals(lEquals, this.description, lTest.description);

            lEquals = PojoUtil.equals(lEquals, this.mUser, lTest.mUser);

            lEquals = PojoUtil.equals(lEquals, this.defaultPageId, lTest.defaultPageId);

            lEquals = PojoUtil.equals(lEquals, this.weblogDayPageId, lTest.weblogDayPageId);

            lEquals = PojoUtil.equals(lEquals, this.enableBloggerApi, lTest.enableBloggerApi);

            lEquals = PojoUtil.equals(lEquals, this.bloggerCategory.getId(), lTest.bloggerCategory.getId());

            lEquals = PojoUtil.equals(lEquals, this.defaultCategory.getId(), lTest.defaultCategory.getId());

            lEquals = PojoUtil.equals(lEquals, this.editorPage, lTest.editorPage);

            lEquals = PojoUtil.equals(lEquals, this.ignoreWords, lTest.ignoreWords);

            lEquals = PojoUtil.equals(lEquals, this.allowComments, lTest.allowComments);
            
            lEquals = PojoUtil.equals(lEquals, this.emailComments, lTest.emailComments);
            
            lEquals = PojoUtil.equals(lEquals, this.emailAddress, lTest.emailAddress);
            
            lEquals = PojoUtil.equals(lEquals, this.emailFromAddress, lTest.emailFromAddress);

            lEquals = PojoUtil.equals(lEquals, this.editorTheme, lTest.editorTheme);

            lEquals = PojoUtil.equals(lEquals, this.locale, lTest.locale);

            lEquals = PojoUtil.equals(lEquals, this.timeZone, lTest.timeZone);

            lEquals = PojoUtil.equals(lEquals, this.mDefaultPlugins, lTest.mDefaultPlugins);
            
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
        result = PojoUtil.addHashCode(result, this.name);
        result = PojoUtil.addHashCode(result, this.description);
        result = PojoUtil.addHashCode(result, this.mUser);
        result = PojoUtil.addHashCode(result, this.defaultPageId);
        result = PojoUtil.addHashCode(result, this.weblogDayPageId);
        result = PojoUtil.addHashCode(result, this.enableBloggerApi);
        //result = PojoUtil.addHashCode(result, this.bloggerCategory);
        //result = PojoUtil.addHashCode(result, this.defaultCategory);
        result = PojoUtil.addHashCode(result, this.editorPage);
        result = PojoUtil.addHashCode(result, this.ignoreWords);
        result = PojoUtil.addHashCode(result, this.allowComments);
        result = PojoUtil.addHashCode(result, this.emailComments);
        result = PojoUtil.addHashCode(result, this.emailAddress);
        result = PojoUtil.addHashCode(result, this.emailFromAddress);
        result = PojoUtil.addHashCode(result, this.editorTheme);
        result = PojoUtil.addHashCode(result, this.locale);
        result = PojoUtil.addHashCode(result, this.timeZone);
        result = PojoUtil.addHashCode(result, this.mDefaultPlugins);

        return result;
    }

    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.roller.pojos.PersistentObject otherData)
    {
        WebsiteData other = (WebsiteData)otherData;

        this.id = other.id;
        this.name = other.name;
        this.handle = other.handle;
        this.description = other.description;
        this.mUser = other.mUser;
        this.defaultPageId = other.defaultPageId;
        this.weblogDayPageId = other.weblogDayPageId;
        this.enableBloggerApi = other.enableBloggerApi;
        this.bloggerCategory = other.bloggerCategory;
        this.defaultCategory = other.defaultCategory;
        this.editorPage = other.editorPage;
        this.ignoreWords = other.ignoreWords;
        this.allowComments = other.allowComments;
        this.emailComments = other.emailComments;
        this.emailAddress = other.emailAddress;
        this.emailFromAddress = other.emailFromAddress;
        this.editorTheme = other.editorTheme;
        this.locale = other.locale;
        this.timeZone = other.timeZone;
        this.mDefaultPlugins = other.mDefaultPlugins;
        this.enabled = other.enabled;
        this.dateCreated = dateCreated;
    }
    
    /**
     * Parse locale value and instantiate a Locale object,
     * otherwise return default Locale.
     *
     * @roller.wrapPojoMethod type="simple"
     * @return Locale
     */
    public Locale getLocaleInstance()
    {
        if (locale != null)
        {
            String[] localeStr = StringUtils.split(locale,"_");
            if (localeStr.length == 1)
            {
                if (localeStr[0] == null) localeStr[0] = "";
                return new Locale(localeStr[0]);
            }
            else if (localeStr.length == 2)
            {
                if (localeStr[0] == null) localeStr[0] = "";
                if (localeStr[1] == null) localeStr[1] = "";
                return new Locale(localeStr[0], localeStr[1]);
            }
            else if (localeStr.length == 3)
            {
                if (localeStr[0] == null) localeStr[0] = "";
                if (localeStr[1] == null) localeStr[1] = "";
                if (localeStr[2] == null) localeStr[2] = "";
                return new Locale(localeStr[0], localeStr[1], localeStr[2]);
            } 
        } 
        return Locale.getDefault();
    }
    
    /**
     * Return TimeZone instance for value of timeZone,
     * otherwise return system default instance.
     *
     * @roller.wrapPojoMethod type="simple"
     * @return TimeZone
     */
    public TimeZone getTimeZoneInstance()
    {
    	if (timeZone == null) 
        {
            if (TimeZone.getDefault() != null) 
            {
                this.setTimeZone( TimeZone.getDefault().getID() );
            }
            else
            {
                this.setTimeZone("America/New_York");
            }
        }
        return TimeZone.getTimeZone(timeZone);
    }
    
    /** 
     * @see org.roller.pojos.PersistentObject#remove()
     */
    public void remove() throws RollerException
    {
        RollerFactory.getRoller().getUserManager().removeWebsiteContents(this);        
        super.remove();
    }

    public boolean canSave() throws RollerException
    {
        Roller roller = RollerFactory.getRoller();
        if (roller.getUser().equals(UserData.SYSTEM_USER)) 
        {
            return true;
        }
        if (hasUserPermissions(roller.getUser(), 
                (short)(PermissionsData.ADMIN|PermissionsData.AUTHOR)))
        {
            return true;
        }
        return false;
    }

    /**
     * Returns true if user has all permissions specified by mask.
     */
    public boolean hasUserPermissions(UserData user, short mask)
    {
        // look for user in website's permissions
        PermissionsData userPerms = null;
        Iterator iter = getPermissions().iterator();
        while (iter.hasNext())
        {
            PermissionsData perms = (PermissionsData) iter.next();
            if (perms.getUser().getId().equals(user.getId())) 
            {
                userPerms = perms;
                break;
            }
        }
        // if we found one, does it satisfy the mask?
        if (userPerms != null && !userPerms.isPending())
        {
            if (userPerms != null && (userPerms.getPermissionMask() & mask) == mask) 
            {
                return true;
            }
        }
        // otherwise, check to see if user is a global admin
        if (user != null && user.hasRole("admin")) return true;
        return false;
    }
    
    /** Get number of users associated with website */
    public int getUserCount() 
    {
        return getPermissions().size();
    }
    
    /** No-op needed to please XDoclet generated code */
    private int userCount = 0; 
    public void setUserCount(int userCount)
    {
        // no-op
    }
    
    public int getAdminUserCount() 
    {
        int count = 0;
        PermissionsData userPerms = null;
        Iterator iter = getPermissions().iterator();
        while (iter.hasNext())
        {
            PermissionsData perms = (PermissionsData) iter.next();
            if (perms.getPermissionMask() == PermissionsData.ADMIN) 
            {
                count++;
            }
        }
        return count;
    }
    
    /** No-op needed to please XDoclet generated code */
    private int adminUserCount = 0; 
    public void setAdminUserCount(int adminUserCount) 
    {
        // no-op
    }
}

