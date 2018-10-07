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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.business.URLStrategyImpl;
import org.tightblog.business.URLStrategy;
import org.tightblog.config.DynamicProperties;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.rendering.requests.WeblogRequest;

import java.util.Map;

/**
 * Provides access to URL building functionality.
 */
@Component("urlModel")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@EnableConfigurationProperties(DynamicProperties.class)
public class URLModel implements Model {

    private Weblog weblog;
    private boolean preview;

    @Autowired
    protected URLStrategy urlStrategy;

    @Autowired
    private DynamicProperties dp;

    public void setUrlStrategy(URLStrategy urlStrategy) {
        this.urlStrategy = urlStrategy;
    }

    public URLModel() {
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }

    @Override
    public String getModelName() {
        return "url";
    }

    /**
     * Init page model, requires a WeblogRequest object.
     */
    @Override
    public void init(Map<String, Object> initData) {
        WeblogRequest weblogRequest = (WeblogRequest) initData.get("parsedRequest");
        if (weblogRequest == null) {
            throw new IllegalStateException("Expected 'parsedRequest' init param!");
        }

        this.weblog = weblogRequest.getWeblog();

        if (preview) {
            this.urlStrategy = new URLStrategyImpl(weblog.getTheme(), weblog.isUsedForThemePreview(), dp);
        }
    }

    public String getSiteHome() {
        return urlStrategy.getHomeURL();
    }

    public String getLoginURL() {
        return urlStrategy.getLoginURL();
    }

    public String getLogoutURL() {
        return urlStrategy.getLogoutURL();
    }

    public String getRegisterURL() {
        return urlStrategy.getRegisterURL();
    }

    public String getWeblogHome() {
        return urlStrategy.getWeblogURL(weblog);
    }

    public String getEntriesURLForCategory(String category) {
        return urlStrategy.getWeblogCollectionURL(weblog, category, null, null, -1);
    }

    public String getEntriesURLForTag(String tag, String category) {
        return urlStrategy.getWeblogCollectionURL(weblog, category, null, tag, -1);
    }

    public String getEntriesURLForDate(String date) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, date, null, -1);
    }

    public String getURL(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryURL(entry);
    }

    public String getNewEntryURL() {
        return urlStrategy.getNewEntryURL(weblog.getId());
    }

    public String getEntryEditURL(WeblogEntry entry) {
        if (entry != null) {
            return urlStrategy.getEntryEditURL(entry);
        }
        return null;
    }

    public String getCommentURL(WeblogEntry entry, String timeStamp) {
        return urlStrategy.getCommentURL(entry, timeStamp);
    }

    public String getWeblogEntryPostCommentURL(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryPostCommentURL(entry, false);
    }

    public String getWeblogEntryPreviewCommentURL(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryPostCommentURL(entry, true);
    }

    public String getCommentAuthenticatorURL() {
        return urlStrategy.getCommentAuthenticatorURL();
    }

    public String getCommentsURL(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryCommentsURL(entry);
    }

    public String getSearchURL() {
        return urlStrategy.getWeblogSearchURL(weblog, null, null, -1);
    }

    public String getCustomPageURL(String pageLink) {
        return urlStrategy.getCustomPageURL(weblog, pageLink, null);
    }

    public String getThemeResourceURL(String theme, String filePath) {
        return urlStrategy.getThemeResourceURL(theme, filePath);
    }

    public String getConfigURL() {
        return urlStrategy.getWeblogConfigURL(weblog.getId());
    }

    public String getAtomFeedURL() {
        return urlStrategy.getAtomFeedURL(weblog);
    }

    public String getAtomFeedURLForCategory(String category) {
        return urlStrategy.getAtomFeedURLForCategory(weblog, category);
    }

    public String getAtomFeedURLForTag(String tag) {
        return urlStrategy.getAtomFeedURLForTag(weblog, tag);
    }
}
