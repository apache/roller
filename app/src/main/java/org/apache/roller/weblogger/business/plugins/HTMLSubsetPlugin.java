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
package org.apache.roller.weblogger.business.plugins;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.Utilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Transforms the given String into a subset of HTML.
 */
public class HTMLSubsetPlugin implements WeblogEntryCommentPlugin {

    private static Logger log = LoggerFactory.getLogger(HTMLSubsetPlugin.class);

    public HTMLSubsetPlugin() {
        log.debug("Instantiating HTMLSubsetPlugin");
    }

    /**
     * Unique identifier.  This should never change. 
     */
    public String getId() {
        return "HTMLSubset";
    }

    public String getName() {
        return "HTML Subset Restriction";
    }

    public String getDescription() {
        return "Transforms the given comment body into a subset of HTML";
    }

    public String render(final WeblogEntryComment comment, String text) {
        String output = text;
        
        // only do this if comment is HTML
        if ("text/html".equals(comment.getContentType())) {
            log.debug("starting value:\n {}", output);
            	        
	        // escape all html
	        output = StringEscapeUtils.escapeHtml4(output);
	        
	        // return some of the escaped tags back to HTML
	        output = Utilities.transformToHTMLSubset(output);
	        log.debug("ending value:\n {}", text);
        }
                
        return output;
    }
    
}
