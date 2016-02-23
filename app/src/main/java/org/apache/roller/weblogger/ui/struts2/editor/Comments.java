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

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.*;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Action for managing weblog comments.
 */
@RestController
public class Comments extends UIAction {

    private static final long serialVersionUID = -104973988372024709L;

    private static Log log = LogFactory.getLog(Comments.class);

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    // number of comments to show per page
    private static final int COUNT = 30;

    // bean for managing submitted data
    private CommentsBean bean = new CommentsBean();

    // pager for the comments we are viewing
    private CommentsPager pager = null;

    // first comment in the list
    private WeblogEntryComment firstComment = null;

    // last comment in the list
    private WeblogEntryComment lastComment = null;

    // entry associated with comments or null if either weblog-wide
    // or global comment management
    private WeblogEntry queryEntry = null;

    // indicates number of comments that would be deleted by bulk removal
    // a non-zero value here indicates bulk removal is a valid option
    private int bulkDeleteCount = 0;

    public Comments() {
        this.pageTitle = "commentManagement.title";
    }

    private boolean isGlobalCommentManagement() {
        return actionName.equals("globalCommentManagement");
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return (isGlobalCommentManagement()) ? GlobalRole.ADMIN : GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return (isGlobalCommentManagement()) ? WeblogRole.NOBLOGNEEDED : WeblogRole.POST;
    }

