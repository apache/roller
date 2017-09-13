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
import org.tightblog.business.WeblogManager;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.util.Blacklist;
import org.tightblog.util.Utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BlacklistCommentValidatorTest {

    private WeblogEntryComment generateWeblogEntryComment() {
        Weblog weblog = new Weblog();
        WeblogEntry weblogEntry = new WeblogEntry();
        weblogEntry.setWeblog(weblog);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setWeblogEntry(weblogEntry);
        return wec;
    }

    private BlacklistCommentValidator generateBlacklistValidator(String blacklistStr) {
        Blacklist blacklist = new Blacklist(blacklistStr, null);
        WeblogManager mockWeblogManager = mock(WeblogManager.class);
        when(mockWeblogManager.getWeblogBlacklist(any())).thenReturn(blacklist);
        BlacklistCommentValidator validator = new BlacklistCommentValidator();
        validator.setWeblogManager(mockWeblogManager);
        return validator;
    }

    @Test
    public void acceptNullComment() throws Exception {
        BlacklistCommentValidator validator = generateBlacklistValidator("badword");
        WeblogEntryComment wec = generateWeblogEntryComment();
        wec.setContent(null);
        Map<String, List<String>> messageMap = new HashMap<>();
        int result = validator.validate(wec, messageMap);
        assertEquals("Null comment wasn't accepted", Utilities.PERCENT_100, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void failBlacklistInCommentURL() throws Exception {
        String badSite = "badword.com";
        BlacklistCommentValidator validator = generateBlacklistValidator(badSite);
        WeblogEntryComment wec = generateWeblogEntryComment();
        wec.setUrl(badSite);
        Map<String, List<String>> messageMap = new HashMap<>();
        int result = validator.validate(wec, messageMap);
        String expectedKey = "comment.validator.blacklistMessage";
        assertEquals("Blacklisted term in URL wasn't failed", 0, result);
        assertEquals("Message Map hasn't one entry",1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
    }

    @Test
    public void failBlacklistInCommentEmailAddress() throws Exception {
        String badWord = "badword.com";
        BlacklistCommentValidator validator = generateBlacklistValidator(badWord);
        WeblogEntryComment wec = generateWeblogEntryComment();
        wec.setEmail("abc@" + badWord);
        int result = validator.validate(wec, new HashMap<>());
        assertEquals("Blacklisted term in email address wasn't failed", 0, result);
    }

    @Test
    public void failBlacklistInCommentName() throws Exception {
        String badWord = "badword";
        BlacklistCommentValidator validator = generateBlacklistValidator(badWord);
        WeblogEntryComment wec = generateWeblogEntryComment();
        wec.setName("Bob " + badWord);
        int result = validator.validate(wec, new HashMap<>());
        assertEquals("Blacklisted term in commenter name wasn't failed", 0, result);
    }

    @Test
    public void failBlacklistInCommentContent() throws Exception {
        String badWord = "badword";
        BlacklistCommentValidator validator = generateBlacklistValidator(badWord);
        WeblogEntryComment wec = generateWeblogEntryComment();
        wec.setContent("hello " + badWord + " how are you");
        int result = validator.validate(wec, new HashMap<>());
        assertEquals("Blacklisted term in comment content wasn't failed", 0, result);
    }

    @Test
    public void acceptNoBlacklistedWordInComment() throws Exception {
        String badWord = "badword";
        BlacklistCommentValidator validator = generateBlacklistValidator(badWord);
        WeblogEntryComment wec = generateWeblogEntryComment();
        wec.setName("Bob");
        wec.setUrl("http://www.foo.com");
        wec.setEmail("bob@foo.com");
        wec.setContent("great blog article!");
        Map<String, List<String>> messageMap = new HashMap<>();
        int result = validator.validate(wec, messageMap);
        String expectedKey = "comment.validator.blacklistMessage";
        assertEquals("Clean comment wasn't accepted", Utilities.PERCENT_100, result);
        assertEquals("Message Map hasn't zero entries",0, messageMap.size());
    }
}