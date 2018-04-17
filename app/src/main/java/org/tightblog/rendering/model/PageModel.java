/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

package org.tightblog.rendering.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.business.URLStrategyImpl;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.ThemeManager;
import org.tightblog.pojos.CalendarData;
import org.tightblog.pojos.CommentSearchCriteria;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.pojos.WeblogEntryTagAggregate;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.rendering.generators.CalendarGenerator;
import org.tightblog.rendering.pagers.WeblogEntriesPager;
import org.tightblog.rendering.pagers.WeblogEntriesPermalinkPager;
import org.tightblog.rendering.pagers.WeblogEntriesTimePager;
import org.tightblog.rendering.requests.WeblogPageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Model which provides information needed to render a weblog page.
 */
@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PageModel implements Model {

    static final int MAX_ENTRIES = 100;

    private WeblogPageRequest pageRequest;
    private WeblogEntryComment commentForm;
    private boolean preview;

    @Autowired
    protected URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    protected WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    protected ThemeManager themeManager;

    @Autowired
    private CalendarGenerator weblogCalendar;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

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
    public void init(Map<String, Object> initData) {
        this.pageRequest = (WeblogPageRequest) initData.get("parsedRequest");

        if (pageRequest == null) {
            throw new IllegalStateException("Missing WeblogPageRequest object");
        }

        // see if there is a comment form
        this.commentForm = (WeblogEntryComment) initData.get("commentForm");

        if (preview) {
            this.urlStrategy = new URLStrategyImpl(pageRequest.getWeblog().getTheme(),
                    pageRequest.getWeblog().isUsedForThemePreview());
        }
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
        return pageRequest.getWeblogEntryAnchor() != null;
    }

    /**
     * Is this page showing search results?
     */
    public boolean isSearchResults() {
        return false;
    }

    /**
     * Whether a "noindex" directive should be added to the page to discourage
     * search engines from returning the page in search results
     *
     * @return true if page shouldn't be indexed
     */
    public boolean isAddNoIndexDirective() {
        return preview || pageRequest.isNoIndex();
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
            return weblogManager.getAnalyticsTrackingCode(pageRequest.getWeblog());
        }
    }

    public String getTransformedText(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getText());
    }

    public String getTransformedSummary(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getSummary());
    }

    public boolean canSubmitNewComments(WeblogEntry entry) {
        return weblogEntryManager.canSubmitNewComments(entry);
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
        return pageRequest.getWeblogEntry();
    }

    /**
     * Get weblog template being displayed.
     */
    public Template getWeblogPage() {
        return pageRequest.getTemplate();
    }

    public List<? extends Template> getTemplates() {
        return themeManager.getWeblogTheme(pageRequest.getWeblog()).getTemplates();
    }

    public String getTemplateIdByName(String name) {
        Template template = themeManager.getWeblogTheme(pageRequest.getWeblog()).getTemplateByName(name);
        return template != null ? template.getId() : null;
    }

    /**
     * Get category name specified by request.
     */
    public String getCategoryName() {
        return pageRequest.getWeblogCategoryName();
    }

    /**
     * Get up to 100 most recent published entries in weblog.
     *
     * @param category    Category name or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntries(String category, int length) {
        if (length > MAX_ENTRIES) {
            length = MAX_ENTRIES;
        }
        List<WeblogEntry> recentEntries = new ArrayList<>();
        if (length < 1) {
            return recentEntries;
        }
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(pageRequest.getWeblog());
        wesc.setCategoryName(category);
        wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        wesc.setMaxResults(length);
        wesc.setCalculatePermalinks(true);
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
        return weblogManager.getPopularTags(pageRequest.getWeblog(), 0, length);
    }

    /**
     * Returns the tag specified in the request. if any /tag/foo
     */
    public String getTag() {
        return pageRequest.getTag();
    }

    /**
     * Access to device type, which is NORMAL, MOBILE, or TABLET
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
        if (pageRequest.getWeblogEntryAnchor() != null) {
            return new WeblogEntriesPermalinkPager(
                    weblogEntryManager,
                    urlStrategy,
                    pageRequest.getWeblog(),
                    pageRequest.getCustomPageName(),
                    pageRequest.getWeblogEntryAnchor(),
                    // preview can show draft entries
                    preview);
        } else {
            return new WeblogEntriesTimePager(
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

    public boolean isUserAuthenticated() {
        return pageRequest.getAuthenticatedUser() != null;
    }

    public boolean isUserBlogPublisher() {
        return checkUserRights(WeblogRole.POST);
    }

    public boolean isUserBlogOwner() {
        return checkUserRights(WeblogRole.OWNER);
    }

    private boolean checkUserRights(WeblogRole role) {
        return !preview && userManager.checkWeblogRole(pageRequest.getAuthenticatedUser(), getWeblog().getHandle(), role);
    }

    public CalendarData getCalendarData(boolean includeBlogEntryData) {
        return weblogCalendar.getCalendarData(pageRequest, includeBlogEntryData);
    }

    public String getRequestParameter(String paramName) {
        return pageRequest.getRequestParameter(paramName);
    }

}
