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

import org.tightblog.domain.Weblog;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class EntryEditMetadata {
    private Map<String, String> categories;
    private Map<String, String> commentDayOptions;
    private boolean author;
    private boolean commentingEnabled;
    private int defaultCommentDays = -1;
    private Weblog.EditFormat defaultEditFormat;
    private Map<String, String> editFormats;
    private String timezone;

    // getters needed for JSON serialization: http://stackoverflow.com/a/35822500
    public Map<String, String> getCategories() {
        if (categories == null) {
            categories = new LinkedHashMap<>();
        }
        return categories;
    }

    public Map<String, String> getCommentDayOptions() {
        if (commentDayOptions == null) {
            commentDayOptions = new HashMap<>();
        }
        return commentDayOptions;
    }

    public boolean isAuthor() {
        return author;
    }

    public void setAuthor(boolean author) {
        this.author = author;
    }

    public boolean isCommentingEnabled() {
        return commentingEnabled;
    }

    public void setCommentingEnabled(boolean commentingEnabled) {
        this.commentingEnabled = commentingEnabled;
    }

    public int getDefaultCommentDays() {
        return defaultCommentDays;
    }

    public void setDefaultCommentDays(int defaultCommentDays) {
        this.defaultCommentDays = defaultCommentDays;
    }

    public Weblog.EditFormat getDefaultEditFormat() {
        return defaultEditFormat;
    }

    public void setDefaultEditFormat(Weblog.EditFormat defaultEditFormat) {
        this.defaultEditFormat = defaultEditFormat;
    }

    public Map<String, String> getEditFormats() {
        if (editFormats == null) {
            editFormats = new LinkedHashMap<>();
        }
        return editFormats;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}
