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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.DatabaseProvider;


/**
 * Handles the install/upgrade of the Roller Weblogger database when the user
 * has configured their installation type to 'auto'.
 */
public class DatabaseInstaller {
    
    private static Log log = LogFactory.getLog(DatabaseInstaller.class);
    
    private final DatabaseProvider db;
    private final DatabaseScriptProvider scripts;
    private final String version;
    private List<String> messages = new ArrayList<String>();
    
    // the name of the property which holds the dbversion value
    private static final String DBVERSION_PROP = "roller.database.version";
    
    
    public DatabaseInstaller(DatabaseProvider dbProvider, DatabaseScriptProvider scriptProvider) {
        db = dbProvider;
        scripts = scriptProvider;
        
        Properties props = new Properties();
        try {
            props.load(getClass().getResourceAsStream("/roller-version.properties"));
        } catch (IOException e) {
            log.error("roller-version.properties not found", e);
        }
        
        version = props.getProperty("ro.version", "UNKNOWN");
    }
    
    
    /** 
     * Determine if database schema needs to be upgraded.
     */
    public boolean isCreationRequired() {
        Connection con = null;
        try {            
            con = db.getConnection();
            
            // just check for a couple key Roller tables
            // roller_user table called rolleruser before Roller 5.1
            if (tableExists(con, "weblog") && (tableExists(con, "roller_user") || tableExists(con, "rolleruser"))) {
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
        int desiredVersion = parseVersionString(version);
        int databaseVersion;
        try {
            databaseVersion = getDatabaseVersion();
        } catch (StartupException ex) {
            throw new RuntimeException(ex);
        }
        
        // if dbversion is unset then assume a new install, otherwise compare
        if (databaseVersion < 0) {
            // if this is a fresh db then we need to set the database version
            Connection con = null;
            try {
                con = db.getConnection();
                setDatabaseVersion(con, version);
            } catch (Exception ioe) {
                errorMessage("ERROR setting database version");
            } finally {
                try {
                    if (con != null) {
                        con.close();
                    }
                } catch (Exception ignored) {
                }
            }

            return false;
        } else {
            return databaseVersion < desiredVersion;
        }
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
     */
    public void createDatabase() throws StartupException {
        
        log.info("Creating Roller Weblogger database tables.");
        
        Connection con = null;
        SQLScriptRunner create = null;
        try {
            con = db.getConnection();
            String handle = getDatabaseHandle(con);
            create = new SQLScriptRunner(scripts.getDatabaseScript(handle + "/createdb.sql"));
            create.runScript(con, true);
            messages.addAll(create.getMessages());
            
            setDatabaseVersion(con, version);
            
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
    }
    
    
    /**
     * Upgrade database if dbVersion is older than desiredVersion.
     */
    public void upgradeDatabase(boolean runScripts) throws StartupException {
        
        int myVersion = parseVersionString(version);
        int dbversion = getDatabaseVersion();
        
        log.debug("Database version = "+dbversion);
        log.debug("Desired version = "+myVersion);
       
        Connection con = null;
        try {
            con = db.getConnection();
            if(dbversion < 0) {
                String msg = "Cannot upgrade database tables, Roller database version cannot be determined";
                errorMessage(msg);
                throw new StartupException(msg);
            } else if (dbversion < 400) {
                String msg = "Roller " + myVersion + " cannot upgrade from versions older than 4.0; " +
                        "try first upgrading to an earlier version of Roller.";
                errorMessage(msg);
                throw new StartupException(msg);
            } else if(dbversion >= myVersion) {
                log.info("Database is current, no upgrade needed");
                return;
            }

            log.info("Database is old, beginning upgrade to version "+myVersion);

            // iterate through each upgrade as needed
            // to add to the upgrade sequence simply add a new "if" statement
            // for whatever version needed and then define a new method upgradeXXX()

            if(dbversion < 500) {
                upgradeTo500(con, runScripts);
                dbversion = 500;
            }
            if(dbversion < 510) {
                upgradeTo510(con, runScripts);
                dbversion = 510;
            }
            if(dbversion < 520) {
                upgradeTo520(con, runScripts);
                dbversion = 520;
            }
            
            // make sure the database version is the exact version
            // we are upgrading too.
            updateDatabaseVersion(con, myVersion);
        
        } catch (SQLException e) {
            throw new StartupException("ERROR obtaining connection");
        } finally {
            try {
                if (con != null) {
                    con.close();
                }
            } catch (Exception ignored) {}
        }
    }

    /**
     * Upgrade database to Roller 5.0
     */
    private void upgradeTo500(Connection con, boolean runScripts) throws StartupException {
        
        // first we need to run upgrade scripts 
        SQLScriptRunner runner = null;
        try {    
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/400-to-500-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch(Exception ex) {
            log.error("ERROR running 500 database upgrade script", ex);
            if (runner != null) {
                messages.addAll(runner.getMessages());
            }
            
            errorMessage("Problem upgrading database to version 500", ex);
            throw new StartupException("Problem upgrading database to version 500", ex);
        }        
    }

    /**
     * Upgrade database to Roller 5.1
     */
	private void upgradeTo510(Connection con, boolean runScripts) throws StartupException {
        
        // first we need to run upgrade scripts 
        SQLScriptRunner runner = null;
        try {    
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/500-to-510-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);                
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch(Exception ex) {
            log.error("ERROR running 510 database upgrade script", ex);
            if (runner != null) {
                messages.addAll(runner.getMessages());
            }
            
            errorMessage("Problem upgrading database to version 510", ex);
            throw new StartupException("Problem upgrading database to version 510", ex);
        }        
	}

    /**
     * Upgrade database to Roller 5.2
     */
    private void upgradeTo520(Connection con, boolean runScripts) throws StartupException {

        // first we need to run upgrade scripts
        SQLScriptRunner runner = null;
        try {
            if (runScripts) {
                String handle = getDatabaseHandle(con);
                String scriptPath = handle + "/510-to-520-migration.sql";
                successMessage("Running database upgrade script: "+scriptPath);
                runner = new SQLScriptRunner(scripts.getDatabaseScript(scriptPath));
                runner.runScript(con, true);
                messages.addAll(runner.getMessages());
            }
        } catch(Exception ex) {
            log.error("ERROR running 520 database upgrade script", ex);
            if (runner != null) {
                messages.addAll(runner.getMessages());
            }

            errorMessage("Problem upgrading database to version 520", ex);
            throw new StartupException("Problem upgrading database to version 520", ex);
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
        } else if (productName.toLowerCase().contains("microsoft")) {
            handle =  "mssql";
        } else if (productName.toLowerCase().contains("db2")) {
            handle =  "db2";
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
    
    
    private int getDatabaseVersion() throws StartupException {
        int dbversion = -1;
        
        // get the current db version
        Connection con = null;
        try {
            con = db.getConnection();
            Statement stmt = con.createStatement();
            
            // just check in the roller_properties table
            ResultSet rs = stmt.executeQuery(
                    "select value from roller_properties where name = '"+DBVERSION_PROP+"'");
            
            if(rs.next()) {
                dbversion = Integer.parseInt(rs.getString(1));
                
            } else {
                // tough to know if this is an upgrade with no db version :/
                // however, if roller_properties is not empty then we at least
                // we have someone upgrading from 1.2.x
                rs = stmt.executeQuery("select count(*) from roller_properties");
                if (rs.next() && rs.getInt(1) > 0) {
                    dbversion = 120;
                }
            }
            
        } catch(Exception e) {
            // that's strange ... hopefully we didn't need to upgrade
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
    
    
    private int parseVersionString(String vstring) {        
        int myversion = 0;
        
        // NOTE: this assumes a maximum of 3 digits for the version number
        // so if we get to 10.0 then we'll need to upgrade this
        
        // strip out non-digits
        vstring = vstring.replaceAll("\\Q.\\E", "");
        vstring = vstring.replaceAll("\\D", "");
        if(vstring.length() > 3) {
            vstring = vstring.substring(0, 3);
        }
        
        // parse to an int
        try {
            int parsed = Integer.parseInt(vstring);            
            if(parsed < 100) {
                myversion = parsed * 10;
            } else {
                myversion = parsed;
            }
        } catch(Exception e) {}
        
        return myversion;
    }
    

    /**
     * Insert a new database.version property.
     * This should only be called once for new installations
     */
    private void setDatabaseVersion(Connection con, String version) 
            throws StartupException {
        setDatabaseVersion(con, parseVersionString(version));
    }

    /**
     * Insert a new database.version property.
     * This should only be called once for new installations
     */
    private void setDatabaseVersion(Connection con, int version)
            throws StartupException {
        
        try {
            Statement stmt = con.createStatement();
            stmt.executeUpdate("insert into roller_properties "+
                    "values('"+DBVERSION_PROP+"', '"+version+"')");
            
            log.debug("Set database verstion to "+version);
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
            stmt.executeUpdate("update roller_properties "+
                    "set value = '"+version+"'"+
                    "where name = '"+DBVERSION_PROP+"'");
            
            log.debug("Updated database verstion to "+version);
        } catch(SQLException se) {
            throw new StartupException("Error setting database version.", se);
        } 
    }

}
