/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package org.apache.roller.weblogger.ui.struts2.core;

import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.security.BootstrapSecurity;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.action.ServletResponseAware;

public class BootstrapToken extends UIAction implements ServletResponseAware {

    private static final Log LOG = LogFactory.getLog(BootstrapToken.class);

    private HttpServletResponse response;
    private String token;

    public BootstrapToken() {
        this.pageTitle = "installer.bootstrap.pageTitle";
    }

    public String execute() {
        setResponseHeaders();
        LOG.info("Roller is waiting for an administrator to submit the one-time setup token; "
                + "database setup will not continue until the token is accepted. Setup page: "
                + getServletRequest().getRequestURL());
        return INPUT;
    }

    public String redeem() {
        setResponseHeaders();
        if (!"POST".equalsIgnoreCase(getServletRequest().getMethod())) {
            addActionError(getText("installer.bootstrap.postRequired"));
            return INPUT;
        }
        if (BootstrapSecurity.redeem(getServletRequest(), token == null ? null : token.trim())) {
            LOG.info("One-time setup token accepted; continuing database setup.");
            return SUCCESS;
        }
        addActionError(getText("installer.bootstrap.invalidToken"));
        return INPUT;
    }

    private void setResponseHeaders() {
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void withServletResponse(HttpServletResponse response) {
        this.response = response;
    }

    public boolean isUserRequired() {
        return false;
    }

    public boolean isWeblogRequired() {
        return false;
    }
}
