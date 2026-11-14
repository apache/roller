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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.apache.struts2.interceptor.parameter.StrutsParameter;

/**
 * Manage weblog categories: list them with their entry counts, and change
 * their order. Adding, editing and removing are done by CategoryEdit and
 * CategoryRemove.
 */
public class Categories extends UIAction {

    private static final Log log = LogFactory.getLog(Categories.class);

    // all categories from the action weblog, in display order
    private List<WeblogCategory> allCategories = Collections.emptyList();

    // number of entries in each category, by category id
    private Map<String, Long> entryCounts = Collections.emptyMap();

    // category ids in their new order, posted by reorder
    private List<String> categoryIds = new ArrayList<>();

    public Categories() {
        this.actionName = "categories";
        this.desiredMenu = "editor";
        this.pageTitle = "categoriesForm.rootTitle";
    }

    @Override
    public List<String> requiredWeblogPermissionActions() {
        return Collections.singletonList(WeblogPermission.POST);
    }

    @Override
    public String execute() {
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            allCategories = wmgr.getWeblogCategories(getActionWeblog());
            entryCounts = wmgr.getWeblogCategoryEntryCounts(getActionWeblog());
        } catch (WebloggerException ex) {
            log.error("Error building categories list", ex);
            addError("generic.error.check.logs");
        }
        return LIST;
    }

    /**
     * Save a new display order. The posted ids must name every category of
     * the weblog exactly once.
     */
    public String reorder() {
        try {
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
            List<WeblogCategory> current = wmgr.getWeblogCategories(getActionWeblog());

            Map<String, WeblogCategory> byId = new HashMap<>();
            for (WeblogCategory category : current) {
                byId.put(category.getId(), category);
            }
            if (categoryIds.size() != current.size()
                    || !byId.keySet().equals(new HashSet<>(categoryIds))) {
                addError("categoriesForm.error.reorder");
                return execute();
            }

            // the categories are managed, so setting the positions is enough;
            // the weblog is saved once, to update its last-modified time
            for (int position = 0; position < categoryIds.size(); position++) {
                byId.get(categoryIds.get(position)).setPosition(position);
            }
            WebloggerFactory.getWeblogger().getWeblogManager().saveWeblog(getActionWeblog());
            // the weblog's own list is sorted only when it is loaded, and themes
            // and new categories read it, so put it in the new order too
            getActionWeblog().getWeblogCategories()
                    .sort(Comparator.comparingInt(WeblogCategory::getPosition));
            WebloggerFactory.getWeblogger().flush();
            CacheManager.invalidate(getActionWeblog());
            addMessage("categoriesForm.orderSaved");
        } catch (WebloggerException ex) {
            log.error("Error saving category order", ex);
            addError("generic.error.check.logs");
        }
        return execute();
    }

    public List<WeblogCategory> getAllCategories() {
        return allCategories;
    }

    /** Number of entries in the category, in any status. */
    public long getEntryCount(String categoryId) {
        Long count = entryCounts.get(categoryId);
        return count == null ? 0 : count;
    }

    /**
     * True if removing the category means moving something to another one:
     * its entries, or its role as the blogging clients' default category.
     */
    public boolean isInUse(String categoryId) {
        WeblogCategory clientDefault = getActionWeblog().getBloggerCategory();
        return getEntryCount(categoryId) > 0
                || (clientDefault != null && categoryId.equals(clientDefault.getId()));
    }

    /** The media library filter that offers only images, for the category image chooser. */
    public String getImageChooserType() {
        return MediaFileImageChooser.IMAGES_ONLY;
    }

    /** True when media uploads are on, so an image can be chosen from the media library. */
    public boolean isMediaLibraryEnabled() {
        return WebloggerRuntimeConfig.getBooleanProperty("uploads.enabled");
    }

    public List<String> getCategoryIds() {
        return categoryIds;
    }

    @StrutsParameter
    public void setCategoryIds(List<String> categoryIds) {
        this.categoryIds = categoryIds == null ? new ArrayList<>() : categoryIds;
    }
}
