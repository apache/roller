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

package org.apache.roller.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import junit.framework.TestCase;

import org.apache.catalina.Lifecycle;
import org.apache.catalina.LifecycleEvent;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.LifecycleListener;

public class HSQLDBUtilityTest extends TestCase {
	
	public void testStartup() throws Exception {
		
		final String port = "8808";
		System.setProperty("hsqldb_database", "/tmp/testdb");
		System.setProperty("hsqldb_port", "8808");
		HSQLDBUtility.start();
		
		Thread.sleep(2000); // give HSQLDB time to startup
		
		Connection con = getConnection(port);
		assertNotNull(con);
		
		HSQLDBUtility.stop();

		// TODO: test for shutdown
		//Thread.sleep(2000); // give HSQLDB time to shutdown
		//Connection nocon = getConnection(port);
		//assertNull(nocon);
    }

	/**
	 * Return local HSQLDB connection or null on error.
	 */
	private Connection getConnection(final String port) {
		Connection con = null;
		try {
			Class.forName("org.hsqldb.jdbcDriver");
			con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:"+port);
		} catch (ClassNotFoundException e) {
			System.out.println(e);
		} catch (SQLException e) {
			System.out.println(e);
		}
		return con;
	}
}
