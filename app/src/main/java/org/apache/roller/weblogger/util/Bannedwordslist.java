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
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.roller.util.RollerConstants;
import org.apache.roller.weblogger.config.WebloggerConfig;
import org.apache.commons.lang3.StringUtils;
import org.apache.roller.util.DateUtil;

/**
 * Loads MT-Bannedwordslist style bannedwordslist from disk and allows callers to test
 * strings against the bannedwordslist and (optionally) addition bannedwordslists.
 * <br />
 * First looks for bannedwordslist.txt in uploads directory, than in classpath
 * as /bannedwordslist.txt. Download from web feature disabled.
 * <br />
 * Bannedwordslist is formatted one entry per line.
 * Any line that begins with # is considered to be a comment. 
 * Any line that begins with ( is considered to be a regex expression. 
 * <br />
 * For more information on the (discontinued) MT-Bannedwordslist service:
 * http://www.jayallen.org/projects/mt-bannedwordslist.
 *
 * @author Lance Lavandowska
 * @author Allen Gilliland
 */
public final class Bannedwordslist {
    
    private static Log mLogger = LogFactory.getLog(Bannedwordslist.class);
    
    private static Bannedwordslist bannedwordslist;
    private static final String BANNEDWORDSLIST_FILE = "bannedwordslist.txt";
    private static final String LAST_UPDATE_STR = "Last update:";

    /** We no longer have a bannedwordslist update URL */
    private static final String BANNEDWORDSLIST_URL = null;

    private Date lastModified = null;
    private List<String> bannedwordslistStr = new LinkedList<String>();
    private List<Pattern> bannedwordslistRegex = new LinkedList<Pattern>();
    
    // setup our singleton at class loading time
    static {
        mLogger.info("Initializing MT Bannedwordslist");
        bannedwordslist = new Bannedwordslist();
        bannedwordslist.loadBannedwordslistFromFile(null);
    }
    
    /** Hide constructor */
    private Bannedwordslist() {
    }
      
    /** Singleton factory method. */
    public static Bannedwordslist getBannedwordslist() {
        return bannedwordslist;
    }
    
    /** Non-Static update method. */
    public void update() {
        if (BANNEDWORDSLIST_URL != null) {
            boolean bannedwordslist_updated = this.downloadBannedwordslist();
            if (bannedwordslist_updated) {
                this.loadBannedwordslistFromFile(null);
            }
        }
    }
        
    /** Download the MT bannedwordslist from the web to our uploads directory. */
    private boolean downloadBannedwordslist() {
        
        boolean bannedwordslistUpdated = false;
        try {
            mLogger.debug("Attempting to download MT bannedwordslist");
            
            URL url = new URL(BANNEDWORDSLIST_URL);
            HttpURLConnection connection = 
                    (HttpURLConnection) url.openConnection();
            
            // after spending way too much time debugging i've discovered
            // that the bannedwordslist server is selective based on the User-Agent
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
                mLogger.debug("MT bannedwordslist site says we are current");
                return false;
            }
            
            // did the connection return a LastModified header?
            long lastModifiedLong = 
                    connection.getHeaderFieldDate("Last-Modified", -1);
            
            // if the file is newer than our current then we need do update it
            if (responseCode == HttpURLConnection.HTTP_OK &&
                    (this.lastModified == null ||
                    this.lastModified.getTime() < lastModifiedLong)) {

                mLogger.debug("my last modified = " + (this.lastModified == null ? "(null)" :
                        this.lastModified.getTime()));
                mLogger.debug("MT last modified = " + lastModifiedLong);
                
                // save the new bannedwordslist
                InputStream instream = connection.getInputStream();
                
                String uploadDir = WebloggerConfig.getProperty("uploads.dir");
                String path = uploadDir + File.separator + BANNEDWORDSLIST_FILE;
                FileOutputStream outstream = new FileOutputStream(path);
                
                mLogger.debug("writing updated MT bannedwordslist to "+path);
                
                // read from url and write to file
                byte[] buf = new byte[RollerConstants.FOUR_KB_IN_BYTES];
                int length;
                while((length = instream.read(buf)) > 0) {
                    outstream.write(buf, 0, length);
                }
                
                outstream.close();
                instream.close();
                
                bannedwordslistUpdated = true;
                
                mLogger.debug("MT bannedwordslist download completed.");
                
            } else {
                mLogger.debug("bannedwordslist *NOT* saved, assuming we are current");
            }
            
        } catch (Exception e) {
            mLogger.error("error downloading bannedwordslist", e);
        }
        
