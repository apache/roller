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
