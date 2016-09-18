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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.struts2.core;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.roller.weblogger.business.DatabaseInstaller;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.struts2.util.UIAction;
import org.apache.roller.weblogger.util.Utilities;
import org.apache.roller.weblogger.util.WebloggerException;
import org.apache.struts2.ServletActionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.sql.DataSource;

/**
 * Walk user through install process.
 */
public class Install extends UIAction {

    private static Logger log = LoggerFactory.getLogger(Install.class);

    private static final String DATABASE_ERROR = "database_error";
    private static final String CREATE_DATABASE = "create_database";
    private static final String UPGRADE_DATABASE = "upgrade_database";
    private static final String BOOTSTRAP = "bootstrap";

    private Throwable rootCauseException = null;
    private boolean error = false;
    private boolean success = false;
    private List<String> messages = null;

    private DatabaseInstaller databaseInstaller;

    private DataSource tbDataSource;

    public void setTbDataSource(DataSource tbDataSource) {
        this.tbDataSource = tbDataSource;
        databaseInstaller = new DatabaseInstaller(tbDataSource);
    }

    @Override
    public GlobalRole getRequiredGlobalRole() {
        return GlobalRole.NOAUTHNEEDED;
    }

    @Override
    public WeblogRole getRequiredWeblogRole() {
        return WeblogRole.NOBLOGNEEDED;
    }

    public String execute() throws WebloggerException {
        if (WebloggerContext.isBootstrapped()) {
            return SUCCESS;
        }

        try {
            Utilities.testDataSource(tbDataSource);
        } catch (WebloggerException we) {
            if (we.getRootCause() != null) {
                rootCauseException = we.getRootCause();
            } else {
                rootCauseException = we;
            }
            messages = Collections.singletonList(we.getMessage());

            log.debug("Forwarding to database error page");
            setPageTitle("installer.error.connection.pageTitle");
            return DATABASE_ERROR;
        }

        if (databaseInstaller.isCreationRequired()) {
            log.info("TightBlog database needs creating, forwarding to creation page");
            setPageTitle("installer.database.creation.pageTitle");
            return CREATE_DATABASE;
        }

        if (databaseInstaller.isUpgradeRequired()) {
            log.info("TightBlog database needs upgrading, forwarding to upgrade page");
            setPageTitle("installer.database.upgrade.pageTitle");
            return UPGRADE_DATABASE;
        }

        bootstrap();
        return SUCCESS;
    }

    public String create() {
        if (WebloggerContext.isBootstrapped()) {
            return SUCCESS;
        }

        try {
            messages = databaseInstaller.createDatabase();
            success = true;
        } catch (WebloggerException se) {
            error = true;
            messages = Collections.emptyList();
        }

        setPageTitle("installer.database.creation.pageTitle");
        return CREATE_DATABASE;
    }

    public String upgrade() {
        if (WebloggerContext.isBootstrapped()) {
            return SUCCESS;
        }

        try {
            messages = databaseInstaller.upgradeDatabase();
            success = true;
        } catch (WebloggerException se) {
            error = true;
            messages = Collections.emptyList();
        }

        setPageTitle("installer.database.upgrade.pageTitle");
        return UPGRADE_DATABASE;
    }

    public String bootstrap() {
        log.info("ENTERING");

        if (WebloggerContext.isBootstrapped()) {
            log.info("EXITING - already bootstrapped, forwarding to weblogger");
            return SUCCESS;
        }

        try {
            // trigger bootstrapping process
            ServletContext sc = ServletActionContext.getServletContext();
            ApplicationContext ac = WebApplicationContextUtils.getRequiredWebApplicationContext(sc);

            WebloggerContext.bootstrap(ac);
            log.info("EXITING - Bootstrap successful, forwarding to weblogger");
            return SUCCESS;
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
            con = tbDataSource.getConnection();
            name = con.getMetaData().getDatabaseProductName();
        } catch (Exception intentionallyIgnored) {
            // ignored
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (Exception ignored) {
                }
            }
        }

        return name;
    }

    public String getProp(String key) {
        // Static config only, we don't have database yet
        String value = WebloggerStaticConfig.getProperty(key);
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

    public boolean isError() {
        return error;
    }

    public List<String> getMessages() {
        return messages;
    }

    public boolean isSuccess() {
        return success;
    }

}
