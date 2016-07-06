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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */

package org.apache.roller.weblogger.ui.rendering.comment;

import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.Blacklist;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Validates comment if comment does not contain blacklisted words.
 */
public class BlacklistCommentValidator implements CommentValidator {
    public String getName() {
        return "Blacklist Comment Validator";
    }

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        if (checkComment(comment)) {
            messages.addError("comment.validator.blacklistMessage");
            return 0;
        }
        return Utilities.PERCENT_100;
    }

    /**
     * Test comment, applying weblog's blacklist
     * @return True if comment matches a blacklist term
     */
    private boolean checkComment(WeblogEntryComment comment) {
        boolean isBlacklisted = false;

        Blacklist bl = weblogManager.getWeblogBlacklist(comment.getWeblogEntry().getWeblog());

        if (bl.isBlacklisted(comment.getUrl())
                || bl.isBlacklisted(comment.getEmail())
                || bl.isBlacklisted(comment.getName())
                || bl.isBlacklisted(comment.getContent())) {
            isBlacklisted = true;
        }
        return isBlacklisted;
    }
}
