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
package org.tightblog.ui.restapi;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.tightblog.business.MailManager;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.search.IndexManager;
import org.tightblog.pojos.AtomEnclosure;
import org.tightblog.pojos.User;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogEntry.PubStatus;
import org.tightblog.pojos.WeblogEntrySearchCriteria;
import org.tightblog.pojos.WeblogEntryTagAggregate;
import org.tightblog.pojos.WeblogRole;
import org.tightblog.pojos.WebloggerProperties;
import org.tightblog.repository.UserRepository;
import org.tightblog.repository.WeblogCategoryRepository;
import org.tightblog.repository.WeblogRepository;
import org.tightblog.repository.WebloggerPropertiesRepository;
import org.tightblog.util.Utilities;
import org.tightblog.util.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    private static Logger log = LoggerFactory.getLogger(WeblogEntryController.class);

    private static DateTimeFormatter pubDateFormat = DateTimeFormatter.ofPattern("M/d/yyyy");

    private WeblogRepository weblogRepository;
    private WeblogCategoryRepository weblogCategoryRepository;
    private UserRepository userRepository;
    private UserManager userManager;
    private WeblogManager weblogManager;
    private WeblogEntryManager weblogEntryManager;
    private IndexManager indexManager;
    private URLStrategy urlStrategy;
    private MailManager mailManager;
    private MessageSource messages;
    private WebloggerPropertiesRepository webloggerPropertiesRepository;

    @Autowired
    public WeblogEntryController(WeblogRepository weblogRepository, WeblogCategoryRepository weblogCategoryRepository,
                                 UserRepository userRepository, UserManager userManager, WeblogManager weblogManager,
                                 WeblogEntryManager weblogEntryManager, IndexManager indexManager,
                                 URLStrategy urlStrategy, MailManager mailManager, MessageSource messages,
                                 WebloggerPropertiesRepository webloggerPropertiesRepository) {
        this.weblogRepository = weblogRepository;
        this.weblogCategoryRepository = weblogCategoryRepository;
        this.userRepository = userRepository;
        this.userManager = userManager;
        this.weblogManager = weblogManager;
        this.webloggerPropertiesRepository = webloggerPropertiesRepository;
        this.weblogEntryManager = weblogEntryManager;
        this.indexManager = indexManager;
        this.urlStrategy = urlStrategy;
        this.mailManager = mailManager;
        this.messages = messages;
    }

    // number of entries to show per page
    private static final int ITEMS_PER_PAGE = 30;

    // Max Tags to show for autocomplete
    @Value("${max.autocomplete.tags:20}")
    private int maxAutocompleteTags;

    @PostMapping(value = "/{weblogId}/page/{page}")
    public WeblogEntryData getWeblogEntries(@PathVariable String weblogId, @PathVariable int page,
                                              @RequestBody WeblogEntrySearchCriteria criteria, Principal principal,
                                              HttpServletResponse response) {

        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
        if (weblog != null && userManager.checkWeblogRole(principal.getName(), weblog, WeblogRole.OWNER)) {

            WeblogEntryData data = new WeblogEntryData();

            criteria.setWeblog(weblog);
            criteria.setOffset(page * ITEMS_PER_PAGE);
            criteria.setMaxResults(ITEMS_PER_PAGE + 1);
            criteria.setCalculatePermalinks(true);
            List<WeblogEntry> rawEntries = weblogEntryManager.getWeblogEntries(criteria);
            data.entries = new ArrayList<>();
            data.entries.addAll(rawEntries.stream()
                    .peek(re -> re.setWeblog(null))
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
        boolean hasMore;

        public List<WeblogEntry> getEntries() {
            return entries;
        }

        public boolean isHasMore() {
            return hasMore;
        }
    }

    @GetMapping(value = "/{weblogId}/searchfields")
    public WeblogEntrySearchFields getWeblogEntrySearchFields(@PathVariable String weblogId, Principal principal,
                                                              HttpServletResponse response, Locale locale) {

        // Get user permissions and locale
        User user = userRepository.findEnabledByUserName(principal.getName());
        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);

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
            fields.sortByOptions.put(WeblogEntrySearchCriteria.SortBy.PUBLICATION_TIME.name(),
                    messages.getMessage("entries.label.pubTime", null, locale));
            fields.sortByOptions.put(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME.name(),
                    messages.getMessage("entries.label.updateTime", null, locale));

            // status options
            fields.statusOptions = new LinkedHashMap<>();
            fields.statusOptions.put("", messages.getMessage("entries.label.allEntries", null, locale));
            fields.statusOptions.put("DRAFT", messages.getMessage("entries.label.draftOnly", null, locale));
            fields.statusOptions.put("PUBLISHED", messages.getMessage("entries.label.publishedOnly", null, locale));
            fields.statusOptions.put("PENDING", messages.getMessage("entries.label.pendingOnly", null, locale));
            fields.statusOptions.put("SCHEDULED", messages.getMessage("entries.label.scheduledOnly", null, locale));
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

    @DeleteMapping(value = "/{id}")
    public void deleteWeblogEntry(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogEntry itemToRemove = weblogEntryManager.getWeblogEntry(id, false);
            if (itemToRemove != null) {
                Weblog weblog = itemToRemove.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.POST)) {
                    // remove from search index
                    if (itemToRemove.isPublished()) {
                        indexManager.updateIndex(itemToRemove, true);
                    }

                    // update last weblog change so any site weblog knows it needs to update
                    WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
                    props.setLastWeblogChange(Instant.now());
                    webloggerPropertiesRepository.saveAndFlush(props);

                    // remove entry itself
                    weblogEntryManager.removeWeblogEntry(itemToRemove);

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

    @GetMapping(value = "/{id}/tagdata")
    public WeblogTagData getWeblogTagData(@PathVariable String id, @RequestParam("prefix") String prefix)
            throws ServletException {

        List<WeblogEntryTagAggregate> tags;

        try {
            Weblog weblog = weblogRepository.findById(id).orElse(null);
            tags = weblogManager.getTags(weblog, null, prefix, 0, maxAutocompleteTags);

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

        WeblogTagData() {
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

    @GetMapping(value = "/{weblogId}/recententries/{pubStatus}")
    private List<WeblogEntry> getRecentEntries(@PathVariable String weblogId,
                                                         @PathVariable WeblogEntry.PubStatus pubStatus,
                                               Principal p, HttpServletResponse response) {

        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);
        WeblogRole minimumRole = (pubStatus == PubStatus.DRAFT || pubStatus == PubStatus.PENDING) ?
                WeblogRole.EDIT_DRAFT : WeblogRole.POST;
        if (userManager.checkWeblogRole(p.getName(), weblog, minimumRole)) {
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setMaxResults(20);
            wesc.setStatus(pubStatus);
            List<WeblogEntry> entries = weblogEntryManager.getWeblogEntries(wesc);
            List<WeblogEntry> recentEntries = entries.stream().map(e -> new WeblogEntry(e.getTitle(),
                    urlStrategy.getEntryEditURL(e))).collect(Collectors.toList());
            response.setStatus(HttpServletResponse.SC_OK);
            return recentEntries;
        } else if (WeblogRole.POST.equals(minimumRole) &&
                userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.EDIT_DRAFT)) {
            // contributors get empty array for certain pub statuses
            response.setStatus(HttpServletResponse.SC_OK);
            return new ArrayList<>();
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    @GetMapping(value = "/{id}")
    public WeblogEntry getWeblogEntry(@PathVariable String id, Principal p, HttpServletResponse response)
            throws ServletException {
        try {
            WeblogEntry entry = weblogEntryManager.getWeblogEntry(id, true);
            if (entry != null) {
                Weblog weblog = entry.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog, WeblogRole.EDIT_DRAFT)) {
                    entry.setCommentsUrl(urlStrategy.getCommentManagementURL(weblog.getId(), entry.getId()));
                    entry.setPermalink(urlStrategy.getWeblogEntryURL(entry));
                    entry.setPreviewUrl(urlStrategy.getWeblogEntryDraftPreviewURL(entry));

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

    @GetMapping(value = "/{weblogId}/entryeditmetadata")
    public EntryEditMetadata getEntryEditMetadata(@PathVariable String weblogId, Principal principal,
                                                              Locale locale, HttpServletResponse response) {

        // Get user permissions and locale
        User user = userRepository.findEnabledByUserName(principal.getName());
        Weblog weblog = weblogRepository.findById(weblogId).orElse(null);

        if (weblog != null && userManager.checkWeblogRole(user, weblog, WeblogRole.EDIT_DRAFT)) {
            EntryEditMetadata fields = new EntryEditMetadata();

            // categories
            fields.categories = new LinkedHashMap<>();
            for (WeblogCategory cat : weblog.getWeblogCategories()) {
                fields.categories.put(cat.getId(), cat.getName());
            }

            fields.author = userManager.checkWeblogRole(user, weblog, WeblogRole.POST);
            fields.commentingEnabled = !WebloggerProperties.CommentPolicy.NONE.equals(
                    webloggerPropertiesRepository.findOrNull().getCommentPolicy()) &&
                    !WebloggerProperties.CommentPolicy.NONE.equals(weblog.getAllowComments());
            fields.defaultCommentDays = weblog.getDefaultCommentDays();
            fields.defaultEditFormat = weblog.getEditFormat();
            fields.timezone = weblog.getTimeZone();

            for (Weblog.EditFormat format : Weblog.EditFormat.values()) {
                fields.editFormatDescriptions.put(format, messages.getMessage(format.getDescriptionKey(), null, locale));
            }

            // comment day options
            fields.commentDayOptions = Arrays.stream(WeblogEntry.CommentDayOption.values())
                    .collect(Utilities.toLinkedHashMap(cdo -> Integer.toString(cdo.getDays()),
                            cdo -> messages.getMessage(cdo.getDescriptionKey(), null, locale)));

            return fields;
        } else {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return null;
        }

    }

    public class EntryEditMetadata {
        Map<String, String> categories;
        Map<String, String> commentDayOptions;
        boolean author;
        boolean commentingEnabled;
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
    @PostMapping(value = "/{weblogId}/entries")
    public ResponseEntity postEntry(@PathVariable String weblogId, @Valid @RequestBody WeblogEntry entryData,
                                       Locale locale, Principal p) throws ServletException {

        try {

            boolean createNew = false;
            WeblogEntry entry = null;

            if (entryData.getId() != null) {
                entry = weblogEntryManager.getWeblogEntry(entryData.getId(), false);
            }

            // Check user permissions
            User user = userRepository.findEnabledByUserName(p.getName());
            Weblog weblog = (entry == null) ? weblogRepository.findById(weblogId).orElse(null)
                    : entry.getWeblog();

            WeblogRole necessaryRole = (PubStatus.PENDING.equals(entryData.getStatus()) ||
                    PubStatus.DRAFT.equals(entryData.getStatus())) ? WeblogRole.EDIT_DRAFT : WeblogRole.POST;
            if (weblog != null && userManager.checkWeblogRole(user, weblog, necessaryRole)) {

                // create new?
                if (entry == null) {
                    createNew = true;
                    entry = new WeblogEntry();
                    entry.setCreator(user);
                    entry.setWeblog(weblog);
                    entry.setEditFormat(entryData.getEditFormat());
                    entryData.setWeblog(weblog);
                }

                entry.setUpdateTime(Instant.now());
                Instant pubTime = calculatePubTime(entryData);
                entry.setPubTime((pubTime != null) ? pubTime : entry.getUpdateTime());

                if (PubStatus.PUBLISHED.equals(entryData.getStatus()) &&
                        entry.getPubTime().isAfter(Instant.now().plus(1, ChronoUnit.MINUTES))) {
                    entryData.setStatus(PubStatus.SCHEDULED);
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
                WeblogCategory category = weblogCategoryRepository.findById(entryData.getCategory().getId()).orElse(null);
                if (category != null) {
                    entry.setCategory(category);
                } else {
                    throw new IllegalArgumentException("Category is invalid.");
                }

                entry.setCommentDays(entryData.getCommentDays());

                if (!StringUtils.isEmpty(entry.getEnclosureUrl())) {
                    // Fetch MediaCast resource
                    log.debug("Checking MediaCast attributes");
                    AtomEnclosure enclosure;

                    try {
                        enclosure = weblogEntryManager.generateEnclosure(entry.getEnclosureUrl());
                    } catch (IllegalArgumentException e) {
                        BindException be = new BindException(entry, "new data object");
                        be.addError(new ObjectError("Enclosure URL", messages.getMessage(e.getMessage(), null,
                                locale)));
                        return ResponseEntity.badRequest().body(ValidationError.fromBindingErrors(be));
                    }

                    // set enclosure attributes
                    entry.setEnclosureUrl(enclosure.getUrl());
                    entry.setEnclosureType(enclosure.getContentType());
                    entry.setEnclosureLength(enclosure.getLength());
                }

                weblogEntryManager.saveWeblogEntry(entry);

                // update last weblog change so any site weblog knows it needs to update
                WebloggerProperties props = webloggerPropertiesRepository.findOrNull();
                props.setLastWeblogChange(Instant.now());
                webloggerPropertiesRepository.saveAndFlush(props);

                // notify search of the new entry
                if (entry.isPublished()) {
                    indexManager.updateIndex(entry, false);
                } else if (!createNew) {
                    indexManager.updateIndex(entry, true);
                }

                if (PubStatus.PENDING.equals(entry.getStatus())) {
                    mailManager.sendPendingEntryNotice(entry);
                }

                SuccessfulSaveResponse ssr = new SuccessfulSaveResponse();
                ssr.entryId = entry.getId();

                switch (entry.getStatus()) {
                    case DRAFT:
                        ssr.message = messages.getMessage("entryEdit.draftSaved", null, locale);
                        break;
                    case PUBLISHED:
                        ssr.message = messages.getMessage("entryEdit.publishedEntry", null, locale);
                        break;
                    case SCHEDULED:
                        ssr.message = messages.getMessage("entryEdit.scheduledEntry",
                                new Object[] {DateTimeFormatter.ISO_DATE_TIME.withZone(entry.getWeblog().getZoneId())
                                        .format(entry.getPubTime())}, null, locale);
                        break;
                    case PENDING:
                        ssr.message = messages.getMessage("entryEdit.submittedForReview", null, locale);
                        break;
                    default:
                }

                return ResponseEntity.ok(ssr);
            } else {
                return ResponseEntity.status(403).body(messages.getMessage("error.title.403", null, locale));
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
