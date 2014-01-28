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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.pagers.CommentsPager;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Action for managing weblog comments.
 */
public class Comments extends UIAction {

    private static final long serialVersionUID = -104973988372024709L;

    private static Log log = LogFactory.getLog(Comments.class);

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

    // entry associated with comments or null if none
    private WeblogEntry queryEntry = null;

    // indicates number of comments that would be deleted by bulk removal
    // a non-zero value here indicates bulk removal is a valid option
    private int bulkDeleteCount = 0;

    public Comments() {
        this.actionName = "comments";
        this.desiredMenu = "editor";
        this.pageTitle = "commentManagement.title";
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }

    public void loadComments() {

        List<WeblogEntryComment> comments = Collections.emptyList();
        boolean hasMore = false;
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager();

            // lookup weblog entry if necessary
            if (!StringUtils.isEmpty(getBean().getEntryId())) {
                setQueryEntry(wmgr.getWeblogEntry(getBean().getEntryId()));
            }

            // query for comments (reverse chrono order)
            List<WeblogEntryComment> rawComments = wmgr.getComments(
                    getActionWeblog(), getQueryEntry(), getBean()
                            .getSearchString(), getBean().getStartDate(),
                    getBean().getEndDate(), getBean().getStatus(), true,
                    getBean().getPage() * COUNT, COUNT + 1);
            comments = new ArrayList<WeblogEntryComment>();
            comments.addAll(rawComments);
            if (comments != null && comments.size() > 0) {
                if (comments.size() > COUNT) {
                    comments.remove(comments.size() - 1);
                    hasMore = true;
                }

                setFirstComment((WeblogEntryComment) comments.get(0));
                setLastComment((WeblogEntryComment) comments.get(comments
                        .size() - 1));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up comments", ex);
            // TODO: i18n
            addError("Error looking up comments");
        }

        // build comments pager
        String baseUrl = buildBaseUrl();
        setPager(new CommentsPager(baseUrl, getBean().getPage(), comments,
                hasMore));
    }

    // use the action data to build a url representing this action, including
    // query data
    private String buildBaseUrl() {

        Map<String, String> params = new HashMap<String, String>();

        if (!StringUtils.isEmpty(getBean().getEntryId())) {
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
        if (!StringUtils.isEmpty(getBean().getSpamString())) {
            params.put("bean.spamString", getBean().getSpamString());
        }

        return WebloggerFactory
                .getWeblogger()
                .getUrlStrategy()
                .getActionURL("comments", "/roller-ui/authoring",
                        getActionWeblog().getHandle(), params, false);
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
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager();

            // reverse chrono order
            List<WeblogEntryComment> allMatchingComments = wmgr.getComments(
                    getActionWeblog(), null, getBean().getSearchString(),
                    getBean().getStartDate(), getBean().getEndDate(), getBean()
                            .getStatus(), true, 0, -1);

            if (allMatchingComments.size() > COUNT) {
                setBulkDeleteCount(allMatchingComments.size());
            }

        } catch (WebloggerException ex) {
            log.error("Error looking up comments", ex);
            // TODO: i18n
            addError("Error looking up comments");
        }

        return LIST;
    }

    /**
     * Bulk delete all comments matching query criteria.
     */
    public String delete() {

        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager();

            // if search is enabled, we will need to re-index all entries with
            // comments that have been deleted, so build a list of those entries
            Set<WeblogEntry> reindexEntries = new HashSet<WeblogEntry>();
            if (WebloggerConfig.getBooleanProperty("search.enabled")) {
                List<WeblogEntryComment> targetted = (List<WeblogEntryComment>) wmgr
                        .getComments(getActionWeblog(), getQueryEntry(),
                                getBean().getSearchString(), getBean()
                                        .getStartDate(),
                                getBean().getEndDate(), getBean().getStatus(),
                                true, 0, -1);
                for (WeblogEntryComment comment : targetted) {
                    reindexEntries.add(comment.getWeblogEntry());
                }
            }

            int deleted = wmgr.removeMatchingComments(getActionWeblog(), null,
                    getBean().getSearchString(), getBean().getStartDate(),
                    getBean().getEndDate(), getBean().getStatus());

            // if we've got entries to reindex then do so
            if (!reindexEntries.isEmpty()) {
                IndexManager imgr = WebloggerFactory.getWeblogger()
                        .getIndexManager();
                for (WeblogEntry entry : reindexEntries) {
                    imgr.addEntryReIndexOperation(entry);
                }
            }

            addMessage("commentManagement.deleteSuccess",
                    Integer.toString(deleted));

            // reset form and load fresh comments list
            setBean(new CommentsBean());

            return execute();

        } catch (WebloggerException ex) {
            log.error("Error doing bulk delete", ex);
            // TODO: i18n
            addError("Bulk delete failed due to unexpected error");
        }

