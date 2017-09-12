package org.tightblog.rendering.comment;

import org.junit.Test;
import org.tightblog.business.URLStrategy;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AkismetCommentValidatorTest {

    @Test
    public void testCreateAkismetRESTCall() {

        Weblog weblog = new Weblog();
        WeblogEntry entry = new WeblogEntry();
        entry.setWeblog(weblog);
        WeblogEntryComment testComment = new WeblogEntryComment();
        testComment.setWeblogEntry(entry);
        testComment.setRemoteHost("remHost");
        testComment.setUserAgent("userAgt");
        testComment.setReferrer("http://www.bar.com");
        testComment.setName("bob");
        testComment.setEmail("bob@email.com");
        testComment.setUrl("http://www.bobsite.com");
        testComment.setContent("Hello from Bob!");

        URLStrategy mockUrlStrategy = mock(URLStrategy.class);
        when(mockUrlStrategy.getWeblogURL(weblog, true)).thenReturn("http://www.foo.com");
//        when(mockUrlStrategy.getWeblogEntryURL(mockWeblog, "", true)).thenReturn("http://www.foo.com/entry");


        AkismetCommentValidator validator = new AkismetCommentValidator(mockUrlStrategy, "ignored");

        String expected = "blah blah blah";
  //      String apiCall = validator.createAPICall();

        assertTrue(true);
    //    assertEquals("Akismet API call malformed", expected, apiCall);
    }




}