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

package org.apache.roller.weblogger.ui.rendering.plugins.comments;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Responsible for loading validators and using them to validate comments.
 */
public class CommentValidationManager {
    private static Log log = LogFactory.getLog(CommentValidationManager.class);
    private List<CommentValidator> validators = new ArrayList<>();

    public CommentValidationManager(List<CommentValidator> validators) {
        this.validators = validators;
        log.info("Provided " + validators.size() + " CommentValidators");
    }

    /**
     * @param comment Comment to be validated
     * @param messages Messages object to which errors will be added
     * @return Number indicating confidence that comment is valid (100 meaning 100%)
     */
    public int validateComment(WeblogEntryComment comment, RollerMessages messages) {
        int total = 0;
        if (validators.size() > 0) {
            for (CommentValidator val : validators) {
                log.debug("Invoking comment validator " + val.getName());
                total += val.validate(comment, messages);
            }
            total = total / validators.size();
        } else {
            // When no validators: consider all comments valid
            total = WebloggerCommon.PERCENT_100;
        }
        return total;
    }
    
}
