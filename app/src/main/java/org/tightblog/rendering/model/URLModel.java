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
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogEntry;
import org.tightblog.rendering.requests.WeblogRequest;

import java.util.Map;

/**
 * Provides access to URL building functionality.
 * <p>
 * NOTE: We purposely go against the standard getter/setter bean standard
 * for several methods that take arguments so that template designers get a more
 * consistent way to access those methods in their templates. e.g.
 * <p>
 * $url.login and $url.category("foo") instead of $url.getCategory("foo")
 */
@Component("urlModel")
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class URLModel implements Model {

    private Weblog weblog;
    private boolean preview;

    @Autowired
    protected URLStrategy urlStrategy;

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

    public String getSite() {
        return urlStrategy.getHomeURL();
    }

    public String getLogin() {
        return urlStrategy.getLoginURL();
    }

    public String getLogout() {
        return urlStrategy.getLogoutURL();
    }

    public String getRegister() {
        return urlStrategy.getRegisterURL();
    }

    public String getWeblogHome() {
        return urlStrategy.getWeblogURL(weblog);
    }

    public String getWeblogHome(Weblog blog) {
        return urlStrategy.getWeblogURL(blog);
    }

    public String category(String catName) {
        return urlStrategy.getWeblogCollectionURL(weblog, catName, null, null, -1);
    }

    public String tag(String tag) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, null, tag, -1);
    }

    public String date(String date) {
        return urlStrategy.getWeblogCollectionURL(weblog, null, date, null, -1);
    }

    public String entry(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryURL(entry);
    }

    public String getCreateEntry() {
        return urlStrategy.getEntryAddURL(weblog.getId());
    }

    public String editEntry(WeblogEntry entry) {
        if (entry != null) {
            return urlStrategy.getEntryEditURL(entry);
        }
        return null;
    }

    public String comment(WeblogEntry entry, String timeStamp) {
        return urlStrategy.getWeblogCommentURL(entry, timeStamp);
    }

    public String entryComment(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryCommentURL(entry, false);
    }

    public String entryCommentPreview(WeblogEntry entry) {
        return urlStrategy.getWeblogEntryCommentURL(entry, true);
    }

    public String getCommentAuthenticator() {
        return urlStrategy.getCommentAuthenticatorURL();
    }

    public String comments(WeblogEntry entry) {
        return urlStrategy.getWeblogCommentsURL(entry);
    }

    public String getSearch() {
        return urlStrategy.getWeblogSearchURL(weblog, null, null, -1);
    }

    public String page(String pageLink) {
        return urlStrategy.getCustomPageURL(weblog, pageLink, null);
    }

    public String themeResource(String theme, String filePath) {
        return urlStrategy.getThemeResourceURL(theme, filePath);
    }

    public String getWeblogConfigURL() {
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
