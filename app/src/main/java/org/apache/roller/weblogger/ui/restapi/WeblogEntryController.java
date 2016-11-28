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

import org.apache.roller.weblogger.business.URLStrategy;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogEntry;
import org.apache.roller.weblogger.pojos.WeblogEntrySearchCriteria;
import org.apache.roller.weblogger.pojos.WeblogEntryTagAggregate;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.I18nMessages;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.security.Principal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping(path = "/tb-ui/authoring/rest/weblogentries")
public class WeblogEntryController {

    private static Logger log = LoggerFactory.getLogger(WeblogController.class);

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
            data.entries.addAll(rawEntries);

            if (rawEntries.size() > ITEMS_PER_PAGE) {
                data.entries.remove(data.entries.size() - 1);
                data.hasMore = true;
            }

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
        Locale userLocale = (user == null) ? Locale.getDefault() : Locale.forLanguageTag(user.getLocale());
        I18nMessages messages = I18nMessages.getMessages(userLocale);

        Weblog weblog = weblogManager.getWeblog(weblogId);

        if (weblog != null && userManager.checkWeblogRole(user, weblog, WeblogRole.OWNER)) {
            WeblogEntrySearchFields fields = new WeblogEntrySearchFields();

            // categories
            fields.categories = new LinkedHashMap<>();
            fields.categories.put("", "(Any)");
            for (WeblogCategory cat : weblog.getWeblogCategories()) {
                fields.categories.put(cat.getName(), cat.getName());
            }

            // sort by options
            fields.sortByOptions = new LinkedHashMap<>();
            fields.sortByOptions.put(WeblogEntrySearchCriteria.SortBy.PUBLICATION_TIME.name(), messages.getString("weblogEntryQuery.label.pubTime"));
            fields.sortByOptions.put(WeblogEntrySearchCriteria.SortBy.UPDATE_TIME.name(), messages.getString("weblogEntryQuery.label.updateTime"));

            // status options
            fields.statusOptions = new LinkedHashMap<>();
            fields.statusOptions.put("", messages.getString("weblogEntryQuery.label.allEntries"));
            fields.statusOptions.put("DRAFT", messages.getString("weblogEntryQuery.label.draftOnly"));
            fields.statusOptions.put("PUBLISHED", messages.getString("weblogEntryQuery.label.publishedOnly"));
            fields.statusOptions.put("PENDING", messages.getString("weblogEntryQuery.label.pendingOnly"));
            fields.statusOptions.put("SCHEDULED", messages.getString("weblogEntryQuery.label.scheduledOnly"));

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
            WeblogEntry itemToRemove = weblogEntryManager.getWeblogEntry(id);
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
            tags = weblogEntryManager.getTags(weblog, null, prefix, 0, MAX_TAGS);

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
    private List<RecentWeblogEntryData> getRecentEntries(@PathVariable String weblogId,
                                                         @PathVariable WeblogEntry.PubStatus pubStatus,
                                               Principal p, HttpServletResponse response) {


        Weblog weblog = weblogManager.getWeblog(weblogId);
        if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.POST)) {
            WeblogEntrySearchCriteria wesc = new WeblogEntrySearchCriteria();
            wesc.setWeblog(weblog);
            wesc.setMaxResults(20);
            wesc.setStatus(pubStatus);
            List<WeblogEntry> entries = weblogEntryManager.getWeblogEntries(wesc);
            List<RecentWeblogEntryData> recentEntries = new ArrayList<>();
            for (WeblogEntry entry : entries) {
                RecentWeblogEntryData recentEntry = new RecentWeblogEntryData();
                recentEntry.setTitle(entry.getTitle());
                recentEntry.setEditUrl(urlStrategy.getEntryEditURL(weblog.getId(), entry.getId(), true));
                recentEntries.add(recentEntry);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return recentEntries;
        } else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
    }

    private static class RecentWeblogEntryData {
        private String title;
        private String editUrl;

        public RecentWeblogEntryData() {
        }

        public String getTitle() {
            return title;
        }

        public String getEditUrl() {
            return editUrl;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public void setEditUrl(String editUrl) {
            this.editUrl = editUrl;
        }
    }

}
