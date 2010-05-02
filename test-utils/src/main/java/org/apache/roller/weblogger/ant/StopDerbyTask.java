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
package org.apache.roller.weblogger.ant;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.roller.util.SQLScriptRunner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant Task to stop Derby
 * @author Dave Johnson
 */
public class StopDerbyTask extends Task {
    private String databaseDir = null;
    private String databaseScriptsDir = null;
    private String port = null;
    private boolean skip = false;

    public void execute() throws BuildException {
        try {
            if (!isSkip()) {

                Class.forName("org.apache.derby.jdbc.ClientDriver");
                
                String driverURL =
                    "jdbc:derby://localhost:" + port + "/rollerdb";
                Connection conn =
                    DriverManager.getConnection(driverURL,"APP", "APP");

                //Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
                //Connection conn = DriverManager.getConnection(
                    //"jdbc:derby:rollerdb;create=true","APP", "APP");

                // drop Roller tables
                SQLScriptRunner runner = new SQLScriptRunner(
                        databaseScriptsDir
                        + File.separator + "droptables.sql");
                runner.runScript(conn, false);

                System.out.println("==============");
                System.out.println("Stopping Derby");
                System.out.println("==============");
                
                try {
                    DriverManager.getConnection(driverURL + ";shutdown=true");
                } catch (Exception ignored) {}

                System.setProperty("derby.system.home", databaseDir);

                // Network Derby
                System.setProperty("derby.drda.portNumber", port);
                System.setProperty("derby.drda.host", "localhost");
                System.setProperty("derby.drda.maxThreads","10");
                //System.setProperty("derby.drda.logConnections","true");
                NetworkServerControl server = new NetworkServerControl();
                server.shutdown();

                //try {
                //    while (true) {
                //       server.ping();
                //    }
                //} catch (Exception expected) {}

                // Embedded Derby
                //DriverManager.getConnection("jdbc:derby:;shutdown=true");

                //try {Thread.sleep(2000);} catch (Exception ignored) {}

            } else {
                System.out.println("Skipping Derby shutdown");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new BuildException(e.getMessage());
        }
    }
    /**
     * @return Returns the port.
     */
    public String getPort() {
        return port;
    }
    /**
     * @param port The port to set.
     */
    public void setPort(String port) {
        this.port = port;
    }

    /**
     * @return the skip
     */
    public boolean isSkip() {
        return skip;
    }

    /**
     * @param skip the skip to set
     */
    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    /**
     * @return the databaseDir
     */
    public String getDatabaseDir() {
        return databaseDir;
    }

    /**
     * @param databaseDir the databaseDir to set
     */
    public void setDatabaseDir(String databaseDir) {
        this.databaseDir = databaseDir;
    }

    /**
     * @return the databaseScriptsDir
     */
    public String getDatabaseScriptsDir() {
        return databaseScriptsDir;
    }

    /**
     * @param databaseScriptsDir the databaseScriptsDir to set
     */
    public void setDatabaseScriptsDir(String databaseScriptsDir) {
        this.databaseScriptsDir = databaseScriptsDir;
    }
}
