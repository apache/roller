/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.apache.roller.weblogger.webservices.tagdata;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Return tag statistics in JSON or Atom Category format.
 * These URLs are supported:
 * <ul>
 * <li>/roller-services/tagdata - get tag data for entire site</li>
 * <li>/roller-services/tagdata/weblogs/[handle] - get tag data for specific weblog</li>
 * </ul>
 * See the <a href="http://cwiki.apache.org/confluence/display/ROLLER/Proposal+Tag+Data+API">
 * Tag Data API</a> proposal for details.
 * 
 * @author Elias Torres (<a href="mailto:eliast@us.ibm.com">eliast@us.ibm.com</a>)
 * @author Dave Johnson (<a href="mailto:davidm.johnson@sun.com">davidm.johnson@sun.com</a>)
 */
public class TagDataServlet extends HttpServlet {

    private static final int MAX = WebloggerConfig.getIntProperty("services.tagdata.max", 30);

    
    protected void doPost(
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }

    
    public void doGet(
            HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {

        String[] pathInfo = new String[0];
        boolean siteWide;
        String handle;
        String prefix;
        String format = "json";
        int page = 0;
        
        // TODO: last modified or ETag support, caching, etc.

        if (request.getPathInfo() != null) {
            pathInfo = Utilities.stringToStringArray(request.getPathInfo(),"/");
        }
        if (pathInfo.length == 0) {
            siteWide = true;
            // we'll use the front-page weblog to form URLs
            handle = WebloggerRuntimeConfig.getProperty("site.frontpage.weblog.handle");
        } else if (pathInfo.length == 2 && "weblog".equals(pathInfo[0])) {
            siteWide = false;
            handle = pathInfo[1];
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL");
            return;
        }
        prefix = request.getParameter("prefix");
        if (request.getParameter("format") != null) {
            format = request.getParameter("format");
        }
        try {
            page = Integer.parseInt(request.getParameter("page"));
        } catch (Exception ignored) {}

        Weblogger roller = WebloggerFactory.getWeblogger();
        List<TagStat> tags;
        Weblog weblog;
        try {
            WeblogManager wmgr = roller.getWeblogManager();
            WeblogEntryManager emgr = roller.getWeblogEntryManager();
            weblog = wmgr.getWeblogByHandle(handle);
            // get tags, if site-wide then don't specify weblog
            tags = emgr.getTags(siteWide ? null : weblog, null, prefix, page * MAX, MAX + 1);

        } catch (WebloggerException we) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "ERROR fetching tags");
            return;
        }

        if ("json".equals(format)) {
            response.setContentType("application/json; charset=utf-8");
            PrintWriter pw = response.getWriter();
            pw.println("{ \"prefix\": \"" + (prefix == null ? "" : prefix) + "\",");
            pw.println("  \"weblog\": \"" + (!siteWide ? handle : "") + "\",");
            pw.println("  \"tagcounts\": [" );
            int count = 0;
            for (Iterator it = tags.iterator(); it.hasNext();) {
                TagStat stat = (TagStat) it.next();
                pw.print("    { \"tag\" : \"");
                pw.print(stat.getName());
                pw.print("\", ");
                pw.print("\"count\" : ");
                pw.print(stat.getCount());
                pw.print(" }");
                if (it.hasNext()) {
                    pw.println(", ");
                }
                if (count++ > MAX) {
                    break;
                }
            }
            pw.println("\n  ]\n}");
            response.flushBuffer();
            
        } else if ("xml".equals(format)) {
            URLStrategy urlstrat = roller.getUrlStrategy();
            response.setContentType("application/tagdata+xml; charset=utf-8");
            PrintWriter pw = response.getWriter();
            pw.println("<?xml version=\"1.0\" encoding=\"utf-8\"?>"); 
            pw.println("<categories fixed=\"no\" "); 
            pw.println("   xmlns=\"http://www.w3.org/2007/app\""); 
            pw.println("   xmlns:atom=\"http://www.w3.org/2005/Atom\""); 
            pw.println("   xmlns:tagdata=\"http://roller.apache.org/ns/tagdata\">");
            int count = 0;
            for (TagStat stat : tags) {
                String term = stat.getName();
                // gWCURL fields: weblog, locale, category, dateString, tags, pageNum, absolute
                String viewURI = urlstrat.getWeblogCollectionURL(weblog,
                        null, null, null,
                        Collections.singletonList(stat.getName()),
                        0, true);
                int frequency = stat.getCount();
                pw.print("<atom:category term=\"" + term + "\" tagdata:frequency=\"" + frequency + "\" ");
                pw.println("tagdata:href=\"" + viewURI + "\" />");
                if (count++ > MAX) {
                    break;
                }
            }
            if (tags.size() > MAX) {
                // get next URI, if site-wide then don't specify weblog
                String nextURI = urlstrat.getWeblogTagsJsonURL(weblog, true, page + 1);
                pw.println("<atom:link rel=\"next\" href=\"" + nextURI + "\" />");
            }
            if (page > 0) {
                // get prev URI, if site-wide then don't specify weblog
                String prevURI = urlstrat.getWeblogTagsJsonURL(weblog, true, page - 1);
                pw.println("<atom:link rel=\"previous\" href=\"" + prevURI + "\" />");
            }
            pw.println("</categories>");
            response.flushBuffer();
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Malformed URL");
        }
    }
}

