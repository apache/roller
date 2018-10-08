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
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.rendering.comment.CommentValidator.ValidationResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ExcessLinksCommentValidatorTest {

    private String generateCommentWithLinks(int numLinks) {
        StringBuilder commentBuilder = new StringBuilder();
        for (int i = 0; i < numLinks; i++) {
            char delim = (i % 2 == 0) ? '\'' : '"';
            commentBuilder.append("hi <a href=").append(delim)
                .append("http://www.aa.com").append(delim)
                .append(">link ").append(i + 1).append("</a>");
        }
        return commentBuilder.toString();
    }

    @Test
    public void acceptNullComment() throws Exception {
        ExcessLinksCommentValidator validator = new ExcessLinksCommentValidator();
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent(null);
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        assertEquals("Null comment wasn't accepted", ValidationResult.NOT_SPAM, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void acceptCommentLessThanLimit() throws Exception {
        ExcessLinksCommentValidator validator = new ExcessLinksCommentValidator();
        validator.setLimit(3);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent(generateCommentWithLinks(2));
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        assertEquals("Comment below limit wasn't accepted", ValidationResult.NOT_SPAM, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void acceptCommentEqualToLimit() throws Exception {
        ExcessLinksCommentValidator validator = new ExcessLinksCommentValidator();
        validator.setLimit(3);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent(generateCommentWithLinks(3));
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        assertEquals("Comment at limit wasn't accepted", ValidationResult.NOT_SPAM, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void failCommentMoreThanLimit() throws Exception {
        ExcessLinksCommentValidator validator = new ExcessLinksCommentValidator();
        validator.setLimit(3);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent(generateCommentWithLinks(4));
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        String expectedKey = "comment.validator.excessLinksMessage";
        assertEquals("Comment above limit was accepted", ValidationResult.SPAM, result);
        assertEquals("Message Map hasn't one entry", 1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
        assertEquals("Message Map value hasn't one element", 1,
                messageMap.get(expectedKey).size());
        assertEquals("Message Map value isn't limit size", "3",
                messageMap.get(expectedKey).get(0));
    }

    @Test
    public void validationIgnoredWithNegativeLimit() throws Exception {
        ExcessLinksCommentValidator validator = new ExcessLinksCommentValidator();
        validator.setLimit(-1);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent(generateCommentWithLinks(4));
        Map<String, List<String>> messageMap = new HashMap<>();
        ValidationResult result = validator.validate(wec, messageMap);
        assertEquals("Validation wasn't skipped with negative limit", ValidationResult.NOT_SPAM, result);
        assertEquals(0, messageMap.size());
    }

}
