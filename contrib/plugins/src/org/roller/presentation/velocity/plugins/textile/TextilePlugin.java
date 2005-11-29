
package org.roller.presentation.velocity.plugins.textile;

import javax.servlet.ServletConfig;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.velocity.context.Context;
import org.roller.pojos.WeblogEntryData;
import org.roller.pojos.WebsiteData;
import org.roller.model.PagePlugin;

/**
 * @author David M Johnson
 */
public class TextilePlugin implements PagePlugin
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
    public void init(
            WebsiteData website,
            Object config,
            String baseURL,
            Context ctx)
    {
        ctx.put("textileRenderer",this);
    }
    
    /** 
     * Convert an input string that contains text that uses the Textile
     * syntax to an output string in HTML format.
     * @param src Input string that uses Textile syntax
     * @return Output string in HTML format.
     */
    public String render( String src )
    {
        return mTextile.process(src);
    }
    
    public String render( WeblogEntryData entry, boolean skipFlag )
    {
        return render( entry.getText() );
    }

    public String getName() { return name; }
    public String getDescription() { return StringEscapeUtils.escapeJavaScript(description); }
}
