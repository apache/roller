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

package org.apache.roller.weblogger.business.themes;

import java.util.List;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.InitializationException;
import org.apache.roller.weblogger.pojos.WeblogTheme;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Manager interface for accessing Theme related objects.
 */
public interface ThemeManager {

	/**
	 * Initialize the theme system.
	 * 
	 * @throws InitializationException
	 *             If there is a problem during initialization.
	 */
	public void initialize() throws InitializationException;

	/**
	 * Get the Theme object with the given id.
	 * 
	 * @return Theme The Theme object with the given id.
	 * @throws ThemeNotFoundException
	 *             If the named theme cannot be found.
	 * @throws WebloggerException
	 *             If there is some kind of fatal backend error.
	 **/
	public SharedTheme getTheme(String id) throws ThemeNotFoundException,
			WebloggerException;

	/**
	 * Get the WeblogTheme for a given weblog.
	 * 
	 * @param weblog
	 *            The weblog to get the theme for.
	 * @return WeblogTheme The theme to be used for the given weblog.
	 * @throws WebloggerException
	 *             If there is some kind of fatal backend error.
	 */
	public WeblogTheme getTheme(Weblog weblog) throws WebloggerException;

	/**
	 * Get a list of all theme names that are currently enabled. This list is
	 * ordered alphabetically by default.
	 * 
	 * @return List A list of Theme objects which are enabled.
	 */
	public List getEnabledThemesList();

	/**
	 * Import all the contents for a Theme into a weblog.
	 * 
	 * @param weblog
	 *            The weblog to import the theme into.
	 * @param theme
	 *            The theme that should be imported.
	 * 
	 * @throws WebloggerException
	 *             If there is some kind of error in saving.
	 */
	public void importTheme(Weblog website, SharedTheme theme)
			throws WebloggerException;

	/**
	 * This is a development only method which reloads theme data from themes
	 * stored on the filesystem in the webapp /themes/ directory.
	 * 
	 * @param reloadTheme
	 *            the reload theme
	 * 
	 * @return true, if reloaded theme from disk
	 */
	public boolean reLoadThemeFromDisk(String reloadTheme);

}
