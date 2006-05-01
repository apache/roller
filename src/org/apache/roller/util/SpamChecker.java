/*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  The ASF licenses this file to You
* under the Apache License, Version 2.0 (the "License"); you may not
* use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.  For additional information regarding
* copyright in this work, please see the NOTICE file in the top level
* directory of this distribution.
*/
package org.apache.roller.util;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.config.RollerConfig;
import org.apache.roller.config.RollerRuntimeConfig;
import org.apache.roller.pojos.CommentData;
import org.apache.roller.pojos.RefererData;
import org.apache.roller.pojos.WebsiteData;

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

    /** Test referrer URL, applying blacklist and website blacklist only if configured */
    public static boolean checkReferrer(WebsiteData website, String referrerURL) {
        if (RollerConfig.getBooleanProperty("site.blacklist.enable.referrers")) {
            List stringRules = new ArrayList();
            List regexRules = new ArrayList();
            Blacklist.populateSpamRules(
                website.getBlacklist(), stringRules, regexRules, null);
            if (RollerRuntimeConfig.getProperty("spam.blacklist") != null) {
                Blacklist.populateSpamRules(
                    RollerRuntimeConfig.getProperty("spam.blacklist"), stringRules, regexRules, null);
            }
            return Blacklist.matchesRulesOnly(referrerURL, stringRules, regexRules);
        }
        return false;
    }

    /** Test comment against built in blacklist, site blacklist and website blacklist */
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

