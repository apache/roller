
package org.apache.roller.ui.rendering.velocity;

import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.ui.core.RollerContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;


/**
 * Represents the VelocityEngine used by Roller.
 *
 * We construct our own instance of VelocityEngine, initialize it, and provide
 * access to the instance via the Singleton getInstance() method.
 */
public class RollerVelocity {
    
    public static final String VELOCITY_CONFIG = "/WEB-INF/velocity.properties";
    
    private static Log log = LogFactory.getLog(RollerVelocity.class);
    
    private static VelocityEngine velocityEngine = null;
    
    
    static {
        log.info("Initializing Velocity Rendering Engine");
        
        // initialize the Velocity engine
        Properties velocityProps = new Properties();
        
        try {
            InputStream instream =
                    RollerContext.getServletContext().getResourceAsStream(VELOCITY_CONFIG);
            
            velocityProps.load(instream);
            
            log.info("Velocity engine props = "+velocityProps);
            
            // construct the VelocityEngine
            velocityEngine = new VelocityEngine();
            
            // init velocity with our properties
            velocityEngine.init(velocityProps);
            
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Access to the VelocityEngine.
     *
     * This shouldn't ever be needed, but it's here just in case someone
     * really needs to do something special.
     */
    public static VelocityEngine getEngine() {
        return velocityEngine;
    }
    
    
    /**
     * Convenience static method for looking up a template.
     */
    public static Template getTemplate(String name)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        return velocityEngine.getTemplate(name);
    }
    
    
    /**
     * Convenience static method for looking up a template.
     */
    public static Template getTemplate(String name, String encoding)
            throws ResourceNotFoundException, ParseErrorException, Exception {
        return velocityEngine.getTemplate(name, encoding);
    }
    
}
