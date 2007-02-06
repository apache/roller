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
package org.apache.roller.planet.ui.utils;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.ui.forms.ConfigForm;

/**
 * Simple support for "bookmarkable" GET requests in JSF - gives a page's form 
 * the opportunity to load itself from the request before the page is executed. 
 * Once loaded, the form bean is saved as a request attribute so that it is 
 * available in the JSF page.
 *
 * <pre>
 * To use this make sure your form class:
 *
 * 1) Implements LoadableForm and loads itself in load() method
 *    i.e. Use request params and/or pathInfo to load form from backend
 *
 * 2) Has same name as corresponding page
 *    e.g. page name configForm.faces corresponds to class name ConfigForm
 *    and to request parameter configForm
 *
 * 3) Exists in package specified by context param 'loadableFormsPackage'
 *    (defaults to org.apache.roller.planet.ui.forms if not specified)
 * </pre>
 */
public class LoadableFormFilter implements Filter {
    private static Log log = LogFactory.getLog(LoadableFormFilter.class);
    private String loadableFormsPackage = "org.apache.roller.planet.ui.forms";
    
    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext ctx = filterConfig.getServletContext();
        String param = ctx.getInitParameter("loadableFormsPackage");
        if (StringUtils.isNotEmpty(param)) {
            loadableFormsPackage = param;
        }
    }

    public void doFilter(
            ServletRequest servletRequest, 
            ServletResponse servletResponse, 
            FilterChain filterChain) throws IOException, ServletException {
        
        HttpServletRequest request = (HttpServletRequest)servletRequest;
        
        // only handle with HTTP GET requests with URIs ending in .faces
        if ("GET".equals(request.getMethod()) 
                && request.getRequestURI() != null 
                && request.getRequestURI().endsWith(".faces")) {
            String formName = null;
            LoadableForm form = null; 
            try { // to load form class based on page name in URI
                String[] pathInfo = request.getRequestURI().split("/");
                String lastSegment = pathInfo[pathInfo.length - 1];            
                formName = lastSegment.substring(0, lastSegment.length() - 6);
                String className = loadableFormsPackage + "." + StringUtils.capitalize(formName);
                Class formClass = Class.forName(className);
                form = (LoadableForm)formClass.newInstance();                    
            } catch (Exception ignored) {
                // that's OK, not every page has a loadable form
                log.debug("Problem loading form for " + formName, ignored);
            }                                
            if (form != null) try { // to call form class load() method 
                log.debug("Loading form: " + formName);  
                form.load(request);                       
                request.getSession().setAttribute(formName, form);
            } catch (Exception e) {
                // error during form load indicates real problem
                throw new ServletException("ERROR loading form " + formName, e);
            }                
        }
        filterChain.doFilter(servletRequest, servletResponse);               
    }

    public void destroy() {}
    
}
