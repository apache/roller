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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * A list view of entries in a weblog.
 */
public class Entries extends UIAction {
    
    private static Log log = LogFactory.getLog(Entries.class);
    
    // bean for managing submitted data
    private EntriesBean bean = new EntriesBean();
    
    // list of entries to display
    private List<WeblogEntry> entries = Collections.EMPTY_LIST;
    
    // first entry in the list
    private WeblogEntry firstEntry = null;
    
    // last entry in the list
    private WeblogEntry lastEntry = null;
    
    // are there more results for the query?
    private boolean moreResults = false;
    
    
    public Entries() {
        this.actionName = "entries";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEntryQuery.title";
    }
    
    
    @Override
    public short requiredWeblogPermissions() {
        return WeblogPermission.AUTHOR;
    }
    
    
    public String execute() {
        
        if(log.isDebugEnabled()) {
            log.debug("entries bean is ...\n"+getBean().toString());
        }
        
        try {
            String status = getBean().getStatus();
            
            WeblogManager wmgr = RollerFactory.getRoller().getWeblogManager();
            List<WeblogEntry> entries = wmgr.getWeblogEntries(
                    getActionWeblog(),
                    null,
                    getBean().getStartDate(),
                    getBean().getEndDate(),
                    getBean().getCategoryPath(),
                    getBean().getTags(),
                    ("ALL".equals(status)) ? null : status,
                    getBean().getText(),
                    getBean().getSortBy(),
                    null,
                    null,
                    getBean().getOffset(),
                    getBean().getCount() + 1);
            
            if(entries != null && entries.size() > 0) {
                log.debug("query found "+entries.size()+" results");
                
                if(entries.size() > getBean().getCount()) {
                    entries.remove(entries.size()-1);
                    setMoreResults(true);
                }
                
                setEntries(entries);
                setFirstEntry((WeblogEntry)entries.get(0));
                setLastEntry((WeblogEntry)entries.get(entries.size()-1));
            }
        } catch (RollerException ex) {
            log.error("Error looking up entries", ex);
            // TODO: i18n
            addError("Error looking up entries");
        }
        
        return LIST;
    }
    
    
    public List<KeyValueObject> getSortByOptions() {
        List<KeyValueObject> opts = new ArrayList();
        
        opts.add(new KeyValueObject("pubTime", getText("weblogEntryQuery.label.pubTime")));
        opts.add(new KeyValueObject("updateTime", getText("weblogEntryQuery.label.updateTime")));
        
        return opts;
    }
    
    public List<KeyValueObject> getStatusOptions() {
        List<KeyValueObject> opts = new ArrayList();
        
        opts.add(new KeyValueObject("ALL", getText("weblogEntryQuery.label.allEntries")));
        opts.add(new KeyValueObject("DRAFT", getText("weblogEntryQuery.label.draftOnly")));
        opts.add(new KeyValueObject("PUBLISHED", getText("weblogEntryQuery.label.publishedOnly")));
        opts.add(new KeyValueObject("PENDING", getText("weblogEntryQuery.label.pendingOnly")));
        opts.add(new KeyValueObject("SCHEDULED", getText("weblogEntryQuery.label.scheduledOnly")));
        
        return opts;
    }
    
    
    public EntriesBean getBean() {
        return bean;
    }

    public void setBean(EntriesBean bean) {
        this.bean = bean;
    }

    public List<WeblogEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<WeblogEntry> entries) {
        this.entries = entries;
    }

    public WeblogEntry getFirstEntry() {
        return firstEntry;
    }

    public void setFirstEntry(WeblogEntry firstEntry) {
        this.firstEntry = firstEntry;
    }

    public WeblogEntry getLastEntry() {
        return lastEntry;
    }

    public void setLastEntry(WeblogEntry lastEntry) {
        this.lastEntry = lastEntry;
    }

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
    }
    
}
