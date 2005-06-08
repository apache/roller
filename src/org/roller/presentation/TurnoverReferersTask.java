/*
 * Created on Aug 16, 2003
 */
package org.roller.presentation;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.RollerException;
import org.roller.model.RefererManager;
import org.roller.model.RollerFactory;
import org.roller.util.DateUtil;

import java.util.TimerTask;

/**
 * @author aim4min
 */
public class TurnoverReferersTask extends TimerTask {
    
    private static Log mLogger = 
        LogFactory.getFactory().getInstance(TurnoverReferersTask.class);    
    private RefererManager refManager = null;
    
    long period = DateUtil.millisInDay;

    public TurnoverReferersTask(RefererManager refManager) {
        this.refManager=refManager;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    public void run() {
        if (refManager!=null)
            try {
                RollerFactory.getRoller().begin();
                refManager.checkForTurnover(false,null);
                RollerFactory.getRoller().commit();
                RollerFactory.getRoller().release();
            } catch (RollerException e) {
                mLogger.error("Error while checking for referer turnover",e);
            }
    }

}
