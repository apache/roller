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

package org.apache.roller.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import org.apache.commons.lang.StringUtils;
import org.apache.roller.RollerException;
import org.apache.roller.business.referrers.RefererManager;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.util.PojoUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.business.ThemeNotFoundException;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.business.BookmarkManager;
import org.apache.roller.business.PluginManager;
import org.apache.roller.business.Roller;
import org.apache.roller.business.ThemeManager;
import org.apache.roller.business.UserManager;
import org.apache.roller.business.WeblogManager;

/**
 * Website has many-to-many association with users. Website has one-to-many and
 * one-direction associations with weblog entries, weblog categories, folders and
 * other objects. Use UserManager to create, fetch, update and retreive websites.
 *
 * @author David M Johnson
 *
 * @ejb:bean name="WebsiteData"
 * @struts.form include-all="true"
 * @hibernate.class lazy="false"  table="website"
 * @hibernate.cache usage="read-write"
 */
public class WebsiteData extends org.apache.roller.pojos.PersistentObject
        implements Serializable {
    public static final long serialVersionUID = 206437645033737127L;
    
    private static Log log = LogFactory.getLog(WebsiteData.class);
    
    // Simple properties
    private String  id               = null;
    private String  handle           = null;
    private String  name             = null;
    private String  description      = null;
    private String  defaultPageId    = "dummy";
    private String  weblogDayPageId  = "dummy";
    private Boolean enableBloggerApi = Boolean.TRUE;
    private String  editorPage       = null;
    private String  blacklist        = null;
    private Boolean allowComments    = Boolean.TRUE;
    private Boolean emailComments    = Boolean.FALSE;
    private String  emailFromAddress = null;
    private String  emailAddress     = null;
    private String  editorTheme      = null;
    private String  locale           = null;
    private String  timeZone         = null;
    private String  defaultPlugins   = null;
    private Boolean enabled          = Boolean.TRUE;
    private Boolean active           = Boolean.TRUE;
    private Date    dateCreated      = new java.util.Date();
    private Boolean defaultAllowComments = Boolean.TRUE;
    private int     defaultCommentDays = 0;
    private Boolean moderateComments  = Boolean.FALSE;
    private int     entryDisplayCount = 15;
    private Date    lastModified     = new Date();
    private String  pageModels       = new String();
    private boolean enableMultiLang = false;
    private boolean showAllLangs = true;
    
    
    // Associated objects
    private UserData           creator = null; 
    private List               permissions = new ArrayList();
    private WeblogCategoryData bloggerCategory = null;
    private WeblogCategoryData defaultCategory = null;
    
    private Map initializedPlugins = null;
    
    public WebsiteData() {    
    }
    
    public WebsiteData(
            String handle,
            UserData creator,
            String name,
            String desc,
            String email,
            String emailFrom,
            String editorTheme,
            String locale,
            String timeZone) {
        
        this.handle = handle;
        this.creator = creator;
        this.name = name;
        this.description = desc;
        this.emailAddress = email;
        this.emailFromAddress = emailFrom;
        this.editorTheme = editorTheme;
        this.locale = locale;
        this.timeZone = timeZone;
    }
    
    public WebsiteData(WebsiteData otherData) {
        this.setData(otherData);
    }
    
    /**
     * @hibernate.bag lazy="true" inverse="true" cascade="delete"
     * @hibernate.collection-key column="website_id"
     * @hibernate.collection-one-to-many
     *    class="org.apache.roller.pojos.PermissionsData"
     */
    public List getPermissions() {
        return permissions;
    }
    public void setPermissions(List perms) {
        permissions = perms;
    }
    /**
     * Remove permission from collection.
     */
    public void removePermission(PermissionsData perms) {
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
                log.error(tnfe);
            }
        }
        
        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            template = userMgr.getPage(this.defaultPageId);
        }
        
        if(template != null)
            log.debug("returning default template id ["+template.getId()+"]");
        
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
                    log.error(tnfe);
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
     * @roller.wrapPojoMethod type="pojo"
     */
    public Template getPageByName(String name) throws RollerException {
        
        if(name == null)
            return null;
        
        log.debug("looking up template ["+name+"]");
        
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
                log.error(tnfe);
            }
            
        }
        
        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            template = userMgr.getPageByName(this, name);
        }
        
        if(template != null)
            log.debug("returning template ["+template.getId()+"]");
        
        return template;
    }
    
    
    /**
     * Lookup a template for this website by link.
     * @roller.wrapPojoMethod type="pojo"
     */
    public Template getPageByLink(String link) throws RollerException {
        
        if(link == null)
            return null;
        
        log.debug("looking up template ["+link+"]");
        
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
                log.error(tnfe);
            }
            
        }
        
        // if we didn't get the Template from a theme then look in the db
        if(template == null) {
            UserManager userMgr = RollerFactory.getRoller().getUserManager();
            template = userMgr.getPageByLink(this, link);
        }
        
        if(template != null)
            log.debug("returning template ["+template.getId()+"]");
        
        return template;
    }
    
    
    /**
     * Get a list of all pages that are part of this website.
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.Template"
     */
    public List getPages() {
        
        Map pages = new TreeMap();
        
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
            log.error(e);
        }
        
        
        // now get theme pages if needed and put them in place of db pages
        if (this.editorTheme != null && !this.editorTheme.equals(Theme.CUSTOM)) {
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
                log.error(e);
            }
        }
        
        return new ArrayList(pages.values());
    }
    
    
    /**
     * Id of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.id column="id"
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
     * Short URL safe string that uniquely identifies the website.
     * @ejb:persistent-field
     * @hibernate.property column="handle" non-null="true" unique="true"
     * @roller.wrapPojoMethod type="simple"
     */
    public String getHandle() {
        return this.handle;
    }
    
    /** @ejb:persistent-field */
    public void setHandle(String handle) {
        this.handle = handle;
    }
    
    /**
     * Name of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="name" non-null="true" unique="false"
     */
    public String getName() {
        return this.name;
    }
    
    /** @ejb:persistent-field */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Description
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="description" non-null="true" unique="false"
     */
    public String getDescription() {
        return this.description;
    }
    
    /** @ejb:persistent-field */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Original creator of website
     *
     * @roller.wrapPojoMethod type="pojo"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="userid" cascade="none" not-null="true"
     */
    public org.apache.roller.pojos.UserData getCreator() {
        return creator;
    }
    
    /** @ejb:persistent-field */
    public void setCreator( org.apache.roller.pojos.UserData ud ) {
        creator = ud;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultpageid" non-null="true" unique="false"
     */
    public String getDefaultPageId() {
        return this.defaultPageId;
    }
    
    /**
     * @ejb:persistent-field
     */
    public void setDefaultPageId(String defaultPageId) {
        this.defaultPageId = defaultPageId;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @deprecated
     * @ejb:persistent-field
     * @hibernate.property column="weblogdayid" non-null="true" unique="false"
     */
    public String getWeblogDayPageId() {
        return this.weblogDayPageId;
    }
    
    /**
     * @deprecated
     * @ejb:persistent-field
     */
    public void setWeblogDayPageId(String weblogDayPageId) {
        this.weblogDayPageId = weblogDayPageId;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="enablebloggerapi" non-null="true" unique="false"
     */
    public Boolean getEnableBloggerApi() {
        return this.enableBloggerApi;
    }
    
    /** @ejb:persistent-field */
    public void setEnableBloggerApi(Boolean enableBloggerApi) {
        this.enableBloggerApi = enableBloggerApi;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.many-to-one column="bloggercatid" non-null="false"
     */
    public WeblogCategoryData getBloggerCategory() {
        return bloggerCategory;
    }
    
    /** @ejb:persistent-field */
    public void setBloggerCategory(WeblogCategoryData bloggerCategory) {
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
    public WeblogCategoryData getDefaultCategory() {
        return defaultCategory;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultCategory(WeblogCategoryData defaultCategory) {
        this.defaultCategory = defaultCategory;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="editorpage" non-null="true" unique="false"
     */
    public String getEditorPage() {
        return this.editorPage;
    }
    
    /** @ejb:persistent-field */
    public void setEditorPage(String editorPage) {
        this.editorPage = editorPage;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="blacklist" non-null="true" unique="false"
     */
    public String getBlacklist() {
        return this.blacklist;
    }
    
    /** @ejb:persistent-field */
    public void setBlacklist(String blacklist) {
        this.blacklist = blacklist;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="allowcomments" non-null="true" unique="false"
     */
    public Boolean getAllowComments() {
        return this.allowComments;
    }
    
    /** @ejb:persistent-field */
    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultallowcomments" non-null="true" unique="false"
     */
    public Boolean getDefaultAllowComments() {
        return defaultAllowComments;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultAllowComments(Boolean defaultAllowComments) {
        this.defaultAllowComments = defaultAllowComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="defaultcommentdays" non-null="true" unique="false"
     */
    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="commentmod" non-null="true" unique="false"
     */
    public Boolean getModerateComments() {
        return moderateComments;
    }
    
    /** @ejb:persistent-field */
    public void setModerateComments(Boolean moderateComments) {
        this.moderateComments = moderateComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="emailcomments" non-null="true" unique="false"
     */
    public Boolean getEmailComments() {
        return this.emailComments;
    }
    
    /** @ejb:persistent-field */
    public void setEmailComments(Boolean emailComments) {
        this.emailComments = emailComments;
    }
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="emailfromaddress" non-null="true" unique="false"
     */
    public String getEmailFromAddress() {
        return this.emailFromAddress;
    }
    
    /** @ejb:persistent-field */
    public void setEmailFromAddress(String emailFromAddress) {
        this.emailFromAddress = emailFromAddress;
    }
    
    /**
     * @ejb:persistent-field
     * @roller.wrapPojoMethod type="simple"
     * @hibernate.property column="emailaddress" non-null="true" unique="false"
     */
    public String getEmailAddress() {
        return this.emailAddress;
    }
    
    /** @ejb:persistent-field */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }
    
    /**
     * EditorTheme of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="editortheme" non-null="true" unique="false"
     */
    public String getEditorTheme() {
        return this.editorTheme;
    }
    
    /** @ejb:persistent-field */
    public void setEditorTheme(String editorTheme) {
        this.editorTheme = editorTheme;
    }
    
    /**
     * Locale of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="locale" non-null="true" unique="false"
     */
    public String getLocale() {
        return this.locale;
    }
    
    /** @ejb:persistent-field */
    public void setLocale(String locale) {
        this.locale = locale;
    }
    
    /**
     * Timezone of the Website.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="timeZone" non-null="true" unique="false"
     */
    public String getTimeZone() {
        return this.timeZone;
    }
    
    /** @ejb:persistent-field */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    /**
     * @ejb:persistent-field
     * @hibernate.property column="datecreated" non-null="true" unique="false"
     * @roller.wrapPojoMethod type="simple"
     */
    public Date getDateCreated() {
        if (dateCreated == null) {
            return null;
        } else {
            return (Date)dateCreated.clone();
        }
    }
    /** @ejb:persistent-field */
    public void setDateCreated(final Date date) {
        if (date != null) {
            dateCreated = (Date)date.clone();
        } else {
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
    public String getDefaultPlugins() {
        return defaultPlugins;
    }
    
    /** @ejb:persistent-field */
    public void setDefaultPlugins(String string) {
        defaultPlugins = string;
    }
    
    public String toString() {
        StringBuffer str = new StringBuffer("{");
        
        str.append("id=" + id + " " + "name=" + name + " " + "description=" +
                description + " " +
                "defaultPageId=" + defaultPageId + " " +
                "weblogDayPageId=" + weblogDayPageId + " " +
                "enableBloggerApi=" + enableBloggerApi + " " +
                "bloggerCategory=" + bloggerCategory + " " +
                "defaultCategory=" + defaultCategory + " " +
                "editorPage=" + editorPage + " " +
                "blacklist=" + blacklist + " " +
                "allowComments=" + allowComments + " " +
                "emailAddress=" + emailAddress + " " +
                "emailComments=" + emailComments + " " +
                "emailFromAddress=" + emailFromAddress + " " +
                "editorTheme=" + editorTheme + " " +
                "locale=" + locale + " " +
                "timeZone=" + timeZone + " " +
                "defaultPlugins=" + defaultPlugins);
        str.append('}');
        
        return (str.toString());
    }
    
    public boolean equals(Object pOther) {
        if (pOther instanceof WebsiteData) {
            WebsiteData lTest = (WebsiteData) pOther;
            boolean lEquals = true;
            lEquals = PojoUtil.equals(lEquals, this.getId(), lTest.getId());
            lEquals = PojoUtil.equals(lEquals, this.getName(), lTest.getName());
            lEquals = PojoUtil.equals(lEquals, this.getDescription(), lTest.getDescription());
            lEquals = PojoUtil.equals(lEquals, this.getCreator(), lTest.getCreator());
            lEquals = PojoUtil.equals(lEquals, this.getDefaultPageId(), lTest.getDefaultPageId());
            lEquals = PojoUtil.equals(lEquals, this.getWeblogDayPageId(), lTest.getWeblogDayPageId());
            lEquals = PojoUtil.equals(lEquals, this.getEnableBloggerApi(), lTest.getEnableBloggerApi());
            lEquals = PojoUtil.equals(lEquals, this.getBloggerCategory(), lTest.getBloggerCategory());
            lEquals = PojoUtil.equals(lEquals, this.getDefaultCategory(), lTest.getDefaultCategory());
            lEquals = PojoUtil.equals(lEquals, this.getEditorPage(), lTest.getEditorPage());
            lEquals = PojoUtil.equals(lEquals, this.getBlacklist(), lTest.getBlacklist());
            lEquals = PojoUtil.equals(lEquals, this.getAllowComments(), lTest.getAllowComments());
            lEquals = PojoUtil.equals(lEquals, this.getEmailComments(), lTest.getEmailComments());
            lEquals = PojoUtil.equals(lEquals, this.getEmailAddress(), lTest.getEmailAddress());
            lEquals = PojoUtil.equals(lEquals, this.getEmailFromAddress(), lTest.getEmailFromAddress());
            lEquals = PojoUtil.equals(lEquals, this.getEditorTheme(), lTest.getEditorTheme());
            lEquals = PojoUtil.equals(lEquals, this.getLocale(), lTest.getLocale());
            lEquals = PojoUtil.equals(lEquals, this.getTimeZone(), lTest.getTimeZone());
            lEquals = PojoUtil.equals(lEquals, this.getDefaultPlugins(), lTest.getDefaultPlugins());
            return lEquals;
        } else {
            return false;
        }
    }
    
    public int hashCode() {
        int result = 17;
        result = PojoUtil.addHashCode(result, this.id);
        result = PojoUtil.addHashCode(result, this.name);
        result = PojoUtil.addHashCode(result, this.description);
        result = PojoUtil.addHashCode(result, this.creator);
        result = PojoUtil.addHashCode(result, this.defaultPageId);
        result = PojoUtil.addHashCode(result, this.weblogDayPageId);
        result = PojoUtil.addHashCode(result, this.enableBloggerApi);
        //result = PojoUtil.addHashCode(result, this.bloggerCategory);
        //result = PojoUtil.addHashCode(result, this.defaultCategory);
        result = PojoUtil.addHashCode(result, this.editorPage);
        result = PojoUtil.addHashCode(result, this.blacklist);
        result = PojoUtil.addHashCode(result, this.allowComments);
        result = PojoUtil.addHashCode(result, this.emailComments);
        result = PojoUtil.addHashCode(result, this.emailAddress);
        result = PojoUtil.addHashCode(result, this.emailFromAddress);
        result = PojoUtil.addHashCode(result, this.editorTheme);
        result = PojoUtil.addHashCode(result, this.locale);
        result = PojoUtil.addHashCode(result, this.timeZone);
        result = PojoUtil.addHashCode(result, this.defaultPlugins);
        
        return result;
    }
    
    /**
     * Setter is needed in RollerImpl.storePersistentObject()
     */
    public void setData(org.apache.roller.pojos.PersistentObject otherData) {
        WebsiteData other = (WebsiteData)otherData;
        
        this.id = other.getId();
        this.name = other.getName();
        this.handle = other.getHandle();
        this.description = other.getDescription();
        this.creator = other.getCreator();
        this.defaultPageId = other.getDefaultPageId();
        this.weblogDayPageId = other.getWeblogDayPageId();
        this.enableBloggerApi = other.getEnableBloggerApi();
        this.bloggerCategory = other.getBloggerCategory();
        this.defaultCategory = other.getDefaultCategory();
        this.editorPage = other.getEditorPage();
        this.blacklist = other.getBlacklist();
        this.allowComments = other.getAllowComments();
        this.emailComments = other.getEmailComments();
        this.emailAddress = other.getEmailAddress();
        this.emailFromAddress = other.getEmailFromAddress();
        this.editorTheme = other.getEditorTheme();
        this.locale = other.getLocale();
        this.timeZone = other.getTimeZone();
        this.defaultPlugins = other.getDefaultPlugins();
        this.enabled = other.getEnabled();
        this.dateCreated = other.getDateCreated();
        this.entryDisplayCount = other.getEntryDisplayCount();
        this.active = other.getActive();
        this.lastModified = other.getLastModified();
    }
    
    /**
     * Parse locale value and instantiate a Locale object,
     * otherwise return default Locale.
     *
     * @roller.wrapPojoMethod type="simple"
     * @return Locale
     */
    public Locale getLocaleInstance() {
        if (locale != null) {
            String[] localeStr = StringUtils.split(locale,"_");
            if (localeStr.length == 1) {
                if (localeStr[0] == null) localeStr[0] = "";
                return new Locale(localeStr[0]);
            } else if (localeStr.length == 2) {
                if (localeStr[0] == null) localeStr[0] = "";
                if (localeStr[1] == null) localeStr[1] = "";
                return new Locale(localeStr[0], localeStr[1]);
            } else if (localeStr.length == 3) {
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
    public TimeZone getTimeZoneInstance() {
        if (timeZone == null) {
            if (TimeZone.getDefault() != null) {
                this.setTimeZone( TimeZone.getDefault().getID() );
            } else {
                this.setTimeZone("America/New_York");
            }
        }
        return TimeZone.getTimeZone(timeZone);
    }
    
    
    /**
     * Returns true if user has all permissions specified by mask.
     */
    public boolean hasUserPermissions(UserData user, short mask) {
        // look for user in website's permissions
        PermissionsData userPerms = null;
        Iterator iter = getPermissions().iterator();
        while (iter.hasNext()) {
            PermissionsData perms = (PermissionsData) iter.next();
            if (perms.getUser().getId().equals(user.getId())) {
                userPerms = perms;
                break;
            }
        }
        // if we found one, does it satisfy the mask?
        if (userPerms != null && !userPerms.isPending()) {
            if (userPerms != null && (userPerms.getPermissionMask() & mask) == mask) {
                return true;
            }
        }
        // otherwise, check to see if user is a global admin
        if (user != null && user.hasRole("admin")) return true;
        return false;
    }
    
    /** Get number of users associated with website */
    public int getUserCount() {
        return getPermissions().size();
    }
    
    /** No-op needed to please XDoclet generated code */
    private int userCount = 0;
    public void setUserCount(int userCount) {
        // no-op
    }
    
    public int getAdminUserCount() {
        int count = 0;
        PermissionsData userPerms = null;
        Iterator iter = getPermissions().iterator();
        while (iter.hasNext()) {
            PermissionsData perms = (PermissionsData) iter.next();
            if (perms.getPermissionMask() == PermissionsData.ADMIN) {
                count++;
            }
        }
        return count;
    }
    
    /** No-op needed to please XDoclet generated code */
    private int adminUserCount = 0;
    public void setAdminUserCount(int adminUserCount) {
        // no-op
    }
    
    
    /**
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="displaycnt" not-null="true"
     */
    public int getEntryDisplayCount() {
        return entryDisplayCount;
    }
    
    /**
     * @ejb:persistent-field
     */
    public void setEntryDisplayCount(int entryDisplayCount) {
        this.entryDisplayCount = entryDisplayCount;
    }
    
    /**
     * Set to FALSE to completely disable and hide this weblog from public view.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="isenabled" non-null="true" unique="false"
     */
    public Boolean getEnabled() {
        return this.enabled;
    }
    
    /** @ejb:persistent-field */
    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
    
    /**
     * Set to FALSE to exclude this weblog from community areas such as the
     * front page and the planet page.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="isactive" not-null="true"
     */
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    /**
     * Returns true if comment moderation is required by website or config.
     */ 
    public boolean getCommentModerationRequired() { 
        return (getModerateComments().booleanValue()
         || RollerRuntimeConfig.getBooleanProperty("users.moderation.required"));
    }
    
    /** No-op */
    public void setCommentModerationRequired(boolean modRequired) {}    

    
    /**
     * The last time any visible part of this weblog was modified.
     * This includes a change to weblog settings, entries, themes, templates, 
     * comments, categories, bookmarks, folders, etc.
     *
     * Pings and Referrers are explicitly not included because pings to not
     * affect visible changes to a weblog, and referrers change so often that
     * it would diminish the usefulness of the attribute.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="lastmodified" not-null="true"
     */
    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }
  
    
    /**
     * Is multi-language blog support enabled for this weblog?
     *
     * If false then urls with various locale restrictions should fail.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="enablemultilang" not-null="true"
     */
    public boolean isEnableMultiLang() {
        return enableMultiLang;
    }

    public void setEnableMultiLang(boolean enableMultiLang) {
        this.enableMultiLang = enableMultiLang;
    }
    
    
    /**
     * Should the default weblog view show entries from all languages?
     *
     * If false then the default weblog view only shows entry from the
     * default locale chosen for this weblog.
     *
     * @roller.wrapPojoMethod type="simple"
     * @ejb:persistent-field
     * @hibernate.property column="showalllangs" not-null="true"
     */
    public boolean isShowAllLangs() {
        return showAllLangs;
    }

    public void setShowAllLangs(boolean showAllLangs) {
        this.showAllLangs = showAllLangs;
    }
    
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     */
    public String getURL() {
        // TODO: ATLAS reconcile entry.getPermaLink() with new URLs
        String relPath = RollerRuntimeConfig.getRelativeContextURL();
        return relPath + "/" + getHandle();
        //return URLUtilities.getWeblogURL(this, null, false);
    }
    public void setURL(String url) {
        // noop
    }
    
    
    /** 
     * @roller.wrapPojoMethod type="simple"
     */
    public String getAbsoluteURL() {
        // TODO: ATLAS reconcile entry.getPermaLink() with new URLs
        String relPath = RollerRuntimeConfig.getAbsoluteContextURL();
        return relPath + "/" + getHandle();
        //return URLUtilities.getWeblogURL(this, null, true);
    }
    public void setAbsoluteURL(String url) {
        // noop
    }
    
    
    /**
     * Comma-separated list of additional page models to be created when this
     * weblog is rendered.
     *
     * @ejb:persistent-field
     * @hibernate.property column="pagemodels" not-null="false"
     */
    public String getPageModels() {
        return pageModels;
    }
    public void setPageModels(String pageModels) {
        this.pageModels = pageModels;
    }

    
    /**
     * Get initialized plugins for use during rendering process.
     */
    public Map getInitializedPlugins() {
        if (initializedPlugins == null) {
            try {
                Roller roller = RollerFactory.getRoller();
                PluginManager ppmgr = roller.getPagePluginManager();
                initializedPlugins = ppmgr.getWeblogEntryPlugins(this); 
            } catch (Exception e) {
                this.log.error("ERROR: initializing plugins");
            }
        }
        return initializedPlugins;
    }
    
    
    /**
     * Returns categories under the default category of the weblog.
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogCategoryData"
     */
    public List getWeblogCategories() {
        List ret = new ArrayList();
        try {           
            WeblogCategoryData category = this.getDefaultCategory();
            ret = category.getWeblogCategories();
        } catch (RollerException e) {
            log.error("ERROR: fetching categories", e);
        }
        return ret;
    }
    
    
    /**
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogCategoryData"
     */
    public List getWeblogCategories(String categoryPath) {
        List ret = new ArrayList();
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();            
            WeblogCategoryData category = null;
            if (categoryPath != null && !categoryPath.equals("nil")) {
                category = wmgr.getWeblogCategoryByPath(this, null, categoryPath);
            } else {
                category = this.getDefaultCategory();
            }
            ret = category.getWeblogCategories();
        } catch (RollerException e) {
            log.error("ERROR: fetching categories for path: " + categoryPath, e);
        }
        return ret;
    }

    
    /**
     * @roller.wrapPojoMethod type="pojo" class="org.apache.roller.pojos.WeblogCategoryData"
     */
    public WeblogCategoryData getWeblogCategory(String categoryPath) {
        WeblogCategoryData category = null;
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            if (categoryPath != null && !categoryPath.equals("nil")) {
                category = wmgr.getWeblogCategoryByPath(this, null, categoryPath);
            } else {
                category = this.getDefaultCategory();
            }
        } catch (RollerException e) {
            log.error("ERROR: fetching category at path: " + categoryPath, e);
        }
        return category;
    }
    
    
    /**
     * Get up to 100 most recent published entries in weblog.
     * @param cat Category path or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.WeblogEntryData"
     */
    public List getRecentWeblogEntries(String cat, int length) {  
        if (cat != null && "nil".equals(cat)) cat = null;
        if (length > 100) length = 100;
        List recentEntries = new ArrayList();
        if (length < 1) return recentEntries;
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            recentEntries = wmgr.getWeblogEntries(
                    this, 
                    null,       // user
                    null,       // startDate
                    new Date(), // endDate
                    cat,        // cat or null
                    null, 
                    WeblogEntryData.PUBLISHED, 
                    "pubTime",  // sortby
                    null, 
                    0,
                    length); 
        } catch (RollerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return recentEntries;
    }
   
    
    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     * @param length Max entries to return (1-100)
     * @return List of comment objects.
     *
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.CommentData"
     */
    public List getRecentComments(int length) {   
        if (length > 100) length = 100;
        List recentComments = new ArrayList();
        if (length < 1) return recentComments;
        try {
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            recentComments = wmgr.getComments(
                    this,
                    null,          // weblog entry
                    null,          // search String
                    null,          // startDate
                    null,          // endDate
                    null,          // pending
                    Boolean.TRUE,  // approved only
                    Boolean.FALSE, // no spam
                    true,          // we want reverse chrono order
                    0,             // offset
                    length);       // length
        } catch (RollerException e) {
            log.error("ERROR: getting recent comments", e);
        }
        return recentComments;
    }

    
    /**
     * Get bookmark folder by name.
     * @param folderName Name or path of bookmark folder to be returned (null for root)
     * @return Folder object requested.
     *
     * @roller.wrapPojoMethod type="pojo" class="org.apache.roller.pojos.FolderData"
     */
    public FolderData getBookmarkFolder(String folderName) {
        FolderData ret = null;
        try {
            Roller roller = RollerFactory.getRoller();
            BookmarkManager bmgr = roller.getBookmarkManager();
            if (folderName == null || folderName.equals("nil") || folderName.trim().equals("/")) {
                return bmgr.getRootFolder(this);
            } else {
                return bmgr.getFolder(this, folderName);
            }
        } catch (RollerException re) {
            log.error("ERROR: fetching folder for weblog", re);
        }
        return ret;
    }

    
    /** 
     * Return collection of referrers for current day.
     * @roller.wrapPojoMethod type="pojo-collection" class="org.apache.roller.pojos.RefererData"
     */
    public List getTodaysReferrers() {
        List referers = null;
        try {
            Roller roller = RollerFactory.getRoller();
            RefererManager rmgr = roller.getRefererManager();
            return rmgr.getTodaysReferers(this);
            
        } catch (RollerException e) {
            log.error("PageModel getTodaysReferers()", e);
        }
        return (referers == null ? Collections.EMPTY_LIST : referers);        
    }
    
    /** No-op method to please XDoclet */
    public void setTodaysReferrers(List ignored) {}
    
    /**
     * Get number of hits counted today.
     * @roller.wrapPojoMethod type="simple"
     */
    public int getTodaysHits() {
        try {
            Roller roller = RollerFactory.getRoller();
            WeblogManager mgr = roller.getWeblogManager();
            HitCountData hitCount = mgr.getHitCountByWeblog(this);
            
            return (hitCount != null) ? hitCount.getDailyHits() : 0;
            
        } catch (RollerException e) {
            log.error("PageModel getTotalHits()", e);
        }
        return 0;
    }
    
    
    
    /**
     * Get a list of TagStats objects for the most popular tags
     *
     * @param sinceDays Number of days into past (or -1 for all days)
     * @param length    Max number of tags to return.
     * @return          Collection of WeblogEntryTag objects
     *
     * @roller.wrapPojoMethod type="simple"
     */
    public List getPopularTags(int sinceDays, int length) {
        List results = new ArrayList();
        Date startDate = null;
        if(sinceDays > 0) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, -1 * sinceDays);        
            startDate = cal.getTime();     
        }        
        try {            
            Roller roller = RollerFactory.getRoller();
            WeblogManager wmgr = roller.getWeblogManager();
            results = wmgr.getPopularTags(this, startDate, length);
        } catch (Exception e) {
            log.error("ERROR: fetching weblog tags list", e);
        }
        return results;
    }      
    
    /** No-op method to please XDoclet */
    public void setTodaysHits(int ignored) {}
}

