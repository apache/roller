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
package org.apache.roller.weblogger.ui.struts2;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.roller.weblogger.business.WebloggerContext;
import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.apache.roller.weblogger.pojos.GlobalRole;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.util.SQLScriptRunner;
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

    private static final String INSTALL = "install";

    private Throwable rootCauseException = null;
    private List<String> messages = new ArrayList<>();

    public enum StartupStatus {
        databaseError(true),
        tablesMissing(false),
        databaseVersionError(true),
        databaseCreateError(true),
        needsBootstrapping(false),
        bootstrapError(false);

        boolean error;

        public boolean isError() {
            return error;
        }

        StartupStatus(boolean error) {
            this.error = error;
        }
    }

    private StartupStatus status = null;

    private String databaseProductName = "";

    private DataSource tbDataSource;

    public void setTbDataSource(DataSource tbDataSource) {
        this.tbDataSource = tbDataSource;
    }

    public Install() {
        requiredGlobalRole = GlobalRole.NOAUTHNEEDED;
        requiredWeblogRole = WeblogRole.NOBLOGNEEDED;
    }

    public String execute() throws WebloggerException {
        if (WebloggerContext.isBootstrapped()) {
            return SUCCESS;
        }

        // is database accessible?
        try {
            Connection testcon = tbDataSource.getConnection();
            // used if DB creation needed
            databaseProductName = testcon.getMetaData().getDatabaseProductName();
            testcon.close();
        } catch (Exception e) {
            log.error(getText("installer.databaseConnectionError"));
            status = StartupStatus.databaseError;
            rootCauseException = e.getCause();
            messages = Collections.singletonList(e.getMessage());
            return INSTALL;
        }

        status = checkDatabase();

        // is database schema present?
        if (StartupStatus.tablesMissing.equals(status)) {
            log.info("TightBlog database needs creating, forwarding to creation page");
            return INSTALL;
        } else if (StartupStatus.databaseVersionError.equals(status)) {
            log.warn("TightBlog DB version incompatible with application version.");
            return INSTALL;
        }

        // all good, TightBlog ready to bootstrap
        return bootstrap();
    }

    /**
     * Determine if database schema needs to be created.
     */
    private StartupStatus checkDatabase() {
        Connection con = null;
        try {
            con = tbDataSource.getConnection();

            // does the schema already exist?  -- check a couple of tables to find out
            if (!tableExists(con, "weblog") || !tableExists(con, "weblogger_user")) {
                return StartupStatus.tablesMissing;
            }

            // OK, exists -- does the database schema match that used by the application?
            int applicationVersion = WebloggerStaticConfig.getIntProperty("tightblog.database.expected.version");
            int dbversion = -1;
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(
                    "select database_version from weblogger_properties where id = 1");

            if (rs.next()) {
                dbversion = Integer.parseInt(rs.getString(1));
            }

            if (dbversion != applicationVersion) {
                return StartupStatus.databaseVersionError;
            }
        } catch (Exception e) {
            log.error("Error checking for tables", e);
            return StartupStatus.databaseVersionError;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    /**
     * Create database tables.
     *
     * @return List of messages created during processing
     */
    public String create() {
        if (WebloggerContext.isBootstrapped()) {
            return SUCCESS;
        }

        String script = "/createdb.sql";
        Connection con = null;
        SQLScriptRunner runner = null;

        try {
            con = tbDataSource.getConnection();
            String scriptPath = con.getMetaData().getDatabaseProductName().toLowerCase() + script;
            messages.add("Running database script: " + scriptPath);
            runner = new SQLScriptRunner(scriptPath, true);
            runner.runScript(con, true);
            messages.addAll(runner.getMessages());
            status = StartupStatus.needsBootstrapping;
        } catch (Exception ex) {
            messages.add("ERROR processing database script " + script);
            if (runner != null) {
                messages.addAll(runner.getMessages());
            }
            status = StartupStatus.databaseCreateError;
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }

        return INSTALL;
    }

    public String bootstrap() {
        if (WebloggerContext.isBootstrapped()) {
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

        status = StartupStatus.bootstrapError;
        return INSTALL;
    }

    public String getDatabaseProductName() {
        return databaseProductName;
    }

    public String getRootCauseStackTrace() {
        String stackTrace = "";
        if (rootCauseException != null) {
            StringWriter sw = new StringWriter();
            rootCauseException.printStackTrace(new PrintWriter(sw));
            stackTrace = sw.toString().trim();
        }
        return stackTrace;
    }

    public List<String> getMessages() {
        return messages;
    }

    public StartupStatus getStatus() {
        return status;
    }

    /**
     * Return true if named table exists in database.
     */
    private boolean tableExists(Connection con, String tableName) throws SQLException {
        ResultSet rs = con.getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (tableName.equalsIgnoreCase(rs.getString("TABLE_NAME").toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
