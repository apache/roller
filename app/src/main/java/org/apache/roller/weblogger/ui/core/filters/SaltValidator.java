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
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.core.filters;

import java.util.Locale;
import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.apache.roller.weblogger.ui.core.RollerSession;
import org.apache.roller.weblogger.ui.rendering.util.cache.SaltCache;

/**
 * Shared validation for salts submitted by UI forms.
 */
public final class SaltValidator {

    private static final String MULTIPART_FORM_DATA = "multipart/form-data";

    private SaltValidator() {
    }

    /**
     * Validates and consumes the salt submitted as a request parameter.
     *
     * @param request current request
     * @return true when no Roller session is present or the submitted salt is valid
     */
    public static boolean consumeSubmittedSalt(HttpServletRequest request) {
        RollerSession rollerSession = RollerSession.getRollerSession(request);
        if (rollerSession == null) {
            return true;
        }

        String userId = rollerSession.getAuthenticatedUser() != null
                ? rollerSession.getAuthenticatedUser().getId() : "";
        String salt = request.getParameter("salt");
        if (salt == null) {
            return false;
        }

        SaltCache saltCache = SaltCache.getInstance();
        synchronized (saltCache) {
            if (!Objects.equals(saltCache.get(salt), userId)) {
                return false;
            }
            saltCache.remove(salt);
        }
        return true;
    }

    /**
     * Returns true for a multipart form POST, which Struts parses after the
     * servlet filters have run.
     *
     * @param request current request
     * @return true for multipart/form-data POST requests
     */
    public static boolean isMultipartFormPost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }

        String contentType = request.getContentType();
        if (contentType == null) {
            return false;
        }

        int parameterStart = contentType.indexOf(';');
        String mediaType = parameterStart >= 0
                ? contentType.substring(0, parameterStart) : contentType;
        return MULTIPART_FORM_DATA.equals(mediaType.trim().toLowerCase(Locale.ENGLISH));
    }
}
