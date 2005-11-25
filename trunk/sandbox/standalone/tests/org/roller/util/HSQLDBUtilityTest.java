
package org.roller.util;

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
