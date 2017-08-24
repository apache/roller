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
    public void acceptNullComment() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        validator.setThreshold(10);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent(null);
        Map<String, List<String>> messageMap = new HashMap<>();
        validator.validate(wec, messageMap);
        assertEquals("Null comment wasn't accepted", Utilities.PERCENT_100, Utilities.PERCENT_100);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void acceptCommentLessThanThreshold() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        validator.setThreshold(7);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent("123456");
        Map<String, List<String>> messageMap = new HashMap<>();
        validator.validate(wec, messageMap);
        assertEquals("Comment shorter than threshold wasn't accepted", Utilities.PERCENT_100, Utilities.PERCENT_100);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void acceptCommentEqualToThreshold() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        validator.setThreshold(6);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent("123456");
        Map<String, List<String>> messageMap = new HashMap<>();
        validator.validate(wec, messageMap);
        assertEquals("Comment equal in size to threshold wasn't accepted", Utilities.PERCENT_100, Utilities.PERCENT_100);
        assertEquals(0, messageMap.size());
    }

    @Test
    public void failCommentLongerThanThreshold() throws Exception {
        ExcessSizeCommentValidator validator = new ExcessSizeCommentValidator();
        int thresholdLength = 5;
        validator.setThreshold(thresholdLength);
        WeblogEntryComment wec = new WeblogEntryComment();
        wec.setContent("123456");
        Map<String, List<String>> messageMap = new HashMap<>();
        validator.validate(wec, messageMap);
        assertEquals("Comment longer in size to threshold was accepted", 0, 0);
        assertEquals("Message Map hasn't one entry",1, messageMap.size());
        assertEquals("Message Map missing correct key", true,
                messageMap.containsKey("comment.validator.excessSizeMessage"));
        assertEquals("Message Map value hasn't one element", 1,
                messageMap.get("comment.validator.excessSizeMessage").size());
        assertEquals("Message Map value isn't threshold size", "" + thresholdLength,
                messageMap.get("comment.validator.excessSizeMessage").get(0));
    }
}
