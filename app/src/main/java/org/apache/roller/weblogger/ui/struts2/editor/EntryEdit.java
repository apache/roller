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
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.roller.weblogger.WebloggerCommon;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.util.MediacastException;
import org.apache.roller.weblogger.util.MediacastResource;
import org.apache.roller.weblogger.util.MediacastUtil;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Edit a new or existing entry.
 */
@RestController
public final class EntryEdit extends UIAction {

    private static Logger log = LoggerFactory.getLogger(EntryEdit.class);

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    private List<WeblogEntryPlugin> weblogEntryPlugins;

    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    // Max Tags to show for autocomplete
    private static final int MAX_TAGS = WebloggerStaticConfig.getIntProperty("services.tagdata.max", 20);

    // bean for managing form data
    private WeblogEntry bean = new WeblogEntry();

    // the entry we are adding or editing
    private WeblogEntry entry = null;

    public EntryEdit() {
        this.desiredMenu = "editor";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.EDIT_DRAFT;
    }

    public void prepare() {
        if (isAdd()) {
            // Create and initialize new, not-yet-saved Weblog Entry
            entry = new WeblogEntry();
            entry.setId(WebloggerCommon.generateUUID());
            entry.setCreatorId(getAuthenticatedUser().getId());
            entry.setWeblog(getActionWeblog());
        } else {
            // already saved entry
            setEntry(weblogEntryManager.getWeblogEntry(getBean().getId()));
        }
    }

    @SkipValidation
    public String removeViaList() {
        String result = removeCommon();
        return (SUCCESS.equals(result)) ? LIST : result;
    }

    @SkipValidation
    public String remove() {
        String result = removeCommon();
        return (SUCCESS.equals(result)) ? "deleted" : result;
    }

    private String removeCommon() {
        if (getEntry() != null) {
            try {
                WeblogEntry entry = getEntry();

                // remove from search index
                if (entry.isPublished()) {
                    indexManager.removeEntryIndexOperation(entry);
                }

                // flush caches
                cacheManager.invalidate(entry);

                // remove entry itself
                weblogEntryManager.removeWeblogEntry(entry);
                WebloggerFactory.flush();

                // note to user
                addMessage("weblogEdit.entryRemoved", entry.getTitle());

                return SUCCESS;

            } catch (Exception e) {
                log.error("Error removing entry {}", getEntry().getId(), e);
                addError("generic.error.check.logs");
            }
        } else {
            addError("weblogEntry.notFound");
            return ERROR;
        }

        return INPUT;
    }

    private boolean isAdd() {
        return actionName.equals("entryAdd");
    }

    /**
     * Show form for adding/editing weblog entry.
     *
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        if (isAdd()) {
            // set weblog defaults
            bean.setCommentDays(getActionWeblog().getDefaultCommentDays());
            // apply weblog default plugins
            if (getActionWeblog().getDefaultPlugins() != null) {
                bean.setPlugins(getActionWeblog().getDefaultPlugins());
            }
        } else {
            // load bean with pojo data
            bean.setId(entry.getId());
            bean.setTitle(entry.getTitle());
            bean.setStatus(entry.getStatus());
            bean.setText(entry.getText());
            bean.setSummary(entry.getSummary());
            bean.setNotes(entry.getNotes());
            bean.setCategoryId(entry.getCategoryId());
            String tagsAsString = String.join(" ", entry.getTags().stream().map(WeblogEntryTag::getName).collect(Collectors.toSet()));
            bean.setTagsAsString(tagsAsString);
            bean.setSearchDescription(entry.getSearchDescription());
            bean.setPlugins(entry.getPlugins());

            // init pubtime values
            if (entry.getPubTime() != null) {
                log.debug("entry pubtime is {}", entry.getPubTime());

                //Calendar cal = Calendar.getInstance(locale);
                ZonedDateTime zdt = entry.getPubTime().atZone(entry.getWeblog().getTimeZoneInstance().toZoneId());

                bean.setHours(zdt.getHour());
                bean.setMinutes(zdt.getMinute());
                bean.setSeconds(zdt.getSecond());
                DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yy");
                bean.setDateString(df.format(zdt.toLocalDate()));

                if (log.isDebugEnabled()) {
                    log.debug("pubtime vals are " + bean.getDateString() + ", " + bean.getHours() + ", "
                            + bean.getMinutes() + ", " + bean.getSeconds());
                }
            }

            bean.setCommentDays(entry.getCommentDays());
            bean.setEnclosureUrl(entry.getEnclosureUrl());
            bean.setEnclosureType(entry.getEnclosureType());
            bean.setEnclosureLength(entry.getEnclosureLength());
        }
        return INPUT;
    }

    /**
     * Save a draft entry.
     *
     * @return String The result of the action.
     */
    public String saveDraft() {
        getBean().setStatus(PubStatus.DRAFT);
        return save();
    }

