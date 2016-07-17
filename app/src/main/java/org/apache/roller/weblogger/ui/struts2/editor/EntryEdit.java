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
import org.apache.roller.weblogger.business.RuntimeConfigDefs.CommentOption;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.pojos.AtomEnclosure;
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
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Edit a new or existing entry.
 */
@RestController
public final class EntryEdit extends UIAction {

    private static Logger log = LoggerFactory.getLogger(EntryEdit.class);

    private static DateTimeFormatter pubDateFormat = DateTimeFormatter.ofPattern("M/d/yyyy");

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

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
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

    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    // Max Tags to show for autocomplete
    private static final int MAX_TAGS = WebloggerStaticConfig.getIntProperty("services.tagdata.max", 20);

    // the entry we are adding or editing
    private WeblogEntry entry = null;

    private String entryId = null;

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
        entry = weblogEntryManager.getWeblogEntry(entryId);
        if (entry != null) {
            try {
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
            entry = new WeblogEntry();
            entry.setCreatorId(getAuthenticatedUser().getId());
            entry.setWeblog(getActionWeblog());
            entry.setEditFormat(getActionWeblog().getEditFormat());
            entry.setCommentDays(getActionWeblog().getDefaultCommentDays());
        } else {
            entry = weblogEntryManager.getWeblogEntry(entryId);
            // load bean with pojo data
            String tagsAsString = String.join(" ", entry.getTags().stream().map(WeblogEntryTag::getName).collect(Collectors.toSet()));
            entry.setTagsAsString(tagsAsString);
            entry.setCreatorId(entry.getCreator().getId());

            // init pubtime values
            if (entry.getPubTime() != null) {
                log.debug("entry pubtime is {}", entry.getPubTime());

                //Calendar cal = Calendar.getInstance(locale);
                ZonedDateTime zdt = entry.getPubTime().atZone(entry.getWeblog().getZoneId());

                entry.setHours(zdt.getHour());
                entry.setMinutes(zdt.getMinute());
                entry.setSeconds(zdt.getSecond());
                entry.setDateString(pubDateFormat.format(zdt.toLocalDate()));

                if (log.isDebugEnabled()) {
                    log.debug("pubtime vals are " + entry.getDateString() + ", " + entry.getHours() + ", "
                            + entry.getMinutes() + ", " + entry.getSeconds());
                }
            }
        }
        return INPUT;
    }

    /**
     * Save a draft entry.
     *
     * @return String The result of the action.
     */
    public String saveDraft() {
        entry.setStatus(PubStatus.DRAFT);
        return save();
    }

    /**
     * Publish an entry.
     *
     * @return String The result of the action.
     */
    public String publish() {
        if (getActionWeblogRole().hasEffectiveRole(WeblogRole.POST)) {
            Instant pubTime = calculatePubTime();

            if (pubTime != null && pubTime.isAfter(Instant.now().plus(1, ChronoUnit.MINUTES))) {
                entry.setStatus(PubStatus.SCHEDULED);
            } else {
                entry.setStatus(PubStatus.PUBLISHED);
            }
        } else {
            entry.setStatus(PubStatus.PENDING);
        }
        return save();
    }

