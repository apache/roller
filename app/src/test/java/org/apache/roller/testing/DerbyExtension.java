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

package org.apache.roller.testing;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.roller.weblogger.business.startup.SQLScriptRunner;
import org.junit.jupiter.api.extension.*;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;


public class DerbyExtension implements BeforeAllCallback, AfterAllCallback {

    private static Log log = LogFactory.getLog(DerbyExtension.class);

    private static DerbyStartStopper derbyStartStopper =
        new DerbyStartStopper("target/derby-system", "target/dbscripts", "4224");

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        derbyStartStopper.start();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        try {
            derbyStartStopper.stop();
        } catch (Exception e) {
            log.warn("Error stopping Derby", e);
        }
    }
}


class DerbyStartStopper {
    private String databaseDir;
    private String databaseScriptsDir;
    private String port;


    DerbyStartStopper( String databaseDir, String databaseScriptsDir, String port ) {
        this.databaseDir = databaseDir;
        this.databaseScriptsDir = databaseScriptsDir;
        this.port = port;
    }

    public void start() throws Exception {

        System.out.println("==============");
        System.out.println("Starting Derby");
        System.out.println("==============");

        System.setProperty("derby.system.home", databaseDir);
        System.setProperty("derby.drda.portNumber", port);
        System.setProperty("derby.drda.host", "localhost");
        System.setProperty("derby.drda.maxThreads", "10");
        //System.setProperty("derby.drda.logConnections","true");

        NetworkServerControl server = new NetworkServerControl();
        server.start(new PrintWriter(System.out));
        try {
            Thread.sleep(2000);
        } catch (Exception ignored) {
        }
        System.out.println("Runtime Info: " + server.getRuntimeInfo());
        System.out.println("System Info:  " + server.getSysinfo());

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection("jdbc:derby:rollerdb;create=true", "APP", "APP");

        // create roller tables

        SQLScriptRunner runner1 = new SQLScriptRunner(
            databaseScriptsDir
                + File.separator + "droptables.sql");
        runner1.runScript(conn, false);

        SQLScriptRunner runner = new SQLScriptRunner(
            databaseScriptsDir
                + File.separator + "derby"
                + File.separator + "createdb.sql");
        try {
            runner.runScript(conn, true);
        } catch (Exception ignored) {
            for (String message : runner.getMessages()) {
                System.out.println(message);
            }
            ignored.printStackTrace();
        }
    }

    public void stop() throws Exception {

        String driverURL = "jdbc:derby://localhost:" + port + "/rollerdb";

        Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
        Connection conn = DriverManager.getConnection(driverURL,"APP", "APP");

        // drop Roller tables
        SQLScriptRunner runner = new SQLScriptRunner(
            databaseScriptsDir + File.separator + "droptables.sql");
        runner.runScript(conn, false);

        System.out.println("==============");
        System.out.println("Stopping Derby");
        System.out.println("==============");

        try {
            DriverManager.getConnection(driverURL + ";shutdown=true");
        } catch (Exception ignored) {
        }

        System.setProperty("derby.system.home", databaseDir);

        // Network Derby
        System.setProperty("derby.drda.portNumber", port);
        System.setProperty("derby.drda.host", "localhost");
        System.setProperty("derby.drda.maxThreads", "10");
        //System.setProperty("derby.drda.logConnections","true");
        NetworkServerControl server = new NetworkServerControl();
        server.shutdown();

        try {
            while (true) {
               server.ping();
            }
        } catch (Exception expected) {}

        DriverManager.getConnection("jdbc:derby:;shutdown=true");

        try {Thread.sleep(2000);} catch (Exception ignored) {}
    }
}