    /**
     * Publish an entry.
     *
     * @return String The result of the action.
     */
    public String publish() {
        if (getActionWeblogRole().hasEffectiveRole(WeblogRole.POST)) {
            LocalDateTime pubTime = calculatePubTime();

            if (pubTime != null && pubTime.isAfter(LocalDateTime.now().plusMinutes(1))) {
                getBean().setStatus(PubStatus.SCHEDULED);
            } else {
                getBean().setStatus(PubStatus.PUBLISHED);
            }
        } else {
            getBean().setStatus(PubStatus.PENDING);
        }
        return save();
    }

    private LocalDateTime calculatePubTime() {
        LocalDateTime pubtime = null;

        String dateString = bean.getDateString();
        if(!StringUtils.isEmpty(dateString)) {
            try {
                // Don't require user add preceding '0' of month and day.
                DateTimeFormatter df = DateTimeFormatter.ofPattern("M/d/yy");
                LocalDate newDate = LocalDate.parse(dateString, df);

                // Now handle the time from the hour, minute and second combos
                pubtime = newDate.atTime(bean.getHours(), bean.getMinutes(), bean.getSeconds());
            } catch (Exception e) {
                log.error("Error calculating pubtime", e);
            }
        }

        return pubtime;
    }


    /**
     * Processing logic common for saving drafts and publishing entries
     *
     * @return String The result of the action.
     */
    private String save() {
        myValidate();

        if (!hasActionErrors()) {
            try {
                WeblogEntry weblogEntry = getEntry();

                // set updatetime & pubtime
                weblogEntry.setUpdateTime(LocalDateTime.now());
                weblogEntry.setPubTime(calculatePubTime());

                // copy data to pojo
                weblogEntry.setTitle(bean.getTitle().trim());
                weblogEntry.setStatus(bean.getStatus());
                weblogEntry.setText(bean.getText().trim());
                weblogEntry.setSummary(bean.getSummary().trim());
                weblogEntry.setNotes(bean.getNotes().trim());
                Set<String> updatedTags = Utilities.splitStringAsTags(bean.getTagsAsString().trim());
                weblogEntry.updateTags(updatedTags);
                weblogEntry.setSearchDescription(bean.getSearchDescription().trim());
                weblogEntry.setEnclosureUrl(bean.getEnclosureUrl().trim());
                weblogEntry.setEnclosureType(bean.getEnclosureType());
                weblogEntry.setEnclosureLength(bean.getEnclosureLength());
                weblogEntry.setCategory(weblogManager.getWeblogCategory(bean.getCategoryId()));

                // join values from all plugins into a single string
                weblogEntry.setPlugins(bean.getPlugins());

                // comment settings & right-to-left option
                weblogEntry.setCommentDays(bean.getCommentDays());

                // handle pubtime auto set
                if (weblogEntry.isPublished() && weblogEntry.getPubTime() == null) {
                    // no time specified, use current time
                    weblogEntry.setPubTime(weblogEntry.getUpdateTime());
                }

                if (!StringUtils.isEmpty(getBean().getEnclosureUrl())) {
                    try {
                        // Fetch MediaCast resource
                        log.debug("Checking MediaCast attributes");
                        MediacastResource mediacast = MediacastUtil.lookupResource(getBean().getEnclosureUrl());

                        // set mediacast attributes
                        weblogEntry.setEnclosureUrl(mediacast.getUrl());
                        weblogEntry.setEnclosureType(mediacast.getContentType());
                        weblogEntry.setEnclosureLength(mediacast.getLength());

                    } catch (MediacastException ex) {
                        addMessage(getText(ex.getErrorKey()));
                    }
                } else if ("entryEdit".equals(actionName)) {
                    // if MediaCast string is empty, clean out MediaCast attributes
                    weblogEntry.setEnclosureUrl(null);
                    weblogEntry.setEnclosureType(null);
                    weblogEntry.setEnclosureLength(null);
                }

                if (log.isDebugEnabled()) {
                    log.debug("entry bean is ...\n" + getBean().toString());
                    log.debug("final status = " + weblogEntry.getStatus());
                    log.debug("updtime = " + weblogEntry.getUpdateTime());
                    log.debug("pubtime = " + weblogEntry.getPubTime());
                }

                log.debug("Saving entry");
                weblogEntryManager.saveWeblogEntry(weblogEntry);
                WebloggerFactory.flush();

                // notify search of the new entry
                if (weblogEntry.isPublished()) {
                    indexManager.addEntryReIndexOperation(entry);
                } else if ("entryEdit".equals(actionName)) {
                    indexManager.removeEntryIndexOperation(entry);
                }

                // notify caches
                cacheManager.invalidate(weblogEntry);

                if (PubStatus.PENDING.equals(weblogEntry.getStatus()) && mailManager.isMailConfigured()) {
                    mailManager.sendPendingEntryNotice(weblogEntry);
                }
                if ("entryEdit".equals(actionName)) {
                    addStatusMessage(getEntry().getStatus());
                    // continue in entryEdit mode
                    return INPUT;
                } else {
                    // now that entry is saved we have an id value for it
                    // store it back in bean for use in next action
                    getBean().setId(weblogEntry.getId());
                    // flip over to entryEdit mode, as defined in struts.xml
                    return SUCCESS;
                }

            } catch (Exception e) {
                log.error("Error saving new entry", e);
                addError("generic.error.check.logs");
            }
        }
        if ("entryAdd".equals(actionName)) {
            // if here on entryAdd, nothing saved, so reset status to null (unsaved)
            getBean().setStatus(null);
        }
        return INPUT;
    }

