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

package org.apache.roller.weblogger.business.utils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.RollerFactory;

/**
 * Creates Roller database.
 */
public class DatabaseCreator {    
    private static Log log = LogFactory.getLog(DatabaseCreator.class); 
    private DatabaseScriptProvider scripts = null;
    private List<String>           messages = new ArrayList<String>();
    
    
    public DatabaseCreator(DatabaseScriptProvider scriptProvider) {
        this.scripts = scriptProvider;
    }
    
    
    /** 
     * Determine if database schema needs to be upgraded.
     */
    public static boolean isCreationRequired() throws WebloggerException {
        Connection con = null;
        try {            
            con = DatabaseProvider.getDatabaseProvider().getConnection();
            
            // just check for a couple key Roller tables
            if (tableExists(con, "rolleruser") && tableExists(con, "userrole")) return false;
            
        } catch (SQLException e) {
            throw new WebloggerException("ERROR obtaining connection");            
        } finally {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
        return true;
    }
    
    
    /**
     * Create datatabase tables.
     */
    public void createDatabase() throws Exception {
        Connection con = null;
        SQLScriptRunner create = null;
        try {
            con = DatabaseProvider.getDatabaseProvider().getConnection();
            String handle = getDatabaseHandle(con);
            create = new SQLScriptRunner(scripts.getDatabaseScript(handle + "/createdb.sql"));
            create.runScript(con, true);
            messages.addAll(create.getMessages());
            
            DatabaseUpgrader.setDatabaseVersion(con, RollerFactory.getRoller().getVersion());
            
        } catch (SQLException sqle) {
            errorMessage("ERROR running SQL in database creation script");
            throw sqle;           
        } catch (Exception ioe) {
            if (create != null) messages.addAll(create.getMessages());
            errorMessage("ERROR reading/parsing database creation script");
            throw ioe;           
        } finally {
            try { if (con != null) con.close(); } catch (Exception ignored) {}
        }
    }
    
    
    /** Return messages from last run of script, empty if no previous run */
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
     * Use database product name to get the database script directory name.
     */
    public static String getDatabaseHandle(Connection con) throws SQLException {
        String productName = con.getMetaData().getDatabaseProductName(); 
        String handle = "mysql";
        if (       productName.toLowerCase().indexOf("mysql") != -1) {
            handle =  "mysql";
        } else if (productName.toLowerCase().indexOf("derby") != -1) {
            handle =  "derby";
        } else if (productName.toLowerCase().indexOf("hsql") != -1) {
            handle =  "hsqldb";
        } else if (productName.toLowerCase().indexOf("postgres") != -1) {
            handle =  "postgresql";
        } else if (productName.toLowerCase().indexOf("oracle") != -1) {
            handle =  "oracle";
        } else if (productName.toLowerCase().indexOf("microsoft") != -1) {
            handle =  "mssql";
        } else if (productName.toLowerCase().indexOf("db2") != -1) {   
            handle =  "db2";
        }
        return handle;
    }

    
    /**
     * Return true if named table exists in database.
     */
    private static boolean tableExists(Connection con, String tableName) throws SQLException {
        String[] types = {"TABLE"};
        ResultSet rs = con.getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (tableName.toLowerCase().equals(rs.getString("TABLE_NAME").toLowerCase())) {
                return true;
            }
        }
        return false;
    }  

}

