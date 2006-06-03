/*
 * Created on Mar 10, 2004
 */
package org.roller.presentation;

import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ScheduledTask;
import org.roller.util.Blacklist;

/**
 * Update MT Blacklist if needed.
 *
 * @author Allen Gilliland
 */
public class BlacklistUpdateTask extends TimerTask implements ScheduledTask {
    
    private static Log mLogger = LogFactory.getLog(BlacklistUpdateTask.class);
    
    
    /**
     * Task init.
     */
    public void init(Roller roller, String realPath) throws RollerException {
        mLogger.debug("initing");
    }
    
    
    /**
     * Excecute the task.
     */
    public void run() {
        
        mLogger.info("task started");

        Blacklist.checkForUpdate();
        
        mLogger.info("task completed");
    }
    
    
    /**
     * Main method so that this task may be run from outside the webapp.
     */
    public static void main(String[] args) throws Exception {
        try {            
            // NOTE: if this task is run externally from the Roller webapp then
            // all it will really be doing is downloading the MT blacklist file
            BlacklistUpdateTask task = new BlacklistUpdateTask();
            task.init(null, null);
            task.run();
            System.exit(0);
        } catch (RollerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}
