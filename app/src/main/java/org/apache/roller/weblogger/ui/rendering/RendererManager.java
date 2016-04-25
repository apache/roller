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
package org.apache.roller.weblogger.ui.rendering;

import org.apache.roller.weblogger.WebloggerException;

import java.util.HashSet;
import java.util.Set;

import org.apache.roller.weblogger.pojos.Template;
import org.springframework.mobile.device.DeviceType;

/**
 * Returns Renderer for Template via configured RendererFactories.
 * <p/>
 * The purpose of the RendererManager is to provide a level of abstraction
 * between classes that are rendering content and the implementations of the
 * rendering technology.  This allows us to provide easily pluggable rendering
 * implementations.
 */
public final class RendererManager {

    // a set of all renderer factories we are consulting
    private Set<RendererFactory> rendererFactories = new HashSet<>();

    public void setRendererFactories(Set<RendererFactory> rendererFactories) {
        this.rendererFactories = rendererFactories;
    }

    /**
     * Find the appropriate Renderer for the given content.
     * <p/>
     * This method checks all renderer factories configured for the Roller
     * instance and tries to find a Renderer for the content.  If no Renderer
     * can be found then we throw an exception.
     */
    public Renderer getRenderer(Template template, DeviceType deviceType)
            throws WebloggerException {

        Renderer renderer;

        // iterate over our renderer factories and see if one of them wants to handle this content
        for (RendererFactory rendererFactory : rendererFactories) {
            renderer = rendererFactory.getRenderer(template, deviceType);
            if (renderer != null) {
                return renderer;
            }
        }

        throw new WebloggerException("No renderer found for template " + template.getId() + "!");
    }
}