    public void loadComments() {

        List<WeblogEntryComment> comments = Collections.emptyList();
        boolean hasMore = false;
        try {
            // lookup weblog entry if necessary
            if (!StringUtils.isEmpty(getBean().getEntryId())) {
                setQueryEntry(weblogEntryManager.getWeblogEntry(getBean().getEntryId()));
            }

            CommentSearchCriteria csc = new CommentSearchCriteria();
            if (!isGlobalCommentManagement()) {
                csc.setWeblog(getActionWeblog());
                csc.setEntry(getQueryEntry());
            }
            csc.setSearchText(getBean().getSearchString());
            csc.setStartDate(getBean().getStartDate());
            csc.setEndDate(getBean().getEndDate());
            csc.setStatus(getBean().getStatus());
            csc.setOffset(getBean().getPage() * COUNT);
            csc.setMaxResults(COUNT + 1);

            List<WeblogEntryComment> rawComments = weblogEntryManager.getComments(csc);
            comments = new ArrayList<>();
            comments.addAll(rawComments);

            if (comments.size() > 0) {
                if (comments.size() > COUNT) {
                    comments.remove(comments.size() - 1);
                    hasMore = true;
                }

                setFirstComment(comments.get(0));
                setLastComment(comments.get(comments.size() - 1));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up comments", ex);
            addError("commentManagement.lookupError");
        }

        // build comments pager
        String baseUrl = buildBaseUrl();
        setPager(new CommentsPager(baseUrl, getBean().getPage(), comments, hasMore));
    }

    // use the action data to build a url representing this action, including
    // query data
    private String buildBaseUrl() {

        Map<String, String> params = new HashMap<>();

        if (!isGlobalCommentManagement() && !StringUtils.isEmpty(getBean().getEntryId())) {
            params.put("bean.entryId", getBean().getEntryId());
        }
        if (!StringUtils.isEmpty(getBean().getSearchString())) {
            params.put("bean.searchString", getBean().getSearchString());
        }
        if (!StringUtils.isEmpty(getBean().getStartDateString())) {
            params.put("bean.startDateString", getBean().getStartDateString());
        }
        if (!StringUtils.isEmpty(getBean().getEndDateString())) {
            params.put("bean.endDateString", getBean().getEndDateString());
        }
        if (!StringUtils.isEmpty(getBean().getApprovedString())) {
            params.put("bean.approvedString", getBean().getApprovedString());
        }

        return urlStrategy.getActionURL(actionName,
                        isGlobalCommentManagement() ? "/tb-ui/admin" : "/tb-ui/authoring",
                        isGlobalCommentManagement() ? null : getActionWeblog().getHandle(),
                        params, false);
    }

    public String execute() {

        // load list of comments from query
        loadComments();

        // load bean data using comments list
        getBean().loadCheckboxes(getPager().getItems());

        return LIST;
    }

    /**
     * Query for a specific subset of comments based on various criteria.
     */
    public String query() {

        // load list of comments from query
        loadComments();

        // load bean data using comments list
        getBean().loadCheckboxes(getPager().getItems());

        try {
            CommentSearchCriteria csc = new CommentSearchCriteria();
            if (!isGlobalCommentManagement()) {
                csc.setWeblog(getActionWeblog());
            }
            csc.setSearchText(getBean().getSearchString());
            csc.setStartDate(getBean().getStartDate());
            csc.setEndDate(getBean().getEndDate());
            csc.setStatus(getBean().getStatus());

            List<WeblogEntryComment> allMatchingComments = weblogEntryManager.getComments(csc);
            if (allMatchingComments.size() > COUNT) {
                setBulkDeleteCount(allMatchingComments.size());
            }

        } catch (WebloggerException ex) {
            log.error("Error looking up comments", ex);
            addError("commentManagement.lookupError");
        }

        return LIST;
    }

    /**
     * Post user created message after first save.
     */
    @SkipValidation
    public String updateMade() {
        addMessage("commentManagement.updateSuccess");
        return execute();
    }

    /**
     * Update a list of comments.
     */
    public String update() {

        try {
            // Global Management: update weblogs
            HashSet<Weblog> flushWeblogSet = new HashSet<>();

            // if search is enabled, we will need to re-index all entries with
            // comments that have been approved, so build a list of those
            // entries
            Set<WeblogEntry> reindexList = new HashSet<>();

            // delete all comments with delete box checked
            List<String> deletes = Arrays.asList(getBean().getDeleteComments());
            if (deletes.size() > 0) {
                log.debug("Processing deletes - " + deletes.size());

                WeblogEntryComment deleteComment;
                for (String deleteId : deletes) {
                    deleteComment = weblogEntryManager.getComment(deleteId);
                    flushWeblogSet.add(deleteComment.getWeblogEntry().getWeblog());
                    weblogEntryManager.removeComment(deleteComment);

                    // make sure comment is tied to action weblog
                    if (isGlobalCommentManagement() || getActionWeblog().equals(
                            deleteComment.getWeblogEntry().getWeblog())) {
                        reindexList.add(deleteComment.getWeblogEntry());
                    }
                }
            }

            // loop through IDs of all comments displayed on page
            // approvedIds will always be empty for global comment management, as the JSP
            // which populates this value doesn't display approved checkboxes
            List<String> approvedIds = Arrays.asList(getBean().getApprovedComments());

            List<String> spamIds = Arrays.asList(getBean().getSpamComments());
            log.debug(spamIds.size() + " comments marked as spam");

            // track comments approved via moderation
            List<WeblogEntryComment> approvedComments = new ArrayList<>();

            String[] ids = Utilities.stringToStringArray(getBean().getIds(),
                    ",");
            for (String id : ids) {
                log.debug("processing id - " + id);

                // if we already deleted it then skip forward
                if (deletes.contains(id)) {
                    log.debug("Already deleted, skipping - " + id);
                    continue;
                }

                WeblogEntryComment comment = weblogEntryManager.getComment(id);

                // for non-Global, make sure comment is tied to action weblog
                if (isGlobalCommentManagement() || getActionWeblog().equals(comment.getWeblogEntry().getWeblog())) {
                    // comment approvals and mark/unmark spam
                    if (approvedIds.contains(id)) {
                        if (!ApprovalStatus.APPROVED.equals(comment.getStatus())) {
                            // if a comment was previously PENDING then this is
                            // its first approval, so track it for notification
                            if (ApprovalStatus.PENDING.equals(comment.getStatus())) {
                                approvedComments.add(comment);
                            }

                            log.debug("Marking as approved - " + comment.getId());
                            comment.setStatus(ApprovalStatus.APPROVED);
                            weblogEntryManager.saveComment(comment);

                            flushWeblogSet.add(comment.getWeblogEntry().getWeblog());
                            reindexList.add(comment.getWeblogEntry());
                        }
                    } else if (spamIds.contains(id)) {
                        if (!ApprovalStatus.SPAM.equals(comment.getStatus())) {
                            log.debug("Marking as spam - " + comment.getId());
                            comment.setStatus(ApprovalStatus.SPAM);
                            weblogEntryManager.saveComment(comment);

                            flushWeblogSet.add(comment.getWeblogEntry().getWeblog());
                            reindexList.add(comment.getWeblogEntry());
                        }
                    } else if (!ApprovalStatus.DISAPPROVED.equals(comment.getStatus())
                            && !(isGlobalCommentManagement() && ApprovalStatus.APPROVED.equals(comment.getStatus()))) {
                        log.debug("Marking as disapproved - " + comment.getId());
                        comment.setStatus(ApprovalStatus.DISAPPROVED);
                        weblogEntryManager.saveComment(comment);

                        flushWeblogSet.add(comment.getWeblogEntry().getWeblog());
                        reindexList.add(comment.getWeblogEntry());
                    }
                }
            }

            WebloggerFactory.flush();

            if (isGlobalCommentManagement()) {
                // notify caches of changes, flush weblogs affected by changes
                for (Weblog weblog : flushWeblogSet) {
                    cacheManager.invalidate(weblog);
                }
            } else {
                // notify caches of changes by flushing whole weblog because we can't
                // invalidate deleted comment objects (JPA nulls the fields out).
                cacheManager.invalidate(getActionWeblog());

                // if required, send notification for all comments changed
                if (mailManager.isMailConfigured()) {
                    I18nMessages resources = I18nMessages
                            .getMessages(getActionWeblog().getLocaleInstance());
                    mailManager.sendEmailApprovalNotifications(approvedComments,
                            resources);
                }
            }

            // if we've got entries to reindex then do so
            if (!reindexList.isEmpty()) {
                for (WeblogEntry entry : reindexList) {
                    indexManager.addEntryReIndexOperation(entry);
                }
            }

            // reset form and load fresh comments list
            CommentsBean freshBean = new CommentsBean();
            
            // Maintain filter options
            freshBean.setSearchString(getBean().getSearchString());
            freshBean.setStartDateString(getBean().getStartDateString());
            freshBean.setEndDateString(getBean().getEndDateString());
            freshBean.setSearchString(getBean().getSearchString());
            freshBean.setApprovedString(getBean().getApprovedString());

            // but if we're editing an entry's comments stick with that entry
            if (bean.getEntryId() != null) {
                freshBean.setEntryId(bean.getEntryId());
            }
            setBean(freshBean);

            return SUCCESS;

        } catch (Exception ex) {
            log.error("ERROR updating comments", ex);
            addError("commentManagement.updateError", ex.toString());
        }

        return LIST;
    }

    public List<Pair<String, String>> getCommentStatusOptions() {
        List<Pair<String, String>> opts = new ArrayList<>();
        opts.add(Pair.of("ALL", getText("generic.all")));
        opts.add(Pair.of("ONLY_PENDING", getText("commentManagement.onlyPending")));
        opts.add(Pair.of("ONLY_APPROVED", getText("commentManagement.onlyApproved")));
        opts.add(Pair.of("ONLY_DISAPPROVED", getText("commentManagement.onlyDisapproved")));
        opts.add(Pair.of("ONLY_SPAM", getText("commentManagement.onlySpam")));
        return opts;
    }

    public CommentsBean getBean() {
        return bean;
    }

    public void setBean(CommentsBean bean) {
        this.bean = bean;
    }

    public int getBulkDeleteCount() {
        return bulkDeleteCount;
    }

    public void setBulkDeleteCount(int bulkDeleteCount) {
        this.bulkDeleteCount = bulkDeleteCount;
    }

    public WeblogEntryComment getFirstComment() {
        return firstComment;
    }

    public void setFirstComment(WeblogEntryComment firstComment) {
        this.firstComment = firstComment;
    }

    public WeblogEntryComment getLastComment() {
        return lastComment;
    }

    public void setLastComment(WeblogEntryComment lastComment) {
        this.lastComment = lastComment;
    }

    public CommentsPager getPager() {
        return pager;
    }

    public void setPager(CommentsPager pager) {
        this.pager = pager;
    }

    public WeblogEntry getQueryEntry() {
        return queryEntry;
    }

    public void setQueryEntry(WeblogEntry queryEntry) {
        this.queryEntry = queryEntry;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/comment/{id}", method = RequestMethod.GET)
    public CommentData getComment(@PathVariable String id, Principal p, HttpServletResponse response)
    throws ServletException {
        try {
            WeblogEntryComment c = weblogEntryManager.getComment(id);
            if (c == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                // need post permission to view comments
                User authenticatedUser = userManager.getUserByUserName(p.getName());
                Weblog weblog = c.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(authenticatedUser, weblog, WeblogRole.POST)) {
                    CommentData cd = new CommentData();
                    cd.id = id;
                    cd.content = Utilities.escapeHTML(c.getContent());
                    cd.content = WordUtils.wrap(cd.content, 72);
                    cd.content = StringEscapeUtils.escapeEcmaScript(cd.content);
                    return cd;
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            return null;
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/comment/{id}", method = RequestMethod.PUT)
    public CommentData updateComment(@PathVariable String id, Principal p, HttpServletRequest request,
                                     HttpServletResponse response)
            throws ServletException {
        try {
            WeblogEntryComment c = weblogEntryManager.getComment(id);
            if (c == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                // need post permission to edit comments
                User authenticatedUser = userManager.getUserByUserName(p.getName());
                Weblog weblog = c.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(authenticatedUser, weblog, WeblogRole.POST)) {
                    String content = Utilities.streamToString(request.getInputStream());
                    c.setContent(content);
                    // don't update the posttime when updating the comment
                    c.setPostTime(c.getPostTime());
                    weblogEntryManager.saveComment(c);
                    WebloggerFactory.flush();
                    return getComment(id, p, response);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            return null;
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private static class CommentData {
        public CommentData() {
        }

        private String id;
        private String content;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
