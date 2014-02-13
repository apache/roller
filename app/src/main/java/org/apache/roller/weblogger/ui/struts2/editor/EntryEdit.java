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

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.roller.weblogger.util.MailUtil;
import org.apache.roller.weblogger.util.MediacastException;
import org.apache.roller.weblogger.util.MediacastResource;
import org.apache.roller.weblogger.util.MediacastUtil;
import org.apache.roller.weblogger.util.RollerMessages;
import org.apache.roller.weblogger.util.RollerMessages.RollerMessage;
import org.apache.roller.weblogger.util.Trackback;
import org.apache.roller.weblogger.util.TrackbackNotAllowedException;
import org.apache.struts2.interceptor.validation.SkipValidation;

/**
 * Edit an existing entry.
 */
public final class EntryEdit extends EntryBase {

    private static final long MINUTE_IN_MILLIS = 60000;

    private static Log log = LogFactory.getLog(EntryEdit.class);

    // bean for managing form data
    private EntryBean bean = new EntryBean();

    // the entry we are editing
    private WeblogEntry entry = null;

    // url to send trackback to
    private String trackbackUrl = null;

    public EntryEdit() {
        this.actionName = "entryEdit";
        this.desiredMenu = "editor";
        this.pageTitle = "weblogEdit.title.editEntry";
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.EDIT_DRAFT);
    }

    public void myPrepare() {
        if (getBean().getId() != null) {
            try {
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
     * Show form for editing an existing entry.
     * 
     * @return String The result of the action.
     */
    @SkipValidation
    public String execute() {

        // make sure we have an entry to edit and it belongs to the action
        // weblog
        if (getEntry() == null) {
            return ERROR;
        } else if (!getEntry().getWebsite().equals(getActionWeblog())) {
            return DENIED;
        }

        // load bean with pojo data
        getBean().copyFrom(getEntry(), getLocale());

        return INPUT;
    }

    /**
     * Save weblog entry.
     * 
     * @return String The result of the action.
     */
    public String save() {

        // make sure we have an entry to edit and it belongs to the action
        // weblog
        if (getEntry() == null) {
            return ERROR;
        } else if (!getEntry().getWebsite().equals(getActionWeblog())) {
            return DENIED;
        }

        if (!hasActionErrors()) {
            try {
                WeblogEntryManager weblogMgr = WebloggerFactory.getWeblogger()
                        .getWeblogEntryManager();

                WeblogEntry entry = getEntry();

                // set updatetime & pubtime
                entry.setUpdateTime(new Timestamp(new Date().getTime()));
                entry.setPubTime(getBean().getPubTime(getLocale(),
                        getActionWeblog().getTimeZoneInstance()));

                // copy data to pojo
                getBean().copyTo(entry);

                // handle pubtime auto set
                if (entry.isPublished()) {
                    if (entry.getPubTime() == null) {
                        // no time specified, use current time
                        entry.setPubTime(entry.getUpdateTime());
                    }

                    // if user does not have author perms then force PENDING
                    // status
                    if (!getActionWeblog().hasUserPermission(
                            getAuthenticatedUser(), WeblogPermission.POST)) {
                        entry.setStatus(WeblogEntry.PENDING);
                    }

                    // If the entry was published to future, set status as
                    // SCHEDULED we only consider an entry future published if
                    // it is scheduled more than 1 minute into the future
                    if (entry.getPubTime().after(
                            new Date(System.currentTimeMillis() + MINUTE_IN_MILLIS))) {
                        getBean().setStatus(WeblogEntry.SCHEDULED);
                    }

                }

                // if user is an admin then apply pinned to main value as well
                GlobalPermission adminPerm = new GlobalPermission(
                        Collections.singletonList(GlobalPermission.ADMIN));
                if (WebloggerFactory.getWeblogger().getUserManager()
                        .checkPermission(adminPerm, getAuthenticatedUser())) {
                    entry.setPinnedToMain(getBean().getPinnedToMain());
                }

                if (!StringUtils.isEmpty(getBean().getEnclosureURL())) {
                    try {
                        // Fetch MediaCast resource
                        log.debug("Checking MediaCast attributes");
                        MediacastResource mediacast = MediacastUtil
                                .lookupResource(getBean().getEnclosureURL());

                        // set mediacast attributes
                        entry.putEntryAttribute("att_mediacast_url",
                                mediacast.getUrl());
                        entry.putEntryAttribute("att_mediacast_type",
                                mediacast.getContentType());
                        entry.putEntryAttribute("att_mediacast_length", ""
                                + mediacast.getLength());

                    } catch (MediacastException ex) {
                        addMessage(getText(ex.getErrorKey()));
                    }
                } else {
                    try {
                        // if MediaCast string is empty, clean out MediaCast
                        // attributes
                        weblogMgr.removeWeblogEntryAttribute(
                                "att_mediacast_url", entry);
                        weblogMgr.removeWeblogEntryAttribute(
                                "att_mediacast_type", entry);
                        weblogMgr.removeWeblogEntryAttribute(
                                "att_mediacast_length", entry);

                    } catch (WebloggerException e) {
                        addMessage(getText("weblogEdit.mediaCastErrorRemoving"));
                    }
                }

                if (log.isDebugEnabled()) {
                    log.debug("entry bean is ...\n" + getBean().toString());
                    log.debug("final status = " + entry.getStatus());
                    log.debug("updtime = " + entry.getUpdateTime());
                    log.debug("pubtime = " + entry.getPubTime());
                }

                log.debug("Saving entry");
                weblogMgr.saveWeblogEntry(entry);
                WebloggerFactory.getWeblogger().flush();

                // notify search of the new entry
                if (entry.isPublished()) {
                    reindexEntry(entry);
                } else {
                    removeEntryIndex(entry);
                }

                // notify caches
                CacheManager.invalidate(entry);

                // Queue applicable pings for this update.
                if (entry.isPublished()) {
                    WebloggerFactory.getWeblogger().getAutopingManager()
                            .queueApplicableAutoPings(entry);
                }

                if (entry.isPending()) {
                    // implies that entry just changed to pending
                    if (MailUtil.isMailConfigured()) {
                        MailUtil.sendPendingEntryNotice(entry);
                    }
                    addMessage("weblogEdit.submittedForReview");
                } else {
                    addMessage("weblogEdit.changesSaved");
                }

                return INPUT;

            } catch (Exception e) {
                log.error("Error saving new entry", e);
                // TODO: i18n
                addError("Error saving new entry");
            }
        }

        return INPUT;
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
        } else if (!getEntry().getWebsite().equals(getActionWeblog())) {
            return DENIED;
        }

        if (!StringUtils.isEmpty(getTrackbackUrl())) {
            RollerMessages results = null;
            try {
                Trackback trackback = new Trackback(getEntry(),
                        getTrackbackUrl());
                results = trackback.send();
            } catch (TrackbackNotAllowedException ex) {
                addError("error.trackbackNotAllowed");
            } catch (Exception e) {
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
     * Get the list of all categories for the action weblog, not including root.
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
            return Collections.EMPTY_LIST;
        }
    }

    public String getPreviewURL() {
        return WebloggerFactory
                .getWeblogger()
                .getUrlStrategy()
                .getPreviewURLStrategy(null)
                .getWeblogEntryURL(getActionWeblog(), null,
                        getEntry().getAnchor(), true);
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

    public String getTrackbackUrl() {
        return trackbackUrl;
    }

    public void setTrackbackUrl(String trackbackUrl) {
        this.trackbackUrl = trackbackUrl;
    }

}