    private Instant calculatePubTime() {
        Instant pubtime = null;

        String dateString = entry.getDateString();
        if(!StringUtils.isEmpty(dateString)) {
            try {
                LocalDate newDate = LocalDate.parse(dateString, pubDateFormat);

                // Now handle the time from the hour, minute and second combos
                pubtime = newDate.atTime(entry.getHours(), entry.getMinutes(), entry.getSeconds())
                        .atZone(getActionWeblog().getZoneId()).toInstant();
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
                // set updatetime & pubtime
                entry.setUpdateTime(Instant.now());
                entry.setPubTime(calculatePubTime());
                entry.setWeblog(getActionWeblog());
                entry.setCreator(userManager.getUser(entry.getCreatorId()));
                if (entryId == null) {
                    entry.setId(Utilities.generateUUID());
                } else {
                    entry.setId(entryId);
                }

                // copy data to pojo
                entry.setTitle(entry.getTitle().trim());
                entry.setText(entry.getText().trim());
                entry.setSummary(entry.getSummary().trim());
                entry.setNotes(entry.getNotes().trim());
                Set<String> updatedTags = Utilities.splitStringAsTags(entry.getTagsAsString().trim());
                entry.updateTags(updatedTags);
                entry.setSearchDescription(entry.getSearchDescription().trim());
                entry.setEnclosureUrl(entry.getEnclosureUrl().trim());
                entry.setCategory(weblogManager.getWeblogCategory(entry.getCategoryId()));

                // handle pubtime auto set
                if (entry.isPublished() && entry.getPubTime() == null) {
                    // no time specified, use current time
                    entry.setPubTime(entry.getUpdateTime());
                }

                if (!StringUtils.isEmpty(entry.getEnclosureUrl())) {
                    try {
                        // Fetch MediaCast resource
                        log.debug("Checking MediaCast attributes");
                        AtomEnclosure enclosure = weblogEntryManager.generateEnclosure(entry.getEnclosureUrl());

                        // set enclosure attributes
                        entry.setEnclosureUrl(enclosure.getUrl());
                        entry.setEnclosureType(enclosure.getContentType());
                        entry.setEnclosureLength(enclosure.getLength());

                    } catch (IllegalArgumentException ex) {
                        addError(getText(ex.getMessage()));
                        return INPUT;
                    }
                } else if ("entryEdit".equals(actionName)) {
                    // if enclosure string is empty, clean out its attributes
                    entry.setEnclosureUrl(null);
                    entry.setEnclosureType(null);
                    entry.setEnclosureLength(null);
                }

                if (log.isDebugEnabled()) {
                    log.debug("entry bean is ...\n" + getEntry().toString());
                    log.debug("final status = " + entry.getStatus());
                    log.debug("updtime = " + entry.getUpdateTime());
                    log.debug("pubtime = " + entry.getPubTime());
                }

                log.debug("Saving entry");
                if (!isAdd()) {
                    // work with a managed object, so it doesn't try to insert twice.
                    WeblogEntry myEntry = weblogEntryManager.getWeblogEntry(entry.getId());
                    myEntry.setData(entry);
                    weblogEntryManager.saveWeblogEntry(myEntry);
                } else {
                    weblogEntryManager.saveWeblogEntry(entry);
                }
                WebloggerFactory.flush();

                // notify search of the new entry
                if (entry.isPublished()) {
                    indexManager.addEntryReIndexOperation(entry);
                } else if ("entryEdit".equals(actionName)) {
                    indexManager.removeEntryIndexOperation(entry);
                }

                // notify caches
                cacheManager.invalidate(entry);

                if (PubStatus.PENDING.equals(entry.getStatus()) && mailManager.isMailConfigured()) {
                    mailManager.sendPendingEntryNotice(entry);
                }
                if ("entryEdit".equals(actionName)) {
                    addStatusMessage(getEntry().getStatus());
                    // continue in entryEdit mode
                    return INPUT;
                } else {
                    // now that entry is saved we have an id value for it
                    // store it back in bean for use in next action
                    setEntryId(entry.getId());
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
            getEntry().setStatus(null);
        }
        return INPUT;
    }

    public void myValidate() {
        if (StringUtils.isEmpty(entry.getTitle())) {
            addError("Entry.error.titleNull");
        }
        if (StringUtils.isEmpty(entry.getCategoryId())) {
            addError("Entry.error.categoryNull");
        }
        if (StringUtils.isEmpty(entry.getText())) {
            addError("Entry.error.textNull");
        }
    }

    public WeblogEntry getEntry() {
        return entry;
    }

    public void setEntry(WeblogEntry entry) {
        this.entry = entry;
    }

    @SkipValidation
    public String firstSave() {
        entry = weblogEntryManager.getWeblogEntry(entryId);
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
                        DateTimeFormatter.ISO_DATE_TIME.format(getEntry().getPubTime()));
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

    public boolean isUserAnAuthor() {
        return getActionWeblogRole().hasEffectiveRole(WeblogRole.POST);
    }

    public boolean isCommentingEnabled() {
        return !CommentOption.NONE.equals(CommentOption.valueOf(getProp("users.comments.enabled")))
                && !CommentOption.NONE.equals(getActionWeblog().getAllowComments());
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

        List<WeblogEntryTagAggregate> tags;

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

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    private static class WeblogTagData {
        public WeblogTagData() {}

        private String prefix;
        private String weblog;
        private List<WeblogEntryTagAggregate> tagcounts;

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

        public List<WeblogEntryTagAggregate> getTagcounts() {
            return tagcounts;
        }

        public void setTagcounts(List<WeblogEntryTagAggregate> tagcounts) {
            this.tagcounts = tagcounts;
        }

    }

}
