/*
   Copyright 2017 Glen Mazza

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.tightblog.rendering.processors;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.rendering.comment.CommentAuthenticator;
import org.tightblog.rendering.comment.CommentValidator;
import org.tightblog.rendering.requests.WeblogPageRequest;
import org.tightblog.util.HTMLSanitizer;
import org.tightblog.util.I18nMessages;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CommentProcessorTest {

    private HttpServletRequest mockRequest;
    private HttpServletResponse mockResponse;
    private RequestDispatcher mockRequestDispatcher;
    private JPAPersistenceStrategy mockJPA;
    private WebloggerProperties properties;
    private CommentProcessor processor;
    private WeblogPageRequest.Creator wprCreator;
    private WeblogPageRequest commentRequest;
    private I18nMessages messageUtils = I18nMessages.getMessages(Locale.ENGLISH);

    @Before
    public void initialize() {
        mockRequest = mock(HttpServletRequest.class);
        mockResponse = mock(HttpServletResponse.class);
        mockRequestDispatcher = mock(RequestDispatcher.class);
        when(mockRequest.getRequestDispatcher(anyString())).thenReturn(mockRequestDispatcher);
        mockJPA = mock(JPAPersistenceStrategy.class);
        properties = new WebloggerProperties();
        properties.setCommentHtmlPolicy(HTMLSanitizer.Level.LIMITED);
        when(mockJPA.getWebloggerProperties()).thenReturn(properties);
        wprCreator = mock(WeblogPageRequest.Creator.class);
        commentRequest = new WeblogPageRequest();
        when(wprCreator.create(any())).thenReturn(commentRequest);
        processor = new CommentProcessor();
        processor.setPersistenceStrategy(mockJPA);
        processor.setWeblogPageRequestCreator(wprCreator);
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
            processor.postComment(mockRequest, mockResponse);
            verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void postCommentReturn404IfWeblogEntryNotFound() {
        commentRequest.setWeblog(new Weblog());
        processor.setWeblogEntryManager(mock(WeblogEntryManager.class));

        try {
            processor.postComment(mockRequest, mockResponse);
            verify(mockResponse).sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (IOException | ServletException e) {
            fail();
        }
    }

    @Test
    public void testValidationErrorForwarding() {
        processor = Mockito.spy(processor);

        Weblog weblog = new Weblog();
        weblog.setLocale("en");
        weblog.setHandle("myhandle");
        commentRequest.setWeblog(weblog);

        WeblogEntry mockEntry = mock(WeblogEntry.class);
        when(mockEntry.getAnchor()).thenReturn("myblogentry");

        WeblogEntryManager mockWEM = mock(WeblogEntryManager.class);
        when(mockWEM.getWeblogEntryByAnchor(any(), any())).thenReturn(mockEntry);
        processor.setWeblogEntryManager(mockWEM);

        WeblogEntryComment incomingComment = new WeblogEntryComment();
        // doReturn.when vs. when.theReturn wrt spies: https://stackoverflow.com/a/29394497/1207540
        Mockito.doReturn(incomingComment).when(processor).createCommentFromRequest(eq(mockRequest), eq(mockEntry), any());

        try {
            // will return disabled if comments not allowed
            when(mockEntry.getCommentsStillAllowed()).thenReturn(false);
            when(mockEntry.isPublished()).thenReturn(false);
            verifyForwardDueToValidationError(incomingComment, "comments.disabled", null);

            // will still show disabled if comments allowed but entry not published
            when(mockEntry.getCommentsStillAllowed()).thenReturn(true);
            when(mockEntry.isPublished()).thenReturn(false);
            verifyForwardDueToValidationError(incomingComment, "comments.disabled", null);

            // both published and comments allowed, will show auth error if latter failed
            when(mockEntry.isPublished()).thenReturn(true);
            incomingComment.setPreview(false);
            when(mockRequest.getParameter("answer")).thenReturn("123");

            CommentAuthenticator mockAuthenticator = mock(CommentAuthenticator.class);
            when(mockAuthenticator.authenticate(mockRequest)).thenReturn(false);
            processor.setCommentAuthenticator(mockAuthenticator);
            verifyForwardDueToValidationError(incomingComment, "error.commentAuthFailed", "123");

            // ensure auth not checked if preview
            incomingComment.setPreview(true);
            Mockito.doReturn("error.commentPostNameMissing").when(processor).validateComment(incomingComment);
            verifyForwardDueToValidationError(incomingComment, "error.commentPostNameMissing", null);

        } catch (IOException | ServletException e) {
            fail();
        }
    }

    private void verifyForwardDueToValidationError(WeblogEntryComment incomingComment, String errorProperty,
                                                   String errorValue)
            throws ServletException, IOException {
        processor.postComment(mockRequest, mockResponse);
        assertTrue(incomingComment.isError());
        assertEquals(messageUtils.getString(errorProperty, errorValue), incomingComment.getSubmitResponseMessage());
        verify(mockRequest).setAttribute("commentForm", incomingComment);
        verify(mockRequest).getRequestDispatcher(PageProcessor.PATH + "/myhandle/entry/myblogentry");
        verify(mockRequestDispatcher).forward(mockRequest, mockResponse);
        Mockito.clearInvocations(mockRequest, mockRequestDispatcher);
    }


    @Test
    public void testCreateCommentFromRequest() {
        when(mockRequest.getParameter("notify")).thenReturn("anything");
        when(mockRequest.getParameter("email")).thenReturn("bob@email.com");
        when(mockRequest.getParameter("url")).thenReturn("www.foo.com");
        when(mockRequest.getParameter("content")).thenReturn("Enjoy <a href=\"http://www.abc.com\">My Link</a> from Bob!");
        when(mockRequest.getParameter("name")).thenReturn("Bob");
        when(mockRequest.getParameter("preview")).thenReturn(null);
        when(mockRequest.getRemoteHost()).thenReturn("http://www.bar.com");

        WeblogEntry entry = new WeblogEntry();

        WeblogEntryComment wec = processor.createCommentFromRequest(mockRequest, entry, HTMLSanitizer.Level.LIMITED);

        assertEquals("Content not processed correctly (text, whitelist filtering of tags, and adding paragraph tags)",
                "<p>Enjoy My Link from Bob!</p>", wec.getContent());
        assertEquals("Bob", wec.getName());
        assertFalse("Bob", wec.isPreview());
        assertEquals("http:// not added to URL", "http://www.foo.com", wec.getUrl());
        assertTrue(wec.getNotify());
        assertEquals("bob@email.com", wec.getEmail());
        assertEquals("http://www.bar.com", wec.getRemoteHost());
        assertEquals(entry, wec.getWeblogEntry());
        assertNotNull(wec.getPostTime());

        when(mockRequest.getParameter("notify")).thenReturn(null);
        when(mockRequest.getParameter("preview")).thenReturn("true");
        wec = processor.createCommentFromRequest(mockRequest, entry, HTMLSanitizer.Level.BASIC);
        assertFalse(wec.getNotify());
        assertTrue(wec.isPreview());
        assertEquals("Content not processed correctly (text and whitelist filtering of tags)",
                "<p>Enjoy <a href=\"http://www.abc.com\" rel=\"nofollow\">My Link</a> from Bob!</p>", wec.getContent());

        when(mockRequest.getParameter("preview")).thenReturn("false");
        wec = processor.createCommentFromRequest(mockRequest, entry, HTMLSanitizer.Level.BASIC);
        assertFalse(wec.isPreview());
    }

    @Test
    public void testValidateComment() {
        WeblogEntryComment comment = new WeblogEntryComment();

        comment.setContent("Hi!");
        comment.setName("Bob");
        comment.setEmail("bob@email.com");
        comment.setUrl("http://www.bob.com");

        String response = processor.validateComment(comment);
        assertTrue(response == null);

        comment.setUrl("ftp://www.myftpsite.com");
        response = processor.validateComment(comment);
        assertEquals("error.commentPostFailedURL", response);
        comment.setUrl("http://www.bob.com");

        comment.setEmail("bob@abc");
        response = processor.validateComment(comment);
        assertEquals("error.commentPostFailedEmailAddress", response);
        comment.setEmail("");
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
        CommentAuthenticator mockAuthenticator = mock(CommentAuthenticator.class);
        when(mockAuthenticator.getHtml(any(HttpServletRequest.class))).thenReturn(authHTML);

        PrintWriter mockWriter = mock(PrintWriter.class);
        when(mockResponse.getWriter()).thenReturn(mockWriter);

        processor.setCommentAuthenticator(mockAuthenticator);
        processor.generateAuthForm(mockRequest, mockResponse);

        verify(mockResponse).setContentType("text/html; charset=utf-8");
        verify(mockResponse).addHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        verify(mockResponse).addHeader("Pragma", "no-cache");
        verify(mockResponse).addHeader("Expires", "-1");
        verify(mockWriter).println(authHTML);
    }

    @Test
    public void testValidateCommentViaValidators() {
        WeblogEntryComment testComment = new WeblogEntryComment();
        Map<String, List<String>> testMessages = new HashMap<String, List<String>>();

        CommentValidator mockAlwaysBlatantValidator = mock(CommentValidator.class);
        when(mockAlwaysBlatantValidator.validate(any(WeblogEntryComment.class), anyMap()))
                .thenReturn(CommentValidator.ValidationResult.BLATANT_SPAM);

        CommentValidator mockAlwaysSpamValidator = mock(CommentValidator.class);
        when(mockAlwaysSpamValidator.validate(any(WeblogEntryComment.class), anyMap()))
                .thenReturn(CommentValidator.ValidationResult.SPAM);

        CommentValidator mockAlwaysNonSpamValidator = mock(CommentValidator.class);
        when(mockAlwaysNonSpamValidator.validate(any(WeblogEntryComment.class), anyMap()))
                .thenReturn(CommentValidator.ValidationResult.NOT_SPAM);

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
        assertEquals(CommentValidator.ValidationResult.NOT_SPAM, processor.runSpamCheckers(testComment, testMessages));

        processor.setCommentValidators(oneBlatantOneNonSpam);
        assertEquals(CommentValidator.ValidationResult.BLATANT_SPAM, processor.runSpamCheckers(testComment, testMessages));

        processor.setCommentValidators(oneSpamOneNonSpam);
        assertEquals(CommentValidator.ValidationResult.SPAM, processor.runSpamCheckers(testComment, testMessages));
        oneSpamOneNonSpam.add(mockAlwaysBlatantValidator);
        assertEquals(CommentValidator.ValidationResult.BLATANT_SPAM, processor.runSpamCheckers(testComment, testMessages));

        processor.setCommentValidators(twoNonSpam);
        assertEquals(CommentValidator.ValidationResult.NOT_SPAM, processor.runSpamCheckers(testComment, testMessages));
        twoNonSpam.add(mockAlwaysSpamValidator);
        assertEquals(CommentValidator.ValidationResult.SPAM, processor.runSpamCheckers(testComment, testMessages));
    }
}