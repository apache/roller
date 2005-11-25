/*
 * Created on Nov 11, 2003
 */
package org.roller.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.roller.config.RollerConfig;


/**
 * Based on the list provided by Jay Allen for MT-Blacklist:
 *  http://www.jayallen.org/projects/mt-blacklist/
 *
 * Will provide response whether submitted string
 * contains an item listed in the supplied blacklist.
 * This implementation does not do everything
 * MT-Blacklist does, such as the "Search &amp; De-spam mode".
 *
 * @author lance
 * @author Allen Gilliland
 */
public class Blacklist {
    
    private static Log mLogger = LogFactory.getLog(Blacklist.class);
    
    private static final String blacklistFile = "blacklist.txt";
    private static final String blacklistURL = 
            "http://www.jayallen.org/comment_spam/blacklist.txt";
    private static final String lastUpdateStr = "Last update:";

    private static Blacklist blacklist;
    
    // Non-Static attributes
    private String uploadDir = null;
    private Date lastModified = null;
    private List blacklistStr = new LinkedList();
    private List blacklistRegex = new LinkedList();
    
    
    // setup our singleton at class loading time
    static {
        mLogger.info("Initializing MT Blacklist");
        
        blacklist = new Blacklist();
        blacklist.loadBlacklistFromFile();
    }

    
    /**
     * Hide constructor
     */
    private Blacklist() {
        this.uploadDir = RollerConfig.getProperty("uploads.dir");
    }
    
    
    /**
     * Singleton factory method.
     */
    public static Blacklist getBlacklist() {
        return blacklist;
    }
    
    
    /**
     * Updated MT blacklist if necessary.
     */
    public static void checkForUpdate() {
        getBlacklist().update();
    }
    
    
    /**
     * Non-Static update method.
     */
    public void update() {
        boolean blacklist_updated = this.downloadBlacklist();
        
        if(blacklist_updated)
            this.loadBlacklistFromFile();
    }
    
    
    /**
     * Download the MT blacklist from the web to our uploads directory.
     */
    private boolean downloadBlacklist() {
        
        boolean blacklist_updated = false;
        try {
            mLogger.debug("attempting to download MT blacklist");
            
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
                
                String path = this.uploadDir + File.separator + blacklistFile;
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
     *
     * We look for a previously downloaded version of the blacklist first and
     * if it's not found then we load the default blacklist packed with Roller.
     */
    private void loadBlacklistFromFile() {
        
        InputStream txtStream = null;
        try {
            String path = this.uploadDir + File.separator + blacklistFile;
            File blacklistFile = new File(path);
            
            // check our lastModified date to see if we need to re-read the file
            if(this.lastModified != null &&
                    this.lastModified.getTime() >= blacklistFile.lastModified()) {
                
                mLogger.debug("Blacklist is current, no need to load again");
                return;
            } else {
                this.lastModified = new Date(blacklistFile.lastModified());
            }
            
            txtStream = new FileInputStream(blacklistFile);
            
            mLogger.debug("Loading blacklist from "+path);
        } catch (Exception e) {
            // Roller keeps a copy in the webapp just in case
            txtStream = getClass().getResourceAsStream("/"+blacklistFile);
            
            mLogger.debug("Couldn't find downloaded blacklist, "+
                    "loading from classpath instead");
        }
        
        if (txtStream != null) {
            readFromStream(txtStream, false);
        } else {
            mLogger.error("couldn't load a blacklist file from anywhere, "+
                    "this means blacklist checking is disabled for now.");
        }
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
    
    
    /**
     * @param str
     */
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
    
    
    /**
     * Try to parse out "Last update" value: 2004/03/08 23:17:30.
     * @param str
     */
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
     * Does the String argument match any of the rules in the blacklist?
     *
     * @param str
     * @return
     */
    public boolean isBlacklisted(String str) {
        if (str == null || StringUtils.isEmpty(str)) return false;
        
        // First iterate over blacklist, doing indexOf.
        // Then iterate over blacklistRegex and test.
        // As soon as there is a hit in either case return true
        
        // test plain String.indexOf
        if( testStringRules(str) ) return true;
        
        // test regex blacklisted
        return testRegExRules(str);
    }
    
    
    /**
     * Test String against the RegularExpression rules.
     *
     * @param str
     * @return
     */
    private boolean testRegExRules(String str) {
        boolean hit = false;
        Pattern testPattern = null;
        Iterator iter = blacklistRegex.iterator();
        while (iter.hasNext()) {
            testPattern = (Pattern)iter.next();
            
            // want to see what it is matching on
            // if we are in "debug mode"
            if (mLogger.isDebugEnabled()) {
                Matcher matcher = testPattern.matcher(str);
                if (matcher.find()) {
                    mLogger.debug(matcher.group() + " matched by " + testPattern.pattern());
                    hit = true;
                    break;
                }
            } else {
                if (testPattern.matcher(str).find()) {
                    hit = true;
                    break;
                }
            }
        }
        return hit;
    }
    
    
    /**
     * Test the String against the String rules,
     * using simple indexOf.
     *
     * @param str
     * @return
     */
    private boolean testStringRules(String str) {
        String test;
        Iterator iter = blacklistStr.iterator();
        boolean hit = false;
        while (iter.hasNext()) {
            test = (String)iter.next();
            //System.out.println("check against |" + test + "|");
            if (str.indexOf(test) > -1) {
                // want to see what it is matching on
                if (mLogger.isDebugEnabled()) {
                    mLogger.debug("matched:" + test + ":");
                }
                hit = true;
                break;
            }
        }
        return hit;
    }
    
    
    /**
     * Return pretty list of String and RegEx rules.
     */
    public String toString() {
        StringBuffer buf = new StringBuffer("blacklist ");
        buf.append(blacklistStr).append("\n");
        buf.append("Regex blacklist ").append(blacklistRegex);
        return buf.toString();
    }
}
