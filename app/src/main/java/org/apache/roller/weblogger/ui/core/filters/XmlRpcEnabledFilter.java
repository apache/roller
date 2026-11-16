/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.ui.core.filters;

import java.io.IOException;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;

/**
 * Gates the XML-RPC endpoint on <code>webservices.enableXmlRpc</code>, ahead of
 * the servlet, so a disabled service is not reachable.
 */
public class XmlRpcEnabledFilter implements Filter {

    private static final Log LOG = LogFactory.getLog(XmlRpcEnabledFilter.class);

    @Override
    public void init(FilterConfig filterConfig) {
        // nothing to configure
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

        if (!WebloggerRuntimeConfig.getBooleanProperty("webservices.enableXmlRpc")) {
            LOG.debug("XML-RPC service is disabled; rejecting request");
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
            httpResponse.setContentType("text/plain;charset=UTF-8");
            httpResponse.getWriter().write("XML-RPC service is disabled");
            return;
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // nothing to release
    }
}
