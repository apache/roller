/*
 * Copyright 2019 the original author or authors.
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
package org.tightblog.bloggerui.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class GlobalConfigMetadata {
    private Map<String, String> weblogList;
    private Map<String, String> registrationOptions;
    private Map<String, String> blogHtmlLevels;
    private Map<String, String> commentOptions;
    private Map<String, String> commentHtmlLevels;
    private Map<String, String> spamOptions;

    public Map<String, String> getWeblogList() {
        if (weblogList == null) {
            weblogList = new LinkedHashMap<>();
        }
        return weblogList;
    }

    public Map<String, String> getRegistrationOptions() {
        if (registrationOptions == null) {
            registrationOptions = new LinkedHashMap<>();
        }
        return registrationOptions;
    }

    public Map<String, String> getBlogHtmlLevels() {
        if (blogHtmlLevels == null) {
            blogHtmlLevels = new LinkedHashMap<>();
        }
        return blogHtmlLevels;
    }

    public Map<String, String> getCommentOptions() {
        if (commentOptions == null) {
            commentOptions = new LinkedHashMap<>();
        }
        return commentOptions;
    }

    public Map<String, String> getCommentHtmlLevels() {
        if (commentHtmlLevels == null) {
            commentHtmlLevels = new LinkedHashMap<>();
        }
        return commentHtmlLevels;
    }

    public Map<String, String> getSpamOptions() {
        if (spamOptions == null) {
            spamOptions = new LinkedHashMap<>();
        }
        return spamOptions;
    }
}
