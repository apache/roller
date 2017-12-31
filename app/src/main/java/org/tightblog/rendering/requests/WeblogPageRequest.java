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
import org.tightblog.pojos.User;
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
    private String context = null;
    private String weblogEntryAnchor = null;
    private String weblogTemplateName = null;
    private String weblogCategoryName = null;
    private String weblogDate = null;
    private String tag = null;
    private int pageNum = 0;
    // whether a robots meta tag with value "noindex" should be added to discourage search engines from indexing page
    private boolean noIndex = false;

    // heavyweight attributes, populated by processors where appropriate
    private Template template = null;
    private WeblogEntry weblogEntry = null;

    // Page hits
    private boolean weblogPageHit = true;

    public WeblogPageRequest() {
    }

    static public class Creator {
        public WeblogPageRequest create(HttpServletRequest request) {
            return new WeblogPageRequest(request);
        }
    }

    /**
     * Construct the WeblogPageRequest by parsing the incoming url
     */
    public WeblogPageRequest(HttpServletRequest request) {
        // let our parent take care of their business first
        // parent determines weblog handle
        super(request);

        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();

        // parse the request object and figure out what we've got
        log.debug("parsing path {}", pathInfo);

        /*
         * parse path info
         *
         * we expect one of the following forms of url ...
         *
         * /entry/<anchor> - permalink /date/<YYYYMMDD> - date collection view
         * /category/<category> - category collection view
         * /tag/<tag> - tag
         * /category/<category>/tag/<tag> - tag under a category
         * /page/<pagelink> - custom page
         *
         * path info may be null, which indicates the weblog homepage
         */
        if (pathInfo != null && pathInfo.trim().length() > 0) {

            String[] pathElements = pathInfo.split("/", 4);

            // the first part of the path always represents the context
            this.context = pathElements[0];

            // now check the rest of the path and extract other details
            if ("category".equals(this.context) && (pathElements.length == 2 || pathElements.length == 4)) {
                this.weblogCategoryName = Utilities.decode(pathElements[1]);

                if (pathElements.length == 4) {
                    if ("tag".equals(pathElements[2])) {
                        tag = pathElements[3];
                    } else {
                        throw new IllegalArgumentException("Invalid path: " + pathInfo);
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
                        throw new IllegalArgumentException("Invalid date: " + request.getRequestURL());
                    }
                } else if ("page".equals(this.context)) {
                    this.weblogTemplateName = pathElements[1];

                    // Other page, we do not want css etc stuff so filter out
                    if (pathElements[1].contains(".")) {
                        weblogPageHit = false;
                    }
                } else if ("tag".equals(this.context)) {
                    tag = pathElements[1];
                } else {
                    throw new IllegalArgumentException("Context \"" + this.context + "\" not supported, " + request.getRequestURL());
                }
            } else {
                throw new IllegalArgumentException("invalid page request: " + request.getRequestURL());
            }
        }

        // page request param is supported in all views
        if (request.getParameter("page") != null) {
            String pageInt = request.getParameter("page");
            // only index first pages (i.e., those without this parameter)
            noIndex = true;
            try {
                this.pageNum = Integer.parseInt(pageInt);
            } catch (NumberFormatException e) {
                // ignored, bad input
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("context = {}", context);
            log.debug("weblogEntryAnchor = {}", weblogEntryAnchor);
            log.debug("weblogDate = {}", weblogDate);
            log.debug("weblogCategory = {}", weblogCategoryName);
            log.debug("tag = {}", tag);
            log.debug("template = {}", weblogTemplateName);
            log.debug("pageNum = {}", pageNum);
        }
    }

    private boolean isValidDateString(String dateString) {
        // string must be all numeric and 6 or 8 characters
        return (dateString != null && StringUtils.isNumeric(dateString) && (dateString
                .length() == 6 || dateString.length() == 8));
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

    public String getWeblogTemplateName() {
        return weblogTemplateName;
    }

    public void setWeblogTemplateName(String weblogTemplateName) {
        this.weblogTemplateName = weblogTemplateName;
    }

    public String getWeblogCategoryName() {
        return weblogCategoryName;
    }

    public void setWeblogCategoryName(String weblogCategory) {
        this.weblogCategoryName = weblogCategory;
    }

    public String getWeblogDate() {
        return weblogDate;
    }

    public int getPageNum() {
        return pageNum;
    }

    public String getTag() {
        return tag;
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

    public boolean isWeblogPageHit() {
        return weblogPageHit;
    }

    public void setWeblogPageHit(boolean weblogPageHit) {
        this.weblogPageHit = weblogPageHit;
    }

    public boolean isNoIndex() {
        return noIndex;
    }

}
