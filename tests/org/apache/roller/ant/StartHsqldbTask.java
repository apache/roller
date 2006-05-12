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

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.hsqldb.Server;

/**
 * Ant Task to start HSQLDB
 * @author Dave Johnson
 */
public class StartHsqldbTask extends Task
{
    private String database = null;
    private String port = null;
    public void execute() throws BuildException
    {
        if (database != null) {
            Thread server = new Thread() {
                public void run() {
                    System.out.println("Starting HSQLDB");
                    String[] args = { 
                        "-database", database,
                        "-port", port,
                        "-no_system_exit", "true" };
                    Server.main(args);
                }
            };
            server.start();
        }
        try {Thread.sleep(2000);} catch (Exception ignored) {}
    }
    /**
     * @return Returns the database.
     */
    public String getDatabase()
    {
        return database;
    }
    /**
     * @param database The database to set.
     */
    public void setDatabase(String database)
    {
        this.database = database;
    }
    /**
     * @return Returns the port.
     */
    public String getPort()
    {
        return port;
    }
    /**
     * @param port The port to set.
     */
    public void setPort(String port)
    {
        this.port = port;
    }
}
