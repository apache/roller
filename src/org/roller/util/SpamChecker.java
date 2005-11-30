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
    private Blacklist blacklist = Blacklist.getBlacklist();
    
    /** Test comment, applying blacklist if configured */
    public boolean checkComment(CommentData comment) {
        if (RollerConfig.getBooleanProperty("site.blacklist.enable.comments")) {
            return testComment(comment);
        }
        return false;
    }
    
    /** Test trackback comment, applying blacklist if configured */
    public boolean checkTrackback(CommentData comment) {
        if (RollerConfig.getBooleanProperty("site.blacklist.enable.trackbacks")) {
            return testComment(comment);
        }
        return false;
    }

    /** Test comment against built blacklist only */
    public boolean checkReferrer(RefererData referrer) {
        boolean ret = false;        
        List stringRules = new ArrayList();
        List regexRules = new ArrayList();
        WebsiteData website = referrer.getWebsite();
        populateSpamRules(
            website.getBlacklist(), stringRules, regexRules, 
            RollerRuntimeConfig.getProperty("spam.blacklist")); 
        // the blacklist.matches() (doesn't use the built-in blacklist)
        if (Blacklist.matchesRulesOnly(referrer.getRefererUrl(), stringRules, regexRules)) {
            ret = true;
        }
        return ret;
    }

    /** Test comment against built in blacklist + blacklist */
    private boolean testComment(CommentData c) {
        boolean ret = false;
        List stringRules = new ArrayList();
        List regexRules = new ArrayList();
        WebsiteData website = c.getWeblogEntry().getWebsite();
        populateSpamRules(
            website.getBlacklist(), stringRules, regexRules, 
            RollerRuntimeConfig.getProperty("spam.blacklist"));
        if (   blacklist.isBlacklisted(c.getUrl(),     stringRules, regexRules)
            || blacklist.isBlacklisted(c.getEmail(),   stringRules, regexRules)
            || blacklist.isBlacklisted(c.getName(),    stringRules, regexRules)
            || blacklist.isBlacklisted(c.getContent(), stringRules, regexRules)) {
            ret = true;
        }
        return ret;
    }
        
    public static void populateSpamRules(
        String blacklist, List stringRules, List regexRules, String addendum) {
        String weblogWords = blacklist;
        weblogWords = (weblogWords == null) ? "" : weblogWords;
        String siteWords = (addendum != null) ? addendum : "";
        StringTokenizer toker = new StringTokenizer(siteWords + weblogWords,"\n");
        while (toker.hasMoreTokens()) {
            String token = toker.nextToken().trim();
            if (token.startsWith("#")) continue;
            if (token.startsWith("(")) {
                regexRules.add(Pattern.compile(token));
            } else {
                stringRules.add(token);
            }
        }        
    }
}

