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

import org.tightblog.pojos.TemplateRendition;
import org.tightblog.pojos.Template;
import org.tightblog.rendering.Renderer;
import org.tightblog.rendering.RendererFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mobile.device.DeviceType;

/**
 * RendererFactory for Velocity, creates VelocityRenderers.
 */
public class VelocityRendererFactory implements RendererFactory {
    private static Logger log = LoggerFactory.getLogger(VelocityRendererFactory.class);

    public Renderer getRenderer(Template template, DeviceType deviceType) {
        Renderer renderer = null;
        TemplateRendition tr;

        if (template == null || template.getId() == null) {
            return null;
        }

        tr = template.getTemplateRendition(TemplateRendition.RenditionType.valueOf(deviceType.name()));

        // fallback to normal if template for requested device type not available
        if (tr == null && !DeviceType.NORMAL.equals(deviceType)) {
            tr = template.getTemplateRendition(TemplateRendition.RenditionType.NORMAL);
        }

        // nothing we can do with null values
        if (tr == null) {
            return null;
        }

        // VelocityRenderer handles Velocity templates only
        if (TemplateRendition.Parser.VELOCITY.equals(tr.getParser())) {
            try {
                renderer = new VelocityRenderer(template, deviceType);
            } catch (Exception ex) {
                log.error("ERROR creating VelocityRenderer", ex);
                return null;
            }
        }
        return renderer;
    }
}
