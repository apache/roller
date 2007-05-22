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

package org.apache.roller.ui.struts2.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.business.RollerFactory;
import org.apache.roller.business.UserManager;
import org.apache.roller.ui.struts2.util.UIAction;


/**
 * Page used to display Roller isntall instructions.
 */
public class Setup extends UIAction {
    
    private static final Log log = LogFactory.getLog(Setup.class);
    
    private long userCount = 0;
    private long blogCount = 0;
    
    
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
            UserManager umgr = RollerFactory.getRoller().getUserManager();
            setUserCount(umgr.getUserCount());
            setBlogCount(umgr.getWeblogCount());
        } catch (RollerException ex) {
            log.error("Error getting user/weblog counts", ex);
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
    
}
