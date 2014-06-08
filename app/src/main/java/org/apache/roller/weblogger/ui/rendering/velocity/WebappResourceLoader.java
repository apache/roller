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

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.apache.commons.collections.ExtendedProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;

/**
 * Loads Velocity resources from the webapp.
 * 
 * All resource urls begin from the root of the webapp. If a resource path is
 * relative (does not begin with a /) then it is prefixed with the path
 * /WEB-INF/velocity/, which is where Roller keeps its velocity files.
 * 
 * Resource loader that uses the ServletContext of a webapp to load Velocity
 * templates. (it's much easier to use with servlets than the standard
 * FileResourceLoader, in particular the use of war files is transparent).
 * 
 * The default search path is '/' (relative to the webapp root), but you can
 * change this behaviour by specifying one or more paths by mean of as many
 * webapp.resource.loader.path properties as needed in the velocity.properties
 * file.
 * 
 * All paths must be relative to the root of the webapp.
 * 
 * To enable caching and cache refreshing the webapp.resource.loader.cache and
 * webapp.resource.loader.modificationCheckInterval properties need to be set in
 * the velocity.properties file ... auto-reloading of global macros requires the
 * webapp.resource.loader.cache property to be set to 'false'.
 * 
 */
public class WebappResourceLoader extends ResourceLoader {

	private static Log log = LogFactory.getLog(WebappResourceLoader.class);

	// The root paths for templates (relative to webapp's root).
	protected String[] paths = null;
	protected Map<String, String> templatePaths = null;
	protected ServletContext servletContext = null;

	/**
	 * This is abstract in the base class, so we need it. <br>
	 * NOTE: this expects that the ServletContext has already been placed in the
	 * runtime's application attributes under its full class name (i.e.
	 * "javax.servlet.ServletContext").
	 * 
	 * @param configuration
	 *            the {@link ExtendedProperties} associated with this resource
	 *            loader.
	 */
	public void init(ExtendedProperties configuration) {

		if (log.isDebugEnabled()) {
			log.debug("WebappResourceLoader: initialization starting.");
        }

		// get configured paths
		paths = configuration.getStringArray("path");
		if (paths == null || paths.length == 0) {
			paths = new String[1];
			paths[0] = "/";
		} else {
			// make sure the paths end with a '/'
			for (int i = 0; i < paths.length; i++) {
				if (!paths[i].endsWith("/")) {
					paths[i] += '/';
				}
				if (log.isDebugEnabled()) {
                    log.debug("WebappResourceLoader: added template path - '"
                            + paths[i] + "'");
                }
			}
		}

		// get the ServletContext
		servletContext = RollerContext.getServletContext();

		if (log.isDebugEnabled()) {
			log.debug("Servlet Context = "
					+ servletContext.getRealPath("/WEB-INF/velocity/"));
        }

		// init the template paths map
		templatePaths = new HashMap<String, String>();

		if (log.isDebugEnabled()) {
            log.debug("WebappResourceLoader: initialization complete.");
        }
	}

	/**
	 * Get an InputStream so that the Runtime can build a template with it.
	 * 
	 * @param name
	 *            name of template to get
	 * @return InputStream containing the template
	 * @throws ResourceNotFoundException if template not found in classpath.
	 *
	 */
	public InputStream getResourceStream(String name) {

		InputStream result = null;
		Exception exception = null;

		if (name == null || name.length() == 0) {
			throw new ResourceNotFoundException(
					"WebappResourceLoader: No template name provided");
		}

		// names are <template>|<deviceType>
		// loading weblog.vm etc will not have the type so only check for
		// one.
		String[] split = name.split("\\|", 2);
		if (split.length < 1) {
			throw new ResourceNotFoundException("Invalid ThemeRL key " + name);
		}

		String savedPath = templatePaths.get(name);
		if (savedPath != null) {
			result = servletContext.getResourceAsStream(savedPath + split[0]);
		}

		if (result == null) {
            for (String pathSegment : paths) {
				String path = pathSegment + split[0];
				try {
					result = servletContext.getResourceAsStream(path);

					// save the path and exit the loop if we found the template
					if (result != null) {
						templatePaths.put(name, pathSegment);
						break;
					}
				} catch (NullPointerException npe) {
					// no servletContext was set, whine about it!
					throw npe;
				} catch (Exception e) {
					// only save the first one for later throwing
					if (exception == null) {
						if (log.isDebugEnabled()) {
							log.debug("WebappResourceLoader: Could not load "
									+ path, e);
						}
						exception = e;
					}
				}
			}
		}

		// If we never found the template
		if (result == null) {
			String msg = "WebappResourceLoader: Resource '" + name
					+ "' not found.";

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

	/**
	 * Gets the cached file.
	 * 
	 * @param rootPath
	 *            the root path
	 * @param fileName
	 *            the file name
	 * 
	 * @return the cached file
	 */
	private File getCachedFile(String rootPath, String fileName) {

		// We do this when we cache a resource, so do it again to ensure a match
		while (fileName.startsWith("/")) {
			fileName = fileName.substring(1);
		}

		String savedPath = templatePaths.get(fileName);

		// names are <template>|<deviceType>
		// loading weblog.vm etc will not have the type so only check for
		// one.
		String[] split = fileName.split("\\|", 2);
		return new File(rootPath + savedPath, split[0]);

	}

	/**
	 * Checks to see if a resource has been deleted, moved or modified. When
	 * using the resource.loader.cache=true option
	 * 
	 * @param resource
	 *            Resource The resource to check for modification
	 * 
	 * @return boolean True if the resource has been modified
	 */
	public boolean isSourceModified(Resource resource) {

		String rootPath = servletContext.getRealPath("/");
		if (rootPath == null) {
			// RootPath is null if the servlet container cannot translate the
			// virtual path to a real path for any reason (such as when the
			// content is being made available from a .war archive)
			return false;
		}

		// first, try getting the previously found file
		String fileName = resource.getName();
		File cachedFile = getCachedFile(rootPath, fileName);
		if (!cachedFile.exists()) {
			// then the source has been moved and/or deleted
			return true;
		}

		/*
		 * Check to see if the file can now be found elsewhere before it is
		 * found in the previously saved path
		 */
		File currentFile = null;
		for (String path : paths) {
			currentFile = new File(rootPath + path, fileName);
			if (currentFile.canRead()) {
				/*
				 * stop at the first resource found (just like in
				 * getResourceStream())
				 */
				break;
			}
		}

		// If the current is the cached and it is readable
		if (cachedFile.equals(currentFile) && cachedFile.canRead()) {
			// then (and only then) do we compare the last modified values
			return (cachedFile.lastModified() != resource.getLastModified());
		} else {
			// We found a new file for the resource or the resource is no longer
			// readable.
			return true;
		}
	}

	/**
	 * Checks to see when a resource was last modified
	 * 
	 * @param resource
	 *            Resource the resource to check
	 * 
	 * @return long The time when the resource was last modified or 0 if the
	 *         file can't be read
	 */
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