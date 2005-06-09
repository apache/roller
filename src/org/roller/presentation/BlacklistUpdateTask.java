/*
 * Created on Mar 10, 2004
 */
package org.roller.presentation;

import java.util.TimerTask;

import org.roller.RollerException;
import org.roller.model.Roller;
import org.roller.model.RollerFactory;
import org.roller.model.ScheduledTask;
import org.roller.util.Blacklist;

/**
 * @author lance.lavandowska
 */
public class BlacklistUpdateTask extends TimerTask implements ScheduledTask
{
    public void run() 
    {
        // try reading new def from URL
        Blacklist.checkForUpdate();
    }
    public void init(Roller roller, String realPath) throws RollerException
    {
        // load Blacklist from file
        String uploadDir = RollerFactory.getRoller().getFileManager().getUploadDir();
        Blacklist.getBlacklist(null, uploadDir);
        // now have it check for an update
        Blacklist.checkForUpdate();
    }
}
