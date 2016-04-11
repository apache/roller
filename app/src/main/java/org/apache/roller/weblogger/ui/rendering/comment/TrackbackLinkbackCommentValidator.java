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

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.LinkbackExtractor;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * Validates comment if comment's URL links back to the comment's entry,
 * intended for use with trackbacks only.
 */
public class TrackbackLinkbackCommentValidator implements CommentValidator {
    
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");

    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    public String getName() {
        return bundle.getString("comment.validator.trackbackLinkbackName");
    }
    
    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        
        // linkback validation can be toggled at runtime, so check if it's enabled
        // if it's disabled then just return a score of 100
        if(!WebloggerFactory.getWeblogger().getPropertiesManager().getBooleanProperty(
                "site.trackbackVerification.enabled")) {
            return WebloggerCommon.PERCENT_100;
        }
        
        int ret = 0;
        LinkbackExtractor linkback = null;
        try {
            linkback = new LinkbackExtractor(comment.getUrl(), urlStrategy.getWeblogEntryURL(
                    comment.getWeblogEntry().getWeblog(), comment.getWeblogEntry().getAnchor(), true));
        } catch (IOException ignored) {
        }
        
        if (linkback != null && linkback.getExcerpt() != null) {
            ret = WebloggerCommon.PERCENT_100;
        } else {
            messages.addError("comment.validator.trackbackLinkbackMessage");
        }
        return ret;
    }
    
}
