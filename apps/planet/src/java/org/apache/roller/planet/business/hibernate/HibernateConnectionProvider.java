package org.apache.roller.planet.business.hibernate;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.startup.PlanetStartup;
import org.hibernate.HibernateException;
import org.hibernate.connection.ConnectionProvider;

/**
 * Allows user to provide Hibernate with database connections via Roller's
 * DatabaseProvider class. By default HibernatePersistenceStrategy adds this 
 * class to Hibernate's configuration. If you'd like to provide your own
 * ConnctionProvider implementation you can do so by overriding Roller's 
 * 'hibernate.connectionProvider' property with the classname of your impl.
 */
public class HibernateConnectionProvider implements ConnectionProvider {
    private static Log log = LogFactory.getLog(HibernateConnectionProvider.class);
    
    /** No-op: we get our configuration from Roller's DatabaseProvider */
    public void configure(Properties properties) throws HibernateException {
    }

    /** Get connecetion from Roller's Database provider */
    public Connection getConnection() throws SQLException {
        return PlanetStartup.getDatabaseProvider().getConnection();
    }

    /** Close connection by calling connection.close() */
    public void closeConnection(Connection connection) throws SQLException {
        connection.close();
    }

    /** No-op: no need to close Roller's DatabaseProvider */
    public void close() throws HibernateException {
        // no-op
    }

    /** Returns false, we don't support aggressive release */
    public boolean supportsAggressiveRelease() {
        return false;
    }
    
}
