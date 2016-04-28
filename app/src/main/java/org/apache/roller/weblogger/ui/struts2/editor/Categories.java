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

import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.User;
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

	private static Log log = LogFactory.getLog(Categories.class);

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
		try {
			allCategories = weblogManager.getWeblogCategories(getActionWeblog());
		} catch (WebloggerException ex) {
			log.error("Error building categories list", ex);
			addError("Error building categories list");
		}

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
                               HttpServletResponse response)
            throws ServletException {
        try {
            WeblogCategory c = weblogManager.getWeblogCategory(id);
            if (c == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else {
                // need owner permission to edit categories
                User authenticatedUser = userManager.getUserByUserName(p.getName());
                Weblog weblog = c.getWeblog();
                if (userManager.checkWeblogRole(authenticatedUser, weblog, WeblogRole.OWNER)) {
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
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

    @RequestMapping(value = "/tb-ui/authoring/rest/categories", method = RequestMethod.PUT)
    public void addCategory(@RequestParam(name="weblog") String weblogHandle, @RequestBody TextNode categoryName, Principal p,
                            HttpServletResponse response)
            throws ServletException {
        try {
            Weblog weblog = weblogManager.getWeblogByHandle(weblogHandle);
            if (weblog == null) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            } else {
                // need owner permission to edit categories
                User authenticatedUser = userManager.getUserByUserName(p.getName());
                if (userManager.checkWeblogRole(authenticatedUser, weblog, WeblogRole.OWNER)) {
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
            }
        } catch (Exception e) {
            throw new ServletException(e.getMessage());
        }
    }

}
