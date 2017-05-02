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

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.apache.roller.weblogger.ui.rendering.generators.BigWeblogCalendar;
import org.apache.roller.weblogger.ui.rendering.generators.WeblogCalendar;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesPermalinkPager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager;
import org.apache.roller.weblogger.ui.rendering.pagers.WeblogEntriesTimePager.PagingInterval;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogPageRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model which provides information needed to render a weblog page.
 */
public class PageModel implements Model {

    private static Logger log = LoggerFactory.getLogger(PageModel.class);

    private static final int MAX_ENTRIES = 100;

    private WeblogPageRequest pageRequest = null;
    private WeblogEntryComment commentForm = null;
    private Map requestParameters = null;
    private boolean preview = false;

    protected URLStrategy urlStrategy = null;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    protected WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    protected ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    private WeblogEntry weblogEntry = null;

    /**
     * Creates an un-initialized new instance, Weblogger calls init() to complete
     * construction.
     */
    public PageModel() {
    }

    /**
     * Template context name to be used for model
     */
    @Override
    public String getModelName() {
        return "model";
    }

    /**
     * Init page model, requires a WeblogPageRequest object.
     */
    @Override
    public void init(Map initData) {
        this.pageRequest = (WeblogPageRequest) initData.get("parsedRequest");

        if (pageRequest == null) {
            throw new IllegalStateException("Missing WeblogPageRequest object");
        }

        // see if there is a comment form
        this.commentForm = (WeblogEntryComment) initData.get("commentForm");

        // custom request parameters
        this.requestParameters = (Map) initData.get("requestParameters");
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
     * Is this page showing search results?
     */
    public boolean isSearchResults() {
        // the search results model will extend this class and override this
        return false;
    }

    /**
     * Adds a tracking code for website analytics (e.g. Google Analytics). Will use the blog-defined
     * tracking code if defined and permitted by the installation, else the server-defined tracking
     * code if defined will be used.
     */
    public String getAnalyticsTrackingCode() {
        if (preview) {
            return "";
        } else {
            WebloggerProperties props = WebloggerContext.getWebloggerProperties();
            if (props.isUsersOverrideAnalyticsCode() &&
                    !StringUtils.isBlank(pageRequest.getWeblog().getAnalyticsCode())) {
                return pageRequest.getWeblog().getAnalyticsCode();
            } else {
                return StringUtils.defaultIfEmpty(props.getDefaultAnalyticsCode(), "");
            }
        }
    }

    public String getTransformedText(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getText());
    }

