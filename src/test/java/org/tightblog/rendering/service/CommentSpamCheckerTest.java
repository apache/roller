/*
   Copyright 2019 Glen Mazza

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
package org.tightblog.rendering.service;

import org.junit.Before;
import org.junit.Test;
import org.tightblog.dao.WebloggerPropertiesDao;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.SpamCheckResult;
import org.tightblog.domain.WebloggerProperties;
import org.tightblog.service.URLService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CommentSpamCheckerTest {

    private WebloggerPropertiesDao mockWebloggerPropertiesDao;
    private URLService mockUrlService;
    private Map<String, List<String>> messageMap;
    private WebloggerProperties webloggerProperties;
    private Weblog weblog;
    private WeblogEntryComment comment;

    @Before
    public void initializeMocks() {
        mockWebloggerPropertiesDao = mock(WebloggerPropertiesDao.class);
        mockUrlService = mock(URLService.class);
        messageMap = new HashMap<>();

        webloggerProperties = new WebloggerProperties();
        when(mockWebloggerPropertiesDao.findOrNull()).thenReturn(webloggerProperties);

        weblog = new Weblog();
        WeblogEntry weblogEntry = new WeblogEntry();
        weblogEntry.setWeblog(weblog);
        comment = new WeblogEntryComment();
        comment.setWeblogEntry(weblogEntry);

        when(mockUrlService.getWeblogURL(weblog)).thenReturn("http://www.foo.com");
        when(mockUrlService.getWeblogEntryURL(weblogEntry)).thenReturn("http://www.foo.com/entry/bar");
    }

    @Test
    public void validateLinkCount() {
        CommentSpamChecker newCommentValidator = createValidator(-1, 3);

        SpamCheckResult vr = newCommentValidator.evaluateViaExcessSize(
                generateCommentWithLinks(2), messageMap);
        assertEquals(SpamCheckResult.NOT_SPAM, vr);
        assertEquals(0, messageMap.size());

        messageMap = new HashMap<>();
        vr = newCommentValidator.evaluateViaExcessSize(
                generateCommentWithLinks(3), messageMap);
        assertEquals(SpamCheckResult.NOT_SPAM, vr);
        assertEquals(0, messageMap.size());

        messageMap = new HashMap<>();
        vr = newCommentValidator.evaluateViaExcessSize(
                generateCommentWithLinks(4), messageMap);
        assertEquals(SpamCheckResult.SPAM, vr);
        String expectedKey = "commentSpamChecker.excessLinksMessage";
        assertEquals("Message Map hasn't one entry", 1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
    }

    @Test
    public void validateCommentLength() {
        CommentSpamChecker newCommentValidator = createValidator(5, -1);

        SpamCheckResult vr = newCommentValidator.evaluateViaExcessSize(
                new WeblogEntryComment(null), messageMap);
        assertEquals(SpamCheckResult.NOT_SPAM, vr);
        assertEquals(0, messageMap.size());

        messageMap = new HashMap<>();
        vr = newCommentValidator.evaluateViaExcessSize(
                new WeblogEntryComment("1234"), messageMap);
        assertEquals(SpamCheckResult.NOT_SPAM, vr);
        assertEquals(0, messageMap.size());

        messageMap = new HashMap<>();
        vr = newCommentValidator.evaluateViaExcessSize(
                new WeblogEntryComment("12345"), messageMap);
        assertEquals(SpamCheckResult.NOT_SPAM, vr);
        assertEquals(0, messageMap.size());

        messageMap = new HashMap<>();
        vr = newCommentValidator.evaluateViaExcessSize(
                new WeblogEntryComment("123456"), messageMap);
        assertEquals(SpamCheckResult.SPAM, vr);
        String expectedKey = "commentSpamChecker.excessSizeMessage";
        assertEquals("Message Map hasn't one entry", 1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
    }

    @Test
    public void compileBlacklist() {
        String blacklistString = "badword,badUrl.com,bad phrase";
        List<Pattern> patterns = CommentSpamChecker.compileBlacklist(blacklistString);
        assertEquals(patterns.size(), 3);
        assertEquals("\\b(badword)\\b", patterns.get(0).pattern());
        assertEquals("\\b(badUrl\\.com)\\b", patterns.get(1).pattern());
        assertEquals("\\b(bad phrase)\\b", patterns.get(2).pattern());
    }

    @Test
    public void validateViaBlacklist() {
        webloggerProperties.setGlobalSpamFilter("bad phrase");
        CommentSpamChecker newCommentValidator = createValidator(-1, 3);

        weblog.setBlacklist("badweblogword");
        // comment fields all null here, validator should pass
        SpamCheckResult result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("Null comment wasn't accepted", SpamCheckResult.NOT_SPAM, result);
        assertEquals(0, messageMap.size());

        comment.setContent("a badweblogword");
        result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("Weblog blacklist ignored", SpamCheckResult.SPAM, result);

        comment.setContent("goodword");
        comment.setEmail("a bad phrase is here");
        result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("Global blacklist ignored", SpamCheckResult.SPAM, result);

        comment.setContent("goodword");
        comment.setEmail("goodword");
        comment.setName("bad phrase");
        result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("Name not checked against blacklist", SpamCheckResult.SPAM, result);

        comment.setContent("goodword");
        comment.setEmail("goodword");
        comment.setName("goodword");
        comment.setUrl("a badweblogword");
        result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("URL not checked against blacklist", SpamCheckResult.SPAM, result);

        messageMap = new HashMap<>();
        comment.setContent("goodword");
        comment.setEmail("goodword");
        comment.setName("goodword");
        comment.setUrl("goodword");
        result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("Blacklist failing non-spam content", SpamCheckResult.NOT_SPAM, result);
        assertEquals("Message Map hasn't zero entries", 0, messageMap.size());

        weblog.setBlacklist("badsite.com");
        comment.setContent("badsite.com");
        result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("Blacklist not working with periods", SpamCheckResult.SPAM, result);

        comment.setContent("badsiteacom");
        result = newCommentValidator.evaluateViaBlacklist(comment, messageMap);
        assertEquals("Blacklist not escaping periods", SpamCheckResult.NOT_SPAM, result);
    }

    @Test
    public void testCreateAkismetCallBody() {
        CommentSpamChecker newCommentValidator = createValidator(-1, 3);
        comment.setRemoteHost("remHost");
        comment.setUserAgent("userAgt");
        comment.setReferrer("http://www.bar.com");
        comment.setName("bob");
        comment.setEmail("bob@email.com");
        comment.setUrl("http://www.bobsite.com");
        comment.setContent("Hello from Bob!");
        
        String apiCall = newCommentValidator.createAkismetRequestBody(comment);

        String expected = "blog=http://www.foo.com&user_ip=remHost&user_agent=userAgt&referrer=http://www.bar.com" +
                "&permalink=http://www.foo.com/entry/bar&comment_type=comment" +
                "&comment_author=bob&comment_author_email=bob@email.com" +
                "&comment_author_url=http://www.bobsite.com&comment_content=Hello from Bob!";
        assertEquals("Akismet API call malformed", expected, apiCall);
    }

    @Test
    public void validateViaAkismet() {
    }

    private CommentSpamChecker createValidator(int sizeLimit, int linksLimit) {
        CommentSpamChecker ncv = new CommentSpamChecker(mockUrlService, mockWebloggerPropertiesDao,
                "Version 1.2.3", true, true, true, sizeLimit,
                linksLimit, "apikey", false);
        ncv.refreshGlobalBlacklist();
        return ncv;
    }

    private static WeblogEntryComment generateCommentWithLinks(int numLinks) {
        StringBuilder commentBuilder = new StringBuilder();
        for (int i = 0; i < numLinks; i++) {
            char delim = (i % 2 == 0) ? '\'' : '"';
            commentBuilder.append("hi <a href=").append(delim)
                    .append("http://www.aa.com").append(delim)
                    .append(">link ").append(i + 1).append("</a>");
        }
        return new WeblogEntryComment(commentBuilder.toString());
    }
}