    public void myValidate() {
        if (StringUtils.isEmpty(bean.getTitle())) {
            addError("Entry.error.titleNull");
        }
        if (StringUtils.isEmpty(bean.getCategoryId())) {
            addError("Entry.error.categoryNull");
        }
        if (StringUtils.isEmpty(bean.getText())) {
            addError("Entry.error.textNull");
        }
    }

    public WeblogEntry getBean() {
        return bean;
    }

    public void setBean(WeblogEntry bean) {
        this.bean = bean;
    }

    public WeblogEntry getEntry() {
        return entry;
    }

    public void setEntry(WeblogEntry entry) {
        this.entry = entry;
    }

    @SkipValidation
    public String firstSave() {
        addStatusMessage(getEntry().getStatus());
        return execute();
    }

    private void addStatusMessage(PubStatus pubStatus) {
        switch (pubStatus) {
            case DRAFT:
                addMessage("weblogEdit.draftSaved");
                break;
            case PUBLISHED:
                addMessage("weblogEdit.publishedEntry");
                break;
            case SCHEDULED:
                addMessage("weblogEdit.scheduledEntry",
                        DateFormatUtils.ISO_DATE_FORMAT.format(getEntry().getPubTime()));
                break;
            case PENDING:
                addMessage("weblogEdit.submittedForReview");
                break;
        }
    }

