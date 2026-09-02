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

import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.FrontpageSettings;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.rendering.util.cache.SiteWideCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogFeedCache;
import org.apache.roller.weblogger.ui.rendering.util.cache.WeblogPageCache;
import org.apache.struts2.ServletActionContext;

/**
 * Chooses the site frontpage weblog for the first time.
 *
 * <p>This exists separately from {@link Setup} because the bootstrap page is
 * reachable without a login while the site has no users. Here the caller must
 * hold the global administrator permission, which the first registered user
 * receives by default.
 *
 * <p>The action applies only to the initial choice. Once a frontpage weblog is
 * set, later changes go through the global configuration screen, which is
 * already administrator-only.
 */
public class FrontpageSetup extends Setup {

    private static final Log LOG = LogFactory.getLog(FrontpageSetup.class);

    private String frontpageBlog;
    private Boolean aggregated;

    public FrontpageSetup() {
        this.pageTitle = "index.heading";
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }

    @Override
    public boolean isUserRequired() {
        return true;
    }

    @Override
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }

    /**
     * Stores the initial frontpage selection.
     *
     * <p>Reached only by POST, so the CSRF salt filter covers it, and only while
     * no frontpage weblog has been chosen.
     */
    public String save() {

        if (!isPostRequest()) {
            return DENIED;
        }

        try {
            if (!FrontpageSettings.applyInitial(frontpageBlog, aggregated)) {
                addError("frontpageConfig.alreadyConfigured");
                loadSetupModel();
                setFrontpageConfigured(true);
                return INPUT;
            }

            invalidateRenderedContent();
            addMessage("frontpageConfig.values.saved");

        } catch (FrontpageSettings.InvalidFrontpageWeblogException ex) {
            addError("frontpageConfig.invalidWeblog");
            loadSetupModel();
            return INPUT;

        } catch (WebloggerException ex) {
            LOG.error("ERROR saving frontpage configuration", ex);
            addError("frontpageConfig.values.error");
            loadSetupModel();
            return INPUT;
        }

        return "home";
    }

    protected boolean isPostRequest() {
        HttpServletRequest req = ServletActionContext.getRequest();
        return req != null && "POST".equalsIgnoreCase(req.getMethod());
    }

    private void invalidateRenderedContent() {
        SiteWideCache.getInstance().clear();
        WeblogPageCache.getInstance().clear();
        WeblogFeedCache.getInstance().clear();
    }

    public String getFrontpageBlog() {
        return frontpageBlog;
    }

    public void setFrontpageBlog(String frontpageBlog) {
        this.frontpageBlog = frontpageBlog;
    }

    public Boolean getAggregated() {
        return aggregated;
    }

    public void setAggregated(Boolean aggregated) {
        this.aggregated = aggregated;
    }
}
