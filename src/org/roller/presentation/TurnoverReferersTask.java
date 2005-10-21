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
 * @author aim4min
 */
public class TurnoverReferersTask extends TimerTask implements ScheduledTask
{
    private static Log mLogger = LogFactory.getFactory().getInstance(
            TurnoverReferersTask.class);
    private RefererManager refManager = null;
    
    public void init(Roller roller, String realPath) throws RollerException
    {
        refManager = roller.getRefererManager();
    }
    public void run()
    {
        if (refManager != null)
            try
            {
                RollerFactory.getRoller().begin();
                refManager.checkForTurnover(false, null);
                RollerFactory.getRoller().commit();
                RollerFactory.getRoller().release();
            }
            catch (RollerException e)
            {
                mLogger.error("Error while checking for referer turnover", e);
            }
    }
}