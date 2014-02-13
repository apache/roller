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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.WeblogEntryManager;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogCategoryPathComparator;
import org.apache.roller.weblogger.pojos.WeblogPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Manage weblog categories.
 */
public class Categories extends UIAction {

	private static Log log = LogFactory.getLog(Categories.class);

	// list of category ids to move
	private String[] selectedCategories = null;

	// category id of the category to move to
	private String targetCategoryId = null;

	// all categories from the action weblog
	private Set<WeblogCategory> allCategories = Collections.EMPTY_SET;

	public Categories() {
		this.actionName = "categories";
		this.desiredMenu = "editor";
		this.pageTitle = "categoriesForm.rootTitle";
	}

	// author perms required
	public List<String> requiredWeblogPermissionActions() {
		return Collections.singletonList(WeblogPermission.POST);
	}

	public String execute() {

		// build list of categories for display
		TreeSet<WeblogCategory> allCategories = new TreeSet<WeblogCategory>(new WeblogCategoryPathComparator());

		try {
			// Build list of all categories, except for current one, sorted by
			// path.
            WeblogEntryManager wmgr = WebloggerFactory.getWeblogger().getWeblogEntryManager();
			List<WeblogCategory> cats = wmgr.getWeblogCategories(getActionWeblog());
			for (WeblogCategory cat : cats) {
			    allCategories.add(cat);
			}
		} catch (WebloggerException ex) {
			log.error("Error building categories list", ex);
			// TODO: i18n
			addError("Error building categories list");
		}

		if (allCategories.size() > 0) {
			setAllCategories(allCategories);
		}

		return LIST;
	}

	public String move() {
        // no-op today as subcategories no longer supported
		return execute();
	}

	public String[] getSelectedCategories() {
		return selectedCategories;
	}

	public void setSelectedCategories(String[] selectedCategories) {
		this.selectedCategories = selectedCategories;
	}

	public String getTargetCategoryId() {
		return targetCategoryId;
	}

	public void setTargetCategoryId(String targetCategoryId) {
		this.targetCategoryId = targetCategoryId;
	}

	public Set getAllCategories() {
		return allCategories;
	}

	public void setAllCategories(Set allCategories) {
		this.allCategories = allCategories;
	}
}
