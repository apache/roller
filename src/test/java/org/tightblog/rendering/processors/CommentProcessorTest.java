/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tightblog.rendering.processors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.tightblog.TestUtils;
import org.tightblog.config.DynamicProperties;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.service.EmailService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;
import org.tightblog.domain.WeblogEntryComment.ValidationResult;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.domain.WebloggerProperties.CommentPolicy;
import org.tightblog.rendering.comment.CommentAuthenticator;
import org.tightblog.rendering.comment.CommentValidator;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;
import org.tightblog.util.HTMLSanitizer;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CommentProcessorTest {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private RequestDispatcher mockRequestDispatcher;
    private Weblog weblog;
    private WeblogEntry weblogEntry;
    private User user;
    private WebloggerProperties properties;
    private CommentProcessor processor;
    private MessageSource mockMessageSource;
    private WeblogRepository mockWR;
    private WeblogEntryManager mockWEM;
    private UserRepository mockUR;
    private UserManager mockUM;
    private LuceneIndexer mockIM;
    private EmailService mockES = mock(EmailService.class);
    private CommentValidator alwaysSpamValidator = (comment, messages) -> ValidationResult.SPAM;
    private CommentValidator alwaysNotSpamValidator = (comment, messages) -> ValidationResult.NOT_SPAM;
    private CommentValidator alwaysBlatantSpamValidator = (comment, messages) -> ValidationResult.BLATANT_SPAM;
    private CommentAuthenticator mockAuthenticator;

    @Before
    public void initialize() {
        mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();
        when(mockRequest.getParameter("name")).thenReturn("Sam");
        when(mockRequest.getParameter("email")).thenReturn("sam@yopmail.com");
        when(mockRequest.getParameter("notify")).thenReturn("notify");
        when(mockRequest.getRemoteHost()).thenReturn("https://www.duckduckgo.com");
        when(mockRequest.getParameter("preview")).thenReturn(null);
        when(mockRequest.getParameter("url")).thenReturn("https://www.glenmazza.net");
        when(mockRequest.getParameter("content")).thenReturn("My comment for this blog entry.");

        mockRequestDispatcher = mock(RequestDispatcher.class);
        when(mockRequest.getRequestDispatcher(anyString())).thenReturn(mockRequestDispatcher);

        WebloggerPropertiesRepository mockPropertiesRepository = mock(WebloggerPropertiesRepository.class);
        properties = new WebloggerProperties();
        properties.setCommentHtmlPolicy(HTMLSanitizer.Level.LIMITED);
        when(mockPropertiesRepository.findOrNull()).thenReturn(properties);

        mockWR = mock(WeblogRepository.class);
        weblog = new Weblog();
        weblog.setHandle("myblog");
        when(mockWR.findByHandleAndVisibleTrue("myblog")).thenReturn(weblog);

        mockWEM = mock(WeblogEntryManager.class);
        weblogEntry = new WeblogEntry();
        weblogEntry.setAnchor("entry-anchor");
        weblogEntry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        when(mockWEM.getWeblogEntryByAnchor(weblog, weblogEntry.getAnchor())).thenReturn(weblogEntry);
        when(mockWEM.canSubmitNewComments(weblogEntry)).thenReturn(true);

        mockUR = mock(UserRepository.class);
        user = new User();
        user.setUserName("bob");
        when(mockUR.findEnabledByUserName("bob")).thenReturn(user);

        mockIM = mock(LuceneIndexer.class);
        when(mockIM.isIndexComments()).thenReturn(true);

        mockES = mock(EmailService.class);
        mockUM = mock(UserManager.class);
        mockMessageSource = mock(MessageSource.class);
        PageModel mockPageModel = mock(PageModel.class);

        processor = new CommentProcessor(mockWR, mockUR, mockIM, mockWEM, mockUM,
                mockES, new DynamicProperties(), mockMessageSource, mockPageModel, mockPropertiesRepository);
        processor.setCommentValidators(Collections.singletonList(alwaysNotSpamValidator));

        EntityManager mockEM = mock(EntityManager.class);
        processor.setEntityManager(mockEM);

        mockAuthenticator = mock(CommentAuthenticator.class);
        when(mockAuthenticator.authenticate(mockRequest)).thenReturn(true);
        processor.setCommentAuthenticator(mockAuthenticator);

        mockResponse = mock(HttpServletResponse.class);
    }

    @Test
    public void testCommentSpamChecking() {
        try {
            // confirm authenticator ignored if user logged in
            when(mockAuthenticator.authenticate(mockRequest)).thenReturn(false);

            // test that if it is the blogger's comment it is automatically not spam
            // make this a blogger's comment
            when(mockUM.checkWeblogRole(any(User.class), any(Weblog.class), eq(WeblogRole.POST))).thenReturn(true);
            // have it evaluate to spam
            processor.setCommentValidators(Collections.singletonList(alwaysSpamValidator));

            // still approved
            verifyForwardAfterSpamChecking(ApprovalStatus.APPROVED, "commentServlet.commentAccepted", false);
            ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            WeblogEntryComment testComment = commentCaptor.getValue();
            assertEquals("", testComment.getContent());

            // blogger written to comment object
            assertEquals(user, testComment.getBlogger());

            // make subsequent tests a non-blogger comment
            when(mockAuthenticator.authenticate(mockRequest)).thenReturn(true);
            when(mockUM.checkWeblogRole(any(User.class), any(Weblog.class), eq(WeblogRole.POST))).thenReturn(false);
            when(mockUR.findEnabledByUserName(any())).thenReturn(null);

            // check spam comment requires approval even if must moderate off
            properties.setCommentPolicy(CommentPolicy.YES);
            weblog.setAllowComments(CommentPolicy.YES);
            verifyForwardAfterSpamChecking(ApprovalStatus.SPAM, "commentServlet.submittedToModerator", false);

            Mockito.clearInvocations(mockWEM, mockES, mockIM);

            // check non-spam comment doesn't require approval if must moderate off
            processor.setCommentValidators(Collections.singletonList(alwaysNotSpamValidator));
            properties.setCommentPolicy(CommentPolicy.YES);
            weblog.setAllowComments(CommentPolicy.YES);
            verifyForwardAfterSpamChecking(ApprovalStatus.APPROVED, "commentServlet.commentAccepted", false);
            verify(mockES).sendNewPublishedCommentNotification(any());
            verify(mockIM).updateIndex(weblogEntry, false);

            // check non-spam comment requires approval if must moderate set globally
            properties.setCommentPolicy(CommentPolicy.MUSTMODERATE);
            verifyForwardAfterSpamChecking(ApprovalStatus.PENDING, "commentServlet.submittedToModerator", false);

            // check non-spam comment requires approval if must moderate set for blog
            properties.setCommentPolicy(CommentPolicy.YES);
            weblog.setAllowComments(CommentPolicy.MUSTMODERATE);
            verifyForwardAfterSpamChecking(ApprovalStatus.PENDING, "commentServlet.submittedToModerator", false);

            // check no indexing if indexing shut off
            Mockito.clearInvocations(mockWEM, mockES, mockIM, mockRequest);
            weblog.setAllowComments(CommentPolicy.YES);
            when(mockIM.isIndexComments()).thenReturn(false);
            processor.postComment(mockRequest, mockResponse);
            verify(mockES).sendNewPublishedCommentNotification(any());
            verify(mockIM, never()).updateIndex(weblogEntry, false);

            // no blogger written to comment object
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            testComment = commentCaptor.getValue();
            assertNull(testComment.getBlogger());

            // check spam persisted to database with autodelete spam off
            Mockito.clearInvocations(mockWEM, mockES, mockIM);
            processor.setCommentValidators(Collections.singletonList(alwaysSpamValidator));
            properties.setAutodeleteSpam(false);
            processor.postComment(mockRequest, mockResponse);
            verify(mockWEM).saveComment(any(), anyBoolean());
            verify(mockES).sendPendingCommentNotice(any(), any());
            verify(mockIM, never()).updateIndex(any(WeblogEntry.class), anyBoolean());

            // check spam not persisted to database with autodelete spam on
            Mockito.clearInvocations(mockWEM);
            properties.setAutodeleteSpam(true);
            processor.postComment(mockRequest, mockResponse);
            verify(mockWEM, never()).saveComment(any(), anyBoolean());

            Mockito.clearInvocations(mockWEM);

            // check blatant spam not persisted to database
            processor.setCommentValidators(Collections.singletonList(alwaysBlatantSpamValidator));
            processor.postComment(mockRequest, mockResponse);
            verify(mockWEM, never()).saveComment(any(), anyBoolean());

            // confirm no spam checking if comment is preview
            Mockito.clearInvocations(mockUM);
            when(mockRequest.getParameter("preview")).thenReturn("preview");
            verifyForwardAfterSpamChecking(ApprovalStatus.DISAPPROVED, null, true);
            verify(mockUM, never()).checkWeblogRole(any(User.class), any(), any());

        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void checkBloggerFieldPopulatedCorrectly() {
        try {
            processor.postComment(mockRequest, mockResponse);
            ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            assertEquals(user, commentCaptor.getValue().getBlogger());

            Mockito.clearInvocations(mockRequest);
            when(mockRequest.getUserPrincipal()).thenReturn(null);
            processor.postComment(mockRequest, mockResponse);
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            assertNull(commentCaptor.getValue().getBlogger());
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void postCommentReturn403IfCommentingDisabled() {
        properties.setCommentPolicy(WebloggerProperties.CommentPolicy.NONE);

        try {
            processor.postComment(mockRequest, mockResponse);
            verify(mockResponse).sendError(HttpServletResponse.SC_FORBIDDEN);
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void postCommentReturn404IfWeblogNotFound() {
        try {
            when(mockWR.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
            processor.postComment(mockRequest, mockResponse);
            verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void postCommentReturn404IfWeblogEntryUnavailable() {
        when(mockWR.findByHandleAndVisibleTrue(any())).thenReturn(new Weblog());

        try {
            // entry null
            processor.postComment(mockRequest, mockResponse);

            // entry available but not published
            WeblogEntry entry = new WeblogEntry();
            entry.setStatus(WeblogEntry.PubStatus.DRAFT);
            when(mockWEM.getWeblogEntryByAnchor(any(), any())).thenReturn(entry);
            processor.postComment(mockRequest, mockResponse);
            verify(mockResponse, times(2)).sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void testValidationErrorForwarding() {
        try {
            // will return disabled if comments not allowed
            when(mockWEM.canSubmitNewComments(any())).thenReturn(false);
            verifyForwardDueToValidationError("comments.disabled", null);

            // comments allowed, but will show auth error if latter failed
            when(mockWEM.canSubmitNewComments(any())).thenReturn(true);
            when(mockRequest.getParameter("answer")).thenReturn("123");

            // no blogger, so authenticator will be activated
            when(mockRequest.getUserPrincipal()).thenReturn(null);
            when(mockAuthenticator.authenticate(mockRequest)).thenReturn(false);
            verifyForwardDueToValidationError("error.commentAuthFailed", "123");

            // ensure auth not checked if preview and content remains in commentForm
            when(mockRequest.getParameter("preview")).thenReturn("preview");
            when(mockRequest.getParameter("name")).thenReturn(null);
            verifyForwardDueToValidationError("error.commentPostNameMissing", null);
            ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            assertEquals("<p>My comment for this blog entry.</p>", commentCaptor.getValue().getContent());

            // ensure no authentication if authenticator null
            processor.setCommentAuthenticator(null);
            when(mockRequest.getParameter("preview")).thenReturn(null);
            verifyForwardDueToValidationError("error.commentPostNameMissing", null);

        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void testCreateCommentFromRequest() {
        when(mockRequest.getParameter("url")).thenReturn("www.foo.com");
        when(mockRequest.getParameter("content")).thenReturn("Enjoy <a href=\"http://www.abc.com\">My Link</a> from Bob!");

        WeblogPageRequest wpr = WeblogPageRequest.Creator.create(mockRequest, mock(PageModel.class));
        WeblogEntry entry = new WeblogEntry();
        User blogger = new User();
        wpr.setWeblogEntry(entry);
        wpr.setBlogger(blogger);

        WeblogEntryComment wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.LIMITED);

        assertEquals("Content not processed correctly (text, whitelist filtering of tags, and adding paragraph tags)",
                "<p>Enjoy My Link from Bob!</p>", wec.getContent());
        assertEquals("Sam", wec.getName());
        assertFalse("Bob", wec.isPreview());
        assertEquals("http:// not added to URL", "http://www.foo.com", wec.getUrl());
        assertTrue(wec.getNotify());
        assertEquals("sam@yopmail.com", wec.getEmail());
        assertEquals("https://www.duckduckgo.com", wec.getRemoteHost());
        assertEquals(entry, wec.getWeblogEntry());
        assertEquals(blogger, wec.getBlogger());
        assertNotNull(wec.getPostTime());

        when(mockRequest.getParameter("notify")).thenReturn(null);
        when(mockRequest.getParameter("preview")).thenReturn("true");
        wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.BASIC);
        assertFalse(wec.getNotify());
        assertTrue(wec.isPreview());
        assertEquals("Content not processed correctly (text and whitelist filtering of tags)",
                "<p>Enjoy <a href=\"http://www.abc.com\" rel=\"nofollow\">My Link</a> from Bob!</p>", wec.getContent());

        // test other cases
        when(mockRequest.getParameter("preview")).thenReturn("false");
        when(mockRequest.getParameter("url")).thenReturn("http://www.foo.com");
        wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.BASIC);
        assertFalse(wec.isPreview());
        assertEquals("http://www.foo.com", wec.getUrl());

        when(mockRequest.getParameter("url")).thenReturn("https://www.foo.com");
        wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.BASIC);
        assertEquals("https://www.foo.com", wec.getUrl());

        // capture some edge cases
        when(mockRequest.getUserPrincipal()).thenReturn(null);
        when(mockRequest.getParameter("content")).thenReturn("   ");
        when(mockRequest.getParameter("url")).thenReturn(null);
        wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.BASIC);
        assertNull(wec.getContent());
        assertNull(wec.getUrl());
    }

    @Test
    public void testValidateComment() {
        WeblogEntryComment comment = new WeblogEntryComment();

        comment.setContent("Hi!");
        comment.setName("Bob");
        comment.setEmail("bob@email.com");
        comment.setUrl("http://www.bob.com");

        String response = processor.validateComment(comment);
        assertNull(response);

        // URL is optional
        comment.setUrl(null);
        response = processor.validateComment(comment);
        assertNull(response);

        comment.setUrl("ftp://www.myftpsite.com");
        response = processor.validateComment(comment);
        assertEquals("error.commentPostFailedURL", response);

        comment.setUrl("http://www.bob.com");
        comment.setEmail("bob@abc");
        response = processor.validateComment(comment);
        assertEquals("error.commentPostFailedEmailAddress", response);
        comment.setEmail("");
        response = processor.validateComment(comment);
        assertEquals("error.commentPostFailedEmailAddress", response);
        comment.setEmail("bob@email.com");

        comment.setName(null);
        response = processor.validateComment(comment);
        assertEquals("error.commentPostNameMissing", response);
        comment.setName("Bob");

        comment.setContent("  ");
        response = processor.validateComment(comment);
        assertEquals("error.commentPostContentMissing", response);
    }

    @Test
    public void testGenerateAuthForm() throws IOException {
        String authHTML = "difficult math question";
        when(mockAuthenticator.getHtml(any(HttpServletRequest.class))).thenReturn(authHTML);

        PrintWriter mockWriter = mock(PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(mockWriter);

        processor.generateAuthForm(mockRequest, mockResponse);

        verify(mockResponse).setContentType("text/html; charset=utf-8");
        verify(mockResponse).addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        verify(mockResponse).addHeader("Pragma", "no-cache");
        verify(mockResponse).addHeader("Expires", "-1");
        verify(mockWriter).println(authHTML);

        // test if no authenticator, then empty auth form
        Mockito.clearInvocations(mockWriter);
        processor.setCommentAuthenticator(null);
        processor.generateAuthForm(mockRequest, mockResponse);
        verify(mockWriter).println("");
    }

    @Test
    public void testValidateCommentViaValidators() {
        WeblogEntryComment testComment = new WeblogEntryComment();
        Map<String, List<String>> testMessages = new HashMap<>();

        CommentValidator mockAlwaysBlatantValidator = mock(CommentValidator.class);
        when(mockAlwaysBlatantValidator.validate(any(WeblogEntryComment.class), anyMap()))
                .thenReturn(ValidationResult.BLATANT_SPAM);

        CommentValidator mockAlwaysSpamValidator = mock(CommentValidator.class);
        when(mockAlwaysSpamValidator.validate(any(WeblogEntryComment.class), anyMap()))
                .thenReturn(ValidationResult.SPAM);

        CommentValidator mockAlwaysNonSpamValidator = mock(CommentValidator.class);
        when(mockAlwaysNonSpamValidator.validate(any(WeblogEntryComment.class), anyMap()))
                .thenReturn(ValidationResult.NOT_SPAM);

        List<CommentValidator> emptyList = new ArrayList<>();
        List<CommentValidator> oneBlatantOneNonSpam = new ArrayList<>();
        oneBlatantOneNonSpam.add(mockAlwaysBlatantValidator);
        oneBlatantOneNonSpam.add(mockAlwaysNonSpamValidator);
        List<CommentValidator> oneSpamOneNonSpam = new ArrayList<>();
        oneSpamOneNonSpam.add(mockAlwaysSpamValidator);
        oneSpamOneNonSpam.add(mockAlwaysNonSpamValidator);
        List<CommentValidator> twoNonSpam = new ArrayList<>();
        twoNonSpam.add(mockAlwaysNonSpamValidator);
        twoNonSpam.add(mockAlwaysNonSpamValidator);

        processor.setCommentValidators(emptyList);
        assertEquals(ValidationResult.NOT_SPAM, processor.runSpamCheckers(testComment, testMessages));

        processor.setCommentValidators(oneBlatantOneNonSpam);
        assertEquals(ValidationResult.BLATANT_SPAM, processor.runSpamCheckers(testComment, testMessages));

        processor.setCommentValidators(oneSpamOneNonSpam);
        assertEquals(ValidationResult.SPAM, processor.runSpamCheckers(testComment, testMessages));
        oneSpamOneNonSpam.add(mockAlwaysBlatantValidator);
        assertEquals(ValidationResult.BLATANT_SPAM, processor.runSpamCheckers(testComment, testMessages));

        processor.setCommentValidators(twoNonSpam);
        assertEquals(ValidationResult.NOT_SPAM, processor.runSpamCheckers(testComment, testMessages));
        twoNonSpam.add(mockAlwaysSpamValidator);
        assertEquals(ValidationResult.SPAM, processor.runSpamCheckers(testComment, testMessages));
    }

    private void verifyForwardAfterSpamChecking(ApprovalStatus status, String commentStatusKey, boolean preview)
            throws ServletException, IOException {
        Mockito.clearInvocations(mockWEM, mockMessageSource, mockRequest, mockRequestDispatcher);
        processor.postComment(mockRequest, mockResponse);
        ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
        if (preview) {
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
        } else {
            verify(mockWEM).saveComment(commentCaptor.capture(), anyBoolean());
        }
        assertEquals(status, commentCaptor.getValue().getStatus());
        if (commentStatusKey != null) {
            verify(mockMessageSource).getMessage(commentStatusKey, null, Locale.GERMAN);
        }
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    private void verifyForwardDueToValidationError(String errorProperty, String errorValue)
            throws ServletException, IOException {
        Mockito.clearInvocations(mockMessageSource, mockRequest, mockRequestDispatcher);
        processor.postComment(mockRequest, mockResponse);
        ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
        verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
        assertTrue(commentCaptor.getValue().isInvalid());
        verify(mockMessageSource).getMessage(errorProperty, new Object[] {errorValue}, Locale.GERMAN);
        verify(mockRequest).getRequestDispatcher(PageProcessor.PATH + "/myblog/entry/entry-anchor");
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }
}
