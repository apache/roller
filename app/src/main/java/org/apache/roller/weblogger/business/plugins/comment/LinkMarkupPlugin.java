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

package org.apache.roller.weblogger.business.plugins.comment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Comment plugin which turns plain text URLs into hyperlinks using
 * the html anchor (&lt;a&gt;) tag.
 * 
 * Contributed by Matthew Montgomery.
 */
public class LinkMarkupPlugin implements WeblogEntryCommentPlugin {
    private static final Log LOG = LogFactory.getLog(LinkMarkupPlugin.class);

    private static final Pattern pattern = Pattern.compile(
            "http[s]?://[^/][\\S]+", Pattern.CASE_INSENSITIVE);  
    
    public LinkMarkupPlugin() {
        LOG.debug("Instantiating LinkMarkupPlugin");
    }
    
    /**
     * Unique identifier.  This should never change. 
     */
    public String getId() {
        return "LinkMarkup";
    }
    
    
    /** Returns the display name of this Plugin. */
    public String getName() {
        return "Link Markup";
    }
    
    
    /** Briefly describes the function of the Plugin.  May contain HTML. */
    public String getDescription() {
        return "Automatically creates hyperlinks out of embedded URLs.";
    }
    
    
    /**
     * Apply plugin to the specified text.
     *
     * @param comment The comment being rendered.
     * @param text String to which plugin should be applied.
     *
     * @return Results of applying plugin to string.
     */
    public String render(final WeblogEntryComment comment, String text) {
        StringBuilder result;
        result = new StringBuilder();
        
        if (text != null) {
            Matcher matcher;
            matcher = pattern.matcher(text);

            int start = 0;
            int end = text.length();
            
            while (start < end) {
                if (matcher.find()) {
                    // Copy up to the match
                    result.append(text.substring(start, (matcher.start())));

                    // Copy the URL and create the hyperlink
                    // Unescape HTML as we don't know if that setting is on
                    String url;
                    url = Utilities.unescapeHTML(text.substring(
                            matcher.start(), matcher.end()));

                    // Build the anchor tag and escape HTML in the URL output
                    result.append("<a href=\"");
                    result.append(Utilities.escapeHTML(url));
                    result.append("\">");
                    result.append(Utilities.escapeHTML(url));
                    result.append("</a>");

                    // Increment the starting index
                    start = matcher.end();
                }
                else {
                    // Copy the remainder
                    result.append(text.substring(start, end));

                    // Increment the starting index to exit the loop
                    start = end;
                }
            }
        }
        else {
            result.append(text);    
        }

        return result.toString();
    }
    
}
