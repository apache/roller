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
import org.tightblog.service.UserManager;
import org.tightblog.service.WeblogEntryManager;
import org.tightblog.service.WeblogManager;
import org.tightblog.service.ThemeManager;
import org.tightblog.domain.Weblog;
import org.tightblog.domain.WeblogEntryTagAggregate;
import org.tightblog.rendering.service.CalendarGenerator;
import org.tightblog.rendering.service.WeblogEntryListGenerator;

import java.util.List;

/**
 * Model which provides information needed to render a weblog page.
 */
@Component
public class PageModel {

    private UserManager userManager;
    private WeblogManager weblogManager;
    private WeblogEntryManager weblogEntryManager;
    protected ThemeManager themeManager;
    private WeblogEntryListGenerator weblogEntryListGenerator;
    private CalendarGenerator calendarGenerator;
    private int maxEntriesPerPage;

    @Autowired
    public PageModel(
            UserManager userManager,
            WeblogManager weblogManager,
            WeblogEntryManager weblogEntryManager,
            ThemeManager themeManager,
            WeblogEntryListGenerator weblogEntryListGenerator,
            CalendarGenerator calendarGenerator,
            @Value("${site.pages.maxEntries:30}") int maxEntriesPerPage) {

        this.userManager = userManager;
        this.weblogManager = weblogManager;
        this.weblogEntryManager = weblogEntryManager;
        this.themeManager = themeManager;
        this.weblogEntryListGenerator = weblogEntryListGenerator;
        this.calendarGenerator = calendarGenerator;
        this.maxEntriesPerPage = maxEntriesPerPage;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public WeblogEntryManager getWeblogEntryManager() {
        return weblogEntryManager;
    }

    public ThemeManager getThemeManager() {
        return themeManager;
    }

    public WeblogEntryListGenerator getWeblogEntryListGenerator() {
        return weblogEntryListGenerator;
    }

    public CalendarGenerator getCalendarGenerator() {
        return calendarGenerator;
    }

    public int getMaxEntriesPerPage() {
        return maxEntriesPerPage;
    }

    /**
     * Adds a tracking code for website analytics (e.g. Google Analytics). Will use the blog-defined
     * tracking code if defined and permitted by the installation, else the server-defined tracking
     * code if defined will be used.
     */
    public String getAnalyticsTrackingCode(Weblog weblog, boolean preview) {
        if (preview) {
            return "";
        } else {
            return weblogManager.getAnalyticsTrackingCode(weblog);
        }
    }

    public List<WeblogEntryTagAggregate> getPopularTags(Weblog weblog, int length) {
        return weblogManager.getPopularTags(weblog, 0, length);
    }
}
