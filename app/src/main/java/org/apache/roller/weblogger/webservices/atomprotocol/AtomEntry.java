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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * An atom:entry. The {@code draft} and {@code edited} fields carry the APP
 * control extension (app:control/app:draft and app:edited). Roller's own
 * extension elements ({@link AtomConstants#ROLLER_NS}) are kept by local name
 * in {@link #getExtensions()}; the mobile template rendition and the
 * thr:in-reply-to link (RFC 4685) have their own fields.
 */
public class AtomEntry {

    private String id;
    private String title;
    private AtomContent content;
    private AtomContent summary;
    private Date published;
    private Date updated;
    private Date edited;
    private boolean draft;
    private List<AtomPerson> authors = new ArrayList<>();
    private List<AtomCategory> categories = new ArrayList<>();
    private List<AtomLink> links = new ArrayList<>();
    private Map<String, String> extensions = new LinkedHashMap<>();
    private String mobileRendition;
    private String inReplyToRef;
    private String inReplyToHref;

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

    public AtomContent getContent() {
        return content;
    }

    public void setContent(AtomContent content) {
        this.content = content;
    }

    public AtomContent getSummary() {
        return summary;
    }

    public void setSummary(AtomContent summary) {
        this.summary = summary;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public Date getUpdated() {
        return updated;
    }

    public void setUpdated(Date updated) {
        this.updated = updated;
    }

    public Date getEdited() {
        return edited;
    }

    public void setEdited(Date edited) {
        this.edited = edited;
    }

    public boolean isDraft() {
        return draft;
    }

    public void setDraft(boolean draft) {
        this.draft = draft;
    }

    public List<AtomPerson> getAuthors() {
        return authors;
    }

    public void setAuthors(List<AtomPerson> authors) {
        this.authors = authors;
    }

    public List<AtomCategory> getCategories() {
        return categories;
    }

    public void setCategories(List<AtomCategory> categories) {
        this.categories = categories;
    }

    public List<AtomLink> getLinks() {
        return links;
    }

    public void setLinks(List<AtomLink> links) {
        this.links = links;
    }

    /** Simple roller:* extension values, by local name, in document order. */
    public Map<String, String> getExtensions() {
        return extensions;
    }

    public String getExtension(String name) {
        return extensions.get(name);
    }

    public void setExtension(String name, String value) {
        if (value == null) {
            extensions.remove(name);
        } else {
            extensions.put(name, value);
        }
    }

    /** Source of the mobile template rendition, or null when there is none. */
    public String getMobileRendition() {
        return mobileRendition;
    }

    public void setMobileRendition(String mobileRendition) {
        this.mobileRendition = mobileRendition;
    }

    /** The thr:in-reply-to ref (the parent's atom:id), or null. */
    public String getInReplyToRef() {
        return inReplyToRef;
    }

    public void setInReplyToRef(String inReplyToRef) {
        this.inReplyToRef = inReplyToRef;
    }

    /** The thr:in-reply-to href, or null. */
    public String getInReplyToHref() {
        return inReplyToHref;
    }

    public void setInReplyToHref(String inReplyToHref) {
        this.inReplyToHref = inReplyToHref;
    }

    /** Return the href of the first link with the given rel, or null. */
    public String getLinkHref(String rel) {
        if (links != null) {
            for (AtomLink link : links) {
                if (rel.equals(link.getRel())) {
                    return link.getHref();
                }
            }
        }
        return null;
    }
}
