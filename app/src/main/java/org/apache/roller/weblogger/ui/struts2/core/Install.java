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
import java.sql.Connection;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.WebloggerFactory;
import org.apache.roller.weblogger.business.startup.StartupException;
import org.apache.roller.weblogger.business.startup.WebloggerStartup;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.struts2.convention.annotation.AllowedMethods;
import org.springframework.beans.factory.access.BootstrapException;


/**
 * Walk user through install process.
 */
// TODO: make this work @AllowedMethods({"execute","create","upgrade","bootstrap"})
public class Install extends UIAction {

    private static Log log = LogFactory.getLog(Install.class);

    private static final String DATABASE_ERROR = "database_error";
    private static final String CREATE_DATABASE = "create_database";
    private static final String UPGRADE_DATABASE = "upgrade_database";
    private static final String BOOTSTRAP = "bootstrap";

    private Throwable rootCauseException = null;
    private boolean error = false;
    private boolean success = false;
    private List<String> messages = null;
    private String databaseName = "Unknown";


    @Override
    public boolean isUserRequired() {
        return false;
    }

    @Override
    public boolean isWeblogRequired() {
        return false;
    }


    @Override
    public String execute() {

        if (WebloggerFactory.isBootstrapped()) {
            return SUCCESS;
        }

        if (WebloggerStartup.getDatabaseProviderException() != null) {
            StartupException se = WebloggerStartup.getDatabaseProviderException();
            if (se.getRootCause() != null) {
                rootCauseException = se.getRootCause();
            } else {
                rootCauseException = se;
            }
            messages = se.getStartupLog();

            log.debug("Forwarding to database error page");
            setPageTitle("installer.error.connection.pageTitle");
            return DATABASE_ERROR;
        }

        if (WebloggerStartup.isDatabaseCreationRequired()) {
            log.debug("Forwarding to database table creation page");
            setPageTitle("installer.database.creation.pageTitle");
            return CREATE_DATABASE;
        }
        if (WebloggerStartup.isDatabaseUpgradeRequired()) {
            log.debug("Forwarding to database table upgrade page");
            setPageTitle("installer.database.upgrade.pageTitle");
            return UPGRADE_DATABASE;
        }

        setPageTitle("installer.error.unknown.pageTitle");
        rootCauseException = new Exception("UNKNOWN ERROR");
        rootCauseException.fillInStackTrace();
        return BOOTSTRAP;
    }


    public String create() {

        if (WebloggerFactory.isBootstrapped()) {
            return SUCCESS;
        }

        try {
            messages = WebloggerStartup.createDatabase();

            success = true;
        } catch (StartupException se) {
            error = true;
            messages = se.getStartupLog();
        }

        setPageTitle("installer.database.creation.pageTitle");
        return CREATE_DATABASE;
    }


    public String upgrade() {

        if (WebloggerFactory.isBootstrapped()) {
            return SUCCESS;
        }

        try {
            messages = WebloggerStartup.upgradeDatabase(true);

            success = true;
        } catch (StartupException se) {
            error = true;
            messages = se.getStartupLog();
        }

        setPageTitle("installer.database.upgrade.pageTitle");
        return UPGRADE_DATABASE;
    }


    public String bootstrap() {
        log.info("ENTERING");

        if (WebloggerFactory.isBootstrapped()) {
            log.info("EXITING - already bootstrapped, forwarding to Roller");
            return SUCCESS;
        }

        try {
            // trigger bootstrapping process
            WebloggerFactory.bootstrap();

            // trigger initialization process
            WebloggerFactory.getWeblogger().initialize();

            // also need to do planet if it's configured
            if (WebloggerConfig.getBooleanProperty("planet.aggregator.enabled")) {

                // Now prepare the core services of planet so we can bootstrap it
                try {
                    WebloggerStartup.prepare();
                } catch (Exception ex) {
                    log.fatal("Roller Planet startup failed during app preparation", ex);
                }
            }
            log.info("EXITING - Bootstrap successful, forwarding to Roller");
            return SUCCESS;

        } catch (BootstrapException ex) {
            log.error("BootstrapException", ex);
            rootCauseException = ex;
        } catch (WebloggerException ex) {
            log.error("WebloggerException", ex);
            rootCauseException = ex;
        } catch (Exception e) {
            log.error("Exception", e);
            rootCauseException = e;
        }

        log.info("EXITING - Bootstrap failed, forwarding to error page");
        setPageTitle("installer.error.unknown.pageTitle");
        return BOOTSTRAP;
    }


    public String getDatabaseProductName() {
        String name = "unknown";

        Connection con = null;
        try {
            con = WebloggerStartup.getDatabaseProvider().getConnection();
            name = con.getMetaData().getDatabaseProductName();
        } catch (Exception intentionallyIgnored) {
            // ignored
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ex) {
                }
            }
        }

        return name;
    }

    @Override
    public String getProp(String key) {
        // Static config only, we don't have database yet
        String value = WebloggerConfig.getProperty(key);
        return (value == null) ? key : value;
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

    public boolean isUpgradeRequired() {
        return WebloggerStartup.isDatabaseUpgradeRequired();
    }

    public boolean isError() {
        return error;
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public boolean isSuccess() {
        return success;
    }

}
