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
package org.apache.roller.weblogger.business;

import org.apache.roller.weblogger.util.SQLScriptRunner;
import org.apache.roller.weblogger.util.WebloggerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles the install/upgrade of the TightBlog Weblogger database when the user
 * has configured their installation type to 'auto'.
 */
public class DatabaseInstaller {

    private static Logger log = LoggerFactory.getLogger(DatabaseInstaller.class);

    private final DataSource dataSource;
    private final int targetVersion;
    private List<String> messages = new ArrayList<>();

    public DatabaseInstaller(DataSource dataSource) {
        this.dataSource = dataSource;
        targetVersion = WebloggerStaticConfig.getIntProperty("tightblog.database.expected.version");
    }

    /**
     * Determine if database schema needs to be upgraded.
     */
    public boolean isCreationRequired() {
        Connection con = null;
        try {
            con = dataSource.getConnection();

            // just check for a couple key database tables
            if (tableExists(con, "weblog") && tableExists(con, "weblogger_user")) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking for tables", e);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }

        return true;
    }

    /**
     * Determine if database schema needs to be upgraded.
     */
    public boolean isUpgradeRequired() throws WebloggerException {
        return getDatabaseVersion() < targetVersion;
    }

    public List<String> getMessages() {
        return messages;
    }

    private void errorMessage(String msg) {
        messages.add(msg);
        log.error(msg);
    }

    private void successMessage(String msg) {
        messages.add(msg);
        log.trace(msg);
    }

    /**
     * Create database tables.
     *
     * @return List of messages created during processing
     */
    public List<String> createDatabase() throws WebloggerException {
        runDbScript("/createdb.sql");
        return messages;
    }

    /**
     * Upgrade database if dbVersion is older than desiredVersion.
     *
     * @return List of messages created during processing
     */
    public List<String> upgradeDatabase() throws WebloggerException {
        int currentDbVersion = getDatabaseVersion();
        log.debug("Database version = " + currentDbVersion);
        log.debug("Desired version = " + targetVersion);

        if (currentDbVersion >= targetVersion) {
            log.info("Database is current, no upgrade needed");
            return null;
        }

        log.info("Database is old (version {}), beginning upgrade to DB version {}", currentDbVersion, targetVersion);

        // run each upgrade as needed (no upgrades presently)
        // to add to the upgrade sequence just create a new "if" statement for whatever version needed, e.g.:
        // if (currentDbVersion == 100) {
        //    runDbScript("/100-to-200-migration.sql");
        //    currentDbVersion = 200;
        // };
        // make sure migration scripts update the DBVERSION_PROP to targetVersion.

        return messages;
    }

    private void runDbScript(String script) throws WebloggerException {
        Connection con = null;
        SQLScriptRunner runner = null;
        try {
            con = dataSource.getConnection();
            String scriptPath = con.getMetaData().getDatabaseProductName().toLowerCase() + script;
            successMessage("Running database script: " + scriptPath);
            runner = new SQLScriptRunner(scriptPath, true);
            runner.runScript(con, true);
            messages.addAll(runner.getMessages());
        } catch (Exception ex) {
            errorMessage("ERROR processing database script " + script);
            if (runner != null) {
                messages.addAll(runner.getMessages());
            }
            throw new WebloggerException("Problem running database script " + script, ex);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
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

    private int getDatabaseVersion() throws WebloggerException {
        int dbversion = -1;

        // get the current db version
        Connection con = null;
        try {
            con = dataSource.getConnection();
            Statement stmt = con.createStatement();

            // just check in the weblogger_properties table
            ResultSet rs = stmt.executeQuery(
                    "select database_version from weblogger_properties where id = 1");

            if (rs.next()) {
                dbversion = Integer.parseInt(rs.getString(1));
            }
        } catch (Exception e) {
            String msg = "Current TightBlog database version cannot be determined, check database_version " +
                    " property in weblogger_properties table";
            errorMessage(msg);
            throw new WebloggerException(msg, e);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {
            }
        }
        return dbversion;
    }

}
