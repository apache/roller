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
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.tightblog.business.URLStrategyImpl;
import org.tightblog.business.URLStrategy;
import org.tightblog.business.WeblogEntryManager;
import org.tightblog.business.WebloggerStaticConfig;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.rendering.requests.WeblogRequest;

import java.util.Map;

/**
 * Provides access to URL building functionality.
 * <p>
 * NOTE: we purposely go against the standard getter/setter bean standard
 * for methods that take arguments so that users get a consistent way to
 * access those methods in their templates. i.e.
 * <p>
 * $url.category("foo") instead of $url.getCategory("foo")
 */
@Component("urlModel")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class URLModel implements Model {

    protected Weblog weblog;

    @Autowired
    private WeblogEntryManager weblogEntryManager;

    @Autowired
    protected URLStrategy urlStrategy;

    private boolean preview;

    public void setWeblogEntryManager(WeblogEntryManager weblogEntryManager) {
        this.weblogEntryManager = weblogEntryManager;
    }

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
            this.urlStrategy = new URLStrategyImpl(weblog.getTheme(), weblog.isUsedForThemePreview());
        }

    }

    /**
     * Relative URL of weblogger, e.g. /tightblog
     */
    public String getSite() {
        return WebloggerStaticConfig.getRelativeContextURL();
    }

    /**
     * Absolute URL of weblogger, e.g. http://localhost:8080/tightblog
     */
    public String getAbsoluteSite() {
        return WebloggerStaticConfig.getAbsoluteContextURL();
    }

    /**
     * URL for logging in
     */
    public String getLogin() {
        return urlStrategy.getLoginURL(false);
    }

    /**
     * URL for logging out
     */
    public String getLogout() {
        return urlStrategy.getLogoutURL(false);
    }

    /**
     * URL for registering
     */
    public String getRegister() {
        return urlStrategy.getRegisterURL(false);
    }

    public String getCommentAuthenticator() {
        return getSite() + "/tb-ui/rendering/comment/authform";
    }

    public String themeResource(String theme, String filePath) {
        return getSite() + "/blogthemes/" + theme + "/" + filePath;
    }

    public String getHome() {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, null, -1, true);
    }

    public String entry(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryURL(entry, true);
    }

    public String entryComment(String anchor) {
        return urlStrategy.getWeblogEntryCommentURL(weblog, anchor, false);
    }

    public String entryCommentPreview(String anchor) {
        return urlStrategy.getWeblogEntryCommentURL(weblog, anchor, true);
    }

    public String comment(WeblogEntry entry, String timeStamp) {
        return urlStrategy.getWeblogCommentURL(entry, timeStamp);
    }

    public String comments(WeblogEntry entry) {
        return urlStrategy.getWeblogCommentsURL(entry);
    }

    public String category(String catName) {
        return urlStrategy.getWeblogCollectionURL(weblog, catName, null, null, -1, true);
    }

    public String tag(String tag) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, tag, -1, true);
    }

    public String date(String date) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, date, null, -1, true);
    }

    public String getSearch() {
        return urlStrategy.getWeblogSearchURL(weblog, null, null, -1, false);
    }

    public String page(String pageLink) {
        return urlStrategy.getCustomPageURL(weblog, pageLink, null, true);
    }

    public String getAtomFeed() {
        return urlStrategy.getWeblogFeedURL(weblog, null, null);
    }

    public String getAtomFeedByCategory(String category) {
        return urlStrategy.getWeblogFeedURL(weblog, category, null);
    }

    public String getAtomFeedByTag(String tag) {
        return urlStrategy.getWeblogFeedURL(weblog, null, tag);
    }


    /**
     * URL for editing a weblog entry
     */
    public String editEntry(String anchor) {
        // need to determine entryId from anchor
        WeblogEntry entry = weblogEntryManager.getWeblogEntryByAnchor(weblog, anchor);
        if (entry != null) {
            return urlStrategy.getEntryEditURL(weblog.getId(), entry.getId(), false);
        }
        return null;
    }

    /**
     * URL for creating a new weblog entry
     */
    public String getCreateEntry() {
        return urlStrategy.getEntryAddURL(weblog.getId(), false);
    }

    /**
     * URL for editing weblog settings
     */
    public String getEditSettings() {
        return urlStrategy.getWeblogConfigURL(weblog.getId(), false);
    }

}
