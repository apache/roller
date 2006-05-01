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
