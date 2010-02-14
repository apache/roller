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

package org.apache.roller.weblogger.jetty;

import java.io.PrintWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.drda.NetworkServerControl;
import org.mortbay.component.LifeCycle;


/**
 * Enables start and stop Derby from the Maven Jetty plugin.
 */
public class DerbyLifeCycle implements LifeCycle {
    private static Log log =
            LogFactory.getFactory().getInstance(DerbyLifeCycle.class);
    private boolean started = false;
    private boolean starting = false;
    private boolean stopping= false;

    private boolean failed = false;
    private String database = null;
    private String port = null;

    public void start() throws Exception {
        log.info("**************");
        log.info("Starting Derby");
        log.info("**************");
        try {
            System.setProperty("derby.system.home", database);
            System.setProperty("derby.drda.portNumber", port);
            System.setProperty("derby.drda.host", "localhost");
            NetworkServerControl server = new NetworkServerControl();
            server.start(new PrintWriter(System.out));
            try {Thread.sleep(2000);} catch (Exception ignored) {}
            started = true;
            starting = false;
            stopping = false;
            failed = false;
        } catch (Exception e) {
            failed = true;
            starting = false;
            throw new Exception("Unable to start Derby. EXCEPTION: "
                    + e.getClass().getName() + " Message: " + e.getMessage());
        }
    }

    public void stop() throws Exception {
        log.info("**************");
        log.info("Stopping Derby");
        log.info("**************");
        stopping = true;
        try {
            System.out.println("Stopping Derby");
            System.setProperty("derby.drda.portNumber", port);
            System.setProperty("derby.drda.host", "localhost");
            NetworkServerControl server = new NetworkServerControl();
            server.shutdown();
            try {Thread.sleep(2000);} catch (Exception ignored) {}
            started = false;
            starting = false;
            stopping = false;
            failed = false;
        } catch (Exception e) {
            failed = true;
            stopping = false;
            throw new Exception("Unable to start Derby. EXCEPTION: "
                    + e.getClass().getName() + " Message: " + e.getMessage());
        }
    }

    /**
     * @return Returns the database.
     */
    public String getDatabase() {
        return database;
    }

    /**
     * @param database The database to set.
     */
    public void setDatabase(String database) {
        this.database = database;
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

    public boolean isRunning() {
        return started;
    }

    public boolean isStarted() {
        return started;
    }

    public boolean isStarting() {
        return starting;
    }

    public boolean isStopping() {
        return stopping;
    }

    public boolean isStopped() {
        return !started;
    }

    public boolean isFailed() {
        return failed;
    }

}
