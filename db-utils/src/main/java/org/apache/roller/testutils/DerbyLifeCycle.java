/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.roller.testutils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.derby.drda.NetworkServerControl;
import org.eclipse.jetty.util.component.LifeCycle;
import java.io.PrintWriter;

/**
 * Enables start and stop Derby from the Maven Jetty plugin.
 */
public class DerbyLifeCycle implements LifeCycle.Listener {
    private static final Log log = LogFactory.getFactory().getInstance(DerbyLifeCycle.class);

    private void setupDerby() {
        System.setProperty("derby.system.home", "./target/derby");
        System.setProperty("derby.drda.portNumber", "4224");
        System.setProperty("derby.drda.host", "localhost");
    }

    @Override
    public void lifeCycleStarting(LifeCycle event) {
        log.info("**************");
        log.info("Starting Derby");
        log.info("**************");
        try {
            setupDerby();
            NetworkServerControl server = new NetworkServerControl();
            server.start(new PrintWriter(System.out));
            try {Thread.sleep(2000);} catch (Exception ignored) {}
        } catch (Exception e) {
            log.error("Error starting Derby", e);
        }
    }

    @Override
    public void lifeCycleStopped(LifeCycle event) {
        log.info("**************");
        log.info("Stopping Derby");
        log.info("**************");
        try {
            setupDerby();
            NetworkServerControl server = new NetworkServerControl();
            server.shutdown();
            try {Thread.sleep(2000);} catch (Exception ignored) {}
        } catch (Exception e) {
            log.error("Error stopping Derby", e);
        }
    }
}