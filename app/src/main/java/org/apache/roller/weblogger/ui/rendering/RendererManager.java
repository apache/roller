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
package org.apache.roller.weblogger.ui.rendering;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.Template;

import java.util.HashSet;
import java.util.Set;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;

/**
 * Returns Renderer for Template via configured RendererFactories.
 * <p/>
 * The purpose of the RendererManager is to provide a level of abstraction
 * between classes that are rendering content and the implementations of the
 * rendering technology.  This allows us to provide easily pluggable rendering
 * implementations.
 */
public class RendererManager {

    private static Log log = LogFactory.getLog(RendererManager.class);
    // a set of all renderer factories we are consulting
    private static Set<RendererFactory> rendererFactories = new HashSet();

    static {
        // lookup set of renderer factories we are going to use
        String rollerFactories = WebloggerConfig.getProperty("rendering.rollerRendererFactories");
        String userFactories = WebloggerConfig.getProperty("rendering.userRendererFactories");

        // instantiate user defined renderer factory classes
        if (userFactories != null && userFactories.trim().length() > 0) {

            RendererFactory rendererFactory = null;
            String[] uFactories = userFactories.split(",");
            for (int i = 0; i < uFactories.length; i++) {
                try {
                    Class factoryClass = Class.forName(uFactories[i]);
                    rendererFactory = (RendererFactory) factoryClass.newInstance();
                    rendererFactories.add(rendererFactory);
                } catch (ClassCastException cce) {
                    log.error("It appears that your factory does not implement "
                            + "the RendererFactory interface", cce);
                } catch (Exception e) {
                    log.error("Unable to instantiate renderer factory [" + uFactories[i] + "]", e);
                }
            }
        }

        // instantiate roller standard renderer factory classes
        if (rollerFactories != null && rollerFactories.trim().length() > 0) {

            RendererFactory rendererFactory = null;
            String[] rFactories = rollerFactories.split(",");
            for (int i = 0; i < rFactories.length; i++) {
                try {
                    Class factoryClass = Class.forName(rFactories[i]);
                    rendererFactory = (RendererFactory) factoryClass.newInstance();
                    rendererFactories.add(rendererFactory);
                } catch (ClassCastException cce) {
                    log.error("It appears that your factory does not implement "
                            + "the RendererFactory interface", cce);
                } catch (Exception e) {
                    log.error("Unable to instantiate renderer factory [" + rFactories[i] + "]", e);
                }
            }
        }

        if (rendererFactories.size() < 1) {
            // hmm ... failed to load any renderer factories?
            log.warn("Failed to load any renderer factories.  "
                    + "Rendering probably won't function as you expect.");
        }

        log.info("Renderer Manager Initialized.");
    }

    // this class is non-instantiable
    private RendererManager() {
    }

    /**
     * Find the appropriate Renderer for the given content.
     * <p/>
     * This method checks all renderer factories configured for the Roller
     * instance and tries to find a Renderer for the content.  If no Renderer
     * can be found then we throw an exception.
     */
    public static Renderer getRenderer(Template template, MobileDeviceRepository.DeviceType deviceType)
            throws RenderingException {

        Renderer renderer = null;

        // iterate over our renderer factories and see if one of them
        // wants to handle this content
        for (RendererFactory rendererFactory : rendererFactories) {
            renderer = rendererFactory.getRenderer(template, deviceType);
            if (renderer != null) {
                return renderer;
            }
        }

        throw new RenderingException("No renderer found for template "
                + template.getId() + "!");
    }
}
