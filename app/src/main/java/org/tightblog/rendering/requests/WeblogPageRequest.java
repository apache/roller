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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.rendering.requests;

import org.apache.commons.lang3.StringUtils;
import org.tightblog.pojos.Template;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Represents a request for a TightBlog weblog page
 * <p>
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogPageRequest extends WeblogRequest {

    private static Logger log = LoggerFactory.getLogger(WeblogPageRequest.class);

    // lightweight attributes
    private String context;
    private String weblogEntryAnchor;
    private String customPageName;
    private String category;
    private String weblogDate;
    private String tag;
    private String query;

    // whether a robots meta tag with value "noindex" should be added to discourage search engines from indexing page
    private boolean noIndex;

    // attributes populated by processors where appropriate
    private Template template;
    private WeblogEntry weblogEntry;

    public WeblogPageRequest() {
    }

    public static class Creator {
        public WeblogPageRequest create(HttpServletRequest servletRequest) {
            WeblogPageRequest weblogPageRequest = new WeblogPageRequest();
            WeblogRequest.parseRequest(weblogPageRequest, servletRequest);
            weblogPageRequest.parseExtraPathInfo();
            return weblogPageRequest;
        }
    }

    private void parseExtraPathInfo() {
        /*
         * This subclass handles the following forms of url:
         *
         * /entry/<anchor> - permalink
         * /date/<YYYYMMDD> - date collection view
         * /tag/<tag> - tag
         * /category/<category> - category collection view
         * /category/<category>/tag/<tag> - tag under a category
         * /search?q=xyz[&cat=Sports] - search on xyz (optionally under given category)
         * /page/<pagelink> - custom page
         * path info may be null, which indicates the weblog homepage
         */
        if (extraPathInfo != null && extraPathInfo.trim().length() > 0) {

            String[] pathElements = extraPathInfo.split("/", 4);

            // the first part of the path always represents the context
            this.context = pathElements[0];

            // now check the rest of the path and extract other details
            if ("category".equals(this.context) && (pathElements.length == 2 || pathElements.length == 4)) {
                this.category = Utilities.decode(pathElements[1]);

                if (pathElements.length == 4) {
                    if ("tag".equals(pathElements[2])) {
                        tag = pathElements[3];
                    } else {
                        throw new IllegalArgumentException("Invalid path: " + extraPathInfo);
                    }
                }
            } else if (pathElements.length == 2) {
                if ("entry".equals(this.context)) {
                    this.weblogEntryAnchor = Utilities.decode(pathElements[1]);
                } else if ("date".equals(this.context)) {
                    if (this.isValidDateString(pathElements[1])) {
                        this.weblogDate = pathElements[1];
                        // discourage date-based URLs from appearing in search engine results
                        // (encourages appearance of blog home URL or permalinks instead)
                        noIndex = true;
                    } else {
                        throw new IllegalArgumentException("Invalid date found for request: "
                                + extraPathInfo);
                    }
                } else if ("page".equals(this.context)) {
                    this.customPageName = pathElements[1];

                    // Custom pages may have a date parameter, e.g., the month to display on a blog archive page
                    String date = getRequestParameter("date");
                    if (this.isValidDateString(date)) {
                        this.weblogDate = date;
                    }
                } else if ("tag".equals(this.context)) {
                    tag = pathElements[1];
                } else {
                    throw new IllegalArgumentException("Context \"" + this.context + "\" not supported");
                }
            } else if ("search".equals(this.context) && pathElements.length == 1) {
                this.query = getRequestParameter("q");
                this.category = Utilities.decode(getRequestParameter("cat"));
            } else {
                throw new IllegalArgumentException("Invalid page request: " + extraPathInfo);
            }
        }

        if (getPageNum() > 0) {
            // only index first pages (i.e., those without this parameter)
            noIndex = true;
        }

        if (log.isDebugEnabled()) {
            log.debug("context = {}", context);
            log.debug("weblogEntryAnchor = {}", weblogEntryAnchor);
            log.debug("weblogDate = {}", weblogDate);
            log.debug("weblogCategory = {}", category);
            log.debug("tag = {}", tag);
            log.debug("template = {}", customPageName);
            log.debug("search query = {}", query);
        }
    }

    private boolean isValidDateString(String dateString) {
        // string must be all numeric and 6 or 8 characters
        return dateString != null && StringUtils.isNumeric(dateString) && (dateString
                .length() == 6 || dateString.length() == 8);
    }

    public String getContext() {
        return context;
    }

    public String getWeblogEntryAnchor() {
        return weblogEntryAnchor;
    }

    public void setWeblogEntryAnchor(String weblogEntryAnchor) {
        this.weblogEntryAnchor = weblogEntryAnchor;
    }

    public String getCustomPageName() {
        return customPageName;
    }

    public void setCustomPageName(String customPageName) {
        this.customPageName = customPageName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String weblogCategory) {
        this.category = weblogCategory;
    }

    public String getWeblogDate() {
        return weblogDate;
    }

    public void setWeblogDate(String weblogDate) {
        this.weblogDate = weblogDate;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Template getTemplate() {
        return template;
    }

    public void setTemplate(Template template) {
        this.template = template;
    }

    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry weblogEntry) {
        this.weblogEntry = weblogEntry;
    }

    public boolean isNoIndex() {
        return noIndex;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

}
