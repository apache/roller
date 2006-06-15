
package org.apache.roller.ui.rendering;

import org.apache.roller.ui.rendering.velocity.VelocityRenderer;
import org.apache.roller.ui.rendering.velocity.VelocityWeblogPageRenderer;


/**
 * The default RendererFactory for Roller.
 */
public class VelocityRendererFactory implements RendererFactory {
    
    
    public Renderer getRenderer(String rendererType, String resourceId) {
        
        if("velocity".equals(rendererType)) {
            
            // standard velocity template
            return new VelocityRenderer(resourceId);
        } else if("velocityWeblogPage".equals(rendererType)) {
            
            // special case for velocity weblog page templates
            // needed because of the way we do the decorator stuff
            return new VelocityWeblogPageRenderer(resourceId);
        }
        
        // we don't want to handle this content
        return null;
    }
    
}
