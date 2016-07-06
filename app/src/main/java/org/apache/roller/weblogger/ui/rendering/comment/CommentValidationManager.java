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

import java.util.ArrayList;
import java.util.List;

import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for loading validators and using them to validate comments.
 */
public class CommentValidationManager {
    private static Logger log = LoggerFactory.getLogger(CommentValidationManager.class);
    private List<CommentValidator> validators = new ArrayList<>();

    public CommentValidationManager(List<CommentValidator> validators) {
        this.validators = validators;
        log.info("Provided {} CommentValidators", validators.size());
    }

    /**
     * @param comment Comment to be validated
     * @param messages Messages object to which errors will be added
     * @return Number indicating confidence that comment is valid (100 meaning 100%)
     */
    public int validateComment(WeblogEntryComment comment, RollerMessages messages) {
        int total = 0;
        int singleResponse;
        if (validators.size() > 0) {
            for (CommentValidator val : validators) {
                log.debug("Invoking comment validator {}", val.getName());
                singleResponse = val.validate(comment, messages);
                if (singleResponse == -1) { // blatant spam
                    return -1;
                }
                total += singleResponse;
            }
            total = total / validators.size();
        } else {
            // When no validators: consider all comments valid
            total = Utilities.PERCENT_100;
        }
        return total;
    }
    
}
