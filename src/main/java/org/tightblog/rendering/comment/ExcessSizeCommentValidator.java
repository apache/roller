/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.rendering.comment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tightblog.domain.WeblogEntryComment;
import org.tightblog.domain.WeblogEntryComment.ValidationResult;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validates comment if it neither exceeds a given size in characters nor a specified
 * number of links.  Size can be adjusted by the excessSize.validator.sizeLimit and
 * links by the excessSize.validator.linksLimit property (defaults to 1000 and 3
 * respectively.)
 */
@Component
public class ExcessSizeCommentValidator implements CommentValidator {

    private static Pattern LINK_PATTERN = Pattern.compile("<a\\s*href\\s*=");

    private int sizeLimit;
    private int linksLimit;

    @Autowired
    ExcessSizeCommentValidator(
            @Value("${excessSize.validator.sizeLimit:1000}") int sizeLimit,
            @Value("${excessSize.validator.linksLimit:3}") int linksLimit) {
        this.sizeLimit = sizeLimit;
        this.linksLimit = linksLimit;
    }

    @Override
    public ValidationResult validate(WeblogEntryComment comment, Map<String, List<String>> messages) {

        if (comment.getContent() != null) {
            // check size
            if (sizeLimit >= 0 && comment.getContent().length() > sizeLimit) {
                messages.put("comment.validator.excessSizeMessage",
                        Collections.singletonList(Integer.toString(sizeLimit)));
                return ValidationResult.SPAM;
            }

            // check # of links
            if (linksLimit >= 0) {
                Matcher m = LINK_PATTERN.matcher(comment.getContent());
                int count = 0;
                while (m.find()) {
                    if (++count > linksLimit) {
                        messages.put("comment.validator.excessLinksMessage",
                                Collections.singletonList(Integer.toString(linksLimit)));
                        return ValidationResult.SPAM;
                    }
                }
            }
        }
        return ValidationResult.NOT_SPAM;
    }
}
