/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.roller.weblogger.ui.rendering.plugins.comments;

import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.RollerMessages;

/**
 * No-op retained temporarily for installations that still name this plugin.
 *
 * @deprecated The associated protocol endpoint is no longer available.
 */
@Deprecated(since = "6.1.6", forRemoval = true)
public class TrackbackLinkbackCommentValidator implements CommentValidator {

    @Override
    public String getName() {
        return "Compatibility comment validator";
    }

    @Override
    public int validate(WeblogEntryComment comment, RollerMessages messages) {
        return RollerConstants.PERCENT_100;
    }
}
