package org.roller.pojos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang.StringUtils;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.util.PojoUtil;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.ThemeNotFoundException;
import org.roller.model.Template;
import org.roller.model.ThemeManager;
import org.roller.model.UserManager;


/**
 * A user's website is a weweblog, newsfeed channels and bookmarks.
 * @author David M Johnson
 *
 * @ejb:bean name="WebsiteData"
 * @struts.form include-all="true"
 * @hibernate.class table="website"
 * hibernate.jcs-cache usage="read-write"
 */
public class WebsiteData extends org.roller.pojos.PersistentObject
    implements java.io.Serializable
{
    static final long serialVersionUID = 206437645033737127L;
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(WebsiteData.class);
    
    protected java.lang.String id;
    protected java.lang.String name;
    protected java.lang.String description;
    protected java.lang.String defaultPageId;
    protected java.lang.String weblogDayPageId;
    protected java.lang.Boolean enableBloggerApi;
    protected WeblogCategoryData bloggerCategory;
    protected WeblogCategoryData defaultCategory;
    protected java.lang.String editorPage;
    protected java.lang.String ignoreWords;
    protected java.lang.Boolean allowComments;
    protected java.lang.Boolean emailComments;
    protected java.lang.String emailFromAddress;
    protected java.lang.String editorTheme;
    protected java.lang.String locale;
    protected java.lang.String timezone;
    protected java.lang.String mDefaultPlugins;
    protected java.lang.Boolean isEnabled;

    protected UserData mUser = null;

    public WebsiteData()
    {
    }

    public WebsiteData(final java.lang.String id, 
                       final java.lang.String name,
                       final java.lang.String description,
                       final UserData user,
                       final java.lang.String defaultPageId,
                       final java.lang.String weblogDayPageId,
                       final java.lang.Boolean enableBloggerApi,
                       final WeblogCategoryData bloggerCategory,
                       final WeblogCategoryData defaultCategory,
                       final java.lang.String editorPage,
                       final java.lang.String ignoreWords,
                       final java.lang.Boolean allowComments,
                       final java.lang.Boolean emailComments,
                       final java.lang.String emailFromAddress,
                       final java.lang.Boolean isEnabled)
    {
        this.id = id;
        this.name = name;
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
        this.isEnabled = isEnabled;
    }

    public WebsiteData(WebsiteData otherData)
    {
        this.setData(otherData);
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
     * Name of the Website.
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
     * Id of owner.
     * @ejb:persistent-field
     * @hibernate.many-to-one column="userid" cascade="none" not-null="true"
     */
    public org.roller.pojos.UserData getUser()
    {
        return mUser;
    }

    /** @ejb:persistent-field */
    public void setUser( org.roller.pojos.UserData ud )
    {
        mUser = ud;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="defaultpageid" non-null="true" unique="false"
     */
    public java.lang.String getDefaultPageId()
    {
        return this.defaultPageId;
    }

    /**
     * @ejb:persistent-field
     */
    public void setDefaultPageId(java.lang.String defaultPageId)
    {
        this.defaultPageId = defaultPageId;
    }

    /**
     * @deprecated
     * @ejb:persistent-field
     * @hibernate.property column="weblogdayid" non-null="true" unique="false"
     */
    public java.lang.String getWeblogDayPageId()
    {
        return this.weblogDayPageId;
    }

    /**
     * @deprecated
     * @ejb:persistent-field
     */
    public void setWeblogDayPageId(java.lang.String weblogDayPageId)
    {
        this.weblogDayPageId = weblogDayPageId;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="enablebloggerapi" non-null="true" unique="false"
     */
    public java.lang.Boolean getEnableBloggerApi()
    {
        return this.enableBloggerApi;
    }

    /** @ejb:persistent-field */
    public void setEnableBloggerApi(java.lang.Boolean enableBloggerApi)
    {
        this.enableBloggerApi = enableBloggerApi;
    }

    /**
     * @ejb:persistent-field
     * 
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
     * @ejb:persistent-field
     * 
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
     * @ejb:persistent-field
     * @hibernate.property column="editorpage" non-null="true" unique="false"
     */
    public java.lang.String getEditorPage()
    {
        return this.editorPage;
    }

    /** @ejb:persistent-field */
    public void setEditorPage(java.lang.String editorPage)
    {
        this.editorPage = editorPage;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="ignorewords" non-null="true" unique="false"
     */
    public java.lang.String getIgnoreWords()
    {
        return this.ignoreWords;
    }

    /** @ejb:persistent-field */
    public void setIgnoreWords(java.lang.String ignoreWords)
    {
        this.ignoreWords = ignoreWords;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="allowcomments" non-null="true" unique="false"
     */
    public java.lang.Boolean getAllowComments()
    {
        return this.allowComments;
    }

    /** @ejb:persistent-field */
    public void setAllowComments(java.lang.Boolean allowComments)
    {
        this.allowComments = allowComments;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="emailcomments" non-null="true" unique="false"
     */
    public java.lang.Boolean getEmailComments()
    {
        return this.emailComments;
    }

    /** @ejb:persistent-field */
    public void setEmailComments(java.lang.Boolean emailComments)
    {
        this.emailComments = emailComments;
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="emailfromaddress" non-null="true" unique="false"
     */
    public java.lang.String getEmailFromAddress()
    {
        return this.emailFromAddress;
    }

    /** @ejb:persistent-field */
    public void setEmailFromAddress(java.lang.String emailFromAddress)
    {
        this.emailFromAddress = emailFromAddress;
    }
    
    /**
     * EditorTheme of the Website.
     * @ejb:persistent-field
     * @hibernate.property column="editortheme" non-null="true" unique="false"
     */
    public java.lang.String getEditorTheme()
    {
        return this.editorTheme;
    }

    /** @ejb:persistent-field */
    public void setEditorTheme(java.lang.String editorTheme)
    {
        this.editorTheme = editorTheme;
    }

    /**
     * Locale of the Website.
     * @ejb:persistent-field
     * @hibernate.property column="locale" non-null="true" unique="false"
     */
    public java.lang.String getLocale()
    {
        return this.locale;
    }

    /** @ejb:persistent-field */
    public void setLocale(java.lang.String locale)
    {
        this.locale = locale;
    }

    /**
     * Timezone of the Website.
     * @ejb:persistent-field
     * @hibernate.property column="timezone" non-null="true" unique="false"
     */
    public java.lang.String getTimezone()
    {
        return this.timezone;
    }

    /** @ejb:persistent-field */
    public void setTimezone(java.lang.String timezone)
    {
        this.timezone = timezone;
    }

    /**
     * Comma-delimited list of user's default Plugins.
     * @ejb:persistent-field
     * @hibernate.property column="defaultplugins" non-null="false" unique="false"
     */
    public java.lang.String getDefaultPlugins()
    {
        return mDefaultPlugins;
    }

    /** @ejb:persistent-field */
    public void setDefaultPlugins(java.lang.String string)
    {
        mDefaultPlugins = string;
    }

    /**
     * @ejb:persistent-field
     * @hibernate.property column="isenabled" non-null="true" unique="false"
     */
    public java.lang.Boolean getIsEnabled()
    {
        return this.isEnabled;
    }
    
    /** @ejb:persistent-field */ 
    public void setIsEnabled(java.lang.Boolean isEnabled)
    {
        this.isEnabled = isEnabled;
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
                   "emailComments=" + emailComments + " " + 
                   "emailFromAddress=" + emailFromAddress + " " +
                   "editorTheme=" + editorTheme + " " +
                   "locale=" + locale + " " +
                   "timezone=" + timezone + " " +
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
            
            lEquals = PojoUtil.equals(lEquals, this.emailFromAddress, lTest.emailFromAddress);

            lEquals = PojoUtil.equals(lEquals, this.editorTheme, lTest.editorTheme);

            lEquals = PojoUtil.equals(lEquals, this.locale, lTest.locale);

            lEquals = PojoUtil.equals(lEquals, this.timezone, lTest.timezone);

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
        result = PojoUtil.addHashCode(result, this.emailFromAddress);
        result = PojoUtil.addHashCode(result, this.editorTheme);
        result = PojoUtil.addHashCode(result, this.locale);
        result = PojoUtil.addHashCode(result, this.timezone);
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
        this.emailFromAddress = other.emailFromAddress;
        this.editorTheme = other.editorTheme;
        this.locale = other.locale;
        this.timezone = other.timezone;
        this.mDefaultPlugins = other.mDefaultPlugins;
        this.isEnabled = other.isEnabled;
    }
    
    /**
     * Parse locale value and instantiate a Locale object,
     * otherwise return default Locale.
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
     * Return TimeZone instance for value of timezone,
     * otherwise return system default instance.
     * @return TimeZone
     */
    public TimeZone getTimeZoneInstance()
    {
    	if (timezone == null) 
        {
            if (TimeZone.getDefault() != null) 
            {
                this.setTimezone( TimeZone.getDefault().getID() );
            }
            else
            {
                this.setTimezone("America/New_York");
            }
        }
        return TimeZone.getTimeZone(timezone);
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
        if (roller.getUser().equals(getUser()))
        {
            return true;
        }
        return false;
    }

}