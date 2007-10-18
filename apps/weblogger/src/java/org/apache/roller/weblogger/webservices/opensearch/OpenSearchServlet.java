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
package org.apache.roller.weblogger.webservices.opensearch;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Return OpenSearch descriptor that describes Roller's search facilities.
 * @author Dave Johnson (<a href="mailto:davidm.johnson@sun.com">davidm.johnson@sun.com</a>)
 */
public class OpenSearchServlet extends HttpServlet {
    
    public void doGet(
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        String[] pathInfo = new String[0];
        String handle = null;
        
        // Will return descriptor for searching specified blog
        if (request.getPathInfo() != null) {
            pathInfo = Utilities.stringToStringArray(request.getPathInfo(), "/");
        }

        if (pathInfo.length == 0) {
            // URL format: [context]/roller-services/opensearch
            handle = WebloggerRuntimeConfig.getProperty("site.frontpage.weblog.handle");

        } else if (pathInfo.length == 1) {
            // URL format: [context]/roller-services/opensearch/[weblog-handle]
            handle = pathInfo[0];

        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL");
            return;
        }

        String shortName = null;
        String description = null;
        String contact = null;
        String searchFeed = null;
        String searchPage = null;
        
        URLStrategy strat = WebloggerFactory.getWeblogger().getUrlStrategy();
        Weblog weblog = null;
        try {
            weblog = WebloggerFactory.getWeblogger().getWeblogManager().getWeblogByHandle(handle);
        } catch (WebloggerException ex) {
            throw new ServletException("ERROR: fetching specified weblog");
        }
        searchPage = StringEscapeUtils.escapeXml(
                strat.getWeblogSearchPageURLTemplate(weblog));
        searchFeed = StringEscapeUtils.escapeXml(
                strat.getWeblogSearchFeedURLTemplate(weblog));

        boolean siteWide = WebloggerRuntimeConfig.isSiteWideWeblog(handle);
        if (siteWide) {
            shortName = "[Search Descriptor] " + StringEscapeUtils.escapeXml(
                    WebloggerRuntimeConfig.getProperty("site.shortName"));
            description = StringEscapeUtils.escapeXml(
                    WebloggerRuntimeConfig.getProperty("site.description"));
            contact = StringEscapeUtils.escapeXml(
                    WebloggerRuntimeConfig.getProperty("site.adminemail"));
                    
        } else {
            shortName = StringEscapeUtils.escapeXml(weblog.getName());
            description = StringEscapeUtils.escapeXml(weblog.getDescription());
            contact = StringEscapeUtils.escapeXml(weblog.getEmailAddress());
        }

        response.setContentType("application/opensearchdescription+xml");
        
        PrintWriter pw = new PrintWriter(response.getWriter());
        pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        pw.println("<OpenSearchDescription xmlns=\"http://a9.com/-/spec/opensearch/1.1/\">");
        pw.println("   <ShortName>" + shortName + "</ShortName>");
        pw.println("   <Description>" + description + "</Description>");
        pw.println("   <Contact>" + contact + "</Contact>");
        pw.println("   <Url type=\"application/atom+xml\" ");
        pw.println("      template=\"" + searchFeed + "\"/>");
        pw.println("   <Url type=\"text/html\" ");
        pw.println("      template=\"" + searchPage + "\"/>");
        pw.println("</OpenSearchDescription>");
        pw.flush();            
        pw.close();
    }
}

