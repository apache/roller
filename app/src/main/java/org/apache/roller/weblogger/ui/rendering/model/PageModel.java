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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.rendering.model; 

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.UserWeblogRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.ui.core.menu.Menu;
import org.apache.roller.weblogger.ui.core.menu.MenuHelper;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager.PagingInterval;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPermalinkPager;
import org.apache.roller.weblogger.ui.rendering.comment.WeblogEntryCommentForm;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogRequest;


/**
 * Model which provides information needed to render a weblog page.
 */
public class PageModel implements Model {
    
    private static Log log = LogFactory.getLog(PageModel.class);

    private static final int MAX_ENTRIES = 100;

    private WeblogPageRequest pageRequest = null;
    private WeblogEntryCommentForm commentForm = null;
    private Map requestParameters = null;

    protected boolean isPreview = false;
    protected URLStrategy urlStrategy = null;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    protected WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    protected PropertiesManager propertiesManager;

    public void setPropertiesManager(PropertiesManager propertiesManager) {
        this.propertiesManager = propertiesManager;
    }

    protected ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    /**
     * Creates an un-initialized new instance, Weblogger calls init() to complete
     * construction.
     */
    public PageModel() {}
    
    
    /** 
     * Template context name to be used for model.
     */
    public String getModelName() {
        return "model";
    }
    
    
    /** 
     * Init page model based on request. 
     */
    public void init(Map initData) throws WebloggerException {
        
        // we expect the init data to contain a weblogRequest object
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if (weblogRequest == null) {
            throw new WebloggerException("expected weblogRequest from init data");
        }
        
        // PageModel only works on page requests, so cast weblogRequest
        // into a WeblogPageRequest and if it fails then throw exception
        if(weblogRequest instanceof WeblogPageRequest) {
            this.pageRequest = (WeblogPageRequest) weblogRequest;
        } else {
            throw new WebloggerException("weblogRequest is not a WeblogPageRequest."+
                    "  PageModel only supports page requests.");
        }
        
        // see if there is a comment form
        this.commentForm = (WeblogEntryCommentForm) initData.get("commentForm");
        
        // custom request parameters
        this.requestParameters = (Map)initData.get("requestParameters");
    }
    
    
    /**
     * Get the weblog locale used to render this page, null if no locale.
     */
    public String getLocale() {
        return null;
    }
    
    
    /**
     * Get weblog being displayed.
     */
    public Weblog getWeblog() {
        return pageRequest.getWeblog();
    }

    /**
     * Is this page considered a permalink?
     */
    public boolean isPermalink() {
        return (pageRequest.getWeblogAnchor() != null);
    }

    /**
     * Is page in preview mode?
     */
    public boolean isPreview() {
        return isPreview;
    }

    /**
     * Is this page showing search results?
     */
    public boolean isSearchResults() {
        // the search results model will extend this class and override this
        return false;
    }
    
    
    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public WeblogEntry getWeblogEntry() {
        return pageRequest.getWeblogEntry();
    }
    
    
    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public Template getWeblogPage() {
        if(pageRequest.getWeblogTemplateName() != null) {
            return pageRequest.getWeblogTemplate();
        } else {
            try {
                return themeManager.getWeblogTheme(pageRequest.getWeblog()).getTemplateByAction(Template.ComponentType.WEBLOG);
            } catch (WebloggerException ex) {
                log.error("Error getting default page", ex);
            }
        }
        return null;
    }

    public List<? extends Template> getTemplates() throws WebloggerException {
        return themeManager.getWeblogTheme(pageRequest.getWeblog()).getTemplates();
    }

    public Template getTemplateByName(String name) throws WebloggerException {
        return themeManager.getWeblogTheme(pageRequest.getWeblog()).getTemplateByName(name);
    }

    /**
     * Get category path or name specified by request.
     */
    public String getCategoryName() {
        return pageRequest.getWeblogCategoryName();
    }

    /**
     * Get up to 100 most recent published entries in weblog.
     * @param cat Category name or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntries(String cat, int length) {
        if (cat != null && "nil".equals(cat)) {
            cat = null;
        }
        if (length > MAX_ENTRIES) {
            length = MAX_ENTRIES;
        }
        List<WeblogEntry> recentEntries = new ArrayList<>();
        if (length < 1) {
            return recentEntries;
        }
        try {
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(pageRequest.getWeblog());
            wesc.setCatName(cat);
            wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
            wesc.setMaxResults(length);
            recentEntries = weblogEntryManager.getWeblogEntries(wesc);
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent entries", e);
        }
        return recentEntries;
    }

    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     * @param length Max entries to return (1-100)
     * @return List of comment objects.
     */
    public List<WeblogEntryComment> getRecentComments(int length) {
        if (length > MAX_ENTRIES) {
            length = MAX_ENTRIES;
        }
        List<WeblogEntryComment> recentComments = new ArrayList<>();
        if (length < 1) {
            return recentComments;
        }
        try {
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(pageRequest.getWeblog());
            csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
            csc.setMaxResults(length);
            recentComments = weblogEntryManager.getComments(csc);
        } catch (WebloggerException e) {
            log.error("ERROR: getting recent comments", e);
        }
        return recentComments;
    }

