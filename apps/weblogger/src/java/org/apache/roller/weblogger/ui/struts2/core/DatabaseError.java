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

import java.io.StringWriter;
import java.util.List;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Display error message about database.
 */
public class DatabaseError extends UIAction { 
    
    public String execute() {
        return SUCCESS;
    }
    
    public boolean isUserRequired() {
        return false;
    }
    
    public boolean isWeblogRequired() {
        return false;
    }
    
    public String getProp(String key) {
        // Static config only, we don't have database yet
        String value = RollerConfig.getProperty(key);
        return (value == null) ? key : value;
    }    
    
    public List<String> getStartupLog() {
        return DatabaseProvider.getStartupLog();
    }
    
    public Throwable getRootCauseException() {
        RollerException re = DatabaseProvider.getStartupException();
        if (re.getRootCause() != null) {
            return re.getRootCause();
        } else {
            return re;
        }
    }
    
    public String getRootCauseStackTrace() {
        StringWriter sw = new java.io.StringWriter();
        Throwable e = getRootCauseException();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString().trim();
    }
}
