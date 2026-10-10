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

package org.apache.roller.weblogger.webservices.atomprotocol;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.CommentSearchCriteria;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.WeblogEntryComment.ApprovalStatus;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * The comments of a weblog as an AtomPub collection, for comment moderation
 * by users who may post to the weblog. Clients can list and read comments,
 * change a comment's roller:status, and delete comments; the collection does
 * not accept POST.
 *
 * <p>Each comment is an entry: atom:author is the commenter, atom:content
 * the comment text, atom:published the post time, thr:in-reply-to (RFC 4685)
 * the weblog entry, roller:status the approval status. The list can be
 * filtered with the {@code status} and {@code entry} (weblog entry id) query
 * parameters and paged like the entries collection.
 */
public class CommentCollection {

    private static final Log log = LogFactory.getLog(CommentCollection.class);

    private static final int MAX_ENTRIES = 20;

    private final Weblogger roller;
    private final User user;
    private final String atomURL;

    public CommentCollection(User user, String atomURL) {
        this.user = user;
        this.atomURL = atomURL;
        this.roller = WebloggerFactory.getWeblogger();
    }

    public AtomFeed getCollection(AtomRequest areq) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        int start = CollectionSupport.offset(pathInfo);
        try {
            CommentSearchCriteria csc = new CommentSearchCriteria();
            csc.setWeblog(weblog);
            csc.setReverseChrono(true);
            csc.setOffset(start);
            csc.setMaxResults(MAX_ENTRIES + 1);
            String status = areq.getParameter("status");
            if (StringUtils.isNotEmpty(status)) {
                csc.setStatus(parseStatus(status));
            }
            String entryId = areq.getParameter("entry");
            if (StringUtils.isNotEmpty(entryId)) {
                WeblogEntry entry = manager().getWeblogEntry(weblog, entryId);
                if (entry == null) {
                    throw new AtomNotFoundException("Cannot find weblog entry");
                }
                csc.setEntry(entry);
            }
            List<WeblogEntryComment> comments = manager().getComments(csc);

            String base = atomURL + "/" + weblog.getHandle() + "/comments/";
            String query = queryString(status, entryId);
            AtomFeed feed = new AtomFeed();
            feed.setId(base + start);
            feed.setTitle(weblog.getName() + " comments");
            feed.setUpdated(new Date());
            int count = 0;
            for (WeblogEntryComment comment : comments) {
                if (count++ >= MAX_ENTRIES) {
                    break;
                }
                feed.getEntries().add(createAtomEntry(weblog, comment));
            }
            if (comments.size() > MAX_ENTRIES) {
                feed.getLinks().add(CollectionSupport.link("next",
                        base + (start + MAX_ENTRIES) + query));
            }
            if (start > 0) {
                feed.getLinks().add(CollectionSupport.link("previous",
                        base + Math.max(0, start - MAX_ENTRIES) + query));
            }
            return feed;
        } catch (WebloggerException e) {
            throw new AtomException("Getting comment collection", e);
        }
    }

    public AtomEntry getEntry(AtomRequest areq) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            return createAtomEntry(weblog, requireComment(weblog, pathInfo));
        } catch (WebloggerException e) {
            throw new AtomException("Getting comment", e);
        }
    }

    /**
     * Changes the comment's approval status. Other fields are ignored. A
     * pending comment that is approved sends the approval notification, as
     * on the Comments page.
     */
    public void putEntry(AtomRequest areq, AtomEntry entry) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        String status = entry.getExtension("status");
        if (StringUtils.isEmpty(status)) {
            throw CollectionSupport.badRequest("roller:status is required");
        }
        ApprovalStatus newStatus = parseStatus(status);
        try {
            WeblogEntryComment comment = requireComment(weblog, pathInfo);
            ApprovalStatus oldStatus = comment.getStatus();
            if (newStatus.equals(oldStatus)) {
                return;
            }
            comment.setStatus(newStatus);
            manager().saveComment(comment);
            roller.flush();
            CacheManager.invalidate(weblog);

            if (ApprovalStatus.PENDING.equals(oldStatus)
                    && ApprovalStatus.APPROVED.equals(newStatus)
                    && MailUtil.isMailConfigured()) {
                try {
                    MailUtil.sendEmailApprovalNotifications(Collections.singletonList(comment),
                            I18nMessages.getMessages(weblog.getLocaleInstance()));
                } catch (Exception e) {
                    // The status change is saved; a mail failure must not undo it
                    log.error("Error sending comment approval notification", e);
                }
            }
            roller.getIndexManager().addEntryReIndexOperation(comment.getWeblogEntry());
        } catch (WebloggerException e) {
            throw new AtomException("Updating comment", e);
        }
    }

    public void deleteEntry(AtomRequest areq) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            WeblogEntryComment comment = requireComment(weblog, pathInfo);
            WeblogEntry entry = comment.getWeblogEntry();
            manager().removeComment(comment);
            roller.flush();
            // JPA clears a removed comment, so invalidate the whole weblog
            CacheManager.invalidate(weblog);
            roller.getIndexManager().addEntryReIndexOperation(entry);
        } catch (WebloggerException e) {
            throw new AtomException("Removing comment", e);
        }
    }

    private AtomEntry createAtomEntry(Weblog weblog, WeblogEntryComment comment) {
        String editURI = atomURL + "/" + weblog.getHandle() + "/comment/" + comment.getId();
        AtomEntry entry = new AtomEntry();
        entry.setId(editURI);
        WeblogEntry parent = comment.getWeblogEntry();
        entry.setTitle("Comment on " + parent.getTitle());
        entry.setPublished(comment.getPostTime());
        entry.setUpdated(comment.getPostTime() != null ? comment.getPostTime() : new Date());

        AtomPerson author = new AtomPerson();
        author.setName(StringUtils.isNotEmpty(comment.getName()) ? comment.getName() : "Anonymous");
        author.setEmail(StringUtils.trimToNull(comment.getEmail()));
        author.setUri(StringUtils.trimToNull(comment.getUrl()));
        entry.getAuthors().add(author);

        String type = "text/html".equals(comment.getContentType()) ? "html" : "text";
        entry.setContent(CollectionSupport.text(type, comment.getContent()));

        entry.setInReplyToRef(parent.getPermalink());
        entry.setInReplyToHref(atomURL + "/" + weblog.getHandle() + "/entry/" + parent.getId());

        entry.setExtension("status", comment.getStatus().name().toLowerCase(Locale.ROOT));
        entry.setExtension("remoteHost", comment.getRemoteHost());
        entry.setExtension("referrer", comment.getReferrer());

        List<AtomLink> links = new ArrayList<>();
        links.add(CollectionSupport.link("edit", editURI));
        links.add(CollectionSupport.link("related", parent.getPermalink()));
        entry.setLinks(links);
        return entry;
    }

    private static ApprovalStatus parseStatus(String value) throws AtomException {
        try {
            return ApprovalStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw CollectionSupport.badRequest(
                    "Unknown comment status (approved, disapproved, spam, pending): " + value);
        }
    }

    private static String queryString(String status, String entryId) {
        List<String> params = new ArrayList<>();
        if (StringUtils.isNotEmpty(status)) {
            params.add("status=" + URLEncoder.encode(status, StandardCharsets.UTF_8));
        }
        if (StringUtils.isNotEmpty(entryId)) {
            params.add("entry=" + URLEncoder.encode(entryId, StandardCharsets.UTF_8));
        }
        return params.isEmpty() ? "" : "?" + String.join("&", params);
    }

    private Weblog requireWeblog(String[] pathInfo) throws AtomException {
        return CollectionSupport.requireWeblog(roller, user, pathInfo, WeblogPermission.POST);
    }

    private WeblogEntryComment requireComment(Weblog weblog, String[] pathInfo)
            throws AtomException, WebloggerException {
        WeblogEntryComment comment = pathInfo.length > 2
                ? manager().getComment(weblog, pathInfo[2]) : null;
        if (comment == null) {
            throw new AtomNotFoundException("Cannot find comment");
        }
        return comment;
    }

    private WeblogEntryManager manager() {
        return roller.getWeblogEntryManager();
    }
}
