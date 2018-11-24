package org.tightblog.ui.restapi;

import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.service.EmailService;
import org.tightblog.service.URLService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.CommentSearchCriteria;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.WeblogEntryCommentRepository;
import org.tightblog.repository.WeblogEntryRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.Utilities;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    private static Logger log = LoggerFactory.getLogger(CommentController.class);

    // number of comments to show per page
    private static final int ITEMS_PER_PAGE = 30;

    private WeblogRepository weblogRepository;
    private WeblogEntryRepository weblogEntryRepository;
    private WeblogEntryCommentRepository weblogEntryCommentRepository;
    private WebloggerPropertiesRepository webloggerPropertiesRepository;
    private UserManager userManager;
    private UserRepository userRepository;
    private WeblogEntryManager weblogEntryManager;
    private LuceneIndexer luceneIndexer;
    private URLService urlService;
    private EmailService emailService;
    private MessageSource messages;

    @Autowired
    public CommentController(WeblogRepository weblogRepository, UserManager userManager, UserRepository userRepository,
                             WeblogEntryManager weblogEntryManager,
                             LuceneIndexer luceneIndexer, URLService urlService, EmailService emailService,
                             MessageSource messages, WebloggerPropertiesRepository webloggerPropertiesRepository,
                             WeblogEntryRepository weblogEntryRepository,
                             WeblogEntryCommentRepository weblogEntryCommentRepository) {
        this.weblogRepository = weblogRepository;
        this.weblogEntryRepository = weblogEntryRepository;
        this.weblogEntryCommentRepository = weblogEntryCommentRepository;
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
        this.userManager = userManager;
        this.userRepository = userRepository;
        this.weblogEntryManager = weblogEntryManager;
        this.luceneIndexer = luceneIndexer;
        this.urlService = urlService;
        this.emailService = emailService;
        this.messages = messages;
    }

    @PostMapping(value = "/{weblogId}/page/{page}")
    public CommentData getWeblogComments(@PathVariable String weblogId, @PathVariable int page,
                                         @RequestParam(required = false) String entryId,
                                         @RequestBody CommentSearchCriteria criteria,
                                         Principal principal, HttpServletResponse response) {

        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
        if (weblog != null && userManager.checkWeblogRole(principal.getName(), weblog, WeblogRole.POST)) {

            CommentData data = new CommentData();

            criteria.setWeblog(weblog);
            if (entryId != null) {
                criteria.setEntry(weblogEntryRepository.findByIdOrNull(entryId));
                data.entryTitle = criteria.getEntry().getTitle();
            }
            criteria.setOffset(page * ITEMS_PER_PAGE);
            criteria.setMaxResults(ITEMS_PER_PAGE + 1);

            List<WeblogEntryComment> rawComments = weblogEntryManager.getComments(criteria);
            data.comments = new ArrayList<>();
            data.comments.addAll(rawComments.stream()
                    .peek(c -> c.getWeblogEntry().setPermalink(
                            urlService.getWeblogEntryURL(c.getWeblogEntry())))
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

    @GetMapping(value = "/{weblogId}/searchfields")
    public CommentSearchFields getCommentSearchFields(@PathVariable String weblogId, Principal principal, Locale locale,
                                                              HttpServletResponse response) {

        // Get user permissions and locale
        User user = userRepository.findEnabledByUserName(principal.getName());

        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);

        if (weblog != null && userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
            CommentSearchFields fields = new CommentSearchFields();

            // status options
            fields.statusOptions = new LinkedHashMap<>();
            fields.statusOptions.put("", messages.getMessage("generic.all", null, locale));
            fields.statusOptions.put("PENDING", messages.getMessage("comments.onlyPending", null, locale));
            fields.statusOptions.put("APPROVED", messages.getMessage("comments.onlyApproved", null, locale));
            fields.statusOptions.put("DISAPPROVED", messages.getMessage("comments.onlyDisapproved", null, locale));
            fields.statusOptions.put("SPAM", messages.getMessage("comments.onlySpam", null, locale));
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

    @DeleteMapping(value = "/{id}")
    public void deleteComment(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogEntryComment itemToRemove = weblogEntryCommentRepository.findByIdOrNull(id);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
                    weblogEntryManager.removeComment(itemToRemove);
                    luceneIndexer.updateIndex(itemToRemove.getWeblogEntry(), false);

                    // update last weblog change so any site weblog knows it needs to update
                    WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
                    props.setLastWeblogChange(Instant.now());
                    webloggerPropertiesRepository.saveAndFlush(props);
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

    @PostMapping(value = "/{id}/approve")
    private void approveComment(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        changeApprovalStatus(id, p, response, WeblogEntryComment.ApprovalStatus.APPROVED);
    }

    @PostMapping(value = "/{id}/hide")
    private void hideComment(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        changeApprovalStatus(id, p, response, WeblogEntryComment.ApprovalStatus.DISAPPROVED);
    }

    private void changeApprovalStatus(@PathVariable String id, Principal p, HttpServletResponse response,
                               WeblogEntryComment.ApprovalStatus status)
            throws ServletException {

        try {
            WeblogEntryComment comment = weblogEntryCommentRepository.findByIdOrNull(id);
            if (comment != null) {
                Weblog weblog = comment.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
                    WeblogEntryComment.ApprovalStatus oldStatus = comment.getStatus();
                    comment.setStatus(status);
                    // send approval notification only first time, not after any subsequent hide and approves.
                    if ((oldStatus == ApprovalStatus.PENDING || oldStatus == ApprovalStatus.SPAM) &&
                            status == ApprovalStatus.APPROVED) {
                        emailService.sendYourCommentWasApprovedNotifications(Collections.singletonList(comment));
                    }
                    weblogEntryManager.saveComment(comment, true);
                    luceneIndexer.updateIndex(comment.getWeblogEntry(), false);
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

    @PutMapping(value = "/{id}/content")
    public WeblogEntryComment updateComment(@PathVariable String id, Principal p, HttpServletRequest request,
                                     HttpServletResponse response)
            throws ServletException {
        try {
            WeblogEntryComment wec = weblogEntryCommentRepository.findByIdOrNull(id);
            if (wec == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                // need post permission to edit comments
                User authenticatedUser = userRepository.findEnabledByUserName(p.getName());
                Weblog weblog = wec.getWeblogEntry().getWeblog();
                if (userManager.checkWeblogRole(authenticatedUser, weblog, WeblogRole.POST)) {
                    String content = Utilities.apiValueToFormSubmissionValue(request.getInputStream());

                    // Validate content
                    HTMLSanitizer.Level sanitizerLevel = webloggerPropertiesRepository.findOrNull().getCommentHtmlPolicy();
                    Whitelist commentHTMLWhitelist = sanitizerLevel.getWhitelist();

                    wec.setContent(Jsoup.clean(content, commentHTMLWhitelist));

                    // don't update the posttime when updating the comment
                    wec.setPostTime(wec.getPostTime());
                    weblogEntryManager.saveComment(wec, true);
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
