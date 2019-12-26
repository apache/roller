/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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
package org.tightblog.rendering.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.tightblog.rendering.service.CalendarGenerator;
import org.tightblog.rendering.service.WeblogEntryListGenerator;
import org.tightblog.service.ThemeManager;
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.service.LuceneIndexer;
import org.tightblog.dao.WeblogEntryDao;

/**
 * Extends normal page renderer model to represent search results.
 * <p>
 * Also adds some methods which are specific only to search results.
 */
@Component
public class SearchResultsModel extends PageModel {

    private WeblogEntryDao weblogEntryDao;
    private LuceneIndexer luceneIndexer;

    @Autowired
    SearchResultsModel(
            UserManager userManager,
            WeblogManager weblogManager,
            WeblogEntryManager weblogEntryManager,
            ThemeManager themeManager,
            WeblogEntryListGenerator weblogEntryListGenerator,
            CalendarGenerator calendarGenerator,
            @Value("${site.pages.maxEntries:30}") int maxEntriesPerPage,
            WeblogEntryDao weblogEntryDao,
            LuceneIndexer luceneIndexer) {

        super(userManager, weblogManager, weblogEntryManager, themeManager, weblogEntryListGenerator,
                calendarGenerator, maxEntriesPerPage);

        this.weblogEntryDao = weblogEntryDao;
        this.luceneIndexer = luceneIndexer;
    }

    public WeblogEntryDao getWeblogEntryDao() {
        return weblogEntryDao;
    }

    public LuceneIndexer getLuceneIndexer() {
        return luceneIndexer;
    }
}
