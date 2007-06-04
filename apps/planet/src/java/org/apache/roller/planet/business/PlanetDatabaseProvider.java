package org.apache.roller.planet.business;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.planet.business.DatabaseProvider.ConfigurationType;
import org.apache.roller.planet.config.PlanetConfig;

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
public class PlanetDatabaseProvider extends DatabaseProvider {
    private static Log log = LogFactory.getLog(PlanetDatabaseProvider.class);
   
    /**
     * Contruct DatabseProvider using RollerConfig properties. 
     */ 
    private PlanetDatabaseProvider() throws DatabaseProviderException {
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
        
        init(type, jndiName, jdbcDriverClass, jdbcConnectionURL, jdbcUsername, jdbcPassword);
    }
}
