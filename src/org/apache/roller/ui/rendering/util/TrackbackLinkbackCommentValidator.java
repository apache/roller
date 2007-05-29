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

package org.apache.roller.ui.rendering.util;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ResourceBundle;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.WeblogEntryComment;
import org.apache.roller.util.LinkbackExtractor;
import org.apache.roller.util.RollerMessages;
import org.apache.roller.util.URLUtilities;


/**
 * Validates comment if comment's URL links back to the comment's entry,
 * intended for use with trackbacks only.
 */
public class TrackbackLinkbackCommentValidator implements CommentValidator {
    
    private ResourceBundle bundle = ResourceBundle.getBundle("ApplicationResources");
    
    public String getName() {
        return bundle.getString("comment.validator.trackbackLinkbackName");
    }
    
    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        
        // linkback validation can be toggled at runtime, so check if it's enabled
        // if it's disabled then just return a score of 100
        if(!RollerRuntimeConfig.getBooleanProperty("site.trackbackVerification.enabled")) {
            return 100;
        }
        
        int ret = 0;
        LinkbackExtractor linkback = null;
        try {
            linkback = new LinkbackExtractor(
                    comment.getUrl(),
                    URLUtilities.getWeblogEntryURL(
                    comment.getWeblogEntry().getWebsite(),
                    null,
                    comment.getWeblogEntry().getAnchor(),
                    true));
        } catch (MalformedURLException ignored1) {
        } catch (IOException ignored2) {}
        
        if (linkback != null && linkback.getExcerpt() != null) {
            ret = 100;
        } else {
            messages.addError("comment.validator.trackbackLinkbackMessage");
        }
        return ret;
    }
    
}
