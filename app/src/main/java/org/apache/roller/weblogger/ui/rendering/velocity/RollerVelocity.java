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

package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;


/**
 * Represents the VelocityEngine used by Roller.
 *
 * We construct our own instance of VelocityEngine, initialize it, and provide
 * access to the instance via the Singleton getInstance() method.
 */
public class RollerVelocity {
    
    public static final String VELOCITY_CONFIG = "/WEB-INF/velocity.properties";
    
    private static final Log log = LogFactory.getLog(RollerVelocity.class);
    
    private static final VelocityEngine velocityEngine;
    
    
    static {
        log.info("Initializing Velocity Rendering Engine");
        
        // initialize the Velocity engine
        Properties velocityProps = new Properties();
        
        try {
            try (InputStream instream =
                    RollerContext.getServletContext().getResourceAsStream(VELOCITY_CONFIG)) {
                velocityProps.load(instream);
            }

            // Development theme reloading
            Boolean themeReload = WebloggerConfig.getBooleanProperty("themes.reload.mode");
            
            // Override for theme reloading
            if (themeReload) {
                velocityProps.setProperty("resource.loader.class.cache", "false");
                velocityProps.setProperty("resource.loader.class.modification_check_interval", "2");
                velocityProps.setProperty("resource.loader.webapp.cache", "false");
                velocityProps.setProperty("resource.loader.webapp.modification_check_interval", "2");
                velocityProps.setProperty("velocimacro.library.autoreload", "true");
            }
           
            log.debug("Velocity engine props = "+velocityProps);
            
            // construct the VelocityEngine
            velocityEngine = new VelocityEngine();
            
            // init velocity with our properties
            velocityEngine.init(velocityProps);
            
        } catch (IOException e) {
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
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name) {
        return velocityEngine.getTemplate(name + "|standard");
    }

    /**
     * Convenience static method for looking up a template.
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name, 
			MobileDeviceRepository.DeviceType deviceType) {
        return velocityEngine.getTemplate(name + "|" + deviceType);
    }
    
    /**
     * Convenience static method for looking up a template.
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name, String encoding) {
        return velocityEngine.getTemplate(name + "|standard", encoding);
    }
	
    /**
     * Convenience static method for looking up a template.
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name, 
			MobileDeviceRepository.DeviceType deviceType, String encoding) {
        return velocityEngine.getTemplate(name + "|" + deviceType, encoding);
    }
}
