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

package org.apache.roller.weblogger.ui.rendering.util;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Validates comment only if it has fewer links than comment.validator.excessSize.threshold
 */
public class ExcessLinksCommentValidator implements CommentValidator {
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");  
    private Pattern linkPattern = Pattern.compile("<a\\s*href\\s*=");    
    private int threshold;
        
    public ExcessLinksCommentValidator() {
        threshold = RollerConfig.getIntProperty("comment.validator.excessLinks.threshold");
    }
        
    public String getName() {
        return bundle.getString("comment.validator.excessLinksName");
    }

    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        Matcher m = linkPattern.matcher(comment.getContent());
        int count = 0;
        while (m.find()) {
            if (count++ > threshold) {
                messages.addError("comment.validator.excessLinksMessage", Integer.toString(threshold));
                return 0;
            }
        }
        return 100;
    }
    
}
