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
package org.apache.roller.weblogger.business.plugins.comment;

import java.io.BufferedReader;
import java.io.StringReader;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Comment plugin which turns plain text paragraph formatting into html
 * paragraph formatting using the <p> tag.
 */
public class AutoformatPlugin implements WeblogEntryCommentPlugin {

    private static Logger log = LoggerFactory.getLogger(AutoformatPlugin.class);

    public AutoformatPlugin() {
    }

    /**
     * Unique identifier.  This should never change. 
     */
    public String getId() {
        return "AutoFormat";
    }

    public String getName() {
        return "Auto Format";
    }

    public String getDescription() {
        return "Converts plain text style paragraphs into html paragraphs.";
    }

    public String render(final WeblogEntryComment comment, String text) {
        log.debug("starting value: {}", text);
        
        /* 
         * setup a buffered reader and iterate through each line
         * inserting html as needed
         *
         * NOTE: we consider a paragraph to be 2 endlines with no text between them
         */
        StringBuilder buf = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new StringReader(text));
            
            String line;
            boolean insidePara = false;
            while((line = br.readLine()) != null) {
                
                if (!insidePara && line.trim().length() > 0) {
                    // start of a new paragraph
                    buf.append("<p>");
                    buf.append(line);
                    insidePara = true;
                } else if (insidePara && line.trim().length() == 0) {
                    // end of a paragraph
                    buf.append("</p>");
                    insidePara = false;
                } else {
                    buf.append(" ").append(line);
                }
            }
            
            // if the text ends without an empty line then we need to
            // terminate the last paragraph now
            if (insidePara) {
                buf.append("</p>");
            }
            
        } catch(Exception e) {
            log.warn("trouble rendering text.", e);
        }
        
        log.debug("ending value:\n {}", buf.toString());
        return buf.toString();
    }
    
}
