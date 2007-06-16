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

import com.opensymphony.xwork2.ActionSupport;
import java.sql.Connection;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.utils.DatabaseScriptProvider;
import org.apache.roller.weblogger.business.utils.DatabaseUpgrader;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.struts2.interceptor.ApplicationAware;

/**
 * Walk user through database auto-upgrade process.
 */
public class UpgradeDatabase extends ActionSupport implements ApplicationAware { 
    private static Log log = LogFactory.getLog(UpgradeDatabase.class);
    
    private DatabaseScriptProvider scripts = null;
    private boolean error = false;
    private List<String> messages = null;
    
    public String execute() {
        return SUCCESS;
    }
    
    /**
     * Looks for DatabaseScriptProvider via key 'DatabaseScriptProvider'
     */
    public void setApplication(Map map) {
        if (map.get("DatabaseScriptProvider") != null) {
            scripts = (DatabaseScriptProvider)map.get("DatabaseScriptProvider");
        }
    }
    
    public String upgrade() {
        DatabaseUpgrader upgrader = new DatabaseUpgrader(scripts);
        try {
            upgrader.upgradeDatabase(true);
        } catch (Exception ex) {
            log.error("ERROR running database upgrade scripts", ex);
            error = true;
        }
        messages = upgrader.getMessages(); 
        return SUCCESS;
    }

    public boolean isUpgradeRequired() {
        try {
            return DatabaseUpgrader.isUpgradeRequired();
        } catch (WebloggerException ex) {
            log.error("ERROR determining if database upgrade required", ex);
        }
        return false;
    }
    
    public List<String> getMessages() {
        return messages;
    }
    
    public boolean getError() {
        return error;
    }
    
    public String getProp(String key) {
        // Static config only, we don't have database yet
        String value = RollerConfig.getProperty(key);
        return (value == null) ? key : value;
    }
    
    public String getDatabaseProductName() {
        String name = "error";
        try {
            Connection con = DatabaseProvider.getDatabaseProvider().getConnection();
            name = con.getMetaData().getDatabaseProductName();
        } catch (Exception intentionallyIgnored) {}
        return name;
    }
}

