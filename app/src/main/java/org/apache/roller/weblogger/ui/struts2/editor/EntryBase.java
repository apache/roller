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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.plugins.UIPluginManager;
import org.apache.roller.weblogger.ui.core.plugins.WeblogEntryEditor;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * A collection of base functionality used by entry actions.
 */
public abstract class EntryBase extends UIAction {

	private static Log log = LogFactory.getLog(EntryBase.class);

	/**
	 * Trigger reindexing of modified entry.
	 * 
	 * @param entry
	 *            the entry
	 */
	protected void reindexEntry(WeblogEntry entry) {
		IndexManager manager = WebloggerFactory.getWeblogger()
				.getIndexManager();
		try {
			manager.addEntryReIndexOperation(entry);
		} catch (WebloggerException ex) {
			log.warn("Trouble triggering entry indexing", ex);
		}
	}

	/**
	 * Trigger reindexing of modified entry.
	 */
	protected void removeEntryIndex(WeblogEntry entry) {
		IndexManager manager = WebloggerFactory.getWeblogger()
				.getIndexManager();
		try {
			manager.removeEntryIndexOperation(entry);
		} catch (WebloggerException ex) {
			log.warn("Trouble triggering entry indexing", ex);
		}

	}

	/**
	 * Get recent weblog entries using request parameters to determine username,
	 * date, and category name parameters.
	 * 
	 * @return List of WeblogEntryData objects.
	 */
	public List<WeblogEntry> getRecentPublishedEntries() {
		List<WeblogEntry> entries = Collections.emptyList();
		try {
			entries = WebloggerFactory.getWeblogger().getWeblogEntryManager()
					.getWeblogEntries(getActionWeblog(),
							null, null, // startDate
							null, // endDate
							null, // catName
							null, WeblogEntry.PUBLISHED,
							null, // text
							null, // sortby (null for pubTime)
							null, null, 0, 20);
		} catch (WebloggerException ex) {
			log.error("Error getting entries list", ex);
		}
		return entries;
	}

	/**
	 * Get recent weblog entries using request parameters to determine username,
	 * date, and category name parameters.
	 * 
	 * @return List of WeblogEntryData objects.
	 */
	public List<WeblogEntry> getRecentScheduledEntries() {
		List<WeblogEntry> entries = Collections.emptyList();
		try {
			entries = WebloggerFactory.getWeblogger().getWeblogEntryManager()
					.getWeblogEntries(getActionWeblog(),
							null, null, // startDate
							null, // endDate
							null, // catName
							null, WeblogEntry.SCHEDULED,
							null, // text
							null, // sortby (null for pubTime)
							null, null, 0, 20);
		} catch (WebloggerException ex) {
			log.error("Error getting entries list", ex);
		}
		return entries;
	}

	/**
	 * Get recent weblog entries using request parameters to determine username,
	 * date, and category name parameters.
	 * 
	 * @return List of WeblogEntryData objects.
	 */
	public List<WeblogEntry> getRecentDraftEntries() {
		List<WeblogEntry> entries = Collections.emptyList();
		try {
			entries = WebloggerFactory.getWeblogger().getWeblogEntryManager()
					.getWeblogEntries(getActionWeblog(), null, null,
							null, // endDate
							null, // catName
							null, WeblogEntry.DRAFT,
							null, // text
							"updateTime",
							null, null, 0, 20); // maxEntries
		} catch (WebloggerException ex) {
			log.error("Error getting entries list", ex);
		}
		return entries;
	}

	/**
	 * Get recent weblog entries using request parameters to determine username,
	 * date, and category name parameters.
	 * 
	 * @return List of WeblogEntryData objects.
	 */
	public List<WeblogEntry> getRecentPendingEntries() {
		List<WeblogEntry> entries = Collections.emptyList();
		try {
			entries = WebloggerFactory.getWeblogger().getWeblogEntryManager()
					.getWeblogEntries(getActionWeblog(), null, null,
							null, // endDate
							null, // catName
							null, WeblogEntry.PENDING,
							null, // text
							"updateTime",
							null, null, 0, 20);
		} catch (WebloggerException ex) {
			log.error("Error getting entries list", ex);
		}
		return entries;
	}

	public List<WeblogEntryPlugin> getEntryPlugins() {
		List<WeblogEntryPlugin> availablePlugins = Collections.emptyList();
		try {
			PluginManager ppmgr = WebloggerFactory.getWeblogger()
					.getPluginManager();
			Map<String, WeblogEntryPlugin> plugins = ppmgr
					.getWeblogEntryPlugins(getActionWeblog());

			if (plugins.size() > 0) {
				availablePlugins = new ArrayList<WeblogEntryPlugin>();
				for (WeblogEntryPlugin plugin : plugins.values()) {
					availablePlugins.add(plugin);
				}
			}
		} catch (Exception ex) {
			log.error("Error getting plugins list", ex);
		}
		return availablePlugins;
	}

	public WeblogEntryEditor getEditor() {
		UIPluginManager pmgr = RollerContext.getUIPluginManager();
		return pmgr.getWeblogEntryEditor(getActionWeblog().getEditorPage());
	}

	public boolean isUserAnAuthor() {
		return getActionWeblog().hasUserPermission(getAuthenticatedUser(),
				WeblogPermission.POST);
	}

	public String getJsonAutocompleteUrl() {
		return WebloggerFactory.getWeblogger().getUrlStrategy()
				.getWeblogTagsJsonURL(getActionWeblog(), false, 0);
	}

}
