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

import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.TagStat;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.plugins.UIPluginManager;
import org.apache.roller.weblogger.ui.core.plugins.WeblogEntryEditor;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.MediacastException;
import org.apache.roller.weblogger.util.MediacastResource;
import org.apache.roller.weblogger.util.MediacastUtil;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.Trackback;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.validation.SkipValidation;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Edit a new or existing entry.
 */
@RestController
public final class EntryEdit extends UIAction {

    private static Log log = LogFactory.getLog(EntryEdit.class);

    // Max Tags to show for autocomplete
    private static final int MAX_TAGS = WebloggerConfig.getIntProperty("services.tagdata.max", 20);

    // bean for managing form data
    private EntryBean bean = new EntryBean();

    // the entry we are adding or editing
    private WeblogEntry entry = null;

    // url to send trackback to
    private String trackbackUrl = null;

    public EntryEdit() {
        this.desiredMenu = "editor";
    }

    @Override
    public void setPageTitle(String pageTitle) {
        this.pageTitle = pageTitle;
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.EDIT_DRAFT;
    }

    public void myPrepare() {
        if (getBean().getId() == null) {
            // Create and initialize new, not-yet-saved Weblog Entry
            entry = new WeblogEntry();
            entry.setCreatorUserName(getAuthenticatedUser().getUserName());
            entry.setWeblog(getActionWeblog());
        } else {
            // already saved entry
            try {
                // retrieve from DB WeblogEntry based on ID
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
                        .getWeblogEntryManager();
                setEntry(wmgr.getWeblogEntry(getBean().getId()));
            } catch (WebloggerException ex) {
                log.error(
                        "Error looking up entry by id - " + getBean().getId(),
                        ex);
            }
        }
    }

    /**
     * Show form for adding/editing weblog entry.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {
        if (getActionName().equals("entryEdit")) {
            // load bean with pojo data
            getBean().copyFrom(getEntry(), getLocale());
        } else {
            // set weblog defaults
            getBean().setAllowComments(getActionWeblog().getDefaultAllowComments());
            getBean().setCommentDays(getActionWeblog().getDefaultCommentDays());
            // apply weblog default plugins
            if (getActionWeblog().getDefaultPlugins() != null) {
                getBean().setPlugins(
                        StringUtils.split(getActionWeblog().getDefaultPlugins(),
                                ","));
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
        getBean().setStatus(PubStatus.DRAFT.name());
        if (entry.isPublished()) {
            // entry reverted from published to non-viewable draft
            // so need to reduce tag aggregates
            entry.setRefreshAggregates(true);
        }
        return save();
    }

    /**
     * Publish an entry.
     *
     * @return String The result of the action.
     */
    public String publish() {
        if (getActionWeblog().userHasWeblogRole(
                getAuthenticatedUser(), WeblogRole.POST)) {
            Timestamp pubTime = getBean().getPubTime(getLocale(),
                    getActionWeblog().getTimeZoneInstance());
            if (pubTime != null && pubTime.after(
                    new Date(System.currentTimeMillis() + DateUtils.MILLIS_PER_MINUTE))) {
                getBean().setStatus(PubStatus.SCHEDULED.name());
                if (entry.isPublished()) {
                    // entry went from published to scheduled, need to reduce tag aggregates
                    entry.setRefreshAggregates(true);
                }
            } else {
                getBean().setStatus(PubStatus.PUBLISHED.name());
                if (getBean().getId() != null && !entry.isPublished()) {
                    // if not a new add, need to add tags to aggregates
                    entry.setRefreshAggregates(true);
                }
            }
        } else {
            getBean().setStatus(PubStatus.PENDING.name());
        }
        return save();
    }

