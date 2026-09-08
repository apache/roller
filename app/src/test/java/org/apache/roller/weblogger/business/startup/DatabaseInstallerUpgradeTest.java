/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.startup;

import java.sql.*;
import java.util.Properties;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class DatabaseInstallerUpgradeTest {
    @Test
    void versionOnlyUpgradeReportsCompletion() throws Exception {
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
        when(rows.getString(1)).thenReturn("610");
        when(con.prepareStatement(anyString())).thenReturn(update);
        DatabaseInstaller installer = new DatabaseInstaller(db, scripts);

        // the version the installer writes is the one it reads from
        // /roller-version.properties, so derive the expectation from the
        // same source and parser instead of pinning a release constant
        Properties props = new Properties();
        props.load(getClass().getResourceAsStream("/roller-version.properties"));
        int expectedVersion = DatabaseInstaller.parseVersionString(props.getProperty("ro.version", "UNKNOWN"));

        installer.upgradeDatabase(true);
        verify(update).setString(1, String.valueOf(expectedVersion));
        verify(update).executeUpdate();
        verifyNoInteractions(scripts);
        assertTrue(installer.getMessages().stream().anyMatch(m -> m.contains("No table changes were required.")));
        assertTrue(installer.getMessages().stream().anyMatch(m -> m.contains("Database version updated to " + expectedVersion + ".")));
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

        // a 520 database upgrades through the 520->610 schema step; run with
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
