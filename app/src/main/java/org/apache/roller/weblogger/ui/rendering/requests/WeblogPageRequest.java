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
package org.apache.roller.weblogger.ui.rendering.requests;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.util.Utilities;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a request for a Roller weblog page.
 * 
 * any url from PageProcessor.PATH value
 * 
 * We use this class as a helper to parse an incoming url and sort out the
 * information embedded in the url for later use.
 */
public class WeblogPageRequest extends WeblogRequest {

    private static Log log = LogFactory.getLog(WeblogPageRequest.class);

    // lightweight attributes
    private String context = null;
    protected String weblogAnchor = null;
    protected String weblogTemplateName = null;
    private String weblogCategoryName = null;
    private String weblogDate = null;
    private List<String> tags = null;
    private int pageNum = 0;
    private Map<String, String[]> customParams = new HashMap<>();

    // heavyweight attributes
    protected Template template = null;

    // Page hits
    private boolean websitePageHit = false;
    private boolean otherPageHit = false;

    public WeblogPageRequest() {
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
        log.debug("parsing path " + pathInfo);

        /*
         * parse path info
         * 
         * we expect one of the following forms of url ...
         * 
         * /entry/<anchor> - permalink /date/<YYYYMMDD> - date collection view
         * /category/<category> - category collection view /tags/<tag>+<tag> -
         * tags /page/<pagelink> - custom page
         * 
         * path info may be null, which indicates the weblog homepage
         */
        if (pathInfo != null && pathInfo.trim().length() > 0) {

            // all views use 2 path elements
            String[] pathElements = pathInfo.split("/", 2);

            // the first part of the path always represents the context
            this.context = pathElements[0];

            // now check the rest of the path and extract other details
            if (pathElements.length == 2) {

                if ("entry".equals(this.context)) {
                    this.weblogAnchor = Utilities.decode(pathElements[1]);

                    // Other page
                    otherPageHit = true;

                } else if ("date".equals(this.context)) {
                    if (this.isValidDateString(pathElements[1])) {
                        this.weblogDate = pathElements[1];
                    } else {
                        throw new IllegalArgumentException("Invalid date, " + request.getRequestURL());
                    }

                    // Other page
                    otherPageHit = true;

                } else if ("category".equals(this.context)) {
                    this.weblogCategoryName = Utilities
                            .decode(pathElements[1]);

                    // Other page
                    otherPageHit = true;

                } else if ("page".equals(this.context)) {
                    this.weblogTemplateName = pathElements[1];
                    String tagsString = request.getParameter("tags");
                    if (tagsString != null) {
                        this.tags = Utilities.splitStringAsTags(Utilities.decode(tagsString));
                    }

                    // Other page, we do not want css etc stuff so filter out
                    if (!pathElements[1].contains(".")) {
                        otherPageHit = true;
                    }

                } else if ("tags".equals(this.context)) {
                    String tagsString = pathElements[1].replace('+', ' ');
                    this.tags = Utilities.splitStringAsTags(Utilities
                            .decode(tagsString));
                    int maxSize = WebloggerStaticConfig.getIntProperty(
                            "tags.queries.maxIntersectionSize", 3);
                    if (this.tags.size() > maxSize) {
                        throw new IllegalArgumentException(
                                "Max number of tags allowed is " + maxSize + ", " + request.getRequestURL());
                    }

                    // Other page
                    otherPageHit = true;

                } else {
                    throw new IllegalArgumentException("Context " + this.context + " not supported, " + request.getRequestURL());
                }

            } else {
                // empty data is only allowed for the tags section
                if (!"tags".equals(this.context)) {
                    throw new IllegalArgumentException("invalid index page, " + request.getRequestURL());
                }
            }
        } else {
            // default page
            websitePageHit = true;
        }

        /*
         * parse request parameters
         * 
         * the only params we currently allow are: date - specifies a weblog
         * date string cat - specifies a weblog category anchor - specifies a
         * weblog entry (old way) entry - specifies a weblog entry
         * 
         * we only allow request params if the path info is null or on user
         * defined pages (for backwards compatability). this way we prevent
         * mixing of path based and query param style urls.
         */
        if (pathInfo == null || this.weblogTemplateName != null) {

            // check for entry/anchor params which indicate permalink
            if (request.getParameter("entry") != null) {
                String anchor = request.getParameter("entry");
                if (StringUtils.isNotEmpty(anchor)) {
                    this.weblogAnchor = anchor;
                }
            } else if (request.getParameter("anchor") != null) {
                String anchor = request.getParameter("anchor");
                if (StringUtils.isNotEmpty(anchor)) {
                    this.weblogAnchor = anchor;
                }
            }

            // only check for other params if we didn't find an anchor above or tags
            if (this.weblogAnchor == null && this.tags == null) {
                if (request.getParameter("date") != null) {
                    String date = request.getParameter("date");
                    if (this.isValidDateString(date)) {
                        this.weblogDate = date;
                    } else {
                        throw new IllegalArgumentException("Invalid date, " + request.getRequestURL());
                    }
                }

                if (request.getParameter("cat") != null) {
                    this.weblogCategoryName = Utilities.decode(request
                            .getParameter("cat"));
                }
            }
        }

        // page request param is supported in all views
        if (request.getParameter("page") != null) {
            String pageInt = request.getParameter("page");
            try {
                this.pageNum = Integer.parseInt(pageInt);
            } catch (NumberFormatException e) {
                // ignored, bad input
            }
        }

        // build customParams Map, we remove built-in params because we only
        // want this map to represent params defined by the template author
        customParams = new HashMap<>(request.getParameterMap());
        customParams.remove("entry");
        customParams.remove("anchor");
        customParams.remove("date");
        customParams.remove("cat");
        customParams.remove("page");
        customParams.remove("tags");

        if (log.isDebugEnabled()) {
            log.debug("context = " + this.context);
            log.debug("weblogAnchor = " + this.weblogAnchor);
            log.debug("weblogDate = " + this.weblogDate);
            log.debug("weblogCategory = " + this.weblogCategoryName);
            log.debug("tags = " + this.tags);
            log.debug("template = " + this.weblogTemplateName);
            log.debug("pageNum = " + this.pageNum);
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

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

    public String getWeblogTemplateName() {
        return weblogTemplateName;
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

    public Map<String, String[]> getCustomParams() {
        return customParams;
    }

    public List<String> getTags() {
        return tags;
    }

    public Template getWeblogTemplate() {

        if (template == null && weblogTemplateName != null) {
            try {
                template = WebloggerFactory.getWeblogger().getThemeManager().
                        getWeblogTheme(getWeblog()).getTemplateByPath(weblogTemplateName);
            } catch (WebloggerException ex) {
                log.error("Error getting weblog page " + weblogTemplateName, ex);
            }
        }

        return template;
    }

    public boolean isWebsitePageHit() {
        return websitePageHit;
    }

    public boolean isOtherPageHit() {
        return otherPageHit;
    }

}
