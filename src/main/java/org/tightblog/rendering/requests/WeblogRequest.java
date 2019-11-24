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

import org.tightblog.domain.User;
import org.tightblog.domain.Weblog;
import org.springframework.mobile.device.DeviceType;

import java.security.Principal;

/**
 * Represents a request to a weblog.
 *
 * NOTE: It is extremely important to mention that this class and all of its
 * subclasses are meant to be extremely light weight.  Meaning they should
 * avoid time consuming operations whenever possible, especially operations
 * which require a trip to the db.
 */
public class WeblogRequest {

    // lightweight attributes
    private String weblogHandle;
    private DeviceType deviceType = DeviceType.NORMAL;
    protected int pageNum;
    private Principal principal;

    // attributes populated by processors where appropriate
    protected Weblog weblog;
    private User blogger;
    private boolean siteWide;

    public WeblogRequest() {
    }

    WeblogRequest(Principal principal) {
        this.principal = principal;
    }

    public String getWeblogHandle() {
        return weblogHandle;
    }

    public void setWeblogHandle(String weblogHandle) {
        this.weblogHandle = weblogHandle;
    }

    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    public String getAuthenticatedUser() {
        if (principal != null) {
            return principal.getName();
        }
        return null;
    }

    public boolean isLoggedIn() {
        return getAuthenticatedUser() != null;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public User getBlogger() {
        return blogger;
    }

    public void setBlogger(User blogger) {
        this.blogger = blogger;
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
