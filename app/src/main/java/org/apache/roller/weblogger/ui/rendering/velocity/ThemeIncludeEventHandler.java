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

package org.apache.roller.weblogger.ui.rendering.velocity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.context.Context;

/**
 * Keeps <code>#include</code> and <code>#parse</code> inside the template
 * namespace they are rendered from.
 *
 * <p>Weblog templates are authored by weblog administrators, whom Roller treats
 * as untrusted: the rendering engine runs them under
 * <code>SecureUberspector</code> so they cannot reach arbitrary objects. That
 * sandbox governs method calls, not resource resolution, so the include
 * directives are confined here instead.
 *
 * <p>Legitimate includes name a resource within the current theme, or a stored
 * template resolved by id through the weblog's own template collection. Neither
 * needs to leave the namespace, so a name that is absolute, walks upward, or
 * carries a scheme is refused.
 *
 * <p>Returning null tells Velocity not to resolve the resource at all.
 */
public class ThemeIncludeEventHandler implements IncludeEventHandler {

    private static final Log LOG = LogFactory.getLog(ThemeIncludeEventHandler.class);

    @Override
    public String includeEvent(Context context, String includeResourcePath,
                               String currentResourcePath, String directiveName) {

        if (includeResourcePath == null || includeResourcePath.trim().isEmpty()) {
            return null;
        }

        String path = includeResourcePath.trim();

        if (isOutsideNamespace(path)) {
            // Logged rather than raised: a template that asks for something it
            // may not have renders without that fragment, which is how Velocity
            // already treats a resource it cannot find.
            LOG.debug("Refusing #" + directiveName + " of '" + path
                    + "' from '" + currentResourcePath + "': outside the template namespace");
            return null;
        }

        // Keep the caller's spelling (including any intentional surrounding
        // whitespace) once validation has accepted the identifier.
        return includeResourcePath;
    }

    /**
     * @return true when the name reaches outside the namespace it was written
     *         in — an absolute path, an upward traversal, or a scheme such as
     *         file: or http:
     */
    private boolean isOutsideNamespace(String path) {
        String normalized = path.replace('\\', '/');

        if (normalized.startsWith("/")) {
            return true;
        }
        if (normalized.contains("../") || normalized.endsWith("..")) {
            return true;
        }
        // URI schemes and Windows drive prefixes are not theme identifiers.
        int colon = normalized.indexOf(':');
        if (colon > -1) {
            if (normalized.indexOf(':', colon + 1) > -1) {
                return true;
            }
            if (colon == 1 && Character.isLetter(normalized.charAt(0))) {
                return true;
            }
            if (colon + 1 == normalized.length()
                    || normalized.charAt(colon + 1) == '/') {
                return true;
            }
            String namespace = normalized.substring(0, colon);
            return namespace.indexOf('.') > -1 || namespace.indexOf('/') > -1;
        }
        return false;
    }
}
