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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.requests;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a request regarding a particular weblog entry.
 */
public class WeblogEntryRequest extends WeblogRequest {

    private static Logger log = LoggerFactory.getLogger(WeblogEntryRequest.class);

    private String weblogAnchor = null;

    public WeblogEntryRequest(HttpServletRequest request) {
        
        // let our parent take care of their business first
        // parent determines weblog handle and locale if specified
        super(request);
        
        // we only want the path info left over from after our parents parsing
        String pathInfo = this.getPathInfo();
        
        /*
         * parse path info.  we expect ...
         * /entry/<anchor> - permalink
         */
        if (pathInfo != null && pathInfo.trim().length() > 0) {
            // we should only ever get 2 path elements
            String[] pathElements = pathInfo.split("/");
            if (pathElements.length == 2) {
                String context = pathElements[0];
                if ("entry".equals(context)) {
                    try {
                        this.weblogAnchor = URLDecoder.decode(pathElements[1], "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        // should never happen
                        log.error("exception", ex);
                    }
                }
            }
        }

        if (weblogAnchor == null) {
            throw new IllegalArgumentException("bad path info: " + request.getRequestURL());
        }
    }

    public String getWeblogAnchor() {
        return weblogAnchor;
    }

}
