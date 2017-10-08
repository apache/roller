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

package org.apache.roller.weblogger.ui.struts2.admin;

import java.util.Collections;
import java.util.List;

import org.apache.roller.weblogger.config.AuthMethod;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.pojos.GlobalPermission;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.convention.annotation.AllowedMethods;


/**
 * Action which displays user admin search page.
 */
@AllowedMethods({"execute"})
public class UserAdmin extends UIAction {
    
    public UserAdmin() {
        this.actionName = "userAdmin";
        this.desiredMenu = "admin";
        this.pageTitle = "userAdmin.title.searchUser";
    }

    private AuthMethod authMethod = WebloggerConfig.getAuthMethod();

    // admin role required    
    public List<String> requiredGlobalPermissionActions() {
        return Collections.singletonList(GlobalPermission.ADMIN);
    }
    
    // no weblog required
    public boolean isWeblogRequired() {
        return false;
    }

    /**
     * Show user admin search page.
     */
    public String execute() {
        return SUCCESS;
    }

    public String getAuthMethod() {
        return authMethod.name();
    }

}
