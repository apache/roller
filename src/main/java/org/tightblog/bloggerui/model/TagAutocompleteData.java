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

import org.tightblog.domain.WeblogEntryTagAggregate;

import java.util.ArrayList;
import java.util.List;

public class TagAutocompleteData {
    private String prefix;
    private List<WeblogEntryTagAggregate> tagcounts;

    public TagAutocompleteData() {
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<WeblogEntryTagAggregate> getTagcounts() {
        if (tagcounts == null) {
            tagcounts = new ArrayList<>();
        }
        return tagcounts;
    }

}
