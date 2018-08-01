/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.tightblog.rendering.comment;

import org.tightblog.pojos.WeblogEntryComment;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates comment only if its number of links don't exceed the configured limit.
 */
public class ExcessLinksCommentValidator implements CommentValidator {
    private Pattern linkPattern = Pattern.compile("<a\\s*href\\s*=");
    private int limit = 3;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    @Override
    public ValidationResult validate(WeblogEntryComment comment, Map<String, List<String>> messages) {
        if (comment.getContent() != null) {
            Matcher m = linkPattern.matcher(comment.getContent());
            int count = 0;
            while (m.find()) {
                if (++count > limit) {
                    messages.put("comment.validator.excessLinksMessage",
                            Collections.singletonList(Integer.toString(limit)));
                    return ValidationResult.SPAM;
                }
            }
        }
        return ValidationResult.NOT_SPAM;
    }

}
