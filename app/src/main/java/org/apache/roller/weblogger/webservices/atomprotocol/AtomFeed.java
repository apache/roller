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
*/
package org.apache.roller.weblogger.webservices.atomprotocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * An atom:feed used to represent an AtomPub collection.
 */
public class AtomFeed {

    private String id;
    private String title;
    private Date updated;
    private List<AtomLink> links = new ArrayList<>();
    private List<AtomEntry> entries = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public List<AtomLink> getLinks() {
        return links;
    }

    public void setLinks(List<AtomLink> links) {
        this.links = links;
    }

    public List<AtomEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<AtomEntry> entries) {
        this.entries = entries;
    }
}
