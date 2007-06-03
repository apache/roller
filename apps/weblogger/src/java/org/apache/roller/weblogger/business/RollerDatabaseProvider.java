package org.apache.roller.weblogger.business;

import java.sql.Connection;
import java.util.Properties;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.weblogger.business.DatabaseProvider.ConfigurationType;
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
@com.google.inject.Singleton
public class RollerDatabaseProvider extends DatabaseProvider {
    private static Log log = LogFactory.getLog(RollerDatabaseProvider.class);
   
    /**
     * Contruct DatabseProvider using RollerConfig properties. 
     */ 
    private RollerDatabaseProvider() throws RollerException {
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
        
        init(type, jndiName, jdbcDriverClass, jdbcConnectionURL, jdbcUsername, jdbcPassword);
    }
}
