package org.tightblog.rendering.comment;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.WebloggerTest;
import org.tightblog.service.URLService;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ValidationResult;
import org.tightblog.service.WeblogEntryManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AkismetCommentValidatorTest {

    private static Logger log = LoggerFactory.getLogger(AkismetCommentValidatorTest.class);

    private Weblog weblog;
    private WeblogEntry entry;
    private WeblogEntryComment testComment;
    private WeblogEntryManager mockWEM;
    private URLService mockURLService;

    @Before
    public void initialize() {
        weblog = new Weblog();
        entry = new WeblogEntry();
        entry.setWeblog(weblog);
        testComment = new WeblogEntryComment();
        testComment.setWeblogEntry(entry);
        testComment.setRemoteHost("remHost");
        testComment.setUserAgent("userAgt");
        testComment.setReferrer("http://www.bar.com");
        testComment.setName("bob");
        testComment.setEmail("bob@email.com");
        testComment.setUrl("http://www.bobsite.com");
        testComment.setContent("Hello from Bob!");
        mockWEM = mock(WeblogEntryManager.class);
        mockURLService = mock(URLService.class);
    }

    @Test
    public void testCreateAkismetCallBody() {
        when(mockURLService.getWeblogURL(weblog)).thenReturn("http://www.foo.com");
        when(mockURLService.getWeblogEntryURL(entry)).thenReturn("http://www.foo.com/entry/bar");

        AkismetCommentValidator validator = new AkismetCommentValidator(mockWEM, mockURLService, false);
        String apiCall = validator.createAPIRequestBody(testComment);

        String expected = "blog=http://www.foo.com&user_ip=remHost&user_agent=userAgt&referrer=http://www.bar.com" +
                "&permalink=http://www.foo.com/entry/bar&comment_type=comment" +
                "&comment_author=bob&comment_author_email=bob@email.com" +
                "&comment_author_url=http://www.bobsite.com&comment_content=Hello from Bob!";
        assertEquals("Akismet API call malformed", expected, apiCall);
    }

    @Test
    public void testValidatorHandlesNonSpam() throws IOException {
        when(mockWEM.makeAkismetCall(anyString())).thenReturn(ValidationResult.NOT_SPAM);
        AkismetCommentValidator validator = new AkismetCommentValidator(mockWEM, mockURLService, false);
        String apiRequestBody = validator.createAPIRequestBody(testComment);

        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(testComment, messageMap);
        assertEquals("Comment not marked as non-spam", ValidationResult.NOT_SPAM, result);
        assertEquals("Message Map hasn't zero entries", 0, messageMap.size());

        // confirm Akismet call made with expected values
        verify(mockWEM).makeAkismetCall(eq(apiRequestBody));
    }

    @Test
    public void testValidatorHandlesSpam() throws IOException {
        when(mockWEM.makeAkismetCall(anyString())).thenReturn(ValidationResult.SPAM);
        AkismetCommentValidator validator = new AkismetCommentValidator(mockWEM, mockURLService, false);

        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(testComment, messageMap);
        String expectedKey = "comment.validator.akismetMessage.spam";
        assertEquals("Comment not marked as spam", ValidationResult.SPAM, result);
        assertEquals("Message Map hasn't one entry", 1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
        assertNull("Message Map value isn't null", messageMap.get(expectedKey));
    }

    @Test
    public void testValidatorHandlesBlatantSpamWithDelete() throws IOException {
        when(mockWEM.makeAkismetCall(anyString())).thenReturn(ValidationResult.BLATANT_SPAM);
        AkismetCommentValidator validator = new AkismetCommentValidator(mockWEM, mockURLService, true);

        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(testComment, messageMap);
        assertEquals("Comment not marked as blatant spam", ValidationResult.BLATANT_SPAM, result);
        assertEquals("Message Map hasn't zero entries", 0, messageMap.size());
    }

    @Test
    public void testValidatorHandlesBlatantSpamWithNonDelete() throws IOException {
        when(mockWEM.makeAkismetCall(anyString())).thenReturn(ValidationResult.BLATANT_SPAM);

        AkismetCommentValidator validator = new AkismetCommentValidator(mockWEM, mockURLService, false);

        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(testComment, messageMap);
        String expectedKey = "comment.validator.akismetMessage.blatantNoDelete";
        assertEquals("Non-delete blatant spam not marked as spam", ValidationResult.SPAM, result);
        assertEquals("Message Map hasn't one entry", 1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
        assertNull("Message Map value isn't null", messageMap.get(expectedKey));
    }

    @Test
    public void testValidatorTreatsExceptionAsSpam() throws IOException {
        when(mockWEM.makeAkismetCall(anyString())).thenThrow(new IOException());
        AkismetCommentValidator validator = new AkismetCommentValidator(mockWEM, mockURLService, false);

        Map<String, List<String>> messageMap = new HashMap<>();
        WebloggerTest.logExpectedException(log, "Exception");
        ValidationResult result = validator.validate(testComment, messageMap);
        String expectedKey = "comment.validator.akismetMessage.error";
        assertEquals("Comment not marked as spam", ValidationResult.SPAM, result);
        assertEquals("Message Map hasn't one entry", 1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
        assertNull("Message Map value isn't null", messageMap.get(expectedKey));
    }
}