        return bannedwordslistUpdated;
    }
        
    /**
     * Load the MT bannedwordslist from the file system.
     * We look for a previously downloaded version of the bannedwordslist first and
     * if it's not found then we load the default bannedwordslist packed with Roller.
     * Only public for purposes of unit testing.
     */
    public void loadBannedwordslistFromFile(String bannedwordslistFilePath) {
        
        InputStream txtStream;
        try {
            String path = bannedwordslistFilePath;
            if (path == null) {
                String uploadDir = WebloggerConfig.getProperty("uploads.dir");
                path = uploadDir + File.separator + BANNEDWORDSLIST_FILE;
            }
            File bannedwordslistFile = new File(path);
            
            // check our lastModified date to see if we need to re-read the file
            if (this.lastModified != null &&
                    this.lastModified.getTime() >= bannedwordslistFile.lastModified()) {
                mLogger.debug("Bannedwordslist is current, no need to load again");
                return;
            } else {
                this.lastModified = new Date(bannedwordslistFile.lastModified());
            }           
            txtStream = new FileInputStream(bannedwordslistFile);
            mLogger.info("Loading bannedwordslist from "+path);
            
        } catch (Exception e) {
            // Roller keeps a copy in the webapp just in case
            txtStream = getClass().getResourceAsStream("/bannedwordslist.txt");
            mLogger.warn(
                "Couldn't find downloaded bannedwordslist, loaded bannedwordslist.txt from classpath instead");
        }
        
        if (txtStream != null) {
            readFromStream(txtStream, false);
        } else {
            mLogger.error("Couldn't load a bannedwordslist file from anywhere, "
                        + "this means bannedwordslist checking is disabled for now.");
        }
        mLogger.info("Number of bannedwordslist string rules: "+bannedwordslistStr.size());
        mLogger.info("Number of bannedwordslist regex rules: "+bannedwordslistRegex.size());
    }
       
    /**
     * Read in the InputStream for rules.
     * @param txtStream stream to read from
     */
    private String readFromStream(InputStream txtStream, boolean saveStream) {
        String line;
        StringBuilder buf = new StringBuilder();
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
                
                if (saveStream) {
                    buf.append(line).append("\n");
                }
            }
        } catch (Exception e) {
            mLogger.error(e);
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e1) {
                mLogger.error(e1);
            }
        }
        return buf.toString();
    }
    
    private void readRule(String str) {
        // check for bad condition
        if (StringUtils.isEmpty(str)) {
            return;
        }
        
        String rule = str.trim();

        // line has a comment?
        if (str.indexOf('#') > 0) {
            int commentLoc = str.indexOf('#');
            // strip comment
            rule = str.substring(0, commentLoc-1).trim();
        }

        // regex rule?
        if (rule.indexOf( '(' ) > -1) {
            // pre-compile patterns since they will be frequently used
            bannedwordslistRegex.add(Pattern.compile(rule));
        } else if (StringUtils.isNotEmpty(rule)) {
            bannedwordslistStr.add(rule);
        }
    }
        
    /** Read comment and try to parse out "Last update" value */
    private void readComment(String str) {
        int lastUpdatePos = str.indexOf(LAST_UPDATE_STR);
        if (lastUpdatePos > -1) {
            str = str.substring(lastUpdatePos + LAST_UPDATE_STR.length());
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
     * Does the String argument match any of the rules in the built-in bannedwordslist?
     */
    public boolean isBannedwordslisted(String str) {
        return isBannedwordslisted(str, null, null);
    }
    
    /** 
     * Does the String argument match any of the rules in the built-in bannedwordslist
     * plus additional bannedwordslists provided by caller?
     * @param str             String to be checked against bannedwordslist
     * @param moreStringRules Additional string rules to consider
     * @param moreRegexRules  Additional regex rules to consider 
     */
    public boolean isBannedwordslisted(
         String str, List<String> moreStringRules, List<Pattern> moreRegexRules) {
        if (str == null || StringUtils.isEmpty(str)) {
            return false;
        }

        // First iterate over bannedwordslist, doing indexOf.
        // Then iterate over bannedwordslistRegex and test.
        // As soon as there is a hit in either case return true
        
        // test plain String.indexOf
        List<String> stringRules = bannedwordslistStr;
        if (moreStringRules != null && moreStringRules.size() > 0) {
            stringRules = new ArrayList<String>();
            stringRules.addAll(moreStringRules);
            stringRules.addAll(bannedwordslistStr);
        }
        if (testStringRules(str, stringRules)) {
            return true;
        }
        
        // test regex bannedwordslisted
        List<Pattern> regexRules = bannedwordslistRegex;
        if (moreRegexRules != null && moreRegexRules.size() > 0) {
            regexRules = new ArrayList<Pattern>();
            regexRules.addAll(moreRegexRules);
            regexRules.addAll(bannedwordslistRegex);
        }
        return testRegExRules(str, regexRules);
    }      

    /** 
     * Test string only against rules provided by caller, NOT against built-in bannedwordslist.
     * @param str             String to be checked against rules
     * @param stringRules String rules to consider
     * @param regexRules  Regex rules to consider
     */
    public static boolean matchesRulesOnly(
        String str, List<String> stringRules, List<Pattern> regexRules) {
        return testStringRules(str, stringRules) || testRegExRules(str, regexRules);
    }
        
    /** Test String against the RegularExpression rules. */
    private static boolean testRegExRules(String str, List<Pattern> regexRules) {
        for (Pattern testPattern : regexRules) {
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
        return false;
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
                if (matches && mLogger.isDebugEnabled()) {
                    // Log the matched rule in debug mode
                    mLogger.debug("matched:" + rule + ":");
                }
            }
        }
        
        return matches;
    }   
    
    /** Utility method to populate lists based a bannedwordslist in string form */
    public static void populateSpamRules(
        String bannedwordslist, List<String> stringRules, List<Pattern> regexRules, String addendum) {
        String weblogWords = bannedwordslist;
        weblogWords = (weblogWords == null) ? "" : weblogWords;
        String siteWords = (addendum != null) ? addendum : "";
        StringTokenizer toker = new StringTokenizer(siteWords + "\n" + weblogWords, "\n");
        while (toker.hasMoreTokens()) {
            String token = toker.nextToken().trim();
            if (token.startsWith("#")) {
                continue;
            }
            if (token.startsWith("(")) {
                regexRules.add(Pattern.compile(token));
            } else {
                stringRules.add(token);
            }
        }        
    }
        
    /** Return pretty list of String and RegEx rules. */
    public String toString() {
        String val = "bannedwordslist " + bannedwordslistStr;
        val += "\nRegex bannedwordslist " + bannedwordslistRegex;
        return val;
    }
}
