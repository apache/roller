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

import javax.servlet.http.HttpServletRequest;

import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.util.Utilities;
import org.springframework.mobile.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a request to a weblog.
 * <p>
 * This is a fairly generic parsed request which is only trying to figure out
 * the elements of a weblog request which apply to all requests.  We try to
 * determine the weblogHandle, and then what extra path info remains.  The basic
 * format is like this ...
 * <p>
 * /<weblogHandle>[/extra/path/info]
 * <p>
 * All weblog urls require a weblogHandle, so we ensure that part of the url is
 * properly specified, and path info is always optional.
 * <p>
 * NOTE: this class purposely exposes a getExtraPathInfo() method which provides the
 * path info specified by the request that has not been parsed by this
 * particular class.  this makes it relatively easy for subclasses to extend
 * this class and simply pick up where it left off in the parsing process.
 * <p>
 * NOTE: It is extremely important to mention that this class and all of its
 * subclasses are meant to be extremely light weight.  Meaning they should
 * avoid time consuming operations whenever possible, especially operations
 * which require a trip to the db.
 */
public class WeblogRequest {

    private static Logger log = LoggerFactory.getLogger(WeblogRequest.class);

    // lightweight attributes
    private String weblogHandle;
    private String queryString;
    private String authenticatedUser;
    private DeviceType deviceType = DeviceType.NORMAL;
    private HttpServletRequest request;
    private int pageNum;
    String extraPathInfo;

    // attributes populated by processors where appropriate
    private Weblog weblog;
    private User blogger;
    private boolean siteWide;

    public WeblogRequest() {
    }

    public static class Creator {
        public WeblogRequest create(HttpServletRequest servletRequest) {
            WeblogRequest weblogRequest = new WeblogRequest();
            WeblogRequest.parseRequest(weblogRequest, servletRequest);
            return weblogRequest;
        }
    }

    static void parseRequest(WeblogRequest wreq, HttpServletRequest sreq) {
        wreq.request = sreq;
        wreq.queryString = sreq.getQueryString();
        wreq.deviceType = Utilities.getDeviceType(sreq);

        // login status
        java.security.Principal principal = sreq.getUserPrincipal();
        if (principal != null) {
            wreq.authenticatedUser = principal.getName();
        }

        // servlet path: /tb-ui/rendering/(page|feed|mediafile|comment|search)/aaa[/bbb]
        String path = sreq.getServletPath();

        log.debug("parsing path {}", path);

        // first extract the weblog handle
        if (path != null && path.trim().length() > 1) {

            // strip off the leading slash
            path = path.substring(1);

            String[] pathElements = path.split("/", 5);

            wreq.weblogHandle = pathElements[3];

            // if there is more left of the path info then hold onto it
            if (pathElements.length == 5) {
                wreq.extraPathInfo = pathElements[4];
            }

            // Page number for a specific window of results (e.g., Atom feed or entries under a category)
            if (sreq.getParameter("page") != null) {
                try {
                    wreq.pageNum = Integer.parseInt(sreq.getParameter("page"));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        log.debug("handle = {}, extraPathInfo = {}, pageNum = {}", wreq.weblogHandle, wreq.extraPathInfo, wreq.pageNum);
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public void setWeblogHandle(String weblogHandle) {
        this.weblogHandle = weblogHandle;
    }

    public String getExtraPathInfo() {
        return extraPathInfo;
    }

    public void setExtraPathInfo(String extraPathInfo) {
        this.extraPathInfo = extraPathInfo;
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public String getAuthenticatedUser() {
        return this.authenticatedUser;
    }

    public void setAuthenticatedUser(String authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }

    public boolean isLoggedIn() {
        return this.authenticatedUser != null;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType type) {
        this.deviceType = type;
    }

    /* Supports custom parameters often needed by custom external pages */
    public String getRequestParameter(String paramName) {
        return request.getParameter(paramName);
    }

    public User getBlogger() {
        return blogger;
    }

    public void setBlogger(User blogger) {
        this.blogger = blogger;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public boolean isSiteWide() {
        return siteWide;
    }

    public void setSiteWide(boolean siteWide) {
        this.siteWide = siteWide;
    }
}
