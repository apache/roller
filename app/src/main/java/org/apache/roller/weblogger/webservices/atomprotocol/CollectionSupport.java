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

package org.apache.roller.weblogger.webservices.atomprotocol;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogPermission;

/**
 * Helpers shared by the template, category and comment collections.
 */
final class CollectionSupport {

    private CollectionSupport() {
    }

    /** Splits the path info; element 0 is the weblog handle, element 1 the collection. */
    static String[] path(AtomRequest areq) {
        String[] pathInfo = StringUtils.split(areq.getPathInfo(), "/");
        return pathInfo != null ? pathInfo : new String[0];
    }

    /**
     * Returns the weblog named by the first path element when the user holds
     * {@code action} permission on it.
     */
    static Weblog requireWeblog(Weblogger roller, User user, String[] pathInfo, String action)
            throws AtomException {
        if (pathInfo.length == 0) {
            throw new AtomNotFoundException("Cannot find weblog");
        }
        Weblog weblog;
        try {
            weblog = roller.getWeblogManager().getWeblogByHandle(pathInfo[0]);
        } catch (WebloggerException e) {
            throw new AtomException("Looking up weblog", e);
        }
        if (weblog == null) {
            throw new AtomNotFoundException("Cannot find weblog: " + pathInfo[0]);
        }
        if (!hasPermission(user, weblog, action)) {
            throw new AtomNotAuthorizedException("Not authorized to access weblog: " + pathInfo[0]);
        }
        return weblog;
    }

    static boolean hasPermission(User user, Weblog weblog, String action) {
        try {
            return weblog.hasUserPermission(user, action);
        } catch (Exception e) {
            return false;
        }
    }

    /** True when the user may manage the weblog's templates and settings. */
    static boolean isAdmin(User user, Weblog weblog) {
        return hasPermission(user, weblog, WeblogPermission.ADMIN);
    }

    /** The paging offset in the third path element, or 0. */
    static int offset(String[] pathInfo) {
        if (pathInfo.length > 2) {
            try {
                return Math.max(0, Integer.parseInt(pathInfo[2]));
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }

    /** A client error (HTTP 400). */
    static AtomException badRequest(String message) {
        return new AtomException(message, HttpServletResponse.SC_BAD_REQUEST, null);
    }

    /** A conflict with the current state of the resource (HTTP 409). */
    static AtomException conflict(String message) {
        return new AtomException(message, HttpServletResponse.SC_CONFLICT, null);
    }

    static AtomLink link(String rel, String href) {
        AtomLink link = new AtomLink();
        link.setRel(rel);
        link.setHref(href);
        return link;
    }

    static AtomContent text(String type, String value) {
        AtomContent content = new AtomContent();
        content.setType(type);
        content.setValue(value);
        return content;
    }

    /** The text value of an Atom text construct, or null. */
    static String value(AtomContent content) {
        return content != null ? content.getValue() : null;
    }

    static Boolean parseBoolean(String value) throws AtomException {
        if (value == null) {
            return null;
        }
        String v = value.trim();
        if ("true".equalsIgnoreCase(v)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(v)) {
            return Boolean.FALSE;
        }
        throw badRequest("Expected true or false: " + value);
    }
}
