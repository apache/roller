
package org.apache.roller.ui.rendering;

import java.io.Writer;
import java.util.Map;


/**
 * Interface representing a content renderer in Roller.
 */
public interface Renderer {
    
    public void render(Map model, Writer writer) throws Exception;
    
}
