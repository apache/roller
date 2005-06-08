/* * Created on Mar 10, 2004 */package org.roller.presentation;import org.roller.util.Blacklist;import java.util.TimerTask;/** * @author lance.lavandowska */public class BlacklistUpdateTask extends TimerTask
{        private Blacklist blacklist;    
    public BlacklistUpdateTask(String uploadDir)
    {
        // load Blacklist from file
        blacklist = Blacklist.getBlacklist(uploadDir);        
        // now have it check for an update
        blacklist.checkForUpdate();
    }
    public void run() 
    {
        // try reading new def from URL
        blacklist.checkForUpdate();
    }
}
