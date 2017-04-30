/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
package org.apache.roller.weblogger.ui.core.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Entry point filter for all requests. This filter ensures that the request
 * encoding is set to UTF-8 before any other processing forces request parsing
 * using a default encoding.
 * This filter should normally be first in the chain.
 */
public class CharEncodingFilter implements Filter {

    private static Logger log = LoggerFactory.getLogger(CharEncodingFilter.class);

    public void init(FilterConfig filterConfig) throws ServletException {
    }

    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        log.debug("Processing CharEncodingFilter");
        try {
            if (!"UTF-8".equals(req.getCharacterEncoding())) {
                // only set encoding if not already UTF-8
                // despite the fact that this is the first filter in the chain, on Glassfish it
                // is already too late to set request encoding without getting a WARN level log message
                req.setCharacterEncoding("UTF-8");
            }
            log.debug("Set request character encoding to UTF-8");
        } catch (UnsupportedEncodingException e) {
            // This should never happen since UTF-8 is a Java-specified required encoding.
            throw new ServletException("Can't set incoming encoding to UTF-8");
        }

        chain.doFilter(req, res);
    }

}
