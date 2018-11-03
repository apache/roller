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
package org.tightblog.rendering.comment;

import org.junit.Test;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WebloggerProperties;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.tightblog.rendering.comment.CommentValidator.ValidationResult;
import org.tightblog.repository.WebloggerPropertiesRepository;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BlacklistCommentValidatorTest {

    private WeblogEntryComment generateWeblogEntryComment(String weblogBlacklistStr) {
        Weblog weblog = new Weblog();
        weblog.setBlacklist(weblogBlacklistStr);
        WeblogEntry weblogEntry = new WeblogEntry();
        weblogEntry.setWeblog(weblog);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setWeblogEntry(weblogEntry);
        return wec;
    }

    private BlacklistCommentValidator generateBlacklistValidator(String siteBlacklistStr) {
        WebloggerProperties properties = new WebloggerProperties();
        properties.setCommentSpamFilter(siteBlacklistStr);
        WebloggerPropertiesRepository mockPropertiesRepository = mock(WebloggerPropertiesRepository.class);
        when(mockPropertiesRepository.findOrNull()).thenReturn(properties);
        return new BlacklistCommentValidator(mockPropertiesRepository);
    }

    @Test
    public void acceptNullComment() {
        BlacklistCommentValidator validator = generateBlacklistValidator("badsiteword");
        WeblogEntryComment wec = generateWeblogEntryComment("badweblogword");
        wec.setContent(null);
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        assertEquals("Null comment wasn't accepted", ValidationResult.NOT_SPAM, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void failBlacklistInCommentURL() {
        BlacklistCommentValidator validator = generateBlacklistValidator("badurl\\.com");
        WeblogEntryComment wec = generateWeblogEntryComment("");
        wec.setUrl("badurl.com");
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        String expectedKey = "comment.validator.blacklistMessage";
        assertEquals("Blacklisted term in URL wasn't failed", ValidationResult.SPAM, result);
        assertEquals("Message Map hasn't one entry", 1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
    }

    @Test
    public void failBlacklistInCommentEmailAddress() {
        BlacklistCommentValidator validator = generateBlacklistValidator("");
        WeblogEntryComment wec = generateWeblogEntryComment("badorg\\.com");
        wec.setEmail("abc@badorg.com");
        ValidationResult result = validator.validate(wec, new HashMap<>());
        assertEquals("Blacklisted term in email address wasn't failed", ValidationResult.SPAM, result);
    }

    @Test
    public void failBlacklistInCommentName() {
        String badPerson = "Bad Person";
        BlacklistCommentValidator validator = generateBlacklistValidator(badPerson);
        WeblogEntryComment wec = generateWeblogEntryComment("");
        wec.setName(badPerson);
        ValidationResult result = validator.validate(wec, new HashMap<>());
        assertEquals("Blacklisted term in commenter name wasn't failed", ValidationResult.SPAM, result);
    }

    @Test
    public void failBlacklistInCommentContent() {
        String badWord = "badword";
        BlacklistCommentValidator validator = generateBlacklistValidator("");
        WeblogEntryComment wec = generateWeblogEntryComment(badWord);
        // testing case-insensitive match
        wec.setContent("hello " + badWord.toUpperCase() + " how are you");
        ValidationResult result = validator.validate(wec, new HashMap<>());
        assertEquals("Blacklisted term in comment content wasn't failed", ValidationResult.SPAM, result);
    }

    @Test
    public void acceptNoBlacklistedWordInComment() {
        BlacklistCommentValidator validator = generateBlacklistValidator("badword");
        WeblogEntryComment wec = generateWeblogEntryComment("verybadword");
        wec.setName("Bob");
        wec.setUrl("http://www.foo.com");
        wec.setEmail("bob@foo.com");
        wec.setContent("great blog article!");
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        assertEquals("Clean comment wasn't accepted", ValidationResult.NOT_SPAM, result);
        assertEquals("Message Map hasn't zero entries", 0, messageMap.size());
    }

    @Test
    public void testPopulateSpamRules() {
        String blacklistString = "badword\nbadUrl\\.com\n#comment to ignore\nbad phrase";
        List<Pattern> patterns = BlacklistCommentValidator.populateSpamRules(blacklistString);
        assertEquals(patterns.size(), 3);
        assertEquals("badword", patterns.get(0).pattern());
        assertEquals("badUrl\\.com", patterns.get(1).pattern());
        assertEquals("bad phrase", patterns.get(2).pattern());
    }
}
