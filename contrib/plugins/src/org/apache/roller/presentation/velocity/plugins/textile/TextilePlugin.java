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

package org.apache.roller.presentation.velocity.plugins.textile;

import java.util.Map;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.pojos.WebsiteData;
import org.apache.roller.model.WeblogEntryPlugin;

/**
 * @author David M Johnson
 */
public class TextilePlugin implements WeblogEntryPlugin
{
    public String name = "Textile Formatter";
    public String description = "Allows use of Textile formatting to easily " +
        "generate HTML. See the <a href='http://textism.com/tools/textile' target='textile'>Textile</a> site.";

    public String toString() { return name; } 
    
    private net.sf.textile4j.Textile mTextile = new net.sf.textile4j.Textile();
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(TextilePlugin.class);
    
    public TextilePlugin()
    {
        mLogger.debug("Textile Plugin instantiated.");
    }
    
    /** 
     * Put plugin into the page context so templates may access it.
     */
    public void init(WebsiteData website, Map model)
    {
        model.put("textileRenderer",this);
    }
    
    /** 
     * Convert an input string that contains text that uses the Textile
     * syntax to an output string in HTML format.
     * @param src Input string that uses Textile syntax
     * @return Output string in HTML format.
     */
    public String render(WeblogEntryData entry, String src )
    {
        return mTextile.process(src);
    }

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }

    //public boolean getSkipOnSingleEntry() {return false;}
}
