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

package org.apache.roller.weblogger.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Remove a weblog entry.
 */
public class EntryRemove extends UIAction {

    private static Log log = LogFactory.getLog(EntryRemove.class);

    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    // id of entry to remove
    private String removeId = null;

    // entry object to remove
    private WeblogEntry removeEntry = null;

    public EntryRemove() {
        // actionName defined in struts.xml as it's different based on the caller
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEdit.deleteEntry";
    }

    public void myPrepare() {
        if (getRemoveId() != null) {
            try {
                setRemoveEntry(weblogEntryManager.getWeblogEntry(getRemoveId()));
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

                // remove from search index
                if (entry.isPublished()) {
                    indexManager.removeEntryIndexOperation(entry);
                }

                // flush caches
                CacheManager.invalidate(entry);

                // remove entry itself
                weblogEntryManager.removeWeblogEntry(entry);
                WebloggerFactory.flush();

                // note to user
                addMessage("weblogEdit.entryRemoved", entry.getTitle());

                return SUCCESS;

            } catch (Exception e) {
                log.error("Error removing entry " + getRemoveId(), e);
                addError("generic.error.check.logs");
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

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    // will allow drafters to delete just their own draft/pending blog entries.
    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.EDIT_DRAFT;
    }

}
