
package org.apache.roller.ui.rendering;


/**
 * A factory for Renderer objects.
 *
 * Implementations of this interface are used to handle that actual lookup of
 * what Renderer object should be used to render a given resource.
 */
public interface RendererFactory {
    
    public Renderer getRenderer(String rendererType, String resourceId) throws Exception;
    
}
