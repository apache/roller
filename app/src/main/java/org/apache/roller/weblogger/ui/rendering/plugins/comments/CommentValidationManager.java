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

package org.apache.roller.weblogger.ui.rendering.plugins.comments;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.Reflection;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Responsible for loading validators and using them to validate comments.
 */
public class CommentValidationManager {

    private static final Log log = LogFactory.getLog(CommentValidationManager.class);
    private final List<CommentValidator> validators = new ArrayList<>();

    public CommentValidationManager() {
        
        // instantiate the validators that are configured
        try {
            validators.addAll(Reflection.newInstancesFromProperty("comment.validator.classnames"));
        } catch (ReflectiveOperationException ex) {
            log.error("Error instantiating comment validators", ex);
        }
        
        log.info("Configured " + validators.size() + " CommentValidators");
        log.info(validators.stream().map(t -> t.getClass().toString()).collect(Collectors.joining(",", "[", "]")));
    }
    
    /**
     * Add validator to those managed by this manager (testing purposes).
     */
    public void addCommentValidator(CommentValidator val) {
        validators.add(val);
    }
    
    /**
     * @param comment Comment to be validated
     * @param messages Messages object to which errors will be added
     * @return Number indicating confidence that comment is valid (100 meaning 100%)
     */
    public int validateComment(WeblogEntryComment comment, RollerMessages messages) {
        int total = 0;
        if (!validators.isEmpty()) {
            for (CommentValidator val : validators) {
                log.debug("Invoking comment validator "+val.getName());
                total += val.validate(comment, messages);
            }
            total = total / validators.size();
        } else {
            // When no validators: consider all comments valid
            total = RollerConstants.PERCENT_100;
        }
        return total;
    }
    
}
