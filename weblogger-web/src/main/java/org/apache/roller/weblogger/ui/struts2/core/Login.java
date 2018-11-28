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

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;


/**
 * Handle user logins.
 */
public class Login extends UIAction {
    
    private String error = null;
    private boolean openidEnabled = WebloggerConfig.getBooleanProperty("authentication.openid.enabled");
    
    public Login() {
        this.pageTitle = "loginPage.title";
    }
    
    
    // override default security, we do not require an authenticated user
    public boolean isUserRequired() {
        return false;
    }
    
    // override default security, we do not require an action weblog
    public boolean isWeblogRequired() {
        return false;
    }

    public String getOpenIdConfiguration() {
        return WebloggerConfig.getProperty("authentication.openid");
    }
    
    public String execute() {
        
        // set action error message if there was login error
        if(getError() != null) {
            addError("error.password.mismatch");
        }
        
        return SUCCESS;
    }

    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
    
}
