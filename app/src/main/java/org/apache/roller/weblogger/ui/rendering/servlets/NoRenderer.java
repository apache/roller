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

package org.apache.roller.weblogger.ui.rendering.servlets;

import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * The answer to a page or preview request for a template that no renderer can
 * render: log it once and send a 404.
 */
final class NoRenderer {

    private NoRenderer() {
    }

    static void sendNotFound(HttpServletResponse response, Template page, Weblog weblog, Log log)
            throws IOException {
        if (hasNoContent(page, log)) {
            // nothing else logs this case
            log.warn("No renderer for template '" + page.getName() + "' (" + page.getId()
                    + ") of weblog '" + weblog.getHandle() + "': it has no content.");
        } else {
            // the renderer factory already logged why it could not create a renderer
            log.debug("No renderer for template " + page.getId());
        }
        if (!response.isCommitted()) {
            response.reset();
        }
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private static boolean hasNoContent(Template page, Log log) {
        try {
            return page.getTemplateRendition(RenditionType.STANDARD) == null;
        } catch (WebloggerException e) {
            log.warn("Could not read the content of template " + page.getId(), e);
            return false;
        }
    }
}
