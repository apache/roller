package org.roller.ant;

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
