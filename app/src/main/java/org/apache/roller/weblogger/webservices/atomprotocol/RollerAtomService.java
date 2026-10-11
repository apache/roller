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
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.MediaFileManager;
import org.apache.roller.weblogger.business.Weblogger;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.MediaFileDirectory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.User;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.util.Utilities;


/**
 * Builds Roller's APP service document. The document is assembled into an
 * {@link AtomServiceDoc} wire-model object during construction and is available
 * via {@link #getServiceDoc()}.
 */
public class RollerAtomService {

    private final AtomServiceDoc serviceDoc = new AtomServiceDoc();

    public RollerAtomService(User user, String atomURL) throws WebloggerException, AtomException {
        Weblogger roller = WebloggerFactory.getWeblogger();
        List<WeblogPermission> perms;

        if (!WebloggerRuntimeConfig.getBooleanProperty("webservices.enableAtomPub")) {
        	throw new AtomException("AtomPub not enabled for this Roller installation");
        }

        try {
            perms = roller.getUserManager().getWeblogPermissions(user);
        } catch (WebloggerException re) {
            throw new AtomException("Getting user's weblogs", re);
        }
        List<String> uploadAccepts;
        try {
            uploadAccepts = getAcceptedContentTypeRange();
        } catch (WebloggerException re) {
            throw new AtomException("Getting site's accept range", re);
        }
        if (perms != null) {
            for (WeblogPermission perm : perms) {
                Weblog weblog = perm.getWeblog();
                AtomWorkspace workspace;
                try {

                    // Create workspace to represent weblog
                    workspace = new AtomWorkspace();
                    workspace.setTitle(Utilities.removeHTML(perm.getWeblog().getName()));
                    serviceDoc.getWorkspaces().add(workspace);

                    // Create collection for entries within that workspace
                    AtomCollection entryCol = new AtomCollection();
                    entryCol.setTitle("Weblog Entries");
                    entryCol.setHref(atomURL + "/" + weblog.getHandle() + "/entries");
                    entryCol.getAccepts().add("application/atom+xml;type=entry");

                    // The weblog's fixed categories, both inline (for older
                    // clients) and as an out-of-line category document
                    entryCol.getCategories().add(weblogCategories(weblog));
                    AtomCategories catsDoc = new AtomCategories();
                    catsDoc.setHref(atomURL + "/" + weblog.getHandle() + "/categories.atomcat");
                    entryCol.getCategories().add(catsDoc);

                    // Indicte that free form categories are allowed
                    AtomCategories tags = new AtomCategories();
                    tags.setFixed(false);
                    entryCol.getCategories().add(tags);

                    workspace.getCollections().add(entryCol);
                } catch (Exception e) {
                    throw new AtomException("Creating weblog entry collection for service doc", e);
                }

                // And add one media collection for each of weblog's upload directories
                try {
                    MediaFileManager mgr = roller.getMediaFileManager();
                    List<MediaFileDirectory> dirs = mgr.getMediaFileDirectories(weblog);
                    for (MediaFileDirectory dir : dirs) {
                        AtomCollection uploadSubCol = new AtomCollection();
                        uploadSubCol.setTitle("Media Files: " + dir.getName());
                        uploadSubCol.setHref(
                            atomURL + "/" + weblog.getHandle() + "/resources/" + dir.getName());
                        uploadSubCol.setAccepts(uploadAccepts);
                        workspace.getCollections().add(uploadSubCol);
                    }

                } catch (Exception e) {
                    throw new AtomException("Creating weblog entry collection for service doc", e);
                }

                addManagementCollections(workspace, user, weblog, atomURL);
            }
        }
    }

    /**
     * Adds the template, category and comment collections that the user may
     * use. Templates need weblog ADMIN permission and a custom theme; the
     * others need POST permission.
     */
    private static void addManagementCollections(AtomWorkspace workspace, User user,
            Weblog weblog, String atomURL) {
        String base = atomURL + "/" + weblog.getHandle();
        if (TemplateCollection.isAvailable(user, weblog)) {
            workspace.getCollections().add(collection("Templates", base + "/templates",
                    "application/atom+xml;type=entry"));
        }
        if (CollectionSupport.hasPermission(user, weblog, WeblogPermission.POST)) {
            workspace.getCollections().add(collection("Categories", base + "/categories",
                    "application/atom+xml;type=entry"));
            // An empty accept list means the collection does not accept POST
            workspace.getCollections().add(collection("Comments", base + "/comments", ""));
        }
    }

    private static AtomCollection collection(String title, String href, String accept) {
        AtomCollection collection = new AtomCollection();
        collection.setTitle(title);
        collection.setHref(href);
        collection.getAccepts().add(accept);
        return collection;
    }

    /**
     * The weblog's categories as a fixed category set. The scheme points to
     * the weblog because categories are weblog specific.
     */
    static AtomCategories weblogCategories(Weblog weblog) throws WebloggerException {
        AtomCategories cats = new AtomCategories();
        cats.setFixed(true);
        cats.setScheme(getWeblogCategoryScheme(weblog));
        List<WeblogCategory> rollerCats = WebloggerFactory.getWeblogger()
                .getWeblogEntryManager().getWeblogCategories(weblog);
        for (WeblogCategory rollerCat : rollerCats) {
            AtomCategory cat = new AtomCategory();
            cat.setTerm(rollerCat.getName());
            cat.setLabel(rollerCat.getName());
            cats.getCategories().add(cat);
        }
        return cats;
    }

    /**
     * The assembled service document.
     */
    public AtomServiceDoc getServiceDoc() {
        return serviceDoc;
    }

    /**
     * Build accept range by taking things that appear to be content-type rules
     * from site's file-upload allowed extensions.
     */
    private List<String> getAcceptedContentTypeRange() throws WebloggerException {
        List<String> accepts = new ArrayList<>();
        Weblogger roller = WebloggerFactory.getWeblogger();
        Map<String, RuntimeConfigProperty> config = roller.getPropertiesManager().getProperties();
        String allows = config.get("uploads.types.allowed").getValue();
        String[] rules = StringUtils.split(StringUtils.deleteWhitespace(allows), ",");
        if (rules != null) {
            for (String rule : rules) {
                if (rule.indexOf('/') == -1) {
                    continue;
                }
                accepts.add(rule);
            }
        }
        return accepts;
    }

    public static String getWeblogCategoryScheme(Weblog website) {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(website, null, true);
    }
}
