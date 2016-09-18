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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A list view of entries of a weblog.
 */
public class Entries extends UIAction {

    private static Logger log = LoggerFactory.getLogger(Entries.class);

    private static DateTimeFormatter searchDateFormatter = DateTimeFormatter.ofPattern("M/d/yyyy");

    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

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
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.POST;
    }

    public String execute() {

        if (log.isDebugEnabled()) {
            log.debug("entries bean is ...\n" + getBean().toString());
        }

        List<WeblogEntry> entries;
        boolean hasMore = false;
        String status = getBean().getStatus();

        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(getActionWeblog());
        if (getBean().getStartDateString() != null) {
            LocalDate ld = LocalDate.parse(getBean().getStartDateString(), searchDateFormatter);
            wesc.setStartDate(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        if (getBean().getEndDateString() != null) {
            LocalDate ld = LocalDate.parse(getBean().getEndDateString(), searchDateFormatter).plusDays(1);
            wesc.setEndDate(ld.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        }
        if (!"".equals(getBean().getCategoryName())) {
            wesc.setCatName(getBean().getCategoryName());
        }
        wesc.setTags(getBean().getTags());
        wesc.setStatus("ALL".equals(status) ? null : WeblogEntry.PubStatus.valueOf(status));
        wesc.setText(getBean().getText());
        wesc.setSortBy(getBean().getSortBy());
        wesc.setOffset(getBean().getPage() * COUNT);
        wesc.setMaxResults(COUNT + 1);
        List<WeblogEntry> rawEntries = weblogEntryManager.getWeblogEntries(wesc);
        entries = new ArrayList<>();
        entries.addAll(rawEntries);
        if (entries.size() > 0) {
            log.debug("query found " + rawEntries.size() + " results");

            if (rawEntries.size() > COUNT) {
                entries.remove(entries.size() - 1);
                hasMore = true;
            }

            setFirstEntry(entries.get(0));
            setLastEntry(entries.get(entries.size() - 1));
        }

        // build entries pager
        String baseUrl = buildBaseUrl();
        setPager(new EntriesPager(baseUrl, getBean().getPage(), entries, hasMore));

        return LIST;
    }

    // use the action data to build a url representing this action, including query data
    private String buildBaseUrl() {

        Map<String, String> params = new HashMap<>();

        if (!StringUtils.isEmpty(getBean().getCategoryName())) {
            params.put("bean.categoryPath", getBean().getCategoryName());
        }
        if (!StringUtils.isEmpty(getBean().getTagsAsString())) {
            params.put("bean.tagsAsString", getBean().getTagsAsString());
        }
        if (!StringUtils.isEmpty(getBean().getText())) {
            params.put("bean.text", getBean().getText());
        }
        if (!StringUtils.isEmpty(getBean().getStartDateString())) {
            params.put("bean.startDateString", getBean().getStartDateString());
        }
        if (!StringUtils.isEmpty(getBean().getEndDateString())) {
            params.put("bean.endDateString", getBean().getEndDateString());
        }
        if (!StringUtils.isEmpty(getBean().getStatus())) {
            params.put("bean.status", getBean().getStatus());
        }
        if (getBean().getSortBy() != null) {
            params.put("bean.sortBy", getBean().getSortBy().toString());
        }

        return urlStrategy.getActionURL("entries", "/tb-ui/authoring", getActionWeblog(),
                params, false);
    }

    /**
     * Get the list of all categories for the action weblog, not including root.
     */
    public List<Pair<String, String>> getCategories() {
        List<Pair<String, String>> opts = new ArrayList<>();
        opts.add(Pair.of("", "(Any)"));
        for (WeblogCategory cat : getActionWeblog().getWeblogCategories()) {
            opts.add(Pair.of(cat.getName(), cat.getName()));
        }
        return opts;
    }

    public List<Pair<String, String>> getSortByOptions() {
        List<Pair<String, String>> opts = new ArrayList<>();
        opts.add(Pair.of(WeblogEntrySearchCriteria.SortBy.PUBLICATION_TIME.name(), getText("weblogEntryQuery.label.pubTime")));
        opts.add(Pair.of(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME.name(), getText("weblogEntryQuery.label.updateTime")));
        return opts;
    }

    public List<Pair<String, String>> getStatusOptions() {
        List<Pair<String, String>> opts = new ArrayList<>();
        opts.add(Pair.of("ALL", getText("weblogEntryQuery.label.allEntries")));
        opts.add(Pair.of("DRAFT", getText("weblogEntryQuery.label.draftOnly")));
        opts.add(Pair.of("PUBLISHED", getText("weblogEntryQuery.label.publishedOnly")));
        opts.add(Pair.of("PENDING", getText("weblogEntryQuery.label.pendingOnly")));
        opts.add(Pair.of("SCHEDULED", getText("weblogEntryQuery.label.scheduledOnly")));
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
