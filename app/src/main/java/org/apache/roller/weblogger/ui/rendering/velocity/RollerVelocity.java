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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.velocity;

import java.io.InputStream;
import java.util.Properties;

import org.apache.roller.weblogger.business.startup.RollerContext;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.mobile.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the VelocityEngine used by Roller.  In the getTemplate(name...)
 * overrides in this class, Velocity uses the resource loaders defined
 * in velocity.properties (in the order given in the resource.loader
 * property value in that file) to obtain the Template to use for the
 * given template name.
 *
 * Further, ThemeResourceLoader parses out the |xxxxx portion added in
 * the getTemplate() overrides to determine the proper device type's rendition
 * to use.
 *
 * We construct our own instance of VelocityEngine, initialize it, and provide
 * access to the instance via the Singleton getInstance() method.
 */
public class RollerVelocity {
    
    public static final String VELOCITY_CONFIG = "/WEB-INF/velocity.properties";

    private static Logger log = LoggerFactory.getLogger(RollerVelocity.class);
    
    private static VelocityEngine velocityEngine = null;
    
    
    static {
        log.info("Initializing Velocity Rendering Engine");
        
        // initialize the Velocity engine
        Properties velocityProps = new Properties();
        
        try {
            InputStream instream = RollerContext.getServletContext().getResourceAsStream(VELOCITY_CONFIG);
            
            velocityProps.load(instream);
            
            log.debug("Velocity engine props = {}", velocityProps);
            
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
     * Convenience static method for retrieving a Velocity template.
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name) {
        return velocityEngine.getTemplate(name + "|normal");
    }

     /**
     * Convenience static method for retrieving a Velocity template.
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name, DeviceType deviceType) {
        return velocityEngine.getTemplate(name + "|" + deviceType);
    }
    
    /**
     * Convenience static method for retrieving a Velocity template.
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name, String encoding) {
        return velocityEngine.getTemplate(name + "|normal", encoding);
    }
	
    /**
     * Convenience static method for retrieving a Velocity template.
     * @throws org.apache.velocity.exception.ResourceNotFoundException,
     *       org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name, DeviceType deviceType, String encoding) {
        return velocityEngine.getTemplate(name + "|" + deviceType, encoding);
    }
}
