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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import org.apache.commons.collections.ExtendedProperties;
import org.apache.roller.weblogger.business.themes.SharedTheme;
import org.apache.roller.weblogger.pojos.TemplateRendition.RenditionType;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.themes.ThemeManager;
import org.apache.roller.weblogger.pojos.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ThemeResourceLoader is a Velocity template loader which loads templates
 * from shared themes.  See RollerVelocity javadoc for more information.
 * 
 * @author Allen Gilliland
 */
public class ThemeResourceLoader extends ResourceLoader {

    private static Logger logger = LoggerFactory.getLogger(ThemeResourceLoader.class);

    public void init(ExtendedProperties configuration) {
        if (logger.isDebugEnabled()) {
            logger.debug(configuration.toString());
        }
    }

    /**
     * @throws ResourceNotFoundException
     */
    public InputStream getResourceStream(String name) {

        if (log.isDebugEnabled()) {
            logger.debug("Looking for: {}", name);
        }

        if (name == null || name.length() < 1) {
            throw new ResourceNotFoundException("Need to specify a template name!");
        }

        RenditionType renditionType = RenditionType.NORMAL;
        if (name.contains("|")) {
            String[] pair = name.split("\\|");
            name = pair[0];
            renditionType = RenditionType.valueOf(pair[1].toUpperCase());
        }

        try {
            // parse the name ... shared theme template names are
            // <theme>:<template>|<deviceType>
            // where theme:template is defined via ThemeManagerImpl.loadThemeData().
            String[] split = name.split(":", 2);
            if (split.length < 2) {
                throw new ResourceNotFoundException("Invalid ThemeRL key " + name);
            }

            // lookup the template from the proper theme
            ThemeManager themeMgr = WebloggerFactory.getWeblogger().getThemeManager();
            SharedTheme theme = themeMgr.getSharedTheme(split[0]);
            Template template = theme.getTemplateByName(split[1]);

            if (template == null) {
                throw new ResourceNotFoundException("Template [" + split[1]
                        + "] doesn't seem to be part of theme [" + split[0] + "]");
            }

            final String contents;

            if (template.getTemplateRendition(renditionType) != null) {
                contents = template.getTemplateRendition(renditionType).getRendition();
            } else if (renditionType != RenditionType.NORMAL
                    && template.getTemplateRendition(RenditionType.NORMAL) != null) {
                // fall back to standard rendition type if others not defined
                contents = template.getTemplateRendition(RenditionType.NORMAL).getRendition();
            } else {
                throw new ResourceNotFoundException("Rendering [" + renditionType.name()
                        + "] of Template [" + split[1] + "] not found.");
            }

            logger.debug("Resource found!");

            // return the input stream
            return new ByteArrayInputStream(contents.getBytes("UTF-8"));

        } catch (UnsupportedEncodingException uex) {
            // We expect UTF-8 in all JRE installation.
            // This rethrows as a Runtime exception after logging.
            logger.error("exception", uex);
            throw new RuntimeException(uex);
        }
    }

    /**
     * Files loaded by this resource loader are not reloadable here, as they are
     * stored in shared themes and there is no way velocity can trigger a
     * reload.
     * 
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#isSourceModified(org.apache.velocity.runtime.resource.Resource)
     */
    public boolean isSourceModified(Resource resource) {
        return false;
    }

    /**
     * @see org.apache.velocity.runtime.resource.loader.ResourceLoader#getLastModified(org.apache.velocity.runtime.resource.Resource)
     */
    public long getLastModified(Resource resource) {
        return 0;
    }

}
