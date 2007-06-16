package org.apache.roller.weblogger.business;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.config.RollerConfig;

/**
 * Encapsulates Roller database configuration via JDBC properties or JNDI.
 *
 * <p>To keep the logs from filling up with DB connection errors, will only 
 * attempt to connect once.</p>
 * 
 * <p>Keeps startup exception and log so we can present useful debugging
 * information to whoever is installing Roller.</p>
 *
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
    private ConfigurationType type = ConfigurationType.JNDI_NAME; 
    
    private static DatabaseProvider singletonInstance = null;
    private static WebloggerException startupException = null;
    private static List<String> startupLog = new ArrayList<String>();
    
    private DataSource dataSource = null;    
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
    private DatabaseProvider() throws WebloggerException {
        
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
        
        successMessage("SUCCESS: Got parameters. Using configuration type " + type);

        // If we're doing JDBC then attempt to load JDBC driver class
        if (getType() == ConfigurationType.JDBC_PROPERTIES) {
            successMessage("-- Using JDBC driver class: "   + jdbcDriverClass);
            successMessage("-- Using JDBC connection URL: " + jdbcConnectionURL);
            successMessage("-- Using JDBC username: "       + jdbcUsername);
            successMessage("-- Using JDBC password: [hidden]");
            try {
                Class.forName(getJdbcDriverClass());
            } catch (ClassNotFoundException ex) {
                String errorMsg = 
                     "ERROR: cannot load JDBC driver class [" + getJdbcDriverClass()+ "]. "
                    +"Likely problem: JDBC driver jar missing from server classpath.";
                errorMessage(errorMsg);
                startupException = new WebloggerException(errorMsg, ex);
                throw startupException;
            }
            successMessage("SUCCESS: loaded JDBC driver class [" +getJdbcDriverClass()+ "]");
            
            if (getJdbcUsername() != null || getJdbcPassword() != null) {
                props = new Properties();
                if (getJdbcUsername() != null) props.put("user", getJdbcUsername());
                if (getJdbcPassword() != null) props.put("password", getJdbcPassword());
            }
            
        // Else attempt to locate JNDI datasource
        } else { 
            String name = "java:comp/env/" + getJndiName();
            successMessage("-- Using JNDI datasource name: " + name);
            try {
                InitialContext ic = new InitialContext();
                dataSource = (DataSource)ic.lookup(name);
            } catch (NamingException ex) {
                String errorMsg = 
                    "ERROR: cannot locate JNDI DataSource [" +name+ "]. "
                   +"Likely problem: no DataSource or datasource is misconfigured.";
                errorMessage(errorMsg);
                startupException =  new WebloggerException(errorMsg, ex);
                throw startupException;
            }            
            successMessage("SUCCESS: located JNDI DataSource [" +name+ "]");
        }
        
        // So far so good. Now, can we get a connection?
        try { 
            Connection testcon = getConnection();
            testcon.close();
        } catch (Throwable t) {
            String errorMsg = 
                "ERROR: unable to obtain database connection. "
               +"Likely problem: bad connection parameters or database unavailable.";
            errorMessage(errorMsg);
            startupException =  new WebloggerException(errorMsg, t);
            throw startupException;
        }
    }
    
    private void successMessage(String msg) {
        startupLog.add(msg);
        log.info(msg);
    }
    
    private void errorMessage(String msg) {
        startupLog.add(msg);
        log.error(msg);
    }
    
    /**
     * Get global database provider singlton, instantiating if necessary.
     */
    public static DatabaseProvider getDatabaseProvider() throws WebloggerException {
        // No need to jam log with database connection attempts
        if (startupException != null) {
            throw startupException;
        }
        if (singletonInstance == null) {
            singletonInstance = new DatabaseProvider();
        }
        return singletonInstance;
    }
    
    /**
     * Exception that occured during startup, or null if none occured.
     */
    public static WebloggerException getStartupException() {
        return startupException;
    }

    /** 
     * List of success and error messages when class was first instantiated.
     **/
    public static List<String> getStartupLog() {
        return startupLog;
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
