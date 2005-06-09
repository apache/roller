package org.roller.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.business.ThreadManagerImpl;
import org.roller.model.RollerFactory;
import org.roller.model.ThreadManager;
import org.roller.pojos.CommentData;

/**
 * Created on Mar 9, 2004
 * @author lance.lavandowska
 */
public class CommentSpamChecker
{
    private static Log mLogger = LogFactory.getLog(CommentSpamChecker.class);
    private Blacklist blacklist = Blacklist.getBlacklist(null,null);

    // -----------------------------------------------------------------------
    /**
     * Runs comment check on comment, sets spam flag on comment.
     */
    public void testComment(CommentData comment)
    {
        try
        {
            // by using the OR conditional it'll test each
            // one in order and fall into the body without
            // having to test each and every condition.
            // Not sure which is the optimal order to check, though.
            boolean isSpam = blacklist.isBlacklisted(comment.getUrl());
            isSpam = blacklist.isBlacklisted(comment.getContent())?true:isSpam;
            isSpam = blacklist.isBlacklisted(comment.getEmail())?true:isSpam;

            if (isSpam)
            {
                comment.setSpam(Boolean.TRUE);
                comment.save();
                RollerFactory.getRoller().commit();
            }
        }
        catch (Exception e)
        {
            mLogger.error("Processing Comment",e);
        }
        finally
        {
            RollerFactory.getRoller().release();
        }
    }
    
    // -----------------------------------------------------------------------
    /**
     * Spawns thread to run comment check.
     */
    public void testComment(CommentData comment, ThreadManager threadMgr)
    {
        try
        {
            if (threadMgr != null)
            {
                threadMgr.executeInBackground(new CommentCheckerRunnable(comment));
            }
            else
            {
                mLogger.warn("No thread manager found.");
            }
        } 
        catch (InterruptedException e) {
            mLogger.warn("Interrupted during Comment Spam check",e);
        }
    }

    // -----------------------------------------------------------------------
    /**
     * Runnable to run spam check on it's own thread.
     */
    private class CommentCheckerRunnable implements Runnable
    {
        private CommentData mComment = null;
        public CommentCheckerRunnable( CommentData comment)
        {
            mComment = comment;
        }
        public void run()
        {
            testComment(mComment);
        }
    }
}