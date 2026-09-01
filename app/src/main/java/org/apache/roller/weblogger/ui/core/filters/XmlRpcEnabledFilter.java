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

package org.apache.roller.weblogger.ui.core.filters;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

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
            LOG.warn("XML-RPC service is disabled; rejecting request");
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
