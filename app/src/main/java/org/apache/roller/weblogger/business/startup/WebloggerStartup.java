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

package org.apache.roller.weblogger.business.startup;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.business.DatabaseProvider;
import org.apache.roller.weblogger.business.MailProvider;
import org.apache.roller.weblogger.config.WebloggerConfig;


/**
 * Manages Roller Weblogger startup process.
 */
public final class WebloggerStartup {
    
    private static final Log log = LogFactory.getLog(WebloggerStartup.class);
    
    private static boolean prepared = false;
    
    private static DatabaseProvider dbProvider = null;
    private static StartupException dbProviderException = null;
    
    private static MailProvider mailProvider = null;
    
    
    // non-instantiable
    private WebloggerStartup() {}
    
    
    /**
     * Is the Roller Weblogger app properly prepared to be bootstrapped?
     */
    public static boolean isPrepared() {
        return prepared;
    }
    
    
    /**
     * Get a reference to the currently configured DatabaseProvider.
     *
     * @return DatabaseProvider The configured database provider.
     * @throws IllegalStateException If the app has not been properly prepared yet.
     */
    public static DatabaseProvider getDatabaseProvider() {
        if (dbProvider == null) {
            throw new IllegalStateException("Roller Weblogger has not been prepared yet");
        }
        return dbProvider;
    }
    
    
    /**
     * Get a reference to the exception thrown while instantiating the 
     * database provider, if any.
     *
     * @return StartupException Exception from db provider, or null if no exception thrown.
     */
    public static StartupException getDatabaseProviderException() {
        return dbProviderException;
    }
    
    
    /**
     * Get a reference to the currently configured MailProvider, if available.
     *
     * @return MailProvider The configured mail provider, or null if none configured.
     */
    public static MailProvider getMailProvider() {
        return mailProvider;
    }
    
    
    /**
     * Does the app need to create the database tables?
     */
    public static boolean isDatabaseCreationRequired() {
        return getDatabaseInstaller().isCreationRequired();
    }
    
    
    /**
     * Run database creation scripts.
     */
    public static List<String> createDatabase() throws StartupException {
        
        DatabaseInstaller installer = getDatabaseInstaller();
        try {
            installer.createDatabase();
            
            // any time we've successfully installed a db we are prepared
            prepared = true;
            
        } catch (StartupException se) {
            throw new StartupException(se.getMessage(), se.getRootCause(), installer.getMessages());
        }
        
        return installer.getMessages();
    }
    
    
    /**
     * Does the app require any upgrading?
     */
    public static boolean isDatabaseUpgradeRequired() {
        return getDatabaseInstaller().isUpgradeRequired();
    }
    
    
    /**
     * Run database upgrade work, optionally including upgrade scripts.
     */
    public static List<String> upgradeDatabase(boolean runScripts) 
            throws StartupException {
        
        DatabaseInstaller installer = getDatabaseInstaller();
        try {
            installer.upgradeDatabase(true);
            
            // any time we've successfully upgraded a db we are prepared
            prepared = true;
            
        } catch (StartupException se) {
            throw new StartupException(se.getMessage(), se.getRootCause(), installer.getMessages());
        }
        
        return installer.getMessages();
    }
    
    
    /**
     * Get a database installer.
     *
     * @return DatabaseInstaller A database installer.
     * @throws IllegalStateException If the database provider has not been properly setup yet.
     */
    private static DatabaseInstaller getDatabaseInstaller() {
        return new DatabaseInstaller(getDatabaseProvider(), new ClasspathDatabaseScriptProvider());
    }
    
    
    /**
     * Run the Roller Weblogger preparation sequence.
     *
     * This sequence is what prepares the core services of the application such
     * as setting up the database and mail providers.
     */
    public static void prepare() throws StartupException {
        
        // setup database provider
        try {
            dbProvider = new DatabaseProvider();
        } catch(StartupException ex) {
            dbProviderException = ex;
            throw ex;
        }
        
        // setup mail provider, if configured
        try {
            mailProvider = new MailProvider();
        } catch(StartupException ex) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to setup mail provider, continuing anyways.\n"
                    + "Reason: " + ex.getMessage(), ex);
            } else {
                log.warn("Failed to setup mail provider, continuing anyways.\n"
                    + "Reason: " + ex.getMessage());
            }
        }
        
        // now we need to deal with database install/upgrade logic
        if("manual".equals(WebloggerConfig.getProperty("installation.type"))) {
            
            // if we are doing manual install then all that is needed is the
            // app handled database upgrade work, not the db scripts
            DatabaseInstaller dbInstaller = getDatabaseInstaller();
            if(dbInstaller.isUpgradeRequired()) {
                dbInstaller.upgradeDatabase(false);
            }
            
            prepared = true;
            
        } else {
            
            // we are in auto install mode, so see if there is any work to do
            DatabaseInstaller dbInstaller = getDatabaseInstaller();
            if(!dbInstaller.isCreationRequired() && 
                    !dbInstaller.isUpgradeRequired()) {
                prepared = true;
            }
        }
        
    }
    
}
