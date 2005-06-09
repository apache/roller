package org.roller.presentation.planet;

import java.util.TimerTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ScheduledTask;

/**
 * Run the Planet Roller refresh-entries method to fetch and parse newsfeeds.
 * @author Dave Johnson
 */
public class RefreshEntriesTask extends TimerTask implements ScheduledTask
{
    private static Log logger = 
        LogFactory.getFactory().getInstance(RefreshEntriesTask.class);
    private Roller roller = null;
    
    /** Task may be run from the command line */
    public static void main(String[] args) throws Exception
    {
        RollerFactory.setRoller(
            "org.roller.business.hibernate.HibernateRollerImpl");
        RefreshEntriesTask task = new RefreshEntriesTask();
        task.init(RollerFactory.getRoller(), "dummy");
        task.run();
    }
    public void init(Roller roller, String realPath) throws RollerException
    {
        this.roller = (Roller)roller;
    }
    public void run()
    {
        try
        {
            roller.begin();
            roller.getPlanetManager().refreshEntries();
            roller.commit();
            roller.release();
        }
        catch (RollerException e)
        {
            logger.error("ERROR refreshing entries", e);
        }
    }
}

