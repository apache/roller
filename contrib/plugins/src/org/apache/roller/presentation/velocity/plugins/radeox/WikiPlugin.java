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

package org.apache.roller.presentation.velocity.plugins.radeox;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.radeox.EngineManager;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.engine.context.RenderContext;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.model.PagePlugin;
import org.apache.roller.pojos.WebsiteData;

/**
 * @author David M Johnson
 */
public class WikiPlugin implements PagePlugin
{
    protected String name = "Radeox Wiki";
    protected String description = "Allows use of Radeox formatting to generate HTML. " +
        "See the <a href='http://radeox.org/space/snipsnap-help' target='radeox'>Radeox</a> site.";
    
    private static Log mLogger = 
       LogFactory.getFactory().getInstance(WikiPlugin.class);
    
    public WikiPlugin()
    {
        mLogger.debug("Radeox WikiPlugin instantiated.");
    }
    
    public String toString() { return name; }
    
    /** 
     * Put plugin into the page context so templates may access it.
     */
    public void init(
            WebsiteData website,
            Object config,
            String baseURL,
            Context ctx)
    {
        ctx.put("wikiRenderer",this);
    }
    
    /** 
     * Convert an input string that contains text that uses the Radeox Wiki
     * syntax to an output string in HTML format.
     * @param src Input string that uses Radeox Wiki syntax
     * @return Output string in HTML format.
     */
    public String render( String src )
    {
        RenderContext context = new BaseRenderContext();
        return EngineManager.getInstance().render(src, context);
    }
    
    public String render( WeblogEntryData entry, String str)
    {
        return render(str);
    }

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }

    public boolean getSkipOnSingleEntry() {return false;}
}
