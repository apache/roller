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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.jpa.JPAPersistenceStrategy;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogBookmark;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * List bookmarks and allow for moving them around and deleting them.
 */
public class Bookmarks extends UIAction {

    private static Log log = LogFactory.getLog(Bookmarks.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    private JPAPersistenceStrategy persistenceStrategy = null;

    public void setPersistenceStrategy(JPAPersistenceStrategy persistenceStrategy) {
        this.persistenceStrategy = persistenceStrategy;
    }

    // the weblog being viewed
    private Weblog weblogObj = null;

    // the list of bookmarks to move or delete
    private String[] selectedBookmarks = null;

    public Bookmarks() {
        this.actionName = "bookmarks";
        this.desiredMenu = "editor";
        this.pageTitle = "bookmarksForm.rootTitle";
    }

    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.OWNER;
    }

    public void prepare() {
        setWeblogObj(getActionWeblog());
    }

    /**
     * Present the weblog's bookmarks
     */
    public String execute() {
        return LIST;
    }

    /**
     * Delete bookmarks.
     */
    public String delete() {

        try {
            WeblogBookmark bookmark;
            String bookmarks[] = getSelectedBookmarks();
            if (null != bookmarks && bookmarks.length > 0) {
                if (log.isDebugEnabled()) {
                    log.debug("Processing delete of " + bookmarks.length
                            + " bookmarks.");
                }
                for (int j = 0; j < bookmarks.length; j++) {
                    if (log.isDebugEnabled()) {
                        log.debug("Deleting bookmark - " + bookmarks[j]);
                    }
                    bookmark = weblogManager.getBookmark(bookmarks[j]);
                    if (bookmark != null) {
                        weblogManager.removeBookmark(bookmark);
                    }

                }
            }

            // flush changes
            persistenceStrategy.flushAndInvalidateWeblog(getActionWeblog());

        } catch (WebloggerException ex) {
            log.error("Error doing bookmark deletes", ex);
            addError("Error doing bookmark deletes");
        }

        return execute();
    }

    public String[] getSelectedBookmarks() {
        return selectedBookmarks;
    }

    public void setSelectedBookmarks(String[] bookmarks) {
        this.selectedBookmarks = bookmarks;
    }

    public Weblog getWeblogObj() {
        return weblogObj;
    }

    public void setWeblogObj(Weblog weblogObj) {
        this.weblogObj = weblogObj;
    }
}
