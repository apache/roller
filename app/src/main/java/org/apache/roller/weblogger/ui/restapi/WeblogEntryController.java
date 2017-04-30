/*
 *
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.apache.roller.weblogger.ui.restapi;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.business.MailManager;
import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.AtomEnclosure;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntry.PubStatus;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryTag;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.pojos.WebloggerProperties;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.ValidationError;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.security.Principal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/tb-ui/authoring/rest/weblogentries")
public class WeblogEntryController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

    private static DateTimeFormatter pubDateFormat = DateTimeFormatter.ofPattern("M/d/yyyy");

    // number of entries to show per page
    private static final int ITEMS_PER_PAGE = 30;

    // Max Tags to show for autocomplete
    private static final int MAX_TAGS = WebloggerStaticConfig.getIntProperty("services.tagdata.max", 20);

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Autowired
    private IndexManager indexManager;

    public void setIndexManager(IndexManager indexManager) {
        this.indexManager = indexManager;
    }

    @Autowired
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @Autowired
    private URLStrategy urlStrategy;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    @Autowired
    private MailManager mailManager;

    public void setMailManager(MailManager manager) {
        mailManager = manager;
    }

    @RequestMapping(value = "/{weblogId}/page/{page}", method = RequestMethod.POST)
    public WeblogEntryData getWeblogEntries(@PathVariable String weblogId, @PathVariable int page,
                                              @RequestBody WeblogEntrySearchCriteria criteria, Principal principal,
                                              HttpServletResponse response) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        if (weblog != null && userManager.checkWeblogRole(principal.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

            WeblogEntryData data = new WeblogEntryData();

            criteria.setWeblog(weblog);
            criteria.setOffset(page * ITEMS_PER_PAGE);
            criteria.setMaxResults(ITEMS_PER_PAGE + 1);
            List<WeblogEntry> rawEntries = weblogEntryManager.getWeblogEntries(criteria);
            data.entries = new ArrayList<>();
            data.entries.addAll(rawEntries.stream().peek(re -> re.setWeblog(null))
                    .peek(re -> re.getCategory().setWeblog(null))
                    .collect(Collectors.toList()));

            if (rawEntries.size() > ITEMS_PER_PAGE) {
                data.entries.remove(data.entries.size() - 1);
                data.hasMore = true;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            return data;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }
    }

    public class WeblogEntryData {
        List<WeblogEntry> entries;
        boolean hasMore = false;

        public List<WeblogEntry> getEntries() {
            return entries;
        }

        public boolean isHasMore() {
            return hasMore;
        }
    }

    @RequestMapping(value = "/{weblogId}/searchfields", method = RequestMethod.GET)
    public WeblogEntrySearchFields getWeblogEntrySearchFields(@PathVariable String weblogId, Principal principal,
                                                              HttpServletResponse response) {

        // Get user permissions and locale
        User user = userManager.getEnabledUserByUserName(principal.getName());
        I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();

        Weblog weblog = weblogManager.getWeblog(weblogId);

        if (weblog != null && userManager.checkWeblogRole(user, weblog, WeblogRole.POST)) {
            WeblogEntrySearchFields fields = new WeblogEntrySearchFields();

            // categories
            fields.categories = new LinkedHashMap<>();
            fields.categories.put("", "(Any)");
            for (WeblogCategory cat : weblog.getWeblogCategories()) {
                fields.categories.put(cat.getName(), cat.getName());
            }

            // sort by options
            fields.sortByOptions = new LinkedHashMap<>();
            fields.sortByOptions.put(WeblogEntrySearchCriteria.SortBy.PUBLICATION_TIME.name(), messages.getString("entries.label.pubTime"));
            fields.sortByOptions.put(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME.name(), messages.getString("entries.label.updateTime"));

            // status options
            fields.statusOptions = new LinkedHashMap<>();
            fields.statusOptions.put("", messages.getString("entries.label.allEntries"));
            fields.statusOptions.put("DRAFT", messages.getString("entries.label.draftOnly"));
            fields.statusOptions.put("PUBLISHED", messages.getString("entries.label.publishedOnly"));
            fields.statusOptions.put("PENDING", messages.getString("entries.label.pendingOnly"));
            fields.statusOptions.put("SCHEDULED", messages.getString("entries.label.scheduledOnly"));

            return fields;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

    }

    public class WeblogEntrySearchFields {
        Map<String, String> categories;
        Map<String, String> sortByOptions;
        Map<String, String> statusOptions;

        // getters needed for JSON serialization: http://stackoverflow.com/a/35822500
        public Map<String, String> getCategories() {
            return categories;
        }

        public Map<String, String> getSortByOptions() {
            return sortByOptions;
        }

        public Map<String, String> getStatusOptions() {
            return statusOptions;
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public void deleteWeblogEntry(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogEntry itemToRemove = weblogEntryManager.getWeblogEntry(id, false);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.POST)) {
                    // remove from search index
                    if (itemToRemove.isPublished()) {
                        indexManager.removeEntryIndexOperation(itemToRemove);
                    }

                    // flush caches
                    cacheManager.invalidate(itemToRemove);

                    // remove entry itself
                    weblogEntryManager.removeWeblogEntry(itemToRemove);
                    persistenceStrategy.flush();

                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error removing entry {}", id, e);
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/{id}/tagdata", method = RequestMethod.GET)
    public WeblogTagData getWeblogTagData(@PathVariable String id, @RequestParam("prefix") String prefix)
            throws ServletException {

        List<WeblogEntryTagAggregate> tags;

        try {
            Weblog weblog = weblogManager.getWeblog(id);
            tags = weblogManager.getTags(weblog, null, prefix, 0, MAX_TAGS);

            WeblogTagData wtd = new WeblogTagData();
            wtd.setPrefix(prefix);
            wtd.setTagcounts(tags);
            return wtd;
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    private static class WeblogTagData {
        private String prefix;
        private List<WeblogEntryTagAggregate> tagcounts;

        public WeblogTagData() {
        }

        public String getPrefix() {
            return prefix;
        }

        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        public List<WeblogEntryTagAggregate> getTagcounts() {
            return tagcounts;
        }

        public void setTagcounts(List<WeblogEntryTagAggregate> tagcounts) {
            this.tagcounts = tagcounts;
        }
    }

    @RequestMapping(value = "/{weblogId}/recententries/{pubStatus}", method = RequestMethod.GET)
    private List<WeblogEntry> getRecentEntries(@PathVariable String weblogId,
                                                         @PathVariable WeblogEntry.PubStatus pubStatus,
                                               Principal p, HttpServletResponse response) {

        Weblog weblog = weblogManager.getWeblog(weblogId);
        WeblogRole minimumRole = (pubStatus == PubStatus.DRAFT || pubStatus == PubStatus.PENDING) ?
                WeblogRole.EDIT_DRAFT : WeblogRole.POST;
        if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), minimumRole)) {
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setMaxResults(20);
            wesc.setStatus(pubStatus);
            List<WeblogEntry> entries = weblogEntryManager.getWeblogEntries(wesc);
            List<WeblogEntry> recentEntries = entries.stream().map(r -> new WeblogEntry(r.getTitle(),
                    urlStrategy.getEntryEditURL(weblog.getId(), r.getId(), true))).collect(Collectors.toList());
            response.setStatus(HttpServletResponse.SC_OK);
            return recentEntries;
        } else if (WeblogRole.POST.equals(minimumRole) &&
                userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.EDIT_DRAFT)) {
            // contributors get empty array for certain pub statuses
            response.setStatus(HttpServletResponse.SC_OK);
            return new ArrayList<>();
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public WeblogEntry getWeblogEntry(@PathVariable String id, Principal p, HttpServletResponse response) throws ServletException {
        try {
            WeblogEntry entry = weblogEntryManager.getWeblogEntry(id, true);
            if (entry != null) {
                Weblog weblog = entry.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.EDIT_DRAFT)) {
                    String tagsAsString = String.join(" ", entry.getTags().stream().map(WeblogEntryTag::getName).collect(Collectors.toSet()));
                    entry.setTagsAsString(tagsAsString);
                    entry.setCommentsUrl(urlStrategy.getCommentManagementURL(weblog.getId(), entry.getId(), true));
                    entry.setPermalink(urlStrategy.getWeblogEntryURL(weblog, entry.getAnchor(), true));
                    entry.setPreviewUrl(urlStrategy.getPreviewURLStrategy(null).getWeblogEntryURL(weblog,
                            entry.getAnchor(), true));

                    if (entry.getPubTime() != null) {
                        log.debug("entry pubtime is {}", entry.getPubTime());
                        ZonedDateTime zdt = entry.getPubTime().atZone(entry.getWeblog().getZoneId());
                        entry.setHours(zdt.getHour());
                        entry.setMinutes(zdt.getMinute());
                        entry.setCreator(null);
                        entry.setDateString(pubDateFormat.format(zdt.toLocalDate()));
                    }

                    response.setStatus(HttpServletResponse.SC_OK);
                    return entry;
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            log.error("Error retrieving entry {}", id, e);
            throw new ServletException(e.getMessage());
        }
        return null;
    }

    @RequestMapping(value = "/{weblogId}/entryeditmetadata", method = RequestMethod.GET)
    public EntryEditMetadata getEntryEditMetadata(@PathVariable String weblogId, Principal principal,
                                                              HttpServletResponse response) {

        // Get user permissions and locale
        User user = userManager.getEnabledUserByUserName(principal.getName());
        I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();

        Weblog weblog = weblogManager.getWeblog(weblogId);

        if (weblog != null && userManager.checkWeblogRole(user, weblog, WeblogRole.EDIT_DRAFT)) {
            EntryEditMetadata fields = new EntryEditMetadata();

            // categories
            fields.categories = new LinkedHashMap<>();
            for (WeblogCategory cat : weblog.getWeblogCategories()) {
                fields.categories.put(cat.getId(), cat.getName());
            }

            fields.author = userManager.checkWeblogRole(user, weblog, WeblogRole.POST);
            fields.commentingEnabled = !WebloggerProperties.CommentPolicy.NONE.equals(
                    persistenceStrategy.getWebloggerProperties().getCommentPolicy()) &&
                    !WebloggerProperties.CommentPolicy.NONE.equals(weblog.getAllowComments());
            fields.defaultCommentDays = weblog.getDefaultCommentDays();
            fields.defaultEditFormat = weblog.getEditFormat();
            fields.timezone = weblog.getTimeZone();

            for (Weblog.EditFormat format : Weblog.EditFormat.values()) {
                fields.editFormatDescriptions.put(format, messages.getString(format.getDescriptionKey()));
            }

            // comment day options
            fields.commentDayOptions = Arrays.stream(WeblogEntry.CommentDayOption.values())
                    .collect(Utilities.toLinkedHashMap(cdo -> Integer.toString(cdo.getDays()),
                            cdo -> messages.getString(cdo.getDescriptionKey())));

            return fields;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

    }

    public class EntryEditMetadata {
        Map<String, String> categories;
        Map<String, String> commentDayOptions;
        boolean author = false;
        boolean commentingEnabled = false;
        int defaultCommentDays = -1;
        Weblog.EditFormat defaultEditFormat;
        Map<Weblog.EditFormat, String> editFormatDescriptions = new HashMap<>();
        String timezone;

        // getters needed for JSON serialization: http://stackoverflow.com/a/35822500
        public Map<String, String> getCategories() {
            return categories;
        }

        public Map<String, String> getCommentDayOptions() {
            return commentDayOptions;
        }

        public boolean isAuthor() {
            return author;
        }

        public boolean isCommentingEnabled() {
            return commentingEnabled;
        }

        public int getDefaultCommentDays() {
            return defaultCommentDays;
        }

        public Weblog.EditFormat getDefaultEditFormat() {
            return defaultEditFormat;
        }

        public Map<Weblog.EditFormat, String> getEditFormatDescriptions() {
            return editFormatDescriptions;
        }

        public String getTimezone() {
            return timezone;
        }
    }

    // publish
    // save
    // submit for review
    @RequestMapping(value = "/{weblogId}/entries", method = RequestMethod.POST)
    public ResponseEntity postEntry(@PathVariable String weblogId, @Valid @RequestBody WeblogEntry entryData,
                                       Principal p) throws ServletException {

        try {

            boolean createNew = false;
            WeblogEntry entry = null;

            if (entryData.getId() != null) {
                entry = weblogEntryManager.getWeblogEntry(entryData.getId(), false);
            }

            // Check user permissions
            User user = userManager.getEnabledUserByUserName(p.getName());
            I18nMessages messages = (user == null) ? I18nMessages.getMessages(Locale.getDefault()) : user.getI18NMessages();

            Weblog weblog = (entry == null) ? weblogManager.getWeblog(weblogId) : entry.getWeblog();

            WeblogRole necessaryRole = (PubStatus.PENDING.equals(entryData.getStatus()) ||
                    PubStatus.DRAFT.equals(entryData.getStatus())) ? WeblogRole.EDIT_DRAFT : WeblogRole.POST;
            if (weblog != null && userManager.checkWeblogRole(user, weblog, necessaryRole)) {

                // create new?
                if (entry == null) {
                    createNew = true;
                    entry = new WeblogEntry();
                    entry.setId(Utilities.generateUUID());
                    entry.setCreator(user);
                    entry.setWeblog(weblog);
                    entry.setEditFormat(entryData.getEditFormat());
                }

                entry.setUpdateTime(Instant.now());
                if (PubStatus.PUBLISHED.equals(entryData.getStatus())) {
                    Instant pubTime = calculatePubTime(entryData);
                    entry.setPubTime((pubTime != null) ? pubTime : entry.getUpdateTime());

                    if (entry.getPubTime().isAfter(Instant.now().plus(1, ChronoUnit.MINUTES))) {
                        entryData.setStatus(PubStatus.SCHEDULED);
                    }
                }

                entry.setStatus(entryData.getStatus());
                entry.setTitle(entryData.getTitle());
                entry.setText(entryData.getText());
                entry.setSummary(entryData.getSummary());
                entry.setNotes(entryData.getNotes());
                if (!StringUtils.isEmpty(entryData.getTagsAsString())) {
                    entry.updateTags(new HashSet<>(Arrays.asList(entryData.getTagsAsString().trim().split("\\s+"))));
                } else {
                    entry.updateTags(new HashSet<>());
                }
                entry.setSearchDescription(entryData.getSearchDescription());
                entry.setEnclosureUrl(entryData.getEnclosureUrl());
                entry.setCategory(weblogManager.getWeblogCategory(entryData.getCategory().getId()));

                entry.setCommentDays(entryData.getCommentDays());

                if (!StringUtils.isEmpty(entry.getEnclosureUrl())) {
                    // Fetch MediaCast resource
                    log.debug("Checking MediaCast attributes");
                    AtomEnclosure enclosure;

                    try {
                        enclosure = weblogEntryManager.generateEnclosure(entry.getEnclosureUrl());
                    } catch (IllegalArgumentException e) {
                        BindException be = new BindException(entry, "new data object");
                        be.addError(new ObjectError("Enclosure URL", messages.getString(e.getMessage())));
                        return ResponseEntity.badRequest().body(ValidationError.fromBindingErrors(be));
                    }

                    // set enclosure attributes
                    entry.setEnclosureUrl(enclosure.getUrl());
                    entry.setEnclosureType(enclosure.getContentType());
                    entry.setEnclosureLength(enclosure.getLength());
                }

                weblogEntryManager.saveWeblogEntry(entry);
                persistenceStrategy.flush();

                // notify search of the new entry
                if (entry.isPublished()) {
                    indexManager.addEntryReIndexOperation(entry);
                } else if (!createNew) {
                    indexManager.removeEntryIndexOperation(entry);
                }

                // notify caches
                cacheManager.invalidate(entry);

                if (PubStatus.PENDING.equals(entry.getStatus())) {
                    mailManager.sendPendingEntryNotice(entry);
                }

                SuccessfulSaveResponse ssr = new SuccessfulSaveResponse();
                ssr.entryId = entry.getId();

                switch (entry.getStatus()) {
                    case DRAFT:
                        ssr.message = messages.getString("entryEdit.draftSaved");
                        break;
                    case PUBLISHED:
                        ssr.message = messages.getString("entryEdit.publishedEntry");
                        break;
                    case SCHEDULED:
                        ssr.message = messages.getString("entryEdit.scheduledEntry",
                                DateTimeFormatter.ISO_DATE_TIME.withZone(entry.getWeblog().getZoneId())
                                        .format(entry.getPubTime()));
                        break;
                    case PENDING:
                        ssr.message = messages.getString("entryEdit.submittedForReview");
                        break;
                    default:
                }

                return ResponseEntity.ok(ssr);
            } else {
                return ResponseEntity.status(403).body(messages.getString("error.title.403"));
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    public class SuccessfulSaveResponse {
        private String entryId;
        private String message;

        public String getEntryId() {
            return entryId;
        }

        public String getMessage() {
            return message;
        }
    }

    private Instant calculatePubTime(WeblogEntry entry) {
        Instant pubtime = null;

        String dateString = entry.getDateString();
        if (!StringUtils.isEmpty(dateString)) {
            try {
                LocalDate newDate = LocalDate.parse(dateString, pubDateFormat);

                // Now handle the time from the hour, minute and second combos
                pubtime = newDate.atTime(entry.getHours(), entry.getMinutes())
                        .atZone(entry.getWeblog().getZoneId()).toInstant();
            } catch (Exception e) {
                log.error("Error calculating pubtime", e);
            }
        }

        return pubtime;
    }

}
