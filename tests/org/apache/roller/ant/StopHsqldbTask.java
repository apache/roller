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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * Ant Task to stop HSQLDB
 * @author Dave Johnson
 */
public class StopHsqldbTask extends Task
{
    private String port = null;
    public void execute() throws BuildException
    {
        try 
        {
            if (port==null)
            {
                throw new BuildException("missing port attribute");
            }
            System.out.println("Stopping HSQLDB at port " + port);
            final Connection con = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost:"+port);
            con.createStatement().execute("SHUTDOWN");
        } 
        catch (SQLException e) 
        {
            throw new BuildException(e.getMessage());
        }
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
