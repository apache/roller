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
/* Created on Nov 11, 2003 */
package org.apache.roller.weblogger.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.apache.roller.weblogger.config.RollerConfig;
import org.apache.commons.lang.StringUtils;

/**
 * Loads MT-Blacklist style blacklist from disk and allows callers to test
 * strings against the blacklist and (optionally) addition blacklists.
 * <br />
 * First looks for blacklist.txt in uploads directory, than in classpath 
 * as /blacklist.txt. Download from web feature disabed.
 * <br />
 * Blacklist is formatted one entry per line. 
 * Any line that begins with # is considered to be a comment. 
 * Any line that begins with ( is considered to be a regex expression. 
 * <br />
 * For more information on the (discontinued) MT-Blacklist service:
 * http://www.jayallen.org/projects/mt-blacklist. 
 *
 * @author Lance Lavandowska
 * @author Allen Gilliland
 */
public class Blacklist {
    
    private static Log mLogger = LogFactory.getLog(Blacklist.class);
    
    private static Blacklist blacklist;
    private static final String blacklistFile = "blacklist.txt";
    private static final String lastUpdateStr = "Last update:";

    /** We no longer have a blacklist update URL */
    private static final String blacklistURL = null; 

    private Date lastModified = null;
    private List blacklistStr = new LinkedList();
    private List blacklistRegex = new LinkedList();
    
    // setup our singleton at class loading time
    static {
        mLogger.info("Initializing MT Blacklist");
        blacklist = new Blacklist();
        blacklist.loadBlacklistFromFile(null);
    }
    
    /** Hide constructor */
    private Blacklist() {
    }
      
    /** Singleton factory method. */
    public static Blacklist getBlacklist() {
        return blacklist;
    }
    
    /** Updated MT blacklist if necessary. */
    public static void checkForUpdate() {
        getBlacklist().update();
    }
    
    /** Non-Static update method. */
    public void update() {
        if (this.blacklistURL != null) {
            boolean blacklist_updated = this.downloadBlacklist();
            if (blacklist_updated) {
                this.loadBlacklistFromFile(null);
            }
        }
    }
        
    /** Download the MT blacklist from the web to our uploads directory. */
    private boolean downloadBlacklist() {
        
        boolean blacklist_updated = false;
        try {
            mLogger.debug("Attempting to download MT blacklist");
            
            URL url = new URL(blacklistURL);
            HttpURLConnection connection = 
                    (HttpURLConnection) url.openConnection();
            
            // after spending way too much time debugging i've discovered
            // that the blacklist server is selective based on the User-Agent
            // header.  without this header set i always get a 403 response :(
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            if (this.lastModified != null) {
                connection.setRequestProperty("If-Modified-Since",
                        DateUtil.formatRfc822(this.lastModified));
            }
            
            int responseCode = connection.getResponseCode();
            
            mLogger.debug("HttpConnection response = "+responseCode);
            
            // did the connection return NotModified? If so, no need to parse
            if (responseCode == HttpURLConnection.HTTP_NOT_MODIFIED) {
                mLogger.debug("MT blacklist site says we are current");
                return false;
            }
            
            // did the connection return a LastModified header?
            long lastModifiedLong = 
                    connection.getHeaderFieldDate("Last-Modified", -1);
            
            // if the file is newer than our current then we need do update it
            if (responseCode == HttpURLConnection.HTTP_OK &&
                    (this.lastModified == null ||
                    this.lastModified.getTime() < lastModifiedLong)) {

                mLogger.debug("my last modified = "+this.lastModified.getTime());
                mLogger.debug("MT last modified = "+lastModifiedLong);
                
                // save the new blacklist
                InputStream instream = connection.getInputStream();
                
                String uploadDir = RollerConfig.getProperty("uploads.dir");
                String path = uploadDir + File.separator + blacklistFile;
                FileOutputStream outstream = new FileOutputStream(path);
                
                mLogger.debug("writing updated MT blacklist to "+path);
                
                // read from url and write to file
                byte[] buf = new byte[4096];
                int length = 0;
                while((length = instream.read(buf)) > 0)
                    outstream.write(buf, 0, length);
                
                outstream.close();
                instream.close();
                
                blacklist_updated = true;
                
                mLogger.debug("MT blacklist download completed.");
                
            } else {
                mLogger.debug("blacklist *NOT* saved, assuming we are current");
            }
            
        } catch (Exception e) {
            mLogger.error("error downloading blacklist", e);
        }
        
        return blacklist_updated;
    }
        
