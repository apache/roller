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
import org.apache.roller.weblogger.business.PropertiesManager;
import org.apache.roller.weblogger.business.WeblogManager;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.pojos.RuntimeConfigProperty;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Page used to display Roller isntall instructions.
 */
public class Setup extends UIAction {
    
    private static final Log log = LogFactory.getLog(Setup.class);
    
    private long userCount = 0;
    private long blogCount = 0;

    private String frontpageBlog;
    private Boolean aggregated;

    // weblogs for frontpage blog chooser
    private Collection<Weblog> weblogs;

    
    
    public Setup() {
        // TODO: i18n
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
    
    
    public String execute() {
        
        try {
            WeblogManager mgr =  WebloggerFactory.getWeblogger().getWeblogManager();
            setWeblogs(mgr.getWeblogs(true, null, null, null, 0, -1));
        } catch (WebloggerException ex) {
            log.error("Error getting weblogs", ex);
            addError("frontpageConfig.weblogs.error");
        }

        try {
            setUserCount(WebloggerFactory.getWeblogger().getUserManager().getUserCount());
            setBlogCount(WebloggerFactory.getWeblogger().getWeblogManager().getWeblogCount());
        } catch (WebloggerException ex) {
            log.error("Error getting user/weblog counts", ex);
        }
        
        return SUCCESS;
    }

    public String save() {
        PropertiesManager mgr = WebloggerFactory.getWeblogger().getPropertiesManager();
        try {
            RuntimeConfigProperty frontpageBlogProp = mgr.getProperty("site.frontpage.weblog.handle");
            frontpageBlogProp.setValue(frontpageBlog);
            mgr.saveProperty(frontpageBlogProp);

            RuntimeConfigProperty aggregatedProp = mgr.getProperty("site.frontpage.weblog.aggregated");
            aggregatedProp.setValue(aggregated.toString());
            mgr.saveProperty(aggregatedProp);

            WebloggerFactory.getWeblogger().flush();

            addMessage("frontpageConfig.values.saved");

        } catch (WebloggerException ex) {
            log.error("ERROR saving frontpage configuration", ex);
            addError("frontpageConfig.values.error");
        }
        return "home";
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

    public Collection<Weblog> getWeblogs() {
        return weblogs;
    }

    public void setWeblogs(Collection<Weblog> weblogs) {
        this.weblogs = weblogs;
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
