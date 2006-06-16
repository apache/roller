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
package org.apache.roller.ui.rendering.model;

import javax.servlet.http.HttpServletRequest;

/**
 * New Atlas config page model provides access to site URLs and runtime configs.
 */
public class ConfigPageModel implements PageModel {
    
    /** Creates a new instance of ConfigPageModel */
    public ConfigPageModel() {
    }

    /** Template context name to be used for model */
    public String getModelName() {
        return "config";
    }

    /** Init page model based on request */
    public void init(HttpServletRequest request) {
    }
    
    /** Absolute URL of Roller server, e.g. http://localhost:8080 */
    public String getAbsoluteSiteURL() {
        return null;
    }
    
    /** Absolute URL of Roller server, e.g. http://localhost:8080/roller */
    public String getAbsoluteContextURL() {
        return null;
    }
    
    /** Get Roller string runtime configuration property */
    public String getConfigProperty(String name) {
        return null;
    }
    
    /** Get Roller integer runtime configuration property */
    public int getConfigPropertyInt(String name) {
        return 0;
    }
    
    /** Get Roller version string */
    public String getRollerVersion() {
        return null;
    }
    
    /** Get timestamp of Roller build */
    public String getRollerBuildTimestamp() {
        return null;
    }
    
    /** Get username who created Roller build */
    public String getRollerBuildUser() {
        return null;
    }
}