    /**
     * Processing logic common for saving drafts and publishing entries
     *
     * @return String The result of the action.
     */
    private String save() {
        if (!hasActionErrors()) {
            try {
                WeblogEntryManager weblogEntryManager = WebloggerFactory.getWeblogger()
                        .getWeblogEntryManager();

                IndexManager indexMgr = WebloggerFactory.getWeblogger()
                        .getIndexManager();

                WeblogEntry weblogEntry = getEntry();

                // set updatetime & pubtime
                weblogEntry.setUpdateTime(new Timestamp(new Date().getTime()));
                weblogEntry.setPubTime(getBean().getPubTime(getLocale(),
                        getActionWeblog().getTimeZoneInstance()));

                // copy data to pojo
                getBean().copyTo(weblogEntry);

                // handle pubtime auto set
                if (weblogEntry.isPublished() && weblogEntry.getPubTime() == null) {
                    // no time specified, use current time
                    weblogEntry.setPubTime(weblogEntry.getUpdateTime());
                }

                // if user is an admin then apply pinned to main value as well
                if (WebloggerFactory.getWeblogger().getUserManager()
                        .isGlobalAdmin(getAuthenticatedUser())) {
                    weblogEntry.setPinnedToMain(getBean().getPinnedToMain());
                }

                if (!StringUtils.isEmpty(getBean().getEnclosureUrl())) {
                    try {
                        // Fetch MediaCast resource
                        log.debug("Checking MediaCast attributes");
                        MediacastResource mediacast = MediacastUtil
                                .lookupResource(getBean().getEnclosureUrl());

                        // set mediacast attributes
                        weblogEntry.setEnclosureUrl(mediacast.getUrl());
                        weblogEntry.setEnclosureType(mediacast.getContentType());
                        weblogEntry.setEnclosureLength(new Long(mediacast.getLength()));

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
                    indexMgr.addEntryReIndexOperation(entry);
                } else if ("entryEdit".equals(actionName)) {
                    indexMgr.removeEntryIndexOperation(entry);
                }

                // notify caches
                CacheManager.invalidate(weblogEntry);

                if (weblogEntry.isPending() && MailUtil.isMailConfigured()) {
                    MailUtil.sendPendingEntryNotice(weblogEntry);
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

    public EntryBean getBean() {
        return bean;
    }

    public void setBean(EntryBean bean) {
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
        return WebloggerFactory
                .getWeblogger()
                .getUrlStrategy()
                .getPreviewURLStrategy(null)
                .getWeblogEntryURL(getActionWeblog(),
                        getEntry().getAnchor(), true);
    }

    public String getTrackbackUrl() {
        return trackbackUrl;
    }

    public void setTrackbackUrl(String trackbackUrl) {
        this.trackbackUrl = trackbackUrl;
    }

    /**
     * Send trackback to a specific url.
     */
    @SkipValidation
    public String trackback() {

        // make sure we have an entry to edit and it belongs to the action
        // weblog
        if (getEntry() == null) {
            return ERROR;
        } else if (!getEntry().getWeblog().equals(getActionWeblog())) {
            return DENIED;
        }

        if (!StringUtils.isEmpty(getTrackbackUrl())) {
            RollerMessages results = null;
            try {
                Trackback trackback = new Trackback(getEntry(),
                        getTrackbackUrl());
                results = trackback.send();
            } catch (IllegalArgumentException ex) {
                addError("error.trackbackNotAllowed");
            } catch (WebloggerException e) {
                log.error("Error sending trackback", e);
                // TODO: error handling
                addError("error.general", e.getMessage());
            }

            if (results != null) {
                for (Iterator mit = results.getMessages(); mit.hasNext();) {
                    RollerMessage msg = (RollerMessage) mit.next();
                    if (msg.getArgs() == null) {
                        addMessage(msg.getKey());
                    } else {
                        addMessage(msg.getKey(), Arrays.asList(msg.getArgs()));
                    }
                }

                for (Iterator eit = results.getErrors(); eit.hasNext();) {
                    RollerMessage err = (RollerMessage) eit.next();
                    if (err.getArgs() == null) {
                        addError(err.getKey());
                    } else {
                        addError(err.getKey(), Arrays.asList(err.getArgs()));
                    }
                }
            }

            // reset trackback url
            setTrackbackUrl(null);

        }

        return INPUT;
    }

    /**
     * Get the list of all categories for the action weblog
     */
    public List<WeblogCategory> getCategories() {
        try {
            WeblogManager wmgr = WebloggerFactory.getWeblogger().getWeblogManager();
            return wmgr.getWeblogCategories(getActionWeblog());
        } catch (WebloggerException ex) {
            log.error(
                    "Error getting category list for weblog - " + getWeblog(),
                    ex);
            return Collections.emptyList();
        }
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
        return getActionWeblog().userHasWeblogRole(getAuthenticatedUser(),
                WeblogRole.POST);
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
        List<WeblogEntry> entries = Collections.emptyList();
        try {
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(getActionWeblog());
            wesc.setMaxResults(20);
            wesc.setStatus(pubStatus);
            wesc.setSortBy(sortBy);
            entries = WebloggerFactory.getWeblogger().getWeblogEntryManager()
                    .getWeblogEntries(wesc);
        } catch (WebloggerException ex) {
            log.error("Error getting entries list", ex);
        }
        return entries;
    }

    @RequestMapping(value = "/tagdata/{handle}", method = RequestMethod.GET)
    public WeblogTagData getWeblogTagData(@PathVariable String handle, @RequestParam("prefix") String prefix)
            throws ServletException {

        List<TagStat> tags;

        try {
            Weblogger roller = WebloggerFactory.getWeblogger();
            WeblogManager wmgr = roller.getWeblogManager();
            WeblogEntryManager emgr = roller.getWeblogEntryManager();
            Weblog weblog = wmgr.getWeblogByHandle(handle);
            tags = emgr.getTags(weblog, null, prefix, 0, MAX_TAGS);

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
