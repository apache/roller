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
package org.tightblog.rendering.cache;

import org.tightblog.business.themes.ThemeManager;
import org.tightblog.pojos.WeblogBookmark;
import org.tightblog.pojos.WeblogEntryComment;
import org.tightblog.pojos.User;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.pojos.WeblogTemplate;
import org.tightblog.pojos.Weblog;

import javax.annotation.PostConstruct;
import java.time.Instant;

/**
 * Cache for site-wide weblog content.  The site weblog needs a different type of
 * cache from regular weblogs because only certain changes from a regular weblog
 * (for example, new weblog entry) need to trigger a refresh to the site weblog,
 * while other changes from regular weblogs do not alter the appearance of the site
 * weblog and hence the latter's pages can remain as-is in the cache.
 */
public class SiteWideCache extends ExpiringCache implements BlogEventListener {

    // for 304 Not Modified calculations
    private Instant lastUpdateTime = null;

    private ThemeManager themeManager;

    public void setThemeManager(ThemeManager themeManager) {
        this.themeManager = themeManager;
    }

    @PostConstruct
    public void init() {
        super.init();
        cacheManager.registerHandler(this);
    }

    public Instant getLastModified() {
        // first try our cached version
        if (lastUpdateTime == null) {
            lastUpdateTime = Instant.now();
        }

        return lastUpdateTime;
    }

    @Override
    public void weblogChanged(Weblog weblog) {
        if (enabled) {
            this.contentCache.invalidateAll();
            this.lastUpdateTime = null;
        }
    }

    @Override
    public void entryChanged(WeblogEntry entry) {
        weblogChanged(entry.getWeblog());
    }

    @Override
    public void userChanged(User user) {
        // ignored
    }

    /**
     * Trigger a weblog change only if the bookmark belongs to the site blog.
     */
    @Override
    public void bookmarkChanged(WeblogBookmark bookmark) {
        if (enabled && themeManager.getSharedTheme(bookmark.getWeblog().getTheme()).isSiteWide()) {
            weblogChanged(bookmark.getWeblog());
        }
    }

    /**
     * Trigger a weblog change only if the comment belongs to the site blog.
     */
    @Override
    public void commentChanged(WeblogEntryComment comment) {
        if (enabled && themeManager.getSharedTheme(comment.getWeblogEntry().getWeblog().getTheme()).isSiteWide()) {
            weblogChanged(comment.getWeblogEntry().getWeblog());
        }
    }

    /**
     * Trigger a weblog change only if the category belongs to the site blog.
     */
    @Override
    public void categoryChanged(WeblogCategory category) {
        if (enabled && themeManager.getSharedTheme(category.getWeblog().getTheme()).isSiteWide()) {
            weblogChanged(category.getWeblog());
        }
    }

    /**
     * Trigger a weblog change only if the template belongs to the site blog.
     */
    @Override
    public void templateChanged(WeblogTemplate template) {
        if (enabled && themeManager.getSharedTheme(template.getWeblog().getTheme()).isSiteWide()) {
            weblogChanged(template.getWeblog());
        }
    }
}
