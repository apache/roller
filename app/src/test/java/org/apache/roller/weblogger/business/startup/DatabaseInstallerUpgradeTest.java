/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0.
 */
package org.apache.roller.weblogger.business.startup;

import java.sql.*;
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
        installer.upgradeDatabase(true);
        verify(update).setString(1, "616");
        verify(update).executeUpdate();
        verifyNoInteractions(scripts);
        assertTrue(installer.getMessages().stream().anyMatch(m -> m.contains("No table changes were required.")));
        assertTrue(installer.getMessages().stream().anyMatch(m -> m.contains("Database version updated to 616.")));
    }
}
