/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.roller.weblogger.ui.rendering.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;

/**
 * Decides who may see a weblog's page and resource previews.
 */
public final class PreviewAccess {

    private static final Log log = LogFactory.getLog(PreviewAccess.class);

    private PreviewAccess() {
    }

    /**
     * Previews show unpublished content, so only an enabled user with a role
     * in the weblog (or a global admin) may see them.
     */
    public static boolean canPreview(Weblog weblog, User user) {
        boolean allowed = weblog != null && user != null
                && Boolean.TRUE.equals(user.getEnabled())
                && weblog.hasUserPermission(user, WeblogPermission.EDIT_DRAFT);
        if (!allowed && log.isDebugEnabled()) {
            log.debug("Preview refused: user " + (user == null ? null : user.getUserName())
                    + ", weblog " + (weblog == null ? null : weblog.getHandle()));
        }
        return allowed;
    }
}
