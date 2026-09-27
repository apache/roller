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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.roller.weblogger.business.DatabaseProvider;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DatabaseInstallerCreationTest {

    private static final String CATALOG = "rollerdb";

    private DatabaseMetaData metaData;

    /**
     * An installer whose catalog contains exactly the named tables. Each
     * getTables() call yields a fresh cursor, as a real driver would.
     */
    private DatabaseInstaller installerSeeing(String... tableNames) throws Exception {
        DatabaseProvider db = mock(DatabaseProvider.class);
        Connection con = mock(Connection.class);
        metaData = mock(DatabaseMetaData.class);

        when(db.getConnection()).thenReturn(con);
        when(con.getCatalog()).thenReturn(CATALOG);
        when(con.getMetaData()).thenReturn(metaData);
        when(metaData.getTables(any(), any(), any(), any()))
                .thenAnswer(invocation -> cursorOver(tableNames));

        return new DatabaseInstaller(db, mock(DatabaseScriptProvider.class));
    }

    private static ResultSet cursorOver(String[] tableNames) throws Exception {
        ResultSet rs = mock(ResultSet.class);
        Iterator<String> remaining = Arrays.asList(tableNames).iterator();
        AtomicReference<String> current = new AtomicReference<>();
        when(rs.next()).thenAnswer(invocation -> {
            if (!remaining.hasNext()) {
                return false;
            }
            current.set(remaining.next());
            return true;
        });
        when(rs.getString("TABLE_NAME")).thenAnswer(invocation -> current.get());
        return rs;
    }

    @Test
    void tableLookupIsScopedToTheConnectionCatalog() throws Exception {
        DatabaseInstaller installer = installerSeeing();

        assertTrue(installer.isCreationRequired());

        // A null catalog means "every catalog on the server" to some drivers,
        // which would find another Roller database's tables and wrongly skip
        // creation of this one.
        verify(metaData, atLeastOnce()).getTables(eq(CATALOG), isNull(), eq("%"), isNull());
        verify(metaData, never()).getTables(isNull(), any(), any(), any());
    }

    @Test
    void unrelatedTablesStillNeedCreation() throws Exception {
        DatabaseInstaller installer = installerSeeing("some_unrelated_table");

        assertTrue(installer.isCreationRequired());
    }

    @Test
    void partialSchemaStillNeedsCreation() throws Exception {
        DatabaseInstaller installer = installerSeeing("userrole");

        assertTrue(installer.isCreationRequired());
    }

    @Test
    void populatedCatalogDoesNotNeedCreation() throws Exception {
        DatabaseInstaller installer = installerSeeing("userrole", "roller_user");

        assertFalse(installer.isCreationRequired());
    }

    @Test
    void preRoller51TableNamesAreRecognised() throws Exception {
        DatabaseInstaller installer = installerSeeing("userrole", "rolleruser");

        assertFalse(installer.isCreationRequired());
    }
}
