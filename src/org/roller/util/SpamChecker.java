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
    
    /** Test comment against built in blacklist + ignoreWords */
    private boolean testComment(CommentData c) {
        boolean ret = false;
        
        String weblogWords = c.getWeblogEntry().getWebsite().getIgnoreWords();
        weblogWords = (weblogWords == null) ? "" : weblogWords;
        String siteWords = 
                RollerRuntimeConfig.getProperty("spam.referers.ignorewords");
        siteWords = (siteWords == null) ? "" : siteWords;
        
        List stringRules = new ArrayList();
        List regexRules = new ArrayList();
        StringTokenizer toker = new StringTokenizer(siteWords + weblogWords,"\n");
        while (toker.hasMoreTokens()) {
            String token = toker.nextToken();
            if (token.startsWith("#")) continue;
            if (token.startsWith("(")) {
                regexRules.add(Pattern.compile(token));
            } else {
                stringRules.add(token);
            }
        }
        if (   blacklist.isBlacklisted(c.getUrl(),     stringRules, regexRules)
            || blacklist.isBlacklisted(c.getEmail(),   stringRules, regexRules)
            || blacklist.isBlacklisted(c.getContent(), stringRules, regexRules)) {
            c.setSpam(Boolean.TRUE);
            ret = true;
        }
        return ret;
    }
        
    /** Test comment against built ignoreWords only */
    public boolean testReferrer(RefererData referrer) {
        boolean ret = false;
        
        String weblogWords = referrer.getWebsite().getIgnoreWords();
        weblogWords = (weblogWords == null) ? "" : weblogWords;
        String siteWords = 
                RollerRuntimeConfig.getProperty("spam.referers.ignorewords");
        siteWords = (siteWords == null) ? "" : siteWords;
        
        List stringRules = new ArrayList();
        List regexRules = new ArrayList();
        StringTokenizer toker = 
            new StringTokenizer(siteWords + weblogWords,"\n");
        while (toker.hasMoreTokens()) {
            String token = toker.nextToken();
            if (token.startsWith("#")) continue;
            if (token.startsWith("(")) {
                regexRules.add(token);
            } else {
                stringRules.add(token);
            }
        }   
        // the blacklist.matches() doesn't use the built-in blacklist
        if (Blacklist.matches(
                referrer.getRefererUrl(), stringRules, regexRules)) {
            ret = true;
        }
        return ret;
    }
}

