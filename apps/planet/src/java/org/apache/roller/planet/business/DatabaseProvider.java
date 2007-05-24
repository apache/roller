package org.apache.roller.planet.business;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.planet.config.PlanetConfig;

/**
 * Encapsulates Roller database configuration via JDBC properties or JNDI.
 *
 * <p>Reads configuration properties from PlanetConfig:</p>
 * <pre>
 * # Specify database configuration type of 'jndi' or 'jdbc'
 * database.configurationType=jndi
 * 
 * # For database configuration type 'jndi',this will be used
 * database.jndi.name=jdbc/rollerdb
 * 
 * # For database configuration type of 'jdbc', you MUST override these
 * database.jdbc.driverClass=
 * database.jdbc.connectionURL=
 * database.jdbc.username=
 * database.jdbc.password=
 * </pre>
 */
public class DatabaseProvider  {
    private static Log log = LogFactory.getLog(DatabaseProvider.class);
    private enum ConfigurationType {JNDI_NAME, JDBC_PROPERTIES;}
    
    private static DatabaseProvider singletonInstance = null;
    
    private DataSource dataSource = null;
    
    private ConfigurationType type = ConfigurationType.JNDI_NAME; 
    
    private String jndiName = null; 
    
    private String jdbcDriverClass = null;
    private String jdbcConnectionURL = null;
    private String jdbcPassword = null;
    private String jdbcUsername = null;
    private Properties props = null;
   
    /**
     * Reads configuraiton, loads driver or locates data-source and attempts
     * to get test connecton so that we can fail early.
     */ 
    private DatabaseProvider() throws RollerException {
        String connectionTypeString = 
                PlanetConfig.getProperty("database.configurationType"); 
        if ("jdbc".equals(connectionTypeString)) {
            type = ConfigurationType.JDBC_PROPERTIES;
        }
        jndiName =          PlanetConfig.getProperty("database.jndi.name");
        jdbcDriverClass =   PlanetConfig.getProperty("database.jdbc.driverClass");
        jdbcConnectionURL = PlanetConfig.getProperty("database.jdbc.connectionURL");
        jdbcUsername =      PlanetConfig.getProperty("database.jdbc.username");
        jdbcPassword =      PlanetConfig.getProperty("database.jdbc.password");
        
        // init now so we fail early
        if (type == ConfigurationType.JDBC_PROPERTIES) {
            log.info("Using 'jdbc' properties based configuration");
            try {
                Class.forName(jdbcDriverClass);
            } catch (ClassNotFoundException ex) {
                throw new RollerException(
                   "Cannot load specified JDBC driver class [" +jdbcDriverClass+ "]", ex);
            }
            if (jdbcUsername != null || jdbcPassword != null) {
                props = new Properties();
                if (jdbcUsername != null) props.put("user", jdbcUsername);
                if (jdbcPassword != null) props.put("password", jdbcPassword);
            }
        } else {
            log.info("Using 'jndi' based configuration");
            String name = "java:comp/env/" + jndiName;
            try {
                InitialContext ic = new InitialContext();
                dataSource = (DataSource)ic.lookup(name);
            } catch (NamingException ex) {
                throw new RollerException(
                    "ERROR looking up data-source with JNDI name: " + name, ex);
            }            
        }
        try { 
            Connection testcon = getConnection();
            testcon.close();
        } catch (Throwable t) {
            throw new RollerException("ERROR unable to obtain connection", t);
        }
    }
    
    /**
     * Get global database provider singlton, instantiating if necessary.
     */
    public static DatabaseProvider getDatabaseProvider() throws RollerException {
        if (singletonInstance == null) {
            singletonInstance = new DatabaseProvider();
        }
        return singletonInstance;
    }
    
    /**
     * Get database connection from data-source or driver manager, depending 
     * on which is configured.
     */
    public Connection getConnection() throws SQLException {
        if (type == ConfigurationType.JDBC_PROPERTIES) {
            return DriverManager.getConnection(jdbcConnectionURL, props);
        } else {
            return dataSource.getConnection();
        }
    } 
}
