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

import java.util.ResourceBundle;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Validates comment only if it has less than comment.validator.excessSize.threshold characters
 */
public class ExcessSizeCommentValidator implements CommentValidator {
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");  
    private int threshold;
    
    public ExcessSizeCommentValidator() {
        threshold = RollerConfig.getIntProperty("comment.validator.excessSize.threshold");
    }
    
    public String getName() {
        return bundle.getString("comment.validator.excessSizeName");
    }

    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        if (comment.getContent() != null && comment.getContent().length() > threshold) {
            messages.addError("comment.validator.excessSizeMessage", Integer.toString(threshold));
            return 0;
        }
        return 100;
    }
    
}
