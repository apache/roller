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
package org.tightblog.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.tightblog.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Comparator;
import java.util.Objects;

@Entity
@Table(name = "weblog_entry_tag")
public class WeblogEntryTag implements Comparable<WeblogEntryTag> {

    private String id = Utilities.generateUUID();
    private int hashCode;
    private Weblog weblog;
    private WeblogEntry weblogEntry;
    private String name;

    public WeblogEntryTag() {
    }

    public WeblogEntryTag(Weblog weblog, WeblogEntry weblogEntry, String name) {
        this.weblog = weblog;
        this.weblogEntry = weblogEntry;
        this.name = name;
    }

    /**
     * Unique ID and primary key.
     */
    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * ID of weblog that this tag refers to.
     */
    @ManyToOne
    @JoinColumn(name = "weblogid", nullable = false)
    @JsonIgnore
    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog website) {
        this.weblog = website;
    }

    @ManyToOne
    @JoinColumn(name = "entryid", nullable = false)
    @JsonIgnore
    public WeblogEntry getWeblogEntry() {
        return weblogEntry;
    }

    public void setWeblogEntry(WeblogEntry data) {
        weblogEntry = data;
    }

    /**
     * Tag value.
     */
    @Basic(optional = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return "WeblogEntryTag: id=" + id + ", name=" + name + ", weblog=" + weblog.getHandle()
                + ", entry=" + weblogEntry.getAnchor();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof WeblogEntryTag && Objects.equals(id, ((WeblogEntryTag) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }

    private static final Comparator<WeblogEntryTag> COMPARATOR =
            Comparator.comparing(WeblogEntryTag::getWeblog, Weblog.HANDLE_COMPARATOR)
                    .thenComparing(WeblogEntryTag::getName);

    @Override
    public int compareTo(WeblogEntryTag o) {
        return COMPARATOR.compare(this, o);
    }
}
