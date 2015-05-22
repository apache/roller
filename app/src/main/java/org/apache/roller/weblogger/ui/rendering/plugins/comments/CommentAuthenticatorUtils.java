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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

class CommentAuthenticatorUtils {
    private static Log log = LogFactory.getLog(CommentAuthenticatorUtils.class);

    public static Locale getLocale(HttpServletRequest request) {
        String handle = request.getParameter("weblog");
        try {
            Weblog weblog = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(handle);
            return weblog.getLocaleInstance();
        } catch (WebloggerException e) {
            log.debug("Failed to determine weblog's locale. fallback to the locale of the request", e);
            return request.getLocale();
        }
    }
}
