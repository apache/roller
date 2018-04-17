package org.tightblog.ui.restapi;

import org.tightblog.business.MailManager;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.CommentSearchCriteria;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WeblogEntryComment.ApprovalStatus;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.I18nMessages;
import org.tightblog.util.Utilities;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/tb-ui/authoring/rest/comments")
public class CommentController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

    // number of comments to show per page
    private static final int ITEMS_PER_PAGE = 30;

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @Autowired
    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Autowired
    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    @Autowired
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @RequestMapping(value = "/{weblogId}/page/{page}", method = RequestMethod.POST)
    public CommentData getWeblogComments(@PathVariable String weblogId, @PathVariable int page,
                                         @RequestParam(required = false) String entryId,
                                         @RequestBody CommentSearchCriteria criteria,
                                         Principal principal, HttpServletResponse response) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        if (weblog != null && userManager.checkWeblogRole(principal.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

            CommentData data = new CommentData();

            criteria.setWeblog(weblog);
            if (entryId != null) {
                criteria.setEntry(weblogEntryManager.getWeblogEntry(entryId, false));
                data.entryTitle = criteria.getEntry().getTitle();
            }
            criteria.setOffset(page * ITEMS_PER_PAGE);
            criteria.setMaxResults(ITEMS_PER_PAGE + 1);

            List<WeblogEntryComment> rawComments = weblogEntryManager.getComments(criteria);
            data.comments = new ArrayList<>();
            data.comments.addAll(rawComments.stream()
                    .peek(c -> c.getWeblogEntry().setPermalink(
                            urlStrategy.getWeblogEntryURL(c.getWeblogEntry(), true)))
                    .collect(Collectors.toList()));

            if (rawComments.size() > ITEMS_PER_PAGE) {
                data.comments.remove(data.comments.size() - 1);
                data.hasMore = true;
            }

            return data;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    public class CommentData {
        List<WeblogEntryComment> comments;
        boolean hasMore;
        String entryTitle;

        public List<WeblogEntryComment> getComments() {
            return comments;
        }

        public String getEntryTitle() {
            return entryTitle;
        }

        public boolean isHasMore() {
            return hasMore;
        }
    }

    @RequestMapping(value = "/{weblogId}/searchfields", method = RequestMethod.GET)
    public CommentSearchFields getCommentSearchFields(@PathVariable String weblogId, Principal principal,
                                                              HttpServletResponse response) {

        // Get user permissions and locale
        User user = userManager.getEnabledUserByUserName(principal.getName());
        I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();

        Weblog weblog = weblogManager.getWeblog(weblogId);

        if (weblog != null && userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
            CommentSearchFields fields = new CommentSearchFields();

            // status options
            fields.statusOptions = new LinkedHashMap<>();
            fields.statusOptions.put("", messages.getString("generic.all"));
            fields.statusOptions.put("PENDING", messages.getString("comments.onlyPending"));
            fields.statusOptions.put("APPROVED", messages.getString("comments.onlyApproved"));
            fields.statusOptions.put("DISAPPROVED", messages.getString("comments.onlyDisapproved"));
            fields.statusOptions.put("SPAM", messages.getString("comments.onlySpam"));

            return fields;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

    }

    public class CommentSearchFields {
        Map<String, String> statusOptions;

        // getters needed for JSON serialization: http://stackoverflow.com/a/35822500
        public Map<String, String> getStatusOptions() {
            return statusOptions;
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteComment(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogEntryComment itemToRemove = weblogEntryManager.getComment(id);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.POST)) {
                    weblogEntryManager.removeComment(itemToRemove);
                    indexManager.updateIndex(itemToRemove.getWeblogEntry(), false);

                    // update last weblog change so any site weblog knows it needs to update
                    WebloggerProperties props = persistenceStrategy.getWebloggerProperties();
                    props.setLastWeblogChange(Instant.now());
                    persistenceStrategy.store(props);
                    persistenceStrategy.flush();
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error removing entry {}", id, e);
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/approve", method = RequestMethod.POST)
    private void approveComment(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        changeApprovalStatus(id, p, response, WeblogEntryComment.ApprovalStatus.APPROVED);
    }

    @RequestMapping(value = "/{id}/hide", method = RequestMethod.POST)
    private void hideComment(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        changeApprovalStatus(id, p, response, WeblogEntryComment.ApprovalStatus.DISAPPROVED);
    }

    private void changeApprovalStatus(@PathVariable String id, Principal p, HttpServletResponse response,
                               WeblogEntryComment.ApprovalStatus status)
            throws ServletException {

        try {
            WeblogEntryComment comment = weblogEntryManager.getComment(id);
            if (comment != null) {
                Weblog weblog = comment.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.POST)) {
                    WeblogEntryComment.ApprovalStatus oldStatus = comment.getStatus();
                    comment.setStatus(status);
                    // send approval notification only first time, not after any subsequent hide and approves.
                    if ((oldStatus == ApprovalStatus.PENDING || oldStatus == ApprovalStatus.SPAM) &&
                            status == ApprovalStatus.APPROVED) {
                        mailManager.sendYourCommentWasApprovedNotifications(Collections.singletonList(comment));
                    }
                    weblogEntryManager.saveComment(comment, true);
                    persistenceStrategy.flush();
                    indexManager.updateIndex(comment.getWeblogEntry(), false);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error removing entry {}", id, e);
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/content", method = RequestMethod.PUT)
    public WeblogEntryComment updateComment(@PathVariable String id, Principal p, HttpServletRequest request,
                                     HttpServletResponse response)
            throws ServletException {
        try {
            WeblogEntryComment wec = weblogEntryManager.getComment(id);
            if (wec == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                // need post permission to edit comments
                User authenticatedUser = userManager.getEnabledUserByUserName(p.getName());
                Weblog weblog = wec.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(authenticatedUser, weblog, WeblogRole.POST)) {
                    String content = Utilities.apiValueToFormSubmissionValue(request.getInputStream());

                    // Validate content
                    HTMLSanitizer.Level sanitizerLevel = persistenceStrategy.getWebloggerProperties().getCommentHtmlPolicy();
                    Whitelist commentHTMLWhitelist = sanitizerLevel.getWhitelist();

                    wec.setContent(Jsoup.clean(content, commentHTMLWhitelist));

                    // don't update the posttime when updating the comment
                    wec.setPostTime(wec.getPostTime());
                    weblogEntryManager.saveComment(wec, true);
                    persistenceStrategy.flush();
                    return wec;
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            }
            return null;
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }
}
