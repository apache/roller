
package org.apache.roller.ui.rendering.velocity;

import java.io.Writer;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.rendering.Renderer;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;


/**
 * Renderer for Velocity templates.
 */
public class VelocityRenderer implements Renderer {
    
    private static Log log = LogFactory.getLog(VelocityRenderer.class);
    
    private String resourceId = null;
    
    
    public VelocityRenderer(String resource) throws Exception {
        
        this.resourceId = resource;
    }
    
    
    public void render(Map model, Writer out) throws Exception {
        
        // lookup the specified resource
        Template tmpl = RollerVelocity.getTemplate(this.resourceId, "UTF-8");
        
        // convert model to Velocity Context
        Context ctx = new VelocityContext(model);
        
        // render output to servlet response
        tmpl.merge(ctx, out);
    }
    
}
