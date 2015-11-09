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

import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.search.IndexManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.cache.CacheManager;

/**
 * Allows user to perform maintenance operations such as flushing the page cache
 * or re-indexing the search index.
 */
public class Maintenance extends UIAction {

    private static Log log = LogFactory.getLog(Maintenance.class);

    private WeblogManager weblogManager;

    public void setWeblogManager(WeblogManager weblogManager) {
        this.weblogManager = weblogManager;
    }

    public Maintenance() {
        this.actionName = "maintenance";
        this.desiredMenu = "editor";
        this.pageTitle = "maintenance.title";
    }

    public String execute() {
        return SUCCESS;
    }


    @Override
    public GlobalRole requiredGlobalRole() {
        return GlobalRole.BLOGGER;
    }

    @Override
    public WeblogRole requiredWeblogRole() {
        return WeblogRole.POST;
    }

    /**
     * Rebuild search index for weblog.
     */
    public String index() {

        try {
            IndexManager manager = WebloggerFactory.getWeblogger()
                    .getIndexManager();
            manager.rebuildWeblogIndex(getActionWeblog());

            addMessage("maintenance.message.indexed");
        } catch (Exception ex) {
            log.error("Error doing index rebuild", ex);
            addError("maintenance.message.indexed.failure");
        }

        return SUCCESS;
    }

    /**
     * Flush page cache for weblog.
     */
    public String flushCache() {

        try {
            Weblog weblog = getActionWeblog();

            // some caches are based on weblog last-modified, so update it
            weblog.setLastModified(new Date());

            weblogManager.saveWeblog(weblog);
            WebloggerFactory.flush();

            // also notify cache manager
            CacheManager.invalidate(weblog);

            addMessage("maintenance.message.flushed");

        } catch (Exception ex) {
            log.error("Error saving weblog - " + getActionWeblog().getHandle(),
                    ex);
            addError("Error flushing page cache");
        }

        return SUCCESS;
    }

    /**
     * Reset hit count for weblog.
     */
    public String reset() {

        try {
            Weblog weblog = getActionWeblog();

            WeblogManager mgr = WebloggerFactory.getWeblogger()
                    .getWeblogManager();
            mgr.resetHitCount(weblog);

            // some caches are based on weblog last-modified, so update it
            weblog.setLastModified(new Date());

            weblogManager.saveWeblog(weblog);
            WebloggerFactory.flush();

            // also notify cache manager
            CacheManager.invalidate(weblog);

            addMessage("maintenance.message.reset");

        } catch (Exception ex) {
            log.error("Error saving weblog - " + getActionWeblog().getHandle(),
                    ex);
            addError("Error flushing page cache");
        }

        return SUCCESS;
    }

}
