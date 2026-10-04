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

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.util.DateUtil;
import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.plugins.PluginManager;
import org.apache.roller.weblogger.business.plugins.entry.WeblogEntryPlugin;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.MediaFile;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.core.RollerContext;
import org.apache.roller.weblogger.ui.core.plugins.UIPluginManager;
import org.apache.roller.weblogger.ui.core.plugins.WeblogEntryEditor;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.EnclosureMetadata;
import org.apache.roller.weblogger.util.InlineImageData;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Edit a new or existing entry.
 */
// TODO: make this work @AllowedMethods({"execute","firstSave","saveDraft","publish","entryEdit","entryAdd"})
public final class EntryEdit extends UIAction {

    private static Log log = LogFactory.getLog(EntryEdit.class);

    // bean for managing form data
    private EntryBean bean = new EntryBean();

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
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.EDIT_DRAFT);
    }

    @Override
    public void myPrepare() {
        if (getBean().getId() == null) {
            // Create and initialize new, not-yet-saved Weblog Entry
            entry = new WeblogEntry();
            entry.setCreatorUserName(getAuthenticatedUser().getUserName());
            entry.setWebsite(getActionWeblog());
        } else {
            // already saved entry
            try {
                // retrieve from DB WeblogEntry based on ID
                WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
                        .getWeblogEntryManager();
                setEntry(wmgr.getWeblogEntry(getActionWeblog(), getBean().getId()));
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
    @Override
    public String execute() {
        if (getActionName().equals("entryEdit")) {
            if (!requireEntry()) {
                return ERROR;
            }
            // load bean with pojo data
            getBean().copyFrom(getEntry(), getLocale());
        } else {
            // set weblog defaults
            getBean().setLocale(getActionWeblog().getLocale());
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
        if (!requireEntry()) {
            return INPUT;
        }
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
        if (!requireEntry()) {
            return INPUT;
        }
        if (getActionWeblog().hasUserPermission(
                getAuthenticatedUser(), WeblogPermission.POST)) {
            Timestamp pubTime = getBean().getPubTime(getLocale(),
                    getActionWeblog().getTimeZoneInstance());
            if (pubTime != null && pubTime.after(
                    new Date(System.currentTimeMillis() + RollerConstants.MIN_IN_MS))) {
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
    // Package-private rather than private so EntryEditEnclosureTest can drive
    // it directly.
    String save() {
        if (!requireEntry()) {
            return INPUT;
        }
        if (!hasActionErrors()) {
            EnclosureMetadata enclosure = validateEnclosure();
            if (hasActionErrors()) {
                return failedSave();
            }

            String submittedText = getBean().getText();
            String submittedSummary = getBean().getSummary();
            List<MediaFile> createdImages = new ArrayList<>();
            boolean entrySaved = false;
            try {
                if (!prepareInlineImages(createdImages)) {
                    return failedSave();
                }

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
                GlobalPermission adminPerm = new GlobalPermission(
                        Collections.singletonList(GlobalPermission.ADMIN));
                if (WebloggerFactory.getWeblogger().getUserManager()
                        .checkPermission(adminPerm, getAuthenticatedUser())) {
                    weblogEntry.setPinnedToMain(getBean().getPinnedToMain());
                }

                if (enclosure != null) {
                    weblogEntry.putEntryAttribute("att_mediacast_url",
                            enclosure.getUrl());
                    weblogEntry.putEntryAttribute("att_mediacast_type",
                            enclosure.getContentType());
                    weblogEntry.putEntryAttribute("att_mediacast_length",
                            enclosure.getLength());
                } else if ("entryEdit".equals(actionName)) {
                    try {
                        // if MediaCast string is empty, clean out MediaCast
                        // attributes
                        weblogEntryManager.removeWeblogEntryAttribute(
                                "att_mediacast_url", weblogEntry);
                        weblogEntryManager.removeWeblogEntryAttribute(
                                "att_mediacast_type", weblogEntry);
                        weblogEntryManager.removeWeblogEntryAttribute(
                                "att_mediacast_length", weblogEntry);

                    } catch (WebloggerException e) {
                        addMessage(getText("weblogEdit.mediaCastErrorRemoving"));
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("entry bean is ...\n" + getBean().toString());
                    log.debug("final status = " + weblogEntry.getStatus());
                    log.debug("updtime = " + weblogEntry.getUpdateTime());
                    log.debug("pubtime = " + weblogEntry.getPubTime());
                }

                log.debug("Saving entry");
                weblogEntryManager.saveWeblogEntry(weblogEntry);
                WebloggerFactory.getWeblogger().flush();
                entrySaved = true;

                // notify search of the new entry
                if (weblogEntry.isPublished()) {
                    indexMgr.addEntryReIndexOperation(entry);
                } else if ("entryEdit".equals(actionName)) {
                    indexMgr.removeEntryIndexOperation(entry);
                }

                // notify caches
                CacheManager.invalidate(weblogEntry);

                // Queue applicable pings for this update.
                if (weblogEntry.isPublished()) {
                    WebloggerFactory.getWeblogger().getAutopingManager()
                            .queueApplicableAutoPings(weblogEntry);
                }

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
                if (!entrySaved) {
                    // The entry may already hold the failed edit, and the
                    // cleanup below commits its own transaction. Roll the
                    // failed edit back first so that commit cannot save it.
                    WebloggerFactory.getWeblogger().release();
                    getBean().setText(submittedText);
                    getBean().setSummary(submittedSummary);
                    removeCreatedImages(WebloggerFactory.getWeblogger()
                            .getMediaFileManager(), createdImages);
                }
                addError("generic.error.check.logs");
            }
        }
        return failedSave();
    }

    /**
     * Uploads data images in the submitted text and summary as media files, or
     * keeps them inline when uploads are unavailable. Adds an action error and
     * returns false if the entry cannot be saved. Media files it creates are
     * added to createdImages so a later failure can remove them.
     */
    // Package-private so EntryEditInlineImagesTest can drive it directly.
    boolean prepareInlineImages(List<MediaFile> createdImages)
            throws WebloggerException {
        String submittedText = getBean().getText();
        String submittedSummary = getBean().getSummary();
        Map<String, InlineImageData.Image> images = new HashMap<>();
        List<InlineImageData.Source> textImages = InlineImageData.findSources(submittedText);
        List<InlineImageData.Source> summaryImages = InlineImageData.findSources(submittedSummary);
        boolean keepInline = WebloggerConfig.getBooleanProperty(
                "weblog.inlineImages.preferInline")
                || !WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled")
                || !getActionWeblog().hasUserPermission(
                        getAuthenticatedUser(), WeblogPermission.POST);
        long maxUploadBytes = 0;
        if (!keepInline && (!textImages.isEmpty() || !summaryImages.isEmpty())) {
            maxUploadBytes = (long) (RollerConstants.ONE_MB_IN_BYTES
                    * new BigDecimal(WebloggerRuntimeConfig.getProperty(
                            "uploads.file.maxsize")).doubleValue());
        }
        if (!validateInlineImages(textImages, images, keepInline, maxUploadBytes)
                || !validateInlineImages(summaryImages, images, keepInline,
                        maxUploadBytes)) {
            return false;
        }
        if (!images.isEmpty()) {
            if (keepInline) {
                String inlineText = normalizeInlineSources(submittedText,
                        textImages);
                String inlineSummary = normalizeInlineSources(submittedSummary,
                        summaryImages);
                if (!inlineFieldFits(inlineText, textImages)
                        || !inlineFieldFits(inlineSummary, summaryImages)) {
                    return false;
                }
                getBean().setText(inlineText);
                getBean().setSummary(inlineSummary);
            } else {
                MediaFileManager mediaManager = WebloggerFactory.getWeblogger()
                        .getMediaFileManager();
                MediaFileDirectory directory = mediaManager
                        .getDefaultMediaFileDirectory(getActionWeblog());
                if (directory == null) {
                    directory = mediaManager.createDefaultMediaFileDirectory(
                            getActionWeblog());
                }
                Map<String, String> mediaUrls = new HashMap<>();
                getBean().setText(replaceInlineImages(submittedText,
                        textImages, images, mediaUrls, directory,
                        mediaManager, createdImages));
                if (!hasActionErrors()) {
                    getBean().setSummary(replaceInlineImages(submittedSummary,
                            summaryImages, images, mediaUrls, directory,
                            mediaManager, createdImages));
                }
                if (hasActionErrors()) {
                    getBean().setText(submittedText);
                    getBean().setSummary(submittedSummary);
                    removeCreatedImages(mediaManager, createdImages);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validateInlineImages(List<InlineImageData.Source> sources,
            Map<String, InlineImageData.Image> images, boolean keepInline,
            long maxUploadBytes) {
        for (InlineImageData.Source source : sources) {
            if (!keepInline && InlineImageData.exceedsUploadLimit(
                    source.getValue(), maxUploadBytes)) {
                addError("weblogEdit.inlineImageUploadTooLarge");
                return false;
            }
            InlineImageData.Image image = keepInline
                    ? InlineImageData.parse(source.getValue())
                    : InlineImageData.parseForUpload(source.getValue(),
                            maxUploadBytes);
            if (image == null) {
                addError("weblogEdit.inlineImageInvalid");
                return false;
            }
            images.put(source.getValue(), image);
        }
        return true;
    }

    private boolean inlineFieldFits(String html, List<InlineImageData.Source> sources) {
        if (!sources.isEmpty() && html.getBytes(StandardCharsets.UTF_8).length
                > InlineImageData.maxFieldBytes()) {
            addError("weblogEdit.inlineImageTooLarge");
            return false;
        }
        return true;
    }

    private String normalizeInlineSources(String html,
            List<InlineImageData.Source> sources) {
        if (html == null || sources.isEmpty()) {
            return html;
        }
        StringBuilder result = new StringBuilder(html.length());
        int cursor = 0;
        for (InlineImageData.Source source : sources) {
            result.append(html, cursor, source.getStart());
            result.append("src=\"").append(source.getValue()).append('"');
            cursor = source.getEnd();
        }
        result.append(html, cursor, html.length());
        return result.toString();
    }

    private String replaceInlineImages(String html,
            List<InlineImageData.Source> sources,
            Map<String, InlineImageData.Image> images,
            Map<String, String> mediaUrls, MediaFileDirectory directory,
            MediaFileManager mediaManager, List<MediaFile> createdImages)
            throws WebloggerException {
        if (html == null || sources.isEmpty()) {
            return html;
        }
        StringBuilder result = new StringBuilder(html.length());
        int cursor = 0;
        for (InlineImageData.Source source : sources) {
            String url = mediaUrls.get(source.getValue());
            if (url == null) {
                InlineImageData.Image image = images.get(source.getValue());
                String name = "entry-image-" + UUID.randomUUID() + "."
                        + image.getExtension();
                RollerMessages errors = new RollerMessages();
                if (!WebloggerFactory.getWeblogger().getFileContentManager()
                        .canSave(getActionWeblog(), name, image.getContentType(),
                                image.getBytes().length, errors)) {
                    addMediaErrors(errors);
                    return html;
                }
                MediaFile media = new MediaFile();
                media.setName(name);
                media.setWeblog(getActionWeblog());
                media.setDirectory(directory);
                media.setLength(image.getBytes().length);
                media.setContentType(image.getContentType());
                media.setInputStream(new ByteArrayInputStream(image.getBytes()));
                // Track the upload before creating it: createMediaFile commits
                // the record before it writes the file, so a failed write can
                // still leave a record to remove.
                createdImages.add(media);
                mediaManager.createMediaFile(getActionWeblog(), media, errors);
                if (errors.getErrorCount() > 0) {
                    addMediaErrors(errors);
                    return html;
                }
                url = media.getPermalink();
                mediaUrls.put(source.getValue(), url);
            }
            result.append(html, cursor, source.getStart());
            result.append("src=\"").append(url).append('"');
            cursor = source.getEnd();
        }
        result.append(html, cursor, html.length());
        return result.toString();
    }

    private void addMediaErrors(RollerMessages errors) {
        for (Iterator<RollerMessage> it = errors.getErrors(); it.hasNext();) {
            RollerMessage message = it.next();
            String[] args = message.getArgs();
            addError(message.getKey(), args == null
                    ? Collections.emptyList() : java.util.Arrays.asList(args));
        }
    }

    private void removeCreatedImages(MediaFileManager mediaManager,
            List<MediaFile> createdImages) {
        for (MediaFile image : createdImages) {
            try {
                // Look the upload up again: the save may have rolled back and
                // released the session, and an attempted upload may never
                // have been stored.
                MediaFile stored = mediaManager.getMediaFile(image.getId());
                if (stored != null) {
                    mediaManager.removeMediaFile(stored.getWeblog(), stored);
                }
            } catch (WebloggerException cleanupError) {
                log.warn("Could not remove an image from a failed entry save", cleanupError);
            }
        }
        if (!createdImages.isEmpty()) {
            try {
                WebloggerFactory.getWeblogger().flush();
            } catch (WebloggerException cleanupError) {
                log.warn("Could not flush image cleanup after a failed entry save",
                        cleanupError);
            }
        }
    }

    EnclosureMetadata validateEnclosure() {
        if (StringUtils.isEmpty(getBean().getEnclosureURL())) {
            return null;
        }
        try {
            return EnclosureMetadata.of(
                    getBean().getEnclosureURL(),
                    getBean().getEnclosureType(),
                    getBean().getEnclosureLength());
        } catch (EnclosureMetadata.ValidationException invalid) {
            if (submittedEnclosureMatchesStored()) {
                getBean().setEnclosureURL(null);
                getBean().setEnclosureType(null);
                getBean().setEnclosureLength(null);
                addMessage("weblogEdit.enclosureMetadataRemoved");
            } else {
                switch (invalid.getField()) {
                    case URL:
                        addError("weblogEdit.enclosureURLInvalid");
                        break;
                    case TYPE:
                        addError("weblogEdit.enclosureTypeInvalid");
                        break;
                    case LENGTH:
                        addError("weblogEdit.enclosureLengthInvalid");
                        break;
                    default:
                        throw invalid;
                }
            }
            return null;
        }
    }

    private boolean submittedEnclosureMatchesStored() {
        return "entryEdit".equals(actionName) && getEntry() != null
                && StringUtils.equals(getBean().getEnclosureURL(),
                        getEntry().findEntryAttribute("att_mediacast_url"))
                && StringUtils.equals(getBean().getEnclosureType(),
                        getEntry().findEntryAttribute("att_mediacast_type"))
                && StringUtils.equals(getBean().getEnclosureLength(),
                        getEntry().findEntryAttribute("att_mediacast_length"));
    }

    private String failedSave() {
        if ("entryAdd".equals(actionName)) {
            // If here on entryAdd, nothing saved, so reset status to null (unsaved).
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

    private boolean requireEntry() {
        if (entry == null) {
            addError("weblogEntry.notFound");
            return false;
        }
        return true;
    }

    @SkipValidation
    public String firstSave() {
        if (!requireEntry()) {
            return ERROR;
        }
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
                addMessage("weblogEdit.scheduledEntry", DateUtil.fullDate(getEntry().getPubTime()));
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
                .getWeblogEntryURL(getActionWeblog(), null,
                        getEntry().getAnchor(), false);
    }

    /**
     * Get the list of all categories for the action weblog
     */
    public List<WeblogCategory> getCategories() {
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger()
                    .getWeblogEntryManager();
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

            if (!plugins.isEmpty()) {
                availablePlugins = new ArrayList<>();
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

}