    public String getTransformedSummary(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getSummary());
    }

    /**
     * Transform string based on Edit Format and HTML policy
     */
    private String render(Weblog.EditFormat format, String str) {
        return weblogEntryManager.processBlogText(format, str);
    }

    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public WeblogEntry getWeblogEntry() {
        if (weblogEntry == null && pageRequest.getWeblogAnchor() != null) {
            weblogEntry = weblogEntryManager.getWeblogEntryByAnchor(getWeblog(), pageRequest.getWeblogAnchor());
        }
        return weblogEntry;
    }

    /**
     * Get weblog entry being displayed or null if none specified by request.
     */
    public Template getWeblogPage() {
        if (pageRequest.getWeblogTemplateName() != null) {
            return pageRequest.getWeblogTemplate();
        } else {
            return themeManager.getWeblogTheme(pageRequest.getWeblog()).getTemplateByAction(Template.ComponentType.WEBLOG);
        }
    }

    public List<? extends Template> getTemplates() {
        return themeManager.getWeblogTheme(pageRequest.getWeblog()).getTemplates();
    }

    public Template getTemplateByName(String name) {
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
     *
     * @param cat    Category name or null for no category restriction
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
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(pageRequest.getWeblog());
        wesc.setCategoryName(cat);
        wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        wesc.setMaxResults(length);
        recentEntries = weblogEntryManager.getWeblogEntries(wesc);
        return recentEntries;
    }

    /**
     * Get up to 100 most recent approved and non-spam comments in weblog.
     *
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
        CommentSearchCriteria csc = new CommentSearchCriteria();
        csc.setWeblog(pageRequest.getWeblog());
        csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
        csc.setMaxResults(length);
        recentComments = weblogEntryManager.getComments(csc);
        return recentComments;
    }

    /**
     * Get a list of WeblogEntryTagAggregate objects for the most popular tags
     *
     * @param length Max number of tags to return.
     * @return Collection of WeblogEntryTag objects
     */
    public List<WeblogEntryTagAggregate> getPopularTags(int length) {
        List<WeblogEntryTagAggregate> results = new ArrayList<>();
        try {
            results = weblogManager.getPopularTags(pageRequest.getWeblog(), 0, length);
        } catch (Exception e) {
            log.error("ERROR: fetching popular tags for weblog {}", pageRequest.getWeblog().getName(), e);
        }
        return results;
    }

    /**
     * Returns the list of tags specified in the request /tags/foo+bar
     */
    public String getTag() {
        return pageRequest.getTag();
    }

    /**
     * Access to device type, which is either 'mobile' or 'standard'
     *
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

        // determine which mode to use
        if (pageRequest.getWeblogAnchor() != null) {
            return new WeblogEntriesPermalinkPager(
                    weblogEntryManager,
                    urlStrategy,
                    pageRequest.getWeblog(),
                    pageRequest.getWeblogTemplateName(),
                    pageRequest.getWeblogAnchor(),
                    // preview can show draft entries
                    preview);
        } else {
            PagingInterval interval = PagingInterval.LATEST;

            if (pageRequest.getWeblogDate() != null) {
                int len = pageRequest.getWeblogDate().length();
                if (len == 8) {
                    interval = PagingInterval.DAY;
                } else if (len == 6) {
                    interval = PagingInterval.MONTH;
                }
            }

            return new WeblogEntriesTimePager(
                    interval,
                    weblogEntryManager,
                    urlStrategy,
                    pageRequest.getWeblog(),
                    pageRequest.getWeblogDate(),
                    pageRequest.getWeblogCategoryName(),
                    pageRequest.getTag(),
                    pageRequest.getPageNum());
        }
    }

    /**
     * Get comment form to be displayed, may contain preview data.
     *
     * @return Comment form object
     */
    public WeblogEntryComment getCommentForm() {
        if (commentForm == null) {
            commentForm = new WeblogEntryComment();
            commentForm.initializeFormFields();
        }
        return commentForm;
    }

    /**
     * Get request parameter by name.
     */
    public String getRequestParameter(String paramName) {
        if (requestParameters != null) {
            String[] values = (String[]) requestParameters.get(paramName);
            if (values != null && values.length > 0) {
                return values[0];
            }
        }
        return null;
    }

    public boolean isUserAuthenticated() {
        return (pageRequest.getAuthenticatedUser() != null);
    }

    public boolean isUserBlogPublisher(Weblog weblog) {
        try {
            if (!preview && pageRequest.getAuthenticatedUser() != null) {
                // using handle variant of checkWeblogRole as id is presently nulled out in the templates
                return userManager.checkWeblogRole(pageRequest.getAuthenticatedUser(), weblog.getHandle(), WeblogRole.POST);
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return false;
    }

    public boolean isUserBlogOwner(Weblog weblog) {
        try {
            if (!preview && pageRequest.getAuthenticatedUser() != null) {
                // using handle variant of checkWeblogRole as id is presently nulled out in the templates
                return userManager.checkWeblogRole(pageRequest.getAuthenticatedUser(), weblog.getHandle(), WeblogRole.OWNER);
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return false;
    }

    public String showWeblogEntryCalendar(boolean big) {
        String ret = null;
        try {
            WeblogCalendar calendar = big ? new BigWeblogCalendar(pageRequest, weblogEntryManager, urlStrategy) :
                    new WeblogCalendar(weblogEntryManager, urlStrategy, pageRequest);
            ret = calendar.generateHTML();
        } catch (Exception e) {
            log.error("ERROR: initializing calendar tag", e);
        }
        return ret;
    }

}