    /**
     * Get a list of TagStats objects for the most popular tags
     *
     * @param length    Max number of tags to return.
     * @return          Collection of WeblogEntryTag objects
     */
    public List<TagStat> getPopularTags(int length) {
        List<TagStat> results = new ArrayList<>();
        try {
            results = weblogEntryManager.getPopularTags(pageRequest.getWeblog(), 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching popular tags for weblog " + pageRequest.getWeblog().getName(), e);
        }
        return results;
    }


    /**
     * Returns the list of tags specified in the request /tags/foo+bar
     */
    public List getTags() {
        return pageRequest.getTags();
    }
    

	/**
	 * Access to device type, which is either 'mobile' or 'standard'
	 * @return device type
	 */
	public String getDeviceType() {
        return pageRequest.getDeviceType().toString();
	}


    /**
     * A map of entries representing this page. The collection is grouped by 
     * days of entries.  Each value is a list of entry objects keyed by the 
     * date they were published.
     */
    public WeblogEntriesPager getWeblogEntriesPager() {
        return getWeblogEntriesPager(null, null);
    }
    
    public WeblogEntriesPager getWeblogEntriesPager(String catArgument, String tagArgument) {
        
        // category specified by argument wins over request parameter
        String cat = pageRequest.getWeblogCategoryName();
        if (catArgument != null && !StringUtils.isEmpty(catArgument) && !"nil".equals(catArgument)) {
            cat = catArgument;
        }
        
        List<String> tags = pageRequest.getTags();
        if (tagArgument != null && !StringUtils.isEmpty(tagArgument) && !"nil".equals(tagArgument)) {
            tags = new ArrayList<>();
            tags.add(tagArgument);
        }
        
        String dateString = pageRequest.getWeblogDate();
        
        // determine which mode to use
        if (pageRequest.getWeblogAnchor() != null) {
            return new WeblogEntriesPermalinkPager(
                    weblogEntryManager,
                    urlStrategy,
                    pageRequest.getWeblog(),
                    pageRequest.getWeblogTemplateName(),
                    pageRequest.getWeblogAnchor(),
                    cat,
                    tags,
                    true);
        } else {
            PagingInterval interval = PagingInterval.LATEST;

            if (dateString != null) {
                int len = dateString.length();
                if (len == 8) {
                    interval = PagingInterval.DAY;
                } else if (len == 6) {
                    interval = PagingInterval.MONTH;
                }
            }

            return new WeblogEntriesTimePager(
                    interval,
                    weblogEntryManager,
                    propertiesManager,
                    urlStrategy,
                    pageRequest.getWeblog(),
                    pageRequest.getWeblogDate(),
                    cat,
                    tags,
                    pageRequest.getPageNum());
        }
    }

    /**
     * Get comment form to be displayed, may contain preview data.
     *
     * @return Comment form object
     */
    public WeblogEntryCommentForm getCommentForm() {
        
        if(commentForm == null) {
            commentForm = new WeblogEntryCommentForm();
        }
        return commentForm;
    }
    
    /**
     * Get request parameter by name.
     */
    public String getRequestParameter(String paramName) {
        if (requestParameters != null) {
            String[] values = (String[])requestParameters.get(paramName);
            if (values != null && values.length > 0) {
                return values[0];
            }
        }
        return null;
    }

    /**
     * Get a Menu representing the editor UI action menu, if the user is
     * currently logged in.
     */
    public Menu getEditorMenu() {
        try {
            if (pageRequest.isLoggedIn()) {
                UserWeblogRole uwr = userManager.getWeblogRole(pageRequest.getUser(), pageRequest.getWeblog());
                return MenuHelper.generateMenu("editor", null, pageRequest.getUser(), pageRequest.getWeblog(), uwr.getWeblogRole());
            } else {
                return null;
            }
        } catch (Exception e) {
            log.error("GetWeblogRole() failed for user: " + pageRequest.getUser() + " and weblog: " + pageRequest.getWeblog());
            return null;
        }
    }
}
