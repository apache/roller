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

package org.apache.roller.util;

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
public class DatabaseProvider  {
    private static Log log = LogFactory.getLog(DatabaseProvider.class);

    public enum ConfigurationType {JNDI_NAME, JDBC_PROPERTIES;}
    protected ConfigurationType type = ConfigurationType.JNDI_NAME; 
    
    private static DatabaseProvider singletonInstance = null;
    private static DatabaseProviderException startupException = null;
    private static List<String> startupLog = new ArrayList<String>();
    
    protected DataSource dataSource = null;    
    protected String jndiName = null; 
    
    protected String jdbcDriverClass = null;
    protected String jdbcConnectionURL = null;
    protected String jdbcPassword = null;
    protected String jdbcUsername = null;
    protected Properties props = null;
    
    
    /**
     * Reads configuraiton, loads driver or locates data-source and attempts
     * to get test connecton so that we can fail early.
     */ 
    protected void init(
        ConfigurationType type,
        String jndiName, 
        String jdbcDriverClass,
        String jdbcConnectionURL,
        String jdbcUsername,
        String jdbcPassword) throws DatabaseProviderException { 
        
        this.type              = type;
        this.jndiName          = jndiName;
        this.jdbcDriverClass   = jdbcDriverClass;
        this.jdbcConnectionURL = jdbcConnectionURL;
        this.jdbcUsername      = jdbcUsername;
        this.jdbcPassword      = jdbcPassword;
         
        if ("jdbc".equals(type)) {
            type = ConfigurationType.JDBC_PROPERTIES;
        }
        
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
                startupException = new DatabaseProviderException(errorMsg, ex);
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
                startupException =  new DatabaseProviderException(errorMsg, ex);
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
            startupException =  new DatabaseProviderException(errorMsg, t);
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
    public static DatabaseProvider getDatabaseProvider() throws DatabaseProviderException {
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
    public static DatabaseProviderException getStartupException() {
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