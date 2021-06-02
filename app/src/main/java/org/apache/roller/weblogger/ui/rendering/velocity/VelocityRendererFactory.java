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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.pojos.Template;
import org.apache.roller.weblogger.pojos.TemplateRendition;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.roller.weblogger.pojos.TemplateRendition.TemplateLanguage;
import org.apache.roller.weblogger.ui.rendering.Renderer;
import org.apache.roller.weblogger.ui.rendering.RendererFactory;
import org.apache.roller.weblogger.ui.rendering.mobile.MobileDeviceRepository;
import org.apache.velocity.exception.ResourceNotFoundException;


/**
 * RendererFactory for Velocity, creates VelocityRenderers.
 */
public class VelocityRendererFactory implements RendererFactory {
    private static final Log log = LogFactory.getLog(VelocityRendererFactory.class);
    
    @Override
    public Renderer getRenderer(Template template, 
			MobileDeviceRepository.DeviceType deviceType) {

        // nothing we can do with null values
        if (template == null || template.getId() == null) {
            return null;
        }

        TemplateRendition tr;
        try {
            tr = template.getTemplateRendition(RenditionType.STANDARD);
            if (tr == null) {
                return null;
            }
        } catch (WebloggerException e) {
            return null;
        }

        Renderer renderer = null;
        
        if (TemplateLanguage.VELOCITY.equals(tr.getTemplateLanguage())) {
            // standard velocity template
            try {
               renderer = new VelocityRenderer(template, deviceType);
            } catch (ResourceNotFoundException ex) {
                // allready logged in VelocityRenderer
            } catch(Exception ex) {
                // some kind of exception so we don't have a renderer
				log.error("ERROR creating VelocityRenderer", ex);
            }            
        }
        return renderer;
    }
}
