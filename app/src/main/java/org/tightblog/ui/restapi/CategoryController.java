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

package org.tightblog.ui.restapi;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.tightblog.business.UserManager;
import org.tightblog.business.WeblogManager;
import org.tightblog.business.JPAPersistenceStrategy;
import org.tightblog.pojos.Weblog;
import org.tightblog.pojos.WeblogCategory;
import org.tightblog.pojos.WeblogRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

/**
 * Manage weblog categories.
 */
@RestController
public class CategoryController {

    public CategoryController() {
    }

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
    private JPAPersistenceStrategy persistenceStrategy;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    @PutMapping(value = "/tb-ui/authoring/rest/category/{id}")
    public void updateCategory(@PathVariable String id, @RequestBody WeblogCategory updatedCategory, Principal p,
                               HttpServletResponse response) throws ServletException {
        try {
            WeblogCategory c = weblogManager.getWeblogCategory(id);
            if (c != null) {
                Weblog weblog = c.getWeblog();
                if (userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                    if (!c.getName().equals(updatedCategory.getName())) {
                        c.setName(updatedCategory.getName());
                        try {
                            weblogManager.saveWeblogCategory(c);
                            persistenceStrategy.flush();
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

    @PutMapping(value = "/tb-ui/authoring/rest/categories")
    public void addCategory(@RequestParam(name = "weblogId") String weblogId, @RequestBody WeblogCategory newCategory,
                            Principal p, HttpServletResponse response) throws ServletException {
        try {
            Weblog weblog = weblogManager.getWeblog(weblogId);
            if (weblog != null && userManager.checkWeblogRole(p.getName(), weblog.getHandle(), WeblogRole.OWNER)) {
                WeblogCategory wc = new WeblogCategory(weblog, newCategory.getName());
                try {
                    weblogManager.saveWeblogCategory(wc);
                    weblog.addCategory(wc);
                    persistenceStrategy.flush();
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

    @GetMapping(value = "/tb-ui/authoring/rest/categories")
    public List<WeblogCategory> getWeblogCategories(@RequestParam(name = "weblogId") String weblogId) {
        return weblogManager.getWeblogCategories(weblogManager.getWeblog(weblogId))
                .stream()
                .peek(cat -> cat.setWeblog(null))
                .collect(Collectors.toList());
    }

    @DeleteMapping(value = "/tb-ui/authoring/rest/category/{id}")
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
                        persistenceStrategy.flush();
                    }

                    weblogManager.removeWeblogCategory(categoryToRemove);
                    persistenceStrategy.flush();
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
