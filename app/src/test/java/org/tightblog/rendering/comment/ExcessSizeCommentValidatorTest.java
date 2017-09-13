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
import org.tightblog.util.Utilities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class ExcessSizeCommentValidatorTest {

    @Test
    public void checkDefaultThreshold() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        int threshold = validator.getLimit();
        assertEquals("default threshold not expected value", 1000, threshold);
    }

    @Test
    public void acceptNullComment() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        validator.setLimit(10);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent(null);
        Map<String, List<String>> messageMap = new HashMap<>();
        int result = validator.validate(wec, messageMap);
        assertEquals("Null comment wasn't accepted", Utilities.PERCENT_100, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void acceptCommentLessThanLimit() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        validator.setLimit(7);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent("123456");
        Map<String, List<String>> messageMap = new HashMap<>();
        int result = validator.validate(wec, messageMap);
        assertEquals("Comment below limit wasn't accepted", Utilities.PERCENT_100, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void acceptCommentEqualToLimit() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        validator.setLimit(6);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent("123456");
        Map<String, List<String>> messageMap = new HashMap<>();
        int result = validator.validate(wec, messageMap);
        assertEquals("Comment at limit wasn't accepted", Utilities.PERCENT_100, result);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void failCommentMoreThanLimit() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        int limitLength = 5;
        validator.setLimit(limitLength);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent("123456");
        Map<String, List<String>> messageMap = new HashMap<>();
        int result = validator.validate(wec, messageMap);
        String expectedKey = "comment.validator.excessSizeMessage";
        assertEquals("Comment above limit was accepted", 0, result);
        assertEquals("Message Map hasn't one entry",1, messageMap.size());
        assertTrue("Message Map missing correct key", messageMap.containsKey(expectedKey));
        assertEquals("Message Map value hasn't one element", 1,
                messageMap.get(expectedKey).size());
        assertEquals("Message Map value isn't limit size", "" + limitLength,
                messageMap.get(expectedKey).get(0));
    }
}
