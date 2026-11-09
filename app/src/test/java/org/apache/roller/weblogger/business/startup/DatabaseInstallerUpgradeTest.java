/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  The ASF licenses this file to You
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

package org.apache.roller.weblogger.business.startup;

import java.sql.*;
import java.util.Properties;
import java.util.UUID;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseInstallerUpgradeTest {

    @Test
    void upgradeFrom610RemovesObsoleteTablesAndSettings() throws Exception {
        assertUpgradeRemovesObsoleteTablesAndSettings(610);
    }

    @Test
    void upgradeFrom615RemovesObsoleteTablesAndSettings() throws Exception {
        assertUpgradeRemovesObsoleteTablesAndSettings(615);
    }

    @Test
    void upgradeFrom617RemovesObsoleteTablesAndSettings() throws Exception {
        assertUpgradeRemovesObsoleteTablesAndSettings(617);
    }

    private void assertUpgradeRemovesObsoleteTablesAndSettings(int oldVersion) throws Exception {
        String url = "jdbc:derby:memory:upgrade" + UUID.randomUUID() + ";create=true";
        DatabaseProvider db = mock(DatabaseProvider.class);
        when(db.getConnection()).thenAnswer(invocation -> DriverManager.getConnection(url));
        DatabaseScriptProvider scripts = path -> getClass().getResourceAsStream("/dbscripts/" + path);

        try (Connection con = DriverManager.getConnection(url); Statement stmt = con.createStatement()) {
            stmt.executeUpdate("create table weblog (id varchar(48) primary key)");
            stmt.executeUpdate("create table pingtarget (id varchar(48) primary key)");
            stmt.executeUpdate("create table autoping (id varchar(48) primary key, "
                    + "websiteid varchar(48) references weblog(id), "
                    + "pingtargetid varchar(48) references pingtarget(id))");
            stmt.executeUpdate("create table pingqueueentry (id varchar(48) primary key)");
            stmt.executeUpdate("create table roller_properties (name varchar(255) primary key, value varchar(255))");
            stmt.executeUpdate("create table roller_tasklock (id varchar(48) primary key, name varchar(255))");
            stmt.executeUpdate("insert into weblog values ('weblog')");
            stmt.executeUpdate("insert into pingtarget values ('target')");
            stmt.executeUpdate("insert into autoping values ('auto', 'weblog', 'target')");
            stmt.executeUpdate("insert into pingqueueentry values ('queued')");
            stmt.executeUpdate("insert into roller_properties values ('roller.database.version', '" + oldVersion + "')");
            stmt.executeUpdate("insert into roller_properties values ('pings.suspendPingProcessing', 'false')");
            stmt.executeUpdate("insert into roller_properties values ('pings.logOnly', 'true')");
            stmt.executeUpdate("insert into roller_properties values ('site.name', 'Keep this site')");
            stmt.executeUpdate("insert into roller_tasklock values ('old', 'PingQueueTask')");
            stmt.executeUpdate("insert into roller_tasklock values ('keep', 'ScheduledEntriesTask')");

            DatabaseInstaller installer = new DatabaseInstaller(db, scripts);
            assertTrue(installer.isUpgradeRequired());
            installer.upgradeDatabase(true);

            assertNoObsoleteTables(con);
            assertEquals(0, count(stmt, "select count(*) from roller_properties where name like 'pings.%'"));
            assertEquals(0, count(stmt, "select count(*) from roller_tasklock where name = 'PingQueueTask'"));
            assertEquals(1, count(stmt, "select count(*) from roller_tasklock where name = 'ScheduledEntriesTask'"));
            assertEquals(1, count(stmt, "select count(*) from roller_properties where name = 'site.name' and value = 'Keep this site'"));
            assertVersion700(stmt);
            assertEquals(1, count(stmt, "select count(*) from weblog"));
            assertFalse(installer.isUpgradeRequired());
            assertFalse(installer.getMessages().stream().anyMatch(m -> m.contains("No table changes were required.")));
        }
    }

    @Test
    void freshInstallOmitsObsoleteTables() throws Exception {
        String url = "jdbc:derby:memory:fresh" + UUID.randomUUID() + ";create=true";
        DatabaseProvider db = mock(DatabaseProvider.class);
        when(db.getConnection()).thenAnswer(invocation -> DriverManager.getConnection(url));
        DatabaseScriptProvider scripts = path -> getClass().getResourceAsStream("/dbscripts/" + path);
        DatabaseInstaller installer = new DatabaseInstaller(db, scripts);

        installer.createDatabase();

        try (Connection con = DriverManager.getConnection(url); Statement stmt = con.createStatement()) {
            assertNoObsoleteTables(con);
            assertVersion700(stmt);
            assertEquals(0, count(stmt, "select count(*) from weblogentry"));
            assertFalse(installer.isCreationRequired());
            assertFalse(installer.isUpgradeRequired());
        }
    }

    private void assertNoObsoleteTables(Connection con) throws SQLException {
        for (String name : new String[] {"AUTOPING", "PINGQUEUEENTRY", "PINGTARGET"}) {
            try (ResultSet tables = con.getMetaData().getTables(null, "APP", name, new String[] {"TABLE"})) {
                assertFalse(tables.next(), name + " should be absent");
            }
        }
    }

    private int count(Statement stmt, String sql) throws SQLException {
        try (ResultSet rows = stmt.executeQuery(sql)) {
            assertTrue(rows.next());
            return rows.getInt(1);
        }
    }

    private void assertVersion700(Statement stmt) throws SQLException {
        try (ResultSet rows = stmt.executeQuery("select value from roller_properties where name = 'roller.database.version'")) {
            assertTrue(rows.next());
            assertEquals("700", rows.getString(1));
        }
    }

    @Test
    void currentSchemaDoesNotRequireUpgrade() throws Exception {
        DatabaseProvider db = mock(DatabaseProvider.class);
        Connection con = mock(Connection.class);
        Statement query = mock(Statement.class);
        ResultSet rows = mock(ResultSet.class);
        when(db.getConnection()).thenReturn(con);
        when(con.createStatement()).thenReturn(query);
        when(query.executeQuery(anyString())).thenReturn(rows);
        when(rows.next()).thenReturn(true);
        when(rows.getString(1)).thenReturn("700");

        DatabaseInstaller installer = new DatabaseInstaller(db, mock(DatabaseScriptProvider.class));

        assertFalse(installer.isUpgradeRequired());
    }

    @Test
    void pendingDatabaseMigrationRequiresUpgrade() throws Exception {
        DatabaseProvider db = mock(DatabaseProvider.class);
        Connection con = mock(Connection.class);
        Statement query = mock(Statement.class);
        ResultSet rows = mock(ResultSet.class);
        when(db.getConnection()).thenReturn(con);
        when(con.createStatement()).thenReturn(query);
        when(query.executeQuery(anyString())).thenReturn(rows);
        when(rows.next()).thenReturn(true);
        when(rows.getString(1)).thenReturn("615");

        DatabaseInstaller installer = new DatabaseInstaller(db, mock(DatabaseScriptProvider.class));

        assertTrue(installer.isUpgradeRequired());
    }

    @Test
    void schemaUpgradeSuppressesNoChangesMessage() throws Exception {
        DatabaseProvider db = mock(DatabaseProvider.class);
        DatabaseScriptProvider scripts = mock(DatabaseScriptProvider.class);
        Connection con = mock(Connection.class);
        Statement query = mock(Statement.class);
        ResultSet rows = mock(ResultSet.class);
        PreparedStatement update = mock(PreparedStatement.class);
        when(db.getConnection()).thenReturn(con);
        when(con.createStatement()).thenReturn(query);
        when(query.executeQuery(anyString())).thenReturn(rows);
        when(rows.next()).thenReturn(true);
        when(rows.getString(1)).thenReturn("520");
        when(con.prepareStatement(anyString())).thenReturn(update);
        DatabaseInstaller installer = new DatabaseInstaller(db, scripts);

        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/roller-version.properties"));
        int expectedVersion = DatabaseInstaller.parseVersionString(props.getProperty("ro.version", "UNKNOWN"));

        // A 520 database upgrades through the 610 and 700 schema steps; run with
        // runScripts=false so the step does its bookkeeping without executing a
        // migration script. Because a schema step ran, the "no table changes"
        // message must be suppressed.
        installer.upgradeDatabase(false);

        verify(update).setString(1, String.valueOf(expectedVersion));
        verify(update).executeUpdate();
        assertTrue(installer.getMessages().stream().anyMatch(m -> m.contains("Database version updated to " + expectedVersion + ".")));
        assertFalse(installer.getMessages().stream().anyMatch(m -> m.contains("No table changes were required.")));
    }
}
