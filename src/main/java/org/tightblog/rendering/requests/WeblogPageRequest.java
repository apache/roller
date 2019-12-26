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
package org.tightblog.rendering.requests;

import org.tightblog.domain.CalendarData;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.Template;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntrySearchCriteria;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.tightblog.domain.WeblogRole;
import org.tightblog.rendering.service.WeblogEntryListGenerator;
import org.tightblog.rendering.model.PageModel;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a request for a TightBlog weblog page
 * <p>
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogPageRequest extends WeblogRequest {

    static final int MAX_ENTRIES = 100;

    // lightweight attributes
    private String weblogEntryAnchor;
    private String customPageName;
    protected String category;
    private String weblogDate;
    private String tag;
    private HttpServletRequest request;
    private String queryString;
    private boolean preview;

    // whether a robots meta tag with value "noindex" should be added to discourage search engines from indexing page
    private boolean noIndex;

    // attributes populated by processors where appropriate
    private Template template;
    private WeblogEntry weblogEntry;
    private PageModel pageModel;
    private WeblogEntryComment commentForm;
    protected WeblogEntryListGenerator.WeblogEntryListData pager;

    public WeblogPageRequest(String weblogHandle, Principal principal, PageModel pageModel) {
        super(principal);
        setWeblogHandle(weblogHandle);
        this.pageModel = pageModel;
    }

    public WeblogPageRequest(String weblogHandle, Principal principal, PageModel pageModel, boolean preview) {
        this(weblogHandle, principal, pageModel);
        this.preview = preview;
    }

    public WeblogPageRequest() {
    }

    public void setWeblogEntryAnchor(String weblogEntryAnchor) {
        this.weblogEntryAnchor = weblogEntryAnchor;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setNoIndex(boolean noIndex) {
        this.noIndex = noIndex;
    }

    public boolean isPreview() {
        return preview;
    }

    public String getWeblogEntryAnchor() {
        return weblogEntryAnchor;
    }

    public String getCustomPageName() {
        return customPageName;
    }

    public void setCustomPageName(String customPageName) {
        this.customPageName = customPageName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String weblogCategory) {
        this.category = weblogCategory;
    }

    public String getWeblogDate() {
        return weblogDate;
    }

    public void setWeblogDate(String weblogDate) {
        this.weblogDate = weblogDate;
    }

    public String getTag() {
        return tag;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry weblogEntry) {
        this.weblogEntry = weblogEntry;
    }

    public boolean isNoIndex() {
        return noIndex;
    }

    /* Supports custom parameters often needed by custom external pages */
    public String getRequestParameter(String paramName) {
        return request.getParameter(paramName);
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String toString() {
        return String.format("WeblogPageRequest: anchor=%s date=%s category=%s tag=%s customPageName=%s",
                weblogEntryAnchor, weblogDate, category, tag, customPageName);
    }

    public WeblogEntryComment getCommentForm() {
        if (commentForm == null) {
            commentForm = new WeblogEntryComment();
            commentForm.initializeFormFields();
        }
        return commentForm;
    }

    public void setCommentForm(WeblogEntryComment commentForm) {
        this.commentForm = commentForm;
    }

    public boolean canSubmitNewComments(WeblogEntry entry) {
        return pageModel.getWeblogEntryManager().canSubmitNewComments(entry);
    }

    public String getTransformedText(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getText());
    }

    public String getTransformedSummary(WeblogEntry entry) {
        return render(entry.getEditFormat(), entry.getSummary());
    }

    private String render(Weblog.EditFormat format, String str) {
        return pageModel.getWeblogEntryManager().processBlogText(format, str);
    }

    public CalendarData getCalendarData(boolean includeBlogEntryData) {
        return pageModel.getCalendarGenerator().getCalendarData(this, includeBlogEntryData);
    }

    public boolean isUserBlogPublisher() {
        return checkUserRights(WeblogRole.POST);
    }

    public boolean isUserBlogOwner() {
        return checkUserRights(WeblogRole.OWNER);
    }

    private boolean checkUserRights(WeblogRole role) {
        return !preview && getAuthenticatedUser() != null
                && pageModel.getUserManager().checkWeblogRole(getAuthenticatedUser(), weblog, role);
    }

    /**
     * Get up to 100 most recent published entries in weblog.
     *
     * @param catName    Category name or null for no category restriction
     * @param length Max entries to return (1-100)
     * @return List of weblog entry objects.
     */
    public List<WeblogEntry> getRecentWeblogEntries(String catName, int length) {
        if (length > MAX_ENTRIES) {
            length = MAX_ENTRIES;
        }
        List<WeblogEntry> recentEntries = new ArrayList<>();
        if (length < 1) {
            return recentEntries;
        }
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(weblog);
        wesc.setCategoryName(catName);
        wesc.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        wesc.setMaxResults(length);
        wesc.setCalculatePermalinks(true);
        recentEntries = pageModel.getWeblogEntryManager().getWeblogEntries(wesc);
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
        csc.setWeblog(weblog);
        csc.setStatus(WeblogEntryComment.ApprovalStatus.APPROVED);
        csc.setMaxResults(length);
        recentComments = pageModel.getWeblogEntryManager().getComments(csc);
        return recentComments;
    }

    /**
     * Get a list of WeblogEntryTagAggregate objects for the most popular tags
     *
     * @param length Max number of tags to return.
     * @return Collection of WeblogEntryTagAggregate objects
     */
    public List<WeblogEntryTagAggregate> getPopularTags(int length) {
        return pageModel.getPopularTags(weblog, length);
    }

    public boolean isPermalink() {
        return getWeblogEntryAnchor() != null;
    }

    public boolean isSearchResults() {
        return false;
    }

    public String getAnalyticsTrackingCode() {
        return pageModel.getAnalyticsTrackingCode(weblog, preview);
    }

    public List<? extends Template> getCustomPages() {
        return pageModel.getThemeManager().getWeblogTheme(weblog).getTemplates().stream()
                .filter(t -> Template.Role.CUSTOM_EXTERNAL.equals(t.getRole()))
                .collect(Collectors.toList());
    }

    public String getTemplateIdByName(String name) {
        Template tmpl = pageModel.getThemeManager().getWeblogTheme(weblog).getTemplateByName(name);
        return tmpl != null ? tmpl.getId() : null;
    }

    public WeblogEntryListGenerator.WeblogEntryListData getWeblogEntriesPager() {
        if (pager == null) {
            // determine which mode to use
            if (getWeblogEntryAnchor() != null) {
                pager = pageModel.getWeblogEntryListGenerator().getPermalinkPager(weblog, getWeblogEntryAnchor(),
                        preview);
            } else {
                pager = pageModel.getWeblogEntryListGenerator().getChronoPager(weblog, weblogDate, category,
                        tag, pageNum, Math.min(pageModel.getMaxEntriesPerPage(), weblog.getEntriesPerPage()),
                        false);
            }
        }
        return pager;
    }
}
