/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.rendering.requests;

import javax.servlet.http.HttpServletRequest;

import org.tightblog.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a request for a TightBlog weblog feed.
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogFeedRequest extends WeblogRequest {

    private static Logger log = LoggerFactory.getLogger(WeblogFeedRequest.class);

    // type is "entries" or "comments"
    private String type = null;
    private String categoryName = null;
    private String tag = null;
    private boolean siteWideFeed = false;
    private int page = 0;

    public WeblogFeedRequest() {
    }

    static public class Creator {
        public WeblogFeedRequest create(HttpServletRequest request) {
            return new WeblogFeedRequest(request);
        }
    }

    /**
     * Construct the WeblogFeedRequest by parsing the incoming url
     */
    public WeblogFeedRequest(HttpServletRequest request) {

        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);

        // we only want the path info left over from after our parents parsing
        String pathInfo = getPathInfo();

        // parse the request object and figure out what we've got
        log.debug("parsing path {}", pathInfo);

        /*
         * parse the path info.  Format:
         * /<type>
         */
        if (pathInfo != null && pathInfo.trim().length() > 1) {
            type = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        }

        if (!"comments".equals(type)) {
            type = "entries";
        }

        // parse request parameters
        if (request.getParameter("cat") != null) {
            // replacing any plus signs with their encoded equivalent (http://stackoverflow.com/a/6926987)
            categoryName = Utilities.decode(request.getParameter("cat").replace("+", "%2B"));
        } else {
            tag = request.getParameter("tag");
        }

        if (request.getParameter("page") != null) {
            try {
                page = Integer.parseInt(request.getParameter("page"));
            } catch (NumberFormatException ignored) {
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("type = " + type);
            log.debug("page = " + type);
            log.debug("category = " + categoryName);
            log.debug("tag = " + tag);
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public boolean isSiteWideFeed() {
        return siteWideFeed;
    }

    public void setSiteWideFeed(boolean siteWideFeed) {
        this.siteWideFeed = siteWideFeed;
    }

}
