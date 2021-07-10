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

package org.apache.roller.weblogger.business.plugins.entry;

import java.io.BufferedReader;
import java.io.StringReader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.Weblog;


/**
 * Simple page plugin that converts paragraphs of plain text into html paragraphs.
 * We wrap each full paragraph in html &lt;p&gt; opening and closing tags, and
 * also add &lt;br&gt; tags to the end of lines with breaks inside a paragraph.
 *
 * Example:
 * This is one
 * paragraph
 *
 * Becomes:
 * &lt;p&gt;This is one&lt;br/&gt;
 * paragraph&lt;/p&gt;
 *
 */
public class ConvertLineBreaksPlugin implements WeblogEntryPlugin {
    
    private static final Log mLogger = LogFactory.getLog(ConvertLineBreaksPlugin.class);
    
    private static final String NAME = "Convert Line Breaks";
    private static final String DESCRIPTION = "Convert plain text paragraphs to html by adding p and br tags";
    private static final String VERSION = "0.1";
    
    
    public ConvertLineBreaksPlugin() {
        mLogger.debug("Instantiating ConvertLineBreaksPlugin v"+ ConvertLineBreaksPlugin.VERSION);
    }
    
    
    @Override
    public String getName() {
        return NAME;
    }
    
    
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
    
    
    @Override
    public void init(Weblog website) throws WebloggerException {
        // we don't need to do any init.
        mLogger.debug("initing");
    }
    
    
    /**
     * Transform the given plain text into html text by inserting p and br
     * tags around paragraphs and after line breaks.
     */
    @Override
    public String render(WeblogEntry entry, String str) {
        
        if(str == null || str.isBlank()) {
            return "";
        }
        
        mLogger.debug("Rendering string of length "+str.length());
        
        /* setup a buffered reader and iterate through each line
         * inserting html as needed
         *
         * NOTE: we consider a paragraph to be 2 endlines with no text between them
         */
        StringBuilder buf = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new StringReader(str));
            
            String line;
            boolean insidePara = false;
            while((line = br.readLine()) != null) {
                
                if(!insidePara && !line.isBlank()) {
                    // start of a new paragraph
                    buf.append("\n<p>");
                    buf.append(line);
                    insidePara = true;
                } else if(insidePara && !line.isBlank()) {
                    // another line in an existing paragraph
                    buf.append("<br/>\n");
                    buf.append(line);
                } else if(insidePara && line.isBlank()) {
                    // end of a paragraph
                    buf.append("</p>\n\n");
                    insidePara = false;
                }
            }
            
            // if the text ends without an empty line then we need to
            // terminate the last paragraph now
            if(insidePara) {
                buf.append("</p>\n\n");
            }
            
        } catch(Exception e) {
            mLogger.warn("trouble rendering text.", e);
            return str;
        }
        
        return buf.toString();
    }
    
}
