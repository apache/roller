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
 *
 * Source file modified from the original ASF source; all changes made
 * are also under Apache License.
 */
package org.apache.roller.weblogger.ui.rendering.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.roller.weblogger.WebloggerException;
import org.apache.roller.weblogger.business.UserManager;
import org.apache.roller.weblogger.pojos.WeblogRole;
import org.apache.roller.weblogger.ui.rendering.requests.WeblogRequest;
import org.apache.roller.weblogger.pojos.Weblog;
import org.apache.roller.weblogger.util.Utilities;

/**
 * Model which provides access to a set of general utilities.
 */
public class UtilitiesModel implements Model {
    private static Log log = LogFactory.getLog(UtilitiesModel.class);

    private UserManager userManager;

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    private WeblogRequest weblogRequest = null;

    private Weblog weblog = null;

    /** Template context name to be used for model */
    public String getModelName() {
        return "utils";
    }

    /** Init page model based on request */
    public void init(Map initData) throws WebloggerException {      
        
        // we expect the init data to contain a weblogRequest object
        weblogRequest = (WeblogRequest) initData.get("parsedRequest");

        if (weblogRequest == null) {
            throw new WebloggerException("expected weblogRequest from init data");
        }
        
        // extract weblog object if possible
        weblog = weblogRequest.getWeblog();
    }

    //---------------------------------------------------- Authentication utils

    public boolean isUserBlogPublisher(Weblog weblog) {
        try {
            if (weblogRequest.getAuthenticatedUser() != null) {
                // using handle variant of checkWeblogRole as id is presently nulled out in the templates
                return userManager.checkWeblogRole(weblogRequest.getAuthenticatedUser(), weblog.getHandle(), WeblogRole.POST);
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return false;
    }
    
    public boolean isUserBlogAdmin(Weblog weblog) {
        try {
            if (weblogRequest.getAuthenticatedUser() != null) {
                // using handle variant of checkWeblogRole as id is presently nulled out in the templates
                return userManager.checkWeblogRole(weblogRequest.getAuthenticatedUser(), weblog.getHandle(), WeblogRole.OWNER);
            }
        } catch (Exception e) {
            log.warn("ERROR: checking user authorization", e);
        }
        return false;
    }
        
    public boolean isUserAuthenticated() {
        return (weblogRequest.getAuthenticatedUser() != null);
    }
       
    public String autoformat(String s) {
        return Utilities.autoformat(s);
    }

    //-------------------------------------------------------------- Date utils
    /**
     * Return date for current time.
     */
    public static Date getNow() {
        return new Date();
    }
    
    /**
     * Format date using SimpleDateFormat format string.
     */
    public String formatDate(Date d, String fmt) {
        return formatDate(d, fmt, weblog.getTimeZoneInstance());
    }
    
    /**
     * Format date using SimpleDateFormat format string.
     */
    public String formatDate(Date d, String fmt, TimeZone tzOverride) {
        
        if (d == null || fmt == null) {
            return fmt;
        }
        
        SimpleDateFormat format = new SimpleDateFormat(fmt, weblog.getLocaleInstance());
        if (tzOverride != null) {
            format.setTimeZone(tzOverride);
        }
        
        return format.format(d);
    }
    
    /**
     * Format date in ISO-8601 format (YYYY-MM-DD)
     */
    public String formatIso8601Date(Date d) {
        return DateFormatUtils.ISO_DATETIME_TIME_ZONE_FORMAT.format(d);
    }
    
    /**
     * Return a date in RFC-822 format (EEE, dd MMM yyyy HH:mm:ss Z)
     */
    public String formatRfc822Date(Date date) {
        return DateFormatUtils.SMTP_DATETIME_FORMAT.format(date);
    }
    
    //------------------------------------------------------------ String utils
    
    // isEmpty = empty (size = 0) or null
    public boolean isEmpty(String str) {
        return StringUtils.isEmpty(str);
    }
    
    public boolean isNotEmpty(String str) {
        return StringUtils.isNotEmpty(str);
    }
    
    public String left(String str, int length) {
        return StringUtils.left(str, length);
    }

    public String right(String str, int length) {
        return StringUtils.right(str, length);
    }

    public String escapeHTML(String str) {
        return Utilities.escapeHTML(str);
    }
    
    public String unescapeHTML(String str) {
        return Utilities.unescapeHTML(str);
    }

    public String escapeXML(String str) {
        return StringEscapeUtils.escapeXml10(str);
    }

    /**
     * Remove occurrences of html, defined as any text between the characters "&lt;" and "&gt;".
     */
    public String removeHTML(String str) {
        return Utilities.removeHTML(str, false);
    }

    /**
     * Truncates based on text-only but retains HTML.
     */
    public String truncateHTML(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateHTML(str, lower, upper, appendToEnd);
    }

    /**
     * Strips HTML and truncates.
     */
    public String truncateText(String str, int lower, int upper, String appendToEnd) {
        return Utilities.truncateText(str, lower, upper, appendToEnd);
    }

    /**
     * URL encoding.
     * @param s a string to be URL-encoded
     * @return URL encoding of s using character encoding UTF-8; null if s is null.
     */
    public final String encode(String s) {
        return (s == null) ? null : Utilities.encode(s);
    }

    /**
     * URL decoding.
     * @param s a URL-encoded string to be URL-decoded
     * @return URL decoded value of s using character encoding UTF-8; null if s is null.
     */
    public final String decode(String s) {
        return (s == null) ? null : Utilities.decode(s);
    }
        
    /**
     * Code (stolen from Pebble) to add rel="nofollow" string to all links in HTML.
     */
    public String addNofollow(String html) {
        return Utilities.addNofollow(html);
    }
    
    /**
     * Transforms the given String into a subset of HTML displayable on a web
     * page. The subset includes &lt;b&gt;, &lt;i&gt;, &lt;p&gt;, &lt;br&gt;,
     * &lt;pre&gt; and &lt;a href&gt; (and their corresponding end tags).
     *
     * @param s   the String to transform
     * @return    the transformed String
     */
    public String transformToHTMLSubset(String s) {
        return Utilities.transformToHTMLSubset(s);
    }
    
}
