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

package org.apache.roller.ui.struts2.editor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.WeblogManager;
import org.apache.roller.business.search.IndexManager;
import org.apache.roller.pojos.WeblogEntryData;
import org.apache.roller.ui.struts2.util.UIAction;
import org.apache.roller.util.cache.CacheManager;


/**
 * Remove a weblog entry.
 */
public class EntryRemove extends UIAction {
    
    private static Log log = LogFactory.getLog(EntryRemove.class);
    
    // id of entry to remove
    private String removeId = null;
    
    // entry object to remove
    private WeblogEntryData removeEntry = null;
    
    
    public EntryRemove() {
        this.actionName = "entryRemove";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEdit.title.newEntry";
    }
    
    
    public void myPrepare() {
        if(getRemoveId() != null) {
            try {
                WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
                setRemoveEntry(wmgr.getWeblogEntry(getRemoveId()));
            } catch (RollerException ex) {
                log.error("Error looking up entry by id - "+getRemoveId(), ex);
            }
        }
    }
    
    
    public String execute() {
        return INPUT;
    }
    
    
    public String remove() {
        
        if(getRemoveEntry() != null) try {
            
            WeblogEntryData entry = getRemoveEntry();
            
            try {
                // remove the entry from the search index
                // TODO: can we do this in a better way?
                entry.setStatus(WeblogEntryData.DRAFT);
                IndexManager manager = RollerFactory.getRoller().getIndexManager();
                manager.addEntryReIndexOperation(entry);
            } catch (RollerException ex) {
                log.warn("Trouble triggering entry indexing", ex);
            }
            
            // remove entry itself
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            wmgr.removeWeblogEntry(entry);
            RollerFactory.getRoller().flush();
            
            // flush caches
            CacheManager.invalidate(entry);
            
            // note to user
            addMessage("weblogEdit.entryRemoved");
            
            return SUCCESS;
            
        } catch(Exception e) {
            log.error("Error removing entry "+getRemoveId(), e);
            // TODO: i18n
            addError("Error removing entry");
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

    public WeblogEntryData getRemoveEntry() {
        return removeEntry;
    }

    public void setRemoveEntry(WeblogEntryData removeEntry) {
        this.removeEntry = removeEntry;
    }
    
}
