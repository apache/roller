/*
 * Created on Aug 16, 2003
 */
package org.roller.presentation;

import java.util.TimerTask;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.RefererManager;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ScheduledTask;


/**
 * Reset referer counts.
 *
 * @author Allen Gilliland
 */
public class TurnoverReferersTask extends TimerTask implements ScheduledTask {
    
    private static Log mLogger = LogFactory.getLog(TurnoverReferersTask.class);
    
    
    /**
     * Task init.
     */
    public void init(Roller roller, String realPath) throws RollerException {
        mLogger.debug("initing");
    }
    
    
    /**
     * Execute the task.
     */
    public void run() {
        
        mLogger.info("task started");
        
        try {
            Roller roller = RollerFactory.getRoller();
            roller.begin();
            roller.getRefererManager().checkForTurnover(true, null);
            roller.commit();
            roller.release();
            mLogger.info("task completed");   
            
        } catch (RollerException e) {
            mLogger.error("Error while checking for referer turnover", e);
        } catch (Exception ee) {
            mLogger.error("unexpected exception", ee);
        }


    }
    
    
    /**
     * Main method so that this task may be run from outside the webapp.
     */
    public static void main(String[] args) throws Exception {
        try {            
            TurnoverReferersTask task = new TurnoverReferersTask();
            task.init(null, null);
            task.run();
            System.exit(0);
        } catch (RollerException ex) {
            ex.printStackTrace();
            System.exit(-1);
        }
    }
    
}