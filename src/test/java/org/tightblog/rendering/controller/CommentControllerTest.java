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
package org.tightblog.rendering.controller;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.context.MessageSource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.tightblog.TestUtils;
import org.tightblog.domain.WebloggerProperties.SpamPolicy;
import org.tightblog.rendering.model.PageModel;
import org.tightblog.rendering.service.CommentSpamChecker;
import org.tightblog.service.EmailService;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ApprovalStatus;
import org.tightblog.domain.WeblogEntryComment.SpamCheckResult;
import org.tightblog.domain.WeblogRole;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.domain.WebloggerProperties.CommentPolicy;
import org.tightblog.rendering.service.CommentAuthenticator;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.dao.UserDao;
import org.tightblog.dao.WeblogDao;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.util.HTMLSanitizer;

import javax.persistence.EntityManager;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Locale;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class CommentControllerTest {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private RequestDispatcher mockRequestDispatcher;
    private Weblog weblog;
    private WeblogEntry weblogEntry;
    private User user;
    private WebloggerProperties properties;
    private CommentController processor;
    private MessageSource mockMessageSource;
    private WeblogDao mockWD;
    private WeblogEntryManager mockWEM;
    private UserDao mockUD;
    private UserManager mockUM;
    private LuceneIndexer mockIM;
    private EmailService mockES = mock(EmailService.class);
    private CommentSpamChecker mockCommentSpamChecker;
    private CommentAuthenticator mockAuthenticator;
    private Principal mockPrincipal;

    @Before
    public void initialize() {
        mockRequest = TestUtils.createMockServletRequestForWeblogEntryRequest();
        when(mockRequest.getParameter("name")).thenReturn("Sam");
        when(mockRequest.getParameter("email")).thenReturn("sam@yopmail.com");
        when(mockRequest.getParameter("notify")).thenReturn("notify");
        when(mockRequest.getRemoteHost()).thenReturn("https://www.duckduckgo.com");
        when(mockRequest.getParameter("url")).thenReturn("https://www.glenmazza.net");
        when(mockRequest.getParameter("content")).thenReturn("My comment for this blog entry.");

        mockRequestDispatcher = mock(RequestDispatcher.class);
        when(mockRequest.getRequestDispatcher(anyString())).thenReturn(mockRequestDispatcher);

        WebloggerPropertiesDao mockPropertiesDao = mock(WebloggerPropertiesDao.class);
        properties = new WebloggerProperties();
        properties.setCommentHtmlPolicy(HTMLSanitizer.Level.LIMITED);
        properties.setSpamPolicy(SpamPolicy.MARK_SPAM);
        when(mockPropertiesDao.findOrNull()).thenReturn(properties);

        mockCommentSpamChecker = mock(CommentSpamChecker.class);
        when(mockCommentSpamChecker.evaluate(any(), any())).thenReturn(SpamCheckResult.NOT_SPAM);

        mockWD = mock(WeblogDao.class);
        weblog = new Weblog();
        weblog.setHandle("myblog");
        when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(weblog);

        mockWEM = mock(WeblogEntryManager.class);
        weblogEntry = new WeblogEntry();
        weblogEntry.setAnchor("entry-anchor");
        weblogEntry.setStatus(WeblogEntry.PubStatus.PUBLISHED);
        when(mockWEM.getWeblogEntryByAnchor(weblog, weblogEntry.getAnchor())).thenReturn(weblogEntry);
        when(mockWEM.canSubmitNewComments(weblogEntry)).thenReturn(true);

        mockPrincipal = mock(Principal.class);
        when(mockPrincipal.getName()).thenReturn("bob");

        mockUD = mock(UserDao.class);
        user = new User();
        user.setUserName("bob");
        when(mockUD.findEnabledByUserName("bob")).thenReturn(user);

        mockIM = mock(LuceneIndexer.class);
        when(mockIM.isIndexComments()).thenReturn(true);

        mockES = mock(EmailService.class);
        mockUM = mock(UserManager.class);
        mockMessageSource = mock(MessageSource.class);
        PageModel mockPageModel = mock(PageModel.class);

        processor = new CommentController(mockWD, mockUD, mockIM, mockWEM, mockUM,
                mockES, mockMessageSource, mockPageModel, mockCommentSpamChecker,
                mockPropertiesDao);

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
            when(mockCommentSpamChecker.evaluate(any(), any())).thenReturn(SpamCheckResult.SPAM);

            // still approved
            verifyForwardAfterSpamChecking(ApprovalStatus.APPROVED, "commentServlet.commentAccepted");
            ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            WeblogEntryComment testComment = commentCaptor.getValue();
            assertEquals("", testComment.getContent());

            // blogger written to comment object
            assertEquals(user, testComment.getBlogger());

            // make subsequent tests a non-blogger comment
            when(mockAuthenticator.authenticate(mockRequest)).thenReturn(true);
            when(mockUM.checkWeblogRole(any(User.class), any(Weblog.class), eq(WeblogRole.POST))).thenReturn(false);
            when(mockUD.findEnabledByUserName(any())).thenReturn(null);

            Mockito.clearInvocations(mockWEM, mockES, mockIM);

            // check no indexing if indexing shut off
            Mockito.clearInvocations(mockWEM, mockES, mockIM, mockRequest);
            weblog.setAllowComments(CommentPolicy.MODERATE_NONPUB);
            weblog.setSpamPolicy(SpamPolicy.MARK_SPAM);
            when(mockIM.isIndexComments()).thenReturn(false);
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);
            verify(mockES).sendPendingCommentNotice(any(), any());
            verify(mockIM, never()).updateIndex(weblogEntry, false);

            // no blogger written to comment object
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            testComment = commentCaptor.getValue();
            assertNull(testComment.getBlogger());

            // check spam persisted to database with autodelete spam off
            Mockito.clearInvocations(mockWEM, mockES, mockIM);
            when(mockCommentSpamChecker.evaluate(any(), any())).thenReturn(SpamCheckResult.SPAM);
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);
            verify(mockWEM).saveComment(any(), anyBoolean());
            verify(mockES).sendPendingCommentNotice(any(), any());
            verify(mockIM, never()).updateIndex(any(WeblogEntry.class), anyBoolean());

            // check spam not persisted to database with autodelete spam on
            Mockito.clearInvocations(mockWEM);
            properties.setSpamPolicy(SpamPolicy.JUST_DELETE);
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);
            verify(mockWEM, never()).saveComment(any(), anyBoolean());

            Mockito.clearInvocations(mockWEM);

        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void checkBloggerFieldPopulatedCorrectly() {
        try {
            properties.setCommentPolicy(CommentPolicy.MODERATE_NONPUB);
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), mockPrincipal);
            ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            assertEquals(user, commentCaptor.getValue().getBlogger());

            Mockito.clearInvocations(mockRequest);
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);
            verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
            assertNull(commentCaptor.getValue().getBlogger());
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void postCommentReturn404IfCommentingDisabled() {
        properties.setCommentPolicy(WebloggerProperties.CommentPolicy.NONE);

        try {
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);
            verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void postCommentReturn404IfWeblogNotFound() {
        try {
            when(mockWD.findByHandleAndVisibleTrue("myblog")).thenReturn(null);
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);
            verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void postCommentReturn404IfWeblogEntryUnavailable() {
        when(mockWD.findByHandleAndVisibleTrue(any())).thenReturn(new Weblog());

        try {
            // entry null
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);

            // entry available but not published
            WeblogEntry entry = new WeblogEntry();
            entry.setStatus(WeblogEntry.PubStatus.DRAFT);
            when(mockWEM.getWeblogEntryByAnchor(any(), any())).thenReturn(entry);
            processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), null);
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
            verifyForwardDueToValidationError("comments.disabled", null, true);

            // comments allowed, but will show auth error if latter failed
            when(mockWEM.canSubmitNewComments(any())).thenReturn(true);
            when(mockRequest.getParameter("answer")).thenReturn("123");

            // no blogger, so authenticator will be activated
            when(mockAuthenticator.authenticate(mockRequest)).thenReturn(false);
            verifyForwardDueToValidationError("error.commentAuthFailed", "123", false);

            // ensure no authentication if authenticator null
            when(mockRequest.getParameter("name")).thenReturn(null);
            processor.setCommentAuthenticator(null);
            verifyForwardDueToValidationError("error.commentPostNameMissing", null, false);

        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void testCreateCommentFromRequest() {
        when(mockRequest.getParameter("url")).thenReturn("www.foo.com");
        when(mockRequest.getParameter("content")).thenReturn("Enjoy <a href=\"http://www.abc.com\">My Link</a> from Bob!");

        WeblogPageRequest wpr = new WeblogPageRequest("myblog", mockPrincipal, mock(PageModel.class));
        WeblogEntry entry = new WeblogEntry();
        User blogger = new User();
        wpr.setWeblogEntry(entry);
        wpr.setBlogger(blogger);

        WeblogEntryComment wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.LIMITED);

        assertEquals("Content not processed correctly (text, whitelist filtering of tags, and adding paragraph tags)",
                "<p>Enjoy My Link from Bob!</p>", wec.getContent());
        assertEquals("Sam", wec.getName());
        assertEquals("https:// not added to URL", "https://www.foo.com", wec.getUrl());
        assertTrue(wec.getNotify());
        assertEquals("sam@yopmail.com", wec.getEmail());
        assertEquals("https://www.duckduckgo.com", wec.getRemoteHost());
        assertEquals(entry, wec.getWeblogEntry());
        assertEquals(blogger, wec.getBlogger());
        assertNotNull(wec.getPostTime());

        when(mockRequest.getParameter("notify")).thenReturn(null);
        wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.BASIC);
        assertFalse(wec.getNotify());
        assertEquals("Content not processed correctly (text and whitelist filtering of tags)",
                "<p>Enjoy <a href=\"http://www.abc.com\" rel=\"nofollow\">My Link</a> from Bob!</p>", wec.getContent());

        // test other cases
        when(mockRequest.getParameter("url")).thenReturn("http://www.foo.com");
        wec = processor.createCommentFromRequest(mockRequest, wpr, HTMLSanitizer.Level.BASIC);
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

        ResponseEntity<Resource> result = processor.generateAuthForm(mockRequest);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(CacheControl.noStore().getHeaderValue(), result.getHeaders().getCacheControl());
        assertEquals(MediaType.TEXT_HTML, result.getHeaders().getContentType());
        assertEquals(authHTML.length(), result.getHeaders().getContentLength());

        // test if no authenticator, then empty auth form
        Mockito.clearInvocations(mockWriter);
        processor.setCommentAuthenticator(null);
        result = processor.generateAuthForm(mockRequest);
        assertEquals(0, result.getHeaders().getContentLength());
    }

    private void verifyForwardAfterSpamChecking(ApprovalStatus status, String commentStatusKey)
            throws ServletException, IOException {
        Mockito.clearInvocations(mockWEM, mockMessageSource, mockRequest, mockRequestDispatcher);
        processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(), mockPrincipal);
        ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
        verify(mockWEM).saveComment(commentCaptor.capture(), anyBoolean());
        assertEquals(status, commentCaptor.getValue().getStatus());
        if (commentStatusKey != null) {
            verify(mockMessageSource).getMessage(commentStatusKey, null, Locale.GERMAN);
        }
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }

    private void verifyForwardDueToValidationError(String errorProperty, String errorValue, boolean hasPrincipal)
            throws ServletException, IOException {
        Mockito.clearInvocations(mockMessageSource, mockRequest, mockRequestDispatcher);
        processor.postComment(mockRequest, mockResponse, weblog.getHandle(), weblogEntry.getAnchor(),
                hasPrincipal ? mockPrincipal : null);
        ArgumentCaptor<WeblogEntryComment> commentCaptor = ArgumentCaptor.forClass(WeblogEntryComment.class);
        verify(mockRequest).setAttribute(eq("commentForm"), commentCaptor.capture());
        assertTrue(commentCaptor.getValue().isInvalid());
        verify(mockMessageSource).getMessage(errorProperty, new Object[] {errorValue}, Locale.GERMAN);
        verify(mockRequest).getRequestDispatcher(PageController.PATH + "/myblog/entry/entry-anchor");
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
    }
}