    /**
     * Load the MT blacklist from the file system.
     * We look for a previously downloaded version of the blacklist first and
     * if it's not found then we load the default blacklist packed with Roller.
     * Only public for purposes of unit testing.
     */
    public void loadBlacklistFromFile(String blacklistFilePath) {
        
        InputStream txtStream = null;
        try {
            String path = blacklistFilePath;
            if (path == null) {
                String uploadDir = RollerConfig.getProperty("uploads.dir");
                path = uploadDir + File.separator + blacklistFile;
            }
            File blacklistFile = new File(path);
            
            // check our lastModified date to see if we need to re-read the file
            if (this.lastModified != null &&
                    this.lastModified.getTime() >= blacklistFile.lastModified()) {               
                mLogger.debug("Blacklist is current, no need to load again");
                return;
            } else {
                this.lastModified = new Date(blacklistFile.lastModified());
            }           
            txtStream = new FileInputStream(blacklistFile);           
            mLogger.info("Loading blacklist from "+path);
            
        } catch (Exception e) {
            // Roller keeps a copy in the webapp just in case
            txtStream = getClass().getResourceAsStream("/blacklist.txt");           
            mLogger.warn(
                "Couldn't find downloaded blacklist, loaded blacklist.txt from classpath instead");
        }
        
        if (txtStream != null) {
            readFromStream(txtStream, false);
        } else {
            mLogger.error("Couldn't load a blacklist file from anywhere, "
                        + "this means blacklist checking is disabled for now.");
        }
        mLogger.info("Number of blacklist string rules: "+blacklistStr.size());
        mLogger.info("Number of blacklist regex rules: "+blacklistRegex.size());
    }
       
