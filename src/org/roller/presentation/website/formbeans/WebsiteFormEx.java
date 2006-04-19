/*
 * Created on Feb 14, 2004
 */
package org.roller.presentation.website.formbeans;

import java.sql.Timestamp;
import java.util.Date;
import org.apache.commons.lang.StringUtils;
import org.roller.RollerException;
import org.roller.model.RollerFactory;
import org.roller.model.WeblogManager;
import org.roller.pojos.WeblogCategoryData;
import org.roller.pojos.WebsiteData;
import org.roller.presentation.forms.WebsiteForm;
import org.roller.util.DateUtil;

/**
 * @struts.form name="websiteFormEx"
 */
public class WebsiteFormEx extends WebsiteForm {
    private String bloggerCategoryId;
    private String defaultCategoryId;
    private String[] defaultPluginsArray;
    private boolean applyCommentDefaults = false;
    
    /**
     * @return Returns the bloggerCategoryId.
     */
    public String getBloggerCategoryId() {
        return bloggerCategoryId;
    }
    
    /**
     * @param bloggerCategoryId The bloggerCategoryId to set.
     */
    public void setBloggerCategoryId(String bloggerCategoryId) {
        this.bloggerCategoryId = bloggerCategoryId;
    }
    
    /**
     * @return Returns the defeaultCategoryId.
     */
    public String getDefaultCategoryId() {
        return defaultCategoryId;
    }
    
    /**
     * @param defeaultCategoryId The defeaultCategoryId to set.
     */
    public void setDefaultCategoryId(String defeaultCategoryId) {
        this.defaultCategoryId = defeaultCategoryId;
    }
    
    /**
     * @return
     */
    public String[] getDefaultPluginsArray() {
        return defaultPluginsArray;
    }
    
    /**
     * @param strings
     */
    public void setDefaultPluginsArray(String[] strings) {
        defaultPluginsArray = strings;
    }
    
    public boolean isApplyCommentDefaults() {
        return applyCommentDefaults;
    }
    
    public void setApplyCommentDefaults(boolean applyCommentDefaults) {
        this.applyCommentDefaults = applyCommentDefaults;
    }
    
    /**
     * @see org.roller.presentation.forms.WebsiteForm#copyFrom(org.roller.pojos.WebsiteData)
     */
    public void copyFrom(WebsiteData dataHolder, java.util.Locale locale) throws RollerException {
        super.copyFrom(dataHolder, locale);
        if (dataHolder.getDefaultCategory() != null) {
            defaultCategoryId = dataHolder.getDefaultCategory().getId();
        }
        if (dataHolder.getBloggerCategory() != null) {
            bloggerCategoryId = dataHolder.getBloggerCategory().getId();
        }
        if (dataHolder.getDefaultPlugins() != null) {
            defaultPluginsArray = StringUtils.split(dataHolder.getDefaultPlugins(), ",");
        }
    }
    
    /**
     * Utility to convert from String to Date.
     */
    public void setDateCreatedAsString(String value) {
        if ( value == null || value.trim().length() == 0 ) {
            this.setDateCreated(null);
        } else {
            try {
                Date pubDate = DateUtil.parse(
                        value, DateUtil.friendlyTimestampFormat());
                this.setDateCreated(new Timestamp(pubDate.getTime()));
            } catch (java.text.ParseException pe) {
                // wasn't proper format, try others
                Date pubDate = DateUtil.parseFromFormats(value);
                this.setDateCreated( new Timestamp(pubDate.getTime()) );
            }
        }
    }
    
    /**
     * Returns a formatted pubTime string.
     */
    public String getDateCreatedAsString() {
        return DateUtil.friendlyTimestamp(this.getDateCreated());
    }
    
    /**
     * @see org.roller.presentation.forms.WebsiteForm#copyTo(org.roller.pojos.WebsiteData)
     */
    public void copyTo(WebsiteData dataHolder, java.util.Locale locale) throws RollerException {
        Date dateCreated = dataHolder.getDateCreated();
        
        super.copyTo(dataHolder, locale);
        
        dataHolder.setDateCreated(dateCreated);
        dataHolder.setDefaultPlugins( StringUtils.join(this.defaultPluginsArray,",") );
        
        // checkboxes return no value when not checked
        if (getAllowComments() == null) {
            dataHolder.setAllowComments(Boolean.FALSE);
        }
        if (getEmailComments() == null) {
            dataHolder.setEmailComments(Boolean.FALSE);
        }
        if (getEnableBloggerApi() == null) {
            dataHolder.setEnableBloggerApi(Boolean.FALSE);
        }
        if (getDefaultAllowComments() == null) {
            dataHolder.setDefaultAllowComments(Boolean.FALSE);
        }
        if (getModerateComments() == null) {
            dataHolder.setModerateComments(Boolean.FALSE);
        }
        if (this.getActive() == null) {
            dataHolder.setActive(Boolean.FALSE);
        }
        
        WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
        
        if (getDefaultCategoryId() != null) {
            WeblogCategoryData defaultCat =
                wmgr.getWeblogCategory(getDefaultCategoryId());
            dataHolder.setDefaultCategory(defaultCat);
        }
        
        if (getBloggerCategoryId() != null) {
            WeblogCategoryData bloggerCat =
                wmgr.getWeblogCategory(getBloggerCategoryId());
            dataHolder.setBloggerCategory(bloggerCat);
        }
    }
    
    public void reset(
            org.apache.struts.action.ActionMapping mapping,
            javax.servlet.ServletRequest request) {
        doReset(mapping, request);
        defaultPluginsArray = new String[0];
    }
    
    public void reset(
            org.apache.struts.action.ActionMapping mapping,
            javax.servlet.http.HttpServletRequest request) {
        doReset(mapping, request);
        defaultPluginsArray = new String[0];
    }
    
}
