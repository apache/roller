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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.pagers.EntriesPager;
import org.apache.roller.weblogger.ui.struts2.util.KeyValueObject;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * A list view of entries in a weblog.
 */
public class Entries extends UIAction {
    
    private static Log log = LogFactory.getLog(Entries.class);
    
    // number of comments to show per page
    private static final int COUNT = 30;
    
    // bean for managing submitted data
    private EntriesBean bean = new EntriesBean();
    
    // pager for the entries we are viewing
    private EntriesPager pager = null;
    
    // first entry in the list
    private WeblogEntry firstEntry = null;
    
    // last entry in the list
    private WeblogEntry lastEntry = null;
    
    
    public Entries() {
        this.actionName = "entries";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEntryQuery.title";
    }
    
    
    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }
    
    
    public String execute() {
        
        if (log.isDebugEnabled()) {
            log.debug("entries bean is ...\n"+getBean().toString());
        }
        
        List<WeblogEntry> entries = null;
        boolean hasMore = false;
        try {
            String status = getBean().getStatus();
            
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(getActionWeblog());
            wesc.setStartDate(getBean().getStartDate());
            wesc.setEndDate(getBean().getEndDate());
            wesc.setCatName(getBean().getCategoryName());
            wesc.setTags(getBean().getTags());
            wesc.setStatus("ALL".equals(status) ? null : status);
            wesc.setText(getBean().getText());
            wesc.setSortBy(getBean().getSortBy());
            wesc.setOffset(getBean().getPage() * COUNT);
            wesc.setMaxResults(COUNT + 1);
            List<WeblogEntry> rawEntries = wmgr.getWeblogEntries(wesc);
            entries = new ArrayList<WeblogEntry>();
            entries.addAll(rawEntries);
            if (entries.size() > 0) {
                log.debug("query found "+rawEntries.size()+" results");
                
                if(rawEntries.size() > COUNT) {
                    entries.remove(entries.size()-1);
                    hasMore = true;
                }
                
                setFirstEntry(entries.get(0));
                setLastEntry(entries.get(entries.size()-1));
            }
        } catch (WebloggerException ex) {
            log.error("Error looking up entries", ex);
            // TODO: i18n
            addError("Error looking up entries");
        }
        
        // build entries pager
        String baseUrl = buildBaseUrl();
        setPager(new EntriesPager(baseUrl, getBean().getPage(), entries, hasMore));
                
        return LIST;
    }
    
    
    // use the action data to build a url representing this action, including query data
    private String buildBaseUrl() {
        
        Map<String, String> params = new HashMap<String, String>();
        
        if(!StringUtils.isEmpty(getBean().getCategoryName())) {
            params.put("bean.categoryPath", getBean().getCategoryName());
        }
        if(!StringUtils.isEmpty(getBean().getTagsAsString())) {
            params.put("bean.tagsAsString", getBean().getTagsAsString());
        }
        if(!StringUtils.isEmpty(getBean().getText())) {
            params.put("bean.text", getBean().getText());
        }
        if(!StringUtils.isEmpty(getBean().getStartDateString())) {
            params.put("bean.startDateString", getBean().getStartDateString());
        }
        if(!StringUtils.isEmpty(getBean().getEndDateString())) {
            params.put("bean.endDateString", getBean().getEndDateString());
        }
        if(!StringUtils.isEmpty(getBean().getStatus())) {
            params.put("bean.status", getBean().getStatus());
        }
        if(getBean().getSortBy() != null) {
            params.put("bean.sortBy", getBean().getSortBy().toString());
        }

        return WebloggerFactory.getWeblogger().getUrlStrategy().getActionURL("entries", "/roller-ui/authoring", 
                getActionWeblog().getHandle(), params, false);
    }
    
    
    /**
     * Get the list of all categories for the action weblog, not including root.
     */
    public List<WeblogCategory> getCategories() {
        // make list of categories with first option being being a transient
        // category just meant to represent the default option of any category
        List<WeblogCategory> cats = new ArrayList<WeblogCategory>();
        
        WeblogCategory tmpCat = new WeblogCategory();
        tmpCat.setName("Any");
        cats.add(tmpCat);
        
        List<WeblogCategory> weblogCats = Collections.emptyList();
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            weblogCats = wmgr.getWeblogCategories(getActionWeblog());
        } catch (WebloggerException ex) {
            log.error("Error getting category list for weblog - "+getWeblog(), ex);
        }
        
        cats.addAll(weblogCats);
        
        return cats;
    }
    
    
    public List<KeyValueObject> getSortByOptions() {
        List<KeyValueObject> opts = new ArrayList<KeyValueObject>();
        
        opts.add(new KeyValueObject(WeblogEntrySearchCriteria.SortBy.PUBLICATION_TIME.toString(), getText("weblogEntryQuery.label.pubTime")));
        opts.add(new KeyValueObject(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME.toString(), getText("weblogEntryQuery.label.updateTime")));
        
        return opts;
    }
    
    public List<KeyValueObject> getStatusOptions() {
        List<KeyValueObject> opts = new ArrayList<KeyValueObject>();
        
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

    public EntriesPager getPager() {
        return pager;
    }

    public void setPager(EntriesPager pager) {
        this.pager = pager;
    }
    
}
