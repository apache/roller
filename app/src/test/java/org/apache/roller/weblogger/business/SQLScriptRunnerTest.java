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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.roller.weblogger.WebloggerTest;
import org.apache.roller.weblogger.business.startup.SQLScriptRunner;
import org.junit.Before;
import org.junit.Test;

import javax.annotation.Resource;
import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * Test parsing and running of SQL scripts
 */
public class SQLScriptRunnerTest extends WebloggerTest {

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Resource
    private DataSource tbDataSource;

    public void setTbDataSource(DataSource tbDataSource) {
        this.tbDataSource = tbDataSource;
    }

    @Test
    public void testParseOnly() throws Exception {
        Connection con = tbDataSource.getConnection();
        
        // normally tests run against Derby
        String databaseProductName = con.getMetaData().getDatabaseProductName();
        String dbname = "derby";
        if (databaseProductName.toLowerCase().indexOf("mysql") > 0) {
            // but some folks test against MySQL
            dbname = "mysql";
        }
        
        String scriptPath = System.getProperty("project.build.directory")
                + "/test-classes/WEB-INF/dbscripts/dummydb/createdb-"+dbname+".sql";
        SQLScriptRunner runner = new SQLScriptRunner(scriptPath, false);
        assertTrue(runner.getCommandCount() == 5);
    }

    @Test
    public void testSimpleRun() throws Exception {
        Connection con = tbDataSource.getConnection();

        // normaly tests run against Derby
        String databaseProductName = con.getMetaData().getDatabaseProductName();
        String dbname = "derby";
        if (databaseProductName.toLowerCase().indexOf("mysql") > 0) {
            // but some folks test against MySQL
            dbname = "mysql";
        }
        
        // run script to create tables
        SQLScriptRunner create = 
            new SQLScriptRunner(System.getProperty("project.build.directory")
                    + "/test-classes/WEB-INF/dbscripts/dummydb/createdb-"+dbname+".sql", false);
        create.runScript(con, true);
        
        // check to ensure tables were created
        assertTrue(tableExists(con, "testrolleruser"));
        assertTrue(tableExists(con, "testuserrole"));
        
        // drop tables
        SQLScriptRunner drop = 
            new SQLScriptRunner(System.getProperty("project.build.directory") + "/test-classes/WEB-INF/dbscripts/dummydb/droptables.sql", false);
        drop.runScript(con, false);

        // check to ensure tables were dropped
        assertFalse(tableExists(con, "testrolleruser"));
        assertFalse(tableExists(con, "testuserrole"));
    }

    private boolean tableExists(Connection con, String tableName) throws SQLException {
        ResultSet rs = con.getMetaData().getTables(null, null, "%", null);
        while (rs.next()) {
            if (tableName.toLowerCase().equals(rs.getString("TABLE_NAME").toLowerCase())) {
                return true;
            }
        }
        return false;
    }    
}
