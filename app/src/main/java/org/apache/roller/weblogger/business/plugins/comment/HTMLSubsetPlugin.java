/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.weblogger.business.plugins.comment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Transforms the given String into a subset of HTML.
 */
public class HTMLSubsetPlugin implements WeblogEntryCommentPlugin {
    private static final Log LOG = LogFactory.getLog(HTMLSubsetPlugin.class);
    
    
    public HTMLSubsetPlugin() {
        LOG.debug("Instantiating HTMLSubsetPlugin");
    }
    
    
    /**
     * Unique identifier.  This should never change. 
     */
    @Override
    public String getId() {
        return "HTMLSubset";
    }
    
    
    @Override
    public String getName() {
        return "HTML Subset Restriction";
    }
    
    
    @Override
    public String getDescription() {
        return "Transforms the given comment body into a subset of HTML";
    }
    
    
    @Override
    public String render(final WeblogEntryComment comment, String text) {
        String output = text;
        
        // only do this if comment is HTML
        if ("text/html".equals(comment.getContentType())) {
            output = Utilities.sanitizeToHTMLSubset(output);
        }
                
        return output;
    }
    
}