    /**
     * Read in the InputStream for rules.
     * @param txtStream
     */
    private String readFromStream(InputStream txtStream, boolean saveStream) {
        String line;
        StringBuffer buf = new StringBuffer();
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader( txtStream, "UTF-8" ) );
            while ((line = in.readLine()) != null) {
                if (line.startsWith("#")) {
                    readComment(line);
                } else {
                    readRule(line);
                }
                
                if (saveStream) buf.append(line).append("\n");
            }
        } catch (Exception e) {
            mLogger.error(e);
        } finally {
            try {
                if (in != null) in.close();
            } catch (IOException e1) {
                mLogger.error(e1);
            }
        }
        return buf.toString();
    }
    
    private void readRule(String str) {
        if (StringUtils.isEmpty(str)) return; // bad condition
        
        String rule = str.trim();
        
        if (str.indexOf("#") > 0) // line has a comment
        {
            int commentLoc = str.indexOf("#");
            rule = str.substring(0, commentLoc-1).trim(); // strip comment
        }
        
        if (rule.indexOf( "(" ) > -1) // regex rule
        {
            // pre-compile patterns since they will be frequently used
            blacklistRegex.add(Pattern.compile(rule));
        } else if (StringUtils.isNotEmpty(rule)) {
            blacklistStr.add(rule);
        }
    }
        
    /** Read comment and try to parse out "Last update" value */
    private void readComment(String str) {
        int lastUpdatePos = str.indexOf(lastUpdateStr);
        if (lastUpdatePos > -1) {
            str = str.substring(lastUpdatePos + lastUpdateStr.length());
            str = str.trim();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                lastModified = DateUtil.parse(str, sdf);
            } catch (ParseException e) {
                mLogger.debug("ParseException reading " + str);
            }
        }
    }
       
    /** 
     * Does the String argument match any of the rules in the built-in blacklist? 
     */
    public boolean isBlacklisted(String str) {
        return isBlacklisted(str, null, null);
    }
    
    /** 
     * Does the String argument match any of the rules in the built-in blacklist
     * plus additional blacklists provided by caller?
     * @param str             String to be checked against blacklist
     * @param moreStringRules Additional string rules to consider
     * @param moreRegexRules  Additional regex rules to consider 
     */
    public boolean isBlacklisted(
         String str, List moreStringRules, List moreRegexRules) {
        if (str == null || StringUtils.isEmpty(str)) return false;

        // First iterate over blacklist, doing indexOf.
        // Then iterate over blacklistRegex and test.
        // As soon as there is a hit in either case return true
        
        // test plain String.indexOf
        List stringRules = blacklistStr;
        if (moreStringRules != null && moreStringRules.size() > 0) {
            stringRules = new ArrayList();
            stringRules.addAll(moreStringRules);
            stringRules.addAll(blacklistStr);
        }
        if (testStringRules(str, stringRules)) return true;
        
        // test regex blacklisted
        List regexRules = blacklistRegex;
        if (moreRegexRules != null && moreRegexRules.size() > 0) {
            regexRules = new ArrayList();
            regexRules.addAll(moreRegexRules);
            regexRules.addAll(blacklistRegex);
        }
        return testRegExRules(str, regexRules);
    }      

    /** 
     * Test string only against rules provided by caller, NOT against built-in blacklist.
     * @param str             String to be checked against rules
     * @param moreStringRules String rules to consider
     * @param moreRegexRules  Regex rules to consider 
     */
    public static boolean matchesRulesOnly(
        String str, List stringRules, List regexRules) {
        if (testStringRules(str, stringRules)) return true;
        return testRegExRules(str, regexRules);  
    }
        
    /** Test String against the RegularExpression rules. */
    private static boolean testRegExRules(String str, List regexRules) {
        boolean hit = false;
        Pattern testPattern = null;
        Iterator iter = regexRules.iterator();
        while (iter.hasNext()) {
            testPattern = (Pattern)iter.next();
            
            // want to see what it is matching on, but only in debug mode
            if (mLogger.isDebugEnabled()) {
                Matcher matcher = testPattern.matcher(str);
                if (matcher.find()) {
                    mLogger.debug(matcher.group() 
                         + " matched by " + testPattern.pattern());
                    return true;
                }
            } else {
                if (testPattern.matcher(str).find()) {
                    return true;
                }
            }
        }
        return hit;
    }

    /**
     * Tests the source text against the String rules. Each String rule is
     * first treated as a word-boundary, case insensitive regular expression.
     * If a PatternSyntaxException is encountered, a simple contains test
     * is performed.
     *
     * @param source The text in which to apply the matching rules.
     * @param rules A list a simple matching rules.
     *
     * @return true if a match was found, otherwise false
     */
    private static boolean testStringRules(String source, List rules) {
        boolean matches = false;
        
        for (Object ruleObj : rules) {
            String rule;
            rule = (String) ruleObj;

            try {
                StringBuilder patternBuilder;
                patternBuilder = new StringBuilder();
                patternBuilder.append("\\b(");
                patternBuilder.append(rule);
                patternBuilder.append(")\\b");

                Pattern pattern;
                pattern = Pattern.compile(patternBuilder.toString(),
                        Pattern.CASE_INSENSITIVE);

                Matcher matcher;
                matcher = pattern.matcher(source);

                matches = matcher.find();
                if (matches) {
                    break;
                }
            }
            catch (PatternSyntaxException e) {
                matches = source.contains(rule);
                if (matches) {
                    break;
                }
            }
            finally {
                if (matches) {
                    // Log the matched rule in debug mode
                    if (mLogger.isDebugEnabled()) {
                        mLogger.debug("matched:" + rule + ":");
                    }
                }
            }
        }
        
        return matches;
    }   
    
    /** Utility method to populate lists based a blacklist in string form */
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
        
    /** Return pretty list of String and RegEx rules. */
    public String toString() {
        StringBuffer buf = new StringBuffer("blacklist ");
        buf.append(blacklistStr).append("\n");
        buf.append("Regex blacklist ").append(blacklistRegex);
        return buf.toString();
    }
}
