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
import org.tightblog.util.Utilities;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Validates comment only if it does not exceed number of characters specified by the threshold property.
 */
public class ExcessSizeCommentValidator implements CommentValidator {
    private int threshold = 1000;

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public String getName() {
        return "Excess Size Comment Validator";
    }

    public int validate(WeblogEntryComment comment, Map<String, List<String>> messages) {
        if (comment.getContent() != null && comment.getContent().length() > threshold) {
            messages.put("comment.validator.excessSizeMessage",
                    Collections.singletonList(Integer.toString(threshold)));
            return 0;
        }
        return Utilities.PERCENT_100;
    }

}
