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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.cache.CacheManager;

import java.util.Collections;
import java.util.List;

/**
 * Remove a weblog entry.
 */
public class EntryRemove extends EntryBase {

	private static Log log = LogFactory.getLog(EntryRemove.class);

	// id of entry to remove
	private String removeId = null;

	// entry object to remove
	private WeblogEntry removeEntry = null;

	public EntryRemove() {
		this.actionName = "entryRemove";
		this.desiredMenu = "editor";
		this.pageTitle = "weblogEdit.title.newEntry";
	}

	public void myPrepare() {
		if (getRemoveId() != null) {
			try {
				WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
						.getWeblogEntryManager();
				setRemoveEntry(wmgr.getWeblogEntry(getRemoveId()));
			} catch (WebloggerException ex) {
				log.error("Error looking up entry by id - " + getRemoveId(), ex);
			}
		}
	}

	public String execute() {
		return INPUT;
	}

	public String remove() {

		if (getRemoveEntry() != null) {
			try {

				WeblogEntry entry = getRemoveEntry();

				try {
					// remove the entry from the search index
					// TODO: can we do this in a better way?
					String originalStatus = entry.getStatus();
					entry.setStatus(WeblogEntry.DRAFT);
					IndexManager manager = WebloggerFactory.getWeblogger()
							.getIndexManager();
					manager.addEntryReIndexOperation(entry);
					entry.setStatus(originalStatus);
				} catch (WebloggerException ex) {
					log.warn("Trouble triggering entry indexing", ex);
				}

				// remove from search index
				if (entry.isPublished()) {
					removeEntryIndex(entry);
				}

				// flush caches
				CacheManager.invalidate(entry);

				// remove entry itself
				WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
						.getWeblogEntryManager();
				wmgr.removeWeblogEntry(entry);
				WebloggerFactory.getWeblogger().flush();

				// note to user
				addMessage("weblogEdit.entryRemoved");

				return SUCCESS;

			} catch (Exception e) {
				log.error("Error removing entry " + getRemoveId(), e);
				// TODO: i18n
				addError("Error removing entry");
			}
        } else {
			addError("weblogEntry.notFound");
			return ERROR;
		}

		return INPUT;
	}

	public String getRemoveId() {
		return removeId;
	}

	public void setRemoveId(String removeId) {
		this.removeId = removeId;
	}

	public WeblogEntry getRemoveEntry() {
		return removeEntry;
	}

	public void setRemoveEntry(WeblogEntry removeEntry) {
		this.removeEntry = removeEntry;
	}

    // allow LIMITED users to delete their own draft/pending blog entries
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.EDIT_DRAFT);
    }

}
