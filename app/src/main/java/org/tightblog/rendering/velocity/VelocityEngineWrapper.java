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
package org.tightblog.rendering.velocity;

import java.io.InputStream;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.springframework.mobile.device.DeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents the VelocityEngine used by TightBlog.  In the getTemplate(name...)
 * overrides in this class, Velocity uses the resource loaders defined
 * in velocity.properties (in the order given in the resource.loader
 * property value in that file) to obtain the Template to use for the
 * given template name.
 * <p>
 * Further, ThemeResourceLoader parses out the |xxxxx portion added in
 * the getTemplate() overrides to determine the proper device type's rendition
 * to use.
 * <p>
 * We construct our own instance of VelocityEngine, initialize it, and provide
 * access to the instance via the Singleton getEngine() method.
 */
public class VelocityEngineWrapper {

    private static final String VELOCITY_CONFIG = "/velocity.properties";

    private static Logger log = LoggerFactory.getLogger(VelocityEngineWrapper.class);

    private static VelocityEngine velocityEngine = null;

    static {
        log.info("Initializing Velocity Rendering Engine");

        // initialize the Velocity engine
        Properties velocityProps = new Properties();

        try {
            InputStream instream = VelocityEngineWrapper.class.getClassLoader().getResourceAsStream(VELOCITY_CONFIG);

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
     * <p>
     * This shouldn't ever be needed, but it's here just in case someone
     * really needs to do something special.
     */
    public static VelocityEngine getEngine() {
        return velocityEngine;
    }

    /**
     * Convenience static method for retrieving a Velocity template.
     *
     * @throws org.apache.velocity.exception.ResourceNotFoundException, org.apache.velocity.exception.ParseErrorException
     */
    public static Template getTemplate(String name, DeviceType deviceType)
            throws ResourceNotFoundException, ParseErrorException {
        return velocityEngine.getTemplate(name + "|" + deviceType, "UTF-8");
    }
}
