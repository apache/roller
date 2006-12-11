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
package org.apache.roller.ant;

import java.io.PrintWriter;
import org.apache.derby.drda.NetworkServerControl;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant Task to start Derby
 * @author Dave Johnson
 */
public class StartDerbyTask extends Task {
    private String database = null;
    private String port = null;
    
    public void execute() throws BuildException {
        try {
            System.out.println("Stopping Derby");
            System.setProperty("derby.system.home", database);
            System.setProperty("derby.drda.portNumber", port);
            System.setProperty("derby.drda.host", "localhost");
            NetworkServerControl server = new NetworkServerControl();
            server.start(new PrintWriter(System.out));
            try {Thread.sleep(2000);} catch (Exception ignored) {}
        } catch (Exception e) {
            throw new BuildException("Unable to load Derby driver");
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
}
