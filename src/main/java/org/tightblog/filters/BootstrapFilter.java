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
package org.tightblog.filters;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tightblog.config.DynamicProperties;
import org.tightblog.util.Utilities;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Used during app startup, determines base site URL if not specified via property
 * and checks if initial application installation is necessary.
 */
@Component
@EnableConfigurationProperties(DynamicProperties.class)
public class BootstrapFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(BootstrapFilter.class);

    @Autowired
    private DynamicProperties dp;

    private boolean siteUrlInitialized;

    private ServletContext context;

    public void init(FilterConfig filterConfig) throws ServletException {
        context = filterConfig.getServletContext();
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) req;

        if (!siteUrlInitialized) {
            // determine absolute path for the app
            if (StringUtils.isBlank(dp.getAbsoluteUrl())) {
                String absPath = Utilities.determineSiteUrl(request);
                dp.setAbsoluteUrl(absPath);
                log.info("Base site URL used to create links calculated to be {}, if desired " +
                        "use site.absoluteUrl property to override", absPath);
            } else {
                log.info("Base site URL of {} set via site.absoluteUrl property", dp.getAbsoluteUrl());
            }
            this.siteUrlInitialized = true;
        }

        if (!dp.isDatabaseReady() && !isInstallUrl(request.getRequestURI())) {
            log.debug("Forwarding to install page");
            // install page will check database connectivity & schema status and bootstrap if all OK.
            RequestDispatcher rd = context.getRequestDispatcher("/tb-ui/install/install");
            rd.forward(req, res);
        } else {
            chain.doFilter(request, res);
        }
    }

    private boolean isInstallUrl(String uri) {
        return uri != null && (
                uri.endsWith("/install/bootstrap") || uri.endsWith("/install/create") ||
                        uri.endsWith(".js") || uri.endsWith(".css"));
    }

    public void destroy() {
    }
}
