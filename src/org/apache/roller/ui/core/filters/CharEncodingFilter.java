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
 */

package org.apache.roller.ui.core.filters;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.jstl.core.Config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.Globals;

/**
 * Entry point filter for all requests.  This filter ensures that the request encoding is set to UTF-8 before any other
 * processing forces request parsing using a default encoding.  It also syncs up the Struts and JSTL locales.  This
 * filter should normally be first and last in the chain.
 *
 * @author <a href="mailto:anil@busybuddha.org">Anil Gangolli</a>
 * @web.filter name="CharEncodingFilter"
 */

public class CharEncodingFilter implements Filter
{
    private FilterConfig mFilterConfig = null;
    private static Log mLogger =
        LogFactory.getFactory().getInstance(CharEncodingFilter.class);

    /**
     * init
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
        mFilterConfig = filterConfig;
    }

    /**
     * destroy
     */
    public void destroy()
    {
    }

    /**
     * Set the character encoding and sync up Struts and JSTL locales.  This filter should normally be first (and last)
     * in the chain.
     */
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
        throws IOException, ServletException
    {
        if (mLogger.isDebugEnabled()) mLogger.debug("Processing CharEncodingFilter");
        try
        {
            req.setCharacterEncoding("UTF-8");
            if (mLogger.isDebugEnabled()) mLogger.debug("Set request character encoding to UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            // This should never happen since UTF-8 is a Java-specified required encoding.
            throw new ServletException("Can't set incoming encoding to UTF-8");
        }

        chain.doFilter(req, res);
    }

}
