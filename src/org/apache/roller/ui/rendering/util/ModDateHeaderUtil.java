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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Utility class to localize the modification date header-related logic.
 */
public class ModDateHeaderUtil {
    private static final Log log = LogFactory.getLog(ModDateHeaderUtil.class);

    // Utility class with static methods; inhibit construction
    private ModDateHeaderUtil() {

    }

    /**
     * Sets the HTTP response status to 304 (NOT MODIFIED) if the request contains an
     * If-Modified-Since header that specifies a time that is
     * at or after the time specified by the value of lastModifiedTimeMillis
     * <em>truncated to second granularity</em>.  Returns true if
     * the response status was set, false if not.
     *
     * @param request
     * @param response
     * @param lastModifiedTimeMillis
     * @return true if a response status was sent, false otherwise.
     */
    public static boolean respondIfNotModified(HttpServletRequest request,
                                               HttpServletResponse response,
                                               long lastModifiedTimeMillis) {
        long sinceDate = request.getDateHeader("If-Modified-Since");
        // truncate to seconds
        lastModifiedTimeMillis -= (lastModifiedTimeMillis % 1000);
        log.debug("since date = " + sinceDate);
        log.debug("last mod date (trucated to seconds) = " + lastModifiedTimeMillis);
        if (lastModifiedTimeMillis <= sinceDate) {
            log.debug("NOT MODIFIED " + request.getRequestURL());
            response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Set the Last-Modified header using the given time in milliseconds.  Note that because the
     * header has the granularity of one second, the value will get truncated to the nearest second that does not
     * exceed the provided value.
     * <p/>
     * This will also set the Expires header to a date in the past.  This forces clients to revalidate the cache each
     * time.
     *
     * @param response
     * @param lastModifiedTimeMillis
     */
    public static void setLastModifiedHeader(HttpServletResponse response, long lastModifiedTimeMillis) {
        response.setDateHeader("Last-Modified", lastModifiedTimeMillis);
        // Force clients to revalidate each time
        response.setDateHeader("Expires", 0);
    }
}