        return LIST;
    }

    /**
     * Update a list of comments.
     */
    public String update() {

        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager();

            List<WeblogEntryComment> flushList = new ArrayList<WeblogEntryComment>();

            // if search is enabled, we will need to re-index all entries with
            // comments that have been approved, so build a list of those
            // entries
            Set<WeblogEntry> reindexList = new HashSet<WeblogEntry>();

            // delete all comments with delete box checked
            List<String> deletes = Arrays.asList(getBean().getDeleteComments());
            if (deletes != null && deletes.size() > 0) {
                log.debug("Processing deletes - " + deletes.size());

                WeblogEntryComment deleteComment = null;
                for (String deleteId : deletes) {
                    deleteComment = wmgr.getComment(deleteId);

                    // make sure comment is tied to action weblog
                    if (getActionWeblog().equals(
                            deleteComment.getWeblogEntry().getWebsite())) {
                        flushList.add(deleteComment);
                        reindexList.add(deleteComment.getWeblogEntry());
                        wmgr.removeComment(deleteComment);
                    }
                }
            }

            // loop through IDs of all comments displayed on page
            List<String> approvedIds = Arrays.asList(getBean()
                    .getApprovedComments());
            List<String> spamIds = Arrays.asList(getBean().getSpamComments());
            log.debug(spamIds.size() + " comments marked as spam");

            // Determine whether we want approval emails
            boolean approvalNotification = WebloggerConfig.getBooleanProperty(
                    "comment.notification.onapproval", false);

            // track comments approved via moderation
            List<WeblogEntryComment> approvedComments = new ArrayList<WeblogEntryComment>();

            String[] ids = Utilities.stringToStringArray(getBean().getIds(),
                    ",");
            for (int i = 0; i < ids.length; i++) {
                log.debug("processing id - " + ids[i]);

                // if we already deleted it then skip forward
                if (deletes.contains(ids[i])) {
                    log.debug("Already deleted, skipping - " + ids[i]);
                    continue;
                }

                WeblogEntryComment comment = wmgr.getComment(ids[i]);

                // make sure comment is tied to action weblog
                if (getActionWeblog().equals(
                        comment.getWeblogEntry().getWebsite())) {
                    // comment approvals and mark/unmark spam
                    if (approvedIds.contains(ids[i])) {
                        // if a comment was previously PENDING then this is
                        // its first approval, so track it for notification
                        if (WeblogEntryComment.PENDING.equals(comment
                                .getStatus())) {
                            approvedComments.add(comment);
                        }

                        log.debug("Marking as approved - " + comment.getId());
                        comment.setStatus(WeblogEntryComment.APPROVED);
                        wmgr.saveComment(comment);

                        flushList.add(comment);
                        reindexList.add(comment.getWeblogEntry());

                    } else if (spamIds.contains(ids[i])) {
                        log.debug("Marking as spam - " + comment.getId());
                        comment.setStatus(WeblogEntryComment.SPAM);
                        wmgr.saveComment(comment);

                        flushList.add(comment);
                        reindexList.add(comment.getWeblogEntry());

                    } else if (!WeblogEntryComment.DISAPPROVED.equals(comment
                            .getStatus())) {
                        log.debug("Marking as disapproved - " + comment.getId());
                        comment.setStatus(WeblogEntryComment.DISAPPROVED);
                        wmgr.saveComment(comment);

                        flushList.add(comment);
                        reindexList.add(comment.getWeblogEntry());
                    }
                }
            }

            WebloggerFactory.getWeblogger().flush();

            // notify caches of changes by flushing whole site because we can't
            // invalidate deleted comment objects (JPA nulls the fields out).
            CacheManager.invalidate(getActionWeblog());

            // if required, send notification for all comments changed
            if (approvalNotification && MailUtil.isMailConfigured()) {
                I18nMessages resources = I18nMessages
                        .getMessages(getActionWeblog().getLocaleInstance());
                MailUtil.sendEmailApprovalNotifications(approvedComments,
                        resources);
            }

            // if we've got entries to reindex then do so
            if (!reindexList.isEmpty()) {
                IndexManager imgr = WebloggerFactory.getWeblogger()
                        .getIndexManager();
                for (WeblogEntry entry : reindexList) {
                    imgr.addEntryReIndexOperation(entry);
                }
            }

            addMessage("commentManagement.updateSuccess");

            // reset form and load fresh comments list
            CommentsBean freshBean = new CommentsBean();

            // but if we're editing an entry's comments stick with that entry
            if (bean.getEntryId() != null) {
                freshBean.setEntryId(bean.getEntryId());
            }
            setBean(freshBean);

            return execute();

        } catch (Exception ex) {
            log.error("ERROR updating comments", ex);
            addError("commentManagement.updateError", ex.toString());
        }

        return LIST;
    }

    public List<KeyValueObject> getCommentStatusOptions() {

        List<KeyValueObject> opts = new ArrayList<KeyValueObject>();

        opts.add(new KeyValueObject("ALL", getText("commentManagement.all")));
        opts.add(new KeyValueObject("ONLY_PENDING",
                getText("commentManagement.onlyPending")));
        opts.add(new KeyValueObject("ONLY_APPROVED",
                getText("commentManagement.onlyApproved")));
        opts.add(new KeyValueObject("ONLY_DISAPPROVED",
                getText("commentManagement.onlyDisapproved")));

        return opts;
    }

    public List<KeyValueObject> getSpamStatusOptions() {

        List<KeyValueObject> opts = new ArrayList<KeyValueObject>();

        opts.add(new KeyValueObject("ALL", getText("commentManagement.all")));
        opts.add(new KeyValueObject("NO_SPAM",
                getText("commentManagement.noSpam")));
        opts.add(new KeyValueObject("ONLY_SPAM",
                getText("commentManagement.onlySpam")));

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
}
