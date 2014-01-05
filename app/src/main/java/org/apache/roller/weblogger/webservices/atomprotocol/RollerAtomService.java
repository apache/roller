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

import org.apache.commons.lang.StringUtils;
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

import com.sun.syndication.feed.atom.Category;
import com.sun.syndication.propono.atom.common.AtomService;
import com.sun.syndication.propono.atom.common.Categories;
import com.sun.syndication.propono.atom.common.Collection;
import com.sun.syndication.propono.atom.common.Workspace;
import com.sun.syndication.propono.atom.server.AtomException;


/**
 * Roller's Atom service.
 */
public class RollerAtomService extends AtomService {

    /**
     * Creates a new instance of FileBasedAtomService.
     */
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
        List uploadAccepts = new ArrayList();
        try {
            uploadAccepts = getAcceptedContentTypeRange();
        } catch (WebloggerException re) {
            throw new AtomException("Getting site's accept range", re);
        }
        if (perms != null) {
            for (WeblogPermission perm : perms) {
                Weblog weblog = perm.getWeblog();
                Workspace workspace;
                try {

                    // Create workspace to represent weblog
                    workspace = new Workspace(Utilities.removeHTML(perm.getWeblog().getName()), "text");
                    addWorkspace(workspace);

                    // Create collection for entries within that workspace
                    Collection entryCol = new Collection("Weblog Entries", "text", atomURL + "/" + weblog.getHandle() + "/entries");
                    entryCol.addAccept("application/atom+xml;type=entry");

                    // Add fixed categories using scheme that points to
                    // weblog because categories are weblog specific
                    weblog = perm.getWeblog();
                    Categories cats = new Categories();
                    cats.setFixed(true);
                    cats.setScheme(getWeblogCategoryScheme(weblog));
                    List<WeblogCategory> rollerCats = roller.getWeblogEntryManager().getWeblogCategories(weblog, false);
                    for (WeblogCategory rollerCat : rollerCats) {
                        Category cat = new Category();
                        cat.setTerm(rollerCat.getPath().substring(1));
                        cat.setLabel(rollerCat.getName());
                        cats.addCategory(cat);
                    }
                    entryCol.addCategories(cats);

                    // Add tags as free-form categories using scheme that points
                    // to site because tags can be considered site-wide
                    Categories tags = new Categories();
                    tags.setFixed(false);
                    entryCol.addCategories(tags);

                    workspace.addCollection(entryCol);
                } catch (Exception e) {
                    throw new AtomException("Creating weblog entry collection for service doc", e);
                }

                // And add one media collection for each of weblog's upload directories
                try {
                    MediaFileManager mgr = roller.getMediaFileManager();
                    List<MediaFileDirectory> dirs = mgr.getMediaFileDirectories(weblog);
                    for (MediaFileDirectory dir : dirs) {
                        Collection uploadSubCol = new Collection(
                            "Media Files: " + dir.getPath(), "text",
                            atomURL + "/" + weblog.getHandle() + "/resources/" + dir.getPath());
                        uploadSubCol.setAccepts(uploadAccepts);
                        workspace.addCollection(uploadSubCol);
                    }

                } catch (Exception e) {
                    throw new AtomException("Creating weblog entry collection for service doc", e);
                }
            }
        }
    }
    
    /**
     * Build accept range by taking things that appear to be content-type rules 
     * from site's file-upload allowed extensions.
     */
    private List getAcceptedContentTypeRange() throws WebloggerException {
        List accepts = new ArrayList();
        Weblogger roller = WebloggerFactory.getWeblogger();
        Map config = roller.getPropertiesManager().getProperties();        
        String allows = ((RuntimeConfigProperty)config.get("uploads.types.allowed")).getValue();
        String[] rules = StringUtils.split(StringUtils.deleteWhitespace(allows), ",");
        for (int i = 0; i < rules.length; i++) {
            if (rules[i].indexOf('/') == -1) {
                continue;
            }
            accepts.add(rules[i]);
        }
        return accepts;             
    }      
            
    public static String getWeblogCategoryScheme(Weblog website) {
        return WebloggerFactory.getWeblogger().getUrlStrategy().getWeblogURL(website, null, true);
    }
}

