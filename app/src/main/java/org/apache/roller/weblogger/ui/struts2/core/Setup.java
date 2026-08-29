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

package org.apache.roller.weblogger.ui.struts2.core;

import java.util.Collection;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FrontpageSettings;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Page used to display Roller install instructions.
 *
 * <p>This page is reachable without a login because a brand new site has no
 * users yet. While the site is empty it shows bootstrap guidance; once users
 * exist it requires a global administrator, and once a frontpage weblog has
 * been chosen it redirects home.
 *
 * <p>Choosing the initial frontpage weblog is {@link FrontpageSetup}, a
 * separate global-administrator action; later changes go through the global
 * configuration screen.
 */
public class Setup extends UIAction {
    
    private static final Log LOG = LogFactory.getLog(Setup.class);
    
    private long userCount = 0;
    private long blogCount = 0;

    // weblogs for frontpage blog chooser
    private Collection<Weblog> weblogs;

    // true while the site has no users and only bootstrap guidance is shown
    private boolean bootstrap = false;

    public Setup() {
        this.pageTitle = "index.heading";
    }

    @Override
    public boolean isUserRequired() {
        return false;
    }
    
    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public String execute() {

        try {
            setUserCount(WebloggerFactory.getWeblogger().getUserManager().getUserCount());
            setBlogCount(WebloggerFactory.getWeblogger().getWeblogManager().getWeblogCount());
        } catch (WebloggerException ex) {
            LOG.error("Error getting user/weblog counts", ex);
        }

        // A site with no users cannot have an administrator yet, so the
        // bootstrap instructions are shown to anyone. Nothing about the site's
        // contents is exposed here: registering the first user is the only
        // thing that can usefully be done.
        if (getUserCount() == 0) {
            setBootstrap(true);
            return SUCCESS;
        }

        // Beyond that point this is a site configuration screen.
        if (!isUserIsAdmin()) {
            return DENIED;
        }

        try {
            if (FrontpageSettings.isConfigured()) {
                // Already chosen; later changes belong in global configuration.
                return "home";
            }
        } catch (WebloggerException ex) {
            LOG.error("Error reading frontpage configuration", ex);
        }

        try {
            WeblogManager mgr = WebloggerFactory.getWeblogger().getWeblogManager();
            setWeblogs(mgr.getWeblogs(true, null, null, null, 0, -1));
        } catch (WebloggerException ex) {
            LOG.error("Error getting weblogs", ex);
            addError("frontpageConfig.weblogs.error");
        }

        return SUCCESS;
    }


    
    public long getUserCount() {
        return userCount;
    }

    public void setUserCount(long userCount) {
        this.userCount = userCount;
    }

    public long getBlogCount() {
        return blogCount;
    }

    public void setBlogCount(long blogCount) {
        this.blogCount = blogCount;
    }

    public boolean isBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(boolean bootstrap) {
        this.bootstrap = bootstrap;
    }

    public Collection<Weblog> getWeblogs() {
        return weblogs;
    }

    public void setWeblogs(Collection<Weblog> weblogs) {
        this.weblogs = weblogs;
    }
    
}
