
package org.roller.presentation.velocity.plugins.radeox;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.radeox.EngineManager;
import org.radeox.engine.context.BaseRenderContext;
import org.radeox.engine.context.RenderContext;
import org.roller.pojos.WeblogEntryData;
import org.roller.presentation.RollerRequest;
import org.roller.presentation.velocity.PagePlugin;

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
    public void init(RollerRequest rreq, Context ctx)
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
    
    public String render( WeblogEntryData entry, boolean skipFlag)
    {
        return render( entry.getText() );
    }

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }
}
