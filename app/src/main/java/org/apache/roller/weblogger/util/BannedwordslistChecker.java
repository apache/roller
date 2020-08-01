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
package org.apache.roller.weblogger.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.roller.weblogger.config.WebloggerRuntimeConfig;
import org.apache.roller.weblogger.pojos.WeblogEntryComment;
import org.apache.roller.weblogger.pojos.Weblog;

/**
 * Checks comment, trackbacks and referrers for spam.
 * @author Lance Lavandowska
 * @author Dave Johnson
 */
public final class BannedwordslistChecker {

    private BannedwordslistChecker() {
        // never instantiable
        throw new AssertionError();
    }

    /**
     * Test comment, applying all bannedwordslists, if configured
     * @return True if comment matches bannedwordslist term
     */
    public static boolean checkComment(WeblogEntryComment comment) {
        if (WebloggerConfig.getBooleanProperty("site.bannedwordslist.enable.comments")) {
            return testComment(comment);
        }
        return false;
    }
    
    /** 
     * Test trackback comment, applying all bannedwordslists, if configured
     * @return True if comment matches bannedwordslist term
     */
    public static boolean checkTrackback(WeblogEntryComment comment) {
        if (WebloggerConfig.getBooleanProperty("site.bannedwordslist.enable.trackbacks")) {
            return testComment(comment);
        }
        return false;
    }

    /** 
     * Test referrer URL, applying bannedwordslist and website bannedwordslist only if configured
     * @return True if comment matches bannedwordslist term
     */
    public static boolean checkReferrer(Weblog website, String referrerURL) {
        if (WebloggerConfig.getBooleanProperty("site.bannedwordslist.enable.referrers")) {
            List<String> stringRules = new ArrayList<String>();
            List<Pattern> regexRules = new ArrayList<Pattern>();
            Bannedwordslist.populateSpamRules(
                website.getBannedwordslist(), stringRules, regexRules, null);
            if (WebloggerRuntimeConfig.getProperty("spam.bannedwordslist") != null) {
                Bannedwordslist.populateSpamRules(
                    WebloggerRuntimeConfig.getProperty("spam.bannedwordslist"), stringRules, regexRules, null);
            }
            return Bannedwordslist.matchesRulesOnly(referrerURL, stringRules, regexRules);
        }
        return false;
    }

    /** 
     * Test comment against built in bannedwordslist, site bannedwordslist and website bannedwordslist
     * @return True if comment matches bannedwordslist term
     */
    private static boolean testComment(WeblogEntryComment c) {
        boolean ret = false;
        List<String> stringRules = new ArrayList<String>();
        List<Pattern> regexRules = new ArrayList<Pattern>();
        Weblog website = c.getWeblogEntry().getWebsite();
        Bannedwordslist.populateSpamRules(
            website.getBannedwordslist(), stringRules, regexRules,
            WebloggerRuntimeConfig.getProperty("spam.bannedwordslist"));
        Bannedwordslist bannedwordslist = Bannedwordslist.getBannedwordslist();
        if (   bannedwordslist.isBannedwordslisted(c.getUrl(),     stringRules, regexRules)
            || bannedwordslist.isBannedwordslisted(c.getEmail(),   stringRules, regexRules)
            || bannedwordslist.isBannedwordslisted(c.getName(),    stringRules, regexRules)
            || bannedwordslist.isBannedwordslisted(c.getContent(), stringRules, regexRules)) {
            ret = true;
        }
        return ret;
    }        
}

