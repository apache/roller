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
package org.apache.roller.weblogger.business.startup;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.roller.weblogger.business.WebloggerStaticConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Handles the install/upgrade of the TightBlog Weblogger database when the user
 * has configured their installation type to 'auto'.
 */
public class DatabaseInstaller {

    private static Logger log = LoggerFactory.getLogger(DatabaseInstaller.class);
    
    private final DatabaseProvider db;
    private final int targetVersion;
    private List<String> messages = new ArrayList<>();
    
    // the name of the property in weblogger_properties table which holds the dbversion value
    private static final String DBVERSION_PROP = "tightblog.database.version";
    
    public DatabaseInstaller(DatabaseProvider dbProvider) {
        db = dbProvider;
        targetVersion = WebloggerStaticConfig.getIntProperty("tightblog.database.expected.version");
    }

    /** 
     * Determine if database schema needs to be upgraded.
     */
    public boolean isCreationRequired() {
        Connection con = null;
        try {            
            con = db.getConnection();
            
            // just check for a couple key database tables
            if (tableExists(con, "weblog") && (tableExists(con, "weblogger_user"))) {
                return false;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error checking for tables", e);
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {}
        }
        
        return true;
    }
    
    
    /** 
     * Determine if database schema needs to be upgraded.
     */
    public boolean isUpgradeRequired() {
        return getDatabaseVersion() < targetVersion;
    }
    
    
    public List<String> getMessages() {
        return messages;
    }
    
    
    private void errorMessage(String msg) {
        messages.add(msg);
        log.error(msg);
    }    
    
    
    private void errorMessage(String msg, Throwable t) {
        messages.add(msg);
        log.error(msg, t);
    }
    
    
    private void successMessage(String msg) {
        messages.add(msg);
        log.trace(msg);
    }
    
    
    /**
     * Create datatabase tables.
     * @return List of messages created during processing
     */
    public List<String> createDatabase() throws StartupException {
        
        log.info("Creating weblogger database tables.");
        
        Connection con = null;
        SQLScriptRunner create = null;
        try {
            con = db.getConnection();
            String handle = getDatabaseHandle(con);
            create = new SQLScriptRunner(handle + "/createdb.sql", true);
            create.runScript(con, true);
            messages.addAll(create.getMessages());
            
            insertDatabaseVersion(con, targetVersion);
            
        } catch (SQLException sqle) {
            log.error("ERROR running SQL in database creation script", sqle);
            if (create != null) {
                messages.addAll(create.getMessages());
            }
            errorMessage("ERROR running SQL in database creation script");
            throw new StartupException("Error running sql script", sqle); 
            
        } catch (Exception ioe) {
            log.error("ERROR running database creation script", ioe);
            if (create != null) {
                messages.addAll(create.getMessages());
            }
            errorMessage("ERROR reading/parsing database creation script");
            throw new StartupException("Error running SQL script", ioe);

        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {}
        }

        return messages;
    }
    
    
    /**
     * Upgrade database if dbVersion is older than desiredVersion.
     * @return List of messages created during processing
     */
    public List<String> upgradeDatabase(boolean runScripts) throws StartupException {
        
        int currentDbVersion = getDatabaseVersion();
        
        log.debug("Database version = " + currentDbVersion);
        log.debug("Desired version = " + targetVersion);
       
        Connection con = null;
        try {
            con = db.getConnection();
            if (currentDbVersion < 0) {
                String msg = "Cannot upgrade database tables, TightBlog database version cannot be determined";
                errorMessage(msg);
                throw new StartupException(msg);
            } if (currentDbVersion >= targetVersion) {
                log.info("Database is current, no upgrade needed");
                return null;
            }

            log.info("Database is old (version {}), beginning upgrade to DB version {}", currentDbVersion, targetVersion);

            // iterate through each upgrade as needed
            // to add to the upgrade sequence simply add a new "if" statement for whatever version needed
            if (runScripts) {
                // no-op until new versions released
            }

            // finished, update DB version in properties table
            updateDatabaseVersion(con, targetVersion);
        
        } catch (SQLException e) {
            throw new StartupException("ERROR obtaining connection");
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {}
        }

        return messages;
    }

    private void runDbUpgrade(Connection con, String versionStr, String script) throws StartupException {

        SQLScriptRunner runner = null;
        try {
            String handle = getDatabaseHandle(con);
            String scriptPath = handle + script;
            successMessage("Running database upgrade script: " + scriptPath);
            runner = new SQLScriptRunner(scriptPath, true);
            runner.runScript(con, true);
            messages.addAll(runner.getMessages());
        } catch(Exception ex) {
            log.error("ERROR running {} database upgrade script", versionStr, ex);
            if (runner != null) {
                messages.addAll(runner.getMessages());
            }

            errorMessage("Problem upgrading database to version " + versionStr, ex);
            throw new StartupException("Problem upgrading database to version " + versionStr, ex);
        }
    }


    /**
     * Use database product name to get the database script directory name.
     */
    public String getDatabaseHandle(Connection con) throws SQLException {
        
        String productName = con.getMetaData().getDatabaseProductName();
        String handle = "mysql";
        if (       productName.toLowerCase().contains("mysql")) {
            handle =  "mysql";
        } else if (productName.toLowerCase().contains("derby")) {
            handle =  "derby";
        } else if (productName.toLowerCase().contains("hsql")) {
            handle =  "hsqldb";
        } else if (productName.toLowerCase().contains("postgres")) {
            handle =  "postgresql";
        } else if (productName.toLowerCase().contains("oracle")) {
            handle =  "oracle";
        }
        
        return handle;
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
    
    
    private int getDatabaseVersion() {
        int dbversion = -1;
        
        // get the current db version
        Connection con = null;
        try {
            con = db.getConnection();
            Statement stmt = con.createStatement();
            
            // just check in the weblogger_properties table
            ResultSet rs = stmt.executeQuery(
                    "select value from weblogger_properties where name = '" + DBVERSION_PROP + "'");
            
            if (rs.next()) {
                dbversion = Integer.parseInt(rs.getString(1));
            }
        } catch(Exception e) {
            // that's strange ... hopefully we don't need to upgrade
            log.error("Couldn't lookup current database version", e);           
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {}
        }       
        return dbversion;
    }
    
    /**
     * Insert a new database.version property.
     * This should only be called once for new installations
     */
    private void insertDatabaseVersion(Connection con, int version) throws StartupException {
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("insert into weblogger_properties values('"
                    + DBVERSION_PROP + "', '" + version + "')");
            
            log.debug("Set database version to {}", version);
        } catch(SQLException se) {
            throw new StartupException("Error setting database version.", se);
        }
    }
    
    
    /**
     * Update the existing database.version property
     */
    private void updateDatabaseVersion(Connection con, int version)
            throws StartupException {
        
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("update weblogger_properties set value = '"
                    + version + "' where name = '" + DBVERSION_PROP + "'");
            
            log.debug("Updated database version to {}", version);
        } catch(SQLException se) {
            throw new StartupException("Error setting database version.", se);
        } 
    }
}
