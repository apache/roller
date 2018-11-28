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

package org.apache.roller.planet.ui.core.struts2;

import com.opensymphony.xwork2.ActionSupport;
import java.util.List;


/**
 * Extends the Struts2 ActionSupport class to add in support for handling an
 * error and status success.  Other actions extending this one only need to
 * calle setError() and setSuccess() accordingly.
 * 
 * NOTE: as a small convenience, all errors and messages are assumed to be keys
 * which point to a success in a resource bundle, so we automatically call
 * getText(key) on the param passed into setError() and setSuccess().
 */
public abstract class PlanetActionSupport extends ActionSupport {
    
    // status params
    private String error = null;
    private String warning = null;
    private String success = null;
    
    
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = getText(error);
    }
    
    public void setError(String error, String param) {
        this.error = getText(error, error, param);
    }
    
    public String getWarning() {
        return warning;
    }

    public void setWarning(String warning) {
        this.warning = getText(warning);
    }
    
    public void setWarning(String warning, String param) {
        this.warning = getText(warning, warning, param);
    }
    
    public String getSuccess() {
        return success;
    }

    public void setSuccess(String message) {
        this.success = getText(message);
    }
    
    public void setSuccess(String message, String param) {
        this.success = getText(message, message, param);
    }
    
}
