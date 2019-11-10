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

public class WeblogEntrySearchFields {
    private Map<String, String> categories;
    private Map<String, String> sortByOptions;
    private Map<String, String> statusOptions;

    // getters needed for JSON serialization: http://stackoverflow.com/a/35822500
    public Map<String, String> getCategories() {
        if (categories == null) {
            categories = new LinkedHashMap<>();
        }
        return categories;
    }

    public Map<String, String> getSortByOptions() {
        if (sortByOptions == null) {
            sortByOptions = new LinkedHashMap<>();
        }
        return sortByOptions;
    }

    public Map<String, String> getStatusOptions() {
        if (statusOptions == null) {
            statusOptions = new LinkedHashMap<>();
        }
        return statusOptions;
    }
}
