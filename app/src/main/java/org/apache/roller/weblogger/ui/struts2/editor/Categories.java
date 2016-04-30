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

package org.apache.roller.weblogger.ui.struts2.editor;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogCategory;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Manage weblog categories.
 */
@RestController
public class Categories extends UIAction {

    @Autowired
    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Autowired
    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    @Autowired
    private CacheManager cacheManager;

    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    // all categories from the action weblog
	private List<WeblogCategory> allCategories;

	public Categories() {
		this.actionName = "categories";
		this.desiredMenu = "editor";
		this.pageTitle = "categoriesForm.rootTitle";
	}

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.POST;
    }

	public String execute() {
        allCategories = weblogManager.getWeblogCategories(getActionWeblog());
		return LIST;
	}

	public List<WeblogCategory> getAllCategories() {
		return allCategories;
	}

	public void setAllCategories(List<WeblogCategory> allCategories) {
		this.allCategories = allCategories;
	}

    @RequestMapping(value = "/tb-ui/authoring/rest/category/{id}", method = RequestMethod.PUT)
    public void updateCategory(@PathVariable String id, @RequestBody TextNode newName, Principal p,
                               HttpServletResponse response) throws ServletException {
        try {
            WeblogCategory c = weblogManager.getWeblogCategory(id);
            if (c != null) {
                Weblog weblog = c.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                    if (!c.getName().equals(newName.asText())) {
                        c.setName(newName.asText());
                        try {
                            weblogManager.saveWeblogCategory(c);
                            WebloggerFactory.flush();
                            cacheManager.invalidate(c);
                            response.setStatus(HttpServletResponse.SC_OK);
                        } catch (IllegalArgumentException e) {
                            response.setStatus(HttpServletResponse.SC_CONFLICT);
                        }
                    }
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/categories", method = RequestMethod.PUT)
    public void addCategory(@RequestParam(name="weblog") String weblogHandle, @RequestBody TextNode categoryName, Principal p,
                            HttpServletResponse response) throws ServletException {
        try {
            if (userManager.checkWeblogRole(p.getName(), weblogHandle, WeblogRole.OWNER)) {
                Weblog weblog = weblogManager.getWeblogByHandle(weblogHandle);
                WeblogCategory wc = new WeblogCategory(weblog, categoryName.asText());
                try {
                    weblogManager.saveWeblogCategory(wc);
                    weblog.addCategory(wc);
                    WebloggerFactory.flush();
                    cacheManager.invalidate(wc);
                    response.setStatus(HttpServletResponse.SC_OK);
                } catch (IllegalArgumentException e) {
                    response.setStatus(HttpServletResponse.SC_CONFLICT);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/categories", method = RequestMethod.GET)
    public List<WeblogCategory> getWeblogCategories(@RequestParam(name="weblog") String weblogHandle,
                                                    @RequestParam String skipCategoryId) {
        List<WeblogCategory> list = weblogManager.getWeblogCategories(weblogManager.getWeblogByHandle(weblogHandle)).stream()
                .filter(t -> !t.getId().equals(skipCategoryId))
                .collect(Collectors.toList());
        list.forEach(p -> p.setWeblog(null));
        return list;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/categories/inuse", method = RequestMethod.GET)
    public boolean isCategoryInUse(@RequestParam String categoryId) {
        WeblogCategory category = weblogManager.getWeblogCategory(categoryId);
        return category != null && weblogManager.isWeblogCategoryInUse(category);
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/categories/loggedin", method = RequestMethod.GET)
    public boolean loggedIn() {
        return true;
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/category/{id}", method = RequestMethod.DELETE)
    public void removeCategory(@PathVariable String id, @RequestParam(required = false) String targetCategoryId,
            Principal p, HttpServletResponse response)
            throws ServletException {

        try {
            WeblogCategory categoryToRemove = weblogManager.getWeblogCategory(id);
            if (categoryToRemove != null) {
                Weblog weblog = categoryToRemove.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {

                    WeblogCategory targetCategory = targetCategoryId == null ?
                            null : weblogManager.getWeblogCategory(targetCategoryId);

                    if (targetCategory != null) {
                        weblogManager.moveWeblogCategoryContents(categoryToRemove, targetCategory);
                        WebloggerFactory.flush();
                    }

                    weblogManager.removeWeblogCategory(categoryToRemove);
                    WebloggerFactory.flush();
                    cacheManager.invalidate(categoryToRemove);
                    response.setStatus(HttpServletResponse.SC_OK);
                } else {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                }
            } else {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

}
