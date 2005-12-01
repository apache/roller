package org.roller.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.roller.config.RollerConfig;
import org.roller.config.RollerRuntimeConfig;
import org.roller.pojos.CommentData;
import org.roller.pojos.RefererData;
import org.roller.pojos.WebsiteData;

/**
 * Checks comment, trackbacks and referrers for spam.
 * @author Lance Lavandowska
 * @author Dave Johnson
 */
public class SpamChecker { 
    private static Log mLogger = LogFactory.getLog(SpamChecker.class);
    
    /** Test comment, applying all blacklists, if configured */
    public static boolean checkComment(CommentData comment) {
        if (RollerConfig.getBooleanProperty("site.blacklist.enable.comments")) {
            return testComment(comment);
        }
        return false;
    }
    
    /** Test trackback comment, applying all blacklists, if configured */
    public static boolean checkTrackback(CommentData comment) {
        if (RollerConfig.getBooleanProperty("site.blacklist.enable.trackbacks")) {
            return testComment(comment);
        }
        return false;
    }

    /** Test referrer URL, applying website blacklist only, if configured */
    public static boolean checkReferrer(WebsiteData website, String referrerURL) {
        if (RollerConfig.getBooleanProperty("site.blacklist.enable.referrers")) {
            List stringRules = new ArrayList();
            List regexRules = new ArrayList();
            Blacklist.populateSpamRules(
                website.getBlacklist(), stringRules, regexRules, null);
            return Blacklist.matchesRulesOnly(referrerURL, stringRules, regexRules);
        }
        return false;
    }

    /** Test comment against built in blacklist + blacklist */
    private static boolean testComment(CommentData c) {
        boolean ret = false;
        List stringRules = new ArrayList();
        List regexRules = new ArrayList();
        WebsiteData website = c.getWeblogEntry().getWebsite();
        Blacklist.populateSpamRules(
            website.getBlacklist(), stringRules, regexRules, 
            RollerRuntimeConfig.getProperty("spam.blacklist"));
        Blacklist blacklist = Blacklist.getBlacklist();
        if (   blacklist.isBlacklisted(c.getUrl(),     stringRules, regexRules)
            || blacklist.isBlacklisted(c.getEmail(),   stringRules, regexRules)
            || blacklist.isBlacklisted(c.getName(),    stringRules, regexRules)
            || blacklist.isBlacklisted(c.getContent(), stringRules, regexRules)) {
            ret = true;
        }
        return ret;
    }        
}

