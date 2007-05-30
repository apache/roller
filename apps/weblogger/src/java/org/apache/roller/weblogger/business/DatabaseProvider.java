package org.apache.roller.weblogger.business;

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
import org.apache.roller.weblogger.config.RollerConfig;

/**
 * Encapsulates Roller database configuration via JDBC properties or JNDI.
 *
 * <p>Reads configuration properties from RollerConfig:</p>
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
    public enum ConfigurationType {JNDI_NAME, JDBC_PROPERTIES;}
    
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
                RollerConfig.getProperty("database.configurationType"); 
        if ("jdbc".equals(connectionTypeString)) {
            type = ConfigurationType.JDBC_PROPERTIES;
        }
        jndiName =          RollerConfig.getProperty("database.jndi.name");
        jdbcDriverClass =   RollerConfig.getProperty("database.jdbc.driverClass");
        jdbcConnectionURL = RollerConfig.getProperty("database.jdbc.connectionURL");
        jdbcUsername =      RollerConfig.getProperty("database.jdbc.username");
        jdbcPassword =      RollerConfig.getProperty("database.jdbc.password");
        
        // init now so we fail early
        if (getType() == ConfigurationType.JDBC_PROPERTIES) {
            log.info("Using 'jdbc' properties based configuration");
            try {
                Class.forName(getJdbcDriverClass());
            } catch (ClassNotFoundException ex) {
                throw new RollerException(
                   "Cannot load specified JDBC driver class [" +getJdbcDriverClass()+ "]", ex);
            }
            if (getJdbcUsername() != null || getJdbcPassword() != null) {
                props = new Properties();
                if (getJdbcUsername() != null) props.put("user", getJdbcUsername());
                if (getJdbcPassword() != null) props.put("password", getJdbcPassword());
            }
        } else {
            log.info("Using 'jndi' based configuration");
            String name = "java:comp/env/" + getJndiName();
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
        if (getType() == ConfigurationType.JDBC_PROPERTIES) {
            return DriverManager.getConnection(getJdbcConnectionURL(), props);
        } else {
            return dataSource.getConnection();
        }
    } 

    public ConfigurationType getType() {
        return type;
    }

    public String getJndiName() {
        return jndiName;
    }

    public String getJdbcDriverClass() {
        return jdbcDriverClass;
    }

    public String getJdbcConnectionURL() {
        return jdbcConnectionURL;
    }

    public String getJdbcPassword() {
        return jdbcPassword;
    }

    public String getJdbcUsername() {
        return jdbcUsername;
    }
}
