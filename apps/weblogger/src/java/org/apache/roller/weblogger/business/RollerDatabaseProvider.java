package org.apache.roller.weblogger.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.RollerException;
import org.apache.roller.util.DatabaseProvider;
import org.apache.roller.util.DatabaseProvider.ConfigurationType;
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
        init(type, 
            RollerConfig.getProperty("database.jndi.name"),
            RollerConfig.getProperty("database.jdbc.driverClass"),
            RollerConfig.getProperty("database.jdbc.connectionURL"),
            RollerConfig.getProperty("database.jdbc.username"),
            RollerConfig.getProperty("database.jdbc.password"));
    }
}
