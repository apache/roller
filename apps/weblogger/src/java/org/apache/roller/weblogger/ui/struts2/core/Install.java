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

import java.io.PrintWriter;
import java.io.StringWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.RollerFactory;
import org.apache.roller.weblogger.business.utils.DatabaseCreator;
import org.apache.roller.weblogger.business.utils.DatabaseUpgrader;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;

/**
 * Walk user through install process.
 */
public class Install extends UIAction { 
    private static Log log = LogFactory.getLog(Install.class);
    private static final String DATABASE_ERROR = "database_error";
    private static final String CREATE_DATABASE = "create_database";
    private static final String UPGRADE_DATABASE = "upgrade_database";
    private static final String UNKNOWN_ERROR = "unknown_error";
    private Throwable rootCauseException = null;
       
    public String execute() {

        try {
            DatabaseProvider dp = DatabaseProvider.getDatabaseProvider();             
        } catch (RollerException e) {
            return DATABASE_ERROR;
        }   
        
        try {               
            if (DatabaseCreator.isCreationRequired()) {  
                log.info("Forwarding to database table creation page");
                return CREATE_DATABASE;
            } 
            if (DatabaseUpgrader.isUpgradeRequired()) {
                log.info("Forwarding to database table upgrade page");
                return UPGRADE_DATABASE;
            }
        } catch (Throwable t) {
            rootCauseException = t;
            log.error("ERROR checking database status", t);
            return UNKNOWN_ERROR;
        } 
        
        try {
            log.info("Attempting to bootstrap Roller");
            RollerFactory.bootstrap();
        } catch (Throwable t) {
            rootCauseException = t;
            log.error("ERROR bootstrapping Roller", t);
            return UNKNOWN_ERROR;
        }
        
        return SUCCESS;
    }

    public boolean isUserRequired() {
        return false;
    }
    
    public boolean isWeblogRequired() {
        return false;
    }
    
    public Throwable getRootCauseException() {
        return rootCauseException;
    }
    
    public String getRootCauseStackTrace() {
        String stackTrace = "";
        if (getRootCauseException() != null) {
            StringWriter sw = new StringWriter();
            getRootCauseException().printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString().trim();
        }
        return stackTrace;
    }
}

