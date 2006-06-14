
package org.apache.roller.ui.rendering;

import org.apache.roller.ui.rendering.velocity.VelocityRenderer;
import org.apache.roller.ui.rendering.velocity.VelocityWeblogPageRenderer;


/**
 * The default RendererFactory for Roller.
 */
public class RollerRendererFactory implements RendererFactory {
    
    
    public Renderer getRenderer(String rendererType, String resourceId) 
            throws Exception {
        
        if("velocity".equals(rendererType)) {
            
            // standard velocity template
            return new VelocityRenderer(resourceId);
        } else if("velocityWeblogPage".equals(rendererType)) {
            
            // special case for velocity weblog page templates
            // needed because of the way we do the decorator stuff
            return new VelocityWeblogPageRenderer(resourceId);
        }
        
        throw new Exception("No renderer found!");
    }
    
}