    public String getPreviewURL() {
        return urlStrategy.getPreviewURLStrategy(null)
                .getWeblogEntryURL(getActionWeblog(),
                        getEntry().getAnchor(), true);
    }

    /**
     * Get the list of all categories for the action weblog
     */
    public List<WeblogCategory> getCategories() {
        return getActionWeblog().getWeblogCategories();
    }

    public String getEditor() {
        return getActionWeblog().getEditorPage();
    }

    public boolean isUserAnAuthor() {
        return getActionWeblogRole().hasEffectiveRole(WeblogRole.POST);
    }

    public boolean isCommentingEnabled() {
        return getBooleanProp("users.comments.enabled") && getActionWeblog().getAllowComments();
    }

    /**
     * Get recent published weblog entries
     * @return List of published WeblogEntry objects sorted by publication time.
     */
    public List<WeblogEntry> getRecentPublishedEntries() {
        return getRecentEntries(PubStatus.PUBLISHED, WeblogEntrySearchCriteria.SortBy.PUBLICATION_TIME);
    }

    /**
     * Get recent scheduled weblog entries
     * @return List of scheduled WeblogEntry objects sorted by publication time.
     */
    public List<WeblogEntry> getRecentScheduledEntries() {
        return getRecentEntries(PubStatus.SCHEDULED, WeblogEntrySearchCriteria.SortBy.PUBLICATION_TIME);
    }

    /**
     * Get recent draft weblog entries
     * @return List of draft WeblogEntry objects sorted by update time.
     */
    public List<WeblogEntry> getRecentDraftEntries() {
        return getRecentEntries(PubStatus.DRAFT, WeblogEntrySearchCriteria.SortBy.UPDATE_TIME);
    }

    /**
     * Get recent pending weblog entries
     * @return List of pending WeblogEntry objects sorted by update time.
     */
    public List<WeblogEntry> getRecentPendingEntries() {
        return getRecentEntries(PubStatus.PENDING, WeblogEntrySearchCriteria.SortBy.UPDATE_TIME);
    }

    public List<WeblogEntryPlugin> getWeblogEntryPlugins() {
        return weblogEntryPlugins;
    }

    public void setWeblogEntryPlugins(List<WeblogEntryPlugin> weblogEntryPlugins) {
        this.weblogEntryPlugins = weblogEntryPlugins;
    }

    private List<WeblogEntry> getRecentEntries(PubStatus pubStatus, WeblogEntrySearchCriteria.SortBy sortBy) {
        WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
        wesc.setWeblog(getActionWeblog());
        wesc.setMaxResults(20);
        wesc.setStatus(pubStatus);
        wesc.setSortBy(sortBy);
        return weblogEntryManager.getWeblogEntries(wesc);
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/tagdata/{handle}", method = RequestMethod.GET)
    public WeblogTagData getWeblogTagData(@PathVariable String handle, @RequestParam("prefix") String prefix)
            throws ServletException {

        List<TagStat> tags;

        try {
            Weblog weblog = weblogManager.getWeblogByHandle(handle);
            tags = weblogEntryManager.getTags(weblog, null, prefix, 0, MAX_TAGS);

            WeblogTagData wtd = new WeblogTagData();
            wtd.setWeblog(handle);
            wtd.setPrefix(prefix);
            wtd.setTagcounts(tags);
            return wtd;
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private static class WeblogTagData {
        public WeblogTagData() {}

        private String prefix;
        private String weblog;
        private List<TagStat> tagcounts;

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public String getWeblog() {
            return weblog;
        }

        public void setWeblog(String weblog) {
            this.weblog = weblog;
        }

        public List<TagStat> getTagcounts() {
            return tagcounts;
        }

        public void setTagcounts(List<TagStat> tagcounts) {
            this.tagcounts = tagcounts;
        }

    }
}
