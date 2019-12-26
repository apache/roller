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
import org.tightblog.domain.Weblog;
import org.tightblog.service.URLService;
import org.tightblog.config.DynamicProperties;
import org.tightblog.domain.WeblogEntry;
import org.tightblog.rendering.requests.WeblogRequest;

import java.util.Map;

/**
 * Provides access to URL building functionality.
 */
@Component("urlModel")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@EnableConfigurationProperties(DynamicProperties.class)
public class URLModel implements Model {

    protected URLService urlService;
    private Weblog weblog;

    @Autowired
    public URLModel(URLService urlService) {
        this.urlService = urlService;
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
    }

    public String getSiteHome() {
        return urlService.getHomeURL();
    }

    public String getLoginURL() {
        return urlService.getLoginURL();
    }

    public String getLogoutURL() {
        return urlService.getLogoutURL();
    }

    public String getRegisterURL() {
        return urlService.getRegisterURL();
    }

    public String getWeblogHome() {
        return urlService.getWeblogURL(weblog);
    }

    public String getEntriesURLForCategory(String category) {
        return urlService.getWeblogCollectionURL(weblog, category, null, null, -1);
    }

    public String getEntriesURLForTag(String tag, String category) {
        return urlService.getWeblogCollectionURL(weblog, category, null, tag, -1);
    }

    public String getEntriesURLForDate(String date) {
        return urlService.getWeblogCollectionURL(weblog, null, date, null, -1);
    }

    public String getURL(WeblogEntry entry) {
        return urlService.getWeblogEntryURL(entry);
    }

    public String getNewEntryURL() {
        return urlService.getNewEntryURL(weblog.getId());
    }

    public String getEntryEditURL(WeblogEntry entry) {
        if (entry != null) {
            return urlService.getEntryEditURL(entry);
        }
        return null;
    }

    public String getCommentURL(WeblogEntry entry, String timeStamp) {
        return urlService.getCommentURL(entry, timeStamp);
    }

    public String getWeblogEntryPostCommentURL(WeblogEntry entry) {
        return urlService.getWeblogEntryPostCommentURL(entry);
    }

    public String getCommentAuthenticatorURL() {
        return urlService.getCommentAuthenticatorURL();
    }

    public String getCommentsURL(WeblogEntry entry) {
        return urlService.getWeblogEntryCommentsURL(entry);
    }

    public String getSearchURL() {
        return urlService.getWeblogSearchURL(weblog, null, null, -1);
    }

    public String getCustomPageURL(String pageLink) {
        return urlService.getCustomPageURL(weblog, pageLink, null);
    }

    public String getThemeResourceURL(String filePath) {
        return urlService.getThemeResourceURL(weblog.getTheme(), filePath);
    }

    public String getConfigURL() {
        return urlService.getWeblogConfigURL(weblog.getId());
    }

    public String getAtomFeedURL() {
        return urlService.getAtomFeedURL(weblog);
    }

    public String getAtomFeedURLForCategory(String category) {
        return urlService.getAtomFeedURLForCategory(weblog, category);
    }

    public String getAtomFeedURLForTag(String tag) {
        return urlService.getAtomFeedURLForTag(weblog, tag);
    }
}
