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

package org.roller.presentation.filters;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.util.StringUtils;

/**
 * Braindead simple IPBanFilter. XDoclet tags disabled because I don't want this
 * to be on by default. Users who want it can configure it themselves in web.xml.
 * web.filter name="IPBanFilter"
 * web.filter-init-param name="denyFrom" value="" description="Comma-separated list of banned IPs"
 * @author David M Johnson
 */
public class IPBanFilter implements Filter
{
    private List denyFrom = null;
    private static Log mLogger =
        LogFactory.getFactory().getInstance(IPBanFilter.class);
    
    public IPBanFilter()
    {
        super();
    }

    public void init(FilterConfig filterConfig) throws ServletException
    {
        /*
         * This should be updated to the new config, however I don't want
         * to do it myself since I'm not sure if/how it is being used.
         * 
         * This looks like something that could be a long list, so maybe
         * it should be in the DB or possibly its own file?
         * -- Allen G
         */
        String denyFromParam = filterConfig.getInitParameter("denyFrom");
        denyFrom = Arrays.asList(StringUtils.split(denyFromParam,","));
    }

    public void doFilter(
        ServletRequest req,
        ServletResponse res,
        FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (denyFrom.contains(request.getRemoteAddr()))
        {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        else 
        {            
            chain.doFilter(request, response);
        }
    }

    public void destroy() 
    {
    }
}
