/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
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

import org.tightblog.util.Utilities;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

@Entity
@Table(name = "weblog_category")
public class WeblogCategory implements Comparable<WeblogCategory>, WeblogOwned {

    // unique internal ID of object
    private String id = Utilities.generateUUID();
    private int hashCode;
    // category name
    private String name;
    // left-to-right comparative ordering of category, higher numbers go to the right
    private int position;
    // parent weblog of category
    private Weblog weblog;

    // transient fields for statistics
    private int numEntries;
    private LocalDate firstEntry;
    private LocalDate lastEntry;

    public WeblogCategory() {
    }

    public WeblogCategory(
            Weblog weblog,
            String name) {
        this.name = name;
        this.weblog = weblog;
        calculatePosition();
    }

    // algorithm assumes category not yet added to the weblog's list
    private void calculatePosition() {
        int size = weblog.getWeblogCategories().size();
        if (size == 0) {
            this.position = 0;
        } else {
            this.position = weblog.getWeblogCategories().get(size - 1).getPosition() + 1;
        }
    }

    @Id
    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic(optional = false)
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic(optional = false)
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @ManyToOne
    @JoinColumn(name = "weblogid", nullable = false)
    public Weblog getWeblog() {
        return weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    private static final Comparator<WeblogCategory> COMPARATOR =
            Comparator.comparing(WeblogCategory::getWeblog, Weblog.HANDLE_COMPARATOR)
                        .thenComparingInt(WeblogCategory::getPosition);

    @Override
    public int compareTo(WeblogCategory o) {
        return COMPARATOR.compare(this, o);
    }

    public String toString() {
        return "WeblogCategory: id=" + id + ", weblog=" + weblog.getHandle() + ", name=" + name;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || (other instanceof WeblogCategory && Objects.equals(id, ((WeblogCategory) other).id));
    }

    @Override
    public int hashCode() {
        if (hashCode == 0) {
            hashCode = Objects.hashCode(id);
        }
        return hashCode;
    }

    @Transient
    public int getNumEntries() {
        return numEntries;
    }

    public void setNumEntries(int numEntries) {
        this.numEntries = numEntries;
    }

    @Transient
    public LocalDate getFirstEntry() {
        return firstEntry;
    }

    public void setFirstEntry(LocalDate firstEntry) {
        this.firstEntry = firstEntry;
    }

    @Transient
    public LocalDate getLastEntry() {
        return lastEntry;
    }

    public void setLastEntry(LocalDate lastEntry) {
        this.lastEntry = lastEntry;
    }
}
