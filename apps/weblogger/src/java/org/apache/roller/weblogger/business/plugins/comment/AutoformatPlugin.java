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

import java.io.BufferedReader;
import java.io.StringReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;


/**
 * Comment plugin which turns plain text paragraph formatting into html
 * paragraph formatting using <p> and <br/> tags.
 */
public class AutoformatPlugin implements WeblogEntryCommentPlugin {
    
    private static final Log log = LogFactory.getLog(AutoformatPlugin.class);
    
    
    public AutoformatPlugin() {
        // no-op
    }
    
    
    public String getName() {
        return "AutoFormat";
    }
    
    
    public String getDescription() {
        return "Converts plain text style paragraphs into html paragraphs.";
    }
    
    
    public String render(final WeblogEntryComment comment, String text) {
        
        log.debug("starting value:\n"+text);
        
        /* 
         * setup a buffered reader and iterate through each line
         * inserting html as needed
         *
         * NOTE: we consider a paragraph to be 2 endlines with no text between them
         */
        StringBuffer buf = new StringBuffer();
        try {
            BufferedReader br = new BufferedReader(new StringReader(text));
            
            String line = null;
            boolean insidePara = false;
            while((line = br.readLine()) != null) {
                
                if(!insidePara && line.trim().length() > 0) {
                    // start of a new paragraph
                    buf.append("\n<p>");
                    buf.append(line);
                    insidePara = true;
                } else if(insidePara && line.trim().length() > 0) {
                    // another line in an existing paragraph
                    buf.append("<br/>\n");
                    buf.append(line);
                } else if(insidePara && line.trim().length() < 1) {
                    // end of a paragraph
                    buf.append("</p>\n\n");
                    insidePara = false;
                }
            }
            
            // if the text ends without an empty line then we need to
            // terminate the last paragraph now
            if(insidePara)
                buf.append("</p>\n\n");
            
        } catch(Exception e) {
            log.warn("trouble rendering text.", e);
        }
        
        log.debug("ending value:\n"+buf.toString());
        
        return buf.toString();
    }
    
}
