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

package org.tightblog.pojos;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Comparator;

/**
 * Tag aggregate data, "stored" as a view.
 */
@Entity
@Table(name = "weblog_entry_tag_agg")
public class WeblogEntryTagAggregate {

    private String name = null;
    private Weblog weblog = null;
    private int total = 0;
    private int intensity = 0;

    // temporary non-persisted fields used for forms
    private String viewUrl;

    public WeblogEntryTagAggregate() {
    }

    //------------------------------------------------------- Simple properties

    @ManyToOne
    @JoinColumn(name = "weblogid", nullable = true)
    @Id
    public Weblog getWeblog() {
        return this.weblog;
    }

    public void setWeblog(Weblog weblog) {
        this.weblog = weblog;
    }

    @Basic(optional = false)
    @Id
    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic(optional = false)
    public int getTotal() {
        return this.total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Transient
    @JsonIgnore
    public int getIntensity() {
        return intensity;
    }

    public void setIntensity(int intensity) {
        this.intensity = intensity;
    }

    @Transient
    public String getViewUrl() {
        return viewUrl;
    }

    public void setViewUrl(String viewUrl) {
        this.viewUrl = viewUrl;
    }

    public String toString() {
        return "{" + (getWeblog() != null ? getWeblog().getHandle() + ", " : "") + getName() + ", " + getTotal() + "}";
    }

    public static java.util.Comparator<WeblogEntryTagAggregate> nameComparator = (weta1, weta2) ->
            weta1.getName().compareToIgnoreCase(weta2.getName());

    public static Comparator<WeblogEntryTagAggregate> countComparator = (weta1, weta2) -> {
        // higher numbers first for counts
        int compVal = Integer.valueOf(weta2.getTotal()).compareTo(weta1.getTotal());

        // still alpha order if tied
        if (compVal == 0) {
            compVal = weta1.getName().compareTo(weta2.getName());
        }
        return compVal;
    };

}
