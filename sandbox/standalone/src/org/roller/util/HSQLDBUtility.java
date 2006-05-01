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
package org.roller.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.hsqldb.Server;

/** 
 * Created for standalone Roller/Tomcat/HSQLDB demo 
 */
public class HSQLDBUtility {
	
	public static void start() {	
		final String database = System.getProperties().getProperty("hsqldb_database");
		final String port = System.getProperties().getProperty("hsqldb_port");
		if (database != null) {
			Thread server = new Thread() {
				public void run() {
					System.out.println("Starting HSQLDB");
					String[] args = { 
					    "-database", database,
						"-port", port,
						"-no_system_exit", "true" };
					Server.main(args);
				}
			};
			server.start();
		}
	}
		
    public static void stop() {
		try {
			System.out.println("Stopping HSQLDB");
			final String port = System.getProperties().getProperty("hsqldb_port");
			final Connection con = DriverManager.getConnection("jdbc:hsqldb://localhost:"+port);
			con.createStatement().execute("SHUTDOWN");
		} catch (SQLException e) {
			System.out.println("ERROR shutting down HSQLDB");
		}
	}
}
