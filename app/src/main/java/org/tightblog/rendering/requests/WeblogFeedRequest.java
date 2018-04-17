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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.tightblog.rendering.requests;

import javax.servlet.http.HttpServletRequest;

import org.tightblog.util.Utilities;

/**
 * Represents a request for a TightBlog weblog feed.
 */
public class WeblogFeedRequest extends WeblogRequest {

    private String weblogCategoryName;
    private String tag;

    public WeblogFeedRequest() {
    }

    public static class Creator {
        public WeblogFeedRequest create(HttpServletRequest servletRequest) {
            WeblogFeedRequest feedRequest = new WeblogFeedRequest();
            WeblogRequest.parseRequest(feedRequest, servletRequest);
            feedRequest.parseFeedRequestInfo();
            return feedRequest;
        }
    }

    /**
     * Handles:
     * /feed - Atom feed
     * /feed/category/<category> - Atom feed of category
     * /feed/tag/<tag> - Atom feed of tag
     */
    private void parseFeedRequestInfo() {
        if (extraPathInfo != null && extraPathInfo.trim().length() > 0) {
            String[] pathElements = extraPathInfo.split("/", 3);

            if (pathElements.length == 3) {
                if ("category".equals(pathElements[1])) {
                    weblogCategoryName = Utilities.decode(pathElements[2].replace("+", "%2B"));
                } else if ("tag".equals(pathElements[1])) {
                    tag = Utilities.decode(pathElements[2].toLowerCase().replace("+", "%2B"));
                }
            }
        }
    }

    public String getWeblogCategoryName() {
        return weblogCategoryName;
    }

    public void setWeblogCategoryName(String weblogCategoryName) {
        this.weblogCategoryName = weblogCategoryName;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
