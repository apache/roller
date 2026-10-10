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
 */

package org.apache.roller.weblogger.webservices.atomprotocol;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * The categories of a weblog as an AtomPub collection, for users who may
 * post to the weblog. Each category is an entry: atom:title is the name,
 * atom:summary the description, roller:image the image URL; roller:position
 * and roller:inUse are read-only.
 *
 * <p>A category that entries still use is removed only when the request
 * names another category of the same weblog in the {@code moveTo} parameter;
 * the entries move there first. Otherwise DELETE answers 409.
 */
public class CategoryCollection {

    private final Weblogger roller;
    private final User user;
    private final String atomURL;

    public CategoryCollection(User user, String atomURL) {
        this.user = user;
        this.atomURL = atomURL;
        this.roller = WebloggerFactory.getWeblogger();
    }

    public AtomFeed getCollection(AtomRequest areq) throws AtomException {
        Weblog weblog = requireWeblog(CollectionSupport.path(areq));
        try {
            AtomFeed feed = new AtomFeed();
            String uri = atomURL + "/" + weblog.getHandle() + "/categories";
            feed.setId(uri);
            feed.setTitle(weblog.getName() + " categories");
            feed.setUpdated(new Date());
            feed.getLinks().add(CollectionSupport.link("self", uri));
            for (WeblogCategory category : manager().getWeblogCategories(weblog)) {
                feed.getEntries().add(createAtomEntry(weblog, category));
            }
            return feed;
        } catch (WebloggerException e) {
            throw new AtomException("Getting category collection", e);
        }
    }

    /** The weblog's categories as an APP category document (RFC 5023 section 7). */
    public AtomCategories getCategoriesDocument(AtomRequest areq) throws AtomException {
        Weblog weblog = requireWeblog(CollectionSupport.path(areq));
        try {
            return RollerAtomService.weblogCategories(weblog);
        } catch (WebloggerException e) {
            throw new AtomException("Getting category document", e);
        }
    }

    public AtomEntry getEntry(AtomRequest areq) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            return createAtomEntry(weblog, requireCategory(weblog, pathInfo));
        } catch (WebloggerException e) {
            throw new AtomException("Getting category", e);
        }
    }

    public AtomEntry postEntry(AtomRequest areq, AtomEntry entry) throws AtomException {
        Weblog weblog = requireWeblog(CollectionSupport.path(areq));
        String name = validName(entry.getTitle());
        if (weblog.hasCategory(name)) {
            throw CollectionSupport.conflict("Category name already in use: " + name);
        }
        try {
            WeblogCategory category = new WeblogCategory();
            category.setWeblog(weblog);
            category.setName(name);
            copyProperties(entry, category);
            weblog.addCategory(category);
            category.calculatePosition();
            manager().saveWeblogCategory(category);
            roller.flush();
            CacheManager.invalidate(weblog);
            return createAtomEntry(weblog, category);
        } catch (WebloggerException e) {
            throw new AtomException("Creating category", e);
        }
    }

    public void putEntry(AtomRequest areq, AtomEntry entry) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            WeblogCategory category = requireCategory(weblog, pathInfo);
            if (entry.getTitle() != null) {
                String name = validName(entry.getTitle());
                WeblogCategory existing = weblog.getWeblogCategory(name);
                if (existing != null && !existing.getId().equals(category.getId())) {
                    throw CollectionSupport.conflict("Category name already in use: " + name);
                }
                category.setName(name);
            }
            copyProperties(entry, category);
            manager().saveWeblogCategory(category);
            roller.flush();
            CacheManager.invalidate(category);
        } catch (WebloggerException e) {
            throw new AtomException("Updating category", e);
        }
    }

    public void deleteEntry(AtomRequest areq) throws AtomException {
        String[] pathInfo = CollectionSupport.path(areq);
        Weblog weblog = requireWeblog(pathInfo);
        try {
            WeblogEntryManager mgr = manager();
            WeblogCategory category = requireCategory(weblog, pathInfo);
            String moveTo = areq.getParameter("moveTo");
            if (StringUtils.isNotEmpty(moveTo)) {
                WeblogCategory target = mgr.getWeblogCategory(weblog, moveTo);
                if (target == null || target.getId().equals(category.getId())) {
                    throw CollectionSupport.badRequest("moveTo must name another category of this weblog");
                }
                mgr.moveWeblogCategoryContents(category, target);
                roller.flush();
            } else if (mgr.isWeblogCategoryInUse(category)) {
                throw CollectionSupport.conflict(
                        "Category is in use; name a category to move its entries to with moveTo");
            }
            CacheManager.invalidate(category);
            mgr.removeWeblogCategory(category);
            roller.flush();
        } catch (WebloggerException e) {
            throw new AtomException("Removing category", e);
        }
    }

    private AtomEntry createAtomEntry(Weblog weblog, WeblogCategory category)
            throws WebloggerException {
        String editURI = atomURL + "/" + weblog.getHandle() + "/category/" + category.getId();
        AtomEntry entry = new AtomEntry();
        entry.setId(editURI);
        entry.setTitle(category.getName());
        entry.setUpdated(new Date());
        if (category.getDescription() != null) {
            entry.setSummary(CollectionSupport.text("text", category.getDescription()));
        }
        entry.setExtension("image", category.getImage());
        entry.setExtension("position", Integer.toString(category.getPosition()));
        entry.setExtension("inUse", Boolean.toString(manager().isWeblogCategoryInUse(category)));
        List<AtomLink> links = new ArrayList<>();
        links.add(CollectionSupport.link("edit", editURI));
        entry.setLinks(links);
        return entry;
    }

    private static void copyProperties(AtomEntry entry, WeblogCategory category) {
        if (entry.getSummary() != null) {
            category.setDescription(entry.getSummary().getValue());
        }
        if (entry.getExtensions().containsKey("image")) {
            category.setImage(StringUtils.trimToNull(entry.getExtension("image")));
        }
    }

    /** Category names must be present and free of HTML markup, as in the category editor. */
    private static String validName(String name) throws AtomException {
        String trimmed = StringUtils.trimToNull(name);
        if (trimmed == null || !trimmed.equals(StringEscapeUtils.escapeHtml4(trimmed))) {
            throw CollectionSupport.badRequest("Invalid category name");
        }
        return trimmed;
    }

    private Weblog requireWeblog(String[] pathInfo) throws AtomException {
        return CollectionSupport.requireWeblog(roller, user, pathInfo, WeblogPermission.POST);
    }

    private WeblogCategory requireCategory(Weblog weblog, String[] pathInfo)
            throws AtomException, WebloggerException {
        WeblogCategory category = pathInfo.length > 2
                ? manager().getWeblogCategory(weblog, pathInfo[2]) : null;
        if (category == null) {
            throw new AtomNotFoundException("Cannot find category");
        }
        return category;
    }

    private WeblogEntryManager manager() {
        return roller.getWeblogEntryManager();
    }
}
