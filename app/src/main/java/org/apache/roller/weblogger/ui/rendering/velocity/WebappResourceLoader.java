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

import java.io.File;
import java.io.InputStream;

import javax.servlet.ServletContext;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads non-theme Velocity resources from the webapp (global Velocity macros
 * configured in velocity.properties as well as the feed templates, error
 * templates, etc.)
 * <p>
 * All paths requested should be relative to the WEB-INF/velocity folder, e.g.:
 * "templates/feeds/entries-atom.vm", "templates/error-page.vm".
 * <p>
 * To enable caching and cache refreshing the webapp.resource.loader.cache and
 * webapp.resource.loader.modificationCheckInterval properties need to be set in
 * the velocity.properties file ... auto-reloading of global macros requires the
 * webapp.resource.loader.cache property to be set to 'false'.
 */
public class WebappResourceLoader extends ResourceLoader {

    private static Logger logger = LoggerFactory.getLogger(WebappResourceLoader.class);

    // The root paths for templates (relative to webapp's root).
    protected String servletPath = "/WEB-INF/velocity/";
    protected ServletContext servletContext = null;

    /**
     * This is abstract in the base class, so we need it. <br>
     * NOTE: this expects that the ServletContext has already been placed in the
     * runtime's application attributes under its full class name (i.e.
     * "javax.servlet.ServletContext").
     *
     * @param configuration the {@link ExtendedProperties} associated with this resource loader
     */
    public void init(ExtendedProperties configuration) {
        logger.debug("WebappResourceLoader: initialization starting.");

        // get the ServletContext
        servletContext = WebloggerContext.getServletContext();

        if (logger.isDebugEnabled()) {
            logger.debug("Search directory for non-Theme Velocity files = {}", servletContext.getRealPath(servletPath));
        }

        logger.debug("WebappResourceLoader: initialization complete.");
    }

    /**
     * Get an InputStream so that the Runtime can build a template with it.
     *
     * @param name name of template to get
     * @return InputStream containing the template
     * @throws ResourceNotFoundException if template not found in classpath.
     */
    public InputStream getResourceStream(String name) {
        InputStream result = null;
        Exception exception = null;

        if (name == null || name.length() == 0) {
            throw new ResourceNotFoundException("WebappResourceLoader: No template name provided");
        }

        // names are <template>|<deviceType>
        // loading weblog.vm etc will not have the type so only check for one string being returned.
        String[] split = name.split("\\|", 2);
        if (split.length < 1) {
            throw new ResourceNotFoundException("Invalid key " + name);
        }

        String path = servletPath + split[0];
        try {
            result = servletContext.getResourceAsStream(path);
        } catch (Exception e) {
            logger.debug("WebappResourceLoader: Could not load " + path, e);
            exception = e;
        }

        // If we never found the template
        if (result == null) {
            String msg = "WebappResourceLoader: Resource '" + name + "' not found.";

            // convert to a general Velocity ResourceNotFoundException
            if (exception == null) {
                throw new ResourceNotFoundException(msg);
            } else {
                msg += "  Due to: " + exception;
                throw new ResourceNotFoundException(msg, exception);
            }
        }

        return result;
    }

    private File getCachedFile(String rootPath, String fileName) {
        // We do this when we cache a resource, so do it again to ensure a match
        while (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }

        // names are <template>|<deviceType>
        // loading weblog.vm etc will not have the type so only check for one.
        String[] split = fileName.split("\\|", 2);
        return new File(rootPath + servletPath, split[0]);
    }

    /**
     * Checks to see if a resource has been deleted, moved or modified. When
     * using the resource.loader.cache=true option
     *
     * @param resource - The resource to check for modification
     * @return boolean True if the resource has been modified
     */
    @Override
    public boolean isSourceModified(Resource resource) {
        String rootPath = servletContext.getRealPath("/");
        if (rootPath == null) {
            // RootPath is null if the servlet container cannot translate the
            // virtual path to a real path for any reason (such as when the
            // content is being made available from a .war archive)
            return false;
        }

        File cachedFile = getCachedFile(rootPath, resource.getName());
        return !cachedFile.exists() || (cachedFile.lastModified() != resource.getLastModified());
    }

    @Override
    public long getLastModified(Resource resource) {
        String rootPath = servletContext.getRealPath("/");
        if (rootPath == null) {
            // RootPath is null if the servlet container cannot translate the
            // virtual path to a real path for any reason (such as when the
            // content is being made available from a .war archive)
            return 0;
        }

        File cachedFile = getCachedFile(rootPath, resource.getName());
        if (cachedFile.canRead()) {
            return cachedFile.lastModified();
        } else {
            return 0;
        }
    }
}
